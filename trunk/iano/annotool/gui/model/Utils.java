package annotool.gui.model;

import ij.gui.Roi;
import ij.io.RoiDecoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.ImageIcon;

public class Utils {
	// Extension for model and chain files
	public final static String MODEL_EXT = "imdl";
	public final static String CHAIN_EXT = "ichn";

	/*
	 * Get the extension of a file.
	 */
	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	// Removes extension from file name
	public static String removeExtension(String fileName) {
		int i = fileName.lastIndexOf('.');
		if (i > 0 && i < fileName.length() - 1) {
			fileName = fileName.substring(0, i);
		}
		return fileName;
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	public static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = Utils.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	/**
	 * Method to read a single roi zip file and get a list or roi objects. Based on ImageJ RoiManager method
	 * 
	 * @param path
	 * @return
	 */
	public static void openRoiZip(String path, HashMap<String, Roi> rois, HashMap<String, String> classMap, String className) throws IOException {		
		ZipInputStream in = null;
		ByteArrayOutputStream out;
		int nRois = 0;
		try {
			in = new ZipInputStream(new FileInputStream(path));
			byte[] buf = new byte[1024];
			int len;
			ZipEntry entry = in.getNextEntry();
			while (entry != null) {
				String name = entry.getName();
				if (name.endsWith(".roi")) {
					out = new ByteArrayOutputStream();
					while ((len = in.read(buf)) > 0)
						out.write(buf, 0, len);
					out.close();
					byte[] bytes = out.toByteArray();
					RoiDecoder rd = new RoiDecoder(bytes, name);
					Roi roi = rd.getRoi();
					if (roi != null) {
						name = name.substring(0, name.length() - 4);
						name = getUniqueName(classMap.keySet(), name);
						classMap.put(name, className);
						rois.put(name, roi);
						nRois++;
					}
				}
				entry = in.getNextEntry();
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		if (nRois == 0)
			System.out.println("ZIP archive does not appear to contain \".roi\" files");
	}
	
	private static String getUniqueName(Set<String> nameList, String name) {
		String name2 = name;
        int n = 1;
        
        while(nameList.contains(name2)) {
        	int lastDash = name2.lastIndexOf("-");
        	if (lastDash!=-1 && name2.length()-lastDash<5)
                name2 = name2.substring(0, lastDash);
            name2 = name2+"-"+n;
            n++;
        }
        return name2;
	}
}