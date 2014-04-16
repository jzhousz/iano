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
//import annotool.Annotator;
import annotool.classify.Classifier;
import annotool.classify.SavableClassifier;
import annotool.classify.CommEns.CommitteeEnsemble;
import annotool.gui.model.ClassifierInfo;
import annotool.gui.model.Extractor;
import annotool.gui.model.Selector;
import annotool.gui.model.Utils;

/**
 * This class is responsible for storing trained  model from memory into file and 
 * loading from file into the memory.
 * 
 *
 */
public class ChainModel {
	//Data members
	private String imageSet = null;
	private String testingSet = null; //Used if training/testing
	private String imageSize = null;
	private String mode = null;
	private String channel = null;
	private float result;
	private String label = null;
	private HashMap<String, String> classNames = null;
	private ArrayList<Extractor> extractors = null;
	private ArrayList<Selector> selectors = null;
	
	SavableClassifier classifier = null; //added 1/16/2014
	private ArrayList<ClassifierInfo> classifiersInfo = null; //added 1/16/2014
	
	//private String ensembleName = null;
	private HashMap<String, String> ensParams = null;
	//private String ensemblePath = null;
	//private String ensembleClass = null;
	
	//private String classifierName = null; //removed 1/16/2014
	//private Classifier classifier = null;  //removed 1/16/2014
	//private HashMap<String, String> classParams = null; //removed 1/16/2014
	//private String classifierPath = null;	//If external classifier (i.e. from plugin), path needs to be saved removed 1/16/2014
	//private String classifierClass = null; //removed 1/16/2014
	
	JProgressBar bar = null;
	
