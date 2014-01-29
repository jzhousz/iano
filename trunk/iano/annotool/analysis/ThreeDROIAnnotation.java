package annotool.analysis;

import ij.*;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.*;

import java.awt.Color;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import annotool.Annotation;
import annotool.Annotator;
import annotool.extract.*;
import annotool.gui.model.*;
import annotool.classify.*;
import annotool.classify.MLP.MLPClassifier;
import annotool.io.ChainIO;
import annotool.io.DataInput;

/*
 * 1/24/2012
 //This class is for synapse analysis using ROI recognition
 //It reads in a 3D image, and a list of tagged 3D ROIs -current GUI can't take 3D image set yet as of Jan 2012. 
 //It will then load a model (or do it in a hard-coded way)
 //Training
 //Apply to 3D local maxima
 //Output a set of detected candidates (synapses).
 //The output file will be combined with detection based on component analysis
 //Go through 3D splitting, to output final synapse count information.

 //This class is independent of other BioCAT classes for testing purpose.

 //Tricky: Because I converted the data to float BEFORE passing it to
 //the extractor, I need to change the imageType correspondingly.
 //Is there another way to do it?
 */

/*
 * CHANGE LOG Jon Sanders
 * 11/03/13 - provide utility for output to file for object quantification
 *            to facilitate better detected.marker generation.
 * 11/04/13 - file writing for v3D file type.
 *          - file saving for generated local maxima image as .tif
 * 11/10/13 - fix missing else in test3DRoi, either use isRaw settings or not, not both.
 * 11/15/13 - rework get3DFeaturesViaHaar into a more general name, get3DFeaViaExtrator,
 *            and able to call other FEs.
 * 11/18/13 - adjust logic in test3droi to ignore edge pixels.
 * 11/20/13 - adjust logic in train3droi to ignore edge pixels.
 *          - switch classifier type to weka W_RandomForest.
 *          
 * 01/22/14 - begin overhaul conversion to DataInput style
 * 01/25/14 - implement new buildDataFromRoi() loic copied from annImageTable
 * 01/28/14 - write main() to take command args and run without gui.
 * 			- NOTE: all gui dependent and alldata code to be deprecated soon.
 * 01/29/14	- Loading from chain files. a little scattered though, but works.
 * 			- EXCEPT for getting a savable classifier by name.
 */

public class ThreeDROIAnnotation {

	static SavableClassifier classifier = null;
	static boolean useRaw = false;

	
	/////////////////////
	// deprecated in command line version
	// anisotropic microscopic image cube: 9*9*3
	static int rx = 4, ry = 4;
	static int rz = 1;
	//
	//int positiveIndex = 17; //for cd7  17/11
	static int positiveIndex = 7;  //for axon synapse large set.
	//////////////////////
	
	//***********************
	// new variables for conversion to DataInput
	ImagePlus imp;
	int depth = 1, width = 1, height = 1;
	annotool.io.DataInput problem = null;
	int problemLength;
	ArrayList<Chain> chainList;
	
