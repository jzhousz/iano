package annotool.gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import java.awt.BorderLayout;
import java.awt.event.*;

/**
 * Panel with scrollable text area and clear button.
 * Used for displaying messages.
 * Public method setOutput() is used for adding text.
 *
 */
public class AnnOutputPanel extends JPanel implements ActionListener {

	JTextArea output = new JTextArea(6,30);
	JScrollPane pane = new JScrollPane(output);
	JButton clearOutputB = new JButton("Clear Output");
	Object workingFrame = null; //the parent JFrame or JPanel where the outputpanel resides in, such as ExpertFrame, AutoCompFrame, ImageReadyPanel

	public AnnOutputPanel(Object theContainer)
	{
		this.workingFrame = theContainer;
		this.setLayout(new BorderLayout());
		this.add(pane, BorderLayout.CENTER);
		JPanel tmpPanel = new JPanel();
		tmpPanel.add(clearOutputB);
		//tmpPanel.setBackground(java.awt.Color.white);
		this.add(tmpPanel, BorderLayout.SOUTH);
		this.setBorder(new CompoundBorder(new TitledBorder(null,"output", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 
		output.setEditable(false);
		//this.setBackground(java.awt.Color.white);
		
		clearOutputB.addActionListener(this);
	}

	public void setOutput(String arg)
	{
		if(!(output.getText().equals("")))
		  output.setText(output.getText()+'\n'+arg);
		else
		  output.setText(arg);
		
		output.setCaretPosition(output.getText().length());
		
        /*
         * set the last row visible, to be added.
		int w = pane.getSize().width;
        int h = pane.getSize().height;
        int y = this.getFont().getHeight();
        if (y < 0) y = 0;
        Rectangle rect = new Rectangle(0, y, W, H);
        circles.addElement(rect);
        drawingPane.scrollRectToVisible(rect);
        */
	}
	public void clearOutput()
	{
		  output.setText("");
		  if (workingFrame instanceof ExpertFrame) //the single selection has progress bar
		      ((ExpertFrame)workingFrame).getBar().setValue(0); //Set the bar back to 0%
	}
	
	public void actionPerformed(ActionEvent e)
	{
		clearOutput();
	}
	
	public void clearEnabled(boolean flag)
	{
		clearOutputB.setEnabled(flag);
	}
}
