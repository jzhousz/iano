package annotool.classify;

/*
 * Savable classifier returns a model (an object of a Serializable class),
 * so that the classifier may be persisted and loaded back to memory for use.
 * Depending on the classifier, the returned Object maybe a String (filename), e.g. SVM.
 */
public interface SavableClassifier extends Classifier {
	
	//it trains and sets an internal model, called before getModel();   
	void trainingOnly(float[][] trainingpatterns, int[] trainingtargets);
	   
	//matching pair that returns and sets the model
	Object getModel();  //get and/or save the trained model
	void setModel(java.lang.Object model); //load or get the trained model
   
	//two methods that classify one or multiple testing patterns.
	int classifyUsingModel(Object model, float[] testingPattern) throws Exception;
	int[] classifyUsingModel(Object model, float[][] testingPatterns) throws Exception;
}