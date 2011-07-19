package annotool.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import annotool.Annotator;
import annotool.classify.Classifier;
import annotool.classify.SavableClassifier;
import annotool.gui.model.Extractor;
import annotool.gui.model.Selector;
import annotool.gui.model.Utils;

public class ChainModel {
	//Data members
	private String imageSet = null;
	private String testingSet = null; //Used if training/testing
	private String imageSize = null;
	private String mode = null;
	private String channel = null;
	private float result;
	private String label = null;
	private ArrayList<Extractor> extractors = null;
	private ArrayList<Selector> selectors = null;
	private String classifierName = null;
	private Classifier classifier = null;
	private HashMap<String, String> classParams = null;
	
	JProgressBar bar = null;
	
	public ChainModel() {
		extractors = new ArrayList<Extractor>();
		selectors = new ArrayList<Selector>();
	}
	
	/*
	 * Method to write to file
	 */
	public void write(File baseFile) {
		//Cross platform new line character
    	String newLine = System.getProperty("line.separator");
    	
    	String fileName = baseFile.getName();
    	if(fileName.endsWith("." + Utils.MODEL_EXT))
    		fileName = Utils.removeExtension(fileName);
    	
    	File file = new File(baseFile.getParent(), fileName + "_" + label + "." + Utils.MODEL_EXT);
        try {
    		//FileWriter baseFileWriter = new FileWriter(baseFile);
    		//baseFileWriter.write(file.getPath() + newLine + "EOF");
    		//baseFileWriter.flush();
    		
        	BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        	//Write comment section
        	writer.write("# Image Set: " + imageSet + newLine);
        	if(this.testingSet != null) {
        		writer.write("# Testing Set: " + testingSet + newLine);
        	}
        	writer.write("# Image Size: " + imageSize + newLine);
        	writer.write("# Mode: " + mode + newLine);
        	writer.write("# Channel: " + channel + newLine);
        	writer.write("# Result: " + result * 100 + "%" + newLine);
        	writer.write("# Label: " + label + newLine);
        	
        	writer.newLine();
        	
        	setProgress(5);
        	
        	//Write feature extractor
        	for(Extractor ex : extractors) {
	        	writer.write("[FEATURE_EXTRACTOR]" + newLine);
	        	writer.write("Name=" + ex.getName() + newLine);
	        	writer.write("[PARAMETER_START]" + newLine);
	        	for (String parameter : ex.getParams().keySet()) {
	        		writer.write(parameter + "=" +ex.getParams().get(parameter) + newLine);
	        	}
	        	writer.write("[PARAMETER_END]" + newLine);
        	}
        	
        	setProgress(15);
        	
        	//Write feature selector
        	for(Selector sel : selectors) {
        		writer.write("[FEATURE_SELECTOR]" + newLine);
        		writer.write("Name=" + sel.getName() + newLine);
        		int[] selectedIndices = sel.getSelectedIndices();
        		if(selectedIndices != null){
        			writer.write("[SELECTED_INDICES_START]" + newLine);
    	        	for(int i=0; i < selectedIndices.length; i++) {
    	        		writer.write(selectedIndices[i] + newLine);
    	        	}
    	        	writer.write("[SELECTED_INDICES_END]" + newLine);
        		}
        	}
        	
        	setProgress(30);
        	
        	//Write classifier
        	writer.write("[CLASSIFIER]" + newLine);
        	writer.write("Name=" + classifierName + newLine);
        	//Write classifier parameters
        	writer.write("[PARAMETER_START]" + newLine);
        	for (String parameter : classParams.keySet()) {
        		writer.write(parameter + "=" +classParams.get(parameter) + newLine);
        	}
        	writer.write("[PARAMETER_END]" + newLine);
        	
        	//Save trained model of the classifier
        	if(classifier != null) {
        		if(classifier instanceof SavableClassifier) {
	        		String modelPath = getUniquePath(file.getParent(), fileName + "_" + label + "_model");            	
	        		((SavableClassifier)classifier).saveModel(((SavableClassifier)classifier).getModel(), modelPath);
	        		writer.write("Path=" + modelPath + newLine);
        		}
        	}
        	
        	setProgress(80);
        	
        	writer.flush();
        	writer.close();
        	
        	setProgress(100);
        }
        catch(IOException ex) {
        	System.out.println("Exception occured while writing file: " + file.getName());
        	System.out.println("Exception: " + ex.getMessage());
        	ex.printStackTrace();
        }
	}
	/*
	 * Gets a unique file name, given a parent path and base name
	 */
	private String getUniquePath(String path, String name) {
		int trail = 0;
		File f = new File(path, name);
		while(f.exists()) {
			trail++;
			f = new File(path, name + trail);
		}
		return f.getPath();
	}
	/*
	 * Scans and reads the chain file and loads model properties from file content
	 */
	public void read(File file) throws FileNotFoundException, Exception {
		Scanner scanner = null;
		try {
			scanner = new Scanner(file);
		}
		catch (FileNotFoundException e) {
			System.out.println("Target file not found.");
			e.printStackTrace();
			throw e;
		}
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if(line.startsWith("#")) {
				//Read channel information
				if(line.startsWith("# Channel")) {
					channel = line.replaceFirst("# Channel:", "").trim();
				}
				else if(line.startsWith("# Image Size:")) {
					imageSize = line.replaceFirst("# Image Size:", "").trim();
				}
				else if(line.startsWith("# Label:")) {
					label = line.replaceFirst("# Label:", "").trim();
				}
				//Ignore other comments
				continue;
			}
			if(line.equals("[FEATURE_EXTRACTOR]")) {
				//First line after this tag should be name of the extractor
				line = scanner.nextLine();
				
				Extractor ex = null;
				//Read extractor name
				if(line.startsWith("Name=")) {
					ex = new Extractor(line.replaceFirst("Name=", ""));
				}
				else
					throw new Exception("Invalid chain file.");
				//End extractor name
				
				//Read extractor parameters
				line = scanner.nextLine();
				if(line.equals("[PARAMETER_START]")) {
					while(scanner.hasNextLine()) {
						line = scanner.nextLine();
						if(line.equals("[PARAMETER_END]"))
							break;
						String params[] = line.split("=");
						if(params.length == 2)
							ex.addParams(params[0], params[1]);
						else
							throw new Exception("Invalid extractor parameter.");
					}
				}//End extractor parameters	
				extractors.add(ex);
			}
			
			if(line.equals("[FEATURE_SELECTOR]")) {
				//First line after this tag should be name of the selector
				line = scanner.nextLine();
				
				Selector sel = null;
				//Read selector name
				if(line.startsWith("Name=")) {
					sel = new Selector(line.replaceFirst("Name=", ""));
				}
				else
					throw new Exception("Invalid chain file.");
				
				//Read selected indices
				line = scanner.nextLine();
				if(line.equals("[SELECTED_INDICES_START]")) {
                    ArrayList<Integer> indices = new ArrayList<Integer>();
                    while(scanner.hasNextLine()) {
                    	line = scanner.nextLine();
                        if(line.equals("[SELECTED_INDICES_END]"))
                        	break;
                        indices.add(Integer.valueOf(line));                                             
                    }
                    //Convert to int array
                    int[] selectedIndices = new int[indices.size()];
                    for(int i=0; i < indices.size(); i++)
                    	selectedIndices[i] = indices.get(i);
                    sel.setSelectedIndices(selectedIndices);
				}				
				selectors.add(sel);
			}//End Feature Selector
			
			if(line.equals("[CLASSIFIER]")) {
				line = scanner.nextLine();
				//Read classifier name
				if(line.startsWith("Name=")) {
					classifierName = line.replaceFirst("Name=", "");
				}
				else
					throw new Exception("Invalid chain file.");
				
				//Read classifier parameters
				line = scanner.nextLine();
				if(line.equals("[PARAMETER_START]")) {
					classParams = new HashMap<String, String>();
					while(scanner.hasNextLine()) {
						line = scanner.nextLine();
						if(line.equals("[PARAMETER_END]"))
							break;
						String params[] = line.split("=");
						if(params.length == 2)
							classParams.put(params[0], params[1]);
						else
							throw new Exception("Invalid classifier parameter.");
					}
				}//End classifier parameters
				
				line = scanner.nextLine();
				//Read classifier model path
				if(line.startsWith("Path=")) {
					String path = line.replaceFirst("Path=", "");
					classifier = (new Annotator()).getClassifierGivenName(classifierName, classParams);
					if(classifier instanceof SavableClassifier) {
						//TODO: Load model from file and set the model for the classifier (may be the loadmodel() should set as well)
						((SavableClassifier)classifier).setModel(((SavableClassifier)classifier).loadModel(path));
					}
				}
			}
		}
	}
	
	/*
	 * Used for testing that the loaded model is valid. It is valid if there is at least a classifier model
	 */
	public boolean isValid() {
		if(classifier instanceof SavableClassifier) {
			if(((SavableClassifier)classifier).getModel() != null)
				return true;
		}		
		return false;
	}
	
	/*
	 * Shows progress in progress bar if there is one
	 */
	private void setProgress(final int currentProgress)
	{
		if (bar!=null) 
	        SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	            	bar.setValue(currentProgress);
	            }
	        });
	}
	
	public void addExtractor(Extractor ex) {
		this.extractors.add(ex);
	}
	public void addSelector(Selector sel) {
		this.selectors.add(sel);
	}
	
	//Getters and setters
	public String getImageSet() {
		return imageSet;
	}
	public void setImageSet(String imageSet) {
		this.imageSet = imageSet;
	}
	public String getTestingSet() {
		return testingSet;
	}
	public void setTestingSet(String testingSet) {
		this.testingSet = testingSet;
	}
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
	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public float getResult() {
		return result;
	}
	public void setResult(float result) {
		this.result = result;
	}
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
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
	public String getClassifierName() {
		return classifierName;
	}
	public void setClassifierName(String classifierName) {
		this.classifierName = classifierName;
	}
	public Classifier getClassifier() {
		return classifier;
	}
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}
	public HashMap<String, String> getClassParams() {
		return classParams;
	}
	public void setClassParams(HashMap<String, String> classParams) {
		this.classParams = classParams;
	}
	public void setBar(JProgressBar bar) {
		this.bar = bar;
	}
}