	//parse ROI files and construct a DataInput object to encapsulate data
	public void buildDataFromROI(String[] roiPaths) throws Exception
	{
		HashMap classMap;
		HashMap roiList;
		
		//isRoiInput = true;
		//this.imp = new ImagePlus(imagePath);
		//displayImage = new ImagePlus(imagePath);	//Once closed, the image is flushed, we don't want that for our original image because we need the data
		//displayImage.setIgnoreFlush(true);
		//this.imagePath = imagePath;					//To reconstruct display image if it has been flushed
		
		classMap = new HashMap<String, String>();
		roiList = new HashMap<String, Roi>();
		
		for(String path : roiPaths) {
			
			
			if(path.endsWith("marker"))
			{
			 String className = Utils.removeExtension(path.substring(path.lastIndexOf(File.separator) + 1));
			 Utils.openRoiMarker(path, roiList, classMap, className);
			}
			else //ROI zip file
			{
			 String className = Utils.removeExtension(path.substring(path.lastIndexOf(File.separator) + 1));
			 Utils.openRoiZip(path, roiList, classMap, className);
			}
		}
		
		if(roiList.size() < 1){
			System.out.println("ERROR: No rois specified.");
		}
		this.problem = new DataInput(imp, roiList, classMap, Annotator.channel, depth, width, height);
		
		
		problemLength = this.problem.getLength();
		//children = problem.getChildren();
		
		//annotations = problem.getAnnotations();
		//numOfAnno = annotations.size();
		//classNames = problem.getClassNames();
		
	}
	
	
	// will be called by ROITagger "train3droi button"
	//* DEPRACATED 01/25/14 (still functional)
	public static SavableClassifier train3droi(ImagePlus imp) {
		// read ROIm -- ROITAGGER
		RoiManager manager = RoiManager.getInstance();
		if (manager == null) {
			System.out.println("No ROI manager available.");
			return null;
		}

		ImageProcessor ip = imp.getProcessor();
		int totalwidth = ip.getWidth();
		int totalheight = ip.getHeight();
		int totaldepth = imp.getStackSize();
		int imageType = imp.getType();
		
		//System.out.println("image type: " + imageType);//debug

		if (imageType == DataInput.COLOR_RGB) {
			System.out.println("Only grayscale supported");
			return null;
		}

		// get data from ROI
		Roi[] rois = manager.getRoisAsArray();

		// ArrayList (image set) of ArrayList (stacks) of byte[]/int[]/float[]
		int x, y, w, h, cx, cy, cz;
		String name;

		ArrayList<ArrayList> alldata = new ArrayList<ArrayList>();
		for (int i = 0; i < rois.length; i++) {
			x = rois[i].getBounds().x;
			y = rois[i].getBounds().y;
			w = rois[i].getBounds().width;
			h = rois[i].getBounds().height;
			cx = x + w / 2;
			cy = y + h / 2;
			name = rois[i].getName();
			cz = manager.getSliceNumber(name);
			cz = cz -1; //slide starts from 1 but cz starts from 0.
			System.out.println("x:" + cx + " y:" + cy + " z:" + cz);

			// data for one ROI
			ArrayList imageStacks = new ArrayList(2 * rz + 1);
			boolean cubeComplete = true;//true if we can get complete cube data, (i.e. false if the pixel is too close to boundary)
			for (int zi = cz - rz; zi <= cz + rz; zi++) {
				if (zi < 0 || zi >= totaldepth) {//check z bound
					cubeComplete = false;
					break; //stop getting data for this whole slice
				}
				if (!cubeComplete){ //xi or yi out of bounds
					break;
				}
				// get current stack, stack starts from 1.
				ImageProcessor currentip = imp.getStack().getProcessor(zi + 1);
				float[] oneframe = new float[(2 * rx + 1) * (2 * ry + 1)];
				int indexsmall = 0;
				for (int xi = cx - rx; xi <= cx + rx; xi++) {
					if(!cubeComplete) break;//break from xi
					for (int yi = cy - ry; yi <= cy + ry; yi++) {
						if (!checkBound(xi, yi, zi, totalwidth, totalheight,totaldepth )) {
							cubeComplete = false;
							break;//break one layer of yi
						} else {
							indexsmall = (yi - (cy - ry)) * (2 * rx + 1) + (xi - (cx - rx));
							if (checkBound(xi, yi, zi, totalwidth, totalheight,totaldepth)) {
								oneframe[indexsmall] = (float) currentip.getPixel(xi, yi);
								System.out.print(oneframe[indexsmall] + " ");
							} else {
								oneframe[indexsmall] = 0; // fill 0?
							}
						}
					}//end yi				
				}//end xi
				imageStacks.add(oneframe);
			}// end of zi
			
			if(!cubeComplete) continue;//ignore this pixel and the cube around it if near bound.
			
			System.out.println();
			alldata.add(imageStacks);
		}// end of roi list

		// feature extraction, selection
		float[][] extractedFea = null;

		// no feature extraction, flatten all stacks for each image
		if (useRaw) {
			extractedFea = new float[alldata.size()][(2 * rx + 1)
					* (2 * ry + 1) * (2 * rz + 1)];
			int feaIndex = 0;
			for (int i = 0; i < alldata.size(); i++) {
				feaIndex = 0;
				for (int j = 0; j < alldata.get(i).size(); j++) {
					float[] oneimage = (float[]) alldata.get(i).get(j);
					for (int k = 0; k < oneimage.length; k++)
						extractedFea[i][feaIndex++] = oneimage[k];
				}
			}
		} else {
			extractedFea = get3DFeaViaExtrator(alldata,  imageType);
			
		    System.out.println("Extracted features:");
		    for(int i=0; i<(alldata.size()); i++ ) { //debug
		      System.out.println("");
		      for(int j=0; j<(2 * rx + 1); j++){
		    	  System.out.print(extractedFea[i][j] + ", ");
		      }
		    }
		}

		// set up training targets
		int[] target = new int[rois.length];
		
		for (int i = 0; i < positiveIndex; i++)
			target[i] = 1;
		for (int i = positiveIndex ; i < rois.length; i++)
			target[i] = 0;

		// new classifier
		for (int m = 0; m < extractedFea.length; m++) {
			for (int n = 0; n < extractedFea[0].length; n++)
				System.out.print(extractedFea[m][n] + " ");
			System.out.println();
		}

		/*
		 * a variety of classifiers to use, 
		 * for Weka classifiers, wrapper class invoked, and specific classifier 
		 * indicated by a parameter.
		 */
		//classifier = new SVMClassifier((2 * rz + 1) * (2 * ry + 1) * (2 * rx + 1));
		classifier = new MLPClassifier();
		
		//set the type of the weka classifier.
		HashMap<String,String> params = new HashMap<String, String>();
		params.put("W_RandomForest", "W_RandomForest");
	
		//classifier = new WekaClassifiers();
		//classifier.setParameters(params);
		
		try {
			classifier.trainingOnly(extractedFea, target);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Annotation[] ann = new Annotation[extractedFea.length];
		for (int i = 0; i < ann.length; i++)
			ann[i] = new Annotation();
		try {
			float rate = (new Validator()).classify(extractedFea, extractedFea,
					target, target, classifier, ann);
			System.out.println("rate" + rate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < ann.length; i++)
			System.out.println(ann[i].anno);

		return classifier;

	}

	public static void test3droi(ImagePlus imp) {
		// get data from imp
		ImageProcessor ip = imp.getProcessor();
		int totalwidth = ip.getWidth();
		int totalheight = ip.getHeight();
		int totaldepth = imp.getStackSize();
		int imageType = imp.getType();
		
		//if incorrect image type
		if (imageType != DataInput.GRAY8) {
			System.out.println("Only grayscale 8 supported");
			return;
		}

		/*// flatten into an array for local maximum detection
		// not necessary if pass imp to local_maximum method. 
		// Will rewrite utility later using getPixel
		// note the order of data storage.
		System.out.println("flattening ... will remove later");
		float[] data = new float[totalwidth * totalheight * totaldepth];
		int index = 0;
		for (int zi = 0; zi < totaldepth; zi++) {
			ImageProcessor currentstackip = imp.getStack().getProcessor(zi+1);
			for (int yi = 0; yi < totalheight; yi++)
				for (int xi = 0; xi < totalwidth; xi++) {
					data[index++] = currentstackip.get(xi, yi);
				}
		}
		boolean[] isMaxima = Utility.getLocalMaxima(data, totalwidth,
				totalheight, totaldepth, 3, 3, 2);
		*/
		
		// use a window of 3*3*2 (radius, 7*7*5 actual) to get local maxim
		System.out.println("get localmaxima");
		boolean[] isMaxima = Utility.getLocalMaxima(imp, 0, 3, 3, 2); //2nd arg is channel
		draw(isMaxima, imp, totaldepth, totalheight, totalwidth);

		// get cube around local maxima, send to classifier
		java.util.HashSet<Point3D> detectedCenters = new java.util.HashSet<Point3D>();
		int total =0;
		int cubeDimension = (2 * rz + 1) * (2 * rx + 1) * (2 * ry + 1);
		float[] fea = new float[cubeDimension];
		double[] prob = new double[2]; // actually only need 1 spot
		int index = 0;
		int indexForCube = 0; // used for the cube
		// storage for extraction
		ArrayList imageStacks = new ArrayList(2 * rz + 1);
		float[] oneframe = new float[(2 * rx + 1) * (2 * ry + 1)];
		int indexsmall = 0;
		boolean cubeComplete = true; //indicates if we can get complete cube data (i.e. the pixel is not too close to boundary)
		//System.out.println("getting cube around each feature..."); //debug
		for (int cz = 0; cz < totaldepth; cz++) {
			for (int cy = 0; cy < totalheight; cy++) {
				for (int cx = 0; cx < totalwidth; cx++) {
					if (isMaxima[index++]) {
						cubeComplete = true;
						// get the cube round it, order needs to be the same as training
						indexForCube = 0;
						if (!useRaw)	imageStacks.clear();
						for (int zi = cz - rz; zi <= cz + rz; zi++) {
							if (zi < 0 || zi >= totaldepth) // check bound on z
							{
								cubeComplete = false;
								break; //stop getting more data for this cube for the slice. Ignore the pixels that are too close to z-boundary
							}
							if (!cubeComplete) //due to xi or yi being out of boundary
							{ 
								break;  //break out of xi
							}
							// get current stack, stack starts from 1.
							ImageProcessor currentip = imp.getStack()
									.getProcessor(zi + 1);
							if (!useRaw)
								indexsmall = 0;
							for (int xi = cx - rx; xi <= cx + rx; xi++)
							{
								if (!cubeComplete) 
								{ 
									break;  //break out of xi
								}
								for (int yi = cy - ry; yi < cy + ry; yi++) 
								{
									if (!checkBound(xi, yi, zi, totalwidth, totalheight,totaldepth))
									{
										cubeComplete = false;
										break; //one layer break out of yi.
									}
									else
									{	
									   if (!useRaw)
									   {
										indexsmall = (yi - (cy - ry))
												* (2 * rx + 1)
												+ (xi - (cx - rx));
										oneframe[indexsmall] = (float) currentip.getPixel(xi, yi);
									   }
									   else {
										   fea[indexForCube] = (float) currentip.getPixel(xi, yi);
									  
									   //System.out.print(fea[indexForCube] + " ");//debug
									   indexForCube++;
									   }
									}
								} //end yi
							} //end xi
						    if (!useRaw) {//add current slice to the stacks
							  imageStacks.add(oneframe);
						   }
						}// end of zi
						
						if (!cubeComplete)
						{
							continue;   //ignore this pixel (and the cube around it) if it is too close to the boundary
						}
						
						//now do extraction (if not using raw data) and classification on the cube	
						if(!useRaw)
						{
						  // pass in an arraylist of an arraylist of an array
						  ArrayList list = new ArrayList();
						  list.add(imageStacks);
						  float[][] fea3D = get3DFeaViaExtrator(list,  imageType);
						  fea = fea3D[0];
						  
						  System.out.println("Extracted features:"); //debug
						  for(int i=0; i<fea.length; i++ ) { //debug
							  System.out.print(fea[i] + ", ");
						  }
						}
						try {
							int result = classifier.classifyUsingModel(
									classifier.getModel(), fea, prob);
							//System.out.println(result);
							if(result == 1) 
							{
								total ++;
								//mean shift
								Point3D shifted=getMassCenter(imp, totalwidth, totalheight, totaldepth, cx, cy, cz, 3, 3, 1);
								
								//remove duplicates or those that are close enough
								//contains() not useful unless override hashcode.
								//linear search, or use a boolean map to search neighborhood 5*5*3
								if(!(searchForIt(detectedCenters,shifted)))
									detectedCenters.add(shifted);
							}

						} catch (Exception e) {
							e.printStackTrace();
						}

					}// end of the local maxima
				}// end of cx
			}// end of cy
		}// end of cz

		//linear search to remove duplicate
		System.out.println("total detected positive:"+total);
		System.out.println("total detected positive after mean-shifting:"+detectedCenters.size());
		
		
		//for file saving and naming convention
		Date date = new Date();
		SimpleDateFormat fmat =new SimpleDateFormat("_MM_dd_yyyy_hhmmss" );
		String extraName = "";
		
		// output result to a file as  V3D marker file
		try{
			
			String v3DFileName = new String("V3D_detected" + "_"+ fmat.format(date)+ "_" + extraName + ".marker");
			File file = new File(v3DFileName);
			BufferedWriter marker = new BufferedWriter(new FileWriter(file));
			
		    //write each to file
			System.out.println("Vaa3D file output format:");
			System.out.println("x,y,z,radius,shape,name,comment,color_r,color_g,color_b");//v3d marker .csv format
			int i=1;
			for(Point3D p : detectedCenters)
			{
		    	int tmpy=totalheight-p.y;
				System.out.println((p.x+1) + ", " + tmpy + ", " + (p.z+1) + ", 0,1,detected center " + i + "," + fmat.format(date) + ",255,50,25"); //echo to console
				marker.write((p.x+1) + ", " + tmpy + ", " + (p.z+1) + ", 0,1,detected center " + i + "," + fmat.format(date) + ",255,50,25");
				marker.newLine();
				i++;
	
			}
		    
		    System.out.println("written to file: " + v3DFileName);
		    
			marker.close();
			
		} catch(IOException e){
			System.out.println("error writing to file.");
		  	
		} 
        		
		//output result to a file as input for object quantification. No change to coordinate is needed.
		try{
			String markerFileName = new String("detected" + "_" + fmat.format(date) + "_" + extraName + ".marker");
			File file = new File(markerFileName);
			BufferedWriter marker = new BufferedWriter(new FileWriter(file));
			
		    //write each to file
			System.out.println("general marker file output format:");
			System.out.println("x,y,z,");//.marker csv format
		    for(Point3D p : detectedCenters)
			{
				System.out.println(p.x + " " + p.y + " " + p.z); //echo to console
				marker.write(p.x + " " + p.y + " " + p.z);
				marker.newLine();
	
			}
		    
		    System.out.println("written to file: " + markerFileName);
		    
			marker.close();
			
		} catch(IOException e){
			System.out.println("error writing to file.");
		  	
		} 
		
	}
	
	
	// 3D mean shift algorithm to get to center of mass
	//start from ix, iy, iz, until converge to a stable center of mass
	// rx, ry, rz is the searching radium
	// If in valley, will not move to high lands. 
	//
	static Point3D getMassCenter(ImagePlus imp, int sx, int sy, int sz, int ix, int iy, int iz, int rx, int ry, int rz)
	{
		 double rx_ms = rx, ry_ms = ry, rz_ms = rz;
	     double scx=0,scy=0,scz=0,si=0;
		 double ocx, ocy, ocz; //old center position
		 double ncx, ncy, ncz; //new center position
	     int k1, j1, i1;

	     ncx=ix;
	     ncy=iy;
	     ncz=iz; //new center position
		 
	     ImageProcessor ip;
	     int cv;
		 while (true) //mean shift to estimate the true center
		 {
				ocx=ncx; ocy=ncy; ocz=ncz;

				for (k1=(int) (ocz-rz_ms) ;k1<= (int) (ocz+rz_ms);k1++)
				{
	 			   if (k1<0 || k1>=sz)  continue;
	 			   ip = imp.getStack().getProcessor(k1+1);
				   for (j1= (int)(ocy-ry_ms);j1<= (int) (ocy+ry_ms);j1++)
				   {
							if (j1<0 || j1>=sy)	continue;
							for (i1= (int) (ocx-rx_ms);i1<= (int) (ocx+rx_ms);i1++)
							{
								if (i1<0 || i1>=sx)		continue;
								
								cv = ip.getPixel(i1,j1);

	                        	scz += k1*cv;
								scy += j1*cv;
								scx += i1*cv;
								si += cv;
							}
					}
				}
				if (si>0)
					{ncx = scx/si; ncy = scy/si; ncz = scz/si;}
				else
					{ncx = ocx; ncy = ocy; ncz = ocz;}

				if (ncx<rx || ncx>=sx-1-rx || ncy<ry || ncy>=sy-1-ry || ncz<rz || ncz>=sz-1-rz) //move out of boundary
				{
					ncx = ocx; ncy = ocy; ncz = ocz; //use the last valid center
					break;
				}
				
				//stop when the difference between old center and new center is small enough
				if (Math.sqrt((ncx-ocx)*(ncx-ocx)+(ncy-ocy)*(ncy-ocy)+(ncz-ocz)*(ncz-ocz))<=1)
				{
					System.out.println("cell mass center found: "+ (int) (ncx+0.5)+ " " + (int) (ncy+0.5) +  " " + (int) (ncz+0.5));
					System.out.println("(moved from " + ix + " " + iy + " " + iz +  ")" );
					break;
				}
			}//end of search for mass center

		 int movedx = (int)(ncx+0.5);
		 int movedy = (int)(ncy+0.5);
		 int movedz = (int)(ncz+0.5);
		 return new Point3D(movedx,movedy, movedz);
	}

	public static boolean searchForIt(java.util.HashSet<Point3D> detectedCenters, Point3D input)
	{
	  for(Point3D p : detectedCenters)
		//if (p.equals(input)) return true;
		  if (p.closesTo(input, rx, ry, rz)) return true;
	  return false;
	}

	//old logic, not loading from file.
	public static float[][] get3DFeaViaExtrator(ArrayList alldata,  int imageType) {
		float[][] extractedFea = null;
		StackSimpleHaarFeatureExtractor ex = new StackSimpleHaarFeatureExtractor();
		//ImageMoments3D ex = new ImageMoments3D();
		//FeatureJEdges3D ex = new FeatureJEdges3D();
		
		// override default if needed.
		// HashMap<String, String> parameter = new HashMap<String, String>();
		// parameter.put(..,..); //ex.setParameter(parameter);
		annotool.ImgDimension dim = new annotool.ImgDimension();
		dim.height = 2 * ry + 1;
		dim.width = 2 * rx + 1;
		dim.depth = 2 * rz + 1;
		
		try {
			// trick the HaarFeatureExtractor to take float
			// this is a bit weird. because calFeatures usually takes raw data
			// Object
			int changedImageType = DataInput.GRAY32;
			extractedFea = ex.calcFeatures(alldata, changedImageType, dim);
			
			//extractedFea = ex.calcFeatures(alldata, imageType, dim);
			
			
		} catch (Exception e) {
			e.getStackTrace();
		}

		return extractedFea;
	}
	
	// new logic using DataInput 1/27/14
	public static float[][] get3DFeaViaExtrator(DataInput problem, ArrayList<Chain> chainList, Annotator a) {
		float[][] extractedFea = null;
		//StackSimpleHaarFeatureExtractor ex = new StackSimpleHaarFeatureExtractor();
		
		try {
			// trick the HaarFeatureExtractor to take float
			// this is a bit weird. because calFeatures usually takes raw data
			// Object
			//int changedImageType = DataInput.GRAY32;
			/* System.out.println("Extracting old way:");//debug
			
			extractedFea = ex.calcFeatures(problem);
			
		    System.out.println("Extracted features:");//debug
			for (int m = 0; m < extractedFea.length; m++) {
				for (int n = 0; n < extractedFea[0].length; n++)
					System.out.print(extractedFea[m][n] + " ");
					System.out.println();
			}
			*/
			
			
			//load from only first specified chain, others ignored
			 System.out.println("Extracting new way:");//debug
			ArrayList<Extractor> extractors = chainList.get(0).getExtractors();
			extractedFea = a.extractWithMultipleExtractors(problem, extractors);
		    System.out.println("Extracted features:");//debug
			for (int m = 0; m < extractedFea.length; m++) {
				for (int n = 0; n < extractedFea[0].length; n++)
					System.out.print(extractedFea[m][n] + " ");
					System.out.println();
			}
					
			
		} catch (Exception e) {
			e.getStackTrace();
		}

		return extractedFea;
		
		
	}
	
	//maybe deprecated, possible use in testing logic. 1/29/14
	static boolean checkBound(int xi, int yi, int zi, int totalwidth,
			int totalheight, int totaldepth) {
		if (xi < 0 || xi >= totalwidth)
			return false;
		if (yi < 0 || yi >= totalheight)
			return false;
		if (zi < 0 || zi >= totaldepth)
			return false;
		return true;
	}

	static void draw(boolean[] isMaxima, ImagePlus imp, int totaldepth, int totalheight, int totalwidth)
	{
	  /*//draw on the original image
  	  ImageConverter ic = new ImageConverter(imp);
	  ic.convertToRGB();
	  ImageProcessor ip;
	  //ip = imp.getChannelProcessor();
	
      int index =-1;
	  for(int z= 0; z < totaldepth; z++)
	  {
  		  ip = imp.getStack().getProcessor(z+1);
  		  for(int y = 0; y < totalheight; y++) 
  		  {
		    for(int x = 0; x < totalwidth; x++) 
		    {
		      index++;	
			  if(isMaxima[index]) {
				ip.moveTo(x, y);						
    			Color c = new Color(1f, 0f, 0f);
    			ip.setColor(c);
    			ip.fillOval(x - 1, y - 1, 2, 2);
			}
		  }
  		}
	} 
	
	imp.updateAndDraw();
	*/
    
	//draw a new one
	ImageProcessor ip;
    ImagePlus centers= ij.gui.NewImage.createShortImage("Synapse Centers",totalwidth,totalheight,totaldepth,0);
    ImageStack stackParticles= centers.getStack();
    int index= -1;
    for (int z= 0; z< totaldepth; z++){
        ip =stackParticles.getProcessor(z+1);
        for (int y=0; y< totalheight; y++){
            for (int x=0; x< totalwidth; x++){
            	index++;
                if (isMaxima[index]) {
    				  ip.moveTo(x, y);						
        			  Color c = new Color(1f, 0f, 0f);
        			  ip.setColor(c);
        			  ip.fillOval(x - 1, y - 1, 2, 2);
                    //    ip.setValue(col);
                    //    ip.drawPixel(x, y);
        			 
                    }
                }
            }
        }
    centers.show();
    
	//for file saving and naming convention
	Date date = new Date();
	SimpleDateFormat fmat =new SimpleDateFormat("_MM_dd_yyyy_hhmmss" );
    
    //save local maxima image fior inspection.
    System.out.println("saving local maxima image...");
    String imgFileName = new String("local_maxima" + fmat.format(date) +".tiff" );
    IJ.save(centers, imgFileName);
    
    }
	
	
	// return the number of ROIS in this instance
	private int getLength(){
		return problemLength;
	}
	
	//command line version of code
	//takes args: image file path, positive roi, negative roi, roi depth, width, height, chain file 
	public static void main(String[] args){
		ThreeDROIAnnotation anno = new ThreeDROIAnnotation();
		
		String[] roiPaths;
		String chainPath;
		
		//check for null args
		if( args == null || args.length == 0 || args.length > 7){
			System.out.println("Correct usage: java ThreeDROIAnnotation imagePath, pos ROI, neg ROI, ROI depth, width, height, chain file ");
		}
		
		roiPaths = new String[2];
		//set vars from args
		try{
			anno.imp = new ImagePlus( args[0]);     anno.imp.show(); //show original image
			roiPaths[0] = args[1];					System.out.println(roiPaths[0]);
			roiPaths[1] = args[2];					System.out.println(roiPaths[1]);
			anno.depth = Integer.parseInt(args[3]);	System.out.println(anno.depth);
			anno.width = Integer.parseInt(args[4]);	System.out.println(anno.width);
			anno.height = Integer.parseInt(args[5]);System.out.println(anno.height);
			chainPath = args[6];					System.out.println(chainPath);
		} catch(Exception argsE){
			System.out.println("Error parsing args");
			return;
		}
		
		System.out.println("success parsing args...");
	
		try{ //build a DataInput object to encapsulate data
		anno.buildDataFromROI(roiPaths);
		} catch( Exception e){
			System.out.println("ERROR: Problem building data from ROI");
			return;
		}
		
		//load the chain file as an ArrayList
		ChainIO chainLoader = new ChainIO();
		try {
			anno.chainList = chainLoader.load(new File(chainPath));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		
		System.out.println("success building data...\n");
		
		/******************************************
	    new train3DROI() logic, temp here in main;
	    *//////////////////////////////////////////
		
		// feature extraction, selection
		float[][] extractedFea = null;
		Annotator annotator = new Annotator();

		extractedFea = get3DFeaViaExtrator(anno.problem, anno.chainList, annotator);
		
	    
		// set up training targets
		int[] target = new int[anno.getLength()];
		target = anno.problem.getTargets()[0];
		
		
		// new classifier */
		/*
		 * a variety of classifiers to use, 
		 * for Weka classifiers, wrapper class invoked, and specific classifier 
		 * indicated by a parameter.
		 */

		//classifier = new SVMClassifier((2 * rz + 1) * (2 * ry + 1) * (2 * rx + 1));
		//classifier = new MLPClassifier();
		
		//get classifier from chain file. first only, ignore rest
		try {
			String classi = anno.chainList.get(0).getClassifier();
			HashMap<String,String> params = anno.chainList.get(0).getClassParams();
			//classifier =  annotator.getSavableClassifierGivenName(classi, anno.chainList.get(0).getClassifierExternalPath(), params);
			//**** not able to get a savable classifier!
			classifier = new MLPClassifier();
			
			
		
			classifier.trainingOnly(extractedFea, target);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Annotation[] ann = new Annotation[extractedFea.length];
		for (int i = 0; i < ann.length; i++)
			ann[i] = new Annotation();
		try {
			float rate = (new Validator()).classify(extractedFea, extractedFea,
					target, target, classifier, ann);
			System.out.println("rate" + rate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		//int[] results;
		//results = 
		
					System.out.println("C :: T");
		for (int i = 0; i < ann.length; i++)
			System.out.println(ann[i].anno + " :: "+ target[i]);

		//return classifier;


		
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in)); 
		try {
			stdin.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}//endclass ThreeDROIAnnotation

class Point3D
{
	int x, y, z;
	Point3D(int x, int y, int z) {this.x=x; this.y=y; this.z=z;}
	//Return tree if 2 points are close enough
	boolean equals(Point3D p2)
	{ 
		int distx, disty, distz;
		if (x>p2.x) distx = x-p2.x; else distx = p2.x -x;
		if (y>p2.y) disty = y-p2.y; else disty = p2.y -y;
		if (z>p2.z) distz = z-p2.z; else distz = p2.z -z;
		//distance on xy plane should be smaller than 2, on z should be smaller than 1. - Due to different resolution, don't say distx+disty+distz < 3
		if((distx<2) && (disty<=2) && distz <=1) return true;
		else return false; 
	}

	boolean closesTo(Point3D p2, int dx, int dy, int dz)
	{ 
		int distx, disty, distz;
		if (x>p2.x) distx = x-p2.x; else distx = p2.x -x;
		if (y>p2.y) disty = y-p2.y; else disty = p2.y -y;
		if (z>p2.z) distz = z-p2.z; else distz = p2.z -z;
		//distance on xy plane should be smaller than 2, on z should be smaller than 1. - Due to different resolution, don't say distx+disty+distz < 3
		if((distx <= dx) && (disty <= dy) && distz <= dz) return true;
		else return false; 
	}

}
	
