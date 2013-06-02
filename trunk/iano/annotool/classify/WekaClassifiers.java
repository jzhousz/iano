package annotool.classify;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.core.*;
/**
* This is a wrapper for a collection of some classifiers provided in Weka.
* Weka: http://www.cs.waikato.ac.nz/ml/weka/
* Note: annotool.classify.Classifier is a different interface from
*    weka.classifiers.Classifier.
*/
public class WekaClassifiers implements SavableClassifier {

	int dimension = 1;
	private Instances m_Data = null;
	private weka.classifiers.Classifier m_Classifier = null;
	java.util.ArrayList<Integer> targetList = null;
	public static final String CLASSIFIER_TYPE = "CLASSIFIER_TYPE";
	//other parameters for specific classifiers 6/1/2013
	public static final String K_KNN = "Number of Nearest Neighbors";
	public static final String K_RF = "Number of Trees in the Forest";
	public static final String F_RF = "Number of Features to Consider";
	public static final	String KE_NB  = "Use Kernel Estimation";
	public static final String SD_NB  = "Use Supervised Discretization";
	public static final String UP_TREE  = "Use Unpruned Tree";
	public static final String K_TREE  = "Min Number of Samples (Images) Per Leaf";

	/**
    * Default constructor
    */
	public WekaClassifiers() {}

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
		String classifierType = "W_Tree"; //default

		if(para != null && para.containsKey(CLASSIFIER_TYPE))
	          classifierType = para.get(CLASSIFIER_TYPE);

