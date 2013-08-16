package annotool.classify.MLP;

import java.io.Serializable;


public class BPNetTraining implements  Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4115750068659564520L;

	BPNet thenet = null;

	Algorithm algorithm = null;

	public final static double MAX_OUTPUT = 0.95;
	public final static double MIN_OUTPUT = 0.05;
	public final static double MIN_PERR = 0.1;
	public final static double ERR_RATE = 0.011;


	public BPNetTraining( BPNet bpnet )
	{
		thenet = bpnet;
		algorithm = thenet.getAlgorithm();
	}
	
	public BPNetTraining(int inode, int hnode, int onode) 
	{
		thenet = new BPNet(new BPNetStructure(inode, hnode, onode));
		algorithm = thenet.getAlgorithm();

	}


	/* overrides all the default para */
	public BPNetTraining( int inode, int hnode, int onode, double alpha, double eita) 
	{
		thenet = new BPNet(new BPNetStructure(inode, hnode, onode));
		algorithm = thenet.getAlgorithm();
		algorithm.alpha = alpha;
		algorithm.eita = eita;
	}

	public BPNetTraining( int inode, int hnode, int onode, double alpha, double eita, double gamma, Algorithm a) 
	{
		thenet = new BPNet(new BPNetStructure(inode, hnode, onode), a);
		algorithm = a;
		a.alpha = alpha;
		a.eita = eita;
		a.gamma = gamma;
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

}