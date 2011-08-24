package annotool.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;


/**
 * This class scans the plugin folder to check for new algorithms.
 * Each algorithm should be in a folder. Each algorithm folder should have a xml file called algorithm.xml,
 * which contains the details of the algorithm.
 * 
 * @author santosh
 *
 */
public class PluginScanner {
	public static final String PLUGIN_PATH = "plugins";
	public static final String AlGO_XML = "algorithm.xml";
	
	//List of xml files from all algorithm directories in plugins
	private ArrayList<String> xmlFiles = new ArrayList<String>();
	
	public PluginScanner() {
	}
	
	public void scan() {
		File pluginDir = new File(PLUGIN_PATH);
		if(!pluginDir.exists() || !pluginDir.isDirectory())
			return;
		
		for(File child : pluginDir.listFiles())
			if(child.isDirectory())
				scanAlgoDir(child);
	}
	
	/**
	 * Scans the directory for an algorithm. 
	 * It looks for the xml file describing the plugin.
	 */
	private void scanAlgoDir(File child) {
		FilenameFilter filter = new AlgoXMLFilter();
		
		for(File algoFile : child.listFiles(filter))
			xmlFiles.add(algoFile.getPath());
	}
	
	class AlgoXMLFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			return (name.equalsIgnoreCase(AlGO_XML));
		}
	}

	public ArrayList<String> getXmlFiles() {
		return xmlFiles;
	}
}
