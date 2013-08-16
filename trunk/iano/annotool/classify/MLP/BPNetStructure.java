package annotool.classify.MLP;

import java.io.Serializable;


/**
 * This class defines the structures and parameters of the BPNet
 * 
 * 
 */

public class BPNetStructure  implements Serializable
{ 
	private static final long serialVersionUID = -6070106752736070051L;
//number of nodes for input, hidden and output
   int  inodes; 
   int  hnodes;
   int  onodes;
   double weight[][];
   double outweight[][];
   double offset[];
 	
   /** Constructor builds the network structure, allocating space for the weights. */
   public BPNetStructure(int inodes,int hnodes, int onodes)
   {
	   
	    this.inodes = inodes;
	    this.hnodes = hnodes;
		this.onodes = onodes;
		
		
		weight = new double[inodes][hnodes];
		outweight = new double[hnodes][onodes];
		offset = new double[hnodes];
   }
   
   /** Initialize the weights of the network */
   public void initwei()
   {
	/* important: weights must be between -0.5 and +0.5, instead of being all positive. */
	int i,j;
	java.util.Random rand = new java.util.Random();
	for(i=0;i<inodes; i++)
	  for(j=0;j<hnodes; j++)
	   {
	   weight[i][j]= rand.nextDouble()-0.5;
	   }
	for(i=0;i<hnodes; i++)
		  for(j=0;j<onodes; j++)
			outweight[i][j]= rand.nextDouble()-0.5;
	for(i=0;i<hnodes; i++)
			offset[i] = rand.nextDouble()-0.5;
   }
   
}
