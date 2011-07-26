package annotool.gui;

import java.awt.event.WindowEvent;
import javax.swing.JFrame;

import annotool.io.ChainModel;

/*
 * Can bring other common features of expert frame and auto comp frame here
 */
public class PopUpFrame extends JFrame {
	//To keep track of model for each genetic line
	ChainModel[] chainModels = null;
	protected boolean applyModelFired = false;
	
	public PopUpFrame(String arg0) {
		super(arg0);
	}
	
	//Close this window programmatically
	public void pullThePlug() {
        WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
        java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
	}

	public boolean isApplyModelFired() {
		return applyModelFired;
	}
	public ChainModel[] getChainModels() {
		return chainModels;
	}

	public void setChainModels(ChainModel[] chainModels) {
		this.chainModels = chainModels;
	}
}
