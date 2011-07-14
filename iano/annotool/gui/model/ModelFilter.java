package annotool.gui.model;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ModelFilter extends FileFilter {
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }    	
    	
        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals(Utils.MODEL_EXT)) {
                    return true;
            } else {
                return false;
            }
        }
        return false;
    }

    //The description of this filter
    public String getDescription() {
        return "Model Files";
    }
    
    //TODO: can put this method into a common utility class (also the method to create image icon)
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}
