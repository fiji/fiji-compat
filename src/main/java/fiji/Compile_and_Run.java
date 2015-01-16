
package fiji;

import ij.IJ;
import ij.plugin.PlugIn;

public class Compile_and_Run implements PlugIn {

	protected static String directory, fileName;

	public void run(String arg) {
		IJ.log("WARNING: Dynamic plugin execution is no longer supported. Please" +
			" install your plugin and restart Fiji instead.");
	}
}
