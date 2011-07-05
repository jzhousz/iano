package annotool.gui;

import java.util.ArrayList;
import java.util.HashMap;

public class Chain {
	private ArrayList<Extractor> extractors = null;
	private String selector = null;
	private HashMap<String, String> selParams = null;
	private String classifier = null;
	private HashMap<String, String> classParams = null;
	
	public Chain() {
		extractors = new ArrayList<Extractor>();
		selParams = new HashMap<String, String>();
		classParams = new HashMap<String, String>();
	}
	
	public void addExtractor(Extractor ex) {
		extractors.add(ex);
	}
	
	public void addSelectorParam(String key, String value) {
		selParams.put(key, value);
	}
	public void addClassifierParam(String key, String value) {
		classParams.put(key, value);
	}
	
	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		for(int i = 0; i < extractors.size(); i++) {
			str.append("FE"+ (i+1) +": " + extractors.get(i).getName() + " ");
		}
		if(selector != null)
			str.append("; " + selector);
		if(classifier != null)
			str.append("; " + classifier);
		return str.toString();
	}

	/*
	 * Getters and Setters
	 */
	public ArrayList<Extractor> getExtractors() {
		return extractors;
	}

	public void setExtractors(ArrayList<Extractor> extractors) {
		this.extractors = extractors;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public HashMap<String, String> getSelParams() {
		return selParams;
	}

	public void setSelParams(HashMap<String, String> selParams) {
		this.selParams = selParams;
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
	
}
