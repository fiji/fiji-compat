package fiji;

/**
 * Modify some IJ1 quirks at runtime, thanks to Javassist
 */

import imagej.legacy.LegacyExtensions;
import imagej.util.AppUtils;

import java.io.File;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

public class IJHacker extends JavassistHelper {

	@Override
	public void instrumentClasses() throws BadBytecode, CannotCompileException, NotFoundException {
		CtClass clazz;
		CtMethod method;

		// Class ij.io.Opener
		clazz = get("ij.io.Opener");

		// open text in the Fiji Editor
		method = clazz.getMethod("open", "(Ljava/lang/String;)V");
		method.insertBefore("if (isText($1) && fiji.FijiTools.maybeOpenEditor($1)) return;");

		// Class ij.plugin.DragAndDrop
		clazz = get("ij.plugin.DragAndDrop");

		handleHTTPS(clazz.getMethod("drop", "(Ljava/awt/dnd/DropTargetDropEvent;)V"));

		// Class ij.plugin.Commands
		clazz = get("ij.plugin.Commands");

		// open StartupMacros with the Script Editor
		method = clazz.getMethod("openStartupMacros", "()V");
		method.insertBefore("if (fiji.FijiTools.openStartupMacros())"
			+ "  return;");

		boolean scriptEditorStuff = true;
		if (!scriptEditorStuff) {
			// Class ij.plugin.frame.Recorder
			clazz = get("ij.plugin.frame.Recorder");

			// create new macro in the Script Editor
			method = clazz.getMethod("createMacro", "()V");
			dontReturnWhenEditorIsNull(method.getMethodInfo());
			method.instrument(new ExprEditor() {
				@Override
				public void edit(MethodCall call) throws CannotCompileException {
					if (call.getMethodName().equals("createMacro"))
						call.replace("if ($1.endsWith(\".txt\"))"
							+ "  $1 = $1.substring($1.length() - 3) + \"ijm\";"
							+ "boolean b = fiji.FijiTools.openEditor($1, $2);"
							+ "return;");
					else if (call.getMethodName().equals("runPlugIn"))
						call.replace("$_ = null;");
				}
			});
			// create new plugin in the Script Editor
			clazz.addField(new CtField(pool.get("java.lang.String"), "nameForEditor", clazz));
			method = clazz.getMethod("createPlugin", "(Ljava/lang/String;Ljava/lang/String;)V");
			method.insertBefore("this.nameForEditor = $2;");
			method.instrument(new ExprEditor() {
				@Override
				public void edit(MethodCall call) throws CannotCompileException {
					if (call.getMethodName().equals("runPlugIn"))
						call.replace("$_ = null;"
							+ "new ij.plugin.NewPlugin().createPlugin(this.nameForEditor, ij.plugin.NewPlugin.PLUGIN, $2);"
							+ "return;");
				}
			});

			// Class ij.plugin.NewPlugin
			clazz = get("ij.plugin.NewPlugin");

			// open new plugin in Script Editor
			method = clazz.getMethod("createMacro", "(Ljava/lang/String;)V");
			method.instrument(new ExprEditor() {
				@Override
				public void edit(MethodCall call) throws CannotCompileException {
					if (call.getMethodName().equals("create"))
						call.replace("if ($1.endsWith(\".txt\"))"
							+ "  $1 = $1.substring(0, $1.length() - 3) + \"ijm\";"
							+ "if ($1.endsWith(\".ijm\")) {"
							+ "  fiji.FijiTools.openEditor($1, $2);"
							+ "  return;"
							+ "}"
							+ "int options = (monospaced ? ij.plugin.frame.Editor.MONOSPACED : 0)"
							+ "  | (menuBar ? ij.plugin.frame.Editor.MENU_BAR : 0);"
							+ "new ij.plugin.frame.Editor(rows, columns, 0, options).create($1, $2);");
					else if (call.getMethodName().equals("runPlugIn"))
						call.replace("$_ = null;");
				}

				@Override
				public void edit(NewExpr expr) throws CannotCompileException {
					if (expr.getClassName().equals("ij.plugin.frame.Editor"))
						expr.replace("$_ = null;");
				}
			});
			// open new plugin in Script Editor
			method = clazz.getMethod("createPlugin", "(Ljava/lang/String;ILjava/lang/String;)V");
			dontReturnWhenEditorIsNull(method.getMethodInfo());
			method.instrument(new ExprEditor() {
				@Override
				public void edit(MethodCall call) throws CannotCompileException {
					if (call.getMethodName().equals("create"))
						call.replace("boolean b = fiji.FijiTools.openEditor($1, $2);"
							+ "return;");
					else if (call.getMethodName().equals("runPlugIn"))
						call.replace("$_ = null;");
				}

			});
		}

		// Class ij.macro.Functions
		clazz = get("ij.macro.Functions");

		handleHTTPS(clazz.getMethod("exec", "()Ljava/lang/String;"));

		// handle https:// in addition to http://
		try {
			clazz = get("ij.io.PluginInstaller");
		} catch (NotFoundException e) {
			clazz = get("ij.plugin.PluginInstaller");
		}
		handleHTTPS(clazz.getMethod("install", "(Ljava/lang/String;)Z"));

		clazz = get("ij.plugin.ListVirtualStack");
		handleHTTPS(clazz.getMethod("run", "(Ljava/lang/String;)V"));

		// If there is a macros/StartupMacros.fiji.ijm, but no macros/StartupMacros.txt, execute that
		clazz = get("ij.Menus");
		File macrosDirectory = new File(FijiTools.getFijiDir(), "macros");
		File startupMacrosFile = new File(macrosDirectory, "StartupMacros.fiji.ijm");
		if (startupMacrosFile.exists() &&
				!new File(macrosDirectory, "StartupMacros.txt").exists() &&
				!new File(macrosDirectory, "StartupMacros.ijm").exists()) {
			method = clazz.getMethod("installStartupMacroSet", "()V");
			final String startupMacrosPath = startupMacrosFile.getPath().replace("\\", "\\\\").replace("\"", "\\\"");
			method.instrument(new ExprEditor() {
				@Override
				public void edit(MethodCall call) throws CannotCompileException {
					if (call.getMethodName().equals("installFromIJJar"))
						call.replace("$0.installFile(\"" + startupMacrosPath + "\");"
							+ "nMacros += $0.getMacroCount();");
				}
			});
		}

		LegacyExtensions.setAppName("(Fiji Is Just) ImageJ");
		LegacyExtensions.setIcon(new File(AppUtils.getBaseDirectory(), "images/icon.png"));
	}

