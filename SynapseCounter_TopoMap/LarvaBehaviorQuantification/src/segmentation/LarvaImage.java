package segmentation;

import java.util.ArrayList;

public class LarvaImage
{
	public int imageId = 0;
	public int larvaNum = 0;
	public ArrayList<SegmentApproach> segmentApproaches = null;

	public LarvaImage()
	{
		segmentApproaches = new ArrayList<SegmentApproach>();
	}
	
	public void addLarvaSegmentApproach(SegmentApproach segmentApproach)
	{
		segmentApproaches.add( segmentApproach );
	}
	
	public static LarvaImage getLarvaImage(ArrayList<LarvaImage> larvaImages, int imageId)
	{
		for(LarvaImage larvaImage : larvaImages)
		{
			if(larvaImage.imageId == imageId)
				return larvaImage;
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{

	}

}
