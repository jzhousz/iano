package annotool.classify;

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.core.*;

//This classifier will use Weka's implementation of C4.5 tree called J48
//Weka: http://www.cs.waikato.ac.nz/ml/weka/
//Note: annotool.classify.Classifier is a different interface from
//     weka.classifiers.Classifier.
// Not used in GUI? 08/11
public class TreeClassifier implements Classifier {

	private Instances m_Data = null;
	//private weka.classifiers.Classifier m_Classifier = new J48();
	private weka.classifiers.Classifier m_Classifier = new weka.classifiers.trees.RandomForest();
	
	int dimension = 1;
	
	public TreeClassifier() {}
	
	 
	public void setParameters(java.util.HashMap<String, String> para) 
	{}
	
	public TreeClassifier(int dimension)
	{
		this.dimension = dimension;

	}
	
	//@Override follow the main() in weka example
	public void classify(float[][] trainingpatterns, int[] trainingtargets,
			float[][] testingpatterns, int[] predictions,  double[] prob) 
	{
		this.dimension = trainingpatterns[0].length;
		int traininglen = trainingpatterns.length;
		int testinglen = testingpatterns.length;
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
	
   //create an empty classifier/training set. In weka tutorial example's constructor.	
   public void createModel(float[][] trainingpatterns, int[] trainingtargets)
   {
	String nameOfDataset = "ImageClassificationProblem";

	// Create attributes (features in our case)
	FastVector attributes = new FastVector(dimension + 1);
	for (int i = 0 ; i < dimension; i++) {
	    attributes.addElement(new Attribute("f"+i));
	}

	// Add class attribute. (Names of classes)
	int numOfClasses = getTargetListSize(trainingtargets);
	FastVector classValues = new FastVector(numOfClasses);
	// Just use simple names since targets are just ints.
	// Assumption: the target "labels" in target file starts at 1.
	for (int i = 0; i<numOfClasses; i++)
	  classValues.addElement(String.valueOf(i+1)); 

	attributes.addElement(new Attribute("Class", classValues));
	// Create dataset with initial capacity of 100, and set index of class.
	m_Data = new Instances(nameOfDataset, attributes, 100);
	m_Data.setClassIndex(m_Data.numAttributes() - 1);
  }
	
	/**
	* Updates model using the given training image.
	* This is essentially training
	*/
	public void updateModel(float[] features, int classValue) throws Exception 
	{
	 // Convert message string into instance.
	 Instance instance = makeInstance(features);
	 instance.setClassValue(classValue-1);//index? which class instead of target label?
	 // Add instance to training data.
	 m_Data.add(instance);
	 // Use filter.
	 //m_Filter.inputFormat(m_Data);
	 //Instances filteredData = Filter.useFilter(m_Data, m_Filter);
	}
	
	
	/**
	* Classify: This is testing 
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

	//a useful util method for getting target into
    private int getTargetListSize(int[] targets)
    {
			java.util.ArrayList<Integer> targetList = new java.util.ArrayList<Integer>();
			for (int i=0; i < targets.length; i++)
				if(!targetList.contains(targets[i]))
					targetList.add(targets[i]);
			
			return targetList.size();
    }

    public boolean doesSupportProbability()
    {
    	return false;
    }
	
}
