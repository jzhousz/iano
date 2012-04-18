package annotool;

import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import javax.swing.border.*;
import javax.swing.*;

import annotool.gui.AnnOutputPanel;
import annotool.gui.AnnTablePanel;

import java.awt.event.*;

public class AnnControlPanel extends JPanel implements ActionListener {

	String[] channels = {  "red (channel 1)", "green (channel 2)", "blue (channel 3)" };
	String[] channelInputs = {  "r", "g", "b" };//actual input to algorithm
	String[] folds = {  "LOO (Leave One Out)", "5 fold CV", "10 fold CV" };
	String[] foldInputs = { "LOO" , "5", "10" };//actual input to algorithm

	//Names of the methods can be rearranged or removed, but not modified, due to dependency in actionPerformed.
	String[] extractors = {  "2D discrete wavelet transform", "None", "Principal Components" };
	//for a 3Dimage of nx*ny*nz: 
	// Number of features after partial frames approach: nx*ny
	//							light approach: nx*ny + nx*nz + ny*nz
	//                          full approach: nx*ny*nz
	//String[] extractors3d = {  "3D wavelet (partial frames)", "3D wavelet (light)", "3D wavelet (full)" }; //01/26/09: add 3D
	String[] extractors3d = {  "3D wavelet partial frames", "3D wavelet all frames (slow)" }; //01/26/09: add 3D
	//String[] extractors3dSimpleStrs = {"PARTIAL3D", "LIGHT3D"};

	String[] selectors = {"None", "mRMR-MIQ", "mRMR-MID", "Information Gain"}; //081007: add MID
	//String[] selectors = {"None", "mRMR-MIQ", "mRMR-MID"}; 
	
	//String[] classifiers = { "Support Vector Machine", "Discrimant Analysis", "Tree (J48)", "Naive Bayes", "Random Forest", "Nearest Neighbor"};
	static String[] classifiers = { "Compare All", "Support Vector Machine", "Tree (J48)", "Naive Bayes", "Random Forest", "Nearest Neighbor"};
	//simplified strings for classifiers used in code (Annotator.java). It should be modified together with Classifiers.
	static String[] classifierSimpleStrs = { "Compare All", "SVM", "W_Tree", "W_NaiveBayes", "W_RandomForest", "W_NearestNeighbor"};
		
	String[] outputs = { "Classification Accuracy", "Annotation Table"};

	JLabel  chanL = new JLabel("Channel");
	JLabel  feL = new JLabel("2D Feature Extractor");
	JLabel  fe3dL = new JLabel("3D Feature Extractor");
	JLabel  fsL = new JLabel ("Feature Selector");
	JLabel  classL = new JLabel ("Classifier");
	//JLabel  outputL = new JLabel ("Output");

	JComboBox fexBox = new JComboBox(extractors);
	JComboBox fex3dBox = new JComboBox(extractors3d);
	JComboBox fslBox = new JComboBox(selectors);
	JComboBox classifierBox = new JComboBox(classifiers);
	//JComboBox outputBox = new JComboBox(outputs);
	JButton goButton = new JButton("Start Annotation");
	JButton cancelButton = new JButton("Cancel");

	JProgressBar progressBar = new JProgressBar(0, 100);

	AnnOutputPanel outputPanel = new AnnOutputPanel();
	AnnTablePanel  tablePanel;  
	AnnotatorGUI   gui;
	Annotator      currentAnnotator;
	
