package annotool.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import annotool.AnnOutputPanel;
import annotool.Annotation;
import annotool.Annotator;
import annotool.ComboFeatures;
import annotool.classify.Classifier;
import annotool.classify.SavableClassifier;
import annotool.classify.Validator;
import annotool.gui.model.Chain;
import annotool.gui.model.ChainFilter;
import annotool.gui.model.ChainTableModel;
import annotool.gui.model.Extractor;
import annotool.gui.model.ModelFilter;
import annotool.gui.model.ModelSaver;
import annotool.gui.model.Selector;
import annotool.gui.model.Utils;
import annotool.io.ChainIO;
import annotool.io.ChainModel;
import annotool.io.DataInput;

public class ChainPanel extends JPanel implements ActionListener, ListSelectionListener, TableModelListener, Runnable{
	private JPanel pnlMain, pnlDetail,
				   pnlTable, pnlControl,
				   pnlButton,
				   pnlSouth;
	private JTable tblChain = null;
	private JScrollPane scrollPane = null;
	
	private JButton btnNew, btnRemove, 
					btnSaveChain, btnLoadChain, 
					btnRun, btnSaveModel,
					btnApplyModel;
	
	private ChainTableModel tableModel = new ChainTableModel();
	
	//Details
	JTextArea taDetail = new JTextArea(6,30);
	JScrollPane detailPane = new JScrollPane(taDetail);
	
	AutoCompFrame gui = null;
	
	JFileChooser fileChooser = null;
	
	private Thread thread = null;
	private boolean isRunning = false;
	
	private String channel;
	
	JProgressBar bar = null;
	AnnOutputPanel pnlOutput = null;
	
	TableColumnAdjuster tca = null;
	
	//Indices for table columns
	public final static int COL_CHECK = 0;	
	public final static int COL_NAME = 1;	
	public final static int COL_CHAIN = 2;
	
	//To keep track of the best model for each genetic line
	ChainModel[] chainModels = null;
	
	public ChainPanel(AutoCompFrame gui, String channel, AnnOutputPanel pnlOutput) {
		this.channel = channel;		
		this.gui = gui;
		gui.setButtonsEnabled(false);
		this.pnlOutput = pnlOutput;
		
		fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false); //Remove "all files" options from file chooser
		
		this.setLayout(new BorderLayout());
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		pnlTable = new JPanel(new BorderLayout());
		
