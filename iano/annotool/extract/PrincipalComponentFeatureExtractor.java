package annotool.extract;

import java.util.ArrayList;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import annotool.ImgDimension;
import annotool.io.DataInput;
import weka.attributeSelection.PrincipalComponents;
import weka.core.Instances;

/*
// This is a work in progress.   
// It uses Weka PCA. It is too slow on images.  Not used in GUI after May 2012 
//
// 5/25/2012  Due to the incompatability with the updated DataInput, the methods are commented out.
*/
public class PrincipalComponentFeatureExtractor implements FeatureExtractor {

	Instances m_Data = null; //will build it
	annotool.io.DataInput problem = null;
	protected float[][] features = null;
    protected byte[][] data;
    int length;
    int width;
    int height;
	int mask = 0xff; 
	int longmask = 0xffff;
	int imageType;

    /**
     * Default constructor
     */
    public PrincipalComponentFeatureExtractor() 
    {}
    
    /**
     * Empty implementation of setParameters 
     * 
     * @param  para  Each element of para holds a parameter’s name for its key
     *               and a parameter’s value for its value. The parameters
     *               should be the same as those in the algorithms.xml file.
     */
    public void setParameters(java.util.HashMap<String, String> parameters) {
  	  //process parameter if any	
  	}
  	
    /**
     * Empty implementation of PrincipalComponentFeatureExtractor(java.util.HashMap<String, String> parameters)
     * 
     * @param  para  Each element of para holds a parameter’s name for its key
     *               and a parameter’s value for its value. The parameters
     *               should be the same as those in the algorithms.xml file.
     */
    public PrincipalComponentFeatureExtractor(java.util.HashMap<String, String> parameters) {
	  //process parameter if any	
	}
	   
    /**
     * Empty implementation of PrincipalComponentFeatureExtractor(annotool.io.DataInput problem)
     * 
     * @param  problem  Image to be processed
     * @throws Exception 
     */
	public PrincipalComponentFeatureExtractor(annotool.io.DataInput problem) throws Exception {
		
		   /*length = problem.getLength();
		   width = problem.getWidth();
		   height = problem.getHeight();
		   imageType = problem.getImageType();
		   
		   this.problem = problem;
		   
		   features  = new float[length][width*height]; 
		   
		   return calcFeatures();*/
		}

   /**
    * Empty implementation of calcFeatures(ArrayList data, int imageType, ImgDimension dim)
    * 
    * @param   data       Data taken from the image
    * @param   imageType  Type of the image
    * @param   dim        Dimenstions of the image
    * @return             Array of features
    * @throws  Exception  (Not used)
    */
	public float[][] calcFeatures(ArrayList data, int imageType, ImgDimension dim) throws Exception
	{ 
		System.out.println("This method is not yet supported.");
		throw new Exception("Not supported.");
	}

    /**
     * Empty implementation of calcFeatures(annotool.io.DataInput problem)
     * 
     * @param   problem    Image to be processed
     * @return             Array of features
     * @throws  Exception  (Not used)
     */
	@Override
	public float[][] calcFeatures(annotool.io.DataInput problem) throws Exception
	{
		   length = problem.getLength();
		   width = problem.getWidth();
		   height = problem.getHeight();
		   imageType = problem.getImageType();
		   
		   this.problem = problem;
		   
		   features  = new float[length][width*height]; 
		   
		   return calcFeatures();
	}
	
   /**
    * Calculates features based on data and dim.
    * 
    * @param   data       Data taken from the image
    * @param   dim        Dimenstions of the image
    * @return             Array of features
    * @throws Exception 
    */
	private float[][] calcFeatures(byte[][] data, ImgDimension dim) throws Exception
	{
		   /*length = data.length;
		   width = dim.width;
		   height = dim.height;
		   
		   features  = new float[length][width*height]; 
		   
	       for(int i=0; i <length; i++)
 		     for(int j = 0; j< width*height; j++)
			       features[i][j] = data[i][j]&0xff;*/
		
	       return calcFeatures();
	}
	   
	
	
	protected float[][] calcFeatures() throws Exception {
		//Build the data matrix (rgb values) is already done
		//Do this incrementally, eg. one row in the matrix at a time.
		//Initialize the different matricies.
		int imgDim = width*height;
		System.out.println("Image dimension: " + imgDim);
		double[] mean = new double[imgDim];
		double[][] cov = new double[imgDim][imgDim];
		double[] z = new double[imgDim];
		double[] eigValues = null;
		double[][]eigVectors = null;
		Object imgData;
		//Temp variable declaration
		int i = 0, j = 0, k = 0;
		for (i = 0; i < length; i++)
		{
			//Get data
			imgData = problem.getData(i, 1);
			
			//Find the sum
			for (j = 1; j < height; j++)
					mean[i] += getValue(imgData, j*width+i);
			k++;
		}
		//Find mean
		for (i = 0; i < imgDim; i++)
			mean[i] /= k;
		
		//Calculate covariance matrix (R)
		for (k = 0; k < length; k++)
		{
			//Get data
			imgData = problem.getData(k, 1);
			
			for (i = 0; i < imgDim; i++)
				z[i] = getValue(imgData, i) - mean[i];
			
			for (i = 0; i < imgDim; i++)
			{
				for (j = 0; j < imgDim; j++)
					cov[i][j] += z[i]*z[j];
			}
		}
		
		//Find eigenvectors/values
		Matrix a = new Matrix(cov);
		EigenvalueDecomposition b = a.eig();
		a = b.getV();
		eigVectors = a.getArray();
		eigValues = b.getRealEigenvalues();
		
		// (Dimension reduction, fewer columns)
		
		
		//Transform to get new data set (matrix)
		//The energies in the makefv are for error, and not necessarily useful
		double vv;
		for (i = 0; i < length; i++)
		{
			for (j = 0; j < length; j++)
				features[i][j] = 0;
			
			//Dot products
			for (j = 0; j < length; j++)
			{
				vv = eigVectors[i][j];
				for (k = 0; k < length; k++)
					features[i][k] = (float) ((float) vv*eigValues[k]);
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
	{  return false;} 
	
	//get value
	private int getValue(Object imgData, int arrayIndex)
	{
		int value =0;
		if (imageType == DataInput.GRAY8 || imageType ==  DataInput.COLOR_RGB)
			value = (int) (((byte[])imgData)[arrayIndex] & mask);
		else if (imageType == DataInput.GRAY16)
			value = (int) (((short[])imgData)[arrayIndex] & longmask);
		return value;
	}
}
