package annotool;

import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;

import annotool.classify.*;
import annotool.extract.HaarFeatureExtractor;
import annotool.io.*;
import annotool.select.FeatureSelector;
import annotool.select.mRMRFeatureSelector;
import ij.*;
import ij.process.*;

/*
 * The annotating mode that does Region of Interest Annotation.
 * It inherits methods and properties from Annotator.
 * 
 * Bug to be fixed 03242010 for mRMR feature selection
 */
public class AnnROIAnnotator extends Annotator {

	//default window size for Region of Interest (adjustable later)
	public final static String DEFAULT_ROIDIM = "100"; //size is 100*100 
	public final static String DEFAULT_ROIINC = "10"; //the intensity of annotation. If 1, then annotate every pixel.

	public static String roidim = System.getProperty("roidim", DEFAULT_ROIDIM);
	public static String roiinc = System.getProperty("roiinc", DEFAULT_ROIDIM);
	
	int ROISIZE; //= Integer.parseInt(roidim); 
	int INCREMENT; //= Integer.parseInt(roiinc); 

	//predefined color masks: equivalent to or more than number of annotations;
	//Otherwise some colors may be reused.
	float colorMasks[][] = {{1.0f, 0.0f, 0.0f},{1.0f, 1.0f, 1.0f}, {0.0f, 0.0f, 0.0f}};
	int  numOfColors = colorMasks.length;
	
	public void getROIParameters()
	{
		ROISIZE = Integer.parseInt(roidim); 
		INCREMENT = Integer.parseInt(roiinc); 
	}
	
	public AnnROIAnnotator()
	{
		getROIParameters(); //get updated parameters (GUI might change it)
		
		DataInput trainingProblem = new DataInput(dir, ext, channel);
		DataInputDynamic testingProblem = new DataInputDynamic(testdir, testext, channel); 
		
		int[] resArr = new int[2];
		int[][] trainingTargets = readTargets(trainingProblem, targetFile, resArr, null);  
		float[][] trainingfeatures = extractGivenAMethod(featureExtractor, null, trainingProblem); //data,length, width, height);
		int numoffeatures = getNumberofFeatures();
	    int incomingDim = trainingfeatures[0].length;

	    //important: error checking. Don't go further if training images are not ROI size. 
	    if (incomingDim != ROISIZE*ROISIZE)
	    {
	       System.err.println("Sizes of training images are incorrect for ROI annotation. ");
	       //IJ.showMessage("Sizes of training images are incorrect for ROI annotation. ");
	       javax.swing.JOptionPane.showMessageDialog(null,"Training image size incorrect for ROI");
	       return;
	    }
	    
	    //select training features
		if (featureSelector.equalsIgnoreCase("None"))
			numoffeatures = incomingDim;
		boolean discrete = Boolean.parseBoolean(discreteFlag);
		float   th = Float.parseFloat(threshold);
		Classifier classifier = null;
		if (classifierChoice.equalsIgnoreCase("SVM"))
				classifier = new SVMClassifier(numoffeatures, svmpara);
		else if (classifierChoice.equalsIgnoreCase("LDA"))
				classifier = new LDAClassifier();
	
		//feature selection on training images
		if (featureSelector.equalsIgnoreCase("mRMR-MIQ") || featureSelector.equalsIgnoreCase("mRMR-MID"))
		{
			FeatureSelector selector = (new mRMRFeatureSelector(trainingfeatures, trainingTargets[0], trainingfeatures.length, incomingDim, numoffeatures, featureSelector, discrete, th));
			trainingfeatures = selector.selectFeatures(trainingfeatures, trainingTargets[0]); //override the original features
			annotateImage(trainingfeatures, trainingTargets, testingProblem, classifier, selector.getIndices());	
		}
		else
			annotateImage(trainingfeatures, trainingTargets, testingProblem, classifier, null);
	}

	void annotateImage(float[][] trainingfeatures, int[][] trainingTargets, DataInputDynamic testingProblem, Classifier classifier, int[] indices)
	{
		java.util.ArrayList<byte[]> data  = testingProblem.getData();
		for(int i = 0; i<testingProblem.getLength(); i++)
		{
			//System.out.println("Now annotating image "+i);	
			annotateAnImage(trainingfeatures, trainingTargets, testingProblem.getImagePlus(i), data.get(i), classifier, indices);
		}
	}
	
