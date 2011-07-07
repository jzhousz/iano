package annotool.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class ACResultPanel extends JPanel {
	int tabIndex; //the index of this panel in the parent tabbed pane
	JTabbedPane parentPane = null;
	ChartPanel pnlChart = null;
	
	public ACResultPanel(JTabbedPane parentPane)
	{
	   	this.parentPane = parentPane;
	   	this.tabIndex = parentPane.getTabCount();
	   	
	   	this.setLayout(new BorderLayout());
	   	this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	}
	/*
	 * Argument: float[][] rates: recognition rate for each label for each chain
	 */
	public void showChart(float[][] rates, ArrayList<String> labels)
	{
		// create the dataset...
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for(int i=0; i < rates.length; i++)//Each chain
        {
        	for(int j=0; j < rates[i].length; j++)//Each label
        	{
        		dataset.addValue(100.0*rates[i][j], labels.get(j), "Chain " + (i + 1));
        	}
        }
		
		JFreeChart chart = ChartFactory.createBarChart3D("Auto Comparison Result", "Chain", "Rate (%)", dataset, PlotOrientation.VERTICAL, true, true, false);
		
		CategoryPlot plot = chart.getCategoryPlot();
		ValueAxis axis = plot.getRangeAxis();
		axis.setAutoRange(false);
		axis.setRange(0, 100);
		//CategoryItemRenderer r = plot.getRenderer(); 
		//r.setSeriesPaint(0, new Color(76, 182, 73)); 
		//r.setSeriesPaint(1, new Color(223, 34, 39));
		
		pnlChart = new ChartPanel(chart);
		pnlChart.setBorder(BorderFactory.createEtchedBorder());
		this.add(pnlChart, BorderLayout.CENTER);
		
		parentPane.setEnabledAt(tabIndex,true);
	    parentPane.setSelectedIndex(tabIndex);
	}
}
