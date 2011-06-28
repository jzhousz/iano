package annotool.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import annotool.classify.Classifier;
import annotool.classify.SavableClassifier;

public class ChainModel {
	//Data members
	private String imageSet = null;
	private String imageSize = null;
	private String mode = null;
	private float result;
	private String label = null;
	private String extractorName = null;
	private HashMap<String, String> exParams = null;
	private String selectorName = null;
	private int[] selectedIndices = null;
	private String classifierName = null;
	private Classifier classifier = null;
	
	/*
	 * Method to write to file
	 */
	public void write(File baseFile) {
		//Cross platform new line character
    	String newLine = System.getProperty("line.separator");
    	
		File file = new File(baseFile.getParent(), baseFile.getName() + "_" + label);
        try {
    		//FileWriter baseFileWriter = new FileWriter(baseFile);
    		//baseFileWriter.write(file.getPath() + newLine + "EOF");
    		//baseFileWriter.flush();
    		
        	BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        	//Write comment section
        	writer.write("# Image Set: " + imageSet + newLine);
        	writer.write("# Image Size: " + imageSize + newLine);
        	writer.write("# Mode: " + mode + newLine);
        	writer.write("# Result: " + result * 100 + "%" + newLine);
        	writer.write("# Label: " + label + newLine);
        	
        	writer.newLine();
        	
        	//Write feature extractor
        	writer.write("[FEATURE_EXTRACTOR]" + newLine);
        	writer.write("Name=" + extractorName + newLine);
        	writer.write("[PARAMETER_START]" + newLine);
        	for (String parameter : exParams.keySet()) {
        		writer.write(parameter + "=" +exParams.get(parameter) + newLine);
        	}
        	writer.write("[PARAMETER_END]" + newLine);
        	//Write feature selector
        	writer.write("[FEATURE_SELECTOR]" + newLine);
        	writer.write("Name=" + selectorName + newLine);
        	if(selectedIndices != null) {
        		writer.write("[SELECTED_INDICES_START]" + newLine);
	        	for(int i=0; i < selectedIndices.length; i++) {
	        		writer.write(selectedIndices[i] + newLine);
	        	}
	        	writer.write("[SELECTED_INDICES_END]" + newLine);
        	}	
        	
        	//Write classifier
        	writer.write("[Classifier]" + newLine);//TODO
        	writer.write("Name:" + classifierName + newLine);
        	if(classifier != null) {
        		if(classifier instanceof SavableClassifier) {
	        		String modelPath = getUniquePath(file.getParent(), file.getName() + "_model");            	
	        		((SavableClassifier)classifier).saveModel(((SavableClassifier)classifier).getModel(), modelPath);
	        		writer.write("Path:" + modelPath + newLine);
        		}
        	}
        	
        	writer.flush();
        	writer.close();
        }
        catch(IOException ex) {
        	System.out.println("Exception occured while writing file: " + file.getName());
        	ex.printStackTrace();
        }
	}
	/*
	 * Gets a unique file name, given a parent path and base name
	 */
	private String getUniquePath(String path, String name) {//TODO
		int trail = 0;
		File f = new File(path, name + trail);
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
				//Ignore comments
				continue;
			}
			if(line.equals("[FEATURE_EXTRACTOR]")) {
				//First line after this tag should be name of the extractor
				line = scanner.nextLine();
				//Read extractor name
				if(line.startsWith("Name=")) {
					extractorName = line.replaceFirst("Name=", "");
				}
				else
					throw new Exception("Invalid chain file.");
				//End extractor name
				
				//Read extractor parameters
				line = scanner.nextLine();
				if(line.equals("[PARAMETER_START]")) {
					exParams = new HashMap<String, String>();
					while(scanner.hasNextLine()) {
						line = scanner.nextLine();
						if(line.equals("[PARAMETER_END]"))
							break;
						String params[] = line.split("=");
						if(params.length == 2)
							exParams.put(params[0], params[1]);
						else
							throw new Exception("Invalid extractor parameter.");
					}
				}//End extractor parameters				
			}
			
			if(line.equals("[FEATURE_SELECTOR]")) {
				//First line after this tag should be name of the selector
				line = scanner.nextLine();
				//Read selector name
				if(line.startsWith("Name=")) {
					selectorName = line.replaceFirst("Name=", "");
				}
				else
					throw new Exception("Invalid chain file.");
				
				//Read selected indices
				if(scanner.hasNextLine()) {
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
						selectedIndices = new int[indices.size()];
						for(int i=0; i < indices.size(); i++)
							selectedIndices[i] = indices.get(i);
					}
				}
			}//End Feature Selector
			
			if(line.equals("[CLASSIFIER]")) {
				line = scanner.nextLine();
				//Read classifier name
				if(line.startsWith("Name=")) {
					classifierName = line.replaceFirst("Name=", "");
				}
				line = scanner.nextLine();
				//Read classifier model path
				if(line.startsWith("Path=")) {
					String path = line.replaceFirst("Path=", "");
					//TODO : load model from path
				}
			}
		}
	}
	//Getters and setters
	public String getImageSet() {
		return imageSet;
	}
	public void setImageSet(String imageSet) {
		this.imageSet = imageSet;
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
	public int[] getSelectedIndices() {
		return selectedIndices;
	}
	public void setSelectedIndices(int[] selectedIndices) {
		this.selectedIndices = selectedIndices;
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
}
