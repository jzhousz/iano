package annotool.gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import annotool.Annotator;

import java.awt.event.*;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

public class LandingPanel extends JPanel implements ActionListener, ItemListener
{
	private JPanel pnlMainScreen, pnlModeSelect,          //Main screen, Mode select screen container
	          	   pnlRow1, pnlRow2, pnlRow3,             //Three rows for three buttons and labels
	          	   pnlDesc1, pnlDesc2, pnlDesc3,          //Container for labels in each row
	          	   pnlBtnContainer,      
	          	   pnlModeBox, pnlFoldBox, pnlSpinner;
	
	private ImageReadyPanel pnlImages;					  //Panel with loaded images	
				   
	private JButton btnModelSelect, btnTrain, btnAnnotate,
					btnLoadImages;
	private ImageIcon iconMS, iconTrain, iconAnnotate;
	private JLabel lbMS, lbTrain, lbAnnotate,
				   lbMSSmall, lbTrainSmall, lbAnnotateSmall,
				   lbSpinner;
	
	private JRadioButton rbTT, rbCV; //Test-Train and Cross-Validation
	private JCheckBox cbLoo, cbShuffle;
	private JSpinner spFold;
	
	//Path of image icons for the buttons on main screen
	final static String imgMS = "images/ms.png";
	final static String imgTrain = "images/train.png";
	final static String imgAnnotate = "images/anno.png";
	
	//String for defining panels in card layout
	final static String MAIN = "First Panel";
	final static String MODESELECT = "Mode Select Panel";
	final static String IMAGEREADY = "Image Ready Panel";
	
	//Larger font for titles
	Font titleFont = new Font("Dialog", 1, 14);
	
	//Main GUI
	AnnotatorGUI gui = null;
	
	//Constructor
	public LandingPanel(AnnotatorGUI gui)
	{
		this.gui = gui;
		
		this.setLayout(new CardLayout());
		
		buildMainScreen();
		buildModeSelectScreen();
		
		//Add panel for loaded images
		pnlImages = new ImageReadyPanel(gui);
		this.add(pnlImages, IMAGEREADY);
	}
	
