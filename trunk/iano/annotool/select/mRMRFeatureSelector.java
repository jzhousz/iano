package annotool.select;

import java.util.HashMap;

/*
  parameters for mRMR:

  - threshold: a float number of the discretization threshold; non-specifying this parameter means no discretizaton (i.e. data is already integer); 0 to make binarization.

  - method: either \"MID\" or \"MIQ\" (Capital case), default is MID

  - number of features: A natural number. efault is 50.

  - max number of samples: a natural number, default is 1000. Note that if you don't have or don't need big memory, set this value small, as this program will use this value to pre-allocate memory in data file reading.

  - max number of variables/attibutes in data>   a natural number, default is 10000. Note that if you don't have or don't need big memory, set this value small, as this program will use this value to pre-allocate memory in data file reading.

    From the C version:
    ("\nUsage: mrmr -i <dataset> -t <threshold> [optional arguments]\n");
    ("\t -i <dataset>    .CSV file containing M rows and N columns, row - sample, column - variable/attribute.\n");
    ("\t -t <threshold> a float number of the discretization threshold; non-specifying this parameter means no discretizaton (i.e. data is already integer); 0 to make binarization.\n");
    ("\t -n <number of features>   a natural number, default is 50.\n");
    ("\t -m <selection method>    either \"MID\" or \"MIQ\" (Capital case), default is MID.\n");
    ("\t -s <MAX number of samples>   a natural number, default is 1000. Note that if you don't have or don't need big memory, set this value small, as this program will use this value to pre-allocate memory in data file reading.\n");
    ("\t -v <MAX number of variables/attibutes in data>   a natural number, default is 10000. Note that if you don't have or don't need big memory, set this value small, as this program will use this value to pre-allocate memory in data file reading.\n");
 */

public class mRMRFeatureSelector implements FeatureSelector
{

	// Loads the file mRMRNative.DLL at run-time
	static {
		System.loadLibrary("mRMRNative");
	}

	float[][] features;
	int[] targets;
	int length = 0;
	int dimension = 0;
	int numberofFeatures = 10; //default
	int[] indices = null;
	String method = annotool.Annotator.DEFAULT_MRMRTYPE; //Default is "mRMR-MIQ".
	
	//keys for parameter HashMap
	public final static String KEY_NUM = "Number of features";
    public final static String KEY_DISCRETE = "Discrete Flag";
    public final static String KEY_DIS_TH = "Discretize Threshold";
    public final static String KEY_METHOD = "Method";
    
	//taking in the parameters in a standardized way.
    public mRMRFeatureSelector(java.util.HashMap<String, String> parameters)
    {
		int threshold = 0;
		boolean discreteflag = true; //default is to discretize

    	if (parameters.containsKey(KEY_NUM))
			this.numberofFeatures = Integer.parseInt((String)parameters.get(KEY_NUM));
		if (parameters.containsKey(KEY_DISCRETE))
			discreteflag = (Integer.parseInt((String)parameters.get(KEY_DISCRETE)) == 1) ? true : false ;
		if (parameters.containsKey(KEY_DIS_TH))
			threshold = Integer.parseInt((String)parameters.get(KEY_DIS_TH));
		if (parameters.containsKey(KEY_METHOD))
			this.method = (String)parameters.get(KEY_METHOD);
		if(discreteflag)
		    //discretizeV2(features, length, dimension);
			discretize(features, length, dimension);
		
    }
	
    
    public mRMRFeatureSelector(float[][] features, int[] targets, String methodname, java.util.HashMap<String, String> parameters)
	{
		int threshold = 0;
		boolean discreteflag = true; //default is to discretize
		
		//get the parameters and set the instance variables.
		this.features = features;
		this.targets = targets;
		this.length = features.length;
		this.dimension = features[0].length;
		this.method = methodname;
		
		if (parameters.containsKey(KEY_NUM))
			this.numberofFeatures = Integer.parseInt((String)parameters.get(KEY_NUM));
		if (parameters.containsKey(KEY_DISCRETE))
			discreteflag = (Integer.parseInt((String)parameters.get(KEY_DISCRETE)) == 1) ? true : false ;
		if (parameters.containsKey(KEY_DIS_TH))
			threshold = Integer.parseInt((String)parameters.get(KEY_DIS_TH));
		//if (parameters.containsKey(KEY_METHOD))
		//	this.method = (String)parameters.get(KEY_METHOD);
		
		if(discreteflag)
			    //discretizeV2(features, length, dimension);
				discretize(features, length, dimension);
	}

	
	public mRMRFeatureSelector(float[][] features, int[] targets, int length, int dimension, int numberofFeatures, String method, boolean discreteflag, float threshold)
	{

		this(features, targets, length, dimension, numberofFeatures, discreteflag, threshold);	
		this.method = method;
	}

