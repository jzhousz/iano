import ij.process.ImageProcessor;

public class Dilate {
	public static ImageProcessor run(ImageProcessor ip, ImageProcessor edgeIP, int targetX, int targetY) {
		int width = ip.getWidth();
		int height = ip.getHeight();
		
		ImageProcessor newIP = ip.duplicate();
		newIP.setColor(new java.awt.Color(0, 0, 0));
		
		int nX, nY;
		int value, nValue;
		boolean done;
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				value = ip.getPixel(x, y);
				
				done = false;
				
				if(value == 255) {
					if(edgeIP.getPixel(x, y) == 255 && (x != targetX && y != targetY))
						continue;
					//check if any neighbor pixels is black
					for (nY = y - 1; nY <= y + 1; nY++) {
						for (nX = x - 1; nX <= x + 1; nX++) {
							if(withinBounds(nX, nY, width, height)) {
								nValue = ip.getPixel(nX, nY);
								
								if(nValue == 0){
									newIP.drawPixel(x, y);
									done = true;
									break;
								}
							}
						}
						if(done)
							break;
					}
				}
			}
		}
		
		return newIP;
	}
	
	private static boolean withinBounds(int x, int y, int width, int height) {
	    return (x >= 0 && x < width && y >= 0 && y < height);
	}
}
