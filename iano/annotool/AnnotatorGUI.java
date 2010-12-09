package annotool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

import annotool.io.LabelReader;

/**
 * 
 * Image Annotation Tool  with Swing Interface
 * 
 * 2008
 * 
 * @author jzhou
 * Northern Illinois University
 * 
 */

public class AnnotatorGUI extends JFrame {

	String infoResourceImage = "images/ContextualHelp16.gif";
	JTabbedPane screen = new JTabbedPane();
	AnnControlPanel controlPanel = new AnnControlPanel(this);
	int tabNumber = 2; //initial number of tabs

	//parameter fields
	JPanel paraPanel = null;
	JPanel exPanel = new JPanel();
	JPanel slPanel = new JPanel();
	JPanel classifierPanel = new JPanel();
	JPanel roiPanel = null;
	public static JTextField levelField = new JTextField(Annotator.DEFAULT_WAVLEVEL, 20);
	public static JTextField numFeatureField = new JTextField(Annotator.DEFAULT_FEATURENUM, 20);
	public static JTextField svmParaField = new JTextField(Annotator.DEFAULT_SVM, 20);
	public static JTextField roiDimField = new JTextField(AnnROIAnnotator.DEFAULT_ROIDIM, 20);
	public static JTextField roiIncField = new JTextField(AnnROIAnnotator.DEFAULT_ROIINC, 20);
	
