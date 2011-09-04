package annotool.extract;

import annotool.ImgDimension;


/*
 * 
 * Seperable three dimensional wavelet transform
 * Full extension of 2D DWT with 8 subbands: LLL, LLH, LHL, LHH, HLL, HLH, HHL,  HHH.
 * The coefficients number is the same as original 3D image size. 
 * 
 */
public class StackFullHaarFeatureExtractor implements FeatureExtractor {

	
	public StackFullHaarFeatureExtractor()
	{}

	public void setParameters(java.util.HashMap<String, String> parameters)
	{	
	}

	public float[][] calcFeatures(annotool.io.DataInput problem) throws Exception {
		System.out.println("This method is not yet supported by 3D feature extractors");

		throw new Exception("Not supported");

	}

	public float[][] calcFeatures(java.util.ArrayList data,  int imgType, ImgDimension dim) throws Exception
	{
		System.out.println("This method is not yet supported by 3D feature extractors");

		throw new Exception("Not supported");
	}

	public boolean is3DExtractor()
	{  return true;} 
}
