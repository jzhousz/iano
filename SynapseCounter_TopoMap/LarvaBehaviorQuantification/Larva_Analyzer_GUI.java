
import java.util.ArrayList;

import javax.swing.JOptionPane;

import entities.OptionComponent;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import manager.HelperManager;
import manager.PropertyManager;

/**
 * This class will create all the interface component (e.g. label, text fields,
 * buttons) for users to enter the arguments for the image process.
 */
public class Larva_Analyzer_GUI implements PlugIn {
	
	public void run(String arg) 
	{
		ImagePlus ip = IJ.getImage();
		FileInfo info = ip.getOriginalFileInfo();
		String dir = info.directory;
		String fileName = info.fileName;
		RoiManager rm = RoiManager.getInstance();
		RoiManager roiManager = RoiManager.getInstance();
		ArrayList<Roi> listRoi = new ArrayList<Roi>();
		
		// if RoiManager is null
		if ( roiManager == null ) 
		{
			// handle single region of interest selection
			Roi roi = ip.getRoi();
			if (roi == null) {
				IJ.error("No selection found.");
				return;
			}
			
			listRoi.add(roi);
			
		// if RoiManager is NOT null
		} else {
			// handle multiple region of interest selection
			Roi[] rois = roiManager.getRoisAsArray();

			if (rois.length == 0) {
				IJ.error("No selections detected.");
				return;
			}

			for (Roi r : rois) {
				listRoi.add(r);
							
			}
		}
		
		System.out.println("avi_file:"+ dir + fileName);
		PropertyManager.setProperty("avi_file", dir + fileName);
		OptionComponent optionComponent = new OptionComponent(dir, fileName, "Setting - Larva Behavior Quantification", listRoi);
		
		optionComponent.show();
	}

}
