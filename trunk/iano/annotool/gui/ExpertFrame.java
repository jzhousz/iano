package annotool.gui;

import javax.swing.*;

import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.*;

import annotool.classify.Classifier;
import annotool.classify.LDAClassifier;
import annotool.classify.SVMClassifier;
import annotool.classify.SavableClassifier;
import annotool.classify.Validator;
import annotool.classify.WekaClassifiers;
import annotool.io.AlgoXMLParser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import annotool.io.Algorithm;
import annotool.io.ChainModel;
import annotool.io.DataInput;
import annotool.io.Parameter;
import annotool.Annotation;
import annotool.Annotator;
import annotool.ComboFeatures;
import annotool.AnnOutputPanel;

public class ExpertFrame extends JFrame implements ActionListener, ItemListener, Runnable {
	private JTabbedPane tabPane;
	private JPanel pnlMain,
				   pnlAlgo,
				   pnlExt, pnlSel, pnlClass,
				   pnlExtMain, pnlSelMain, pnlClassMain,
				   pnlButton;
	private JButton btnRun, btnSaveModel, btnAnnotate;
	
	private JLabel lbExtractor, lbSelector, lbClassifier;
	private JComboBox cbExtractor, cbSelector, cbClassifier;
	
	private String channel;
	
	int tabNumber = 1; //Initial number of tabs
	
	//Algorithms and parameters
	Algorithm extractor = null;			
	Algorithm selector = null;			
	Algorithm classifier = null;	
	
	//To keep track of dynamically added components
	HashMap<String, JComponent> exParamControls = null;			//For extractor parameters
	HashMap<String, JComponent> selParamControls = null;		//For selector parameters
	HashMap<String, JComponent> classParamControls = null;		//For classifier parameters
	
	//Actual parameters and values to use in algorithms
	HashMap<String, String> exParams = null;
    HashMap<String, String> selParams = null;
    HashMap<String, String> classParams = null;
    
    //Algorithm names
    String featureExtractor = null;
    String featureSelector = null;
    String classifierChoice = null;
    
    Annotator anno = null;
	
	AnnOutputPanel pnlOutput = null;
	JProgressBar bar = null;
	
	private Thread thread = null;
	private boolean isRunning;
	
	JFileChooser fileChooser;
	ChainModel[] chainModels = null;
	
