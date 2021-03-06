import ij.plugin.filter.BackgroundSubtracter;
import ij.process.ImageProcessor;
import ij.process.ColorProcessor;
import ij.process.ByteProcessor;

public class Territory {
	private double radius;
	
	public Territory() {
		this(50);
	}
	
	public Territory(double radius) {
		this.radius = radius;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	public ImageProcessor run(ImageProcessor ip) {
		int width = ip.getWidth();
		int height = ip.getHeight();

		byte[] pixels = new byte[width*height];
		byte[] tmppixels = new byte[width*height];

		if(ip instanceof ColorProcessor) {
			((ColorProcessor)ip).getRGB(tmppixels,pixels,tmppixels);
			ip = new ByteProcessor(width, height, pixels, null);
		}		
		
		//Subtract background
		BackgroundSubtracter bs = new BackgroundSubtracter();
		//Parameters: ImageProcessor ip, double radius, boolean createBackground, boolean lightBackground, boolean useParaboloid, boolean doPresmooth, boolean correctCorners
		bs.rollingBallBackground(ip, radius, false, true, false, true, false);
		bs.rollingBallBackground(ip, radius, true, false, false, true, false);
		
		//Threshold
		ip.autoThreshold();
		
		return ip;
	}
}
