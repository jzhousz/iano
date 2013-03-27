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

2.  Else, start adaptive segmentation (RATS), iteratively, based on a loose size limit (e.g.2*actual size):

      start from low theshold 
      detect the number and size of the components
      repeat while the sizes of all detected components are less than limit.
     
    If after segmentation, the number of components are very few, discard
       (possibly a not-usable image to start with, such as excuessive density or very smeared).

3.  Refinement (conservative):
   
    Check which synapses are countable based on size: If not, clear up that area.

Output:  a usable (binary) image with unsuable areas removed. 
   (It also has the statistics of punta, but if MetaMorph give more stats, can use MetaMorph).
 * 
 *
 */

public class Puncta_Counter_ImageCleaner implements PlugIn { //, AdjustmentListener, TextListener {
    static final String pluginName = "Puncta_Counter_ImageCleaner";
  
    boolean debug = false;
    Vector sliders;
    Vector value;

    int	segmentationInitThreshold = 5;  //25 is usually a good number but start low will help us to indendify images that are un-usable.
    int avgIntensityLowerBound = 1; 
    int minSynapseSize = 0;
    int maxSynapseSize = 120;
    int pixelResolutionInNM = 611;  //
  

    /*
    int thrValSoma= 42;
    int thrValDendrite= 23;
    int	backgroundSubtractionRadius = 3;
    int	dilateSomaTimes = 0;
    int	dilateDendriteTimes = 1;
    boolean or, og, ob, br, bg, bb;
    ImagePlus imgObj=null, imgDen=null;
    */

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
	gd.addSlider("Initial Noise Threshold for Synapse (Will be automatically adjusted to find the optimal, just need a lower bound): ",ip.getMin(), ip.getMax(),segmentationInitThreshold);
	gd.addSlider("Dark Image Avg Intensity Lower Bound: ",ip.getMin(), ip.getMax(),avgIntensityLowerBound);

	//gd.addMessage("");
	//gd.addSlider("Rollingball radius for extracting soma: ",0, 100,backgroundSubtractionRadius);
	//gd.addSlider("Number of Dilation Operations for Soma: ",0, 100,dilateSomaTimes);
	//gd.addSlider("Number of Dilation Operations for Dendrite (to cover the synpase area): ",0, 100,dilateDendriteTimes);
	//gd.addSlider("Minimum number of pixels per synpase: ",0, 100,minSynapseSize);
	gd.addSlider("Maximum number of pixels per synpase: ",0, 10000,maxSynapseSize);
      	gd.addSlider("Pixel Resolution in Nano Meter (Synapse size will be reported using Micron): ",0, 100000, pixelResolutionInNM);
    	//gd.addCheckbox("Synapse channel is red", or_default);
        //gd.addCheckbox("Synapse channel is green", og_default);
        //gd.addCheckbox("Synapse channel is blue", ob_default);
        //gd.addCheckbox("Dendrite/Morphology channel is red", br_default);
        //gd.addCheckbox("Dendrite/Morphology channel is green", bg_default);
        //gd.addCheckbox("Dendrite/Morphology channel is blue", bb_default);
        gd.showDialog();
        
        if (gd.wasCanceled()){
            ip.resetThreshold();
            img.updateAndDraw();
            return false;
        }

        //thrValSoma=(int) gd.getNextNumber();
        //thrValDendrite=(int) gd.getNextNumber();
	segmentationInitThreshold = (int) gd.getNextNumber();
	avgIntensityLowerBound = (int) gd.getNextNumber();
	//minSynapseSize  = (int) gd.getNextNumber();
        maxSynapseSize  = (int) gd.getNextNumber();
	pixelResolutionInNM = (int) gd.getNextNumber();

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
       // parameter1: a very low average intensity
       int avgIntensityLowerBound = 10;
       int avgIntensity = getAvgIntensity(imp);
       IJ.log("average intensity: "+avgIntensity);
       if (avgIntensity < avgIntensityLowerBound)
       {
	     IJ.log("This image is too dark.  Considered unusable for punta counting.");
	     return;
       }

       //start adapative segmentation:
       //parameter2: synapseMaxSize;
       //parameter3:  segmentationInitThreshold;  (lower than the usual threshold)
       int sizeLimit = 2* maxSynapseSize;
       int step = 2;
       int maxSize;
       //loop until all the sizes are less than a size limit
       int[] stat = new int[2];
       ImagePlus segmentedSynapseImage;
       for(int th = segmentationInitThreshold; ; th += step)
       {
          //call segmentation method, get a) count of compnents  b) maxSize
          segmentedSynapseImage = segmentSynapse(img, stat, th);
	  maxSize = stat[1];
	  if (maxSize > sizeLimit)
              break;
       }
       //get a segmented image where each compnent is less than 2*synapse max size.
       if (stat[0]  < 10)   
       {
          IJ.log("This image only contains a few synapses. It is probably because the original image was not usable (e.g. excessive density or very smeared).");
	  return;
       }
       //else  get  a segmented image before removing unnessary areas.
       segmentedSynapseImage.show();
		
