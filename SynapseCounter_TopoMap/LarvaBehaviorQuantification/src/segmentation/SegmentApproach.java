package segmentation;

import java.util.ArrayList;

public class SegmentApproach
{
	public ArrayList<LarvaSegment> larvaSegments = null;

	public SegmentApproach()
	{
		larvaSegments = new ArrayList<LarvaSegment>();
	}
	
	public void addLarvaSegment(LarvaSegment larvaSegment)
	{
		larvaSegments.add( larvaSegment );
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

}
