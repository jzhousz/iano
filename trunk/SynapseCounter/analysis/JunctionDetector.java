package annotool.analysis;

import java.util.ArrayList;
import java.util.HashMap;

import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.process.BinaryProcessor;
import ij.process.ImageProcessor;

import skeleton_analysis.*;

public class JunctionDetector {
	public static final double ALPHA = 1.5;
	public static final double BETA = 2.0;
	public static final double OBJ_THRES = 20;
	
	/**
	 * Processes the binary image to get the list of junction points
	 * 
	 * @param ip
	 * @return
	 */
	public ArrayList<skeleton_analysis.Point> run(BinaryProcessor ip) {
		//Reduce to 1 pixel thickness
		ip.skeletonize();
		
		//Detect candidate junction points
		ip.invert();	//Analyze skeleton requires black as background
		
		AnalyzeSkeleton_ skel = new AnalyzeSkeleton_();
		ImagePlus imp = new ImagePlus("Skeleton", ip);
		skel.setup("", imp);
		// Perform analysis in silent mode
		// (work on a copy of the ImagePlus if you don't want it displayed)
		// run(int pruneIndex, boolean pruneEnds, boolean shortPath, ImagePlus origIP, boolean silent, boolean verbose)
		SkeletonResult skelResult = skel.run(AnalyzeSkeleton_.NONE, false, false, null, true, false);
		return skelResult.getListOfJunctionVoxels();//junction vs junction voxels? (multiple junction points may be detected for one junction)
	}
	
	/**
	 * 
	 * @param center
	 * @param binIP
	 * @return
	 */
	private int getWallDistance(Point center, final ImageProcessor binIP) {
		int width = binIP.getWidth();
		int height = binIP.getHeight();
		
		int radius = 1, i;
		int[] ex = new int[8];
		int[] ey = new int[8];
		
		boolean done = false;
		
		while(true) {
			ex[0] = center.x + radius;
			ey[0] = center.y;
			
			ex[1] = center.x + radius;
			ey[1] = center.y + radius;
			
			ex[2] = center.x;
			ey[2] = center.y + radius;
			
			ex[3] = center.x - radius;
			ey[3] = center.y + radius;
			
			ex[4] = center.x - radius;
			ey[4] = center.y;
			
			ex[5] = center.x - radius;
			ey[5] = center.y - radius;
			
			ex[6] = center.x;
			ey[6] = center.y - radius;
			
			ex[7] = center.x + radius;
			ey[7] = center.y - radius;
			
			for(i = 0; i < 8; i++) {
				if(SomaDilation.isWithinBounds(ex[i], ey[i], width, height) 
						&& binIP.getPixel(ex[i], ey[i]) == 255) {
					done = true;
					break;
				}
			}
			
			if(done)
				break;
						   
			radius++;
		}//End of while
		
		return radius;
	}
	