       //STEP 3:  REMOVAL OF TOO BIG AREAS. conservative:  just remove large synapses;
       //go through the returned components,  for each component,  check size, and apply mask to clear it.
       //Question to Chris: Is this enough?  Or the halo/region around it should be removed too?
       //
       

       //Do the counting using segmentedSyanpseImage again. 


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
        IJ.log("Set pixel resolution to "+ pixelResolution + " micron.");

	img.setCalibration(cal);
    }


    /*
    //use neuron channel to segment soma, return a binary mask image
    public void segmentSoma(ImagePlus mapImage, ImagePlus synapseImage)
    {
	ImageProcessor mapip = mapImage.getProcessor();    
	BackgroundSubtracter bs = new BackgroundSubtracter();
	//Parameters: ImageProcessor ip, double radius, boolean createBackground, boolean lightBackground, boolean useParaboloid, boolean doPresmooth, boolean correctCorners
	bs.rollingBallBackground(mapip, backgroundSubtractionRadius, true, false, false, false, false);
	//segment soma
	mapip.threshold(thrValSoma); //, BLACK_AND_WHITE_LUT);
	mapip.invert();
	for(int i = 0; i < dilateSomaTimes; i++)
	   mapip.dilate();

	//subtract soma from synpase
	mapip.invert();
	ImageProcessor synapseIp = synapseImage.getProcessor();
	synapseIp.copyBits(mapip, 0, 0, Blitter.SUBTRACT);
    }

    //use neuron channel to segment neurites (after removing soma), return a binary mask image
    public void segmentDendrite(ImagePlus mapImage, ImagePlus somaImage, ImagePlus synapseImage)
    {
	//subtract soma
	ImageProcessor mapIp = mapImage.getProcessor();
	mapIp.copyBits(somaImage.getProcessor(), 0, 0, Blitter.SUBTRACT);
	
	//despeckle and enhance
	//Remove noise with despeckle : median filter with radius 1
	RankFilters filter = new RankFilters();
	filter.rank(mapIp, 1, RankFilters.MEDIAN);
	HistogramEq.run(mapImage, 127, 256, 3.00f, null, null);
	//binarize
	mapIp.threshold(thrValDendrite);  //set black as background?
	//how to convert to mask?
	mapIp.invert();
	//despeckle again
	filter.rank(mapIp, 1, RankFilters.MEDIAN);
	//dilate
	for(int i = 0; i < dilateDendriteTimes; i++)
	   mapIp.dilate();      //white gets smaller if not inverting mapIp??????!!!!!
	mapImage.show();

	//mask onto synpase
	mapIp.invert();  //convert back before mask ont synapse!
	ImageProcessor synapseIp = synapseImage.getProcessor();
	synapseIp.copyBits(mapIp, 0, 0, Blitter.AND);
    } */
 
 
    //segment synapse image to get the foreground
    //return  a) max size of all the components;  b) total number of all the components
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

	//count
	//set to the original synapseImage
	//run("Set Measurements...", "area center redirect=[" + title_Synapse_dup + "] decimal=3");
	//run("Analyze Particles...", "size=0-" + maxSynpaseSize + " circularity=0.00-1.00 show=[Bare Outlines] display exclude summarize");
	//ParticleAnalyzer(int options, int measurements, ResultsTable rt, double minSize, double maxSize) 

	//setCalibration(synapseImage);
	Analyzer.setRedirectImage(synapseImage); //redirect to the original gray scale for measure
	int options = Prefs.getInt("ap.options",ParticleAnalyzer.SHOW_OUTLINES+ParticleAnalyzer.DISPLAY_SUMMARY+ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES+ParticleAnalyzer.SHOW_RESULTS);  //See ParticleAnalyzer for full list of options
	int systemMeasurements = Prefs.getInt("measurements",Analyzer.AREA); //See Analyzer for full list of measurements
	ResultsTable res= new ResultsTable();
	ParticleAnalyzer pa = new ParticleAnalyzer(options, systemMeasurements, res, minSynapseSize, maxSynapseSize);
	pa.analyze(segmentedImage); 

	//go through objects in the results table, calculate # and maxSize .. TOBEDONE.
	int colNumForSize = 0;
	int totalColumn = res.getLastColumn(); 
        IJ.log("number of columns:" + res.getLastColumn() + " number of rows:" + res.getCounter()); 
	double maxSize = res.getValueAsDouble(colNumForSize,0);  //first column, first row
	double cellValue;
	for(int i=1; i< res.getCounter(); i++)
	{
	  cellValue = res.getValueAsDouble(colNumForSize, i);
  	  IJ.log("cell value: " +cellValue);
          if ( cellValue > maxSize)
       		maxSize = cellValue;
	}
	stat[0] = res.getCounter();
	stat[1] = (int) maxSize;


	IJ.log("Number: "+stat[0]+" max: " +stat[1]);

	return resImg;
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



