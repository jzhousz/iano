package annotool.classify;

import javax.swing.SwingUtilities;

import annotool.Annotation;

//This helper class is to take the fold number and all data to do cross validation
//pass to classifier, then do some output
public class Validator
{
   /****************
     K fold CV, with K <= length. When K == length, it is leave one out validation.
     Shuffling is needed before calling this method if the samples are ordered sequentially based on categories.

     @return overall recognition rate

   ****************/
	javax.swing.JProgressBar bar = null;
	int startPos = 0;
	int totalRange = 0;
	
	public Validator() {}
   
    public Validator(javax.swing.JProgressBar bar, int start, int total)
   {
	  this.bar = bar;
	  this.startPos = start;
	  this.totalRange = total;
   }
   public float  KFold(int K, int length, int dimension, float[][] data, int[] targets, Classifier classifier, boolean shuffle, Annotation[] results)
   {
	  if (shuffle)
	    shuffle(length, dimension, data, targets);

	  float[][] testingPatterns;
      float[][] trainingPatterns;
      int[] trainingTargets;
      int[] testingTargets;
      int[] predictions;
      double[] prob;
      int testinglength, traininglength; //may vary for the last fold 
      int foldsize = length/K; //size per fold except the last one;

	  if (K > length)
	  {
		  System.out.println("K-fold validation must have a K that is smaller than the total number of observations");
		  return 0;
      }

      int correct = 0;
      for (int k = 0; k < K; k++)
      {
    	 //if GUI, update 5 times 
    	 if (bar != null)
    		 if (k%(K/5) == 0)
    			    setProgress(totalRange/5*k/(K/5));
    		        
    	  
		 if (k == K-1)  //the last fold may have more testing samples than other folds.
		    testinglength = length/K + length %  K;
		 else
		    testinglength = length/K;

	     traininglength = length - testinglength;
	     System.out.println("foldsize:" + foldsize);
	     System.out.println("testinglength:" + testinglength);
	     System.out.println("traininglength: " + traininglength );
	     System.out.println("dimension: " + dimension );

	     testingPatterns = new float[testinglength][dimension];
         trainingPatterns = new float[traininglength][dimension];
         trainingTargets = new int[traininglength];
         testingTargets = new int[testinglength];
		 predictions = new int[testinglength];
		 prob = new double[testinglength];

		//setup testing patterns
		for(int i=0; i<testinglength; i++)
		  for(int j=0; j < dimension; j++)
		  {
		    testingPatterns[i][j] = data[k*foldsize+i][j];
		    testingTargets[i] = targets[k*foldsize+i];
	      }

        //setup training patterns before the testing samples
		for(int i=0; i< k*foldsize; i++)
		  for(int j=0; j < dimension; j++)
		  {
		    trainingPatterns[i][j] = data[i][j];
		    trainingTargets[i] = targets[i];
	      }
		//the training samples after the testing samples
        for(int i= k*foldsize + testinglength; i< length; i++)
		  for(int j=0; j < dimension; j++)
		  {
		    trainingPatterns[i-testinglength][j] = data[i][j];
		    trainingTargets[i-testinglength] = targets[i];
	      }

	     //send to the classifier
         classifier.classify(trainingPatterns, trainingTargets, testingPatterns, predictions, traininglength, testinglength, prob);

         //compare the output prediction with the real targets on the testing set
         for(int i=0; i<testinglength; i++)
         {
			System.out.println("predicted category:" + predictions[i]);
			System.out.println("actual category:" + testingTargets[i]);
			results[k*foldsize+i].anno = predictions[i];
	        if(predictions[i] == testingTargets[i])
	           correct ++;
	     }

      }

      //output the overal results
      System.out.println("overall recognition rate: " + (float)correct/length);
      return (float) correct/length;
   }


