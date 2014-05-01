package fiji;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import net.imagej.patcher.LegacyInjector;

import org.scijava.util.AppUtils;

/**
 * Patch ij.jar using Javassist, handle headless mode, too.
 * 
 * @author Johannes Schindelin
 */

public class IJ1Patcher implements Runnable {
	private static boolean alreadyPatched;
	static boolean ij1PatcherFound, previousIJ1PatcherFound;

	@Override
	public void run() {
		if (alreadyPatched || "false".equals(System.getProperty("patch.ij1")))
			return;
		try {
			LegacyInjector.preinit();
			ij1PatcherFound = true;
		} catch (NoClassDefFoundError e) {
			fallBackToPreviousPatcher();
		}
		alreadyPatched = true;
	}

	private void fallBackToPreviousPatcher() {
		try {
			Thread.currentThread().setContextClassLoader(
					getClass().getClassLoader());
			final ClassPool pool = ClassPool.getDefault();

			try {
				fallBackToPreviousLegacyEnvironment(pool);
				return;
			} catch (Throwable t) {
				// ignore; fall back to previous patching method
			}

			CtClass clazz = pool.makeClass("fiji.$TransientFijiEditor");
			clazz.addInterface(pool
					.get("imagej.legacy.LegacyExtensions$LegacyEditorPlugin"));
			clazz.addConstructor(CtNewConstructor.make(new CtClass[0],
					new CtClass[0], clazz));
			clazz.addMethod(CtNewMethod.make(
					"public boolean open(java.io.File path) {"
							+ "  return fiji.FijiTools.openFijiEditor(path);"
							+ "}", clazz));
			clazz.addMethod(CtNewMethod
					.make("public boolean create(java.lang.String title, java.lang.String body) {"
							+ "  return fiji.FijiTools.openFijiEditor(title, body);"
							+ "}", clazz));
			clazz.toClass();

			compileAndRun(
					pool,
					"imagej.legacy.LegacyExtensions.setAppName(\"(Fiji Is Just) ImageJ\");"
							+ "imagej.legacy.LegacyExtensions.setIcon(new java.io.File(\""
							+ AppUtils.getBaseDirectory(Main.class)
							+ "/images/icon.png\"));"
							+ "imagej.legacy.LegacyExtensions.setLegacyEditor(new fiji.$TransientFijiEditor());"
							+
							/*
							 * make sure to run some Fiji-specific stuff after
							 * Help>Refresh Menus, e.g. installing all scripts
							 * into the menu
							 */
							"imagej.legacy.LegacyExtensions.runAfterRefreshMenus(new fiji.MenuRefresher());"
							+
							/* make sure that ImageJ2's LegacyInjector runs */
							"imagej.legacy.DefaultLegacyService.preinit();");
			return;
		} catch (NoClassDefFoundError e) {
			// ignore: probably have newer ImageJ2 in class path
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void fallBackToPreviousLegacyEnvironment(final ClassPool pool)
			throws NotFoundException, CannotCompileException,
			InstantiationException, IllegalAccessException {
		compileAndRun(
				pool,
				"imagej.patcher.LegacyInjector.preinit();"
						+ "new imagej.patcher.LegacyEnvironment(getClass().getClassLoader(),"
						+ " java.awt.GraphicsEnvironment.isHeadless());");
		previousIJ1PatcherFound = true;
	}

	static void fallBackToPreviousLegacyEnvironmentMain(final String... args)
			throws SecurityException, NoSuchMethodException,
			ClassNotFoundException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		final ClassLoader loader = Thread.currentThread()
				.getContextClassLoader();
		final Method get = loader.loadClass("imagej.patcher.LegacyEnvironment")
				.getMethod("getPatchedImageJ1");
		final Object patched = get.invoke(null);
		final Method main = patched.getClass()
				.getMethod("main", String[].class);
		main.invoke(patched, (Object) args);
	}

	private int counter = 1;

	private void compileAndRun(final ClassPool pool, final String code)
			throws NotFoundException, CannotCompileException,
			InstantiationException, IllegalAccessException {
		CtClass clazz;
		clazz = pool.makeClass("fiji.$TransientFijiPatcher" + counter++);
		clazz.addInterface(pool.get("java.lang.Runnable"));
		clazz.addMethod(CtNewMethod.make("public void run() {" + code + "}",
				clazz));
		Runnable run = (Runnable) clazz.toClass().newInstance();
		run.run();
	}
}
