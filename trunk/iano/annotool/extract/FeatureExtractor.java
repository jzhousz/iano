package annotool.extract;

import annotool.io.DataInput;

/** interface for all feature extractors such as HaarFeatureExtractor **/

public interface FeatureExtractor
{

   //get features based on input data in the instance	
   //public float[][] calcFeatures();
   //get features based on raw image in the problem
   public float[][] calcFeatures(DataInput problem);
   
   //get features based on input data. Not supported by all feature extractors especially 3D.
   public float[][] calcFeatures(float[][] data, DataInput problem);
   //indicate if it is applicable to 3D image stack.
   boolean is3DExtractor();

}