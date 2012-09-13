package annotool.gui.model;

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

import annotool.Annotation;
import annotool.Annotator;
import annotool.ImgDimension;
import annotool.analysis.Utility;
import annotool.classify.SavableClassifier;
import annotool.gui.AnnOutputPanel;
import annotool.gui.ImageReadyPanel;
import annotool.gui.LegendDialog;
import annotool.gui.ROIParameterPanel;
import annotool.io.ChainModel;
import annotool.io.DataInput;

/**
 * ROI annotator executes the algorithms in ROI annotation mode.
 * 
 * 8/10/2012: Add 3D ROI:
 *   Note: 3D ROI with a depth of 1 is treated as 2D ROI.
 *   ArrayList can be a 3D data.
 *   
 *   interval -> z-interval? The same for now.
 *   mark images on result if 2D
 *   only export to file if 3D
 */
public class ROIAnnotator {
	private int interval;
	private int zInterval;
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
		this.zInterval = interval; //simplified for now
		this.paddingMode = paddingMode;
		this.isExport = isExport;
		if(!"".equals(exportDir))
			this.exportDir = exportDir + "/";
		
		this.isMaximaOnly = isMaximaOnly;
		
		DataInput problem = pnlImages.getTablePanel().getProblem();	//Get the problem already loaded
		if(!problem.getChannel().equals(channel))
        	problem.setChannel(channel);
		