   /**************

   @return The annotation results (with probility estimation) for each sample based on Leave One Output

   **************/
   public void LOO(int length, int dimension, float[][] data, int[] targets, Classifier classifier, Annotation annos[], boolean shuffle)
   {
	   //No need to shuffle in LOO 12/23/08
	   //if (shuffle)
	   //   shuffle(length, dimension, data, targets);

	   int testinglength = 1;
	   int traininglength = length - testinglength;

	   float[][] testingPatterns = new float[testinglength][dimension];
	   float[][] trainingPatterns = new float[traininglength][dimension];
	   int[] trainingTargets = new int[traininglength];
	   //int testingTargets;
	   int[] prediction = new int[1];
	   double[] prob = new double[1];


	   for (int k = 0; k < length; k++)
	   {
		   	 //if GUI, update 5 times 
	    	 if (bar != null)
	    		 if (k%(length/5) == 0)
	    			 bar.setValue(startPos + (totalRange/5)*(k/(length/5)));
	 
		//setup testing patterns
		for(int i=0; i < testinglength; i++)
 	      for(int j=0; j < dimension; j++)
		  {
		    testingPatterns[i][j] = data[k+i][j];
		    //testingTargets = targets[k]; //not needed since not calculating recog rate
	      }
        //setup trainingpatterns
		for(int i=0; i< k; i++)
		  for(int j=0; j < dimension; j++)
		  {
		    trainingPatterns[i][j] = data[i][j];
		    trainingTargets[i] = targets[i];
	      }
		//the training samples after the testing samples
        for(int i=(k+1); i< length; i++)
		  for(int j=0; j < dimension; j++)
		  {
		    trainingPatterns[i-1][j] = data[i][j];
		    trainingTargets[i-1] = targets[i];
	      }
	     //send to the classifier
         classifier.classify(trainingPatterns, trainingTargets, testingPatterns, prediction, traininglength, testinglength, prob);
         //System.out.println("prediciton[0]:" + prediction[0]);
         annos[k].anno = prediction[0];
         annos[k].prob = prob[0];
	   }

   }

  //overloadded version: no testing targets 
  public int[] classify(float[][] trainingPatterns, float[][] testingPatterns, int[] trainingTargets, Classifier classifier)
  {
	  int testingLength = testingPatterns.length;
	  int[]	 predictions = new int[testingLength];
	  double[]	 prob = new double[testingLength];

	  classifier.classify(trainingPatterns, trainingTargets, testingPatterns, predictions, trainingPatterns.length, testingLength, prob);
	  return predictions;
  }  
  
   
   /*
    * Input: training and testing data and targets
    * Function: send to classify
    * Ouput: prediction results, recognition rate
    */
   public float classify(int trainingLength, int testingLength, int numoffeatures, float[][] trainingPatterns, float[][] testingPatterns,int[] trainingTargets, int[] testingTargets, Classifier classifier, Annotation[] annotations)
   {
	   int[]	 predictions = new int[testingLength];
	   double[]	 prob = new double[testingLength];
			
       classifier.classify(trainingPatterns, trainingTargets, testingPatterns, predictions, trainingLength, testingLength, prob);

       //calculate the recognition rate
       int correct = 0;
       for(int i=0; i<testingLength; i++)
       {
			//System.out.println("predicted category:" + predictions[i]);
			//System.out.println("actual category:" + testingTargets[i]);
    	    annotations[i].anno = predictions[i];
            //annos[i].prob = prob[i];
	        if(predictions[i] == testingTargets[i])
	           correct ++;
	    }
      //output the overall results
    System.out.println("overall recognition rate: " + (float)correct/testingLength);
    return (float) correct/testingLength;
       
   }
   
   
   //shuffle the data
   private void shuffle(int length, int dimension, float[][] data, int[] targets)
   {
       for (int i = 0; i < length; i++)
       {
	       int r = i + (int) (Math.random() * (length-i));   // between i and length-1
	       exch(data, targets, i, r, dimension);
	   }

   }

   //swap images i and j (row)
   private void exch(float[][] data, int[] targets, int i, int j, int dimension)
   {
      float tmp;

      for(int d=0; d<dimension; d++)
      {
        tmp = data[i][d];
        data[i][d] = data[j][d];
        data[j][d] = tmp;
      }

      int swap = targets[i];
      targets[i] = targets[j];
      targets[j] = swap;

   }
   
	private void setProgress(final int currentProgress)
	{
		if (bar!=null) 
	        SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	            	bar.setValue(currentProgress);
	            }
	            });
	}

}