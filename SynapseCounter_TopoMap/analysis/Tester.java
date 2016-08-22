
/* notes from Jon Sanders

	-changed default filechooser directory to user/desktop 
	-added Binning controls to GUI 
	-set window title
	-added labels to gui for useability
*/


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class Tester extends JFrame implements ActionListener{
	private JPanel mainPanel = new JPanel();
	private AnnOutputPanel pnlStatus = new AnnOutputPanel();
	private JPanel pnlLabels = new JPanel(new GridLayout(2, 1, 2, 2));
	
	private JButton btnSynapse = new JButton("Select synapse file...");
	private JButton btnNeuron = new JButton("Select neuron file...");
	private JButton btnRun = new JButton("Run");
	
	private JTextField tfWidth = new JTextField("1024");
	private JTextField tfHeight = new JTextField("1024");
	private JTextField tfDepth = new JTextField("155");
	
	private JTextField tfMinTh = new JTextField("" + SynapseStats.MIN_TH_DEFAULT);
	private JTextField tfMaxTh = new JTextField("" + SynapseStats.MAX_TH_DEFAULT);
	private JTextField tfBins  = new JTextField("" + SynapseStats.BINS_DEFAULT);
	
	private JLabel lbSynapse = new JLabel();
	private JLabel lbNeuron = new JLabel();
	
	private JLabel lbMinTh = new JLabel("Min Threshold:");
	private JLabel lbMaxTh = new JLabel("Max Threshold:");
	private JLabel lbBins = new JLabel("Bins:");
	
	private JLabel lbHeight = new JLabel("Height:");
	private JLabel lbWidth = new JLabel("Width:");
	private JLabel lbDepth = new JLabel("Depth:");
	
	private JRadioButton rbCalcStats = new JRadioButton("Calculate Stats");
	private JRadioButton rbConvertNeuron = new JRadioButton("Amira to V3D (neuron trace)");
	private JRadioButton rbConvertSynapse = new JRadioButton("ImageJ to V3D (synapse)");
	
	private final JFileChooser fileChooser = new JFileChooser();
	private String homeDir = System.getProperty("user.home");
	
	private File synapseFile = null;
	private File[] neuronFiles = null;
	
	public Tester(String arg0) {
		super(arg0);
		this.add(mainPanel, BorderLayout.NORTH);
		this.setPreferredSize(new Dimension(680, 720));
	
		setTitle("Synapse Density Calculator");
		
		//main layout
		mainPanel.setLayout(new GridLayout(0, 3, 2, 2));
		
		//file chooser and run buttons
		mainPanel.add(btnSynapse);
		mainPanel.add(btnNeuron);
		mainPanel.add(btnRun);
		
		//height width depth selection
		mainPanel.add(lbWidth);
		mainPanel.add(lbHeight);
		mainPanel.add(lbDepth);
		mainPanel.add(tfWidth);
		mainPanel.add(tfHeight);
		mainPanel.add(tfDepth);			
		
		//binning controls
		mainPanel.add(lbMinTh);
		mainPanel.add(lbMaxTh);
		mainPanel.add(lbBins);
		mainPanel.add(tfMinTh);
		mainPanel.add(tfMaxTh);
		mainPanel.add(tfBins);
	
		
		//mode options
		mainPanel.add(rbCalcStats);
		mainPanel.add(rbConvertNeuron);
		mainPanel.add(rbConvertSynapse);
		
		
		ButtonGroup group = new ButtonGroup();
		group.add(rbCalcStats);
		group.add(rbConvertNeuron);
		group.add(rbConvertSynapse);
		
		rbCalcStats.setSelected(true);
		
		btnSynapse.addActionListener(this);
		btnNeuron.addActionListener(this);
		btnRun.addActionListener(this);
		
		this.add(pnlStatus, BorderLayout.CENTER);
		
		
		pnlLabels.add(lbSynapse);
		pnlLabels.add(lbNeuron);
		this.add(pnlLabels, BorderLayout.SOUTH);
		
		//Set the initial window position
		this.pack();
		Dimension dim =
			Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int)(dim.getWidth() - getWidth())/2;
		int y = (int)(dim.getHeight() - getHeight())/2;
		setLocation(x,y);
		
		
		//initialize the file chooser directory
		fileChooser.setCurrentDirectory(new File(homeDir + System.getProperty("file.separator")+ "Desktop"));
	}
	
	public static void main(String[] args) {
		Tester gui = new Tester("Sandbox");
		gui.pack();
        gui.setVisible(true);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		if(ev.getSource() == btnSynapse) {
			fileChooser.setMultiSelectionEnabled(false);
			int ret = fileChooser.showOpenDialog(this);
			if(ret == JFileChooser.CANCEL_OPTION)
				return;
			synapseFile = fileChooser.getSelectedFile();
			lbSynapse.setText("Synapse file: " + synapseFile.getPath());
		}
		else if(ev.getSource() == btnNeuron) {
			fileChooser.setMultiSelectionEnabled(true);
			int ret = fileChooser.showOpenDialog(this);
			if(ret == JFileChooser.CANCEL_OPTION)
				return;
			neuronFiles = fileChooser.getSelectedFiles();
			if(neuronFiles.length == 1)
				lbNeuron.setText("Neuron file: " + neuronFiles[0].getPath());
			else
				lbNeuron.setText("Multiple neuron files selected (if converting, only first will be used).");
		}
		else if(ev.getSource() == btnRun) {
			/* Temp code to get colinergic synapses
			ImagePlus impOne = new ImagePlus(synapseFile.getPath());
			ImagePlus impTwo = new ImagePlus(neuronFiles[0].getPath());
			
			int width = impOne.getWidth();
			int height = impOne.getHeight();
			int depth = impOne.getStackSize();
			
			int value1, value2;
			for(int i=0; i < depth; i++) {
				impOne.setSlice(i + 1);
				impTwo.setSlice(i + 1);
				
				ImageProcessor ip1 = impOne.getProcessor();
				ImageProcessor ip2 = impTwo.getProcessor();
				for(int y=0; y < height; y++) {
					for(int x=0; x < width; x++) {
						value1 = ip1.getPixel(x, y);
						value2 = ip2.getPixel(x, y);
						if(value1 > value2)
							value1 -= value2;
						else
							value1 = 0;
						
						ip1.putPixel(x, y, value1);
					}
				}
			}
			
			impOne.show();
			ij.IJ.save(impOne, synapseFile.getParent() + "/result.tif");*/
			
			//Check if proper files are selected
			if(rbCalcStats.isSelected() && (synapseFile == null || neuronFiles == null)) {
				pnlStatus.setOutput("Both files are required.");
				return;
			}
			else if(rbConvertNeuron.isSelected() && neuronFiles == null) {
				pnlStatus.setOutput("Neuron file is required.");
				return;
			}
			else if(rbConvertSynapse.isSelected() && synapseFile == null) {
				pnlStatus.setOutput("Synapse file is required.");
				return;
			}
			
			int width, height, depth;
			try {
				width = Integer.parseInt(tfWidth.getText());
				height = Integer.parseInt(tfHeight.getText());
				depth = Integer.parseInt(tfDepth.getText());
			} catch (NumberFormatException ex) {
				pnlStatus.setOutput("Width, height and depth must be integers");
				return;
			}		
			
			double minTh, maxTh;
			int bins;
			try {
				minTh = Double.parseDouble(tfMinTh.getText());
				maxTh = Double.parseDouble(tfMaxTh.getText());
				bins  = Integer.parseInt(tfBins.getText());
			} catch (NumberFormatException ex2) {
				pnlStatus.setOutput("Min Threshold and Max Threshold must be doubles.\nBins must be integer.");
				return;
			}
			
			
			if(rbCalcStats.isSelected()) {
				SynapseStats synStats = new SynapseStats(synapseFile, neuronFiles, pnlStatus, width, height, depth, minTh, maxTh, bins);
				synStats.calcStats();
			}
			else {
				fileChooser.setCurrentDirectory(new File(homeDir + System.getProperty("file.separator")+ "Desktop"));
				int ret = fileChooser.showSaveDialog(this);
				if(ret == JFileChooser.CANCEL_OPTION)
					return;
				File outputFile = fileChooser.getSelectedFile();
				
				if(rbConvertNeuron.isSelected()){
					FileConverter fc = new FileConverter(neuronFiles[0], outputFile, height, pnlStatus);				
					fc.convertNeuronFile();
				}	
				else if(rbConvertSynapse.isSelected()) {
					FileConverter fc = new FileConverter(synapseFile, outputFile, height, pnlStatus);
					fc.convertSynapseFile();
				}
			}
		}
	}
}
