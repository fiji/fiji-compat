package imagej.legacy.plugin;

import java.io.File;

import org.scijava.plugin.SciJavaPlugin;

/**
 * Old interface for the editor to use instead of ImageJ 1.x' limited AWT-based one.
 * 
 * <p>
 * This interface used to live in {@code ij-legacy} and is reproduced here --
 * with permission by the original author :-) -- only for backwards compatibility.
 * </p>
 * 
 * @author Johannes Schindelin
 */
@Deprecated
public interface LegacyEditor extends SciJavaPlugin {
	public boolean open(final File path);
	public boolean create(final String title, final String content);
}
