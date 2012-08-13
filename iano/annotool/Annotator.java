package annotool;

import javax.swing.SwingUtilities;
import java.util.*;

import annotool.gui.AnnOutputPanel;
import annotool.gui.model.Extractor;
import annotool.io.*;
import annotool.extract.*;
import annotool.select.*;
import annotool.classify.*;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.*;

/********************************************************************************************

Image Annotation Tool          Jie Zhou   

This class is the original entry point of the annotation algorithms. 
It can be called from GUI or start from command line.
Command line Usage:

java -Djava.library.path="../mRMRFeatureselector" annotool.Annotator

Properties can be set to change the default parameters such as image directory and extensions. 
  
A List of Todos And Thoughts (May 2011):
-x-  algorithm parameters will be passed in separately as a Hashmap of String (instead of property)
-x-  Save project (model) (provide a better GUI with algorithm details),Save results. 
      Open project. (project explorer at the left has details of projects).
-x - read from a property file such as "./.annotool_properties". (take care by GUI now)
-x- ROI can save model after training to save time.(Now training/annotation can be separated.)
-x- GUI: Multiple tabs for result visualization; project detail; image detail.
-x- Manual mode (current);  Auto mode (comparison, by Aleksey L, Santosh L)
-x- CV modes are not calling the ..Given...() methods. To be checked later.
-x- an option to provide probability output of some classifiers

-- Load examples with suggested models
-- Plug-and-play by passing in full class names of the algorithms.
-- provide ranked output of the annotation result for entire image (based on the probability output).
-- Principal component feature extraction: transform matrix based on training images needs to be saved and then applied to testing images.  
-- Check out LibLinear for fast training.
-- The option of "all" channels was done in Bala-Sift module. To be incorporated.
-- rethink GUI: swt, using Eclipse professional style: 
-- GUI: Project Explorer and Image Explorer, when clicked, show in the middle? 
-- Save project (model) (provide a better GUI with algorithm setup details), 
-- Open project. (project explorer at the left has details of projects).

Note: 08/11: algorithm loading are now dynamic. The original mapping is obselete and may fail to work.

 ************************************************************************************************************/
public class Annotator 
{
    /*
     *  a list of default application properties
     */
    //directory for training or cross-validation images
    public final static String DEFAULT_DIR = "k150_train/"; //"stage4_6/" for embryos
    //image extension for training or cross-validation images
    public final static String DEFAULT_EXT = ".jpg";  //".png";
    //target file name for training or cross-validation images
    public final static String DEFAULT_TARGET = "k150_train/target.txt";  //"target4_6_1.txt";
    //directory for testing images
    public final static String DEFAULT_TESTDIR = "k150_test/"; //"stage4_6/" for embryos
    //image extension for testing images
    public final static String DEFAULT_TESTEXT = ".jpg";  //".png";
    //target file name for testing images
    public final static String DEFAULT_TESTTARGET = "k150_test/target.txt";  //"target4_6_1.txt";
    //feature extractor
    public final static String DEFAULT_EXTRACTOR = "HAAR";
    //feature selector
    public final static String DEFAULT_SELECTOR = "None"; // "MRMR" on 081007, PHC
    //feature classifier
    public final static String DEFAULT_CLASSIFIER = "SVM";
    //mode of the annotator.
    public final static String DEFAULT_OUTPUT = "TT";
    //Choice of modes: TT: Training/Testing; CV: cross-validation; ROI: Region-Of-Interest; TO: Train Only; AN: Annotate
    public final static String[] OUTPUT_CHOICES = {"TT", "CV", "ROI"};//TODO: these are only used for old codes
    public final static String TT = "TT"; //Training Testing
    public final static String CV = "CV"; //Cross Validation
    public final static String TO = "TO"; //Training Only
    public final static String AN = "AN"; //Annotation
    public final static String ROI = "ROI"; //Region of Interest
    