	private void dontReturnWhenEditorIsNull(MethodInfo info) throws CannotCompileException {
		CodeIterator iterator = info.getCodeAttribute().iterator();
	        while (iterator.hasNext()) try {
	                int pos = iterator.next();
			int c = iterator.byteAt(pos);
			if (c == Opcode.IFNONNULL && iterator.byteAt(pos + 3) == Opcode.RETURN) {
				iterator.writeByte(Opcode.POP, pos);
				iterator.writeByte(Opcode.NOP, pos + 1);
				iterator.writeByte(Opcode.NOP, pos + 2);
				iterator.writeByte(Opcode.NOP, pos + 3);
				return;
			}
		}
		catch (BadBytecode e) {
			throw new CannotCompileException(e);
		}
		throw new CannotCompileException("Check not found");
	}

	private void handleHTTPS(final CtMethod method) throws CannotCompileException {
		method.instrument(new ExprEditor() {
			@Override
			public void edit(MethodCall call) throws CannotCompileException {
				try {
					if (call.getMethodName().equals("startsWith") &&
							"http://".equals(getLatestArg(call, 0)))
						call.replace("$_ = $0.startsWith($1) || $0.startsWith(\"https://\");");
				} catch (BadBytecode e) {
					e.printStackTrace();
				}
			}
		});
	}

}
