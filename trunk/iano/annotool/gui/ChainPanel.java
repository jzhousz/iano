package annotool.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
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
import javax.swing.table.TableCellRenderer;

import annotool.AnnOutputPanel;
import annotool.Annotation;
import annotool.Annotator;
import annotool.ComboFeatures;
import annotool.classify.Classifier;
import annotool.classify.SavableClassifier;
import annotool.io.ChainIO;
import annotool.io.ChainModel;
import annotool.io.DataInput;

public class ChainPanel extends JPanel implements ActionListener, ListSelectionListener, Runnable{
	private JPanel pnlMain, pnlControl, pnlDetail,
				   pnlTable, pnlButton,
				   pnlSouth;
	private JTable tblChain = null;
	private JScrollPane scrollPane = null;
	
	private JButton btnNew, btnRemove, 
					btnSave, btnLoad, btnRun;
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
	
	public ChainPanel(AutoCompFrame gui, String channel, AnnOutputPanel pnlOutput) {
		this.channel = channel;		
		this.gui = gui;
		gui.setButtonsEnabled(false);
		this.pnlOutput = pnlOutput;
		
		this.setLayout(new BorderLayout());
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		pnlTable = new JPanel(new BorderLayout());
		pnlControl = new JPanel();
		pnlDetail = new JPanel(new BorderLayout());
		pnlDetail.setBorder(new CompoundBorder(new TitledBorder(null, "Selected Chain Detail", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(10, 10, 10, 10)));
		pnlDetail.add(detailPane, BorderLayout.CENTER);
		pnlDetail.setPreferredSize(new java.awt.Dimension(300, 200));
		taDetail.setMargin(new Insets(10,10,10,10));
		taDetail.setEditable(false);
		
		tblChain = new JTable(tableModel);
		tblChain.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblChain.getSelectionModel().addListSelectionListener(this);
		    
		//tblChain.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblChain.getColumnModel().getColumn(0).setPreferredWidth(30);
		tblChain.getColumnModel().getColumn(1).setPreferredWidth(320);
		scrollPane = new JScrollPane(tblChain);
		scrollPane.setPreferredSize(new java.awt.Dimension(350, 150));
		pnlTable.add(scrollPane, BorderLayout.CENTER);
		
		btnNew = new JButton("New");
		btnNew.addActionListener(this);
		btnRemove = new JButton("Remove");
		btnRemove.setEnabled(false);
		btnRemove.addActionListener(this);

		pnlControl.add(btnNew);
		pnlControl.add(btnRemove);
		
		btnSave = new JButton("Save Chain");
		btnSave.setEnabled(false);
		btnSave.addActionListener(this);
		btnLoad = new JButton("Load Chain");
		btnLoad.addActionListener(this);
		btnRun = new JButton("Run");
		btnRun.setEnabled(false);
		btnRun.addActionListener(this);
		
		pnlButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlButton.add(btnSave);
		pnlButton.add(btnLoad);
		pnlButton.add(btnRun);
		
		pnlMain = new JPanel(new BorderLayout());
		pnlMain.add(pnlTable, BorderLayout.CENTER);
		pnlMain.add(pnlButton, BorderLayout.SOUTH);
		
		bar = new JProgressBar(0, 100);
		bar.setValue(0);
		bar.setStringPainted(true);
		
		pnlSouth  = new JPanel(new BorderLayout());
		pnlSouth.add(bar, BorderLayout.SOUTH);
		pnlSouth.add(pnlDetail, BorderLayout.CENTER);
		
		this.add(pnlMain, BorderLayout.CENTER);
		this.add(pnlControl, BorderLayout.EAST);
		this.add(pnlSouth, BorderLayout.SOUTH);
	}
	
	public void actionPerformed(ActionEvent ev) {
		if(ev.getSource().equals(btnNew)) {
			//Check if last chain in the table is complete
			int size = tblChain.getRowCount();
			if(size > 0) {
				Chain lastChain = (Chain)tblChain.getValueAt(size - 1, 1);
				if(!lastChain.isComplete()) {
					JOptionPane.showMessageDialog(this,
						    "The last chain is not yet complete.", 
						    "Incomplete Chain",
						    JOptionPane.INFORMATION_MESSAGE);
					return;
				}
			}			
			Object[] rowData = {new Boolean(false), new Chain()};
			tableModel.insertNewRow(rowData);
			tblChain.changeSelection(tableModel.getRowCount() - 1, 1, false, false);
			setButtonState();
		}
		else if(ev.getSource().equals(btnRemove)) {
			tableModel.removeRow(tblChain.getSelectedRow());
			taDetail.setText("");
			setButtonState();
		}
		else if(ev.getSource().equals(btnSave)) {
			//Save chains to file
			ArrayList<Chain> chainList = new ArrayList<Chain>();
        	for(int i = 0; i < tableModel.getRowCount(); i++) {
        		chainList.add((Chain)tableModel.getValueAt(i, 1));//Second column is chain object
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
	            		Object[] rowData = {new Boolean(false), chain};
	        			tableModel.insertNewRow(rowData);	        			
	            	}
	            	pnlOutput.setOutput("Chain successfully loaded.");
	            }
	            catch (Exception ex) {
	            	System.out.println("Exception thrown while loading chain list from file.");
	        		ex.printStackTrace();
	        		pnlOutput.setOutput("Load failed.");
	            }
	            
	            //Enable/disable buttons based on whether has rows or not
	            setButtonState();
	        }
		}
		else if(ev.getSource().equals(btnRun)) {
			//Check if the last chain is complete
			int size = tblChain.getRowCount();
			if(size > 0) {
				Chain lastChain = (Chain)tblChain.getValueAt(size - 1, 1);
				if(!lastChain.isComplete()) {
					JOptionPane.showMessageDialog(this,
						    "The last chain is not yet complete.", 
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
	public void addExtractor(Extractor ex) {
		int currentRow = tblChain.getSelectedRow();
		if(currentRow < 0)
			return;
		Chain chain = (Chain)tblChain.getValueAt(currentRow, 1);
		if(ex.getName().equalsIgnoreCase("None") && chain.hasExtractors())	//Cannot add "None" as extractor if another extractor has already been added
			return;
		chain.addExtractor(ex);
		tblChain.repaint();
		showItemDetail();
	}
	public void addSelector(String name, HashMap<String, String> params) {
		int currentRow = tblChain.getSelectedRow();
		if(currentRow < 0)
			return;
		Chain chain = (Chain)tblChain.getValueAt(currentRow, 1);		
		chain.setSelector(name);
		chain.setSelParams(params);
		tblChain.repaint();
		showItemDetail();
	}
	public void addClassifier(String name, HashMap<String, String> params) {
		int currentRow = tblChain.getSelectedRow();
		if(currentRow < 0)
			return;
		Chain chain = (Chain)tblChain.getValueAt(currentRow, 1);
		chain.setClassifier(name);
		chain.setClassParams(params);
		tblChain.repaint();
		showItemDetail();
	}
	private void showItemDetail() {
		int currentRow = tblChain.getSelectedRow();
		if(currentRow < 0)
			return;
		
		final Chain chain = (Chain)tblChain.getValueAt(currentRow, 1);
		taDetail.setText("");
		if(chain.getExtractors().size() > 0) {
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			taDetail.setText(taDetail.getText() + "FEATURE EXTRACTOR(S):\n");
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			for(Extractor ex : chain.getExtractors()) {
				taDetail.setText(taDetail.getText() + ex.getName() + "\n");
				for (String parameter : ex.getParams().keySet()) {
					taDetail.setText(taDetail.getText() + parameter + "=" +ex.getParams().get(parameter) + "\n");
	        	}
				taDetail.setText(taDetail.getText() + "\n");
			}
		}
		if(chain.getSelector() != null) {
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			taDetail.setText(taDetail.getText() + "FEATURE SELECTOR:\n");
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			taDetail.setText(taDetail.getText() + chain.getSelector() + "\n");
			for (String parameter : chain.getSelParams().keySet()) {
				taDetail.setText(taDetail.getText() + parameter + "=" +chain.getSelParams().get(parameter) + "\n");
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
		
		taDetail.setCaretPosition(taDetail.getText().length());
	}

	@Override
	public void run() {
		//Disable buttons
		btnRun.setEnabled(false);
		btnNew.setEnabled(false);
		btnRemove.setEnabled(false);
		btnLoad.setEnabled(false);
		gui.setButtonsEnabled(false);
		
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
		gui.setButtonsEnabled(true);
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
        
        
        //Initialize float array to hold rates for each annotation for each chain
        float[][] rates = new float[tableModel.getRowCount()][numOfAnno];
        
        for(int row = 0; row < tableModel.getRowCount(); row++) {
        	selectRow(row);
        	//feature extraction.
            if (!setProgress(30))  {
                return;
            }
            
        	Chain chain = (Chain)tableModel.getValueAt(row, 1);//Second column is chain object
        	
        	pnlOutput.setOutput("Processing chain " + (row+1) + ":");
        	
        	if(!chain.isComplete()) {
        		pnlOutput.setOutput("Incomplete chain encountered. Chain = " + (row + 1));
        		continue;
        	}
        	
	        pnlOutput.setOutput("Extracting features...");
	        
	        //for now lets just assume 1 extractor at index 0 TODO
	        float[][] trainingFeatures = anno.extractGivenAMethod(chain.getExtractors().get(0).getName(), chain.getExtractors().get(0).getParams(), trainingProblem);
	        float[][] testingFeatures = anno.extractGivenAMethod(chain.getExtractors().get(0).getName(), chain.getExtractors().get(0).getParams(), testingProblem);
	        
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
	        
	        //loop for each annotation target (one image may have multiple labels)
	        for (int i = 0; i < numOfAnno; i++) {
	        	
	            if (chain.getSelector().equalsIgnoreCase("None")) { //use the original feature without selection -- overwrite numoffeatures value
	            	numoffeatures = trainingFeatures[0].length;
	            }
	            else 
	            {
	                pnlOutput.setOutput("Selecting features...");
	            	//Supervised feature selectors need corresponding target data
	                ComboFeatures combo = anno.selectGivenAMethod(chain.getSelector(), chain.getSelParams(), trainingFeatures, testingFeatures, trainingTargets[i], testingTargets[i]);
	                //selected features overrides the passed in original features
	                trainingFeatures = combo.getTrainingFeatures();
	                testingFeatures = combo.getTestingFeatures();
	                numoffeatures = trainingFeatures[0].length;
	            }
	
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
	            
	            System.out.println(rate);
	                
	            pnlOutput.setOutput("Recog Rate for " + anno.getAnnotationLabels().get(i) + ": " + rate);
	            if (!setProgress(50 + (i + 1) * 50 / numOfAnno)) {
	                return;
	            }
	        }//end of loop for annotation targets
        }//End of loop for chains
        
        //Display result
        gui.addTab("Auto Comparison Results", rates, anno.getAnnotationLabels());
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
}
