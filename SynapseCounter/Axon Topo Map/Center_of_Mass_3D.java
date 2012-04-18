/*
 *
 * This is a simple measurement of objects in a RGB image stack,
 *  to get Center of Mass of a particular channel.
 *
 *  3/8/2011  Jie Zhou
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

public class Center_Of_Mass_3D implements PlugIn, AdjustmentListener, TextListener, Runnable {
    static final String pluginName = "Center Of Mass 3D";
    boolean debug = false;

    Vector sliders;
    Vector value;

    //some variable are to be removed.
    public static int minSize_default = 10;
    public static int maxSize_default = Integer.MAX_VALUE;
    public static boolean showParticles_default = true;
    public static boolean showEdges_default = false;
    public static boolean showCentres_default = false;
    public static boolean showCentresInt_default = false;
    public static int DotSize_default = 3;
    public static boolean showNumbers_default = false;
    public static boolean new_results_default = true;
    public static int FontSize_default = 12;
    public static boolean summary_default = true;
    public static boolean or_default = true;
    public static boolean og_default = false;
    public static boolean ob_default = false;

    int ThrVal =0;
    int pixelDepthInNM;
    int two_channel_proximity;
    int minSize;
    int maxSize;
    int DotSize;
    boolean showNumbers;
    boolean new_results;
    int FontSize;
    boolean summary;
    boolean or, og, ob, br, bg, bb;
 
    ImagePlus img;
    ImageProcessor ip;

    int Width;
    int Height;
    int NbSlices;
    int arrayLength;
    String imgtitle;
    int PixVal;

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

        GenericDialog gd=new GenericDialog("Center_Of_Mass_3D");
	
	gd.addSlider("Threshold for object channel: ",ip.getMin(), ip.getMax(),ThrVal);
        sliders=gd.getSliders();
        ((Scrollbar)sliders.elementAt(0)).addAdjustmentListener(this);
        value = gd.getNumericFields();
        ((TextField)value.elementAt(0)).addTextListener(this);
    	gd.addCheckbox("Object channel is red", or_default);
        gd.addCheckbox("Object channel is green", og_default);
        gd.addCheckbox("Object channel is blue", ob_default);
        gd.showDialog();
        
        if (gd.wasCanceled()){
            ip.resetThreshold();
            img.updateAndDraw();
            return false;
        }

        ThrVal=(int) gd.getNextNumber();
        or =gd.getNextBoolean();            or_default = or;
        og =gd.getNextBoolean();            og_default = og;
        ob =gd.getNextBoolean();            ob_default = ob;

        IJ.register(Center_Of_Mass_3D.class); // static fields preserved when plugin is restarted
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
	 Com[2] = totalz/intensity;

	 return CoM;

    }
    
    
    void analyze() {
        IJ.showStatus("Measure Center of Mass in a  3D Image Stack");

        long start=System.currentTimeMillis();
        debug = IJ.debugMode;
        int x, y, z;
        int xn, yn, zn;
        int i, j, k, arrayIndex, offset;
        int voisX = -1, voisY = -1, voisZ = -1;
        int maxX = Width-1, maxY=Height-1;

        int index;
        int val;
        double col;

        short minTag;
        short minTagOld;
        
        //set channel index
        int index1 =0, index2 =0;
      	if (or == true)      	  index1 = 0;
      	else if (og == true)  	  index1 = 1;
      	else if (ob == true)      index1 = 2;
      	
      	ImageStack stack = img.getStack();

	float[] CoM = getCoM(index1, ThrVal, stack);
	java.text.DecialFormat df = new java.text.DecimalFormat("#.00");

	IJ.showStatus("Center of Mass for the Selected Channel (x, y, z): " +df.format(CoM[0])+" , " + df.format(CoM[1]) + "," + df.format(CoM[2]) + ".");
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
    	
        ThrVal=((Scrollbar)sliders.elementAt(0)).getValue();
    }

    public void textValueChanged(TextEvent e) {
        ((Scrollbar)sliders.elementAt(0)).setValue((int) Tools.parseDouble(((TextField)value.elementAt(0)).getText()));
  
    }

 
}
