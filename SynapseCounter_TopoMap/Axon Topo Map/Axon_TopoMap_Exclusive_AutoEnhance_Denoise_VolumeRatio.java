/*
 *
 * This is to quantify several measurements in a RGB image stack,
 * particularly for axon terminal in neuropil.
 *
 * From computational view, it measures the relative position of one region (axon terminal) in one channel (clone), within another region (neuropil) in another channel.
 *
 * It measures the topographic index,  and territory proportion.
 *
 * It can also output the center of mass and geometric center of the axon terminal.
 *
 *  3/i10/2011  Jie Zhou
 *
 *
If there are some black slices, adjust t to lower. If there are large blobs of white pixels, adjust it to higher.  Within these two situations, typically there is a large range of possible ts.
In this range, I suggest to set it lower (conservative), so 1) more signal are included 2) easier to set a uniform t for a set. 
 * 
 *  4/19/2012  Add display average intensity of two channels.  
 *
 *  4/26/2012  Add adjustment of intensity:
 *               The optimal range of intensity for the neuropil is "80-100".  
 *
 *  Since we now know that neuropil intensity of "90" is about right, we can fix it and test the range for the clone. 
 *  For NEUROPIL ONLY: (<35 -> exclude; <90 -> adjust to 90; >90 -> no adjustment)
 *       If less than 35, discard. Else if less than 80, increase to above 80 (but lower than 100).   Otherwise, no enhancement.
 *  We'll then use the new plugin to test for clone intensity and suggest a value for automatic adjustment of the clones.  
 * 
 *  5/17/2012
 *  Denoise: When the neuropil's noise is high, it might cause the mask to be problematic.
 *  Denoise based on 2D max projection.
 *  Add a batch mode macro to let it work with a directory.
 *  Need to adjust RATS parameter of leaf size (segmentation for neuropil and axon), as well as enhance target?
 *  
 *  5/22/2012
 *  Q: what is the exact effect of  adjusting window, set min/max?  What happened the the original high spots???
 *
 *  5/26/2012
 *  Test on exclusive data. The difference between exclusive and inclusive: 
 *   a. Before denoise, combine axon and neuropil channel.  Need to combine the original value. Otherwise, the intensity is too low.
 *   b. Don't duplicate for z-projection based denoise. So the combined channel is used for later calculation.
 *   c. Comment out code for RTI.
 *   d. target intensity can be lower? Add parameter knobs.
 *
 * 11/21/2012
 *   Add the calculation of volume ratio between neuropil and clone.
 *  
 */


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

public class Axon_TopoMap_Exclusive_AutoEnhance_Denoise_VolumeRatio implements PlugIn, AdjustmentListener, TextListener {
    static final String pluginName = "Axon_TopoMap_Exclusive_AutoEnhance_Denoise_VolumeRatio";
    int InclusiveLimit = 2;   //level of allowance of axon outside neuropil
    final int EXCLUSIVELIMIT = 999; //a very big number
    int ENHANCE_BASE = 35;
    int ENHANCE_TARGET = 80;
    String outputfilename = "TIOutput.csv";
  
    boolean debug = false;
    boolean useCLAHE = false; //test and compare with window/level adjustment
    boolean useRATS = true;   //Is rats too adaptive for such high noise images? 
                              //note that simple setThreshold() does not work with very dim images (largely white for some images in 2nd set)
			      //Current conclusion: still use RATS, but with more suitable parameters, plus 2D projection ...
   int  leafSizeAxon = 5;
   int  leafSizeNeuropil = 5; //Should I adjust leaf size to overcome the speckly noise(up to 28?
				//   what is the upperlimt? width or height /2 , whichever is smaller!!
                              //What about lamda?

    Vector sliders;
    Vector value;

    public static boolean or_default = false;
    public static boolean og_default = true;
    public static boolean ob_default = false;
    public static boolean br_default = true;
    public static boolean bg_default = false;
    public static boolean bb_default = false;
    public static boolean despeckle_default = false;
    public static boolean smooth_default = false;
    public static boolean show_switched = true;
    public static boolean show_mask_default = true;
    public static boolean enhanceNeuropil = false;
   
    int ThrVal = 6;
    int ThrVal2  = 7;  //channel for neurpoil
    
    boolean or, og, ob, br, bg, bb;
    boolean show_mask;
    boolean despeckle, smooth;
 
    ImagePlus img;
    ImageProcessor ip;

    int Width;
    int Height;
    int NbSlices;
    int arrayLength;
    String imgtitle;
    int PixVal;
    int xorigin, yorigin, zorigin;

    public void run(String arg) {
        if (! setupGUI(arg)) return;
        analyze();
    }

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
        NbSlices=img.getStackSize();
        arrayLength=Width*Height*NbSlices;
        imgtitle = img.getTitle();

        //does not work with RGB processor-- Thresholding of RGB images is not supported!
        ip=img.getProcessor();
        //ThrVal=ip.getAutoThreshold();
        ip.setThreshold(ThrVal,Math.pow(2,16),ImageProcessor.RED_LUT);
        img.setSlice((int)NbSlices/2);
        img.updateAndDraw();

        GenericDialog gd=new GenericDialog(pluginName);
	gd.addMessage("Assumed image stack direction: x: Anterior-Posterior;  y: Dorsal-Ventral; z: cross-section (Left-Right). The directions y and z were switched from confocal microscope imaging.");
	gd.addMessage("");
	gd.addSlider("Noise Threshold for Axon: ",ip.getMin(), ip.getMax(),ThrVal);
	gd.addSlider("Noise_ Threshold for Neuropil: ",ip.getMin(), ip.getMax(),ThrVal2);
	
