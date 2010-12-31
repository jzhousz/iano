package annotool.classify;

import Jama.*;

//  Linear Discriminant Classifier based on Multivariate Normal Distribution.
//  Estimate the pooled covariance matrix of all classes.
// TODO 11/11/08:  
//       1. normalize posterior probability
//       2. check the pooled covariance matrix to be positive definite
//       3. throw exception for badly formed matrix
public class LDAClassifier implements Classifier {

	int dimension = 0;
	int ngroups = 0;
	float[] priors = null;
	java.util.HashMap targetmap = null;

	/**
	 * a simple testing case
	 */
	public static void main(String[] args) {

		float[][]  training =  {{-1.4240f,   -0.5131f},
				{0.7762f,   -0.1264f}, 
				{1.0581f,    0.1850f},
				{0.5754f,    1.3666f},
				{0.7971f,    0.4139f},
				{1.1742f,   -0.8539f},
				{-0.8019f,   -1.3120f},
				{-3.6343f,   -1.3946f},
				{-1.6423f,   -2.0376f},
				{-1.9222f,   -1.0913f}};

		float[][]  testing = {{2.2406f,   4.8616f},
				{-2.1837f,   -0.2666f},
				{-2.3818f,    4.0282f},
				{2.0847f,   -0.4894f},
				{2.8386f,    3.0452f}};

		int[] trainingtargets = new int[10];
		for(int i =0 ; i < 5; i++)
			trainingtargets[i] = 1;
		for(int i =5 ; i < 10; i++)
			trainingtargets[i] = 2;

		float[] priors = new float[2];
		priors[0] = 0.3f;
		priors[1] = 0.7f;

		int[] predictions  = new int[5];
		double[] probest = new double[5];

		LDAClassifier classifier = new LDAClassifier(2, priors);
		classifier.classify(training,  trainingtargets, testing, predictions, probest);

		for(int i = 0; i < predictions.length; i++)
			System.out.println(predictions[i]); //11121

	}

	public LDAClassifier(int dimension)
	{
		this.dimension = dimension;
	}

	public LDAClassifier(int dimension, float[] priors)
	{
		this.dimension = dimension;
		this.priors = priors;
	}


	public void classify(float[][] trainingPatterns, int[] trainingtargets, float[][] testingPatterns, int[] predictions, double[] probesti)
	{
		//make sure that the targets are in the range of 0 ... ngroups -1
		//also set the ngroups
		int traininglength = trainingPatterns.length; 
		int testinglength = testingPatterns.length;
		int[] convertedTargets = convertTargets(trainingtargets);
		for(int i =0; i < convertedTargets.length; i++)
			System.out.println(convertedTargets[i]);
		
		if(priors == null && ngroups != 0)
			setUniformPriors();
		
		//get a matrix from centralized training patterns 
		float[][] means = new float[ngroups][dimension]; //need for testing
		double[][] normalizedTraining = normalizeTraining(trainingPatterns, convertedTargets, means);
		Matrix trainingM = new Matrix(normalizedTraining);

		//System.out.println("Training M:");
		//trainingM.print(10, 7);
		QRDecomposition decom = new QRDecomposition(trainingM);
		Matrix R = decom.getR();
		//System.out.println("R:");
		//R.print(10, 7);

		//R = R/sqrt(n-ngroups)
		double cons = Math.sqrt(traininglength - ngroups);
		Matrix B = new Matrix(R.getRowDimension(), R.getColumnDimension(),  cons);
		R.arrayRightDivideEquals(B);

		SingularValueDecomposition svd = new SingularValueDecomposition(R);
		//s is a vector containing the singular values
		//s = svd(R)
		double[] s = svd.getSingularValues();
		//for(int i =0; i<s.length; i++)
		//	System.out.println(s[i]);

		//error checking to see if the pooled covariance matrix of trainingM is positive definite
		//TODO

		//logDetSigma = 2*sum(log(s))
		double logDetSigma = 0;
		for(int i = 0; i<s.length; i++)
			logDetSigma += Math.log(s[i]);
		logDetSigma = 2*logDetSigma;

		//Now testing!
		//Multivariate Normal (MVN) relative log posterior density
		double[][] posterior = new double[testinglength][ngroups];
		for (int k = 0; k < ngroups; k++)
		{
			//normalize testing sample
			double[][] normalizedTesting = normalizeTesting(testingPatterns, means[k]);
			Matrix testingM = new Matrix(normalizedTesting);
			//A = (sample - repmat(gmea...)) /R; 
			//Matrix A = testingM.arrayRightDivide(R); -- element-by-element
			//A/B roughly the same as A*inv(B)
			Matrix A;
			try{
			A = testingM.times(R.inverse()); //may throw exception for singular R
			}catch(Exception e)
			{
				javax.swing.JOptionPane.showMessageDialog(null,"Matrix is singular. Results are not reliable. Please try another classifier.");
				return;
			}
			//D(:,k)=log(prior(k)) - .5*(sum(A.*A, 2) + logDetSigma);
			A.arrayTimesEquals(A);
			double[] sum = new double[A.getRowDimension()];
			for(int i =0; i<sum.length; i++)
			{
				for(int j =0; j< A.getColumnDimension(); j++)
					sum[i] += A.get(i,j);
				sum[i] += logDetSigma;
				sum[i] *= 0.5;
				//get the posterior density
				posterior[i][k] = Math.log(priors[k]) - sum[i];
			}
		}  

		for(int i = 0; i < testinglength; i++)
		{
			for(int k = 0; k < ngroups; k++)
				System.out.print(posterior[i][k] + "\t");
			System.out.println();
		}

		//fill predictions and prosesti
		for(int i = 0; i < testinglength; i++)
		{
			double max = posterior[i][0]; 
			int target = 0;
			for(int k = 1; k < ngroups; k++)
				if (posterior[i][k] > max)
				{
					target = k;
					max = posterior[i][k];
				}
			probesti[i] = max;
			predictions[i] =  ((Integer) targetmap.get(target)).intValue();	
		}


	}

