package annotool.classify.MLP;

public class BPNetTesting {
	
	BPNet thenet = null;
	Algorithm algorithm = null;


	public BPNetTesting(BPNet thenet)
	{
		this.thenet = thenet;
		algorithm = thenet.getAlgorithm();
	}
	

	 public BPNetTesting(int inode, int hnode, int onode)
	 {
		   thenet = new BPNet(new BPNetStructure(inode, hnode, onode));
		   algorithm = thenet.getAlgorithm();
	 }
 
	/** This acts as a wrapper for algorithm's test method */

	public int testModel(double prob[], int pro_index )
	{
		return algorithm.testModel( prob, pro_index );
	}
	

	/** Return the algorithm the testing is using (needed for interpreting weights) */
	public Algorithm getAlgorithm()
	{
		return algorithm;
	}    
}
