package annotool;

import javax.swing.SwingUtilities;
import java.util.*;

import annotool.io.*;
import annotool.extract.*;
import annotool.select.*;
import annotool.classify.*;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.*;

/********************************************************************************************

Image Annotation Tool          Jie Zhou   

This class is the entry point of the annotation algorithms. 
It can be called from GUI or start from command line.
Command line Usage:

java -Djava.library.path="../mRMRFeatureselector" annotool.Annotator

Properties can be set to change the default parameters such as image directory and extensions. 

May 2nd, 2011:  CV modes are not calling the ..Given...() methods. To be checked later.

A List of Todos And Thoughts (May 2011):
-- algorithm parameters will be passed in separately as a Hashmap of String (instead of property)
-- ! Save project (model) (provide a better GUI with algorithm details),Save results. 
Open project. (project explorer at the left has details of projects).
-- Load examples with suggested models

-- Plug-and-play by passing in full class names of the algorithms.
-- read from a property file such as "./.annotool_properties".
-- an option to provide probability output of some classifiers
-- provide ranked output of the annotation result for entire image (based on the probability output).
-- Principal component feature extraction: transform matrix based on training images needs to be saved and then applied to testing images.  
-- ROI can save model after training to save time.
-- Check out LibLinear for fast training.
-- The option of "all" channels was done in Bala-Sift module. To be incorporated.
-- rethink GUI: swt, using Eclipse professional style: 
Left Panel: Project Explore and Image Explore, when clicked, show in the middle? 
Center Panel: multiple tabs for result visualization; project detail; image detail.
Manual mode (current);  Auto mode (comparison, by Aleksey Levy)
Save project (model) (provide a better GUI with algorithm setup details), 
Open project. (project explorer at the left has details of projects).
-- Load examples with suggested models

 ************************************************************************************************************/
public class Annotator implements Runnable
{
    /*
     *  a list of default application properties
     */
    //directory for training or cross-validation images
    public final static String DEFAULT_DIR = "k150/"; //"stage4_6/" for embryos
    //image extension for training or cross-validation images
    public final static String DEFAULT_EXT = ".jpg";  //".png";
    //target file name for training or cross-validation images
    public final static String DEFAULT_TARGET = "k150_4c_target.txt";  //"target4_6_1.txt";
    //directory for testing images
    public final static String DEFAULT_TESTDIR = "k150/"; //"stage4_6/" for embryos
    //image extension for testing images
    public final static String DEFAULT_TESTEXT = ".jpg";  //".png";
    //target file name for testing images
    public final static String DEFAULT_TESTTARGET = "k150_4c_target.txt";  //"target4_6_1.txt";
    //feature extractor
    public final static String DEFAULT_EXTRACTOR = "HAAR";
    //feature selector
    public final static String DEFAULT_SELECTOR = "None"; // "MRMR" on 081007, PHC
    //feature classifier
    public final static String DEFAULT_CLASSIFIER = "SVM";
    //mode of the annotator.
    public final static String DEFAULT_OUTPUT = "TT";
    //Choice of modes: TT: Training/Testing; CV: cross-validation; ROI: Region-Of-Interest
    public final static String[] OUTPUT_CHOICES = {"TT", "CV", "ROI"};
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
    protected AnnControlPanel container = null;
    protected AnnOutputPanel outputPanel = null;
    protected AnnotatorGUI gui = null;

    //default constructor for command line
    public Annotator() {
    }

    //constructor used by GUI.
    public Annotator(AnnotatorGUI gui) {
        this.gui = gui;
        this.bar = gui.getControlPanel().getBar();
        this.goButton = gui.getControlPanel().getGoButton();
        this.cancelButton = gui.getControlPanel().getCancelButton();
        this.container = gui.getControlPanel();
        this.outputPanel = gui.getControlPanel().getOutputPanel();
    }

    //This method can be easily replaced by an ImageJ plugin entrance method.
    public static void main(String[] argv) {
        if (argv.length >= 1) {
            printUsage(); //print out default parameters in the system.
            return;
        }
        (new Annotator()).annotate();
    }

