package annotool.gui.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a single chain of algorithms.
 * It consists one classifier, 0 or more extractors and 0 or more selectors.
 * 
 * @author Santosh
 *
 */
public class Chain {
	private String name = null;
	private ArrayList<Extractor> extractors = null;
	private ArrayList<Selector> selectors = null;
	private String classifier = null;
	private String classifierClassName = null;
	private String classifierExternalPath = null;
	private HashMap<String, String> classParams = null;
	
	public Chain(String name) {
		this.name = name;
		
		extractors = new ArrayList<Extractor>();
		selectors = new ArrayList<Selector>();
		classParams = new HashMap<String, String>();
	}
	
	public void addExtractor(Extractor ex) {
		extractors.add(ex);
	}
	public void addSelector(Selector sel) {
		selectors.add(sel);
	}
	public void clearExtractors() {
		extractors.clear();
	}
	public void clearSelectors() {
		selectors.clear();
	}
	public void addClassifierParam(String key, String value) {
		classParams.put(key, value);
	}
	public boolean hasExtractors() {
		if (extractors.size() > 0)
			return true;
		return false;
	}
	public boolean hasSelectors() {
		if (selectors.size() > 0)
			return true;
		return false;
	}
	public boolean isComplete() {
		//Chain is complete when there is at least one classifier
		//It is valid even if there is no extractor or selector because they should default to 'None'
		if(classifier == null || classifier.equalsIgnoreCase("None"))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		for(int i = 0; i < extractors.size(); i++) {
			str.append("FE"+ (i+1) +": " + extractors.get(i).getName() + " ");
		}
		if(extractors.size() > 0)
			str.append("; ");
		else
			str.append("FE: NONE; ");
		
		for(int i = 0; i < selectors.size(); i++) {
			str.append("FS"+ (i+1) +": " + selectors.get(i).getName() + " ");
		}
		if(selectors.size() > 0)
			str.append("; ");
		else
			str.append("FS: NONE; ");
		
		if(classifier != null)
			str.append("Classifier: " + classifier);
		else
			str.append("Classifier: NONE");
		
		return str.toString();
	}	

	/*
	 * Getters and Setters
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public ArrayList<Extractor> getExtractors() {
		return extractors;
	}

	public void setExtractors(ArrayList<Extractor> extractors) {
		this.extractors = extractors;
	}
	
	public ArrayList<Selector> getSelectors() {
		return selectors;
	}

	public void setSelectors(ArrayList<Selector> selectors) {
		this.selectors = selectors;
	}

	public String getClassifier() {
		return classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	public HashMap<String, String> getClassParams() {
		return classParams;
	}

	public void setClassParams(HashMap<String, String> classParams) {
		this.classParams = classParams;
	}

	public String getClassifierClassName() {
		return classifierClassName;
	}

	public void setClassifierClassName(String classifierClassName) {
		this.classifierClassName = classifierClassName;
	}

	public String getClassifierExternalPath() {
		return classifierExternalPath;
	}

	public void setClassifierExternalPath(String classifierExternalPath) {
		this.classifierExternalPath = classifierExternalPath;
	}
	
}
