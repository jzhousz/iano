package annotool.extract;

import ij.plugin.filter.BackgroundSubtracter;
import ij.plugin.filter.RankFilters;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.util.ArrayList;
import java.util.HashMap;

import annotool.ImgDimension;
import annotool.io.DataInput;

/**
 * Extracts the object territory from the image.
 * The number of object pixels is the extracted feature.
 * 
 */
public class Territory  implements FeatureExtractor {
	protected float[][] features = null;
	DataInput problem = null;
	double radius = 50;			//Default value for radius of the rolling ball
	int length;
	int imageType;
	int width;
	int height;
	
	protected ArrayList data = null;
	
	public final static String RADIUS_KEY = "Radius";

   /**
    * Sets algorithm parameters from para 
    * 
    * @param  para  Each element of para holds a parameter’s name for its key
    *               and a parameter’s value for its value. The parameters
    *               should be the same as those in the algorithms.xml file.
    */
	@Override
	public void setParameters(HashMap<String, String> parameter) {
		if (parameter != null)
		    if(parameter.containsKey(RADIUS_KEY)) 
		    	radius = Double.parseDouble(parameter.get(RADIUS_KEY));		
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
		//this.data = problem.getData();
		this.imageType = problem.getImageType();
		this.features = new float[length][1];
		this.width = problem.getWidth();
		this.height = problem.getHeight();
		
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
	
	public float[][] calcFeatures(ArrayList data, int imageType,
ImgDimension dim) throws Exception {
		this.data = data;
		this.length = data.size();
		this.imageType = imageType;
		this.features = new float[length][1];
		this.height = dim.height;
		this.width = dim.width;
		
		return calcFeatures();
	}
	
	protected float[][] calcFeatures() throws Exception {		

		ImageProcessor ip = null;
		RankFilters filter = new RankFilters();
		BackgroundSubtracter bs = new BackgroundSubtracter();
		int[][] binaryData = null;
		
		for(int imageIndex = 0; imageIndex < length; imageIndex++) {
        	if (problem !=null)
        	{
        		if (!problem.ofSameSize())
        		{  	//set the size for this image
        		 this.height = problem.getHeightList()[imageIndex];
        		 this.width = problem.getWidthList()[imageIndex];
        		}
        	}
			if(imageType == DataInput.GRAY8 || imageType == DataInput.COLOR_RGB) {
				if (data != null)
				 ip = new ByteProcessor(width, height, (byte[])data.get(imageIndex), null);
				else 
				 ip = new ByteProcessor(width, height, (byte[])problem.getData(imageIndex, 1), null);
		    }
		    else if(imageType == DataInput.GRAY16) {
		    	if (data != null)
		    	 ip = new ShortProcessor(width, height, (short[])data.get(imageIndex), null);
		    	else
		    	 ip = new ShortProcessor(width, height, (short[])problem.getData(imageIndex,1), null);
		    }	
	 	    else if(imageType == DataInput.GRAY32) {
	 	    	if (data != null)
		    	 ip = new FloatProcessor(width, height, (float[])data.get(imageIndex), null);
	 	    	else
			     ip = new FloatProcessor(width, height, (float[])problem.getData(imageIndex, 1), null);
	 	    }
	 	    else {
	 	    	throw new Exception("Unsuppored image type");
	 	    }
		
		/*for (int imageIndex = 0; imageIndex < this.length; imageIndex++) {
			if (imageType == DataInput.GRAY8 || imageType ==  DataInput.COLOR_RGB)
	            for (int i = 0; i < width*height; i++) 
	            	ip = new ByteProcessor(width, height, (byte[])data.get(imageIndex), null);
	        else if (imageType == DataInput.GRAY16)
	            for (int i = 0; i < width*height; i++) 
	            	ip = new FloatProcessor(width, height, (int[])data.get(imageIndex));
	        else if (imageType == DataInput.GRAY32)
	            for (int i = 0; i < width*height; i++) 
	            	ip = new FloatProcessor(width, height, (float[])data.get(imageIndex), null);
	        else
	              throw new Exception("Not supported image type: type "+imageType);
		 */	
			//Apply despeckle : median filter with radius 1			
			filter.rank(ip, 1, RankFilters.MEDIAN);
			
			//Subtract background			
			//First: light background
			bs.rollingBallBackground(ip, this.radius, false, true, false, true, false);
			//Second: create background (don't subtract)
			bs.rollingBallBackground(ip, this.radius, true, false, false, true, false);	//Object pixels will be black and background white

			//Threshold
			ip.autoThreshold();

			binaryData = ip.getIntArray();

			//Count object pixels
			int count = 0;			
			for(int x = 0; x < ip.getWidth(); x++)
			    for(int y=0; y < ip.getHeight(); y++)
			    	if(binaryData[x][y] == 0)
			    		count++;
			
			features[imageIndex][0] = (float)count;
		}
		
		for(int i = 0; i < features.length; i++)
			System.out.println("Feauture " + (i + 1) + ": " + features[i][0]);
		
		return features;
	}

    /**
     * Returns whether or not the algorithm is able to extract from a 3D image 
     * stack. 
     * 
     * @return  <code>True</code> if the algorithm is a 3D extractor, 
     *          <code>False</code> if not. Default is <code>TFalse</code>
     */

	public boolean is3DExtractor() {
		return false;
	}

}
