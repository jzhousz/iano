package annotool.gui.model;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * File filter to display only chain files in open/save dialog.
 * 
 */
public class ChainFilter extends FileFilter {
	public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }    	
    	
        String extension = Utils.getExtension(f);
        if (extension != null) {
            if (extension.equals(Utils.CHAIN_EXT)) {
                    return true;
            } else {
                return false;
            }
        }
        return false;
    }

    //The description of this filter
    public String getDescription() {
        return "Chain Files";
    }
}