	JRadioButton redButton = new JRadioButton(channels[0]); 
	JRadioButton greenButton  = new JRadioButton(channels[1]); 
	JRadioButton blueButton  = new JRadioButton(channels[2]);
	JRadioButton looButton = new JRadioButton(folds[0]); 
	JRadioButton fold2Button = new JRadioButton(folds[1]); 
	JRadioButton fold3Button = new JRadioButton(folds[2]);
	
	
	public AnnControlPanel(AnnotatorGUI gui)
	{
		this.gui = gui;
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		tablePanel = new AnnTablePanel(gui);
		thingsEnabled(false);
		cancelButton.setEnabled(false);

		//left: parameter  //right: imagePanel
		JPanel  leftPanel =  AnnotatorGUI.createVerticalPanel(true);
		Border loweredBorder = new CompoundBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED), 
				new EmptyBorder(5,5,5,5));
		leftPanel.setBorder(loweredBorder);
		tablePanel.setBorder(loweredBorder);
	
		//left down panel
		JPanel  ldPanel =  new JPanel();
		ldPanel.setLayout(new GridLayout(4, 3, 5, 5));
		ldPanel.add(feL);
		ldPanel.add(fexBox);
		ldPanel.add(fe3dL);
		ldPanel.add(fex3dBox);
		ldPanel.add(fsL);
		ldPanel.add(fslBox);
		ldPanel.add(classL);
		ldPanel.add(classifierBox);
		//ldPanel.add(outputL);
		//ldPanel.add(outputBox);
		ldPanel.setBorder(new CompoundBorder(new TitledBorder(null, "components", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 
		//ldPanel.setBackground(java.awt.Color.white);

		//left control components
		//add radio button for channel and fold selection
		greenButton.setSelected(true);
		ButtonGroup group = new ButtonGroup();
		group.add(redButton);
	    group.add(greenButton);
	    group.add(blueButton);
        JPanel radioPanel = new JPanel(new GridLayout(0, 1));
	    radioPanel.add(redButton);
	    radioPanel.add(greenButton);
	    radioPanel.add(blueButton);
		radioPanel.setBorder(new CompoundBorder(new TitledBorder(null,"channels", 
		TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 
	        
		looButton.setSelected(true);
		//looButton.setBackground(java.awt.Color.white);
		//fold2Button.setBackground(java.awt.Color.white);
		//fold3Button.setBackground(java.awt.Color.white);
		ButtonGroup foldgroup = new ButtonGroup();
		foldgroup.add(looButton);
	    foldgroup.add(fold2Button);
	    foldgroup.add(fold3Button);
        JPanel foldPanel = new JPanel(new GridLayout(0, 1));
	    foldPanel.add(looButton);
	    foldPanel.add(fold2Button);
	    foldPanel.add(fold3Button);
		foldPanel.setBorder(new CompoundBorder(new TitledBorder(null,"fold in cross validation", 
		TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 
		//radioPanel.setBackground(java.awt.Color.white);
		//foldPanel.setBackground(java.awt.Color.white);
		//radioPanel.setForeground(java.awt.Color.white);


		JPanel selectionPanel = new JPanel();
		selectionPanel.setLayout(new GridLayout(1,2));
		selectionPanel.add(foldPanel);
		selectionPanel.add(radioPanel);
		
		
		JPanel  lbPanel =  new JPanel();
		lbPanel.add(goButton);
		lbPanel.add(cancelButton);
		lbPanel.setBorder(new CompoundBorder(new TitledBorder(null,"control", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 

		//build left panel
		leftPanel.add(ldPanel);
		leftPanel.add(selectionPanel);
		leftPanel.add(lbPanel);
		leftPanel.add(outputPanel);
		leftPanel.add(progressBar);

		setLayout(new BorderLayout());
		add(leftPanel, BorderLayout.WEST);
		add(tablePanel, BorderLayout.CENTER);

		//action listeners ...
		addListeners();
	}	

	public JPanel createHorizontalPanel(boolean threeD) 
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.setAlignmentY(TOP_ALIGNMENT);
		p.setAlignmentX(LEFT_ALIGNMENT);
		if(threeD) {
			Border loweredBorder = new CompoundBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED), 
					new EmptyBorder(5,5,5,5));
			p.setBorder(loweredBorder);
		}
		return p;
	}


	private void addListeners()
	{
		goButton.addActionListener(this); //get all values from all tabs!
		cancelButton.addActionListener(this); //get all values from all tabs!

		fexBox.addActionListener(this);  //if "None", gray extractor tab, otherwise, may add?
		fslBox.addActionListener(this);  //if "None", gray selector tab?
		
	    redButton.addActionListener(this);
	    greenButton.addActionListener(this);
	    blueButton.addActionListener(this);
	    looButton.addActionListener(this);
	    fold2Button.addActionListener(this);
	    fold3Button.addActionListener(this);
	}

	//call back event handlers
	//default choices should be the same as those defined in Annotator.
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == redButton)
			Annotator.channel = channelInputs[0];
		else if (e.getSource() == greenButton)
			Annotator.channel = channelInputs[1];
		else if (e.getSource() == blueButton)
			Annotator.channel = channelInputs[2];
		
		if (e.getSource() == looButton)
			Annotator.fold = foldInputs[0];
		else if (e.getSource() == fold2Button)
			Annotator.fold = foldInputs[1];
		else if (e.getSource() == fold3Button)
			Annotator.fold = foldInputs[2];

		else if (e.getSource() == goButton)
			startWorking();

		//To Be Added: automatic collapsing or disabling unnecessary tabs
		//else  if (e.getSource() == fexBox) ...		
		//   else  if (e.getSource() == fslBox)
		else if (e.getSource() == cancelButton)
			stopWorking();
	}


	private void startWorking()
	{
		//get parameters
		getParametersFromGUI();

		goButton.setEnabled(false);
		cancelButton.setEnabled(true);
		//outputPanel.clearOutput();
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		//Annotator object contains mostly static (class) variables. I should create a Singleton here!!
		currentAnnotator = new Annotator(gui);
		//this will start a separate thread.
		currentAnnotator.startAnnotation();	

	}

	private void stopWorking()
	{   //does not seem to respond 05212010
		System.out.println("Cancel button clicked.");
		currentAnnotator.stopAnnotation();
		//goButton.setEnabled(true);
		//cancelButton.setEnabled(false);

	}
	
	private void getParametersFromGUI()
	{
		if(fexBox.isEnabled())
			Annotator.featureExtractor = (String) fexBox.getSelectedItem();
		else if(fex3dBox.isEnabled())
			Annotator.featureExtractor = (String) fex3dBox.getSelectedItem();

		Annotator.featureSelector = (String) fslBox.getSelectedItem();
		if (Annotator.featureExtractor.equals("2D discrete wavelet transform"))
			Annotator.featureExtractor = "HAAR"; 
		else if (Annotator.featureExtractor.equals("3D wavelet partial frames"))
			Annotator.featureExtractor = "PARTIAL3D";
		else if (Annotator.featureExtractor.equals("3D wavelet all frames (slow)"))
  	    	Annotator.featureExtractor = "LIGHT3D";

		Annotator.classifierChoice = classifierSimpleStrs[classifierBox.getSelectedIndex()]; 

		//From Other Tabs
		Annotator.featureNum = AnnotatorGUI.numFeatureField.getText().trim();
		//public static String selectorType = System.getProperty("mRMRType", DEFAULT_MRMRTYPE);
		Annotator.svmpara = AnnotatorGUI.svmParaField.getText().trim();
		Annotator.waveletLevel = AnnotatorGUI.levelField.getText().trim();
		AnnROIAnnotator.roidim = AnnotatorGUI.roiDimField.getText().trim();
		AnnROIAnnotator.roiinc = AnnotatorGUI.roiIncField.getText().trim();
		
		//other stuff
		//public static String debugFlag =  System.getProperty("debug", DEFAULT_DEBUG);
		//public static String discreteFlag = System.getProperty("discreteflag",DEFAULT_DISCRETE);
		//public static String shuffleFlag = System.getProperty("shuffleflag",DEFAULT_SHUFFLE);
	}

	//get the table panel to be used by others for displaying images
	public AnnTablePanel getTablePanel()
	{
		return tablePanel;
	}

	public AnnOutputPanel getOutputPanel()
	{
		return outputPanel;
	}

	public void channelEnabled(boolean flag)
	{
		redButton.setEnabled(flag);
		greenButton.setEnabled(flag);
		blueButton.setEnabled(flag);
	}

	public void threeDEnabled(boolean flag)
	{
		//System.out.println(flag);
		feL.setEnabled(!flag);
		fe3dL.setEnabled(flag);
		fexBox.setEnabled(!flag);
		fex3dBox.setEnabled(flag);
		
	}

	public void goEnabled(boolean flag)
	{
		goButton.setEnabled(flag);
	}
	
	public void foldEnabled(boolean flag)
	{
		looButton.setEnabled(flag);
		fold2Button.setEnabled(flag);
		fold3Button.setEnabled(flag);
	}

	public void thingsEnabled(boolean flag)
	{ 
		channelEnabled(flag);
		foldEnabled(flag);
		goEnabled(flag);
		cancelButton.setEnabled(!flag);
		
		feL.setEnabled(flag);
		fe3dL.setEnabled(flag);
		fsL.setEnabled(flag);
		classL.setEnabled(flag);

		fexBox.setEnabled(flag);
		fex3dBox.setEnabled(flag);
		fslBox.setEnabled(flag);
		classifierBox.setEnabled(flag);
		
		outputPanel.clearEnabled(flag);
	}
	
	public JButton getGoButton()
	{
		return goButton;
	}

	public JButton getCancelButton()
	{
		return cancelButton;
	}

	public JProgressBar getBar()
	{
		return progressBar;
	}


}