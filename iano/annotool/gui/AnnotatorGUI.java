package annotool.gui;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import annotool.AnnSplashScreen;
import annotool.Annotator;

import java.awt.Dimension;
import java.awt.Toolkit;

public class AnnotatorGUI extends JFrame
{
	AnnMenuBar menuBar;
	
	public AnnotatorGUI(String arg0)
	{
		super(arg0);
		
		//Build Menu and Toolbar
		menuBar = new AnnMenuBar(this);
		
		pnlLanding = new LandingPanel(this);
		this.add(pnlLanding);
		
		//Set the initial window position
		this.pack();
		Dimension dim =
			Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int)(dim.getWidth() - getWidth())/2;
		int y = (int)(dim.getHeight() - getHeight())/2;
		setLocation(x,y);
	}
	
	LandingPanel pnlLanding;

	public static void main(String[] args) 
	{		
		//Show splash screen
		final AnnSplashScreen splash = new AnnSplashScreen();
		splash.showSplashScreen();
		try 
		{
            Thread.currentThread().sleep(1000);
        } 
		catch(InterruptedException ex) 
		{
        }
        
        JFrame.setDefaultLookAndFeelDecorated(true);
        SwingUtilities.invokeLater(new Runnable() {
          public void run() 
          {
	          try 
	          {
	        	  for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) 
	        	  {
	    		      if ("Nimbus".equals(info.getName())) 
	    		      {
	    		          UIManager.setLookAndFeel(info.getClassName());
	    		          break;
	    		      }
	        	  }
	          } 
	          catch (Exception e) 
	          {
	        	  System.out.println("Substance L&F failed to initialize");
	          }
    		
	          AnnotatorGUI gui = new AnnotatorGUI("IANO -- Image Annotation Tool 1.0");
	          System.out.println("Hello IANO!");
    		
	          gui.pack();
	          gui.setVisible(true);
	          gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   //finish the program when closing the window.
          }
        });
	}
	public void reset() {
		//this.remove(pnlLanding);
		//pnlLanding = new LandingPanel(this);
		pnlLanding.displayChosenPanel(LandingPanel.MAIN);
		pnlLanding.getImageReadyPanel().reset();
		//this.add(pnlLanding);
		this.pack();
	}
	
	/**
	 * Shows annotation mode step of the wizard
	 */
	public void initAnnotationWizard() {
		pnlLanding.getImageReadyPanel().reset();
		pnlLanding.displayChosenPanel(LandingPanel.ANNOTYPES);
		this.pack();
	}
	/**
	 * Shows model selection step of the wizard
	 */
	public void initModelSelectWizard() {
		pnlLanding.getImageReadyPanel().reset();
		pnlLanding.displayChosenPanel(LandingPanel.MODESELECT);
		this.pack();
	}
	
	public void setMenuEnabled(boolean isEnabled) {
		menuBar.setMenuEnabled(isEnabled);
	}
	public void initTT() {
		//Load separate training and testing image sets
		Annotator.output = Annotator.TT;
		AnnLoadImageDialog loadDialog = new AnnLoadImageDialog(this, pnlLanding, Annotator.TT);
	}
	public void initTrainOnly() {
		Annotator.output = Annotator.TO;
		AnnLoadImageDialog loadDialog = new AnnLoadImageDialog(this, pnlLanding, Annotator.TO);
	}
	public void initAnnotate() {
		Annotator.output = Annotator.AN;
		AnnLoadImageDialog loadDialog = new AnnLoadImageDialog(this, pnlLanding, Annotator.AN);
	}
	public void initROI() {
		Annotator.output = Annotator.ROI;
		AnnLoadImageDialog loadDialog = new AnnLoadImageDialog(this, pnlLanding, Annotator.ROI);
	}
}
