package fiji;

import ij.IJ;
import ij.Menus;
import ij.Prefs;
import ij.plugin.PlugIn;

import java.awt.Menu;
import java.util.Hashtable;

public class MenuRefresher implements PlugIn, Runnable {

	/**
	 * The entry point of the {@link PlugIn}.
	 * 
	 * @param arg "update" if called from ij-legacy-patched ImageJ 1.x when the
	 *          user called <i>Help>Refresh Menus</i>.
	 */
	@Override
	public void run(final String arg) {
		run();
	}

	@Override
	public void run() {
		overrideCommands();
		SampleImageLoader.install();
		Main.installRecentCommands();
	}

	@SuppressWarnings("unchecked")
	static void overrideCommands() {
		final Hashtable<String, String> commands = Menus.getCommands();
		if (!commands.containsKey("Install PlugIn...")) {
			commands.put("Install PlugIn...", "fiji.PlugInInstaller");
			if (IJ.getInstance() != null) {
				final Menu plugins = FijiTools.getMenu("Plugins");
				if (plugins != null)
					for (int i = 0; i < plugins.getItemCount(); i++)
						if (plugins.getItem(i).getLabel().equals("-")) {
							plugins.insert("Install PlugIn...", i);
							plugins.getItem(i).addActionListener(
									IJ.getInstance());
							break;
						}
			}
		}
		commands.put("Compile and Run...", "fiji.Compile_and_Run");
		// make sure "Edit>Options>Memory & Threads runs Fiji's plugin
		commands.put("Memory & Threads...", "fiji.Memory");

		// redirect Bio-Formats's "Update LOCI Plugins" to use the ImageJ updater
		Prefs.set("bioformats.upgradeCheck", false);
		if (commands.containsKey("Update LOCI Plugins")) {
			commands.put("Update LOCI Plugins", "fiji.updater.Updater(\"update\")");
		}
	}

}