    //called by GUI to start the thread of annotation
    public boolean startAnnotation() {
        if (thread == null) {
            thread = new Thread(this);
            isRunningFlag = true;
            thread.start();
        }
        else {
            return false;
        }

        return true;
    }

    //called by GUI, trying to stop the thread of annotation.
    public boolean stopAnnotation() {
        if (thread != null) {
            isRunningFlag = false;
            setGUIOutput("Annotator is trying to stop itself. May take a while.");
            System.out.println("Annotator is trying to stop itself. May take a while.");
            return true;
        }
        else {
            System.err.println("No thread to stop.");
            return false;
        }
    }

    //A desperate stop. Currently not called by GUI since the stop() method is deprecated.
    public void stopAnnotationRightNow() {
        thread.stop();
    }

    //the working thread
    public void run() {
        annotate();
        //reset cursor etc after done
        resetGUI();
    }

    /**
     *   This method is the main entrance. It starts one mode.
     **/
    public void annotate() {
        if (!setProgress(10)) {
            return;
        }
        System.out.println("output:" + output);
        if (output.equals(OUTPUT_CHOICES[0])) {
            TTAnnotate();
        }
        else if (output.equals(OUTPUT_CHOICES[1])) {
            CVAnnotate();
        }
        else if (output.equals(OUTPUT_CHOICES[2])) {
            ROIAnnotate();
        }
        else {
            System.out.println("Output mode:" + output + "is unknown");
            System.exit(0);
        }
    }

    /**
     *  Do the annotation in Training/Testing mode.
     */
    public void TTAnnotate() {
        //read images and wrapped into DataInput instances.
        DataInput trainingProblem = new DataInput(dir, ext, channel);
        DataInput testingProblem = new DataInput(testdir, testext, channel);

        //read targets
        if (!setProgress(20)) {
            return;
        }
        int[] resArr = new int[2]; //place holder for misc results
        java.util.ArrayList<String> annoLabels = new java.util.ArrayList<String>();
        int[][] trainingTargets = readTargets(trainingProblem, targetFile, resArr, annoLabels);
        //get statistics from training set
        int numOfAnno = resArr[0];
        annotationLabels = annoLabels;

        //testing set targets
        int[][] testingTargets = readTargets(testingProblem, testtargetFile, resArr, null);

        //feature extraction.
        if (!setProgress(30)) {
            return;
        }
        setGUIOutput("Extracing features ... ");
        //need to call extraction twice to get the features back.
        java.util.HashMap<String, String> parameters = new java.util.HashMap<String, String>();
        if (featureExtractor.contains("HAAR")) {
            parameters.put(annotool.extract.HaarFeatureExtractor.LEVEL_KEY, String.valueOf(getWavletLevel()));
        }
        float[][] trainingFeatures = extractGivenAMethod(featureExtractor, parameters, trainingProblem);
        float[][] testingFeatures = extractGivenAMethod(featureExtractor, parameters, testingProblem);
        //clear data memory
        trainingProblem.setDataNull();
        testingProblem.setDataNull();

        //apply feature selector and classifier
        if (!setProgress(50)) {
            return;
        }
        trainingTestingOutput(trainingFeatures, testingFeatures, trainingTargets, testingTargets, numOfAnno);
    }

