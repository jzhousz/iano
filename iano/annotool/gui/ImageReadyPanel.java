package annotool.gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import annotool.AnnOutputPanel;
import annotool.AnnTablePanel;
import annotool.Annotation;
import annotool.Annotator;
import annotool.gui.model.ModelFilter;
import annotool.gui.model.ModelHelper;
import annotool.gui.model.ModelLoader;
import annotool.gui.model.PDFFilter;
import annotool.io.ReportSaver;

public class ImageReadyPanel extends JPanel implements ActionListener
{
	private JPanel pnlRight, pnlRightCenter, pnlDynamic, 
				   pnlLegends,
				   pnlModeInfo, pnlChannel, pnlButton;
	private ROIParameterPanel pnlROIParam = null;
	
	JLabel lbModeInfo;
	JRadioButton rbRed, rbGreen, rbBlue;
	JButton btnSimple, btnAutoComp,
			btnLoadModel, btnApplyModel, btnSaveReport, btnViewModels;
	
	String[] channels = {  "red (channel 1)", "green (channel 2)", "blue (channel 3)" };
	String[] channelInputs = {  "r", "g", "b" };//actual input to algorithm
	
	private AnnOutputPanel pnlStatus;
	private AnnTablePanel pnlTable;
	
	AnnotatorGUI gui = null;
	
	String modelInfo = null;
	
	private boolean is3D = false;
	
	private int openFrameCount = 0;
	
	private ModelLoader loader = null;
	
	//File chooser and context specific file filters to use with the file chooser
	JFileChooser fileChooser = new JFileChooser();
	private ModelFilter modelFilter = new ModelFilter();
	private PDFFilter pdfFilter = new PDFFilter();
	
	ArrayList<PopUpFrame> openFrames = new ArrayList<PopUpFrame>();
		
	public ImageReadyPanel(AnnotatorGUI gui) {
		this.gui = gui;
		
		//Center panel for displaying loaded images		
		pnlTable = new AnnTablePanel(gui);
		//Text area for status
		pnlStatus = new AnnOutputPanel();
		
		//Mode information
		pnlModeInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
		lbModeInfo = new JLabel();	
		pnlModeInfo.add(lbModeInfo);		
		
		createChannelPanel();
		createButtonsPanel();
		
		//Panel for center part of right panel
		pnlRightCenter = new JPanel(new BorderLayout());
		pnlRightCenter.add(pnlChannel, BorderLayout.NORTH);
		
		//Container for dynamic panels
		pnlDynamic = new JPanel();
		pnlDynamic.setLayout(new BoxLayout(pnlDynamic, BoxLayout.PAGE_AXIS));
		pnlRightCenter.add(pnlDynamic, BorderLayout.CENTER);
		
		//Container for legends
		pnlLegends = new JPanel();
		pnlLegends.setLayout(new BoxLayout(pnlLegends, BoxLayout.PAGE_AXIS));
		pnlDynamic.add(pnlLegends);	
			
		//Add components to right side bar
		pnlRight = new JPanel(new BorderLayout());
		pnlRight.add(pnlModeInfo, BorderLayout.NORTH);
		pnlRight.add(pnlRightCenter, BorderLayout.CENTER);
		pnlRight.add(pnlButton, BorderLayout.SOUTH);
		
		
		//Add components to top level container
		this.setLayout(new BorderLayout());
		this.add(pnlTable, BorderLayout.CENTER);
		this.add(pnlRight, BorderLayout.EAST);
		this.add(pnlStatus, BorderLayout.SOUTH);
		
		//File chooser should show all files option
		fileChooser.setAcceptAllFileFilterUsed(false);
		
	}
	
	//Creates the panel with buttons
	//The actually addition of  buttons to pnlButton id done in setMode method since separate modes require separate button sets
	private void createButtonsPanel() {
		//Panel for buttons
		pnlButton = new JPanel();
		
		//Expert and Auto Comparison buttons
		btnSimple = new JButton("Simple");
		btnSimple.addActionListener(this);
		btnAutoComp = new JButton("Comparison");
		btnAutoComp.addActionListener(this);
		
		//Load and model button
		btnApplyModel = new JButton("Apply Model");
		btnApplyModel.addActionListener(this);
		
		btnLoadModel = new JButton("Load Model(s)");
		btnLoadModel.addActionListener(this);
		
		btnViewModels = new JButton("View Model(s)");
		btnViewModels.addActionListener(this);
		
		btnSaveReport = new JButton("Save Report");
		btnSaveReport.addActionListener(this);	
	}
	
