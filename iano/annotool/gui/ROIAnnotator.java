package annotool.gui;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import annotool.AnnOutputPanel;
import annotool.Annotation;
import annotool.Annotator;
import annotool.ImgDimension;
import annotool.analysis.Utility;
import annotool.classify.SavableClassifier;
import annotool.gui.model.Extractor;
import annotool.gui.model.Selector;
import annotool.io.ChainModel;
import annotool.io.DataInput;
import annotool.io.DataInputDynamic;

public class ROIAnnotator {
	private int interval;
	private int paddingMode;
	private String exportDir = "";
	private boolean isExport = false;
	private boolean isMaximaOnly = false;

	AnnOutputPanel pnlStatus = null;
	ImageReadyPanel pnlImages = null;
	
	//predefined color masks: equivalent to or more than number of annotations;
	//Otherwise some colors may be reused.
	float colorMasks[][] = {{1.0f, 0.0f, 0.0f},{1.0f, 1.0f, 1.0f}, {0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 1.0f},
			{0.0f, 1.0f, 0.0f},{1.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 1.0f}, {1.0f, 0.0f, 1.0f}};
	//float colorMasks[][] = {{1.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 1.0f}, {0.0f, 1.0f, 0.0f}};
	int  numOfColors = colorMasks.length;
	
	//Class names to use for exporting annotation result to text file
	HashMap<String, String> classNames = null;
	
	/**
	 * 
	 * @param size : ROI Window Size
	 * @param interval : Increment for sliding ROI window over the target image
	 * @param paddingMode : Determines the mode used to pad the image for annotation near the image edges.
	 * 						Modes - ROIParameterPanel.NONE, ROIParameterPanel.SYMMETRIC
	 * @param channel : Channel information of the image
	 * @param selectedImages: List of indices for images selected for annotations
	 */
	public ROIAnnotator(int interval, int paddingMode, String channel, ArrayList<ChainModel> chainModels, 
			int[] selectedImages, String exportDir, boolean isExport, boolean isMaximaOnly, ImageReadyPanel pnlImages) {
		//Reference to gui
		this.pnlImages = pnlImages;
		this.pnlStatus = pnlImages.getOutputPanel();
		
		this.interval = interval;
		this.paddingMode = paddingMode;
		this.isExport = isExport;
		if(!"".equals(exportDir))
			this.exportDir = exportDir + "/";
		
		this.isMaximaOnly = isMaximaOnly;
		
		DataInputDynamic problem = new DataInputDynamic(Annotator.dir, Annotator.ext, channel); 
		
		for(ChainModel model : chainModels) {
			this.classNames = model.getClassNames();	//Retrieve class names to export annotated pixels
			annotate(problem, model, selectedImages);
			
			LegendDialog ld = new LegendDialog("Legends", colorMasks, classNames);
		}
	}
	
	private void annotate(DataInputDynamic problem, ChainModel model, int[] selectedImages) {
		//Get size of the roi window
		String[] roiSize = model.getImageSize().split("x");
		int roiWidth = Integer.parseInt(roiSize[0]);
		int roiHeight = Integer.parseInt(roiSize[1]);
		
		ArrayList<byte[]> data  = problem.getData();		
		for(int i = 0; i < problem.getLength(); i++) {
			boolean isSelected = false;
			for(int index=0; index < selectedImages.length; index++){
				if(selectedImages[index] == i) {
					isSelected = true; 
					break;
				}
			}
			if(isSelected)
				annotateAnImage(problem.getImagePlus(i), data.get(i), model, roiWidth, roiHeight);//If we are using ImagePlus here, why have byte[] data as well?
		}
	}
	