	public boolean isJunction(Point p, final BinaryProcessor binIP) {
		int d = getWallDistance(p, binIP);
		double rmin = d * ALPHA;
		double rmax = d * BETA;
		
		int rad = (int)rmax;
		int dim = 2 * rad + 1;	//width/height of the rectangular region to process
		int[] tag = new int[dim * dim];
		
		//System.out.println("rmin=" + rmin);
		//System.out.println("rmax=" + rmax);
		
		binIP.setRoi(new OvalRoi(p.x, p.y, dim, dim));
		
		boolean[] isObjPixel = new boolean[dim * dim];
		
		int x, y;
		int value;
		int arrayIndex;
		
		double distance = 0;
		
		//Load the image section in a one dimension array of boolean
        arrayIndex = 0;
        for(y = p.y - rad; y <= p.y + rad; y++) {
			for(x = p.x - rad; x <= p.x + rad; x++) {
				value = binIP.getPixel(x, y);
				if(value == 0) {
					distance = p.getDistanceTo(x, y);
					if(distance <= rmax && distance >= rmin)
						isObjPixel[arrayIndex] = true;
					else
						isObjPixel[arrayIndex] = false;
				}
				else
					isObjPixel[arrayIndex] = false;
				
				arrayIndex++;
			}
		}
        
        
        int tagvois;
	    int ID = 1;
	    arrayIndex = 0;
	    int minTag;
	    int i, offset;
	    
	    int nX = -1, nY = -1;
		
	    //First ID attribution
	    for(y = p.y - rad; y <= p.y + rad; y++) {
			for(x = p.x - rad; x <= p.x + rad; x++) {
				if(isObjPixel[arrayIndex]) {
					tag[arrayIndex] = ID;
					minTag = ID;
					
					i = 0;
                    //Find the minimum tag in the neighbors pixels
					for (nY = y - 1; nY <= y + 1; nY++) {
						for (nX = x - 1; nX <= x + 1; nX++) {
							if(withinBounds(nX, nY, p, rad)) {
								offset = offset(nX, nY, p.x - rad, p.y - rad, dim);
								if(isObjPixel[offset]) {
									i++;						//If neighbor pixel is object, increment neighbor object pixel count
									tagvois = tag[offset];
									if (tagvois != 0 && tagvois < minTag) 	//If any neighbor object pixel is already tagged, use that
										minTag = tagvois;
								}
							}
						}
					}
					
                    tag[arrayIndex] = minTag;
                    if (minTag == ID){					//If current pixel was given the new tag, increment tag number for next run
                        ID++;
                    }
				}
				arrayIndex++;
			}
		}
        
	    //Minimization of IDs = connection of structures
	    arrayIndex = 0;
	    for(y = p.y - rad; y <= p.y + rad; y++) {
			for(x = p.x - rad; x <= p.x + rad; x++) {
				if(isObjPixel[arrayIndex]) {
					minTag = tag[arrayIndex];
					
					//Find the minimum tag in the neighbors pixels
					for (nY = y - 1; nY <= y + 1; nY++) {
						for (nX = x - 1; nX <= x + 1; nX++) {
							if(withinBounds(nX, nY, p, rad)) {
								offset = offset(nX, nY, p.x - rad, p.y - rad, dim);
								if(isObjPixel[offset]) {
									tagvois = tag[offset];
									if (tagvois != 0 && tagvois < minTag)
										minTag = tagvois;
								}
							}
						}
					}
					
					//Replacing tag by the minimum tag found
					for (nY = y - 1; nY <= y + 1; nY++) {
						for (nX = x - 1; nX <= x + 1; nX++) {
							if(withinBounds(nX, nY, p, rad)) {
								offset = offset(nX, nY, p.x - rad, p.y - rad, dim);
								if(isObjPixel[offset]) {
									tagvois = tag[offset];
									if (tagvois != 0 && tagvois != minTag)
										replacetag(tagvois, minTag, tag);
								}
							}
						}
					}
				}
				arrayIndex++;
			}
	    }
		
		
	    //GET Tag counts
	    //Key: ID, value: pixel count
		HashMap<Integer, Integer> tagCount = new HashMap<Integer, Integer>();	
		
		arrayIndex = 0;
		for(y = p.y - rad; y <= p.y + rad; y++) {
			for(x = p.x - rad; x <= p.x + rad; x++) {
				if(tag[arrayIndex] != 0) {
					int count = 0;
					if(tagCount.containsKey(tag[arrayIndex])) {
						count = tagCount.get(tag[arrayIndex]);
					}
					
					count++;
					tagCount.put(tag[arrayIndex], count);
				}
				
				arrayIndex++;
			}
		}
		
		ArrayList<Integer> connectedObj = new ArrayList<Integer>();
		for(Integer item : tagCount.keySet())  {
			if(tagCount.get(item) > OBJ_THRES)
				connectedObj.add(item);				
		}
			
		System.out.println("Original: " + tagCount.keySet().size());
		System.out.println("Reduced: " + connectedObj.size());
		//If more than one tag => more than one object, so it is true junction (tags with few pixels eliminated)
		//Also, if original (without applying threshold for size) had large number of branching
		if(connectedObj.size() >= 3 || tagCount.keySet().size() >= 5)
			return true;
		else
			return false;
	}
	
	private boolean withinBounds(int x, int y, Point center, int rad) {
	    return (x >= center.x - rad && x <= center.x + rad && y >= center.y - rad && y <= center.y + rad);
	}
	//Calculates the 1D array index for given x and y in the region starting at startX, startY
	private int offset(int x, int y, int startX, int startY, int width) {
	    return (x - startX) + (y - startY) * width;
	}
	
	private void replacetag(int m, int n, int[] tag){
	    for (int i=0; i < tag.length; i++) 
	    	if (tag[i] == m) tag[i] = n;
	}
}