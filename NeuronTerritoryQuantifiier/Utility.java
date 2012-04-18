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
}
