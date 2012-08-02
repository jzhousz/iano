package annotool.classify;

import java.util.HashMap;

import javax.swing.SwingUtilities;

import annotool.Annotation;

/**
* This helper class takes the fold number and all data to do cross validation,
* pass the data to the classifier, then do some output.
*/
public class Validator
{
	javax.swing.JProgressBar bar = null;
	int startPos = 0;
	int totalRange = 0;
	
	/**
    * Default constructor
    */
	public Validator() {}
   
	/**
    * Constructor that takes the progress bar, start, and total parameters and stores them in instance variables.
    * This is for showing the progress of cross-validation
    * 
    * @param  bar    Progress bar object
    * @param  start  Initial position of the progress bar
    * @param  total  Total range and final position of the progress bar
    */
    public Validator(javax.swing.JProgressBar bar, int start, int total)
    {
	   this.bar = bar;
	   this.startPos = start;
	   this.totalRange = total;
    }
    
   /*****************************
   * K fold CV with Classifier object, with K <= length. When K == length, it is leave one out (LOO) validation.
   * Shuffling is needed before calling this method if the samples are ordered sequentially based on categories.
   * 
   * It will split the data, and call the xxxGivenMethod in the Annotator class.
   * 
   * @param K            the number of the fold
   * @param data         two-dimensional array of the image features 
   * @param targets      array of targets for the data
   * @param classifier   classifier to be used
   * @param shuffle      whether or not the data needs shuffling
   * @param results      annotations to compare against
   * @return             overall recognition rate
   * @throws Exception   Thrown if K value is greater than the number of observations
   ********************************/
   public float  KFold(int K, float[][] data, int[] targets, Classifier classifier, boolean shuffle, Annotation[] results) throws Exception
   {
	  int length = data.length;
	  int dimension = data[0].length;
	  
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
         classifier.classify(trainingPatterns, trainingTargets, testingPatterns, predictions, prob);

         //compare the output prediction with the real targets on the testing set
         for(int i=0; i<testinglength; i++)
         {
			System.out.println("predicted category:" + predictions[i]);
			System.out.println("actual category:" + testingTargets[i]);
			results[k*foldsize+i].anno = predictions[i];
	        if(predictions[i] == testingTargets[i])
	           correct ++;
	     }
         
    	 //update GUI 5 times 
    	 if (bar != null)
    	 {
    		 if (K < 5)
    			   setProgress(startPos + k*totalRange/K);
    		 else if (k%(K/5) == 0)
    			    setProgress(startPos + totalRange/5*k/(K/5));
    	 }		        

      }

