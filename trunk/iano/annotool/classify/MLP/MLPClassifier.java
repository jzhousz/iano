package annotool.classify.MLP;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import annotool.classify.SavableClassifier;

/** 							MLPClassifier
* 
* The MLPClassifier is a back propagation neural network. It is used to make, train
* and test a model used for predictions.
* 
* 
* KEY_PARA				General Parameter to be set
* 
* DEFAULT_MODEL_FILE 	Default place to save model
* 
* model					Model that is trained and test for use for predictions
* 
* pro_index				index value of probability array
* 
* hasProb				BioCat probability does not work, will keep code in for when it does
* 
* iterations			amount of iterations used by algorithm, used for Refining error in predictions
* 
* alpha					momentum of learning rate
* 
* eita					learning rate
* 
* gamma					learning rate
* 
* thvalue				training Parameters
*/

public class MLPClassifier  implements SavableClassifier, Serializable {

	private static final long serialVersionUID = 720985494174045458L;

	/* Parameters */
    //public final static String KEY_PARA = "General Parameter";
    public final static String DEFAULT_MODEL_FILE = "model_file";
  
	/* Internal model */
	
	private Object model = null;
	
	/* probability array index */
	private int pro_index = 0;
	
	
	/* if probability is possible */
	private boolean hasProb = true;
	
	/* Changeable settings */
	private int iterations = 100;
	private double alpha = 0.8, eita = 0.5, gamma = 0.5, thvalue = 0.0;
	
	/* nodes */
	private int outputNodes = 0;
	private int hiddenNodes = 20;
	
	/* If train only */
	private boolean trainedOnly = true;
	
	/* XML Cons */
	public static final String
	HIDDEN_NODES_XML = "Hidden Nodes",
	//NEURON_FIRE_XML = "Neuron Fire Threshold",
	UPPER_LIMIT_XML = "Uplimit of Iterations";
	
    //move below to BPNetTraining, which is the model.
	/* Conversation */
	HashMap<Integer, Integer> targetmap = null;
	/* transform coefficient to scale the data.  */
	float [] min = null;
	float [] max = null;
	
	
	/** 							classify
	* 
	* Used to make the model, train the model and test the model
	* 
	* @param trainingpatterns 	Information passed in form BioCat, used to train the model
	* 
	* @param trainingtargets	What the neural network will try to predict and will refine itself till it can 
	* 							match the trainingtargets
	* 
	* @param predictions		Array of predictions of possible outcomes
	* 
	* @param prob				probability array, hold probability of predictions being correct
	*/
	
	public void classify(float[][] trainingpatterns, int[] trainingtargets, float[][] testingpatterns, 
			int[] predictions, double[] prob) throws Exception 
	{
		
		int[] preHolder = null;
		
		/*
		for( int i = 0; i < trainingpatterns.length; i++)
		{
			testconv[i] = new float[trainingpatterns[i].length];
		}
		*/
	
		trainedOnly = false;
		
		/*Trains the model */
		model = trainingOnly(trainingpatterns, trainingtargets);
		
		
		/* Test the model */
		preHolder = classifyUsingModel(model, testingpatterns, prob );		
		
		for ( int i = 0; i < preHolder.length; i++)
		{
			predictions[i] = preHolder[i];
		}
		
		
	}

	/** 							doesSupportProbability
	* 
	* @return true because this classifier has Probability
	*/
	
	public boolean doesSupportProbability() {
		
		return hasProb;
	}

	/** 							trainingOnly
	* 
	* used to train a model and if a model is null it will create a model based on trainingpatterns
	* and trainingtargets
	* 
	* @param trainingpatterns 	Information passed in form BioCat, used to train the model
	* 
	* @param trainingtargets	What the neural network will try to predict and will refine itself till it can 
	* 					    	match the trainingtargets
	* 
	* @return Trainer			Returns a trained model
	* 
	* @exception				Not used as of now
	*/
	
	public Object trainingOnly(float[][] trainingpatterns, int[] trainingtargets)
			throws Exception {
		
		/* trainConver took the place of trainingtargets */
		BPNetTraining Trainer;
		
		min = new float[trainingpatterns[0].length]; 
		max = new float[trainingpatterns[0].length];
		
		for( int j = 0; j < trainingpatterns[0].length; j++)
		{
			min[j] = trainingpatterns[0][j];
			max[j] = trainingpatterns[0][j];
			
			for( int i = 0; i < trainingpatterns.length; i++ )
			{
				if( max[j] < trainingpatterns[i][j] )
				{
					max[j] = trainingpatterns[i][j];
				}
				else if( min[j] > trainingpatterns[i][j] )
				{
					min[j] = trainingpatterns[i][j];
				}
			}
			
			for( int i = 0; i < trainingpatterns.length; i++ )
			{
			    //if max[i] is the same as min[j] (the column is all the same), then they are all 0.99.
				if( max[j] == trainingpatterns[i][j] )
				{
					trainingpatterns[i][j] = (float) 0.99;
				}
				else if( min[j] ==trainingpatterns[i][j] )
				{
					trainingpatterns[i][j] = (float) 0.01;
				}
				else
				{
					trainingpatterns[i][j] = (trainingpatterns[i][j] - min[j])/( max[j] - min[j]);
					//limit the range to be 0.99 and 0.01
				    if (trainingpatterns[i][j] > (float) 0.99)
				      trainingpatterns[i][j] = (float) 0.99;
				    else if (trainingpatterns[i][j] < (float) 0.01)
				       trainingpatterns[i][j] = (float) 0.01;
				}
		
			}	
			
		}
		
		int[] trainConver = convertTargets( trainingtargets );
	
		Trainer = new BPNetTraining( trainingpatterns[0].length, hiddenNodes, outputNodes );
		Trainer.algorithm.iterations = iterations;
		Trainer.algorithm.thvalue = thvalue;
		Trainer.algorithm.alpha = alpha;
		Trainer.algorithm.eita = eita;
		Trainer.algorithm.gamma = gamma;

		Trainer.algorithm.sample = trainingpatterns;
		Trainer.algorithm.code = trainConver;
		Trainer.startTrain( trainingpatterns.length );
		
		if(trainedOnly)
		{
			model = Trainer;
		}
		
		return Trainer; 
	}

