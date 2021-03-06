package annotool.classify;


import libsvm.*;

import java.util.StringTokenizer;

/**

   The following LibSVM options are from LibSVM website http://www.csie.ntu.edu.tw/~cjlin/libsvm/.

Examples of options: -s 0 -c 10 -t 1 -g 1 -r 1 -d 3
Classify a binary data with polynomial kernel (u'v+1)^3 and C = 10

options:
-s svm_type : set type of SVM (default 0)
	0 -- C-SVC
	1 -- nu-SVC
	2 -- one-class SVM
	3 -- epsilon-SVR
	4 -- nu-SVR
-t kernel_type : set type of kernel function (default 2)
	0 -- linear: u'*v
	1 -- polynomial: (gamma*u'*v + coef0)^degree
	2 -- radial basis function: exp(-gamma*|u-v|^2)
	3 -- sigmoid: tanh(gamma*u'*v + coef0)
-d degree : set degree in kernel function (default 3)
-g gamma : set gamma in kernel function (default 1/k)
-r coef0 : set coef0 in kernel function (default 0)
-c cost : set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default 1)
-n nu : set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)
-p epsilon : set the epsilon in loss function of epsilon-SVR (default 0.1)
-m cachesize : set cache memory size in MB (default 100)
-e epsilon : set tolerance of termination criterion (default 0.001)
-h shrinking: whether to use the shrinking heuristics, 0 or 1 (default 1)
-b probability_estimates: whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)
-wi weight: set the parameter C of class i to weight*C, for C-SVC (default 1)

The k in the -g option means the number of attributes in the input data.

option -v randomly splits the data into n parts and calculates cross
validation accuracy/mean squared error on them.

**/

public class SVMClassifier implements SavableClassifier
{
    int dimension;
    int maxClassNum = 0;
    svm_parameter param = new svm_parameter();
    //public final static String KEY_PARA = "General Parameter";
    public final static String DEFAULT_MODEL_FILE = "SVM_MODELFILE";
    svm_model trainedModel = null;
    boolean supportProbability = false;
    
	/* XML Cons */
	public static final String
	SVM_TYPE="Svm Type",
	KERNEL_TYPE = "Kernel Type",
    EPS_TER_CRI = "Epsilon Termination Criterion",
	GEN_PARA = "General Parameter";
	
   /**
   * Default constructor
   */
   public SVMClassifier() {}    
    
   /**
    * Sets algorithm parameters from para
    * 
    * @param   para  Each element of para holds a parameter's name
    *                for its key and a its value is that of the parameter's.
    *                The parameters should be the same as those in the 
    *                algorithms.xml file.
    */
   public void setParameters(java.util.HashMap<String, String> para)
   {
	   
	   String SvmParas = "";
	   if(para != null )
		{
		   if(para.containsKey(SVM_TYPE))
		   {
			   
			   if(((String) para.get(SVM_TYPE)).equalsIgnoreCase("C-SVC"))
				   SvmParas = "-s " + "0";
			   else if(((String) para.get(SVM_TYPE)).equalsIgnoreCase("nu-SVC"))
				   SvmParas = "-s " + "1";
			   else if(((String) para.get(SVM_TYPE)).equalsIgnoreCase("one-class SVM"))
				   SvmParas = "-s " + "2";
			   else if(((String) para.get(SVM_TYPE)).equalsIgnoreCase("epsilon-SVR"))
				   SvmParas = "-s " + "3";
			   else
				   SvmParas = "-s " + "4";
		   }
		   
		   if(para.containsKey(KERNEL_TYPE))
		   {
			   if(((String) para.get(KERNEL_TYPE)).equalsIgnoreCase("linear"))
				   SvmParas += " -t " + "0";
			   else if(((String) para.get(KERNEL_TYPE)).equalsIgnoreCase("polynomial"))
				   SvmParas += " -t " + "1";
			   else if(((String) para.get(KERNEL_TYPE)).equalsIgnoreCase("radial basis function"))
				   SvmParas += " -t " + "2";
			   else
				   SvmParas += " -t " + "3";
			   
		   }
		   
		   if(para.containsKey(EPS_TER_CRI))
		   {
			   SvmParas += " -e " + ((String) para.get(EPS_TER_CRI));
		   }
		   
		   if(para.containsKey(GEN_PARA))
		   {
			   SvmParas = ((String) para.get(GEN_PARA));
		   }
		   
		   if(!SvmParas.equals(""))
		   {
			  System.out.println(SvmParas);
			   initSVMParameters(SvmParas);
		   }
		}
	   else
	 	initSVMParameters(annotool.Annotator.DEFAULT_SVM);
   }
 
