package annotool.gui;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import java.awt.BorderLayout;
import java.awt.event.*;


/* 
 * The menu items and their action listeners
 * This class also sets the output mode based on file menu selection.
 */
public class AnnMenuBar implements ActionListener {

	AnnotatorGUI frame;
	final String NEWWZ = "New Wizard";
	final String LOADST = "Load Training/Testing Data Sets..";
	final String LOADCVST = "Load Cross Validation Data Set..";
	final String LOADROIST = "Load ROI (RegionOfInterest) Annotation Data Set..";
	final String LOADEX  = "Load Examples..";
	final String EXAMPLE1  = "Fruitfly Brain Neuronal Bundles (2D) ";
	final String EXAMPLE2  = "Fruitfly Brain Neuronal Bundles (3D) ";
	final String imageResTT = "images/Open16_TT.gif";
	final String imageResCV = "images/Open16_CV.gif";
	final String imageResROI = "images/Open16_ROI.gif";
	final String imageResPA = "images/Preferences16.gif";
	final String imageResHelp = "images/ContextualHelp16.gif";
	final String imageResAbout = "images/About16.gif";
	final String imageResExit ="images/exit16.gif";
	JButton btt, bcv, broi, bpa, babout, bhelp, bexit; //icons on toolbar
	ImageIcon  ttIcon= new ImageIcon(imageResTT);
	ImageIcon cvIcon = new ImageIcon(imageResCV);
	ImageIcon roiIcon = new ImageIcon(imageResROI);

