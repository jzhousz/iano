package annotool.extract;

import ij.ImagePlus;
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
	DataInput problem = null;
	int length;
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
		this.problem = problem;
		this.length = problem.getLength();
		this.features = new float[this.length][problem.getWidth() * problem.getHeight()];
		return calcFeatures();
	}

	@Override
	public float[][] calcFeatures(ArrayList data, int imageType,
			ImgDimension dim) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected float[][] calcFeatures() throws Exception {
		//for each image in the set
		ImagePlus imp = null;
		Image img = null;
		
		Differentiator differentiator = new Differentiator();
		Coordinates startCO = new Coordinates(0, 0);
		
		int width = problem.getWidth();
		int height = problem.getHeight();
		
		double values[][] = new double[height][width];
		
        for (int imageIndex = 0; imageIndex < this.length; imageIndex++) {
        	imp = problem.getImagePlus(imageIndex);
        	img = Image.wrap(imp);
        	
        	img = differentiator.run(img, scale, x_order, y_order, z_order);
        	img.axes(Axes.X + Axes.Y);
        	img.get(startCO, values);
        	
        	int i = 0;
        	for(int y = 0; y < height; y++)
        		for(int x = 0; x < width; x++) {
        			features[imageIndex][i] = (float)values[y][x];
        			i++;
        		}
        	
        }
		return features;
	}

	@Override
	public boolean is3DExtractor() {
		// TODO Auto-generated method stub
		return false;
	}
}
