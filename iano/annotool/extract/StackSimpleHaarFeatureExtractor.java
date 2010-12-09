package annotool.extract;

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

	public StackSimpleHaarFeatureExtractor(annotool.io.DataInput problem, int level)
	{
		this.problem = problem;
		this.length = problem.getLength();
		this.width  =  problem.getWidth();
		this.height  = problem.getHeight();
		this.stackSize  = problem.getStackSize();
		this.level = level;
	}

	public float[][] calcFeatures()
	{
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


}
