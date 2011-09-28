package annotool.extract;

import ij.ImagePlus;
import imagescience.feature.Statistics;
import imagescience.image.Image;

import java.util.ArrayList;
import java.util.HashMap;

import annotool.ImgDimension;
import annotool.io.DataInput;

public class FeatureJStatistics implements FeatureExtractor {
	protected float[][] features = null;
	DataInput problem = null;
	int length;
	private static final int numFeatures = 14;
	
	@Override
	public void setParameters(HashMap<String, String> parameter) {
		// TODO Auto-generated method stub

	}

	@Override
	public float[][] calcFeatures(DataInput problem) throws Exception {
		this.problem = problem;
		this.length = problem.getLength();
		features = new float[length][numFeatures];
		
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
		Statistics stats = new Statistics();
        for (int imageIndex = 0; imageIndex < this.length; imageIndex++) {
        	imp = problem.getImagePlus(imageIndex);
        	img = Image.wrap(imp);
        	
        	stats.run(img);
        	features[imageIndex][0] = (float)stats.get(Statistics.ADEVIATION);
        	features[imageIndex][1] = (float)stats.get(Statistics.ELEMENTS);
        	features[imageIndex][2] = (float)stats.get(Statistics.KURTOSIS);
        	features[imageIndex][3] = (float)stats.get(Statistics.L1NORM);
        	features[imageIndex][4] = (float)stats.get(Statistics.L2NORM);
        	features[imageIndex][5] = (float)stats.get(Statistics.MASS);
        	features[imageIndex][6] = (float)stats.get(Statistics.MAXIMUM);
        	features[imageIndex][7] = (float)stats.get(Statistics.MEAN);
        	features[imageIndex][8] = (float)stats.get(Statistics.MEDIAN);
        	features[imageIndex][9] = (float)stats.get(Statistics.MINIMUM);
        	features[imageIndex][10] = (float)stats.get(Statistics.MODE);
        	features[imageIndex][11] = (float)stats.get(Statistics.SDEVIATION);
        	features[imageIndex][12] = (float)stats.get(Statistics.SKEWNESS);
        	features[imageIndex][13] = (float)stats.get(Statistics.VARIANCE);
        	
        	System.out.println("Features for image: " + (imageIndex + 1));
        	for(int i=0; i < numFeatures; i++)
        		System.out.println(features[imageIndex][i]);
        }
		return features;
	}

	@Override
	public boolean is3DExtractor() {
		// TODO Auto-generated method stub
		return false;
	}

}
