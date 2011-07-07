package annotool.gui;

import java.io.File;
import annotool.io.ChainModel;

import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import annotool.AnnOutputPanel;

public class ModelSaver implements Runnable {
	JProgressBar bar = null;
	AnnOutputPanel pnlOutput = null;
	ChainModel[] chainModels = null;
	File file = null;
	JButton[] buttons = null;
	
	public ModelSaver(JProgressBar bar, AnnOutputPanel pnlOutput, JButton[] buttons, ChainModel[] chainModels, File file) {
		  this.bar = bar;
		  this.chainModels = chainModels;
		  this.pnlOutput = pnlOutput;
		  this.file = file;
		  this.buttons = buttons;
	}
	
	public void run() {
		//Disable buttons
		for(int i = 0; i < buttons.length; i++)
			buttons[i].setEnabled(false);
		
		//Iterate through the chain models and write a file for each label
		for(int i = 0; i < chainModels.length; i++) {
        	chainModels[i].write(file);
        	try {
        		Thread.sleep(100);
        	}
        	catch (InterruptedException ex) {
        		ex.printStackTrace();
        		pnlOutput.setOutput(ex.getMessage());
        		return;
        	}
        	setProgress((i + 1)*100/chainModels.length);
        }
        pnlOutput.setOutput("Save complete. Dump file base path: " + file.getPath());
        
        //Enable buttons
		for(int i = 0; i < buttons.length; i++)
			buttons[i].setEnabled(true);
	}
	private void setProgress(final int currentProgress)
	{
		if (bar!=null) 
	        SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	            	bar.setValue(currentProgress);
	            }
	        });
	}
}
