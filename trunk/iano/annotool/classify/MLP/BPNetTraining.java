package annotool.classify.MLP;

import java.io.Serializable;
import java.util.HashMap;


public class BPNetTraining implements  Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4115750068659564520L;

	BPNet thenet = null;

	Algorithm algorithm = null;
	//Nodes
	private int outputNodes = 0;
	
	HashMap<Integer, Integer> targetmap = null;
	/* transform coefficient to scale the data.  */
	float [] min = null;
	float [] max = null;

	public final static double MAX_OUTPUT = 0.95;
	public final static double MIN_OUTPUT = 0.05;
	public final static double MIN_PERR = 0.1;
	public final static double ERR_RATE = 0.011;


	public BPNetTraining( BPNet bpnet )
	{
		thenet = bpnet;
		algorithm = thenet.getAlgorithm();
	}
	
	public BPNetTraining(int inode, int hnode, float[][] trainer, int[] target) 
	{
		int [] convert = convertTargets(target);
		
		thenet = new BPNet(new BPNetStructure(inode, hnode, outputNodes));
		algorithm = thenet.getAlgorithm();
		algorithm.code = convert;
		setMinMax(trainer);

	}


	/** Train acts as a wrapper for algorithm's train method 
	 * @throws Exception */
	public boolean startTrain( int numsample) throws Exception 
	{
			return algorithm.startTrain( numsample );
	}

	/** Stop acts as a wrapper for algorithm's stop method */
	public void stop() {
		algorithm.stop();
	}

	/** Return the algorithm that BPNetTraining is using */
	public Algorithm getAlgorithm() {
		return algorithm;
	}

	/** Initialize temp weights based on the algorithm's needs */
	public void inittempwei() {
		algorithm.inittempwei();
	}
	
	private void setMinMax( float[][]  trainingpatterns)
	{
		min = new float[trainingpatterns[0].length]; 
		max = new float[trainingpatterns[0].length];
		
		for( int j = 0; j < trainingpatterns[0].length; j++)
		{
			min[j] = trainingpatterns[0][j];
			max[j] = trainingpatterns[0][j];
			
			for( int i = 0; i < trainingpatterns.length; i++ )
			{
				if( max[j] < trainingpatterns[i][j] )
				{
					max[j] = trainingpatterns[i][j];
				}
				else if( min[j] > trainingpatterns[i][j] )
				{
					min[j] = trainingpatterns[i][j];
				}
			}
			
			for( int i = 0; i < trainingpatterns.length; i++ )
			{
			    //if max[i] is the same as min[j] (the column is all the same), then they are all 0.99.
				if( max[j] == trainingpatterns[i][j] )
				{
					trainingpatterns[i][j] = (float) 0.99;
				}
				else if( min[j] ==trainingpatterns[i][j] )
				{
					trainingpatterns[i][j] = (float) 0.01;
				}
				else
				{
					trainingpatterns[i][j] = (trainingpatterns[i][j] - min[j])/( max[j] - min[j]);
					//limit the range to be 0.99 and 0.01
				    if (trainingpatterns[i][j] > (float) 0.99)
				      trainingpatterns[i][j] = (float) 0.99;
				    else if (trainingpatterns[i][j] < (float) 0.01)
				       trainingpatterns[i][j] = (float) 0.01;
				}
		
			}	
		
		}
	}
	
	private int[] convertTargets(int[] targets)
   	{
   		
   		int[] convertedTargets = new int[targets.length];
		
   		//targetmap is for converting back from new targets to original targets
		//key: new target; value: original target
		
   		if (targetmap == null)
			targetmap = new java.util.HashMap<Integer, Integer>();
		else
			targetmap.clear();

		//map the original target to new target, just for converting forward
		java.util.HashMap<Integer, Integer> orig2new = new java.util.HashMap<Integer, Integer>();

		int targetIndex = -1;
		for (int i=0; i < targets.length; i++)
		{
			if(!targetmap.containsValue(targets[i]))
			{
				targetIndex ++;
				targetmap.put(new Integer(targetIndex), new Integer(targets[i]));
				orig2new.put(new Integer(targets[i]),new Integer(targetIndex));
				convertedTargets[i] = targetIndex;
			}
			else
				convertedTargets[i] = ((Integer) orig2new.get(targets[i])).intValue();
		}	 
		
		outputNodes = orig2new.size();
		return convertedTargets;

   	
   	}

}