package segmentation;

import java.util.ArrayList;

import ij.ImagePlus;

public class LarvaImage
{
	public int imageId = 0;
//	public int candidateCaseId = 0;
	public ImagePlus imageOriginal = null;
	public ImagePlus imageSegmented = null;
	public int larvaNum = 0;
	public ArrayList<CandidateCase> candidateCases = null;
//	ArrayList<Double> larvaFeatureData = null;

	public LarvaImage()
	{
		candidateCases = new ArrayList<CandidateCase>();
//		larvaFeatureData = new ArrayList<Double>();
	}
	
	public LarvaImage(int imageId, ImagePlus imageOriginal, int larvaNum)
	{
		this();
		this.imageId = imageId;
		this.imageOriginal = imageOriginal;
		this.larvaNum = larvaNum;
	}
	
	public void addCandidateCase(CandidateCase candidateCase)
	{
		candidateCases.add( candidateCase );
	}
	
	public static LarvaImage getLarvaImage9(ArrayList<LarvaImage> larvaImages, int imageId)
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
