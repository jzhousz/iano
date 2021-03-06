package annotool.classify;

import Jama.*;
/**
*  Linear Discriminant Classifier based on Multivariate Normal Distribution.
*  It estimates the pooled covariance matrix of all classes.
*/
// TODO 11/11/08:  
//       1. normalize posterior probability (done 07/26/2011)
//       2. check the pooled covariance matrix to be positive definite
//  

public class LDAClassifier implements SavableClassifier {

	int ngroups = 0;
	float[] priors = null;
	java.util.HashMap<Integer, Integer> targetmap = null;
	LDATrainedModel trainedModel = null;
	public final static String KEY_PRIORS = "Priors";

	/*
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

		//There are three ways to use this classifier.
        
		
        //the one method version		
		LDAClassifier classifier = new LDAClassifier(priors);
		try {
		classifier.classify(training,  trainingtargets, testing, predictions, probest);
		for(int i=0; i< 5; i++)
		  System.out.println(predictions[i]+":prob:"+probest[i]);
		}catch(Exception e)
		{ e.printStackTrace();}
        
 		
        /*  		
		//the two methods (train/classify) version
		java.util.HashMap<String, String> parameters = new java.util.HashMap<String, String>();
		parameters.put("Priors", "0.3 0.7");
		SavableClassifier classifier = new LDAClassifier(parameters);
		Object model = classifier.trainingOnly(training, trainingtargets);
		try{
		   predictions = classifier.classifyUsingModel(model, testing, probest);
			for(int i=0; i< 5; i++)
				  System.out.println(predictions[i]+":prob:"+probest[i]);

		}catch(Exception e)
		{ e.printStackTrace();}
		*/
		 
