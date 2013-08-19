package annotool.classify.MLP;

import java.util.ArrayList;



/**
 *  This class contains support for the standard
 *  back propagation network.
 * 
 * 	 7/1/2013
 * 	 Modified class to be Serializable, added testModel 
 *   and omitted model, and a few other functions
 *    
 *  
*/   

public class StandardBP extends Algorithm
{
	
	private static final long serialVersionUID = -4365119072994894912L;
	boolean  isInterrupted = false;
	boolean  isTraining = false;
	
	int numsample;

	//for window momentum
	ArrayList<WMQueue>[] H_Delta;
	ArrayList<WMQueue>[] I_Delta;
	int Window_Size = 1; //if Size is 1, it is the same as normal BP.
	
	/**
	 * Constructor.  Sets up the standard BP algorithm and hooks
	 * it to the BPNet.
	 * 
	 */

	public StandardBP(BPNet net)
	{
		thenet = net;
		bpns = net.bpns;
	}
	

	/** this method define the activation function in the hidden neuron */
	public double f(double d)
	{ 
		return 1.0/(1.0+ Math.pow(Math.E,(-0.1)*d));
	}
    
	public void initwei()
	{  if(bpns != null)
		bpns.initwei();
	}

	/*
	 * Calculates the output of the network.
	 * Called by train() and testModel()
	 */
   private int forwardCalculation(float[] input, double[] hid, double[] out)
   { 
	int i, j;
	for(i=0; i< bpns.hnodes; i++)
	{
		hid[i] = 0;
		
		for (j=0; j< bpns.inodes; j++)
				hid[i] +=  bpns.weight[j][i]*input[j];
	 
		hid[i] = hid[i] - bpns.offset[i];
		hid[i] = f(hid[i]);
	}
   		
	for(i=0; i< bpns.onodes; i++)
	{
		out[i] = 0;
		
		for(j=0; j< bpns.hnodes; j++)
				out[i] += bpns.outweight[j][i]*hid[j];
	  

		out[i] = f(out[i]);
		
		if(out[i]> BPNetTraining.MAX_OUTPUT)  
			out[i] = BPNetTraining.MAX_OUTPUT;
		
		if(out[i]< BPNetTraining.MIN_OUTPUT)  
			out[i] = BPNetTraining.MIN_OUTPUT;
	} 		
  		 
	int maxcode = 0; 
	double max = out[0];  
	
	for(i=0; i< bpns.onodes; i++)
	{ 
		if(out[i]>max) {maxcode =i; max= out[i];}  
	}
	
	return maxcode; 
   }

