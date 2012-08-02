package annotool.extract;

import annotool.ImgDimension;


/**
 * 
 * Separable three dimensional wavelet transform
 * Full extension of 2D DWT with 8 subbands: LLL, LLH, LHL, LHH, HLL, HLH, HHL,  HHH.
 * The coefficients number is the same as original 3D image size. 
 * 
 */
public class StackFullHaarFeatureExtractor implements FeatureExtractor {

	/**
	 * Default constructor
	 */
	public StackFullHaarFeatureExtractor()
	{}

    /**
     * Empty implementation of setParameters 
     * 
     * @param  para  Each element of para holds a parameter’s name for its key
     *               and a parameter’s value for its value. The parameters
     *               should be the same as those in the algorithms.xml file.
     */
	public void setParameters(java.util.HashMap<String, String> parameters)
	{	
	}

    /**
     * Empty implementation of calcFeatures(annotool.io.DataInput problem)
     * 
     * @param   problem    Image to be processed
     * @return             Array of features
     * @throws  Exception  Thrown because this method is not supported by 3D feature extractors
     */
	public float[][] calcFeatures(annotool.io.DataInput problem) throws Exception {
		System.out.println("This method is not yet supported by 3D feature extractors");

		throw new Exception("Not supported");

	}

	   /**
	    * Empty implementation of calcFeatures(java.util.ArrayList data, int imageType, ImgDimension dim)
	    * 
	    * @param   data       Data taken from the image
	    * @param   imageType  Type of the image
	    * @param   dim        Dimenstions of the image
	    * @return             Array of features
	    * @throws  Exception  Thrown because this method is not supported by 3D feature extractors
	    */
	public float[][] calcFeatures(java.util.ArrayList data,  int imgType, ImgDimension dim) throws Exception
	{
		System.out.println("This method is not yet supported by 3D feature extractors");

		throw new Exception("Not supported");
	}

    /**
     * Returns whether or not the algorithm is able to extract from a 3D image 
     * stack. 
     * 
     * @return  <code>True</code> if the algorithm is a 3D extractor, 
     *          <code>False</code> if not. Default is <code>True</code>
     */
	public boolean is3DExtractor()
	{  return true;} 
}
