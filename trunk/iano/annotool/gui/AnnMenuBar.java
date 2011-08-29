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
	
	final String MODELSELECT = "Model Selection";
	final String TRAINONLY = "Training Only";
	
	final String ANNO = "Annotate";
	final String IMGANNO = "Image Annotation";
	final String ROIANNO = "Region of Interest Annotation";
	
	final String LOADEX  = "Load Examples..";
	final String EXAMPLE1  = "Fruitfly Brain Neuronal Bundles (2D) ";
	final String EXAMPLE2  = "Fruitfly Brain Neuronal Bundles (3D) ";
	
	final String imageResNewWiz = "images/icons/bullet_green.png";
	final String imageResHelp = "images/icons/help.png";
	final String imageResAbout = "images/icons/information.png";
	final String imageResExit ="images/icons/cross.png";
	
	final String aboutText = "About BI-CAT (Biological Image Classification and Annotation Tool)";
	
	JButton bNewWiz, babout, bhelp, bexit; //icons on toolbar
	
	JMenuItem newWizardItem, itemMDSelect, itemTrainOnly;
	
	JMenu annoMenu;

	public AnnMenuBar(AnnotatorGUI frame)
	{
		this.frame = frame;
		
		//add tool bar
		buildToolBar();

		JMenuBar bar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("Home");
		fileMenu.setMnemonic('o');

		newWizardItem = new JMenuItem(NEWWZ);
		newWizardItem.addActionListener(this);
		
		itemMDSelect = new JMenuItem(MODELSELECT);
		itemTrainOnly = new JMenuItem(TRAINONLY);
		itemMDSelect.addActionListener(this);
		itemTrainOnly.addActionListener(this);
		
		annoMenu = new JMenu(ANNO);
		JMenuItem imgAnnoItem = new JMenuItem(IMGANNO);
		JMenuItem roiAnnoItem = new JMenuItem(ROIANNO);
		annoMenu.add(imgAnnoItem);
		annoMenu.add(roiAnnoItem);
		imgAnnoItem.addActionListener(this);
		roiAnnoItem.addActionListener(this);

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
		saveItem.setEnabled(false);

		
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.setMnemonic('x');
		exitItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_X, ActionEvent.ALT_MASK));
		exitItem.addActionListener(this);
		//add them onto the right place.
		fileMenu.add(newWizardItem);
		
		fileMenu.add(itemMDSelect);
		fileMenu.add(itemTrainOnly);
		
		fileMenu.add(annoMenu);
		
		fileMenu.add(loadexMenu);
		fileMenu.add(saveItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		JMenuItem helpCItem = new JMenuItem("Help Contents");
		helpCItem.setMnemonic('e');
		helpCItem.addActionListener(this);
		JMenuItem aboutItem = new JMenuItem(aboutText);
		aboutItem.setMnemonic('A');
		aboutItem.addActionListener(this);
		helpMenu.add(helpCItem);
		helpMenu.add(aboutItem);

		bar.add(fileMenu);
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
			
			url = this.getClass().getResource("/"+imageResNewWiz);
			ImageIcon img1 = new ImageIcon(url);
			if (img1!=null)   bNewWiz = new JButton(img1);
			
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
			bNewWiz = new JButton(new ImageIcon(imageResNewWiz)); //New Wizard
			bhelp = new JButton(new ImageIcon(imageResHelp)); //help
			babout = new JButton(new ImageIcon(imageResAbout)); //about
			bexit = new JButton(new ImageIcon(imageResExit)); //about
		}

		bNewWiz.setToolTipText("New Wizard");
		bhelp.setToolTipText("Help");
		babout.setToolTipText("About");
		bexit.setToolTipText("Exit");


		toolBar.add(bNewWiz);
		toolBar.add(bhelp);
		toolBar.add(babout);
		toolBar.add(bexit);


		bNewWiz.addActionListener(this);
		bhelp.addActionListener(this);
		babout.addActionListener(this);
		bexit.addActionListener(this);
		
		frame.add(toolBar, BorderLayout.NORTH);
	}


	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();
		Object source = e.getSource();
		
		if(command.equals(MODELSELECT))
			frame.initModelSelectWizard();
		else if(command.equals(TRAINONLY))
			frame.initTrainOnly();
		else if(command.equals(IMGANNO))
			frame.initAnnotate();
		else if(command.equals(ROIANNO))
			frame.initROI();
		else if(command.equals(EXAMPLE1))
			loadExamples(1);
		else if(command.equals(EXAMPLE2))
			loadExamples(2);
		else if(command.equals("Save"))
			saveWorkspace();
		
		else if(command.equals("Exit") || source == bexit)
			System.exit(0);

		else if(command.equals("Metal LF"))
			setLF("Metal");
		else if(command.equals("System LF"))
			setLF("System");
		else if(command.equals("Nimbus LF"))
			setLF("Nimbus");
		else if(command.equals("Help Contents") || source == bhelp)
			displayHelp();
		else if(command.equals(aboutText) || source == babout)
			displayAbout();
		else if(command.equals(NEWWZ) || source == bNewWiz)
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
			frame.pnlLanding.getImageReadyPanel().updateLookAndFeelForOpenFrames();
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

	public void displayAbout()
	{
		String aboutString =  "BI-CAT (Biological Image Classification and Annotation Tool)\n\n" +
		"Version: 1.0.0\n\n" +
		"Jie Zhou, Hanchuan Peng, Santosh Lamichhane and other contributors\n" +
		"2009 - 2011.  All rights reserved.\n\n" +
		"This tool uses software below:\n" +
		"mRMR -- \thttp://research.janelia.org/peng/proj/mRMR/ \n" +
		"ImageJ -- \thttp://rsbweb.nih.gov/ij \n" +
		"LibSVM -- \thttp://www.csie.ntu.edu.tw/~cjlin/libsvm/ \n" +
		"Jama -- \thttp://math.nist.gov/javanumerics/jama/ \n\n" +
		"Weka -- \thttp://www.cs.waikato.ac.nz/ml/weka/ \n\n" +
		"jFreeChart -- \thttp://www.jfree.org/jfreechart/ \n" +
		"iText -- \thttp://itextpdf.com/ \n\n" +
		"Toolbar icons -- \thttp://www.famfamfam.com/lab/icons/silk/\n";
//		"Reference:\n" +
//	"Jie Zhou and Hanchuan Peng, \"Automatic recognition and annotation of gene expression \n"+
//		   "patterns of fly embryos,\" Bioinformatics, 23(5):589-596, 2007.";

		javax.swing.JOptionPane.showMessageDialog(null,aboutString);

	}

	public void displayHelp() {
		String helpString =  "BI-CAT (Biological Image Classification and Annotation Tool): \n" +
		"Annotation tool for multi-dimensional biological images.\n\n" +
		"It supports Training/Testing and Cross Validation modes for selecting model. \n" + 
		"Training only mode can also be used.\n\n" +
		"For each of these methods, there are two modes - Simple mode and Comparision mode.\n\n" +
		"In Simple mode, the user can select a single algorithm for feature extractor, feature selector and classifier.\n" + 
		"Then the selected algorithms will be applied and a model created. \n\n" + 
		"In Comparison mode, the user can create multiple chains of algorithms and run the selected chains. \n" +
		"The best chain identified for a given problem will be selected.\n\n" +
		"For image classification and annotation, there are two modes - Image Annotation and ROI(Region of Interest) Annotation.\n\n" +
		"Both of these modes can be used by loading saved model or by using the model in memory.\n\n";
		

		javax.swing.JOptionPane.showMessageDialog(null, helpString);
	}
	/*
	 * Enables/disables the new wizard menu item
	 */
	public void setMenuEnabled(boolean isEnabled) {
		newWizardItem.setEnabled(isEnabled);
		bNewWiz.setEnabled(isEnabled);
		itemMDSelect.setEnabled(isEnabled); 
		itemTrainOnly.setEnabled(isEnabled);		
		annoMenu.setEnabled(isEnabled);
	}

}