      //output the overall result
      setProgress(startPos + totalRange);
      System.out.println("overall recognition rate: " + (float)correct/length);
      return (float) correct/length;
   }


 
   /*****************************
   * K fold CV, with K <= length. When K == length, it is leave one out (LOO) validation.
   
   * Pass in String as the classifier name, and parameter as Hashmap.
   * It will split the data, and call the xxxGivenMethod in the Annotator class.
   * 
   * @param K                 the number of the fold
   * @param data              two-dimensional array of the image features 
   * @param targets           array of targets for the data
   * @param chosenClassifier  name of the classifier to use
   * @param para              parameters for the classifier to use
   * @param shuffle           whether or not the data needs shuffling
   * @param results           annotations to compare against
   * @return                  overall recognition rate
   * @throws Exception        Thrown if K value is greater than the number of observations
   ********************************/
  public float[]  KFoldGivenAClassifier(int K, float[][] data, int[] targets, String chosenClassifier, String path, HashMap<String,String> para, boolean shuffle, Annotation[] results) throws Exception
  {
	  int length = data.length;
	  int dimension = data[0].length;
	  
	  if (shuffle)
	    shuffle(length, dimension, data, targets);
 
	  float[][] testingPatterns;
      float[][] trainingPatterns;
      int[] trainingTargets;
      int[] testingTargets;
      Annotation[] annotedPredictions;
      double[] prob;
      int testinglength, traininglength; //may vary for the last fold 
      int foldsize = length/K; //size per fold except the last one;

      if (K > length)
	  {
		  System.out.println("K-fold validation must have a K that is smaller than the total number of observations");
		  throw new Exception("K-fold validation must have a K that is smaller than the total number of observations");
      }

      int correct = 0;
      float[] foldresults = new float[K+1];
      for (int k = 0; k < K; k++)
      {
    		 
		 if (k == K-1)  //the last fold may have more testing samples than other folds.
			    testinglength = length/K + length %  K;
		 else
			    testinglength = length/K;

		  traininglength = length - testinglength;
		  System.out.println("foldsize:" + foldsize + " testing length:"+ testinglength+" training length:" + traininglength + " dimension:" + dimension);
	      testingPatterns = new float[testinglength][dimension];
	      trainingPatterns = new float[traininglength][dimension];
	      trainingTargets = new int[traininglength];
	      testingTargets = new int[testinglength];
	      annotedPredictions = new Annotation[testinglength];
	      for(int m = 0; m < testinglength; m++)
	    	  annotedPredictions[m] = new Annotation();
			 
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

    	 (new annotool.Annotator()).classifyGivenAMethod(chosenClassifier, path, para, trainingPatterns, testingPatterns, trainingTargets, testingTargets, annotedPredictions);
    	 
    	  //compare the output prediction with the real targets on the testing set
    	 int currentFoldCorrect = 0;
         for(int i=0; i<testinglength; i++)
         {
			System.out.println("predicted category:" + annotedPredictions[i].anno);
			System.out.println("actual category:" + testingTargets[i]);
			results[k*foldsize+i].anno = annotedPredictions[i].anno;
	        if(annotedPredictions[i].anno == testingTargets[i])
	        {
	           correct ++;
	           currentFoldCorrect ++;
	        }
	     }
         foldresults[k] = (float) currentFoldCorrect/testinglength;

      
   	   //if GUI, update 5 times unless K is small than 5
       if (bar != null)
       {
    		 if (K < 5) //update K times
    			    setProgress(startPos + (k+1)*totalRange/K);
    	     else if (k%(K/5) == 0)
    			    setProgress(startPos + totalRange/5*(k+1)/(K/5));
        }	 
      }//end of K

      //output the overall results of all folds
      System.out.println("overall recognition rate: " + (float)correct/length);
      foldresults[K] = (float) correct/length;
      
      setProgress(startPos + totalRange);
      return foldresults;
 	  
  }
  
  /*****************************
  * K fold CV with Classifier object, with K <= length. When K == length, it is leave one out (LOO) validation.
  * Shuffling is needed before calling this method if the samples are ordered sequentially based on categories.
  * 
  * It will split the data, and call the xxxGivenMethod in the Annotator class.
  * 
  * @param K            the number of the fold
  * @param data         two-dimensional array of the image features 
  * @param targets      array of targets for the data
  * @param classifier   classifier to be used
  * @param para         parameters for the classifier to use
  * @param shuffle      whether or not the data needs shuffling
  * @param results      annotations to compare against
  * @return             overall recognition rate
  * @throws Exception   Thrown if K value is greater than the number of observations
  ********************************/
  public float[]  KFoldGivenAClassifier(int K, float[][] data, int[] targets, Classifier chosenClassifier, HashMap<String,String> para, boolean shuffle, Annotation[] results) throws Exception
  {
	  int length = data.length;
	  int dimension = data[0].length;
	  
	  if (shuffle)
	    shuffle(length, dimension, data, targets);
 
	  float[][] testingPatterns;
      float[][] trainingPatterns;
      int[] trainingTargets;
      int[] testingTargets;
      Annotation[] annotedPredictions;
      double[] prob;
      int testinglength, traininglength; //may vary for the last fold 
      int foldsize = length/K; //size per fold except the last one;

      if (K > length)
	  {
		  System.out.println("K-fold validation must have a K that is smaller than the total number of observations");
		  throw new Exception("K-fold validation must have a K that is smaller than the total number of observations");
      }

      int correct = 0;
      float[] foldresults = new float[K+1];
      for (int k = 0; k < K; k++)
      {
    		 
		 if (k == K-1)  //the last fold may have more testing samples than other folds.
			    testinglength = length/K + length %  K;
		 else
			    testinglength = length/K;

		  traininglength = length - testinglength;
		  System.out.println("foldsize:" + foldsize + " testing length:"+ testinglength+" training length:" + traininglength + " dimension:" + dimension);
	      testingPatterns = new float[testinglength][dimension];
	      trainingPatterns = new float[traininglength][dimension];
	      trainingTargets = new int[traininglength];
	      testingTargets = new int[testinglength];
	      annotedPredictions = new Annotation[testinglength];
	      for(int m = 0; m < testinglength; m++)
	    	  annotedPredictions[m] = new Annotation();
			 
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

    	 (new annotool.Annotator()).classifyGivenAMethod(chosenClassifier, para, trainingPatterns, testingPatterns, trainingTargets, testingTargets, annotedPredictions);
    	 
    	  //compare the output prediction with the real targets on the testing set
    	 int currentFoldCorrect = 0;
         for(int i=0; i<testinglength; i++)
         {
			System.out.println("predicted category:" + annotedPredictions[i].anno);
			System.out.println("actual category:" + testingTargets[i]);
			results[k*foldsize+i].anno = annotedPredictions[i].anno;
	        if(annotedPredictions[i].anno == testingTargets[i])
	        {
	           correct ++;
	           currentFoldCorrect ++;
	        }
	     }
         foldresults[k] = (float) currentFoldCorrect/testinglength;

      
   	   //if GUI, update 5 times unless K is small than 5
       if (bar != null)
       {
    		 if (K < 5) //update K times
    			    setProgress(startPos + (k+1)*totalRange/K);
    	     else if (k%(K/5) == 0)
    			    setProgress(startPos + totalRange/5*(k+1)/(K/5));
        }	 
      }//end of K

      //output the overall results of all folds
      System.out.println("overall recognition rate: " + (float)correct/length);
      foldresults[K] = (float) correct/length;
      
      setProgress(startPos + totalRange);
      return foldresults;
 	  
  }
   
   /*****************************
   * Sends data to the classifier and returns the prediction results and recognition rate. It is called by classifyGivenAMethod() in Annotator
   * 
   * @param   trainingpatterns  Pattern data to train the algorithm
   * @param   testingpatterns   Pattern data to be classified
   * @param   trainingtargets   Targets for the training pattern
   * @param   testingtargets    Targets for the testing pattern
   * @param   classifier        classifier to be used
   * @param   annotations       storage array for predictions and probabilities
   * @return                    overall recognition rate
   * @throws  Exception         (Not used)
   ********************************/
   public float classify(float[][] trainingPatterns, float[][] testingPatterns,int[] trainingTargets, int[] testingTargets, Classifier classifier, Annotation[] annotations) throws Exception
   {
	   int testingLength = testingPatterns.length;
	   int[]	 predictions = new int[testingLength];
	   double[]	 prob = new double[testingLength];
			
       classifier.classify(trainingPatterns, trainingTargets, testingPatterns, predictions,  prob);

       //calculate the recognition rate
       int correct = 0;
       for(int i=0; i<testingLength; i++)
       {
			//System.out.println("predicted category:" + predictions[i]);
			//System.out.println("actual category:" + testingTargets[i]);
    	    annotations[i].anno = predictions[i];
            annotations[i].prob = prob[i];
	        if(predictions[i] == testingTargets[i])
	           correct ++;
	    }
      //output the overall results
    System.out.println("overall recognition rate: " + (float)correct/testingLength);
    return (float) correct/testingLength;
   }
   

   /*****************************
   * Sends data to the classifier and returns the prediction results and recognition rate. It is called by classifyGivenAMethod() in Annotator
   * overloaded version: no testing targets
   * 
   * @param   trainingpatterns  Pattern data to train the algorithm
   * @param   testingpatterns   Pattern data to be classified
   * @param   trainingtargets   Targets for the training pattern
   * @param   classifier        classifier to be used
   * @return                    predictions of the classifier
   * @throws  Exception         (Not used)
   ********************************/
   public int[] classify(float[][] trainingPatterns, float[][] testingPatterns, int[] trainingTargets, Classifier classifier) throws Exception
   {
 	  int testingLength = testingPatterns.length;
 	  int[]	 predictions = new int[testingLength];
 	  double[]	 prob = new double[testingLength];

 	  classifier.classify(trainingPatterns, trainingTargets, testingPatterns, predictions,  prob);
 	  return predictions;
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
	
	
	   /*****************************
	   * Sets up the parameters for the classifier, calls the classifier, and stores the returned annotations.
	   * 
	   * Not linked to GUI after June 2011.
	   * 
	   * @param data         two-dimensional array of the image features 
	   * @param targets      array of targets for the data
	   * @param classifier   classifier to be used
	   * @param annos        storage array for predictions and probabilities
	   * @param shuffle      (Not used) whether or not the data needs shuffling
	   * @throws Exception   (Not used)
	   ********************************/
	   public void LOO(float[][] data, int[] targets, Classifier classifier, Annotation annos[], boolean shuffle) throws Exception
	   {
		   //No need to shuffle in LOO 12/23/08
		   //if (shuffle)
		   //   shuffle(length, dimension, data, targets);

		   int length = data.length;
		   int dimension = data[0].length;
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
	         classifier.classify(trainingPatterns, trainingTargets, testingPatterns, prediction,  prob);
	         annos[k].anno = prediction[0];
	         annos[k].prob = prob[0];
		   }

	   }



}