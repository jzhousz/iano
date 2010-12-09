package annotool;

import javax.swing.*;

import java.awt.FontMetrics;
import java.awt.Graphics;

public class AnnVisualPanel extends JPanel {

	JTabbedPane parentPane = null;
	int index; //the index of this panel in the parent tabbed pane
	java.text.DecimalFormat df = new java.text.DecimalFormat("#.0");
	float recrate;
	//int numofClasses;
	int maxTestingSample;
	int[] targets;
	Annotation[] results;
	java.util.ArrayList labels;
	
	public AnnVisualPanel(JTabbedPane tabs, int ind)
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
		java.awt.FontMetrics fm = g.getFontMetrics();
		g.setFont(font);
		int fontHei = fm.getHeight();
		String s = "Recognition Rate:  " + df.format(recrate*100) + "%";
		int textHeight = g.getFontMetrics().getHeight() + 10;
		g.drawString(s, 0, textHeight);
		
		//show the statistics of each class: green is correct, black is wrong? bottom is correct? up is wrong?
		//how many bars are needed?
		int[][] info = infoForClasses();
		int numofClasses = info.length;
		int maxbarheight = (int) ((height - textHeight)*0.75);
		int maxbarwidth = (int) (wid/numofClasses*0.75);
		
		int i;
        int barw = maxbarwidth - 50;

		for(i=0; i< numofClasses; i++)
		{
		  //One bar per class. bar's height is proportional to total sample in the class.
		  int barh = (int) (info[i][0]/(float) maxTestingSample * maxbarheight);
		  // upper portion is for the incorrectly predicted samples
	      int upbarh = (int) (((info[i][0]-info[i][1])/(float) info[i][0])*(float) barh);
		  //System.out.println(i+":total"+info[i][0]+":correct:"+info[i][1]+":barh:" + barh + " upbarh:"+upbarh + " barw:"+barw);

	      //draw label
		  g.setColor(java.awt.Color.BLACK);
		  g.drawString(String.valueOf(info[i][2]), maxbarwidth*i+10, height-barh-5);
		  //upper bar. incorrect
		  g.setColor(java.awt.Color.RED);
		  g.fillRect(maxbarwidth*i+5, height-barh, barw, upbarh);
		  //lower bar. correct
		  g.setColor(java.awt.Color.GREEN);
		  g.fillRect(maxbarwidth*i+5, height-barh+upbarh, barw, barh-upbarh);
		}
		  
		//draw legend
 	    //g.setColor(java.awt.Color.BLACK);
		//g.drawRect(maxbarwidth*i+5, height-maxbarheight/2, maxbarwidth+fm.stringWidth("incorrect"),70);
	    g.setColor(java.awt.Color.GREEN);
		g.fillRect(maxbarwidth*i+10, height-maxbarheight/2+10, barw/2, 20);
		g.drawString("correct",maxbarwidth*i+barw/2+14, height-maxbarheight/2 +10+fontHei);
	    g.setColor(java.awt.Color.RED);
		g.fillRect(maxbarwidth*i+10, height-maxbarheight/2+40, barw/2, 20);
		g.drawString("incorrect",maxbarwidth*i+barw/2+14, height-maxbarheight/2+40+fontHei);
		
		  
		
	}
	
	//need information on correct and wrong of each category - a new class Result!
	public void showResult(float recograte, int[] targets, Annotation[] results)
	{
		this.recrate = recograte;
		//this.numofClasses = numofclasses;
		this.targets = targets;
		this.results = results;
		repaint();
		parentPane.setEnabledAt(index,true);
	    parentPane.setSelectedIndex(index);
	}
	
	public void hideResult()
	{
		   parentPane.setEnabledAt(index,false);
	}
	
	
	//calculate result statistics
	private int[][] infoForClasses()
	{
		//get the  target labels
        labels = new java.util.ArrayList();
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
