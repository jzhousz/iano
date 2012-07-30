package annotool.classify;

/**
 * Savable classifier returns a model (an object of a Serializable class),
 * so that the classifier may be persisted and loaded back to memory for use.
 * 
 * @see  Classifier 
 */
public interface SavableClassifier extends Classifier 
{
   /**
    * Trains and returns an internal model using a training set.
    * 
    * @param   trainingpatterns  Pattern data to train the algorithm
    * @param   trainingtargets   Targets for the training pattern
    * @return                    Model created by the classifier
    * @throws  Exception         Optional, generic exception to be thrown
    */ 
	Object trainingOnly(float[][] trainingpatterns, int[] trainingtargets) throws Exception;

	
	
   /**
    * Gets the internal model from the classifier
    * 
    * @return  Model created by the classifier.
    */
	Object getModel(); 

	
	
   /**
    * Sets an internal model to be used by the classifier
    * 
    * @param   model      Model to be used by the classifier
    * @throws  Exception  Exception thrown if model is incompatible
    */

	void setModel(java.lang.Object model) throws Exception; 

	
	
   /**
    * Classifies the internal model using one testing pattern
    * 
    * @param   model            Model to be used by the classifier
    * @param   testingPattern   Pattern data to be classified
    * @param   prob             Storage for probability result
    * @return                   The prediction result
    * @throws  Exception        Exception thrown if model is incompatible
    */
	int classifyUsingModel(Object model, float[] testingPattern, double[] prob) throws Exception;

	
	
   /**
    * Classifies the internal model using multiple testing patterns
    * 
    * @param   model             Model to be used by the classifier
    * @param   testingPatterns   Pattern data to be classified
    * @param   prob              Storage for probability result 
    * @return                    Array of prediction results
    * @throws  Exception         Exception thrown if model is incompatible
    */
	int[] classifyUsingModel(Object model, float[][] testingPatterns, double[] prob) throws Exception;

	
	
   /**
    * Saves a specified model to a specified file
    * 
    * @param   trainedModel     Trained model that is to be saved
    * @param   model_file_name  Name of the file to be saved to
    * @throws  Exception        Exception thrown if model cannot be saved
    */
    void saveModel(Object trainedModel, String model_file_name) throws java.io.IOException;

    
    
   /**
    * Loads a previously saved model back into the classifier.
    * 
    * @param   model_file_name  Name of the file to be loaded
    * @return                   Model that was loaded
    * @throws  Exception        Exception thrown if file cannot be found
    */
    Object loadModel(String model_file_name) throws java.io.IOException;
}
