package annotool;

import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Not used anymore (TODO: check to make sure)
 *
 */
public class AnnCompareVisualPanel extends JPanel {
	
	JTabbedPane parentPane = null;
	int index; //the index of this panel in the parent tabbed pane
	String[] methods;
	float [] rates;
	int numOfMethods;
	
	public AnnCompareVisualPanel(JTabbedPane tabs, int ind)
	{
	   	parentPane = tabs;
	   	index = ind;
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		int wid = getSize().width;
		int height = getSize().height;
		g.clearRect(0, 0, wid, height);

		//show a final recognition rate, error rate
		java.awt.Font font = new java.awt.Font("Kaufmann", java.awt.Font.BOLD, 15);
		g.setFont(font);
		String s = "Recognition rates of all classifiers:";
		int textHeight = g.getFontMetrics().getHeight() + 10;
		g.drawString(s, 0, textHeight);
		
		int maxbarheight = (int) ((height - textHeight)*0.75);
		int maxbarwidth = (int) (wid/numOfMethods*0.75);
		
		int i ,c;
        int barw = maxbarwidth - 50;

        c = 0; //the count for number of bars
		for(i=0; i< rates.length; i++)
		{
		 //avoid the comparison option itself in the method selection	
		 if(!methods[i].startsWith("Compare"))
		 {
 		  c++;
		  //One bar per method. bar's height is proportional to rate.
		  int barh = (int) (rates[i] * maxbarheight);
	      //draw label
		  g.setColor(java.awt.Color.BLACK);
		  g.drawString(String.valueOf(methods[i]), maxbarwidth*c+10, height-barh-5-textHeight);
		  g.drawString(String.valueOf(rates[i]*100)+'%', maxbarwidth*c+10, height-barh-5);

		  //draw bar
		  g.setColor(java.awt.Color.GREEN);
		  g.fillRect(maxbarwidth*c+5, height-barh, barw, barh);
		 } 
		}
		  
	}
	
	//need information on correct and wrong of each category - a new class Result!
	public void showResult(String[] methods, float[] rates, int count)
	{
		this.rates = rates;
		this.methods = methods;
	   	this.numOfMethods = count;
	
		repaint();
		parentPane.setEnabledAt(index,true);
	    parentPane.setSelectedIndex(index);
	}
	
	public void hideResult()
	{
		   parentPane.setEnabledAt(index,false);
	}

}