   /**
    * Constructor that checks for relevant parameters and calls SVMClassifier 
    * with either parameters or if there are none, the default parameter.
    * 
    * @param   parameters  Each element of parameter holds a parameter's name
    *                      for its key and a its value is that of the parameter's.
    *                      The parameters should be the same as those in the 
    *                      algorithms.xml file.
    */
   public SVMClassifier(java.util.HashMap<String,String> parameters)
   {
	  
	   setParameters(parameters);
	   
	   /*
	   if(parameters != null && parameters.containsKey(KEY_PARA))
          initSVMParameters(parameters.get(KEY_PARA));
 	  else
 		initSVMParameters(annotool.Annotator.DEFAULT_SVM);
	    */
   
   }

   /**
   * Constructor that copies dimension to an instance variable and calls initSVMParameters 
   * with the parameters passed in.
   * 
   * @param   dimension   The dimension of data (number of features)
   * @param   parameters  LiBSVM's parameter string (the general parameter e.g. "-s 0")
   */
   public SVMClassifier(int dimension, String parameters)
   {
	  this.dimension = dimension;
	  initSVMParameters(parameters);
   }

   /**
   * Constructor that copies dimension to an instance variable and calls initSVMParameters 
   * with the default parameters.
   * 
   * @param   dimension   The dimension of data (number of features)
   */
   public SVMClassifier(int dimension)
   {
      this.dimension = dimension;
      initSVMParameters(annotool.Annotator.DEFAULT_SVM);
   }

   //parameter setting follows the LibSVM convention.
   //See details on LibSVM webpage.
   protected void initSVMParameters(String parameters)
   {
	   System.out.println( parameters ); 
	   
		// default values
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		param.gamma = 0;
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 40;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 0;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];

		//parse options on kernel type etc
		StringTokenizer st = new StringTokenizer(parameters);
		String[] argv = new String[st.countTokens()];
	    for(int i=0;i<argv.length;i++)
			argv[i] = st.nextToken();

