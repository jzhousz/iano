package annotool.classify;

/**
 * Classifier returns a prediction based on training and testing image sets.
 * 
 * @see  SavableClassifier 
 */
public interface Classifier
{
   /**
    * Sets algorithm parameters from para
    * 
    * @param   para  Each element of para holds a parameter name
    *                for its key and a its value is that of the parameter.
    *                The parameters should be the same as those in the 
    *                algorithms.xml file.
    */
   public void setParameters(java.util.HashMap<String, String> para);

   
   
   /**
    * Classifies the patterns using the input parameters.
    * 
    * @param   trainingpatterns  Pattern data to train the algorithm
    * @param   trainingtargets   Targets for the training pattern
    * @param   testingpatterns   Pattern data to be classified
    * @param   predictions       Storage for the resulting prediction
    * @param   prob              Storage for probability result
    * @throws  Exception         Optional, generic exception to be thrown
    */
   public void classify(float[][] trainingpatterns, int[] trainingtargets, float[][] testingpatterns, int[] predictions, double[] prob) throws Exception;

   
   
   /**
    * Returns whether or not the algorithm uses probability estimations.
    * 
    * @return  <code>True</code> if the algorithm uses probability 
    *          estimations, <code>False</code> if not
    */
   boolean doesSupportProbability();   
}