    /*
     *  Apply Feature Selection and Classification in TT mode.
     *  Called by TTAnnotate().
     */
    private void trainingTestingOutput(float[][] trainingfeatures, float[][] testingfeatures, int[][] trainingtargets, int[][] testingtargets, int numOfAnno) {
        int trainingLength = trainingfeatures.length;
        int testingLength = testingfeatures.length;
        int numoffeatures;

        //initialize structure to store annotation results
        Annotation[][] annotations = new Annotation[numOfAnno][testingLength];
        for (int i = 0; i < numOfAnno; i++) {
            for (int j = 0; j < testingLength; j++) {
                annotations[i][j] = new Annotation();
            }
        }
        //parameter hashmap for 2 mRMR selectors
        HashMap<String, String> para = new HashMap<String, String>();
        if (featureSelector.contains("mRMR")) {
        	int discretef = (discreteFlag.equalsIgnoreCase("true"))? 1:0; //parameter property file use 1/0 for boolean
            para = new HashMap<String, String>();
            para.put(annotool.select.mRMRFeatureSelector.KEY_NUM, String.valueOf(getNumberofFeatures()));
            para.put(annotool.select.mRMRFeatureSelector.KEY_DISCRETE, String.valueOf(discretef));
            para.put(annotool.select.mRMRFeatureSelector.KEY_DIS_TH, threshold);
        }

        //loop for each annotation target (one image may have multiple labels)
        for (int i = 0; i < numOfAnno; i++) {
            if (featureSelector.equalsIgnoreCase("None")) //use the original feature without selection -- overwrite numoffeatures value
            {
                numoffeatures = trainingfeatures[0].length;
            }
            else {
                setGUIOutput("Selecting features ... ");
                //Supervised feature selectors need corresponding target data
                ComboFeatures combo = selectGivenAMethod(featureSelector, para, trainingfeatures, testingfeatures, trainingtargets[i], testingtargets[i]);
                //selected features overrides the passed in original features
                trainingfeatures = combo.getTrainingFeatures();
                testingfeatures = combo.getTestingFeatures();
                numoffeatures = trainingfeatures[0].length;
            }

            //pass the training and testing data to Validator
            //get rate and prediction results for testing data
            float rate = 0;
            setGUIOutput("Classifying/Annotating ... ");
            if (!classifierChoice.startsWith("Compare All")) {
                para.clear();
                if (classifierChoice.equalsIgnoreCase("SVM")) {
                    para.put(annotool.classify.SVMClassifier.KEY_PARA, svmpara);
                }

                rate = classifyGivenAMethod(classifierChoice, para, trainingfeatures, testingfeatures, trainingtargets[i], testingtargets[i], annotations[i]);
                setGUIOutput("Recog Rate for " + annotationLabels.get(i) + ": " + rate);
                if (!setProgress(50 + (i + 1) * 50 / numOfAnno)) {
                    return;
                }
                if (gui != null) {
                    gui.addResultPanel(annotationLabels.get(i), rate, testingtargets[i], annotations[i]);
                }
                //put the prediction results back to GUI
                if (container != null) {
                    container.getTablePanel().updateTestingTable(annotations);
                }
            }
            else //compare all classifiers
            {
                if (numOfAnno != 1) {
                    setGUIOutput("Classifiers are only compared when there is one target.");
                    return;
                }
                setGUIOutput("Comparing all classification schedules ... ");
                float rates[] = new float[AnnControlPanel.classifiers.length];
                for (int c = 0; c < AnnControlPanel.classifiers.length; c++) {
                    if (!AnnControlPanel.classifierSimpleStrs[c].startsWith("Compare")) //avoid the comparing option itself in the selection
                    {
                        para.clear();
                        if (AnnControlPanel.classifierSimpleStrs[c].equalsIgnoreCase("SVM")) {
                            para.put(annotool.classify.SVMClassifier.KEY_PARA, svmpara);
                        }

                        rates[c] = classifyGivenAMethod(AnnControlPanel.classifierSimpleStrs[c], para, trainingfeatures, testingfeatures, trainingtargets[i], testingtargets[i], annotations[i]);
                        setGUIOutput(AnnControlPanel.classifiers[c] + ": Recog Rate for " + annotationLabels.get(i) + ": " + rates[c]);
                        if (!setProgress(50 + (c + 1) * 50 / AnnControlPanel.classifiers.length)) {
                            return;
                        }
                    }
                }
                if (gui != null) {
                    gui.addCompareResultPanel(AnnControlPanel.classifiers, rates, AnnControlPanel.classifiers.length - 1);
                }
            }
        }//end of loop for annotation targets
    }