	public AnnMenuBar(AnnotatorGUI frame)
	{
		this.frame = frame;
		
		//add tool bar
		buildToolBar();

		JMenuBar bar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');

		JMenuItem newWizardItem = new JMenuItem(NEWWZ);
		newWizardItem.addActionListener(this);
		
		JMenuItem loadItem = new JMenuItem(LOADST, ttIcon);
		loadItem.setMnemonic('L');
		loadItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_L, ActionEvent.ALT_MASK));
		loadItem.addActionListener(this);

		JMenuItem loadcvItem = new JMenuItem(LOADCVST, cvIcon);
		loadcvItem.setMnemonic('C');
		loadcvItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_C, ActionEvent.ALT_MASK));
		loadcvItem.addActionListener(this);

		JMenuItem loadroiItem = new JMenuItem(LOADROIST, roiIcon);
		loadroiItem.setMnemonic('R');
		loadroiItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_R, ActionEvent.ALT_MASK));
		loadroiItem.addActionListener(this);

		//load example data sets with configuration
		JMenu loadexMenu = new JMenu(LOADEX);
		JMenuItem example1Item = new JMenuItem(EXAMPLE1);
		JMenuItem example2Item = new JMenuItem(EXAMPLE2);
		loadexMenu.add(example1Item);
		loadexMenu.add(example2Item);
		example1Item.addActionListener(this);
		example2Item.addActionListener(this);
		loadexMenu.setEnabled(false); //03/10/2010. TO be enabled!

		JMenuItem saveItem = new JMenuItem("Save");
		saveItem.setMnemonic('S');
		saveItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_S, ActionEvent.ALT_MASK));
		saveItem.addActionListener(this);

		
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.setMnemonic('x');
		exitItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_X, ActionEvent.ALT_MASK));
		exitItem.addActionListener(this);
		//add them onto the right place.
		fileMenu.add(newWizardItem); 
		fileMenu.add(loadItem);
		fileMenu.add(loadcvItem);
		fileMenu.add(loadroiItem);
		fileMenu.add(loadexMenu);
		fileMenu.add(saveItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);

		//need to be disabled/enabled based on control panel selection
		JMenu paraMenu = new JMenu("Parameters");
		paraMenu.setMnemonic('P');
		JMenuItem fexParaItem = new JMenuItem("Feature Extractor Tuning");
		fexParaItem.setMnemonic('E');
		fexParaItem.addActionListener(this);
		JMenuItem fseParaItem = new JMenuItem("Feature Selector Tuning");
		fseParaItem.setMnemonic('S');
		fseParaItem.addActionListener(this);
		JMenuItem classParaItem = new JMenuItem("Classifier Tuning");
		classParaItem.setMnemonic('C');
		fseParaItem.addActionListener(this);
		paraMenu.add(fexParaItem);
		paraMenu.add(fseParaItem);
		paraMenu.add(classParaItem);
		classParaItem.setEnabled(false); //03/10/2010. To be enabled.

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		JMenuItem helpCItem = new JMenuItem("Help Contents");
		helpCItem.setMnemonic('e');
		helpCItem.addActionListener(this);
		JMenuItem aboutItem = new JMenuItem("About IANO (Image Annotation Tool)");
		aboutItem.setMnemonic('A');
		aboutItem.addActionListener(this);
		helpMenu.add(helpCItem);
		helpMenu.add(aboutItem);

		bar.add(fileMenu);
		bar.add(paraMenu);
		bar.add(buildLFMenu());
		//bar.add(buildComponentMenu());  //12/20/08: removed.
		bar.add(helpMenu);
		frame.setJMenuBar(bar);  //method of JFrame.

	}

	private void buildToolBar()
	{
		//add some image icon menus
		JToolBar toolBar = new JToolBar(); //(JToolBar.VERTICAL);

		java.net.URL url = null;
		boolean loaded = false;
		//2 ways of loading images. 
		//If in ImageJ plugin or in jar file, use resource
		//If in Eclipse or unjared, relative path works (Eclipse loads class from /bin)
		try
		{
			System.out.println("Load icon images from resources..");
			url = this.getClass().getResource("/"+imageResTT);
			System.out.println("url:"+ url);
			//ttIcon = new ImageIcon(frame.createImage((java.awt.image.ImageProducer)url.getContent()));
			ttIcon = new ImageIcon(url);
			btt = new JButton(ttIcon);

			url = this.getClass().getResource("/"+imageResCV);
			cvIcon = new ImageIcon(url);
			bcv = new JButton(cvIcon);

			url = this.getClass().getResource("/"+imageResROI);
			roiIcon = new ImageIcon(url);
			broi = new JButton(roiIcon);
			
			url = this.getClass().getResource("/"+imageResPA);
			ImageIcon img = new ImageIcon(url);
			if (img!=null)   bpa = new JButton(img);

			url = this.getClass().getResource("/"+imageResHelp);
			ImageIcon img2 = new ImageIcon(url);
			if (img2!=null)   bhelp = new JButton(img2);

			url = this.getClass().getResource("/"+imageResAbout);
			ImageIcon img3 = new ImageIcon(url);
			if (img3!=null)   babout = new JButton(img3);

			url = this.getClass().getResource("/"+imageResExit);
			ImageIcon img4 = new ImageIcon(url);
			if (img4!=null)   bexit = new JButton(img4);

			loaded = true;

		}catch(Exception e)
		{
			//e.printStackTrace();
			loaded = false;
			System.out.println("Load icon images from relative path..");
		}

		if (!loaded)
		{
			//System.out.println("Try to load application icon images from relative path.");
			btt = new JButton(new ImageIcon(imageResTT)); //opentt
			bcv = new JButton(new ImageIcon(imageResCV)); //open
			broi = new JButton(new ImageIcon(imageResROI)); //ROI
			bpa = new JButton(new ImageIcon(imageResPA)); //preference
			bhelp = new JButton(new ImageIcon(imageResHelp)); //help
			babout = new JButton(new ImageIcon(imageResAbout)); //about
			bexit = new JButton(new ImageIcon(imageResExit)); //about
		}

		btt.setToolTipText(LOADST);
		bcv.setToolTipText(LOADCVST);
		broi.setToolTipText(LOADROIST);
		bpa.setToolTipText("Set Parameters");
		bhelp.setToolTipText("Help");
		babout.setToolTipText("About");
		bexit.setToolTipText("Exit");


		toolBar.add(btt);
		toolBar.add(bcv);
		toolBar.add(broi);
		toolBar.add(bpa);
		toolBar.add(bhelp);
		toolBar.add(babout);
		toolBar.add(bexit);


		btt.addActionListener(this);
		bcv.addActionListener(this);
		broi.addActionListener(this);
		bpa.addActionListener(this);
		bhelp.addActionListener(this);
		babout.addActionListener(this);
		bexit.addActionListener(this);
		
		frame.add(toolBar, BorderLayout.NORTH);
	}


	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();
		Object source = e.getSource();
		if(command.equals(LOADST) || source == btt)
			loadImagesPerformed();
		else if(command.equals(LOADCVST) || source == bcv)
			loadCVImagesPerformed();
		else if(command.equals(LOADROIST) || source == broi)
			loadROIImagesPerformed();
		else if(command.equals(EXAMPLE1))
			loadExamples(1);
		else if(command.equals(EXAMPLE2))
			loadExamples(2);
		else if(command.equals("Save"))
			saveWorkspace();
		
		else if(command.equals("Exit") || source == bexit)
			System.exit(0);

		/*else if(command.equals("Feature Extractor Tuning") || source == bpa)
			frame.setPane(1);
		else if(command.equals("Feature Selector Tuning") || source == bpa)
			frame.setPane(1);
		else if(command.equals("Classifier Tuning") || source == bpa)
			frame.setPane(1);*/

		else if(command.equals("Metal LF"))
			setLF("Metal");
		else if(command.equals("System LF"))
			setLF("System");
		else if(command.equals("Nimbus LF"))
			setLF("Nimbus");
		else if(command.equals("Help Contents") || source == bhelp)
			displayHelp();
		else if(command.equals("About IANO (Image Annotation Tool)") || source == babout)
			displayAbout();
		else if(command.equals(NEWWZ))
			frame.reset();
	}

	private void setLF(String lnfName)
	{
		//can support other LF (such as motif) if I want 
		String lookAndFeel = null;
		try{

			if (lnfName.equals("Metal")) {
				lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
				// lookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel";
			}
			else if (lnfName.equals("System")) {
				lookAndFeel = UIManager.getSystemLookAndFeelClassName();
			}
			else if (lnfName.equals("Nimbus")) {
		       	  	for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		   		        if ("Nimbus".equals(info.getName())) {
		   		            lookAndFeel = info.getClassName();
		   		            break;
		   		        }    
		       	  	}
			}
			UIManager.setLookAndFeel(lookAndFeel);
			SwingUtilities.updateComponentTreeUI(frame);
			frame.pack();
		}catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			// handle exception
			e.printStackTrace();
		}
		catch (InstantiationException e) {
			// handle exception
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			// handle exception
			e.printStackTrace();
		}

	}

	protected void loadImagesPerformed()
	{
		//set the mode to Training/Testing, and pop up a dialog to load images
		//Annotator.output = Annotator.OUTPUT_CHOICES[0];
		//AnnLoadImageDialog loadDialog = new AnnLoadImageDialog(frame, controlPanel, Annotator.OUTPUT_CHOICES[0]);
	}

	protected void loadCVImagesPerformed()
	{
		//set the mode of one of the cross validation modes
		//Annotator.output = Annotator.OUTPUT_CHOICES[1];
		//AnnLoadImageDialog loadDialog = new AnnLoadImageDialog(frame, controlPanel, Annotator.OUTPUT_CHOICES[1]);
	}	

	protected void loadROIImagesPerformed()
	{
		//load roi training images, load roi tag file (similar as loadCV)--display?
		//load the image(s) to be annotated and display!
		//result of ROI annotation should show on the image, how?
		//additional parameters: jumping gap of moving window for annotation? 
		//Annotator.output = Annotator.OUTPUT_CHOICES[2];
		//AnnLoadImageDialog loadDialog = new AnnLoadImageDialog(frame, controlPanel, Annotator.OUTPUT_CHOICES[2]);
	}
	
	//load the example
	protected void loadExamples(int exNo)
	{
		//AnnExampleLoader loader = new AnnExampleLoader();
		//loader.load(exNo);
	}	
	
	//save the parameters and results
	protected void saveWorkspace()
	{
		//AnnSaver saver = new AnnSaver();
		//saver.save();
	}

	private JMenu buildLFMenu()
	{
		JMenu buildMenu = new JMenu("Look and Feel");
		buildMenu.setMnemonic('L');
		JMenuItem metalLFItem = new JMenuItem("Metal LF");
		metalLFItem.setMnemonic('M');
		JMenuItem systemLFItem = new JMenuItem("System LF");
		systemLFItem.setMnemonic('S');
		JMenuItem nimbusLFItem = new JMenuItem("Nimbus LF");
		nimbusLFItem.setMnemonic('N');
		buildMenu.add(metalLFItem);
		buildMenu.add(systemLFItem);
		buildMenu.add(nimbusLFItem);

		metalLFItem.addActionListener(this);
		systemLFItem.addActionListener(this);
		nimbusLFItem.addActionListener(this);
		
		return buildMenu;
	}

	private JMenu buildComponentMenu()
	{
		JMenu componentMenu = new JMenu("Components");
		componentMenu.setMnemonic('C');
		JMenuItem addExItem = new JMenuItem("Add a feature extractor");
		addExItem.setMnemonic('e');
		JMenuItem addEsItem = new JMenuItem("Add a feature selector");
		addExItem.setMnemonic('s');
		JMenuItem addClItem = new JMenuItem("Add a classifier");
		addClItem.setMnemonic('c');

		componentMenu.add(addExItem);
		componentMenu.add(addEsItem);
		componentMenu.add(addClItem);

		//functionality is not implemented yet
		//dynamic adding feature extractor etc involves invoking editor, compiler or just add
		//an existing class file?
		addExItem.setEnabled(false);
		addEsItem.setEnabled(false);
		addClItem.setEnabled(false);

		return componentMenu;
	}


	public void displayAbout()
	{
		String aboutString =  "IANO (Image Annotation Tool)\n\n" +
		"Version: 1.0.0\n\n" +
		"Jie Zhou, Hanchuan Peng and other contributors\n" +
		"2008 - 2009.  All rights reserved.\n\n" +
		"This tool uses software below:\n" +
		"mRMR -- \thttp://research.janelia.org/peng/proj/mRMR/ \n" +
		"ImageJ -- \thttp://rsbweb.nih.gov/ij \n" +
		"LibSVM -- \thttp://www.csie.ntu.edu.tw/~cjlin/libsvm/ \n" +
		"Jama -- \thttp://math.nist.gov/javanumerics/jama/ \n\n" +
		"Weka -- \thttp://www.cs.waikato.ac.nz/ml/weka/ \n\n";
//		"Reference:\n" +
//	"Jie Zhou and Hanchuan Peng, \"Automatic recognition and annotation of gene expression \n"+
//		   "patterns of fly embryos,\" Bioinformatics, 23(5):589-596, 2007.";

		javax.swing.JOptionPane.showMessageDialog(null,aboutString);

	}

	public void displayHelp()
	{
		String helpString =  "IANO (Image Annotation Tool): \n" +
		"Annotation tool for multi-dimensional biological images.\n\n" +
		"-- It supports three modes: training/testing,  cross-validation and RegionOfInterest.\n\n" + 
		
/*
		"-- 3D Feature Extractors: For a 3Dimage of size nx*ny*nz:\n" + 
		" Number of features after the partial frames approach is (nx*ny)\n"+
		" Number of features after the light approach is (nx*ny + nx*nz + ny*nz)\n"+
		" Number of features after the full approach is (nx*ny*nz)\n\n"+
*/
		"More help contents to be added.";

		javax.swing.JOptionPane.showMessageDialog(null,helpString);

	}

}
