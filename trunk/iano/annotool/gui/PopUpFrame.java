package annotool.gui;

import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

import annotool.io.ChainModel;
import annotool.io.DataInput;
import java.awt.event.WindowListener;

/**
 * Super class for ExpertFrame(Simple mode) and AutoCompFrame(Auto comparison mode).
 * 
 */
public class PopUpFrame extends JFrame implements WindowListener {
	//To keep track of model for each genetic line
	ChainModel[] chainModels = null;
	protected boolean applyModelFired = false;
	
	protected DataInput trainingProblem = null;
	protected DataInput testingProblem = null;


	public PopUpFrame(String arg0, DataInput trainingProblem, DataInput testingProblem, String channel, boolean channelFlag) {
		super(arg0);
		if (channelFlag == false)
			channel = "";
		this.setTitle(arg0 + " - channel " + getChannelNameForDisplay(channel));
						
		this.trainingProblem = trainingProblem;
		this.testingProblem = testingProblem;
		
		//this.trainingProblem.setChannel(channel);
		//if(this.testingProblem != null)				//Testing problem can be null if it is training only or cv mode
			//this.testingProblem.setChannel(channel);
	}
	
	//Close this window programmatically
	public void pullThePlug() {
        WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
        java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
        System.out.println("Testing cpull the plug method...");
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
	
	private String getChannelNameForDisplay(String channel) {
		if(channel.equals("r"))
			return "red";
		if(channel.equals("g"))
			return "green";
		if(channel.equals("b"))
			return "blue";
		return "gray scale"; //What appears when there is no channel selected.
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		System.out.println("Testing closing method...");
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		System.out.println("Testing closed method...");
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

	
}
