package annotool.gui;

import java.awt.Color;

import javax.swing.*;

import annotool.Annotation;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Not used anymore (TODO: check to make sure)
 *
 */
public class VisualizationPanel extends JPanel
{
	JTabbedPane parentPane = null;
	int tabIndex; //the index of this panel in the parent tabbed pane
	float recogRate; //Recognition rate
	
	int[] targets;
	int maxTestingSample;
	Annotation[] results;
	java.util.ArrayList<Integer> labels;
	
	public VisualizationPanel(JTabbedPane parentPane, int tabIndex)
	{
	   	this.parentPane = parentPane;
	   	this.tabIndex = tabIndex;
	}
	
	public void showResult(float recogRate, int[] targets, Annotation[] results)
	{
		this.recogRate = recogRate;
		this.targets = targets;
		this.results = results;
		
		int[][] info = infoForClasses();
		int numOfClasses = info.length;
		
		// create the dataset...
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        double value = 0.0;
        for(int i=0; i < numOfClasses; i++)
        {
        	value = 100.0 * info[i][0] / info[i][1];
        	dataset.addValue(value, "Correct", String.valueOf(info[i][2]));
        	dataset.addValue(100 - value, "Incorrect", String.valueOf(info[i][2]));
        }
		
		// create a chart...
		//JFreeChart chart = ChartFactory.createBarChart3D("Annotation Result", "Class", "Rate", dataset, PlotOrientation.VERTICAL, false, false, false);
		JFreeChart chart = ChartFactory.createStackedBarChart3D("Annotation Result", "Class", "Rate", dataset, PlotOrientation.VERTICAL, true, true, false);
		CategoryPlot plot = chart.getCategoryPlot();
		CategoryItemRenderer r = plot.getRenderer(); 
		r.setSeriesPaint(0, new Color(76, 182, 73)); 
		r.setSeriesPaint(1, new Color(223, 34, 39));
		
		ChartPanel chartPanel = new ChartPanel(chart);
		this.setLayout(new java.awt.GridLayout(1, 1));
		this.add(chartPanel);
	}
	
	//calculate result statistics
	private int[][] infoForClasses()
	{
		//get the  target labels
        labels = new java.util.ArrayList<Integer>();
        for(int j=0; j < targets.length; j++) 
        	if (!labels.contains(targets[j]))
        		labels.add(targets[j]);

		int numofClasses = labels.size();
		int[][] info = new int[numofClasses][3];
		
		this.maxTestingSample = 0;
		for(int classIndex = 0; classIndex < numofClasses; classIndex++)
		{
		  int correct = 0;
		  int total = 0;
		  for(int i = 0; i< targets.length; i++)
		  {
			if (targets[i] == ((Integer) labels.get(classIndex)).intValue())  //target label may not be 0, 1, 2
			{
				total ++;
				if(targets[i] == results[i].anno)
					correct ++;
			}
		  }
		  info[classIndex][0] = total;
		  info[classIndex][1] = correct;
		  info[classIndex][2] = ((Integer) labels.get(classIndex)).intValue();
		  if (total > maxTestingSample) maxTestingSample = total;
		}	
		
		return info;
	}
}
