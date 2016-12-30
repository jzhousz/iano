package segmentation;

import java.util.ArrayList;

public class CandidateCase
{
	public ArrayList<CandidateLarva> candidateLarvae = null;
	private int candidateCaseId = 0;
	
	// the exponent part of the density function, give a some large number
	public double probability1 = 999999999;
	// the natural number, e, part of the density function
	public double probability2 = 0;
		
	public CandidateCase(int candidateCaseId)
	{
		this.candidateCaseId = candidateCaseId;
		candidateLarvae = new ArrayList<CandidateLarva>();
	}
	
	public void addCandidateLarva(CandidateLarva candidateLarva)
	{
		candidateLarvae.add( candidateLarva );
	}
	
//	public static SegmentApproach getSegmentApproachs(ArrayList<LarvaImage> larvaImages, int imageId)
//	{
////		System.out.println("======= finding imageId:" + imageId + "======");
//		
//		for(int i = 0; i < larvaImages.size(); i++)
//		{
//			LarvaImage larvaImage = larvaImages.get(i);
//			
//			for(int j = 0; j < larvaImage.segmentApproaches.size(); j++)
//			{
//				SegmentApproach segmentApproach = larvaImage.segmentApproaches.get(j);
//				
//				for(int k = 0; k < segmentApproach.larvaSegments.size(); k++)
//				{
//					LarvaSegment larvaSegment = segmentApproach.larvaSegments.get(k);
//					
//					if(larvaSegment.segmentId == imageId)
//					{
//						
//					}
//				}
//			}
//		}
//	}
	
	public static void main(String[] args)
	{

	}

	public int getCandidateCaseId()
	{
		return candidateCaseId;
	}

	public void setCandidateCaseId(int candidateCaseId)
	{
		this.candidateCaseId = candidateCaseId;
	}

}
