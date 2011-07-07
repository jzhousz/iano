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

public class AutoCompFrame extends JFrame implements ActionListener, ItemListener {
	private JTabbedPane tabPane;
	private JPanel pnlMainOuter,
				   pnlMain,
				   pnlAlgo,
				   pnlExt, pnlSel, pnlClass,
				   pnlExtMain, pnlSelMain, pnlClassMain,
				   pnlExtBtn, pnlSelBtn, pnlClassBtn;
	
	private ChainPanel pnlChain;
	
	private JButton btnAddEx, btnAddSel, btnAddClass;
	
	private JLabel lbExtractor, lbSelector, lbClassifier;
	private JComboBox cbExtractor, cbSelector, cbClassifier;
	
	int tabNumber = 1; //Initial number of tabs
	
	//To keep track of dynamically added components
	HashMap<String, JComponent> exParamControls = null;			//For extractor parameters
	HashMap<String, JComponent> selParamControls = null;		//For selector parameters
	HashMap<String, JComponent> classParamControls = null;		//For classifier parameters
	
	AnnOutputPanel pnlOutput = null;
	
	public AutoCompFrame(String arg0, boolean is3D, String channel) {
		super(arg0);
		
		pnlMain = new JPanel();
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
		pnlMain.setPreferredSize(new java.awt.Dimension(540, 680));
		pnlMain.setBorder(new EmptyBorder(10, 10, 10, 10));
		pnlMain.setAlignmentY(TOP_ALIGNMENT);
		pnlMain.setAlignmentX(LEFT_ALIGNMENT);
		//this.add(pnlMain, BorderLayout.WEST);
		
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
				
		pnlOutput = new AnnOutputPanel();
		
		//Right side panel for chains
		pnlChain = new ChainPanel(this, channel, pnlOutput);
		
		pnlMainOuter = new JPanel(new BorderLayout());
		pnlMainOuter.add(pnlMain, BorderLayout.WEST);
		pnlMainOuter.add(pnlChain, BorderLayout.CENTER);
		
		tabPane = new JTabbedPane();
		tabPane.addTab("Algorithms", pnlMainOuter);
		this.add(tabPane);
		
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
		
		
		pnlMain.add(pnlAlgo);
		pnlMain.add(pnlOutput);
		
		//Build parameter panels for default selection
		buildExParameterPanel();
		buildSelParameterPanel();
		buildClassParameterPanel();		
	}
	public void actionPerformed(ActionEvent e) {
		//Add buttons
		if(e.getSource() == btnAddEx) {
			Algorithm extractor = (Algorithm)cbExtractor.getSelectedItem();
			Extractor ex = new Extractor(extractor.getName());
			ex.setParams(getParameterList(extractor, "Extractor"));	        
	        pnlChain.addExtractor(ex);
		}
		else if(e.getSource() == btnAddSel) {
			Algorithm selector = (Algorithm)cbSelector.getSelectedItem();	        
	        pnlChain.addSelector(selector.getName(), getParameterList(selector, "Selector"));
		}
		else if(e.getSource() == btnAddClass) {
			Algorithm classifier = (Algorithm)cbClassifier.getSelectedItem();	        
	        pnlChain.addClassifier(classifier.getName(), getParameterList(classifier, "Classifier"));
		}
	}
	private HashMap<String, String> getParameterList(Algorithm al, String type) {
		HashMap<String, String> params = new HashMap<String, String>();
		
		String value = null;
        //Parameters for classifier
        for(Parameter param : al.getParam()) {
        	JComponent control = null;

        	if(type.equals("Extractor"))
        		control = exParamControls.get(param.getParamName());
        	else if(type.equals("Selector"))
        		control = selParamControls.get(param.getParamName());
        	else if(type.equals("Classifier"))
        		control = classParamControls.get(param.getParamName());
        		
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
        
        return params;
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
	public void setButtonsEnabled(boolean flag) {
		btnAddEx.setEnabled(flag);
		btnAddSel.setEnabled(flag);
		btnAddClass.setEnabled(flag);
	}
	
	public void addTab(String title, float[][] rates, ArrayList<String> labels) {
		//Display result
        ACResultPanel pnlResult = new ACResultPanel(tabPane);
        tabPane.addTab(title, pnlResult);
        pnlResult.showChart(rates, labels); 
        
        //Add panel with title label and close button to the tab
        tabPane.setTabComponentAt(tabPane.getTabCount() - 1, 
                new ButtonTabComponent(title, tabPane));
	}
    //Temporary main method for testing GUI
	public static void main(String[] args) {
		AutoCompFrame frame = new AutoCompFrame("Auto Comparison Mode", false, "g");
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
