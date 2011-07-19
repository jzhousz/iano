package annotool.gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;

import annotool.AnnOutputPanel;
import annotool.AnnTablePanel;
import annotool.Annotation;
import annotool.Annotator;
import annotool.gui.model.ModelFilter;
import annotool.gui.model.ModelLoader;
import annotool.gui.model.Utils;
import annotool.io.ChainModel;
import annotool.io.ReportSaver;

public class ImageReadyPanel extends JPanel implements ActionListener
{
	private JPanel pnlRight, pnlRightCenter,
				   pnlModelInfo, pnlChannel, pnlButton;
	
	JLabel lbModelInfo;
	JRadioButton rbRed, rbGreen, rbBlue;
	JButton btnExpert, btnAutoComp,
			btnLoadModel, btnApplyModel, btnSaveReport;
	
	String[] channels = {  "red (channel 1)", "green (channel 2)", "blue (channel 3)" };
	String[] channelInputs = {  "r", "g", "b" };//actual input to algorithm
	
	private AnnOutputPanel pnlStatus;
	private AnnTablePanel pnlTable;
	AnnotatorGUI gui = null;
	
	String modelInfo = null;
	
	private boolean is3D = false;
	
	private int openFrameCount = 0;
	
	private ModelLoader loader = null;
	JFileChooser fileChooser = new JFileChooser();
	private Annotation[][] annotations = null;
	
	public ImageReadyPanel(AnnotatorGUI gui) {
		this.gui = gui;		
		
		lbModelInfo = new JLabel();
		
		pnlModelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlModelInfo.setBorder(new EmptyBorder(5,5,5,5));
		pnlModelInfo.add(lbModelInfo);		
		
		//Channel selection panel
		pnlChannel = new JPanel();
		pnlChannel.setLayout(new GridLayout(3, 1));
		pnlChannel.setBorder(new CompoundBorder(new TitledBorder(null, "Channel", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5)));
		
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
		
		//Panel for center part of right panel
		pnlRightCenter = new JPanel(new BorderLayout());
		pnlRightCenter.add(pnlChannel, BorderLayout.NORTH);
		
		//Expert and Auto Comparison buttons
		btnExpert = new JButton("Expert");
		btnExpert.addActionListener(this);
		btnAutoComp = new JButton("Auto Comp");
		btnAutoComp.addActionListener(this);
		
		//Load and model button
		btnApplyModel = new JButton("Apply Model");
		btnApplyModel.addActionListener(this);
		
		btnLoadModel = new JButton("Load Model");
		btnLoadModel.addActionListener(this);
		
		btnSaveReport = new JButton("Save Report");
		btnSaveReport.addActionListener(this);
		
		//Panel for buttons
		pnlButton = new JPanel();
		//pnlButton.setLayout(new GridLayout(1, 2));
		//pnlButton.add(btnExpert);
		//pnlButton.add(btnAutoComp); 			
			
		//Add components to right side bar
		pnlRight = new JPanel(new BorderLayout());
		pnlRight.add(pnlModelInfo, BorderLayout.NORTH);
		pnlRight.add(pnlRightCenter, BorderLayout.CENTER);
		pnlRight.add(pnlButton, BorderLayout.SOUTH);
		
		//Text area for status
		pnlStatus = new AnnOutputPanel();
		
		//Center panel for displaying loaded images		
		pnlTable = new AnnTablePanel(gui);
		
		//Add components to top level container
		this.setLayout(new BorderLayout());
		this.add(pnlRight, BorderLayout.EAST);
		this.add(pnlStatus, BorderLayout.SOUTH);
		this.add(pnlTable, BorderLayout.CENTER);
		
	}
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnExpert) {
			if(rbRed.isSelected())
				Annotator.channel = channelInputs[0];
			else if(rbGreen.isSelected())
				Annotator.channel = channelInputs[1];
			else if(rbBlue.isSelected())
				Annotator.channel = channelInputs[2];
			
			ExpertFrame ef = new ExpertFrame("Expert Mode", is3D, Annotator.channel);			
			ef.setVisible(true);
			ef.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			openFrameCount++;
			gui.setNewWizardEnabled(false);	//Disable new wizard item
			ef.addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent evt) {
	            	//ef.setVisible(false);
	            	//ef.getContentPane().removeAll();
	            	//ef.dispose();
	            	openFrameCount--;
	            	if(openFrameCount < 1)
	            		gui.setNewWizardEnabled(true); //Enable new wizard when all pop up frames are closed
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
			
			AutoCompFrame frame = new AutoCompFrame("Auto Comparison Mode", is3D, Annotator.channel);			
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			openFrameCount++;
			gui.setNewWizardEnabled(false);	//Disable new wizard item
			frame.addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent evt) {
	            	//frame.setVisible(false);
	            	//frame.getContentPane().removeAll();
	            	//frame.dispose();
                    
	            	openFrameCount--;
	            	if(openFrameCount < 1)
	            		gui.setNewWizardEnabled(true); //Enable new wizard when all pop up frames are closed
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
			loader = new ModelLoader(this);
			if(loader.loadModels()) {
				btnApplyModel.setEnabled(true);
			}
		}
		else if(e.getSource() == btnApplyModel) {	
			//If it is image classification mode, load the model first
			if(Annotator.output.equals(Annotator.CL)) {
				loader = new ModelLoader(this);
				if(!loader.loadModel()) {					
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
			annotations = loader.getAnnotations();
		}
		else if(e.getSource() == btnSaveReport) {
			int returnVal = fileChooser.showSaveDialog(this);
			
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fileChooser.getSelectedFile();
	            String filePath = file.getPath();
	            if(!filePath.toLowerCase().endsWith(".pdf")) {
	            	file = new File(filePath + ".pdf");
	            }
	            
	            ReportSaver reportSaver = new ReportSaver();
	            reportSaver.saveAnnotationReport(file, annotations);
	        }
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
		
		//Add or remove appropriate buttons
		pnlButton.removeAll();
		if(Annotator.output.equals(Annotator.CL)) {
			modelInfo = "Image Classification";
			pnlButton.setLayout(new GridLayout(1, 2));
			btnApplyModel.setText("Load/Apply Model");
			pnlButton.add(btnApplyModel);
			
			btnSaveReport.setEnabled(false);
			pnlButton.add(btnSaveReport);
		}
		else if(Annotator.output.equals(Annotator.AN)) {
			modelInfo = "Image Annotation";
			pnlButton.setLayout(new GridLayout(2, 2));
			pnlButton.add(btnLoadModel);
			
			btnApplyModel.setText("Apply Model");
			btnApplyModel.setEnabled(false);
			pnlButton.add(btnApplyModel);
			
			btnSaveReport.setEnabled(false);
			pnlButton.add(btnSaveReport);
		}
		else {
			pnlButton.setLayout(new GridLayout(1, 2));
			pnlButton.add(btnExpert);
			pnlButton.add(btnAutoComp);
		}
		
		lbModelInfo.setText("<html><b>Mode: </b>" + modelInfo + "</html>");
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
}