package segmentation;

import java.util.ArrayList;

public class LarvaSegment
{
	public int segmentId = 0;
	public String fileName;
	public double probability1 = 0;
	public double probability2 = 0;
	
	public static void setLarvaSegment(ArrayList<LarvaImage> larvaImages, int imageId
			, double probability1, double probability2)
	{
		System.out.println("======= finding imageId:" + imageId + "======");
		
		for(int i = 0; i < larvaImages.size(); i++)
		{
			LarvaImage larvaImage = larvaImages.get(i);
			
			for(int j = 0; j < larvaImage.segmentApproaches.size(); j++)
			{
				SegmentApproach segmentApproach = larvaImage.segmentApproaches.get(j);
				
				for(int k = 0; k < segmentApproach.larvaSegments.size(); k++)
				{
					LarvaSegment larvaSegment = segmentApproach.larvaSegments.get(k);
					
					if(larvaSegment.segmentId == imageId)
					{
						larvaSegment.probability1 = probability1;
						larvaSegment.probability2 = probability2;
					}
				}
			}
		}
	}
	
	public static LarvaSegment getLarvaSegment(ArrayList<LarvaImage> larvaImages, int imageId
			, double probability1, double probability2)
	{
		System.out.println("======= finding imageId:" + imageId + "======");
		
		for(int i = 0; i < larvaImages.size(); i++)
		{
			LarvaImage larvaImage = larvaImages.get(i);
			
			for(int j = 0; j < larvaImage.segmentApproaches.size(); j++)
			{
				SegmentApproach segmentApproach = larvaImage.segmentApproaches.get(j);
				
				for(int k = 0; k < segmentApproach.larvaSegments.size(); k++)
				{
					LarvaSegment larvaSegment = segmentApproach.larvaSegments.get(k);
					
					if(larvaSegment.segmentId == imageId)
					{
						return larvaSegment;
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
