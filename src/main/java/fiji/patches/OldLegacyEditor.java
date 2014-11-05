package fiji.patches;

import fiji.FijiTools;
import imagej.legacy.plugin.LegacyEditor;

import java.io.File;

import org.scijava.plugin.Plugin;

/**
 * Fall-back plugin for {@code ij-legacy}.
 * <p>
 * In case of version skew (e.g. when ij-legacy.jar is available, but
 * imagej-legacy.jar is not), we need to fall back to the plugins using the old
 * package names.
 * </p>
 * <p>
 * Since it is meant as a fall-back in the absence of {@code imagej-legacy}, we
 * cannot simply extend {@code FijiLegacyEditor} because that class cannot be
 * loaded without the required imagej-legacy interfaces on the class path.
 * </p>
 * 
 * @author Johannes Schindelin
 */
@Deprecated
@Plugin(type = LegacyEditor.class)
public class OldLegacyEditor implements LegacyEditor {
	public boolean open(final File file) {
		return FijiTools.openFijiEditor(file);
	}

	public boolean create(final String title, final String content) {
		return FijiTools.openFijiEditor(title, content);
	}
}
