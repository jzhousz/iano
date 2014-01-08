package annotool.extract;

import ij.ImagePlus;
import ij.ImageStack;
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
public class FeatureJStatistics3D implements FeatureExtractor {
	protected float[][] features = null;
	protected ArrayList data = null;
	protected DataInput problem = null;
	int length;
	int width;
	int height;
    int depth;
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
	public FeatureJStatistics3D() {
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
        this.depth = problem.getDepth();
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
        this.depth = dim.depth;
		this.imageType = imageType;
		
		return calcFeatures();
	}
	
	protected float[][] calcFeatures() throws Exception {
		
		if (selectedCount == 0)
			  throw new Exception("No feature has been selected.");

		//Initialize features array
		features = new float[length][selectedCount];
		
		ImagePlus combinedImage = null;
		Image img = null;
        
		Statistics stats = new Statistics();

		int currentHeight = 0, currentWidth = 0;
		int stackSize;
		ImageStack stack;
        
        ArrayList currentImage = null;
		for(int imageIndex = 0; imageIndex < length; imageIndex++) {
		
			currentWidth = this.width;
			currentHeight = this.height;
			
			if (problem !=null){
				if (!problem.ofSameSize()) {
				  throw new Exception("3D edge features requires the images to be of the same size");
				}
			}
			else if (data !=null){
				 currentImage = (ArrayList) data.get(imageIndex);
			}
	
			stack = new ImageStack(this.width, this.height);
    		stackSize = this.depth;
                
            //for each slice of a 3D image
            for(int imageSlice = 1; imageSlice <= stackSize; imageSlice++)
            {
                ImageProcessor ip = null;
            
                if(imageType == DataInput.GRAY8 || imageType == DataInput.COLOR_RGB) {
                    if (data != null)
                        ip = new ByteProcessor(currentWidth, currentHeight, (byte[])currentImage.get(imageSlice-1), null);
                    else 
                        ip = new ByteProcessor(currentWidth, currentHeight, (byte[])problem.getData(imageIndex, imageSlice), null);
                }
                else if(imageType == DataInput.GRAY16) {
                    if (data != null)
                        ip = new ShortProcessor(currentWidth, currentHeight, (short[])currentImage.get(imageSlice-1), null);
                    else
                        ip = new ShortProcessor(currentWidth, currentHeight, (short[])problem.getData(imageIndex, imageSlice), null);
                }   
                else if(imageType == DataInput.GRAY32) {
                    if (data != null)
                        ip = new FloatProcessor(currentWidth, currentHeight, (float[])currentImage.get(imageSlice-1), null);
                    else
                        ip = new FloatProcessor(currentWidth, currentHeight, (float[])problem.getData(imageIndex, imageSlice), null);
                }
                else {
                    throw new Exception("Unsupported image type");
                }

                stack.addSlice("", ip);
            }//end of slice

            combinedImage = new ImagePlus("Combined", stack);
            
            img = Image.wrap(combinedImage);
        
            stats.run(img);
            
            //For testing purposes
            //img.imageplus().show();   	
        
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
		return true;
	}
    
}
