package annotool.ensemble;

import java.util.ArrayList;
import java.util.HashMap;

import annotool.Annotation;

/**
   Committee machine based ensemble
*/
public class ComEnsemble implements Ensemble {
private ArrayList<Annotation[]> annoResults;

/*****************************
 * 
 * Constructor, Used to make a new Arraylist of Annotation[]
 *
********************************/
	public ComEnsemble()
	{
		annoResults = new ArrayList<Annotation[]>();
	}
	   /*****************************
	   * Used to add results of classifiers into the Ensemble, Should be called before classfiy
	   * 
	   * 
	   * @param annotedPredictions    Array of predictions from the classifier
	   * 
	   ********************************/
	public void addResult(Annotation[] annotedPredictions)
	{
		annoResults.add(new Annotation[annotedPredictions.length]);
		
		for( int i = 0; i < annotedPredictions.length; i++)
		{
			annoResults.get(annoResults.size() - 1)[i] = new Annotation();
			annoResults.get(annoResults.size() - 1)[i].anno = annotedPredictions[i].anno;
			annoResults.get(annoResults.size() - 1)[i].prob = annotedPredictions[i].prob;
		}
		
	}
	   /*****************************
	   * Used to combine all the results from the classifers, and with a 
	   * commite votes come make a new array of predictions
	   * 
	   * 
	   * @param annotedPredictions    Array of predictions from the classifier
	   * 
	   * @return finalResults         Array of predictions based on the input of
	   * 							  all the classifiers            
	   ********************************/
	public int[] classify() 
	{
		
		System.out.println("Ensemble Classifying ...");
		
		//committee machine
		int[] finalResults =  new int[annoResults.get(0).length];
		if( annoResults.size() == 1 ) //just one classifier
		{
			System.out.println("Classifier Array size 1");
			
			for( int i = 0; i < finalResults.length; i++)
			{
				finalResults[i] =  annoResults.get(0)[i].anno;
			}
			
			return finalResults;
		}
		
		if( annoResults.size() % 2 == 0)
		{
			System.out.println("Even Number of classifiers, if there is a tie, results will be picked using the one with higher probability or randomly if probability is missing");
		}
		
		for( int i = 1;  i < annoResults.size(); i++)
		{
			if(annoResults.get(0).length != annoResults.get(i).length)
			{
				System.out.println("ERROR: Results array is not the same size");
				return null;
			}
		}
		
		//more than one classifier case
		HashMap<Integer, Integer> votes = new HashMap<Integer, Integer>();  //key: label; value: number of votes
		for( int i = 0; i < finalResults.length; i++)  //loop through each testing sample
		{
		   votes.clear();
		   for (int j = 0; j <   annoResults.size(); j++ ) // loop through each classifier
		   {
		      //if the label is already in the map, increment the value by .
			  System.out.print(annoResults.get(j)[i].anno + " " + (int) (annoResults.get(j)[i].prob * 100 )+ "%" + " ");
			   
			  Integer previousVoteCount = null;
			  if ((previousVoteCount = votes.get(annoResults.get(j)[i].anno))!= null) 
			  {
			
				  	votes.put(annoResults.get(j)[i].anno, previousVoteCount.intValue() + 1);
			  }
			  else
			  {
			     //else put the label in the map, with value 1.
				  votes.put(annoResults.get(j)[i].anno, 1);
			  }
		   }
		   //go through the map, to find the majority vote
		   int maxVote = -1;
		   int maxLabel = -1;
		   for(Integer label: votes.keySet())
		   {
		       if (votes.get(label) > maxVote)
               {
			      maxVote = votes.get(label);
				  maxLabel = label;
               }
		      
		   }
		   
		   System.out.print("| " + maxLabel + "\n");
		   
		   //still need to take care of ties ....
		   finalResults[i] = maxLabel;  
		} // end if i loop
		
		return finalResults;
	}

}
