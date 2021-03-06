import java.util.ArrayList;

import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class DilateToEdge {
	public static void axonRun(final BinaryProcessor binaryIP, ImageProcessor axonIP) {
		int width = axonIP.getWidth();
		int height = axonIP.getHeight();
		
		JunctionDetector jd = new JunctionDetector();
		ArrayList<skeleton_analysis.Point> junctionVoxels = jd.run(new BinaryProcessor((ByteProcessor)binaryIP.duplicate()));
		
		//Boolean array to store if a given point in the image is a junction candidate
		boolean[] jCandidates = new boolean[width * height];
		for(skeleton_analysis.Point p : junctionVoxels) {
			int offset = Utility.offset(p.x, p.y, width);
			jCandidates[offset] = true;
		}
		
		//Erode detected axon to keep it within the edge
		for(int i=0; i < 4; i++)
			axonIP.erode();
		
		int[][] ipData = axonIP.getIntArray();
		int[][] binaryData = binaryIP.getIntArray();
		
		int boundary = getBoundary(ipData, width, height);
		
		int nX, nY;
		boolean replace, done;
		boolean didChange = true;	//To track if there were any changes (dilation) in the last run
		int loopCount = 0;
		
		boolean targetReached = false;
		while(didChange) {
			didChange = false;	
			
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {				
					done = false;
					replace = false;
					//Only process pixels that are white and within the object
					if(ipData[x][y] == 255 && binaryData[x][y] == 0) {//outer-if
						//check if any neighbor pixels is black and that none of the neighbor is on edge
						for (nY = y - 1; nY <= y + 1; nY++) {
							for (nX = x - 1; nX <= x + 1; nX++) {
								if(withinBounds(nX, nY, width, height)) {									
									if(binaryData[nX][nY] == 255) {//This is so that axon does not dilate into fine structures
										replace = false;
										done = true;
										break;
									}
									if(ipData[nX][nY] == 0) {
										replace = true;
										//done = true;
										//break;
									}
								}
							}
							if(done)
								break;
						}//End of neighbor pixel check
						if (replace) {
							if(!targetReached || x < boundary) {	//Change pixel only if target is not reached or if the pixel is towards left
								axonIP.set(x, y, 0);
								didChange = true;
							}
							
							//If the replaced pixel is a junction candidate, check if it is actually a junction
							if(x >= boundary && jCandidates[Utility.offset(x, y, width)]) {
								if(jd.isJunction(new Point(x, y), binaryIP)) {
									targetReached = true;
								}
							}
						}
					}//End of outer-if
				}
			}
			ipData = axonIP.getIntArray();
			loopCount++;
		} //End of while
		
		//Dilate once more to remove edge pixels
		axonIP.dilate();
	}
	public static void somaRun(final BinaryProcessor binaryIP, ImageProcessor somaIP) {
		int width = somaIP.getWidth();
		int height = somaIP.getHeight();
		
		JunctionDetector jd = new JunctionDetector();
		ArrayList<skeleton_analysis.Point> junctionVoxels = jd.run(new BinaryProcessor((ByteProcessor)binaryIP.duplicate()));
		
		//Boolean array to store if a given point in the image is a junction candidate
		boolean[] jCandidates = new boolean[width * height];
		for(skeleton_analysis.Point p : junctionVoxels) {
			int offset = Utility.offset(p.x, p.y, width);
			jCandidates[offset] = true;
		}
		
		//Erode detected axon to keep it within the edge
		for(int i=0; i < 4; i++)
			somaIP.erode();
		
		int[][] ipData = somaIP.getIntArray();
		int[][] binaryData = binaryIP.getIntArray();
		
		int nX, nY;
		boolean replace, done;
		boolean didChange = true;	//To track if there were any changes (dilation) in the last run
		int loopCount = 0;
		
		boolean targetReached = false;
		while(didChange) {
			didChange = false;	
			
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {				
					done = false;
					replace = false;
					//Only process pixels that are white and within the object
					if(ipData[x][y] == 255 && binaryData[x][y] == 0) {//outer-if
						//check if any neighbor pixels is black and that none of the neighbor is on edge
						for (nY = y - 1; nY <= y + 1; nY++) {
							for (nX = x - 1; nX <= x + 1; nX++) {
								if(withinBounds(nX, nY, width, height)) {									
									//if(binaryData[nX][nY] == 255) {
										//replace = false;
										//done = true;
										//break;
									//}
									if(ipData[nX][nY] == 0) {
										replace = true;
										done = true;
										break;
									}
								}
							}
							if(done)
								break;
						}//End of neighbor pixel check
						if (replace) {
							if(!targetReached) {	//Change pixel only if target is not reached or if the pixel is towards left
								somaIP.set(x, y, 0);
								didChange = true;
							}
							
							//If the replaced pixel is a junction candidate, check if it is actually a junction
							if( jCandidates[Utility.offset(x, y, width)]) {
								if(jd.isJunction(new Point(x, y), binaryIP)) {
									targetReached = true;
								}
							}
						}
					}//End of outer-if
				}
			}
			ipData = somaIP.getIntArray();
			loopCount++;
		} //End of while
		
		//Dilate once more to remove edge pixels
		somaIP.dilate();
	}
	public static void run2(final ImageProcessor edgeIP, final ImageProcessor binaryIP, ImageProcessor ip, int threshold) {
		int width = ip.getWidth();
		int height = ip.getHeight();
		
		//Reduce blob to center pixel and also get that center pixel (EDIT: changed to few rounds of erosion to bring inside the edge if any pixel is outside)
		Point center = DilateToEdge.createBlobCenters(ip);
		//If center is outside bounds or white, no need to dilate
		if(!SomaDilation.isWithinBounds(center, width, height) || ip.getPixel(center.x, center.y) == 255) {
			//TODO: set blob ip to white or return flag to indicate that this blob should not be used
			return;
		}
			
		Point targetPoint = DilateToEdge.getFarthestEdge(center, edgeIP, threshold);
		
		int[][] ipData = ip.getIntArray();
		int[][] binaryData = binaryIP.getIntArray();
		int[][] edgeData = edgeIP.getIntArray();
		
		int nX, nY;
		boolean replace, done;
		boolean didChange = true;	//To track if there were any changes (dilation) in the last run
		int loopCount = 0;
		
		boolean targetNotReached = true;
		while(didChange && targetNotReached) {
			didChange = false;	
			
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {				
					done = false;
					replace = false;
					//Only process pixels that are white and within the object
					if(ipData[x][y] == 255 && binaryData[x][y] == 0 && edgeData[x][y] == 0) {//outer-if
						//check if any neighbor pixels is black and that none of the neighbor is on edge
						for (nY = y - 1; nY <= y + 1; nY++) {
							for (nX = x - 1; nX <= x + 1; nX++) {
								if(withinBounds(nX, nY, width, height)) {									
									if(edgeData[nX][nY] == 255) {
										replace = false;
										done = true;
										break;
									}
									if(ipData[nX][nY] == 0) {
										replace = true;
										//done = true;
										//break;
									}
								}
							}
							if(done)
								break;
						}//End of neighbor pixel check
						if (replace) {
							ip.set(x, y, 0);
							didChange = true;
						}
					}//End of outer-if
				}
			}
			ipData = ip.getIntArray();
			loopCount++;
			
			//Check if there is any black pixel in 5x5 neighborhood of target pixel
			for (nY = targetPoint.y - 2; nY <= targetPoint.y + 2; nY++) {
				for (nX = targetPoint.x - 2; nX <= targetPoint.x + 2; nX++) {
					if(withinBounds(nX, nY, width, height) && ip.get(nX, nY) == 0) {
						targetNotReached = false;
						break;
					}
				}
				if(!targetNotReached)
					break;
			}
		} //End of while
		
		//Dilate a few times to include edge pixels (loop dilates to within a pixel of the edge + edge thickness is a few pixels)
		for(int i=0; i < 4; i++)
			ip.dilate();
	}
	
	private static boolean withinBounds(int x, int y, int width, int height) {
	    return (x >= 0 && x < width && y >= 0 && y < height);
	}
	
	public static Point createBlobCenters(ImageProcessor ip) {
		Point center = SomaDilation.calcCenter(ip);
		/*for(int y = 0; y < ip.getHeight(); y++)
			for(int x = 0; x < ip.getWidth(); x++)
				if(x != center.x || y != center.y)
					ip.set(x, y, 255);*/
		
		//Eroding instead of reducing to center pixel, 4 pixels used empirically, to remove any potential pixel being outside edge
		for(int i=0; i < 4; i++)
			ip.erode();
		
		return center;
	}
	
	public static Point getFarthestEdge(Point center, final ImageProcessor edgeIP, int threshold) {
		int width = edgeIP.getWidth();
		int height = edgeIP.getHeight();
		
		//Check if center is within bounds and on the blob
		//if(!SomaDilation.isWithinBounds(center, width, height) || ip.getPixel(center.x, center.y) == 255)
			//return null;
		
		//Initialize array to hold edge points in 8 directions
		Point[] edgePoint = new Point[8];	//Value will be null if it shoots out of bounds in that direction
		boolean[] done = new boolean[8];	//To keep track of which direction is done
		int doneCount = 0;
		for(int i = 0; i < 8; i++)
			done[i] = false;
		
		int radius = 1, i;
		int[] ex = new int[8];
		int[] ey = new int[8];
		
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
				if(!done[i]) {
					if(SomaDilation.isWithinBounds(ex[i], ey[i], width, height)) {
						if(edgeIP.getPixel(ex[i], ey[i]) == 255) {
							edgePoint[i] = new Point(ex[i], ey[i]);
							done[i] = true;
							doneCount++;
						}
					}
					else {
						edgePoint[i] = new Point(center);
						done[i] = true;
						doneCount++;
					}
				}
			}
			
			if(doneCount == 8)
				break;
						   
			radius++;
		}//End of while
		
		double distance = 0;
		double maxDistance = 0;
		int maxIndex = -1;
		for(i = 0; i < 8; i++) {
			distance = center.getDistanceTo(edgePoint[i]);
			if(distance > maxDistance && distance < threshold) {
				maxDistance = distance;
				maxIndex = i;
			}
		}
		
		return edgePoint[maxIndex];
	}
	
	/**
	 * Gives the vertical line that represents the right-most edge of the detected axon blob.
	 * It is used so that dilation will always continue left of this line even if junction pixel reached
	 * @param data
	 * @return
	 */
	private static int getBoundary(int[][] data, int width, int height) {
		for(int x = width - 1; x > 0; x--) {
			for(int y = 0; y < height; y++) {
				if(data[x][y] == 0)
					return x;					
			}
		}
		return 0;
	}
	
	/*public static void main(String[] args) {
		ImagePlus impBlob = new ImagePlus("test/blob.tif");
		ImagePlus impEdge = new ImagePlus("test/edge.tif");
		ImagePlus impBinary = new ImagePlus("test/binary.tif");
		
		ImageProcessor ipBlob = impBlob.getProcessor();
		ImageProcessor ipEdge = impEdge.getProcessor();
		ImageProcessor ipBinary = impBinary.getProcessor();
		DilateToEdge.run2(ipEdge, ipBinary, ipBlob, 35);
	}*/
}
