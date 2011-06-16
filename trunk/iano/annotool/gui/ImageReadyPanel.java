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

import annotool.AnnOutputPanel;
import annotool.AnnTablePanel;
import annotool.Annotator;

public class ImageReadyPanel extends JPanel implements ActionListener
{
	private JPanel pnlRight, pnlRightCenter,
				   pnlModelInfo, pnlChannel, pnlButton;
	
	JLabel lbModelInfo;
	JRadioButton rbRed, rbGreen, rbBlue;
	JButton btnExpert, btnAutoComp;
	
	String[] channels = {  "red (channel 1)", "green (channel 2)", "blue (channel 3)" };
	String[] channelInputs = {  "r", "g", "b" };//actual input to algorithm
	
	private AnnOutputPanel pnlStatus;
	private AnnTablePanel pnlTable;
	AnnotatorGUI gui = null;
	
	String modelInfo = null;
	
	private boolean is3D = false;
	
	public ImageReadyPanel(AnnotatorGUI gui)
	{
		this.gui = gui;
		//Information panel with label to display info
		if(Annotator.output.equals("TT"))
			modelInfo = "Testing/Training";
		else if(Annotator.output.equals("CV"))
			modelInfo = "Cross Validation";
		
		lbModelInfo = new JLabel("<html><b>Mode: </b>" + modelInfo + "</html>");
		
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
		
		//Panel for buttons
		pnlButton = new JPanel();
		pnlButton.setLayout(new GridLayout(1, 2));
		pnlButton.add(btnExpert);
		pnlButton.add(btnAutoComp);
		
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
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btnExpert)
		{
			if(rbRed.isSelected())
				Annotator.channel = channelInputs[0];
			else if(rbGreen.isSelected())
				Annotator.channel = channelInputs[1];
			else if(rbBlue.isSelected())
				Annotator.channel = channelInputs[2];
			
			ExpertFrame ef = new ExpertFrame("Expert Mode", is3D, Annotator.channel);			
			ef.setVisible(true);
			ef.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			
			ef.pack();
			Dimension dim =
				Toolkit.getDefaultToolkit().getScreenSize();
			int x = (int)(dim.getWidth() - getWidth())/2;
			int y = (int)(dim.getHeight() - getHeight())/2;
			ef.setLocation(x,y);
		}
	}
	public AnnTablePanel getTablePanel()
	{
		return pnlTable;
	}
	public AnnOutputPanel getOutputPanel()
	{
		return pnlStatus;
	}
	public void channelEnabled(boolean flag)
	{
		rbRed.setEnabled(flag);
		rbGreen.setEnabled(flag);
		rbBlue.setEnabled(flag);
	}
	public void setIs3D(boolean flag)
	{
		this.is3D = flag;
	}
}