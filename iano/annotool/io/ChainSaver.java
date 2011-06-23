package annotool.io;

import java.io.*;
import java.util.HashMap;

import annotool.classify.SavableClassifier;

public class ChainSaver {
	//Data members
	//Image set(s) image size?
	private String imageSize = null;
	private String mode = null;
	private HashMap<String, Float> results = null;
	
	private String extractorName = null;
	private HashMap<String, String> exParams = null;
	private String selectorName = null;
	private HashMap<String, String> selParams = null;
	private String classifierName = null;
	
	private int[] selectedIndices = null;
	
	private SavableClassifier classifier = null;

	//Constructor
	public ChainSaver() {
		results = new HashMap<String, Float>();
	}
	
	/*
	 * Method to write to file
	 */
	public void write(File file) {
        try {
        	BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        	
        	String newLine = System.getProperty("line.separator");
        	//Write comment section
        	writer.write("# Image Size: " + imageSize + newLine);
        	writer.write("# Mode: " + mode + newLine);
        	writer.write("# Labels (result) : ");
        	
        	for (String label : results.keySet()) {
        		writer.write(label + " (" + results.get(label) * 100 + " %) ");
        	}
        	writer.newLine();							//Also uses line.separator
        	
        	//Write feature extractor
        	writer.write("[Feature Extractor]" + newLine);
        	writer.write("Name " + extractorName + newLine);
        	for (String parameter : exParams.keySet()) {
        		writer.write(parameter + " " +exParams.get(parameter) + newLine);
        	} 
        	
        	//Write classifier
        	writer.write("[Classifier]" + newLine);
        	writer.write("Name " + classifierName + newLine);
        	
        	if(classifier != null) {
        		classifier.saveModel(classifier.getModel(), getUniqueName());//TODO
        	}
        	
        	writer.flush();
        	writer.close();
        }
        catch(IOException ex) {
        	System.out.println("Exception occured while writing file: " + file.getName());
        	ex.printStackTrace();
        }
	}
	private String getUniqueName() {//TODO
		return "";
	}
	//Getters and setters
	public String getImageSize() {
		return imageSize;
	}
	public void setImageSize(String imageSize) {
		this.imageSize = imageSize;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public HashMap<String, Float>  getResults() {
		return results;
	}
	public void setResults(HashMap<String, Float> results) {
		this.results = results;
	}
	
	public void addResult(String label, Float rate) {
		this.results.put(label, rate);
	}
	
	public String getExtractorName() {
		return extractorName;
	}

	public void setExtractorName(String extractorName) {
		this.extractorName = extractorName;
	}

	public HashMap<String, String> getExParams() {
		return exParams;
	}

	public void setExParams(HashMap<String, String> exParams) {
		this.exParams = exParams;
	}

	public String getSelectorName() {
		return selectorName;
	}

	public void setSelectorName(String selectorName) {
		this.selectorName = selectorName;
	}

	public HashMap<String, String> getSelParams() {
		return selParams;
	}

	public void setSelParams(HashMap<String, String> selParams) {
		this.selParams = selParams;
	}
	public String getClassifierName() {
		return classifierName;
	}

	public void setClassifierName(String classifierName) {
		this.classifierName = classifierName;
	}

	public int[] getSelectedIndices() {
		return selectedIndices;
	}

	public void setSelectedIndices(int[] selectedIndices) {
		this.selectedIndices = selectedIndices;
	}

	public SavableClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(SavableClassifier classifier) {
		this.classifier = classifier;
	}	
}
