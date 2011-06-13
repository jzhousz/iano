package annotool.gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import annotool.AnnOutputPanel;
import annotool.AnnTablePanel;

public class ImageReadyPanel extends JPanel implements ActionListener
{
	private JPanel pnlRight,
				   pnlModelInfo, pnlChannel, pnlButton;
	
	JLabel lbModelInfo;
	JRadioButton rbRed, rbGreen, rbBlue;
	JButton btnExpert, btnAutoComp;
	
	String[] channels = {  "red (channel 1)", "green (channel 2)", "blue (channel 3)" };
	
	private AnnOutputPanel pnlStatus;
	private AnnTablePanel pnlTable;
	AnnotatorGUI gui = null;
	
	public ImageReadyPanel(AnnotatorGUI gui)
	{
		this.gui = gui;
		//Information panel with label to display info
		lbModelInfo = new JLabel("TT/CV");
		
		pnlModelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlModelInfo.setBorder(new CompoundBorder(new TitledBorder(null, "Model Info", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5)));
		pnlModelInfo.add(lbModelInfo);
		
		//Channel selection panel
		pnlChannel = new JPanel();
		pnlChannel.setLayout(new GridLayout(0, 1));
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
		
		//Expert and Auto Comparison buttons
		btnExpert = new JButton("Expert");
		btnExpert.addActionListener(this);
		btnAutoComp = new JButton("Auto Comp");
		btnAutoComp.addActionListener(this);
		
		//Panel for buttons
		pnlButton = new JPanel();
		pnlButton.setLayout(new BoxLayout(pnlButton, BoxLayout.X_AXIS));
		pnlButton.add(btnExpert);
		pnlButton.add(btnAutoComp);
		
		//Add components to right side bar
		pnlRight = new JPanel(new GridLayout(3, 1));
		pnlRight.add(pnlModelInfo);
		pnlRight.add(pnlChannel);
		pnlRight.add(pnlButton);
		
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
	}
	public AnnTablePanel getTablePanel()
	{
		return pnlTable;
	}
	public AnnOutputPanel getOutputPanel()
	{
		return pnlStatus;
	}
	public void setModelInfo(String value)
	{
		lbModelInfo.setText(value);
	}
}