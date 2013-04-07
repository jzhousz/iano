import ij.*;
import ij.ImagePlus.*;
import ij.plugin.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;
import ij.util.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

/**
 *
1.  Given an image (assume a one channel tiff)
    
    measure average intensity.  If too low, discard.

2.  Else, start adaptive segmentation (RATS), iteratively search for a good threshold, 

      start from a high theshold, so that there are typically a few components and their sizes are small (unless the image was excessove;u bright. 
      detect the number and size of the connected components (puncta)
      reduce the threshold, repeat (the number of components and the sizes of them should be increasing).
      stop when the max size of all component is bigger than a preset limit set loosely based on size (e.g. 2*actual synapse size), or when #of trials exceeds preset number of trials (e.g. 20)

     Once the search is stopped, check:
     If the number of components are only a few or the max size is too big (e.g. >10*actual synapse size), discard
       (not-usable image, typically due to excuessive density or very smeared).
       

3.  Refinement in counting (conservative):   (NOTE: Stringent treatment is not considered yet)
   
    Check the synapse size and count only those that are smaller than synapse size limit

     Output: statistics.
   If needed, can also otuput a usable (binary) image with unsuable areas removed. 
   (but if MetaMorph give more stats, can use MetaMorph)


 * 
 *3/26: remove init threadshold;  add small numbers ..
 *
 * 4/7: output total pixel
 */

public class Puncta_Counter_ImageCleaner implements PlugIn { //, AdjustmentListener, TextListener {
    static final String pluginName = "Puncta_Counter_ImageCleaner";
  
    boolean debug = false;
    Vector sliders;
    Vector value;

    int	segmentationInitThreshold = 100;  //25 is usually a good number but start high will help us to indendify images that are un-usable.
    int avgIntensityLowerBound = 15; 
    int minSynapseSize = 0;
    int maxSynapseSize = 100;     //adjustable, control when to stop threshold searching. 
    int pixelResolutionInNM = 1000;  //number of pixels per nano meter
    int sizeLimitForThesholdSearch = (int) 2*maxSynapseSize;
    int trialLimit = 40;  //max: initTh/step.  //adjustable, control when to stop threshold searching
 
    ImagePlus img;
    ImageProcessor ip;
    Calibration cal;

    int Width;
    int Height;
    int imageType;
    String imgtitle;
    int PixVal;
    int xorigin, yorigin, zorigin;

    public void run(String arg) {
        if (! setupGUI(arg)) return;
        analyze();
    }

    //get the parameters
    public boolean setupGUI(String arg) {
        
	img = WindowManager.getCurrentImage();
        if (img==null){
            IJ.noImage();
            return false;
        } 
        if (img.getType() != ImagePlus.GRAY8 && img.getType() != ImagePlus.GRAY16)
        {
            IJ.error("Only takes one channel gray-level image. RBG image needs to have channels split first.");
            return false;
        }
        Width=img.getWidth();
        Height=img.getHeight();
        imgtitle = img.getTitle();
	ip = img.getProcessor();
        imageType =  img.getType();    


        GenericDialog gd=new GenericDialog("Puncta Counter Image Cleaner");

	//probably not needed
	gd.addSlider("Initial Threshold (to be adjusted downward): ",ip.getMin(), ip.getMax(),segmentationInitThreshold);
	gd.addSlider("Dark Image Avg Intensity Lower Bound: ",ip.getMin(), ip.getMax(),avgIntensityLowerBound);
	gd.addSlider("Minimum number of pixels per synapse: ",0, 10000,minSynapseSize);
	gd.addSlider("Maximum number of pixels per synapse: ",0, 10000,maxSynapseSize);
      	//gd.addSlider("Pixel Resolution in Nano Meter (Synapse size will be reported using Micron): ",0, 100000, pixelResolutionInNM);
        gd.showDialog();
        
        if (gd.wasCanceled()){
            ip.resetThreshold();
            img.updateAndDraw();
            return false;
        }

	segmentationInitThreshold = (int) gd.getNextNumber();
	avgIntensityLowerBound = (int) gd.getNextNumber();
	minSynapseSize  = (int) gd.getNextNumber();
        maxSynapseSize  = (int) gd.getNextNumber();
	//pixelResolutionInNM = (int) gd.getNextNumber();

        IJ.register(Puncta_Counter_ImageCleaner.class);
        return true;
    }

