package annotool.gui.model;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * File filter to allow selection of only pdf files in open/save dialogs.
 * 
 * @author Santosh
 *
 */
public class PDFFilter extends FileFilter {
	public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }    	
    	
        String extension = Utils.getExtension(f);
        if (extension != null) {
            if (extension.equalsIgnoreCase("pdf")) {
                    return true;
            } else {
                return false;
            }
        }
        return false;
    }

    //The description of this filter
    public String getDescription() {
        return "PDF Files";
    }
}