		pnlDetail = new JPanel(new BorderLayout());
		pnlDetail.setBorder(new CompoundBorder(new TitledBorder(null, "Selected Chain Detail", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(10, 10, 10, 10)));
		pnlDetail.add(detailPane, BorderLayout.CENTER);
		pnlDetail.setPreferredSize(new java.awt.Dimension(300, 200));
		taDetail.setMargin(new Insets(10,10,10,10));
		taDetail.setEditable(false);
		
		tblChain = new JTable(tableModel){
			//preferred size or the viewport size, whichever is greater
			//Needed since we are using auto resize off
            public boolean getScrollableTracksViewportWidth()
            {            	
                return getPreferredSize().width < getParent().getWidth();
            }
        };
		tblChain.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblChain.getSelectionModel().addListSelectionListener(this);
		tblChain.getModel().addTableModelListener(this);
		    
		tblChain.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 		//To show horizontal scrollbars
		tblChain.getColumnModel().getColumn(COL_CHECK).setMaxWidth(30);
		tblChain.getColumnModel().getColumn(COL_CHECK).setMinWidth(30);
		tblChain.getColumnModel().getColumn(COL_NAME).setMinWidth(80);
		
		//Left justify the header text
		((DefaultTableCellRenderer)tblChain.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
		
		//For adjusting table columns to fit content width
		tca = new TableColumnAdjuster(tblChain);
		tca.setOnlyAdjustLarger(false);
		tca.adjustColumns();
		
		
		scrollPane = new JScrollPane(tblChain);
		pnlTable.add(scrollPane, BorderLayout.CENTER);
		
		btnNew = new JButton("New");
		btnNew.addActionListener(this);
		btnRemove = new JButton("Remove");
		btnRemove.setEnabled(false);
		btnRemove.addActionListener(this);		
		btnSaveChain = new JButton("Save Chains");
		btnSaveChain.setEnabled(false);
		btnSaveChain.addActionListener(this);
		btnLoadChain = new JButton("Load Chains");
		btnLoadChain.addActionListener(this);
		btnRun = new JButton("Run");
		btnRun.setEnabled(false);
		btnRun.addActionListener(this);
		btnSaveModel = new JButton("Save Model");
		btnSaveModel.setToolTipText("Save best model from comparision.");
		btnSaveModel.setEnabled(false);
		btnSaveModel.addActionListener(this);
		btnApplyModel = new JButton("Annotate");
		btnApplyModel.setEnabled(false);
		btnApplyModel.addActionListener(this);
		
		pnlButton = new JPanel(new GridLayout(4, 2));
		pnlButton.add(btnNew);
		pnlButton.add(btnRemove);
		pnlButton.add(btnSaveChain);
		pnlButton.add(btnLoadChain);
		pnlButton.add(btnRun);
		pnlButton.add(btnSaveModel);
		pnlButton.add(btnApplyModel);
		
		pnlControl = new JPanel();
		pnlControl.setLayout(new FlowLayout());
		pnlControl.add(pnlButton);	
		
		pnlMain = new JPanel(new BorderLayout());
		pnlMain.setBorder(new CompoundBorder(new TitledBorder(null, "Algorithm Chains", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(10, 10, 10, 10)));
		pnlMain.add(pnlTable, BorderLayout.CENTER);
		pnlMain.add(pnlControl, BorderLayout.EAST);
		
		bar = new JProgressBar(0, 100);
		bar.setValue(0);
		bar.setStringPainted(true);
		
		pnlSouth  = new JPanel(new BorderLayout());
		pnlSouth.add(bar, BorderLayout.SOUTH);
		pnlSouth.add(pnlDetail, BorderLayout.CENTER);
		
		this.add(pnlMain, BorderLayout.CENTER);
		this.add(pnlSouth, BorderLayout.SOUTH);
	}
	
	public void actionPerformed(ActionEvent ev) {
		if(ev.getSource().equals(btnNew)) {
			//Check if last chain in the table is complete
			int size = tblChain.getRowCount();
			if(size > 0) {
				Chain lastChain = (Chain)tblChain.getValueAt(size - 1, COL_CHAIN);
				if(!lastChain.isComplete()) {
					JOptionPane.showMessageDialog(this,
						    "The last chain is not yet complete. Classifier is required.", 
						    "Incomplete Chain",
						    JOptionPane.INFORMATION_MESSAGE);
					return;
				}
			}
			
			String name = createChainName();
			Chain chain = new Chain(name);
			Object[] rowData = {new Boolean(false), name, chain};//chain.setName((String)tableModel.getValueAt(insertIndex, COL_NAME));
			tableModel.insertNewRow(rowData);
			
			//Select the newly inserted row
			tblChain.changeSelection(tableModel.getRowCount() - 1, COL_CHAIN, false, false);
			
			//Put the name column in edit mode
			tblChain.editCellAt(tableModel.getRowCount() - 1, COL_NAME);
			
			setButtonState();
		}
		else if(ev.getSource().equals(btnRemove)) {
			tableModel.removeRow(tblChain.getSelectedRow());
			
			tca.adjustColumns();
			
			taDetail.setText("");
			setButtonState();
		}
		else if(ev.getSource().equals(btnSaveChain)) {
			//Save chains to file
			ArrayList<Chain> chainList = new ArrayList<Chain>();
			
        	for(int i = 0; i < tableModel.getRowCount(); i++) {
        		chainList.add((Chain)tableModel.getValueAt(i, COL_CHAIN));
        	}
        	if(chainList.isEmpty()) {
        		JOptionPane.showMessageDialog(this,
					    "There shoulb be at least one chain to save.", 
					    "Empty List",
					    JOptionPane.INFORMATION_MESSAGE);
				return;
        	}
        	
        	fileChooser.resetChoosableFileFilters();
        	fileChooser.addChoosableFileFilter(new ChainFilter());
			int returnVal = fileChooser.showSaveDialog(this);
	        if (returnVal == JFileChooser.APPROVE_OPTION) {	        	
	        	ChainIO chainSaver = new ChainIO();
	        	try {
	        		File file = fileChooser.getSelectedFile();
	        		//Add extension if not present
	        		String filePath = file.getPath();
	                if(!filePath.toLowerCase().endsWith("." + Utils.CHAIN_EXT)) {
	                	file = new File(filePath + "." + Utils.CHAIN_EXT);
	                }
	        		chainSaver.save(file, chainList);
	        		pnlOutput.setOutput("Chain saved to " + file.getAbsolutePath());
	        	}
	        	catch (IOException ex) {
	        		System.out.println("Exception thrown while writing chain list to file.");
	        		ex.printStackTrace();
	        		pnlOutput.setOutput("Save failed.");
	        	}
	        }
		}
		else if(ev.getSource().equals(btnLoadChain)) {
			fileChooser.resetChoosableFileFilters();
			fileChooser.addChoosableFileFilter(new ChainFilter());
			int returnVal = fileChooser.showOpenDialog(this);
			
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fileChooser.getSelectedFile();
	            
	            ChainIO chainLoader = new ChainIO();
	            try {
	            	ArrayList<Chain> chainList = chainLoader.load(file);
	            	tableModel.removeAll();
	            	taDetail.setText("");
	            	for(Chain chain : chainList) {
	            		Object[] rowData = {new Boolean(false), chain.getName(), chain};		//TODO: load and use chain names also
	        			tableModel.insertNewRow(rowData);	        			
	            	}
	            	pnlOutput.setOutput("Chain successfully loaded.");
	            }
	            catch (Exception ex) {
	            	System.out.println("Exception thrown while loading chain list from file.");
	        		ex.printStackTrace();
	        		pnlOutput.setOutput("Load failed.");
	            }
	            
	            tca.adjustColumns();
	            
	            //Enable/disable buttons based on whether has rows or not
	            setButtonState();
	        }
		}
		else if(ev.getSource().equals(btnRun)) {
			//Check if the last chain is complete
			int size = tblChain.getRowCount();
			if(size > 0) {
				Chain lastChain = (Chain)tblChain.getValueAt(size - 1, COL_CHAIN);
				if(!lastChain.isComplete()) {
					JOptionPane.showMessageDialog(this,
						    "The last chain is not yet complete. Classifier is required.", 
						    "Incomplete Chain",
						    JOptionPane.INFORMATION_MESSAGE);
					return;
				}
			
				if (thread == null)  {
		            thread = new Thread(this);
		            isRunning = true;
		            thread.start();
		        }
			}
		}
		else if(ev.getSource().equals(btnSaveModel)) {
			if(thread == null) {
				//Show confirmation dialog
				int choice = JOptionPane.showConfirmDialog(this,
					    "Depending on the algorithm, saving a model can take a while to finish.\n" + 
					    "If two chains have the same result, the first one will be saved\n" + 
					    "Do you want to continue?",
					    "Information",
					    JOptionPane.OK_CANCEL_OPTION,
					    JOptionPane.INFORMATION_MESSAGE);
				
				if(choice == JOptionPane.CANCEL_OPTION)
					return;

				fileChooser.resetChoosableFileFilters();
				fileChooser.addChoosableFileFilter(new ModelFilter());
		        int returnVal = fileChooser.showSaveDialog(this);
	
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            pnlOutput.setOutput("Saving Model...");
		        	File file = fileChooser.getSelectedFile();
		        	
		        	//Reset progress bar
		            bar.setValue(0);
		            
		            JButton[] buttons = {btnRun, btnSaveModel}; 
		            ModelSaver saver = new ModelSaver(bar, pnlOutput, buttons, chainModels, file);
		            Thread t1 = new Thread(saver);
		            t1.start();
		        }
			}
			else
				pnlOutput.setOutput("Cannot save model during processing.");
		}
		else if(ev.getSource().equals(btnApplyModel)) {
			if(thread == null) {
				int choice = JOptionPane.showConfirmDialog(this,
						"This will close all open windows.\n" +
						"The best model from the comparison will be used for the annotation process.\n" + 
						"All other unsaved progress will be lost.\n" +
						"Do you wish to continue?",
					    "Information",
					    JOptionPane.OK_CANCEL_OPTION,
					    JOptionPane.INFORMATION_MESSAGE);
				
				if(choice == JOptionPane.CANCEL_OPTION)
					return;
				
				//Set the flag that indicates this frame as annotation firing frame and then fire close window
				gui.applyModelFired = true;
				gui.setChainModels(chainModels);
				gui.pullThePlug();				
			}
		}
		
	}
	private void setButtonState() {
		if(tableModel.getRowCount() > 0) {
			btnRemove.setEnabled(true);
			btnSaveChain.setEnabled(true);
			gui.setButtonsEnabled(true);
		}
		else {
			btnRemove.setEnabled(false);
			btnSaveChain.setEnabled(false);
			gui.setButtonsEnabled(false);
		}		

		btnRun.setEnabled(hasSelectedChain());
	}
	/*
	 * Checks if there is at least one selected chain.
	 */
	private boolean hasSelectedChain() {		
		for(int row = 0; row < tableModel.getRowCount(); row++) {
	    	if((Boolean)tableModel.getValueAt(row, COL_CHECK)) 
	    		return true;
	    }
		return false;
	}
	public void valueChanged(ListSelectionEvent ev) {
		if (ev.getValueIsAdjusting()) {
            return;
        }
		showItemDetail();
	}
	public void tableChanged(TableModelEvent ev) {
		int row = ev.getFirstRow();
        int column = ev.getColumn();
        if(row < 0 || column < 0)
        	return;
        
        TableModel model = (TableModel)ev.getSource();
        String columnName = model.getColumnName(column);
        if(columnName.equals("Name")) {
        	String name = model.getValueAt(row, column).toString();
        	Chain chain = (Chain)model.getValueAt(row, COL_CHAIN);
        	chain.setName(name);
        }
        else if(columnName.equals("")) {	//If check box state changed, then enable/disable run button
        	btnRun.setEnabled(hasSelectedChain());
        }
	}
	public void addExtractor(Extractor ex) {
		int currentRow = tblChain.getSelectedRow();
		if(currentRow < 0)
			return;
		Chain chain = (Chain)tblChain.getValueAt(currentRow, COL_CHAIN);
		if(ex.getName().equalsIgnoreCase("None"))						//Clear extractors if 'None' selected
			chain.clearExtractors();
		else
			chain.addExtractor(ex);
		

		tca.adjustColumns();
		
		tblChain.repaint();
		showItemDetail();
	}
	public void addSelector(Selector sel) {
		int currentRow = tblChain.getSelectedRow();
		if(currentRow < 0)
			return;
		Chain chain = (Chain)tblChain.getValueAt(currentRow, COL_CHAIN);		
		if(sel.getName().equalsIgnoreCase("None"))						//Clear selectors if 'None' selected
			chain.clearSelectors();
		else
			chain.addSelector(sel);
		
		tca.adjustColumns();
		
		tblChain.repaint();
		showItemDetail();
	}
	public void addClassifier(String name, HashMap<String, String> params) {
		int currentRow = tblChain.getSelectedRow();
		if(currentRow < 0)
			return;
		Chain chain = (Chain)tblChain.getValueAt(currentRow, COL_CHAIN);
		chain.setClassifier(name);
		chain.setClassParams(params);
		
		tca.adjustColumns();
		
		tblChain.repaint();
		showItemDetail();
	}
	private void showItemDetail() {
		int currentRow = tblChain.getSelectedRow();
		if(currentRow < 0)
			return;
		
		final Chain chain = (Chain)tblChain.getValueAt(currentRow, COL_CHAIN);
		taDetail.setText("");
		if(chain.getExtractors().size() > 0) {
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			taDetail.setText(taDetail.getText() + "FEATURE EXTRACTOR(S):\n");
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			for(Extractor ex : chain.getExtractors()) {
				taDetail.setText(taDetail.getText() + ex.getName() + "\n");
				for (String parameter : ex.getParams().keySet()) {
					taDetail.setText(taDetail.getText() + parameter + "=" + ex.getParams().get(parameter) + "\n");
	        	}
				taDetail.setText(taDetail.getText() + "\n");
			}
		}
		if(chain.getSelectors().size() > 0) {
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			taDetail.setText(taDetail.getText() + "FEATURE SELECTOR (S):\n");
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			for(Selector sel : chain.getSelectors()) {
				taDetail.setText(taDetail.getText() + sel.getName() + "\n");
				for (String parameter : sel.getParams().keySet()) {
					taDetail.setText(taDetail.getText() + parameter + "=" + sel.getParams().get(parameter) + "\n");
	        	}
				taDetail.setText(taDetail.getText() + "\n");
			}
		}
		if(chain.getClassifier() != null) {
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			taDetail.setText(taDetail.getText() + "CLASSIFIER:\n");
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			taDetail.setText(taDetail.getText() + chain.getClassifier() + "\n");
			for (String parameter : chain.getClassParams().keySet()) {
				taDetail.setText(taDetail.getText() + parameter + "=" +chain.getClassParams().get(parameter) + "\n");
	    	}
		}
		
		//taDetail.setCaretPosition(taDetail.getText().length());
	}

