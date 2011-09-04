package annotool.extract;

import annotool.ImgDimension;
import annotool.io.DataInput;
import java.util.ArrayList;
/*
 *  weighted average of middle stack features
 * 
 */
public class StackSimpleHaarFeatureExtractor implements FeatureExtractor {

	
	public final static String LEVEL_KEY = "Wavelet Level";
	public final static String STACKS_TO_INCLUDE = "Midstacks to Include";
	public final static String WEIGHT_KEY = "WEIGHTED";

	protected float[][] features = null;
	//one of the two below will contain data to work on
	annotool.io.DataInput problem = null;
	ArrayList all3DData = null;
	int length; 
	ImgDimension dim = new ImgDimension();
	int stackSize;
	int level;
	int imageType;

	int stacksToInclude = 3; //#of center stacks considered, typically an odd num
	boolean weighted = true; //default: stacks are weighted differently

	public StackSimpleHaarFeatureExtractor()
	{}
	
	public void setParameters(java.util.HashMap<String, String> parameters)
	{
	    if (parameters != null && parameters.containsKey(LEVEL_KEY))
			  this.level = Integer.parseInt(parameters.get(LEVEL_KEY));
	    if (parameters != null && parameters.containsKey(STACKS_TO_INCLUDE))
			  this.stacksToInclude = Integer.parseInt(parameters.get(STACKS_TO_INCLUDE));
	    if (parameters != null && parameters.containsKey(WEIGHT_KEY))
		     weighted = Boolean.parseBoolean(parameters.get(WEIGHT_KEY));
	}

	public float[][] calcFeatures(DataInput problem) throws Exception
	{
		this.problem = problem;
		this.length = problem.getLength();
		this.dim.width = problem.getWidth();
		this.dim.height = problem.getHeight();
		this.imageType = problem.getImageType();

		this.stackSize  = problem.getStackSize();
		//the uplimit of stacksToInclude is stackSize
		if(stacksToInclude > stackSize)
			stacksToInclude = stackSize;
		return calcFeatures();
	}
	
	//What is the data type? --
	// ArrayList (image set) of ArrayList (stacks) of byte[]/int[]/float[]
	public float[][] calcFeatures(ArrayList all3DData, int imageType, ImgDimension dim) throws Exception 
	{
		
		this.length = all3DData.size();
		this.dim = dim;
		this.imageType = imageType;
		this.all3DData = all3DData;
		this.stackSize = ((ArrayList)all3DData.get(0)).size();
		if(stacksToInclude > stackSize)
			stacksToInclude = stackSize;
		
		return calcFeatures();
	}
	
	//the method that calculates the features based on either all3DData or problem
	private float[][] calcFeatures() throws Exception
	{
		if(features == null)
		 features  = new float[length][dim.width*dim.height]; 

		float[][] features4CurrentStack;

		float weight;
		int mid = stackSize/2; 
		int lefthalf = stacksToInclude/2;
		int righthalf = lefthalf;
		if (stacksToInclude %2 == 1) //odd number
			righthalf = stacksToInclude/2 +1;

		//get an extractor obejct
		HaarFeatureExtractor haar = new HaarFeatureExtractor();
		java.util.HashMap<String, String> para = new java.util.HashMap<String, String>();
		para.put(HaarFeatureExtractor.LEVEL_KEY, String.valueOf(level));
		haar.setParameters(para);
		
		if(all3DData != null && problem == null)
		{
 		  float[] features4OneImage = new float[dim.width*dim.height];
  		  for(int i=0; i< this.length; i++)
		  {
			  for(int stackIndex= mid-lefthalf;  stackIndex < mid + righthalf; stackIndex ++)
			  {  
		        Object oneImageData = ((ArrayList)all3DData.get(i)).get(stackIndex);		  
		        haar.getHaarFeatureOfOneImage(oneImageData, features4OneImage);
		        weight = getWeightForStack(stackIndex, stackSize); 
		        for(int j=0; j<dim.width*dim.height; j++)
		        	features[i][j]+=weight*features4OneImage[j];
			  }
		  }
		}
		else //DataInput is used instead of ArrayList
		{
		  //stacks to include, for all images
		  for (int stackIndex = mid-lefthalf;  stackIndex < mid + righthalf; stackIndex ++) 
		  {
			//certain stack of all images  
			ArrayList data = problem.getData(stackIndex);
			features4CurrentStack = haar.calcFeatures(data, imageType, dim); 

			weight = getWeightForStack(stackIndex, stackSize);
			//System.out.println("stack index: " + stackIndex + " weight: " + weight);

			//add all stacks together
			addFeatures(features,features4CurrentStack, length, dim.width*dim.height, weight);
			} 
		}
		return features;
	}

    private float getWeightForStack(int stackIndex, int stackSize)
    {
    	float weight = 1;
    	if(weighted)
    	{
    	  int mid = stackSize/2;
		  //consider weighting: middle stacks are weighted more
		  // N(stackSize/2, 1.0) or linear
		  if (stackIndex < mid/2 || stackIndex > mid*1.5) weight = 0;
		  else  if (stackIndex <= mid)
			weight = (float) stackIndex/mid;
		  else
			weight = (float) (stackSize - stackIndex)/mid;
    	}
		return weight;
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
