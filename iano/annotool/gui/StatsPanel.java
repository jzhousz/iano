package annotool.gui;

import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.*;

import annotool.Annotation;

/**
 * Creates a panel of statistics based on annotation results
 */
public class StatsPanel extends JPanel {
	private HashMap<String, String> classNames = null;
	
	public StatsPanel(Annotation[][] annotations, HashMap<String, String> classNames, String[] modelLabels) {
		this.classNames = classNames;
		
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		//Build panel for each annotation label
		for(int i=0; i < annotations.length; i++)
			this.add(getStatsPanelForModel(annotations[i], modelLabels[i]));
	}
	
	/*
	 * Creates stats panel from annotations (from one model)
	 */
	private JPanel getStatsPanelForModel(Annotation[] annotations, String modelLabel) {
		//Initialize treemap(sorted) to store count for each class
		TreeMap<String, Integer> counts = new TreeMap<String, Integer>();
		for(String key : classNames.keySet())
			counts.put(key, 0);
		
		for(int i = 0; i < annotations.length; i++) {
			Integer count = counts.get(String.valueOf(annotations[i].anno));
			count++;
			counts.put(String.valueOf(annotations[i].anno), count);
		}

		JPanel pnlStatGroup = new JPanel();
		pnlStatGroup.setLayout(new BoxLayout(pnlStatGroup, BoxLayout.PAGE_AXIS));
		JLabel lbTitle = new JLabel("<html><b>Identified classes (" + modelLabel + "):<b></html>");
		this.add(lbTitle);
		
		for(String key : counts.keySet()) {
			JLabel lb = new JLabel(key +" (" + classNames.get(key) + ") = " + counts.get(key));//eg. 1(classA) = 10
			this.add(lb);
		}
		
		return pnlStatGroup;
	}
}
