package annotool.extract;

import annotool.ImgDimension;
import annotool.io.DataInput;
import java.util.ArrayList;

/**
 *  This class calculates Hu's moments (seven, plus one more)
 *  http://en.wikipedia.org/wiki/Image_moment#Rotation_invariant_moments
 */

public class ImageMoments implements FeatureExtractor
{
    protected ArrayList data;    //data from DataInput
    int totalwidth;             //width of images
    int totalheight;            //height of images
    int length;                 //number of images
    int imageType;              //the type of image (defined in DataInput)

    //features to return
    protected float[][] features = null;

    /**
     * Default constructor
     */
    public ImageMoments() 
    {}
    
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
        data = problem.getData();
        length = problem.getLength();
        totalwidth = problem.getWidth();
        totalheight = problem.getHeight();
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
        this.data = data;
        length = data.size();
        totalwidth = dim.width;
        totalheight = dim.height;
        features = new float[length][8];
        this.imageType = imageType;
 
    	return calcFeatures();
    }

    //converts a flattened image into a 2d array.
    //yes, I'm just moving bytes around in memory,
    //but 2d arrays are a lot easier to work with...
    //a 4000x4000 image takes ~135ms to convert on my computer (2.53Ghz cpu)
    //09/01/11: change to convert the original Object (byte[]/int[]/float[]) to float[][]
    protected void convert_flat_to_2d(Object flat_image, float[][] image) throws Exception 
    {
        int mask = 0xff; 
        int longmask = 0xffff;

        //not needed to new for each image. waste of memory  jz 
        //float[][] image = new float[totalheight][totalwidth];
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
               image[y][x] = (float) (((int[])flat_image)[i] & longmask);
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
 
    }

    //calculates raw image moments
    protected double m_pq(float[][] image, int p, int q) {
        CompSumDouble m = new CompSumDouble(); //compensated sum
        for (int y = 0; y < image.length; y++) {
            for (int x = 0; x < image[0].length; x++) {
                m.Add(Math.pow(x, p) * Math.pow(y, q) * image[y][x]);
            }
        }
        return m.getSum();
    }

    //the next two functions calculate the centered values for x and y,
    // based on mean x and mean y.  This way, instead of subtracting the mean
    //from x or y each time when I calculate the central moments,
    //(essentially doing it M*N times for every one), I pre-calculate them
    //in advance (M+N times).  Trading a little memory for a little speed...
    protected double[] center_x(float[][] image, double x_mean, double[] centered_x) {
        // double[] centered_x = new double[image[0].length]; //waste
        for (int x = 0; x < image[0].length; x++) {
            centered_x[x] = x - x_mean;
        }
        return centered_x;
    }

    protected static double[] center_y(float[][] image, double y_mean, double[] centered_y) {
        //double[] centered_y = new double[image.length]; //waste
        for (int y = 0; y < image.length; y++) {
            centered_y[y] = y - y_mean;
        }
        return centered_y;
    }

    //calculates central moments
    protected double mu_pq(float[][] image, int p, int q,
                           double[] centered_x, double[] centered_y) {
        CompSumDouble mu = new CompSumDouble();
        for (int y = 0; y < image.length; y++) {
            for (int x = 0; x < image[0].length; x++) {
                mu.Add(
                        Math.pow(centered_x[x], p)
                        * Math.pow(centered_y[y], q)
                        * image[y][x] );
            }
        }
        return mu.getSum();
    }

    //calculates scale invariant moments, (a.k.a. normalized central moments)
    protected double n_pq(double mu_pq, int p, int q, double mu_00) {
        return (mu_pq / Math.pow(mu_00, ((double) (p + q) / 2) + 1));
    }
    

    protected float[][] calcFeatures() throws Exception {
        //single image
    	//reuse to save memory 09/01/2011
        float[][] image = new float[totalheight][totalwidth];
        //centered x and y based on mean
        double mean_x;
        double mean_y;
        double[] centered_x = new double[image[0].length];
        double[] centered_y = new double[image.length];
        //raw image moments
        double m_00, m_01, m_10;
        //central moments
        double mu_00, mu_11, mu_02, mu_20, mu_21, mu_12, mu_03, mu_30;
        //scale invariant moments, (a.k.a. normalized central moments)
        double n_11, n_02, n_20, n_21, n_12, n_03, n_30;
        //Hu's moments (and one more)
        double[] Hu = new double[8];

        //for each image in the set
        for (int image_num = 0; image_num < this.length; image_num++) {
            //convert this image from flattened to a 2d array
            convert_flat_to_2d(data.get(image_num), image);

            //calculate raw moments
            m_00 = m_pq(image, 0, 0);
            m_10 = m_pq(image, 1, 0);
            m_01 = m_pq(image, 0, 1);

            //calculate mean of x and y
            mean_x = m_10 / m_00;
            mean_y = m_01 / m_00;

            //calculate centered x and y based on mean
            center_x(image, mean_x, centered_x);
            center_y(image, mean_y, centered_y);

            //calculate central moments
            mu_00 = mu_pq(image, 0, 0, centered_x, centered_y);
            mu_20 = mu_pq(image, 2, 0, centered_x, centered_y);
            mu_02 = mu_pq(image, 0, 2, centered_x, centered_y);
            mu_11 = mu_pq(image, 1, 1, centered_x, centered_y);
            mu_30 = mu_pq(image, 3, 0, centered_x, centered_y);
            mu_12 = mu_pq(image, 1, 2, centered_x, centered_y);
            mu_21 = mu_pq(image, 2, 1, centered_x, centered_y);
            mu_03 = mu_pq(image, 0, 3, centered_x, centered_y);

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
            //double[] Hu = new double[8];
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
