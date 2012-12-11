package annotool.gui.model;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * FIle filter to select only images in open/save dialog.
 * File extensions considered: jpg, jpeg, png, bmp, tif, tiff, gif 
 * Version 1.2.4+ supports other extensions as long as they are supported by ImageJ.
 *
 */
public class ImageFilter extends FileFilter {
	private String validExtensions = "^jpg|jpeg|png|bmp|tif|tiff|gif$";
			
	public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }    	
    	
        String extension = Utils.getExtension(f);
        if (extension != null) {
            if (extension.matches(validExtensions)) {
                    return true;
            } else {
                return false;
            }
        }
        return false;
    }

    //The description of this filter
    public String getDescription() {
        return "Image Files";
    }
}
