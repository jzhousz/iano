package annotool.gui;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.*;

import annotool.AnnOutputPanel;

public class ROITagger extends JDialog implements ActionListener {
	private AnnOutputPanel pnlStatus = new AnnOutputPanel();
	private ImagePlus imp = null;
	private JButton btnLoadImg = null;
	private JButton btnLoadROI = null;
	private JButton btnSave = null;
	
	//File chooser and context specific file filters to use with the file chooser
	final JFileChooser fileChooser = new JFileChooser();
	
	public ROITagger(JFrame parent, String title, boolean isModal) {
		super(parent, title, isModal);
		
		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		
		container.add(createRightColumn(), BorderLayout.EAST);
		container.add(pnlStatus, BorderLayout.SOUTH);
		
		pack();
		ij.gui.GUI.center(this);
		setVisible(true);
	}
	
	private JPanel createRightColumn() {
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new GridLayout(3, 1, 2, 2));
		
		btnLoadImg = new JButton("Load Image");
		btnLoadImg.addActionListener(this);
		
		btnLoadROI = new JButton("ROI Manager");
		btnLoadROI.addActionListener(this);
		
		btnSave = new JButton("Save");
		btnSave.addActionListener(this);
		
		rightPanel.add(btnLoadImg);
		rightPanel.add(btnLoadROI);
		rightPanel.add(btnSave);
		
		return rightPanel;
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		if(ev.getSource() == btnLoadImg) {
			//fileChooser.resetChoosableFileFilters();
			//fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			
			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.CANCEL_OPTION)
				return;
			
			File file = fileChooser.getSelectedFile();
			imp = new ImagePlus(file.getPath());
			imp.show();
		}
		else if(ev.getSource() == btnLoadROI) {
			RoiManager manager = RoiManager.getInstance();
			if (manager == null)
			    manager = new RoiManager();
		}
		else if(ev.getSource() == btnSave) {			
			RoiManager manager = RoiManager.getInstance();
			if (manager == null) {
				this.pnlStatus.setOutput("No ROI manager available.");
				return;
			}
			
			boolean is3D = (imp.getStackSize() > 1);
			
			
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			int returnVal = fileChooser.showSaveDialog(this);
			if (returnVal == JFileChooser.CANCEL_OPTION)
				return;
			
			File file = fileChooser.getSelectedFile();
			
			Roi[] rois = manager.getRoisAsArray();
			
			//Cross platform new line character
	    	String newLine = System.getProperty("line.separator");
	    	
	    	BufferedWriter writer = null;
	    	try {
		    	writer = new BufferedWriter(new FileWriter(new File(file, "target.txt")));
		    	
		    	for(int i = 0; i < rois.length; i++) {
					imp.setRoi(rois[i]);
					ImagePlus roiImg = new ImagePlus("ROI", 
							imp.getProcessor().crop());
					ij.IJ.saveAs(roiImg, "jpeg", file.getPath() + "/" + (i + 1) + ".jpg");
					writer.write((i + 1) + ".jpg" + newLine);
				}
		    	
		    	writer.close();
		    	pnlStatus.setOutput("DONE!!!");
	    	}
	    	catch (IOException ex) {
	    		pnlStatus.setOutput("Exception occured while writing target file: " + file.getName());
	        	ex.printStackTrace();
	    	}
		}
	}
	
}
