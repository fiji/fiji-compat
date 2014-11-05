package fiji.patches;

import fiji.Main;
import imagej.legacy.plugin.LegacyAppConfiguration;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.scijava.plugin.Plugin;
import org.scijava.util.AppUtils;

/**
 * Fall-back plugin for {@code ij-legacy}.
 * <p>
 * In case of version skew (e.g. when ij-legacy.jar is available, but
 * imagej-legacy.jar is not), we need to fall back to the plugins using the old
 * package names.
 * </p>
 * <p>
 * Since it is meant as a fall-back in the absence of {@code imagej-legacy}, we
 * cannot simply extend {@code FijiAppConfiguration} because that class cannot be
 * loaded without the required imagej-legacy interfaces on the class path.
 * </p>
 * 
 * @author Johannes Schindelin
 */
@Deprecated
@Plugin(type = LegacyAppConfiguration.class)
public class OldAppConfiguration implements
		LegacyAppConfiguration {

	final private static String appName = "(Fiji Is Just) ImageJ";
	final private static URL iconURL;

	static {
		URL url;
		try {
			final File file = new File(AppUtils.getBaseDirectory(Main.class), "images/icon.png");
			url = file.exists() ? file.toURI().toURL() : null;
		} catch (MalformedURLException e) {
			url = null;
		} finally{}
		iconURL = url;
	}

	@Override
	public String getAppName() {
		return appName;
	}

	@Override
	public URL getIconURL() {
		return iconURL;
	}

}
