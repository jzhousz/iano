package annotool;

import annotool.classify.SVMClassifier;

public class Util {
	
	public static float[][] discretize(float [][] features, int[] targets, int[] targetList, float[][] means)
	{
		   //Integer[] list = getTargetList(targets);
		   int dimension = features[0].length;
		   int length = features.length;
		   
	       for(int k = 0; k < targetList.length; k++)
	       {	   
			float sum = 0, mean = 0;
	        for (int j=0; j< dimension; j++)
	        {
	  		  sum =0;
	          for (int i =0; i<length; i++)
				   sum += features[i][j];
		      mean =  sum/ (float) length;  //without (float), the dicretization is based on 0?
		      means[k][j] = mean; //set the return array
		      
	          for (int i =0; i<length; i++)
	          {
	            if(targets[i] == targetList[k])
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
          return features;
	}
	
	
	public static void discretizeCombined(float[][] fea1, float[][] fea2, int[] targets1, int[] targets2)
	{
		   int dimension = fea1[0].length;
		   int length1 = fea1.length;
		   int length2 = fea2.length;
		   int[] targetList = getTargetList(targets1);
		   
		   
	       for(int k = 0; k < targetList.length; k++)
	       {	   
			float sum = 0, mean = 0;
	        for (int j=0; j< dimension; j++)
	        {
	  		  sum =0;
	          for (int i =0; i<length1; i++)
				   sum += fea1[i][j];
	          for (int i =0; i<length2; i++)
				   sum += fea2[i][j];

	          mean = sum/(length1+length2);

	          for (int i =0; i<length1; i++)
	          {
	            if(targets1[i] == targetList[k])
	            {	
	        	  if(fea1[i][j] > mean)
				     fea1[i][j] = 1;
				   else if(fea1[i][j] < mean)
				     fea1[i][j] = -1;
				   else
				       fea1[i][j] = 0;
	            }   
		     }

	          for (int i =0; i<length2; i++)
	          {
	            if(targets2[i] == targetList[k])
	            {	
	        	  if(fea2[i][j] > mean)
				     fea2[i][j] = 1;
				   else if(fea2[i][j] < mean)
				     fea2[i][j] = -1;
				   else
				       fea2[i][j] = 0;
	            }   
		     }

	        
	        }
	      } 
	}
	
	public static void discretizeCombinedUnsupervised(float[][] fea1, float[][] fea2, int[] targets1, int[] targets2)
	{
		   int dimension = fea1[0].length;
		   int length1 = fea1.length;
		   int length2 = fea2.length;
		  // int[] targetList = getTargetList(targets1);
		   
		   
	       //for(int k = 0; k < targetList.length; k++)
	       //{	   
			float sum = 0, mean = 0;
	        for (int j=0; j< dimension; j++)
	        {
	  		  sum =0;
	          for (int i =0; i<length1; i++)
				   sum += fea1[i][j];
	          for (int i =0; i<length2; i++)
				   sum += fea2[i][j];

	          mean = sum/(length1+length2);

	          for (int i =0; i<length1; i++)
	          {
	           // if(targets1[i] == targetList[k])
	           // {	
	        	  if(fea1[i][j] > mean)
				     fea1[i][j] = 1;
				   else if(fea1[i][j] < mean)
				     fea1[i][j] = -1;
				   else
				       fea1[i][j] = 0;
	            //}   
		     }

	          for (int i =0; i<length2; i++)
	          {
	          //  if(targets2[i] == targetList[k])
	            //{	
	        	  if(fea2[i][j] > mean)
				     fea2[i][j] = 1;
				   else if(fea2[i][j] < mean)
				     fea2[i][j] = -1;
				   else
				       fea2[i][j] = 0;
	           // }   
		     }

	        
	        //}
	      } 
	}

	
	public static float[][] discretizeGivenFilter(float[][] means, float[][] features, int[] targets, int[] targetList)
	{
		   int dimension = features[0].length;
		   int length = features.length;
	
	       for(int k = 0; k < targetList.length; k++)
	       {	   
	        for (int j=0; j< dimension; j++)
	        {
      	      for (int i =0; i<length; i++)
	          {
	            if(targets[i] == targetList[k])
	            {	
	        	  if(features[i][j] > means[k][j])
				     features[i][j] = 1;
				   else if(features[i][j] < means[k][j])
				     features[i][j] = -1;
				   else
				       features[i][j] = 0;
	            }   
		     }
		   }
	      } 
          return features;
	}

    //The method is not used.	
	public static float[][] discretizeGivenFilterUnsupervised(float[][] means, float[][] features, int[] targets)
	{
		   System.out.println("in dicretize");
		   int dimension = features[0].length;
		   int length = features.length;
	
		   //for each image, find out which target group it is closest in training
           for (int i =0; i<length; i++) //each image
	       {
 		    int targetGuess = -1;   
	        float mindifference = 0;	
 		    for(int k = 0; k < means.length; k++) //each group
	        {
 		       float difference = 0;	
	    	   for (int j=0; j< 300; j++) //50 is the dimension if size that correspond to group mean
	    	   {   
		    	  // System.out.println(i+":cur dif:"+(features[i][j]-means[k][j])+" -- updated dif:"+difference);
	    		   difference += Math.abs(features[i][j]-means[k][j]);
	    	   }
	    	   System.out.println("final difference for image"+i+":"+difference);

	    		   if (targetGuess == -1) 
	    	   { //first group
	    		   mindifference = difference;
	    		   targetGuess = 0;
	    	   }else if (difference < mindifference)
	    	   {
	    		   mindifference = difference;
	    		   targetGuess = k;
	    	   }
	        }	  
      	    	  
 		    System.out.println("targetGuess:"+targetGuess+" actual (index):"+(targets[i]-1));
      	    //use targetGuess for image i
	        for (int j=0; j< dimension; j++)
	        {
 	        	  if(features[i][j] > means[targetGuess][j])
				     features[i][j] = 1;
				   else if(features[i][j] < means[targetGuess][j])
				     features[i][j] = -1;
				   else
				       features[i][j] = 0;
	               
		     }
 	      } //end for each image

		   System.out.println("out dicretize");

           return features;
	}

	
	//expensive: use continuous training feature again!
	public static float[][] discretizeGivenFilterWrapper(float[][] means,float[][] testingFeatures, float[][] trainingFeatures, int[] trainingtargets, int numoffeatures)
	{
		//Use a wrapper classifier to guess the possible target
		//not final answer, just for discretization.
		annotool.classify.Classifier wrapper= new annotool.classify.SVMClassifier(numoffeatures, "-t 0");
		int[] predictions = new int[testingFeatures.length];
		double[] prob = new double[testingFeatures.length];

		try  {
		wrapper.classify(trainingFeatures, trainingtargets, testingFeatures, predictions, prob);
		}catch(Exception e)
		{ System.err.println("Problem in discretizing:"+e.getMessage());}
		
		//look for index!!
		int[] targetList = getTargetList(trainingtargets);

		for(int i = 0; i < testingFeatures.length; i++)
		{
			int targetGuess = predictions[i] -1; 
			for(int index = 0; index < targetList.length; index++)
			{
				if(targetList[index] == predictions[i])
				{
					targetGuess = index;
				    break;
			   }
			}
			for(int j =0; j < testingFeatures[0].length; j++)
			{
				if(testingFeatures[i][j] > means[targetGuess][j])
					     testingFeatures[i][j] = 1;
					   else if(testingFeatures[i][j] < means[targetGuess][j])
					     testingFeatures[i][j] = -1;
					   else
					       testingFeatures[i][j] = 0;
		               
			     }
		}
		
		return testingFeatures;
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static int[] getTargetList(int[] targets)
    {
		java.util.ArrayList<Integer> targetList = new java.util.ArrayList<Integer>();
		for (int i=0; i < targets.length; i++)
			if(!targetList.contains(targets[i]))
				targetList.add(targets[i]);
		
		int res[] = new int[targetList.size()];
		for(int i = 0; i < targetList.size(); i++)
			res[i] = ((Integer)targetList.get(i)).intValue();
		
		return res;
    }

	//When called by raw image list, this zscore standardize each image
	public static void zscoreAlongLenth(float[][] features, int length, int dimension)
	{  //use double for better precision
		int i, j;
		double cursum, curmean, curstd;
		for(i = 0; i < length; i++)
		{
			//standardize each image
			cursum = 0;
		    curmean = 0;
		    curstd = 0;
		    for (j = 0; j < dimension; j++)
		      cursum += features[i][j];
			curmean = cursum / dimension;
			cursum = 0;
			double tmpf;
			for (j = 0; j < dimension; j++)
			{
			    tmpf = features[i][j] - curmean;
			    cursum += tmpf * tmpf;
			}
			curstd = (dimension == 1) ? 0 : Math.sqrt (cursum / (dimension - 1));	//length -1 is an unbiased version for Gaussian
			for (j = 0; j < dimension; j++)
			  features[i][j] = new Double((features[i][j] - curmean) / curstd).floatValue();
		}	  
	}

	//ZScore a raw image
	public static void zscoreAnImage(float[] features)
	{  //use double for better precision
		int j;
		int dimension = features.length;
		double cursum, curmean, curstd;

		//standardize an image
		cursum = 0;
		curmean = 0;
		curstd = 0;
		for (j = 0; j < dimension; j++)
		      cursum += features[j];
		curmean = cursum / dimension;
		cursum = 0;
		double tmpf;
		for (j = 0; j < dimension; j++)
		{
		    tmpf = features[j] - curmean;
			cursum += tmpf * tmpf;
		}
		curstd = (dimension == 1) ? 0 : Math.sqrt (cursum / (dimension - 1));	//an unbiased version for Gaussian
		for (j = 0; j < dimension; j++)
		{
		  features[j] = new Double((features[j] - curmean) / curstd).floatValue();
		  //System.out.println(features[j]);
		}
 
	}
	
	//03312010: scale globally from [0, 255] to [-1,+1]
	// instead of score each image
	public static void scaleAnImage(float[] features)
	{
	   //System.out.println("scaling ...");	
	   for(int i=0; i<features.length; i++)
	   {
		features[i] = (features[i]-127)/127;
		//System.out.println(features[i]);
	   }
	   
	}
	
}
