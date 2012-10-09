package annotool.analysis;

import ij.*;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import annotool.Annotation;
import annotool.extract.*;
import annotool.classify.*;
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
public class ThreeDROIAnnotation {

	static SavableClassifier classifier = null;
	static boolean useRaw = true;

	// anisotropic microsopic image cube: 9*9*3
	static int rx = 4, ry = 4;
	static int rz = 1;
	//
	//int positiveIndex = 17; //for cd7  17/11
	static int positiveIndex =7;  //for axon synapse   7/6

	// will be called by ROITagger "train3droi button"
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
			for (int zi = cz - rz; zi <= cz + rz; zi++) {
				if (zi < 0 || zi > totaldepth)
					continue; // check bound
				// get current stack, stack starts from 1.
				ImageProcessor currentip = imp.getStack().getProcessor(zi + 1);
				float[] oneframe = new float[(2 * rx + 1) * (2 * ry + 1)];
				int indexsmall = 0;
				for (int xi = cx - rx; xi <= cx + rx; xi++)
					for (int yi = cy - ry; yi <= cy + ry; yi++) {
						indexsmall = (yi - (cy - ry)) * (2 * rx + 1)
								+ (xi - (cx - rx));
						if (checkBound(xi, yi, zi, totalwidth, totalheight,
								totaldepth)) {
							oneframe[indexsmall] = (float) currentip.getPixel(
									xi, yi);
							System.out.print(oneframe[indexsmall] + " ");
						} else
							oneframe[indexsmall] = 0; // fill 0?
					}
				imageStacks.add(oneframe);
			}// end of zi
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
			extractedFea = get3DFeaViaHaar(alldata);
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

		classifier = new SVMClassifier((2 * rz + 1) * (2 * ry + 1)
				* (2 * rx + 1));
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
		if (imageType != DataInput.GRAY8) {
			System.out.println("Only grayscale 8 supported");
			return;
		}

		/*// flatten into an array for local maximum detection
		// not necessary if pass imp to local_maximum method. 
		// Will rewrite utiliy later using getPixel
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
		// use a window of 3*3*2 to get local maxim
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
		// storage for haar extraction
		ArrayList imageStacks = new ArrayList(2 * rz + 1);
		float[] oneframe = new float[(2 * rx + 1) * (2 * ry + 1)];
		int indexsmall = 0;
		for (int cz = 0; cz < totaldepth; cz++) {
			for (int cy = 0; cy < totalheight; cy++) {
				for (int cx = 0; cx < totalwidth; cx++) {
					if (isMaxima[index++]) {
						// get the cube round it, order needs to be the same as training
						indexForCube = 0;
						if (!useRaw)	imageStacks.clear();
						for (int zi = cz - rz; zi <= cz + rz; zi++) {
							// get current stack, stack starts from 1.
							ImageProcessor currentip = imp.getStack()
									.getProcessor(zi + 1);
							if (!useRaw)
								indexsmall = 0;
							for (int xi = cx - rx; xi <= cx + rx; xi++)
							{
								for (int yi = cy - ry; yi <= cy + ry; yi++) 
								{
									if (!useRaw)
									{
										indexsmall = (yi - (cy - ry))
												* (2 * rx + 1)
												+ (xi - (cx - rx));
										oneframe[indexsmall] = (float) currentip
													.getPixel(xi, yi);
									}
									fea[indexForCube] = (float) currentip
												.getPixel(xi, yi);
									//System.out.print(fea[indexForCube] + " ");
									indexForCube++;
								} //end yi
							} //end xi
						    if (!useRaw) {//add current slice to the stacks
							  imageStacks.add(oneframe);
						   }
						}// end of zi
						if(!useRaw)
						{
						  // pass in an arraylist of an arraylist of an array
						  ArrayList list = new ArrayList();
						  list.add(imageStacks);
						  float[][] fea3D = get3DFeaViaHaar(list);
						  fea = fea3D[0];
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
		
		// output result to a file as  V3D marker file
		for(Point3D p : detectedCenters)
		{
			int tmpy=totalheight-p.y;
			System.out.println((p.x+1) + ", " + tmpy + ", " + (p.z+1) + ", 0,1,unknow,,,,");
		}

		//output result to a file as input for object quantification. No change to coordinate is needed.
		for(Point3D p : detectedCenters)
		{
			System.out.println(p.x + " " + p.y + " " + p.z);
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
		if (p.equals(input)) return true;
	  return false;
	}

	
	public static float[][] get3DFeaViaHaar(ArrayList alldata) {
		float[][] extractedFea = null;
		StackSimpleHaarFeatureExtractor ex = new StackSimpleHaarFeatureExtractor();
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
			int imageType = DataInput.GRAY32;
			extractedFea = ex.calcFeatures(alldata, imageType, dim);
		} catch (Exception e) {
			e.getStackTrace();
		}

		return extractedFea;
	}

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
    
  }
	
}

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
}
	
