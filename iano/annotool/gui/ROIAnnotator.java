package annotool.gui;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import annotool.Annotation;
import annotool.Annotator;
import annotool.classify.SavableClassifier;
import annotool.extract.HaarFeatureExtractor;
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
	 * 						Modes - ROIParameterPanel.RESIZE, ROIParameterPanel.SYMMETRIC, ROIParameterPanel.REPLICATE
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
				if(index == i) {
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
		
		int numSubImages = ((ip.getWidth()-roiWidth) / interval + 1) * ((ip.getHeight() - roiHeight) / interval + 1);
		System.out.println("Number of sub-images in the image:" + numSubImages);
		
		Annotation[] annotations = new Annotation[numSubImages];
		
		//Data structure to store target patterns.
		float[][] targetROIPatterns = new float[numSubImages][];
		
		int imageIndex = -1;
		for(int i = 0; i < ip.getWidth() - roiWidth + 1; i = i + interval)             //or i <= ip.getWidth() - roiWidth
		{	
			//columns
			for(int j = 0 ; j < ip.getHeight() - roiHeight + 1; j = j + interval)
			{ 
				//rows
				imageIndex++;
				
				//(i,j) is the upperleft corner of the subimage
				for(int m = 0; m < roiWidth; m++)//col
					for(int n = 0; n < roiHeight; n++) //row
						subImage[n * roiWidth + m] = data[ (n + j) * ip.getWidth() + m + i];//row major //out of bound exception?4/1/09
				
				//feature extraction on testing subimage.
				float[] features  = getExtractedFeaturesFromROI(subImage, roiWidth, roiHeight, model.getExtractors());
				
				//Selecting features
		        for(Selector sel : model.getSelectors()) {
			        if(sel.getSelectedIndices() != null) {
			        	System.out.println("Selecting features with " + sel.getName());
			        	features = this.selectGivenIndices(features, sel.getSelectedIndices());
			        }
		        }
		        
		        targetROIPatterns[imageIndex] =  features;
		        
		        //Initialize annotation objects
		        annotations[imageIndex] = new Annotation();
			}//	end of j
	    } //end of i
		
		//Annotate
		System.out.println("Classifying with " + model.getClassifierName());
		SavableClassifier classifier = (SavableClassifier)model.getClassifier();
        boolean supportsProb = classifier.doesSupportProbability();
        int[] predictions = null;
        try {
        	predictions = (new Annotator()).classifyGivenAMethod(classifier, targetROIPatterns, annotations);
		} catch (Exception ex) {
			System.out.println("Classification using model failed.");
			ex.printStackTrace();
		}
		
		System.out.println("Marking annotation results");
		markResultsOnImage(imp, predictions, roiWidth, roiHeight);
	}
	
	protected float[] getExtractedFeaturesFromROI(byte[] subImage, int width, int height, ArrayList<Extractor> extractors)
	{
		if(extractors.size() < 1) {
			float[] features = new float[width * height];
			for (int index = 0; index < width * height; index++)
				features[index] = (float) (subImage[index]&0xff);
			return features;
		}
		
		
		//TODO: The codes below need to be generic
		String extractorName = null;
		HashMap<String, String> params = null;
		
		int validExtractors = 0;	//Only HAAR valid currently
        for(Extractor ex : extractors) {
        	if(ex.getName().equalsIgnoreCase("HAAR"))
        		validExtractors++;
        }
        float[][] exFeatures = new float[validExtractors][];
        
		int dataSize = 0;
        for(int exIndex=0; exIndex < extractors.size(); exIndex++) {
			extractorName = extractors.get(exIndex).getName();
        	params = extractors.get(exIndex).getParams();
        	
        	if(extractorName.equalsIgnoreCase("HAAR")) {
        		int level = 2; //default
        		if(params != null && params.containsKey(HaarFeatureExtractor.LEVEL_KEY))
        			level = Integer.parseInt(params.get(HaarFeatureExtractor.LEVEL_KEY));
        		
        		exFeatures[exIndex] = (new HaarFeatureExtractor(level, subImage, width, height)).getFeatures()[0];
        		dataSize += exFeatures[exIndex].length;
        	}
        	else
        		System.out.println("Feature Extractor Methods Not Supported for ROI Annotation");
		}
		
        //Initialize array to hold extracted feature from all extractors
        float[] features = new float[dataSize];
        //Copy over features from multiple extractors into single dimension
        int destPos = 0;
    	for(int exIndex=0; exIndex < validExtractors; exIndex++) {
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
    public void markResultsOnImage(ImagePlus imp, int[] predictions, int roiWidth, int roiHeight)
    {    	
    	ImageProcessor ip = imp.getProcessor();

    	//for color blending
    	Color c = null;
    	float alpha = 0.6f; //transparent parameter (0: transparent; 1: opaque)
    	int[] colors = new int[3];
    	float[] fcolors = new float[3];
    	int colorLabel = 0;

    	int index = 0;	
    	for(int i=0; i <= ip.getWidth()-roiWidth; i = i + interval)
    		for(int j=0 ; j <= ip.getHeight()-roiHeight; j = j + interval)
    		{
    			int res = predictions[index++];
    			ip.moveTo(i+roiWidth/2, j+roiHeight/2);
	    	 
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

    			/*
              	draw a short cross of 10 pixel by 10 pixel
	    	 	ip.drawLine(i+ROISIZE/2, j+ROISIZE/2-5, i+ROISIZE/2, j+ROISIZE/2+5);
	    	 	ip.drawLine(i+ROISIZE/2-5, j+ROISIZE/2, i+ROISIZE/2+5, j+ROISIZE/2);
	    	 	ip.drawString(String.valueOf(res));
    			 */
    			//how to gray the original color? 
    			//define the bounding box of the area to fill.
    			/*int xcor[] = {i+ROISIZE/2-INCREMENT/2,i+ROISIZE/2-INCREMENT/2,i+ROISIZE/2+INCREMENT/2, i+ROISIZE/2+INCREMENT/2 };
	    	 	int ycor[] = {j+ROISIZE/2-INCREMENT/2,j+ROISIZE/2+INCREMENT/2,j+ROISIZE/2+INCREMENT/2, j+ROISIZE/2-INCREMENT/2 };
	    	 	java.awt.Polygon aoi = new java.awt.Polygon(xcor, ycor, 4);
	    	 	ip.fillPolygon(aoi);*/
    			//ip.drawRect(i+ROISIZE/2-INCREMENT/2,j+ROISIZE/2-INCREMENT/2, INCREMENT, INCREMENT);
    			ip.fillOval(i + roiWidth/2 - interval/2, j + roiHeight/2 - interval/2, interval, interval);
    		}
	  
    	//display the annotated image
    	imp.updateAndDraw();
    	imp.show();
    }
}
