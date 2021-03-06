package entities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.OverlayLayout;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import controller.LarvaController;
import ij.IJ;
import ij.gui.Roi;
import manager.AVIManager;
import manager.FileManager;
import manager.HelperManager;
import manager.MathManager;
import manager.PropertyManager;
import entities.TitledPanel;

/**
* The GUI class containing GUI elements.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class OptionComponent
{
	private JFrame frame = null;
	private String directory = "";
	private String aviFile = "";
	private ArrayList<Roi> listRoi = null;
	// the text area in text status window'
	private JTextPane textStatusPane = null; 
	private JButton btnStart = null;
	final JPanel panelMain = new JPanel();
	// the text area in exception window
	private JTextArea textException = null;
	
	private OptionComponent self = null;
	
	private final JButton btnLeft1 = new JButton("General");
	private final JButton btnLeft2 = new JButton("Debug");
	private final JButton btnLeft5 = new JButton("Exceptions");
	private final JButton btnLeft4 = new JButton("AVI Generat.");
	private final JButton btnLeft3 = new JButton("Text Status");
	private final JButton btnLeft6 = new JButton("Image Status");
	private final JButton btnDefault = new JButton("Reset settings");
	private final JButton btnUpdateSet = new JButton("Update Settings"); 
	
	private PropertyManager prop = null;
	
	private final JTextField textDir = new JTextField("", 30);
	private final JTextField textFromFrame = new JTextField("", 5);
	private final JTextField textToFrame = new JTextField("", 5);
	private final JTextField textAutoRollPerc = new JTextField("", 5);
	private final JTextField textAutoRoll = new JTextField("", 5);
	private final JTextField textAutoMinSize = new JTextField("", 5);
	private final JTextField textAutoMaxSize = new JTextField("", 5);
	private final JTextField textAutoMinSkeleton = new JTextField("", 5);
	private final JTextField textAutoMaxSkeleton = new JTextField("", 5);
	
	final JRadioButton radioBlue = new JRadioButton("Blue Light", false);
	final JRadioButton radioChrimson = new JRadioButton("Chrimson", false);
	final JCheckBox cbCurl = new JCheckBox("Curling Angle");
	final JCheckBox cbSpeed = new JCheckBox("Sideways Speed");
	final JCheckBox cbRoll = new JCheckBox("Rolling Detection");
	final JRadioButton radioAllFrame = new JRadioButton("Process All frames", false);
	final JRadioButton radioRangeFrame = new JRadioButton("Process a range of frames", false);
	final JCheckBox cbVideo = new JCheckBox("Generate Annotaion Video", false);
	final JCheckBox cbAnimatedImage = new JCheckBox("Generate Annotaion Images", false);
	final JCheckBox cbDebug = new JCheckBox("Generate Debug Images", false);
	final JCheckBox cbCSV = new JCheckBox("Generate Complete CSV", false );
	
	final JCheckBox cbAutoRoll = new JCheckBox("Auto Detect Rolling", false );
	final JCheckBox cbAutoCheckSize = new JCheckBox("Auto Set Larva Size", false );
	final JCheckBox cbAutoCheckSkeleton = new JCheckBox("Auto Set Larva Skeleton", false );
	
	final JCheckBox cbStatSegmentation = new JCheckBox("Use statistical segmentation", false);
//	final JCheckBox cbTrainParticularLarva = new JCheckBox("Train Using Frames of a Particular Larva", false);
	
	final TitledPanel panelRight1 = new TitledPanel("General:");
	final TitledPanel panelRight2 = new TitledPanel("Debug:");
	final TitledPanel panelRight3 = new TitledPanel("Status Window:");
	final TitledPanel panelRight4 = new TitledPanel("AVI Generation:");
	final TitledPanel panelRight5 = new TitledPanel("Exeptions:");
	final TitledPanel panelRight6 = new TitledPanel("Image Status:");
	
	private ArrayList<JLabel> labelsCrop = new ArrayList<JLabel>();
	private ArrayList<JLabel> labelsBinary = new ArrayList<JLabel>();
	private ArrayList<JLabel> labelsCropText = new ArrayList<JLabel>();
	private ArrayList<JLabel> labelsBinaryText = new ArrayList<JLabel>();
	private Color colorImageStatus = Color.white;
	
	private final JLabel textSysStatus = new JLabel("Status: ");
	
	final ImageIcon sampleCropImageIcon = new ImageIcon("plugins/Larva/Icon/Crop.jpg");
	final ImageIcon sampleBinaryImageIcon = new ImageIcon("plugins/Larva/Icon/Binary.jpg");
	
	Font fontOption = new Font("SansSerif", Font.PLAIN, 12);
	final JPanel panelBottom = new JPanel();
	private PrintStream standardOut;
	
	public OptionComponent(String directory, String aviFile, String title, ArrayList<Roi> listRoi)
	{
		this.self = this;
		
		this.aviFile = aviFile;
		this.directory = directory;
		this.listRoi = listRoi;
		
		frame = new JFrame("Larva Behavior Quantification");
		frame.setLayout(new GridLayout(1,1));
		frame.setSize(650, 400);
		frame.setFont(fontOption);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
		
		ImageIcon img = new ImageIcon("plugins/Larva/Icon/larva.png");
		frame.setIconImage(img.getImage());
		
		frame.addWindowListener(new WindowAdapter() 
		{
			  public void windowClosing(WindowEvent e) 
			  {
			    int confirmed = JOptionPane.showConfirmDialog(null, 
			        "Are you sure you want to exit the program?", "Message",
			        JOptionPane.YES_NO_OPTION);

			    System.out.println("confirmed:"+confirmed);
			    
			    if (confirmed == JOptionPane.YES_OPTION) {
			    	System.exit(0);
//			    	frame.dispose();
			    }
			  }
		});
		
		prop = new PropertyManager();
		prop.getAllProperties();
		
		addComponents();
		
		changeFont ( panelRight1, fontOption );
		changeFont ( panelRight2, fontOption );
		changeFont ( panelRight5, fontOption );
		changeFont ( panelBottom, fontOption );
	}
	
	/**
	* Add all components to GUI.
	* 
	*/
	private void addComponents()
	{
		panelMain.setLayout(new BorderLayout());
		
		frame.add(panelMain);
		
		final JPanel panelTop = new JPanel();
		panelTop.setLayout(new FlowLayout(FlowLayout.CENTER));
		final JLabel lableTitle = new JLabel("Larva Behavior Quantification");
		lableTitle.setForeground(Color.black);
		lableTitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
		panelTop.add(lableTitle);
		
		panelMain.add(panelTop, BorderLayout.NORTH);
		
		final TitledPanel panelLeft = new TitledPanel("Menu:");
		panelLeft.setLayout(new GridLayout(11,1));
		
		btnLeft1.setFont(fontOption);
		panelLeft.add(btnLeft1);
		btnLeft2.setFont(fontOption);
		panelLeft.add(btnLeft2);
		btnLeft5.setFont(fontOption);
//		panelLeft.add(btnLeft5);
		
		btnLeft3.setFont(fontOption);
		panelLeft.add(btnLeft3);
		btnLeft6.setFont(fontOption);
		panelLeft.add(btnLeft6);
		
        panelMain.add(panelLeft, BorderLayout.WEST);
		        
		panelRight1.setLayout(new GridLayout(4,1));
		
		panelMain.add(panelRight1, BorderLayout.CENTER);
		
		panelRight2.setLayout(new BorderLayout());
	    
		panelRight3.setLayout(new GridLayout(1,1));
		
		panelRight4.setLayout(new GridLayout(0,1));
		
		panelRight5.setLayout(new GridLayout(0,1));
		
		panelRight6.setBackground(colorImageStatus);
		panelRight6.setLayout(new GridLayout(2,3));
		
		btnLeft1.addActionListener(new ActionListener() 
		{
	        public void actionPerformed(ActionEvent e) 
	        {
	        	panelMain.remove(panelRight2);
	        	panelMain.remove(panelRight3);
	        	panelMain.remove(panelRight4);
	        	panelMain.remove(panelRight5);
	        	panelMain.remove(panelRight6);
	        	panelMain.add(panelRight1);
	        	panelMain.revalidate();
	        	panelMain.repaint();
	        }
	     });
		
		btnLeft2.addActionListener(new ActionListener() 
		{
	        public void actionPerformed(ActionEvent e) 
	        {
	        	panelMain.remove(panelRight1);
	        	panelMain.remove(panelRight3);
	        	panelMain.remove(panelRight4);
	        	panelMain.remove(panelRight5);
	        	panelMain.remove(panelRight6);
	        	panelMain.add(panelRight2);
	        	panelMain.revalidate();
	        	panelMain.repaint();
	        }

	     });
		
		btnLeft3.addActionListener(new ActionListener() 
		{
	        public void actionPerformed(ActionEvent e) 
	        {
	        	showTextStatusPanel();
	        }
	     });
		
		btnLeft4.addActionListener(new ActionListener() 
		{
	        public void actionPerformed(ActionEvent e) 
	        {
	        	panelMain.remove(panelRight1);
	        	panelMain.remove(panelRight2);
	        	panelMain.remove(panelRight3);
	        	panelMain.remove(panelRight5);
	        	panelMain.remove(panelRight6);
	        	panelMain.add(panelRight4);
	        	panelMain.revalidate();
	        	panelMain.repaint();
	        }

	     });
		
		btnLeft5.addActionListener(new ActionListener() 
		{
	        public void actionPerformed(ActionEvent e) 
	        {
	        	panelMain.remove(panelRight1);
	        	panelMain.remove(panelRight2);
	        	panelMain.remove(panelRight3);
	        	panelMain.remove(panelRight4);
	        	panelMain.remove(panelRight6);
	        	panelMain.add(panelRight5);
	        	panelMain.revalidate();
	        	panelMain.repaint();
	        }

	     });
		
		btnLeft6.addActionListener(new ActionListener() 
		{
	        public void actionPerformed(ActionEvent e) 
	        {
	        	panelMain.remove(panelRight1);
	        	panelMain.remove(panelRight2);
	        	panelMain.remove(panelRight3);
	        	panelMain.remove(panelRight4);
	        	panelMain.remove(panelRight5);
	        	panelMain.add(panelRight6);
	        	panelMain.revalidate();
	        	panelMain.repaint();
	        }
	     });
		
		// ----------------- Output Setting: ---------------
		final TitledPanel panelDir = new TitledPanel("Output Setting:");
	    panelDir.setLayout(new GridLayout(2,2));

	    final JLabel labelDirectory = new JLabel("Please choose the output directory.");
		panelDir.add(labelDirectory);
		
		panelDir.add(new JLabel(""));
		
		textDir.setText(prop.getOutput_path());
		
		final JButton btnDir = new JButton("Browse");

		btnDir.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) {
				textDir.setText(IJ.getDirectory("Choose Output Directory"));
				
				prop.setOutput_path(textDir.getText());
			}
		});
		
		panelDir.add(textDir);
		panelDir.add(btnDir);

		panelRight1.add(panelDir);
		
		// ----------------- Stimulus Setting: ---------------
		
		final TitledPanel panelStimulus = new TitledPanel("Stimulus Setting:");
		panelStimulus.setLayout(new GridLayout(2,2));

		panelStimulus.add(new JLabel("Please choose a stimulus."));
		
		final JLabel lableWarn = new JLabel("(Need to choose this)");
		lableWarn.setForeground(Color.red);
		panelStimulus.add(lableWarn);
		
		final ButtonGroup groupStimulus = new ButtonGroup();
		
		groupStimulus.add(radioBlue);
		groupStimulus.add(radioChrimson);
		
		panelStimulus.add(radioBlue);
		panelStimulus.add(radioChrimson);
		
		panelRight1.add(panelStimulus);
		
		radioBlue.addItemListener(new ItemListener() 
		{
	         public void itemStateChanged(ItemEvent e) {         
	        	 // if Optogenetic radio button is checked
	        	 // set chrimson_stimulus property to false
	        	prop.setChrimson_stimulus(PropertyManager.getBoolStr( !(e.getStateChange()==1)) );
	         }           
	     });
		
		radioChrimson.addItemListener(new ItemListener() 
		{
	         public void itemStateChanged(ItemEvent e) {         
	        	 // if Optogenetic radio button is checked
	        	 // set chrimson_stimulus property to false
	        	 prop.setChrimson_stimulus(PropertyManager.getBoolStr( e.getStateChange()==1) );
	         }           
	     });
	      
		// ----------------- Output Options: ---------------
		
		final TitledPanel panelOptions = new TitledPanel("Output Options:");
		panelOptions.setLayout(new GridLayout(2,3));
		
		panelOptions.add(new JLabel("Pease choose what to be output."));
		panelOptions.add(new JLabel(""));
		panelOptions.add(new JLabel(""));
		
		cbCurl.setSelected( PropertyManager.getBool(prop.getOutput_curl()) );
		cbSpeed.setSelected( PropertyManager.getBool(prop.getOutput_speed()) );
		cbRoll.setSelected( PropertyManager.getBool(prop.getOutput_roll()) );
		
		panelOptions.add(cbCurl);
		panelOptions.add(cbSpeed);
		panelOptions.add(cbRoll);
		
		cbCurl.addItemListener(new ItemListener() {
	         public void itemStateChanged(ItemEvent e) {  
	        	 prop.setOutput_curl(PropertyManager.getBoolStr( e.getStateChange()==1 ));
	         }          
         });
		
		cbSpeed.addItemListener(new ItemListener() {
	         public void itemStateChanged(ItemEvent e) {  
	        	 prop.setOutput_speed( PropertyManager.getBoolStr( e.getStateChange()==1 ) );
	         }          
        });
		
		cbRoll.addItemListener(new ItemListener() {
	         public void itemStateChanged(ItemEvent e) {  
	        	 prop.setOutput_roll( PropertyManager.getBoolStr( e.getStateChange()==1 ) );
	         }          
       });
		
		panelRight1.add(panelOptions);
		
		// ------------------- Buttons -----------------------
		
		panelBottom.setLayout(new BorderLayout());
		
		final JPanel panelButtons = new JPanel();
		panelButtons.setLayout(new GridLayout(1,5));

		btnStart = new JButton("Start");
		panelButtons.add(btnStart);
		panelButtons.add(btnUpdateSet);
		final JButton btnCancel = new JButton("Exit");
		panelButtons.add(btnCancel);
		panelButtons.add(btnDefault);
		final JButton btnInfo = new JButton("About");
		panelButtons.add(btnInfo);
		
		panelBottom.add( panelButtons, BorderLayout.CENTER );
		
		final JPanel panelSystemStatus = new JPanel();
		panelSystemStatus.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		panelSystemStatus.add( textSysStatus );
		
		panelBottom.add( panelSystemStatus, BorderLayout.SOUTH );
		
		panelMain.add(panelBottom, BorderLayout.SOUTH);
	    
		btnCancel.addActionListener(new ActionListener() 
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	int confirmed = JOptionPane.showConfirmDialog(null, 
				        "Are you sure you want to exit the program?", "Message",
				        JOptionPane.YES_NO_OPTION);

			    if (confirmed == JOptionPane.YES_OPTION) {
			    	System.exit(0);
//			    	frame.dispose();
			    }
				    
		    }
		});
		
		btnInfo.addActionListener(new ActionListener() 
		{
	        public void actionPerformed(ActionEvent e) 
	        {
	        	JOptionPane.showMessageDialog(frame, "Larva Behavior Quantification System v1.03 -\nThis software uses libraries from the FFmpeg project under the LGPLv2.1.", "System Version", JOptionPane.INFORMATION_MESSAGE);
	        }

	     });
		
		// =================== Text Status JPanel ---------------
