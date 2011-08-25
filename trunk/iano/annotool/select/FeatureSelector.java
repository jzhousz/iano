package annotool.select;

/** interface for all feature selectors such as mRMRFeatureSelector 
 Parameters can be passed to the constructor of various selectors using a HashMap **/

public interface FeatureSelector
{

   public void setParameters(java.util.HashMap<String, String> para);
   public float[][] selectFeatures(float[][] data, int[] targets);
   public int[] getIndices();
   public float[][] selectFeaturesGivenIndices(float[][] data, int [] indices);
   
}