    /**
     *  Do the annotation in Cross Validation mode.
     */
    public void CVAnnotate() {
        //------ read image data from the directory ------------//
        DataInput problem = new DataInput(dir, ext, channel);

        //-----  read targets matrix (for multiple annotations, one per column) --------//
        if (!setProgress(20)) {
            return;
        }
        int[] resArr = new int[2]; //place holder for misc results
        java.util.ArrayList<String> annoLabels = new java.util.ArrayList<String>();
        int[][] targets = readTargets(problem, targetFile, resArr, annoLabels);
        int numOfAnno = resArr[0];
        annotationLabels = annoLabels;

        //----- feature extraction -------//
        if (!setProgress(30)) {
            return;
        }
        setGUIOutput("Extracing features ... ");
        float[][] features = extractGivenAMethod(featureExtractor, null, problem);
        //raw data is not used after this point, set to null.
        problem.setDataNull();

        //-----  output the annotation/classification results
        if (!setProgress(50)) {
            return;
        }
        cvOutput(features, targets, numOfAnno);
    }//end of CV annotate


    /*
     * Apply Feature Selection and Classification in CV mode.
     * This method uses k-fold CV.
     * Output the recognition rate of each task (per column) to a file.
     * Called by CVAnnotate().
     */
    private void cvOutput(float[][] features, int[][] targets, int numOfAnno) {
        int incomingDim = features[0].length;
        int length = features.length;
        int numoffeatures = incomingDim; //original dimension before selection

        if (fileFlag.equals("true")) {
            try {
                outputfile = new java.io.BufferedWriter(new java.io.FileWriter("output"));
                ;
                outputfile.write("Outputs:\n");
                outputfile.flush();
            }
            catch (Exception e) {
                System.out.println("Output File Cann't Be Generated.");
            }
        }

        // parameters that are same for all target labels
        boolean shuffle = Boolean.parseBoolean(shuffleFlag);
        // fold number K
        int K = 0;
        try {
            if (fold.equals("LOO")) {
                K = length;
            }
            else {
                K = Integer.parseInt(fold);
            }
        }
        catch (NumberFormatException e) {
            System.out.println("Number of fold is not a valid int. Set to " + length + ".");
            K = length;
        }
        if (K <= 0 || K > length) {
            System.out.println("Number of fold is not a valid int. Set to " + length + ".");
            K = length;
        }

        //allocate space for the results.
        Annotation[][] results = new Annotation[numOfAnno][length];
        for (int i = 0; i < numOfAnno; i++) {
            for (int j = 0; j < length; j++) {
                results[i][j] = new Annotation();
            }
        }

        //loop for each annotation target
        for (int i = 0; i < numOfAnno; i++) {
            float recograte = 0;
            int start = 50 + i * 50 / numOfAnno;
            int region = 50 / numOfAnno;

            //If selector is None, use default numoffeatures. Else, call the selector.
            if (!featureSelector.equalsIgnoreCase("None")) {
                setGUIOutput("Selecting features ... ");
                //override the original features and num of features
                features = selectGivenAMethod(featureSelector, "", features, targets[i]);
                numoffeatures = features[0].length;
            }

            setGUIOutput("Classifying/Annotating ... ");
            Classifier classifier = null;
            if (classifierChoice.equalsIgnoreCase("SVM")) {
                classifier = new SVMClassifier(numoffeatures, svmpara);
            }
            else if (classifierChoice.equalsIgnoreCase("LDA")) {
                classifier = new LDAClassifier(numoffeatures);
            }
            else if (classifierChoice.startsWith("W_")) {
                classifier = new WekaClassifiers(numoffeatures, classifierChoice);
            }
            recograte = (new Validator(bar, start, region)).KFold(K, features, targets[i], classifier, shuffle, results[i]);

            //output results to GUI and file
            System.out.println("rate for annotation target " + i + ": " + recograte);
            setGUIOutput("Recog Rate for " + annotationLabels.get(i) + ": " + recograte);
            if (gui != null) {
                gui.addResultPanel(annotationLabels.get(i), recograte, targets[i], results[i]);
            }
            if (outputfile != null && fileFlag.equals("true")) {
                try {
                    outputfile.write("Recognition Rate for annotation target " + i + ": " + recograte);
                    outputfile.flush();
                }
                catch (java.io.IOException e) {
                    System.out.println("Writing to output file failed.");
                }
            }
        } //end of loop for annotation targets

        if (outputfile != null && fileFlag.equals("true")) {
            try {
                outputfile.close();
            }
            catch (Exception e) {
            }
        }

        //put the prediction results back to GUI
        if (container != null) {
            container.getTablePanel().updateCVTable(results);
        }
    }