	/**
	 * ROI annotation of single image.
	 * 
	 * @param imp : ImagePlus object representing the image to annotate.
	 * @param data : Image data in single dimensional bytes array (width * height).
	 * @param model : Model to use for annotation.
	 */
	private void annotateAnImage(ImagePlus imp, byte[] data, ChainModel model, int roiWidth, int roiHeight) {
		ImageProcessor ip = imp.getProcessor();
		
		//If only local maxima are to be annotated, find local maxima
		int width = ip.getWidth();
		int height = ip.getHeight();
		
		float[] floatData = null;
		boolean[] isMaxima = null;
		
		if(this.isMaximaOnly) {
			floatData = new float[width * height];
			for(int i = 0; i < width*height; i++)
				floatData[i] = (float) (data[i]&0xff);
			isMaxima = Utility.getLocalMaxima(floatData, width, height, 1, 3, 3, 1);
		}
		
		//Divide the image into an array of small target images for ROI annotation		
		if(ip.getWidth() < roiWidth || ip.getHeight() < roiHeight) {
			System.out.println("ROI cannot be greater than the image");
			return;
		}
		
		byte[] subImage = new byte[roiWidth*roiHeight];
		
		int numSubImages;
		int startCol, startRow, endRow, endCol;
		if(paddingMode == ROIParameterPanel.SYMMETRIC) {
			numSubImages = ((roiWidth/2 + ip.getWidth() - 1) / interval + 1) * ((roiHeight/2 + ip.getHeight() - 1) / interval + 1);
			startCol = -roiWidth/2;
			startRow = -roiHeight/2;
			endCol = ip.getWidth() - 1;
			endRow = ip.getHeight() - 1;
		}
		else {
			numSubImages = ((ip.getWidth()-roiWidth) / interval + 1) * ((ip.getHeight() - roiHeight) / interval + 1);
			startCol = 0;
			startRow = 0;
			endCol = ip.getWidth() - roiWidth;
			endRow = ip.getHeight() - roiHeight;
		}
		
		System.out.println("Number of sub-images in the image:" + numSubImages);		
		
		//Data structure to store target patterns.
		float[][] targetROIPatterns = new float[1][];
		Annotation[] annotations = new Annotation[1];

		SavableClassifier classifier = (SavableClassifier)model.getClassifier();
		Annotator anno = new Annotator();
		int[] predictions = new int[numSubImages];
		
		int imageIndex = -1;
		for(int i = startCol; i <= endCol; i = i + interval)
		{	
			//columns
			for(int j = startRow ; j <= endRow; j = j + interval)
			{ 
				//If only local maxima are to be annotated, skip those which are not maxima
				if(this.isMaximaOnly && isMaxima != null) {
					int y = j + roiHeight / 2;
					int x = i + roiWidth / 2;
					if (!Utility.isWithinBounds(x, y, width, height))
						continue;
					else if(!isMaxima[y  * width + x])
						continue;
				}
				
				//rows
				imageIndex++;
				
				//(i,j) is the upperleft corner of the subimage
				for(int m = 0; m < roiWidth; m++)//col
					for(int n = 0; n < roiHeight; n++) //row
						//subImage[n * roiWidth + m] = data[ (n + j) * ip.getWidth() + m + i];//row major //out of bound exception?4/1/09
						subImage[n * roiWidth + m] = getSubImageData(data, m + i, n + j, ip.getWidth(), ip.getHeight(), i, j, m, n);
				
				//feature extraction on testing subimage.
				float[] features  = null;
				try {
					features = getExtractedFeaturesFromROI(subImage, roiWidth, roiHeight, model.getExtractors());
				} catch (Exception e) {
					e.printStackTrace();
					this.pnlStatus.setOutput("Feature extraction failure!");
					return;
				}
				
				//Selecting features
		        for(Selector sel : model.getSelectors()) {
			        if(sel.getSelectedIndices() != null) {
			        	features = this.selectGivenIndices(features, sel.getSelectedIndices());
			        }
		        }
		        
		        targetROIPatterns[0] =  features;
		        
		        //Initialize annotation object
		        annotations[0] = new Annotation();
		        
		        //Annotate		        
		        try {
		        	predictions[imageIndex] = anno.classifyGivenAMethod(classifier, targetROIPatterns, annotations)[0];
				} catch (Exception ex) {
					System.out.println("Classification using model failed.");
					ex.printStackTrace();
					this.pnlStatus.setOutput("Classification exception! Classifier=" + model.getClassifierName());
				}
			}//	end of j
	    } //end of i
		System.out.println("Image INdex: " + imageIndex);
		
		markResultsOnImage(imp, predictions, roiWidth, roiHeight, isMaxima, startCol, startRow, endCol, endRow);
	}
	
