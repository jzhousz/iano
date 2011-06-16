package annotool.gui;

import javax.swing.*;
import annotool.Annotation;

public class ConfusionPanel extends JPanel
{
	JTabbedPane parentPane = null;
	int tabIndex; //the index of this panel in the parent tabbed pane
	
	public ConfusionPanel(JTabbedPane parentPane, int tabIndex)
	{
	   	this.parentPane = parentPane;
	   	this.tabIndex = tabIndex;
	}
	public void showMatrix(int[][] testingTargets, Annotation[][] annotations)
	{
		int rows = testingTargets.length;
		int cols = testingTargets[0].length;
		
		Object[] headers = new Object[cols];
		
		Integer[][] confusionMatrix = new Integer[rows][cols];
		
		for(int i = 0; i < rows; i++)
			for(int j=0; j < cols; j++)
			{
				if(testingTargets[i][j] == annotations[i][j].anno)
				{
					confusionMatrix[i][j]++;
				}
			}
		JTable table = new JTable(confusionMatrix, headers);
		this.add(table);
	}
	
}