	//Creates the panel with radio buttons for channel selection
	private void createChannelPanel() {
		//Channel selection panel
		pnlChannel = new JPanel();
		pnlChannel.setLayout(new GridLayout(3, 1));
		pnlChannel.setBorder(BorderFactory.createTitledBorder(null, "Channel", 
				TitledBorder.LEFT, TitledBorder.TOP));
		
		//Channel radio buttons
		rbRed = new JRadioButton(channels[0]);
		rbGreen = new JRadioButton(channels[1]);
		rbBlue = new JRadioButton(channels[2]);
		
		//Group radio buttons
		ButtonGroup channelGroup = new ButtonGroup();
		channelGroup.add(rbRed);
		channelGroup.add(rbGreen);
		channelGroup.add(rbBlue);
		
		rbGreen.setSelected(true);     //Default channel of green
		
		//Add radio buttons to container
		pnlChannel.add(rbRed);
		pnlChannel.add(rbGreen);
		pnlChannel.add(rbBlue);
	}
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnSimple) {
			if(rbRed.isSelected())
				Annotator.channel = channelInputs[0];
			else if(rbGreen.isSelected())
				Annotator.channel = channelInputs[1];
			else if(rbBlue.isSelected())
				Annotator.channel = channelInputs[2];
			
			ExpertFrame ef = new ExpertFrame("Simple Mode", is3D, Annotator.channel, pnlTable.getTrainingProblem(),
					pnlTable.getTestingProblem());
			ef.setVisible(true);
			//ef.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			ef.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			
			//Keep track of opened frames
			openFrames.add(ef);
			openFrameCount++;
			gui.setMenuEnabled(false);	//Disable new wizard item
			ef.addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent evt) {
	            	PopUpFrame frame = (PopUpFrame)evt.getSource();
	            	frameClosed(frame);
	            }
	        });
			ef.pack();
			
			Dimension dim =
				Toolkit.getDefaultToolkit().getScreenSize();
			int x = (int)(dim.getWidth() - getWidth())/2;
			int y = (int)(dim.getHeight() - getHeight())/2;
			ef.setLocation(x,y);
		}
		else if(e.getSource() == btnAutoComp) {
			if(rbRed.isSelected())
				Annotator.channel = channelInputs[0];
			else if(rbGreen.isSelected())
				Annotator.channel = channelInputs[1];
			else if(rbBlue.isSelected())
				Annotator.channel = channelInputs[2];
			
			AutoCompFrame frame = new AutoCompFrame("Auto Comparison Mode", is3D, Annotator.channel, pnlTable.getTrainingProblem(),
					pnlTable.getTestingProblem());			
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			
			//Keep track of opened frames
			openFrames.add(frame);
			openFrameCount++;
			gui.setMenuEnabled(false);	//Disable new wizard item
			frame.addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent evt) {
	            	PopUpFrame frame = (PopUpFrame)evt.getSource();
	            	frameClosed(frame);
	            }
	        });
			
			frame.pack();
			Dimension dim =
				Toolkit.getDefaultToolkit().getScreenSize();
			int x = (int)(dim.getWidth() - getWidth())/2;
			int y = (int)(dim.getHeight() - getHeight())/2;
			frame.setLocation(x,y);
		}
		else if(e.getSource() == btnLoadModel) {
			//Set file chooser behavior for this(load model) context			
			fileChooser.resetChoosableFileFilters();
			fileChooser.addChoosableFileFilter(modelFilter);
			
			if(Annotator.output.equals(Annotator.AN)) {
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			}
			else if (Annotator.output.equals(Annotator.ROI)){	//ROI mode should only allow single model selection
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			}
				
			
			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.CANCEL_OPTION)
				return;
			
			//Otherwise, proceed with loading of selected file(s)			
			//gui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			loader = new ModelLoader(this);
			
			/*if(Annotator.output.equals(Annotator.AN) && loader.loadModels(fileChooser.getSelectedFiles())) {
				btnApplyModel.setEnabled(true);
				btnViewModels.setEnabled(true);
			}
			else if(Annotator.output.equals(Annotator.ROI) && loader.loadModel(fileChooser.getSelectedFile())) {
				btnApplyModel.setEnabled(true);
				btnViewModels.setEnabled(true);
			}*/
			if(Annotator.output.equals(Annotator.AN)) {
				loader.setFiles(fileChooser.getSelectedFiles());
				loader.load();
			}
			else if(Annotator.output.equals(Annotator.ROI)) {
				loader.setFile(fileChooser.getSelectedFile());
				loader.load();
			}
			
			//gui.setCursor(Cursor.getDefaultCursor());
		}
		else if(e.getSource() == btnApplyModel) {
			//If ROI mode, check if at least one image is selected
			if(Annotator.output.equals(Annotator.ROI)) {
				int[] selectedRows = pnlTable.getAnnotationTable().getSelectedRows();
				if(selectedRows == null || selectedRows.length < 1) {
					JOptionPane.showMessageDialog(this,
						    "Select one or more image for ROI annotation.", 
						    "No image selected",
						    JOptionPane.INFORMATION_MESSAGE);
					return;
				}
			}
			
			//Check if model has same information for channel as the current channel selection
			if(!loader.validate()) {
				int choice = JOptionPane.showConfirmDialog(this,
					    "Channel information in the loaded model(s) is different than the selected one.\n" +
					    "Do you still want to apply?",
					    "Confirmation",
					    JOptionPane.OK_CANCEL_OPTION,
					    JOptionPane.INFORMATION_MESSAGE);
				
				if(choice == JOptionPane.CANCEL_OPTION) {
					pnlStatus.setOutput("Annotation cancelled by user.");
					return;
				}
			}
			loader.applyModel();
		}
		else if(e.getSource() == btnSaveReport) {
			//Change file chooser behavior to match this context
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.resetChoosableFileFilters();
			fileChooser.addChoosableFileFilter(pdfFilter);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			
			int returnVal = fileChooser.showSaveDialog(this);
			
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fileChooser.getSelectedFile();
	            String filePath = file.getPath();
	            if(!filePath.toLowerCase().endsWith(".pdf")) {
	            	file = new File(filePath + ".pdf");
	            }
	            
	            ReportSaver reportSaver = new ReportSaver();
	            boolean success = reportSaver.saveAnnotationReport(file, loader.getAnnotations(), loader.getClassNames(), loader.getModelLabels(), loader.getSupportsProb(),
	            		pnlTable.getAnnotationTable().getChildren(),
	            		loader.isBinary());
	            
	            if(success)
	            	pnlStatus.setOutput("Report saved: " + file.getAbsolutePath());
	            else
	            	pnlStatus.setOutput("Failed to save report.");
	        }
		}
		else if(e.getSource() == btnViewModels) {
			JOptionPane.showMessageDialog(this, 
					ModelHelper.getModelInfo(loader.getChainModels()), 
					"Loaded Models", 
					JOptionPane.INFORMATION_MESSAGE);
		}
	}
	public AnnTablePanel getTablePanel() {
		return pnlTable;
	}
	public AnnOutputPanel getOutputPanel() {
		return pnlStatus;
	}
	public void channelEnabled(boolean flag) {
		rbRed.setEnabled(flag);
		rbGreen.setEnabled(flag);
		rbBlue.setEnabled(flag);
	}
	public void setIs3D(boolean flag) {
		this.is3D = flag;
	}
	public void setMode() {
		//Remove roi parameter panel if it exists and reset legends panel
		if(pnlROIParam != null) {
			pnlDynamic.remove(pnlROIParam);
			pnlROIParam = null;
			pnlDynamic.revalidate();
		}
		pnlLegends.removeAll();
		pnlLegends.setBorder(BorderFactory.createEmptyBorder());
		pnlLegends.revalidate();
		gui.pack();
		
		//Information panel with label to display info		
		if(Annotator.output.equals(Annotator.TT)) {
			modelInfo = "Testing/Training";
			btnAutoComp.setEnabled(true);
		}
		else if(Annotator.output.equals(Annotator.CV)) {
			modelInfo = "Cross Validation. " + "Fold: " + Annotator.fold;
			btnAutoComp.setEnabled(true);
		}
		else if(Annotator.output.equals(Annotator.TO)) {
			modelInfo = "Train Only";
			btnAutoComp.setEnabled(false);
		}
		else if(Annotator.output.equals(Annotator.AN)) {
			modelInfo = "Image Annotation";
		}
		else if(Annotator.output.equals(Annotator.ROI)) {
			modelInfo = "ROI Annotation";
			createAndShowROIParam();
		}		
		
		//Add or remove appropriate buttons
		pnlButton.removeAll();
		
		if(Annotator.output.equals(Annotator.AN) || Annotator.output.equals(Annotator.ROI)) {
			pnlButton.setLayout(new GridLayout(2, 2));
			pnlButton.add(btnLoadModel);
			
			if(loader == null || loader.getChainModels().size() < 1) {
				btnApplyModel.setEnabled(false);
				btnViewModels.setEnabled(false);
			}
			else
			{
				btnApplyModel.setEnabled(true);
				btnViewModels.setEnabled(true);
			}
			pnlButton.add(btnApplyModel);
			pnlButton.add(btnViewModels);
			
			btnSaveReport.setEnabled(false);
			pnlButton.add(btnSaveReport);
		}
		else {
			pnlButton.setLayout(new GridLayout(1, 2));
			pnlButton.add(btnSimple);
			pnlButton.add(btnAutoComp);
		}
		
		lbModeInfo.setText("<html><b>Mode: </b>" + modelInfo + "</html>");
	}
	
	public void enableSaveReport(boolean state){
		btnSaveReport.setEnabled(state);
	}
	
	public String getSelectedChannel() {
		String selectedChannel = null;
		
		if(rbRed.isSelected())
			selectedChannel = channelInputs[0];
		else if(rbGreen.isSelected())
			selectedChannel = channelInputs[1];
		else if(rbBlue.isSelected())
			selectedChannel = channelInputs[2];
		
		return selectedChannel;
	}
	
	/**
	 * Displays simple statistics for the annotation results
	 * 
	 * @param classNames
	 * @param annotations
	 * @param modelLabels
	 */
	public void showStats(HashMap<String, String> classNames, Annotation[][] annotations, String[] modelLabels) {
		JLabel lbInfo = new JLabel(ModelHelper.getStatsInfo(classNames, annotations, modelLabels));
		pnlLegends.removeAll();
		pnlLegends.setBorder(BorderFactory.createTitledBorder(null, "Identified Classes", 
				TitledBorder.LEFT, TitledBorder.TOP));
		pnlLegends.add(lbInfo);		
		pnlLegends.revalidate();
		pnlLegends.repaint();
		gui.pack();
	}
	
	public void showClassLegends() {
		if(pnlTable.getClassNames() != null) {
			JLabel lbInfo = new JLabel(ModelHelper.getClassNamesInfo(pnlTable.getClassNames()));
			pnlLegends.removeAll();
			pnlLegends.setBorder(BorderFactory.createTitledBorder(null, "Classes", 
					TitledBorder.LEFT, TitledBorder.TOP));
			pnlLegends.add(lbInfo);		
			pnlLegends.revalidate();
			pnlLegends.repaint();
			gui.pack();
		}
	}
	
	private void createAndShowROIParam() {
		pnlROIParam = new ROIParameterPanel();
		pnlDynamic.add(pnlROIParam);
		pnlROIParam.revalidate();
		pnlROIParam.repaint();
		gui.pack();
	}
	
	/**
	 * Called when the PopUp frame is closed.
	 * Removes the frame from a list of open frames. If none of the frames are open, enables the menu items (New Wizard etc).
	 * 
	 * Also, it checks if the frame initiated annotation process. If so, it closes all the other pop up frames and takes the user to annotation mode selection.
	 * 
	 * @param frame
	 */
	private void frameClosed(PopUpFrame frame) {
		if(openFrames.contains(frame))
    		openFrames.remove(frame);
        
    	openFrameCount--;
    	if(openFrameCount < 1)
    		gui.setMenuEnabled(true); //Enable new wizard etc when all pop up frames are closed
    	
    	//If this frame initiated apply model, then get chainModels in memory and close all pop up frames
    	if(frame.isApplyModelFired()) {
    		loader = new ModelLoader(this);
    		loader.setChainModelsFromArray(frame.getChainModels());
    		
    		for(PopUpFrame aFrame : openFrames)
    			aFrame.pullThePlug();
    		openFrameCount = 0;    
    		
    		gui.initAnnotationWizard();
    	}
    	
    	//Get rid of the frame
    	frame.setVisible(false);
    	frame.dispose();
	}
	/**
	 * Resets the image ready panel minimal state. Used when starting new wizard
	 */
	public void reset() {
		rbGreen.setSelected(true);
		pnlLegends.removeAll();
		pnlLegends.setBorder(BorderFactory.createEmptyBorder());
		
		if(pnlROIParam != null) {
			pnlDynamic.remove(pnlROIParam);
			pnlROIParam = null;
		}
			
		pnlTable.removeTables();
	}
	public void updateLookAndFeelForOpenFrames() {
		for(PopUpFrame frame : openFrames)
			SwingUtilities.updateComponentTreeUI(frame);
	}

	public ROIParameterPanel getPnlROIParam() {
		return pnlROIParam;
	}
	
	public void setButtonsEnabled(boolean flag) {
		this.btnLoadModel.setEnabled(flag);
		this.btnApplyModel.setEnabled(flag);
	}
	public void setButtonsEnabledOnModelLoad(boolean flag) {
		btnLoadModel.setEnabled(flag);
		btnApplyModel.setEnabled(flag);
		btnViewModels.setEnabled(flag);
	}
}