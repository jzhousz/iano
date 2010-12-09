package annotool;

import javax.swing.SwingUtilities;

import annotool.io.*;
import annotool.extract.*;
import annotool.select.*;
import annotool.classify.*;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.*;


/********************************************************************************************

  Image Annotation Tool

  Jie Zhou  2008 - 2010

  Usage:java -Djava.library.path="../mRMRFeatureselector" annotool.Annotator

  Todo:
    -- read from a property file: System.getProperty("imagedir")
    -- provide ranked output of the annotation result (Classifier has reliable probability estimation?)
    -- Principal component feature extraction should be done combined for TT. Otherwise it may be an approximation since coordinates may be slightly difference unless the testing images are enough and meaningful?
    -- ROI should save model after training to save time.
    -- Check out LibLinear for fast training.

 ************************************************************************************************************/

public class Annotator implements Runnable
{
	//These properties will be saved in "./.annotool_properties" for user modification
	//They can also be changed at command line, by simply adding the property name/value pair:
	//    java -Dimgext=.png  classname

	//default system properties
	public final static String DEFAULT_DIR = "k150/"; //"stage4_6/" for embryos
	public final static String DEFAULT_EXT = ".jpg";  //".png"; 
	public final static String DEFAULT_TARGET = "k150_4c_target.txt";  //"target4_6_1.txt";
	public final static String DEFAULT_TESTDIR = "k150/"; //"stage4_6/" for embryos
	public final static String DEFAULT_TESTEXT = ".jpg";  //".png"; 
	public final static String DEFAULT_TESTTARGET = "k150_4c_target.txt";  //"target4_6_1.txt";
	public final static String DEFAULT_EXTRACTOR = "HAAR"; //"None"
	public final static String DEFAULT_SELECTOR = "mRMR-MIQ";  //"None" //change from "MRMR" on 081007, PHC
	public final static String DEFAULT_CLASSIFIER = "SVM";  //"SVM", "LDA".

	//default algo properties
	public final static String  DEFAULT_FEATURENUM = "8";
	public final static String  DEFAULT_MRMRTYPE = "mRMR-MIQ"; //081007: revised from "MIQ"
	public final static String  DEFAULT_CHANNEL = "g";
	public final static String  DEFAULT_DEBUG = "false";  //controls feature img display
	public final static String  DEFAULT_FILE = "false";  //file output
	public final static String  DEFAULT_DISCRETE = "true"; //mRMR needs this to be true
	public final static String  DEFAULT_THRESHOLD = "0";
	public final static String  DEFAULT_SHUFFLE = "false";
	public final static String  DEFAULT_SVM = "-t 0"; //"-t 2 -c 100 -b 1";
	public final static String  DEFAULT_WAVLEVEL = "1";
	public final static String  DEFAULT_FOLD = "LOO"; 
	public final static String  DEFAULT_OUTPUT = "TT"; 
	public final static String[] OUTPUT_CHOICES = {"TT", "CV", "ROI"}; 

	//properties values read from environment
	public static String dir = System.getProperty("imgdir", DEFAULT_DIR);
	public static String ext = System.getProperty("imgext", DEFAULT_EXT);
	public static String targetFile = System.getProperty("target", DEFAULT_TARGET);

	public static String testdir = System.getProperty("testimgdir", DEFAULT_TESTDIR);
	public static String testext = System.getProperty("testimgext", DEFAULT_TESTEXT); 
	public static String testtargetFile =  System.getProperty("testtarget", DEFAULT_TESTTARGET);

	public static String featureExtractor = System.getProperty("extractor", DEFAULT_EXTRACTOR); 
	public static String featureSelector = System.getProperty("selector", DEFAULT_SELECTOR); 
	public static String classifierChoice = System.getProperty("classifier", DEFAULT_CLASSIFIER); 

	public static String featureNum = System.getProperty("numoffeature", DEFAULT_FEATURENUM);
	public static String channel = System.getProperty("channel", DEFAULT_CHANNEL);
	public static String debugFlag =  System.getProperty("debug", DEFAULT_DEBUG);
	public static String fileFlag = System.getProperty("fileflag", DEFAULT_FILE);
	public static String discreteFlag = System.getProperty("discreteflag",DEFAULT_DISCRETE);
	public static String threshold = System.getProperty("threshold",DEFAULT_THRESHOLD);
	public static String shuffleFlag = System.getProperty("shuffleflag",DEFAULT_SHUFFLE);
	public static String svmpara = System.getProperty("svmpara",DEFAULT_SVM);
	public static String waveletLevel = System.getProperty("waveletlevel",DEFAULT_WAVLEVEL);
	public static String fold = System.getProperty("fold",DEFAULT_FOLD);
	public static String output = System.getProperty("output", DEFAULT_OUTPUT);