   /* 
    * one iteration of training
    * Called by testModel
    */
private int backpropagation(int numsample)
   {
	
	   int bpflag =0, n, i, j, k, errorcount = 0;
	   double terr = 0;
	  
	   /* init output of hidden and output layers */
	   double hid[] = new double[thenet.bpns.hnodes];
	   double deltahid[] = new double[thenet.bpns.hnodes];
	   double out[] = new double[thenet.bpns.onodes];
	   double deltaout[] = new double[thenet.bpns.onodes];
     	
	   for(n=0; n< numsample; n++)
	   {
		   //forward calculation, input sample[n], get hid[], get out[].
		   forwardCalculation(sample[n], hid, out);
        
		   double perr =0.0;
		   int errorflag = 0, maxcode = 0; 
		   
		   double max = out[0];  		
		   
		   for(i=0; i< thenet.bpns.onodes; i++)
		   { 
			   perr += Math.pow((out[i]-desired[n][i]), (double) 2.0);
			   if(out[i]>max) 
			   {
				   maxcode =i; 
				   max= out[i];
			   }  
		   }
		 
		   if(code[n] != maxcode) 
			   errorflag =1;
		
		   errorcount=errorcount+errorflag;
		   perr=perr/2.0;
		   terr = terr + perr;
      
		   // error calculation and bp
		   double delta, momentumTerm;
		   
		   if(perr > BPNetTraining.MIN_PERR || errorflag == 1)
		   {
			   bpflag ++;
		  	
			   //adjust output layer weights: delta = y(1-y)(d-y) 
			   for(i=0; i<thenet.bpns.onodes; i++)
			   {
				   deltaout[i]= eita*gamma*out[i]*(1-out[i])*(desired[n][i]-out[i]);
				   
				   for(j=0; j<thenet.bpns.hnodes; j++)
				   {
						delta = deltaout[i]*hid[j];
						momentumTerm = alpha*(thenet.bpns.outweight[j][i] - lastoutweight[j][i]);
					   
					    H_Delta[j].get(i).push(momentumTerm); // added for windowed momentum
					   	lastoutweight[j][i] = thenet.bpns.outweight[j][i]; 
						thenet.bpns.outweight[j][i] += (delta + ( H_Delta[j].get(i).sum() / H_Delta[j].get(i).QSize() ));  //use the windowed momentum
				   }
				   //offset of output layer is not adjusted in this version, since is may be a simple summation.
			   }
   		  	
			   //adjust hidden layer weights and offsets
			   for(i=0; i<thenet.bpns.hnodes; i++)
			   {
				   //compute the delta, which summarizies all the output nodes that gets input from hidden node [i], 
				   //also called fan-out of hiddenode [i].
				   deltahid[i]=0;
				   for(k=0;k<thenet.bpns.onodes; k++)
					   deltahid[i] += deltaout[k] * thenet.bpns.outweight[i][k];
				   
				   deltahid[i] = eita*gamma*deltahid[i]*(hid[i])*(1-hid[i]);
				   
				   //adjust weight
				   for(j=0; j<thenet.bpns.inodes;j++)
				   {
						delta = deltahid[i]*sample[n][j]; 
						momentumTerm = alpha*(thenet.bpns.weight[j][i] - lastweight[j][i]);
	
						I_Delta[j].get(i).push(momentumTerm);
					   	lastweight [j][i] = thenet.bpns.weight[j][i];
						thenet.bpns.weight[j][i] +=  (delta + ( I_Delta[j].get(i).sum() / I_Delta[j].get(i).QSize() ));//for windowed momentum
				   }
			  
				   	//adjust offsets
				   thenet.bpns.offset[i] -= deltahid[i] + alpha * (thenet.bpns.offset[i]- lastoffset[i]);
				   lastoffset[i] = thenet.bpns.offset[i];
			   }
		  }
	}
   	
	return bpflag;
   }

   /* 
    * Randomly init the weights.
    * */
	@SuppressWarnings("unchecked")
	public void inittempwei()
	{
		int i, j;
		
		//init lastoffset:
		lastoffset = new double[thenet.bpns.hnodes];
		lastweight =  new double[thenet.bpns.inodes][thenet.bpns.hnodes];
		lastoutweight = new double[thenet.bpns.hnodes][thenet.bpns.onodes];
		
		java.util.Random rand = new java.util.Random();
		
		for(i=0;i<thenet.bpns.hnodes; i++)
				lastoffset[i] = rand.nextDouble()-0.5;
		
		for(i=0;i<thenet.bpns.inodes; i++)
		  for(j=0;j<thenet.bpns.hnodes; j++)
			lastweight[i][j]= rand.nextDouble()-0.5;
		
		for(i=0;i<thenet.bpns.hnodes; i++)
			  for(j=0;j<thenet.bpns.onodes; j++)
				lastoutweight[i][j]= rand.nextDouble()-0.5;


				
		 // Windowed Momentum
		 H_Delta = new ArrayList[thenet.bpns.hnodes];
		 I_Delta = new ArrayList[thenet.bpns.inodes];
		 System.out.println("Window Size: " + Window_Size);
		 
		 for(i=0; i< thenet.bpns.hnodes; i++)
		   {
			   
			   H_Delta[i] = new ArrayList<WMQueue>();
			   
			   for(j=0; j<thenet.bpns.onodes; j++)
			   {
				   H_Delta[i].add(new WMQueue(Window_Size));
			   }
		   }
		   
		   for(i=0; i<thenet.bpns.inodes; i++)
		   {
			   
			   I_Delta[i] = new ArrayList<WMQueue>();
			   
			   for(j=0; j<thenet.bpns.hnodes; j++)
			   {
				   I_Delta[i].add(new WMQueue(Window_Size));
			   }
		   } 
	}
  	

