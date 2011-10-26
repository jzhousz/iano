import java.util.HashMap;

import ij.*;
import ij.process.*;
import ij.plugin.*;

import imagescience.feature.Edges;
import imagescience.image.Image;
import imagescience.image.Aspects;
import imagescience.image.FloatImage;

import ij.plugin.filter.RankFilters;


public class Soma_Detection implements PlugIn {
	public void run(String arg) {
		final ImagePlus imp = WindowManager.getCurrentImage();
		if (imp == null)  {
			IJ.error("There are no images open");
			return;
		}
		
		ImageProcessor ip = imp.getProcessor().duplicate();
		int width = ip.getWidth();
		int height = ip.getHeight();
		
		//Remove noise with despeckle : median filter with radius 1
		RankFilters filter = new RankFilters();
		filter.rank(ip, 1, RankFilters.MEDIAN);
		
		//Keep original image for applying territory analysis after removing soma
		ImageProcessor somaip = ip.duplicate();		
		//Another for extracting territory for masking
		ImageProcessor maskIP = ip.duplicate();

		//Create territory mask from original
		Territory t = new Territory(50);
		maskIP = t.run(maskIP);
		

		//histogram equalization		
		//ContrastEnhancer ce = new ContrastEnhancer();
		//ce.equalize(ip);
		HistogramEq.run(new ImagePlus("EQ", ip), 127, 256, 3.00f, null, null);
		
		//Use green channel if color image
		byte[] pixels= new byte[width*height];
		if (ip instanceof ColorProcessor) {			
			byte[] tmppixels= new byte[width*height];			
			((ColorProcessor)ip).getRGB(tmppixels,pixels,tmppixels);
			ip = new ByteProcessor(width, height, pixels, null);
		}

		//Binary image to use as guide for region growing/dilation
		ImageProcessor binaryIP = ip.duplicate();
		binaryIP.autoThreshold();
		binaryIP.invert();
		

		//Edge detection
		final Image img = Image.wrap(new ImagePlus("Soma", ip));
		Image newimg = new FloatImage(img);
			
		final Aspects aspects = newimg.aspects();
		
		//if (!FJ_Options.isotropic) 
			newimg.aspects(new Aspects());
		
		final Edges edges = new Edges();
		newimg = edges.run(newimg,1.0,false);
		newimg.aspects(aspects);
		
		//Hough transform
		ip = newimg.imageplus().getProcessor().convertToByte(true);	        
		HoughTransform ht = new HoughTransform(10, 20, 2, width, height, width, 0, 0);
		ImageProcessor hip = ip.duplicate();
		try {
			hip = ht.run(hip);
		}
		catch(Exception ex) {
			IJ.error("Exception: ", ex.getMessage());
		}
		if(ip != null) {
			hip.autoThreshold();
			new ImagePlus("Hough Space [min radius only]", hip).show(); // Shows only the hough space for the minimun radius
		}

		for(int x=0; x < width; x++)
			for(int y = 0; y < height; y++)
				//Keep only the blob that is within the territory of the neuron
				if(hip.get(x, y) == 0 && maskIP.get(x, y) == 255)
					hip.set(x, y, 255);
		
		//Mark each objects
		ObjectDetection od = new ObjectDetection();
		int[] tag = od.run(hip);
		HashMap<Integer, Integer> tagCount = od.getTagCount(300);	//Gets tag and corresponding pixel count (if pixel count > 300)
		HashMap<Integer, ImageProcessor> blobIPs = new HashMap<Integer, ImageProcessor>();
		
		for(Integer key : tagCount.keySet()) {
			ImageProcessor blobIP = hip.createProcessor(width, height);
			blobIP.or(255);
			blobIPs.put(key, blobIP);
		}
		
		//TODO: Instead of using separate IPs for each blob, use single IP and tag map
		
		//Create separate imageprocessor for each blob
		int arrayIndex = 0;
		for(int y=0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				if(tag[arrayIndex] != 0 && blobIPs.containsKey(tag[arrayIndex])) {
					ImageProcessor blobIP = blobIPs.get(tag[arrayIndex]);
					blobIP.set(x, y, 0);
				}
				arrayIndex++;
			}
		}
		
		//Get binary edge image
		ip.autoThreshold();
		
		//Select relevant blobs as potential soma
		/*java.util.ArrayList<Integer> blobToRemove = new java.util.ArrayList<Integer>();
		ObjectSelection selector = new ObjectSelection();
		for(Integer key : blobIPs.keySet())
			if(!selector.isSoma(blobIPs.get(key), ip, 7, 10))
				blobToRemove.add(key);
		for(Integer key : blobToRemove)
			blobIPs.remove(key);*/
		
		//Dilate to edge
		for(Integer key : blobIPs.keySet())
			DilateToEdge.run2(ip, binaryIP, blobIPs.get(key), 35);

		
		//Display each blob
		for(Integer key : blobIPs.keySet())
			new ImagePlus("Blob" + key, blobIPs.get(key)).show(); 
			
		//Mark blob area on original image
		for(Integer key : blobIPs.keySet()) {
			ImageProcessor blobIP = blobIPs.get(key);
			for(int x=0; x < width; x++)
				for(int y = 0; y < height; y++)
					if(blobIP.get(x, y) == 0)
						somaip.set(x, y, 0);
		}
				
		//Apply the territory analysis on the image with soma removed
		t.setRadius(50);
		ImageProcessor tp = t.run(somaip);
		
		new ImagePlus("Soma", somaip).show();
		new ImagePlus("Territory", tp).show();
		new ImagePlus("Edge", ip).show();
		new ImagePlus("Binary", binaryIP).show();		

		int count = 0, countBack = 0;		
		for(int x = 0; x < width; x++)
		    for(int y=0; y < height; y++)
		    	if(tp.get(x, y) == 255)
		    		countBack++;
		    	else
		    		count++;
		
		//IJ.write("Object pixels: " + count);
		//IJ.write("Back pixels: " + countBack);

		IJ.showProgress(1.0);
	}
}