		for(int i=0;i<argv.length;i++)
		{
				if(argv[i].charAt(0) != '-') break;
				if(++i>=argv.length)
				{
					System.err.print("unknown option\n");
						break;
				}
				switch(argv[i-1].charAt(1))
				{
					case 's':
						param.svm_type = atoi(argv[i]);
						break;
					case 't':
						param.kernel_type = atoi(argv[i]);
						break;
					case 'd':
						param.degree = atoi(argv[i]);
						break;
					case 'g':
						param.gamma = atof(argv[i]);
						break;
					case 'r':
						param.coef0 = atof(argv[i]);
						break;
					case 'n':
						param.nu = atof(argv[i]);
						break;
					case 'm':
						param.cache_size = atof(argv[i]);
						break;
					case 'c':
						param.C = atof(argv[i]);
						break;
					case 'e':
						param.eps = atof(argv[i]);
						break;
					case 'p':
						param.p = atof(argv[i]);
						break;
					case 'h':
						param.shrinking = atoi(argv[i]);
						break;
					case 'b':
						param.probability = atoi(argv[i]);
						break;
					case 'w':
						++param.nr_weight;
						{
							int[] old = param.weight_label;
							param.weight_label = new int[param.nr_weight];
							System.arraycopy(old,0,param.weight_label,0,param.nr_weight-1);
						}

						{
							double[] old = param.weight;
							param.weight = new double[param.nr_weight];
							System.arraycopy(old,0,param.weight,0,param.nr_weight-1);
						}

						param.weight_label[param.nr_weight-1] = atoi(argv[i-1].substring(2));
						param.weight[param.nr_weight-1] = atof(argv[i]);
						break;
					default:
						System.err.print("unknown option\n");
				}//end of switch for a parameter
        }//end of parsing parameters
   }//end of init parameters

   
   /**
    * Classifies the patterns using the input parameters.
    * 
    * @param   trainingpatterns  Pattern data to train the algorithm
    * @param   trainingtargets   Targets for the training pattern
    * @param   testingpatterns   Pattern data to be classified
    * @param   predictions       Storage for the resulting prediction
    * @param   prob              Storage for probability result
    */
   //This is an interface method to LibSVM package.
   //It handles training and testing in one method.
   public void classify(float[][] trainingpatterns, int[] trainingtargets, float[][] testingpatterns, int[] predictions, double[] probesti)
   {
	    int traininglength = trainingpatterns.length;
	    int testinglength = testingpatterns.length;
	    dimension = trainingpatterns[0].length;
        svm_problem prob = new svm_problem();
		prob.l = traininglength;
		prob.y = new double[prob.l];

		maxClassNum = getMaxNumOfClass(trainingtargets);
		System.out.println("maxClassNum:"+ maxClassNum);
		double[] prob_estimates = new double[maxClassNum];

        if(param.svm_type == svm_parameter.EPSILON_SVR ||
			param.svm_type == svm_parameter.NU_SVR)
		{
			System.out.println("svm type is set to regression. It should be classification.");
			return;
	    }
	    else
	    {
			if(param.gamma == 0) param.gamma = 0.5;
			prob.x = new svm_node[prob.l][dimension];

			//set up the data (x: svm_nodes; y: targets)
			for(int i=0;i<prob.l;i++)
			{
				for (int j =0; j<dimension; j++)
				{
				  prob.x[i][j] = new svm_node();
				  prob.x[i][j].index = j+1;   //what is this for? dimension index?
				  prob.x[i][j].value = (double) trainingpatterns[i][j];
			    }
				prob.y[i] = trainingtargets[i];
			}

			// build model
			svm_model model = svm.svm_train(prob, param);
			//set the instance variable for saving
			trainedModel = model;

			//classify
			for(int i=0; i< testinglength; i++)
			{
			    svm_node[] testx = new svm_node[dimension];
			    for(int j=0; j<dimension; j++)
			    {
			      testx[j] = new svm_node();
			      testx[j].index = j+1;
			      testx[j].value = (double) testingpatterns[i][j];
			    }
			    //typecast to int for category labels.
			    //predictions[i] =  (int) svm.svm_predict(model, testx);
			    predictions[i] =  (int) svm.svm_predict_probability(model,testx,prob_estimates);
			    
			    //Check if the model supports probability  (-b 1 is not set or the model cann't be used to estimate probability) 
				if(svm.svm_check_probability_model(model)==0)
					supportProbability = false;
				else
				{
				    supportProbability = true;		
				    double max = 0; 
			        for (int index = 0; index < maxClassNum; index++)
			       {
			         System.out.println("prob["+index+"]="+prob_estimates[index]);
			         
			         if (max <  prob_estimates[index])
			             max = prob_estimates[index];
			       }
			       probesti[i] = max;
			       System.out.println("prediction: "+ predictions[i] + "  probability:"+ probesti[i]);
				}
			     	
			}
        }
   }


   	private static int atoi(String s)
   	{
   		return Integer.parseInt(s);
   	}

   	private static double atof(String s)
	{
		return Double.valueOf(s).doubleValue();
	}


    private int getMaxNumOfClass(int[] targets)
    {
	   java.util.HashSet<Integer> targetset = new java.util.HashSet<Integer>();
	   for (int i=0; i < targets.length; i++)
			if(!targetset.contains(new Integer(targets[i])))
				targetset.add(new Integer(targets[i]));
	   
	   return targetset.size();
	   
    }
  
    /* 6/14/2011 add 5 methods to save model/load model and classify based on model
    *  
    *  Rewrite: 6/17/2011
    *  Interface methods to be added to annotool.classify.Classifier:  
    *    --  Use object serialization for uniform interface. It may be just a String.
    *    --  Assumption: The returned model object needs to be serializable.  
    *  public void trainingOnly(float[][] trainingpatterns, int[] trainingtargets);
    *  public Object getModel(String);  //get and/or save
    *  public void setModel(Object); //load
    *  public int classifyUsingModel(Object model, float[] testingPattern) throws Exception
    *  public int[] classifyUsingModel(Object model, float[][] testingPatterns) throws Exception
    *  
    *  Example code using uniform interface:
    *   SavableClassifier c = new SVMClassifier(parameters);
    *   c.trainingOnly(selectedTrainingFeatures, trainingtargets);
    *   //save to file
    *   Object model =  c.getModel();
    *   //The chain saver can use Object Serialization to save this model.
    *   //load and use (in a different place)
    *   //model would be read using Object Serialization
    *   try{
    *     int[] predictions = c.classifyUsingModel(model, selectedTestingFeatures);
    *   }catch(Exception e)
    *   { e.printStackTrace();}
    *     
    */
  
    /**
     * Trains and returns an internal model using a training set.
     * 
     * @param   trainingpatterns  Pattern data to train the algorithm
     * @param   trainingtargets   Targets for the training pattern
     * @return                    Model created by the classifier
     */
    public Object trainingOnly(float[][] trainingpatterns, int[] trainingtargets)
    {
    	
	    int traininglength = trainingpatterns.length;
	    dimension = trainingpatterns[0].length;
        svm_problem prob = new svm_problem();
		prob.l = traininglength;
		prob.y = new double[prob.l];

        if(param.svm_type == svm_parameter.EPSILON_SVR ||
			param.svm_type == svm_parameter.NU_SVR)
		{
			System.out.println("svm type is set to regression. It should be classification.");
			return null;
	    }
	    else
	    {
			if(param.gamma == 0) param.gamma = 0.5;
			prob.x = new svm_node[prob.l][dimension];

			//set up the data (x: svm_nodes; y: targets)
			for(int i=0;i<prob.l;i++)
			{
				for (int j =0; j<dimension; j++)
				{
				  prob.x[i][j] = new svm_node();
				  prob.x[i][j].index = j+1;   //what is this for? dimension index?
				  prob.x[i][j].value = (double) trainingpatterns[i][j];
			    }
				prob.y[i] = trainingtargets[i];
			}

			// build model and save the instance variable
			trainedModel = svm.svm_train(prob, param);
			return trainedModel;
	    }
        
    }
    
    /**
     * Saves a specified model to a specified file
     * 
     * @param   trainedModel         Trained model that is to be saved
     * @param   model_file_name      Name of the file to be saved to
     * @throws  java.io.IOException  Exception thrown if file cannot be found
     */
    public void saveModel(Object trainedModel, String model_file_name) throws java.io.IOException
    {
    	
    	System.out.println("Saving SVM model to "+ model_file_name);	
    	   
    	java.io.ObjectOutputStream filestream = new java.io.ObjectOutputStream(new java.io.FileOutputStream(model_file_name));
		filestream.writeObject(trainedModel);
		filestream.close();

        /*    	
    	try
    	{
    	 svm.svm_save_model(model_file_name,(svm_model) trainedModel);
    	}catch(java.io.IOException e)
    	{
    		System.err.println("Problem in saving the trained model of SVM");
    		throw new java.io.IOException("Problem in saving the trained model of SVM");
    	}*/
    }

    /**
     * Loads a previously saved model back into the classifier.
     * 
     * @param   model_file_name      Name of the file to be loaded
     * @return                       Model that was loaded
     * @throws  java.io.IOException  Exception thrown if file cannot be found
     */
    public Object loadModel(String model_file_name) throws java.io.IOException
    {
    	System.out.println("Loading SVM model from "+ model_file_name);		
    	  
    	svm_model model = null;
    	java.io.ObjectInputStream filestream = new java.io.ObjectInputStream(new java.io.FileInputStream(model_file_name));
    	try
    	{
    		model = (svm_model) filestream.readObject();
    	}catch(ClassNotFoundException ce)
    	{
    		System.err.println("Class Not Found in Loading SVM model");
    	}
    	filestream.close();
    	return model;
     /*
	  try
	  {
		trainedModel = svm.svm_load_model(model_file_name);
	   }catch(java.io.IOException e)
	  {
	   System.err.println("Problem in loading the trained model of SVM");
	   throw new java.io.IOException("Problem in loading the trained model of SVM");
	  }
	  return  trainedModel;
	  */
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
     * @throws  Exception  Exception thrown if model is an invalid file type
     */
    public void setModel(Object savedModel) throws Exception
    {
    	if(savedModel instanceof svm_model)
    		trainedModel = (svm_model)savedModel;
    	else
    	{
    		System.err.print("Not a valid model type for svm");
    		throw new Exception("Not a valid model type for svm");
    	}
    	
    }
    
    
    /** Classify one testing pattern, based on the model parameter or the instance variable (if the parameter is null)
    * If input model is not null, use it. Otherwise, use the instance variable, 
    * which may be set by setModel() or a previous call of the method. So this method can be called
    * in a loop to classify multiple testing patterns (see the overloaded method for example).
    * 
    * @param   model            null (if internal model is set), String (persisted), or svm_model (internal)
    * @param   testingPattern   Pattern data to be classified
    * @param   prob             Storage for probability result
    * @return                   The prediction result
    * @throws  Exception        Exception thrown if no model is passed in
    */
    public int classifyUsingModel(Object model, float[] testingPattern, double[] prob) throws Exception
    {
       	if (model != null) //model may be null, but only when the internal model is already set.
       	{
       		if (model instanceof String) //load from persisted trained model
       		   loadModel((String)model);
       		else if (model instanceof svm_model) //pass in an internal model
       		   setModel((svm_model) model);
       		}
    	else  if(trainedModel == null)
    	{  //when model is null && there is no instance variable that contains a valid model
    		System.err.println("Err: must pass in a model.");
    		throw new Exception("Err: must pass in a model.");
    	}   	
    	
 		
		maxClassNum = svm.svm_get_nr_class(trainedModel);
	    double[] prob_estimates = new double[maxClassNum];

	    int dimension = testingPattern.length;
	    svm_node[] testx = new svm_node[dimension];
	    for(int j=0; j<dimension; j++)
	    {
	      testx[j] = new svm_node();
	      testx[j].index = j+1;
	      testx[j].value = (double) testingPattern[j];
	    }
	    int prediction =  (int) svm.svm_predict_probability(trainedModel,testx,prob_estimates);
	    
	    //set the probability
		if(svm.svm_check_probability_model(trainedModel)==0)
			supportProbability = false;
		else
		{
		    supportProbability = true;		
		    double max = 0; 
	        for (int index = 0; index < maxClassNum; index++)
	       {
	         System.out.println("prob["+index+"]="+prob_estimates[index]);
	         if (max <  prob_estimates[index])
	             max = prob_estimates[index];
	       }
	       prob[0] = max;
		}
        //System.out.println(prediction+" ");
       	return prediction;
       	
       	
    }
    
    /** The method classify many testing patterns given a model. 
    *   it calls the version that work with one testing pattern.
    *   The first parameter is essencially a String (filename) in the case of SVM.   
    * 
    * @param   model             Model to be used by the classifier
    * @param   testingPatterns   Pattern data to be classified
    * @param   prob              Storage for probability result 
    * @return                    Array of prediction results
    * @throws  Exception         Exception thrown if no model is passed in
    */
    public int[] classifyUsingModel(Object model, float[][] testingPatterns, double[] probest) throws Exception
    {
        //check the type of model
    	if (model != null) //model may be null, but only when the internal model is already set.
       	{
       		if (model instanceof String) //load from persisted trained model
       		   loadModel((String) model);
       		else if (model instanceof svm_model) //pass in an internal model
       			setModel((svm_model) model);
       		}
    	else  if(trainedModel == null)
    	{  //when model is null && there is no instance variable that contains a valid model
    		System.err.println("Err: must pass in a model.");
    		throw new Exception("Err: must pass in a model.");
    	}   	

    	
    	//allocate predictions
    	int[] predictions = new int[testingPatterns.length];
    	double[] probforone = new double[1];
     	for(int i = 0; i < testingPatterns.length; i++)
     	{
    	  predictions[i] = classifyUsingModel(null, testingPatterns[i], probforone);
    	  probest[i] = probforone[0];
     	}
    	
    	return predictions;
    			
    }
    
    /**
     * Returns whether or not the algorithm uses probability estimations.
     * 
     * @return  <code>True</code> if the algorithm uses probability  estimations, 
     *          <code>False</code> if not. Default value is <code>False</code>
     */
    //default false. May be modified in classify if working with probability estimation
    public boolean doesSupportProbability()
    {
    	return supportProbability;
    }
       
}