package annotool.extract;

import annotool.ImgDimension;
import annotool.io.DataInput;

import java.util.ArrayList;

/**
 * This class calculates 8 simple statistics features: 
 * mean and standard deviation of 4 divisions: left, right, upper, down
 */
public class SimpleFeatureExtractor implements FeatureExtractor {

	protected float[][] features = null;
	protected ArrayList data;
	int totalwidth;
	int totalheight;
	int length;
	int imageType;

	/**
	 * Default constructor
	 */
	public SimpleFeatureExtractor()
	{}

    /**
     * Empty implementation of setParameters 
     * 
     * @param  para  Each element of para holds a parameter’s name for its key
     *               and a parameter’s value for its value. The parameters
     *               should be the same as those in the algorithms.xml file.
     */
	public void setParameters(java.util.HashMap<String, String> parameters)
	{}
	
    /**
     * Get features based on raw image stored in problem.
     * 
     * @param   problem    Image to be processed
     * @return             Array of features
     * @throws  Exception  Optional, generic exception to be thrown
     */
	@Override
	public float[][] calcFeatures(annotool.io.DataInput problem) throws Exception
	{
		totalwidth = problem.getWidth();
		totalheight = problem.getHeight();
		this.length = problem.getLength();
		this.data = problem.getData();
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
	public float[][] calcFeatures(ArrayList data, int imageType, ImgDimension dim) throws Exception
	{
		totalwidth = dim.width;
		totalheight = dim.height;
		this.length = data.size();
		this.data = data;
		this.imageType = imageType;
		return calcFeatures();
	}
	
	private float[][] calcFeatures() throws Exception {
		// calculate simple 8 features: mean and st of 4 divisions: left, right, upper, down
		features  = new float[length][8]; 
		for(int i=0; i <length; i++)
			getSimpleFeatureOfOneImage(data.get(i), features[i]);

		return features;

	}

	// calculate simple 8 features: mean and st of 4 divisions: left, right, upper, down
	protected void getSimpleFeatureOfOneImage(Object data, float[] feature) throws Exception
	{
		//feature 0&1: left
		feature[0] = calMean(data,0,totalwidth/2,0,totalheight,totalwidth);
		feature[1] = calStd(data,0,totalwidth/2,0,totalheight,totalwidth, feature[0]);

		//feature 2&3: right
		feature[2] = calMean(data,totalwidth/2+1,totalwidth/2,0,totalheight,totalwidth);
		feature[3] = calStd(data,totalwidth/2+1,totalwidth/2,0,totalheight,totalwidth, feature[2]);

		//feature 4&5: top
		feature[4] = calMean(data,0,totalwidth,0,totalheight/2,totalwidth);
		feature[5] = calStd(data,0,totalwidth,0,totalheight/2,totalwidth, feature[4]);

		//feature 6&7: bottom
		feature[6] = calMean(data,0,totalwidth,totalheight/2+1,totalheight,totalwidth);
		feature[7] = calStd(data,0,totalwidth,totalheight/2+1,totalheight,totalwidth, feature[6]);
	}

	private float calMean(Object indata, int left, int right, int top, int bottom, int totalwidth) throws Exception
	{
	  float mean = 0;	

	 if(imageType == DataInput.GRAY8 || imageType == DataInput.COLOR_RGB)
       {
    	  byte[]data  = (byte[]) indata;
		  for(int i = left; i < right; i++)
			for (int j = top; j < bottom; j++)
				mean += data[i*totalwidth + j]&0xff;
		  mean /= (right-left+1)*(bottom-top+1);
       }
	 else if(imageType == DataInput.GRAY16)
     {
  	  int[] data  = (int[]) indata;
		  for(int i = left; i < right; i++)
			for (int j = top; j < bottom; j++)
				mean += data[i*totalwidth + j]&0xffff;
		  mean /= (right-left+1)*(bottom-top+1);
     }
	 else if(imageType == DataInput.GRAY32)
     {
  	  float[] data  = (float[]) indata;
		  for(int i = left; i < right; i++)
			for (int j = top; j < bottom; j++)
				mean += data[i*totalwidth + j];
		  mean /= (right-left+1)*(bottom-top+1);
     }
	 else
		 throw new Exception ("Not supported image type for extraction.");
	 
	 return mean;
	}

	private float calStd(Object indata, int left, int right, int top, int bottom, int totalwidth, float mean) throws Exception
	{
		float std = 0;	
 	    if(imageType == DataInput.GRAY8 || imageType == DataInput.COLOR_RGB)
	    {
	      byte[] bdata  = (byte[]) indata;
		  for(int i = left; i < right; i++)
			for (int j = top; j < bottom; j++)
			{
				float value = bdata[i*totalwidth + j]&0xff;
				std += (value - mean)*(value - mean);
			}
		    std /= (right-left+1)*(bottom-top+1);
	    }
 	    else if(imageType == DataInput.GRAY16)
 	    {
 	  	  int[] idata  = (int[]) indata;
 		  for(int i = left; i < right; i++)
 			for (int j = top; j < bottom; j++)
 			{
 				float value = idata[i*totalwidth + j]&0xffff;
 				std += (value - mean)*(value - mean);
 			}
 			std /= (right-left+1)*(bottom-top+1);
 		 }
 	     else if(imageType == DataInput.GRAY32)
 	     {
 	  	   float[] fdata  = (float[]) indata;
		   for(int i = left; i < right; i++)
 			 for (int j = top; j < bottom; j++)
 			 {
 				float value = fdata[i*totalwidth + j];
 				std += (value - mean)*(value - mean);
 			 }
 		   std /= (right-left+1)*(bottom-top+1);
 		 }
 		 else
 			 throw new Exception("Not supported image type for extraction.");
 	    
		return (float)Math.sqrt(std);
	}

	/** get the features. If they were not calculated, calcFeatures() is called first. **/
	public float[][] getFeatures() throws Exception
	{
		if (features == null)
			return calcFeatures();
		else
			return features;
	}    

    /**
     * Returns whether or not the algorithm is able to extract from a 3D image 
     * stack. 
     * 
     * @return  <code>True</code> if the algorithm is a 3D extractor, 
     *          <code>False</code> if not. Default is <code>False</code>
     */
	public boolean is3DExtractor()
	{  return false;} 
}


