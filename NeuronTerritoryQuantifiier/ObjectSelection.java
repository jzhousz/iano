import ij.process.ImageProcessor;

public class ObjectSelection {
	private int height, width;
	private Point center = null;
	private double[] distance = new double[8];
	
	public boolean isSoma(final ImageProcessor blobIP, final ImageProcessor edgeIP, double sdThreshold, double rangeThreshold) {
		boolean decision = false;
		
		width = blobIP.getWidth();
		height = blobIP.getHeight();
		
		center = SomaDilation.calcCenter(blobIP);
		//Geometric center has to be within the object
		if(SomaDilation.isWithinBounds(center, width, height) && blobIP.getPixel(center.x, center.y) == 0) {
			calcEdgeDistances(blobIP, edgeIP);
			
			double sd = ObjectSelection.getSD(distance);
			double range = ObjectSelection.getRange(distance);

			ij.IJ.write("SD:" + sd + " Range:" + range);
			
			//SD of the distance from the center to 8 edge points must be within the threshold
			if(sd <= sdThreshold && range <= rangeThreshold)
				decision = true;
		}
		return decision;
	}
	
	/**
	 * Calculates standard deviation of the values in the array
	 * 
	 * @param values
	 * @return
	 */
	public static double getSD(double[] values) {
		double mean = getMean(values);
		CompSumDouble sum = new CompSumDouble();
		for(int i=0; i < values.length; i++) {			
			sum.Add((values[i] - mean) * (values[i] - mean));
		}
		return Math.sqrt(sum.getSum()/(values.length - 1));
	}
	
	/**
	 * Calculates mean of the values in the array
	 * 
	 * @param values
	 * @return
	 */
	public static double getMean(double[] values) {
		if (values.length < 1) return 0;
		
		CompSumDouble sum = new CompSumDouble();
		for(int i=0; i < values.length; i++) {
			sum.Add(values[i]);
		}
		
		return sum.getSum()/values.length;
	} 
	
	/**
	 * Calculates range of values
	 * 
	 * @param values
	 * @return
	 */
	public static double getRange(double[] values) {
		if (values.length < 1) return 0;
		
		double min = values[0];
		double max = values[0];
		
		for(int i=0; i < values.length; i++) {
			if(values[i] < min)
				min = values[i];
			if(values[i] > max)
				max = values[i];
		}
		
		return max - min;
	} 
	
	
	private void calcEdgeDistances(final ImageProcessor ip, final ImageProcessor edgeIP) {
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
		
		//Calculate distances
		for(i = 0; i < 8; i++) 
			distance[i] = center.getDistanceTo(edgePoint[i]);
	}
}
