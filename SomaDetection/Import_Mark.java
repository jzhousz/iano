import java.io.*;
import ij.*;
import ij.io.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;

import java.util.HashMap;

public class Import_Mark implements PlugInFilter {
	ImagePlus imp;

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL;
	}

	public void run(ImageProcessor ip) {
		OpenDialog od = new OpenDialog("Open prediction file ...", "");
		String directory = od.getDirectory();
		String fileName = od.getFileName();
		if (fileName==null) return;
		read(ip, directory,fileName);
		imp.updateAndDraw();
	}

	protected void read(ImageProcessor ip, String dir, String filename) {
		int width = ip.getWidth();
		int height = ip.getHeight();
		
		ImageProcessor axonIP = new ByteProcessor(width, height);
		axonIP.or(255);
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(dir+filename));

			String line = br.readLine();
			String[] co = null;
			int x = 0, y = 0;
			while(line != null) {
				co = line.split(",");
				x = Integer.parseInt(co[0]);
				y = Integer.parseInt(co[1]);
				axonIP.set(x, y, 0);
				ip.set(x, y, 0);				
				
				line = br.readLine();
			}
		}		
		catch (Exception e) {
			IJ.error("Import_Mark Exception: ", e.getMessage());
		}

		//Get each separate object
		ObjectDetection od = new ObjectDetection();
		int[] tag = od.run(axonIP);

		HashMap<Integer, Integer> objectTags = od.getTagCount(300);	//Gets tag and corresponding pixel count (if pixel count > 300)

		for(Integer key : objectTags.keySet())
			IJ.write(key + "-" + objectTags.get(key));
		
		//Now remove objects that are not part of objectTags
		int arrayIndex = 0;
		for(int y=0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				if(!objectTags.containsKey(tag[arrayIndex])) {
					tag[arrayIndex] = 0;	//Not axon at that pixel
					axonIP.set(x, y, 255);
				}
				arrayIndex++;
			}
		}

		new ImagePlus("Axon", axonIP).show();
	}
}
