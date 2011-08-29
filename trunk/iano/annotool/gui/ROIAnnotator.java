package annotool.gui;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import annotool.Annotation;
import annotool.Annotator;
import annotool.ImgDimension;
import annotool.classify.SavableClassifier;
import annotool.gui.model.Extractor;
import annotool.gui.model.Selector;
import annotool.io.ChainModel;
import annotool.io.DataInputDynamic;

public class ROIAnnotator {
	private int interval;
	private int paddingMode;
	
	//predefined color masks: equivalent to or more than number of annotations;
	//Otherwise some colors may be reused.
	float colorMasks[][] = {{1.0f, 0.0f, 0.0f},{1.0f, 1.0f, 1.0f}, {0.0f, 0.0f, 0.0f}};
	int  numOfColors = colorMasks.length;
	
	/**
	 * 
	 * @param size : ROI Window Size
	 * @param interval : Increment for sliding ROI window over the target image
	 * @param paddingMode : Determines the mode used to pad the image for annotation near the image edges.
	 * 						Modes - ROIParameterPanel.NONE, ROIParameterPanel.SYMMETRIC
	 * @param channel : Channel information of the image
	 * @param selectedImages: List of indices for images selected for annotations
	 */
	public ROIAnnotator(int interval, int paddingMode, String channel, ArrayList<ChainModel> chainModels, int[] selectedImages) {
		this.interval = interval;
		this.paddingMode = paddingMode;
		
		DataInputDynamic problem = new DataInputDynamic(Annotator.dir, Annotator.ext, channel); 
		
		for(ChainModel model : chainModels)
			annotate(problem, model, selectedImages);
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
				annotateAnImage(problem.getImagePlus(i), data.get(i), model, roiWidth, roiHeight);
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
		//Divide the image into an array of small target images for ROI annotation
		ImageProcessor ip = imp.getProcessor();
		
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
					//TODO: show error message to user
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
				}
			}//	end of j
	    } //end of i
		System.out.println("Image INdex: " + imageIndex);
		
		markResultsOnImage(imp, predictions, roiWidth, roiHeight, startCol, startRow, endCol, endRow);
	}
	
	private byte getSubImageData(byte[] data, int col, int row, int imgWidth, int imgHeight, int i, int j, int m, int n) {
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
    	byte[][] data = new byte[1][subImage.length];
    	data[0] = subImage;
    	
		float[][] exFeatures = new float[extractors.size()][];
        
		int dataSize = 0;
        for(int exIndex=0; exIndex < extractors.size(); exIndex++) {
			extractorName = extractors.get(exIndex).getClassName();
        	params = extractors.get(exIndex).getParams();
        	extractorPath = extractors.get(exIndex).getExternalPath();
        	
    		exFeatures[exIndex] = new Annotator().extractGivenAMethod(extractorName, extractorPath, params, data, dim)[0];
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
    public void markResultsOnImage(ImagePlus imp, int[] predictions, int roiWidth, int roiHeight, int startCol, int startRow, int endCol, int endRow)
    {    
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
    	for(int i=startCol; i <= endCol; i = i + interval)
    		for(int j=startRow ; j <= endRow; j = j + interval)
    		{
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
    }
}
