package annotool.gui.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import annotool.Annotation;
import annotool.io.ChainModel;

/**
 * This helper class provides methods to get html formatted strings to display in GUI.
 *  
 */
public class ModelHelper {
	/**
	 * Gets the HTML formatted string including the information from the passed in ArrayList of ChainModel.
	 * 
	 */
	public static String getModelInfo(ArrayList<ChainModel> chainModels) {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("<html>");
		
		String newLine = "<br/>";
		
		for(int i=0; i < chainModels.size(); i++) {
			//Add title for each model
			strBuffer.append("<b><font size=+1>Model: " + (i + 1) + "</font></b>" + newLine);
			strBuffer.append("<b>Annotation label: </b>" + chainModels.get(i).getLabel() + newLine);
			strBuffer.append("<b>Image Size: </b>" + chainModels.get(i).getImageSize() + newLine);
			
			//Extractors
			strBuffer.append("<b>Extractor(s): </b>" + newLine);
			for(Extractor ex : chainModels.get(i).getExtractors()) {
				strBuffer.append(ex.getName() + " : ");
				for(String key : ex.getParams().keySet())
					strBuffer.append(key + " = " + ex.getParams().get(key) + " ");
				strBuffer.append(newLine);
			}
			if(chainModels.get(i).getExtractors().size() < 1)
				strBuffer.append("None" + newLine);
			
			//Selectors
			strBuffer.append("<b>Selector(s): </b>" + newLine);		
			for(Selector sel : chainModels.get(i).getSelectors())
				strBuffer.append(sel.getName() + newLine);
			if(chainModels.get(i).getSelectors().size() < 1)
				strBuffer.append("None" + newLine);
			
			//Classifier
			strBuffer.append("<b>Classifier: </b>" + newLine);	
			strBuffer.append(chainModels.get(i).getClassifierName() + " : ");
			for(String key : chainModels.get(i).getClassParams().keySet())
				strBuffer.append(key + " = " + chainModels.get(i).getClassParams().get(key) + " ");
			
			strBuffer.append(newLine);
			strBuffer.append(newLine);
		}
		
		strBuffer.append("</html>");
		
		return strBuffer.toString();
	}
	/**
	 * Gets the HTML formatted string from class names.
	 */
	public static String getClassNamesInfo(HashMap<String, String> classNames) {
		TreeMap<String, String> sortedClassNames = new TreeMap<String, String>();
		sortedClassNames.putAll(classNames);
		
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("<html>");		
		String newLine = "<br/>";
		
		//strBuffer.append("<b>Classes: </b>" + newLine);
		for(String key : sortedClassNames.keySet())
			strBuffer.append(key + " : " + sortedClassNames.get(key) + newLine);
		
		strBuffer.append("</html>");
		return strBuffer.toString();
	}
	
	/**
	 * Returns a HTML formatted string containing simple annotation statistics
	 */
	public static String getStatsInfo(HashMap<String, String> classNames, Annotation[][] annotations, String[] modelLabels) {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("<html>");		
		String newLine = "<br/>";
		
		//strBuffer.append("<b>Identified Classes: </b>" + newLine);
		//Build panel for each annotation label
		for(int i=0; i < annotations.length; i++)
			strBuffer.append(getStatsInfoForModel(classNames, annotations[i], modelLabels[i]) + newLine);		
		
		strBuffer.append("</html>");
		return strBuffer.toString();
	}
	
	private static String getStatsInfoForModel(HashMap<String, String> classNames, Annotation[] annotations, String modelLabel) {
		StringBuffer strBuffer = new StringBuffer();
		String newLine = "<br/>";
		
		//Initialize treemap(sorted) to store count for each class
		TreeMap<String, Integer> counts = new TreeMap<String, Integer>();
		for(String key : classNames.keySet())
			counts.put(key, 0);
		
		for(int i = 0; i < annotations.length; i++) {
			Integer count = counts.get(String.valueOf(annotations[i].anno));
			count++;
			counts.put(String.valueOf(annotations[i].anno), count);
		}

		strBuffer.append("<b>" + modelLabel + ": </b>" + newLine);
		
		for(String key : counts.keySet())
			strBuffer.append(key +" (" + classNames.get(key) + ") = " + counts.get(key) + newLine);//eg. 1(classA) = 10
		
		return strBuffer.toString();
	}
}