	public AnnotatorGUI(String arg0) {
		super(arg0);
		getContentPane().setBackground(java.awt.Color.white); 
		buildCombinedParaPanel();
		
		screen.setBackground(java.awt.Color.white);
		screen.addTab("Control Panel",null ,controlPanel);
		screen.addTab("Parameter Tuning", null, paraPanel, "Parameter Tuning");
		
		this.buildMenuAndToolBar();
		this.add(screen);

		this.pack(); //pack first, otherwise, calculation will be off.
		Dimension dim =
			Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int)(dim.getWidth() - getWidth())/2;
		int y = (int)(dim.getHeight() - getHeight())/2;
		setLocation(x,y);

	}

	/**
	 * GUI starts.
	 */
	public static void main(String[] args) {
		
		final AnnSplashScreen splash = new AnnSplashScreen();
		splash.showSplashScreen();
		try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException ex) {
        }
	
        
        JFrame.setDefaultLookAndFeelDecorated(true);
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            try {
              //UIManager.setLookAndFeel(new org.pushingpixels.substance.api.skin.SubstanceMistAquaLookAndFeel());
              //UIManager.setLookAndFeel(new org.pushingpixels.substance.api.skin.SubstanceRavenLookAndFeel());
        	  	for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
    		        if ("Nimbus".equals(info.getName())) {
    		            UIManager.setLookAndFeel(info.getClassName());
    		            break;
    		        }
        	  	}
            } catch (Exception e) {
              System.out.println("Substance L&F failed to initialize");
            }
    		AnnotatorGUI gui = new AnnotatorGUI("IANO -- Image Annotation Tool 1.0");
    		System.out.println("Hello IANO!");
    		//gui.setSize(1024,600);
    		gui.pack();
    		gui.setVisible(true);
    		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   //finish the program when closing the window.
          }
        });
        

        //Other L&Fs
		//try {
		  //  // Set System L&F
	        //UIManager.setLookAndFeel(
	         //   UIManager.getSystemLookAndFeelClassName());
		  /*     
		  	for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		  }*/
		/*} catch (Exception e) {
	    	System.err.println("Unsupported L&A"+e.getMessage());
	    } */
		
	}

   public void setPane(int i)
   {
		screen.setSelectedIndex(i);
   }
    
   private void buildMenuAndToolBar()
   {
	   new AnnMenuBar(this, controlPanel);
   }

	
	private void buildExPanel() 
	{
		JPanel pPanel = new JPanel();
		pPanel.setLayout(new java.awt.FlowLayout());
		pPanel.add(new JLabel("level (suggested value: 1 or 2)"));
		pPanel.add(levelField);
		pPanel.setBorder(new CompoundBorder(new TitledBorder(null,"wavelet", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 
		pPanel.setBackground(Color.white);
		exPanel.setLayout(new java.awt.FlowLayout(FlowLayout.LEFT));
		exPanel.add(pPanel);
	}

	private void  buildSlPanel() 
	{
		JPanel pPanel = new JPanel();
		pPanel.setLayout(new java.awt.FlowLayout());
		pPanel.add(new JLabel("number of selected features (suggested value: 5 - 20)"));
		pPanel.add(numFeatureField);
		pPanel.setBorder(new CompoundBorder(new TitledBorder(null,"mRMR", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 
		pPanel.setBackground(Color.white);
		slPanel.setLayout(new java.awt.FlowLayout(FlowLayout.LEFT));
		slPanel.add(pPanel);
	}

	private void buildClassifierPanel() 
	{
		final String svmHelp = 
			"Parameters for LibSVM (http://www.csie.ntu.edu.tw/~cjlin/libsvm/). \n" +
			"Example: -s 0 -c 10 -t 1 -g 1 -r 1 -d 3  Classify a binary data with polynomial kernel (u\'v+1)^3 and C = 10  \n" 
			+ 
			"Partial options: \n" +
			"\t-s svm_type : set type of SVM (default 0) \n" +
			"\t0 -- C-SVC  \n" +
			"\t1 -- nu-SVC \n" +
			"\t2 -- one-class SVM  \n" +
			"\t3 -- epsilon-SVR  \n" +
			"\t4 -- nu-SVR  \n" +
			"\t-t kernel_type : set type of kernel function (default 2) \n" +
			"\t0 -- linear: u\'*v  \n" +
			"\t1 -- polynomial: (gamma*u'*v + coef0)^degree \n" +
			"\t2 -- radial basis function: exp(-gamma*|u-v|^2)  \n" +
			"\t3 -- sigmoid: tanh(gamma*u'*v + coef0)  \n" 
		/*
			+
		
			"\t-d degree : set degree in kernel function (default 3)  \n" +
			"\t-g gamma : set gamma in kernel function (default 1/k)  \n" +
			"\t-r coef0 : set coef0 in kernel function (default 0)  \n" +
			"\t-c cost : set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default 1) \n"
		
			"\t-n nu : set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)  \n" +
			"\t-p epsilon : set the epsilon in loss function of epsilon-SVR (default 0.1)  \n" +
			"\t-m cachesize : set cache memory size in MB (default 100)  \n" +
			"\t-e epsilon : set tolerance of termination criterion (default 0.001)  \n" +
			"\t-h shrinking: whether to use the shrinking heuristics, 0 or 1 (default 1)  \n" +
			"\t-b probability_estimates: whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)  \n" +
			"\t-wi weight: set the parameter C of class i to weight*C, for C-SVC (default 1)  \n" +
			"" +
			"\tThe k in the -g option means the number of attributes in the input data.  \n" +
			"\toption -v randomly splits the data into n parts and calculates cross validation accuracy/mean squared error on them.  \n"
			*/
			;
		JPanel pPanel = new JPanel();
		pPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
		pPanel.add(new JLabel("parameters"));
		ImageIcon infoI = null;
		JButton infoB = null;
		try {
			java.net.URL url = this.getClass().getResource("/"+infoResourceImage);
			if (url != null)
			  infoI = new ImageIcon(url);
			else  
  			  infoI = new ImageIcon(infoResourceImage);
			infoB = new JButton(infoI);
		} catch (Exception ex) {
			//Image won't show up. 
			infoB = new JButton("info");
		}

		
		infoB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
	          JOptionPane.showMessageDialog(null,svmHelp);
            }
 	    });
		pPanel.add(svmParaField);
		pPanel.add(infoB);
		pPanel.setBackground(Color.white);

		//JScrollPane helpPanel = new JScrollPane(new JTextArea(svmHelp));
		//helpPanel.setBorder(new CompoundBorder(new TitledBorder(null,"SVM Help", 
		//		TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 
		pPanel.setBorder(new CompoundBorder(new TitledBorder(null,"SVM", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 

		
		classifierPanel.setLayout(new java.awt.FlowLayout(FlowLayout.LEFT));
		classifierPanel.add(pPanel);
		//classifierPanel.add(helpPanel);
	}

	private void buildROIPanel()
	{
		roiPanel = createVerticalPanel(false);  
		JPanel pPanel = new JPanel();
		pPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
		pPanel.add(new JLabel("Dimension of ROI"));
		pPanel.add(roiDimField);
		JPanel p2Panel = new JPanel();
		p2Panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
		p2Panel.add(new JLabel("Annotation Grid"));// (Sugguested value: 10. If set to 1, each pixel is annotated.)"));
		p2Panel.add(roiIncField);
		//pPanel.setBorder(new CompoundBorder(new TitledBorder(null,"??", 
			//	TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 
		pPanel.setBackground(Color.white);
		p2Panel.setBackground(Color.white);
		roiPanel.add(pPanel);
		roiPanel.add(p2Panel);
	}

	private JPanel buildCombinedParaPanel()
	{
		buildExPanel();
		buildSlPanel();
		buildClassifierPanel();
		buildROIPanel();

		//set to a clean background
		controlPanel.setBackground(java.awt.Color.white);
		exPanel.setBackground(java.awt.Color.white);
		slPanel.setBackground(java.awt.Color.white);
		classifierPanel.setBackground(java.awt.Color.white);
		roiPanel.setBackground(java.awt.Color.white);

		paraPanel = new JPanel(); //createVerticalPanel(false);  
		paraPanel.setLayout(new GridLayout(4,1, 5, 5));

		exPanel.setBorder(new CompoundBorder(new TitledBorder(null,"Feature Extractor", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 
		slPanel.setBorder(new CompoundBorder(new TitledBorder(null,"Feature Selector", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 
		classifierPanel.setBorder(new CompoundBorder(new TitledBorder(null,"Classifier", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 
		roiPanel.setBorder(new CompoundBorder(new TitledBorder(null,"ROI", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 

		paraPanel.add(exPanel);
		paraPanel.add(slPanel);
		paraPanel.add(classifierPanel);
		paraPanel.add(roiPanel);

		return paraPanel;
	}
	
	//an utility method for building up vertical panels
	public static JPanel createVerticalPanel(boolean threeD) 
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setAlignmentY(TOP_ALIGNMENT);
		p.setAlignmentX(LEFT_ALIGNMENT);
		if(threeD) {
			Border loweredBorder = new CompoundBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED), 
					new EmptyBorder(5,5,5,5));
			p.setBorder(loweredBorder);
		}
		return p;
	}
	
	public AnnControlPanel getControlPanel()
	{
		return controlPanel;
	}


	//add a new panel to the GUI, displaying current result
	public void addResultPanel(String label, float rate, int[] testingtargets, Annotation[] results)
	{
		AnnVisualPanel resPanel = new AnnVisualPanel(screen, tabNumber++);
		screen.addTab(label, null, resPanel, "Result Visualization");
		resPanel.showResult(rate, testingtargets, results);
	}

	//add a new panel to the GUI, displaying results of different classifiers
	public void addCompareResultPanel(String[] methods, float[] rates, int count)
	{
		AnnCompareVisualPanel resPanel = new AnnCompareVisualPanel(screen, tabNumber++);
		screen.addTab("comparison", null, resPanel, "Result Visualization");
		resPanel.showResult(methods, rates, count);
	}

}
