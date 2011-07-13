package annotool.extract;


/*
 * 
 * Seperable three dimensional wavelet transform
 * Full extension of 2D DWT with 8 subbands: LLL, LLH, LHL, LHH, HLL, HLH, HHL,  HHH.
 * The coefficients number is the same as original 3D image size. 
 * 
 */
public class StackFullHaarFeatureExtractor implements FeatureExtractor {

	
	public StackFullHaarFeatureExtractor(java.util.HashMap<String, String> parameters)
	{
		
	}
	public float[][] calcFeatures(annotool.io.DataInput problem) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean is3DExtractor()
	{  return true;} 
}
