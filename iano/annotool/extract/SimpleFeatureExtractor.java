package annotool.extract;

import annotool.ImgDimension;


/*
 * calculate 8 simple statistics features: 
 * mean and standard deviation of 4 divisions: left, right, upper, down
 */
public class SimpleFeatureExtractor implements FeatureExtractor {

	protected float[][] features = null;
	protected byte[][] data;
	int totalwidth;
	int totalheight;
	int length;

	public SimpleFeatureExtractor(java.util.HashMap<String, String> parameters)
	{
	}

	public SimpleFeatureExtractor()
	{
	}

	public void setParameters(java.util.HashMap<String, String> parameters)
	{}
	
	public SimpleFeatureExtractor(byte[][] data, int length, int width, int height)
	{
		totalwidth = width;
		totalheight = height;
		this.data = data;
		this.length = length;
	}
	
	@Override
	public float[][] calcFeatures(annotool.io.DataInput problem)
	{
		totalwidth = problem.getWidth();
		totalheight = problem.getHeight();
		this.length = problem.getLength();
		this.data = problem.getData();
		return calcFeatures();
	}
	
	
	public float[][] calcFeatures(byte[][] data, ImgDimension dim)
	{
		totalwidth = dim.width;
		totalheight = dim.height;
		this.length = data.length;
		this.data = data;
		return calcFeatures();
	}
	
	public float[][] calcFeatures() {
		// calculate simple 8 features: mean and st of 4 divisions: left, right, upper, down
		features  = new float[length][8]; //In Matlab, an 50*100 image has 5050 features due to rounding.
		for(int i=0; i <length; i++)
		{
			//how does java handle 2D array with scattered storage - ArrayList[]?)
			getSimpleFeatureOfOneImage(data[i], features[i]);
		}

		return features;

	}

	// calculate simple 8 features: mean and st of 4 divisions: left, right, upper, down
	protected void getSimpleFeatureOfOneImage(byte[] data, float[] feature)
	{
		//feature 0&1: left
		feature[0] = calMean(data,0,totalwidth/2,0,totalheight,totalwidth);
		feature[1] = calStd(data,0,totalwidth/2,0,totalheight,totalwidth, feature[0]);

		//feature 2&3: right
		feature[2] = calMean(data,totalwidth/2+1,totalwidth/2,0,totalheight,totalwidth);
		feature[3] = calStd(data,totalwidth/2+1,totalwidth/2,0,totalheight,totalwidth, feature[2]);

		//feature 4&5: top
		feature[4] = calMean(data,0,totalwidth,0,totalheight/2,totalwidth);
		feature[5] = calStd(data,0,totalwidth,0,totalheight/2,totalwidth, feature[4]);

		//feature 6&7: bottom
		feature[6] = calMean(data,0,totalwidth,totalheight/2+1,totalheight,totalwidth);
		feature[7] = calStd(data,0,totalwidth,totalheight/2+1,totalheight,totalwidth, feature[6]);
	}

	private float calMean(byte[] data, int left, int right, int top, int bottom, int totalwidth)
	{
		float mean = 0;	
		for(int i = left; i < right; i++)
			for (int j = top; j < bottom; j++)
				mean += data[i*totalwidth + j]&0xff;
		mean /= (right-left+1)*(bottom-top+1);
		return mean;
	}

	private float calStd(byte[] data, int left, int right, int top, int bottom, int totalwidth, float mean)
	{
		float std = 0;	
		for(int i = left; i < right; i++)
			for (int j = top; j < bottom; j++)
			{
				float value = data[i*totalwidth + j]&0xff;
				std += (value - mean)*(value - mean);
			}
		std /= (right-left+1)*(bottom-top+1);
		return (float)Math.sqrt(std);
	}

	/** get the features. If they were not calculated, calcFeatures() is called first. **/
	public float[][] getFeatures()
	{
		if (features == null)
			return calcFeatures();
		else
			return features;
	}    

	public boolean is3DExtractor()
	{  return false;} 
}