	//default max num of classes among all columns in the target file. Overwritten later.
	public static int maxClass = 10; 
	java.util.ArrayList<String> annotationLabels = null;
	protected Thread thread; //the running thread for background work.
	protected boolean isRunningFlag;
	protected javax.swing.JProgressBar bar = null;  
	protected javax.swing.JButton goButton = null;  
	protected javax.swing.JButton cancelButton = null;  
	protected AnnControlPanel container = null;  
	protected AnnOutputPanel outputPanel = null; 
	protected AnnotatorGUI gui = null; 
	protected java.io.Writer outputfile = null;  //will get file name from user;
	protected int[][] trainingTargets, testingTargets; //filled by readxxxTargets();

	public Annotator()
	{}

	public Annotator(AnnotatorGUI gui)//javax.swing.JProgressBar bar, javax.swing.JButton goButton, AnnControlPanel container, AnnOutputPanel outputPanel)
	{
		this.gui = gui;
		this.bar = gui.getControlPanel().getBar();
		this.goButton = gui.getControlPanel().getGoButton();
		this.cancelButton = gui.getControlPanel().getCancelButton();
		this.container = gui.getControlPanel();
		this.outputPanel = gui.getControlPanel().getOutputPanel();
	}

	//This method can be easily replaced by an imagej plugin entrance method.
	public static void main(String[] argv)
	{   
		if(argv.length >= 1) 
		{
			printUsage(); //print out default parameters in the system.
			return;
		}
		(new Annotator()).annotate();
	}

	public boolean startAnnotation()
	{
		if (thread == null)
		{	
		  thread =	new Thread(this);
		  isRunningFlag = true;
		  thread.start();
		}
		else
			return false;

		return true;
	}

	public boolean stopAnnotation()
	{
		if (thread != null)
		{
			//thread.interrupt();
			isRunningFlag = false;
			setGUIOutput("Annotator is trying to stop itself. May take a while.");
			System.out.println("Annotator is trying to stop itself. May take a while.");
			return true;
		}
		else
		{
			System.err.println("No thread to stop.");
			return false;
		}
		
	}

	//a desperate stop
	public void stopAnnotationRightNow()
	{
		thread.stop();
		
	}
	
	public void run()
	{
		annotate();
		
		//reset cursor etc after done
		resetGUI();
		
	}

	//This function calls one of several modes
	public void annotate()
	{
		if(!setProgress(10)) return;
		System.out.println("output:"+ output);
		if (output.equals(OUTPUT_CHOICES[0]))
			TTAnnotate();
		else if (output.equals(OUTPUT_CHOICES[1])) 
			CVAnnotate();
		else if (output.equals(OUTPUT_CHOICES[2])) 
			ROIAnnotate();
		else
		{
			System.out.println("Output mode:"+output+"is unknown");
			System.exit(0);
		}
	}

	public void TTAnnotate()
	{
		//read images
		DataInput trainingProblem = new DataInput(dir, ext, channel);
		DataInput testingProblem = new DataInput(testdir, testext, channel);

		//read targets
		if(!setProgress(20)) return;
		int numOfAnno = readTrainTestTargets(trainingProblem, testingProblem);

		//feature extraction. For 3D, may be it will be combined with getxxData() for memory issue
		if(!setProgress(30)) return;
		setGUIOutput("Extracing features ... ");
		float[][] trainingFeatures = null, testingFeatures = null;

		//03242010? For PCA, should training/testing to transformed together instead of differently?
		if (featureExtractor.equals("Principal Components"))
		{
			//getTTExtractedFeatures(trainingProblem, testingProblem, trainingFeatures, testingFeatures);
		}
		else
		{
		   trainingFeatures = getExtractedFeatures(trainingProblem); 
		   testingFeatures = getExtractedFeatures(testingProblem);
		}

		trainingProblem.setDataNull();
		testingProblem.setDataNull();

		//get number of features
		if(!setProgress(40)) return;
		int numoffeatures = getNumberofFeatures();

		if(!setProgress(50)) return;
		trainingTestingOutput(trainingProblem, testingProblem, trainingFeatures, testingFeatures, trainingTargets, testingTargets, numoffeatures, numOfAnno);
	} //end of TTAnnotate