//		textStatus = new JTextArea("[System Status]:\n",5,20);
		textStatusPane = new JTextPane();
		
//		JScrollPane scrollPane = new JScrollPane(textStatus);
		JScrollPane scrollPane = new JScrollPane(textStatusPane);
		panelRight3.add(scrollPane);
		
//		HelperManager.showSysStatus(textStatus, "Processing AVI video: "+ directory + aviFile );
		HelperManager.showSysStatus(textStatusPane, "Processing AVI video: "+ directory + aviFile, Color.blue );
				
		// =================== Text Status JPanel ---------------
		final JPanel panelImageCrop1 = new JPanel();
		panelImageCrop1.setBackground(colorImageStatus);
		LayoutManager overlayImageCrop1 = new OverlayLayout(panelImageCrop1);
		panelImageCrop1.setLayout(overlayImageCrop1);
		
		final JLabel labelCropText1 = new JLabel("crop 1");
		labelCropText1.setForeground(Color.GREEN);
		labelCropText1.setAlignmentX(0.5f);
		labelCropText1.setAlignmentY(0.5f);
		labelsCropText.add( labelCropText1 );
		
		final JLabel labelCrop1 = new JLabel(sampleCropImageIcon);
		labelCrop1.setAlignmentX(0.5f);
		labelCrop1.setAlignmentY(0.5f);
		
		labelsCrop.add(labelCrop1);
		
        panelImageCrop1.add( labelCropText1 );
        panelImageCrop1.add( labelCrop1 );
        panelRight6.add(panelImageCrop1);
		
        
        final JPanel panelImageCrop2 = new JPanel();
        panelImageCrop2.setBackground(colorImageStatus);
		LayoutManager overlayImageCrop2 = new OverlayLayout(panelImageCrop2);
		panelImageCrop2.setLayout(overlayImageCrop2);
		
		final JLabel labelCropText2 = new JLabel("crop 2");
		labelCropText2.setForeground(Color.GREEN);
		labelCropText2.setAlignmentX(0.5f);
		labelCropText2.setAlignmentY(0.5f);
		labelsCropText.add( labelCropText2 );
		
		final JLabel labelCrop2 = new JLabel(sampleCropImageIcon);
		labelCrop2.setAlignmentX(0.5f);
		labelCrop2.setAlignmentY(0.5f);
		labelsCrop.add(labelCrop2);
		
		panelImageCrop2.add( labelCropText2 );
		panelImageCrop2.add( labelCrop2 );
        panelRight6.add(panelImageCrop2);
        
        final JPanel panelImageCrop3 = new JPanel();
        panelImageCrop3.setBackground(colorImageStatus);
		LayoutManager overlayImageCrop3 = new OverlayLayout(panelImageCrop3);
		panelImageCrop3.setLayout(overlayImageCrop3);
		
		final JLabel labelCropText3 = new JLabel("crop 3");
		labelCropText3.setForeground(Color.GREEN);
		labelCropText3.setAlignmentX(0.5f);
		labelCropText3.setAlignmentY(0.5f);
		labelsCropText.add( labelCropText3 );
		
		final JLabel labelCrop3 = new JLabel(sampleCropImageIcon);
		labelCrop3.setAlignmentX(0.5f);
		labelCrop3.setAlignmentY(0.5f);
		labelsCrop.add(labelCrop3);
		
		panelImageCrop3.add( labelCropText3 );
		panelImageCrop3.add( labelCrop3 );
        panelRight6.add(panelImageCrop3);
        
		final JPanel panelImageBinary1 = new JPanel();
		panelImageBinary1.setBackground(colorImageStatus);
		LayoutManager overlayImageBinary1 = new OverlayLayout(panelImageBinary1);
		panelImageBinary1.setLayout(overlayImageBinary1);
		
		final JLabel labelBinaryText1 = new JLabel("Binary 1");
		labelBinaryText1.setForeground(Color.GREEN);
		labelBinaryText1.setAlignmentX(0.5f);
		labelBinaryText1.setAlignmentY(0.5f);
		labelsBinaryText.add( labelBinaryText1 );
		
		final JLabel labelBinary1 = new JLabel(sampleBinaryImageIcon);
		labelBinary1.setAlignmentX(0.5f);
		labelBinary1.setAlignmentY(0.5f);
		labelsBinary.add(labelBinary1);
		
		panelImageBinary1.add( labelBinaryText1 );
		panelImageBinary1.add( labelBinary1 );
        panelRight6.add(panelImageBinary1);
		
        final JPanel panelImageBinary2 = new JPanel();
        panelImageBinary2.setBackground(colorImageStatus);
		LayoutManager overlayImageBinary2 = new OverlayLayout(panelImageBinary2);
		panelImageBinary2.setLayout(overlayImageBinary2);
		
		final JLabel labelBinaryText2 = new JLabel("Binary 2");
		labelBinaryText2.setForeground(Color.GREEN);
		labelBinaryText2.setAlignmentX(0.5f);
		labelBinaryText2.setAlignmentY(0.5f);
		labelsBinaryText.add( labelBinaryText2 );
		
		final JLabel labelBinary2 = new JLabel(sampleBinaryImageIcon);
		labelBinary2.setAlignmentX(0.5f);
		labelBinary2.setAlignmentY(0.5f);
		labelsBinary.add(labelBinary2);
		
		panelImageBinary2.add( labelBinaryText2 );
		panelImageBinary2.add( labelBinary2 );
        panelRight6.add(panelImageBinary2);
        
        final JPanel panelImageBinary3 = new JPanel();
        panelImageBinary3.setBackground(colorImageStatus);
		LayoutManager overlayImageBinary3 = new OverlayLayout(panelImageBinary3);
		panelImageBinary3.setLayout(overlayImageBinary3);
		
		final JLabel labelBinaryText3 = new JLabel("Binary 3");
		labelBinaryText3.setForeground(Color.GREEN);
		labelBinaryText3.setAlignmentX(0.5f);
		labelBinaryText3.setAlignmentY(0.5f);
		labelsBinaryText.add( labelBinaryText3 );
		
		final JLabel labelBinary3 = new JLabel(sampleBinaryImageIcon);
		labelBinary3.setAlignmentX(0.5f);
		labelBinary3.setAlignmentY(0.5f);
		labelsBinary.add(labelBinary3);
		
		panelImageBinary3.add( labelBinaryText3 );
		panelImageBinary3.add( labelBinary3 );
        panelRight6.add(panelImageBinary3);
		
		// =================== AVI Generation JPanel ---------------
		panelRight4.add(new JLabel("Path of ffmpeg.exe:"));
		final JTextField textExePath = new JTextField("ffmpeg\\bin\\ffmpeg.exe", 5);
		panelRight4.add(textExePath);
		
		panelRight4.add(new JLabel("Path of the input AVI file:"));
		panelRight4.add(new JLabel("(e.g: E:\\Blue_All_%d.jpg)"));
		final JTextField textImgInput = new JTextField("E:\\1\\", 5);
		panelRight4.add(textImgInput);
		
		panelRight4.add(new JLabel("Path of the output AVI file:"));
		final JTextField textAviOutput = new JTextField("E:\\1\\", 5);
		panelRight4.add(textAviOutput);
		
		panelRight4.add(new JLabel("Start No. of Images:"));
		final JTextField textNumStart = new JTextField("1", 5);
		panelRight4.add(textNumStart);
		
		final JButton btnGenerate = new JButton("Generate AVI");
