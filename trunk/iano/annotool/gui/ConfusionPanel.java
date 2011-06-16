package annotool.gui;

import javax.swing.*;
import annotool.Annotation;

public class ConfusionPanel extends JPanel
{
	JTabbedPane parentPane = null;
	int tabIndex; //the index of this panel in the parent tabbed pane
	
	JTable confusionTable = null;
	
	public ConfusionPanel(JTabbedPane parentPane, int tabIndex)
	{
	   	this.parentPane = parentPane;
	   	this.tabIndex = tabIndex;
	}
	public void showMatrix(int[][] testingTargets, Annotation[][] annotations)
	{
		
	}
	
}
