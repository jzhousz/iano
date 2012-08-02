package annotool.classify;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * This class creates and handles the Weka instance model
 */
//for building an Instances object from float[][]
//usage: (new WekaHelper()).buildM_Data(features, targets, m_Data, "...");
public class WekaHelper {

	java.util.ArrayList<Integer> targetList = null;

    /**
    * Handles the creation of an instance model
    * 
    * @param   features   Image data to use in the weka model
    * @param   targets    Image targets to use in the weka model
    * @param   dsName     Name of the object
    * @return             Instance of the model
    * @throws  Exception  Exception thrown if instance cannot be retrieved
    */
	//m_data is filled after this method  
	public Instances buildM_Data(float[][] features, int[] targets, String dsName)
    {
	  System.out.println("creating model ...");	
      Instances m_Data = createModel(features, targets, dsName);

      try
	  {  for (int i=0; i<features.length; i++)
	     {
 		   updateModel(features[i],targets[i], m_Data);
	     }
	  }catch(Exception e)
	  {
	       System.out.println("Error in getting Instances"+e.getMessage());
	  }
	  System.out.println("model created.");	
	  return m_Data;
    }
	
	//--- The following methods build m_data ---
	//--- Similar as in WeakClassifiers.java ---
	//--- IANO simply uses float to transport data, otherwise we can reuse. ---
	
    //create an empty classifier/training set. In weka tutorial example's constructor.	
    private Instances createModel(float[][] trainingpatterns, int[] trainingtargets, String dsName)
    {
		// Create attributes (features in our case)
		FastVector attributes = new FastVector(trainingpatterns[0].length + 1);
		for (int i = 0 ; i < trainingpatterns[0].length; i++) {
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
		Instances m_Data = new Instances(dsName, attributes, 100);
		m_Data.setClassIndex(m_Data.numAttributes() - 1);
		
		return m_Data;
    }

    /**
    * Update an weka model and adds it to the Instances object
    * 
    * @param   features    Image data to update the weka model
    * @param   classValue  Image target value to update the weka model
    * @param   m_Data      Instances object to add the updated weka model to
    * @throws  Exception   (Not used)
    */
	public void updateModel(float[] features, int classValue, Instances m_Data) throws Exception 
	{
		 // Convert into an instance.
		 Instance instance = makeInstance(features, m_Data);

		 if (targetList == null) //should not be, since Model was created already.
			    System.err.println("Target list is not read right in WekaClassifiers.");
		 int index = targetList.indexOf(classValue);
		 instance.setClassValue(index);

		 // Add instance to training data.
		 m_Data.add(instance);
	}
	
	private Instance makeInstance(float features[], Instances m_Data) 
	{
		Instance instance = new Instance(features.length + 1);
		for (int i = 0; i < features.length; i++) 
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