	public ChainModel() {
		extractors = new ArrayList<Extractor>();
		selectors = new ArrayList<Selector>();
		classifiersInfo = new ArrayList<ClassifierInfo>(); // Added 1/16/2014
		//classifiers = new ArrayList<Classifier>(); // Added 1/16/2014
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
        	
        	if(classNames != null && classNames.size() > 0) {
        		writer.write("# Classes: ");
	        	for(String key : classNames.keySet()) {
	        		writer.write(key + ":" + classNames.get(key) + "   ");
	        	}
	        	writer.newLine();
        	}
        	
        	writer.newLine();
        	
        	setProgress(5);
        	
        	//Write feature extractor
        	for(Extractor ex : extractors) {
	        	writer.write("[FEATURE_EXTRACTOR]" + newLine);
	        	writer.write("Name=" + ex.getName() + newLine);
	        	writer.write("ClassName=" + ex.getClassName() + newLine);
	        	writer.write("Path=" + ex.getExternalPath() + newLine);
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
        	for(ClassifierInfo classifierInfo :  classifiersInfo) 
        	{
        		writer.write("[CLASSIFIER]" + newLine);
        		writer.write("Name=" + classifierInfo.getName() + newLine);
        		writer.write("ClassName=" + classifierInfo.getClassName() + newLine);
        		writer.write("Path=" + classifierInfo.getExternalPath() + newLine); //External classifier (i.e. plugin) or null
        	
        		//Write classifier parameters
            	writer.write("[PARAMETER_START]" + newLine);
            	for (String parameter :  classifierInfo.getParams().keySet()) {
            		writer.write(parameter + "=" + classifierInfo.getParams().get(parameter) + newLine);
            	}
            	writer.write("[PARAMETER_END]" + newLine);
            	
 
        	}
        	
        	
        	String modelfileName = getUniqueFileName(file.getParent(), fileName + "_" + label + "_model");
    		String modelPath = file.getParent() + File.separatorChar + modelfileName;            	
    		((SavableClassifier)classifier ).saveModel(((SavableClassifier)classifier).getModel(), modelPath);
    		writer.write("Path=" + modelfileName + newLine);
        	/*
        	//Write Ensemble
        	writer.write("[Ensemble]" + newLine);
        	writer.write("Name=" + ensembleName + newLine);
        	writer.write("EnsName=" + ensembleClass + newLine);
        	writer.write("Path=" + ensemblePath + newLine); //External Ensemble (i.e. plugin) or null
        	//Write classifier parameters
        	writer.write("[PARAMETER_START]" + newLine);
        	for (String parameter : ensParams.keySet()) {
        		writer.write(parameter + "=" + ensParams.get(parameter) + newLine);
        	}
        	writer.write("[PARAMETER_END]" + newLine);
        	*/
        	/* Removed 1/16/2014
        	//Write classifier
        	writer.write("[CLASSIFIER]" + newLine);
        	writer.write("Name=" + classifierName + newLine);
        	writer.write("ClassName=" + classifierClass + newLine);
        	writer.write("Path=" + classifierPath + newLine); //External classifier (i.e. plugin) or null
        	//Write classifier parameters
        	writer.write("[PARAMETER_START]" + newLine);
        	for (String parameter : classParams.keySet()) {
        		writer.write(parameter + "=" +classParams.get(parameter) + newLine);
        	}
        	writer.write("[PARAMETER_END]" + newLine);
        	
        	//Save trained model of the classifier
        	if(classifier != null) {
        		if(classifier instanceof SavableClassifier) {
        			String modelfileName = getUniqueFileName(file.getParent(), fileName + "_" + label + "_model");
	        		String modelPath = file.getParent() + File.separatorChar + modelfileName;            	
	        		((SavableClassifier)classifier).saveModel(((SavableClassifier)classifier).getModel(), modelPath);
	        		writer.write("Path=" + modelfileName + newLine);
        		}
        	}
        	*/
        	
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
	private String getUniqueFileName(String path, String name) {
		int trail = 0;
		File f = new File(path, name);
		while(f.exists()) {
			trail++;
			f = new File(path, name + trail);
		}
		return f.getName();
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
				else if(line.startsWith("# Classes:")) {
					line = line.replaceFirst("# Classes:", "");
					
					Scanner lineScanner = new Scanner(line);
					classNames = new HashMap<String, String>();
					while(lineScanner.hasNext()){
		                String pair[] = lineScanner.next().split(":");
		                classNames.put(pair[0], pair[1]);
		            }
		            lineScanner.close();  
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
				{
					scanner.close();
					throw new Exception("Invalid model file.");
				}
				//End extractor name
				
				//Read class name
				line = scanner.nextLine();
				if(line.startsWith("ClassName=")) {
					ex.setClassName(line.replaceFirst("ClassName=", ""));
				}
				else
				{
					scanner.close();
					throw new Exception("Invalid model file.");
				}
				
				//Read path
				line = scanner.nextLine();
				if(line.startsWith("Path=")) {
					String path = line.replaceFirst("Path=", "");
					if(!path.equals("null"))
						ex.setExternalPath(path);
				}
				else
				{
					scanner.close();
					throw new Exception("Invalid model file.");
				}
					
				
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
						{
							scanner.close();
							throw new Exception("Invalid extractor parameter.");
						}
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
				{
					scanner.close();
					throw new Exception("Invalid model file.");
				}
				
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

		
			while(line.equals("[CLASSIFIER]")) 
			{
				//First line after this tag should be name of the Classifier
				line = scanner.nextLine();

				System.out.println(line);
				
				ClassifierInfo cal = null;
				//Read Classfier name
				if(line.startsWith("Name=")) {
					cal = new ClassifierInfo(line.replaceFirst("Name=", ""));
					
					line = scanner.nextLine();
					cal.setClassName(line.replaceFirst("ClassName=", ""));
					
					line = scanner.nextLine();
					cal.setExternalPath(line.replaceFirst("Path=", ""));
					
					line = scanner.nextLine();
					if(line.equals("[PARAMETER_START]")) {
						while(scanner.hasNextLine()) {
							line = scanner.nextLine();
							if(line.equals("[PARAMETER_END]"))
								break;
							String params[] = line.split("=");
							if(params.length == 2)
								cal.addParams(params[0], params[1]);
							else
							{
								scanner.close();
								throw new Exception("Invalid classifier parameter.");
							}
						}
							//line = scanner.nextLine();
						
							//System.out.println(line);
							
							classifiersInfo.add(cal);
							
							//Classifier classifier = null;
							
							//String path = line.replaceFirst("Path=", "");;
							//path = file.getParent() + File.separatorChar + path; 
							//classifier = (new Annotator()).getClassifierGivenName(cal.getClassName(), cal.getExternalPath(), cal.getParams());
							//if(classifier instanceof SavableClassifier)
							//	((SavableClassifier)classifier).setModel(((SavableClassifier)classifier).loadModel(path));
							
							if(scanner.hasNext() && !line.equals("[CLASSIFIER]"))
								line = scanner.nextLine();
							
							System.out.println("Chain Model Classifier: " + line);
						
				}	
				else
				{
					scanner.close();
					throw new Exception("Invalid model file.");
				}
			
				}				
			}//End Classifier
			
			if(classifiersInfo.size() == 1)
			{
			
				String path = line.replaceFirst("Path=", "");
				path = file.getParent() + File.separatorChar + path; 
				classifier = (SavableClassifier) (new Annotator()).getClassifierGivenName(classifiersInfo.get(0).getClassName(), classifiersInfo.get(0).getExternalPath(), classifiersInfo.get(0).getParams());
				if(classifier instanceof SavableClassifier)
					((SavableClassifier)classifier).setModel(((SavableClassifier)classifier).loadModel(path));
			}
			else if(classifiersInfo.size() > 1)
			{	
				String path = line.replaceFirst("Path=", "");;
				path = file.getParent() + File.separatorChar + path; 
				classifier = new CommitteeEnsemble(classifiersInfo);
				((SavableClassifier)classifier).setModel(((SavableClassifier)classifier).loadModel(path));
			}
			
			
			/*
			
			if(line.equals("[Ensemble]")) {
				line = scanner.nextLine();
				//Read classifier name
				if(line.startsWith("Name=")) {
					ensembleName = line.replaceFirst("Name=", "");
				}
				else
					throw new Exception("Invalid model file.");
				
				//Read class name
				line = scanner.nextLine();
				if(line.startsWith("EnsName=")) {
					ensembleClass = line.replaceFirst("ClassName=", "");
				}
				else
					throw new Exception("Invalid model file.");
				
				//Read path
				line = scanner.nextLine();
				if(line.startsWith("Path=")) {
					String path = line.replaceFirst("Path=", "");
					if(!path.equals("null"))
						ensemblePath = path;
				}
				else
					throw new Exception("Invalid model file.");
				
				//Read classifier parameters
				line = scanner.nextLine();
				if(line.equals("[PARAMETER_START]")) {
					ensParams = new HashMap<String, String>();
					while(scanner.hasNextLine()) {
						line = scanner.nextLine();
						if(line.equals("[PARAMETER_END]"))
							break;
						String params[] = line.split("=");
						if(params.length == 2)
							ensParams.put(params[0], params[1]);
						else
							throw new Exception("Invalid Ensemble parameter.");
					}
				}
			}//End Ensemble parameters
			*/
			/* Removed 1/16/2014
			if(line.equals("[CLASSIFIER]")) {
				line = scanner.nextLine();
				//Read classifier name
				if(line.startsWith("Name=")) {
					classifierName = line.replaceFirst("Name=", "");
				}
				else
					throw new Exception("Invalid model file.");
				
				//Read class name
				line = scanner.nextLine();
				if(line.startsWith("ClassName=")) {
					classifierClass = line.replaceFirst("ClassName=", "");
				}
				else
					throw new Exception("Invalid model file.");
				
				//Read path
				line = scanner.nextLine();
				if(line.startsWith("Path=")) {
					String path = line.replaceFirst("Path=", "");
					if(!path.equals("null"))
						classifierPath = path;
				}
				else
					throw new Exception("Invalid model file.");
				
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
					path = file.getParent() + File.separatorChar + path; 
					classifier = (new Annotator()).getClassifierGivenName(classifierClass, classifierPath, classParams);
					if(classifier instanceof SavableClassifier)
						((SavableClassifier)classifier).setModel(((SavableClassifier)classifier).loadModel(path));
				}
				
			}
			*/
			
		}
		scanner.close();
	}
	
	/*
	 * Used for testing that the loaded model is valid. It is valid if there is at least a classifier model
	 */
	public boolean isValid() {
	if(classifier != null)
	{
		return true;
	
	}
		
		
		//removed 1/20/2014
	//	if(classifier instanceof SavableClassifier) {
	//		if(((SavableClassifier)classifier).getModel() != null)
	//			return true;
	//	}		
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
	
	//Checks if the particular model has binary classes
	public boolean isBinary() {
		if(classNames.size() != 2)
			return false;
		
		boolean containsYes = false,
				containsNo = false;
		
		for(String key : classNames.keySet()) {
			if(key.equals("1") && classNames.get(key).equalsIgnoreCase("yes"))
				containsYes = true;
			if(key.equals("0") && classNames.get(key).equalsIgnoreCase("no"))
				containsNo = true;
		}
		
		return (containsYes && containsNo);
			
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

	public HashMap<String, String> getClassNames() {
		return classNames;
	}

	public void setClassNames(HashMap<String, String> classNames) {
		this.classNames = classNames;
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
	
	public void addClassifierInfo( ClassifierInfo classifierInfo) {
		
		this.classifiersInfo.add(classifierInfo);
		
	}

	public void setClassifierInfo( ArrayList<ClassifierInfo> classifierInfo) {
		
		this.classifiersInfo = classifierInfo;
		
	}	
	
	public void setSavableClassifier( SavableClassifier classifier )
	{
		this.classifier = classifier;
	}
	
	public ArrayList<ClassifierInfo> getClassifierInfo() {
		
		return classifiersInfo;	
	}
	
	public SavableClassifier getClassifier() {
		/*
		if(classifiersInfo.size() > 1)
		{
			classifier = new CommitteeEnsemble(classifiersInfo);
		} 
		else
		{
			try 
			{
				classifier = (SavableClassifier) ( new annotool.Annotator()).getClassifierGivenName(classifiersInfo.get(0).getClassName(), classifiersInfo.get(0).getExternalPath(), classifiersInfo.get(0).getParams());

			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		*/
		return classifier;
	}
	
	public void setBar(JProgressBar bar) {
		this.bar = bar;
	}

	/*
	public String getEnsembleName() {
		return ensembleName;
	}
	public void setEnsembleName(String EnsembleName) {
		this.ensembleName = EnsembleName;
	}
	 */
	public HashMap<String, String> getEnsParams() {
		return ensParams;
	}
	public void setEnsParams(HashMap<String, String> ensParams) {
		this.ensParams = ensParams;
	}
	/*
	public void setEnsemblePath(String path) {
		this.ensemblePath = path;
	}

	public void setEnsembleClass(String ensembleClass) {
		this.ensembleClass = ensembleClass;
	}
	*/
	/* Removed 1/16/2014 
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

	public void setClassifierPath(String path) {
		this.classifierPath = path;
	}

	public void setClassifierClass(String classifierClass) {
		this.classifierClass = classifierClass;
	}
	*/
	
}
