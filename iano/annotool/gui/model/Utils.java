package annotool.gui.model;

import java.io.File;

import javax.swing.ImageIcon;

public class Utils {
	//Extension for model and chain files
    public final static String MODEL_EXT = "imdl";
    public final static String CHAIN_EXT = "ichn";

    /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
    
    //Removes extension from file name
    public static String removeExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0 &&  i < fileName.length() - 1) {
        	fileName = fileName.substring(0, i);
        }
        return fileName;
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = Utils.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}