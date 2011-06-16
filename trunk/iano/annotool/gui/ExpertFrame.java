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
import annotool.classify.Validator;
import annotool.classify.WekaClassifiers;
import annotool.io.AlgoXMLParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import annotool.io.Algorithm;
import annotool.io.DataInput;
import annotool.io.Parameter;
import annotool.AnnControlPanel;
import annotool.Annotation;
import annotool.Annotator;
import annotool.ComboFeatures;
import annotool.AnnVisualPanel;
import annotool.AnnOutputPanel;

public class ExpertFrame extends JFrame implements ActionListener, ItemListener, Runnable
{
	private JTabbedPane tabPane;
	private JPanel pnlMain,
				   pnlAlgo,
				   pnlExt, pnlSel, pnlClass,
				   pnlExtMain, pnlSelMain, pnlClassMain,
				   pnlExtDesc, pnlSelDesc, pnlClassDesc,
				   pnlExtParam, pnlSelParam, pnlClassParam,
				   pnlButton;
	private JButton btnRun;
	
	private JLabel lbExtractor, lbSelector, lbClassifier;
	private JComboBox cbExtractor, cbSelector, cbClassifier;
	
	private String channel;
	
	int tabNumber = 1; //Initial number of tabs
	
	//To keep track of dynamically added components
	HashMap<String, JComponent> exParamControls = null;			//For extractor parameters
	HashMap<String, JComponent> selParamControls = null;		//For selector parameters
	HashMap<String, JComponent> classParamControls = null;		//For classifier parameters
	
	AnnOutputPanel pnlOutput = null;
	JProgressBar bar = null;
	
	private Thread thread = null;
	private boolean isRunning;
	
