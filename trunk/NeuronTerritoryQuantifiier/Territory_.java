import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;

public class Territory_ implements PlugInFilter {
	ImagePlus imp;

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL;
	}

	public void run(ImageProcessor ip) {
		double radius = IJ.getNumber("Rolling ball radius", 50);

		if(radius == IJ.CANCELED)
			return;

		ImageProcessor newip = null;
		
		int width = ip.getWidth();
		int height = ip.getHeight();

		byte[] pixels = new byte[width*height];
		byte[] tmppixels = new byte[width*height];

		if(ip instanceof ColorProcessor) {
			((ColorProcessor)ip).getRGB(tmppixels,pixels,tmppixels);
			newip = new ByteProcessor(width, height, pixels, null);
			//imp.setProcessor(ip);
		}
		else
			newip = ip.duplicate();

		//Apply despeckle : median filter with radius 1
		RankFilters filter = new RankFilters();
		filter.rank(newip, 1, RankFilters.MEDIAN);
		
		//Subtract background
		BackgroundSubtracter bs = new BackgroundSubtracter();
		bs.rollingBallBackground(newip, radius, false, true, false, true, false);

		bs.rollingBallBackground(newip, radius, true, false, false, true, false);

		//Threshold
		newip.autoThreshold();

		int[][] data = newip.getIntArray();

		int count = 0;
		int countBack = 0, others = 0;
		
		for(int x = 0; x < newip.getWidth(); x++)
		    for(int y=0; y < newip.getHeight(); y++)
		    	if(data[x][y] == 255)
		    		countBack++;
		    	else if(data[x][y] == 0)
		    		count++;

		IJ.write("Object pixels: " + count);
		IJ.write("Back pixels: " + countBack);
		 		
		//imp.updateAndDraw();
		new ImagePlus("Territory Image", newip).show();
	}

}
