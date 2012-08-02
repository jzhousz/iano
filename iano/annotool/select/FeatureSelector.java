package annotool.select;

/**
 * FeatureSelector selects features that are accurate and of interest
 */
public interface FeatureSelector
{
   /**
    * Sets algorithm parameters from para
    * 
    * @param   para  Each element of the hashmap holds a parameter name
    *                for its key and a its value is that of the parameter.
    *                The parameters should be the same as those in the 
    *                algorithms.xml file.
    */
   public void setParameters(java.util.HashMap<String, String> para);

   
   
   /**
    * Stores features from data and store any relevant values such as
    * the dimensions of data and targets.
    * 
    * @param   data     Two-dimensional array of extracted image data
    * @param   targets  Array of the targets for the image
    * @return           Two-dimensional array of features that are selected
    */
   public float[][] selectFeatures(float[][] data, int[] targets);

   
   
   /**
    * Returns the indices of the selected features 
    * (an index is between 0 and one less than the number of features).
    * 
    * @return  The indices of the selected features.
    */
   public int[] getIndices();

   
   
   /**
    * Selects features using indices and returns the selected features.
    * 
    * @param   data     Two-dimensional array of extracted image data
    * @param   indices  Array of indices to the data columns
    * @return           Two-dimensional array of features that are selected   
    */
   public float[][] selectFeaturesGivenIndices(float[][] data, int [] indices);
   
}
