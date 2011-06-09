package annotool.gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import annotool.AnnLoadImageDialog;
import annotool.Annotator;
import annotool.AnnotatorGUI;

import java.awt.Dimension;
import java.awt.event.*;
import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;

public class AnnLandingPanel extends JPanel implements ActionListener, ItemListener
{
	AnnotatorGUI   gui;
	JButton btnTrain, btnAnnotate,
			btnLoad,
			btnExpert, btnAutoComp;
	JPanel pnlFirst, 
		   pnlTrain, pnlModeSelect, pnlFoldSelect,
		   pnlMethodSelect, pnlChannelSelect;
	
	final static String LANDING = "First Panel";
	final static String MODESELECT = "Mode Select Panel";
	final static String METHODSELECT = "Method Select Panel";
	
	String[] modes = {  "Testing/Training", "Cross Validation" };
	String[] folds = {  "LOO (Leave One Out)", "5 fold CV", "10 fold CV" };
	String[] channels = {  "red (channel 1)", "green (channel 2)", "blue (channel 3)" };
	
	JRadioButton rbTestTrain = new JRadioButton(modes[0]);
	JRadioButton rbCrossValidation = new JRadioButton(modes[1]);
	
	JRadioButton redButton = new JRadioButton(channels[0]); 
	JRadioButton greenButton  = new JRadioButton(channels[1]); 
	JRadioButton blueButton  = new JRadioButton(channels[2]);
	
	JRadioButton looButton = new JRadioButton(folds[0]); 
	JRadioButton fold2Button = new JRadioButton(folds[1]); 
	JRadioButton fold3Button = new JRadioButton(folds[2]);
	
	public AnnLandingPanel(AnnotatorGUI gui)
	{
		this.gui = gui;	
		
		
		//Starting panel and it's components
		pnlFirst = new JPanel();
		
		btnTrain = new JButton("Train");
		btnAnnotate = new JButton("Annotate");
		
		btnTrain.addActionListener(this);
		btnAnnotate.addActionListener(this);		
		

		pnlFirst.setLayout(new GridBagLayout());
		pnlFirst.setPreferredSize(new Dimension(640, 480));
		GridBagConstraints c = new GridBagConstraints();
		
		c.ipadx = 50; //Make buttons tall and wide
		c.ipady = 50;
		
		c.gridx = 1;
		c.gridy = 2;		
		pnlFirst.add(btnTrain, c);
		
		c.gridx = 3;
		c.gridy = 2;
		pnlFirst.add(btnAnnotate, c);
		
		//Container for mode Selection panel to select Training/Testing or Cross Validation
		pnlTrain = new JPanel();
		
		//Mode Selection Panel
		pnlModeSelect = new JPanel();
		pnlModeSelect.setLayout(new GridLayout(0, 1));
		pnlModeSelect.setBorder(new CompoundBorder(new TitledBorder(null, "Mode", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5)));
		
		rbTestTrain.setSelected(true);
		ButtonGroup modeGroup = new ButtonGroup();
		modeGroup.add(rbTestTrain);
		modeGroup.add(rbCrossValidation);
		
		//Add listener to mode selection radio buttons
		rbTestTrain.addItemListener(this);
		rbCrossValidation.addItemListener(this);
		
		pnlModeSelect.add(rbTestTrain);
		pnlModeSelect.add(rbCrossValidation);
		
		//Fold selection
		looButton.setSelected(true);
		
		ButtonGroup foldgroup = new ButtonGroup();
		foldgroup.add(looButton);
	    foldgroup.add(fold2Button);
	    foldgroup.add(fold3Button);	    
	    
	    pnlFoldSelect = new JPanel(new GridLayout(0, 1));
	    pnlFoldSelect.add(looButton);
	    pnlFoldSelect.add(fold2Button);
	    pnlFoldSelect.add(fold3Button);
	    pnlFoldSelect.setBorder(new CompoundBorder(new TitledBorder(null,"Fold in Cross Validation", 
		TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5)));
	    foldEnabled(false);
	    
	    //Channel selection 
	    greenButton.setSelected(true);
	    
		ButtonGroup channelGroup = new ButtonGroup();
		channelGroup.add(redButton);
		channelGroup.add(greenButton);
		channelGroup.add(blueButton);
		
        pnlChannelSelect = new JPanel(new GridLayout(0, 1));
        pnlChannelSelect.add(redButton);
        pnlChannelSelect.add(greenButton);
        pnlChannelSelect.add(blueButton);
        pnlChannelSelect.setBorder(new CompoundBorder(new TitledBorder(null,"Channels", 
		TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5)));
	    
	    btnLoad = new JButton("Load");
	    btnLoad.addActionListener(this);
		
		pnlTrain.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 2;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weightx = 0.5;
		gc.weighty = 0.5;
	    pnlTrain.add(pnlModeSelect, gc);
	    gc.gridx = 0;
		gc.gridy = 1;
		gc.gridwidth = 1;
		pnlTrain.add(pnlFoldSelect, gc);
		gc.gridx = 1;
		gc.gridy = 1;
		gc.gridwidth = 1;
		pnlTrain.add(pnlChannelSelect, gc);
		gc.gridx = 1;
		gc.gridy = 2;
		gc.gridwidth = 1;
		gc.ipadx = 70;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		pnlTrain.add(btnLoad, gc);
		
		//Panel with expert mode and auto comparison mode selection
		pnlMethodSelect = new JPanel();
		pnlMethodSelect.setLayout(new GridBagLayout());
		
		btnExpert = new JButton("Expert");
		btnAutoComp = new JButton("Auto Comparision");
		
		btnExpert.addActionListener(this);
		btnAutoComp.addActionListener(this);
		
		c.gridx = 1;
		c.gridy = 2;		
		pnlMethodSelect.add(btnExpert, c);
		
		c.gridx = 3;
		c.gridy = 2;
		pnlMethodSelect.add(btnAutoComp, c);
		
		this.setLayout(new CardLayout());
		this.add(pnlFirst, LANDING);
		this.add(pnlTrain, MODESELECT);
		this.add(pnlMethodSelect, METHODSELECT);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btnTrain)
		{
			CardLayout cl = (CardLayout)(this.getLayout());
			cl.show(this, MODESELECT);
		}
		if(e.getSource() == btnLoad)
		{
			if(rbTestTrain.isSelected())
			{
				AnnLoadImageDialog loadDialog = new AnnLoadImageDialog(gui, gui.controlPanel, Annotator.OUTPUT_CHOICES[0]);
			}
			else if(rbCrossValidation.isSelected())
			{
				AnnLoadImageDialog loadDialog = new AnnLoadImageDialog(gui, gui.controlPanel, Annotator.OUTPUT_CHOICES[1]);
			}
			CardLayout cl = (CardLayout)(this.getLayout());
			cl.show(this, METHODSELECT);
		}
		if(e.getSource() == btnExpert)
		{
			gui.remove(this);
			gui.add(gui.screen);
			gui.pack();
		}
		else if(e.getSource() == btnAutoComp)
		{
			
		}
	}
	public void itemStateChanged(ItemEvent e)
	{
		if(e.getSource() == rbCrossValidation)
			foldEnabled(true);
		else
			foldEnabled(false);
	}
	private void foldEnabled(boolean flag)
	{
		looButton.setEnabled(flag);
		fold2Button.setEnabled(flag);
		fold3Button.setEnabled(flag);
	}
}
