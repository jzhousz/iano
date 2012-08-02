package annotool.classify;

import java.io.Serializable;

import Jama.Matrix;


public class LDATrainedModel implements Serializable {
   	private static final long serialVersionUID = 1L;

    Matrix  trainedR;
    double logDetSigma; 
    float[][] means;
    float[] priors;
    java.util.HashMap<Integer, Integer> targetMap;

   /**
    * Constructor that passes parameters to the setLDATrainedModel method
    *     
    * @param r            The QRDecomposition of the training matrix
    * @param logDetSigma  A statistic of r
    * @param means        The normalized set data
    * @param priors       The previous predictions
    * @param targetMap    The target list where the key is in the range of 0 ... ngroups -1
    */
    LDATrainedModel(Matrix r, double logDetSigma, float[][] means, float[] priors, java.util.HashMap<Integer, Integer> targetMap)
    {
    	setLDATrainedModel(r, logDetSigma, means, priors, targetMap);
    }
    
    /**
     * Copies parameters to instance variables
     *     
     * @param r            The QRDecomposition of the training matrix
     * @param logDetSigma  A statistic of r
     * @param means        The normalized set data
     * @param priors       The previous predictions
     * @param targetMap    The target list where the key is in the range of 0 ... ngroups -1  
     */
    void setLDATrainedModel(Matrix r, double logDetSigma, float[][] means, float[] priors, java.util.HashMap<Integer, Integer> targetMap)
    {
    	trainedR = r;
    	this.logDetSigma = logDetSigma;
    	this.means = means;
    	this.priors = priors;
    	this.targetMap = targetMap;
    }
    
    /**
     * returns the trainedR of the LDATrainedModel
     * 
     * @return  trainedR
     */
    Matrix getTrainedR() { return trainedR; }
    
    /**
     * returns the logDetSigma of the LDATrainedModel
     * 
     * @return  logDetSigma
     */
    double getLogDetSigma() {return logDetSigma;} 
    
    /**
     * returns the means of the LDATrainedModel
     * 
     * @return  means
     */
    float[][] getMeans() { return means; }
    
    /**
     * returns the priors of the LDATrainedModel
     * 
     * @return  priors
     */
    float[] getPriors() { return priors; }
    
    /**
     * returns the targetMap of the LDATrainedModel
     * 
     * @return  targetMap
     */
    java.util.HashMap<Integer, Integer> getTargetMap()  { return targetMap; } 

}
