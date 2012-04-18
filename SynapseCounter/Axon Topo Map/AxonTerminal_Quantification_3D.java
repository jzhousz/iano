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

public class AxonTerminal_Quantification_3D implements PlugIn, AdjustmentListener, TextListener {
    static final String pluginName = "AxonTerminal Quantification 3D";
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
   
    int ThrVal =10;
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

        GenericDialog gd=new GenericDialog("AxonTerminal_Quantification_3D");
	
	gd.addSlider("Threshold for object channel: ",ip.getMin(), ip.getMax(),ThrVal);
        sliders=gd.getSliders();
        ((Scrollbar)sliders.elementAt(0)).addAdjustmentListener(this);
        value = gd.getNumericFields();
        ((TextField)value.elementAt(0)).addTextListener(this);
        //gd.addCheckbox("Despeckle the image first", despeckle_default);
        //gd.addCheckbox("Smooth the image first", smooth_default);
	//
	gd.addCheckbox("Show the segmentation masks", show_mask_default);
    	gd.addCheckbox("Clone channel is red", or_default);
        gd.addCheckbox("Clone channel is green", og_default);
        gd.addCheckbox("Clone channel is blue", ob_default);
        gd.addCheckbox("Neuropil channel is red", br_default);
        gd.addCheckbox("Neuropil channel is green", bg_default);
        gd.addCheckbox("Neuropil channel is blue", bb_default);
  
        gd.showDialog();
        
        if (gd.wasCanceled()){
            ip.resetThreshold();
            img.updateAndDraw();
            return false;
        }

        ThrVal=(int) gd.getNextNumber();
        //despeckle = gd.getNextBoolean();     despeckle_default = despeckle;
        //smooth = gd.getNextBoolean(); 	     smooth_default = smooth;
	show_mask = gd.getNextBoolean();      show_mask_default = show_mask;
        or =gd.getNextBoolean();             or_default = or;
        og =gd.getNextBoolean();             og_default = og;
        ob =gd.getNextBoolean();             ob_default = ob;
        br =gd.getNextBoolean();             br_default = br;
        bg =gd.getNextBoolean();             bg_default = bg;
        bb =gd.getNextBoolean(); 	     bb_default = bb;
  

        IJ.register(AxonTerminal_Quantification_3D.class); // static fields preserved when plugin is restarted
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
    float calcTopologicalIndexForOneVoxel(int z, int minz, int maxz)
   {
	int limit = 5;
	if  (z < minz || z > maxz) //should not happen but could happen due to segmentation
	{
   	  //axon too far from neuropil will be considered noise.  axon close enough but out of neuropile will be set to 0 or 1 depending on which side.
	  if (z < minz)
	     if ((minz-z) < limit)  //small but < limit, return 0, otherwise it will be negative
		  return 0;
	     else 
		  return -1;
	  if (z > maxz)
	     if ((z - maxz) < limit) 
	          return 1;
	     else
	   	  return -1;  //not valid	     
	}

 	return  (z-minz+1)/((float)(maxz-minz+1));
    }

