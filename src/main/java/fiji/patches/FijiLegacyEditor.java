package fiji.patches;

import java.io.File;

import net.imagej.legacy.plugin.LegacyEditor;

import org.scijava.plugin.Plugin;

import fiji.FijiTools;

@Plugin(type = LegacyEditor.class)
public class FijiLegacyEditor implements LegacyEditor {
	public boolean open(final File file) {
		return FijiTools.openFijiEditor(file);
	}

	public boolean create(final String title, final String content) {
		return FijiTools.openFijiEditor(title, content);
	}
}
