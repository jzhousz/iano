package annotool.gui.model;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ModelFilter extends FileFilter {
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }    	
    	
        String extension = Utils.getExtension(f);
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
}