	/* Test the passed in data and return a result
	 * 
	 *
	/* It returns the maxcode, or -1 if rejection. */
	public int testModel(double prob[], int pro_index )
	{
	
			   int i = 0, maxcode1 = 0; 
			   double max1, max2;
			   double[] hid = new double[thenet.bpns.hnodes];
			   double[] out = new double[thenet.bpns.onodes]; 
			  
			   maxcode1 = forwardCalculation(test, hid, out);
		
			   max1= out[maxcode1];  
			   out[maxcode1]=0.0;  
			   max2= out[0];
			   for(i=1; i<thenet.bpns.onodes; i++)
			   {
				   if(out[i]>max2) 
				   { 
					   max2 = out[i]; 
				   }
			   }
			   
			   if( (max1-max2) < thvalue)
			   {
				   prob[pro_index] = 0;
				   return  -1;
			   }
			   else
			   {
				   prob[pro_index] = ( max2 / max1 );
				   return maxcode1;
			   }
	}
	

	
   /**
    * The train is started in a new thread if no training is undergoing right now for the same client.
 * @throws Exception 
    */
   public boolean startTrain( int numsample ) throws Exception
   {
   	 if (!isTraining) //only train if previous training is done.
   	 {
   	   prepareTrain( numsample );

   		   train();
   	    

   	   return true;
   	 }  
   	 else return false;
   }

   /*
    * set the parameters before trainign starts. Called by startTrain().
    */
   private void prepareTrain( int numsample )
	  {
	    this.numsample = numsample;
	  }
   
 
   /*
    * The actual training. It sends information back to the client during 
    * training and after training. Training can be stopped in the middle
    * by setting a flag "isInterrupted".
    * It sleeps between iterations for a multithreaded server.
    */
   private boolean train() throws Exception
   {
   	 isTraining = true;
   	 isInterrupted = false;
	 int i = 0;
	 
	 System.out.println( numsample * .3 + " " + hasWM );
	 
	 if( (int) ( numsample * .3 ) > 1 && hasWM )
	 {
		 Window_Size = (int) ( numsample * .3 );
	 }
	 else
	 {
		 Window_Size = 1;
	 }
	 
	 initwei();
	 inittempwei();
	 
	 
	 if( sample == null || code == null )
	     throw new Exception("data is not set properly for the BP classifier");


	 desired = new double[numsample][thenet.bpns.onodes];
	 	 
         //set the desired output, usually only useful for training. (refactored from Soybean for problem independence 5/25/13)
     for(int l=0; l < numsample; l++)
	 {	
 	     for(int k=0; k < thenet.bpns.onodes; k++)
               desired[l][k] = BPNetTraining.MIN_OUTPUT;
             desired[l][code[l]] = BPNetTraining.MAX_OUTPUT;
	 }

	double bpflag = backpropagation(numsample);
	double bfFlagmod = bpflag;
	
	while (i < iterations && bpflag > BPNetTraining.ERR_RATE * numsample)
	 {  
	 	if (isInterrupted)
	 	{
	 		System.out.println("TrainingStop");
			isTraining = false;
			return false;  //stopped prematurely 
	 	}
	 	
	 	i++;
	 	bpflag = backpropagation(numsample);
		
	 	//dynamic adjustment of parameters. The later stages needs smaller parameters.
		if(  bpflag <  bfFlagmod * .20 )
		{
		          alpha = 0.3;
				  eita = 0.1;
		}
		else if(  bpflag <   bfFlagmod * .50 )
		{
		          alpha = 0.5;
				  eita = 0.2;
		}
		else if(  bpflag <  bfFlagmod * .80 )
		{
		          alpha = 0.7;
				  eita = 0.3;
		}
		 
	 	System.out.println(i + " & "+bpflag + " alpha:  " + alpha + " eita: " + eita );
		
	 }
	
	//natural stop
	System.out.println("TrainingComplete");
	isTraining = false;
	return true;
   }

   /*
    * Prematurely interrupt the training.
    * 
    */
   public void stop()
   {
   	 System.out.println("Trying to stop the training ...");
   	 isInterrupted = true;
   }

}
