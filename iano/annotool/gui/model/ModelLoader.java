package annotool.gui.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFileChooser;

import annotool.AnnOutputPanel;
import annotool.Annotation;
import annotool.Annotator;
import annotool.ComboFeatures;
import annotool.classify.Classifier;
import annotool.classify.SavableClassifier;
import annotool.classify.Validator;
import annotool.gui.ButtonTabComponent;
import annotool.gui.ImageReadyPanel;
import annotool.gui.ResultPanel;
import annotool.gui.StatsPanel;
import annotool.io.ChainModel;
import annotool.io.DataInput;
import annotool.select.FeatureSelector;
import annotool.select.FishersCriterion;

public class ModelLoader implements Runnable {
	AnnOutputPanel pnlStatus = null;
	ImageReadyPanel pnlImages = null;
	
	ArrayList<ChainModel> chainModels = null;
	
	final JFileChooser fileChooser = new JFileChooser();
	
	private Thread thread = null;
	
	private Annotation[][] annotations = null;
	
	public ModelLoader(ImageReadyPanel pnlImages) {
		this.pnlImages = pnlImages;
		this.pnlStatus = pnlImages.getOutputPanel();
		
		chainModels = new ArrayList<ChainModel>();
		
		fileChooser.setMultiSelectionEnabled(true);		
		fileChooser.addChoosableFileFilter(new ModelFilter());
		fileChooser.setAcceptAllFileFilterUsed(false);
	}
	
	/**
	 * Removes all the models previously loaded
	 */
	public void clearModels() {
		chainModels.clear();
	}
	
	/** 
	 * Method: loadModels
	 * Loads models from multiple files and directories.
	 * Each selected directory is traversed to locate model files to load.
	 * Sub-directories are not traversed.
	 * 
	 * @return True if model was loaded, false if canceled
	 */
	public boolean loadModels() {
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		int returnVal = fileChooser.showOpenDialog(pnlImages);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            
			pnlStatus.setOutput("Loading model..");
			
			//For each selected file or directory
			for(int i=0; i < files.length; i++) {
				if(files[i].isDirectory()) {
					File[] childFiles = files[i].listFiles();
					for(int j = 0; j < childFiles.length; j++) {
						if(childFiles[j].isFile()) {	//Only process files (not sub-directories) inside the directory
							//Only process model files
							String ext = Utils.getExtension(childFiles[j]);
							if(ext == null || !ext.equals(Utils.MODEL_EXT))
								continue;
							
							ChainModel ch = new ChainModel();
							try {
				            	ch.read(childFiles[j]);
				            	if(ch.isValid())
				            		chainModels.add(ch);
				            	else
				            		pnlStatus.setOutput(childFiles[j].getName() + " is invalid. Discarded!");
				            	
				            	pnlStatus.setOutput(childFiles[j].getName() + " loaded successfully.");
				            }
				            catch (Exception ex) {
				            	pnlStatus.setOutput("Model loading failure. Current File: " + childFiles[j].getName());
				            	System.out.println(ex.getMessage());
				            }
						}
					}
				}
				else
				{
		            ChainModel ch = new ChainModel();
		            try {
		            	ch.read(files[i]);
		            	if(ch.isValid())
		            		chainModels.add(ch);
		            	else
		            		pnlStatus.setOutput(files[i].getName() + " is invalid. Discarded!");
		            	pnlStatus.setOutput(files[i].getName() + " loaded successfully.");
		            }
		            catch (Exception ex) {
		            	pnlStatus.setOutput("Model loading failure. Current File: " + files[i].getName());
		            	System.out.println(ex.getMessage());
		            }
				}
			}
			pnlStatus.setOutput("Total models loaded: " + String.valueOf(chainModels.size()));
			
			return true;
        }
			
