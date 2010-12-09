package annotool.classify;

/** interface for all classifiers **/

public interface Classifier
{
   //Conform to LibSVM if possible, since that is the 1st classifier to be tested.
   public void classify(float[][] trainingpatterns, int[] trainingtargets, float[][] testingpatterns, int[] predictions, int traininglen, int testinglen, double[] prob);

}