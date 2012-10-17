package annotool.extract;

import annotool.ImgDimension;
import annotool.io.DataInput;
import java.util.ArrayList;
/**
 *  weighted average of middle stack features  (anisotropic)
 * 
 */
public class StackSimpleHaarFeatureExtractor implements FeatureExtractor {

	
	public final static String LEVEL_KEY = "Wavelet Level";
	public final static String STACKS_TO_INCLUDE = "Middle Stacks to Include";
	public final static String WEIGHT_KEY = "Weighted";

	protected float[][] features = null;
	//one of the two below will contain data to work on
	annotool.io.DataInput problem = null;
	ArrayList all3DData = null;
	int length; 
	ImgDimension dim = new ImgDimension();
	int stackSize;
	int level = 1;
	int imageType;

	int stacksToInclude = 3; //#of center stacks considered, typically an odd num
	boolean weighted = false; //default: stacks are weighted differently

	/**
	 * Default constructor
	 */
	public StackSimpleHaarFeatureExtractor()
	{}
	
   /**
    * Sets algorithm parameters from para 
    * 
    * @param  para  Each element of para holds a parameter’s name for its key
    *               and a parameter’s value for its value. The parameters
    *               should be the same as those in the algorithms.xml file.
    */
	public void setParameters(java.util.HashMap<String, String> parameters)
	{
	    if (parameters != null && parameters.containsKey(LEVEL_KEY))
			  this.level = Integer.parseInt(parameters.get(LEVEL_KEY));
	    if (parameters != null && parameters.containsKey(STACKS_TO_INCLUDE))
			  this.stacksToInclude = Integer.parseInt(parameters.get(STACKS_TO_INCLUDE));
	    if (parameters != null && parameters.containsKey(WEIGHT_KEY))
		     weighted = Boolean.parseBoolean(parameters.get(WEIGHT_KEY));
	}

   /**
    * Get features based on raw image stored in problem.
    * 
    * @param   problem    Image to be processed
    * @return             Array of features
    * @throws  Exception  (Not used)
    */
	public float[][] calcFeatures(DataInput problem) throws Exception
	{
		this.problem = problem;
		this.length = problem.getLength();
		this.dim.width = problem.getWidth();
		this.dim.height = problem.getHeight();
		this.imageType = problem.getImageType();

		//8/8/2012 If ROI, need to get ROI depth instead of image stacksize.
		if(problem.getMode() == problem.ROIMODE)
			this.stackSize = problem.getDepth();
		else
		    this.stackSize  = problem.getStackSize();
		
		//the uplimit of stacksToInclude is stackSize
		if(stacksToInclude > stackSize)
		{
			System.err.println("the stacks to include can be maximum "+stackSize);
			stacksToInclude = stackSize;
		}
		return calcFeatures();
	}
	
   /**
    * Get features based on all3DData, imageType, and dim.
    * 
    * @param   all3DData  Data taken from the image
    * @param   imageType  Type of the image
    * @param   dim        Dimenstions of the image
    * @return             Array of features
    * @throws  Exception  (Not used)
    */
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
		{
			System.err.println("the stacks to include can be maximum "+stackSize);
			stacksToInclude = stackSize;
		}
		return calcFeatures();
	}
	
	//This method calculates the features based on either all3DData or problem
	//These two inputs will be handled differently
	// For the all3DData, imageType needs to correspond to the data type in ArrayList, which maybe not the type of the original image, if already processed.
	//
	private float[][] calcFeatures() throws Exception
	{
		if(features == null)
		 features  = new float[length][dim.width*dim.height]; 

		for(int i=0; i<length; i++)
		 for(int j=0; j<dim.width*dim.height; j++)
			 features[i][j]  = 0.0f; 

		float[][] features4CurrentStack = new float[length][];

		float weight;
		int mid = stackSize/2; 
		int lefthalf = stacksToInclude/2;
		int righthalf = lefthalf;
		if (stacksToInclude %2 == 1) //odd number
			righthalf = stacksToInclude/2 +1;

		//get an extractor object //bug!Cann't reuse the same extractor!
		//HaarFeatureExtractor haar = new HaarFeatureExtractor();
		//haar.setParameters(para);
		
		java.util.HashMap<String, String> para = new java.util.HashMap<String, String>();
		para.put(HaarFeatureExtractor.LEVEL_KEY, String.valueOf(level));
		
		if(all3DData != null && problem == null)
		{
		  ArrayList listforstack= new ArrayList();
 		  for(int stackIndex=mid-lefthalf; stackIndex < mid+righthalf; stackIndex++)
 		  {
 			  for(int i=0; i< this.length; i++)
 			  {
		        Object oneImageData = ((ArrayList)all3DData.get(i)).get(stackIndex);
				listforstack.add(oneImageData);   
 			  }
			  HaarFeatureExtractor haar = new HaarFeatureExtractor();
  			  haar.setParameters(para);

			  features4CurrentStack = haar.calcFeatures(listforstack, imageType, dim);
 			  weight = getWeightForStack(stackIndex-1, stackSize);
 			  addFeatures(features,features4CurrentStack, length, dim.width*dim.height, weight);
 			  listforstack.clear();  
 		  }
		}
		else if (problem !=null)//DataInput is used instead of ArrayList
		{
		  //stacks to include, for all images
		  ArrayList<Object> data = new ArrayList<Object>(1); //An ArrayList of one image (to save memory)
		  //stackIndex starts from 1 for getData()!
		  for (int stackIndex = mid-lefthalf +1;  stackIndex <= mid + righthalf; stackIndex ++) 
		  {
			//certain stack of all images;
			System.out.println("Working on slice " + stackIndex);
			for(int imgindex = 0; imgindex < length; imgindex++)
			{
			  data.clear();
			  data.add(problem.getData(imgindex,stackIndex)); //changed  8/20/12
			  //10/16/2012 must get new object to avoid reusing the same feature space!!
			  HaarFeatureExtractor haar = new HaarFeatureExtractor();
			  haar.setParameters(para);
			  features4CurrentStack[imgindex] = haar.calcFeatures(data, imageType, dim)[0];
			} //end of images
			
			//add all stacks together
			weight = getWeightForStack(stackIndex-1, stackSize);
			System.out.println("The weight of  slice " + stackIndex + " is " + weight);
			addFeatures(features,features4CurrentStack, length, dim.width*dim.height, weight);
		  } //end of stacks
		  //return features4CurrentStack;
		  
		}else
		{
			throw new Exception("data is not passed in properly in feature extractor.");
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
			weight = ((float) stackIndex)/mid;
		  else
			weight = ((float) (stackSize - stackIndex))/mid;
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

    /**
     * Returns whether or not the algorithm is able to extract from a 3D image 
     * stack. 
     * 
     * @return  <code>True</code> if the algorithm is a 3D extractor, 
     *          <code>False</code> if not. Default is <code>True</code>
     */
	public boolean is3DExtractor()
	{  return true;} 

}
