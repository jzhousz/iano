package annotool.extract;

import annotool.ImgDimension;
import java.util.ArrayList;
import annotool.Util;
import annotool.io.DataInput;


/**

  2D Discrete Wavelet Transform to Extract Features Using Haar (DB1) Base

**/
public class HaarFeatureExtractor implements FeatureExtractor
{
   protected float[][] features = null;
   protected ArrayList data;
   int totallevel = 1;
   int totalwidth;
   int totalheight;
   int length;
   boolean singleImage = false; //handle many images by default
   protected byte[] singleData;
   public final static String LEVEL_KEY = "Wavelet Level";
   boolean workOnRawBytes = true; //work as the first feature extractor by default
   int imageType;
   
   /**
    * Default constructor
    */
   //parameters will be set by calling setter
   public HaarFeatureExtractor()
   {}
   
   /**
    * Sets algorithm parameters from para 
    * 
    * @param  para  Each element of para holds a parameter’s name for its key
    *               and a parameter’s value for its value. The parameters
    *               should be the same as those in the algorithms.xml file.
    */
   public void  setParameters(java.util.HashMap<String, String> para)
   {
    if (para != null && para.containsKey(LEVEL_KEY))
	     totallevel = Integer.parseInt(para.get(LEVEL_KEY));
   }

   /**
    *  This method calls calcFeatures(DataInput problem). 
    * 
    * @param   data       Data taken from the image
    * @param   problem    Image to be processed
    * @return             Array of features
    * @throws  Exception  
    */
   //not used?
  public float[][] calcFeatures(float[][] data, DataInput problem) throws Exception
  {
 	  this.features = data;
	  workOnRawBytes = false;
	  return calcFeatures(problem);
  }
  
  /**
   * Get features based on raw image stored in problem.
   * 
   * @param   problem    Image data to be processed
   * @return             Array of features
   * @throws  Exception  thrown when the data has a problem or not compatible with the algorithm e.g. images are of various size
   */
  public float[][] calcFeatures(DataInput problem) throws Exception
  {
	 //check if the extractor can handle this problem.
	 if (problem.ofSameSize() == false)
		throw new Exception("The Haar feature extractor has to work with images of same dimension.");
	  
	  totalwidth = problem.getWidth();
	  totalheight = problem.getHeight();
	  this.length = problem.getLength();
      this.imageType = problem.getImageType();
	  if(workOnRawBytes)
   	   data = problem.getData();
      
	  return calcFeatures();
  }
  
  /**
   * Get features based on data passed in as ArrayList, imageType, and dim.
   * 
   * @param   data       Data taken from the image
   * @param   imageType  Type of the image
   * @param   dim        Dimensions of the image
   * @return             Array of features
   * @throws  Exception  
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

   private float[][] calcFeatures() throws Exception
   {
	    if(features == null)
	       features  = new float[length][totalwidth*totalheight]; //In Matlab, an 50*100 image has 5050 features due to rounding.
	    
	    if(workOnRawBytes)
	    {	
          if(!singleImage)
          {
	       for(int i=0; i <length; i++)
            getHaarFeatureOfOneImage(data.get(i), features[i]);
          }
          else 
        	getHaarFeatureOfOneImage(singleData, features[0]);
	    }
	    else //work as 2nd or 3rd feature extractor
	       for(int i=0; i <length; i++)
            getHaarFeatureOfOneImage(features[i]);

	    return features;

   }

   //add ImageType on 1/24/2011, called by StackSimpleHaarFeature
   private void getHaarFeatureOfOneImage(Object datain, float[] feature) throws Exception
   {
	   //copy data to feature,
	    //toFloat() of ColorProcessor does the same loop
	    if(imageType == DataInput.GRAY8 || imageType == DataInput.COLOR_RGB)
	    {
	      byte[] data = (byte[]) datain;
	      for(int i = 0; i< totalwidth*totalheight; i++)
	         feature[i] = (float) (data[i]&0xff);
	    }
	    else if(imageType == DataInput.GRAY16)
	    {
	    	short[] data = (short[]) datain;
 	        for(int i = 0; i< totalwidth*totalheight; i++)
 	    	  feature[i] = (float) (data[i]&0xffff);
	    }	
 	    else if(imageType == DataInput.GRAY32)
 	    {
	    	float[] data = (float[]) datain;
 	        for(int i = 0; i< totalwidth*totalheight; i++)
 	        {
 	 	    	  feature[i] = (float) data[i];
 	 	    	  //System.out.println(i+":"+feature[i]);
 	        }
 	    }
 	    else
 	    {
 	    	throw new Exception("Unsuppored Image Type for Haar Feature Extractor");
 	    }

		//added 03232010:  standardize the image first, so that discretize may be easier
	    //scale based on 0-255.
		Util.scaleAnImage(feature);
		
        haarTransform(feature,  totalwidth, totalheight, totallevel, totalwidth);
   }

   
   protected void getHaarFeatureOfOneImage(float[] feature)
   {
		Util.scaleAnImage(feature);
		
        haarTransform(feature,  totalwidth, totalheight, totallevel, totalwidth);
   }
   
   /* recursive function to do multilevel HaarTransform.
      feature is passed back via argument.

      origWid: Original width of the 1D storage of image. Needed for correctly accessing
      the data for level >= 2.

     Q: TBD: How is the rounding done in Matlab for wid/height that are not powers of 2?
   */
   private void  haarTransform(float[] data, int wid, int height, int level, int origWid)
   {

         if (level < 1)
              return;

         float[] temp = new float[wid*height];
         int i, j, k;


         //process rows (addition and subtraction of neighboring rows) and put into temp
         //System.out.println("height/2:"+height/2);
		 //System.out.println("wid"+wid);
		 //No risk of arrayoutofbound exception here but boundary may be lost. Example: height =9; i= 0-3; k+1 can be up to 7 (row 8).

         for(i = 0; i < height/2; i++)
         {
			k = 2*i;
            for(j = 0; j < wid; j++)
            {
               temp[i*wid+j] = (data[k*origWid+j] + data[(k+1)*origWid+j])/2;  //top half
               //System.out.println("(height/2+i)*origWid+j - " + (height/2+i)*origWid+j);
           //    System.out.println("k*origWid+j - " + k*origWid+j);
           //    System.out.println("(k+1)*origWid+j - " + (k+1)*origWid+j);
               temp[(height/2+i)*wid+j] = (data[k*origWid+j] - data[(k+1)*origWid+j])/2;   //lowerhalf
            }
         }

         //process columns and put back to data
         for(i = 0; i < height; i++)
              for(j=0; j< wid/2; j++)
              {
                k = 2*j;
                data[i*origWid+j] = (temp[i*wid+k] + temp[i*wid+(k+1)])/2; //left half
                data[i*origWid+wid/2+j] =  (temp[i*wid+k]- temp[i*wid+k+1])/2;  //right half
              }

         temp = null;

         if (level > 1)
         {
             //recursively call data, the top-left  1/4 will be changed.
             //note that different from Matlab, 1D storage of data has unchanged width
             haarTransform(data, wid/2, height/2, level -1, origWid);
         }
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
   {  return false; } 

}