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
import annotool.gui.model.Chain;
import annotool.gui.model.ChainTableModel;
import annotool.gui.model.Extractor;
import annotool.gui.model.ModelSaver;
import annotool.gui.model.Selector;
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
					btnSave, btnLoad, 
					btnRun, btnSaveModel;
	private ChainTableModel tableModel = new ChainTableModel();
	
	//Details
	JTextArea taDetail = new JTextArea(6,30);
	JScrollPane detailPane = new JScrollPane(taDetail);
	
	AutoCompFrame gui = null;
	
	JFileChooser fileChooser = new JFileChooser();
	
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
		btnSave = new JButton("Save Chains");
		btnSave.setEnabled(false);
		btnSave.addActionListener(this);
		btnLoad = new JButton("Load Chains");
		btnLoad.addActionListener(this);
		btnRun = new JButton("Run");
		btnRun.setEnabled(false);
		btnRun.addActionListener(this);
		btnSaveModel = new JButton("Save Model");
		btnSaveModel.setEnabled(false);
		btnSaveModel.addActionListener(this);
		
		pnlButton = new JPanel(new GridLayout(3, 2));
		pnlButton.add(btnNew);
		pnlButton.add(btnRemove);
		pnlButton.add(btnSave);
		pnlButton.add(btnLoad);
		pnlButton.add(btnRun);
		pnlButton.add(btnSaveModel);
		
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
		else if(ev.getSource().equals(btnSave)) {
			//Save chains to file
			ArrayList<Chain> chainList = new ArrayList<Chain>();
			ArrayList<String> nameList = new ArrayList<String>();
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
			int returnVal = fileChooser.showSaveDialog(this);			
	        if (returnVal == JFileChooser.APPROVE_OPTION) {	        	
	        	ChainIO chainSaver = new ChainIO();
	        	try {
	        		File file = fileChooser.getSelectedFile();
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
		else if(ev.getSource().equals(btnLoad)) {
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
			}
			
			if (thread == null)  {
	            thread = new Thread(this);
	            isRunning = true;
	            thread.start();
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
		
	}
	private void setButtonState() {
		if(tableModel.getRowCount() > 0) {
			btnRemove.setEnabled(true);
			btnSave.setEnabled(true);
			btnRun.setEnabled(true);
			gui.setButtonsEnabled(true);
		}
		else {
			btnRemove.setEnabled(false);
			btnSave.setEnabled(false);
			btnRun.setEnabled(false);
			gui.setButtonsEnabled(false);
		}
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
		btnLoad.setEnabled(false);
		btnSaveModel.setEnabled(false);
		gui.setButtonsEnabled(false);
		tblChain.setEnabled(false);
		
		if(Annotator.output.equals(Annotator.OUTPUT_CHOICES[0])) {				//TT Mode
			ttRun();
		}
		
		isRunning = false;
		thread = null;
		
		//Enable buttons
		btnRun.setEnabled(true);
		btnNew.setEnabled(true);
		btnRemove.setEnabled(true);
		btnLoad.setEnabled(true);
		btnSaveModel.setEnabled(true);
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
        int[][] trainingTargets = anno.readTargets(trainingProblem, Annotator.targetFile, resArr, annoLabels);
        //get statistics from training set
        int numOfAnno = resArr[0];
        anno.setAnnotationLabels(annoLabels);	        

        //testing set targets
        int[][] testingTargets = anno.readTargets(testingProblem, Annotator.testtargetFile, resArr, null);
        
        
        //Initialize float array to hold rates for each annotation for each selected chain
        int chainCount = 0;
        for(int row = 0; row < tableModel.getRowCount(); row++) {
        	if((Boolean)tableModel.getValueAt(row, COL_CHECK))
        		chainCount++;
        }
        float[][] rates = new float[chainCount][numOfAnno];
        
        boolean executed = false;	//Set to true if at least one chain is executed
        
        ArrayList<String> chainNames = new ArrayList<String>();
        
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
        	chainModels[i].setLabel(anno.getAnnotationLabels().get(i));
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
        	chainNames.add(chain.getName());
        	
        	pnlOutput.setOutput("Processing " + chain.getName() + ":");
        	
        	//Chain list loaded from file may have incomplete chain in the middle if the file has been tampered with
        	if(!chain.isComplete()) {
        		pnlOutput.setOutput("Incomplete chain encountered. Chain = " + chain.getName());
        		continue;
        	}
        	
	        pnlOutput.setOutput("Extracting features...");
	        
	        String extractor = "None";
	        HashMap<String, String> params = new HashMap<String, String>();
	        if(chain.hasExtractors()) {
	        	extractor = chain.getExtractors().get(0).getName();
	        	params = chain.getExtractors().get(0).getParams();
	        }
	        
	        //for now lets just assume 1 extractor at index 0 TODO
	        float[][] trainingFeatures = anno.extractGivenAMethod(extractor, params, trainingProblem);
	        float[][] testingFeatures = anno.extractGivenAMethod(extractor, params, testingProblem);	        
	        
	        //clear data memory
	        //trainingProblem.setDataNull();
	        //testingProblem.setDataNull();
	
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
	        
	        //loop for each annotation target (one image may have multiple labels)
	        for (int i = 0; i < numOfAnno; i++) {
            	ComboFeatures combo = null;
	            if(chain.hasSelectors()) 
	            {
	                pnlOutput.setOutput("Selecting features...");
	                
	                //Apply each feature selector in the chain
	                for(Selector selector : chain.getSelectors()) {
		            	//Supervised feature selectors need corresponding target data
		                combo = anno.selectGivenAMethod(selector.getName(), selector.getParams(), trainingFeatures, testingFeatures, trainingTargets[i], testingTargets[i]);
		                //selected features overrides the passed in original features
		                trainingFeatures = combo.getTrainingFeatures();
		                testingFeatures = combo.getTestingFeatures();
	                }
	            }
	            numoffeatures = trainingFeatures[0].length;
	
	            //pass the training and testing data to Validator
	            //get rate and prediction results for testing data
	            float rate = 0;
	            pnlOutput.setOutput("Classifying/Annotating...");
	            
	            Classifier classifierObj = anno.getClassifierGivenName(chain.getClassifier(), chain.getClassParams());
	            try {
	            	rate = anno.classifyGivenAMethod(classifierObj, chain.getClassParams(), trainingFeatures, testingFeatures, trainingTargets[i], testingTargets[i], annotations[i]);
	            }
	            catch(Exception ex) {
	            	ex.printStackTrace();
	            }
	            
	            rates[row][i] = rate;
	            
	            //If rate for this target(ith target) is better with this chain,
	            //then, save this as new best model
	            if(rate > chainModels[i].getResult()) {
	            	chainModels[i].setExtractors(chain.getExtractors());
	            	chainModels[i].setSelectors(chain.getSelectors());

	            	if(combo != null)
	            		chainModels[i].setSelectedIndices(combo.getSelectedIndices());
	            	else
	            		chainModels[i].setSelectedIndices(null);
	            	
	            	chainModels[i].setClassifierName(chain.getClassifier());
	            	chainModels[i].setClassifier(classifierObj);
	            	chainModels[i].setClassParams(chain.getClassParams());
	            	chainModels[i].setResult(rate);
	            }	            	
	            
	            System.out.println(rate);
	                
	            pnlOutput.setOutput("Recog Rate for " + anno.getAnnotationLabels().get(i) + ": " + rate);
	            if (!setProgress(50 + (i + 1) * 50 / numOfAnno)) {
	                return;
	            }
	        }//end of loop for annotation targets
        }//End of loop for chains
        
        //Display result
        if(executed)	//Display result if at least one chain is executed
        	gui.addTab("Auto Comparison Results", rates, anno.getAnnotationLabels(), chainNames, imgWidth, imgHeight, channel);
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
