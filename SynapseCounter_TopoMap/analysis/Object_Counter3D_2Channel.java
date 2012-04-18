package annotool.analysis;
/*
 10/2/2011  Jie Zhou  3D object counter using 2 channels.
 //added several more GUI elements
 // Only works with RBG file.  Thresholding is not supported for RGB in ImageJ

 3D objects counter v1.4, 14/06/06
 Fabrice P Cordelires, fabrice.cordelieres at curie.u-psud.fr
 Thanks to Gabriel Landini for major feedbacks and suggestions

 New features since v1.0:
 Threshold can now be set up using a slider

 Improvements since v1.0:
 Modifications in algorithm: first step of ID attribution followed by a second step of ID minimization (regularization)

 2006/05/31: Version 1.3 (Modifications: Jonathan Jackson, j.jackson at ucl.ac.uk)

 * Added "New_Results Window" option: Use to preserve existing data in the main ImageJ "Results Table"
 Unselect this option if you wish to use the commands "Analyze->Summarize" and "Analyze->Distribution"
 * Results are given in floating point precision
 * Works in "Batch Mode"
 * Improved exectution speed
 * Added methods to facilitate calling from another plugin

 2006/06/14: Version 1.4 (Modifications: Jonathan Jackson, j.jackson at ucl.ac.uk)

 * Plugin remembers user selected options
 * Fixed bug causing particles to wrap-around image boarders

 2007/06/18: Version 1.5 (Modifications: Jonathan Jackson, jjackson at familyjackson dot net )
 (released  2008/06/17)
 * Measurements now spatially calibrated 
 * Affected values:
 *    Volume
 *    Centre of Mass / Centroid
 * 'Surface' value is now expressed as the fraction of voxels on the object surface

 2008/06/18: Version 1.5.1  (Modifications: Jonathan Jackson, jjackson at familyjackson dot net )
 * Fixes bug introduced in version 1.5 causing 'draw particles' to fail for particles centered in the first slice

 */

/* programmers notes:
 *
 * Code changes since v1.2
 *  Paramarray changed from int[] to double[] to increase precision
 *  Changed the way data is read into pict[] so that it works in batch mode
 *
 * Code changes since v1.3
 *  introduced withinBounds method to prevent wrap-around of particles
 *  replaced get...() and set...() methods with direct access to arrays for faster execution
 *      (use if(withinBounds(...)) before accessing array if there is the possibility of out of bounds access)
 *  Returns error when supplied a colour or float image (code changes are necessary to support these data types)
 *  uses arrayIndex when looping over x,y,z - faster execution, as no need to check if withinBounds
 *  thr[] int array replaced with boolean array
 *  used static fields with the suffix _default to remember user options
 *  (the new _default variables were introduced otherwise static fields could cause problems if there are two instances running simultaneously)
 *
 *
 */

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.io.OpenDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;
import ij.util.Tools;

import java.awt.Font;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

