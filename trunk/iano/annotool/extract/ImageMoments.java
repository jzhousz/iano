package annotool.extract;

import annotool.ImgDimension;
import annotool.io.DataInput;
import java.util.ArrayList;

/**
 *  This class calculates Hu's moments (seven, plus one more)
 *  http://en.wikipedia.org/wiki/Image_moment#Rotation_invariant_moments
 */
//8/18/2012: deal with different image size
//8/20/2012: Avoid converting to 2D array.
public class ImageMoments implements FeatureExtractor
{
    protected ArrayList alldata = null;    //if pass via ArrayList instead of DataInput
    int totalwidth;             //width of images
    int totalheight;            //height of images
    int length;                 //number of images
    int imageType;              //the type of image (defined in DataInput)
    DataInput problem = null;
 
    //features to return
    protected float[][] features = null;

    /**
     * Empty implementation of setParameters 
     * 
     * @param  para  Each element of para holds a parameter’s name for its key
     *               and a parameter’s value for its value. The parameters
     *               should be the same as those in the algorithms.xml file.
     */
    public void  setParameters(java.util.HashMap<String, String> parameters)
    {}

    /**
     * Get features based on raw image stored in problem.
     * 
     * @param   problem    Image to be processed
     * @return             Array of features
     * @throws  Exception  Optional, generic exception to be thrown
     */
    public float[][] calcFeatures(DataInput problem) throws Exception
    {
		this.problem = problem;
        length = problem.getLength();
        
		if (problem.ofSameSize() != false)
		{
		  this.totalwidth  =  problem.getWidth();
		  this.totalheight  = problem.getHeight();
		}
        imageType = problem.getImageType();
        features = new float[length][8];
 
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
        this.alldata = data;
        length = data.size();
        totalwidth = dim.width;
        totalheight = dim.height;
        features = new float[length][8];
        this.imageType = imageType;
 
    	return calcFeatures();
    }

    //calculates raw image moments
    protected double m_pq(Object data, int p, int q, int width, int height) throws Exception {
        CompSumDouble m = new CompSumDouble(); //compensated sum
        int size = width * height;  
        int x, y;
        int mask = 0xff; 
        int longmask = 0xffff;
    	for(int i = 0; i < size; i++) {
    		x = i % width;
    		y = i / width;
    		if (imageType == DataInput.GRAY8 || imageType ==  DataInput.COLOR_RGB)
    			m.Add(getLowerPower(x, p) * getLowerPower(y, q) * (float) (((byte[])data)[i] & mask));
    		else if (imageType == DataInput.GRAY16)
    			m.Add(getLowerPower(x, p) * getLowerPower(y, q) * (float) (((short[])data)[i] & longmask));
    		else if (imageType == DataInput.GRAY32)
    			m.Add(getLowerPower(x, p) * getLowerPower(y, q) * ((float[])data)[i]);
    		else
    			throw new Exception("Not supported image type in Moments. Type = " + imageType);
    	}

        return m.getSum();
    }
    
    //Works with power 0, 1 and 2
    private double getLowerPower(double base, int power) {
    	if (power == 0)
    		return 1;
    	else if(power == 1)
    		return base;
    	else if(power == 2)
    		return base * base;
    	else
    		return 0;
    }

    //the next function calculate the centered values for x and y,
    // based on mean x and mean y.  This way, instead of subtracting the mean
    //from x or y each time when I calculate the central moments,
    //(essentially doing it M*N times for every one), I pre-calculate them
    //in advance (M+N times).  Trading a little memory for a little speed...
    protected static double[] getCenter(int size, double y_mean, double[] centered_y) {
        for (int y = 0; y < size; y++) {
            centered_y[y] = y - y_mean;
        }
        return centered_y;
    }

    //calculates central moments
    protected double mu_pq(Object data, int p, int q, int width, int height, double[] centered_x, double[] centered_y) throws Exception {

        CompSumDouble mu = new CompSumDouble();
        int size = width * height;  
        int x, y;
        int mask = 0xff; 
        int longmask = 0xffff;

    	for(int i = 0; i < size; i++) {
    		x = i % width;
    		y = i / width;
    		if (imageType == DataInput.GRAY8 || imageType ==  DataInput.COLOR_RGB)
    			mu.Add(getLowerPower(centered_x[x], p) * getLowerPower(centered_y[y], q)  * (float) (((byte[])data)[i] & mask));
    		else if (imageType == DataInput.GRAY16)
    			mu.Add(getLowerPower(centered_x[x], p) * getLowerPower(centered_y[y], q)  * (float) (((short[])data)[i] & longmask));
    		else if (imageType == DataInput.GRAY32)
    			mu.Add(getLowerPower(centered_x[x], p) * getLowerPower(centered_y[y], q)  * ((float[])data)[i]);
    		else
    			throw new Exception("Not supported image type in Moments. Type = " + imageType);
    	}
      
    	return mu.getSum();
    }