	/* 
	 * Creates the first screen
	 * It contains three buttons for three main tasks : Model Selection, Training and Annotation
	 */
	private void buildMainScreen()
	{
		//Container panel
		pnlMainScreen = new JPanel();
		pnlMainScreen.setBorder(new EmptyBorder(20, 20, 20, 20) );
		pnlMainScreen.setLayout(new GridLayout(3, 1, 15, 15));
		
		//Icons
		iconMS = createImageIcon("/" + imgMS);
		iconTrain = createImageIcon("/" + imgTrain);
		iconAnnotate = createImageIcon("/" + imgAnnotate);
		
		//If image was not loaded, use relative path
		if(iconMS == null)
			iconMS = new ImageIcon(imgMS);
		if(iconTrain == null)
			iconTrain = new ImageIcon(imgTrain);
		if(iconAnnotate == null)
			iconAnnotate = new ImageIcon(imgAnnotate);
		
		//Buttons
		btnModelSelect = new JButton(iconMS);
		btnTrain = new JButton(iconTrain);
		btnAnnotate = new JButton(iconAnnotate);
		
		//Button Listeners
		btnModelSelect.addActionListener(this);
		btnTrain.addActionListener(this);
		btnAnnotate.addActionListener(this);
		
		//Labels
		lbMS = new JLabel("Model Selection");
		lbTrain = new JLabel("Training Only");
		lbAnnotate = new JLabel("Annotation");
		lbMSSmall = new JLabel("Select algorithm using training/testing image sets or cross validation.");
		lbTrainSmall = new JLabel("Training using an entire set.");
		lbAnnotateSmall = new JLabel("Image classification and labelling using a trained model.");
		
		//Set a larger font for the title labels
		lbMS.setFont(titleFont);
		lbTrain.setFont(titleFont);
		lbAnnotate.setFont(titleFont);
		
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
		pnlMainScreen.add(pnlRow1);
		pnlMainScreen.add(pnlRow2);
		pnlMainScreen.add(pnlRow3);
		
		pnlRow1.add(btnModelSelect, BorderLayout.WEST);
		pnlRow1.add(pnlDesc1, BorderLayout.CENTER);
		pnlRow2.add(btnTrain, BorderLayout.WEST);
		pnlRow2.add(pnlDesc2, BorderLayout.CENTER);
		pnlRow3.add(btnAnnotate, BorderLayout.WEST);
		pnlRow3.add(pnlDesc3, BorderLayout.CENTER);
		
		pnlDesc1.add(lbMS);
		pnlDesc1.add(lbMSSmall);
		pnlDesc2.add(lbTrain);
		pnlDesc2.add(lbTrainSmall);
		pnlDesc3.add(lbAnnotate);
		pnlDesc3.add(lbAnnotateSmall);
		
		//Add the main screen to the container
		this.add(pnlMainScreen, MAIN);
	}
	private void buildModeSelectScreen()
	{
		//Radio buttons
		rbTT = new JRadioButton("Testing/Training");
		rbTT.setSelected(true);
		rbTT.addItemListener(this);
		rbCV = new JRadioButton("Cross Validation");
		rbCV.addItemListener(this);		
		
		//Add radio buttons to a group
		ButtonGroup modeGroup = new ButtonGroup();
		modeGroup.add(rbTT);
		modeGroup.add(rbCV);
		
		//Check box
		cbLoo = new JCheckBox("LOO (Leave One Out)");
		cbLoo.addItemListener(this);
		cbShuffle = new JCheckBox("Shuffle");
		
		//Spinner to select fold
		spFold = new JSpinner(new SpinnerNumberModel(5, 2, 10, 1));
		
		//Label
		lbSpinner = new JLabel("Fold (2 to 10)");
		
		
		//Add radio buttons to mode panel
		pnlModeBox = new JPanel();
		pnlModeBox.setLayout(new GridLayout(2, 1));
		pnlModeBox.setBorder(new CompoundBorder(new TitledBorder(null, "Mode", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5)));
		
		pnlModeBox.add(rbTT);
		pnlModeBox.add(rbCV);
		
		//Add components to fold panel
		pnlFoldBox = new JPanel();
		pnlFoldBox.setLayout(new GridLayout(3, 1));
		pnlFoldBox.setBorder(new CompoundBorder(new TitledBorder(null, "CV Parameters", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5)));		
		
		pnlSpinner = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlSpinner.add(lbSpinner);
		pnlSpinner.add(spFold);		
		
		pnlFoldBox.add(cbLoo);
		pnlFoldBox.add(pnlSpinner);
		pnlSpinner.add(cbShuffle);
		//Button
		btnLoadImages = new JButton("Load Images");
		btnLoadImages.addActionListener(this);		
		
		//Add components to container panel
		pnlModeSelect = new JPanel();
		pnlModeSelect.setLayout(new BorderLayout());
		pnlModeSelect.setBorder(new EmptyBorder(20, 20, 20, 20) );
		
		JPanel pnlSelectBoxes = new JPanel(new GridLayout(1, 2));
		
		pnlSelectBoxes.add(pnlModeBox);
		pnlSelectBoxes.add(pnlFoldBox);
		
		pnlBtnContainer = new JPanel();
		pnlBtnContainer.setLayout(new FlowLayout(FlowLayout.RIGHT));
		pnlBtnContainer.add(btnLoadImages);
		//pnlModeSelect.add(new JLabel(""));
		
		//Header Panel
		JPanel pnlHeader = new JPanel(new BorderLayout());
		pnlHeader.setBorder(new CompoundBorder(new TitledBorder(null, "", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5)));
		//pnlHeader.add(new JLabel("Model Selection > Select Mode"));
		//pnlHeader.setBackground(java.awt.Color.LIGHT_GRAY);
		//pnlHeader.setPreferredSize(new java.awt.Dimension(500, 140));
		
		JLabel lbHeadTitle = new JLabel("Mode Selection");
		lbHeadTitle.setFont(titleFont);
		
		JPanel pnlHeaderDesc = new JPanel(new GridLayout(3, 1));
		pnlHeaderDesc.add(lbHeadTitle);
		pnlHeaderDesc.add(new JLabel("Testing/Training: Load different image sets for testing and training"));
		pnlHeaderDesc.add(new JLabel("Cross Validation: Load single image set for both testing and training"));
		
		pnlHeader.add(new JLabel(iconMS), BorderLayout.WEST);
		pnlHeader.add(pnlHeaderDesc, BorderLayout.CENTER);
		
		pnlModeSelect.add(pnlHeader, BorderLayout.NORTH);
		pnlModeSelect.add(pnlSelectBoxes, BorderLayout.CENTER);
		pnlModeSelect.add(pnlBtnContainer, BorderLayout.SOUTH);
		
		this.add(pnlModeSelect, MODESELECT);
		
		//Disable the fold selection panel by default
		this.foldEnabled(false); 
	}
	
