package annotool.gui;

import javax.swing.*;

import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.*;

import annotool.classify.Validator;
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

public class AutoCompFrame extends JFrame implements ActionListener, ItemListener, Runnable {
	private JTabbedPane tabPane;
	private JPanel pnlMainOuter,
				   pnlMain,
				   pnlAlgo,
				   pnlExt, pnlSel, pnlClass,
				   pnlExtMain, pnlSelMain, pnlClassMain,
				   pnlExtBtn, pnlSelBtn, pnlClassBtn,
				   pnlButton;
	
	private ChainPanel pnlChain;
	
	private JButton btnRun, btnSaveModel, btnAnnotate,
					btnAddEx, btnAddSel, btnAddClass;
	
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
	
	public AutoCompFrame(String arg0, boolean is3D, String channel) {
		super(arg0);
		this.channel = channel;
		fileChooser = new JFileChooser();
		
		pnlMain = new JPanel();
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
		pnlMain.setPreferredSize(new java.awt.Dimension(540, 670));
		pnlMain.setBorder(new EmptyBorder(10, 10, 10, 10));
		pnlMain.setAlignmentY(TOP_ALIGNMENT);
		pnlMain.setAlignmentX(LEFT_ALIGNMENT);
		//this.add(pnlMain, BorderLayout.WEST);
		
		pnlChain = new ChainPanel();
		
		pnlMainOuter = new JPanel(new BorderLayout());
		pnlMainOuter.add(pnlMain, BorderLayout.WEST);
		pnlMainOuter.add(pnlChain, BorderLayout.CENTER);
		
		tabPane = new JTabbedPane();
		tabPane.addTab("Algorithms", pnlMainOuter);
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
		
		//Buttons to add algorithms to chain
		btnAddEx = new JButton("Add");
		btnAddSel = new JButton("Add");
		btnAddClass = new JButton("Add");
		btnAddEx.addActionListener(this);
		btnAddSel.addActionListener(this);
		btnAddClass.addActionListener(this);
		//Add buttons to panels
		pnlExtBtn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		pnlExtBtn.add(btnAddEx);
		pnlSelBtn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		pnlSelBtn.add(btnAddSel);
		pnlClassBtn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		pnlClassBtn.add(btnAddClass);
		
		//Extractor panel
		pnlExt = new JPanel(new BorderLayout());
		pnlExt.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		pnlExtMain = new JPanel(new GridLayout(1, 2));
		pnlExtMain.add(lbExtractor);
		pnlExtMain.add(cbExtractor);
		
		pnlExt.add(pnlExtMain, BorderLayout.NORTH);
		pnlExt.add(pnlExtBtn, BorderLayout.SOUTH);
		
		//Selector panel
		pnlSel = new JPanel(new BorderLayout());
		pnlSel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		pnlSelMain = new JPanel(new GridLayout(1, 2));
		pnlSelMain.add(lbSelector);
		pnlSelMain.add(cbSelector);
		
		pnlSel.add(pnlSelMain, BorderLayout.NORTH);
		pnlSel.add(pnlSelBtn, BorderLayout.SOUTH);
		
		//Classifier panel
		pnlClass = new JPanel(new BorderLayout());
		pnlClass.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		pnlClassMain = new JPanel(new GridLayout(1, 2));
		pnlClassMain.add(lbClassifier);
		pnlClassMain.add(cbClassifier);
		
		pnlClass.add(pnlClassMain, BorderLayout.NORTH);
		pnlClass.add(pnlClassBtn, BorderLayout.SOUTH);
		
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
		
		pnlButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlButton.add(btnRun);
		pnlButton.add(btnSaveModel);
		
		btnAnnotate = new JButton("Apply Model/Annotate");
		if(Annotator.output.equals(Annotator.OUTPUT_CHOICES[3])) {	//If train only mode
			btnRun.setText("Train");								//Set button text to "Train"
			
			btnAnnotate.addActionListener(this);
			btnAnnotate.setEnabled(false);
			pnlButton.add(btnAnnotate);
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
		        int returnVal = fileChooser.showSaveDialog(this);
	
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fileChooser.getSelectedFile();
		            
		            //Iterate through the chain models and write a file for each label
		            for(int i = 0; i < chainModels.length; i++)
		            	chainModels[i].write(file);
		            pnlOutput.setOutput("Save complete. Dump file base path: " + file.getPath());
		        }
			}
			else
				pnlOutput.setOutput("Cannot save model during processing.");
		}
		else if (e.getSource() == btnAnnotate) {
			if(thread == null) {
				//TODO: check if chain models exist and apply
			}
		}
		
		//Add buttons
		if(e.getSource() == btnAddEx) {
			extractor = (Algorithm)cbExtractor.getSelectedItem();
			Extractor ex = new Extractor(extractor.getName());
			
			String value = null;
	        //Parameters for extractor
	        for(Parameter param : extractor.getParam()) {
	        	JComponent control = exParamControls.get(param.getParamName());
	        	if(control instanceof JTextField)
	        		value = ((JTextField) control).getText().trim();
	        	else if(control instanceof JCheckBox)
	        		value = ((JCheckBox)control).isSelected() ? "1" : "0";
	        	else if(control instanceof JSpinner)
	        		value = ((JSpinner)control).getValue().toString();
	        	else if(control instanceof JComboBox)
	        		value = ((JComboBox)control).getSelectedItem().toString();
	        	ex.addParams(param.getParamName(), value);
	        }
	        
	        pnlChain.addExtractor(ex);
		}
		else if(e.getSource() == btnAddSel) {
			selector = (Algorithm)cbSelector.getSelectedItem();
			HashMap<String, String> params = new HashMap<String, String>();
			
			String value = null;
	        //Parameters for selector
	        for(Parameter param : selector.getParam()) {
	        	JComponent control = selParamControls.get(param.getParamName());
	        	if(control instanceof JTextField)
	        		value = ((JTextField) control).getText().trim();
	        	else if(control instanceof JCheckBox)
	        		value = ((JCheckBox)control).isSelected() ? "1" : "0";
	        	else if(control instanceof JSpinner)
	        		value = ((JSpinner)control).getValue().toString();
	        	else if(control instanceof JComboBox)
		        		value = ((JComboBox)control).getSelectedItem().toString();
	        	params.put(param.getParamName(), value);
	        }
	        
	        pnlChain.addSelector(selector.getName(), params);
		}
		else if(e.getSource() == btnAddClass) {
			classifier = (Algorithm)cbClassifier.getSelectedItem();
			HashMap<String, String> params = new HashMap<String, String>();
			
			String value = null;
	        //Parameters for classifier
	        for(Parameter param : classifier.getParam()) {
	        	JComponent control = classParamControls.get(param.getParamName());
	        	if(control instanceof JTextField)
	        		value = ((JTextField) control).getText().trim();
	        	else if(control instanceof JCheckBox)
	        		value = ((JCheckBox)control).isSelected() ? "1" : "0";
	        	else if(control instanceof JSpinner)
	        		value = ((JSpinner)control).getValue().toString();
	        	else if(control instanceof JComboBox)
	        		value = ((JComboBox)control).getSelectedItem().toString();
	        	params.put(param.getParamName(), value);
	        }
	        
	        pnlChain.addClassifier(classifier.getName(), params);
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
        
		thread = null;
		
		//Re-enable the buttons
		btnRun.setEnabled(true);
		btnSaveModel.setEnabled(true);
		btnAnnotate.setEnabled(true);
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
            pnlOutput.setOutput("Annotation process cancelled by user.");
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
		AutoCompFrame frame = new AutoCompFrame("Auto Comparison Mode", false, "g");
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