	//
	// means: a matrix of ngroups *dimension
	// contains the mean of each group for each dimension
	//
	private double[][] normalizeTraining(float[][] trainingPatterns, int[] trainingtargets, float[][]  means)
	{   
		//Jama only works with double, so convert to double
		double[][] convertedPatterns = new double[trainingPatterns.length][dimension];

		//init the mean 
		for (int i = 0 ; i< ngroups; i++)
			for (int j = 0 ; j< dimension; j++)
				means[i][j] = 0;

		//get mean for each group and each dimension
		int[] groupcount = new int[ngroups];
		for (int i =0; i < ngroups; i++)
			groupcount[i] = 0;
		for (int i = 0 ; i< trainingPatterns.length; i++)
		{
			groupcount[trainingtargets[i]]++;
			for (int j =0; j < dimension; j++)
				means[trainingtargets[i]][j] += trainingPatterns[i][j];
		}

		//divide the count
		System.out.println("Means:");
		for (int i = 0 ; i< ngroups; i++)
		{
			for (int j = 0 ; j< dimension; j++)
			{
				means[i][j] /= (float)groupcount[i];
				//System.out.print(means[i][j] + "\t");
			}
			System.out.println();
		}

		for (int i = 0 ; i< trainingPatterns.length; i++)
			for (int j =0; j < dimension; j++)
				convertedPatterns[i][j] =  (double) trainingPatterns[i][j]- means[trainingtargets[i]][j];

		return convertedPatterns;

	}

	private double[][] normalizeTesting(float[][] testingPatterns, float[]  means)
	{
		//Jama only works with doouble, so convert to double
		double[][] convertedPatterns = new double[testingPatterns.length][dimension];

		for (int i = 0 ; i< testingPatterns.length; i++)
			for (int j =0; j < dimension; j++)
				convertedPatterns[i][j] =  (double) testingPatterns[i][j] - means[j];

		return convertedPatterns;

	}

	//	make sure that the targets are in the range of 0 ... ngroups -1
	//also build the targetMap for converting back
	private int[] convertTargets(int[] targets)
	{
		int[] convertedTargets = new int[targets.length];
		//targetmap is for converting back from new targets to original targets
		//key: new target; value: original target
		if (targetmap == null)
			targetmap = new java.util.HashMap();
		else
			targetmap.clear();

		//map the original target to new target, just for converting forward
		java.util.HashMap orig2new = new java.util.HashMap();

		int targetIndex = -1;
		for (int i=0; i < targets.length; i++)
		{
			if(!targetmap.containsValue(targets[i]))
			{
				targetIndex ++;
				targetmap.put(new Integer(targetIndex), new Integer(targets[i]));
				orig2new.put(new Integer(targets[i]),new Integer(targetIndex));
				convertedTargets[i] = targetIndex;
			}
			else
				convertedTargets[i] = ((Integer) orig2new.get(targets[i])).intValue();
		}	 
		
		ngroups = orig2new.size();
		return convertedTargets;
	}

	
	//set uniform priors (called when not already set by constructors)
	private void setUniformPriors()
	{
		if (ngroups != 0)
		{
			if (priors == null)
	    	   priors = new float[ngroups];
			for(int i = 0; i < ngroups; i++)
		  	   priors[i] = (float) 1.0/ngroups;
		}
	}	
}