    //number of folders for cross-validation. Can be Leave-One-Out (LOO).
    public final static String DEFAULT_FOLD = "LOO";
    //image channel used by the algorithm. Such as r, g, b. (all to be added)
    public final static String DEFAULT_CHANNEL = "g";
    //a flag that controls disply of such debugging info such as feature image
    public final static String DEFAULT_DEBUG = "false";
    //a flag that controls the  output of results to a file
    public final static String DEFAULT_FILE = "false";  //file output
    //a flag that controls if cross-validation will shuffle the images
    public final static String DEFAULT_SHUFFLE = "false";  //for cross validation
    //algorithm parameters (for mRMR feature selector, SVM classifier, wavelet feature extractor).
    //Will be bundled as a String and pass into method arguments of individual algorithms.
    public final static String DEFAULT_FEATURENUM = "8";  //for feature selector
    public final static String DEFAULT_MRMRTYPE = "mRMR-MIQ"; //for mRMR
    public final static String DEFAULT_DISCRETE = "true";  //for mRMR
    public final static String DEFAULT_THRESHOLD = "0"; //for mRMR
    public final static String DEFAULT_SVM = "-t 0"; // for SVM
    public final static String DEFAULT_WAVLEVEL = "1";  //for wavelet extractor
    
    /*
     * The above properties can be changed at command line using name/value pair:
     *    java -Dimgext=.png  classname
     */
    public static String dir = System.getProperty("imgdir", DEFAULT_DIR);
    public static String ext = System.getProperty("imgext", DEFAULT_EXT);
    public static String targetFile = System.getProperty("target", DEFAULT_TARGET);
    public static String testdir = System.getProperty("testimgdir", DEFAULT_TESTDIR);
    public static String testext = System.getProperty("testimgext", DEFAULT_TESTEXT);
    public static String testtargetFile = System.getProperty("testtarget", DEFAULT_TESTTARGET);
    public static String featureExtractor = System.getProperty("extractor", DEFAULT_EXTRACTOR);
    public static String featureSelector = System.getProperty("selector", DEFAULT_SELECTOR);
    public static String classifierChoice = System.getProperty("classifier", DEFAULT_CLASSIFIER);
    public static String fold = System.getProperty("fold", DEFAULT_FOLD);
    public static String output = System.getProperty("output", DEFAULT_OUTPUT);
    public static String shuffleFlag = System.getProperty("shuffleflag", DEFAULT_SHUFFLE);
    public static String channel = System.getProperty("channel", DEFAULT_CHANNEL);
    public static String debugFlag = System.getProperty("debug", DEFAULT_DEBUG);
    public static String fileFlag = System.getProperty("fileflag", DEFAULT_FILE);
    public static String featureNum = System.getProperty("numoffeature", DEFAULT_FEATURENUM);
    public static String discreteFlag = System.getProperty("discreteflag", DEFAULT_DISCRETE);
    public static String threshold = System.getProperty("threshold", DEFAULT_THRESHOLD);
    public static String svmpara = System.getProperty("svmpara", DEFAULT_SVM);
    public static String waveletLevel = System.getProperty("waveletlevel", DEFAULT_WAVLEVEL);
    //Other variables
    //public static int maxClass = 10; //used by class SVMClassifier,
    java.util.ArrayList<String> annotationLabels = null; //set after reading targets, used by GUI
    
    protected java.io.Writer outputfile = null;  //will get file name from user;
    //Needed by GUI-based tool.
    protected Thread thread; //the running thread for background work.
    protected boolean isRunningFlag;
    protected javax.swing.JProgressBar bar = null;
    protected javax.swing.JButton goButton = null;
    protected javax.swing.JButton cancelButton = null;
    protected AnnOutputPanel outputPanel = null;

