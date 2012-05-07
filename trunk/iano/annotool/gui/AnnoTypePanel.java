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

/**
 * This panel displays the interface to select either ROI annotation or Image annotation
 * when Annotation is selected in the main screen.
 * 
 * @author Santosh
 *
 */
public class AnnoTypePanel extends JPanel implements ActionListener{
	private AnnotatorGUI gui = null;
	
	private JPanel pnlRow1, pnlRow2,             				//Three rows for three buttons and labels
	   		pnlDesc1, pnlDesc2;					         	//Container for labels in each row
	
	private JButton btnAnnotate, btnROI;
	
	private ImageIcon iconAnnotate, iconROI;
	private JLabel 	lbAnnotate, lbROI, 
					lbAnnotateSmall, lbROISmall;
	
	//Path of image icons for the buttons on main screen
	final static String IMGANNOTATE = "images/tag.png";
	final static String IMGROI = "images/roi.png";
	
	//Label texts
	final static String TXTANNOTATE = "Image Annotation";
	final static String TXTANNOTATESMALL = "Single or multiple labels per image.";
	final static String TXTROI = "ROI Annotation";
	final static String TXTROISMALL = "Region of interest annotation.";
	
	//Larger font for titles
	Font titleFont = new Font("Dialog", 1, 14);
	
	public AnnoTypePanel(AnnotatorGUI gui) {
		this.gui = gui;
		
		this.setBorder(new EmptyBorder(20, 20, 20, 20) );
		this.setLayout(new GridLayout(2, 1, 15, 15));
		
		//Icons
		iconAnnotate = Utils.createImageIcon("/" + IMGANNOTATE);
		iconROI = Utils.createImageIcon("/" + IMGROI);
		
		//If image was not loaded, use relative path
		if(iconAnnotate == null)
			iconAnnotate = new ImageIcon(IMGANNOTATE);
		if(iconROI == null)
			iconROI = new ImageIcon(IMGROI);
		
		//Buttons
		btnAnnotate = new JButton(iconAnnotate);
		btnROI = new JButton(iconROI);
		
		btnAnnotate.setPreferredSize(new Dimension(120, 100));
		btnROI.setPreferredSize(new Dimension(120, 100));
		
		//Button Listeners
		btnAnnotate.addActionListener(this);
		btnROI.addActionListener(this);
		
		//Labels
		lbAnnotate = new JLabel(TXTANNOTATE);
		lbROI = new JLabel(TXTROI);
		lbAnnotateSmall = new JLabel(TXTANNOTATESMALL);
		lbROISmall = new JLabel(TXTROISMALL);
		
		//Set a larger font for the title labels
		lbAnnotate.setFont(titleFont);
		lbROI.setFont(titleFont);
		
		//Panels for layout
		pnlRow1 = new JPanel();
		pnlRow2 = new JPanel();
		
		pnlRow1.setLayout(new BorderLayout());
		pnlRow2.setLayout(new BorderLayout());
		
		pnlDesc1 = new JPanel();
		pnlDesc2 = new JPanel();
		
		pnlDesc1.setLayout(new BoxLayout(pnlDesc1, BoxLayout.Y_AXIS));
		pnlDesc2.setLayout(new BoxLayout(pnlDesc2, BoxLayout.Y_AXIS));
		
		//Add components to container panel
		this.add(pnlRow1);
		this.add(pnlRow2);
		
		pnlRow1.add(btnAnnotate, BorderLayout.WEST);
		pnlRow1.add(pnlDesc1, BorderLayout.CENTER);
		pnlRow2.add(btnROI, BorderLayout.WEST);
		pnlRow2.add(pnlDesc2, BorderLayout.CENTER);
		
		pnlDesc1.add(lbAnnotate);
		pnlDesc1.add(lbAnnotateSmall);
		pnlDesc2.add(lbROI);
		pnlDesc2.add(lbROISmall);
	}
	
	public void actionPerformed(ActionEvent ev) {
		if(ev.getSource() == btnAnnotate)
			gui.initAnnotate();
		else if(ev.getSource() == btnROI)
			gui.initROI();
	}	
}