//		panelRight4.add(btnGenerate);
		
//		btnGenerate.addActionListener(new ActionListener() 
//		{
//	        public void actionPerformed(ActionEvent e) 
//	        {
//	        	HelperManager.showSysStatus(textStatus, "Generating AVI file from images ...");
//	        	AVIManager.generateAVI(textExePath.getText(), textImgInput.getText(), textAviOutput.getText(), textNumStart.getText(), textStatus);
//	        }
//
//	     });
		
		// ----------------- Advanced Options: ---------------
		
		final TitledPanel panelAdvanced = new TitledPanel("Debug Options 1:");
		panelAdvanced.setLayout(new GridLayout(0,2));
		boolean isAllFrame = prop.getFrom_frame().equals("0") && prop.getTo_frame().equals("0");
		
		panelAdvanced.add(radioAllFrame);
		
		panelAdvanced.add(radioRangeFrame);
		
		panelAdvanced.add(new JLabel("Begin Frame  (0,1: Fist frame):"));
		panelAdvanced.add(textFromFrame);
		
		panelAdvanced.add(new JLabel("End Frame  (0: Last frame):"));
		panelAdvanced.add(textToFrame);
		
		if( prop.getFrom_frame().equals("0") && prop.getTo_frame().equals("0")) 
		{
			textFromFrame.setEnabled(false);
   		 	textToFrame.setEnabled(false);
		}else{
			textFromFrame.setEnabled(true);
   		 	textToFrame.setEnabled(true);
		}
		
		panelRight2.add(panelAdvanced, BorderLayout.NORTH);
		
		radioAllFrame.addItemListener(new ItemListener() 
		{
	         public void itemStateChanged(ItemEvent e) 
	         {         
	        	 if(e.getStateChange()==1)
	        	 {
	        		 textFromFrame.setEnabled(false);
	        		 textToFrame.setEnabled(false);
	        		 radioRangeFrame.setSelected(false);
	        	 }else{
	        		 textFromFrame.setEnabled(true);
	        		 textToFrame.setEnabled(true);
	        		 radioRangeFrame.setSelected(true);
	        	 }
	         }           
	     });
		
		radioRangeFrame.addItemListener(new ItemListener() 
		{
	         public void itemStateChanged(ItemEvent e) 
	         {         
	        	 if(e.getStateChange()==1)
	        	 {
	        		 textFromFrame.setEnabled(true);
	        		 textToFrame.setEnabled(true);
	        		 radioAllFrame.setSelected(false);
	        	 }else{
	        		 textFromFrame.setEnabled(false);
	        		 textToFrame.setEnabled(false);
	        		 radioAllFrame.setSelected(true);
	        	 }
	         }           
	     });
		
		// ----------------- Advanced Debug Options 2: ---------------
		
		final TitledPanel panelDebug = new TitledPanel("Debug Options 2:");
		panelDebug.setLayout(new GridLayout(0,2));
		
		panelDebug.add(cbVideo);
		
		final JLabel lableVideo = new JLabel("(Only Work on Windows)");
		lableVideo.setForeground(Color.red);
		
		panelDebug.add(lableVideo);
		
		panelDebug.add(cbAnimatedImage);
		panelDebug.add(new JLabel(""));
		
		panelDebug.add(cbDebug);
		panelDebug.add(new JLabel(""));
		
		panelDebug.add(cbCSV);
		
		cbVideo.addItemListener(new ItemListener() {
	         public void itemStateChanged(ItemEvent e) {  
	        	 prop.setOutput_video( PropertyManager.getBoolStr( e.getStateChange()==1 ) );
	         }          
        });
		
		cbAnimatedImage.addItemListener(new ItemListener() {
	         public void itemStateChanged(ItemEvent e) {  
	        	 prop.setOutput_animated_image( PropertyManager.getBoolStr( e.getStateChange()==1 ) );
	         }          
		});
		
		cbDebug.addItemListener(new ItemListener() {
	         public void itemStateChanged(ItemEvent e) {  
	        	 prop.setOutput_debug( PropertyManager.getBoolStr( e.getStateChange()==1 ) );
	         }          
		});
		
		panelRight2.add(panelDebug, BorderLayout.CENTER);
		
		cbCSV.addItemListener(new ItemListener() {
	         public void itemStateChanged(ItemEvent e) {  
	        	 prop.setOutput_complete_csv( PropertyManager.getBoolStr( e.getStateChange()==1 ) );
	         }          
       });
		
		// ----------------- Advanced Debug Options 3: ---------------
		
		final TitledPanel panelDebug3 = new TitledPanel("Debug Options 3:");
		panelDebug3.setLayout(new GridLayout(1,1));
		
		panelRight2.add(panelDebug3, BorderLayout.SOUTH);
		
		panelDebug3.add( cbStatSegmentation );
		cbStatSegmentation.setSelected( PropertyManager.getBool( prop.getUse_pca() ) );
		
		final JLabel lableSegmentation = new JLabel("(Statistical segm. may cause exceptions.)");
		lableSegmentation.setForeground(Color.red);
		panelDebug3.add(lableSegmentation);
		
