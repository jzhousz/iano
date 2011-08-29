package annotool.gui.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFileChooser;

import annotool.AnnOutputPanel;
import annotool.Annotation;
import annotool.Annotator;
import annotool.classify.SavableClassifier;
import annotool.gui.ImageReadyPanel;
import annotool.gui.ROIAnnotator;
import annotool.gui.ROIParameterPanel;
import annotool.io.ChainModel;
import annotool.io.DataInput;

public class ModelLoader implements Runnable {
	AnnOutputPanel pnlStatus = null;
	ImageReadyPanel pnlImages = null;
	
	ArrayList<ChainModel> chainModels = null;
	
	private Thread thread = null;
	
	private Annotation[][] annotations = null;
	HashMap<String, String> classNames = null;
	private String[] modelLabels = null;
	private boolean[] supportsProb = null; 
	
	public ModelLoader(ImageReadyPanel pnlImages) {
		this.pnlImages = pnlImages;
		this.pnlStatus = pnlImages.getOutputPanel();
		
		chainModels = new ArrayList<ChainModel>();
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
	 * Loaded models are available in chainModels
	 * 
	 * @return True if model was loaded, false if canceled
	 */
	public boolean loadModels(File[] files) {
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
	/**
	 * Loads model from a single file. Not used anymore after merging annotation and classification
	 * 
	 * @return True if model was loaded, false if canceled or invalid model
	 */
	public boolean loadModel(File file) {
		ChainModel chainModel = null;
            
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
		pnlImages.setButtonsEnabled(false);
		
		if (Annotator.output.equals(Annotator.AN)) {
			pnlStatus.setOutput("Classification/Annotation started..");
			classify();
			pnlStatus.setOutput("Classification/Annotation completed.");
			pnlImages.enableSaveReport(true);
		}
		else if (Annotator.output.equals(Annotator.ROI)) {
			pnlStatus.setOutput("ROI annotation started..");
			roiAnnotate();
			pnlStatus.setOutput("ROI annotation completed.");
		}
		thread = null;	
		
		pnlImages.setButtonsEnabled(true);
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
	 * Region of interest annotation
	 */
	private void roiAnnotate() {
		//Get the parameters
		ROIParameterPanel pnlROIParam = pnlImages.getPnlROIParam();
		if(pnlROIParam == null) {
			System.err.println("Parameter panel is null");
			return;
		}
		
		int interval = pnlROIParam.getSelectedInterval();
		int mode = pnlROIParam.getSelectedMode();
		String channel = pnlImages.getSelectedChannel();
		
		int[] selectedImages = pnlImages.getTablePanel().getAnnotationTable().getSelectedRows();
		
		ROIAnnotator roiAnnotator = new ROIAnnotator(interval, mode, channel, chainModels, selectedImages);
	}
	
	/**
	 * Image Classification/Annotation.
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
        modelLabels = new String[numModels];
        supportsProb = new boolean[numModels];
        
        for(int modelIndex = 0; modelIndex < numModels; modelIndex++) {
        	pnlStatus.setOutput("Applying model: " + (modelIndex + 1) + " of " + numModels);
        	
        	ChainModel model = chainModels.get(modelIndex);
        	
        	modelLabels[modelIndex] = model.getLabel();
        	
        	//If image size in the model is not same as the problem size, display message
        	if(!model.getImageSize().equals(imgWidth + "x" + imgHeight))
        		pnlStatus.setOutput("Image size mismatch between model and problem. Model: " + (modelIndex + 1));
        	
	        pnlStatus.setOutput("Extracing features ... ");
	        
	        //Test Test
	        
	        //Start of extraction
	        float[][] features =  null;
	        try {
				features = anno.extractWithMultipleExtractors(problem, model.getExtractors());
			} catch (Exception e) {
				e.printStackTrace();
				pnlStatus.setOutput("Feature extraction failure.");
				return;	//or continue with another model?
			}
	        //End of extraction
	        
	        //raw data is not used after this point, set to null.
	        problem.setDataNull();
	        
	        //Selecting features
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
		classNames = chainModels.get(0).getClassNames();								//TODO
		pnlImages.showStats(classNames, annotations, modelLabels);
		
		pnlStatus.setOutput("DONE");
	}

	public Annotation[][] getAnnotations() {
		return annotations;
	}

	public HashMap<String, String> getClassNames() {
		return classNames;
	}

	public String[] getModelLabels() {
		return modelLabels;
	}

	public boolean[] getSupportsProb() {
		return supportsProb;
	}
	
	/*
	 * Used for setting chainModels from array of chain models (from memory).
	 */
	public void setChainModelsFromArray(ChainModel[] chainModelsArr) {
		if(chainModelsArr != null)
			for(ChainModel model : chainModelsArr)
				this.chainModels.add(model);
	}

	public ArrayList<ChainModel> getChainModels() {
		return chainModels;
	}
}
