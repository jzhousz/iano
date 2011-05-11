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
    svm_parameter param = new svm_parameter();
    public final static String KEY_PARA = "General Parameter";
    
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
   public void classify(float[][] trainingpatterns, int[] trainingtargets, float[][] testingpatterns, int[] predictions, double[] probesti)
   {
	    int traininglength = trainingpatterns.length;
	    int testinglength = testingpatterns.length;
	    dimension = trainingpatterns[0].length;
        svm_problem prob = new svm_problem();
		prob.l = traininglength;
		prob.y = new double[prob.l];

		int maxClassNum = getMaxNumOfClass(trainingtargets);// annotool.Annotator.maxClass;
		
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
			    //typecase to int for category labels.
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
}