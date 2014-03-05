package annotool.io;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.util.ArrayList;

import annotool.ImgDimension;

@Deprecated
public class DataInputROI {
	protected ArrayList<Object> data = null;
	protected String imagePath = null; //Path to the image which is divided into ROIs
	protected String roiFile = null; //Path to file that has roi data (co-ordinates) (How to create it from ROI zip file from ImageJ)
	protected int stackSize = 0;
	protected boolean is3D = false;
	
	ImgDimension roiDimension = new ImgDimension();
	
	ImagePlus imp = null;
	
	String channel = annotool.Annotator.channel;
	
	public DataInputROI(String imagePath, String roiFile, ImgDimension roiDimension, String channel) {
		this.imagePath = imagePath;
		this.roiFile = roiFile;
		this.roiDimension = roiDimension;
		this.channel = channel;
		
		if(roiDimension.height > 1)
			is3D = true;
		
		imp = new ImagePlus(imagePath);
	}
	public DataInputROI(String imagePath, String roiFile, ImgDimension roiDimension) {
		this.imagePath = imagePath;
		this.roiFile = roiFile;
		this.roiDimension = roiDimension;
		
		if(roiDimension.height > 1)
			is3D = true;
		
		imp = new ImagePlus(imagePath);
	}
	
	//Get data for a single region of interest
	//ArrayList contains single object for 2D image,
	//otherwise, each object in the arraylist is a stack of the 3D image
	public ArrayList<Object> getROIData(int x, int y, int z) {		
		//int stackSize = getStackSize();
		
		//x, y, z +- roiDimension.width/2,height/2,depth/2 must be within the image
		//but the ROI information from the James_mouse_36z set has roi points on 2D stack
		
		ArrayList<Object> data = new ArrayList<Object>(roiDimension.depth);
		
		for(int stackIndex = (z - roiDimension.depth / 2); stackIndex <= (z + roiDimension.depth / 2); stackIndex++)
			data.add(openOneImage(x, y, stackIndex));
		
		return data;
	}
	
	private Object openOneImage(int x, int y, int stackIndex){
		ImageProcessor ip = imp.getStack().getProcessor(stackIndex);
		
		int width = ip.getWidth();
		int height = ip.getHeight();
		
		Object results = null;
		
		if (ip instanceof ByteProcessor || ip instanceof ShortProcessor || ip instanceof FloatProcessor)
			results = ip.getPixels();//TODO: get the roi portion only?
		else if (ip instanceof ColorProcessor) {
			byte[] pixels= new byte[width*height];
			byte[] tmppixels= new byte[width*height];

			if (channel == "r")
				((ColorProcessor)ip).getRGB(pixels,tmppixels,tmppixels);
			else if (channel == "g")
				((ColorProcessor)ip).getRGB(tmppixels,pixels,tmppixels);
			else
				((ColorProcessor)ip).getRGB(tmppixels,tmppixels,pixels);
			
			results = pixels;
		}
		else {
			System.err.println("Image type is not supported.");
			System.exit(0);
		}
		
		return results;
	}
	
	public int getStackSize() {
		if(stackSize == 0) {;
			stackSize = imp.getStackSize();
		}
		return stackSize;
	}
}