    //default constructor for command line
    public Annotator() {
    }
    /*******************************************************************************************
     *  The following public/protected methods can be called by entrance methods in this class,
     *  or other modules that mix and match different algorithms (for auto comparison).
     *****************************************************************************************/
    /*
     * return the target matrix
     * resArr: should have memory allocated in caller (2 int).
     *     resArry[0]: number of annotations (targets); resArry[1]: max class in all columns of targets
     * Other: set the annotationLabels via argument (if input is null, it won't be set).
     *  Deprecated in May 2012. Now they are in DataInput
     */
    public int[][] readTargets(DataInput problem, String filename, int[] resArr, java.util.ArrayList<String> annotationLabels) {
        int numOfAnno = 0;
        int maxClassAllTargets = 0;
        int[][] targets = null;
        try {
            int length = problem.getLength();
            LabelReader labelReader = new LabelReader(length, annotationLabels);

            targets = labelReader.getTargets(filename, problem.getChildren());
            maxClassAllTargets = labelReader.getNumOfClasses();
            //annotationLabels = labelReader.getAnnotations();
            
            numOfAnno = labelReader.getNumOfAnnotations();
            
            if (debugFlag.equals("true")) 
            {
              java.util.HashMap<String, String> classnames = labelReader.getClassnames();
              for(int i=0; i<classnames.size(); i++)
              {
            	for (Map.Entry<String, String> e : classnames.entrySet())
            	    System.out.println(e.getKey() + ": " + e.getValue());
              }

              for (int i = 0; i < numOfAnno; i++) {
                    for (int j = 0; j < length; j++) {
                        System.out.print(targets[i][j] + " ");
                    }
              }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        resArr[0] = numOfAnno;
        resArr[1] = maxClassAllTargets;
        return targets;
    }
    
    /* Overloaded version - also takes as argument classNames
     * 
     * return the target matrix
     * resArr: should have memory allocated in caller (2 int).
     *     resArry[0]: number of annotations (targets); resArry[1]: max class in all columns of targets
     * Other: set the annotationLabels via argument (if input is null, it won't be set). Same with classNames
     * 
     * Deprecated in May 2012. Now they are in DataInput
     */
    public int[][] readTargets(DataInput problem, String filename, int[] resArr, ArrayList<String> annotationLabels, HashMap<String, String> classNames) {
        int numOfAnno = 0;
        int maxClassAllTargets = 0;
        int[][] targets = null;
        try {
            int length = problem.getLength();
            LabelReader labelReader = new LabelReader(length, annotationLabels);

            targets = labelReader.getTargets(filename, problem.getChildren());
            maxClassAllTargets = labelReader.getNumOfClasses();
            //annotationLabels = labelReader.getAnnotations();
            
            //Put the class names read with LabelReader for use by caller
            if(classNames != null)
            	for(String key : labelReader.getClassnames().keySet())
            		classNames.put(key, labelReader.getClassnames().get(key));
            
            numOfAnno = labelReader.getNumOfAnnotations();
            
            if (debugFlag.equals("true")) 
            {
              java.util.HashMap<String, String> classnames = labelReader.getClassnames();
              for(int i=0; i<classnames.size(); i++)
              {
            	for (Map.Entry<String, String> e : classnames.entrySet())
            	    System.out.println(e.getKey() + ": " + e.getValue());
              }

              for (int i = 0; i < numOfAnno; i++) {
                    for (int j = 0; j < length; j++) {
                        System.out.print(targets[i][j] + " ");
                    }
              }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        resArr[0] = numOfAnno;
        resArr[1] = maxClassAllTargets;
        return targets;
    }

    /*
     *  A classifier that takes a particular classification algorithm (as a String) and returns
     *   recognition rate. 
     *    
     */
    public float classifyGivenAMethod(String chosenClassifier, String path, HashMap<String, String> parameters, float[][] selectedTrainingFeatures, float[][] selectedTestingFeatures, int[] trainingtargets, int[] testingtargets, Annotation[] annotations) throws Exception {
 
    	Classifier classifier = getClassifierGivenName(chosenClassifier, path, parameters);

    	float rate = 0;
    	if(classifier != null)
    	    rate = (new Validator()).classify(selectedTrainingFeatures, selectedTestingFeatures, trainingtargets, testingtargets, classifier, annotations);
        return rate;
        
    }
    /*
     *  A classifier that takes Classifier object as a particular classification algorithm and returns
     *   recognition rate. 
     *    
     */
    public float classifyGivenAMethod(Classifier classifier, HashMap<String, String> parameters, float[][] selectedTrainingFeatures, float[][] selectedTestingFeatures, int[] trainingtargets, int[] testingtargets, Annotation[] annotations) throws Exception {
        
    	float rate = (new Validator()).classify(selectedTrainingFeatures, selectedTestingFeatures, trainingtargets, testingtargets, classifier, annotations);
        return rate;
    }

    
    /*
     *  A classifier that takes SavableClassifier object as a particular classification algorithm
     *  It has a trained model in it.  Returns prediction results (and probabilities via Annotation argument).
     */
    public int[] classifyGivenAMethod(SavableClassifier classifier, float[][] testingFeatures, Annotation[] annotations) throws Exception {
        int[] results = null;
        double[] prob = new double[testingFeatures.length];

        results = classifier.classifyUsingModel(classifier.getModel(), testingFeatures, prob);
        for(int i=0; i<testingFeatures.length; i++)
        {
        	  annotations[i].anno = results[i];
              annotations[i].prob = prob[i];
        }
        return results;
    }

    /*
     * get an object of a classifier based on name
     */
    /*
    public Classifier getClassifierGivenName(String chosenClassifier, HashMap<String, String> parameters) throws Exception
    {
       Classifier classifier = null;
       if (chosenClassifier.equalsIgnoreCase("SVM")) {
        classifier = new SVMClassifier(parameters);
       }
       else if (chosenClassifier.equalsIgnoreCase("LDA")) {
        classifier = new LDAClassifier(parameters);
       }
       else if (chosenClassifier.startsWith("W_")) {
        classifier = new WekaClassifiers(chosenClassifier);
       }
       else {
        System.err.println(chosenClassifier + "is not a supported classifer.");
        throw new Exception(chosenClassifier + "is not a supported classifer.");
       }
       return classifier;
    }
    */
    
    /*
     *
     * The feature extractor that takes 1 data set.
     * Useful for methods such as wavelet transform.
     * This method takes a HashMap for possible parameters.
     * 
     * The "extractor" may be a class name to allow dynamic loading of algorithm classes.
     *
     */
    public float[][] extractGivenAMethod(String chosenExtractor, String path, java.util.HashMap<String, String> parameters, DataInput problem) throws Exception 
    {
    	if (chosenExtractor == null || chosenExtractor.equalsIgnoreCase("NONE"))
    	{
    		//use raw image or middle stack for 3D
    		if(!problem.ofSameSize())
    			throw new Exception("When no extractor is selected, the images or ROIs must be of the same size.");

    		int stackSize = 0;
    		if (problem.getMode() == DataInput.ROIMODE) //8/6/12
    			stackSize = problem.getDepth();
    		else 
    			stackSize = problem.getStackSize();
            int imageSize = problem.getHeight()*problem.getWidth();
            int imageType = problem.getImageType();
            
            if (stackSize > 1)
            	System.out.println("When no extractor is selected, the middle stack of the 3D image is used for efficiency purpose.");

            return extractGivenNONE(problem.getData(stackSize / 2 + 1), imageSize, imageType);
    	}

        //those that are not "NONE"    	    	
    	FeatureExtractor extractor = getExtractorGivenName(chosenExtractor, path, parameters);

    	//check if it is the right type of feature extractor (2D or 3D)
    	int stackSize = problem.getStackSize();
    	int depth = problem.getDepth();
    	if ((stackSize == 1 && extractor.is3DExtractor()) || (stackSize > 1 && depth !=1 && (!extractor.is3DExtractor())))
    	{
            System.out.println("invalid stack size for the corresponding feature extractor");
            System.exit(1);
    	}
    	return extractor.calcFeatures(problem);	
    		
    }
	
    /**
     * Convert data to float[][] if there is no extractor given.
     * @param problem
     * @return float[][]
     */
    float[][] extractGivenNONE(ArrayList datain, int imageSize, int imageType) throws Exception
    {
  	    
        int length = datain.size();
        float[][] features = new float[length][imageSize];
        for (int i = 0; i < length; i++) {
   	      if(imageType == DataInput.GRAY8 || imageType == DataInput.COLOR_RGB)
   	      {
  	        byte[] data = (byte[]) datain.get(i);
            for (int j = 0; j < imageSize; j++) 
              features[i][j] = (float) (data[j] & 0xff);
          }else if (imageType == DataInput.GRAY16)
          {
	    	int[] data = (int[]) datain.get(i);
 	        for(int j = 0; j< imageSize; j++)
 	    	  features[i][j] = (float) (data[j]&0xffff);
	      }	
 	      else if(imageType == DataInput.GRAY32)
 	      {
	    	float[] data = (float[]) datain.get(i);
 	        for(int j = 0; j< imageSize; j++)
 	 	      features[i][j] = (float) data[j];
 	      }
 	      else
 	      { 
 	    	throw new Exception("Unsuppored Image Type for Feature Extractor");
 	      }
       } //for all images
       return features;
    }
    
    /* 
     * overloaded method for applying extractor to a ROI
     * ImgDimension is the size of the ROI, e.g. width, height, depth (may be 2D or 3D)
     *   
     *  8/5/2011: Current version only deals with 2DROI (depth == 1)
     *  In order to handle 3D ROI, 3D feature extractors need to work with byte[] with 3D info.
     *  9/2/2011: byte[][] is changed to ArrayList for other ImageProcessor types     
     */
     public float[][] extractGivenAMethod(String chosenExtractor, String path, java.util.HashMap<String, String> parameters, ArrayList data, int imageType, ImgDimension dim) throws Exception
     {
    	 if (chosenExtractor == null || chosenExtractor.equalsIgnoreCase("NONE")) 
    		return extractGivenNONE(data, dim.height*dim.width, imageType); 
            
        //those that are not "NONE"    	    	
     	FeatureExtractor extractor = getExtractorGivenName(chosenExtractor, path, parameters);

     	//check if it is the right type of feature extractor (2D or 3D)
    	if (dim.depth > 1 || extractor.is3DExtractor())
    	{
            System.out.println("Calling 3D ROI feature extractor .. ");
            //System.exit(1);
    	}

    	return extractor.calcFeatures(data, imageType, dim);	
     }
     
     /*
    //the obsolete version with static mapping
    public FeatureExtractor getExtractorGivenName(String name, HashMap<String, String> parameters)
    {
       FeatureExtractor extractor = null;
       if (name.equalsIgnoreCase("HAAR")) 
          extractor = new HaarFeatureExtractor(parameters);
       else if (name.equalsIgnoreCase("PARTIAL3D"))
          extractor = new StackSimpleHaarFeatureExtractor(parameters);
       else if (name.equals("LIGHT3D")) 
    	  extractor = new StackThreeDirectionHaarFeatureExtractor(parameters);
       else if (name.equals("2D Hu Moments")) 
          extractor = new ImageMoments(parameters);
   	   else
          setGUIOutput(name + "is not a supported extractor.");

   	   return extractor;
    }
    */
    
    
    /*
     * Dynamically load an feature extractor 08/11
     * classname is the fully qualified name of the class with main(); 
     * cpath (classpath) is the directory or the jar.
     * For example: 
     *    "plugins/DummyFeatureExtractor/"
     * or "plugins/DummyFeatureExtractor/dummy.jar"
     * It may be null if no additional classpath is needed
     */
    public FeatureExtractor getExtractorGivenName(String classname, String cpath, HashMap<String, String> parameters) throws Exception
    {
       FeatureExtractor extractor = null;
       Object o = loadObjectGivenClassName(classname, cpath);
       if (!(o instanceof FeatureExtractor))
    	   throw new Exception("The class is not a supported feature extractor.");
       else
       {
    	   extractor = (FeatureExtractor) o;
           extractor.setParameters(parameters);
     	   return extractor;
       }
    }
    
    public FeatureSelector getSelectorGivenName(String classname, String cpath, HashMap<String, String> parameters) throws Exception
    {
       FeatureSelector selector = null;
       Object o = loadObjectGivenClassName(classname, cpath);
       if (!(o instanceof FeatureSelector))
    	   throw new Exception("The class is not a supported feature selector.");
       else
       {
    	   selector = (FeatureSelector) o;
           selector.setParameters(parameters);
     	   return selector;
       }
    }

    public Classifier getClassifierGivenName(String classname, String cpath, HashMap<String, String> parameters) throws Exception
    {
       Classifier classifier = null;
       Object o = loadObjectGivenClassName(classname, cpath);
       if (!(o instanceof Classifier))
    	   throw new Exception("The class is not a supported classifier.");
       else
       {
    	   classifier = (Classifier) o;
           classifier.setParameters(parameters);
     	   return classifier;
       }
    }

    
    public Object loadObjectGivenClassName(String classname, String cpath) throws Exception
    {
    	ClassLoader loader;
        try
        {
           if (cpath != null)
           {   
         	   java.net.URL[] urls = new java.net.URL[1];
         	   //urls[0] = new java.net.URL("file:E:/IANO/plugins/DummyFeatureExtractor/dummy.jar");
          	   //urls[0] = new java.net.URL("file:plugins/DummyFeatureExtractor/");
        	   urls[0] = new java.net.URL("file:"+cpath);
     	       loader = new java.net.URLClassLoader(urls);
           }
     	   else //if null, use the default loader
     	       loader = this.getClass().getClassLoader();
           
     	   Class c = Class.forName(classname,false, loader);
     	   Object o = c.newInstance();
     	   return o;
        }
        catch(Exception e)
        {
     	   e.printStackTrace(); 
           System.err.println("Problem in loading " + classname + ". If it is a supported algorithm, please check the classpath.");
           throw e;
        }
    }
    
    
    /**
     * This method combines together the extraction result with multiple extractors in a single dimension (per problem)
     * 
     * @param problem
     * @param extractors
     * @return Array of extracted features for each image
     */
    public float[][] extractWithMultipleExtractors(DataInput problem, ArrayList<Extractor> extractors) throws Exception {
    	String extractor = "None";
        HashMap<String, String> params = new HashMap<String, String>();
        String externalPath = null;
        
        int numExtractors = extractors.size();
        float[][][] exFeatures = new float[numExtractors][][];
        
        int dataSize = 0;	//To keep track of total size
        for(int exIndex=0; exIndex < numExtractors; exIndex++) {
        	extractor = extractors.get(exIndex).getClassName();
        	params = extractors.get(exIndex).getParams();
        	externalPath = extractors.get(exIndex).getExternalPath();
        	
        	exFeatures[exIndex] = this.extractGivenAMethod(extractor, externalPath, params, problem);
        	
        	dataSize += exFeatures[exIndex][0].length;
        }
        
        float[][] features = null;
        
        if(numExtractors < 1) {	//If no extractor, call the function by passing "None"
        	features = this.extractGivenAMethod(extractor, null, params, problem);
        }
        else {	//Else, create feature array with enough space to hold data from all extractors 
        	features = new float[problem.getLength()][dataSize];
        	
        	int destPos = 0;
        	for(int exIndex=0; exIndex < numExtractors; exIndex++) {
        		for(int item=0; item < features.length; item++) {
        			System.arraycopy(exFeatures[exIndex][item], 0, features[item], destPos, exFeatures[exIndex][item].length);
        		}
        		destPos += exFeatures[exIndex][0].length;
        	}
        }
        
        return features;
    }
    
    /* 
     * overloaded method for applying 2nd+ subsequent (not parallel) extractor
     * Not used in GUI.
     * Need to supply float[][] as input data. DataInput will be used for some image-related parameter.
     */
     /* public float[][] extractGivenAMethod(String chosenExtractor, java.util.HashMap<String, String> parameters, float[][] data, DataInput problem) {

        float[][] features = null;
        
    	if (chosenExtractor.equalsIgnoreCase("NONE")) 
           return data;
 
    	FeatureExtractor extractor = getExtractorGivenName(chosenExtractor, parameters);

    	//check if it is the right type of feature extractor (2D or 3D)
    	int stackSize = problem.getStackSize();
    	if ((stackSize == 1 && extractor.is3DExtractor()) || (stackSize > 1 && (!extractor.is3DExtractor())))
    	{
            System.out.println("invalid stack size for the corresponding feature extractor");
            System.exit(1);
    	}

    	features = extractor.calcFeatures(data, problem);	
    	return features;	
    }	*/
    

    /*
     * Overloaded version of the extractor that takes 2 data sets
     * It may be useful for methods such as PCA when feature extraction cannot be done separately.
     * Or it takes an object (e.g. transform matrix/model) and a problem?
     * (07/2011: So the feature extractor can also return a model or apply a model, similar as a classifier!)
     * To Be Done later. 
     */
   /* protected void extractGivenAMethod(String chosenExtractor, String parameter, DataInput trainingproblem, DataInput testingproblem) {
        //check which method it is;
        if (chosenExtractor.equals("Principal Components")) {
            System.out.println("PCA is to be added");
            //handle PCA differently since it has unique data-driven transform matrix.
            //get transform matrix based on training images, then applied to testing images.
            //unsupervised, combined feature transform
            //1. transform training data
            //2. get the transforming coefficients from training
            //3. apply to testing
            //pass back. Using ComboFeatures singleton
        }
        else {
            System.out.println("No need to call this version of extractGivenAMethod");
        }
    }*/

    /*
     * The feature selector that takes 1 set. 
     * Used in cross validation mode.
     *
     */
    public ComboFeatures selectGivenAMethod(String chosenSelector, String path, HashMap<String,String> parameters, float[][] features, int[] targets) throws Exception {
  
    	ComboFeatures result = new ComboFeatures();
    	
        if (chosenSelector.equalsIgnoreCase("None"))
        {
        	result.setTrainingFeatures(features);
        	result.setIndices(null);
        }

        FeatureSelector  selector = getSelectorGivenName(chosenSelector, path, parameters);
        try {
        	result.setTrainingFeatures(selector.selectFeatures(features, targets));
            result.setIndices(selector.getIndices());
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
        
        return result;
        
    }

    /*
     *  The feature selector that takes 2 sets (training and testing)
     *  Return: two feature sets wrapped in ComboFeatures
     *  This method takes a HashMap for possible parameters.
     *  Note: If incrementally reading the images, the flow will be different (cann't get features in one shot .. 5/3/2011)
     */
    public ComboFeatures selectGivenAMethod(String chosenSelector, String path, java.util.HashMap<String, String> parameters, float[][] trainingFeatures, float[][] testingFeatures, int[] trainTargets, int[] testTargets) throws Exception {

    	ComboFeatures result = new ComboFeatures();

    	if (chosenSelector.equalsIgnoreCase("None")) 
        {
            result.setTrainingFeatures(trainingFeatures);
            result.setTestingFeatures(testingFeatures);
            result.setIndices(null);
        }
        
        float[][] selectedTrainingFeatures = null;
        int[] indices = null;
        
        //check the need for combined discretize (e.g. mMRM)
        String DISCRETE_FLAG = annotool.select.mRMRFeatureSelector.KEY_DISCRETE;
        boolean discrete = false;
        if(parameters.get(DISCRETE_FLAG) != null)
		  discrete = (Integer.parseInt((String)parameters.get(DISCRETE_FLAG)) == 1) ? true : false ;
        System.out.println("discrete?"+ discrete);
        
        if (discrete) //discretize data in a combined way,
        {
            annotool.Util.discretizeCombinedUnsupervised(trainingFeatures, testingFeatures, trainTargets, testTargets);
            //already discretized, so no need to do it again individually
            parameters.remove(DISCRETE_FLAG);
        }
        
        FeatureSelector selector = getSelectorGivenName(chosenSelector, path, parameters);
        try {
          selectedTrainingFeatures = selector.selectFeatures(trainingFeatures, trainTargets);
          //note: dimension may be different from passed in parameter now
          indices = selector.getIndices();
        }catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            //to be caught by the caller. Don't continue.
            throw new Exception(e.getMessage());
        }
        
        //select testing features
        float[][] selectedTestingFeatures = selector.selectFeaturesGivenIndices(testingFeatures, indices);
        result.setTrainingFeatures(selectedTrainingFeatures);
        result.setTestingFeatures(selectedTestingFeatures);
        result.setIndices(indices);
     
        return result;
        
    }

    //obsolete 08/11
    /*
    public FeatureSelector getSelectorGivenName(String name, HashMap<String, String> parameters) throws Exception
    {
       FeatureSelector selector = null;
       
       if (name.equalsIgnoreCase("Fisher")) 
          selector = new FishersCriterion(parameters);
       
       else if (name.equalsIgnoreCase("mRMR-MIQ") || name.equalsIgnoreCase("mRMR-MID"))
       {
    	   //should be added in xml file.
    	  parameters.put(annotool.select.mRMRFeatureSelector.KEY_METHOD,name);
          selector = new mRMRFeatureSelector(parameters);
       }
       else if (name.equalsIgnoreCase("Information Gain")) 
    	  selector = new WeKaFeatureSelectors(parameters);
       else
          throw new Exception(name + "is not a supported selector.");
   	   
       return selector;
    }*/

    
    /**
     * Selects the features based on pre-determined set of indices
     *      
     * @param features
     * @param indices
     * @return Selected features
     */
    public float[][] selectGivenIndices(float[][] features, int[] indices) {
    	float[][] selectedFeatures = new float[features.length][indices.length];
    	
        for (int i = 0; i < features.length; i++) {
            for (int j = 0; j < indices.length; j++) {
                selectedFeatures[i][j] = features[i][indices[j]];
            }
        }
        return selectedFeatures;        
    }    

    /*************************************************************************
     *   Other supporting methods for GUI and debugging						 *
     *************************************************************************/
    /*
     *  Draw the extract images. The method is for debugging and visualization.
     */
    private void drawFeatures(float[][] features, int width, int height, int length) {
        for (int i = 0; i < length; i++) {
            byte[] pixels = new byte[width * height];
            //find out min/max of features
            float min = 0, max = 0;
            for (int j = 0; j < width * height; j++) {
                if (min > features[i][j]) {
                    min = features[i][j];
                }
                else if (max < features[i][j]) {
                    max = features[i][j];
                }
            }

            //set value
            for (int j = 0; j < width * height; j++) //resize to 0-255
            {
                pixels[j] = (byte) ((features[i][j] - min) / (max - min) * 255);
            }

            //set up image
            ImagePlus testimg = NewImage.createByteImage("feature image" + i, width, height, 1, NewImage.FILL_BLACK);
            ImageProcessor test_ip = testimg.getProcessor();
            test_ip.setPixels(pixels);
            testimg.show();
            testimg.updateAndDraw();
        }
        //debug
        if (debugFlag.equals("true")) {
            System.out.println("feature:");
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < width * height; j++) {
                    System.out.print(features[i][j] + " ");
                }
            }
        }
    }

 
	public java.util.ArrayList<String> getAnnotationLabels() {
		return annotationLabels;
	}

	public void setAnnotationLabels(java.util.ArrayList<String> annotationLabels) {
		this.annotationLabels = annotationLabels;
	}
 
}