	public ExpertFrame(String arg0, boolean is3D, String channel)
	{
		super(arg0);
		this.channel = channel;
		
		pnlMain = new JPanel();
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
		pnlMain.setPreferredSize(new java.awt.Dimension(540, 750));
		pnlMain.setBorder(new EmptyBorder(10, 10, 10, 10));
		pnlMain.setAlignmentY(TOP_ALIGNMENT);
		pnlMain.setAlignmentX(LEFT_ALIGNMENT);
		//this.add(pnlMain, BorderLayout.WEST);
		tabPane = new JTabbedPane();
		tabPane.addTab("Algorithms", pnlMain);
		this.add(tabPane);
		
		//Algorithm selector part
		pnlAlgo = new JPanel(new GridLayout(3, 1));
		//pnlAlgo.setLayout(new BoxLayout(pnlAlgo, BoxLayout.Y_AXIS));
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
		
		pnlButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlButton.add(btnRun);
		
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
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btnRun)
		{			
			if (thread == null) 
			{
	            thread = new Thread(this);
	            isRunning = true;
	            thread.start();
	        }
	    }
	}
	public void itemStateChanged(ItemEvent e)
	{
		if(e.getSource() == cbExtractor && e.getStateChange() == 1)
		{		
			buildExParameterPanel();
		}
		if(e.getSource() == cbSelector && e.getStateChange() == 1)
		{		
			buildSelParameterPanel();
		}
		if(e.getSource() == cbClassifier && e.getStateChange() == 1)
		{		
			buildClassParameterPanel();
		}
	}
	private void buildExParameterPanel()
	{
		if(pnlExtParam == null)
		{
			pnlExtParam = new JPanel(new FlowLayout(FlowLayout.LEFT));
			pnlExtParam.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			pnlExt.add(pnlExtParam, BorderLayout.SOUTH);
		}
		if(pnlExtDesc == null)
		{
			pnlExtDesc = new JPanel(new FlowLayout(FlowLayout.LEFT));
			pnlExtDesc.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			pnlExt.add(pnlExtDesc, BorderLayout.CENTER);
		}
		//Remove previous components (if any) from parameter and description panels
		//TODO: dispose components inside the panel first		
		pnlExtDesc.removeAll();
		pnlExtParam.removeAll();
		
		//Get the currently selected extractor
		Algorithm al = (Algorithm)cbExtractor.getSelectedItem();
		
		//Get parameters for the algorithm
		ArrayList<Parameter> paramList = al.getParam();
		
		exParamControls = new HashMap<String, JComponent>();
		
		JLabel lbDesc = new JLabel(al.getDescription());
		pnlExtDesc.add(lbDesc);
		
		for(Parameter param : paramList)
		{			
			if(param.getParamType().equals("Boolean"))
			{
				JCheckBox cb = new JCheckBox(param.getParamName());
				pnlExtParam.add(cb);
				
				//Put component in hashmap to access the value later
				exParamControls.put(param.getParamName(), cb);
			}
			else if(param.getParamType().equals("Integer"))
			{
				JLabel lb = new JLabel(param.getParamName());
				
				SpinnerNumberModel snm = new SpinnerNumberModel();
				if(param.getParamMax() != null)
					snm.setMaximum(Integer.parseInt(param.getParamMax()));
				if(param.getParamMin() != null)
					snm.setMinimum(Integer.parseInt(param.getParamMin()));
				if(param.getParamDefault() != null)
					snm.setValue(Integer.parseInt(param.getParamDefault()));
				JSpinner sp = new JSpinner(snm);
				
				pnlExtParam.add(lb);
				pnlExtParam.add(sp);
				
				//Put component in hashmap to access the value later
				exParamControls.put(param.getParamName(), sp);
			}
			else if(param.getParamType().equals("String") || param.getParamType().equals("Real"))
			{
				JLabel lb = new JLabel(param.getParamName());
				JTextField tf = new JTextField(param.getParamDefault());
				tf.setPreferredSize(new java.awt.Dimension(50, 30));
				
				pnlExtParam.add(lb);
				pnlExtParam.add(tf);
				
				//Put component in hashmap to access the value later
				exParamControls.put(param.getParamName(), tf);
			}
		}
		pnlExtParam.repaint();
		pnlExtDesc.repaint();
		this.pack();
	}
	private void buildSelParameterPanel()
	{
		if(pnlSelParam == null)
		{
			pnlSelParam = new JPanel(new FlowLayout(FlowLayout.LEFT));
			pnlSelParam.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			pnlSel.add(pnlSelParam, BorderLayout.SOUTH);
		}
		if(pnlSelDesc == null)
		{
			pnlSelDesc = new JPanel(new FlowLayout(FlowLayout.LEFT));
			pnlSelDesc.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			pnlSel.add(pnlSelParam, BorderLayout.CENTER);
		}
		//Remove previous components (if any) from parameter and description panels
		//TODO: dispose components inside the panel first		
		pnlSelDesc.removeAll();
		pnlSelParam.removeAll();
		
		//Get the currently selected extractor
		Algorithm al = (Algorithm)cbSelector.getSelectedItem();
		
		//Get parameters for the algorithm
		ArrayList<Parameter> paramList = al.getParam();
		
		selParamControls = new HashMap<String, JComponent>();
		
		JLabel lbDesc = new JLabel(al.getDescription());
		pnlSelDesc.add(lbDesc);
		
		for(Parameter param : paramList)
		{			
			if(param.getParamType().equals("Boolean"))
			{
				JCheckBox cb = new JCheckBox(param.getParamName());
				pnlSelParam.add(cb);
				
				//Put component in hashmap to access the value later
				selParamControls.put(param.getParamName(), cb);
			}
			else if(param.getParamType().equals("Integer"))
			{
				JLabel lb = new JLabel(param.getParamName());
				
				SpinnerNumberModel snm = new SpinnerNumberModel();
				if(param.getParamMax() != null)
					snm.setMaximum(Integer.parseInt(param.getParamMax()));
				if(param.getParamMin() != null)
					snm.setMinimum(Integer.parseInt(param.getParamMin()));
				if(param.getParamDefault() != null)
					snm.setValue(Integer.parseInt(param.getParamDefault()));
				JSpinner sp = new JSpinner(snm);
				
				pnlSelParam.add(lb);
				pnlSelParam.add(sp);
				
				//Put component in hashmap to access the value later
				selParamControls.put(param.getParamName(), sp);
			}
			else if(param.getParamType().equals("String") || param.getParamType().equals("Real"))
			{
				JLabel lb = new JLabel(param.getParamName());
				JTextField tf = new JTextField(param.getParamDefault());
				tf.setPreferredSize(new java.awt.Dimension(50, 30));
				
				pnlSelParam.add(lb);
				pnlSelParam.add(tf);
				
				//Put component in hashmap to access the value later
				selParamControls.put(param.getParamName(), tf);
			}
		}
		pnlSelParam.repaint();
		pnlSelDesc.repaint();
		this.pack();
	}
	private void buildClassParameterPanel()
	{
		if(pnlClassParam == null)
		{
			pnlClassParam = new JPanel(new FlowLayout(FlowLayout.LEFT));
			pnlClassParam.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			pnlClass.add(pnlClassParam, BorderLayout.SOUTH);
		}
		if(pnlClassDesc == null)
		{
			pnlClassDesc = new JPanel(new FlowLayout(FlowLayout.LEFT));
			pnlClassDesc.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			pnlClass.add(pnlClassDesc, BorderLayout.CENTER);
		}
		//Remove previous components (if any) from parameter and description panels
		//TODO: dispose components inside the panel first		
		pnlClassDesc.removeAll();
		pnlClassParam.removeAll();
		
		//Get the currently selected extractor
		Algorithm al = (Algorithm)cbClassifier.getSelectedItem();
		
		//Get parameters for the algorithm
		ArrayList<Parameter> paramList = al.getParam();
		
		classParamControls = new HashMap<String, JComponent>();
		
		JLabel lbDesc = new JLabel(al.getDescription());
		pnlClassDesc.add(lbDesc);
		
		for(Parameter param : paramList)
		{			
			if(param.getParamType().equals("Boolean"))
			{
				JCheckBox cb = new JCheckBox(param.getParamName());
				pnlClassParam.add(cb);
				
				//Put component in hashmap to access the value later
				classParamControls.put(param.getParamName(), cb);
			}
			else if(param.getParamType().equals("Integer"))
			{
				JLabel lb = new JLabel(param.getParamName());
				
				SpinnerNumberModel snm = new SpinnerNumberModel();
				if(param.getParamMax() != null)
					snm.setMaximum(Integer.parseInt(param.getParamMax()));
				if(param.getParamMin() != null)
					snm.setMinimum(Integer.parseInt(param.getParamMin()));
				if(param.getParamDefault() != null)
					snm.setValue(Integer.parseInt(param.getParamDefault()));
				JSpinner sp = new JSpinner(snm);
				
				pnlClassParam.add(lb);
				pnlClassParam.add(sp);
				
				//Put component in hashmap to access the value later
				classParamControls.put(param.getParamName(), sp);
			}
			else if(param.getParamType().equals("String") || param.getParamType().equals("Real"))
			{
				JLabel lb = new JLabel(param.getParamName());
				JTextField tf = new JTextField(param.getParamDefault());
				tf.setPreferredSize(new java.awt.Dimension(50, 30));
				
				pnlClassParam.add(lb);
				pnlClassParam.add(tf);
				
				//Put component in hashmap to access the value later
				classParamControls.put(param.getParamName(), tf);
			}
		}
		pnlClassDesc.repaint();
		pnlClassParam.repaint();
		this.pack();
	}
	public void run()
	{
		if(Annotator.output.equals(Annotator.OUTPUT_CHOICES[0]))
			ttRun();
		else if(Annotator.output.equals(Annotator.OUTPUT_CHOICES[1]))
			cvRun();
	}
	
	//Training/Testing
	private void ttRun()
	{
		Algorithm extractor = (Algorithm)cbExtractor.getSelectedItem();			
		Algorithm selector = (Algorithm)cbSelector.getSelectedItem();			
		Algorithm classifier = (Algorithm)cbClassifier.getSelectedItem();
		
		Annotator anno = new Annotator();
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
        if (!setProgress(30)) 
        {
            return;
        }
        
        pnlOutput.setOutput("Extracting features...");
        
        HashMap<String, String> exParams = new HashMap<String, String>();
        HashMap<String, String> selParams = new HashMap<String, String>();
        HashMap<String, String> classParams = new HashMap<String, String>();
        
        String featureExtractor = extractor.getName();
        String featureSelector = selector.getName();
        String classifierChoice = classifier.getName();
        
        //Build parameters from the controls
        //TODO: Validate input data
        String value = null;
        //Parameters for extractor
        for(Parameter param : extractor.getParam())
        {
        	JComponent control = exParamControls.get(param.getParamName());
        	if(control instanceof JTextField)
        		value = ((JTextField) control).getText().trim();
        	else if(control instanceof JCheckBox)
        		value = ((JCheckBox)control).isSelected() ? "1" : "0";
        	else if(control instanceof JSpinner)
        		value = ((JSpinner)control).getValue().toString();
        	exParams.put(param.getParamName(), value);
        }
        value = null;
        //Parameters for selector
        for(Parameter param : selector.getParam())
        {
        	JComponent control = selParamControls.get(param.getParamName());
        	if(control instanceof JTextField)
        		value = ((JTextField) control).getText().trim();
        	else if(control instanceof JCheckBox)
        		value = ((JCheckBox)control).isSelected() ? "1" : "0";
        	else if(control instanceof JSpinner)
        		value = ((JSpinner)control).getValue().toString();
        	selParams.put(param.getParamName(), value);
        }
        value = null;
        //Parameters for classifier
        for(Parameter param : classifier.getParam())
        {
        	JComponent control = classParamControls.get(param.getParamName());
        	if(control instanceof JTextField)
        		value = ((JTextField) control).getText().trim();
        	else if(control instanceof JCheckBox)
        		value = ((JCheckBox)control).isSelected() ? "1" : "0";
        	else if(control instanceof JSpinner)
        		value = ((JSpinner)control).getValue().toString();
        	classParams.put(param.getParamName(), value);
        }
        
        
        float[][] trainingFeatures = anno.extractGivenAMethod(featureExtractor, exParams, trainingProblem);
        float[][] testingFeatures = anno.extractGivenAMethod(featureExtractor, exParams, testingProblem);
        //clear data memory
        trainingProblem.setDataNull();
        testingProblem.setDataNull();

        //apply feature selector and classifier
        if (!setProgress(50)) 
        {
            return;
        }
        //trainingTestingOutput(trainingFeatures, testingFeatures, trainingTargets, testingTargets, numOfAnno);
        
        int trainingLength = trainingFeatures.length;
        int testingLength = testingFeatures.length;
        int numoffeatures;

        //initialize structure to store annotation results
        Annotation[][] annotations = new Annotation[numOfAnno][testingLength];
        for (int i = 0; i < numOfAnno; i++) {
            for (int j = 0; j < testingLength; j++) {
                annotations[i][j] = new Annotation();
            }
        }
        
        //loop for each annotation target (one image may have multiple labels)
        for (int i = 0; i < numOfAnno; i++) {
            if (featureSelector.equalsIgnoreCase("None")) //use the original feature without selection -- overwrite numoffeatures value
            {
                numoffeatures = trainingFeatures[0].length;
            }
            else 
            {
                pnlOutput.setOutput("Selecting featurs...");
            	//Supervised feature selectors need corresponding target data
                ComboFeatures combo = anno.selectGivenAMethod(featureSelector, selParams, trainingFeatures, testingFeatures, trainingTargets[i], testingTargets[i]);
                //selected features overrides the passed in original features
                trainingFeatures = combo.getTrainingFeatures();
                testingFeatures = combo.getTestingFeatures();
                numoffeatures = trainingFeatures[0].length;
            }

            //pass the training and testing data to Validator
            //get rate and prediction results for testing data
            float rate = 0;
            //setGUIOutput("Classifying/Annotating ... ");
            pnlOutput.setOutput("Classifying/Annotating...");

            rate = anno.classifyGivenAMethod(classifierChoice, classParams, trainingFeatures, testingFeatures, trainingTargets[i], testingTargets[i], annotations[i]);
                
            System.out.println(rate);
                
            AnnVisualPanel pnlVisual = new AnnVisualPanel(tabPane, tabNumber++);
            tabPane.addTab(anno.getAnnotationLabels().get(i), null, pnlVisual, "Result Visualization");
            pnlVisual.showResult(rate, testingTargets[i], annotations[i]);
                
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
        
        //Display confusion matrix
        //TODO
        ConfusionPanel pnlConfusion = new ConfusionPanel(tabPane, tabNumber++);
        tabPane.addTab("Confusion Matrix", pnlConfusion);
        pnlConfusion.showMatrix(testingTargets, annotations);
	}
	
	//Cross validation
	private void cvRun()
	{
		Algorithm extractor = (Algorithm)cbExtractor.getSelectedItem();			
		Algorithm selector = (Algorithm)cbSelector.getSelectedItem();			
		Algorithm classifier = (Algorithm)cbClassifier.getSelectedItem();
		
		HashMap<String, String> exParams = new HashMap<String, String>();
        HashMap<String, String> selParams = new HashMap<String, String>();
        HashMap<String, String> classParams = new HashMap<String, String>();
        
        String featureExtractor = extractor.getName();
        String featureSelector = selector.getName();
        String classifierChoice = classifier.getName();
        
        //Build parameters from the controls
        //TODO: Validate input data
        String value = null;
        //Parameters for extractor
        for(Parameter param : extractor.getParam())
        {
        	JComponent control = exParamControls.get(param.getParamName());
        	if(control instanceof JTextField)
        		value = ((JTextField) control).getText().trim();
        	else if(control instanceof JCheckBox)
        		value = ((JCheckBox)control).isSelected() ? "1" : "0";
        	else if(control instanceof JSpinner)
        		value = ((JSpinner)control).getValue().toString();
        	exParams.put(param.getParamName(), value);
        }
        value = null;
        //Parameters for selector
        for(Parameter param : selector.getParam())
        {
        	JComponent control = selParamControls.get(param.getParamName());
        	if(control instanceof JTextField)
        		value = ((JTextField) control).getText().trim();
        	else if(control instanceof JCheckBox)
        		value = ((JCheckBox)control).isSelected() ? "1" : "0";
        	else if(control instanceof JSpinner)
        		value = ((JSpinner)control).getValue().toString();
        	selParams.put(param.getParamName(), value);
        }
        value = null;
        //Parameters for classifier
        for(Parameter param : classifier.getParam())
        {
        	JComponent control = classParamControls.get(param.getParamName());
        	if(control instanceof JTextField)
        		value = ((JTextField) control).getText().trim();
        	else if(control instanceof JCheckBox)
        		value = ((JCheckBox)control).isSelected() ? "1" : "0";
        	else if(control instanceof JSpinner)
        		value = ((JSpinner)control).getValue().toString();
        	classParams.put(param.getParamName(), value);
        }
		
		Annotator anno = new Annotator();
		
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

        if (Annotator.fileFlag.equals("true")) {
            /*try {
                outputfile = new java.io.BufferedWriter(new java.io.FileWriter("output"));
                ;
                outputfile.write("Outputs:\n");
                outputfile.flush();
            }
            catch (Exception e) {
                System.out.println("Output File Cann't Be Generated.");
            }*/
        }
        
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
        
        //loop for each annotation target
        for (int i = 0; i < numOfAnno; i++) {
            float recograte = 0;
            int start = 50 + i * 50 / numOfAnno;
            int region = 50 / numOfAnno;

            //If selector is None, use default numoffeatures. Else, call the selector.
            if (!featureSelector.equalsIgnoreCase("None")) {
                pnlOutput.setOutput("Selecting features ... ");
                //override the original features and num of features
                features = anno.selectGivenAMethod(featureSelector, selParams, features, targets[i]);
                numoffeatures = features[0].length;
            }

            pnlOutput.setOutput("Classifying/Annotating ... ");
            recograte = (new Validator(bar, start, region)).KFoldGivenAClassifier(K, features, targets[i], classifierChoice, classParams, shuffle, results[i]);
            
            //output results to GUI and file
            System.out.println("rate for annotation target " + i + ": " + recograte);
            pnlOutput.setOutput("Recog Rate for " + anno.getAnnotationLabels().get(i) + ": " + recograte);
            
            AnnVisualPanel pnlVisual = new AnnVisualPanel(tabPane, tabNumber++);
            tabPane.addTab(anno.getAnnotationLabels().get(i), null, pnlVisual, "Result Visualization");
            pnlVisual.showResult(recograte, targets[i], results[i]);
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
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run() {
                        bar.setValue(0);
                    }
                });
            }
            pnlOutput.setOutput("Annotation process cancelled by user.");
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
	
	//Temporary main method for testing GUI
	public static void main(String[] args) 
	{
		ExpertFrame ef = new ExpertFrame("Expert Mode", false, "g");
		ef.pack();
		ef.setVisible(true);
		ef.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