public class Object_Counter3D_2Channel implements PlugIn, AdjustmentListener,
		TextListener, Runnable {
	static final String pluginName = "Object Counter3D 2Channel";
	boolean debug = false;

	Vector sliders;
	Vector value;

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
	public static boolean or_default = false;
	public static boolean og_default = true;
	public static boolean ob_default = false;
	public static boolean br_default = false;
	public static boolean bg_default = false;
	public static boolean bb_default = true;
	public static int overriddenPixelDepth_default = 400;
	public static boolean despeckle_default = false;
	public static boolean smooth_default = false;
	public static int two_channel_proximity_default = 2; // the smaller, the
															// restricter (may
															// return smaller
															// count)
	
	public static boolean colorByProximity_default = false;

	int ThrVal = 30;
	int ThrVal2 = 30; // for 2nd channel
	int pixelDepthInNM;
	int two_channel_proximity;
	int minSize;
	int maxSize;
	boolean showParticles;
	boolean showEdges;
	boolean showCentres;
	boolean showCentresInt;
	int DotSize;
	boolean showNumbers;
	boolean new_results;
	int FontSize;
	boolean summary;
	boolean or, og, ob, br, bg, bb;

	int Width;
	int Height;
	int NbSlices;
	int arrayLength;
	String imgtitle;
	int PixVal;
	boolean despeckle, smooth;
	
	boolean colorByProximity;

	boolean[] thr; // true if above threshold
	// int[] pict, pict2; // original pixel values
	short[] tag;
	// boolean[] surf;
	short ID;
	int[] IDarray;
	double[][] Paramarray;

	ImagePlus img, img2;
	ImageProcessor ip, ip2;
	ImageStack stackParticles;
	ImageStack stackEdges;
	ImageStack stackCentres;
	ImageStack stackCentresInt;
	Calibration cal;
	ImagePlus Particles;
	ImagePlus Edges;
	ImagePlus Centres;
	ImagePlus CentresInt;
	ResultsTable rt;
	Thread thread;
	
	//Color pool for color coded visualization 
	int colorPool[] = { 16777215, 3092479, 11730948, 15002703, 1240038,
			15007979, 64, 8388672, 16744448, 8388863 };
	int[] tagColor = null;

	public void run(String arg) {
		if (!setupGUI(arg))
			return;
		analyze();
	}

	public boolean setupGUI(String arg) {
		img = WindowManager.getCurrentImage();
		if (img == null) {
			IJ.noImage();
			return false;
		}
		/*
		 * else if (img.getStackSize() == 1) { IJ.error("Stack required");
		 * return false; }
		 */
		if (img.getType() != ImagePlus.COLOR_RGB) {
			IJ.error("RBG image with channels required for 2 channel analysis");
			return false;
		}

		Width = img.getWidth();
		Height = img.getHeight();
		NbSlices = img.getStackSize();
		arrayLength = Width * Height * NbSlices;
		imgtitle = img.getTitle();

		// does not work with RGB processor-- Thresholding of RGB images is not
		// supported!
		ip = img.getProcessor();
		// ThrVal=ip.getAutoThreshold();
		ip.setThreshold(ThrVal, Math.pow(2, 16), ImageProcessor.RED_LUT);
		img.setSlice((int) NbSlices / 2);
		img.updateAndDraw();

		GenericDialog gd = new GenericDialog(
				"3D objects counter using 2 channels");
		gd.addSlider("Threshold for object channel: ", ip.getMin(),
				ip.getMax(), ThrVal);
		gd.addSlider("Threshold for backbone channel: ", ip.getMin(),
				ip.getMax(), ThrVal2);
		gd.addSlider("Slice: ", 1, NbSlices, (int) NbSlices / 2);
		gd.addSlider("Pixel depth (nm): ", 1, 1000,
				overriddenPixelDepth_default);
		sliders = gd.getSliders();
		((Scrollbar) sliders.elementAt(0)).addAdjustmentListener(this);
		((Scrollbar) sliders.elementAt(1)).addAdjustmentListener(this);
		((Scrollbar) sliders.elementAt(2)).addAdjustmentListener(this);
		((Scrollbar) sliders.elementAt(3)).addAdjustmentListener(this);
		value = gd.getNumericFields();
		((TextField) value.elementAt(0)).addTextListener(this);
		((TextField) value.elementAt(1)).addTextListener(this);
		((TextField) value.elementAt(2)).addTextListener(this);
		((TextField) value.elementAt(3)).addTextListener(this);

		gd.addNumericField("two channel proximity check range: ",
				two_channel_proximity_default, 0);
		gd.addNumericField("Min number of voxels: ", minSize_default, 0);
		gd.addNumericField("Max number of voxels: ",
				Math.min(maxSize_default, Height * Width * NbSlices), 0);
		gd.addCheckbox("Despeckle the image first", despeckle_default);
		gd.addCheckbox("Smooth the image first", smooth_default);
		// add selection for two channels. Only relevant for RGB; radiobutton
		// will be better.
		gd.addCheckbox("Object channel is red", or_default);
		gd.addCheckbox("Object channel is green", og_default);
		gd.addCheckbox("Object channel is blue", ob_default);
		gd.addCheckbox("Backbone channel is red", br_default);
		gd.addCheckbox("Backbone channel is green", bg_default);
		gd.addCheckbox("Backbone channel is blue", bb_default);
		gd.addCheckbox("New_Results Table", new_results_default);
		gd.addMessage("Show:");
		gd.addCheckbox("Particles", showParticles_default);
		gd.addCheckbox("Edges", showNumbers_default);
		gd.addCheckbox("Geometrical centre", showCentres_default);
		gd.addCheckbox("Intensity based centre", showCentresInt_default);
		gd.addNumericField("Dot size", DotSize_default, 0);
		gd.addCheckbox("Numbers", showNumbers_default);
		gd.addNumericField("Font size", FontSize_default, 0);
		gd.addMessage("");
		gd.addCheckbox("Summary", summary_default);
		gd.addCheckbox("Color by proximity (requires input file)", colorByProximity_default);

		gd.showDialog();

		if (gd.wasCanceled()) {
			ip.resetThreshold();
			img.updateAndDraw();
			return false;
		}

		ThrVal = (int) gd.getNextNumber();
		ThrVal2 = (int) gd.getNextNumber();
		gd.getNextNumber();
		pixelDepthInNM = (int) gd.getNextNumber();
		overriddenPixelDepth_default = pixelDepthInNM;
		two_channel_proximity = (int) gd.getNextNumber();
		two_channel_proximity_default = two_channel_proximity;
		minSize = (int) gd.getNextNumber();
		minSize_default = minSize;
		maxSize = (int) gd.getNextNumber();
		maxSize_default = maxSize;
		despeckle = gd.getNextBoolean();
		despeckle_default = despeckle;
		smooth = gd.getNextBoolean();
		smooth_default = smooth;
		or = gd.getNextBoolean();
		or_default = or;
		og = gd.getNextBoolean();
		og_default = og;
		ob = gd.getNextBoolean();
		ob_default = ob;
		br = gd.getNextBoolean();
		br_default = br;
		bg = gd.getNextBoolean();
		bg_default = bg;
		bb = gd.getNextBoolean();
		bb_default = bb;
		new_results = gd.getNextBoolean();
		new_results_default = new_results;
		showParticles = gd.getNextBoolean();
		showParticles_default = showParticles;
		showEdges = gd.getNextBoolean();
		showEdges_default = showEdges;
		showCentres = gd.getNextBoolean();
		showCentres_default = showCentres;
		showCentresInt = gd.getNextBoolean();
		showCentresInt_default = showCentresInt;
		DotSize = (int) gd.getNextNumber();
		DotSize_default = DotSize;
		showNumbers = gd.getNextBoolean();
		showNumbers_default = showNumbers;
		FontSize = (int) gd.getNextNumber();
		FontSize_default = FontSize;
		summary = gd.getNextBoolean();
		summary_default = summary;
		colorByProximity = gd.getNextBoolean();
		colorByProximity_default = colorByProximity;

		IJ.register(Object_Counter3D_2Channel.class); // static fields preserved
														// when plugin is
														// restarted
		// Reset the threshold
		ip.resetThreshold();
		img.updateAndDraw();
		return true;
	}

	// set the mask use 2 channels. Potentially this mask can be set using
	// supervised approaches.
	// It limits the candidate of obeject pixels.
	// input: intensity value of both channels; threshold of both channels
	// output: filled thr (via argument)
	//
	// Only the objects (e.g. synapse) that are associated with (attached to)
	// the foreground of the 2nd channel (e.g. axon)
	// are considered.
	//
	void setMaskUsing2Channels(int index1, int index2, int ThrVal, int ThrVal2,
			ImageStack stack, boolean[] thr) {
		int z, y, x;
		int arrayIndex = 0;
		int pict2val;
		int[] PixVal;
		ImageProcessor ip;
		for (z = 1; z <= NbSlices; z++) {
			ip = stack.getProcessor(z);
			for (y = 0; y < Height; y++) {
				for (x = 0; x < Width; x++) {
					PixVal = ip.getPixel(x, y, null);
					if (PixVal[index1] > ThrVal) {
						pict2val = PixVal[index2];
						if (pict2val > ThrVal2) // on the axon
							thr[arrayIndex] = true;
						else {
							// check if very close to axon
							if (neighboring(z, y, x, stack, ThrVal2, index2))
								thr[arrayIndex] = true;
						}

					}
					arrayIndex++;
				}
			}
		}
	}

	// //check neighbors In pict2
	boolean neighboring(int z, int y, int x, ImageStack stack, int ThrVal2,
			int index2) {

		int index = 0;
		int r;
		int[] PixVal;
		int pict2val;
		ImageProcessor ip;
		for (r = 1; r <= two_channel_proximity_default; r++) {
			if (withinBounds(x, y, z - r)) {

				ip = stack.getProcessor(z - r);
				PixVal = ip.getPixel(x, y, null);
				pict2val = PixVal[index2];
				if (pict2val > ThrVal2)
					return true;
			}
			if (withinBounds(x, y, z + r)) {
				ip = stack.getProcessor(z + r);
				PixVal = ip.getPixel(x, y, null);
				pict2val = PixVal[index2];
				if (pict2val > ThrVal2)
					return true;
			}
			if (withinBounds(x, y - r, z)) {
				ip = stack.getProcessor(z);
				PixVal = ip.getPixel(x, y - r, null);
				pict2val = PixVal[index2];
				if (pict2val > ThrVal2)
					return true;
			}
			if (withinBounds(x, y + r, z)) {
				ip = stack.getProcessor(z);
				PixVal = ip.getPixel(x, y + r, null);
				pict2val = PixVal[index2];
				if (pict2val > ThrVal2)
					return true;
			}
			if (withinBounds(x - r, y, z)) {
				ip = stack.getProcessor(z);
				PixVal = ip.getPixel(x - r, y, null);
				pict2val = PixVal[index2];
				if (pict2val > ThrVal2)
					return true;
			}
			if (withinBounds(x + r, y, z)) {
				ip = stack.getProcessor(z);
				PixVal = ip.getPixel(x + r, y, null);
				pict2val = PixVal[index2];
				if (pict2val > ThrVal2)
					return true;
			}
		}
		return false;
	}

	void analyze() {
		IJ.showStatus("3D Objects Counter using 2 Channels");
		long start = System.currentTimeMillis();
		debug = IJ.debugMode;
		int x, y, z;
		int xn, yn, zn;
		int i, j, k, arrayIndex, offset;
		int voisX = -1, voisY = -1, voisZ = -1;
		int maxX = Width - 1, maxY = Height - 1;

		int index;
		int val;
		double col;

		short minTag;
		short minTagOld;

		cal = img.getCalibration();
		if (cal == null) {
			cal = new Calibration(img);
		}
		double pixelDepth = cal.pixelDepth;
		double pixelWidth = cal.pixelWidth;
		double pixelHeight = cal.pixelHeight;

		IJ.log("pixelDepth = " + pixelDepth);
		IJ.log("pixelWidth = " + pixelWidth);
		IJ.log("pixelHeight = " + pixelHeight);

		// overriding depth from property e.g.if the correct info is lost during
		// de-convolution
		pixelDepth = ((double) pixelDepthInNM) / 1000.0;
		cal.pixelDepth = pixelDepth;
		IJ.log("Overriding default pixel depth.Set pixelDepth = " + pixelDepth
				+ " micron instead!");

		double zOrigin = cal.zOrigin;
		double yOrigin = cal.yOrigin;
		double xOrigin = cal.xOrigin;
		double voxelSize = pixelDepth * pixelWidth * pixelHeight;

		IJ.log("voxelSize = " + voxelSize);
		IJ.log("unit = " + cal.getUnit());

		if (despeckle) {
			// Apply despeckle : median filter with radius 1
			IJ.showStatus("Despeckling ...");
			RankFilters filter = new RankFilters();
			ImageStack stack = img.getStack();
			for (z = 1; z <= NbSlices; z++) {
				ip = stack.getProcessor(z);
				filter.rank(ip, 1, RankFilters.MEDIAN);
				IJ.showProgress(z, NbSlices);
			}
		}
		if (smooth) {
			// This filter replaces each pixel with the average of its 3x3
			// neighborhood.
			IJ.showStatus("Smoothing ...");
			ImageStack stack = img.getStack();
			for (z = 1; z <= NbSlices; z++) {
				ip = stack.getProcessor(z);
				ip.smooth();
				IJ.showProgress(z, NbSlices);
			}
		}

		// pict=new int [Height*Width*NbSlices];
		// pict2 = new int[Width*Height*NbSlices];

		thr = new boolean[Height * Width * NbSlices];
		IJ.log("thr allocated");
		tag = new short[Height * Width * NbSlices]; // 2 byte, upto 32,767
													// objects
		IJ.log("tag allocated");
		// surf=new boolean [Height*Width*NbSlices]; //don't show edge
		Arrays.fill(thr, false);
		// Arrays.fill(surf,false);

		// set channel index
		int index1 = 0, index2 = 0;
		if (or == true)
			index1 = 0;
		else if (og == true)
			index1 = 1;
		else if (ob == true)
			index1 = 2;
		if (br == true)
			index2 = 0;
		else if (bg == true)
			index2 = 1;
		else if (bb == true)
			index2 = 2;

		ImageStack stack = img.getStack();
		// new method for using 2 channels, set thr
		setMaskUsing2Channels(index1, index2, ThrVal, ThrVal2, stack, thr);

		// First ID attribution
		short tagvois;
		ID = 1;
		arrayIndex = 0;
		for (z = 1; z <= NbSlices; z++) {
			for (y = 0; y < Height; y++) {
				for (x = 0; x < Width; x++) {
					if (thr[arrayIndex]) {
						tag[arrayIndex] = ID;
						minTag = ID;
						i = 0;
						// Find the minimum tag in the neighbours pixels
						for (voisZ = z - 1; voisZ <= z + 1; voisZ++) {
							for (voisY = y - 1; voisY <= y + 1; voisY++) {
								for (voisX = x - 1; voisX <= x + 1; voisX++) {
									if (withinBounds(voisX, voisY, voisZ)) {
										offset = offset(voisX, voisY, voisZ);
										if (thr[offset]) {
											i++;
											tagvois = tag[offset];
											if (tagvois != 0
													&& tagvois < minTag)
												minTag = tagvois;
										}
									}
								}
							}
						}
						// if (i!=27) surf[arrayIndex]=true;
						tag[arrayIndex] = minTag;
						if (minTag == ID) {
							ID++;
						}
					}
					arrayIndex++;
				}
			}
			IJ.showStatus("Finding structures");
			IJ.showProgress(z, NbSlices);
		}
		ID++;

		// Minimization of IDs=connection of structures
		arrayIndex = 0;
		for (z = 1; z <= NbSlices; z++) {
			for (y = 0; y < Height; y++) {
				for (x = 0; x < Width; x++) {
					if (thr[arrayIndex]) {
						minTag = tag[arrayIndex];
						// Find the minimum tag in the neighbours pixels
						for (voisZ = z - 1; voisZ <= z + 1; voisZ++) {
							for (voisY = y - 1; voisY <= y + 1; voisY++) {
								for (voisX = x - 1; voisX <= x + 1; voisX++) {
									if (withinBounds(voisX, voisY, voisZ)) {
										offset = offset(voisX, voisY, voisZ);
										if (thr[offset]) {
											tagvois = tag[offset];
											if (tagvois != 0
													&& tagvois < minTag)
												minTag = tagvois;
										}
									}
								}
							}
						}
						// Replacing tag by the minimum tag found
						for (voisZ = z - 1; voisZ <= z + 1; voisZ++) {
							for (voisY = y - 1; voisY <= y + 1; voisY++) {
								for (voisX = x - 1; voisX <= x + 1; voisX++) {
									if (withinBounds(voisX, voisY, voisZ)) {
										offset = offset(voisX, voisY, voisZ);
										if (thr[offset]) {
											tagvois = tag[offset];
											if (tagvois != 0
													&& tagvois != minTag)
												replacetag(tagvois, minTag);
										}
									}
								}
							}
						}
					}
					arrayIndex++;
				}
			}
			IJ.showStatus("Connecting structures");
			IJ.showProgress(z, NbSlices);
		}

		// Parameters determination 0:volume; 1:surface; 2:intensity;
		// 3:barycenter x; 4:barycenter y; 5:barycenter z; 6:barycenter x int;
		// 7:barycenter y int; 8:barycenter z int
		arrayIndex = 0;
		Paramarray = new double[ID][9];

		int[] PixVal = null;
		// ImageStack stack = img.getStack();
		for (z = 1; z <= NbSlices; z++) {
			ip = stack.getProcessor(z);
			for (y = 0; y < Height; y++) {
				for (x = 0; x < Width; x++) {
					PixVal = ip.getPixel(x, y, null);
					val = PixVal[index1];
					index = tag[arrayIndex];
					Paramarray[index][0]++;
					// if (surf[arrayIndex]) Paramarray[index][1]++;/
					Paramarray[index][2] += val;
					Paramarray[index][3] += x;
					Paramarray[index][4] += y;
					Paramarray[index][5] += z;
					Paramarray[index][6] += x * val;
					Paramarray[index][7] += y * val;
					Paramarray[index][8] += z * val;
					arrayIndex++;
				}
			}
			IJ.showStatus("Retrieving structures' parameters");
			IJ.showProgress(z, NbSlices);
		}
		double voxCount, intensity;
		for (i = 0; i < ID; i++) {
			voxCount = Paramarray[i][0];
			intensity = Paramarray[i][2]; // sum over all intensity values
			if (voxCount >= minSize && voxCount <= maxSize) {
				if (voxCount != 0) {
					Paramarray[i][2] /= voxCount;
					Paramarray[i][3] /= voxCount;
					Paramarray[i][4] /= voxCount;
					Paramarray[i][5] /= voxCount;
				}
				if (intensity != 0) {
					Paramarray[i][6] /= intensity;
					Paramarray[i][7] /= intensity;
					Paramarray[i][8] /= intensity;
				}
			} else {
				for (j = 0; j < 9; j++)
					Paramarray[i][j] = 0;
			}
			IJ.showStatus("Calculating barycenters' coordinates");
			IJ.showProgress(i, ID);
		}

		// Log data
		if (new_results) {
			rt = new ResultsTable();
		} else {
			rt = ResultsTable.getResultsTable();
		}
		IDarray = new int[ID];

		String[] head = { "Volume", "Surface", "Intensity", "Centre X",
				"Centre Y", "Centre Z", "Centre int X", "Centre int Y",
				"Centre int Z", "X", "Y", "Z" };
		for (i = 0; i < head.length; i++)
			rt.setHeading(i, head[i]);

		k = 1;
		for (i = 1; i < ID; i++) {
			if (Paramarray[i][0] != 0) {
				rt.incrementCounter();
				IDarray[i] = k;
				voxCount = Paramarray[i][0];
				rt.addValue(0, voxCount * voxelSize);
				rt.addValue(1, Paramarray[i][1] / voxCount);
				rt.addValue(2, Paramarray[i][2]);
				rt.addValue(3, cal.getX(Paramarray[i][3]));
				rt.addValue(4, cal.getY(Paramarray[i][4]));
				rt.addValue(5, cal.getZ(Paramarray[i][5] - 1));
				rt.addValue(6, cal.getX(Paramarray[i][6]));
				rt.addValue(7, cal.getY(Paramarray[i][7]));
				rt.addValue(8, cal.getZ(Paramarray[i][8] - 1));
				rt.addValue(
						9,
						(int) Math.round(cal.getX(Paramarray[i][3])
								/ pixelWidth + xOrigin));
				rt.addValue(
						10,
						(int) Math.round(cal.getY(Paramarray[i][4])
								/ pixelHeight + yOrigin));
				rt.addValue(
						11,
						(int) Math.round(cal.getZ(Paramarray[i][5] - 1)
								/ pixelDepth + zOrigin + 1));
				k++;
			}
		}
		if (new_results) {
			rt.show("Results from " + imgtitle);
		} else {
			// if (! IJ.isResultsWindow()) IJ.showResults();
			rt.show("Results");
		}
		int nParticles = rt.getCounter();

		if (showParticles) { // Create 'Particles' image
			if(colorByProximity)
				readProximityFile();
			
			Particles = NewImage.createRGBImage("Particles " + imgtitle,
					Width, Height, NbSlices, NewImage.FILL_BLACK);
			stackParticles = Particles.getStack();
			Particles.setCalibration(cal);
			arrayIndex = 0;
			for (z = 1; z <= NbSlices; z++) {
				ip = stackParticles.getProcessor(z);
				for (y = 0; y < Height; y++) {
					for (x = 0; x < Width; x++) {
						if (thr[arrayIndex]) {
							index = tag[arrayIndex];
							// if (Paramarray[index][0]>0){
							if (Paramarray[index][0] >= minSize
									&& Paramarray[index][0] <= maxSize) {
								if(tagColor != null)
									col = tagColor[index];
								else
									col = IDarray[index] + 1;
								ip.setValue(col);
								ip.drawPixel(x, y);
							}
						}
						arrayIndex++;
					}
				}
			}
			Particles.show();
			IJ.run("Fire");
		}
		/*
		 * if (showEdges){ // Create 'Edges' image
		 * Edges=NewImage.createShortImage
		 * ("Edges "+imgtitle,Width,Height,NbSlices,0);
		 * stackEdges=Edges.getStack(); Edges.setCalibration(cal); arrayIndex=0;
		 * for (z=1; z<=NbSlices; z++){ ip=stackEdges.getProcessor(z); for (y=0;
		 * y<Height; y++){ for (x=0; x<Width; x++){ if (thr[arrayIndex]) {
		 * index=tag[arrayIndex]; if (Paramarray[index][0]>0 &&
		 * surf[arrayIndex]) { col=IDarray[index]+1; ip.setValue(col);
		 * ip.drawPixel(x, y); } } arrayIndex++; } } } Edges.show();
		 * IJ.run("Fire"); }
		 */
		if (showCentres) { // Create 'Centres' image
			Centres = NewImage.createShortImage("Geometrical Centres "
					+ imgtitle, Width, Height, NbSlices, 0);
			stackCentres = Centres.getStack();
			Centres.setCalibration(cal);
			for (i = 0; i < nParticles; i++) {
				ip = stackCentres.getProcessor((int) Math.round(rt.getValue(5,
						i) / pixelDepth + zOrigin + 1));
				ip.setValue(i + 1);
				ip.setLineWidth(DotSize);
				ip.drawDot(
						(int) Math.round(rt.getValue(3, i) / pixelWidth
								+ xOrigin),
						(int) Math.round(rt.getValue(4, i) / pixelHeight
								+ yOrigin));
			}
			Centres.show();
			IJ.run("Fire");
		}

		if (showCentresInt) { // Create 'Intensity weighted Centres' image
			CentresInt = NewImage.createShortImage("Intensity based centres "
					+ imgtitle, Width, Height, NbSlices, 0);
			stackCentresInt = CentresInt.getStack();
			CentresInt.setCalibration(cal);
			for (i = 0; i < nParticles; i++) {
				ip = stackCentresInt.getProcessor((int) Math.round(rt.getValue(
						8, i) / pixelDepth + zOrigin + 1));
				ip.setValue(i + 1);
				ip.setLineWidth(DotSize);
				ip.drawDot(
						(int) Math.round(rt.getValue(6, i) / pixelWidth
								+ xOrigin),
						(int) Math.round(rt.getValue(7, i) / pixelHeight
								+ yOrigin));
			}
			CentresInt.show();
			IJ.run("Fire");
		}

		// "Volume","Surface","Intensity","Centre X","Centre Y","Centre Z","Centre int X","Centre int Y","Centre int Z"

		if (showNumbers) {
			Font font = new Font("SansSerif", Font.PLAIN, FontSize);
			for (i = 0; i < nParticles; i++) {
				z = (int) Math.round(rt.getValue(5, i) / pixelDepth + zOrigin
						+ 1);
				y = (int) Math.round(rt.getValue(4, i) / pixelHeight + yOrigin);
				x = (int) Math.round(rt.getValue(3, i) / pixelWidth + xOrigin);
				if (debug)
					IJ.log(pluginName + " Draw pixels: slice=" + z + " coords"
							+ x + "," + y);
				if (showParticles) {
					ip = stackParticles.getProcessor(z);
					ip.setFont(font);
					ip.setValue(nParticles);
					ip.drawString("" + (i + 1), x, y);
				}
				if (showEdges) {
					ip = stackEdges.getProcessor(z);
					ip.setFont(font);
					ip.setValue(nParticles);
					ip.drawString("" + (i + 1), x, y);
				}
				if (showCentres) {
					ip = stackCentres.getProcessor(z);
					ip.setFont(font);
					ip.setValue(nParticles);
					ip.drawString("" + (i + 1), x, y);
				}
				if (showCentresInt) {
					ip = stackCentresInt.getProcessor(z);
					ip.setFont(font);
					ip.setValue(nParticles);
					ip.drawString("" + (i + 1), x, y);
				}
				IJ.showStatus("Drawing numbers");
				IJ.showProgress(i, nParticles);
			}
		}

		if (showParticles) {
			Particles.getProcessor().setMinAndMax(1, nParticles);
			Particles.updateAndDraw();
		}

		if (showEdges) {
			Edges.getProcessor().setMinAndMax(1, nParticles);
			Edges.updateAndDraw();
		}

		if (showCentres) {
			Centres.getProcessor().setMinAndMax(1, nParticles);
			Centres.updateAndDraw();
		}

		if (showCentresInt) {
			CentresInt.getProcessor().setMinAndMax(1, nParticles);
			CentresInt.updateAndDraw();
		}

		if (summary) {
			double TtlVol = 0;
			double TtlSurf = 0;
			double TtlInt = 0;
			for (i = 0; i < nParticles; i++) {
				TtlVol += rt.getValueAsDouble(0, i);
				TtlSurf += rt.getValueAsDouble(1, i);
				TtlInt += rt.getValueAsDouble(2, i); //
			}
			int precision = Analyzer.precision;
			IJ.log(imgtitle);
			IJ.log("Number of Objects = " + nParticles);
			IJ.log("Mean Volume = " + IJ.d2s(TtlVol / nParticles, precision));
			IJ.log("Mean Surface Fraction = "
					+ IJ.d2s(TtlSurf / nParticles, precision));
			IJ.log("Mean Intensity = " + IJ.d2s(TtlInt / nParticles, precision));
			IJ.log("Voxel Size = " + IJ.d2s(voxelSize));
		}
		IJ.showStatus("Nb of particles: " + nParticles);
		IJ.showProgress(2, 1);
		IJ.showStatus(IJ.d2s((System.currentTimeMillis() - start) / 1000.0, 2)
				+ " seconds");
	}

	public boolean withinBounds(int m, int n, int o) {
		return (m >= 0 && m < Width && n >= 0 && n < Height && o > 0 && o <= NbSlices);
	}

	public int offset(int m, int n, int o) {
		return m + n * Width + (o - 1) * Width * Height;
	}

	public void replacetag(int m, short n) {
		for (int i = 0; i < tag.length; i++)
			if (tag[i] == m)
				tag[i] = n;
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {

		// does not work with RGB processor-- Thresholding of RGB images is not
		// supported!
		ThrVal = ((Scrollbar) sliders.elementAt(0)).getValue();
		ip.setThreshold(ThrVal, Math.pow(2, 16), ImageProcessor.RED_LUT);
		img.setSlice(((Scrollbar) sliders.elementAt(2)).getValue()); // changed
																		// from
																		// 1 to
																		// 2
		img.updateAndDraw();
	}

	public void textValueChanged(TextEvent e) {
		((Scrollbar) sliders.elementAt(0)).setValue((int) Tools
				.parseDouble(((TextField) value.elementAt(0)).getText()));

		// check the range of slice
		((Scrollbar) sliders.elementAt(2)).setValue((int) Tools
				.parseDouble(((TextField) value.elementAt(2)).getText()));
		if ((int) Tools.parseDouble(((TextField) value.elementAt(2)).getText()) > NbSlices) {
			((Scrollbar) sliders.elementAt(2)).setValue(NbSlices);
			((TextField) value.elementAt(2)).setText("" + NbSlices);
		}
		if ((int) Tools.parseDouble(((TextField) value.elementAt(2)).getText()) < 1) {
			((Scrollbar) sliders.elementAt(2)).setValue(1);
			((TextField) value.elementAt(2)).setText("1");
		}
		// check the range of pixel depth
		((Scrollbar) sliders.elementAt(3)).setValue((int) Tools
				.parseDouble(((TextField) value.elementAt(3)).getText()));
		if ((int) Tools.parseDouble(((TextField) value.elementAt(3)).getText()) > 1000) {
			((Scrollbar) sliders.elementAt(3)).setValue(1000);
			((TextField) value.elementAt(3)).setText("1000");
		}
		if ((int) Tools.parseDouble(((TextField) value.elementAt(3)).getText()) < 1) {
			((Scrollbar) sliders.elementAt(3)).setValue(1);
			((TextField) value.elementAt(3)).setText("1");
		}

		// does not work with RGB processor-- Thresholding of RGB images is not
		// supported!
		ThrVal = ((Scrollbar) sliders.elementAt(0)).getValue();
		ip.setThreshold(ThrVal, Math.pow(2, 16), ImageProcessor.RED_LUT);
		img.setSlice(((Scrollbar) sliders.elementAt(2)).getValue());
		img.updateAndDraw();
	}

	public ImagePlus getParticles() {
		return Particles;
	}

	public ImagePlus getEdges() {
		return Edges;
	}

	public ResultsTable getResults() {
		return rt;
	}

	public Thread runThread(ImagePlus img, int ThrVal, int minSize,
			int maxSize, boolean showParticles, boolean showEdges,
			boolean showCentres, boolean showCentresInt, boolean new_results) {
		this.img = img;
		this.ThrVal = ThrVal;
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.showParticles = showParticles;
		this.showEdges = showEdges;
		this.showCentres = showCentres;
		this.showCentresInt = showCentresInt;
		this.new_results = new_results;
		this.summary = false;
		thread = new Thread(Thread.currentThread().getThreadGroup(), this,
				"Object_Counter3D " + img.getTitle());
		thread.start();
		return thread;
	}

	public void run() {
		Width = img.getWidth();
		Height = img.getHeight();
		NbSlices = img.getStackSize();
		imgtitle = img.getTitle();
		analyze();
	}

	// Read output file from proximity analysis
	private void readProximityFile() {
		OpenDialog od = new OpenDialog(
				"Open output file from proximity analysis ...", "");
		String directory = od.getDirectory();
		String fileName = od.getFileName();
		if (fileName == null)
			return;
		
		String[] parts = null;
		int x, y, z, bin, arrayIndex, tag, colorIndex;
		tagColor = new int[IDarray.length];

		try {
			BufferedReader br = new BufferedReader(new FileReader(directory
					+ fileName));
			String line = br.readLine();
			while (line != null) {
				parts = line.split(" ");
				if(parts.length != 4) {
					IJ.error("Invalid parts in line.");
					tagColor = null;
					return;
				}
				
				x = Integer.parseInt(parts[0]);
				y = Integer.parseInt(parts[1]);
				z = Integer.parseInt(parts[2]);
				bin = Integer.parseInt(parts[3]);
				
				arrayIndex = (z - 1)* Width * Height + y * Width + x;
				tag = this.tag[arrayIndex];
				colorIndex = (bin - 1) % 10;
				tagColor[tag] = colorPool[colorIndex];
				
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			IJ.error("File reading error!", e.getMessage());
		} catch (IOException e) {
			IJ.error("I/O error!", e.getMessage());
		}
	}
}
