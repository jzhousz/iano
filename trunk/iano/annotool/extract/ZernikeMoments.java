package annotool.extract;

import java.util.ArrayList;
import java.util.HashMap;

import annotool.ImgDimension;
import annotool.io.DataInput;

/*
 * This class calculates the normalized Zernike Moments of a set of images 
 * according to the order given in the class definition.
 * 
 * Calculation of the moments is accomplished using open source 
 * code from Columbia College 2001 author: Lijun Tang.
 * http://read.pudn.com/downloads11/sourcecode/math/45771/zernike.java__.htm
 *
 * Ref: "A neural network classifier capable of recognizing the patterns of all major subcellular
 *    structures in fluorescence microscope images of HeLa cells",  M.V. Boland and R. F. Murphy
 *    Bioinfomratics, 2001.
 */
 
public class ZernikeMoments implements FeatureExtractor {
	int ORDER = 7;
	int length;
	protected float[][] features = new float[length][1];
	public static final float M_PI=3.1415926535f;   
	 
	int imageType;
	int stackSize;
	DataInput problem = null;
	ArrayList allData = null;
	int mask = 0xff;
	int longmask = 0xffff;
	int width, height; //for current image

	/**
	* Sets algorithm parameters from para 
	* 
	* @param  para  Each element of para holds a parameter’s name for its key
	*               and a parameter’s value for its value. The parameters
	*               should be the same as those in the algorithms.xml file.
	*/
	@Override
	public void setParameters(HashMap<String, String> para) {
			//
	}

