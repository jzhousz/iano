package annotool.extract;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import imagescience.feature.Laplacian;
import imagescience.feature.Statistics;
import imagescience.image.Axes;
import imagescience.image.Coordinates;
import imagescience.image.Image;

import java.util.ArrayList;
import java.util.HashMap;

import annotool.ImgDimension;
import annotool.io.DataInput;

/**
 *  This class wraps a FeatureJ extractor:
 * 
 *  http://www.imagescience.org/meijering/software/featurej/
 *
 */
public class FeatureJStatistics implements FeatureExtractor {
	protected float[][] features = null;
	protected ArrayList data = null;
	protected DataInput problem = null;
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
	
	/**
	 * Default constructor
	 */
	public FeatureJStatistics() {
		//Intialize with all false
		isSelectedFeature = new boolean[numFeatures];
		for(int i = 0; i < numFeatures; i++)
			isSelectedFeature[i] = false;			
	}
	
   /**
    * Sets algorithm parameters from para 
    * 
    * @param  para  Each element of para holds a parameter’s name for its key
    *               and a parameter’s value for its value. The parameters
    *               should be the same as those in the algorithms.xml file.
    */
	@Override
	public void setParameters(HashMap<String, String> para) {
		selectedCount = 0;
		if (para != null) {
			for(int i = 0; i < numFeatures; i++)
				if(para.containsKey(KEYS[i]) && "1".equals(para.get(KEYS[i]))) {
					isSelectedFeature[i] = true;
					selectedCount++;
				}
				else
					isSelectedFeature[i] = false;
		}
	}

   /**
    * Get features based on raw image stored in problem.
    * 
    * @param   problem    Image to be processed
    * @return             Array of features
    * @throws  Exception  (Not used)
    */
	@Override
	public float[][] calcFeatures(DataInput problem) throws Exception {
		this.problem = problem;
		this.length = problem.getLength();
		this.width = problem.getWidth();
		this.height = problem.getHeight();
		this.imageType = problem.getImageType();
		
		return calcFeatures();
	}

   /**
    * Get features based on data, imageType, and dim.
    * 
    * @param   data       Data taken from the image
    * @param   imageType  Type of the image
    * @param   dim        Dimenstions of the image
    * @return             Array of features
    * @throws  Exception  (Not used)
    */
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
		
		for(int imageIndex = 0; imageIndex < length; imageIndex++) {
			if(imageType == DataInput.GRAY8 || imageType == DataInput.COLOR_RGB) {
				if (data != null)
				 ip = new ByteProcessor(this.width, this.height, (byte[])data.get(imageIndex), null);
				else 
				 ip = new ByteProcessor(this.width, this.height, (byte[])problem.getData(imageIndex, 1), null);
		    }
		    else if(imageType == DataInput.GRAY16) {
		    	if (data != null)
		    	 ip = new ShortProcessor(this.width, this.height, (short[])data.get(imageIndex), null);
		    	else
		    	 ip = new ShortProcessor(this.width, this.height, (short[])problem.getData(imageIndex,1), null);
		    }	
	 	    else if(imageType == DataInput.GRAY32) {
	 	    	if (data != null)
		    	 ip = new FloatProcessor(this.width, this.height, (float[])data.get(imageIndex), null);
	 	    	else
			     ip = new FloatProcessor(this.width, this.height, (float[])problem.getData(imageIndex, 1), null);
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

   /**
    * Returns whether or not the algorithm is able to extract from a 3D image 
    * stack. 
    * 
    * @return  <code>True</code> if the algorithm is a 3D extractor, 
    *          <code>False</code> if not. Default is <code>False</code>
    */
	@Override
	public boolean is3DExtractor() {
		return false;
	}

}