		return false;
	}
	/**
	 * Loads model from a single file.
	 * 
	 * @return True if model was loaded, false if canceled or invalid model
	 */
	public boolean loadModel() {
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		ChainModel chainModel = null;
		
		int returnVal = fileChooser.showOpenDialog(pnlImages);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
			pnlStatus.setOutput("Loading model..");
			
			chainModel = new ChainModel();
			try {
				chainModel.read(file);
            	if(!chainModel.isValid()) {
            		chainModel = null;
            		pnlStatus.setOutput(file.getName() + " is invalid. Discarded!");            		
            	}
            	else {      
            		chainModels.add(chainModel);	//Note: it adds to the arraylist so whether there will be one model or multiple model depends on how this is called
                	pnlStatus.setOutput(file.getName() + " loaded successfully.");
            	}
            }
            catch (Exception ex) {
            	pnlStatus.setOutput("Model loading failure. File: " + file.getName());
            	System.out.println(ex.getMessage());
            }
        }
		if(chainModel == null)
			return false;
	
		return true;		
	}
	
	public boolean validate() {
		for(ChainModel model : chainModels) {
			String modelChannel = model.getChannel();
			if(modelChannel == null || !modelChannel.equals(pnlImages.getSelectedChannel())) //If channel is not the same
				return false;
			//TODO: Validate problem size
		}
		return true;
	}
	
	@Override
	public void run() {
		//TODO: disable buttons and enable at the end		
		classify();
		thread = null;
		pnlImages.enableSaveReport(true);
	}
	
	/**
	 * Iterates through the arraylist of loaded models and applies each one to the current image set
	 */
	public void applyModel() {		
		if (thread == null)  {
            thread = new Thread(this);
            thread.start();
        }
	}
	
	/**
	 * Image Classification.
	 */
	private void classify() {
		String channel = pnlImages.getSelectedChannel();
		
		Annotator anno = new Annotator();
		
		//------ read image data from the directory ------------//
        DataInput problem = new DataInput(Annotator.dir, Annotator.ext, channel);
        
      	//TODO: Use this to validate model file
        int imgWidth = problem.getWidth();
        int imgHeight = problem.getHeight();
        
        //Initialize structure to store annotation results
        final int numModels = chainModels.size();
        final int problemSize = problem.getLength();
        annotations = new Annotation[numModels][problemSize];
        for (int i = 0; i < numModels; i++) {
            for (int j = 0; j < problemSize; j++) {
                annotations[i][j] = new Annotation();
            }
        }
        //Also, initialize a list of annotation labels to use for updated table column
        String[] modelLabels = new String[numModels];
        boolean[] supportsProb = new boolean[numModels];
        
        for(int modelIndex = 0; modelIndex < numModels; modelIndex++) {
        	pnlStatus.setOutput("Applying model: " + (modelIndex + 1) + " of " + numModels);
        	
        	ChainModel model = chainModels.get(modelIndex);
        	
        	modelLabels[modelIndex] = model.getLabel();
        	
        	//If image size in the model is not same as the problem size, display message
        	if(!model.getImageSize().equals(imgWidth + "x" + imgHeight))
        		pnlStatus.setOutput("Image size mismatch between model and problem. Model: " + (modelIndex + 1));
        	
	        pnlStatus.setOutput("Extracing features ... ");
	        
	        //Start of extraction : TODO: this and similar parts can be centralized somewhere(in Annotator or a helper class)
	        String extractor = "None";
	        HashMap<String, String> params = new HashMap<String, String>();
	        
	        int numExtractors = model.getExtractors().size();
	        float[][][] exFeatures = new float[numExtractors][][];
	        
	        int dataSize = 0;	//To keep track of total size
	        for(int exIndex=0; exIndex < numExtractors; exIndex++) {
	        	extractor = model.getExtractors().get(exIndex).getName();
	        	params = model.getExtractors().get(exIndex).getParams();
	        	
	        	exFeatures[exIndex] = anno.extractGivenAMethod(extractor, params, problem);
	        	
	        	dataSize += exFeatures[exIndex][0].length;
	        }
	        
	        float[][] features = null;
	        
	        if(numExtractors < 1) {	//If no extractor, call the function by passing "None"
	        	features = anno.extractGivenAMethod(extractor, params, problem);
	        }
	        else {	//Else, create feature array with enough space to hold data from all extractors 
	        	features = new float[problemSize][dataSize];
	        	
	        	int destPos = 0;
	        	for(int exIndex=0; exIndex < numExtractors; exIndex++) {
	        		for(int item=0; item < features.length; item++) {
	        			System.arraycopy(exFeatures[exIndex][item], 0, features[item], destPos, exFeatures[exIndex][item].length);
	        		}
	        		destPos += exFeatures[exIndex][0].length;
	        	}
	        }
	        exFeatures = null;
	        //End of extraction
	        
	        //raw data is not used after this point, set to null.
	        problem.setDataNull();
	        
	        //TODO: Selecting features
	        for(Selector sel : model.getSelectors()) {
		        if(sel.getSelectedIndices() != null) {
		        	pnlStatus.setOutput("Selecting features with " + sel.getName());
		        	features = anno.selectGivenIndices(features, sel.getSelectedIndices());
		        }
	        }
	        
	        //Classify using model
	        pnlStatus.setOutput("Classifying ... ");
	        
	        SavableClassifier classifier = (SavableClassifier)model.getClassifier();
	        supportsProb[modelIndex] = classifier.doesSupportProbability();
	        try {
				anno.classifyGivenAMethod(classifier, features, annotations[modelIndex]);
			} catch (Exception ex) {
				pnlStatus.setOutput("Classification using model failed.");
				ex.printStackTrace();
			}
		        
        }//End of loop for models
        
        //Display results
		pnlImages.getTablePanel().updateAnnotationTable(annotations, modelLabels, supportsProb);
		
		//Display statistics
		HashMap<String, String> classNames = chainModels.get(0).getClassNames();//TODO: each model should have it's own set of class names
		StatsPanel pnlStats = new StatsPanel(annotations, classNames, modelLabels);
		pnlImages.addStatsPanel(pnlStats);
		
		pnlStatus.setOutput("DONE");
	}

	public Annotation[][] getAnnotations() {
		return annotations;
	}
}