    /**
     * A separate class handles ROI annotation mode
     */
    public void ROIAnnotate() {
        new AnnROIAnnotator();
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

            if (debugFlag.equals("true")) {
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
     *  A classifier that takes a particular classification algorithm and returns
     *   recognition rate. 
     *    
     */
    public float classifyGivenAMethod(String chosenClassifier, HashMap<String, String> parameters, float[][] selectedTrainingFeatures, float[][] selectedTestingFeatures, int[] trainingtargets, int[] testingtargets, Annotation[] annotations) {
        int numoffeatures = selectedTrainingFeatures[0].length;
        Classifier classifier = null;
        if (chosenClassifier.equalsIgnoreCase("SVM")) {
            classifier = new SVMClassifier(parameters);
          
            
        }
        else if (chosenClassifier.equalsIgnoreCase("LDA")) {
            classifier = new LDAClassifier(parameters);
        }
        else if (chosenClassifier.startsWith("W_")) {
            classifier = new WekaClassifiers(numoffeatures, chosenClassifier);
        }
        else {
            setGUIOutput(chosenClassifier + "is not a supported classifer.");
            return 0;
        }
        float rate = (new Validator()).classify(selectedTrainingFeatures, selectedTestingFeatures, trainingtargets, testingtargets, classifier, annotations);
        //System.out.println("recognition rate:" + rate);
        return rate;
    }

    /*
     *
     * Feature extractor that takes 1 data set.
     * Useful for methods such as wavelet.
     * This method takes a HashMap for possible parameters.
     * This method is used by the GUI chain comparison module.
     * TBD: The "extractor" may be a classname to allow dynamic loading of algorithm classes.
     *
     */
    public float[][] extractGivenAMethod(String extractor, java.util.HashMap<String, String> parameters, DataInput problem) {
        float[][] features = null;
        int stackSize = problem.getStackSize();

        if (extractor.equals("2D Hu Moments")) {
            if (stackSize == 1) {
            	System.err.println("Will call ImageMoments class");
            	System.exit(1);
                //features = (new ImageMoments(problem)).calcFeatures();
            }
            else {
                System.out.println("invalid stack size for 2D images: " + stackSize);
            }
        }
        if (extractor.equals("HAAR")) {
            if (stackSize == 1) {
            	 //Class.forName(extractor, )
                features = (new HaarFeatureExtractor(problem, parameters)).getFeatures();
            }
            else {
                System.out.println("invalid stack size for 2D images: " + stackSize);
            }
        }
        else if (extractor.equals("PARTIAL3D")) {
            if (stackSize > 1)//3D image stack
            {
                features = (new StackSimpleHaarFeatureExtractor(problem, getWavletLevel())).calcFeatures();
            }
            else {
                System.out.println("invalid stack size for 3D images: " + stackSize);
            }
        }
        else if (extractor.equals("LIGHT3D")) {
            if (stackSize > 1)//3D image stack
            {
                features = (new StackThreeDirectionHaarFeatureExtractor(problem, getWavletLevel())).calcFeatures();
            }
            else {
                System.out.println("invalid stack size for 3D images: " + stackSize);
            }
        }
        else if (extractor.equals("Principal Components")) {  //TO BE DONE,
            System.err.println("PCA is to be added");
            return null;
        }
        else if (extractor.equals("NONE")) //use raw image or middle stack for 3D
        {
            int length = problem.getLength();
            int height = problem.getHeight();
            int width = problem.getWidth();
            byte[][] data = problem.getData(stackSize / 2 + 1);
            features = new float[length][width * height];
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < width * height; j++) {
                    features[i][j] = (float) (data[i][j] & 0xff);
                }
            }
        }
        else{
            System.err.println("Unrecognized Algorithm Name.  Exiting.");
            System.exit(1);
        }

        return features;
    }

