package annotool.extract;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import imagescience.feature.Differentiator;
import imagescience.image.Axes;
import imagescience.image.Coordinates;
import imagescience.image.Image;

import java.util.ArrayList;
import java.util.HashMap;

import annotool.ImgDimension;
import annotool.io.DataInput;

public class FeatureJDerivative implements FeatureExtractor {
	protected float[][] features = null;
	protected ArrayList data;
	int length;
	int width;
	int height;
	int imageType;
	double scale = 1.0;
	int x_order = 0, y_order = 0, z_order = 0;
	
	public final static String SCALE_KEY = "Smoothing Scale";
	public final static String X_ORDER_KEY = "x-order";
	public final static String Y_ORDER_KEY = "y-order";
	public final static String Z_ORDER_KEY = "z-order";
	
	@Override
	public void setParameters(HashMap<String, String> parameter) {
		if (parameter != null) {
		    if(parameter.containsKey(SCALE_KEY)) 
		    	scale = Double.parseDouble(parameter.get(SCALE_KEY));
		    if(parameter.containsKey(X_ORDER_KEY)) 
		    	x_order = Integer.parseInt(parameter.get(X_ORDER_KEY));
		    if(parameter.containsKey(Y_ORDER_KEY)) 
		    	y_order = Integer.parseInt(parameter.get(Y_ORDER_KEY));
		    if(parameter.containsKey(Z_ORDER_KEY)) 
		    	z_order = Integer.parseInt(parameter.get(Z_ORDER_KEY));
		}
	}

	@Override
	public float[][] calcFeatures(DataInput problem) throws Exception {
		this.data = problem.getData();
		this.length = problem.getLength();
		this.width = problem.getWidth();
		this.height = problem.getHeight();
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
		this.imageType = imageType;
		
		return calcFeatures();
	}
	
	protected float[][] calcFeatures() throws Exception {
		this.features = new float[this.length][this.width * this.height];
		
		ImageProcessor ip = null;
		Image img = null;
		
		Differentiator differentiator = new Differentiator();
		Coordinates startCO = new Coordinates(0, 0);
		
		double values[][] = new double[this.height][this.width];
		
		for(int imageIndex = 0; imageIndex < data.size(); imageIndex++) {
			if(imageType == DataInput.GRAY8 || imageType == DataInput.COLOR_RGB) {
				ip = new ByteProcessor(this.width, this.height, (byte[])data.get(imageIndex), null);
		    }
		    else if(imageType == DataInput.GRAY16) {
		    	ip = new FloatProcessor(this.width, this.height, (int[])data.get(imageIndex));
		    }	
	 	    else if(imageType == DataInput.GRAY32) {
		    	ip = new FloatProcessor(this.width, this.height, (float[])data.get(imageIndex), null);
	 	    }
	 	    else {
	 	    	throw new Exception("Unsuppored image type");
	 	    }
			
			img = Image.wrap(new ImagePlus("Image", ip));
			
			img = differentiator.run(img, scale, x_order, y_order, z_order);
        	img.axes(Axes.X + Axes.Y);
        	img.get(startCO, values);
        	
        	int i = 0;
        	for(int y = 0; y < this.height; y++)
        		for(int x = 0; x < this.width; x++) {
        			features[imageIndex][i] = (float)values[y][x];
        			i++;
        		}
		}
		
		return features;
	}

	@Override
	public boolean is3DExtractor() {
		return false;
	}
}
