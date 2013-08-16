package annotool.classify.MLP;

import java.io.Serializable;


/**
 *
 *  This class represents the structure of a MLP network with on layer of hidden nodes.
 *  Feedforward MLP is typically trained by BackPropagration algo, so it is also called BPNet.
 */

/* (non-javadoc: maintenance notes)
 * 
  7/1/2013
  Modified class to be Serializable, No longer need problems
  
  Keywords to search file for:
  MODIFY: 
  WARNING:

*/

public class BPNet implements Serializable{


	private static final long serialVersionUID = -8900454829462233767L;
	BPNetStructure bpns = null;
	Algorithm algorithm = null;
	
	
	
    public int numOfAttributes = 0;
    public int numOfClasses = 0;
    
    public int  inodes = 0; 
    public int  hnodes = 0;
    public int  onodes = 0;
    
	/**
	 *  Constructor takes the problem and algorithm as String objects
	 *  @param bpns The BPNetStructure to use
	 */
	public BPNet(BPNetStructure bpns)
	{
		this.bpns = bpns;
		inodes = bpns.inodes;
		hnodes = bpns.hnodes;
		onodes = bpns.onodes;
		algorithm = new StandardBP(this);
		
	}

	
	/**
	 *  Constructor takes the problem and algorithm as String objects
	 *  @param bpns The BPNetStructure to use
	 *  @param alg The algorithm to use used in the neural network
	 *  
	 *  if used with MLPClassifer, the iterations,
		alpha, eita, gamma, and thvalue will have to be set from algorithm 
		to the values in the MLP
	 */
	public BPNet(BPNetStructure bpns, Algorithm a)
	{
		this.bpns = bpns;
		algorithm = a;
		inodes = bpns.inodes;
		hnodes = bpns.hnodes;
		onodes = bpns.onodes;
	}

	
	public Algorithm getAlgorithm()
	{
		return algorithm;
	}
}
    