   //allow some error talerance for segmentation
   //It should return between 0 and 1.
   // -1 indicates error
   float calcTerritoryPortion(int axonMaxZ, int axonMinZ, int neuropilMaxZ, int neuropilMinZ)
    {
	int limit = 5;    
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
  	    IJ.log("individual terr- "+res);
	    return  res;
	}
    }

   
    // issue:  hollow area:  what should I do:
    //
    // average  (smoothing)   more than 3*3, but do a 3D smoothing?
    //
    // estimate shape??
    //
    float[] getTopoIndex(int index1, int index2, int ThrVal, ImageStack stack) 
    {
	//y: direction of the cross sectin (left-right)
	//x: direction of AP
	//z: direction of DV    
        int y, x, z;	    
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
	float res[] = new float[2]; 
	res[0] =0; res[1] =0;
        int count0 =0, count1 =0;

	String opt = "noise="+ThrVal+" lambda=3 min=5";
	//String opt2 = "noise="+thrVal+" lambda=3 min=5";
	ImageStack rotatedImgStack = null;
	ImageStack maskOneStack = null, maskTwoStack = null;
	if (show_switched)
            rotatedImgStack = new ImageStack(Width, NbSlices);
	if (show_mask)
	{
	    maskOneStack = new ImageStack(Width, NbSlices);
	    maskTwoStack = new ImageStack(Width, NbSlices);
	}

	//for a given y  (x-z plane) 
	for(y =0; y<Height; y++)
	//for(y=0; y<10; y++)
        {
      	  //build an image for each channel
	  ColorProcessor aySlice = new ColorProcessor(Width,NbSlices);
          for(x=0; x<Width; x++)
	  {
	     for (z=0; z<NbSlices; z++)
	     {
		ip = stack.getProcessor(z+1);
		PixVal = ip.getPixel(x,y,null);
		aySlice.putPixel(x, z, PixVal); //put into the processor
	     }
	  }
	  if(show_switched)
          {
	    String label = (y+1) + "in left-right direction (cross-section)";
	    rotatedImgStack.addSlice(label,aySlice);
	  }

	  //despeckle, smooth, 	  //enhance --  possibily needed for red channel only
	  //territory extraction for channel 1	  //rolling ball + binarization
	  //territory extraction for channel 2

	  //segmentation
	  //call RATS for binarization of each channel 
          red = new byte[Width*NbSlices];
  	  gre = new byte[Width*NbSlices];
    	  blue = new byte[Width*NbSlices];
	  aySlice.getRGB(red,gre,blue);
	  if(index1==0) //create an image (or use NewImage class)
	     aySliceImgObj = new ImagePlus("channel for obj (axon)", new ByteProcessor(Width, NbSlices,red, null));
	  else if(index1==1)
  	     aySliceImgObj = new ImagePlus("channel for obj (axon)",new ByteProcessor(Width, NbSlices,gre, null));
	  else if(index1==2)
  	     aySliceImgObj = new ImagePlus("channel for obj (axon)",new ByteProcessor(Width, NbSlices,blue, null));
          RATS_ForAxon rats = new RATS_ForAxon();
	  rats.setup("",aySliceImgObj); 
	  ImagePlus maskImageAxon = rats.run(aySliceImgObj.getProcessor(),opt);
	  if(show_mask)
	  {	
	     maskOneStack.addSlice("axon", maskImageAxon.getProcessor());
	  }

	  if(index2==0)
	      aySliceImgNeu = new ImagePlus("channel for Neu", new ByteProcessor(Width, NbSlices,red, null));
	  if(index2==1)
	      aySliceImgNeu = new ImagePlus("channel for Neu",new ByteProcessor(Width, NbSlices,gre, null));
  	  if(index2==2)
	      aySliceImgNeu = new ImagePlus("channel for Neu",new ByteProcessor(Width, NbSlices,blue, null));
          rats = new RATS_ForAxon();
	  rats.setup("",aySliceImgNeu); 
	  ImagePlus maskImageNeu = rats.run(aySliceImgNeu.getProcessor(),opt);
	  if(show_mask)
	  {
		  maskTwoStack.addSlice("neuropil", maskImageNeu.getProcessor());
	  }
          
	  //call calculation for TI
	  IJ.log((y+1)+" slice:");
          float[] resfory = getTopoIndexGivenTwoImages(maskImageAxon, maskImageNeu);

	  //add  
	  res[0] += resfory[0];
	  if(res[0] > 0)  count0 ++;
	  res[1] += resfory[1]; 
	  if(res[1] > 0) count1 ++;
        }//end of all y

	//normalized for y
	IJ.log("total TI:"+res[0]);
	res[0] /= count0; 
	res[1] /= count1;
	IJ.log("TI:"+res[0]);
	IJ.log("Territory:"+res[1]);

	if(show_switched)
	{
    	  ImagePlus rotated = new ImagePlus(img.getTitle()+".y-z-switched.tif", rotatedImgStack);
	  rotated.show();
	}
	if(show_mask)
	{
	  ImagePlus maskOne = new ImagePlus(img.getTitle()+".axon terminal binary mask.tif", maskOneStack);
	  ImagePlus maskTwo = new ImagePlus(img.getTitle()+".neuropil binary mask.tif", maskTwoStack);
          maskOne.show();
	  maskTwo.show();	  
	}

	return res;

     }

    //
    //calculate topoindex for two binary mask
    //This is foa given y
    //
    float[] getTopoIndexGivenTwoImages(ImagePlus axonImg, ImagePlus neuImg)
    {

    	float ti =0, tisum =0;
	int voxelCount = 0;  //total # of axon terminal voxels;
	int valNeu, valAxon;
	float terr_proportion =0;  //proportion of territory
	float terr_sum =0 ;
	int terr_count = 0;
        int x,z;       
	int neuropilMinZ, neuropilMaxZ, axonMinZ, axonMaxZ;
 
	 byte[] mask1 = (byte[]) axonImg.getProcessor().getPixels();
         byte[] mask2 = (byte[]) neuImg.getProcessor().getPixels();

        for(x=0; x<Width; x++)	
	  {
     	     //find out the uplimit and downlimit of z in neuropil (index 2 channel)
	     neuropilMinZ = NbSlices; neuropilMaxZ = 0;
	     for (z=0; z<NbSlices; z++)
	     {
	       valNeu = mask2[z*Width+x]&0xff;
	       //neuropile channel, update minZ and maxZ
	       if(valNeu != 0)  //white spot on mask
	       {
	            if (z < neuropilMinZ)  
		      neuropilMinZ = z;
                    if (z > neuropilMaxZ)
	              neuropilMaxZ = z;
	       }
	     } //end of first z loop
	     
	     //now look at the inner object in another channel  
	     axonMinZ = NbSlices; axonMaxZ = 0;	  
	     //find out how many z are there in the axon terminal (index 1 channel)
	     for (z=0; z<NbSlices; z++)
	     {
	       valAxon = mask1[z*Width+x]&0xff;
	       //IJ.log("axon mask value:"+valAxon);
	       //check if it is a valid axon voxel and there is sth in the other channel too, if yes, calculate 
	       if ( valAxon != 0 && neuropilMinZ != NbSlices)
	       {
       	         //IJ.log("now calculate:"+valAxon);
	         ti =  calcTopologicalIndexForOneVoxel(z, neuropilMinZ, neuropilMaxZ);
 	         //IJ.log("individual ti- "+ti);
	         if (ti >= 0)  //if ti is < 0, the porportion of ti is not right, does not count (noise)
	         {
                  voxelCount++;
	          tisum += ti;
		
		  //for territory
                  if (z < axonMinZ)  
		      axonMinZ = z;
                  if (z > axonMaxZ)
	              axonMaxZ = z;
		 } 
	      }
	     } //end of 2nd z loop

             //calculate territory proportion if there is sth in both channels
	     if (axonMinZ !=NbSlices && neuropilMinZ !=NbSlices)
	     {
	       terr_proportion = calcTerritoryPortion(axonMaxZ, axonMinZ, neuropilMaxZ, neuropilMinZ);
	       if (terr_proportion >=0) //only if it is a reasonable one 
	       {
	         terr_count ++;	     
 	         terr_sum +=terr_proportion;
	       }
	     }
	 }//end of x
       

       //output total ti for the neuron
       float TI = 0;
       if (voxelCount > 0) TI = tisum/voxelCount;

       //normalize territory proportion? due to size difference during imaging??
       float terri_normalized = 0;
       if (terr_count > 0) terri_normalized = terr_sum /terr_count; 

       float[] res = new float[2];
       res[0] = TI; res[1] = terri_normalized;

       IJ.log("  -- Total number of voxels in the axon terminal: " + voxelCount);	
       IJ.log("  -- Topographic index for the given y: " + res[0]);
       IJ.log("  -- Total territory for the given y: " + terr_sum);
       IJ.log("  -- Total number of x for territory calculation: " + terr_count);
       IJ.log("  -- Size-Normalized territory for the given y: " + res[1]);
            
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
	float[] res = getTopoIndex(index1, index2, ThrVal, stack);



    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
    	
        ThrVal=((Scrollbar)sliders.elementAt(0)).getValue();
    }

    public void textValueChanged(TextEvent e) {
        ((Scrollbar)sliders.elementAt(0)).setValue((int) Tools.parseDouble(((TextField)value.elementAt(0)).getText()));
  
    }

 
}