    /*
     * overloaded version of the extractor that takes 2 data sets
     * useful for methods such as PCA when feature extraction cannot be done separately.
     * TBD.
     */
    protected void extractGivenAMethod(String chosenExtractor, String parameter, DataInput trainingproblem, DataInput testingproblem) {
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
            ;
        }
        else {
            System.out.println("No need to call this version of extractGivenAMethod");
        }
    }

    /*
     * Feature selector that takes 1 set. Used in cv mode
     * To be checked later.
     */
    public float[][] selectGivenAMethod(String chosenSelector, String parameter, float[][] features, int[] targets) {
        int incomingDim = features[0].length;
        int length = features.length;
        int numoffeatures;

        if (chosenSelector.equalsIgnoreCase("None")) {
            return null;
        }
        else //will be passed in through parameter String argument later
        {
            numoffeatures = getNumberofFeatures();
        }

        if (chosenSelector.equalsIgnoreCase("mRMR-MIQ") || chosenSelector.equalsIgnoreCase("mRMR-MID")) {
            //parsing mRMR parameters. Will be moved into algorithm class.
            float th = Float.parseFloat(threshold);
            boolean discrete = Boolean.parseBoolean(discreteFlag);
            //String mRMRPara = "-f " + numoffeatures + " -t " + threshold + " -d "+ discreteFlag;
            //discrete, if set, is done on the set inside the method.
            FeatureSelector selector = (new mRMRFeatureSelector(features, targets, length, incomingDim, numoffeatures, featureSelector, discrete, th));
            try {
                features = selector.selectFeatures();
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        else if (chosenSelector.equalsIgnoreCase("Information Gain")) {
            FeatureSelector selector = new WeKaFeatureSelectors(features, targets, numoffeatures, null, 0.2);
            try {
                features = selector.selectFeatures();
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        return features;
    }

    /*
     *  Feature selector that takes 2 sets (training and testing)
     *  Return: two feature sets wrapped in ComboFeatures
     *  This method takes a HashMap for possible parameters.
     */
    public ComboFeatures selectGivenAMethod(String chosenSelector, java.util.HashMap<String, String> parameters, float[][] trainingFeatures, float[][] testingFeatures, int[] trainTargets, int[] testTargets) {
        //dimension of extracted features before selection
        int incomingDim = trainingFeatures[0].length;
        int trainingLength = trainingFeatures.length;
        int testingLength = testingFeatures.length;
        int numoffeatures;
        //the selector may still be None if called by outsider.
        if (chosenSelector.equalsIgnoreCase("None")) //use the original feature without selection
        {
            numoffeatures = incomingDim;
        }
        else //will be passed in through parameter String argument later
        {
            numoffeatures = getNumberofFeatures();
        }

        //if incrementally reading the images,
        //the flow will be different (cann't get features in one shot .... 5/3/2011)
        float[][] selectedTrainingFeatures = null;
        int[] indices = null;
        if (chosenSelector.equalsIgnoreCase("Fisher")) {
        	System.err.println("will call FishersCriterion class for feature selection");
        	System.exit(1);
        	/*
            FeatureSelector selector = (new FishersCriterion(trainingFeatures, trainTargets, parameters));
            try {
                selectedTrainingFeatures = selector.selectFeatures();
                indices = selector.getIndices();
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
            selector = (new FishersCriterion(testingFeatures, null, parameters));
            float[][] selectedTestingFeatures = selector.selectFeaturesGivenIndices(indices);
            ComboFeatures.getInstance().setTrainingFeatures(selectedTrainingFeatures);
            ComboFeatures.getInstance().setTestingFeatures(selectedTestingFeatures);
            */
        }
        if (chosenSelector.equalsIgnoreCase("mRMR-MIQ") || chosenSelector.equalsIgnoreCase("mRMR-MID")) {
            //parsing algorithm parameters. Will be moved into algorithm class.
            boolean discrete = Boolean.parseBoolean(parameters.get("DISCRETE_FLAG"));
            if (discrete) //discretize data in a combined way,
            {
                annotool.Util.discretizeCombinedUnsupervised(trainingFeatures, testingFeatures, trainTargets, testTargets);
            }

            FeatureSelector selector = (new mRMRFeatureSelector(trainingFeatures, trainTargets, chosenSelector, parameters));
            try {
                selectedTrainingFeatures = selector.selectFeatures();
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
            try {
                indices = selector.getIndices();
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
            selector = (new mRMRFeatureSelector(testingFeatures, testTargets, chosenSelector, parameters));
            float[][] selectedTestingFeatures = selector.selectFeaturesGivenIndices(indices);
            ComboFeatures.getInstance().setTrainingFeatures(selectedTrainingFeatures);
            ComboFeatures.getInstance().setTestingFeatures(selectedTestingFeatures);
        }
        else if (chosenSelector.equalsIgnoreCase("Information Gain")) {
            //Do selection on the training set
            FeatureSelector selector = new WeKaFeatureSelectors(trainingFeatures, trainTargets, numoffeatures, null, 0.2);
            try {
                selectedTrainingFeatures = selector.selectFeatures();
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
            //apply to testing. IMPORTANT: dimension has changed by feature selector
            try {
                indices = selector.getIndices();
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
            numoffeatures = indices.length;
            selector = new WeKaFeatureSelectors(testingFeatures, null, numoffeatures, null, 0.2);//081007
            float[][] selectedTestingFeatures = selector.selectFeaturesGivenIndices(indices);
            ComboFeatures.getInstance().setTrainingFeatures(selectedTrainingFeatures);
            ComboFeatures.getInstance().setTestingFeatures(selectedTestingFeatures);
        }

        return ComboFeatures.getInstance();

    }

    // ----- temporary methods for parsing algorithm parameters.	
    //parse a parameter for feature selector.
    protected int getNumberofFeatures() {
        int numoffeatures;

        try {
            numoffeatures = Integer.parseInt(featureNum);
        }
        catch (NumberFormatException e) {
            System.out.println("Number of features is not a valid int. Set to " + DEFAULT_FEATURENUM + ".");
            numoffeatures = Integer.parseInt(DEFAULT_FEATURENUM);
        }
        return numoffeatures;
    }

    //parameter for wavelet extractor.
    protected int getWavletLevel() {
        int level;
        try {
            level = Integer.parseInt(waveletLevel);
        }
        catch (NumberFormatException e) {
            System.out.println("Number of wavelet levels is not a valid int. Set to " + DEFAULT_WAVLEVEL + ".");
            level = Integer.parseInt(DEFAULT_WAVLEVEL);
        }
        return level;
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

    /*
     * The method has 2 purposes:
     * 1. update the value of the progress bar in GUI.
     * 2. check if there is a need to stop the working thread.
     * It is called periodically by the working thread.
     */
    private boolean setProgress(final int currentProgress) {
        if (thread == null) {
            System.out.println("thread is null");
            return false;
        }
        //if	(thread.isInterrupted())
        if (!isRunningFlag && (currentProgress > 0)) {
            System.out.println("Interrupted at progress " + currentProgress);
            if (bar != null) {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run() {
                        bar.setValue(0);
                    }
                });
            }
            setGUIOutput("Annotation process cancelled by user.");
            return false;
        }

        if (bar != null) {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run() {
                    bar.setValue(currentProgress);
                }
            });
        }
        return true;
    }

    //set the information in the output panel of the GUI.
    private void setGUIOutput(String output) {
        if (outputPanel != null) {
            outputPanel.setOutput(output);
        }
    }

    //reset the control related buttons on GUI.
    private void resetGUI() {
        if (bar != null) {
            goButton.setEnabled(true);
            cancelButton.setEnabled(false);

            container.setCursor(null); //turn off the wait cursor
            setProgress(0);
        }
    }

    // print the usage info for command line.
    private static void printUsage() {
        System.out.println("Usage: java [jvmparameters] [properties] annotool.Annotator");
        System.out.println("Example: java -Xms500M -Xmx -Dimgdir=k150/ annotool.Annotator");
        System.out.println("You will need to set CLASSPATH to include imageJ and libSVM jar files.");
        System.out.println("You may also need to set java.library.path to include the native mRMR library.");

        System.out.println("\nDefault parameters: ");
        System.out.println("\timgdir:" + DEFAULT_DIR);
        System.out.println("\timgext:" + DEFAULT_EXT);
        System.out.println("\ttarget:" + DEFAULT_TARGET);
        System.out.println("\textractor:" + DEFAULT_EXTRACTOR);
        System.out.println("\tselector:" + DEFAULT_SELECTOR);

        System.out.println("\tchannel:" + DEFAULT_CHANNEL + "(for 3 channel color images)");
        System.out.println("\tnumoffeature:" + DEFAULT_FEATURENUM + "(for feature selector)");
        System.out.println("\tsvmpara:" + DEFAULT_SVM);
        System.out.println("\twaveletlevel:" + DEFAULT_WAVLEVEL);
        System.out.println("\tfold:" + DEFAULT_FOLD);
    }

	public java.util.ArrayList<String> getAnnotationLabels() {
		return annotationLabels;
	}

	public void setAnnotationLabels(java.util.ArrayList<String> annotationLabels) {
		this.annotationLabels = annotationLabels;
	}

    /*
     *
     * Output detailed annotation results for each image, with probability ranking.
     * Different from classification output, this method always uses LOO (can apply to testing images too?)
     * Revisit later when classifiers can output probabilities.
     *
     */
    /*
    private void annotationOutput(String[] children, float[][] features, int[][] targets, int length, int width, int height, int numoffeatures, int numOfAnno)
    {
    if (featureSelector.equalsIgnoreCase("None"))
    //use the orignial feature without selection -- overwrite numoffeatures value
    numoffeatures = width*height;

    SVMClassifier classifier = new SVMClassifier(numoffeatures, svmpara);
    boolean discrete = Boolean.parseBoolean(discreteFlag);
    boolean shuffle = Boolean.parseBoolean(shuffleFlag);

    //allocate space for the results.
    Annotation[][] annotations = new Annotation[numOfAnno][length];
    for(int i=0; i < numOfAnno; i++)
    for (int j = 0; j<length; j++)
    annotations[i][j] = new Annotation();

    for (int i = 0; i < numOfAnno; i++)
    {
    int start = 50+i*50/numOfAnno;
    int region = 50/numOfAnno;

    if (featureSelector.equalsIgnoreCase("MRMR-MIQ") || featureSelector.equalsIgnoreCase("MRMR-MID"))
    {
    //FeatureSelector selector = (new mRMRFeatureSelector(features, targets[i], length, width*height, numoffeatures, selectorType, discrete));
    FeatureSelector selector = (new mRMRFeatureSelector(features, targets[i], length, width*height, numoffeatures, featureSelector, discrete)); //081007, by PHC
    float[][] selectedFeatures = selector.selectFeatures();
    (new Validator(bar, start, region)).LOO(length, numoffeatures, selectedFeatures, targets[i],  classifier, annotations[i], shuffle);
    }
    else
    {
    (new Validator(bar, start, region)).LOO(length, numoffeatures, features, targets[i],  classifier, annotations[i], shuffle);
    }
    }

    //Can be sorted based on probability later.
    for(int j = 0; j < length; j++)
    {
    //System.out.print("\n"+ children[j]+ " ");
    System.out.print("\n");
    for (int i= 0; i < numOfAnno; i++)
    //  if (annotations[i][j].anno == 1)
    //    System.out.print("anno "+ i +": "+ annotations[i][j].anno + "("+ annotations[i][j].prob +")\t");
    System.out.print("t:"+targets[i][j] + " p:" +annotations[i][j].anno+'\t');
    }
    }*/
}
