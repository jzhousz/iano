import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import ij.*;
import ij.io.OpenDialog;
import ij.process.*;
import ij.plugin.*;

import imagescience.feature.Edges;
import imagescience.image.Image;
import imagescience.image.Aspects;
import imagescience.image.FloatImage;

import ij.plugin.filter.RankFilters;


public class Neuron_Analyzer implements PlugIn {
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
		
		ImageProcessor binaryIP2 = ip.duplicate();
		
		
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
		//HistogramEq.run(new ImagePlus("EQ", ip), 63, 255, 3.00f, null, null);
		
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
			//new ImagePlus("Hough Space [min radius only]", hip).show(); // Shows only the hough space for the minimun radius
		}

		for(int x=0; x < width; x++)
			for(int y = 0; y < height; y++)
				//Keep only the blob that is within the territory of the neuron
				if(hip.get(x, y) == 0 && maskIP.get(x, y) == 255)
					hip.set(x, y, 255);
		
		//Mark each objects
		ObjectDetection od = new ObjectDetection();
		int[] tag = od.run(hip);
		ArrayList<Integer> largeObjectTags = od.getLargeObjects(300);	//Gets tags of large objects (if pixel count > 300)
		HashMap<Integer, ImageProcessor> blobIPs = new HashMap<Integer, ImageProcessor>();
		
		for(Integer objTag : largeObjectTags) {
			ImageProcessor blobIP = hip.createProcessor(width, height);
			blobIP.or(255);
			blobIPs.put(objTag, blobIP);
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
		//java.util.ArrayList<Integer> blobToRemove = new java.util.ArrayList<Integer>();
		//ObjectSelection selector = new ObjectSelection();
		//for(Integer key : blobIPs.keySet())
			//if(!selector.isSoma(blobIPs.get(key), ip, 7, 10))
				//blobToRemove.add(key);
		//for(Integer key : blobToRemove)
			//blobIPs.remove(key);
		
		//Dilate to edge
		for(Integer key : blobIPs.keySet())
			DilateToEdge.run2(ip, binaryIP, blobIPs.get(key), 35);

		
		//Mark blob area on original image
		for(Integer key : blobIPs.keySet()) {
			ImageProcessor blobIP = blobIPs.get(key);
			for(int x=0; x < width; x++)
				for(int y = 0; y < height; y++)
					if(blobIP.get(x, y) == 0)
						somaip.set(x, y, 0);
		}
		
		new ImagePlus("Soma-removed", somaip.duplicate()).show();
				
		//Apply the territory analysis on the image with soma removed
		t.setRadius(50);
		ImageProcessor tp = t.run(somaip);
		
		
		
		//Remove axon part
		HistogramEq.run(new ImagePlus("EQ", binaryIP2), 63, 255, 3.00f, null, null);		
		//Use green channel if color image
		if (binaryIP2 instanceof ColorProcessor) {			
			byte[] tmppixels= new byte[width*height];			
			((ColorProcessor)binaryIP2).getRGB(tmppixels,pixels,tmppixels);
			binaryIP2 = new ByteProcessor(width, height, pixels, null);
		}
		binaryIP2.autoThreshold();
		binaryIP2.invert();
		//Close - dilation followed by erosion
		binaryIP2.dilate();
		binaryIP2.erode();
		
		//Remove noise with despeckle : median filter with radius 1
		filter.rank(binaryIP2, 1, RankFilters.MEDIAN);
		new ImagePlus("Binary2", binaryIP2).show();
		
		removeAxon(somaip, tp, new BinaryProcessor((ByteProcessor)binaryIP2));
		tp = t.run(somaip);
		
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
		
		IJ.write("Object pixels: " + count);
		IJ.write("Back pixels: " + countBack);

		IJ.showProgress(1.0);
	}
	
	//ip: Territory image
	protected void removeAxon(ImageProcessor ip, final ImageProcessor tp, BinaryProcessor binaryIP) {
		OpenDialog openDialog = new OpenDialog("Open prediction file ...", "");
		String directory = openDialog.getDirectory();
		String fileName = openDialog.getFileName();
		if (fileName==null) return;
		
		int width = ip.getWidth();
		int height = ip.getHeight();
		
		ImageProcessor axonIP = new ByteProcessor(width, height);
		axonIP.or(255);
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(directory + fileName));

			String line = br.readLine();
			String[] co = null;
			int x = 0, y = 0;
			while(line != null) {
				co = line.split(",");
				x = Integer.parseInt(co[0]);
				y = Integer.parseInt(co[1]);
				axonIP.set(x, y, 0);			
				
				line = br.readLine();
			}
		}		
		catch (Exception e) {
			IJ.error("Import_Mark Exception: ", e.getMessage());
			return;
		}
		
		//Remove objects that are on the right half of the foreground
		//Find left edge of bounding box
		int minX = -1;
		for(int x=0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if(tp.getPixel(x, y) == 0) {
					minX = x;
					break;
				}
			}
			if(minX != -1)
				break;
		}
		//Find right edge of bounding box
		int maxX = -1;
		for(int x = width - 1; x > 0; x--) {
			for(int y = 0; y < height; y++) {
				if(tp.getPixel(x, y) == 0) {
					maxX = x;
					break;
				}
			}
			if(maxX != -1)
				break;
		}
		
		int boundary = (minX + maxX) / 2;
		
		
		//Remove axon pixels on right part of the image
		for(int y=0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				if(axonIP.get(x, y) == 0 && x > boundary)	//If detected as axon and on left side of image, remove it
					axonIP.set(x, y, 255);
			}
		}

		//Get each separate object
		ObjectDetection od = new ObjectDetection();
		int[] tag = od.run(axonIP);
		int largestObjectTag = od.getLargestObject();

		//HashMap<Integer, Integer> objectTags = od.getTagCount(300);	//Gets tag and corresponding pixel count (if pixel count > 300)

		//Now remove objects that are not part of largestObject
		int arrayIndex = 0;
		for(int y=0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				if(tag[arrayIndex] != largestObjectTag) {
					tag[arrayIndex] = 0;	//Not axon at that pixel
					axonIP.set(x, y, 255);
				}
				arrayIndex++;
			}
		}
		
		new ImagePlus("Axon-before", axonIP.duplicate()).show();
		
		DilateToEdge.axonRun(binaryIP, axonIP);
		
		for(int y=0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				if(axonIP.get(x, y) == 0)
					ip.set(x, y, 0);
			}
		}
		
		
		new ImagePlus("Axon-after", axonIP).show();
	}
}