	/* 
	 * FE, FS, Classify in TT mode
	 */
	private void trainingTestingOutput(DataInput trainingProblem, DataInput testingProblem, float[][] trainingfeatures, float[][] testingfeatures, int[][] trainingtargets, int[][] testingtargets, int numoffeatures, int numOfAnno)
	{
		//dimension of extracted features before selection
		int incomingDim = trainingfeatures[0].length;
		int trainingLength = trainingProblem.getLength();
		int testingLength = testingProblem.getLength();

		if (featureSelector.equalsIgnoreCase("None"))
			//use the original feature without selection -- overwrite numoffeatures value
			numoffeatures = incomingDim;

		float   th = Float.parseFloat(threshold);
		boolean discrete = Boolean.parseBoolean(discreteFlag);

		Annotation[][] annotations = new Annotation[numOfAnno][testingLength];
		for(int i=0; i < numOfAnno; i++)
			for (int j = 0; j<testingLength; j++)
				annotations[i][j] = new Annotation();

		//pass the training and testing data to Validator
		//get rate and prediction results for testing data
		for (int i = 0; i < numOfAnno; i++)
		{
			float rate = 0;
			float[][] selectedTrainingFeatures = trainingfeatures;
			float[][] selectedTestingFeatures = testingfeatures;
			if (featureSelector.equalsIgnoreCase("mRMR-MIQ") || featureSelector.equalsIgnoreCase("mRMR-MID"))
			{
				//get a rough prediction, if needed by dicretization
				/*annotool.classify.Classifier wrapper= new annotool.classify.SVMClassifier(numoffeatures, "-t 0");
				int[] predictions = new int[testingfeatures.length];
				double[] prob = new double[testingfeatures.length];
				wrapper.classify(trainingfeatures, trainingtargets[i], testingfeatures, predictions, trainingfeatures.length,  testingfeatures.length, prob);
				wrapper = null;*/
	            /*
				float[][] continuous = new float[trainingfeatures.length][incomingDim];
				for(int m=0; m< trainingfeatures.length; m++)
				  for(int n=0; n< trainingfeatures.length; n++)
					  continuous[m][n] = trainingfeatures[m][n];
				*/
				setGUIOutput("Selecting features ... ");
				if (discrete)
				{
					annotool.Util.discretizeCombinedUnsupervised(selectedTrainingFeatures, selectedTestingFeatures, trainingtargets[i], testingtargets[i]);
				}
				
				FeatureSelector selector = (new mRMRFeatureSelector(selectedTrainingFeatures, trainingtargets[i], trainingLength, incomingDim, numoffeatures, featureSelector, false, th));
			    selectedTrainingFeatures = selector.selectFeatures();
			    int[] indices = selector.getIndices();
			    selector = (new mRMRFeatureSelector(selectedTestingFeatures, testingtargets[i], testingLength, incomingDim, numoffeatures, featureSelector, false, th));//081007
			    selectedTestingFeatures = selector.selectFeaturesGivenIndices(indices);
			    
			}
			else if (featureSelector.equalsIgnoreCase("Information Gain"))
			{   //Mar 2010 
				setGUIOutput("Selecting features .... ");
				FeatureSelector selector = new WeKaFeatureSelectors(trainingfeatures, trainingtargets[i], numoffeatures, null, 0.2);
				selectedTrainingFeatures = selector.selectFeatures();
				//IMPORTANT: dimension have changed by feature selector
				numoffeatures = selectedTrainingFeatures[0].length;
				//get testing features using indices from training features?
			    int[] indices = selector.getIndices();
				selector = new WeKaFeatureSelectors(testingfeatures, null, numoffeatures, null, 0.2);//081007
			    selectedTestingFeatures = selector.selectFeaturesGivenIndices(indices);
			}

			setGUIOutput("Classifying/Annotating ... ");
			if (!classifierChoice.startsWith("Compare All"))
			{
			 rate = classifyGivenAMethod(classifierChoice,trainingLength, testingLength, numoffeatures, selectedTrainingFeatures,selectedTestingFeatures, trainingtargets[i], testingtargets[i], annotations[i]);
			 setGUIOutput("Recog Rate for "+ annotationLabels.get(i) + ": " + rate);
			 if(!setProgress(50+(i+1)*50/numOfAnno)) return;
			 if (gui!=null)
					gui.addResultPanel(annotationLabels.get(i), rate, testingtargets[i], annotations[i]);
				//put the prediction results back to GUI
			 if(container != null)
					container.getTablePanel().updateTestingTable(annotations);
			}
			else //compare all
			{
				if(numOfAnno != 1)
				{
					setGUIOutput("Classifiers are only compared when there is one target.");
					return;
				}
				setGUIOutput("Comparing all classification schedules ... ");
				float rates[] = new float[AnnControlPanel.classifiers.length];
				for(int c = 0; c<AnnControlPanel.classifiers.length; c++)
				{
					if(!AnnControlPanel.classifierSimpleStrs[c].startsWith("Compare")) //avoid the comparing option itself in the selection
					{	
					 rates[c] = classifyGivenAMethod(AnnControlPanel.classifierSimpleStrs[c],trainingLength, testingLength, numoffeatures, selectedTrainingFeatures,selectedTestingFeatures, trainingtargets[i], testingtargets[i], annotations[i]);
				     setGUIOutput(AnnControlPanel.classifiers[c]+": Recog Rate for "+ annotationLabels.get(i) + ": " + rates[c]);
					 if(!setProgress(50+(c+1)*50/AnnControlPanel.classifiers.length)) return;
					}
				}
				if (gui!=null)
						gui.addCompareResultPanel(AnnControlPanel.classifiers, rates, AnnControlPanel.classifiers.length -1);
			}
		}//end of loop for annotation tables.
		
	}