//		panelDebug3.add( cbTrainParticularLarva );
//		cbTrainParticularLarva.setSelected( PropertyManager.getBool( prop.getTrain_particular_larva() ) );
//		cbTrainParticularLarva.setEnabled( false ); 
		
		cbStatSegmentation.addItemListener(new ItemListener() {
	         public void itemStateChanged(ItemEvent e) {  
	        	 prop.setUse_pca( PropertyManager.getBoolStr( e.getStateChange()==1 ) );
	        	 
	         }          
		});
		
//		cbStatSegmentation.addItemListener(new ItemListener() 
//		{
//		    @Override
//		    public void itemStateChanged(ItemEvent e) 
//		    {
//		    	// if the "Use Statistical segmentation" option is checked,
//	        	// the "Train Using Frames of a Particular Larva" option is disable.
//		        if (e.getStateChange() == ItemEvent.SELECTED) {
//		        	cbTrainParticularLarva.setEnabled( true ); 
//		        } else {
//		        	cbTrainParticularLarva.setEnabled( false ); 
//		        }
//		    }
//		});
		
//		cbTrainParticularLarva.addItemListener(new ItemListener() {
//	         public void itemStateChanged(ItemEvent e) {  
//	        	 prop.setTrain_particular_larva( PropertyManager.getBoolStr( e.getStateChange()==1 ) );
//	         }          
//		});
		
		// ----------------- Exceptions Panel: ---------------
		
		final TitledPanel panelDebugTab = new TitledPanel("Exceptions:");
		panelDebugTab.setLayout(new GridLayout(0,2));
		
		textException = new JTextArea("[System exceptions]:\n",5,20);
		
		textException.setFont(new Font("SansSerif", Font.PLAIN, 14));
		
		JScrollPane scrollPaneException = new JScrollPane(textException);
		panelRight5.add(scrollPaneException);
		