    //calculates scale invariant moments, (a.k.a. normalized central moments)
    protected double n_pq(double mu_pq, int p, int q, double mu_00) {
        return (mu_pq / Math.pow(mu_00, ((double) (p + q) / 2) + 1));
    }
    

    protected float[][] calcFeatures() throws Exception {
        double mean_x;
        double mean_y;
        double[] centered_x = null;
        double[] centered_y = null;
        //raw image moments
        double m_00, m_01, m_10;
        //central moments
        double mu_00, mu_11, mu_02, mu_20, mu_21, mu_12, mu_03, mu_30;
        //scale invariant moments, (a.k.a. normalized central moments)
        double n_11, n_02, n_20, n_21, n_12, n_03, n_30;
        //Hu's moments (and one more)
        double[] Hu = new double[8];

        //for each image in the set
        Object data = null;
        for (int image_num = 0; image_num < this.length; image_num++) {

        	if (problem !=null)
        	{
        		if (!problem.ofSameSize())
        		{  	//set the size for this image
        		 this.totalheight = problem.getHeightList()[image_num];
        		 this.totalwidth = problem.getHeightList()[image_num];
        		}
        	}
        	
            //calculate raw moments
        	if (problem !=null)
        	  data = problem.getData(image_num,1);
        	else  //alldata is not null
        	  data = alldata.get(image_num);	
        		
        	m_00 = m_pq(data, 0, 0, totalwidth, totalheight);
        	m_10 = m_pq(data, 1, 0, totalwidth, totalheight);
        	m_01 = m_pq(data, 0, 1, totalwidth, totalheight);

            //calculate mean of x and y
            mean_x = m_10 / m_00;
            mean_y = m_01 / m_00;

            //calculate centered x and y based on mean
   			centered_x = new double[totalwidth];
   			centered_y = new double[totalheight];
            getCenter(totalwidth, mean_x, centered_x);
            getCenter(totalheight, mean_y, centered_y);

            //calculate central moments
            mu_00 = mu_pq(data, 0, 0, totalwidth, totalheight, centered_x, centered_y);
            mu_20 = mu_pq(data, 2, 0, totalwidth, totalheight, centered_x, centered_y);
            mu_02 = mu_pq(data, 0, 2, totalwidth, totalheight, centered_x, centered_y);
            mu_11 = mu_pq(data, 1, 1, totalwidth, totalheight, centered_x, centered_y);
            mu_30 = mu_pq(data, 3, 0, totalwidth, totalheight, centered_x, centered_y);
            mu_12 = mu_pq(data, 1, 2, totalwidth, totalheight, centered_x, centered_y);
            mu_21 = mu_pq(data, 2, 1, totalwidth, totalheight, centered_x, centered_y);
            mu_03 = mu_pq(data, 0, 3, totalwidth, totalheight, centered_x, centered_y);

            //calculate //scale invariant moments,
            //(a.k.a. normalized central moments)
            n_20 = n_pq(mu_20, 2, 0, mu_00);
            n_02 = n_pq(mu_02, 0, 2, mu_00);
            n_11 = n_pq(mu_11, 1, 1, mu_00);
            n_30 = n_pq(mu_30, 3, 0, mu_00);
            n_12 = n_pq(mu_12, 1, 2, mu_00);
            n_21 = n_pq(mu_21, 2, 1, mu_00);
            n_03 = n_pq(mu_03, 0, 3, mu_00);

            //calculate Hu's moments
            Hu[0] = n_20 + n_02;
            Hu[1] = (n_20 - n_02) * (n_20 - n_02)
                    + 4 * (n_11 * n_11);
            Hu[2] = (n_30 - 3 * n_12) * (n_30 - 3 * n_12)
                    + (3 * n_21 - n_03) * (3 * n_21 - n_03);
            Hu[3] = (n_30 + n_12) * (n_30 + n_12)
                    + (n_21 + n_03) * (n_21 + n_03);
            Hu[4] = (n_30 - 3 * n_12) * (n_30 + n_12)
                    * ((n_30 + n_12) * (n_30 + n_12) - 3 * (n_21 + n_03) * (n_21 + n_03))
                    + (3 * n_21 - n_03) * (n_21 + n_03)
                    * (3 * (n_30 + n_12) * (n_30 + n_12) - (n_21 + n_03) * (n_21 + n_03));
            Hu[5] = (n_20 - n_02)
                    * ((n_30 + n_12) * (n_30 + n_12) - (n_21 + n_03) * (n_21 + n_03))
                    + 4 * n_11 * (n_30 + n_12) * (n_21 + n_03);
            Hu[6] = (3 * n_21 - n_03) * (n_30 + n_12)
                    * ((n_30 + n_12) * (n_30 + n_12) - 3 * (n_21 + n_03) * (n_21 + n_03))
                    - (n_30 - 3 * n_12) * (n_21 + n_03)
                    * (3 * (n_30 + n_12) * (n_30 + n_12) - (n_21 + n_03) * (n_21 + n_03));
            Hu[7] = n_11 * ((n_30 + n_12) * (n_30 + n_12) - (n_03 + n_21) * (n_03 + n_21))
                    - (n_20 - n_02) * (n_30 + n_12) * (n_03 + n_21);
            //the last one is from wikipedia:
            //http://en.wikipedia.org/wiki/Image_moment

            //write the calculated Hu's moments into the features array
            //that will be returned
            for (int i = 0; i < Hu.length; i++) {
                features[image_num][i] = (float) Hu[i];
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
    public boolean is3DExtractor()
    {  return false ; 
    }


    //converts a flattened image into a 2d array.
    //yes, I'm just moving bytes around in memory,
    //but 2d arrays are a lot easier to work with...
    //a 4000x4000 image takes ~135ms to convert on my computer (2.53Ghz cpu)
    //09/01/11: change to convert the original Object (byte[]/int[]/float[]) to float[][]
    //08/20/2012:  not used
    /*
    protected void convert_flat_to_2d(Object flat_image, float[][] image) throws Exception 
    {
        int mask = 0xff; 
        int longmask = 0xffff;

        int y, x;
        if (imageType == DataInput.GRAY8 || imageType ==  DataInput.COLOR_RGB)
        {
            for (int i = 0; i < totalheight*totalwidth; i++) 
            {
                x = i % totalwidth;
                y = i / totalwidth;
                image[y][x] = (float) (((byte[])flat_image)[i] & mask);
            }
        }
        else if (imageType == DataInput.GRAY16)
        {
            for (int i = 0; i < totalheight*totalwidth; i++) 
            {
               x = i % totalwidth;
               y = i / totalwidth;
               image[y][x] = (float) (((short[])flat_image)[i] & longmask);
            }
        }
        else if (imageType == DataInput.GRAY32)
        { 	
            for (int i = 0; i < totalheight*totalwidth; i++) 
            {
              x = i % totalwidth;
              y = i / totalwidth;
              image[y][x] = ((float[])flat_image)[i];
            }
        }
        else
              throw new Exception("Not supported image type in Moments: type "+imageType);	
 
    }*/
}

/*
class Tester
{
    public static void main(String[] args) {

        System.out.println("Tester class");

        String dir = "/home/aleksey/THESIS/IMAGES/SET3/";
        String ext = ".jpg";
        String channel = "a";

        DataInput problem = null;
        try{
        	problem = new DataInput(dir, ext, channel, null);
        }catch(Exception e)
        {
        	e.printStackTrace();
        }

//        byte[][] data = problem.getData();
//        byte[] a1 = data[0];
//        byte[] a2 = data[1];
//
//        for (int i = 0; i < a1.length; i++) {
//            System.out.print(a1[i]);
//        }
//        System.out.println();
//        for (int i = 0; i < a2.length; i++) {
//            System.out.print(a2[i]);
//        }
//
//        int [][] array = {
//            {1,2,3},
//            {4,5,6}
//        };
//        int[] a = array[1];
//        for(int i = 0; i < a.length; i++){
//            System.out.println(a[i]);
//        }

        ImageMoments m = new ImageMoments();

//        m.debug1();
        float[][] f = null;
        try {
          f = m.calcFeatures(problem);
        }catch(Exception e)
        {
        	e.printStackTrace();
        }
        //print the features
        for (int i = 0; i < f.length; i++) {
            for (int j = 0; j < f[0].length; j++) {
                //System.out.print(f[i][j] + " ");
                System.out.printf("%.20f ", f[i][j]);
            }
            System.out.println();
        }

//        byte[][] data = problem.getData();
//
//        int totallevel;
//        int totalwidth; //dimension
//        int totalheight;//dimension
//        int length; //num of images in set
//
//        length = problem.getLength();
//        totalwidth = problem.getWidth();
//        totalheight = problem.getHeight();
//
//        System.out.println("length = " + length);
//        System.out.println("totalwidth = " + totalwidth);
//        System.out.println("totalheight = " + totalheight);
//
//        //for(int i = 0)
//
//        for (int i = 0; i < data[0].length; i++) {
//            if ((i % totalwidth) == 0) {
//                //System.out.println();
//            }
//
//            //System.out.print((data[1][i] & 0xFF) + "\t");
//        }
//
//        long t1 = System.currentTimeMillis();
//
//        int mask = 0xff;
//
//        //just a single image, as a 2d array
//        byte[][] image = new byte[totalheight][totalwidth];
//        int y = 0, x = 0;
//        for (int i = 0; i < data[0].length; i++) {
//            x = i % totalwidth;
//            y = i / totalheight;
//            //System.out.println("x = " + x + "y = " + y);
//
//            image[y][x] = (byte) (data[0][i] & mask);
//        }
//
//        long t2 = System.currentTimeMillis();
//
//        for (int yy = 0; yy < image.length; yy++) {
//            for (int xx = 0; xx < image[0].length; xx++) {
//                //System.out.print(image[yy][xx]);
//            }
//            //System.out.println();
//        }
//
//        System.out.println("Time = " + (t2 - t1));

//            }

    }
    


}*/
