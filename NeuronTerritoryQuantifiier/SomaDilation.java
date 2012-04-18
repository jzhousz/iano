
import ij.process.ImageProcessor;

public class SomaDilation {
	
	/**
	 * 
	 * @param ip
	 * @param edgeIP
	 * @param threshold Distance from center of the object up to which the edge pixel should be considered
	 */
	public static ImageProcessor run(ImageProcessor ip, ImageProcessor edgeIP, int threshold) {
		int width = ip.getWidth();
		int height = ip.getHeight();
		
		Point center = calcCenter(ip);
		
		//Check if center is within bounds and on the blob
		if(!isWithinBounds(center, width, height) || ip.getPixel(center.x, center.y) == 255)
			return ip;
		
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
					if(isWithinBounds(ex[i], ey[i], width, height)) {
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
		
		if(maxIndex < 0)
			return ip;
		
		//Dilate until the farthest edge pixel is black
		while(ip.getPixel(edgePoint[maxIndex].x, edgePoint[maxIndex].y) == 255) {
			ip = Dilate.run(ip, edgeIP, edgePoint[maxIndex].x, edgePoint[maxIndex].y);
			//ip.dilate();
		}
		
		return ip;
	}
	
	/**
	 * Calculates the geometric center of the blob object in the image
	 */
	public static Point calcCenter(ImageProcessor ip) {
		int width = ip.getWidth();
		int height = ip.getHeight();
		
		Point c = new Point();
		int x_sum = 0, 
			y_sum = 0, 
			count = 0;
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				if(ip.getPixel(x, y) == 0) {
					x_sum += x;
					y_sum += y;
					count++;
				}					
			}
		}
		
		//Find center
		if(count > 0) {		
			c.x = x_sum / count;
			c.y = y_sum /count;
		}
		
		return c;
	}
	
	/**
	 * Returns true if the given point is within the rectangular area, otherwise false
	 * 
	 */
	public static boolean isWithinBounds(Point p, int width, int height) {
		return (p.x >= 0 && p.x < width && p.y >= 0 && p.x < height);
	}
	
	public static boolean isWithinBounds(int x, int y, int width, int height) {
	    return (x >= 0 && x < width && y >= 0 && y < height);
	}
}

class Point {
	public int x;
	public int y;
	
	Point() {
		x = 0;
		y = 0;
	}
	
	Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	Point(Point p) {
		this.x = p.x;
		this.y = p.y;
	}
	
	public double getDistanceTo(int x2, int y2) {
		double distance = Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y));
		return distance;		
	}
	
	public double getDistanceTo(Point p2) {
		return getDistanceTo(p2.x, p2.y);
	}
}