	private byte getSubImageData(byte[] data, int col, int row, int imgWidth, int imgHeight, int i, int j, int m, int n) {//TODO: i, j, m, n only for debugging, to be removed later
		if(row < 0)
			row = -row;
		if(col < 0)
			col = -col;
		if(row >= imgHeight)
			row = (imgHeight - 1) - (row - imgHeight);
		if(col >= imgWidth)
			col = (imgWidth - 1) - (col - imgWidth);
		
		return data[ row * imgWidth + col];
	}
	
	protected float[] getExtractedFeaturesFromROI(byte[] subImage, int width, int height, ArrayList<Extractor> extractors) throws Exception
	{
		if(extractors.size() < 1) {
			float[] features = new float[width * height];
			for (int index = 0; index < width * height; index++)
				features[index] = (float) (subImage[index]&0xff);
			return features;
		}
		
		
		String extractorName = null;
		HashMap<String, String> params = null;
		String extractorPath = null;
		
		ImgDimension dim = new ImgDimension();
    	dim.width = width;
    	dim.height = height;
    	dim.depth = 1;
    	
    	//Using array of bytes array since "extractGivenAMethod" needs bytes[][] instead of bytes
    	//byte[][] data = new byte[1][subImage.length];
    	//data[0] = subImage;
    	
    	//Using arraylist of array to pass to extractor
    	ArrayList data = new ArrayList();
    	data.add(subImage);
    	
		float[][] exFeatures = new float[extractors.size()][];
        
		int dataSize = 0;
        for(int exIndex=0; exIndex < extractors.size(); exIndex++) {
			extractorName = extractors.get(exIndex).getClassName();
        	params = extractors.get(exIndex).getParams();
        	extractorPath = extractors.get(exIndex).getExternalPath();
        	
    		exFeatures[exIndex] = new Annotator().extractGivenAMethod(extractorName, extractorPath, params, data, DataInput.GRAY8, dim)[0];//DataInput.GRAY8 need to fix this when DataInputDynamic is updated
    		dataSize += exFeatures[exIndex].length;
		}
		
        //Initialize array to hold extracted feature from all extractors
        float[] features = new float[dataSize];
        //Copy over features from multiple extractors into single dimension
        int destPos = 0;
    	for(int exIndex=0; exIndex < extractors.size(); exIndex++) {
    		System.arraycopy(exFeatures[exIndex], 0, features, destPos, exFeatures[exIndex].length);
    		destPos += exFeatures[exIndex].length;
    	}
        
		return features;
	}
	
	/**
     * Selects the features based on pre-determined set of indices for a single image
     * 
     * @param features
     * @param indices
     * @return Selected features
     */
    public float[] selectGivenIndices(float[] features, int[] indices) {
    	float[] selectedFeatures = new float[indices.length];
    	
        for (int j = 0; j < indices.length; j++) {
                selectedFeatures[j] = features[indices[j]];
        }
        return selectedFeatures;        
    }
    
