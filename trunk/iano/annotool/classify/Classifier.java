package annotool.classify;

/** interface for all classifiers **/

public interface Classifier
{
   public void setParameters(java.util.HashMap<String, String> para);   
	
   //Conform to LibSVM if possible, since that is the 1st classifier to be tested.
   public void classify(float[][] trainingpatterns, int[] trainingtargets, float[][] testingpatterns, int[] predictions, double[] prob) throws Exception;
   
   //if the returned probabilities are valid (depending on the algorithm or its parameter setup)
   boolean doesSupportProbability();
}