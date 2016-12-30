package segmentation;

import java.util.ArrayList;

import ij.ImagePlus;

public class CandidateLarva
{
	public int candidateLarvaeId = 0;
	public String fileName;

	// the exponent part of the density function, give a some large number
	public double probability1 = 999999999;
	// the natural number, e, part of the density function
	public double probability2 = 0;
	public ImagePlus imagePlus = null;
	public ArrayList<Double> larvaFeatureData = null;
	
	public static void setCandidateLarva(ArrayList<LarvaImage> larvaImages, int imageId
			, double probability1, double probability2)
	{
		for(int i = 0; i < larvaImages.size(); i++)
		{
			LarvaImage larvaImage = larvaImages.get(i);
			
			for(int j = 0; j < larvaImage.candidateCases.size(); j++)
			{
				CandidateCase segmentApproach = larvaImage.candidateCases.get(j);
				
				for(int k = 0; k < segmentApproach.candidateLarvae.size(); k++)
				{
					CandidateLarva candidateLarva = segmentApproach.candidateLarvae.get(k);
					
					if(candidateLarva.candidateLarvaeId == imageId)
					{
						candidateLarva.probability1 = probability1;
						candidateLarva.probability2 = probability2;
					}
				}
			}
		}
	}
	
	public static CandidateLarva getCandidateLarva(ArrayList<LarvaImage> larvaImages, int imageId
			, double probability1, double probability2)
	{
//		System.out.println("======= finding imageId:" + imageId + "======");
		
		for(int i = 0; i < larvaImages.size(); i++)
		{
			LarvaImage larvaImage = larvaImages.get(i);
			
			for(int j = 0; j < larvaImage.candidateCases.size(); j++)
			{
				CandidateCase candidateCase = larvaImage.candidateCases.get(j);
				
				for(int k = 0; k < candidateCase.candidateLarvae.size(); k++)
				{
					CandidateLarva candidateLarva = candidateCase.candidateLarvae.get(k);
					
					if(candidateLarva.candidateLarvaeId == imageId)
					{
						return candidateLarva;
					}
				}
			}
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{

	}

}
