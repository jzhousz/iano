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
    
    LDATrainedModel(Matrix r, double logDetSigma, float[][] means, float[] priors, java.util.HashMap<Integer, Integer> targetMap)
    {
    	setLDATrainedModel(r, logDetSigma, means, priors, targetMap);
    }
    
    void setLDATrainedModel(Matrix r, double logDetSigma, float[][] means, float[] priors, java.util.HashMap<Integer, Integer> targetMap)
    {
    	trainedR = r;
    	this.logDetSigma = logDetSigma;
    	this.means = means;
    	this.priors = priors;
    	this.targetMap = targetMap;
    }
    
    Matrix getTrainedR() { return trainedR; }
    double getLogDetSigma() {return logDetSigma;} 
    float[][] getMeans() { return means; }
    float[] getPriors() { return priors; }
    java.util.HashMap<Integer, Integer> getTargetMap()  { return targetMap; } 

}