    //make an overlay mask on the grayed image
    public void markResultsOnImage(ImagePlus imp, int[] predictions, int roiWidth, int roiHeight, boolean[] isMaxima,
    		int startCol, int startRow, int endCol, int endRow)
    {  
    	String imageName = imp.getTitle();
    	
    	int imageType = imp.getType();
    	if(imageType != ImagePlus.COLOR_RGB) {
    		ImageConverter ic = new ImageConverter(imp);
    		ic.convertToRGB();
    	}
    	
    	ImageProcessor ip = imp.getProcessor();
    	ImageProcessor ipOriginal = ip.duplicate();
    	ImageProcessor ipMaskOnly = ip.duplicate();

    	//for color blending
    	Color c = null;
    	float alpha = 0.6f; //transparent parameter (0: transparent; 1: opaque)
    	int[] colors = new int[3];
    	float[] fcolors = new float[3];
    	int colorLabel = 0;
    	
    	int index = 0;	
    	int width = ip.getWidth();
    	int height = ip.getHeight();
    	
    	for(int i=startCol; i <= endCol; i = i + interval)
    		for(int j=startRow ; j <= endRow; j = j + interval)
    		{
    			//Skip non-maxima if only local maxima annotated
    			if(this.isMaximaOnly && isMaxima != null) {
					int y = j + roiHeight / 2;
					int x = i + roiWidth / 2;
					if (!Utility.isWithinBounds(x, y, width, height))
						continue;
					else if(!isMaxima[y  * width + x])
						continue;
				}
    			
    			int res = predictions[index++];
    			ip.moveTo(i+roiWidth/2, j+roiHeight/2);
    			ipMaskOnly.moveTo(i+roiWidth/2, j+roiHeight/2);
	    	 
    			//get the current image color
    			ip.getPixel(i+roiWidth/2, j+roiHeight/2, colors);
    			for(int k = 0; k < colors.length; k++) fcolors[k] = (float) colors[k]/256;
	    	 
    			//blend on r, g, b
    			colorLabel = res % numOfColors;
    			fcolors[0] = colorMasks[colorLabel][0]*alpha + fcolors[0]*(1-alpha);
    			fcolors[1] = colorMasks[colorLabel][1]*alpha + fcolors[1]*(1-alpha);
    			fcolors[2] = colorMasks[colorLabel][2]*alpha + fcolors[2]*(1-alpha);
    			c = new Color(fcolors[0], fcolors[1], fcolors[2]);
    			ip.setColor(c);    			
    			ip.fillOval(i + roiWidth/2 - interval/2, j + roiHeight/2 - interval/2, interval, interval);
    			
    			//Draw the mask only
    			ipMaskOnly.setColor(new Color(colorMasks[colorLabel][0], colorMasks[colorLabel][1], colorMasks[colorLabel][2]));    			
    			ipMaskOnly.fillOval(i + roiWidth/2 - interval/2, j + roiHeight/2 - interval/2, interval, interval);
    		}
    	ImageStack st = imp.getStack();
    	st.addSlice("Original", ipOriginal);
    	st.addSlice("Mask", ipMaskOnly);
    	imp.setStack("Stack", st);
    	
    	//display the annotated image
    	imp.updateAndDraw();
    	imp.show();
    	
    	//Write prediction indices to file for each class
    	if(this.isExport) {
    		//Check if export dir exists, if not try creating it
    		File dir = new File(this.exportDir);
    		boolean dirExists = dir.exists();
    		if(!dirExists)
    			dirExists = dir.mkdirs();
    		
    		if(dirExists)
    		{
		    	for(String key : classNames.keySet()) {
		    		exportPrediction(startCol, endCol, startRow, endRow, roiWidth, roiHeight, predictions, imageName, key,
		    						width, height, isMaxima);
		    	}
    		}
    		else
    			this.pnlStatus.setOutput("Failed to create export directory.");
    	}
    }
     
    public void exportPrediction(int startCol, int endCol, int startRow, int endRow, int roiWidth, int roiHeight,
    		int[] predictions, String baseFile, String classKey, 
    		int width, int height, boolean[] isMaxima) {
    	String newLine = System.getProperty("line.separator");
    	
    	//Open file for each class : the file contains list of coordinate of annotated pixel
    	File file = new File(this.exportDir + baseFile + classKey + "-" + classNames.get(classKey));
    	
    	try {
	    	BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	    	
	    	int index = 0, res = 0, x = 0, y = 0;	
	    	for(int i=startCol; i <= endCol; i = i + interval) {
	    		for(int j=startRow ; j <= endRow; j = j + interval)
	    		{
	    			y = j + roiHeight / 2;
					x = i + roiWidth / 2;
					
	    			//Skip non-maxima if only local maxima annotated
	    			if(this.isMaximaOnly && isMaxima != null) {						
						if (!Utility.isWithinBounds(x, y, width, height))
							continue;
						else if(!isMaxima[y  * width + x])
							continue;
					}
	    			
	    			res = predictions[index++];
	    			if(classKey.equals(String.valueOf(res))) {
	    				writer.write(x + "," + y);
		    			writer.write(newLine);
	    			}
	    		}
	    	}
	    	
	    	writer.flush();
        	writer.close();
			
			this.pnlStatus.setOutput("Prediction file for '" + classNames.get(classKey) + "' exported to path " + file.getAbsolutePath());
    	}
    	catch(IOException ex) {
        	System.out.println("Exception occured while writing file: " + file.getName());
        	System.out.println("Exception: " + ex.getMessage());
        	ex.printStackTrace();
			this.pnlStatus.setOutput("Failed to write export file in directory " + this.exportDir);
        }
    }
}