    void analyze() 
    {
	final ImagePlus imp = WindowManager.getCurrentImage();
	if (imp == null)  {
		IJ.error("There are no images open");
		return;
	}
    
       // measure average intensity.  If too low, discard.
       int avgIntensity = getAvgIntensity(imp);
       IJ.log("average intensity: "+avgIntensity);
       if (avgIntensity < avgIntensityLowerBound)
       {
	     IJ.log("This image is too dark.  Considered unusable for punta counting.");
	     return;
       }

       //start adapative segmentation:
       int step = 2;
       int maxSize;

       //loop until all the sizes are less than a size limit
       //With a very high threshold, it likely that there are less synapses and there are relatively small
       //as the threshold decreases, typically the # of synapses will increase 
       //eventually it will reach the point that almost all of the components look like a synapse.
       int[] stat = new int[2];
       ImagePlus segmentedSynapseImage = null;
       int th, trials;
       for(th = segmentationInitThreshold, trials = 0; trials < trialLimit ; th -= step, trials++)
       {
	  IJ.log("current threshold: " + th);     
          //call segmentation method, get a) count of compnents  b) maxSize
          segmentedSynapseImage = segmentSynapse(img, stat, th);
	  maxSize = stat[1];
	  if (maxSize > sizeLimitForThesholdSearch) 
              break;
       }
       
       //make them a parameter for unusable. 
       //The higher the size limit, the more lower quality image will be considered usable.
       //The lower the number limit , the more lower quality image will be considered usable.
       if ((stat[1] > 30*maxSynapseSize)|| (stat[0] < 10))  
       {
     	   //the biggest one is huge, probably due to excessive intensity, usually break using a trial.
   	   IJ.log("Excessively bright or smeared.  Not usable.");
	   return;
       }
       
  
       //else  get  a segmented image before removing unnessary areas.
       //ONLY NEEDED DURING DEBUG   4/7/13
       //segmentedSynapseImage.show();
		
       //STEP 3:  REMOVAL OF TOO BIG AREAS. conservative:  just remove large synapses;
       //go through the returned components,  for each component,  check size, and apply mask to clear it.
       //Question to Chris: Is this enough?  Or the halo/region around it should be removed too?
       //
       

       //Do the counting using segmentedSyanpseImage again. 
       segmentedSynapseImage.getProcessor().invert();
       setCalibration(imp);
       Analyzer.setRedirectImage(imp);
       ResultsTable res= new ResultsTable();
	//NOTE:  THIS ALSO SETS A LIMIT ON COUNTING COMPONENTS!  ALL BIG ONES ARE NOT COUNTED!!
	//report: number, size, average intensity
	//ParticleAnalyzer pa = new ParticleAnalyzer(ParticleAnalyzer.SHOW_OUTLINES|ParticleAnalyzer.SHOW_MASKS|ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES|ParticleAnalyzer.SHOW_SUMMARY|ParticleAnalyzer.SHOW_RESULTS, Measurements.AREA|Measurements.MEAN|Measurements.CENTER_OF_MASS, res, minSynapseSize, maxSynapseSize);
	ParticleAnalyzer pa = new ParticleAnalyzer(ParticleAnalyzer.SHOW_OUTLINES|ParticleAnalyzer.SHOW_MASKS|ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES|ParticleAnalyzer.SHOW_RESULTS, Measurements.AREA|Measurements.MEAN|Measurements.CENTER_OF_MASS, res, minSynapseSize, maxSynapseSize);
    	boolean success = pa.analyze(segmentedSynapseImage); 
	if (success == false)
		IJ.log("There is a problem in analyzing the segmented synpases.");

	//report results
	IJ.log("OVERALL RESULT:");
	IJ.log("Entire Area (in pixels): "  + (Width*Height));
       	IJ.log("Total Number of Synapses: " + res.getCounter());
        IJ.log("Synapse Density (#/area): " + ((double) res.getCounter())/(Width*Height)); 

    }

    
    public void setCalibration(ImagePlus img)
    {
        cal = img.getCalibration();
        if (cal == null ) {
            cal = new Calibration(img);
        }
        //overriding depth from property e.g.if the correct info is lost during de-convolution
        double pixelResolution = ((double) pixelResolutionInNM)/1000.0;
        cal.pixelWidth = cal.pixelHeight = pixelResolution;
	cal.setUnit("micron");
        //IJ.log("Set pixel resolution to "+ pixelResolution + " micron.");

	img.setCalibration(cal);
    }


 
    //segment synapse image to get the foreground
    //Assume the image argument is  black background.   
    //return  a) max size of all the components;  b) total number of all the components  c) the segmented image (before inverting)
    //
    public ImagePlus segmentSynapse(ImagePlus synapseImage, int[] stat, int th)
    {
	//semgment    
	RATSForAxon rats = new RATSForAxon();
	rats.setup("",synapseImage); 
	ImagePlus segmentedImage = rats.run(synapseImage.getProcessor(),"noise="+th+" lambda=3 min=54");
	//segmentedImage.show();

	//invert LUT
	ImagePlus resImg = segmentedImage.duplicate(); //keep a copy before inverting
	segmentedImage.getProcessor().invert();

	setCalibration(synapseImage);
	Analyzer.setRedirectImage(synapseImage);
	ResultsTable res= new ResultsTable();
	
	//NOTE:  THIS ALSO SET A LIMIT ON COUNTING COMPONENTS!  ALL BIG ONES ARE NOT COUNTED!!
	//ParticleAnalyzer pa = new ParticleAnalyzer(ParticleAnalyzer.SHOW_OUTLINES|ParticleAnalyzer.SHOW_MASKS|ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES, Measurements.AREA, res, minSynapseSize, 2*maxSynapseSize);
	ParticleAnalyzer pa = new ParticleAnalyzer(ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES, Measurements.AREA, res, 0, 99999);

	boolean success = pa.analyze(segmentedImage); 
	if (success == false)
		IJ.log("There is a problem in analyzing the segmented synpases.");

	//go through objects in the results table, calculate # and maxSize .. TOBEDONE.
	int colNumForSize = 0;
	int totalColumn = res.getLastColumn(); 
	//ONLY NEEDED DURING DEBUGGING
        //IJ.log("number of columns:" + res.getLastColumn() + " number of rows:" + res.getCounter()); 
	stat[0] = res.getCounter();

	if (res.getCounter() == 0)
	{ //no component is less than 99999 at all.  The image is too bright.
	  stat[1] = 99999;  
	}
	else
	{
	  double maxSize = res.getValueAsDouble(colNumForSize,0);  //first column, first row
	  double cellValue;
	  for(int i=1; i< res.getCounter(); i++)
	  {
	    cellValue = res.getValueAsDouble(colNumForSize, i);
  	    //IJ.log("cell value: " +cellValue);
            if ( cellValue > maxSize)
       		maxSize = cellValue;
	  }
	  stat[1] = (int) maxSize;
	}

	IJ.log("Searching... Current iteration (without limiting size) -- Number of components: "+stat[0]+" max size: " +stat[1]);
	return resImg;
	//return segmentedImage;
    }

    //Calculate the avg intensity of the image that are considered as foreground
    int getAvgIntensity(ImagePlus originalImg)
    {
	 int res = 0;

         int maskVal, forVal;
	 int width = originalImg.getWidth();
	 int height = originalImg.getHeight();

         for(int x=0; x< width; x++)	
 	  for (int y=0; y< originalImg.getHeight(); y++)
	     res +=  getValue(originalImg, y*width+x);
	     
	 return res/(width*height);

    }

    int getValue(ImagePlus img,  int arrayIndex)
    {
	int value =0;
	int mask = 0xff;
	long longmask = 0xffff;
	if (imageType == ImagePlus.GRAY8)
		value = (int) (((byte[]) img.getProcessor().getPixels())[arrayIndex] & mask);
	else if (imageType == ImagePlus.GRAY16)
		value = (int) (((short[]) img.getProcessor().getPixels())[arrayIndex] & longmask);
	return value;
    }
}



