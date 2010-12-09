package annotool;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.*;

public class AnnSplashScreen extends JWindow {

	final String imageResSplash = "images/screen.jpg";

	public AnnSplashScreen() {
		ImageIcon image = null;
		JLabel label = new JLabel(image);
		try {
			java.net.URL url = this.getClass().getResource("/"+imageResSplash);
			if (url != null)
			  image = new ImageIcon(url);
			else  
  			  image = new ImageIcon(imageResSplash);
			label = new JLabel(image);
		} catch (Exception ex) {
			label = new JLabel("unable to find splash screen image.");
		}
		getContentPane().add(label);
		pack();
		Dimension dim =
			Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int)(dim.getWidth() - getWidth())/2;
		int y = (int)(dim.getHeight() - getHeight())/2;
		setLocation(x,y);
	}

	public void showSplashScreen()
	{
		pack();
		setVisible(true);
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.currentThread().sleep(5000); 
					setVisible(false);
				} catch (InterruptedException ex) {
				}
			}
		}).start();
	}
}