	//Use the default method (MIQ)
	public mRMRFeatureSelector(float[][] features, int[] targets, int length, int dimension, int numberofFeatures, boolean discreteflag, float threshold)
	{
		//discrete before mRMR selection
		this.features = features;
		this.targets = targets;
		this.length = features.length;
		this.dimension = dimension;
		this.numberofFeatures = numberofFeatures;
		if(discreteflag)
		    //discretizeV2(features, length, dimension);
			discretize(features, length, dimension);
		   //discretizeWithThreshold(features, length, dimension, threshold);
	}
	
	
	protected float[][] selectFeaturesGivenIndices(int[] indices)
	{
		return selectFeaturesGivenIndices(features, indices);
		
		/*
		float[][] selectedFeatures = new float[length][indices.length];
		
		for(int i=0; i<length; i++)
			for(int j=0; j<indices.length; j++)
			{
				//System.out.println("j: " + j + "feature index"+ indices[j]);
				selectedFeatures[i][j] = features[i][indices[j]];
				//System.out.println(selectedFeatures[i][j]);
			}

		return selectedFeatures; */
	}	
	
	//select based on data passed in (instead of instance variable)
	public float[][] selectFeaturesGivenIndices(float[][] data, int [] indices)
	{
		System.out.println("indices+"+indices);
		float[][] selectedFeatures = new float[data.length][indices.length];
		
		for(int i=0; i<data.length; i++)
			for(int j=0; j<indices.length; j++)
				selectedFeatures[i][j] = data[i][indices[j]];

		return selectedFeatures;
	}
	
	//interface method
	public float[][] selectFeatures(float[][] data, int[] targets)
	{
		this.features = data;
		this.targets = targets;
		this.length =  data.length;
		this.dimension = data[0].length;
		
		//debug
		/*
		for(int i =0; i<data.length; i++)
		{
			for(int j=0; j<data[i].length; j++)
				System.out.print(i+","+j+":"+data[i][j]+" ");
			System.out.println();
		}*/
				
		
		return selectFeatures();
	}
	
	//return the selected features in vectors (data need to be set first)
	protected float[][] selectFeatures()
	{
		
		this.indices = mRMRSelection();

		//For debugging
		System.out.println("returning indices in Java:");
		for(int i=0; i<indices.length; i++)
			System.out.print(indices[i]+" ");
		System.out.println();
		
		return selectFeaturesGivenIndices(indices);
	}

	//the one that calls the C native interface
	//return the column indices of the selected features
	protected int[] mRMRSelection()
	{
		
		//C++ mRMR takes 1D array
		float[] OneDfeatures = new float[length*(dimension+1)];
		for(int i=0; i< length; i++)
			for(int j=0; j< dimension+1; j++)
			{
			   if (j == 0)  
				   OneDfeatures[i*(dimension+1)+j] = targets[i];	
			   else
 			       OneDfeatures[i*(dimension+1)+j] = features[i][j-1];
			}

		//targets not used in C code since targets are passed in as first column
		if (method.equalsIgnoreCase("mRMR-MIQ"))
			return mRMRNative.miq(OneDfeatures, targets, numberofFeatures, length, dimension+1);
		else if (method.equalsIgnoreCase("mRMR-MID"))
			return mRMRNative.mid(OneDfeatures, targets, numberofFeatures, length, dimension+1);

		//by PHC, 081007
		else
			return null;

	}

