package annotool.extract;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import imagescience.feature.Structure;
import imagescience.image.Axes;
import imagescience.image.Coordinates;
import imagescience.image.Image;

import java.util.ArrayList;
import java.util.HashMap;

import annotool.ImgDimension;
import annotool.io.DataInput;

public class FeatureJStructure implements FeatureExtractor {
	protected float[][] features = null;
	protected ArrayList data;
	int length;
	int width;
	int height;
	int imageType;
	
	public final static int LARGESTEIGEN = 0;
	public final static int SMALLESTEIGEN = 1;
	
	double sscale = 1.0;
	double iscale = 3.0;
	int selectedEigenValue = LARGESTEIGEN;
	
	public final static String SMOOTHING_KEY = "Smoothing Scale";
	public final static String INTEGRATION_KEY = "Integration Scale";
	public final static String EIGENVALUE_KEY = "Eigenvalue";
	
	@Override
	public void setParameters(HashMap<String, String> parameter) {
		if (parameter != null) {
		    if(parameter.containsKey(SMOOTHING_KEY)) 
		    	sscale = Double.parseDouble(parameter.get(SMOOTHING_KEY));
		    
		    if(parameter.containsKey(INTEGRATION_KEY)) 
		    	iscale = Double.parseDouble(parameter.get(INTEGRATION_KEY));
		    
		    if(parameter.containsKey(EIGENVALUE_KEY)) {
		    	if(parameter.get(EIGENVALUE_KEY).equals("Largest"))
		    		selectedEigenValue = LARGESTEIGEN;
		    	else if(parameter.get(EIGENVALUE_KEY).equals("Smallest"))
		    		selectedEigenValue = SMALLESTEIGEN;
		    }
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
		
		Structure structure = new Structure();
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
			
			img = structure.run(img, sscale, iscale).get(selectedEigenValue);	//2D : index 1 - largest, index 2 - smallest
        	img.axes(Axes.X + Axes.Y);
        	img.get(startCO, values);
        	
        	int i = 0;
        	for(int y = 0; y < this.height; y++)
        		for(int x = 0; x < this.width; x++) {
        			features[imageIndex][i] = (float)values[y][x];
        			i++;
        		}
        	
        	//Testing
        	//if(imageIndex == (this.length - 1))
        		//img.imageplus().show();
		}
		
		return features;
	}

	@Override
	public boolean is3DExtractor() {
		return false;
	}
}
