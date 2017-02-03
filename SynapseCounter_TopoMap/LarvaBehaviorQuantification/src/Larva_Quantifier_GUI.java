
import java.util.ArrayList;
import entities.OptionComponent;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import manager.PropertyManager;

/**
* The GUI class for this program.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class Larva_Quantifier_GUI implements PlugIn 
{
	
	public void run(String arg) 
	{
		ImagePlus ip = IJ.getImage();
		FileInfo info = ip.getOriginalFileInfo();
		String dir = info.directory;
		String fileName = info.fileName;
//		RoiManager rm = RoiManager.getInstance();
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
