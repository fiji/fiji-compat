package fiji.patches;

import imagej.legacy.plugin.LegacyAppConfiguration;

import org.scijava.plugin.Plugin;

/**
 * Fall-back plugin for {@code ij-legacy}.
 * <p>
 * In case of version skew (e.g. when ij-legacy.jar is available, but
 * imagej-legacy.jar is not), we need to fall back to the plugins using the old
 * package names.
 * </p>
 * 
 * @author Johannes Schindelin
 */
@Deprecated
@Plugin(type = LegacyAppConfiguration.class)
public class OldAppConfiguration extends FijiAppConfiguration implements
		LegacyAppConfiguration {
	// this class intentionally blank
}
