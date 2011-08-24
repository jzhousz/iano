package annotool.extract;

import annotool.io.DataInput;
import annotool.ImgDimension;

/** interface for all feature extractors such as HaarFeatureExtractor **/

public interface FeatureExtractor
{
   //pass parameters to the algorithm.
   public void  setParameters(java.util.HashMap<String, String> parameter);
   
   //get features based on raw image in the problem
   public float[][] calcFeatures(DataInput problem);

   //get features based on byte data, with dimension information. 
   public float[][] calcFeatures(byte[][] data, ImgDimension dim);

   //indicate if it is applicable to 3D image stack.
   boolean is3DExtractor();

   //get features based on input data in the instance	
   //public float[][] calcFeatures();
   
   //get features based on input data. Not supported by all feature extractors especially 3D.
   //public float[][] calcFeatures(float[][] data, DataInput problem);

}