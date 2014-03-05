package annotool.gui.model;

import java.util.ArrayList;


/**
 * Represents a single chain of algorithms.
 * It consists one classifier, 0 or more extractors and 0 or more selectors.
 * 
 */
public class Chain {
	private boolean EnsMode = false;
	
	private String name = null;
	private ArrayList<Extractor> extractors = null;
	private ArrayList<Selector> selectors = null;
	//private String classifier = null; Removed 1/16/2014
	private ArrayList<ClassifierChain> classifier = null;
	/*
	private String ensemble = null;
	private String ensembleClassName = null;
	private String ensembleExternalPath = null; 
	private HashMap<String, String> ensParams = null; 
	*/
	//private String classifierClassName = null; Removed 1/16/2014
	//private String classifierExternalPath = null; Removed 1/16/2014
	//private HashMap<String, String> classParams = null; Removed 1/16/2014
	
	public Chain(){
		//Create an empty chain as a placeholder for chainCopy
	}
	
	public Chain(String name) {
		this.name = name;
		
		extractors = new ArrayList<Extractor>();
		selectors = new ArrayList<Selector>();
		classifier =  new ArrayList<ClassifierChain>(); // added 1/16/2014
		//classParams = new HashMap<String, String>(); Removed 1/16/2014
		//ensParams = new HashMap<String, String>();
	}

	/* Added on 1/16/2014 */
	public boolean isEns() {
		return EnsMode;
	}
	
	public void addExtractor(Extractor ex) {
		extractors.add(ex);
	}
	public void addSelector(Selector sel) {
		selectors.add(sel);
	}
	public void addClassifier(ClassifierChain Class) { //Added 1/16/2014
		if(classifier.size() > 1)
			EnsMode = true;
		
		classifier.add(Class);
	}
	public void clearExtractors() {
		extractors.clear();
	}
	public void clearSelectors() {
		selectors.clear();
	}
	public void clearClassifier() { //Added 1/16/2014
		classifier.clear();
	}
	/* Removed on 1/16/2014 
	public void addClassifierParam(String key, String value) {
		classParams.put(key, value);
	}
	*/
	/*
	public void addEnsembleParam(String key, String value) {
		ensParams.put(key, value);
	}
	*/
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
	public boolean hasClassifier() {
		if (classifier.size() > 0)
			return true;
		return false;
	}
	public boolean isComplete() {
		//Chain is complete when there is at least one classifier
		//It is valid even if there is no extractor or selector because they should default to 'None'
		if(classifier == null || classifier.isEmpty() ) /* classifier.equalsIgnoreCase("None")) */ /* Changed 1/16/14 */
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
		
		if( classifier.size() > 0 )/*classifier != null) */ /* Changed 1/16/2014 */
		{
			/* str.append("Classifier: " + classifier); */ /* Removed 1/16/2014 */
			/* Added 1/16/2014 */
			for(int i = 0; i < classifier.size(); i++) {
				str.append("Classifier"+ (i+1) +": " + classifier.get(i).getName() + "; ");
			}
		}
		else
			str.append("Classifier: NONE; ");
	/*	
		if(ensemble != null)
		{
			str.append("Ensemble: " + ensemble);
		}
		*/
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

	public ArrayList<ClassifierChain> getClassifier() { //Added 1/16/2014
		return classifier;
	}

	public void setClassifier( ArrayList<ClassifierChain> classifier ) { /* String classifier) { */ /* Changed 1/16/2014 */
		this.classifier = classifier;
	}
	
	/*
	public String getEnsemble() {  // Added 1/28/2014 
		return ensemble ;
	}
	
	public void setEnsemble( String ensemble ) {  // Added 1/28/2014 
		this.ensemble  = ensemble ;
	}
	*/
	/* Removed 1/16/2014
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
	*/
	
	//Added 1/28/2014
	/*
	public HashMap<String, String> getEnsParams() {
		return ensParams;
	}
	
	public void setEnsParams(HashMap<String, String> ensParams) {
		this.ensParams = ensParams;
	}

	
	public String getEnsembleClassName() {
		return ensembleClassName;
	}
	
	public void setEnsembleClassName(String ensembleClassName) {
		this.ensembleClassName = ensembleClassName;
	}

	public String getEnsembleExternalPath() {
		return ensembleExternalPath;
	}

	public void setEnsembleExternalPath(String ensembleExternalPath) {
		this.ensembleExternalPath = ensembleExternalPath;
	}
	*/
	//
	public Chain copyChain(Chain toBeCopied){
		//Extractors
		for (int i = 0; i < toBeCopied.extractors.size(); i++)
			{
			Extractor ex = new Extractor(toBeCopied.extractors.get(i).getName());
			ex.setClassName(toBeCopied.extractors.get(i).getClassName());
			ex.setExternalPath(toBeCopied.extractors.get(i).getExternalPath());
			ex.setParams(toBeCopied.extractors.get(i).getParams());
			
			this.addExtractor(ex);
			}
		
		//Selectors
		for (int i = 0; i < toBeCopied.selectors.size(); i++)
			{
			Selector sel = new Selector(toBeCopied.selectors.get(i).getName());
			sel.setClassName(toBeCopied.selectors.get(i).getClassName());
			sel.setExternalPath(toBeCopied.selectors.get(i).getExternalPath());
			sel.setParams(toBeCopied.selectors.get(i).getParams());
			
			this.addSelector(sel);
			}
		
		// Added 1/28/2014
		//this.setEnsemble(toBeCopied.getEnsemble());
		//this.setEnsembleClassName(toBeCopied.getEnsembleClassName());
		//this.setEnsParams(toBeCopied.ensParams);
		//this.setEnsembleExternalPath(toBeCopied.getEnsembleExternalPath());	 
		 //
		
		/* Removed 1/16/2014
		//Class Parameters
		this.setClassifier(toBeCopied.getClassifier());
		this.setClassifierClassName(toBeCopied.getClassifierClassName());
		this.setClassParams(toBeCopied.classParams);
		this.setClassifierExternalPath(toBeCopied.getClassifierExternalPath());
		*/
		/* Added 1/16/2014 */
		for (int i = 0; i < toBeCopied.classifier.size(); i++)
		{
			ClassifierChain Class = new ClassifierChain(toBeCopied.classifier.get(i).getName());
			Class.setClassName(toBeCopied.classifier.get(i).getClassName());
			Class.setExternalPath(toBeCopied.classifier.get(i).getExternalPath());
			Class.setParams(toBeCopied.classifier.get(i).getParams());
		
			this.addClassifier(Class);
		}
		
		

		
		return this;
	}
}
