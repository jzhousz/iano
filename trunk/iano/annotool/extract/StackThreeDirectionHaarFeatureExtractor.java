package annotool.extract;


/**
 *  3 directions (x, y, z)  3D wavelet features
 * 
 *  Total extracted features: width*height + width*stackSize + height*stackSize
 */
public class StackThreeDirectionHaarFeatureExtractor implements  FeatureExtractor {

	protected float[][] features = null;
	annotool.io.DataInput problem;
	int length, width, height;
	int stackSize;
	int level;
	public final static String LEVEL_KEY = "Wavelet Level";
	  
	public StackThreeDirectionHaarFeatureExtractor(java.util.HashMap<String, String> parameters)
	{
	    if (parameters != null && parameters.containsKey(LEVEL_KEY))
	 		     level = Integer.parseInt(parameters.get(LEVEL_KEY));
	}

	public StackThreeDirectionHaarFeatureExtractor(annotool.io.DataInput problem, int level)
	{
		this.problem = problem;
		this.length = problem.getLength();
		this.width  =  problem.getWidth();
		this.height  = problem.getHeight();
		this.stackSize  = problem.getStackSize();
		this.level = level;
	}
	
    public float[][] calcFeatures(annotool.io.DataInput problem)
    {
		this.problem = problem;
		this.length = problem.getLength();
		this.width  =  problem.getWidth();
		this.height  = problem.getHeight();
		this.stackSize  = problem.getStackSize();
		
		return calcFeatures();
    	
    }
	
	protected float[][] calcFeatures() {
		//number of extracted features
		int dim = width*height + width*stackSize + height*stackSize;
		features  = new float[length][dim];

		//get into 3D (4D with length) data format (temporary storage)
		byte[][][][] ThreeDData = new byte[length][width][height][stackSize];
		for(int stackIndex = 1; stackIndex <= stackSize; stackIndex ++)
		{
			//length * (width*height)
			byte[][] currentStack = problem.getData(stackIndex);
			for(int i=0; i<length; i++)
				for(int j=0; j<height; j++)
					for(int k=0; k<width; k++)
						ThreeDData[i][j][k][stackIndex-1]=currentStack[i][j*width+k];
		}

		//fill features at xy planes
		byte[][] data; //data at current plane of all images
		float[][] currentFeatures; //features at current plane of all images 
		for(int stackIndex = 1; stackIndex <= stackSize; stackIndex ++)
		{
			data = problem.getData(stackIndex); 
			currentFeatures = (new HaarFeatureExtractor(level, data, length, width, height)).getFeatures();
			//add and fill back into features
			addFeatures(features,currentFeatures, length, width*height, 1, 0);
		}


		//fill features at xz planes
		for(int yIndex = 0; yIndex < height; yIndex++)
		{
			//get that xz plane
			data = new byte[length][width*stackSize];
			for(int i=0; i<length; i++)
				for(int j=0; j<stackSize; j++)
					for(int k=0; k<width; k++)
						data[i][j*width+k] = ThreeDData[i][k][yIndex][j];

			currentFeatures = (new HaarFeatureExtractor(level, data, length, width, stackSize)).getFeatures();
			addFeatures(features,currentFeatures, length, width*stackSize, 1, width*height);
		}

		//fill features at yz planes
		for(int xIndex = 0; xIndex < width; xIndex++)
		{
			//get that yz plane
			data = new byte[length][height*stackSize];
			for(int i=0; i<length; i++)
				for(int j=0; j<stackSize; j++)
					for(int k=0; k<height; k++)
						data[i][j*height+k] = ThreeDData[i][xIndex][k][j];

			currentFeatures = (new HaarFeatureExtractor(level, data, length, height, stackSize)).getFeatures();
			addFeatures(features,currentFeatures, length, height*stackSize, 1, width*height+width*stackSize);
		}

		ThreeDData = null;

		return features;
	}

	//similar as the one in StackSimpleHaar, but take startDim.
	private void addFeatures(float[][] features, float[][] features4CurrentStack, int length, int dimension, float weight, int startDim)
	{
		for(int i= 0; i< length; i++)
			for (int j = 0; j<dimension; j++)
				features[i][j+startDim] += weight*features4CurrentStack[i][j];
	}
	
	public boolean is3DExtractor()
	{  return true;} 

}
