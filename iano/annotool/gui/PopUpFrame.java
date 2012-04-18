package annotool.gui;

import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import annotool.io.ChainModel;
import annotool.io.DataInput;

/*
 * Can bring other common features of expert frame and auto comp frame here
 */
public class PopUpFrame extends JFrame {
	//To keep track of model for each genetic line
	ChainModel[] chainModels = null;
	protected boolean applyModelFired = false;
	
	protected DataInput trainingProblem = null;
	protected DataInput testingProblem = null;
	
	public PopUpFrame(String arg0, DataInput trainingProblem, DataInput testingProblem) {
		super(arg0);
		this.trainingProblem = trainingProblem;
		this.testingProblem = testingProblem;
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