		/*
		//the save model to file version
		java.util.HashMap<String, String> parameters = new java.util.HashMap<String, String>();
		parameters.put("Priors", "0.3 0.7");
		SavableClassifier classifier = new LDAClassifier(parameters);
		Object amodel = classifier.trainingOnly(training, trainingtargets);
		try {
			System.out.println("Saving...");
			classifier.saveModel(amodel, "testLDA_model");
		  
            //somewhere else		
			System.out.println("Loading...");
			Object model = classifier.loadModel("testLDA_model");
 	        predictions = classifier.classifyUsingModel(model, testing, probest);
		}catch(Exception e)
		{ e.printStackTrace();}
		
		for(int i = 0; i < predictions.length; i++)
			System.out.println(predictions[i]+ ":probability"+ probest[i]); //11121
		
		*/
	}

	/**
    * Default constructor. 
    */
	public LDAClassifier() {}
	
    /**
    * Sets algorithm parameters from para
    * 
    * @param   para  Each element of para holds a parameter name
    *                for its key and a its value is that of the parameter.
    *                The parameters should be the same as those in the 
    *                algorithms.xml file.
    */
	public void setParameters(java.util.HashMap<String, String> para)
	{
		//set prior if provided
		if(para != null && para.containsKey(KEY_PRIORS))
	          priors = parsePriors(para.get(KEY_PRIORS));
	}
	
    /**
    * Constructor that sets priors if it has not been set already
    * 
    * @param   para    Each element of para holds a parameter name
    *                  for its key and a its value is that of the parameter.
    *                  The parameters should be the same as those in the 
    *                  algorithms.xml file. Piror probabilities is a parameter 
    *                  that can be set.   If no parameter needs to be set, pass null.
    */
	public LDAClassifier(java.util.HashMap<String, String> para)
	{
		//set prior if provided
		if(para != null && para.containsKey(KEY_PRIORS))
	          priors = parsePriors(para.get(KEY_PRIORS));
	}

    /**
    * Constructor that copies priors to an instance variable
    * 
    * @param   priors  The prior probabilities
    */
	public LDAClassifier(float[] priors)
	{
		this.priors = priors;
	}

    /**
    * Classifies the patterns using the input parameters.
    * 
    * @param   trainingpatterns  Pattern data to train the algorithm
    * @param   trainingtargets   Targets for the training pattern
    * @param   testingpatterns   Pattern data to be classified
    * @param   predictions       Storage for the resulting prediction
    * @param   prob              Storage for probability result
    * @throws  Exception         Not Implemented
    */
    //Posterior probabilities are calculated and filled.
	public void classify(float[][] trainingPatterns, int[] trainingtargets, float[][] testingPatterns, int[] predictions, double[] probesti) throws Exception
	{
		LDATrainedModel trainedModel = (LDATrainedModel) trainingOnly(trainingPatterns, trainingtargets);
	    int[] results = classifyUsingModel(trainedModel, testingPatterns, probesti);

	    //set to the passed in structure
	    for(int i=0; i<predictions.length; i++)
	    	predictions[i] = results[i]; 
	}

	// Do data transform to double, and calculate the means.
	// means: a matrix of ngroups *dimension
	// It contains the means of each group for each dimension.
	private double[][] normalizeTraining(float[][] trainingPatterns, int[] trainingtargets, float[][]  means)
	{   
		//Jama only works with double, so convert to double
		int dimension = trainingPatterns[0].length;
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
		//System.out.println("Means:");
		for (int i = 0 ; i< ngroups; i++)
		{
			for (int j = 0 ; j< dimension; j++)
			{
				means[i][j] /= (float)groupcount[i];
				//System.out.print(means[i][j] + "\t");
			}
			//System.out.println();
		}

		for (int i = 0 ; i< trainingPatterns.length; i++)
			for (int j =0; j < dimension; j++)
				convertedPatterns[i][j] =  (double) trainingPatterns[i][j]- means[trainingtargets[i]][j];

		return convertedPatterns;

	}

	private double[][] normalizeTesting(float[][] testingPatterns, float[]  means)
	{
		//Jama only works with double, so convert to double
		int dimension = testingPatterns[0].length;
		double[][] convertedPatterns = new double[testingPatterns.length][dimension];

		for (int i = 0 ; i< testingPatterns.length; i++)
			for (int j =0; j < dimension; j++)
				convertedPatterns[i][j] =  (double) testingPatterns[i][j] - means[j];

		return convertedPatterns;

	}

	//Make sure that the targets are in the range of 0 ... ngroups -1
	//Build the targetMap for converting back targets.
	private int[] convertTargets(int[] targets)
	{
		int[] convertedTargets = new int[targets.length];
		//targetmap is for converting back from new targets to original targets
		//key: new target; value: original target
		if (targetmap == null)
			targetmap = new java.util.HashMap<Integer, Integer>();
		else
			targetmap.clear();

		//map the original target to new target, just for converting forward
		java.util.HashMap<Integer, Integer> orig2new = new java.util.HashMap<Integer, Integer>();

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
		System.out.println("Number of groups:"+ngroups);
		return convertedTargets;
	}

	
	//set uniform priors (called when not already set by constructors)
	private void setUniformPriors()
	{
		if (ngroups != 0)
		{
			//if (priors == null)  //overwrite even if it is null
	    	priors = new float[ngroups];
			for(int i = 0; i < ngroups; i++)
		  	   priors[i] = (float) 1.0/ngroups;
		}
	}
	
   /**
    * Trains and returns an internal model using a training set.
    * 
    * @param   trainingpatterns  Pattern data to train the algorithm
    * @param   trainingtargets   Targets for the training pattern
    * @return                    Model created by the classifier
    * @throws  Exception         Thrown if there is a problem in training
    */ 
    public Object trainingOnly(float[][] trainingPatterns, int[] trainingtargets) throws Exception
    { 
		int traininglength = trainingPatterns.length; 
		int dimension = trainingPatterns[0].length;
		int[] convertedTargets = convertTargets(trainingtargets);
		
		System.out.print("ngroups before checking priors "+ ngroups);
		
		if(priors == null && ngroups != 0)
			setUniformPriors();
		if(priors != null  && priors.length != ngroups)
		{ 
			System.out.println("Priors must be one per class. Set to uniform instead.");
			setUniformPriors();
		}
		
		//get a matrix from centralized training patterns 
		float[][] means = new float[ngroups][dimension]; //will be needed in testing
		double[][] normalizedTraining = normalizeTraining(trainingPatterns, convertedTargets, means);
		Matrix trainingM = new Matrix(normalizedTraining);

		Matrix R = null;
		try{
		    QRDecomposition decom = new QRDecomposition(trainingM);
		    R = decom.getR();
		}catch(Exception e)
		{
			throw new Exception("Problem in LDA training. Try to reduce the number of features.");
		}
		//R = R/sqrt(n-ngroups)
		double cons = Math.sqrt(traininglength - ngroups);
		Matrix B = new Matrix(R.getRowDimension(), R.getColumnDimension(),  cons);
		R.arrayRightDivideEquals(B);

		SingularValueDecomposition svd = new SingularValueDecomposition(R);
		//s is a vector containing the singular values
		//s = svd(R)
		double[] s = svd.getSingularValues();
		//error checking to see if the pooled covariance matrix of trainingM is positive definite
		//TODO

		//logDetSigma = 2*sum(log(s))
		double logDetSigma = 0;
		for(int i = 0; i<s.length; i++)
			logDetSigma += Math.log(s[i]);
		logDetSigma = 2*logDetSigma;

		trainedModel = new LDATrainedModel(R,logDetSigma, means, priors, targetmap);
		
		return trainedModel;
    }
    
    
    /**
    * Gets the internal model from the classifier
    * 
    * @return  Model created by the classifier.
    */
    public Object getModel()  
    { 
    	return trainedModel; 
    }
    
    /**
    * Sets an internal model to be used by the classifier
    * 
    * @param   model      Model to be used by the classifier
    */
    public void setModel(Object model) //load
    { 
        trainedModel = (LDATrainedModel) model;    	
    }
    
    /**
    * Classifies the internal model using one testing pattern
    * 
    * @param   model            Model to be used by the classifier
    * @param   testingPattern   Pattern data to be classified
    * @param   prob             Storage for probability result
    * @return                   The prediction result
    * @throws  Exception        Not implemented
    */
    //call the overloaded version
    public int classifyUsingModel(Object model, float[] testingPattern, double[] prob) throws Exception
    { 
     	float[][] testingPatterns = new float[1][testingPattern.length];
     	testingPatterns[0] = testingPattern;
     	int[] results = classifyUsingModel(model, testingPatterns, prob);
     	return results[0];
    
    }
    
    /**
    * Classifies the internal model using multiple testing patterns
    * 
    * @param   model             Model to be used by the classifier
    * @param   testingPatterns   Pattern data to be classified
    * @param   prob              Storage for probability result 
    * @return                    Array of prediction results
    * @throws  Exception         Exception thrown if matrix is singular
    */
    public int[] classifyUsingModel(Object model, float[][] testingPatterns, double[] probesti) throws Exception
    { 
      	if (model != null) //model may be null, but only when the internal model is already set.
       		if (model instanceof LDATrainedModel) //pass in an internal model
       			trainedModel = (LDATrainedModel) model;
    	else  if(trainedModel == null)
    	{  //when model is null && there is no instance variable that contains a valid model
    		System.err.println("Err: must pass in a model.");
    		throw new Exception("Err: must pass in a model.");
    	}   	
  
       	Matrix R = trainedModel.getTrainedR();
       	double logDetSigma = trainedModel.getLogDetSigma();
       	float[][] means = trainedModel.getMeans();
       	float[] priors =trainedModel.getPriors();
       	java.util.HashMap<Integer, Integer> targetMap =trainedModel.getTargetMap();
       	int ngroups = means.length;
       	int testinglength = testingPatterns.length;
       	
		double[][] posterior = new double[testinglength][ngroups];
		int[] predictions = new int[testinglength];
		
		for (int k = 0; k < ngroups; k++)
		{
			//normalize testing sample
			double[][] normalizedTesting = normalizeTesting(testingPatterns, means[k]);
			Matrix testingM = new Matrix(normalizedTesting);
			//A = (sample - repmat(gmea...)) /R; 
			//A/B roughly the same as A*inv(B)
			Matrix A;
			try{
			    A = testingM.times(R.inverse()); //may throw exception for singular R
			}catch(Exception e)
			{
				System.err.println("Matrix is singular. Results are not reliable. Please try another classifier.");
				javax.swing.JOptionPane.showMessageDialog(null,"Matrix is singular. Results are not reliable. Please try another classifier.");
				throw e;
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

		//fill predictions 
		for(int i = 0; i < testinglength; i++)
		{
			double max = posterior[i][0]; 
			int target = 0;
			//use total to normalize posterior probability
			double total = Math.exp(posterior[i][0]);

			for(int k = 1; k < ngroups; k++)
			{
				if (posterior[i][k] > max)
				{
					target = k;
					max = posterior[i][k];
				}
			    total += Math.exp(posterior[i][k]);
			}
			//probesti[i] = max;  
			probesti[i] = Math.exp(max)/total;
			predictions[i] =  ((Integer) targetMap.get(target)).intValue();	
		}
      	
    	return predictions;
    
    }

    /**
    * Saves a specified model to a specified file
    * 
    * @param   trainedModel     Trained model that is to be saved
    * @param   model_file_name  Name of the file to be saved to
    * @throws  Exception        Exception thrown if model is not valid
    */
    public void saveModel(Object trainedModel, String model_file_name) throws java.io.IOException
    {
    	if (!(trainedModel instanceof LDATrainedModel))
    	{ 
    		System.err.println("The model is not valid");
    	}
    	else
       	//persist to the file
    	{
    		java.io.ObjectOutputStream filestream = new java.io.ObjectOutputStream(new java.io.FileOutputStream(model_file_name));
    		filestream.writeObject(trainedModel);
    		filestream.close();
    	}
    }

    /**
    * Loads a previously saved model back into the classifier.
    * 
    * @param   model_file_name  Name of the file to be loaded
    * @return                   Model that was loaded
    * @throws  Exception        Exception thrown if file cannot be loaded
    */
    public Object loadModel(String model_file_name) throws java.io.IOException
    {  
    	//read from the file and cast it to trainedModel;
    	LDATrainedModel model = null;
    	java.io.ObjectInputStream filestream = new java.io.ObjectInputStream(new java.io.FileInputStream(model_file_name));
    	try
    	{
    	  model = (LDATrainedModel) filestream.readObject();
    	}catch(ClassNotFoundException ce)
    	{
    		System.err.println("Class Not Found in Loading model");
    	}
    	filestream.close();
    	return model;
    }

    //parse the string to set the priors instance variable
    //split using " " to get priors  e.g. "0.1 0.3 0.6" 
    private float[] parsePriors(String priorsS)
    {
    	String[] res = priorsS.split(" ");
    	float[] priors = new float[res.length];

    	float total = 0;
    	try
    	{
    	 for(int i=0; i<priors.length; i++)
    	 {
    		priors[i] = Float.parseFloat(res[i]);
    		total += priors[i];
    	 }
    	}catch(NumberFormatException e)
    	{
    		System.err.println("parsing problem for priors");
    		return null;
    		
    	}
      	//check if they add up right
    	if (total > 1.0)
    	{
    		System.err.println("The sum of prior probability of all groups should be 1.0");
    		return null;
    	}
    	return priors;
    }
	
     /**
     * Returns whether or not the algorithm uses probability estimations.
     * 
     * @return  <code>True</code> if the algorithm uses probability 
     *          estimations, <code>False</code> if not
     */
    public boolean doesSupportProbability()
    {  
    	return true;
    }
}