//		PrintStream printStream = new PrintStream(new CustomOutputStream(textStatus, textException));
		PrintStream printStream = new PrintStream(new CustomOutputStream(textStatusPane));
         
        // keeps reference of standard output stream
        standardOut = System.out;
         
        // re-assigns standard output stream and error output stream
//        System.setOut(printStream);
        System.setErr(printStream);
	        
//		panelDebugTab.add(cbAutoRoll);
//		panelDebugTab.add(new JLabel(""));
//		
//		panelDebugTab.add(new JLabel("% of Larva Perimeter:"));
//		panelDebugTab.add(textAutoRollPerc);
//		
//		if( !PropertyManager.getBool( prop.getAuto_roll() ) )
//			textAutoRollPerc.setEnabled(false);
//		else
//			textAutoRollPerc.setEnabled(true);
//		
//		panelDebugTab.add(new JLabel("Set Larva Perimeter:"));
//		panelDebugTab.add(textAutoRoll);
//		
//		if( PropertyManager.getBool( prop.getAuto_roll() ) )
//			textAutoRoll.setEnabled(false);
//		else
//			textAutoRoll.setEnabled(true);
//
//		
//		panelDebugTab.add(cbAutoCheckSize);
//		panelDebugTab.add(new JLabel(""));
//		
//		panelDebugTab.add(new JLabel("Minimum Larva Size:"));
//		panelDebugTab.add(textAutoMinSize);
//		
//		panelDebugTab.add(new JLabel("Maximum Larva Size:"));
//		panelDebugTab.add(textAutoMaxSize);
//		
//		if( PropertyManager.getBool( prop.getAuto_check_size() ) )
//		{
//			textAutoMinSize.setEnabled(false);
//			textAutoMaxSize.setEnabled(false);
//		}else{
//			textAutoMinSize.setEnabled(true);
//			textAutoMaxSize.setEnabled(true);
//		}
//		
//		panelDebugTab.add(cbAutoCheckSkeleton);
//		panelDebugTab.add(new JLabel(""));
//		
//		panelDebugTab.add(new JLabel("Minimum Larva Skeleton:"));
//		panelDebugTab.add(textAutoMinSkeleton);
//		
//		panelDebugTab.add(new JLabel("Maximum Larva Skeleton:"));
//		panelDebugTab.add(textAutoMaxSkeleton);
//		
//		if( PropertyManager.getBool( prop.getAuto_check_skeleton() ) )
//		{
//			textAutoMinSkeleton.setEnabled(false);
//			textAutoMaxSkeleton.setEnabled(false);
//		}else{
//			textAutoMinSkeleton.setEnabled(true);
//			textAutoMaxSkeleton.setEnabled(true);
//		}
//		
//		panelRight5.add(panelDebugTab);
//
//		cbAutoRoll.addItemListener(new ItemListener() {
//	         public void itemStateChanged(ItemEvent e) { 
//	        	 
//	        	 prop.setAuto_roll( PropertyManager.getBoolStr( e.getStateChange()==1 ) );
//	        	 
//	        	 if(e.getStateChange()==1)
//	        	 {
//	        		 textAutoRoll.setEnabled(false);
//	        		 textAutoRollPerc.setEnabled(true);
//	        	 }else{
//	        		 textAutoRoll.setEnabled(true);
//	        		 textAutoRollPerc.setEnabled(false);
//	        	 }
//	         }          
//        });
//				
//		cbAutoCheckSize.addItemListener(new ItemListener() {
//	         public void itemStateChanged(ItemEvent e) { 
//	        	 
//	        	 prop.setAuto_roll( PropertyManager.getBoolStr( e.getStateChange()==1 ) );
//	        	 
//	        	 if(e.getStateChange()==1)
//	        	 {
//	        		 textAutoMinSize.setEnabled(false);
//	        		 textAutoMaxSize.setEnabled(false);
//	        	 }else{
//	        		 textAutoMinSize.setEnabled(true);
//	        		 textAutoMaxSize.setEnabled(true);
//	        	 }
//	         }          
//       });
//		
//		cbAutoCheckSkeleton.addItemListener(new ItemListener() {
//	         public void itemStateChanged(ItemEvent e) { 
//	        	 
//	        	 prop.setAuto_roll( PropertyManager.getBoolStr( e.getStateChange()==1 ) );
//	        	 
//	        	 if(e.getStateChange()==1)
//	        	 {
//	        		 textAutoMinSkeleton.setEnabled(false);
//	        		 textAutoMaxSkeleton.setEnabled(false);
//	        	 }else{
//	        		 textAutoMinSkeleton.setEnabled(true);
//	        		 textAutoMaxSkeleton.setEnabled(true);
//	        	 }
//	         }          
//      });
		
		// ******************************************
		// **************** btnDefault ****************
		
		btnDefault.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)
		    {
		    	int confirmed = JOptionPane.showConfirmDialog(null, 
				        "Are you sure you want reset the settings? All settings will be restored to default.", "Message",
				        JOptionPane.YES_NO_OPTION);

			    if (confirmed == JOptionPane.YES_OPTION) 
			    {
			    	if(FileManager.copyFile(new File("plugins/Larva/config.properties.bk"), 
			    			new File("plugins/Larva/config.properties") ) )
			    	{
			    		getAllOptions();
			    		
			    		JOptionPane.showMessageDialog(frame, "Successfully restore settings! Please restart the program to take effect.", "Information", JOptionPane.INFORMATION_MESSAGE);
				    }else{
				    	JOptionPane.showMessageDialog(frame, "Fail to restore settings!", "Error", JOptionPane.ERROR_MESSAGE);
				    }
			    }
		    }
		    
		});
		
		// **********************************************
		// **************** btnUpdateSet ****************
		btnUpdateSet.addActionListener(new ActionListener() 
		{
	        public void actionPerformed(ActionEvent e) 
	        {
	        	if(saveAllOptions())
	        		JOptionPane.showMessageDialog(frame, "Settings have been saved!","Information", JOptionPane.INFORMATION_MESSAGE);
	        	else
	        		JOptionPane.showMessageDialog(frame, "Fail to save all options!","Error", JOptionPane.ERROR_MESSAGE);
	        }

	     });
		// ******************************************
		// **************** btnStart ****************
		btnStart.addActionListener(new ActionListener() 
		{
	        public void actionPerformed(ActionEvent e) 
	        {
	        	File theDir = new File(textDir.getText());

	        	 // if the directory does not exist, create it
	        	 if (!theDir.exists())
	        	 {
	        		 JOptionPane.showMessageDialog(null, "The directory specified on \"General\" tab >> \"Output Settings\" doesn't exist!", "Message", JOptionPane.INFORMATION_MESSAGE);
		        	   return;
	        	 }
	        	
	        	 File aviFile = new File(prop.getAvi_file());

	        	 // if the directory does not exist, create it
	        	 if (!aviFile.exists())
	        	 {
	        		 JOptionPane.showMessageDialog(null, "The video file doesn't exist! Please restart the program.", "Message", JOptionPane.INFORMATION_MESSAGE);
		        	   return;
	        	 }
	        	 
	        	if(groupStimulus.getSelection() == null)
	        	{
	        		JOptionPane.showMessageDialog(frame, "Please choose a stimulus from \"General\" tab >> \"Stimulus Setting\".", "Message", JOptionPane.INFORMATION_MESSAGE);
	        		
		        	return;
	        	}
	        		
	        	// save all options before start the program
	        	if(!saveAllOptions())
	        	{
	        		 int confirmed = JOptionPane.showConfirmDialog(null, 
	     			        "Fail to save all the options. Are you still want to run the program?", "Warning",
	     			        JOptionPane.WARNING_MESSAGE);
	     			    
     			    if (confirmed == JOptionPane.NO_OPTION) {
     			    	return;
     			    }
	        	}
        		
        		Rectangle rect = null;
        		
	        	if(listRoi == null)
	        	{
	        		JOptionPane.showMessageDialog(null, "No region on the video was selected! Please select a region on the video.");
	        		return;
	        	}
	        	
	        	Executor executor = Executors.newSingleThreadExecutor();
	        	
	        	int procIndex = 0;
        		for (Roi roi : listRoi) 
        		{
    				rect = roi.getBounds();
        		
    				prop.setStart_x( Integer.toString(rect.x) );
    				prop.setStart_y( Integer.toString(rect.y) );
    				// save the larva position on the video
    				if(!prop.saveAllProperties())
    				{
    					int confirmed = JOptionPane.showConfirmDialog(null, 
					        "Couldn't save larva position on the video. Would you want to run the program?", "Error",
					        JOptionPane.ERROR_MESSAGE);
					    
					    if (confirmed == JOptionPane.NO_OPTION) {
					    	return;
					    }
    				}
    				
    				LarvaController larvaProcessor = new LarvaController(self, procIndex, listRoi.size());
    				
    				executor.execute(larvaProcessor);
    				
    				procIndex ++;
        		}
        		
        	}

	     });
		
		getAllOptions();
	}
	
	/**
	* Get all options from property file.
	* 
	*/
	public void getAllOptions()
	{
		prop.getAllProperties();
		
		textDir.setText(prop.getOutput_path());
		cbCurl.setSelected( PropertyManager.getBool( prop.getOutput_curl() ) );
		cbSpeed.setSelected( PropertyManager.getBool( prop.getOutput_speed() ) );
		cbRoll.setSelected( PropertyManager.getBool( prop.getOutput_roll() ) );
    	
		boolean isAllFrame = prop.getFrom_frame().equals("0") && prop.getTo_frame().equals("0");

		if(isAllFrame)
		{
			radioAllFrame.setSelected(true);
			radioRangeFrame.setSelected(false);
		}else{
			radioAllFrame.setSelected(false);
			radioRangeFrame.setSelected(true);
		}
		
		textFromFrame.setText( prop.getFrom_frame() );
		textToFrame.setText( prop.getTo_frame() );
		
		cbVideo.setSelected( PropertyManager.getBool( prop.getOutput_video() ) );
		cbAnimatedImage.setSelected( PropertyManager.getBool( prop.getOutput_animated_image() ) );
		cbDebug.setSelected( PropertyManager.getBool( prop.getOutput_debug() ) );
		cbCSV.setSelected( PropertyManager.getBool( prop.getOutput_complete_csv() ) );
		
		System.out.println("prop.getFix_invalid_larva():"+prop.getFix_invalid_larva());
//		cbInvalidFix.setSelected( PropertyManager.getBool( prop.getFix_invalid_larva() ) );
		
		cbAutoRoll.setSelected( PropertyManager.getBool( prop.getAuto_roll() ) );
		textAutoRollPerc.setText( prop.getLarva_perimeter_percentage() );
		textAutoRoll.setText( prop.getLarva_perimeter() );
		
		cbAutoCheckSize.setSelected( PropertyManager.getBool( prop.getAuto_check_size() ) );
		textAutoMinSize.setText( prop.getMin_size() );
		textAutoMaxSize.setText( prop.getMax_size() );
		
		cbAutoCheckSkeleton.setSelected( PropertyManager.getBool( prop.getAuto_check_skeleton() ) );
		textAutoMinSkeleton.setText( prop.getMin_skeleton() );
		textAutoMaxSkeleton.setText( prop.getMax_skeleton() );
	}
	
	/**
	* Save all options to the property file.
	* 
	*/
	public boolean saveAllOptions()
	{
		prop.setOutput_path(textDir.getText());
    	prop.setChrimson_stimulus( PropertyManager.getBoolStr(radioChrimson.isSelected()) );
    	prop.setOutput_curl( PropertyManager.getBoolStr(cbCurl.isSelected() ) );
    	prop.setOutput_speed( PropertyManager.getBoolStr(cbSpeed.isSelected() ) );
    	prop.setOutput_roll( PropertyManager.getBoolStr( cbRoll.isSelected() ) );
    	
    	if(radioAllFrame.isSelected())
    	{
    		prop.setFrom_frame("0");
    		prop.setTo_frame("0");
    	// if the radio all frame is not select
    	}else{
    		
    		if(MathManager.isInteger( textFromFrame.getText() ))
    			prop.setFrom_frame(textFromFrame.getText());
    		else{
    			JOptionPane.showMessageDialog(frame, "\"Debug\" tab >> \"Begin Frame\" is not a number. Please enter a number for the field.", "Warning", JOptionPane.ERROR_MESSAGE);
    			return false;
    		}
    		
    		if(MathManager.isInteger( textToFrame.getText() ))
    			prop.setTo_frame(textToFrame.getText());
    		else{
    			JOptionPane.showMessageDialog(frame, "\"Debug\" tab >> \"End Frame\" is not a number. Please enter a number for the field.", "Warning", JOptionPane.ERROR_MESSAGE);
    			return false;
    		}
    		
    	}
    	
    	prop.setOutput_video(  PropertyManager.getBoolStr(cbVideo.isSelected() )  );
    	
    	prop.setOutput_animated_image(  PropertyManager.getBoolStr(cbAnimatedImage.isSelected() ) );
    	prop.setOutput_debug( PropertyManager.getBoolStr(cbDebug.isSelected() ) );
    	prop.setOutput_complete_csv(  PropertyManager.getBoolStr(cbCSV.isSelected() )  );
    	
//    	prop.setFix_invalid_larva( PropertyManager.getBoolStr( cbInvalidFix.isSelected() ) );
    	
    	prop.setAuto_roll(  PropertyManager.getBoolStr(cbAutoRoll.isSelected() ) );
    	
    	if(MathManager.isDouble( textAutoRollPerc.getText() ))
    		prop.setLarva_perimeter_percentage( textAutoRollPerc.getText() );
		else{
			JOptionPane.showMessageDialog(frame, "\"Threshold\" tab >> \"% of Larva Perimeter\" is not a decimal. Please enter a decimal for the field.", "Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		}
    	
    	if(MathManager.isDouble( textAutoRoll.getText() ))
    		prop.setLarva_perimeter(textAutoRoll.getText());
		else{
			JOptionPane.showMessageDialog(frame, "\"Threshold\" tab >> \"Set Larva Perimeter\" is not a decimal. Please enter a decimal for the field.", "Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		}
    	
    	prop.setAuto_check_size( PropertyManager.getBoolStr( cbAutoCheckSize.isSelected() ) );
    	
    	if(MathManager.isDouble( textAutoMinSize.getText() ))
    		prop.setMin_size( textAutoMinSize.getText() );
		else{
			JOptionPane.showMessageDialog(frame, "\"Threshold\" tab >> \"Minimum Larva Size\" is not a decimal. Please enter a decimal for the field.", "Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		}
    	
    	if(MathManager.isDouble( textAutoMinSize.getText() ))
    		prop.setMax_size( textAutoMaxSize.getText() );
		else{
			JOptionPane.showMessageDialog(frame, "\"Threshold\" tab >> \"Maximum Larva Size\" is not a decimal. Please enter a decimal for the field.", "Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		}
    	
    	prop.setAuto_check_skeleton( PropertyManager.getBoolStr( cbAutoCheckSkeleton.isSelected() ) );
    	
    	if(MathManager.isDouble( textAutoMinSkeleton.getText() ))
    		prop.setMin_skeleton( textAutoMinSkeleton.getText() );
		else{
			JOptionPane.showMessageDialog(frame, "\"Threshold\" tab >> \"Minimum Larva Skeleton\" is not a decimal. Please enter a decimal for the field.", "Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		}
    	
    	if(MathManager.isDouble( textAutoMaxSkeleton.getText() ))
    		prop.setMax_skeleton( textAutoMaxSkeleton.getText() );
		else{
			JOptionPane.showMessageDialog(frame, "\"Threshold\" tab >> \"Maximum Larva Skeleton\" is not a decimal. Please enter a decimal for the field.", "Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		}
			
    	if(prop.saveAllProperties())
    		return true;
    	
		return false;
	}
	
	/**
	* Show all buttons.
	* 
	*/
	public void showAllButtons()
	{
		getBtnStart().setEnabled(true);
		getBtnLeft1().setEnabled(true);
		getBtnLeft2().setEnabled(true);
		getBtnLeft5().setEnabled(false);
		getBtnLeft4().setEnabled(true);
		getBtnDefault().setEnabled(true);
		getBtnUpdateSet().setEnabled(true);
		
		getPanelRight1().setEnabled(true);
		getPanelRight2().setEnabled(true);
		getPanelRight4().setEnabled(true);
		getPanelRight5().setEnabled(true);
	}
	
	/**
	* Set the sample icons on the Image Status Window to show the images have been extracted.
	* 
	*/
	public void updateImageStatusDone()
	{
		for(int i = 0; i < 3; i++)
		{
			getLabelsCrop().get(i).setIcon(getSampleCropImageIcon());
			getLabelsCropText().get(i).setText( "Read all frames" );
			
			getLabelsBinary().get(i).setIcon(getSampleBinaryImageIcon());
			getLabelsBinaryText().get(i).setText( "Read all frames" );
		}
		
		showTextStatusPanel();
	}
	
	/**
	* Update the image status window with the newest binary and cropped images.
	* 
	* @param dirImageDebug The image debug directory.
	* @param frameId The frame Id.
	* @return None.
	*/
	public void updateImageStatus(String dirImageDebug, int frameId)
	{
		final ImageIcon imgaeIconCrop = new ImageIcon(dirImageDebug+"dCrop/Crop_"+ frameId + ".jpg");
		final ImageIcon imgaeIcon2 = (ImageIcon) getLabelsCrop().get(2).getIcon();
		final ImageIcon imgaeIcon1 = (ImageIcon) getLabelsCrop().get(1).getIcon();

		getLabelsCrop().get(2).setIcon(imgaeIconCrop);
		getLabelsCropText().get(2).setText( "frame " + Integer.toString( frameId ) );
		
		if(frameId - Integer.parseInt( prop.getFrom_frame() ) >= 2)
		{
			getLabelsCrop().get(1).setIcon(imgaeIcon2);
			getLabelsCropText().get(1).setText( "frame " + Integer.toString( frameId - 1 ) );
			getLabelsCrop().get(0).setIcon(imgaeIcon1);
			getLabelsCropText().get(0).setText( "frame " + Integer.toString( frameId - 2 ) );
		}
		
		final ImageIcon imgaeIconBinary = new ImageIcon(dirImageDebug+"dBinary/Binary_"+ frameId + ".jpg");
		final ImageIcon imgaeIconBinary2 = (ImageIcon) getLabelsBinary().get(2).getIcon();
		final ImageIcon imgaeIconBinary1 = (ImageIcon) getLabelsBinary().get(1).getIcon();
		
		getLabelsBinary().get(2).setIcon(imgaeIconBinary);
		getLabelsBinaryText().get(2).setText( "frame " + Integer.toString( frameId ) );
		
		if(frameId - Integer.parseInt( prop.getFrom_frame() ) >= 2)
		{
			getLabelsBinary().get(1).setIcon(imgaeIconBinary2);
			
			getLabelsBinaryText().get(1).setText( "frame " + Integer.toString( frameId - 1 ) );
			
			getLabelsBinary().get(0).setIcon(imgaeIconBinary1);
			
			getLabelsBinaryText().get(0).setText( "frame " + Integer.toString( frameId - 2 ) );
		}
	}
	
	/**
	* Change font for all element on the component.
	* 
	* @param component The component.
	* @param font The font.
	* @return None.
	*/
	public static void changeFont ( Component component, Font font )
	{
	    component.setFont ( font );
	    if ( component instanceof Container )
	    {
	        for ( Component child : ( ( Container ) component ).getComponents () )
	        {
	            changeFont ( child, font );
	        }
	    }
	}
	
	/**
	* Show text status.
	* 
	*/
	public void appendTextStatusPanel(String text, Color color)
	{
//		optionComponent.getTextStatus().append(status);
		
		StyledDocument doc = textStatusPane.getStyledDocument();

		Style style = textStatusPane.addStyle("I'm a Style", null);
		StyleConstants.setForeground(style, color);

		try
		{
			doc.insertString(doc.getLength(), text, style);
		} catch (BadLocationException e)
		{
		}
		
		textStatusPane.setCaretPosition(textStatusPane.getDocument().getLength());
		textStatusPane.validate();
	}
	
	/**
	* Show text status window on the UI.
	* 
	*/
	public void showTextStatusPanel()
	{
    	panelMain.remove(panelRight1);
    	panelMain.remove(panelRight2);
    	panelMain.remove(panelRight4);
    	panelMain.remove(panelRight5);
    	panelMain.remove(panelRight6);
    	panelMain.add(panelRight3);
    	panelMain.revalidate();
    	panelMain.repaint();
	}
	
	/**
	* Show exception tab.
	* 
	*/
	public void showExceptionPanel()
	{
    	panelMain.remove(panelRight1);
    	panelMain.remove(panelRight2);
    	panelMain.remove(panelRight3);
    	panelMain.remove(panelRight4);
    	panelMain.remove(panelRight6);
    	panelMain.add(panelRight5);
    	panelMain.revalidate();
    	panelMain.repaint();
	}
	
	/**
	* Hide elements on other panels instead of image status panel.
	* 
	*/
	public void showImageStatusPanel()
	{
		btnStart.setEnabled(false);
		btnLeft1.setEnabled(false);
		btnLeft2.setEnabled(false);
		btnLeft5.setEnabled(false);
		btnLeft4.setEnabled(false);
		btnDefault.setEnabled(false);
		btnUpdateSet.setEnabled(false);
		
		panelRight1.setEnabled(false);
		panelRight2.setEnabled(false);
		panelRight4.setEnabled(false);
		panelRight5.setEnabled(false);
		
		panelMain.remove(panelRight1);
		panelMain.remove(panelRight2);
		panelMain.remove(panelRight3);
		panelMain.remove(panelRight4);
		panelMain.remove(panelRight5);
		panelMain.add(panelRight6);
		panelMain.revalidate();
		panelMain.repaint();
	}
	
	/**
	 * Show the frame.
	 */
	public void show()
	{
		frame.setVisible(true);
	}
	
	public static void main(String[] args) 
	{
		OptionComponent optionComponent = new OptionComponent("E:\\","larva.avi","Setting - Larva Behavior Quantification", null);
		optionComponent.show();
	}

	public JButton getBtnStart() {
		return btnStart;
	}

	public void setBtnStart(JButton btnStart) {
		this.btnStart = btnStart;
	}

	public JButton getBtnLeft1() {
		return btnLeft1;
	}

	public JButton getBtnLeft2() {
		return btnLeft2;
	}

	public JButton getBtnLeft5() {
		return btnLeft5;
	}

	public JButton getBtnLeft4() {
		return btnLeft4;
	}

	public JButton getBtnLeft3() {
		return btnLeft3;
	}

	public JButton getBtnDefault() {
		return btnDefault;
	}

	public JButton getBtnUpdateSet() {
		return btnUpdateSet;
	}

	public TitledPanel getPanelRight1() {
		return panelRight1;
	}

	public TitledPanel getPanelRight2() {
		return panelRight2;
	}

	public TitledPanel getPanelRight3() {
		return panelRight3;
	}

	public TitledPanel getPanelRight4() {
		return panelRight4;
	}

	public TitledPanel getPanelRight5() {
		return panelRight5;
	}

	public ArrayList<JLabel> getLabelsCrop() {
		return labelsCrop;
	}

	public void setLabelsCrop(ArrayList<JLabel> labelsCrop) {
		this.labelsCrop = labelsCrop;
	}

	public ArrayList<JLabel> getLabelsBinary() {
		return labelsBinary;
	}

	public void setLabelsBinary(ArrayList<JLabel> labelsBinary) {
		this.labelsBinary = labelsBinary;
	}

	public ArrayList<JLabel> getLabelsCropText() {
		return labelsCropText;
	}

	public void setLabelsCropText(ArrayList<JLabel> labelsCropText) {
		this.labelsCropText = labelsCropText;
	}

	public ArrayList<JLabel> getLabelsBinaryText() {
		return labelsBinaryText;
	}

	public void setLabelsBinaryText(ArrayList<JLabel> labelsBinaryText) {
		this.labelsBinaryText = labelsBinaryText;
	}

	public JLabel getTextSysStatus() {
		return textSysStatus;
	}

	public ImageIcon getSampleCropImageIcon() {
		return sampleCropImageIcon;
	}

	public ImageIcon getSampleBinaryImageIcon() {
		return sampleBinaryImageIcon;
	}

	public JPanel getPanelMain() {
		return panelMain;
	}
}
