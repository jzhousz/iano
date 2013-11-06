package annotool.extract;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import ij.process.ImageProcessor;
import imagescience.feature.Differentiator;
import imagescience.image.Axes;
import imagescience.image.Coordinates;
import imagescience.image.Image;

import java.util.ArrayList;
import java.util.HashMap;

import annotool.ImgDimension;
import annotool.io.DataInput;


public class FeatureJDerivative3D implements FeatureExtractor {
	protected float[][] features = null;
	protected ArrayList data = null;
	protected DataInput problem = null;
	int length, width, height, depth;
	int imageType;
	double scale = 1.0;
	int x_order = 0, y_order = 0, z_order = 0;
	
	public final static String SCALE_KEY = "Smoothing Scale";
	public final static String X_ORDER_KEY = "x-order";
	public final static String Y_ORDER_KEY = "y-order";
	public final static String Z_ORDER_KEY = "z-order";
	
	
	@Override
	public void setParameters(HashMap<String, String> para) {
		if (para != null) {
		    if(para.containsKey(SCALE_KEY)) 
		    	scale = Double.parseDouble(para.get(SCALE_KEY));
		    if(para.containsKey(X_ORDER_KEY)) 
		    	x_order = Integer.parseInt(para.get(X_ORDER_KEY));
		    if(para.containsKey(Y_ORDER_KEY)) 
		    	y_order = Integer.parseInt(para.get(Y_ORDER_KEY));
		    if(para.containsKey(Z_ORDER_KEY)) 
		    	z_order = Integer.parseInt(para.get(Z_ORDER_KEY));
		}
	}
	
	
	@Override
	public float[][] calcFeatures(DataInput problem) throws Exception {

		this.problem = problem;

		this.length = problem.getLength();

		this.width = problem.getWidth();
		this.height = problem.getHeight();
		this.depth = problem.getDepth();
		
		this.imageType = problem.getImageType();
		
		return calcFeatures();
	}

	
	@Override
	public float[][] calcFeatures(ArrayList data, int imageType,
			ImgDimension dim) throws Exception {
		this.data = data;
		this.length = data.size();

		this.width = dim.width;
		this.height = dim.height;
        this.depth = dim.depth;
		
		this.imageType = imageType;
		
		return calcFeatures();
	}
	
	
	protected float[][] calcFeatures() throws Exception {
		this.features = new float[this.length][this.width * this.height * this.depth];

		Image img = null;
	    ImagePlus combinedImage;
        ImageStack stack;
        
		Differentiator differentiator = new Differentiator();
		Coordinates startCO = new Coordinates(0, 0, 0);
		
		double values[][][] = new double[this.height][this.width][this.depth];
		
		int currentHeight = 0, currentWidth = 0;
		int stackSize;
		
        //iterate through each image
		for(int imageIndex = 0; imageIndex < length; imageIndex++) {
		
			System.out.println("Processing image number " + (imageIndex + 1));
			currentWidth = this.width;
			currentHeight = this.height;
			
			if (problem !=null)
			{
				if (!problem.ofSameSize()) 
				{
				currentWidth = problem.getWidthList()[imageIndex];
				currentHeight = problem.getHeightList()[imageIndex];
				  
				}
				
				if(problem.getMode() == problem.ROIMODE)
					stackSize = problem.getDepth();
				else
				    stackSize  = problem.getStackSize();
				
				stack = new ImageStack(this.width, this.height);
                
                //for each slice of a 3D image
                for(int imageSlice = 1; imageSlice <= stackSize; imageSlice++)
                {
                    ImageProcessor ip = null;
				
                    if(imageType == DataInput.GRAY8 || imageType == DataInput.COLOR_RGB) {
                        if (data != null)
                            ip = new ByteProcessor(currentWidth, currentHeight, (byte[])data.get(imageIndex), null);
                        else 
                        {
                            ip = new ByteProcessor(currentWidth, currentHeight, (byte[])problem.getData(imageIndex, imageSlice), null);
                        }
                    }
                    else if(imageType == DataInput.GRAY16) {
                        if (data != null)
                            ip = new ShortProcessor(currentWidth, currentHeight, (short[])data.get(imageIndex), null);
                        else
                            ip = new ShortProcessor(currentWidth, currentHeight, (short[])problem.getData(imageIndex, imageSlice), null);
                    }   
                    else if(imageType == DataInput.GRAY32) {
                        if (data != null)
                            ip = new FloatProcessor(currentWidth, currentHeight, (float[])data.get(imageIndex), null);
                        else
                            ip = new FloatProcessor(currentWidth, currentHeight, (float[])problem.getData(imageIndex, imageSlice), null);
                    }
                    else {
                        throw new Exception("Unsupported image type");
                    }

                    stack.addSlice("", ip);
                    
                }

                combinedImage = new ImagePlus("Combined", stack);
                
                img = Image.wrap(combinedImage);
                img = differentiator.run(img, scale, x_order, y_order, z_order);
                img.axes(Axes.X + Axes.Y + Axes.Z);
	    
                img.get(startCO, values);
                
                //For testing purposes
                //img.imageplus().show();
            
                int i = 0;
                for(int z = 0; z < this.depth; z++)
                {
                for(int y = 0; y < this.height; y++)
                    //get pixel values from each slice of each image
                    for(int x = 0; x < this.width; x++) {
                        features[imageIndex][i] = (float)values[y][x][z];
                        i++;}
                }
			}	
		}
    	return features;
}
	
	@Override
	public boolean is3DExtractor() {
		return true;
	}
	
}
