package annotool.gui.model;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * File filter to select only model files(*.imdl) in open/save dialogs.
 * 
 */
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
