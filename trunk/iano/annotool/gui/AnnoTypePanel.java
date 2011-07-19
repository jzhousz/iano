package annotool.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import annotool.Annotator;
import annotool.gui.model.Utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.*;

public class AnnoTypePanel extends JPanel implements ActionListener{
	private AnnotatorGUI gui = null;
	
	private JPanel pnlRow1, pnlRow2, pnlRow3,             				//Three rows for three buttons and labels
	   		pnlDesc1, pnlDesc2, pnlDesc3;					         	//Container for labels in each row
	
	private JButton btnClassify, btnAnnotate, btnROI;
	
	private ImageIcon iconClassify, iconAnnotate, iconROI;
	private JLabel 	lbClassify, lbAnnotate, lbROI, 
					lbClassifySmall, lbAnnotateSmall, lbROISmall;
	
	//Path of image icons for the buttons on main screen
	final static String IMGCLASSIFY = "images/class.png";
	final static String IMGANNOTATE = "images/tag.png";
	final static String IMGROI = "images/roi.png";
	
	//Label texts
	final static String TXTCLASSIFY = "Image Classification";
	final static String TXTCLASSIFYSMALL = "One label per image.";
	final static String TXTANNOTATE = "Image Annotation";
	final static String TXTANNOTATESMALL = "Multiple labels per image.";
	final static String TXTROI = "ROI Annotation";
	final static String TXTROISMALL = "Region of interest annotation.";
	
	//Larger font for titles
	Font titleFont = new Font("Dialog", 1, 14);
	
	public AnnoTypePanel(AnnotatorGUI gui) {
		this.gui = gui;
		
		this.setBorder(new EmptyBorder(20, 20, 20, 20) );
		this.setLayout(new GridLayout(3, 1, 15, 15));
		
		//Icons
		iconClassify = Utils.createImageIcon("/" + IMGCLASSIFY);
		iconAnnotate = Utils.createImageIcon("/" + IMGANNOTATE);
		iconROI = Utils.createImageIcon("/" + IMGROI);
		
		//If image was not loaded, use relative path
		if(iconClassify == null)
			iconClassify = new ImageIcon(IMGCLASSIFY);
		if(iconAnnotate == null)
			iconAnnotate = new ImageIcon(IMGANNOTATE);
		if(iconROI == null)
			iconROI = new ImageIcon(IMGROI);
		
		//Buttons
		btnClassify = new JButton(iconClassify);
		btnAnnotate = new JButton(iconAnnotate);
		btnROI = new JButton(iconROI);
		
		btnClassify.setPreferredSize(new Dimension(120, 100));
		btnAnnotate.setPreferredSize(new Dimension(120, 100));
		btnROI.setPreferredSize(new Dimension(120, 100));
		
		//Button Listeners
		btnClassify.addActionListener(this);
		btnAnnotate.addActionListener(this);
		btnROI.addActionListener(this);
		
		//Labels
		lbClassify = new JLabel(TXTCLASSIFY);
		lbAnnotate = new JLabel(TXTANNOTATE);
		lbROI = new JLabel(TXTROI);
		lbClassifySmall = new JLabel(TXTCLASSIFYSMALL);
		lbAnnotateSmall = new JLabel(TXTANNOTATESMALL);
		lbROISmall = new JLabel(TXTROISMALL);
		
		//Set a larger font for the title labels
		lbClassify.setFont(titleFont);
		lbAnnotate.setFont(titleFont);
		lbROI.setFont(titleFont);
		
		//Panels for layout
		pnlRow1 = new JPanel();
		pnlRow2 = new JPanel();
		pnlRow3 = new JPanel();
		
		pnlRow1.setLayout(new BorderLayout());
		pnlRow2.setLayout(new BorderLayout());
		pnlRow3.setLayout(new BorderLayout());
		
		pnlDesc1 = new JPanel();
		pnlDesc2 = new JPanel();
		pnlDesc3 = new JPanel();
		
		pnlDesc1.setLayout(new BoxLayout(pnlDesc1, BoxLayout.Y_AXIS));
		pnlDesc2.setLayout(new BoxLayout(pnlDesc2, BoxLayout.Y_AXIS));
		pnlDesc3.setLayout(new BoxLayout(pnlDesc3, BoxLayout.Y_AXIS));
		
		//Add components to container panel
		this.add(pnlRow1);
		this.add(pnlRow2);
		this.add(pnlRow3);
		
		pnlRow1.add(btnClassify, BorderLayout.WEST);
		pnlRow1.add(pnlDesc1, BorderLayout.CENTER);
		pnlRow2.add(btnAnnotate, BorderLayout.WEST);
		pnlRow2.add(pnlDesc2, BorderLayout.CENTER);
		pnlRow3.add(btnROI, BorderLayout.WEST);
		pnlRow3.add(pnlDesc3, BorderLayout.CENTER);
		
		pnlDesc1.add(lbClassify);
		pnlDesc1.add(lbClassifySmall);
		pnlDesc2.add(lbAnnotate);
		pnlDesc2.add(lbAnnotateSmall);
		pnlDesc3.add(lbROI);
		pnlDesc3.add(lbROISmall);
	}
	
	public void actionPerformed(ActionEvent ev) {
		if(ev.getSource() == btnClassify) {
			Annotator.output = Annotator.CL;
			gui.pnlLanding.getImageReadyPanel().setMode();
			AnnLoadImageDialog loadDialog = new AnnLoadImageDialog(gui, gui.pnlLanding, Annotator.CL);
		}
		else if(ev.getSource() == btnAnnotate) {
			Annotator.output = Annotator.AN;
			gui.pnlLanding.getImageReadyPanel().setMode();
			AnnLoadImageDialog loadDialog = new AnnLoadImageDialog(gui, gui.pnlLanding, Annotator.AN);
		}
		else if(ev.getSource() == btnROI) {
			Annotator.output = Annotator.ROI;
			gui.pnlLanding.getImageReadyPanel().setMode();
			AnnLoadImageDialog loadDialog = new AnnLoadImageDialog(gui, gui.pnlLanding, Annotator.ROI);
		}
	}
}
