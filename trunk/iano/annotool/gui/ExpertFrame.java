package annotool.gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.*;

import annotool.io.AlgoXMLParser;
import java.util.ArrayList;
import annotool.io.Algorithm;
import annotool.io.Parameter;

public class ExpertFrame extends JFrame implements ActionListener, ItemListener
{
	private JPanel pnlMain,
				   pnlAlgo,
				   pnlExt, pnlSel, pnlClass,
				   pnlExtParam,
				   pnlButton;
	private JButton btnTrain;
	
	private JLabel lbExtractor, lbSelector, lbClassifier;
	private JComboBox cbExtractor, cbSelector, cbClassifier;
	
	public ExpertFrame(String arg0, boolean is3D)
	{
		super(arg0);
		
		pnlMain = new JPanel(new BorderLayout());
		pnlMain.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.add(pnlMain);
		
		//Algorithm selector part
		pnlAlgo = new JPanel();
		pnlAlgo.setLayout(new BoxLayout(pnlAlgo, BoxLayout.Y_AXIS));
		pnlAlgo.setBorder(new CompoundBorder(new TitledBorder(null, "Algorithms", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5)));
		
		//Parse the xml document with list of algorithms (for extractors, selectors and classifiers)
		AlgoXMLParser parser = new AlgoXMLParser();
		parser.runParser();
		
		//Labels
		if(is3D)
			lbExtractor = new JLabel("3D Feature Extractor");
		else
			lbExtractor = new JLabel("2D Feature Extractor");
		lbSelector = new JLabel("Feature Selector");
		lbClassifier = new JLabel("Classifier");
		
		//Combo boxes
		if(is3D)
			cbExtractor = new JComboBox(parser.get3DExtractorsAr());
		else
			cbExtractor = new JComboBox(parser.get2DExtractorsAr());
		
		cbSelector = new JComboBox(parser.getSelectorsAr());
		cbClassifier = new JComboBox(parser.getClassifiersAr());
		
		//Add item listeners to combo boxes
		cbExtractor.addItemListener(this);
		cbSelector.addItemListener(this);
		cbClassifier.addItemListener(this);
		
		//Extractor panel
		pnlExt = new JPanel(new GridLayout(2, 2));
		pnlExt.add(lbExtractor);
		pnlExt.add(cbExtractor);	
		
		//Selector panel
		pnlSel = new JPanel(new GridLayout(1, 2));
		pnlSel.add(lbSelector);
		pnlSel.add(cbSelector);
		
		//Classifier panel
		pnlClass = new JPanel(new GridLayout(1, 2)); 
		pnlClass.add(lbClassifier);
		pnlClass.add(cbClassifier);
		
		//Add to container
		pnlAlgo.add(pnlExt);
		pnlAlgo.add(pnlSel);
		pnlAlgo.add(pnlClass);
		
		//Button part
		btnTrain = new JButton("Train");
		btnTrain.addActionListener(this);
		
		pnlButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlButton.add(btnTrain);
		
		pnlMain.add(pnlAlgo, BorderLayout.CENTER);
		pnlMain.add(pnlButton, BorderLayout.SOUTH);
		
		//Build parameter panel for default selection
		buildExParameterPanel();
		
	}
	public void actionPerformed(ActionEvent e)
	{
		
	}
	public void itemStateChanged(ItemEvent e)
	{
		if(e.getSource() == cbExtractor && e.getStateChange() == 1)
		{		
			buildExParameterPanel();
		}
	}
	private void buildExParameterPanel()
	{
		//Remove previous panel if any
		if(pnlExtParam != null)
		{
			pnlExt.remove(pnlExtParam);
			pnlExtParam = null;
		}
		
		//Get the currently selected extractor
		Algorithm al = (Algorithm)cbExtractor.getSelectedItem();
		Parameter param = al.getParam();
		
		if(param != null)
		{
			pnlExtParam = new JPanel(new FlowLayout(FlowLayout.LEFT));
			if(param.getParamType().equals("Boolean"))
			{
				JCheckBox cb = new JCheckBox(param.getParamName());
				pnlExtParam.add(cb);
			}
			else if(param.getParamType().equals("Integer"))
			{
				JLabel lb = new JLabel(param.getParamName());
				
				SpinnerNumberModel snm = new SpinnerNumberModel();
				if(param.getParamMax() != null)
					snm.setMaximum(Integer.parseInt(param.getParamMax()));
				if(param.getParamMin() != null)
					snm.setMinimum(Integer.parseInt(param.getParamMin()));
				if(param.getParamDefault() != null)
					snm.setValue(Integer.parseInt(param.getParamDefault()));
				JSpinner sp = new JSpinner(snm);
				
				pnlExtParam.add(lb);
				pnlExtParam.add(sp);
			}
			else if(param.getParamType().equals("Integer"))
			{
				JLabel lb = new JLabel(param.getParamName());
				JTextField tf = new JTextField(param.getParamDefault());
				
				pnlExtParam.add(lb);
				pnlExtParam.add(tf);
			}
			pnlExt.add(pnlExtParam);
			this.pack();
		}
	}
	public static void main(String[] args) 
	{
		ExpertFrame ef = new ExpertFrame("Expert Mode", false);
		ef.pack();
		ef.setVisible(true);
		ef.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