	@Override
	public void run() {
		//Disable buttons
		btnRun.setEnabled(false);
		btnNew.setEnabled(false);
		btnRemove.setEnabled(false);
		btnLoadChain.setEnabled(false);
		btnSaveModel.setEnabled(false);
		btnApplyModel.setEnabled(false);
		gui.setButtonsEnabled(false);
		tblChain.setEnabled(false);
		
		//Initiate appropriate process
		if(Annotator.output.equals(Annotator.TT)) {				//TT Mode
			ttRun();
		}
		else if(Annotator.output.equals(Annotator.CV)) {			//Cross validation mode
			cvRun();			
		}
		
		isRunning = false;
		thread = null;
		
		//Enable buttons
		btnRun.setEnabled(true);
		btnNew.setEnabled(true);
		btnRemove.setEnabled(true);
		btnLoadChain.setEnabled(true);
		btnSaveModel.setEnabled(true);
		btnApplyModel.setEnabled(true);
		gui.setButtonsEnabled(true);
		tblChain.setEnabled(true);
	}
	//Training/Testing
	private void ttRun() {
		Annotator anno = new Annotator();
		
		//read images and wrapped into DataInput instances.
	    DataInput trainingProblem = new DataInput(Annotator.dir, Annotator.ext, channel);
	    DataInput testingProblem = new DataInput(Annotator.testdir, Annotator.testext, channel);	        
	
	    int[] resArr = new int[2]; //place holder for misc results
	    ArrayList<String> annoLabels = new ArrayList<String>();
	    HashMap<String, String> classNames = new HashMap<String, String>();
	    int[][] trainingTargets = anno.readTargets(trainingProblem, Annotator.targetFile, resArr, annoLabels, classNames);
	    //get statistics from training set
	    int numOfAnno = resArr[0];
	    anno.setAnnotationLabels(annoLabels);	        
	
	    //testing set targets
	    int[][] testingTargets = anno.readTargets(testingProblem, Annotator.testtargetFile, resArr, null, null);
	    
	    
	    //Initialize float array to hold rates for each annotation for each selected chain and list of selected chains to pass to result panel
	    ArrayList<Chain> selectedChains = new ArrayList<Chain>(); 
	    int chainCount = 0;
	    for(int row = 0; row < tableModel.getRowCount(); row++) {
	    	if((Boolean)tableModel.getValueAt(row, COL_CHECK)) {
	    		chainCount++;
	    		selectedChains.add((Chain)tableModel.getValueAt(row, COL_CHAIN));
	    	}
	    }
	    float[][] rates = new float[chainCount][numOfAnno];
	    chainCount = 0; //Reset chain count to use later to put rates for each selected chain
	    
	    boolean executed = false;	//Set to true if at least one chain is executed
	    
	  	//Keep features to be dumped into chain file
	    int imgWidth = trainingProblem.getWidth();
	    int imgHeight = trainingProblem.getHeight();
	    
	    //Chain Models to keep track of the best model for each target
	    chainModels = new ChainModel[numOfAnno];
	    
	    //Initialize common features for chain models
	    for(int i = 0; i < numOfAnno; i++) {
	    	chainModels[i] = new ChainModel();
	    	//Save information to dump in chain file
	    	chainModels[i].setImageSet(new File(Annotator.dir).getAbsolutePath());
	    	chainModels[i].setTestingSet(new File(Annotator.testdir).getAbsolutePath());
	    	chainModels[i].setMode("Training/Testing");
	    	chainModels[i].setChannel(channel);
	    	chainModels[i].setLabel(anno.getAnnotationLabels().get(i));
	    	chainModels[i].setClassNames(classNames);
	    	chainModels[i].setImageSize(imgWidth + "x" + imgHeight);
	    }
	    
	    for(int row = 0; row < tableModel.getRowCount(); row++) {
	    	//Only use the checked chains
	    	if(!(Boolean)tableModel.getValueAt(row, COL_CHECK))
	    		continue;        	
	    
	    	executed = true;
	    	
	    	selectRow(row);
	    	//feature extraction.
	        if (!setProgress(30))  {
	            return;
	        }
	        
	    	Chain chain = (Chain)tableModel.getValueAt(row, COL_CHAIN);//Second column is chain object
	    	
	    	pnlOutput.setOutput("Processing " + chain.getName() + ":");
	    	
	    	//Chain list loaded from file may have incomplete chain in the middle if the file has been tampered with
	    	if(!chain.isComplete()) {
	    		pnlOutput.setOutput("Incomplete chain encountered. Chain = " + chain.getName());
	    		continue;
	    	}
	    	
	        pnlOutput.setOutput("Extracting features...");
	        
	        //Start of extraction: TODO:
	        String extractor = "None";
	        HashMap<String, String> params = new HashMap<String, String>();
	        
	        int numExtractors = chain.getExtractors().size();
	        float[][][] exTrainFeatures = new float[numExtractors][][];
	        float[][][] exTestFeatures = new float[numExtractors][][];
	        
	        int trainSize = 0, testSize = 0;	//To keep track of total size
	        for(int exIndex=0; exIndex < numExtractors; exIndex++) {
	        	extractor = chain.getExtractors().get(exIndex).getName();
	        	params = chain.getExtractors().get(exIndex).getParams();
	        	
	        	exTrainFeatures[exIndex] = anno.extractGivenAMethod(extractor, params, trainingProblem);
	        	exTestFeatures[exIndex] = anno.extractGivenAMethod(extractor, params, testingProblem);
	        	
	        	trainSize += exTrainFeatures[exIndex][0].length;
	        	testSize += exTestFeatures[exIndex][0].length;
	        }
	        
	        float[][] trainingFeatures = null;
	        float[][] testingFeatures = null;
	        
	        if(numExtractors < 1) {	//If no extractor, call the function by passing "None"
	        	trainingFeatures = anno.extractGivenAMethod(extractor, params, trainingProblem);
	        	testingFeatures = anno.extractGivenAMethod(extractor, params, testingProblem);
	        }
	        else {	//Else, create feature array with enough space to hold data from all extractors 
	        	trainingFeatures = new float[trainingProblem.getLength()][trainSize];
	        	testingFeatures = new float[testingProblem.getLength()][testSize];
	        	
	        	int destPosTrain = 0, destPosTest = 0;
	        	for(int exIndex=0; exIndex < numExtractors; exIndex++) {
	        		for(int item=0; item < trainingFeatures.length; item++) {
	        			System.arraycopy(exTrainFeatures[exIndex][item], 0, trainingFeatures[item], destPosTrain, exTrainFeatures[exIndex][item].length);
		        		System.arraycopy(exTestFeatures[exIndex][item], 0, testingFeatures[item], destPosTest, exTestFeatures[exIndex][item].length);
	        		}
	        		destPosTrain += exTrainFeatures[exIndex][0].length;
	        		destPosTest += exTestFeatures[exIndex][0].length;
	        	}
	        }
	        exTrainFeatures = null;
	        exTestFeatures = null;
	        //End of extraction
	        
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
	        
	        //loop for each annotation target (one image may have multiple labels)
	        for (int i = 0; i < numOfAnno; i++) {
	        	//Selected features for each annotation labels
	        	float[][] selectedTrainingFeatures = trainingFeatures;
	        	float[][] selectedTestingFeatures = testingFeatures;
	        	
	        	ArrayList<Selector> selectors = new ArrayList<Selector>();
	        	
	        	ComboFeatures combo = null;
	            if(chain.hasSelectors()) 
	            {
	                pnlOutput.setOutput("Selecting features...");
	                
	                //Apply each feature selector in the chain
	                for(Selector selector : chain.getSelectors()) {
		            	//Supervised feature selectors need corresponding target data
		                combo = anno.selectGivenAMethod(selector.getName(), selector.getParams(), selectedTrainingFeatures, selectedTestingFeatures, trainingTargets[i], testingTargets[i]);
		                //selected features overrides the passed in original features
		                selectedTrainingFeatures = combo.getTrainingFeatures();
		                selectedTestingFeatures = combo.getTestingFeatures();
		                
		                //This is needed for saving model, each annotation label needs a new selector to be created (i.e. cannot reuse selectors in chain)
		                Selector currentSelector = new Selector(selector.getName());
		                currentSelector.setSelectedIndices(combo.getSelectedIndices());
		                selectors.add(currentSelector);
	                }
	            }
	            //pass the training and testing data to Validator
	            //get rate and prediction results for testing data
	            float rate = 0;
	            pnlOutput.setOutput("Classifying/Annotating...");
	            
	            Classifier classifierObj = anno.getClassifierGivenName(chain.getClassifier(), chain.getClassParams());
	            try {
	            	rate = anno.classifyGivenAMethod(classifierObj, chain.getClassParams(), selectedTrainingFeatures, selectedTestingFeatures, trainingTargets[i], testingTargets[i], annotations[i]);
	            }
	            catch(Exception ex) {
	            	ex.printStackTrace();
	            }
	            
	            rates[chainCount][i] = rate;
	            
	            //If rate for this target(ith target) is better with this chain,
	            //then, save this as new best model
	            if(rate > chainModels[i].getResult()) {
	            	chainModels[i].setExtractors(chain.getExtractors());//Can use extractors from chain because every annotation label shares the same extractors
	            	chainModels[i].setSelectors(selectors);				//Cannot use selectors from chain because each annotation label needs separate selected indices
	            	
	            	chainModels[i].setClassifierName(chain.getClassifier());
	            	chainModels[i].setClassifier(classifierObj);
	            	chainModels[i].setClassParams(chain.getClassParams());
	            	chainModels[i].setResult(rate);
	            }	            	
	            
	            System.out.println(rate);
	                
	            pnlOutput.setOutput("Recognition rate for " + anno.getAnnotationLabels().get(i) + ": " + rate);
	            if (!setProgress(50 + (i + 1) * 50 / numOfAnno)) {
	                return;
	            }
	        }//end of loop for annotation targets
	        chainCount++;
	    }//End of loop for chains
	    
	    //Display result
	    if(executed)	//Display result if at least one chain is executed
	    	gui.addTab("Auto Comparison Results", rates, anno.getAnnotationLabels(), selectedChains, imgWidth, imgHeight, channel);
	}

