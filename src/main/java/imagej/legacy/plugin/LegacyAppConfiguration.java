package imagej.legacy.plugin;

import java.net.URL;

import org.scijava.plugin.SciJavaPlugin;

/**
 * Old interface for configuring the legacy application appearance.
 * 
 * <p>
 * This interface used to live in {@code ij-legacy} and is reproduced here --
 * with permission by the original author :-) -- only for backwards compatibility.
 * </p>
 * 
 * @author Johannes Schindelin
 */
@Deprecated
public interface LegacyAppConfiguration extends SciJavaPlugin {

	/**
	 * Returns the application name for use with ImageJ 1.x.
	 * @return the application name
	 */
	String getAppName();

	/**
	 * Returns the icon for use with ImageJ 1.x.
	 * 
	 * @return the application name
	 */
	URL getIconURL();
}
