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
 *  //using MAC and PSD (dentric structure and post-synaptic marker)
 *  //Mac for dendrites, PSD for synaptic markers.
 *
 Fiji:  (If imagej, needs to include CLAHE and RATS jar file)

  Split, duplicate MAP2 and VGLuT.
 
1. Map2:  -> soma    (Or dilate the DAPI?,  threshold 38, then dilate? then denoise?)
    background subtraction, 3.0 (small radius is good), get background (of soma),
    threshold (42, lower than auto), binarize, click dark background, with soma being 255.

2. Map2: -> dentrite 
    subtract soma first, despeckle, enhance (CLAHE)  
   threshold (23 or lower) (no need for RATS?) -> binary, despeckle with dendrite being 255 (dark background)
      dilate a bit afterwards for the same of counting mask!

2. VGluT: 
    1. paste control: SUBTRACT soma
    2. segmentation: RATS default (or a bit lower?).  Get a mark. Need to save the segmentation mask as a separate image.
    3. paster contol: combine with binary MAP2, using AND. 

          Switch 2 and 3!

   Counting:
    4. look up table -> invert LUT, so that foreground is white and background is black (but pixel unchanged) 
     -- not sure why needed by particle analyzer.
    5. set measurement: redirect to the original VGlut image
    6. analyze particle: size limit: 120
    
  Adjustable parameters and default:
   1. Soma mask: background subtraction radius: 3.0;  threshold when binarizing the subtracted soma: 42; (maybe need to dilate 1-2 times)
   2. Dendrite mask:  threshold when binarize enhanced dendrite: 23;   Number of dilation: 1
   3. Synapse segmetation: RATS: default; 
   4/ Synpase counting: min/max size of synapse   1/120
 *
 */

public class Cultured2DSynpase_Counter implements PlugIn { //, AdjustmentListener, TextListener {
    static final String pluginName = "Cultured2DSynpase_Counter";
  
    boolean debug = false;
    Vector sliders;
    Vector value;

    public static boolean or_default = true;
    public static boolean og_default = false;
    public static boolean ob_default = false;
    public static boolean br_default = false;
    public static boolean bg_default = true;
    public static boolean bb_default = false;
    public static boolean despeckle_default = false;
    public static boolean smooth_default = false;
    public static boolean show_switched = true;
    public static boolean show_mask_default = true;
    public static boolean enhanceNeuropil = true;

    int index1 =0, index2 =0; //channel index for object (synapse) and background (morphology).
    int thrValSoma= 42;
    int thrValDendrite= 23;
    int	segmentationThreshold = 25;
    int	backgroundSubtractionRadius = 3;
    int	dilateSomaTimes = 0;
    int	dilateDendriteTimes = 1;
    int minSynapseSize = 0;
    int maxSynapseSize = 120;
    int pixelResolutionInNM = 611;  //
    
    boolean or, og, ob, br, bg, bb;
    ImagePlus img;
    ImageProcessor ip;
    ImagePlus imgObj=null, imgDen=null;
    Calibration cal;

    int Width;
    int Height;
    //int arrayLength;
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
        if (img.getType() != ImagePlus.COLOR_RGB)
        {
            IJ.error("RBG image with channels required.");
            return false;
        }
        Width=img.getWidth();
        Height=img.getHeight();
        imgtitle = img.getTitle();
	ip = img.getProcessor();

        GenericDialog gd=new GenericDialog("Synapse Counter");
	gd.addSlider("Noise Threshold for Soma: ",ip.getMin(), ip.getMax(),thrValSoma);
	gd.addSlider("Noise Threshold for Dendrite: ",ip.getMin(), ip.getMax(),thrValDendrite);
	gd.addSlider("Noise Threshold for Synapse: ",ip.getMin(), ip.getMax(),segmentationThreshold);
	gd.addMessage("");
	gd.addSlider("Rollingball radius for extracting soma: ",0, 100,backgroundSubtractionRadius);
	gd.addSlider("Number of Dilation Operations for Soma: ",0, 100,dilateSomaTimes);
	gd.addSlider("Number of Dilation Operations for Dendrite (to cover the synpase area): ",0, 100,dilateDendriteTimes);
	gd.addSlider("Minimum number of pixels per synpase: ",0, 100,minSynapseSize);
	gd.addSlider("Maximum number of pixels per synpase: ",0, 10000,maxSynapseSize);
      	gd.addSlider("Pixel Resolution in Nano Meter (Synapse size will be reported using Micron): ",0, 100000, pixelResolutionInNM);
       
        //sliders=gd.getSliders();
	//((Scrollbar)sliders.elementAt(0)).addAdjustmentListener(this);
        //value = gd.getNumericFields();
        //((TextField)value.elementAt(0)).addTextListener(this);
	
    	gd.addCheckbox("Synapse channel is red", or_default);
        gd.addCheckbox("Synapse channel is green", og_default);
        gd.addCheckbox("Synapse channel is blue", ob_default);
        gd.addCheckbox("Dendrite/Morphology channel is red", br_default);
        gd.addCheckbox("Dendrite/Morphology channel is green", bg_default);
        gd.addCheckbox("Dendrite/Morphology channel is blue", bb_default);
        gd.showDialog();
        
