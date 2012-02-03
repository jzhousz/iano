package annotool.gui;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import java.awt.BorderLayout;
import java.awt.Color;
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
import annotool.analysis.Utility;
import annotool.io.DataInput;

public class ROITagger extends JDialog implements ActionListener {
	private AnnOutputPanel pnlStatus = new AnnOutputPanel();
	private ImagePlus imp = null;
	private JButton btnLoadImg = null;
	private JButton btnLoadROI = null;
	private JButton btnSave = null;
	private JButton btnTemp = null;
	
	//testing purpose for 3D 
	private JButton btn3DROITrain = null;
	private JButton btn3DROITest = null;
	
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
		rightPanel.setLayout(new GridLayout(4, 1, 2, 2));
		
		btnLoadImg = new JButton("Load Image");
		btnLoadImg.addActionListener(this);
		
		btnLoadROI = new JButton("ROI Manager");
		btnLoadROI.addActionListener(this);
		
		btnSave = new JButton("Save");
		btnSave.addActionListener(this);
		
		btnTemp = new JButton("temp");
		btnTemp.addActionListener(this);
	
		btn3DROITrain = new JButton("3D ROI Train");
		btn3DROITrain.addActionListener(this);
		
		btn3DROITest = new JButton("3D ROI Test");
		btn3DROITest.addActionListener(this);
		
		rightPanel.add(btnLoadImg);
		rightPanel.add(btnLoadROI);
		rightPanel.add(btnSave);
		rightPanel.add(btnTemp);
		rightPanel.add(btn3DROITrain);
		rightPanel.add(btn3DROITest);
		
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
		else if(ev.getSource() == btnTemp) {
			ImageProcessor ip = imp.getProcessor();
			Object datain = ip.getPixels();
			
			int totalwidth = ip.getWidth();
			int totalheight = ip.getHeight();
			
			float[] feature = new float[totalwidth * totalheight];
			
			int imageType = imp.getType();
			if(imageType == DataInput.GRAY8)
		    {
		      byte[] data = (byte[]) datain;
		      for(int i = 0; i< totalwidth*totalheight; i++)
		         feature[i] = (float) (data[i]&0xff);
		    }
		    else if(imageType == DataInput.GRAY16)
		    {
		    	int[] data = (int[]) datain;
	 	        for(int i = 0; i< totalwidth*totalheight; i++)
	 	    	  feature[i] = (float) (data[i]&0xffff);
		    }	
	 	    else if(imageType == DataInput.GRAY32)
	 	    {
		    	float[] data = (float[]) datain;
	 	        for(int i = 0; i< totalwidth*totalheight; i++)
	 	 	    	  feature[i] = (float) data[i];
	 	    }
	 	    else if (imageType == DataInput.COLOR_RGB) {
	 	    	pnlStatus.setOutput("Only grayscale supported");
	 	    	return;
	 	    }
	 	    else
	 	    {
	 	    	pnlStatus.setOutput("Unsuppored Image Type");
	 	    	return;
	 	    }
			
			boolean[] isMaxima = Utility.getLocalMaxima(feature, totalwidth, totalheight, 1, 3, 3, 1);
			
			//float alpha = 0.6f; //transparent parameter (0: transparent; 1: opaque)
	    	//int[] colors = new int[3];
	    	//float[] fcolors = new float[3];
			
			ImageConverter ic = new ImageConverter(imp);
			ic.convertToRGB();
			ip = imp.getChannelProcessor();
	    	
	    	for(int y = 0; y < totalheight; y++) {
				for(int x = 0; x < totalwidth; x++) {
					if(isMaxima[Utility.offset(x, y, totalwidth)]) {
						ip.moveTo(x, y);						
						//ip.getPixel(x, y, colors);
						//for(int k = 0; k < colors.length; k++) fcolors[k] = (float) colors[k]/256;
						
						//fcolors[0] = alpha + fcolors[0]*(1-alpha);
		    			//fcolors[1] = fcolors[1]*(1-alpha);
		    			//fcolors[2] = fcolors[2]*(1-alpha);
		    			
		    			//Color c = new Color(fcolors[0], fcolors[1], fcolors[2]);
		    			Color c = new Color(1f, 0f, 0f);
		    			ip.setColor(c);
		    			ip.fillOval(x - 2, y - 2, 4, 4);
					}
				}
			}
			
			imp.updateAndDraw();
			
			pnlStatus.setOutput("Done");
		}
		else if(ev.getSource() == btn3DROITrain)
		{
			ImageProcessor ip = imp.getProcessor();
			annotool.analysis.ThreeDROIAnnotation.train3droi(imp);
		}
		else if(ev.getSource() == btn3DROITest)
		{
			ImageProcessor ip = imp.getProcessor();
			annotool.analysis.ThreeDROIAnnotation.test3droi(imp);
		}
		
	}
	
	
}
