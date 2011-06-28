package annotool.gui;

import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import annotool.Annotation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class ResultPanel extends JPanel
{
	//Data members
	int tabIndex; //the index of this panel in the parent tabbed pane
	float recogRate; //Recognition rate
	int[] targets;
	int maxTestingSample;
	Annotation[] results;
	ArrayList<Integer> labels = new ArrayList<Integer>();
	Integer[][] confusionMatrix;
	int numClasses;
	
	//For statistical purpose
	double bestRate = 0.0;
	double worstRate = 1.0;
	int bestIndex = -1;
	int worstIndex = -1;
	StringBuffer bestLabels = new StringBuffer();
	StringBuffer worstLabels = new StringBuffer();
	
	//Components
	JTabbedPane parentPane = null;
	JLabel lbHorizontal, lbVertical, lbTitle;
	JTable table = null;
	JScrollPane scrollPane = null;	
	JPanel pnlMatrix;
	ChartPanel pnlChart;
	
	DecimalFormat df = new DecimalFormat("0.00%");
	
	public ResultPanel(JTabbedPane parentPane)
	{
	   	this.parentPane = parentPane;
	   	this.tabIndex = parentPane.getTabCount();
	   	
	   	this.setLayout(new GridLayout(2, 1));
	}
	public void showResult(float recogRate, int[] targets, Annotation[] annotations)
	{		
		this.recogRate = recogRate;
		this.targets = targets;
		this.results = annotations;
		
		this.buildLabels();
		
		//Set the total number of classes
		numClasses = labels.size();
		
		//Confusion matrix
		showMatrix();
		
		//Chart
		showChart();
		
		//Descriptions
		buildDescription();		
		
		revalidate();
		repaint();
	}
	
	/* 
	 * Builds the matrix and displays it 	
	 */
	private void showMatrix()
	{
		buildMatrix();
		
		//Create the header array to pass to JTable constructor
		String[] header = new String[numClasses + 1];
		header[0] = "";	//Empty first cell in header
		for (int i = 0; i < numClasses; i++) 
		{ 
			header[i + 1] = labels.get(i).toString(); 
		}
		
		table = new JTable(confusionMatrix, header){				//Set table cells to non-editable
			 public boolean isCellEditable(int rowIndex, int columnIndex) {
			       return false;
			   }
			};
		table.getTableHeader().setReorderingAllowed(false);
		scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		
		//Horizontal and vertical labels
		lbHorizontal = new JLabel("Classified as");		
		lbHorizontal.setHorizontalAlignment(SwingConstants.CENTER);
		lbHorizontal.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		lbVertical = new JLabel("Actual class");
		lbVertical.setVerticalAlignment(SwingConstants.TOP);
		lbVertical.setBorder(BorderFactory.createEmptyBorder(30, 5, 5, 5));
		
		//Larger font for titles
		Font titleFont = new Font("Tahoma", Font.BOLD, 20);
		//Title label
		lbTitle = new JLabel("Confusion Matrix");
		lbTitle.setForeground(Color.BLACK);
		lbTitle.setFont(titleFont);
		lbTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lbTitle.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));		
		
		JPanel pnlTableContainer = new JPanel(new BorderLayout());
		pnlTableContainer.add(scrollPane, BorderLayout.CENTER);
		pnlTableContainer.add(lbHorizontal, BorderLayout.NORTH);
		pnlTableContainer.add(lbVertical, BorderLayout.WEST);
		
		//Container for the matrix
		pnlMatrix = new JPanel(new BorderLayout());	
		pnlMatrix.add(pnlTableContainer, BorderLayout.CENTER);
		//pnlMatrix.add(lbHorizontal, BorderLayout.NORTH);
		//pnlMatrix.add(lbVertical, BorderLayout.WEST);
		pnlMatrix.add(lbTitle, BorderLayout.NORTH);
		
		this.add(pnlMatrix);	
		
		parentPane.setEnabledAt(tabIndex,true);
	    parentPane.setSelectedIndex(tabIndex);
	}
	
	/*
	 * Displays statistics
	 */
	private void buildDescription()
	{
		Font descFont = new Font("Tahoma", Font.BOLD, 14);
		
		JPanel pnlDescription = new JPanel();
		pnlDescription.setLayout(new BoxLayout(pnlDescription, BoxLayout.PAGE_AXIS));
		pnlDescription.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		pnlDescription.setBackground(Color.LIGHT_GRAY);
		
		//Labels with description
		JLabel lbRecogRate = new JLabel("Recognition Rate: " + df.format(recogRate));
		lbRecogRate.setFont(descFont);
		
		JLabel lbBestRate = new JLabel("Most successful ("+ bestLabels +") : " + df.format(bestRate));
		lbBestRate.setFont(descFont);
		
		JLabel lbWorstRate = new JLabel("Least successful ("+ worstLabels +") : " + df.format(worstRate));
		lbWorstRate.setFont(descFont);
		

		pnlDescription.add(lbRecogRate);
		pnlDescription.add(lbBestRate);
		pnlDescription.add(lbWorstRate);
		
		//Add description panel to the container
		pnlMatrix.add(pnlDescription, BorderLayout.SOUTH);
	}
	/*
	 * Creates the confusion matrix.
	 * The matrix has one extra column at the start to display labels for each row
	 */
	private void buildMatrix()
	{
		//Create and initialize the confusion matrix to 0
		confusionMatrix = new Integer[numClasses][numClasses + 1]; 	//One extra column for row headers (i.e. the first column)
		for(int i = 0; i < numClasses; i++)
		{
			confusionMatrix[i][0] = labels.get(i); 					//Row header
			for(int j=1; j <= numClasses; j++)
				confusionMatrix[i][j] = 0;
		}
		
		int row = 0;
		int col = 0;
		for(int i = 0; i < results.length; i++)
		{
			col = labels.indexOf(results[i].anno);
			row = labels.indexOf(targets[i]);
			if(col > -1)												//Col can be -1 if annotation is 0
				confusionMatrix[row][col + 1]++;
		}
	}
	/*
	 * Creates and displays the bar chart
	 */
	private void showChart()
	{
		int[][] info = infoForClasses();
		
		// create the dataset...
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        //double value = 0.0;
        for(int i=0; i < numClasses; i++)
        {
        	//value = 100.0 * info[i][0] / info[i][1];
        	
        	//First series for correct percentage
        	//dataset.addValue(value, "Correct", String.valueOf(info[i][2]));
        	
        	//Rest for incorrectly identified ones
        	//dataset.addValue(100 - value, "Incorrect", String.valueOf(info[i][2]));
        	for(int j=0; j < numClasses; j++)
        	{
        		//if(i != j)
        		//{
        			//Actual class index = i (row), classified as index = j+1 (column) (first column is for label)
        			dataset.addValue(100.0*confusionMatrix[i][j + 1]/info[i][0], "Identified as " + info[j][2], String.valueOf(info[i][2]));
        		//}
        	}
        }
		
		JFreeChart chart = ChartFactory.createStackedBarChart3D("Annotation Result", "Class", "Rate (%)", dataset, PlotOrientation.VERTICAL, true, true, false);
		CategoryPlot plot = chart.getCategoryPlot();
		CategoryItemRenderer r = plot.getRenderer(); 
		r.setSeriesPaint(0, new Color(76, 182, 73)); 
		r.setSeriesPaint(1, new Color(223, 34, 39));
		
		pnlChart = new ChartPanel(chart);
		this.add(pnlChart);
	}
	
	public void showKFoldChart(float[] results)
	{	
		// create the dataset...
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        //Iterate over the fold results
        for(int i=0; i < results.length - 1; i++) {
        			dataset.addValue(results[i] * 100, "Fold", "Fold" +( i + 1));  //Rounding off the percentage value of rate to 2 decimal places
        }
		
		JFreeChart chart = ChartFactory.createBarChart3D("Fold Results", "Class", "Rate (%)", dataset, PlotOrientation.VERTICAL, false, false, false);
		CategoryPlot plot = chart.getCategoryPlot();
		CategoryItemRenderer r = plot.getRenderer(); 
		r.setSeriesPaint(0, new Color(67, 40, 119)); 
		//r.setSeriesPaint(1, new Color(223, 34, 39));
		
		ChartPanel pnlFoldChart = new ChartPanel(chart);
		this.add(pnlFoldChart);
		
		revalidate();
	}
	
	//calculate result statistics
	private int[][] infoForClasses()
	{
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
		  
		  //For statistical purpose
		  double rate = (double)correct/total;
		  
		  //If the new rate is the best/worst, reset the string buffer of labels
		  if(rate > bestRate)
			  bestLabels.delete(0, bestLabels.length());
		  if(rate < worstRate)
			  worstLabels.delete(0, worstLabels.length());			  
			  
		  if(rate >= bestRate)
		  {
			  bestRate = rate;
			  bestLabels.append(info[classIndex][2] + " ");
		  }
		  if(rate <= worstRate)
		  {
			  worstRate = rate;
			  worstLabels.append(info[classIndex][2] + " ");
		  }			  
		}	
		
		return info;
	}
	
	/*
	 * Builds the arraylist of unique labels from the targets
	 */
	private void buildLabels() {
		for(int i=0; i < targets.length; i++) {
			if(!labels.contains(targets[i]))
				labels.add(targets[i]);
		}
		Collections.sort(labels);
	}
}
