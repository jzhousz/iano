package annotool.select;

/** interface for all feature selectors such as mRMRFeatureSelector **/

public interface FeatureSelector
{

   public float[][] selectFeatures();
   
   public int[] getIndices();

   public float[][] selectFeaturesGivenIndices(int [] indices);
   
}