package annotool.extract;

import ij.ImagePlus;
import imagescience.feature.Laplacian;
import imagescience.image.Axes;
import imagescience.image.Coordinates;
import imagescience.image.Image;

import java.util.ArrayList;
import java.util.HashMap;

import annotool.ImgDimension;
import annotool.io.DataInput;

public class FeatureJLaplacian implements FeatureExtractor {
	protected float[][] features = null;
	DataInput problem = null;
	int length;
	double scale = 1.0;
	
	public final static String SCALE_KEY = "Smoothing Scale";
	
	@Override
	public void setParameters(HashMap<String, String> parameter) {
		if (parameter != null && parameter.containsKey(SCALE_KEY))
		     scale = Double.parseDouble(parameter.get(SCALE_KEY));

	}

	@Override
	public float[][] calcFeatures(DataInput problem) throws Exception {
		this.problem = problem;
		this.length = problem.getLength();
		this.features = new float[this.length][problem.getWidth() * problem.getHeight()];
		return calcFeatures();
	}

	@Override
	public float[][] calcFeatures(ArrayList data, int imageType,
			ImgDimension dim) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected float[][] calcFeatures() throws Exception {
		//for each image in the set
		ImagePlus imp = null;
		Image img = null;
		
		Laplacian laplacian = new Laplacian();
		Coordinates startCO = new Coordinates(0, 0);
		
		int width = problem.getWidth();
		int height = problem.getHeight();
		
		double values[][] = new double[height][width];
		
        for (int imageIndex = 0; imageIndex < this.length; imageIndex++) {
        	imp = problem.getImagePlus(imageIndex);
        	img = Image.wrap(imp);
        	
        	img = laplacian.run(img, scale);
        	img.axes(Axes.X + Axes.Y);
        	img.get(startCO, values);
        	
        	int i = 0;
        	for(int y = 0; y < height; y++)
        		for(int x = 0; x < width; x++) {
        			features[imageIndex][i] = (float)values[y][x];
        			i++;
        		}
        	
        }
		return features;
	}

	@Override
	public boolean is3DExtractor() {
		// TODO Auto-generated method stub
		return false;
	}

}
