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
public class WekaClassifiers implements Classifier {

	int dimension = 1;
	private Instances m_Data = null;
	private weka.classifiers.Classifier m_Classifier = null;
	java.util.ArrayList<Integer> targetList = null;
	public static final String CLASSIFIER_TYPE = "CLASSIFIER_TYPE";
	
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
			m_Classifier = new weka.classifiers.lazy.IBk();
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
	    
		 // build classifier.
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
		{
			instance.setValue(i, features[i]);
		}
		
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
}
