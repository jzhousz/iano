package annotool.classify;

/*
 * Savable classifier returns a model (an object of a Serializable class),
 * so that the classifier may be persisted and loaded back to memory for use.
 * 
 */
public interface SavableClassifier extends Classifier {
	
	//it trains and returns (typically also sets) an internal model   
	Object trainingOnly(float[][] trainingpatterns, int[] trainingtargets);
	   
	//matching pair that returns and sets the model
	Object getModel(); 
	void setModel(java.lang.Object model) throws Exception; 

	//two methods that classify one or multiple testing patterns.
	int classifyUsingModel(Object model, float[] testingPattern) throws Exception;
	int[] classifyUsingModel(Object model, float[][] testingPatterns) throws Exception;
	
	//two methods that load and save model to file
    void saveModel(Object trainedModel, String model_file_name) throws java.io.IOException;
    Object loadModel(String model_file_name) throws java.io.IOException;
}
