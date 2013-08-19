package annotool.classify.MLP;

import java.io.*;

/**
 *  This class provides a framework for training and testing using a
 *  particular algorithm.
 *  
 *  
     Modified class to be Serializable,
     added test array, which is needed for testModel
 *  
 *  
 */

public abstract class Algorithm implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3509709877937454011L;
	BPNetStructure bpns;
	BPNet thenet;


	public final static double ALPHA = 0.5;
	public final static double EITA = 0.5;
	public final static double GAMMA = 0.5;
	
	// Training parameters
	double alpha = 0.5;
	double eita = 0.5;
	double gamma = 0.5;

	// Testing parameters
	double thvalue = 0.0;
    
	// Used by training and testing methods
	float sample[][] = null;
	float test[] = null; /* Test sub-set */
	int code[] = null;
	double desired[][] = null;
	double lastoffset[] = null;
	double lastweight[][] =  null;
	double lastoutweight[][] = null;
	double lastc[][] = null;
	int iterations = 1; 
	boolean hasWM = true;

	// The PrintWriter to send the output to (default should be set to System.out)
	PrintWriter clientWriter;

	/**
	 * startTrain using this algorithm. 
	 * @param samplefile The training samples file
	 * @param numsample The number of samples to read
	 * @param weifile The weight file to output
	 * @param errfile The file to write error messages to
	 * @param if the weights will be saved.
	 * @return If the trainining was not started because there is another unfinished training, return false.
	 * @throws Exception 
	 * */
	public abstract boolean startTrain( int numsample ) throws Exception;
	
	/**
	 *  Test using this algorithm.
	 *   @param  pro_index				index value of probability array
	 *   @param  prob				probability array, hold probability of predictions being correct
	 */

	public abstract int testModel( double prob[], int pro_index );
	
	/**
     *  Interrupt the training before completion
	 */
	
	
	public abstract void stop();

	


	/**
	 *  Initialize temporary weights (precedes training).
	 */
	public abstract void inittempwei();

	/**
	 *  Write the training results to the weight file
	 *  @param weifile The weight file name to write
	 */


	/**
	 *  Save the results summary from testing 
	 *  @param resfile The results file to write
	 *  @param numsample The number of input samples tested
	 *  @param errorcount The number of errors in training
	 *  @param rejcount The number of samples rejected
	 */
	
	/** Set the training parameter alpha */
	public void setAlpha(double alpha)
	{
		this.alpha = alpha;
	}

	/** Set the training parameter for eita */
	public void setEita(double eita)
	{
		this.eita = eita;
	}

	/** Set the training parameter for gamma */
	public void setGamma(double gamma)
	{
		this.gamma = gamma;
	}
	
	public void setThreshold(double thvalue)
	{
		this.thvalue = thvalue;
	}

	/** Set the number of training iterations */
	public void setIterations(int i)
	{
		iterations = i;
	} 

	/**
	 *  Set the PrintWriter the algorithm uses for training/testing output 
	 *  Console applications can use the default System.out, however
	 *  output from the algorithm (ex: a socket over a network connection)
	 */
	public void setPrintWriter(PrintWriter out)
	{
		clientWriter = out;
	}
	
	/**
	 * return the BPNet embedded in the algorithm
	 */
	public BPNet getBPNet()
	{
		return thenet;
	}
	

}