        sliders=gd.getSliders();
        ((Scrollbar)sliders.elementAt(0)).addAdjustmentListener(this);
        value = gd.getNumericFields();
        ((TextField)value.elementAt(0)).addTextListener(this);
        //gd.addCheckbox("Despeckle the image first", despeckle_default);
        //gd.addCheckbox("Smooth the image first", smooth_default);
	//gd.addCheckbox("Enhance the neuropil with CLAHE", enhanceNeuropil);
	gd.addCheckbox("Show the segmentation mask images", show_mask_default);
    	gd.addCheckbox("Clone channel is red", or_default);
        gd.addCheckbox("Clone_ channel is green", og_default);
        gd.addCheckbox("Clone__ channel is blue", ob_default);
        gd.addCheckbox("Neuropil channel is red", br_default);
        gd.addCheckbox("Neuropil_ channel is green", bg_default);
        gd.addCheckbox("Neuropil__ channel is blue", bb_default);
        gd.addSlider("Cushion for Noise of Axon Over Neuropil: ", 0, 10, InclusiveLimit);
	gd.addSlider("Minimum Intensity Requirement for Neuropil: ", ip.getMin(), ip.getMax(), ENHANCE_BASE);
	gd.addSlider("Target Intensity for Enhancing Neuropil: ", ip.getMin(), ip.getMax(), ENHANCE_TARGET);
        gd.addStringField("File to save TI:",outputfilename,20);
        gd.showDialog();
        
        if (gd.wasCanceled()){
            ip.resetThreshold();
            img.updateAndDraw();
            return false;
        }

        ThrVal=(int) gd.getNextNumber();
        ThrVal2=(int) gd.getNextNumber();
	
        //despeckle = gd.getNextBoolean();     despeckle_default = despeckle;
        //smooth = gd.getNextBoolean(); 	     smooth_default = smooth;
	//enhanceNeuropil = gd.getNextBoolean(); 
	show_mask = gd.getNextBoolean();     show_mask_default = show_mask;
        or =gd.getNextBoolean();             or_default = or;
        og =gd.getNextBoolean();             og_default = og;
        ob =gd.getNextBoolean();             ob_default = ob;
        br =gd.getNextBoolean();             br_default = br;
        bg =gd.getNextBoolean();             bg_default = bg;
        bb =gd.getNextBoolean(); 	     bb_default = bb;
	InclusiveLimit = (int) gd.getNextNumber();
	ENHANCE_BASE = (int)  gd.getNextNumber();
	ENHANCE_TARGET = (int) gd.getNextNumber();
	outputfilename = (String) gd.getNextString();
    