	//discretize feature so that it only contains 3 values -1, 0, and +1,
	//called before mRMRfeature selection, if necessary.
	protected void discretize(float[][] features, int length, int dimension)
	{
        float sum = 0, mean = 0;
        for (int j=0; j< dimension; j++)
        {
  		  sum =0;
          for (int i =0; i<length; i++)
			   sum += features[i][j];
	      mean = sum/length;
          for (int i =0; i<length; i++)
          {
            if(features[i][j] > mean)
			     features[i][j] = 1;
			   else if(features[i][j] < mean)
			     features[i][j] = -1;
			   else
			       features[i][j] = 0;
	       
	     }
	   }
	}

	
	//discretize feature so that it only contains 3 values -1, 0, and +1,
	//called before mRMRfeature selection, if necessary.
	protected void discretizeWithThreshold(float[][] features, int length, int dimension, float threshold)
	{   
		//zscore to facilitate the application of the threshold
		zscore(features, length, dimension);
		
		int i,j;
		float tmpf = 0;
		for(j=0; j<dimension; j++)
		{
			for(i=0; i<length; i++)
			{ 
				tmpf = features[i][j];
			    if (tmpf > threshold)
			      tmpf = 1;
			    else if (tmpf < -threshold)
				  tmpf = -1;
			    else
				  tmpf = 0;
			   features[i][j] = tmpf;
			}
		 }
	}

	
	private void zscore(float[][] features, int length, int dimension)
	{  //use double for better precision
		int i, j;
		double cursum, curmean, curstd;
		for(j = 0; j < dimension; j++)
		{
			cursum = 0;
		    curmean = 0;
		    curstd = 0;
		    for (i = 0; i < length; i++)
		      cursum += features[i][j];
			curmean = cursum / length;
			cursum = 0;
			double tmpf;
			for (i = 0; i < length; i++)
			{
			    tmpf = features[i][j] - curmean;
			    cursum += tmpf * tmpf;
			}
			curstd = (length == 1) ? 0 : Math.sqrt (cursum / (length - 1));	//length -1 is an unbiased version for Gaussian
			for (i = 0; i < length; i++)
			  features[i][j] = new Double((features[i][j] - curmean) / curstd).floatValue();
		}	  
	}
	
    private Integer[] getTargetList()
    {
		java.util.ArrayList<Integer> targetList = new java.util.ArrayList<Integer>();
		for (int i=0; i < targets.length; i++)
			if(!targetList.contains(targets[i]))
				targetList.add(targets[i]);
		
		Integer res[] = new Integer[targetList.size()];
		return targetList.toArray(res);
    }

	//This method is good for image categories with different illuminations.
    //i.e, their means are very different.
	protected void discretizeV2(float[][] features, int length, int dimension)
	{
	   Integer[] list = getTargetList();
       for(int k = 0; k < list.length; k++)
       {	   
		float sum = 0, mean = 0;
        for (int j=0; j< dimension; j++)
        {
  		  sum =0;
          for (int i =0; i<length; i++)
			   sum += features[i][j];
	      mean = sum/length;
	      //System.out.println("mean--------" + mean);
          for (int i =0; i<length; i++)
          {
            if(targets[i] == list[k])
            {	
        	  if(features[i][j] > mean)
			     features[i][j] = 1;
			   else if(features[i][j] < mean)
			     features[i][j] = -1;
			   else
			       features[i][j] = 0;
            }   
	     }
	   }
      } 
	}

    public int[] getIndices()
    {
    	if (indices == null)
    		selectFeatures();
    	return indices;
    }
    

    
}

