package annotool.extract;

import annotool.io.DataInput;

/*
 *  weighted average of stack features
 * 
 */
public class StackSimpleHaarFeatureExtractor implements FeatureExtractor {

	protected float[][] features = null;
	annotool.io.DataInput problem;
	int length, width, height;
	int stackSize;
	int level;
    public final static String LEVEL_KEY = "Wavelet Level";
    boolean workOnRawBytes = true; //work as the first feature extractor by default
    

	public StackSimpleHaarFeatureExtractor(annotool.io.DataInput problem, int level)
	{
		this.problem = problem;
		this.length = problem.getLength();
		this.width  =  problem.getWidth();
		this.height  = problem.getHeight();
		this.stackSize  = problem.getStackSize();
		this.level = level;
	}

	
	public StackSimpleHaarFeatureExtractor(java.util.HashMap<String, String> parameters)
	{
	    if (parameters != null && parameters.containsKey(LEVEL_KEY))
			  this.level = Integer.parseInt(parameters.get(LEVEL_KEY));

	}
	
	public float[][] calcFeatures(float[][] data, annotool.io.DataInput problem)
	{
	 	//this.features = data;
		//workOnRawBytes = false;
		//return calcFeatures(problem);

		//input is already an 2D array, not a set of image stacks
		System.out.println("This 3D feature extractor can only be applied to raw image stacks.");
		System.out.println("It can not be used as a subsequent feature extractor");
		
		System.exit(1);
		
		return null;
	}
	
	public float[][] calcFeatures(DataInput problem)
	{
		this.problem = problem;
		this.length = problem.getLength();
		this.width  =  problem.getWidth();
		this.height  = problem.getHeight();
		this.stackSize  = problem.getStackSize();

		return calcFeatures();
	}
	
	public float[][] calcFeatures()
	{
		if(features == null)
		 features  = new float[length][width*height]; 

		float[][] features4CurrentStack;
		byte[][] data;

		float weight;
		int mid = stackSize/2; 

		for(int stackIndex = 1; stackIndex <= stackSize; stackIndex ++)
		{
			if (stackIndex > mid - 2 && stackIndex < mid + 2) //using the middle 3 frame for speed purpose
			{
				data = problem.getData(stackIndex);
				features4CurrentStack = (new HaarFeatureExtractor(level, data, length, width, height)).getFeatures();

				//consider weighting: middle stacks are weighted more
				// N(stackSize/2, 1.0) or linear
				if (stackIndex < mid/2 || stackIndex > mid*1.5) weight = 0;
				else  if (stackIndex <= mid)
					weight = (float) stackIndex/mid;
				else
					weight = (float) (stackSize - stackIndex)/mid;
				System.out.println("stack index: " + stackIndex + " weight: " + weight);

				addFeatures(features,features4CurrentStack, length, width*height, weight);

			} 

		}

		return features;
	}

	//add the second argument to the 1st argument.
	private void addFeatures(float[][] features, float[][] features4CurrentStack, int length, int dimension, float weight)
	{
		for(int i = 0; i < length; i++)
			for (int j = 0; j < dimension; j++)
				features[i][j] += weight*features4CurrentStack[i][j];
	}

	public boolean is3DExtractor()
	{  return true;} 

}