	public ExpertFrame(String arg0, boolean is3D, String channel) {
		super(arg0);
		this.channel = channel;
		
		fileChooser = new JFileChooser();
		
		pnlMain = new JPanel();
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
		//pnlMain.setPreferredSize(new java.awt.Dimension(540, 680));
		pnlMain.setBorder(new EmptyBorder(10, 10, 10, 10));
		pnlMain.setAlignmentY(TOP_ALIGNMENT);
		pnlMain.setAlignmentX(LEFT_ALIGNMENT);
		//this.add(pnlMain, BorderLayout.WEST);
		tabPane = new JTabbedPane();
		tabPane.addTab("Algorithms", pnlMain);
		this.add(tabPane);
		
		//Algorithm selector part
		pnlAlgo = new JPanel();
		pnlAlgo.setLayout(new BoxLayout(pnlAlgo, BoxLayout.PAGE_AXIS));
		pnlAlgo.setBorder(new CompoundBorder(new TitledBorder(null, "Algorithms", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5)));
		//pnlAlgo.setAlignmentY(TOP_ALIGNMENT);
		//pnlAlgo.setAlignmentX(LEFT_ALIGNMENT);
		
		//Parse the xml document with list of algorithms (for extractors, selectors and classifiers)
		AlgoXMLParser parser = new AlgoXMLParser();
		parser.runParser();
		
		//Labels
		if(is3D)
			lbExtractor = new JLabel("3D Feature Extractor");
		else
			lbExtractor = new JLabel("2D Feature Extractor");
		lbSelector = new JLabel("Feature Selector");
		lbClassifier = new JLabel("Classifier");
		
		//Combo boxes
		if(is3D)
			cbExtractor = new JComboBox(parser.get3DExtractorsAr());
		else
			cbExtractor = new JComboBox(parser.get2DExtractorsAr());
		
		cbSelector = new JComboBox(parser.getSelectorsAr());
		cbClassifier = new JComboBox(parser.getClassifiersAr());
		
		//Add item listeners to combo boxes
		cbExtractor.addItemListener(this);
		cbSelector.addItemListener(this);
		cbClassifier.addItemListener(this);
		
		//Extractor panel
		pnlExt = new JPanel(new BorderLayout());
		//pnlExt.setPreferredSize(new java.awt.Dimension(350, 110)); //Supplying height so that the algorithm panels don't shrink when none selected
		pnlExt.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		pnlExtMain = new JPanel(new GridLayout(1, 2));
		pnlExtMain.add(lbExtractor);
		pnlExtMain.add(cbExtractor);
		
		pnlExt.add(pnlExtMain, BorderLayout.NORTH);
		
		//Selector panel
		pnlSel = new JPanel(new BorderLayout());
		pnlSel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		pnlSelMain = new JPanel(new GridLayout(1, 2));
		pnlSelMain.add(lbSelector);
		pnlSelMain.add(cbSelector);
		
		pnlSel.add(pnlSelMain, BorderLayout.NORTH);
		
		//Classifier panel
		pnlClass = new JPanel(new BorderLayout());
		pnlClass.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		pnlClassMain = new JPanel(new GridLayout(1, 2));
		pnlClassMain.add(lbClassifier);
		pnlClassMain.add(cbClassifier);
		
		pnlClass.add(pnlClassMain, BorderLayout.NORTH);
		
		//Add to container
		pnlAlgo.add(pnlExt);
		pnlAlgo.add(pnlSel);
		pnlAlgo.add(pnlClass);
		
		//Button part
		btnRun = new JButton("Run");		
		btnRun.addActionListener(this);
		btnSaveModel = new JButton("Save Model", new ImageIcon("images/save.png"));
		btnSaveModel.setEnabled(false);
		btnSaveModel.addActionListener(this);
		btnAnnotate = new JButton("Apply Model/Annotate");
		btnAnnotate.setEnabled(false);
		btnAnnotate.addActionListener(this);
		
		pnlButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlButton.add(btnRun);
		pnlButton.add(btnSaveModel);
		pnlButton.add(btnAnnotate);	
		
		if(Annotator.output.equals(Annotator.OUTPUT_CHOICES[3])) {	//If train only mode
			btnRun.setText("Train");
		}
		
		pnlOutput = new AnnOutputPanel();
		bar = new JProgressBar(0, 100);
		bar.setValue(0);
		bar.setStringPainted(true);
		
		pnlMain.add(pnlAlgo);
		pnlMain.add(pnlButton);
		pnlMain.add(pnlOutput);
		pnlMain.add(bar);
		
		//Build parameter panels for default selection
		buildExParameterPanel();
		buildSelParameterPanel();
		buildClassParameterPanel();
	}
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnRun) {			
			if (thread == null)  {
	            thread = new Thread(this);
	            isRunning = true;
	            thread.start();
	        }
	    }
		else if (e.getSource() == btnSaveModel) {
			if(thread == null) {
				//Show confirmation dialog
				int choice = JOptionPane.showConfirmDialog(this,
					    "Depending on the algorithm, saving a model can take a while to finish. Do you want to continue?",
					    "Information",
					    JOptionPane.OK_CANCEL_OPTION,
					    JOptionPane.INFORMATION_MESSAGE);
				
				if(choice == JOptionPane.CANCEL_OPTION)
					return;
				
		        int returnVal = fileChooser.showSaveDialog(this);
	
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            pnlOutput.setOutput("Saving Model...");
		        	File file = fileChooser.getSelectedFile();		            
		            
		            
		            //Reset progress bar
		            bar.setValue(0);
		            
		            JButton[] buttons = {btnRun, btnSaveModel, btnAnnotate}; 
		            ModelSaver saver = new ModelSaver(bar, pnlOutput, buttons, chainModels, file);
		            Thread t1 = new Thread(saver);
		            t1.start();
		        }
			}
			else
				pnlOutput.setOutput("Cannot save model during processing.");
		}
		else if (e.getSource() == btnAnnotate) {
			if(thread == null) {
				//TODO: check if chain models exist and apply
				boolean modelLoaded = false;
				
				int returnVal = fileChooser.showOpenDialog(this);
				
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fileChooser.getSelectedFile();
					pnlOutput.setOutput("Loading model..");
		            ChainModel ch = new ChainModel();
		            try {
		            	ch.read(file);
		            	pnlOutput.setOutput("Model loaded successfully.");
		            	modelLoaded = true;
		            }
		            catch (Exception ex) {
		            	pnlOutput.setOutput("Model loading failure.");
		            	System.out.println(ex.getMessage());
		            }
		            
		            //Display model properties in output area
		            pnlOutput.setOutput("Feature Extractor: " + ch.getExtractorName());
		            pnlOutput.setOutput("Params:");
		            HashMap<String, String> params = ch.getExParams();
		            for (String parameter : params.keySet()) {
		            	pnlOutput.setOutput(parameter + "=" +params.get(parameter));
		        	}
		            pnlOutput.setOutput("Feature Selector: " + ch.getSelectorName());
		            for(int i=0; i < ch.getSelectedIndices().length; i++)
		            	pnlOutput.setOutput(String.valueOf(ch.getSelectedIndices()[i]));
		            pnlOutput.setOutput("Classifier: " + ch.getClassifierName());
		            
		            
		        }
			}
		}
	}
	public void itemStateChanged(ItemEvent e) {
		if(e.getSource() == cbExtractor && e.getStateChange() == 1) {		
			buildExParameterPanel();
		}
		if(e.getSource() == cbSelector && e.getStateChange() == 1) {		
			buildSelParameterPanel();
		}
		if(e.getSource() == cbClassifier && e.getStateChange() == 1) {		
			buildClassParameterPanel();
		}
	}
	
	public void run() {
		//Disable run and save buttons
		btnRun.setEnabled(false);
		btnSaveModel.setEnabled(false);
		btnAnnotate.setEnabled(false);
		
		//Get algorithm and parameters from GUI
		extractor = (Algorithm)cbExtractor.getSelectedItem();			
		selector = (Algorithm)cbSelector.getSelectedItem();			
		classifier = (Algorithm)cbClassifier.getSelectedItem();
		
		//HashMap of parameter and corresponding values
		exParams = new HashMap<String, String>();
        selParams = new HashMap<String, String>();
        classParams = new HashMap<String, String>();
        
        //Build parameters from the controls
        //TODO: Validate input data
        String value = null;
        //Parameters for extractor
        for(Parameter param : extractor.getParam()) {
        	JComponent control = exParamControls.get(param.getParamName());
        	if(control instanceof JTextField)
        		value = ((JTextField) control).getText().trim();
        	else if(control instanceof JComboBox)
        		value = ((JComboBox)control).getSelectedItem().toString();
        	else if(control instanceof JCheckBox)
        		value = ((JCheckBox)control).isSelected() ? "1" : "0";
        	else if(control instanceof JSpinner)
        		value = ((JSpinner)control).getValue().toString();
        	else if(control instanceof JComboBox)
        		value = ((JComboBox)control).getSelectedItem().toString();
        	exParams.put(param.getParamName(), value);
        }
        value = null;
        //Parameters for selector
        for(Parameter param : selector.getParam()) {
        	JComponent control = selParamControls.get(param.getParamName());
        	if(control instanceof JTextField)
        		value = ((JTextField) control).getText().trim();
        	else if(control instanceof JComboBox)
        		value = ((JComboBox)control).getSelectedItem().toString();
        	else if(control instanceof JCheckBox)
        		value = ((JCheckBox)control).isSelected() ? "1" : "0";
        	else if(control instanceof JSpinner)
        		value = ((JSpinner)control).getValue().toString();
        	else if(control instanceof JComboBox)
        		value = ((JComboBox)control).getSelectedItem().toString();
        	selParams.put(param.getParamName(), value);
        }
        value = null;
        //Parameters for classifier
        for(Parameter param : classifier.getParam()) {
        	JComponent control = classParamControls.get(param.getParamName());
        	if(control instanceof JTextField)
        		value = ((JTextField) control).getText().trim();
        	else if(control instanceof JComboBox)
        		value = ((JComboBox)control).getSelectedItem().toString();
        	else if(control instanceof JCheckBox)
        		value = ((JCheckBox)control).isSelected() ? "1" : "0";
        	else if(control instanceof JSpinner)
        		value = ((JSpinner)control).getValue().toString();
        	else if(control instanceof JComboBox)
        		value = ((JComboBox)control).getSelectedItem().toString();
        	classParams.put(param.getParamName(), value);
        }
        
        //Names of the algorithms
        featureExtractor = extractor.getName();
        featureSelector = selector.getName();
        classifierChoice = classifier.getName();
		
		anno = new Annotator();
		
		//Initiate appropriate process
		if(Annotator.output.equals(Annotator.OUTPUT_CHOICES[0])) {				//TT Mode
			ttRun();
		}
		else if(Annotator.output.equals(Annotator.OUTPUT_CHOICES[1])) {			//Cross validation mode
			cvRun();			
		}
		else if(Annotator.output.equals(Annotator.OUTPUT_CHOICES[3])) {			//Training only
			trainOnly();			
		}
		
		thread = null;
		
		this.pack();
		//Re-enable the buttons
		btnRun.setEnabled(true);
		btnSaveModel.setEnabled(true);
		btnAnnotate.setEnabled(true);
	}
	//Train Only
	private void trainOnly() {
		//read images and wrapped into DataInput instances.
        DataInput trainingProblem = new DataInput(Annotator.dir, Annotator.ext, channel);	        
      
        int[] resArr = new int[2]; //place holder for misc results
        ArrayList<String> annoLabels = new ArrayList<String>();
        int[][] trainingTargets = anno.readTargets(trainingProblem, Annotator.targetFile, resArr, annoLabels);
        //get statistics from training set
        int numOfAnno = resArr[0];
        anno.setAnnotationLabels(annoLabels);

        //feature extraction.
        if (!setProgress(30))  {
            return;
        }
        
        pnlOutput.setOutput("Extracting features...");
        
        float[][] trainingFeatures = anno.extractGivenAMethod(featureExtractor, exParams, trainingProblem);
        
        //Keep features to be dumped into chain file
        int imgWidth = trainingProblem.getWidth();
        int imgHeight = trainingProblem.getHeight();
        
        //clear data memory
        trainingProblem.setDataNull();

        //apply feature selector and classifier
        if (!setProgress(50)) {
            return;
        }
        
        int numoffeatures;
        
        //Initialize ChainModel object for each label
        chainModels = new ChainModel[numOfAnno];
        
        //loop for each annotation target (one image may have multiple labels)
        for (int i = 0; i < numOfAnno; i++) {
        	chainModels[i] = new ChainModel();
        	
            if (featureSelector.equalsIgnoreCase("None")) { //use the original feature without selection -- overwrite numoffeatures value
            	numoffeatures = trainingFeatures[0].length;
            }
            else 
            {
                pnlOutput.setOutput("Selecting features...");
            	//Supervised feature selectors need corresponding target data
                ComboFeatures combo = anno.selectGivenAMethod(featureSelector, selParams, trainingFeatures, trainingTargets[i]);
                //selected features overrides the passed in original features
                trainingFeatures = combo.getTrainingFeatures();
 
                
                //For dump file
                chainModels[i].setSelectedIndices(combo.getSelectedIndices());
            }

            pnlOutput.setOutput("Creating training model...");
            
            Classifier classifierObj = anno.getClassifierGivenName(classifierChoice, classParams);
            
            if(classifierObj instanceof SavableClassifier) {
            	((SavableClassifier)classifierObj).trainingOnly(trainingFeatures, trainingTargets[i]);
            	//TODO: If classifier does not guarantee that the trained model is set, then need to set it explicitly
            }
            
            //Save information to dump in chain file
        	chainModels[i].setImageSet(new File(Annotator.dir).getAbsolutePath());
        	chainModels[i].setImageSize(imgWidth + "x" + imgHeight);
        	chainModels[i].setMode("Training Only");
        	chainModels[i].setExtractorName(featureExtractor);
        	chainModels[i].setExParams(exParams);
        	chainModels[i].setSelectorName(featureSelector);
        	//chainModels[i].setSelectedIndices(combo.getSelectedIndices());//moved up
        	chainModels[i].setLabel(anno.getAnnotationLabels().get(i));
        	chainModels[i].setClassifierName(classifierChoice);
        	chainModels[i].setClassifier(classifierObj);
        	chainModels[i].setClassParams(classParams);            
            
            if (!setProgress(50 + (i + 1) * 50 / numOfAnno)) {
                return;
            }
        }//end of loop for annotation targets
        pnlOutput.setOutput("Training complete.");
	}
	
	//Training/Testing
	private void ttRun() {		
		//read images and wrapped into DataInput instances.
        DataInput trainingProblem = new DataInput(Annotator.dir, Annotator.ext, channel);
        DataInput testingProblem = new DataInput(Annotator.testdir, Annotator.testext, channel);	        
      
        int[] resArr = new int[2]; //place holder for misc results
        ArrayList<String> annoLabels = new ArrayList<String>();
        int[][] trainingTargets = anno.readTargets(trainingProblem, Annotator.targetFile, resArr, annoLabels);
        //get statistics from training set
        int numOfAnno = resArr[0];
        anno.setAnnotationLabels(annoLabels);	        

        //testing set targets
        int[][] testingTargets = anno.readTargets(testingProblem, Annotator.testtargetFile, resArr, null);

        //feature extraction.
        if (!setProgress(30))  {
            return;
        }
        
        pnlOutput.setOutput("Extracting features...");
        
        float[][] trainingFeatures = anno.extractGivenAMethod(featureExtractor, exParams, trainingProblem);
        float[][] testingFeatures = anno.extractGivenAMethod(featureExtractor, exParams, testingProblem);
        
        //Keep features to be dumped into chain file
        int imgWidth = trainingProblem.getWidth();
        int imgHeight = trainingProblem.getHeight();
        
        //clear data memory
        trainingProblem.setDataNull();
        testingProblem.setDataNull();

        //apply feature selector and classifier
        if (!setProgress(50)) {
            return;
        }
        //trainingTestingOutput(trainingFeatures, testingFeatures, trainingTargets, testingTargets, numOfAnno);
        
        int testingLength = testingFeatures.length;
        int numoffeatures;

        //initialize structure to store annotation results
        Annotation[][] annotations = new Annotation[numOfAnno][testingLength];
        for (int i = 0; i < numOfAnno; i++) {
            for (int j = 0; j < testingLength; j++) {
                annotations[i][j] = new Annotation();
            }
        }
        
        //Initialize ChainModel object for each label
        chainModels = new ChainModel[numOfAnno];
        
        //loop for each annotation target (one image may have multiple labels)
        for (int i = 0; i < numOfAnno; i++) {
        	chainModels[i] = new ChainModel();
        	
            if (featureSelector.equalsIgnoreCase("None")) { //use the original feature without selection -- overwrite numoffeatures value
            	numoffeatures = trainingFeatures[0].length;
            }
            else 
            {
                pnlOutput.setOutput("Selecting features...");
            	//Supervised feature selectors need corresponding target data
                ComboFeatures combo = anno.selectGivenAMethod(featureSelector, selParams, trainingFeatures, testingFeatures, trainingTargets[i], testingTargets[i]);
                //selected features overrides the passed in original features
                trainingFeatures = combo.getTrainingFeatures();
                testingFeatures = combo.getTestingFeatures();
                numoffeatures = trainingFeatures[0].length;
                
                //For dump file
                chainModels[i].setSelectedIndices(combo.getSelectedIndices());
            }

            //pass the training and testing data to Validator
            //get rate and prediction results for testing data
            float rate = 0;
            //setGUIOutput("Classifying/Annotating ... ");
            pnlOutput.setOutput("Classifying/Annotating...");
            
            Classifier classifierObj = anno.getClassifierGivenName(classifierChoice, classParams);
            try {
            	rate = anno.classifyGivenAMethod(classifierObj, classParams, trainingFeatures, testingFeatures, trainingTargets[i], testingTargets[i], annotations[i]);
            }
            catch(Exception ex) {
            	ex.printStackTrace();
            }
            		
            System.out.println(rate);
            
            //Save information to dump in chain file
        	chainModels[i].setImageSet(new File(Annotator.dir).getAbsolutePath());
        	chainModels[i].setTestingSet(new File(Annotator.testdir).getAbsolutePath());
        	chainModels[i].setImageSize(imgWidth + "x" + imgHeight);
        	chainModels[i].setMode("Training/Testing");
        	chainModels[i].setExtractorName(featureExtractor);
        	chainModels[i].setExParams(exParams);
        	chainModels[i].setSelectorName(featureSelector);
        	//chainModels[i].setSelectedIndices(combo.getSelectedIndices());//moved up
        	chainModels[i].setLabel(anno.getAnnotationLabels().get(i));
        	chainModels[i].setResult(rate);
        	chainModels[i].setClassifierName(classifierChoice);
        	chainModels[i].setClassifier(classifierObj);
        	chainModels[i].setClassParams(classParams);
            
            //Display result
            ResultPanel pnlResult = new ResultPanel(tabPane);
            tabPane.addTab("Result - " + anno.getAnnotationLabels().get(i), pnlResult);
            pnlResult.showResult(rate, testingTargets[i], annotations[i]); 
            
            //Add panel with title label and close button to the tab
            tabPane.setTabComponentAt(tabPane.getTabCount() - 1, 
                    new ButtonTabComponent("Result - " + anno.getAnnotationLabels().get(i), tabPane));
                
            pnlOutput.setOutput("Recog Rate for " + anno.getAnnotationLabels().get(i) + ": " + rate);
            if (!setProgress(50 + (i + 1) * 50 / numOfAnno)) {
                return;
            }
                //put the prediction results back to GUI
                //if (container != null) {
                    //container.getTablePanel().updateTestingTable(annotations);
                //}
            
                /*if (gui != null) {
                    gui.addCompareResultPanel(AnnControlPanel.classifiers, rates, AnnControlPanel.classifiers.length - 1);
                }*/
        }//end of loop for annotation targets        
        
	}
	
	//Cross validation
	private void cvRun() {
		//------ read image data from the directory ------------//
        DataInput problem = new DataInput(Annotator.dir, Annotator.ext, channel);

        //-----  read targets matrix (for multiple annotations, one per column) --------//
        if (!setProgress(20)) {
            return;
        }
        int[] resArr = new int[2]; //place holder for misc results
        java.util.ArrayList<String> annoLabels = new java.util.ArrayList<String>();
        int[][] targets = anno.readTargets(problem, Annotator.targetFile, resArr, annoLabels);
        int numOfAnno = resArr[0];
        anno.setAnnotationLabels(annoLabels);
        
        //----- feature extraction -------//
        if (!setProgress(30)) {
            return;
        }
        pnlOutput.setOutput("Extracing features ... ");
        float[][] features = anno.extractGivenAMethod(featureExtractor, exParams, problem);
        
        //Keep features to be dumped into chain file
        int imgWidth = problem.getWidth();
        int imgHeight = problem.getHeight();
        
        //raw data is not used after this point, set to null.
        problem.setDataNull();

        //-----  output the annotation/classification results
        if (!setProgress(50)) {
            return;
        }
        
        /*
         * Apply Feature Selection and Classification in CV mode.
         * This method uses k-fold CV.
         * Output the recognition rate of each task (per column) to a file.
         */
        
        int incomingDim = features[0].length;
        int length = features.length;
        int numoffeatures = incomingDim; //original dimension before selection

        /*if (Annotator.fileFlag.equals("true")) {
            try {
                outputfile = new java.io.BufferedWriter(new java.io.FileWriter("output"));
                ;
                outputfile.write("Outputs:\n");
                outputfile.flush();
            }
            catch (Exception e) {
                System.out.println("Output File Cann't Be Generated.");
            }
        }*/
        
        // parameters that are same for all target labels
        boolean shuffle = Boolean.parseBoolean(Annotator.shuffleFlag);
        // fold number K
        int K = 0;
        try {
            if (Annotator.fold.equals("LOO")) {
                K = length;
            }
            else {
                K = Integer.parseInt(Annotator.fold);
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
        
        chainModels = new ChainModel[numOfAnno];
        
        //loop for each annotation target
        for (int i = 0; i < numOfAnno; i++) {
        	
        	chainModels[i] = new ChainModel();
        	
            float recograte[] = null;
            int start = 50 + i * 50 / numOfAnno;
            int region = 50 / numOfAnno;

            //If selector is None, use default numoffeatures. Else, call the selector.
            if (!featureSelector.equalsIgnoreCase("None")) {
                pnlOutput.setOutput("Selecting features ... ");
                //override the original features and num of features
                ComboFeatures combo = anno.selectGivenAMethod(featureSelector, selParams, features, targets[i]);
                features = combo.getTrainingFeatures();
                
                numoffeatures = features[0].length;
                
                //For chain dump
                chainModels[i].setSelectedIndices(combo.getSelectedIndices());
            }

            pnlOutput.setOutput("Classifying/Annotating ... ");
            Classifier classifierObj = anno.getClassifierGivenName(classifierChoice, classParams);
            try {
            	recograte = (new Validator(bar, start, region)).KFoldGivenAClassifier(K, features, targets[i], classifierObj, classParams, shuffle, results[i]);
            }
            catch(Exception ex) {
            	pnlOutput.setOutput("Exception! " + ex.getMessage());
            	ex.printStackTrace();
            }
            
            //output results to GUI and file
            System.out.println("rate for annotation target " + i + ": " + recograte[K]);
            pnlOutput.setOutput("Recog Rate for " + anno.getAnnotationLabels().get(i) + ": " + recograte[K]);
            
            //Save information to dump in chain file
            chainModels[i].setImageSet(new File(Annotator.dir).getAbsolutePath());
            chainModels[i].setImageSize(imgWidth + "x" + imgHeight);
        	chainModels[i].setMode("Cross Validation. Fold: " + Annotator.fold);
        	chainModels[i].setExtractorName(featureExtractor);
        	chainModels[i].setExParams(exParams);
        	chainModels[i].setSelectorName(featureSelector);
        	//chainModels[i].setSelectedIndices(combo.getSelectedIndices());//moved up
        	chainModels[i].setLabel(anno.getAnnotationLabels().get(i));
        	chainModels[i].setResult(recograte[K]);
        	chainModels[i].setClassifierName(classifierChoice);
        	chainModels[i].setClassifier(classifierObj);
        	chainModels[i].setClassParams(classParams);
            
            //Display result
            ResultPanel pnlResult = new ResultPanel(tabPane);
            tabPane.addTab("Result - " + anno.getAnnotationLabels().get(i), pnlResult);
            pnlResult.showResult(recograte[K], targets[i], results[i]);
            if(!Annotator.fold.equals("LOO"))
            	pnlResult.showKFoldChart(recograte);
            
            //Add panel with title label and close button to the tab
            tabPane.setTabComponentAt(tabPane.getTabCount() - 1, 
                    new ButtonTabComponent("Result - " + anno.getAnnotationLabels().get(i), tabPane));
            
            /*if (outputfile != null && fileFlag.equals("true")) {
                try {
                    outputfile.write("Recognition Rate for annotation target " + i + ": " + recograte);
                    outputfile.flush();
                }
                catch (java.io.IOException e) {
                    System.out.println("Writing to output file failed.");
                }
            }*/
        } //end of loop for annotation targets

        /*if (outputfile != null && fileFlag.equals("true")) {
            try {
                outputfile.close();
            }
            catch (Exception e) {
            }
        }

        //put the prediction results back to GUI
        if (container != null) {
            container.getTablePanel().updateCVTable(results);
        }*/
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
        if (!isRunning && (currentProgress > 0)) {
            System.out.println("Interrupted at progress " + currentProgress);
            if (bar != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        bar.setValue(0);
                    }
                });
            }
            pnlOutput.setOutput("Process cancelled by user.");
            return false;
        }

        if (bar != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    bar.setValue(currentProgress);
                }
            });
        }
        return true;
    }
    /*
     * Builds the panel for feature extraction parameters 
     */
    private void buildExParameterPanel() {
		//Get the currently selected extractor
		Algorithm al = (Algorithm)cbExtractor.getSelectedItem();
		
		exParamControls = new HashMap<String, JComponent>();
		
		BorderLayout layout = (BorderLayout)pnlExt.getLayout();
		java.awt.Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER);
		if(centerComp != null)
			pnlExt.remove(centerComp); //Remove center component from pnlSel
		
		pnlExt.add(buildDynamicPanel(al, exParamControls), BorderLayout.CENTER);
		
		pnlExt.revalidate();
		this.pack();
	}

	/*
     * Builds the panel for selector parameters 
     */
	private void buildSelParameterPanel()
	{
		//Get the currently selected extractor
		Algorithm al = (Algorithm)cbSelector.getSelectedItem();
		
		selParamControls = new HashMap<String, JComponent>();
		
		BorderLayout layout = (BorderLayout)pnlSel.getLayout();
		java.awt.Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER);
		if(centerComp != null)
			pnlSel.remove(centerComp); //Remove center component from pnlSel
		
		pnlSel.add(buildDynamicPanel(al, selParamControls), BorderLayout.CENTER);
		
		pnlSel.revalidate();
		this.pack();
	}
    
	/*
     * Builds the panel for classification parameters 
     */
	private void buildClassParameterPanel()
	{
		//Get the currently selected extractor
		Algorithm al = (Algorithm)cbClassifier.getSelectedItem();
		
		classParamControls = new HashMap<String, JComponent>();
		
		BorderLayout layout = (BorderLayout)pnlClass.getLayout();
		java.awt.Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER);
		if(centerComp != null)
			pnlClass.remove(centerComp); //Remove center component from pnlSel
		
		pnlClass.add(buildDynamicPanel(al, classParamControls), BorderLayout.CENTER);
		
		pnlClass.revalidate();
		this.pack();
	}
	
	/*
	 * Creates the panel with controls for algorithm parameters
	 * 
	 * Algorithm al : Selected algorithm from the combo box
	 * HashMap paramControls : corresponding  hashmap to keep track of dynamically added components to retrieve values later
	 * 
	 */
	private JPanel buildDynamicPanel(Algorithm al, HashMap<String, JComponent> paramControls) {
		JPanel pnlContainer = new JPanel(new BorderLayout());
		
		//Get parameters for the algorithm
		ArrayList<Parameter> paramList = al.getParam();
		
		//Create dynamic components for parameters
		JPanel pnlParams = new JPanel();
		pnlParams.setLayout(new BoxLayout(pnlParams, BoxLayout.PAGE_AXIS));
		
		for(Parameter param : paramList) {
			JPanel pnlItem = new JPanel(new FlowLayout(FlowLayout.LEADING));
			
			if(param.getParamType().equals("Boolean")) {
				JCheckBox cb = new JCheckBox(param.getParamName());
				
				if(param.getParamDefault() != null)
					cb.setSelected((param.getParamDefault().equals("1")) ? true: false);	//1 for true, everything else : false
				
				pnlItem.add(cb);			
				
				//Put component in hashmap to access the value later
				paramControls.put(param.getParamName(), cb);
			}
			else if(param.getParamType().equals("Integer")) {
				JLabel lb = new JLabel(param.getParamName());
				
				SpinnerNumberModel snm = new SpinnerNumberModel();
				if(param.getParamMax() != null)
					snm.setMaximum(Integer.parseInt(param.getParamMax()));
				if(param.getParamMin() != null)
					snm.setMinimum(Integer.parseInt(param.getParamMin()));
				if(param.getParamDefault() != null)
					snm.setValue(Integer.parseInt(param.getParamDefault()));
				JSpinner sp = new JSpinner(snm);
				sp.setPreferredSize(new java.awt.Dimension(80, 30));
				
				pnlItem.add(lb);
				pnlItem.add(sp);
				
				//Put component in hashmap to access the value later
				paramControls.put(param.getParamName(), sp);
			}
			else {// if(param.getParamType().equals("String") || param.getParamType().equals("Real")) {
				JLabel lb = new JLabel(param.getParamName());
				JComponent component = null;
				if(param.getParamDomain() == null) {
					component = new JTextField(param.getParamDefault());
					((JTextField)component).setText(param.getParamDefault());
				}
				else {
					component = new JComboBox(param.getParamDomain());
					((JComboBox)component).setSelectedItem(param.getParamDefault());
				}
				
				//component.setPreferredSize(new java.awt.Dimension(120, 30));
				pnlItem.add(lb);
				pnlItem.add(component);
				
				//Put component in hashmap to access the value later
				paramControls.put(param.getParamName(), component);
			}
			pnlParams.add(pnlItem);
		}
		
		pnlContainer.add(pnlParams, BorderLayout.CENTER);
		
		//Add parameter description
		JLabel lbDesc = new JLabel(al.getDescription());
		pnlContainer.add(lbDesc, BorderLayout.NORTH);
		
		return pnlContainer;
	}
	
	//Temporary main method for testing GUI
	public static void main(String[] args) {
		ExpertFrame ef = new ExpertFrame("Expert Mode", false, "g");
		ef.pack();
		ef.setVisible(true);
		ef.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
