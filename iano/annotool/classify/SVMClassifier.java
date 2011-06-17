package annotool.classify;


import libsvm.*;
import java.util.StringTokenizer;

/**

   Note: When probability of estimation is needed, this class assumes that the num of class less than 10 (can be changed by passing an argument to classify()).

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

public class SVMClassifier implements Classifier
{
    int dimension;
    int maxClassNum = 0;
    svm_parameter param = new svm_parameter();
    public final static String KEY_PARA = "General Parameter";
    public final static String DEFAULT_MODEL_FILE = "SVM_MODELFILE";
    svm_model trainedModel = null;
    
   //initialize # of samples and # of dimension
   public SVMClassifier(java.util.HashMap<String,String> parameters)
   {
	  if(parameters != null && parameters.containsKey(KEY_PARA))
          initSVMParameters(parameters.get(KEY_PARA));
 	  else
 		initSVMParameters(annotool.Annotator.DEFAULT_SVM);
   }

    public SVMClassifier(int dimension, String parameters)
   {
	 this.dimension = dimension;
     initSVMParameters(parameters);
   }

   public SVMClassifier(int dimension)
   {
     this.dimension = dimension;
     initSVMParameters(annotool.Annotator.DEFAULT_SVM);
   }

   //parameter setting follows the LibSVM convention.
   //See details on LibSVM webpage.
   protected void initSVMParameters(String parameters)
   {
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

		maxClassNum = getMaxNumOfClass(trainingtargets);// annotool.Annotator.maxClass;
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
			    //System.out.println("prediction: "+ predictions[i]);
			    
			    //To be fixed: 
			    //prob_estimates may be ordered based on the order the LibSVM sees the label. 
			    //I.e.: prob_estimates[2] may not correspond to label 2.
			    //Note that maxClassNum may not be fully used by libSVM depends on the column
			    /*
			    for (int index = 0; index < maxClassNum; index++)
			    {
			    	if (predictions[i] == ???labels[index] )
			    	{ 	
			           probesti[i] = prob_estimates[index];
			           break;
			    	}
			    }*/
			    //System.out.println("prediction: "+ predictions[i] + "  prob_estimates[0]:"+ prob_estimates[0]+"   prob_estimates[1]:"+prob_estimates[1]);

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
    
    /** 6/14/2011 add 5 methods to save model/load model and classify based on model
    *  
    *  Rewrite: 6/17/2011
    *  Interface methods to be added to annotool.classify.Classifier:  
    *    --  Use object serialization for uniform interface. It may be just a String.
    *    --  Assumption: The returned model object needs to be serializable.  
    *  public void trainingOnly(float[][] trainingpatterns, int[] trainingtargets);
    *  public Object getModel();  //get and/or save
    *  public void setModel(Object); //load
    *  public int classifyUsingModel(Object model, float[] testingPattern) throws Exception
    *  public int[] classifyUsingModel(Object model, float[][] testingPatterns) throws Exception
    *  
    *  Example code using uniform interface:
    *   SVMClassifier c = new SVMClassifier(parameters);
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
    
    //training only and sets the internal model
    public void trainingOnly(float[][] trainingpatterns, int[] trainingtargets)
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
	    }
        
    }
    
    /**
     * Returns the model, for persistence if needed. 
     * The return value may be the content of the internal model OR just
     *   a filename where the actual model is saved, depending on the actual classifier.
     * In the case of LibSVM, it returns the filename and the saving to the file is done inside. 
     * This method pairs up with a setter.
     */
    public Object getModel(String model_file_name)
    {
    	if(trainedModel == null)
    	{
    		System.err.println("SVM Model is not yet trained. It cann't be saved.");
    		return null;
    	}
    	
    	try
    	{
    	 System.out.println("Saving SVM model to "+ model_file_name);	
    	 svm.svm_save_model(model_file_name,trainedModel);
    	}catch(java.io.IOException e)
    	{
    		System.err.println("Problem in saving the trained model of SVM");
    	}
    	
    	return model_file_name;
    }

    //use default model file to save the model.
    public Object getModel()
    {
    	return getModel(DEFAULT_MODEL_FILE);
    }
    
    //set the instance model variable based on input 
    public void setModel(Object savedModel)
    {
    	String model = (String) savedModel;
    	try
    	{
       	    System.out.println("Loading SVM model from "+ model);	
    		trainedModel = svm.svm_load_model(model);
    	}catch(java.io.IOException e)
    	{
    	   System.err.println("Problem in loading the trained model of SVM");
    	}
    }
    
    //use default model file to load the model 
    public void setModel()
    {
    	setModel(DEFAULT_MODEL_FILE);
    }
    
    /** Classify one testing pattern, based on the model parameter or the instance variable (if the parameter is null)
      If input model is not null, use it. Otherwise, use the instance variable, 
      which may be set by setModel() or a previous call of the method. So this method can be called
      in a loop to classify multiple testing patterns (see the overloaded method for example).
     * 
     * @param model: null (if internal model is set), String (persisted), or svm_model (internal)
     * @param testingPattern
     * @return prediction (int)
     * @throws Exception
     */
    public int classifyUsingModel(Object model, float[] testingPattern) throws Exception
    {
       	if (model != null) //model may be null, but only when the internal model is already set.
       	{
       		if (model instanceof String) //load from persisted trained model
       		   setModel(model);
       		else if (model instanceof svm_model) //pass in an internal model
       			trainedModel = (svm_model) model;
       		}
    	else  if(trainedModel == null)
    	{  //when model is null && there is no instance variable that contains a valid model
    		System.err.println("Err: must pass in a model.");
    		throw new Exception("Err: must pass in a model.");
    	}   	
    	
       	//maxClassNum is needed by prob estimation, yet it is only available from training samples. So if it is not set, assume a large number.
		double[] prob_estimates;
		if (maxClassNum == 0) //not known
		  prob_estimates = new double[100];
		else 
		  prob_estimates = new double[maxClassNum];

	    int dimension = testingPattern.length;
	    svm_node[] testx = new svm_node[dimension];
	    for(int j=0; j<dimension; j++)
	    {
	      testx[j] = new svm_node();
	      testx[j].index = j+1;
	      testx[j].value = (double) testingPattern[j];
	    }
	    int prediction =  (int) svm.svm_predict_probability(trainedModel,testx,prob_estimates);
        System.out.println(prediction+" ");
       	return prediction;
       	
       	
    }
    
    /** The method classify many testing patterns given a model. 
        it calls the version that work with one testing pattern.
        The first parameter is essencially a String (filename) in the case of SVM.
     */     
    public int[] classifyUsingModel(Object model, float[][] testingPatterns) throws Exception
    {
        //check the type of model
    	if (model != null) //model may be null, but only when the internal model is already set.
       	{
       		if (model instanceof String) //load from persisted trained model
       		   setModel(model);
       		else if (model instanceof svm_model) //pass in an internal model
       			trainedModel = (svm_model) model;
       		}
    	else  if(trainedModel == null)
    	{  //when model is null && there is no instance variable that contains a valid model
    		System.err.println("Err: must pass in a model.");
    		throw new Exception("Err: must pass in a model.");
    	}   	

    	
    	//allocate predictions
    	int[] predictions = new int[testingPatterns.length];
     	for(int i = 0; i < testingPatterns.length; i++)
    	  predictions[i] = classifyUsingModel(null, testingPatterns[i]);
    	
    	return predictions;
    			
    }
       
}