        if (gd.wasCanceled()){
            ip.resetThreshold();
            img.updateAndDraw();
            return false;
        }

        thrValSoma=(int) gd.getNextNumber();
        thrValDendrite=(int) gd.getNextNumber();
	segmentationThreshold = (int) gd.getNextNumber();
	backgroundSubtractionRadius = (int) gd.getNextNumber();
	dilateSomaTimes = (int) gd.getNextNumber();
	dilateDendriteTimes = (int) gd.getNextNumber();
	minSynapseSize  = (int) gd.getNextNumber();
        maxSynapseSize  = (int) gd.getNextNumber();
	pixelResolutionInNM = (int) gd.getNextNumber();

        or =gd.getNextBoolean();             or_default = or;
        og =gd.getNextBoolean();             og_default = og;
        ob =gd.getNextBoolean();             ob_default = ob;
        br =gd.getNextBoolean();             br_default = br;
        bg =gd.getNextBoolean();             bg_default = bg;
        bb =gd.getNextBoolean(); 	     bb_default = bb;
  
        IJ.register(Cultured2DSynpase_Counter.class);
        return true;
    }

    void analyze() 
    {
	final ImagePlus imp = WindowManager.getCurrentImage();
	if (imp == null)  {
		IJ.error("There are no images open");
		return;
	}
    
         //set channel index
      	if (or == true)      	  index1 = 0;
      	else if (og == true)  	  index1 = 1;
      	else if (ob == true)      index1 = 2;
	if (br == true)      	  index2 = 0;
      	else if (bg == true)  	  index2 = 1;
      	else if (bb == true)    index2 = 2;
    
        //setCalibration(img);

    	//call the submethods
        split((ColorProcessor) imp.getProcessor());
	//duplicate MAP and VluT 
	ImagePlus synDup = imgObj.duplicate();
	ImagePlus mapDup = imgDen.duplicate();

        //MAP2 -> soma, and remove from syn
	segmentSoma(mapDup, synDup);
	synDup.show();
	
	//MAP2 -> dendrite, and map onto syn 
	//The mapDup now contains soma
	segmentDendrite(imgDen, mapDup, synDup);
    	synDup.show();
    
    	//segment and count
	segmentSynapse(synDup);
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

    //split the images into map2 (dendrites) and synapse
    public void split(ColorProcessor ip)
    {
         byte[] red = new byte[Width*Height];
  	 byte[] gre = new byte[Width*Height];
    	 byte[] blue = new byte[Width*Height];
	 ip.getRGB(red,gre,blue);

	 //object: synapse 
	 if(index1==0) //create an image (or use NewImage class)
	     imgObj = new ImagePlus("channel for obj (syn)", new ByteProcessor(Width, Height,red, null));
	 else if(index1==1)
  	     imgObj = new ImagePlus("channel for obj (syn)",new ByteProcessor(Width, Height,gre, null));
	 else if(index1==2)
  	     imgObj = new ImagePlus("channel for obj (syn)",new ByteProcessor(Width, Height,blue, null));
	 //Neuron
	 if(index2==0)
	      imgDen = new ImagePlus("channel for Den", new ByteProcessor(Width, Height,red, null));
	 if(index2==1)
	      imgDen = new ImagePlus("channel for Den",new ByteProcessor(Width, Height,gre, null));
  	 if(index2==2)
	      imgDen = new ImagePlus("channel for Den",new ByteProcessor(Width, Height,blue, null));
    }

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
    }
 
 
    //segment synapse image to get the foreground
    public void segmentSynapse(ImagePlus synapseImage)
    {
	//semgment    
	RATSForAxon rats = new RATSForAxon();
	rats.setup("",synapseImage); 
	ImagePlus segmentedImage = rats.run(synapseImage.getProcessor(),"noise="+segmentationThreshold+" lambda=3 min=54");
	//segmentedImage.show();

	//invert LUT
	segmentedImage.getProcessor().invert();

	//count
	//set to the original synapseImage
	//run("Set Measurements...", "area center redirect=[" + title_Synapse_dup + "] decimal=3");
	//run("Analyze Particles...", "size=0-" + maxSynpaseSize + " circularity=0.00-1.00 show=[Bare Outlines] display exclude summarize");
	//ParticleAnalyzer(int options, int measurements, ResultsTable rt, double minSize, double maxSize) 

	setCalibration(imgObj);
	Analyzer.setRedirectImage(imgObj);
	int options = Prefs.getInt("ap.options",ParticleAnalyzer.SHOW_OUTLINES+ParticleAnalyzer.DISPLAY_SUMMARY+ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES+ParticleAnalyzer.SHOW_RESULTS);  //See ParticleAnalyzer for full list of options
	int systemMeasurements = Prefs.getInt("measurements",Analyzer.AREA+Analyzer.CENTER_OF_MASS); //See Analyzer for full list of measurements
	ResultsTable res= new ResultsTable();
	ParticleAnalyzer pa = new ParticleAnalyzer(options, systemMeasurements, res, minSynapseSize, maxSynapseSize);
	pa.analyze(segmentedImage); 
    }

}



