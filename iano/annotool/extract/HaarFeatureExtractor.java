package annotool.extract;

import annotool.Util;


/**

  2D Discrete Wavelet Transform to Extract Features Using Haar (DB1) Base

**/
public class HaarFeatureExtractor implements FeatureExtractor
{
   protected float[][] features = null;
   protected byte[][] data;
   int totallevel = 1;
   int totalwidth;
   int totalheight;
   int length;
   boolean singleImage = false; //handle many images by default
   protected byte[] singleData;
   public final static String LEVEL_KEY = "Wavelet Level";
   
   
   public HaarFeatureExtractor(annotool.io.DataInput problem, java.util.HashMap<String, String> parameters)
   {
	   data = problem.getData();
	   length = problem.getLength();
	   totalwidth = problem.getWidth();
	   totalheight = problem.getHeight();
	   //parse the parameters to set the wavelet level
	   if (parameters != null && parameters.containsKey(LEVEL_KEY))
	     totallevel = Integer.parseInt(parameters.get(LEVEL_KEY));
	   
   }

   //get the first stack
/*   public HaarFeatureExtractor(annotool.io.DataInput problem, int level)
   {
	   data = problem.getData();
	   length = problem.getLength();
	   totalwidth = problem.getWidth();
	   totalheight = problem.getHeight();
	   totallevel = level;
   }
  */ 
   public HaarFeatureExtractor(annotool.io.DataInput problem, int level, int stackindex)
   {
	   data = problem.getData(stackindex);
	   length = problem.getLength();
	   totalwidth = problem.getWidth();
	   totalheight = problem.getHeight();
	   totallevel = level;
   }

   public HaarFeatureExtractor(byte[][] data, int length, int width, int height)
   {
	   totallevel = 2; //default wavelet level
	   totalwidth = width;
	   totalheight = height;
	   this.data = data;
	   this.length = length;
   }

   public HaarFeatureExtractor(int level, byte[][] data, int length, int width, int height)
   {
   	   totallevel = level;
	   totalwidth = width;
	   totalheight = height;
	   this.data = data;
	   this.length = length;
  }
   
   //pass one image
   public HaarFeatureExtractor(int level, byte[] data, int width, int height)
   {
   	   totallevel = level;
	   totalwidth = width;
	   totalheight = height;
	   this.length = 1;
	   this.singleData = data;
	   this.singleImage = true;
 }
   
   public float[][] calcFeatures()
   {
	    features  = new float[length][totalwidth*totalheight]; //In Matlab, an 50*100 image has 5050 features due to rounding.
        if(!singleImage)
        {
	       for(int i=0; i <length; i++)
            getHaarFeatureOfOneImage(data[i], features[i]);
        }
        else 
        {
        	getHaarFeatureOfOneImage(singleData, features[0]);
        }
        return features;

   }

   protected void getHaarFeatureOfOneImage(byte[] data, float[] feature)
   {
	    //copy data to feature,
	    //toFloat() of ColorProcessor does the same loop
	    for(int i = 0; i< totalwidth*totalheight; i++)
	       feature[i] = data[i]&0xff;

		//added 03232010:  standardize the image first, so that discreize may be easiler
	    //scale based on 0-255.
		Util.scaleAnImage(feature);
		
        haarTransform(feature,  totalwidth, totalheight, totallevel, totalwidth);

        //return total number of features ..

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

		 //No risk of arrayoutofbound excpetion here but boundary may be lost. Example: height =9; i= 0-3; k+1 can be up to 7 (row 8).

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

         //delete[] temp;

         if (level > 1)
         {
             //recursively call data, the top-left  1/4 will be changed.
             //note that different from Matlab, 1D storage of data has unchanged width
             haarTransform(data, wid/2, height/2, level -1, origWid);
         }
   }


   /** get the features. If they were not calculated, calcFeatures() is called first. **/
   public float[][] getFeatures()
   {
	  if (features == null)
	    return calcFeatures();
	  else
        return features;
   }

}