	/**
	 * Get features based on raw image stored in problem.
	 * 
	 * @param   problem    Image to be processed
	 * @return             Array of features
	 * @throws  Exception  Optional, generic exception to be thrown
	 */
	@Override
	public float[][] calcFeatures(DataInput problem) throws Exception {
		
		this.problem = problem;
		this.length = problem.getLength();
		if (problem.ofSameSize() != false) //check for images in set to be same height.
		{
			this.width  =  problem.getWidth();
			this.height  = problem.getHeight();
		}
		this.imageType = problem.getImageType();
		features = new float[length][numFeatures(ORDER)];
	
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
	public float[][] calcFeatures(ArrayList allData, int imageType, ImgDimension dim) throws Exception {
		
		this.allData = allData;
		this.length = allData.size();
		this.width = dim.width;
		this.height = dim.height;
		this.imageType = imageType;
		
		features = new float[length][numFeatures(ORDER)];
		
		return calcFeatures();
	}
	
		@Override
	public boolean is3DExtractor() {
			// default to false for now
			return false;
	}

	
	
	///////////////////////////////////Private///////////////////////////////////////
	
	
	/**
	* calcFeatures()
	* Calculate features using zernike moment code adapted from Columbia code
	* Loops to process each image, gets image data, calls getZernikeMoments
	* on each image.
	*
	* @return 			features for all images
	*                   2d array of all features. (in this case 20 features x length of image set)
	*/
	protected float[][] calcFeatures() throws Exception {
		Object data;
		float[][] features = new float[this.length][numFeatures(ORDER)];
		
		//get image dimensions and data for each image, then get the
		//moments and put in feature array.
		for (int image_num = 0; image_num < this.length; image_num++) 
		{
			//dimension adjust?
	       	if (problem !=null)
        	{
        		if (!problem.ofSameSize())
        		{  	//set the size for this image
        		 this.height = problem.getHeightList()[image_num];
        		 this.width = problem.getWidthList()[image_num];
        		}
        	}
			
			//get data
            if (problem !=null)
                data = problem.getData(image_num,1);
            else  //alldata is not null
                data = allData.get(image_num);			
			
			//getZerMom returns a 1d array
			features[image_num] = getZernikeMoments( ORDER, data ); 
		}
			
		return features;
	}
	
	
	/**
	* numFeatures()
	* Calculate number of features for given order
	*
	* @return       number of features based on order
	*/
	private int numFeatures(int order) {
			int m;
			int num = 0;
			while(order >= 0) {
			  for (m=0; m<=order; m++) {   
			    if ((order-m) %2 == 0)  
					num++;
			  }
			  order--;
			}
			System.out.println(" Number of features is: " + num);
			return num;
	}
	
	/**
	* getZernikeMoments()
	* Calculate m,n pairs, call zer_mom on pair for image data.
	*
	* @param order  order to compute moments up to.
	* @param data   image data passed in
	* @return       moments up to order for image
	*/
	private float[] getZernikeMoments( int n, Object data ) {
	//see Columbia zer_mmts()
		
		int m;
		int p;
		int countMoments = 0;
		int pairCount = 0;
		int orderCount = n;
		
		//get number of n,m pairs
		while(orderCount >= 0)
		{
			for (m=0; m<=orderCount; m++)   
			{   
				if ((orderCount-m) %2 == 0)  
					{
					pairCount++;
					System.out.println("pair: " + orderCount + ", " + m);
					}
			}
			orderCount--;
		}
		System.out.println(" number of pairs; " + pairCount);
		DCOMPLEX[] rawMoments = new DCOMPLEX[pairCount];
		float [] result = new float[pairCount];

	
		//get the actual moments and put them in rawMoments[]
		orderCount = n;
		while(orderCount >= 0)
		{			
			for (m=0; m<=orderCount; m++)   
			{   
				if ((orderCount-m) %2 == 0)   
				{
				//if zer_mom is DCOMPLEX
				rawMoments[countMoments] = zer_mom(orderCount, m, data); 
				countMoments++;
				}
			}
			orderCount--;
		}
		
		//normalize rawMoment into a final result
		//by changeing to magnitude 
	    for( int i=0; i < countMoments; i++)
			result[i] = (float) Math.sqrt(rawMoments[i].re*rawMoments[i].re + rawMoments[i].im*rawMoments[i].im);
				
		return result;
	}
	

	/**
	* Calculate the Zernike moment for an image: A(n, m).
 	* 
	* This assumes the center of the image as the centroid for calculation
	* @return zernike moment for V(n,m,x,y)
	*/
	private DCOMPLEX zer_mom(int n, int m, Object data) {
	
		DCOMPLEX res = new DCOMPLEX();
		
		//adapted logic from reference code
		int i,j;   
		int i_0, j_0;   
		double i_scale, j_scale;   
		double x,y;
		int count = 0;
		int isize = width, jsize = height;   
		  
		DCOMPLEX v=new DCOMPLEX();   
		//  v[0]=new DCOMPLEX();   
		   
		if ((n<0) || (Math.abs(m) > n) || ((n-Math.abs(m))%2!=0))   
		    System.out.print("zer_mom: n=%i, m=%i, n-|m|=%i\n"+ n+ m+(n-Math.abs(m)));   
		   
		i_0 = (isize+1)/2;   
		j_0 = (jsize+1)/2;   
		i_scale = isize/2.0;   
		j_scale = jsize/2.0;   
		   
		res.re = 0.0f;   
		res.im = 0.0f;   
		int[] src_1d=new int[isize*jsize];   
		   
		//Raster rt=inband.getData();   
		//rt.getSamples(0,0,isize,jsize,0,src_1d);   
		//  getRGB(0,0,isize,jsize,src_1d,0,1);   
		   
		for (i=1; i<=isize; i++)
		{   
		  for (j=1; j<= jsize; j++)   
		  {   
		    x = (i-i_0)/i_scale;   
		    y = (j-j_0)/j_scale;   
		   
		/*  printf ("zer_mom: x=%6.3f y=%6.3f i=%i j=%i\n", x,y,i,j);  
		*/   
//		    int value=inband.getData().getSample(j ,i,0);   
//		   int value=src_1d[(i-1)*jsize+j-1]==BKGRND?0:1;
		    
		    if(getValue(data, (i-1)*jsize+j-1) == 0)  continue;
		    //    if(src_1d[(i-1)*jsize+j-1]==BKGRND) continue;   
		    if (((x*x + y*y) <= 1.0))   
		      {   
		        v = ZernikeBasis(n,m,x,y);   
		        res.re += v.re;   
		        res.im += (-v.im);
				count++;
		      }   
		  }   
//		      System.out.print("i="+i+"\n");   
		 }   
		 
		 //normalize the momemnts with respect to pixels counted
		 res.re = res.re*(n+1)/M_PI/count;   
		 res.im = res.im*(n+1)/M_PI/count;   
		  
		return res;
	}
	
	
	/**
	* Calculate the value of a pixel normalized from various
 	* gray scale forms
	*
	* @return value of a pixel
	*/
	private int getValue(Object imgData, int arrayIndex)
	{
		int value =0;
		if (imageType == DataInput.GRAY8 || imageType ==  DataInput.COLOR_RGB)
			value = (int) (((byte[])imgData)[arrayIndex] & mask);
		else if (imageType == DataInput.GRAY16)
			value = (int) (((short[])imgData)[arrayIndex] & longmask);
		return value;
	}
	
	/**
	* Calculate the Zernike basis function for an image of order {n,m}.
 	* V(n,m,x,y)
	*
	* @return zernike moment for V(n,m,x,y)
	*/
	private DCOMPLEX ZernikeBasis(int n, int m, double x, double y) {
	
		//calculate V(n,m,x,y) 
		DCOMPLEX res = new DCOMPLEX();
	    	
		double[] R=new double[1];   
		double arg;   
		   
		  if ((x*x + y*y) > 1.0)   
		    {   
		      res.re = 0.0f;   
		      res.im = 0.0f;   
		    }   
		  else   
		    {   
		      zer_pol_R(n,m,x,y, R);   
		      arg = m*Math.atan2(y,x);   
		      res.re = (float) (R[0]*Math.cos(arg));   
		      res.im = (float) (R[0]*Math.sin(arg));   
		    }   
		    

		return res; //basis function;

	}

	/**  
	 * zer_pol_R() compute the Rnm(p) in polynomial definition of V(n,m,x,y)  
	 * Definition of Rnm(p) refer to &[1]  
	 *
	 * @return res[0] as the value of Rnm(p)  
	 */   
	  public static int zer_pol_R(int n, int m_in, double x, double y, double[] res)
	  {
		  int i;   
		  int m;   
		  int s;   
		  int a; /* (n-s)! */   
		  int b; /*   s!   */   
		  int c; /* [(n+|m|)/2-s]! */   
		  int d; /* [(n-|m|)/2-s]! */   
		  int sign;   
		   
		  m = Math.abs(m_in);   
		   
		  if ((n-m)%2!=0)   
		    System.out.print(   
		        "zer_pol_R: Warning. R(%i,%i,%5.2f,%5.2f). n-|m| = %i is odd!\n"+n+m+x+y+m);   
		   
		  /* The code is optimized with respect to the faculty operations */   
		   
		  res[0] = 0.0;   
		  if ((x*x + y*y) <= 1.0)   
		    {   
		      sign = 1;   
		      a = 1;   
		      for (i=2; i<=n; i++)   
		    a*=i;   
		      b=1;   
		      c = 1;   
		      for (i=2; i <= (n+m)/2; i++)   
		    c*=i;   
		      d = 1;   
		      for (i=2; i <= (n-m)/2; i++)   
		    d*=i;   
		   
		      /* Before the loop is entered, all the integer variables (sign, a, */   
		      /* b, c, d) have their correct values for the s=0 case. */   
		      for (s=0; s<= (n-m)/2; s++)   
		    {   
		      /*printf("zer_pol_R: s=%i, n=%i, m=%i, x=%6.3f, y=%6.3f, a=%i, */   
		      /*b=%i, c=%i, d=%i, sign=%i\n", s,n,m,x,y,a,b,c,d,sign); */   
		      res[0] += sign * (a*1.0/(b*c*d)) * Math.pow((x*x + y*y),(n/2.0)-s);   
		   
		      /* Now update the integer variables before the next iteration of */   
		      /* the loop. */   
		   
		      if (s < (n-m)/2)   
		        {   
		          sign = -sign;   
		          a = a/(n-s);   
		          b = b*(s+1);   
		          c = c / ((n+m)/2 - s);   
		          d = d / ((n-m)/2 - s);   
		        }   
		    }   
		    }   
		  return 0;     
	  }
		
	//test function to see results of features[][]
	/**
	* Test the zernike moment feature extraction 
 	* and print the results.
	*/
	public static void main(String[] args)
        {
                String directory = "J:\\BioCAT\\Work\\Feature extractor work\\testSet2\\";
                String ext = ".png";
                String ch = "g";
				System.out.println( "directory set" ); 
                try
                {
                        annotool.io.DataInput problem = new annotool.io.DataInput(directory, ext, ch, true);
                        FeatureExtractor fe = new ZernikeMoments();
                        //print out
                        float[][] features = fe.calcFeatures(problem); 
                        System.out.println();
                        for(int i = 0; i < features.length; i++)
                        {
                                System.out.print("\nImage #" + (i+1) + ": \n");
                                for(int j=0; j< features[i].length; j++){
										System.out.print("\n");
                                        System.out.printf("%-12f", features[i][j]);
										}
                                System.out.println();
                        }
						
						//specific moments
						System.out.print("\nSpecific moments\n");
						System.out.println("(0,0)        (3,1)        (4,2)");
						
						for(int i = 0; i < features.length; i++)
						{
							System.out.print("\nImage #" + (i+1) + ": \n");
							System.out.printf("%-12f", features[i][8]);
							System.out.printf("%-12f", features[i][3]);
							System.out.printf("%-12f", features[i][1]);
							System.out.print("\n\n");
							
						}
						
                }catch(Exception e)
                {
                        e.printStackTrace();
                        System.out.println(e.getMessage());
                }
        }

} // endclass ZernikeMoment


/* helper class to store a zernike moment as a complex number.
*/
class DCOMPLEX 
{   
	float re, im;   
	public DCOMPLEX() {   
		re=(float)0.0;   
		im=(float)0.0;   
    }
} // endClass DCOMPLEX
   