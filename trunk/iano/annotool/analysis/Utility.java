package annotool.analysis;

public class Utility {
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
	
	//Calculates the 1D array index for given x and y
	public static int offset(int x, int y, int width) {
	    return x + y * width;
	}	
	
	/**
	 * Determines which pixels/voxels in the image passed is local maxima and returns the boolean array or result
	 */
	public static boolean[] getLocalMaxima(float[] data, int width, int height, int depth,
											int wX, int wY, int wZ) {
		int size = width * height * depth;
		boolean[] isMaxima = new boolean[size];
		float[] maxVal = new float[size];
		
		int lowth = 3;
		float sum = 0;
		int num = 0;
		int index = 0;
		int offsetj = 0;
		int offsetk = 0;
		float meanv; 
		
		//Adding all pixels
		for(int z = 0; z < depth; z++) {
			offsetk = z*width*height;		    
			for(int y = 0; y < height; y++) {
				offsetj = y * width;
		        for(int x = 0; x < width; x++) {
		        	index = offsetk + offsetj + x;
	                if(data[index] > lowth) {
	                	sum += data[index];
	                	num ++;
		            }
		        } //End of x
		    } //End of y
		} //End of z
		
		meanv = sum / num;
		
		int numIteration = 3;
		for(int it = 0; it < numIteration; it++) {
			float sum1 = 0, sum2 = 0;
			int num1 = 0, num2 = 0;
			
			for(int z = 0; z < depth; z++) {
				offsetk = z*width*height;		    
				for(int y = 0; y < height; y++) {
					offsetj = y * width;
			        for(int x = 0; x < width; x++) {
			        	index = offsetk + offsetj + x;
			        	
			        	if(data[index] > meanv) {
			        		sum1 += data[index];
			        		num1++;
			        	}
			        	else if(data[index] > lowth) {
			        		sum2 += data[index];
			        		num2++;
			        	}
			        } //End of x
			    } //End of y
			} //End of z
			
			//Adjust new threshold
		     meanv = (int)(0.5 * (sum1/num1 + sum2/num2)); //Is typecasting necessary?
		     
		} //End of iteration
		
		float max = 0;
		int offsetkl, offsetjl;
		float val;
		
		for(int z = 0; z < depth; z++) {
			offsetk = z*width*height;		    
			for(int y = 0; y < height; y++) {
				offsetj = y * width;
		        for(int x = 0; x < width; x++) {
		        	index = offsetk + offsetj + x;
		        	max = 0;
		        	
		        	int xb = x - wX; if(xb < 0) xb = 0;
		        	int xe = x + wX; if(xe >= width - 1) xe = width - 1;
		        	int yb = y - wY; if(yb < 0) yb = 0;
		        	int ye = y + wY; if(ye >= height - 1) ye = height - 1;
		        	int zb = z - wZ; if(zb < 0) zb = 0;
		        	int ze = z + wZ; if(ze >= depth - 1) ze = depth - 1;
                    
		        	for(int k = zb; k <= ze; k++) {
                        offsetkl = k*width*height;
                        for(int j = yb; j <= ye; j++) {
                            offsetjl = j*width;
                            for(int i = xb; i <= xe; i++) {
                                val = data[offsetkl + offsetjl + i];
                                if(max < val) max = val;
                            }
                        }
                   }
		        	
		        	maxVal[index] = max;
		        } //End of x
		    } //End of y
		} //End of z
		
		for(int z = 0; z < depth; z++) {
			offsetk = z*width*height;		    
			for(int y = 0; y < height; y++) {
				offsetj = y * width;
		        for(int x = 0; x < width; x++) {
		        	index = offsetk + offsetj + x;
	                if((data[index] == maxVal[index]) && (data[index] > meanv))
	                	isMaxima[index] = true;
	                else
	                	isMaxima[index] = false;
		        } //End of x
		    } //End of y
		} //End of z
		
		return isMaxima;
	}
}
