package annotool.gui.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import annotool.Annotation;
import annotool.Annotator;
import annotool.classify.SavableClassifier;
import annotool.gui.AnnOutputPanel;
import annotool.gui.ImageReadyPanel;
import annotool.gui.ROIParameterPanel;
import annotool.io.ChainModel;
import annotool.io.DataInput;

/**
 * This class is responsible for loading saved model and applying model.
 * 
 * @author Santosh
 *
 */
public class ModelLoader implements Runnable {
	AnnOutputPanel pnlStatus = null;
	ImageReadyPanel pnlImages = null;
	
	ArrayList<ChainModel> chainModels = null;
	
	private Thread thread = null;
	
	private Annotation[][] annotations = null;
	HashMap<String, String> classNames = null;
	private String[] modelLabels = null;
	private boolean[] supportsProb = null;
	private boolean isBinary;
	
	//Used by thread
	private boolean loadMode = false; 	//Determines if the thread should execute load mode or apply mode
	private File[] files = null;		//Multiple files selected case (for image annotation/classification)
	private File file = null;			//Single file selected case (for ROI annotation)
	
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
	
	public void load() {
		loadMode = true;
		
		if (thread == null)  {
            thread = new Thread(this);
            thread.start();
        }
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
	public boolean loadModels() {
		pnlStatus.setOutput("Loading model(s)...");
		
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
	 * Loads model from a single file.
	 * 
	 * @return True if model was loaded, false if canceled or invalid model
	 */
	public boolean loadModel() {
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
		if(loadMode) {
			pnlImages.setButtonsEnabledOnModelLoad(false); //Re-enabled only on success
			if(Annotator.output.equals(Annotator.AN)) {
				if(loadModels())
					pnlImages.setButtonsEnabledOnModelLoad(true);
			}
			else if(Annotator.output.equals(Annotator.ROI)) {
				if(loadModel())
					pnlImages.setButtonsEnabledOnModelLoad(true);
			}
		}
		else
		{
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
			pnlImages.setButtonsEnabled(true);
		}
		
		thread = null;
	}
	
	/**
	 * Iterates through the arraylist of loaded models and applies each one to the current image set
	 */
	public void applyModel() {
		loadMode = false;
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
		String exportDir = pnlROIParam.getExportDir();
		boolean isExport = pnlROIParam.isExport();
		boolean isMaximaOnly = pnlROIParam.isLocalMaximaOnly();
		
		int[] selectedImages = pnlImages.getTablePanel().getAnnotationTable().getSelectedRows();
		
		ROIAnnotator roiAnnotator = new ROIAnnotator(interval, mode, channel, chainModels, selectedImages, exportDir, isExport, isMaximaOnly,  this.pnlImages);
	}
	
	/**
	 * Image Classification/Annotation.
	 */
	private void classify() {
		Annotator anno = new Annotator();
		
		//------ read image data from the directory ------------//
        //DataInput problem = new DataInput(Annotator.dir, Annotator.ext, channel);
        DataInput problem = pnlImages.getTablePanel().getProblem();
        //If new channel is selected than the one already being used in problem,
        //change problem channel
        if(!problem.getChannel().equals(pnlImages.getSelectedChannel()))
        	problem.setChannel(pnlImages.getSelectedChannel());
        
      	//TODO: Use this to validate model file
        int imgWidth, 
        	imgHeight;
        
        try {
        	imgWidth = problem.getWidth();
        	imgHeight = problem.getHeight();
        }catch(Exception ex) {
        	pnlStatus.setOutput("ERROR: Failed to get width and height from problem.");
        	ex.printStackTrace();
        	return;
        }
        
        //Initialize structure to store annotation results
        final int numModels = chainModels.size();
        final int problemSize;
        try {
        	problemSize = problem.getLength();
        } catch (Exception ex) {
        	pnlStatus.setOutput("ERROR: Failed to get problem size.");
        	ex.printStackTrace();
        	return;
        }
        annotations = new Annotation[numModels][problemSize];
        for (int i = 0; i < numModels; i++) {
            for (int j = 0; j < problemSize; j++) {
                annotations[i][j] = new Annotation();
            }
        }
        //Also, initialize a list of annotation labels to use for updated table column
        modelLabels = new String[numModels];
        supportsProb = new boolean[numModels];
        
        isBinary = true;
        
        for(int modelIndex = 0; modelIndex < numModels; modelIndex++) {
        	pnlStatus.setOutput("Applying model: " + (modelIndex + 1) + " of " + numModels);
        	
        	ChainModel model = chainModels.get(modelIndex);
        	
        	isBinary = isBinary && model.isBinary();
        	
        	modelLabels[modelIndex] = model.getLabel();
        	
        	//If image size in the model is not same as the problem size, display message
        	//Could be 3D such as 5*5*4
        	if(!model.getImageSize().startsWith(imgWidth + "x" + imgHeight)) {
        		pnlStatus.setOutput("Image size mismatch between model and problem. Model: " + (modelIndex + 1));
        		return;
        	}
        	
	        pnlStatus.setOutput("Extracing features ... ");
	        
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
		pnlImages.getTablePanel().updateAnnotationTable(annotations, modelLabels, supportsProb, isBinary);
		
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
	
	public boolean isBinary() {
		return isBinary;
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

	public void setFiles(File[] files) {
		this.files = files;
	}

	public void setFile(File file) {
		this.file = file;
	}
}