	/*
	 *  float[][]  training patterns  (training targets are instance variables.
	 *  ImagePlus  for getting image related information and display results 
	 *  byte[]     extracted pixel data 
	 *  Classifier classification method
	 *  int[]      indices for feature selection if used, null if no feature selection  
	 */
	void annotateAnImage(float[][] trainingfeatures, int[][] trainingTargets, ImagePlus imp, byte[] data, Classifier classifier, int[] indices)
	{
		//divide the image into an array of small subtesting images
		ImageProcessor ip = imp.getProcessor();
		byte[] subimage = new byte[ROISIZE*ROISIZE];

		int numofsubimages = ((ip.getWidth()-ROISIZE)/INCREMENT+1) * ((ip.getHeight()-ROISIZE)/INCREMENT +1);
		System.out.println("Number of subimages in the image:"+numofsubimages);
		int numoffeatures;
		if (indices!=null)
			numoffeatures = indices.length;
		else
			numoffeatures = ROISIZE*ROISIZE;
		//data structure to store testing patterns.
		float[][] testingROIPatterns = new float[numofsubimages][];
	
		int imageIndex = -1;
		for(int i=0; i<ip.getWidth()-ROISIZE+1; i = i+ INCREMENT)
		{	//columns
			for(int j=0 ; j< ip.getHeight()-ROISIZE+1; j = j+ INCREMENT)
			{ //rows
				imageIndex++;
				//(i,j) is the upperleft corner of the subimage
				for(int m=0; m<ROISIZE; m++)//col
					for(int n=0; n<ROISIZE; n++) //row
						subimage[n*ROISIZE+m] = data[(n+j)*ip.getWidth() + m+i];//row major //out of bound exception?4/1/09
				//feature extraction on testing subimage.
				float[] features = getExtractedFeaturesFromROI(subimage, ROISIZE, ROISIZE); 
				//feature selection
				if (featureSelector.equalsIgnoreCase("mRMR-MIQ") || featureSelector.equalsIgnoreCase("mRMR-MID"))
				{
					//use the same index from training features since there is no testing targets for local annotation!
					//assuming that the features are also in ROI range.
					//Will get exception if training images are not of right size. 03/2010
					//Todo: Bug: what if the features on testing images are NOT discretized but training images are? 03/24/2010
					float[] selectedFeatures = new float[numoffeatures];
					for(int l=0; l<numoffeatures; l++)
						selectedFeatures[l] = features[indices[l]];
					features = selectedFeatures;
				}

				testingROIPatterns[imageIndex] =  features;
				//System.out.println("i"+i+"j"+j+"imageIndex:"+imageIndex);
			}//	end of j
	    } //end of i
		
		//apply the training model, cast an ArrayList of one D array to a two D array.
		//would be more space efficient if Validator separates train() and test() so only one array is needed.
		int[] predictions = null;
		try {
		  predictions = (new Validator()).classify(trainingfeatures, testingROIPatterns, trainingTargets[0], classifier);
		}catch(Exception e)
		{
			System.err.println(e.getMessage());
		}
		
		
		markResultsOnImage(imp, predictions);
	}

	//make an overlay mask on the grayed image.  03.11.2010
    public void markResultsOnImage(ImagePlus imp, int[] predictions)
    {
      ImageProcessor ip = imp.getProcessor();

      //for color blending
      Color c = null;
      float alpha = 0.6f; //transparent parameter (0: transparent; 1: opaque)
      int[] colors = new int[3];
      float[] fcolors = new float[3];
  	  int colorLabel = 0;

      int index = 0;	
	  for(int i=0; i<ip.getWidth()-ROISIZE; i = i+INCREMENT)
	     for(int j=0 ; j< ip.getHeight()-ROISIZE; j = j+INCREMENT)
	     {
	    	 int res = predictions[index++];
	    	 ip.moveTo(i+ROISIZE/2, j+ROISIZE/2);
	    	 
	    	 //get the current image color
	    	 ip.getPixel(i+ROISIZE/2, j+ROISIZE/2,colors);
	    	 for(int k=0; k<colors.length; k++) fcolors[k] = (float) colors[k]/256;
	    	 
	    	 //blend on r, g, b
	    	 colorLabel = res%numOfColors;
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
	    	 ip.fillOval(i+ROISIZE/2-INCREMENT/2,j+ROISIZE/2-INCREMENT/2, INCREMENT, INCREMENT);
	     }
	  //display the annotated image, where? added result visual panel? pop up?
	  imp.updateAndDraw();
	  imp.show();
    }

	
	protected float[] getExtractedFeaturesFromROI(byte[] subimage, int wid, int hei)
	{
		if (featureExtractor.equals("HAAR"))
		{	
			float[][] features = (new HaarFeatureExtractor(getWavletLevel(), subimage, wid, hei)).getFeatures();
			return features[0];
		}
		if (featureExtractor.equals("NONE"))
		{	
			float[] features = new float[wid*hei];
			for (int j=0; j < wid*hei; j++)
				features[j] = (float) (subimage[j]&0xff);
			return features;
		}	
		else
		{
			System.out.println("Feature Extractor Methods Not Supported for ROI Annotation");
			return null;
		}
		/* 3D ROI to be added: for a particular stack? for all stacks? for a cube?
		else if (featureExtractor.equals("PARTIAL3D"))
		{
			if (stackSize > 1)//3D image stack
				features = (new StackSimpleHaarFeatureExtractor(problem, getWavletLevel())).calcFeatures();	
			else
				System.out.println("invalid stack size for 3D images: " + stackSize);	
		}
		else if (featureExtractor.equals("LIGHT3D"))
		{
			if (stackSize > 1)//3D image stack
				features = (new StackThreeDirectionHaarFeatureExtractor(problem, getWavletLevel())).calcFeatures();	
			else
				System.out.println("invalid stack size for 3D images: " + stackSize);	
		} */
	}
	
	
	private String[] getTestingFiles(String directory, final String ext)
	{
		    String[] children;
		
			File dir = new File(directory);
			FilenameFilter filter = new FilenameFilter()
			{
				public boolean accept(File dir, String name)
				{ 
					if (ext.equals(".*"))
						return true;
					else
						return name.endsWith(ext);
				}
			};
			children = dir.list(filter);
			if (children == null)
				System.err.println("Problem reading files from the image directory.");
		
		return children;
	}

}