	/** 							getModel
	* 
	* @returns neural network model
	*/
	
	public Object getModel() {
		
		return model;
	}

	/** 							setModel
	* 
	* Sets class model to new model pass in
	* 
	* @param model neural network model
	*/
	
	public void setModel(Object model) throws Exception {
		model = this.model;

	}

	
	/** 							classifyUsingModel
	* 					 	with 1D testingPattern float array 
	* 
	* Test the model passed in and gives a prediction and probability of prediction being correct
	*    
	* @param model 				neural network model
	* 
	* @param testingPattern		A sub-set of testing Patterns
	* 
	* @param prob				probability array, hold probability of prediction being correct
	* 
	* @return 					index of prediction
	* 
	* @exception				model null
	*/
	
	public int classifyUsingModel(Object model , float[] testingPattern,
			double[] prob) throws Exception {
		
		if( model == null )
			throw new Exception("Model passed in is null");
		
		//transform the data
		for( int j = 0; j < testingPattern.length; j++)
		{
		    if (max[j] == min[j])  //a column that does not have valid coefficients
			    testingPattern[j] = 0.99f;
		    else
			    testingPattern[j] = (testingPattern[j] - min[j])/( max[j] - min[j]);

			//if (testingPattern[j] > 0.99)
			//    testingPattern[j] = 0.99f;
			//else if (testingPattern[j] < 0.01)
			//    testingPattern[j] = 0.01f;
		}
		
		BPNetTraining trained = (BPNetTraining) model;
		
		BPNetTesting Tester  = new BPNetTesting( trained.thenet );
		
		Tester.algorithm.test = testingPattern;
		
		return Tester.algorithm.testModel( prob,  pro_index );

	}

	/** 							classifyUsingModel
	* 					 	with 2D testingPattern float array 
	* 
	* Test the model passed in and gives a prediction and probability of prediction being correct by
	* break up the testingPatterns into sets and test each set
	*    
	* @param model 				neural network model
	* 
	* @param testingPattern		A Set of testing Patterns
	* 
	* @param prob				probability array, hold probability of prediction being correct
	* 
	* @return 					index of prediction
	* 
	* @exception				model null
	*/
	
	public int[] classifyUsingModel(Object model, float[][] testingPatterns,
			double[] prob) throws Exception 
	{
		
		pro_index = 0;
		
		if( model == null )
			throw new Exception("Model passed in is null");
		
		int[] rec = new int[testingPatterns.length];
		
		for( int i = 0; i < testingPatterns.length; i++ )
		{
			rec[i]  = ((Integer) targetmap.get(classifyUsingModel( model, testingPatterns[i],  prob  ))).intValue();
			
			pro_index++;
		}
		
		return rec;
	
	}

	/** 	saveModel
	* 
	* Saves the model to a file to be used for a late date, Deep save is used
	* Passed model is ignored. Instead, the internal model is saved.
	*    
	* @param trainedmodel 		neural network model
	* 
	* @param model_file_name	File name to save model
	* 
	* @throws Not used as of now
	*/
	
	public void saveModel(Object trainedmodel, String model_file_name) throws IOException {
	
	
		if( model == null )
		{
			throw new IOException( "Model in class is null");
		}
		else 
			trainedmodel = model;
		
		File file = new File(model_file_name);
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		out.writeObject(model);
		out.flush();
		out.writeObject(targetmap);
		out.flush();
		out.writeObject(min); //new float[trainingpatterns[0].length]; 
		out.flush();
		out.writeObject(max);
		out.close();
		
		
	}

	/** 							loadModel
	* 
	* Loads the model from Disk to be used, Deep load is needed
	* 
	* @param model_file_name	File name to save model
	* 
	* @throws Not used as of now
	* 
	* @return a neural network model
	*/
	
	@SuppressWarnings("unchecked")
	public Object loadModel(String model_file_name) throws IOException {
		
		try
		{
		
		File file = new File(model_file_name);
		ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
		model = input.readObject();
		targetmap = ((HashMap<Integer, Integer>) input.readObject());
		min = (float[]) input.readObject();
		max = (float[])input.readObject();
		input.close();

		} 
	    catch (ClassNotFoundException e1) 
	    {
	    	e1.printStackTrace();
	    }
		return model;
		    
	}

	/** 							setParameters
	* 
	* Used to set Parameters, if it look correct it calls nerualNetworkPara
	* 
	* @param para 	Holds all Set Parameters
	* 
	*/
	public void setParameters(HashMap<String, String> para) 
	{

		
		if(para != null )
		{
			if(para.containsKey(HIDDEN_NODES_XML));
			{
				hiddenNodes = Integer.parseInt((String) para.get(HIDDEN_NODES_XML));
			}
		
			if(para.containsKey(UPPER_LIMIT_XML)); //Iterations 
			{
				iterations = Integer.parseInt((String) para.get(UPPER_LIMIT_XML));
			}
		}
		
	}
	

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
		
		outputNodes = orig2new.size();
		System.out.println("Number of groups:"+ outputNodes);
		return convertedTargets;

   	
   	}
   	
   	
}