	//put into a seperate class
	public void ROIAnnotate()
	{
		new AnnROIAnnotator();
	}

	
	public void CVAnnotate()
	{
		//------ read image data from the directory ------------//
		DataInput problem = new DataInput(dir, ext, channel);

		//-----  read targets matrix (for multiple annotations, one per column) --------//
		if(!setProgress(20)) return;
		int numOfAnno = readTargets(problem);

		//----- feature extraction -------//
		if(!setProgress(30)) return;
		setGUIOutput("Extracing features ... ");
		float[][] features = getExtractedFeatures(problem); //data,length, width, height);

		//raw data is not used after this point, set to null, otherwise will be collected after the method exits  
		problem.setDataNull();

		//----- feature selection and annotation for each target column ---------//
		if(!setProgress(40)) return;
		int numoffeatures =getNumberofFeatures();

		//-----  output the annotation/classification results
		if(!setProgress(50)) return;
		cvOutput(features, trainingTargets, problem, numoffeatures, numOfAnno);
	}//end of CV annotate


	/*
	 * Output the recognition rate of each task (per column)
	 * This method can use k-fold CV.
	 */
	private void cvOutput(float[][] features, int[][] targets, DataInput problem, int numoffeatures, int numOfAnno)
	{
		//dimension of extracted features before selection
		int incomingDim = features[0].length;
		int length = problem.getLength();

		if(fileFlag.equals("true"))
			try{
				outputfile = new java.io.BufferedWriter(new java.io.FileWriter("output"));;
				outputfile.write("Outputs:\n");
				outputfile.flush();
			}catch(Exception e)
			{
				System.out.println("Output File Cann't Be Generated.");
			}

			if (featureSelector.equalsIgnoreCase("None"))
				//use the original feature without selection -- overwrite numoffeatures value
				numoffeatures = incomingDim;

			Classifier classifier = null;
			if (classifierChoice.equalsIgnoreCase("SVM"))
				classifier = new SVMClassifier(numoffeatures, svmpara);
			else if (classifierChoice.equalsIgnoreCase("LDA"))
				classifier = new LDAClassifier(numoffeatures);
			else if (classifierChoice.startsWith("W_"))
				classifier = new WekaClassifiers(numoffeatures, classifierChoice);

			boolean discrete = Boolean.parseBoolean(discreteFlag);
			float   th = Float.parseFloat(threshold);
			boolean shuffle = Boolean.parseBoolean(shuffleFlag);

			int K = 0;
			try
			{
				if (fold.equals("LOO"))
					K = length;
				else K = Integer.parseInt(fold);
			}catch(NumberFormatException e)
			{
				System.out.println("Number of fold is not a valid int. Set to " + length +".");
			}
			if (K <= 0 || K > length)
			{
				System.out.println("Number of fold is not a valid int. Set to " + length +".");
			}

			//allocate space for the results.
			Annotation[][] results = new Annotation[numOfAnno][length];
			for(int i=0; i < numOfAnno; i++)
				for (int j = 0; j<length; j++)
					results[i][j] = new Annotation();

			for (int i =  0; i < numOfAnno; i++)
			{
				float recograte = 0;
				int start = 50+i*50/numOfAnno;
				int region = 50/numOfAnno;

				//put in a method later float[][] getSelectedFeatures(features, targets, ..)
				//combine with ttoutput() for more selector types.
				if (featureSelector.equalsIgnoreCase("mRMR-MIQ") || featureSelector.equalsIgnoreCase("mRMR-MID"))
				{
					setGUIOutput("Selecting features ... ");
					//System.out.println(targets[i]);
					FeatureSelector selector = (new mRMRFeatureSelector(features, targets[i], length, incomingDim, numoffeatures, featureSelector, discrete, th));//081007
					float[][] selectedFeatures = selector.selectFeatures();
					if(debugFlag.equals("true"))
					{
						for(int j=0; j<length; j++)
							for(int k=0; k<numoffeatures; k++)
								System.out.println(selectedFeatures[j][k]);
					}
					setGUIOutput("Classifying/Annotating ... ");
					recograte = (new Validator(bar, start, region)).KFold(K, length, numoffeatures, selectedFeatures, targets[i],  classifier, shuffle, results[i]);
				}
				else if (featureSelector.equalsIgnoreCase("Information Gain"))
				{   //Mar 2010 
					setGUIOutput("Selecting features .... ");
					FeatureSelector selector = new WeKaFeatureSelectors(features, targets[i], numoffeatures, null, 0.2);
					float[][] selectedFeatures = selector.selectFeatures();
					//IMPORTANT: dimension have changed by feature selector
					numoffeatures = selectedFeatures[0].length;
					setGUIOutput("Classifying/Annotating ... ");
					recograte = (new Validator(bar, start, region)).KFold(K, length, numoffeatures, selectedFeatures, targets[i],  classifier, shuffle, results[i]);
				}
				else //no feature selection
				{
					setGUIOutput("Classifying/Annotating ... ");
					recograte = (new Validator(bar, start ,region)).KFold(K, length, numoffeatures, features, targets[i],  classifier, shuffle, results[i]);
				}
				//output
				System.out.println("rate for annotation target "+ i + ": " + recograte);
				setGUIOutput("Recog Rate for "+ annotationLabels.get(i) + ": " + recograte);
				if (gui!=null)
				  gui.addResultPanel(annotationLabels.get(i), recograte, targets[i], results[i]);
				if(outputfile != null && fileFlag.equals("true"))
					try{
						outputfile.write("Recognition Rate for annotation target "+ i + ": " + recograte);
						outputfile.flush();
					}catch(java.io.IOException e)
					{ 
						System.out.println("Writing to output file failed.");
					}
			} //end of loop for annotation targets
			
			if(outputfile != null && fileFlag.equals("true"))
				try{
					outputfile.close();
				}catch(Exception e) {}

				//put the prediction results back to GUI
				if(container != null)
					container.getTablePanel().updateCVTable(results);
	}

