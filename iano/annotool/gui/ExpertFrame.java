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
import annotool.classify.SavableClassifier;
import annotool.classify.Validator;
import annotool.gui.model.ClassifierInfo;
import annotool.gui.model.Extractor;
import annotool.gui.model.ModelFilter;
import annotool.gui.model.ModelSaver;
import annotool.gui.model.Selector;
import annotool.io.AlgoXMLParser;
import annotool.AlgorithmValidation;

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

/**
 * This class is responsible for displaying different algorithm choices and executing the selected ones in Simple Mode.
 * It provides methods that runs training only, training/testing and cross validation modes
 */ 
 /* 8/13/2012: Set ChainModel for 3D image size.
 *
 */
public class ExpertFrame extends PopUpFrame implements ActionListener, ItemListener, Runnable {
	private static final int DIMUPPERBOUND = 600000;
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
	
	private int dimension;
	
	protected JProgressBar bar = null; 
	
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
	
	private Thread thread = null;
	private boolean isRunning;
	
	JFileChooser fileChooser;
	
	boolean enableSave = false;

	//Keep features to be dumped into chain file
    int imgWidth;
    int imgHeight;
    int imgDepth = 1;  //for 3D ROI 8/13/12
    int imgStackSize; //for 3D image
	
	public ExpertFrame(String arg0, boolean is3D, String channel, DataInput trainingProblem, DataInput testingProblem, boolean channelFlag) {
		super(arg0, trainingProblem, testingProblem, channel, channelFlag);
		this.channel = channel;
		
		fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(new ModelFilter());
		
		pnlMain = new JPanel();
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
		pnlMain.setBorder(new EmptyBorder(10, 10, 10, 10));
		pnlMain.setAlignmentY(TOP_ALIGNMENT);
		pnlMain.setAlignmentX(LEFT_ALIGNMENT);
		tabPane = new JTabbedPane();
		tabPane.addTab("Algorithms", pnlMain);
		
		JScrollPane scrollPane = new JScrollPane(tabPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(scrollPane);
		
		//Algorithm selector part
		pnlAlgo = new JPanel();
		pnlAlgo.setLayout(new BoxLayout(pnlAlgo, BoxLayout.PAGE_AXIS));
		pnlAlgo.setBorder(new CompoundBorder(new TitledBorder(null, "Algorithms", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5)));
		
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
		btnAnnotate = new JButton("Annotate");
		btnAnnotate.setEnabled(false);
		btnAnnotate.addActionListener(this);
		
		pnlButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlButton.add(btnRun);
		pnlButton.add(btnSaveModel);
		pnlButton.add(btnAnnotate);	
		
		if(Annotator.output.equals(Annotator.TO)) {	//If train only mode
			btnRun.setText("Train");
		}
		
		pnlOutput = new AnnOutputPanel(this);
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
				
				//Set the dimension of the problem.
				try {
					dimension = trainingProblem.getWidth() * trainingProblem.getHeight() * trainingProblem.getStackSize();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				//Get the currently selected extractor and selector
				extractor = (Algorithm)cbExtractor.getSelectedItem();			
				selector = (Algorithm)cbSelector.getSelectedItem();	
				
				// If the dimension is out of bounds and there is no extractor or selector, have a pop up dialogue
				// for the user.
				if (!(AlgorithmValidation.isWithinBounds( dimension, DIMUPPERBOUND, extractor, selector)) )
				{
					// Prints out the dimension to the console
					System.out.println("Dimension: " + dimension);
					
					String message = "The dimension is too high and might cause a heap space error. \nIt is recomended that you add an extractor or a selector to avoid this. Continue?";
					
					// The actual pop up dialog. 
					int diag_result = JOptionPane.showConfirmDialog(null, message);
					
					// If yes, then continue normally.
					if (diag_result == JOptionPane.YES_OPTION)
					{
						thread = new Thread(this);
		            	isRunning = true;
		            	thread.start();
					}
					// If no, don't do anything.
					else if (diag_result == JOptionPane.NO_OPTION)
					{
						return;
					}
					// Also don't do anything. 
					else
					{
						return;
					}
				}
				// Continue normally.
	            else
	            {
					thread = new Thread(this);
	            	isRunning = true;
	            	thread.start();
	            }
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
				int choice = JOptionPane.showConfirmDialog(this,
						"This will close all open windows.\n" +
						"The model from the current window will be used for the annotation process.\n" + 
						"All other unsaved progress will be lost.\n" +
						"Do you wish to continue?",
					    "Information",
					    JOptionPane.OK_CANCEL_OPTION,
					    JOptionPane.INFORMATION_MESSAGE);
				
				if(choice == JOptionPane.CANCEL_OPTION)
					return;
				
				//Set the flag that indicates this frame as annotation firing frame and then fire close window
				applyModelFired = true;
				this.pullThePlug();				
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
		//extractor = (Algorithm)cbExtractor.getSelectedItem();			
		//selector = (Algorithm)cbSelector.getSelectedItem();			
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

        //set the image dimension data (to be dumped into Chain Model) 
        //extracted from individual process method e.g. ttrun()
	    try {
			imgWidth = trainingProblem.getWidth();
			imgHeight = trainingProblem.getHeight();
			imgStackSize = trainingProblem.getStackSize();
			if(imgStackSize > 1)
			{
			  if (trainingProblem.getMode() == DataInput.ROIMODE)	
				imgDepth = trainingProblem.getDepth();
			  else
				imgDepth = imgStackSize;
			}
		} catch (Exception e) {
			pnlOutput.setOutput("Failed to read width/height/depth/stackSize from the problem.");
			e.printStackTrace();
			return;
		}

		anno = new Annotator();
		
		try {
			//Initiate appropriate process
			if(Annotator.output.equals(Annotator.TT)) {				//TT Mode
				ttRun();
			}
			else if(Annotator.output.equals(Annotator.CV)) {			//Cross validation mode
				cvRun();			
			}
			else if(Annotator.output.equals(Annotator.TO)) {			//Training only
				trainOnly();			
			}
		}
		catch (Throwable t) {
			pnlOutput.setOutput("ERROR: " + t.getMessage());
			t.printStackTrace();
		}
		
		thread = null;
		
		this.pack();
		//Re-enable the buttons
		btnRun.setEnabled(true);
		
		if(enableSave) {
			btnSaveModel.setEnabled(true);
			btnAnnotate.setEnabled(true);
		}
	}
	//Train Only
	private void trainOnly() {
		//read images and wrapped into DataInput instances.
        //DataInput trainingProblem = new DataInput(Annotator.dir, Annotator.ext, channel);	        
		if(trainingProblem == null) {
			pnlOutput.setOutput("Training problem is not set.");
			return;
		}
		
        ArrayList<String> annoLabels = trainingProblem.getAnnotations();
        HashMap<String, String> classNames = trainingProblem.getClassNames();
        int[][] trainingTargets = trainingProblem.getTargets();
        int numOfAnno = annoLabels.size();
        
        anno.setAnnotationLabels(annoLabels);//why??? TODO:used when writing out model file later, can be replaced with annotation labels from problem

        //feature extraction.
        if (!setProgress(30))  {
            return;
        }
        
        pnlOutput.setOutput("Extracting features...");
        
        //float[][] trainingFeatures = anno.extractGivenAMethod(featureExtractor, exParams, trainingProblem);
        float[][] trainingFeatures = null;
        try {
			trainingFeatures = anno.extractGivenAMethod(extractor.getClassName(), extractor.getExternalPath(), exParams, trainingProblem);
		} catch (Exception e) {
			pnlOutput.setOutput("ERROR: Feature extractor failed! Extractor = " + extractor.getName());
			pnlOutput.setOutput("       Message: "+e.getMessage());
			e.printStackTrace();
			setProgress(0);
			enableSave = false;
			return;
		}
        
        //apply feature selector and classifier
        if (!setProgress(50)) 
            return;
        
        //Initialize ChainModel object for each label
        chainModels = new ChainModel[numOfAnno];
        
        //loop for each annotation target (one image may have multiple labels)
        for (int i = 0; i < numOfAnno; i++) {
        	chainModels[i] = new ChainModel();
        	
        	float[][] selectedFeatures = null;
            if (featureSelector.equalsIgnoreCase("None")) { //use the original feature without selection
            	selectedFeatures = trainingFeatures;
            }
            else 
            {
                pnlOutput.setOutput("Selecting features...");
            	//Supervised feature selectors need corresponding target data
                ComboFeatures combo = null;
                try {
                	combo = anno.selectGivenAMethod(selector.getClassName(), selector.getExternalPath(), selParams, trainingFeatures, trainingTargets[i]);
                }
                catch (Exception ex) {
            		pnlOutput.setOutput("ERROR: Feature selection failed! Selector = " + featureSelector);
            		pnlOutput.setOutput("       Message: "+ex.getMessage());
            		ex.printStackTrace();
            		setProgress(0);
                	enableSave = false;
            		return;
                }
                //selected features overrides the passed in original features
                selectedFeatures = combo.getTrainingFeatures();
                
                //For dump file
                Selector sel = new Selector(featureSelector);
            	sel.setSelectedIndices(combo.getSelectedIndices());
            	//sel.setParams(selParams);//Not used in saving model
            	chainModels[i].addSelector(sel);
            }

            pnlOutput.setOutput("Creating training model...");
            
            Classifier classifierObj = null;
            try {
				classifierObj = anno.getClassifierGivenName(classifier.getClassName(), classifier.getExternalPath(), classParams);
			} catch (Exception e) {
				pnlOutput.setOutput("ERROR: Classifier failed! Classifier = " + classifier.getName());
				pnlOutput.setOutput("       Message: "+e.getMessage());
				e.printStackTrace();
				setProgress(0);
				enableSave = false;
				return;
			}
            
            if(classifierObj instanceof SavableClassifier) {
            	try {
            		((SavableClassifier)classifierObj).trainingOnly(selectedFeatures, trainingTargets[i]);
            		enableSave = true;
            	}
            	catch (Exception ex) {
            		pnlOutput.setOutput("ERROR: Classification failed! Classifier = " + classifier.getName());
            		pnlOutput.setOutput("       Message: "+ex.getMessage());
            		ex.printStackTrace();
            		setProgress(0);
            		enableSave = false;
            		return;
            	}
            }
            else
            	enableSave = false;
            
            //Save information to dump in chain file
        	chainModels[i].setImageSet(new File(Annotator.dir).getAbsolutePath());
			if(imgStackSize > 1)
				chainModels[i].setImageSize(imgWidth + "x" + imgHeight + "x" + imgDepth);
			else
				chainModels[i].setImageSize(imgWidth + "x" + imgHeight);
        	chainModels[i].setMode("Training Only");
        	chainModels[i].setChannel(channel);
        	
        	Extractor ex = new Extractor(featureExtractor);
        	ex.setParams(exParams);
        	ex.setClassName(extractor.getClassName());
        	ex.setExternalPath(extractor.getExternalPath());
        	chainModels[i].addExtractor(ex);
        	
        	//chainModels[i].setSelectedIndices(combo.getSelectedIndices());//moved up
        	chainModels[i].setLabel(anno.getAnnotationLabels().get(i));
        	chainModels[i].setClassNames(classNames);
        	
        	//added 1/20/2014
        	ClassifierInfo cal =  new ClassifierInfo(classifierChoice);
        	cal.setClassName(classifier.getClassName());
        	cal.setExternalPath(classifier.getExternalPath());
        	cal.setParams(classParams);
        	
        	chainModels[i].addClassifierInfo(cal);
        	chainModels[i].setSavableClassifier( (SavableClassifier) classifierObj );
        	
        	//Removed 1/20/2014
        	
        	//chainModels[i].setClassifierName(classifierChoice);
        	//chainModels[i].setClassifierClass(classifier.getClassName());
        	//chainModels[i].setClassifierPath(classifier.getExternalPath());
        	//chainModels[i].addClassifier(classifierObj);
        	//chainModels[i].setClassParams(classParams);            
            
            if (!setProgress(50 + (i + 1) * 50 / numOfAnno)) {
                return;
            }
        }//end of loop for annotation targets
        pnlOutput.setOutput("Training complete.");
	}
	
	//Training/Testing
	private void ttRun() {		
		//read images and wrapped into DataInput instances.
 		if(trainingProblem == null || testingProblem == null) {
			pnlOutput.setOutput("Training and/or testing problem is not set.");
			return;
		}
		
	    ArrayList<String> annoLabels = trainingProblem.getAnnotations();
        HashMap<String, String> classNames = trainingProblem.getClassNames();
        int numOfAnno = annoLabels.size();
        int[][] trainingTargets = trainingProblem.getTargets();
        anno.setAnnotationLabels(annoLabels);	        
        int[][] testingTargets = testingProblem.getTargets();
        
        //feature extraction.
        if (!setProgress(30))  {
            return;
        }
        pnlOutput.setOutput("Extracting features...");
        float[][] trainingFeatures = null;
        float[][] testingFeatures = null;
        try {
        	trainingFeatures = anno.extractGivenAMethod(extractor.getClassName(), extractor.getExternalPath(), exParams, trainingProblem);
        	testingFeatures = anno.extractGivenAMethod(extractor.getClassName(), extractor.getExternalPath(), exParams, testingProblem);
        }
        catch(Exception e) {
        	pnlOutput.setOutput("ERROR: Feature extractor failed! Extractor = " + extractor.getName());
        	pnlOutput.setOutput("       Message: "+e.getMessage());
        	e.printStackTrace();
        	setProgress(0);
        	enableSave = false;
        	return;
        }        
        
        //apply feature selector and classifier
        if (!setProgress(50)) {
            return;
        }
        //trainingTestingOutput(trainingFeatures, testingFeatures, trainingTargets, testingTargets, numOfAnno);
        
        int testingLength = testingFeatures.length;

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
        	
        	//Selected features for each annotation label
        	float[][] selectedTrainingFeatures = null;
        	float[][] selectedTestingFeatures = null;
        	
            if (featureSelector.equalsIgnoreCase("None")) { //use the original feature without selection //TODO: handled in annotator
            	selectedTrainingFeatures = trainingFeatures;
            	selectedTestingFeatures = testingFeatures;
            }
            else 
            {
                pnlOutput.setOutput("Selecting features...");
            	//Supervised feature selectors need corresponding target data
                ComboFeatures combo =  null;
                try {
                	combo = anno.selectGivenAMethod(selector.getClassName(), selector.getExternalPath(), selParams, trainingFeatures, testingFeatures, trainingTargets[i], testingTargets[i]);
                }
                catch (Exception ex) {
            		pnlOutput.setOutput("ERROR: Feature selection failed! Selector = " + featureSelector);
            		pnlOutput.setOutput("       Message: "+ex.getMessage());
            		ex.printStackTrace();
            		setProgress(0);
                	enableSave = false;
            		return;
                }
                //selected features overrides the passed in original features
                selectedTrainingFeatures = combo.getTrainingFeatures();
                selectedTestingFeatures = combo.getTestingFeatures();
                
                //For dump file
                Selector sel = new Selector(featureSelector);
            	sel.setSelectedIndices(combo.getSelectedIndices());
            	//sel.setParams(selParams);//Not used in saving model
            	chainModels[i].addSelector(sel);
            }

            //pass the training and testing data to Validator
            //get rate and prediction results for testing data
            float rate = 0;
            //setGUIOutput("Classifying/Annotating ... ");
            pnlOutput.setOutput("Classifying/Annotating...");
            
            Classifier classifierObj = null;
            try {
				classifierObj = anno.getClassifierGivenName(classifier.getClassName(), classifier.getExternalPath(), classParams);
			} catch (Exception e) {
				pnlOutput.setOutput("ERROR: Classifier failed! Classifier = " + classifier.getName());
				pnlOutput.setOutput("       Message: "+e.getMessage());
				e.printStackTrace();
				setProgress(0);
				enableSave = false;
				return;
			}
            try {
            	rate = anno.classifyGivenAMethod(classifierObj, classParams, selectedTrainingFeatures, selectedTestingFeatures, trainingTargets[i], testingTargets[i], annotations[i]);
            	System.out.println("Simple mode: The rate get from classifyGivenAMethod:"+rate);
            }
            catch(Exception ex) {
        		pnlOutput.setOutput("ERROR: Classification failed! Classifier = " + classifier.getName());
        		pnlOutput.setOutput("       Message: "+ex.getMessage());
        		ex.printStackTrace();
        		setProgress(0);
            	enableSave = false;
        		return;
            }
            
            enableSave = (classifierObj instanceof SavableClassifier)? true : false;
            		
            System.out.println(rate);
            
            //Save information to dump in chain file
        	chainModels[i].setImageSet(new File(Annotator.dir).getAbsolutePath());
        	chainModels[i].setTestingSet(new File(Annotator.testdir).getAbsolutePath());
			if(imgStackSize > 1)
				chainModels[i].setImageSize(imgWidth + "x" + imgHeight + "x" + imgDepth);
			else
				chainModels[i].setImageSize(imgWidth + "x" + imgHeight);
        	chainModels[i].setMode("Training/Testing");
        	chainModels[i].setChannel(channel);
        	
        	Extractor ex = new Extractor(featureExtractor);
        	ex.setParams(exParams);
        	ex.setClassName(extractor.getClassName());
        	ex.setExternalPath(extractor.getExternalPath());
        	chainModels[i].addExtractor(ex);
        	
        	//chainModels[i].setSelectedIndices(combo.getSelectedIndices());//moved up
        	chainModels[i].setLabel(anno.getAnnotationLabels().get(i));
        	chainModels[i].setClassNames(classNames);
        	chainModels[i].setResult(rate);
        	
        	//Added 1/20/2014
        	ClassifierInfo cal =  new ClassifierInfo(classifierChoice);
        	cal.setClassName(classifier.getClassName());
        	cal.setExternalPath(classifier.getExternalPath());
        	cal.setParams(classParams);
        	
        	chainModels[i].addClassifierInfo(cal);
        	//chainModels[i].setClassifier(classifierObj);
        	
        	/* Removed 1/20/2014
        	chainModels[i].setClassifierName(classifierChoice);
        	chainModels[i].setClassifierClass(classifier.getClassName());
        	chainModels[i].setClassifierPath(classifier.getExternalPath());
        	chainModels[i].setClassifier(classifierObj);
        	chainModels[i].setClassParams(classParams);
            */ 
            
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
        }//end of loop for annotation targets 
	}
	
	//Cross validation
	private void cvRun() {
		//------ read image data from the directory ------------//
        //DataInput problem = new DataInput(Annotator.dir, Annotator.ext, channel);
		DataInput problem = trainingProblem;
		if(problem == null) {
			pnlOutput.setOutput("Problem is not set.");
			return;
		}
		
        //-----  read targets matrix (for multiple annotations, one per column) --------//
        if (!setProgress(20)) {
            return;
        }
        //int[] resArr = new int[2]; //place holder for misc results
        //java.util.ArrayList<String> annoLabels = new java.util.ArrayList<String>();
        //HashMap<String, String> classNames = new HashMap<String, String>();
        //int[][] targets = anno.readTargets(problem, Annotator.targetFile, resArr, annoLabels, classNames);
        //int numOfAnno = resArr[0];
        ArrayList<String> annoLabels = problem.getAnnotations();
        HashMap<String, String> classNames = problem.getClassNames();
        int[][] targets = problem.getTargets();
        int numOfAnno = annoLabels.size();
        
        anno.setAnnotationLabels(annoLabels);//why???
        
        //----- feature extraction -------//
        if (!setProgress(30)) {
            return;
        }
        pnlOutput.setOutput("Extracing features ... ");
        float[][] features = null;
        try {
			features = anno.extractGivenAMethod(extractor.getClassName(), extractor.getExternalPath(), exParams, problem);
		} catch (Exception e) {
			pnlOutput.setOutput("ERROR: Feature extractor failed! Extractor = " + extractor.getName());
			pnlOutput.setOutput("       Message: "+e.getMessage());
			e.printStackTrace();
			setProgress(0);
			enableSave = false;
			return;
		}
  
        //-----  output the annotation/classification results
        if (!setProgress(50)) {
            return;
        }
        
        //Apply Feature Selection and Classification in CV mode. 
        int length = features.length;
        
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

            //Selected features for each annotation label
            float[][] selectedFeatures = null;
            
            //If selector is None, use default features. Else, call the selector.
            if (featureSelector.equalsIgnoreCase("None")) {
            	selectedFeatures = features;
            }
            else {
                pnlOutput.setOutput("Selecting features ... ");
                //override the original features and num of features
                ComboFeatures combo = null;
                try {
                	combo = anno.selectGivenAMethod(selector.getClassName(), selector.getExternalPath(), selParams, features, targets[i]);
                }
                catch (Exception ex) {
            		pnlOutput.setOutput("ERROR: Feature selection failed! Selector = " + featureSelector);
            		pnlOutput.setOutput("       Message: "+ex.getMessage());
            		ex.printStackTrace();
            		setProgress(0);
                	enableSave = false;
            		return;
                }
                
                selectedFeatures = combo.getTrainingFeatures();
                
                //For dump file
                Selector sel = new Selector(featureSelector);
            	sel.setSelectedIndices(combo.getSelectedIndices());
            	//sel.setParams(selParams);//Not used in saving model
            	chainModels[i].addSelector(sel);
            }

            pnlOutput.setOutput("Classifying/Annotating ... ");
            Classifier classifierObj = null;
            try {
				classifierObj = anno.getClassifierGivenName(classifier.getClassName(), classifier.getExternalPath(), classParams);
			} catch (Exception e) {
				pnlOutput.setOutput("ERROR: Classifier failed! Classifier = " + classifier.getName());
				pnlOutput.setOutput("       Message: "+e.getMessage());
				e.printStackTrace();
				setProgress(0);
				enableSave = false;
				return;
			}
            try {
            	recograte = (new Validator(bar, start, region)).KFoldGivenAClassifier(K, selectedFeatures, targets[i], classifierObj, classParams, shuffle, results[i]);
            }
            catch(Exception ex) {
        		pnlOutput.setOutput("ERROR: Classification failed! Classifier = " + classifier.getName());
            	ex.printStackTrace();
        		setProgress(0);
            	enableSave = false;
        		return;
            }
            
            enableSave = (classifierObj instanceof SavableClassifier)? true : false;
            
            //output results to GUI and file
            System.out.println("rate for annotation target " + i + ": " + recograte[K]);
            pnlOutput.setOutput("Recog Rate for " + anno.getAnnotationLabels().get(i) + ": " + recograte[K]);
            
            //Save information to dump in chain file
            chainModels[i].setImageSet(new File(Annotator.dir).getAbsolutePath());
			if(imgStackSize > 1)
				chainModels[i].setImageSize(imgWidth + "x" + imgHeight + "x" + imgDepth);
			else
				chainModels[i].setImageSize(imgWidth + "x" + imgHeight);
        	chainModels[i].setMode("Cross Validation. Fold: " + Annotator.fold);
        	chainModels[i].setChannel(channel);
        	
        	Extractor ex = new Extractor(featureExtractor);
        	ex.setParams(exParams);
        	ex.setClassName(extractor.getClassName());
        	ex.setExternalPath(extractor.getExternalPath());
        	chainModels[i].addExtractor(ex);
        	
        	//chainModels[i].setSelectedIndices(combo.getSelectedIndices());//moved up
        	chainModels[i].setLabel(anno.getAnnotationLabels().get(i));
        	chainModels[i].setClassNames(classNames);
        	chainModels[i].setResult(recograte[K]);
        	
        	//Added 1/20/2014
        	ClassifierInfo cal =  new ClassifierInfo(classifierChoice);
        	cal.setClassName(classifier.getClassName());
        	cal.setExternalPath(classifier.getExternalPath());
        	cal.setParams(classParams);
        	
        	//Change functions to overload version:
        	chainModels[i].addClassifierInfo(cal);
        	//chainModels[i].setClassifier(classifierObj);
        	
        	/* Removed 1/20/2014
        	chainModels[i].setClassifierName(classifierChoice);
        	chainModels[i].setClassifierClass(classifier.getClassName());
        	chainModels[i].setClassifierPath(classifier.getExternalPath());
        	chainModels[i].setClassifier(classifierObj);
        	chainModels[i].setClassParams(classParams);
            */
            //Display result
            ResultPanel pnlResult = new ResultPanel(tabPane);
            tabPane.addTab("Result - " + anno.getAnnotationLabels().get(i), pnlResult);
            pnlResult.showResult(recograte[K], targets[i], results[i]);
            if(!Annotator.fold.equals("LOO"))
            	pnlResult.showKFoldChart(recograte);
            
            //Add panel with title label and close button to the tab
            tabPane.setTabComponentAt(tabPane.getTabCount() - 1, 
                    new ButtonTabComponent("Result - " + anno.getAnnotationLabels().get(i), tabPane));
        } //end of loop for annotation targets
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
					component.setPreferredSize(new java.awt.Dimension(60, 25));
				}
				else {
					component = new JComboBox(param.getParamDomain());
					((JComboBox)component).setSelectedItem(param.getParamDefault());
				}
				
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
	
	public JProgressBar getBar()
	{
	    return bar;	
	}
}
