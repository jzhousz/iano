package annotool.extract;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import imagescience.feature.Laplacian;
import imagescience.feature.Statistics;
import imagescience.image.Axes;
import imagescience.image.Coordinates;
import imagescience.image.Image;

import java.util.ArrayList;
import java.util.HashMap;

import annotool.ImgDimension;
import annotool.io.DataInput;

public class FeatureJStatistics implements FeatureExtractor {
	protected float[][] features = null;
	protected ArrayList data;
	int length;
	int width;
	int height;
	int imageType;
	private static final int numFeatures = 14;
	
	public final static String[] KEYS = {"AVERAGE ABSOLUTE DEVIATION",
											"ELEMENTS",
											"KURTOSIS",
											"L1NORM",
											"L2NORM",
											"MASS",
											"MAXIMUM",
											"MEAN",
											"MEDIAN",
											"MINIMUM",
											"MODE",
											"SDEVIATION",
											"SKEWNESS",
											"VARIANCE" };
	
	private int[] statKeys = {Statistics.ADEVIATION,
								Statistics.ELEMENTS,
								Statistics.KURTOSIS,
								Statistics.L1NORM,
								Statistics.L2NORM,
								Statistics.MASS,
								Statistics.MAXIMUM,
								Statistics.MEAN,
								Statistics.MEDIAN,
								Statistics.MINIMUM,
								Statistics.MODE,
								Statistics.SDEVIATION,
								Statistics.SKEWNESS,
								Statistics.VARIANCE
								};
	
	private boolean[] isSelectedFeature;
	private int selectedCount = 0;
	
	public FeatureJStatistics() {
		//Intialize with all false
		isSelectedFeature = new boolean[numFeatures];
		for(int i = 0; i < numFeatures; i++)
			isSelectedFeature[i] = false;			
	}
	
	@Override
	public void setParameters(HashMap<String, String> parameter) {
		selectedCount = 0;
		if (parameter != null) {
			for(int i = 0; i < numFeatures; i++)
				if(parameter.containsKey(KEYS[i]) && "1".equals(parameter.get(KEYS[i]))) {
					isSelectedFeature[i] = true;
					selectedCount++;
				}
				else
					isSelectedFeature[i] = false;
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
		//Initialize features array
		features = new float[data.size()][selectedCount];
		
		ImageProcessor ip = null;
		ImagePlus imp = null;
		Image img = null;
		Statistics stats = new Statistics();
		
		for(int imageIndex = 0; imageIndex < this.length; imageIndex++) {
			if(imageType == DataInput.GRAY8 || imageType == DataInput.COLOR_RGB) {
				ip = new ByteProcessor(width, height, (byte[])data.get(imageIndex), null);
		    }
		    else if(imageType == DataInput.GRAY16) {
		    	ip = new FloatProcessor(width, height, (int[])data.get(imageIndex));
		    }	
	 	    else if(imageType == DataInput.GRAY32) {
		    	ip = new FloatProcessor(width, height, (float[])data.get(imageIndex), null);
	 	    }
	 	    else {
	 	    	throw new Exception("Unsuppored image type");
	 	    }
			
			imp = new ImagePlus("Image", ip);
			img = Image.wrap(imp);
			
			stats.run(img);
        	
			System.out.println("Features for image: " + (imageIndex + 1));        	
        	
			for(int i=0, j=0; i < numFeatures; i++) {
        		if(isSelectedFeature[i]) {
        			features[imageIndex][j] = (float)stats.get(statKeys[i]);
        			System.out.println(KEYS[i] + ": " + features[imageIndex][j]);        			
        			j++;
        		}
        	}
		}
		
		return features;
	}

	@Override
	public boolean is3DExtractor() {
		return false;
	}

}