	/**
     * Creates an ImageIcon if the path is valid.
     * @param String - resource path
     */
	protected ImageIcon createImageIcon(String path) 
	{
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) 
        {
            return new ImageIcon(imgURL);
        } 
        else 
        {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
	
	/*
	 * Action Listener
	 */
	public void actionPerformed(ActionEvent e)
	{		
		if(e.getSource() == btnModelSelect)
		{
			CardLayout cl = (CardLayout)(this.getLayout());
			cl.show(this, MODESELECT);
		}
		else if(e.getSource() == btnTrain) 
		{
			Annotator.output = Annotator.OUTPUT_CHOICES[3];
			pnlImages.setMode();
			AnnLoadImageDialog loadDialog = new AnnLoadImageDialog(gui, this, Annotator.OUTPUT_CHOICES[3]);
		}
		else if(e.getSource() == btnAnnotate) 
		{
			Annotator.output = Annotator.OUTPUT_CHOICES[4];
			pnlImages.setMode();
			AnnLoadImageDialog loadDialog = new AnnLoadImageDialog(gui, this, Annotator.OUTPUT_CHOICES[4]);
		}
		else if(e.getSource() == btnLoadImages)
		{
			if(rbTT.isSelected())
			{
				//Load separate training and testing image sets
				Annotator.output = Annotator.OUTPUT_CHOICES[0];
				pnlImages.setMode();
				AnnLoadImageDialog loadDialog = new AnnLoadImageDialog(gui, this, Annotator.OUTPUT_CHOICES[0]);
			}
			else
			{
				//Set choices
				if(cbLoo.isSelected())
					Annotator.fold = "LOO";
				else
				{
					Annotator.fold = spFold.getValue().toString();
					Annotator.shuffleFlag = (cbShuffle.isSelected()) ? "true" : "false";
				}
				//Load one image set for cross validation
				Annotator.output = Annotator.OUTPUT_CHOICES[1];
				pnlImages.setMode();
				AnnLoadImageDialog loadDialog = new AnnLoadImageDialog(gui, this, Annotator.OUTPUT_CHOICES[1]);
			}
		}		
	}
	/*
	 * Item Listener
	 */
	public void itemStateChanged(ItemEvent e)
	{
		if(e.getSource() == cbLoo)
		{
			spFold.setEnabled(!cbLoo.isSelected());
			lbSpinner.setEnabled(!cbLoo.isSelected());
			cbShuffle.setEnabled(!cbLoo.isSelected());
		}
		else if(e.getSource() == rbCV)
			foldEnabled(true);
		else if(e.getSource() == rbTT)
			foldEnabled(false);
	}
	private void foldEnabled(boolean flag)
	{
		cbLoo.setEnabled(flag);
		
		//If the fold selection is being enabled
		//Enable or disable the fold spinner (and label) based on whether or not 'LOO' is selected
		//Otherwise, simply disable the fold spinner (and label)
		if(flag)
		{
			spFold.setEnabled(!cbLoo.isSelected());
			lbSpinner.setEnabled(!cbLoo.isSelected());
			cbShuffle.setEnabled(!cbLoo.isSelected());
		}		
		else
		{
			spFold.setEnabled(flag);
			lbSpinner.setEnabled(flag);
			cbShuffle.setEnabled(flag);
		}
	}
	public void displayImageReadyPanel()
	{
		CardLayout cl = (CardLayout)(this.getLayout());
		cl.show(this, IMAGEREADY);
	}
	public ImageReadyPanel getImageReadyPanel()
	{
		return this.pnlImages;
	}	
}