        IJ.register(Axon_TopoMap_Exclusive_AutoEnhance_Denoise_VolumeRatio.class); // static fields preserved when plugin is restarted
        //Reset the threshold
        ip.resetThreshold();
        img.updateAndDraw();
        return true;
    }

    //get CoM
    float[] getCoM(int index1, int ThrVal, ImageStack stack)
    {
	 float[] CoM = new float[3];  //x, y, z for CoM
	 float intensity =0;

	 int totalx =0, totaly =0, totalz = 0;
    	 int z, y, x;
         int arrayIndex=0;
	 int pict2val;
	 int[] PixVal;
	 ImageProcessor ip;
         for (z=0; z<NbSlices; z++) {
              ip = stack.getProcessor(z+1);
             for (y=0; y<Height;y++) {
                 for (x=0; x<Width;x++) {
			 PixVal = ip.getPixel(x,y,null);
			 if (PixVal[index1] > ThrVal)
                	 {
                            totalx += x*PixVal[index1];
                            totaly += y*PixVal[index1];
			    totalz += z*PixVal[index1];
			    intensity += PixVal[index1];
	                 }
                     arrayIndex++;
                 }
             }
         }
	 //fill result
	 CoM[0] = totalx/intensity;
	 CoM[1] = totaly/intensity;
	 CoM[2] = totalz/intensity;

 	java.text.DecimalFormat df = new java.text.DecimalFormat("#.0");
	IJ.log("Center of Mass for the Selected Channel: ");
     	IJ.log("-- Coordinates (0-based): ");
       	IJ.log("x: "+ df.format(CoM[0]) );
	IJ.log("y: "+ df.format(CoM[1]) );
        IJ.log("z: "+ df.format(CoM[2]) + " (slice number in ImageJ is "+ (df.format(CoM[2]+1))+" since it starts from 1)");

	 return CoM;

    }

    //calculate one voxel's TI.
    //If the voxel to the neuropil's dorsal end's distance (with same x,y) is d, to the ventral end's distance is v, then  
    //
    //    ti =    d / (d + v)
    //
    // Only 0 to 1 are considered as valid.  Negative numbers will be ignored.
    //
    //
    float calcTopologicalIndexForOneVoxel(int z, int minz, int maxz, int limit)
    {
	if  (z < minz || z > maxz) //should not happen but could happen due to segmentation
	{
   	  //axon voxel too far from neuropil will be considered noise.  axon close enough but out of neuropile will be set to 0 or 1 depending on which side.
	  if (z < minz)
	     if ((minz-z) <= limit)  //small but < limit, return 0, otherwise it will be negative
		  return 0;
	     else 
		  return -1;
	  if (z > maxz)
	     if ((z - maxz) <= limit) 
	          return 1;
	     else
	   	  return -1;  //not valid	     
	}

 	return  (z-minz+1)/((float)(maxz-minz+1));
    }

   //allow some error talerance for segmentation
   //It should return between 0 and 1.
   // -1 indicates error
   float calcTerritoryPortion(int axonMaxZ, int axonMinZ, int neuropilMaxZ, int neuropilMinZ, int limit)
    {

	float res =0;
	if  (axonMaxZ < axonMinZ) //should not happen
	{
		IJ.log("problem in calculating terri (max < min, possible hollowfor that x,y line)");
		return -1;
	}
	if (neuropilMaxZ < neuropilMinZ)
	{
               IJ.log("problem in calc terri (neuroMaxZ < neuroMinZ, possible hoollow line for the given x,y");
	       return -1;
	}
	else		          
	    res =  (axonMaxZ - axonMinZ+1)/((float)(neuropilMaxZ - neuropilMinZ+1));

	//maybe good
	if (res > 1.0)
	{
	    //should not happen either, unless due to thresholding the neuropil is smaller than axon	
	    //IJ.log("terr > 1? axonMaxz: "+axonMaxZ + " axonMinz "+ axonMinZ + "neu Max" + neuropilMaxZ + " neu min"+neuropilMinZ);
	    if (( (axonMaxZ - axonMinZ) - (neuropilMaxZ - neuropilMinZ) ) < limit)
		    return 1;
	    else
		    return -1;
	}
	else //finally a good one
	{
  	    //IJ.log("individual terr- "+res);
	    return  res;
	}
    }

    //
    //calculate how many voxels are there in neuropil and axon channel for current x 
    //
    //
    // return: volumesForCurrentX[0]:  neuropl total voxel;   [1]: axon/clone total voxel count
    int calcVolumeSum(int axonMaxZ, int axonMinZ, int neuropilMaxZ, int neuropilMinZ, int limit, int[] volumesForCurrentX)
    {
	if  (axonMaxZ < axonMinZ) //should not happen
	{
		IJ.log("problem in calculating volume (max < min, possible hollow for that x,y line)");
		return -1;
	}
	if (neuropilMaxZ < neuropilMinZ)
	{
               IJ.log("problem in calculating volume (neuroMaxZ < neuroMinZ, possible hollow line for the given x,y");
	       return -1;
	}
	//should not happen either, unless due to thresholding the neuropil is smaller than axon	
	if (( (axonMaxZ - axonMinZ) - (neuropilMaxZ - neuropilMinZ) ) >= limit)
	{
              IJ.log("problem in calculating volume: axon is bigger than neuropil. ");
	      return 0;
	}
	
	//ready to set the value
	volumesForCurrentX[0] = neuropilMaxZ - neuropilMinZ;
	volumesForCurrentX[1] = axonMaxZ - axonMinZ;
	return 1;
    }

    //
    //Calculate the total intensity of the image that are considered as foreground
    // return  [0] total intensity
    //         [1] # of voxel
    int[] calIntensity(ImageProcessor originalImg, ImageProcessor maskImg)
    {
	 int[] res = new int[2];
	 for(int i =0; i < res.length; i++)
		 res[i] = 0;

         byte[] original = (byte[]) originalImg.getPixels();
         byte[] mask = (byte[]) maskImg.getPixels(); 
         
	 int maskVal, forVal;
         for(int x=0; x<Width; x++)	
	 {
 	  for (int y=0; y< Height; y++)
	  {
	     maskVal =	mask[y*Width+x]&0xff;
	     forVal =  original[y*Width+x]&0xff;
	     
	     if(maskVal !=0)
	     {
	       res[0] += forVal;
	       res[1] += 1;
	     }
	  }
	 }
	 return res;

    }

    //
    // Calculate the avearge intensity for all slices of a stack 
    // Based on the segmented foreground of a particular channel
    // 
    int calAverageBgIntensityForStack(ImageStack stack, int ThrVal, int channel)
    {
       ImagePlus aySliceImgNeu=null;
       ColorProcessor aySlice=null;
       float intensNeu[] = new float[2];
       byte[] red,gre,blue;
       RATSForAxon rats = null;
       for(int i=0; i<intensNeu.length; i++) intensNeu[i] =0; 

       for(int z=0; z<NbSlices; z++)
       {
	  aySlice = (ColorProcessor) stack.getProcessor(z+1);
          red = new byte[Width*Height];
  	  gre = new byte[Width*Height];
    	  blue = new byte[Width*Height];
	  aySlice.getRGB(red,gre,blue);
	    
	  if(channel==0)
	      aySliceImgNeu = new ImagePlus("channel for Neu", new ByteProcessor(Width, Height,red, null));
	  if(channel==1)
	      aySliceImgNeu = new ImagePlus("channel for Neu",new ByteProcessor(Width, Height,gre, null));
  	  if(channel==2)
	      aySliceImgNeu = new ImagePlus("channel for Neu",new ByteProcessor(Width, Height,blue, null));
	  rats = new RATSForAxon();
	  rats.setup("",aySliceImgNeu); 
  	  String opt = "noise="+ThrVal+" lambda=3 min=5";
	  ImagePlus maskImageNeu = rats.run(aySliceImgNeu.getProcessor(),opt);

 	  int avgNeurArr[] = calIntensity(aySliceImgNeu.getProcessor(), maskImageNeu.getProcessor());
	  intensNeu[0] += avgNeurArr[0];
	  intensNeu[1] += avgNeurArr[1];
       }

       return (int) (intensNeu[0]/intensNeu[1]);
    }


    //Add axon (index1) into the neuropil channel (index2)
    //05/23/2011: scale the 2 channels so that the sum is less than 255
    void combineAxonAndNeuropil(int index1, int index2, ImageStack stack)
    {
      byte[] red = new byte[Width*Height];
      byte[] gre = new byte[Width*Height];
      byte[] blue = new byte[Width*Height];
      byte[] axonChannel=null;   
      ColorProcessor aySlice=null;
      for(int z=0; z<NbSlices; z++)
      {
	  aySlice = (ColorProcessor) stack.getProcessor(z+1);
  	  aySlice.getRGB(red,gre,blue);
	  //axon
	  if(index1==0) 
	     axonChannel = red;	  
	  else if(index1==1)
	     axonChannel = gre;	  
	  else if(index1==2)
             axonChannel = blue;		  
          //neuropil
	  for(int i=0; i<Width; i++)
	    for(int j=0; j<Height; j++)
	    {
	       //overwrite the original neuropile channel 
	       int[] iArray = new int[3];
	       if (index2==0)
	       {
		  iArray[0] = (red[j*Width+i]&0xff)+(axonChannel[j*Width+i]&0xff);
		  if (iArray[0] > 255) iArray[0] = 255;
		  iArray[1] = gre[j*Width+i];
		  iArray[2] = blue[j*Width+i];
	       }
	       else if (index2==1)
	       {
		  iArray[0] = red[j*Width+i];
	       	  iArray[1] = (gre[j*Width+i]&0xff)+(axonChannel[j*Width+i]&0xff);
		  if (iArray[1] > 255) iArray[1] = 255;
		  iArray[2] = blue[j*Width+i];
	       }
	       else if (index2==2)
	       {
		  iArray[0] = red[j*Width+i];
	       	  iArray[1] = gre[j*Width+i];
		  iArray[2] = (blue[j*Width+i]&0xff)+(axonChannel[j*Width+i]&0xff);
		  if(iArray[2] > 255) iArray[2] = 255;
	       }
	       aySlice.putPixel(i,j,iArray);
	    }
	}//end of all z
    } 
   
    //denoise each stack and each channel, but setting the voxel to 0 if it is out of boundary
    //Pass in processor or a byte[]
    void denoiseBasedOnBoundary(int[] neuropilMinZ, int[] neuropilMaxZ, ImageProcessor imp)
    {
      int x,y;	    
      byte[] mask = (byte[]) imp.getPixels();;
	
      for(x=0; x<Width; x++)
      {
	for(y = 0; y < neuropilMinZ[x]; y++)
		mask[y*Width+x] = 0;
	for(y = neuropilMaxZ[x]; y < Height; y++)
		mask[y*Width+x] = 0;
      }
    }

    //  Input:  an ImagePlus object.
    //  1. Combine axon and neuropil channel (So that the noise in neuropil will be shadowed?!!!) 
    //  2. z-projectin:  max intensity projection
    //  3. Get binary mask on the 2D projection
    //  4. link the boundary to get the signal territory 
    //  5. denoise using the binary territory mask by removing axon/neuropil signal outside of the mask.
    //
    void get2DBoundaryViaProjection(int index1, int index2, int[] neuropilMinZ, int[] neuropilMaxZ, ImagePlus  imgp)
    {
	
	//Reuired for EXCLUSIVE.  
 	combineAxonAndNeuropil(index1, index2, imgp.getStack());
	
	//do z-project (on RGB)
	ZProjector projector = new ZProjector(imgp);
	projector.setMethod(ZProjector.MAX_METHOD);
	projector.doRGBProjection();
	ImagePlus projected = projector.getProjection();

	//get the index 2 channel
        byte[] red = new byte[Width*Height];
        byte[] gre = new byte[Width*Height];
        byte[] blue = new byte[Width*Height];
        byte[] combinedChannel=null;   
        ((ColorProcessor)projected.getProcessor()).getRGB(red,gre,blue);
	if(index2 == 0)
               combinedChannel = red;
        else if(index2 == 1)
		combinedChannel = gre;
	else  
		combinedChannel = blue;
	ImagePlus combinedImage = new ImagePlus("projected neuropil channel", new ByteProcessor(Width, Height,combinedChannel, null));
	
	//get a binary mask ( using RATS? or automatic without threshold input? before enhance, so threshold can be different?)
	//5/22: Conclusion (on ddaC-A2): RATS give more details.  autoThreshold give a rough boundary, which is more conservative for noise removal.
	/*String opt = "noise=5 lambda=3 min=20";
        RATSForAxon  rats = new RATSForAxon();
	rats.setup("",combinedImage); 
	ImagePlus maskImageComb = rats.run(combinedImage.getProcessor(),opt);
	maskImageComb.show();
        byte[] mask = (byte[]) maskImageComb.getProcessor().getPixels();
        */
	//use auto-threshold default in ImageJ?
	//can this be done on the RGB image directly, so that axon signal is considered?
	//The documentation seems not the same as the acutal effect (which works on a converted graylevel instead of working on 3 channels separately)
	combinedImage.getProcessor().autoThreshold();
	if (show_mask)	combinedImage.show();
  	byte[] mask = (byte[]) combinedImage.getProcessor().getPixels();

	getNeuroBoundary(mask, neuropilMinZ, neuropilMaxZ, false);

    }


    //**********************************************************************************************************
    //
    // the main calculating entrance for T.I. calculation.
    //
    //**********************************************************************************************************
    float[] getTopoIndex(int index1, int index2, int ThrVal, int ThrVal2, ImageStack stack) 
    {
	//x: direction of AP
	//y: direction of DV    
	//z: direction of the cross sectin (left-right)
        int x, y, z;	    
	int neuropilMinZ, neuropilMaxZ, axonMinZ, axonMaxZ;
	float ti =0, tisum =0;
	int voxelCount = 0;  //total # of axon terminal voxels;
        int[] PixVal;
	int valNeu, valAxon;
	float terr_proportion =0;  //proportion of territory
	float terr_sum =0 ;
	int terr_count = 0;
        byte[] red,gre,blue;
       	ImagePlus aySliceImgObj=null, aySliceImgNeu=null;
	float res[] = new float[8]; 
	float intensAxon[] = new float[2];
	float intensNeu[] = new float[2];
	float intensBack[] = new float[2];
	ColorProcessor aySlice=null;
	int validTISliceCount = 0;
	int validTeSliceCount = 0;

	for(int i=0; i<res.length; i++) res[i] =0; 
	for(int i=0; i<intensAxon.length; i++) intensAxon[i] =0; 
	for(int i=0; i<intensNeu.length; i++) intensNeu[i] =0; 
	for(int i=0; i<intensBack.length; i++) intensBack[i] =0; 

	String opt = "noise="+ThrVal+" lambda=3 min="+ leafSizeAxon;
	String opt2 = "noise="+ThrVal2+" lambda=3 min=" + leafSizeNeuropil;
		
	ImageStack maskOneStack = null, maskTwoStack = null, enhancedStack = null;
	ImageStack denoisedAxon = null,  flippedNeuro = null;
	if (show_mask)
	{
	    maskOneStack = new ImageStack(Width, Height);
	    maskTwoStack = new ImageStack(Width, Height);
	    denoisedAxon =  new ImageStack(Width, Height);
	    flippedNeuro = new ImageStack(Width, Height); 
	    if(enhanceNeuropil)  enhancedStack = new ImageStack(Width, Height);
	}

	//5/17/2012 get the 2D territory boundary based on Z-projection. Pass in the global ImagePlus.
	//Will be used later after getting masks of each frame.
	//duplicate the imageplus to avoid interfere with later steps?!
 	int[] twoDNeuMinZ = new int[Width];
	int[] twoDNeuMaxZ = new int[Width];
	//different from INCLUSIVE, this will change the original image. So no duplicate.
	//Combination of 2 channels is done inside this method.
	get2DBoundaryViaProjection(index1, index2, twoDNeuMinZ, twoDNeuMaxZ, img);

	//automatic enhancement based on neuropil intensity  4/2012
	//Rule: If less than 35, discard. Else if less than 80, increase to above 80 (but lower than 100).   Otherwise, no enhancement.
	int interval = 10;
	int max = 255;
	int neuroInt = calAverageBgIntensityForStack(stack, ThrVal2, index2);
	ColorProcessor duplicatedImp;
	ImageStack  duplicatedStack;
	IJ.log("starting neuropil intensity: " + neuroInt);		
	if(neuroInt < ENHANCE_BASE)
	{  IJ.log("Neuropil Intensity is only " + neuroInt + " (< "+  ENHANCE_BASE +"). Exit.");
	   return null;
	} else if (neuroInt < ENHANCE_TARGET )
	{
	   //auto adjust to enhance the intensity
	   if(useCLAHE)
	   { 
	    IJ.log("Enhance Neuropil Using CLAHE .. ");
	    duplicatedStack = new ImageStack(Width, Height);	
	    for(z=0; z<NbSlices;z++)
	    {
	      red = new byte[Width*Height];
  	      gre = new byte[Width*Height];
    	      blue = new byte[Width*Height];
	      aySlice = (ColorProcessor) stack.getProcessor(z+1);
	      aySlice.getRGB(red,gre,blue);
	      if(index2==0)
	        aySliceImgNeu = new ImagePlus("channel for Neu", new ByteProcessor(Width, Height,red, null));
 	      if(index2==1)
	       aySliceImgNeu = new ImagePlus("channel for Neu",new ByteProcessor(Width, Height,gre, null));
  	      if(index2==2)
	       aySliceImgNeu = new ImagePlus("channel for Neu",new ByteProcessor(Width, Height,blue, null));

              //parameters are for blockRadius, bins, slope
	      HistogramEq.run(aySliceImgNeu, 20, 255, 3.00f, null, null);	
	      //combine back into a ColorImage:
	      ColorProcessor currentSlice = new ColorProcessor(Width, Height);
	      for(int i=0; i<Width; i++)
		 for(int j=0; j<Height; j++)
		 {
	            int[] iArray = new int[3];		 
		    iArray[0] = red[j*Width+i];
		    iArray[1] = gre[j*Width+i];
		    iArray[2] = blue[j*Width+i];
		    currentSlice.putPixel(i,j,iArray);
		 }
	      duplicatedStack.addSlice("dup",currentSlice);
	    }
	    stack = duplicatedStack; 
	   }
	   else
	   {
	     //only adjust the neuroChannel (for r or g or b) 1. only setMinAndmax for it;  2. only enhance (doing histogram equalization) for it. 
	     //4,2,1 are for r, g, b in ColorProcessor's applyTable(int[] lut, int channel), as called by  setMinAndMax;
 	     int neuroChannel = 4;
	     if (index2 == 1) neuroChannel = 2;
	     else if (index2 == 2) neuroChannel = 1;

     	     duplicatedStack = new ImageStack(Width, Height);	
	     //adjust level/window by move the max down?
	     max  -= interval;
	     //adjust for all slices
	     for(z=0; z<NbSlices;z++)
	     {
   	       aySlice = (ColorProcessor) stack.getProcessor(z+1);
	       duplicatedImp = (ColorProcessor) aySlice.duplicate();
  	       duplicatedImp.setMinAndMax(0, max, neuroChannel); //for ColorProcessor, pixel value will be changed (which channel?)
	       duplicatedStack.addSlice("dup",duplicatedImp);
	     }
	     while((neuroInt = calAverageBgIntensityForStack(duplicatedStack, ThrVal2, index2)) < ENHANCE_TARGET  && max > interval)
	     {
	        duplicatedStack = new ImageStack(Width, Height);	//clear up.	   
	        IJ.log("current neuropil intensity: " + neuroInt);		
	        max = max - interval;
	        //adjust for all slices
	        for(z=0; z<NbSlices;z++)
	        {
	          aySlice = (ColorProcessor) stack.getProcessor(z+1);
 	          duplicatedImp = (ColorProcessor) aySlice.duplicate();
  	          duplicatedImp.setMinAndMax(0, max, neuroChannel); //for ColorProcessor, neuro channel's pixel value will be changed
	          duplicatedStack.addSlice("dup",duplicatedImp);
	       }
	   }
	   if (max <= interval)
	     IJ.log("Can't enhance to desirable intensity: window is already 0.");
	   else
           {
             IJ.log("Target reached. Current neuropil intensity: " + neuroInt);		
	     stack = duplicatedStack; //assign the enhanced one
	   }
	   }
	}//else do nothing because it is already > 80.

	//calculate TI and relative TI
	for(z=0; z<NbSlices; z++)
        {
	  aySlice = (ColorProcessor) stack.getProcessor(z+1);

	  //... despeckle, smooth, 	  //territory extraction for channel 1	  //rolling ball + binarization	  //territory extraction for channel 2
	
	  //segmentation
	  //call RATS for binarization of each channel 
          red = new byte[Width*Height];
  	  gre = new byte[Width*Height];
    	  blue = new byte[Width*Height];
	  aySlice.getRGB(red,gre,blue);

	  //axon
	  if(index1==0) //create an image (or use NewImage class)
	     aySliceImgObj = new ImagePlus("channel for obj (axon)", new ByteProcessor(Width, Height,red, null));
	  else if(index1==1)
  	     aySliceImgObj = new ImagePlus("channel for obj (axon)",new ByteProcessor(Width, Height,gre, null));
	  else if(index1==2)
  	     aySliceImgObj = new ImagePlus("channel for obj (axon)",new ByteProcessor(Width, Height,blue, null));

	  RATSForAxon rats = new RATSForAxon();
	  rats.setup("",aySliceImgObj); 
	  ImagePlus maskImageAxon = rats.run(aySliceImgObj.getProcessor(),opt);

	  //calculate intensity for current slice for axon based on mask, return total, and # of voxels
	  int avgAxonArr[] = calIntensity(aySliceImgObj.getProcessor(), maskImageAxon.getProcessor());	
	  intensAxon[0]  += avgAxonArr[0];
	  intensAxon[1]  += avgAxonArr[1];

	  //Neuropil
	  if(index2==0)
	      aySliceImgNeu = new ImagePlus("channel for Neu", new ByteProcessor(Width, Height,red, null));
	  if(index2==1)
	      aySliceImgNeu = new ImagePlus("channel for Neu",new ByteProcessor(Width, Height,gre, null));
  	  if(index2==2)
	      aySliceImgNeu = new ImagePlus("channel for Neu",new ByteProcessor(Width, Height,blue, null));

	  // enhance --  needed for red channel only
	  /*ImagePlus backup = null;
	  if(enhanceNeuropil)
	  { 
            backup = aySliceImgNeu.duplicate();
            //parameters are for blockRadius, bins, slope
	    HistogramEq.run(aySliceImgNeu, 20, 255, 3.00f, null, null);	
	  }*/

          rats = new RATSForAxon();
	  rats.setup("",aySliceImgNeu); 
	  ImagePlus maskImageNeu = rats.run(aySliceImgNeu.getProcessor(),opt2);

	  //5/22/2012: DENOISE CURRENT SLICE BASED ON 2D BOUNDARY GOT EARLIER
	  denoiseBasedOnBoundary(twoDNeuMinZ, twoDNeuMaxZ, maskImageAxon.getProcessor());
	  denoiseBasedOnBoundary(twoDNeuMinZ, twoDNeuMaxZ, maskImageNeu.getProcessor());

	  int avgNeurArr[] = calIntensity(aySliceImgNeu.getProcessor(), maskImageNeu.getProcessor());
	  intensNeu[0] += avgNeurArr[0];
	  intensNeu[1] += avgNeurArr[1];
	  /*if(enhanceNeuropil)
	  {
   	    int avgBakArr[] = calIntensity(backup.getProcessor(), maskImageNeu.getProcessor());
	    intensBack[0] += avgBakArr[0];
	    intensBack[1] += avgBakArr[1];
	  }*/
          
	  if(show_mask)
	  {
	     maskOneStack.addSlice("axon", maskImageAxon.getProcessor());
	     maskTwoStack.addSlice("neuropil mask", maskImageNeu.getProcessor());
	     if(enhanceNeuropil)  enhancedStack.addSlice("enhaned neuropil", aySliceImgNeu.getProcessor());
	  }

	  //call calculation for TI
	  IJ.log("slice "+(z+1));
	  IJ.log(" -- Caculating T.I. .. ");
          float[] resfory = getTopoIndexGivenTwoImages(maskImageAxon, maskImageNeu, InclusiveLimit);
	  if(resfory !=null) 
	  {
	    res[0] += resfory[0];
	    res[1] += resfory[1];
	    res[2] += resfory[2];
	    res[3] += resfory[3];
	    
	    //11/22/2012   Add volumes of each slice together
	    res[4] += resfory[4];
	    res[5] += resfory[5];
	  }
	  else
	    IJ.log(" -- The slice is skipped for TI calculation!");	  

	  /*  05/26/2012   No need to calculate RTI for exclusive data
	  // calculate relative TI based on flipped out image	
	  IJ.log(" -- Caculating Relative T.I. after flipping the neuropil image .. ");
	  ImagePlus[] flippedImages = getImageUsingComptationalFlipOut(maskImageAxon.duplicate(), maskImageNeu.duplicate());
	  if(flippedImages !=null)
	  {
  	  // if(show_mask)
	  // {
	  //   denoisedAxon.addSlice("denoised axon", flippedImages[0].getProcessor());
	  //   flippedNeuro.addSlice("flipped neuropil", flippedImages[1].getProcessor());
	  // }
            resfory = getTopoIndexGivenTwoImages(flippedImages[0], flippedImages[1], EXCLUSIVELIMIT);
	    res[4] += resfory[0];
	    res[5] += resfory[1];
	    res[6] += resfory[2];
	    res[7] += resfory[3];
	  }
	  else
	    IJ.log(" -- The slice is skipped for relative TI calculation (possibly a black slice)!");	  
	  */


	}//end of all Z

	//normalized for y
	IJ.log("---- Final Result For The Neuron -----");
	IJ.log("Average Intensity:" );
	if(enhanceNeuropil)
	   IJ.log("   Neuropil Before Enhancment:" + intensBack[0]/intensBack[1]); 
	IJ.log("   Neuropil: " + intensNeu[0]/intensNeu[1]);
	IJ.log("   Axon: " + intensAxon[0]/intensAxon[1]);
	IJ.log("Topographic Index:"+res[0]/res[1]);
	IJ.log("volume (# of voxles):  neuropil: "+ res[4] + " axon: " +  res[5] + " ratio: " + res[5]/res[4]);

	//IJ.log("Relative Topographic Index:"+res[4]/res[5]);
	//IJ.log("Territory Proportion:"+res[2]/res[3]);
	
	if(show_mask)
	{
  	  if(enhanceNeuropil)  
	  {
	     ImagePlus neuHE = new ImagePlus(img.getTitle()+".neuropil enhanced.tif", enhancedStack) ;
	     neuHE.show();
	  }

    	  ImagePlus maskOne = new ImagePlus(img.getTitle()+".axon terminal binary mask.tif", maskOneStack);
	  ImagePlus maskTwo = new ImagePlus(img.getTitle()+".neuropil binary mask.tif", maskTwoStack);
          maskOne.show();
	  maskTwo.show();	  

	  //ImagePlus maskThree = new ImagePlus(img.getTitle()+".axon denoised.tif", denoisedAxon);
	  //ImagePlus maskFour = new ImagePlus(img.getTitle()+".flipped neuropil.tif", flippedNeuro);
	  //maskThree.show();
	  //maskFour.show();
	}
	return res;
     }

     //
     // Get D/V boundary by linking neighbors
     //
     boolean getNeuroBoundary(byte[] mask2,  int[] neuropilMinZ, int[] neuropilMaxZ, boolean printBoundary)
     {
        int x,y;
	int valNeu;
	int sumEmptyNeuropilX = 0;

        for(x=0; x<Width; x++)	
	 {
	     neuropilMinZ[x] = Height; neuropilMaxZ[x] = 0;
	     for (y=0; y<Height; y++)
	     {
                 valNeu = mask2[y*Width+x]&0xff;
  	         if(valNeu != 0)  //white spot on mask
	         {
	            if (y < neuropilMinZ[x])  
		      neuropilMinZ[x] = y;
                    if (y > neuropilMaxZ[x])
	              neuropilMaxZ[x] = y;
	        }
	     }
	 }
	 //link neighbors for boundary
	 int leftMinZ, rightMinZ, leftMaxZ, rightMaxZ;
	 int leftIndex, rightIndex;
	 for(x=0; x<Width; x++)	
	 {
            if(neuropilMinZ[x] == Height) //empty x
	    {
		sumEmptyNeuropilX++;
		leftIndex = x;
		while(leftIndex>0 && neuropilMinZ[leftIndex] == Height)
		   leftIndex--;	
                leftMinZ = neuropilMinZ[leftIndex]; 
                leftMaxZ = neuropilMaxZ[leftIndex]; 

		rightIndex = x;
		while(rightIndex<Width-1 && neuropilMinZ[rightIndex] == Height)
		   rightIndex++;	
   		rightMinZ = neuropilMinZ[rightIndex];
		rightMaxZ = neuropilMaxZ[rightIndex];
		if(leftMinZ == Height) //didn't find a good left neighbor, will just use right neighbor
		{
		     if (rightMinZ == Height) // neither is good (the entire slice is empty) 	
			     return false;
		     neuropilMinZ[x] = rightMinZ;
		     neuropilMaxZ[x] = rightMaxZ;
		}	     
		else if(rightMinZ == Height) //didn't find a good right neighbor, will just use left neighbor
		{
		     neuropilMinZ[x] = leftMinZ;
		     neuropilMaxZ[x] = leftMaxZ;
		}
		else  // both are good.  
		{
		     neuropilMinZ[x] = (leftMinZ + rightMinZ)/2;
		     neuropilMaxZ[x] = (leftMaxZ + rightMaxZ)/2;
		}
	    }//end of that empty x
	    //IJ.log(x+": min:"+ neuropilMinZ[x] + "max" + neuropilMaxZ[x]);
	 }//end of all x
	 
	 //IJ.log(" -- ratio of empty vertical lines in neuropil that needs linking (%):"+(sumEmptyNeuropilX*100.0)/Width);
	 //find avg min and max
	 if (printBoundary)
	 {
	  int summax = 0; int summin =0;
	  for(x=0; x<Width; x++)
	  {
             summin += neuropilMinZ[x];
             summax += neuropilMaxZ[x];	     
	  }
	  IJ.log("   -- Dorsal limit (avg):"+summin/Width+" ; Ventral limit (avg):"+summax/Width);
	 }

	 return true;
     }

     //
     //Computational flip out:
     //1. remove noise from green channel (all that are beyong the red channel's binary mask boundary are removed)
     //2. flip out: set red channel to 0 if green channel is 255.
     //3. calulate relative T.I. (set the limit to infinitiy, any green voxel that are beyong the boundary of red chanel are 0 or 1)
     //return:
     //   null:  the neuImg is all black.
     //   Or the denoised axon and the flipped neuroImage
     //
     ImagePlus[] getImageUsingComptationalFlipOut(ImagePlus axonImg, ImagePlus neuImg)
     {
	 int x,y;
	 int axonVal;
       	 byte[] mask1 = (byte[]) axonImg.getProcessor().getPixels();
         byte[] mask2 = (byte[]) neuImg.getProcessor().getPixels();

	 //set boundary
 	 int[] neuropilMinZ = new int[Width];
	 int[] neuropilMaxZ = new int[Width];
 	 //IJ.log("     -- get neuropil boundary for noise removal of axon");
	 if(!getNeuroBoundary(mask2, neuropilMinZ, neuropilMaxZ, false))
		 return null;

	 //remove noise based on neuropil boundary 
	 //(Note: the boundary may change after flip, which will need to relink.)
	 for(x=0; x<Width; x++)	
	 {
 	    for (y= 0; y< neuropilMinZ[x]; y++)
	       mask1[y*Width+x] = 0;
 	    for (y = neuropilMaxZ[x]+1; y< Height; y++)
	       mask1[y*Width+x] = 0;
	 }
	 //get a denoised axon. optional for T.I. since they will be exluded with limit = 0, but needed for calulative relative T.I.
	 ImagePlus denoisedAxon =new ImagePlus("denoised channel for axon", new ByteProcessor(Width, Height,mask1, null)); 
         
	 //flip: set neuropil to 0 if axon is not 0. Simulate "exclusive" data.
	 for(x=0; x<Width; x++)	
	 {
 	  for (y=0; y< Height; y++)
	  {
	     axonVal =	mask1[y*Width+x]&0xff;
	     if(axonVal !=0)
	       mask2[y*Width+x] =0;
	  }
	 }
  
	//get a new flipped image for neuropil
	ImagePlus flippedNeuro =new ImagePlus("flipped channel for neuropil", new ByteProcessor(Width, Height,mask2, null)); 

	ImagePlus[] resImgs= new ImagePlus[2];
	resImgs[0] = denoisedAxon; 
	resImgs[1] = flippedNeuro;
	return resImgs;
     }

    
    //
    //calculate topoindex for two binary mask
    //This is for a given slice
    //
    //limit: The number of voxels that will be included in caculation beyond boundary.
    //
    //For calculating T.I. of inclusive set: the limit is 0.
    //
    //For calculating relative T.I. of exclusive set: the limit is height to include all axon voxels on the side.
    //
    //return:
    //    res[0] = tisum;
    //   res[1] = voxelCount; 
    //   res[2] = terr_sum;
    //   res[3] = terr_count;
    //
    //
    float[] getTopoIndexGivenTwoImages(ImagePlus axonImg, ImagePlus neuImg, int limit)
    {

    	float ti =0, tisum =0;
	int voxelCount = 0;  //total # of axon terminal voxels;
	int valNeu, valAxon;
	float terr_proportion =0;  //proportion of territory
	float terr_sum =0 ;
	int terr_count = 0;
        int x,y;       
	int neuropilMinZ, neuropilMaxZ, axonMinZ, axonMaxZ;
 
	byte[] mask1 = (byte[]) axonImg.getProcessor().getPixels();
        byte[] mask2 = (byte[]) neuImg.getProcessor().getPixels();

	//set boundary
 	int[] neuropilMinY = new int[Width];
	int[] neuropilMaxY = new int[Width];
	//IJ.log("     -- get neuropil boundary for calculating topo index ");
	if(!getNeuroBoundary(mask2, neuropilMinY, neuropilMaxY, true))
		 return null;

	//need boundary of axon for volume, 11/22/2012
	int[] axonMinY = new int[Width];
	int[] axonMaxY = new int[Width];
	if(!getNeuroBoundary(mask1, axonMinY, axonMaxY, true))
		 return null;


	//data hold for volumes
	int[] volumesForCurrentX = new int[2];
	int neuropilVolume = 0;
	int cloneVolume = 0;

        for(x=0; x<Width; x++)	
	  {
             neuropilMinZ = neuropilMinY[x]; 
	     neuropilMaxZ = neuropilMaxY[x];

	     //now look at the inner object in another channel  
	     axonMinZ = Height; axonMaxZ = 0;	  
	     //find out how many z are there in the axon terminal (index 1 channel)
	     for (y=0; y<Height; y++)
	     {
	       valAxon = mask1[y*Width+x]&0xff;
	       //check if it is a valid axon voxel and there is sth in the other channel too, if yes, calculate 
	       if ( valAxon != 0 && neuropilMinZ != Height)
	       {
     	         ti =  calcTopologicalIndexForOneVoxel(y, neuropilMinZ, neuropilMaxZ, limit);
 	         if (ti >= 0)  //if ti is < 0, the porportion of ti is not right, does not count (noise)
	         {
                  voxelCount++;
	          tisum += ti;
		
		  //for territory
                  if (y < axonMinZ)  
		      axonMinZ = y;
                  if (y > axonMaxZ)
	              axonMaxZ = y;
		 } 
	      }
	     } //end of 2nd z loop

             //calculate territory proportion if there is sth in both channels
	     if (axonMinZ != Height && neuropilMinZ != Height)
	     {
	       terr_proportion = calcTerritoryPortion(axonMaxZ, axonMinZ, neuropilMaxZ, neuropilMinZ, limit);
	       if (terr_proportion >=0) //only if it is a reasonable one 
	       {
	         terr_count ++;	     
 	         terr_sum +=terr_proportion;
	       }
	     }

	     //calculate volume ratio. Different from territory porportion, it sums up the voxel num in both clone and neuropil
	     //The method return the volume total for current x
	    if (calcVolumeSum(axonMaxY[x], axonMinY[x], neuropilMaxY[x], neuropilMinY[x], limit, volumesForCurrentX) == -1)
	    {
		//sth wrong with the calucation. Should not happen if the boundaries are already linked. Discard this x?
		IJ.log("  Something wrong with the volume calculation! ");	
	    }
	    else
	    {
	       neuropilVolume += volumesForCurrentX[0];
               cloneVolume += volumesForCurrentX[1]; //Different from voxelCount because it does not check if valAxon is 0 -- so it includes dark voxels within territory
	    }

	 }//end of x
       
 
       float[] res = new float[6];
       res[0] = tisum;
       res[1] = voxelCount; 
       res[2] = terr_sum;
       res[3] = terr_count;
       res[4] = neuropilVolume;
       res[5] = cloneVolume;
       	
       /*
       IJ.log("  -- Total number of voxels in the axon terminal: " + voxelCount);	
       IJ.log("  -- Topographic index for the given plane : " + res[0]);
       IJ.log("  -- Total territory for the given plane: " + terr_sum);
       IJ.log("  -- Total number of x for territory calculation: " + terr_count);
       IJ.log("  -- Size-Normalized territory for the given plane: " + res[1]);
       */

       return res;
    }

   
    void analyze() {
        IJ.showStatus("Measure Center of Mass in a  3D Image Stack");

        debug = IJ.debugMode;
        int x, y, z;

        //set channel index
        int index1 =0, index2 =0;
      	if (or == true)      	  index1 = 0;
      	else if (og == true)  	  index1 = 1;
      	else if (ob == true)      index1 = 2;
	if (br == true)      	  index2 = 0;
      	else if (bg == true)  	  index2 = 1;
      	else if (bb == true)    index2 = 2;
      	

	Calibration cal = img.getCalibration();
	double zOrigin = cal.zOrigin;
	double yOrigin = cal.yOrigin;
	double xOrigin = cal.xOrigin;
	//IJ.log("zOrigin:" +xOrigin + " yOrigin" + yOrigin + " zOrigin" + zOrigin);

        //preprocessing is still needed 
        if(despeckle)
        {
 	  //Apply despeckle : median filter with radius 1
          IJ.showStatus("Despeckling ...");
		  RankFilters filter = new RankFilters();
          ImageStack stack = img.getStack();
          for (z=1; z<=NbSlices; z++) {
            ip = stack.getProcessor(z);
		    filter.rank(ip, 1, RankFilters.MEDIAN);
	        IJ.showProgress(z,NbSlices);
          }
        }
        if(smooth)
        {
       	    //This filter replaces each pixel with the average of its 3x3 neighborhood.
            IJ.showStatus("Smoothing ...");
            ImageStack stack = img.getStack();
            for (z=1; z<=NbSlices; z++) {
                ip = stack.getProcessor(z);
               	ip.smooth();
                IJ.showProgress(z,NbSlices);
            }
        }
     	
	ImageStack stack = img.getStack();

	//getCoM(index1, ThrVal, stack);
        //getGeometricCenter(index1, ThrVal, stack);

	//get topographic index
	float[] res = getTopoIndex(index1, index2, ThrVal, ThrVal2,stack);

	//save TI, and volume info to a file (append)
	if(!outputfilename.trim().equals(""))
	{
	 try{
         java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileOutputStream(new java.io.File(outputfilename), true)); 
	 writer.println(img.getTitle()+","+res[0]/res[1]+","+res[5]+","+res[4]+","+res[5]/res[4]);
	 writer.close();
	 }catch(Exception e)
	 {
	   IJ.log("Problem in saving the result into "+ outputfilename + "--"+e.getMessage());
	   return;
	 }
	 IJ.log("Result appended to "+outputfilename +" (at ImageJ's folder by default).");
	}
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
    	
        ThrVal=((Scrollbar)sliders.elementAt(0)).getValue();
    }

    public void textValueChanged(TextEvent e) {
        ((Scrollbar)sliders.elementAt(0)).setValue((int) Tools.parseDouble(((TextField)value.elementAt(0)).getText()));
  
    }

 
}