	private void cvRun() {
		Annotator anno = new Annotator();
		
		//------ read image data from the directory ------------//
        DataInput problem = new DataInput(Annotator.dir, Annotator.ext, channel);

        //-----  read targets matrix (for multiple annotations, one per column) --------//
        if (!setProgress(20)) {
            return;
        }
        int[] resArr = new int[2]; //place holder for misc results
        ArrayList<String> annoLabels = new ArrayList<String>();
        HashMap<String, String> classNames = new HashMap<String, String>();
        int[][] targets = anno.readTargets(problem, Annotator.targetFile, resArr, annoLabels, classNames);
        int numOfAnno = resArr[0];
        anno.setAnnotationLabels(annoLabels);
        
        //Initialize float array to hold rates for each annotation for each selected chain and list of selected chains to pass to result panel
        ArrayList<Chain> selectedChains = new ArrayList<Chain>(); 
        int chainCount = 0;
        for(int row = 0; row < tableModel.getRowCount(); row++) {
        	if((Boolean)tableModel.getValueAt(row, COL_CHECK)) {
        		chainCount++;
        		selectedChains.add((Chain)tableModel.getValueAt(row, COL_CHAIN));
        	}
        }
        float[][] rates = new float[chainCount][numOfAnno];
        chainCount = 0; //Reset chain count to use later to put rates for each selected chain
        
        boolean executed = false;	//Set to true if at least one chain is executed
        
      	//Keep features to be dumped into chain file
        int imgWidth = problem.getWidth();
        int imgHeight = problem.getHeight();
        
        //Chain Models to keep track of the best model for each target
        chainModels = new ChainModel[numOfAnno];
        
        //Initialize common features for chain models
        for(int i = 0; i < numOfAnno; i++) {
        	chainModels[i] = new ChainModel();
        	//Save information to dump in chain file
        	chainModels[i].setImageSet(new File(Annotator.dir).getAbsolutePath());
        	chainModels[i].setMode("Cross Validation. Fold: " + Annotator.fold);
        	chainModels[i].setChannel(channel);
        	chainModels[i].setLabel(anno.getAnnotationLabels().get(i));
        	chainModels[i].setClassNames(classNames);
        	chainModels[i].setImageSize(imgWidth + "x" + imgHeight);
        }
        
        for(int row = 0; row < tableModel.getRowCount(); row++) {
        	//Only use the checked chains
        	if(!(Boolean)tableModel.getValueAt(row, COL_CHECK))
        		continue;        	
        
        	executed = true;
        	
        	//Select the currently processed row in the table
        	selectRow(row);
            
        	Chain chain = (Chain)tableModel.getValueAt(row, COL_CHAIN);//Second column is chain object
        	
        	pnlOutput.setOutput("Processing " + chain.getName() + ":");
        	
        	//Chain list loaded from file may have incomplete chain in the middle if the file has been tampered with
        	if(!chain.isComplete()) {
        		pnlOutput.setOutput("Incomplete chain encountered. Chain = " + chain.getName());
        		continue;
        	}
        	
	        //----- feature extraction -------//
	        if (!setProgress(30)) {
	            return;
	        }
	        pnlOutput.setOutput("Extracing features ... ");
	        
	        //Start of extraction
	        float[][] features =  anno.extractWithMultipleExtractors(problem, chain.getExtractors());
	        //End of extraction
	        
	        //raw data is not used after this point, set to null.: commented because used for subsequent loop runs
	        //problem.setDataNull();
	
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
	        
	        //loop for each annotation target
	        for (int i = 0; i < numOfAnno; i++) {
	            float recograte[] = null;
	            int start = 50 + i * 50 / numOfAnno;
	            int region = 50 / numOfAnno;
	            
	            //Selected features for each annotation labels
	            float[][] selectedFeatures = features;
	            
	            ComboFeatures combo = null;
	            if(chain.hasSelectors()) 
	            {
	                pnlOutput.setOutput("Selecting features...");
	                
	                //Apply each feature selector in the chain
	                for(Selector selector : chain.getSelectors()) {
		            	//Supervised feature selectors need corresponding target data
		                combo = anno.selectGivenAMethod(selector.getName(), selector.getParams(), selectedFeatures, targets[i]);
		                //selected features overrides the passed in original features
		                selectedFeatures = combo.getTrainingFeatures();
		                
		                selector.setSelectedIndices(combo.getSelectedIndices());
	                }
	            }
	
	            pnlOutput.setOutput("Classifying/Annotating ... ");
	            Classifier classifierObj = anno.getClassifierGivenName(chain.getClassifier(), chain.getClassParams());
	            try {
	            	recograte = (new Validator(bar, start, region)).KFoldGivenAClassifier(K, selectedFeatures, targets[i], classifierObj, chain.getClassParams(), shuffle, results[i]);
	            }
	            catch(Exception ex) {
	            	pnlOutput.setOutput("Exception! " + ex.getMessage());
	            	ex.printStackTrace();
	            }
	            
	            rates[chainCount][i] = recograte[K];
	            
	            //If rate for this target(ith target) is better with this chain,
	            //then, save this as new best model
	            if(recograte[K] > chainModels[i].getResult()) {
	            	chainModels[i].setExtractors(chain.getExtractors());
	            	chainModels[i].setSelectors(chain.getSelectors());
	            	
	            	chainModels[i].setClassifierName(chain.getClassifier());
	            	chainModels[i].setClassifier(classifierObj);
	            	chainModels[i].setClassParams(chain.getClassParams());
	            	chainModels[i].setResult(recograte[K]);
	            }
	            
	            //output results to GUI and file
	            System.out.println("Rate for annotation target " + i + ": " + recograte[K]);
	            pnlOutput.setOutput("Recognition rate for " + anno.getAnnotationLabels().get(i) + ": " + recograte[K]);
	        } //end of loop for annotation targets
	        
	        chainCount++;
        }//End of loop for chains
        
        //Display result
        if(executed)	//Display result if at least one chain is executed
        	gui.addTab("Auto Comparison Results", rates, anno.getAnnotationLabels(), selectedChains, imgWidth, imgHeight, channel);
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
     * Selets the chain(row) currently being processed
     */
    private void selectRow(int row) {
    	tblChain.changeSelection(row, 1, false, false);
    }
    /*
     * Get unique name for a chain
     */
    private String createChainName() {
    	int i=1;
    	
    	while(true) {
    		//Break if unique name found
    		if(!nameExists("Chain " + String.valueOf(i)))
    			break;
    		i++;
    	}
    	return "Chain " + String.valueOf(i);
    }
    /*
     * Check if the supplied chain name already exists in the table
     */
    private boolean nameExists(String name) {
    	for(int row = 0; row < tblChain.getRowCount(); row++) {
    		String rowName = (String)tableModel.getValueAt(row, COL_NAME);
    		if(name.equals(rowName))
    			return true;
    	}
    	return false;
    }
}
