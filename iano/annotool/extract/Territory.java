package annotool.extract;

import ij.IJ;
import ij.plugin.filter.BackgroundSubtracter;
import ij.plugin.filter.RankFilters;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.HashMap;

import annotool.ImgDimension;
import annotool.io.DataInput;

/**
 * Extracts the object territory from the image.
 * The number of object pixels is the extracted feature.
 * 
 * @author santosh
 *
 */
public class Territory  implements FeatureExtractor {
	protected float[][] features = null;
	DataInput problem = null;
	double radius = 50;			//Default value for radius of the rolling ball
	int length;
	int imageType;
	
	protected ArrayList data;
	
	public final static String RADIUS_KEY = "Radius";

	@Override
	public void setParameters(HashMap<String, String> parameter) {
		if (parameter != null)
		    if(parameter.containsKey(RADIUS_KEY)) 
		    	radius = Double.parseDouble(parameter.get(RADIUS_KEY));		
	}

	@Override
	public float[][] calcFeatures(DataInput problem) throws Exception {
		this.problem = problem;
		this.length = problem.getLength();
		this.data = problem.getData();
		this.imageType = problem.getImageType();
		this.features = new float[length][1];
		
		return calcFeatures();
	}

	@Override
	public float[][] calcFeatures(ArrayList data, int imageType,
			ImgDimension dim) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected float[][] calcFeatures() throws Exception {		
		int width = problem.getWidth();
		int height = problem.getHeight();
		
		ImageProcessor ip = null;
		RankFilters filter = new RankFilters();
		BackgroundSubtracter bs = new BackgroundSubtracter();
		int[][] binaryData = null;
		
		for (int imageIndex = 0; imageIndex < this.length; imageIndex++) {
			if (imageType == DataInput.GRAY8 || imageType ==  DataInput.COLOR_RGB)
	            for (int i = 0; i < width*height; i++) 
	            	ip = new ByteProcessor(width, height, (byte[])data.get(imageIndex), null);
	        else if (imageType == DataInput.GRAY16)
	            for (int i = 0; i < width*height; i++) 
	            	ip = new FloatProcessor(width, height, (int[])data.get(imageIndex));
	        else if (imageType == DataInput.GRAY32)
	            for (int i = 0; i < width*height; i++) 
	            	ip = new FloatProcessor(width, height, (float[])data.get(imageIndex), null);
	        else
	              throw new Exception("Not supported image type in Moments: type "+imageType);
			
			//Apply despeckle : median filter with radius 1			
			filter.rank(ip, 1, RankFilters.MEDIAN);
			
			//Subtract background			
			//First: light background
			bs.rollingBallBackground(ip, this.radius, false, true, false, true, false);
			//Second: create background (don't subtract)
			bs.rollingBallBackground(ip, this.radius, true, false, false, true, false);	//Object pixels will be black and background white

			//Threshold
			ip.autoThreshold();

			binaryData = ip.getIntArray();

			//Count object pixels
			int count = 0;			
			for(int x = 0; x < ip.getWidth(); x++)
			    for(int y=0; y < ip.getHeight(); y++)
			    	if(binaryData[x][y] == 0)
			    		count++;
			
			features[imageIndex][0] = (float)count;
		}
		
		for(int i = 0; i < features.length; i++)
			System.out.println("Feauture " + (i + 1) + ": " + features[i][0]);
		
		return features;
	}

	@Override
	public boolean is3DExtractor() {
		// TODO Auto-generated method stub
		return false;
	}

}