		for(ChainModel model : chainModels) {
			this.classNames = model.getClassNames();	//Retrieve class names to export annotated pixels
			annotate(problem, model, selectedImages);
			
			LegendDialog ld = new LegendDialog("Legends", colorMasks, classNames);
		}
	}
	
	private void annotate(DataInput problem, ChainModel model, int[] selectedImages) {
		//Get size of the roi window
		String[] roiSize = model.getImageSize().split("x");
		int roiWidth = Integer.parseInt(roiSize[0]);
		int roiHeight = Integer.parseInt(roiSize[1]);
		int roiDepth = 1;
		if (roiSize.length == 3) //3D 
			roiDepth = Integer.parseInt(roiSize[2]);
		System.out.println("roiWidth:"+roiWidth+"roiHeight "+ roiHeight+" roiDepth"+roiDepth);
		
		//ArrayList data  = null;
		int totalImages = 0;
		try {
			//data = problem.getData();
			totalImages = problem.getLength();
		} catch(Exception ex) {
			ex.printStackTrace();
			pnlStatus.setOutput(ex.getMessage());
			return;
		}
		
		for(int i = 0; i < totalImages; i++) {
			boolean isSelected = false;
			//check if this image is selected.
			for(int index=0; index < selectedImages.length; index++){
				if(selectedImages[index] == i) {
					isSelected = true; 
					break;
				}
			}
			if(isSelected)
			{
			  try
			  {
				//annotateAnImage(problem.getImagePlus(i), data.get(i), model, roiWidth, roiHeight, roiDepth, problem.getImageType());
				  annotateAnImage(problem, i, model, roiWidth, roiHeight, roiDepth);
			  }catch(Exception e)
			  {
				e.printStackTrace();  
				pnlStatus.setOutput("Error in annotating "+ i +"th image.");
				System.err.println(e.getMessage());
			  }
			}
		}
	}
	
	/**
	 * ROI annotation of single image.
	 * 
	 * @param imp : ImagePlus object representing the image to annotate.
	 * @param data : Image data in single dimensional bytes array (width * height).
	 * @param model : Model to use for annotation.
	 */
	//private void annotateAnImage(ImagePlus imp, Object datain, ChainModel model, int roiWidth, int roiHeight, int roiDepth, int imageType) {
	private void annotateAnImage(DataInput problem, int bigImageIndex, ChainModel model, int roiWidth, int roiHeight, int roiDepth) throws Exception 
	{
		ImagePlus imp = problem.getImagePlus(bigImageIndex);
		int imageType = problem.getImageType();
		ImageProcessor ip = imp.getProcessor(); //first slice if 3D?
		int width = ip.getWidth();
		int height = ip.getHeight();
		int stackSize = imp.getImageStackSize();
		boolean[] isMaxima = null;
	
		//If only local maxima are to be annotated, find local maxima
		if(this.isMaximaOnly) {
				int ch = 0; //default "r"
				if (problem.getChannel().equals("g"))  ch = 1;
				else if (problem.getChannel().equals("b")) ch = 2;
				isMaxima = Utility.getLocalMaxima(imp, ch, 3, 3, 1);
		}
		
		//Divide the image into an array of small target images for ROI annotation		
		if(ip.getWidth() < roiWidth || ip.getHeight() < roiHeight || imp.getImageStackSize() < roiDepth) {
			System.out.println("ROI cannot be greater than the image");
			return;
		}
		
		Object subImage = null;
		if(imageType == DataInput.GRAY8 || imageType == DataInput.COLOR_RGB) 
			subImage = new byte[roiWidth*roiHeight];
		else if(imageType == DataInput.GRAY16)
			subImage = new float[roiWidth*roiHeight];
		else if(imageType == DataInput.GRAY32)
			subImage = new int[roiWidth*roiHeight];
		ArrayList<Object> ThreeDsubimage = null; 
		if (roiDepth > 1)
		   ThreeDsubimage = new ArrayList<Object>(roiDepth);

		int numSubImages;
		int startCol, startRow, endRow, endCol;
		int startSlice, endSlice;
		if(paddingMode == ROIParameterPanel.SYMMETRIC) {
			numSubImages = ((roiWidth/2 + ip.getWidth() - 1) / interval + 1)
			            * ((roiHeight/2 + ip.getHeight() - 1) / interval + 1)
			            * ((roiDepth/2 + imp.getImageStackSize() - 1) / zInterval + 1);
			startCol = -roiWidth/2;
			startRow = -roiHeight/2;
			startSlice = -roiDepth/2;
			endCol = ip.getWidth() - 1;
			endRow = ip.getHeight() - 1;
			endSlice = imp.getImageStackSize() -1;
		}
		else {
			numSubImages = ((ip.getWidth()-roiWidth) / interval + 1) 
			         * ((ip.getHeight() - roiHeight) / interval + 1)
			         * ((imp.getImageStackSize() - roiDepth) / zInterval + 1);
			startCol = 0;
			startRow = 0;
			startSlice = 0;
			endCol = ip.getWidth() - roiWidth;
			endRow = ip.getHeight() - roiHeight;
			endSlice = imp.getImageStackSize() - roiDepth;
		}
		
		System.out.println("Number of sub-images in the image:" + numSubImages);		
		
		//Data structure to store target patterns.
		float[][] targetROIPatterns = new float[1][];
		Annotation[] annotations = new Annotation[1];
		SavableClassifier classifier = (SavableClassifier)model.getClassifier();
		Annotator anno = new Annotator();
		int[] predictions = new int[numSubImages];
		Object datain = null; //complete slice of the given image
		
		int imageIndex = -1;
		for(int z = startSlice; z <= endSlice; z= z + zInterval)
		{ 
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
				if (roiDepth > 1)
					   ThreeDsubimage.clear();
				//System.out.println("Working on subImage: " + imageIndex);

				for(int p = 0; p < roiDepth; p++)
                { 
				  int zindex = z; 	
                  if (z < 0)  zindex = -z; //symmetric
                  else if (z >= stackSize) zindex = (stackSize - 1) - (z - stackSize);
                  //slice# start from 1
                  ip = imp.getStack().getProcessor(zindex + 1 + p);
                  //(i,j) is the upperleft corner of the subimage
                  //get that stack
                  datain = problem.getData(bigImageIndex, zindex+1+p); //if 2D, 2nd para is 1.
				  for(int m = 0; m < roiWidth; m++)//col
					for(int n = 0; n < roiHeight; n++) //row
						if(imageType == DataInput.GRAY8 || imageType == DataInput.COLOR_RGB) 
							((byte[]) subImage)[n * roiWidth + m] = (Byte)getSubImageData(datain, m + i, n + j, ip.getWidth(), ip.getHeight(), imageType);
						else if(imageType == DataInput.GRAY16)
							((int[]) subImage)[n * roiWidth + m] = (Integer)getSubImageData(datain, m + i, n + j, ip.getWidth(), ip.getHeight(), imageType);
						else if(imageType == DataInput.GRAY32)
							((float[]) subImage)[n * roiWidth + m] = (Float)getSubImageData(datain, m + i, n + j, ip.getWidth(), ip.getHeight(), imageType);
				
			 	  if(roiDepth > 1)
				   ThreeDsubimage.add(subImage);
                }
			
				//feature extraction on testing subimage.
				float[] features  = null;
				try {
				  if (roiDepth == 1)	
					features = getExtractedFeaturesFromROI(subImage, roiWidth, roiHeight, roiDepth, model.getExtractors(), imageType);
				  else
					features = getExtractedFeaturesFromROI(ThreeDsubimage, roiWidth, roiHeight, roiDepth, model.getExtractors(), imageType);
				  
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
	    } //end of z
		System.out.println("Image INdex: " + imageIndex);

		//visualize and output
		if(stackSize == 1) //2D
		 markResultsOnImage(imp, predictions, roiWidth, roiHeight, isMaxima, startCol, startRow, endCol, endRow);
		
		exportToFile(imp, predictions, roiWidth, roiHeight, roiDepth, isMaxima,
		    		startCol, startRow, endCol, endRow, startSlice, endSlice);
		 
	}
	
	private Object getSubImageData(Object data, int col, int row, int imgWidth, int imgHeight, int imageType) {
		if(row < 0)
			row = -row;
		if(col < 0)
			col = -col;
		if(row >= imgHeight)
			row = (imgHeight - 1) - (row - imgHeight);
		if(col >= imgWidth)
			col = (imgWidth - 1) - (col - imgWidth);
		
		int index = row * imgWidth + col;
		
		if(imageType == DataInput.GRAY8 || imageType == DataInput.COLOR_RGB) 
			return ((byte[]) data)[index];
		else if(imageType == DataInput.GRAY16)
			return ((int[]) data)[index];
		else if(imageType == DataInput.GRAY32)
			return ((float[]) data)[index];
		else
			return null;
	}
	
	protected float[] getExtractedFeaturesFromROI(Object subImage, int width, int height, int depth, ArrayList<Extractor> extractors, int imageType) throws Exception
	{
		/*if(extractors.size() < 1) {
			float[] features = new float[width * height];
			for (int index = 0; index < width * height; index++)
				features[index] = (float) (subImage[index]&0xff);
			return features;
		}*/
		
		
		String extractorName = null;
		HashMap<String, String> params = null;
		String extractorPath = null;
		
		//2D or 3D ROI dimension
		ImgDimension dim = new ImgDimension();
    	dim.width = width;
    	dim.height = height;
    	dim.depth = depth;
    	
    	//Using array of bytes array since "extractGivenAMethod" needs bytes[][] instead of bytes
    	//byte[][] data = new byte[1][subImage.length];
    	//data[0] = subImage;
    	
    	//Using arraylist of array to pass to extractor
    	//Just one subimage here.
    	ArrayList data = new ArrayList();
    	data.add(subImage);
    	
    	if(extractors.size() < 1) {
    		return (new Annotator().extractGivenAMethod(null, null, null, data, imageType, dim)[0]);
    	}
    	
		float[][] exFeatures = new float[extractors.size()][];
        
		int dataSize = 0;
        for(int exIndex=0; exIndex < extractors.size(); exIndex++) {
			extractorName = extractors.get(exIndex).getClassName();
        	params = extractors.get(exIndex).getParams();
        	extractorPath = extractors.get(exIndex).getExternalPath();
        	
    		exFeatures[exIndex] = new Annotator().extractGivenAMethod(extractorName, extractorPath, params, data, imageType, dim)[0];//DataInput.GRAY8 need to fix this when DataInputDynamic is updated
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
    }
    
    private void exportToFile(ImagePlus imp,int[] predictions, int roiWidth, int roiHeight, int roiDepth, boolean[] isMaxima,
    		int startCol, int startRow, int endCol, int endRow, int startSlice, int endSlice)
    {
    	//Write prediction indices to file for each class
    	if(this.isExport) {
    		//Check if export dir exists, if not try creating it
    		File dir = new File(this.exportDir);
    		boolean dirExists = dir.exists();
    		if(!dirExists)
    			dirExists = dir.mkdirs();
    		if(dirExists)
    		{
    			String imageName = imp.getTitle();
    			int width = imp.getProcessor().getWidth();
    			int height = imp.getProcessor().getHeight();
    			int stackSize = imp.getImageStackSize();
		    	for(String key : classNames.keySet()) {
		    		exportPrediction(startCol, endCol, startRow, endRow, startSlice, endSlice, roiWidth, roiHeight, roiDepth, predictions, imageName, key,
		    						width, height, stackSize, isMaxima);
		    	}
    		}
    		else
    			this.pnlStatus.setOutput("Failed to create export directory.");
    	}
    }
     
    //write the center coordinate of the voxel coressponding to the prediction.
    // Coordinates are 0-started?!
    public void exportPrediction(int startCol, int endCol, int startRow, int endRow, int startSlice, int endSlice, int roiWidth, int roiHeight, int roiDepth,
    		int[] predictions, String baseFile, String classKey, 
    		int width, int height, int stackSize, boolean[] isMaxima) {
    	
    	String newLine = System.getProperty("line.separator");
    	
    	//Open file for each class : the file contains list of coordinate of annotated pixel
    	File file = new File(this.exportDir + baseFile + classKey + "-" + classNames.get(classKey));
    	
    	try {
	    	BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	    	
	    	int index = 0, res = 0, x = 0, y = 0, z = 0;	
	    	for(int k = startSlice; k <= endSlice; k = k+ zInterval)
	    	{
	    	  for(int i=startCol; i <= endCol; i = i + interval) 
	    	  {
	    		for(int j=startRow ; j <= endRow; j = j + interval)
	    		{
	    		    z = k + roiDepth / 2;   //if roiDepth=1, then z=k
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
	    				if(stackSize > 1 ) //3D, including roiDepth = 1
	    				 //can accomodate Vaa3D landmark file here.	
		    			   writer.write(x + "," + y + "," + z);
	    				else
	    				   writer.write(x + "," + y);
		    			writer.write(newLine);
	    			}
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