	/*
	 *Output the annotation detailed results for each image, 
	 *different from classification output, this method always uses LOO
	 *This method is not used and will be combined into cvOutput() 09/2008
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


	private void drawFeatures(float[][] features, int width, int height, int length)
	{

		//for(int i = 0; i < length; i++)
		for(int i = 0; i < 1; i++)
		{
			byte[] pixels =  new byte[width*height];
			//find out min/max of features
			float min=0,max=0;
			for(int j=0; j < width*height; j++)
				if (min > features[i][j])
					min = features[i][j];
				else if (max < features[i][j])
					max = features[i][j];

			//set value
			for(int j=0; j < width*height; j++)
				//resize to 0-255
				pixels[j] = (byte) ((features[i][j] - min)/(max-min) * 255);

			//set up image
			ImagePlus testimg = NewImage.createByteImage("feature image"+ i,  width, height, 1, NewImage.FILL_BLACK);
			ImageProcessor test_ip = testimg.getProcessor();
			test_ip.setPixels(pixels);
			testimg.show();
			testimg.updateAndDraw();
		}
		//debug
		/*
   		   if(debugFlag.equals("true"))
		   {
    		  System.out.println("feature:");
              for(int i = 0; i < length; i++)
     	        for (int j = 0; j< width*height; j++)
     	          System.out.print(features[i][j]+" ");
	       }*/
	}

	private static void printUsage()
	{
		System.out.println("Usage: java [jvmparameters] [properties] annotool.Annotator");
		System.out.println("Example: java -Xms500M -Xmx -Dimgdir=k150/ annotool.Annotator");
		System.out.println("You will need to set CLASSPATH to include imageJ and libSVM jar files.");
		System.out.println("You may also need to set java.library.path to include the native mRMR library.");

		System.out.println("\nDefault parameters: ");
		System.out.println("\timgdir:"+DEFAULT_DIR);
		System.out.println("\timgext:"+DEFAULT_EXT);
		System.out.println("\ttarget:" + DEFAULT_TARGET);
		System.out.println("\textractor:"+ DEFAULT_EXTRACTOR); 
		System.out.println("\tselector:"+ DEFAULT_SELECTOR); 

		System.out.println("\tchannel:"+DEFAULT_CHANNEL + "(for 3 channel color images)");
		System.out.println("\tnumoffeature:" + DEFAULT_FEATURENUM + "(for feature selector)");
		System.out.println("\tsvmpara:"+DEFAULT_SVM);
		System.out.println("\twaveletlevel:"+DEFAULT_WAVLEVEL);
		System.out.println("\tfold:"+DEFAULT_FOLD);
	}

	private boolean setProgress(final int currentProgress)
	{
		if (thread == null)
		{
			System.out.println("thread is null");
			return false;
		}
		//if	(thread.isInterrupted())
		if(!isRunningFlag && (currentProgress > 0))
		{
			System.out.println("Interrupted at progress "+currentProgress);
			if (bar!=null) 
		        SwingUtilities.invokeLater(new Runnable() {
		            public void run() {
		            	bar.setValue(0);
		            }
		            });
			setGUIOutput("Annotation process cancelled by user.");
			return false;
		}
		
		if (bar!=null) 
	        SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	            	bar.setValue(currentProgress);
	            }
	            });
		return true;
	}

	private void setGUIOutput(String output)
	{
		if(outputPanel != null)
			outputPanel.setOutput(output);
	}
	private void resetGUI()
	{
		if (bar != null)
		{
			goButton.setEnabled(true);
			cancelButton.setEnabled(false);

			container.setCursor(null); //turn off the wait cursor
			setProgress(0);
		}
	}

	/*
	 * return: number of annotations. Also pass back targets via parameters
	 */
	private int readTrainTestTargets(DataInput trainingProblem, DataInput testingProblem)
	{
		int numOfAnnotations = 0;
		try
		{
			//training
			int trainingLength = trainingProblem.getLength();
			LabelReader labelReader = new LabelReader(trainingLength);
			trainingTargets = labelReader.getTargets(targetFile, trainingProblem.getChildren());
			maxClass = labelReader.getNumOfClasses();
			annotationLabels = labelReader.getAnnotations();
			//testing	
			int testingLength = testingProblem.getLength();
			labelReader = new LabelReader(testingLength);
			testingTargets = labelReader.getTargets(testtargetFile, testingProblem.getChildren());

			numOfAnnotations = labelReader.getNumOfAnnotations();
		}catch(Exception e)
		{ e.printStackTrace();
		}

		return numOfAnnotations;
	}

	/* 
	 * for CV, only return 1 target matrix, and the number of annotations
	 * 
	 */
	protected int readTargets(DataInput problem)
	{
		int numOfAnno = 0;
		try
		{
			int length = problem.getLength();
			LabelReader labelReader = new LabelReader(length);
			trainingTargets = labelReader.getTargets(targetFile, problem.getChildren());
			maxClass = labelReader.getNumOfClasses();
			annotationLabels = labelReader.getAnnotations();
			numOfAnno = labelReader.getNumOfAnnotations();

			//if(debugFlag.equals("true"))
			//{
			//	for(int i=0; i<numOfAnno; i++)
			//	for(int j=0; j<length; j++)
			//	System.out.print(trainingtargets[i][j]+ " ");
			//}
		}catch(Exception e)
		{   e.printStackTrace();
		}

		return numOfAnno;
	}

	//needed for PCA.
	protected void getTTExtractedFeatures(DataInput trainingProblem, DataInput testingProblem, float[][] trainingFeatures, float[][] testingFeatures)
	{
	    //unsupervised, combined feature transform
		//alternatively, getting the transforming coefficients from training
		//1. combine training/testing data
		//2. extract (transform)
		//3. separate training and testing
		//4. How to pass back????
		
		
	
				
	}
	
	
	protected float[][] getExtractedFeatures(DataInput problem)
	{
		float[][] features = null;
		int stackSize = problem.getStackSize();

		if (featureExtractor.equals("HAAR"))
		{
			if(stackSize == 1)
				features = (new HaarFeatureExtractor(problem, getWavletLevel())).getFeatures();
			else
				System.out.println("invalid stack size for 2D images: " + stackSize);
		}
		else if (featureExtractor.equals("PARTIAL3D"))
		{
			if (stackSize > 1)//3D image stack
				features = (new StackSimpleHaarFeatureExtractor(problem, getWavletLevel())).calcFeatures();	
			else
				System.out.println("invalid stack size for 3D images: " + stackSize);	
		}
		else if (featureExtractor.equals("LIGHT3D"))
		{
			if (stackSize > 1)//3D image stack
				features = (new StackThreeDirectionHaarFeatureExtractor(problem, getWavletLevel())).calcFeatures();	
			else
				System.out.println("invalid stack size for 3D images: " + stackSize);	
		}
		/* else if (featureExtractor.equals("FULL3D")) TBA
		{
			if (stackSize > 1)//3D image stack
			  features = (new StackFullHaarFeatureExtractor(problem, getWavletLevel())).calcFeatures();	
			else
  			  System.out.println("invalid stack size for 3D images: " + stackSize);	
		}*/
		else //if (featureExtractor.equals("NONE")) //, use raw image or middle stack for 3D
		{
			int length = problem.getLength(); 
			int height = problem.getHeight();
			int width = problem.getWidth();
			byte[][] data = problem.getData(stackSize/2+1);
			features = new float[length][width*height];
			for(int i = 0; i < length; i++)
				for (int j=0; j < width*height; j++)
				{
					features[i][j] = (float) (data[i][j]&0xff);
					//System.out.println("features"+i+" "+j+":"+features[i][j]);
				}
		}
		//debug: draw the feature image
		if(debugFlag.equals("true"))
		{
			int length = problem.getLength(); 
			int height = problem.getHeight();
			int width = problem.getWidth();
			drawFeatures(features, width, height, length);
		}

		return features;
	}


	protected int getNumberofFeatures()
	{
		int numoffeatures;

		try
		{
			numoffeatures = Integer.parseInt(featureNum);
		}catch(NumberFormatException e)
		{
			System.out.println("Number of features is not a valid int. Set to " + DEFAULT_FEATURENUM +".");
			numoffeatures = Integer.parseInt(DEFAULT_FEATURENUM);
		}
		return numoffeatures;
	}

	protected int getWavletLevel()
	{
		int level;
		try
		{
			level = Integer.parseInt(waveletLevel);
		}catch(NumberFormatException e)
		{
			System.out.println("Number of wavelet levels is not a valid int. Set to " + DEFAULT_WAVLEVEL +".");
			level = Integer.parseInt(DEFAULT_WAVLEVEL);
		}
		return level;
	}


	float classifyGivenAMethod(String chosenClassifier, int trainingLength, int testingLength, int numoffeatures, float[][] selectedTrainingFeatures, float[][] selectedTestingFeatures, int[] trainingtargets, int[] testingtargets, Annotation[] annotations)
	{
		Classifier classifier = null;
		if (chosenClassifier.equalsIgnoreCase("SVM"))
				classifier = new SVMClassifier(numoffeatures, svmpara);
		else if (chosenClassifier.equalsIgnoreCase("LDA"))
				classifier = new LDAClassifier(numoffeatures);
		else if (chosenClassifier.startsWith("W_"))
				classifier = new WekaClassifiers(numoffeatures, chosenClassifier);
		else
		{	setGUIOutput(chosenClassifier + "is not a supported classifer.");
			return 0;
		}
		float rate = (new Validator()).classify(trainingLength, testingLength, numoffeatures, selectedTrainingFeatures,selectedTestingFeatures, trainingtargets, testingtargets, classifier, annotations);
		System.out.println("recognition rate:" + rate);
		return rate;
		
	}

	
	
}