		if (classifierType.equalsIgnoreCase("W_Tree"))
		{
				m_Classifier = new J48();
			   if("1".equals(para.get(UP_TREE)))
				   ((J48) m_Classifier).setUnpruned(true);
			   else
				   ((J48)  m_Classifier).setUnpruned(false);
			   int k = Integer.parseInt(para.get(K_TREE));
			   ((J48) m_Classifier).setMinNumObj(k);
		}
		else if (classifierType.equalsIgnoreCase("W_NaiveBayes"))
		{
			   m_Classifier = new NaiveBayes();
			   if("1".equals(para.get(KE_NB)))
				   ((NaiveBayes)m_Classifier).setUseKernelEstimator(true);
			   else
				   ((NaiveBayes)m_Classifier).setUseKernelEstimator(false);
			   if("1".equals(para.get(SD_NB)))
				   ((NaiveBayes)m_Classifier).setUseSupervisedDiscretization(true);
			   else
				   ((NaiveBayes)m_Classifier).setUseSupervisedDiscretization(false);
		}
		else if (classifierType.equalsIgnoreCase("W_RandomForest"))
		{
			int k = Integer.parseInt(para.get(K_RF));
			int f = Integer.parseInt(para.get(F_RF));

			m_Classifier = new weka.classifiers.trees.RandomForest();
			((weka.classifiers.trees.RandomForest) m_Classifier).setNumTrees(k);
			((weka.classifiers.trees.RandomForest) m_Classifier).setNumFeatures(f);
	    }
		else if (classifierType.equalsIgnoreCase("W_NearestNeighbor"))
	    {
			//get parameters for KNN.
			int k = Integer.parseInt(para.get(K_KNN));
			m_Classifier = new weka.classifiers.lazy.IBk(k);
		}
		else
				System.err.println("Not a classifier support by WekaClassifiers.");

	}

	/**
    * Constructor that takes dimension and classifierType and
    * chooses which classifier to use
    *
    * @param  dimension       The size of the image to use
    * @param  classifierType  The name of the classifier to use
    */
	public WekaClassifiers(int dimension, String classifierType) {
		this.dimension = dimension;
		if (classifierType.equalsIgnoreCase("W_Tree"))
			m_Classifier = new J48();
		else if (classifierType.equalsIgnoreCase("W_NaiveBayes"))
			m_Classifier = new NaiveBayes();
		else if (classifierType.equalsIgnoreCase("W_RandomForest"))
			m_Classifier = new weka.classifiers.trees.RandomForest();
		else if (classifierType.equalsIgnoreCase("W_NearestNeighbor"))
			m_Classifier = new weka.classifiers.lazy.IBk();
		else
			System.err.println("Not a classifier support by WekaClassifiers.");
	}

	/**
    * Constructor that takes classifierType and
    * chooses which classifier to use
    *
    * @param  classifierType  The name of the classifier to use
    */
    public WekaClassifiers(String classifierType) {

		if (classifierType.equalsIgnoreCase("W_Tree"))
			m_Classifier = new J48();
		else if (classifierType.equalsIgnoreCase("W_NaiveBayes"))
			m_Classifier = new NaiveBayes();
		else if (classifierType.equalsIgnoreCase("W_RandomForest"))
			m_Classifier = new weka.classifiers.trees.RandomForest();
		else if (classifierType.equalsIgnoreCase("W_NearestNeighbor"))
		{
			m_Classifier = new weka.classifiers.lazy.IBk();
		}
		else
			System.err.println("Not a classifier support by WekaClassifiers.");
	}

	/**
    * Classifies the patterns using the input parameters.
    *
    * @param   trainingpatterns  Pattern data to train the algorithm
    * @param   trainingtargets   Targets for the training pattern
    * @param   testingpatterns   Pattern data to be classified
    * @param   predictions       Storage for the resulting prediction
    * @param   prob              Storage for probability result (Unused)
    * @throws  Exception         Thrown if classification fails
    */
	public void classify(float[][] trainingpatterns, int[] trainingtargets,
			float[][] testingpatterns, int[] predictions,  double[] prob)
	{
		this.dimension = trainingpatterns[0].length;
		int traininglen = trainingpatterns.length;
		int testinglen =testingpatterns.length;
	    createModel(trainingpatterns, trainingtargets);

	    try
	    {
		 for (int i=0; i<traininglen; i++)
			updateModel(trainingpatterns[i],trainingtargets[i]);

		 // build classifier, training
		 m_Classifier.buildClassifier(m_Data);

	    //classify
		 for (int i=0; i<testinglen; i++)
			 predictions[i] = Integer.parseInt(classifyImage(testingpatterns[i]));

	    }
	    catch(Exception e)
	    { e.printStackTrace();}

	}

	/**
    * Creates a weka model to be used by the weka algorithm.
    *
    * @param   trainingpatterns  Pattern data to train the algorithm (Unused)
    * @param   trainingtargets   Targets for creating class labels
    */
	//create an empty classifier/training set. In weka tutorial example's constructor.
	public void createModel(float[][] trainingpatterns, int[] trainingtargets)
	{
		String nameOfDataset = "ImageClassificationProblem";

		// Create attributes (features in our case)
		FastVector attributes = new FastVector(dimension + 1);
		for (int i = 0 ; i < dimension; i++) {
			attributes.addElement(new Attribute("f"+i));
		}

		// Add class attribute. (target labels in target file)
		Integer[] classLabels = getTargetList(trainingtargets);
		// Weka requires its own format of vector class.
		FastVector classValues = new FastVector(classLabels.length);
		for (int i = 0; i< classLabels.length; i++)
			classValues.addElement(String.valueOf(classLabels[i]));

		attributes.addElement(new Attribute("Class", classValues));
		// Create dataset with initial capacity of 100, and set index of class.
		m_Data = new Instances(nameOfDataset, attributes, 100);
		m_Data.setClassIndex(m_Data.numAttributes() - 1);
	}

	/**
    * Update the weka model using the input parameters.
    *
    * @param   features    Features to be converted into an instance object
    * @param   classValue  Value to be converted into an index
    * @throws  Exception   (Not used)
    */
	public void updateModel(float[] features, int classValue) throws Exception
	{
		// Convert into an instance.
		Instance instance = makeInstance(features);

		if (targetList == null) //should not be, since Model was created already.
			System.err.println("Target list is not read right in WekaClassifiers.");
		int index = targetList.indexOf(classValue);
		instance.setClassValue(index);

		// Add instance to training data.
		m_Data.add(instance);

		// Use filter.
		//m_Filter.inputFormat(m_Data);
		//Instances filteredData = Filter.useFilter(m_Data, m_Filter);
	}


	/**
    * Classifies the image and returns the prediction results.
    *
    * @param   features    Array of features to classify
    * @return              The prediction results of the classification
    * @throws  Exception   Thrown if no classifier is available
    */
	public String classifyImage(float[] features) throws Exception {
		// Check if classifier has been built.
		if (m_Data.numInstances() == 0) {
			throw new Exception("No classifier available.");
		}
		// Convert message string into instance.
		Instance instance = makeInstance(features);
		// Filter instance.
		//m_Filter.input(instance);
		//Instance filteredInstance = m_Filter.output();
		// Get index of predicted class value.
		double predicted = m_Classifier.classifyInstance(instance);
		// Classify instance.
		System.err.println("classified as : "+ m_Data.classAttribute().value((int)predicted));

		//return predicted; //predicted is the class value index, not the actual label.
		return m_Data.classAttribute().value((int)predicted);
	}

	private Instance makeInstance(float features[])
	{

		Instance instance = new Instance(dimension + 1);
		// Initialize counts to zero.
		for (int i = 0; i < dimension; i++)
			instance.setValue(i, features[i]);

		instance.setDataset(m_Data);
		return instance;
	}

	//a useful utility method for getting target into
    private int getTargetListSize(int[] targets)
    {
		java.util.ArrayList<Integer> targetList = new java.util.ArrayList<Integer>();
		for (int i=0; i < targets.length; i++)
			if(!targetList.contains(targets[i]))
				targetList.add(targets[i]);

		return targetList.size();
    }


    private Integer[] getTargetList(int[] targets)
    {
		targetList = new java.util.ArrayList<Integer>();
		for (int i=0; i < targets.length; i++)
			if(!targetList.contains(targets[i]))
				targetList.add(targets[i]);

		Integer res[] = new Integer[targetList.size()];
		return targetList.toArray(res);
    }

    /**
    * Returns whether or not the algorithm uses probability estimations.
    *
    * @return  <code>False</code> because the algorithm does not use probability estimations
    */
    public boolean doesSupportProbability()
    {
    	return false;
    }


    /**
     * Gets the internal model from the classifier
     *
     * @return  Model created by the classifier.
     */
 	public Object getModel()
 	{
 		return m_Classifier;
 	}



    /**
     * Sets an internal model to be used by the classifier
     *
     * @param   model      Model to be used by the classifier
     * @throws  Exception  Exception thrown if model is incompatible
     */
 	public void setModel(java.lang.Object model) throws Exception
 	{
 	   	if(model instanceof weka.classifiers.Classifier)
 	   	   m_Classifier = (weka.classifiers.Classifier)model;
    	else
    	{
    		System.err.print("Not a valid model type for weka classifier");
    		throw new Exception("Not a valid model type for weka classifer");
    	}

 	}



    /**
     * Classifies the internal model using one testing pattern
     *
     * @param   model            Model to be used by the classifier
     * @param   testingPattern   Pattern data to be classified
     * @param   prob             Storage for probability result
     * @return                   The prediction result
     * @throws  Exception        Exception thrown if model is incompatible
     */
 	public int classifyUsingModel(Object model, float[] testingPattern, double[] prob) throws Exception
 	{
      	if (model != null) //model may be null, but only when the internal model is already set.
       		if (model instanceof weka.classifiers.Classifier) //pass in an internal model
       		   setModel(model);
    	else if(m_Classifier == null)
    	{  //when model is null && there is no instance variable that contains a valid model
    		System.err.println("Err: must pass in a model.");
    		throw new Exception("Err: must pass in a model.");
    	}

      	//classify:
      	return Integer.parseInt(classifyImage(testingPattern));
 	}


    /**
     * Classifies the internal model using multiple testing patterns
     *
     * @param   model             Model to be used by the classifier
     * @param   testingPatterns   Pattern data to be classified
     * @param   prob              Storage for probability result
     * @return                    Array of prediction results
     * @throws  Exception         Exception thrown if model is incompatible
     */
 	public int[] classifyUsingModel(Object model, float[][] testingPatterns, double[] prob) throws Exception
 	{
      	if (model != null) //model may be null, but only when the internal model is already set.
       		if (model instanceof weka.classifiers.Classifier) //pass in an internal model
       		   setModel(model);
    	else if(m_Classifier == null)
    	{  //when model is null && there is no instance variable that contains a valid model
    		System.err.println("Err: must pass in a model.");
    		throw new Exception("Err: must pass in a model.");
    	}

    	//allocate predictions
    	int[] predictions = new int[testingPatterns.length];
     	for(int i = 0; i < testingPatterns.length; i++)
    	  predictions[i] = classifyUsingModel(null, testingPatterns[i], null);

    	return predictions;
 	}

    /**
     * Saves a specified model to a specified file
     *
     * @param   trainedModel     Trained model that is to be saved
     * @param   model_file_name  Name of the file to be saved to
     * @throws  Exception        Exception thrown if model cannot be saved
     */
     public void saveModel(Object trainedModel, String model_file_name) throws java.io.IOException
     {
    	  	System.out.println("Saving Weka model to "+ model_file_name);

        	java.io.ObjectOutputStream filestream = new java.io.ObjectOutputStream(new java.io.FileOutputStream(model_file_name));
    		filestream.writeObject(m_Classifier);
    		filestream.close();
     }


    /**
     * Loads a previously saved model back into the classifier.
     *
     * @param   model_file_name  Name of the file to be loaded
     * @return                   Model that was loaded
     * @throws  Exception        Exception thrown if file cannot be found
     */
     public Object loadModel(String model_file_name) throws java.io.IOException
     {
    	   	System.out.println("Loading Weka model from "+ model_file_name);

    	   	weka.classifiers.Classifier model = null;
        	java.io.ObjectInputStream filestream = new java.io.ObjectInputStream(new java.io.FileInputStream(model_file_name));
        	try
        	{
        		model = (weka.classifiers.Classifier) filestream.readObject();
        	}catch(ClassNotFoundException ce)
        	{
        		System.err.println("Class Not Found in Loading SVM model");
        	}
        	filestream.close();
        	return model;
     }

     public Object trainingOnly(float[][] trainingpatterns, int[] trainingtargets)
     {
 		this.dimension = trainingpatterns[0].length;
		int traininglen = trainingpatterns.length;

		createModel(trainingpatterns, trainingtargets);

	    try
	    {
		 for (int i=0; i<traininglen; i++)
			updateModel(trainingpatterns[i],trainingtargets[i]);

		 // build classifier, training
		 m_Classifier.buildClassifier(m_Data);

	    }
	    catch(Exception e)
	    { e.printStackTrace();}

	    return m_Classifier;
    }
}
