package annotool.select;

import annotool.select.FeatureSelector;
//import weka.filters.supervised.attribute.*;
import weka.filters.Filter;
import weka.attributeSelection.*;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.*;


/*
 * Weka has 2 types of feature selectors:
There are two types of evaluators that you can specify with –E: 
ones that consider one attribute at a time, and ones that consider sets of attributes
together. 
The former are subclasses of weka.attributeSelection.AttributeEvaluator—an example is weka.attributeSelection.
InfoGainAttributeEval, which evaluates attributes according to their
information gain. 
The latter are subclasses of weka.attributeSelection.SubsetEvaluator—like weka.attributeSelection.CfsSubsetEval, which
evaluates subsets of features by the correlation among them. 
If you give the name of a subclass of AttributeEvaluator, you must also provide, using
–T, a threshold by which the filter can discard low-scoring attributes. 
On the other hand, if you give the name of a subclass of SubsetEvaluator, you
must provide the name of a search class using –S, which is used to search
through possible subsets of attributes. Any subclass of weka.attributeSelection.ASSearch can be used for this option—for example weka.attributeSelection.BestFirst, which implements a bestfirst
search.
E.g.:
java weka.filters.AttributeSelectionFilter
-S weka.attributeSelection.BestFirst
-E weka.attributeSelection.CfsSubsetEval
-i weather.arff -c5

ASEvaluator eval = new FunkyEvaluator();
System.out.println(SelectAttributes(Evaluator, args));

weka.attributeSelection.AttibuteSelection selector = new weka.attributeSelection.AttributeSelection();
ASEvaluator eval = new weka.attributeSelection.InfoGainAttributeEval();
selector.setEvaluator(eval);
selector.SelectAttributes(m_data));
System.out.println(selector.selectedAttributes());
System.out.println(selector.numberAttributesSelected()); 
System.out.println(selector.toResultsString());

*/


//The numofFeatures is based on the algorithm
//Need to modify interface.

public class WeKaFeatureSelectors implements FeatureSelector {

	
	float[][] features;
	int[] targets;
	int length = 0;
	int dimension = 0;
	int numberofFeatures = 10; //default
	int[] indices = null;
    double threshold = 0.2; //info gain threshold
	
	private Instances m_Data = null;
	private weka.attributeSelection.AttributeSelection selector = null;

	java.util.ArrayList<Integer> targetList = null;
	
	public WeKaFeatureSelectors(float[][] features, int[] targets, int numberofFeatures, String method, double threshold)
	{
		this.features = features;
		this.targets = targets;
		this.length = features.length;
		this.dimension = features[0].length;
		this.numberofFeatures = numberofFeatures; //will be reset by selector
		this.threshold = threshold; 
		/*
		//filter set evaluator class
		m_Filter.setEvaluator(new weka.attributeSelection.CfsSubsetEval());
		//set search class for a subset evaluator
		weka.attributeSelection.BestFirst searcher = new weka.attributeSelection.BestFirst();
		m_Filter.setSearch(searcher);
        */

        /*		//too slow for images.
	    AttributeSelection attsel = new AttributeSelection();
	    CfsSubsetEval eval = new CfsSubsetEval();
	    GreedyStepwise search = new GreedyStepwise();
	    search.setSearchBackwards(true);
	    attsel.setEvaluator(eval);
	    attsel.setSearch(search);
	    try{
	    	System.out.println("started ..");
	    attsel.SelectAttributes(m_Data);
	    int[] indices = attsel.selectedAttributes();
	    System.out.println("selected attribute indices (starting with 0):\n" + Utils.arrayToString(indices));
	    }catch(Exception e)
	    { e.printStackTrace();}
		*/
	}

	public float[][] selectFeatures()
	{
	   System.out.println("in selectFeature()...");	

		//set m_Data by converting them from float to to m_data 
		updateM_Data(features, targets);

		weka.attributeSelection.AttributeSelection selector = new weka.attributeSelection.AttributeSelection();
		AttributeEvaluator eval = new weka.attributeSelection.InfoGainAttributeEval();
		weka.attributeSelection.Ranker searcher = new weka.attributeSelection.Ranker();
	    searcher.setThreshold(threshold);
		selector.setEvaluator(eval);
		selector.setSearch(searcher);

	   
	    double rs[][] = null;  
		try{
			selector.SelectAttributes(m_Data);
			indices = selector.selectedAttributes();
		    numberofFeatures = selector.numberAttributesSelected(); 

			//debug: 
			System.out.println(selector.toResultsString());
			rs = selector.rankedAttributes(); 
			
			}catch(Exception e)
			{ e.printStackTrace();}
			
	   System.out.println("numberofFeatures:"+numberofFeatures);
	   
  	   float[][] selectedFeatures = new float[length][numberofFeatures];
  	   for(int i = 0; i< length; i++)
	       for(int j = 0; j< numberofFeatures; j++)
  		     selectedFeatures[i][j]= features[i][indices[j]]; 

  	   return selectedFeatures;
    }

    //m_data is filled after this method  
	private void updateM_Data(float[][] features, int[] targets)
    {
	  System.out.println("creating model ...");	
      createModel(features, targets);

      try
	  {  for (int i=0; i<length; i++)
	     {
		   //System.out.println("updating model ...");	
 		   updateModel(features[i],targets[i]);
	     }
	  }catch(Exception e)
	  {
	       System.out.println("Error in getting Instances"+e.getMessage());
	  }
    }
	

	
	public int[] getIndices()
	{
		return indices;
        
	}

	//using passed in features, length, numberofFeatures from constructor
	public float[][] selectFeaturesGivenIndices(int[] indices)
	{  
		float[][] selectedFeatures = new float[length][numberofFeatures];
		
		for(int i=0; i<length; i++)
			for(int j=0; j<numberofFeatures; j++)
			{
				//System.out.println("j: " + j + "feature index"+ indices[j]);
				selectedFeatures[i][j] = features[i][indices[j]];
				//System.out.println(selectedFeatures[i][j]);
			}

		return selectedFeatures;
	}
	
	//may have changed after selection, caller can get from selectedFeatures[0].length
	public int getNumOfFeatures()
	{
	    return numberofFeatures;	
	}
	
	//--- The following methods build m_data ---
	//--- Similar as in WeakClassifiers.java ---
	//--- IANO simply uses float to transport data, otherwise we can reuse. ---
	
   //create an empty classifier/training set. In weka tutorial example's constructor.	
   public void createModel(float[][] trainingpatterns, int[] trainingtargets)
   {
		String nameOfDataset = "ImagesForFeatureSelection";

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
	}
	
	private Instance makeInstance(float features[]) 
	{
		Instance instance = new Instance(dimension + 1);
		for (int i = 0; i < dimension; i++) 
				instance.setValue(i, features[i]);
			
		instance.setDataset(m_Data);
			
		return instance;
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
}
