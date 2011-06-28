package annotool.gui;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import annotool.AnnSplashScreen;

import java.awt.Dimension;
import java.awt.Toolkit;

public class AnnotatorGUI extends JFrame
{
	LandingPanel pnlLanding;
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
	public void reset()
	{
		this.remove(pnlLanding);
		pnlLanding = new LandingPanel(this);
		this.add(pnlLanding);
		this.pack();
	}
	
	public void setNewWizardEnabled(boolean isEnabled) {
		menuBar.setNewWizardEnabled(isEnabled);
	}
}
