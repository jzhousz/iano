package annotool.gui.model;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * File filter to allow selection of zip files (roi files) only in open/save dialogs.
 * 
 */
public class ROIFilter extends FileFilter {
	public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }    	
    	
        String extension = Utils.getExtension(f);
        if (extension != null) {
            if (extension.equalsIgnoreCase("zip")) {
                    return true;
            } else {
                return false;
            }
        }
        return false;
    }

    //The description of this filter
    public String getDescription() {
        return "ROI Files";
    }
}