package segmentation;

import java.util.ArrayList;

import ij.ImagePlus;

/**
 * The class used to encapsulate the larva image.
 * 
 * @author Yaoguang Zhong
 * @version 1.1
 * @since 01-21-2017
 */
public class LarvaImage
{
	public int imageId = 0;
	public ImagePlus imageOriginal = null;
	public ImagePlus imageSegmented = null;
	public int larvaNum = 0;
	public ArrayList<CandidateCase> candidateCases = null;

	/** 
	 * Constructor.
	 */
	public LarvaImage()
	{
		candidateCases = new ArrayList<CandidateCase>();
	}
	
	/**
	 * Constructor.
	 * @param imageId The image id.
	 * @param imageOriginal The original image.
	 * @param larvaNum The larva id.
	 */
	public LarvaImage(int imageId, ImagePlus imageOriginal, int larvaNum)
	{
		this();
		this.imageId = imageId;
		this.imageOriginal = imageOriginal;
		this.larvaNum = larvaNum;
	}
	
	/**
	 * Add a candidate case.
	 * @param candidateCase The candidate case.
	 */
	public void addCandidateCase(CandidateCase candidateCase)
	{
		candidateCases.add( candidateCase );
	}
	
//	public static LarvaImage getLarvaImage7(ArrayList<LarvaImage> larvaImages, int imageId)
//	{
//		for(LarvaImage larvaImage : larvaImages)
//		{
//			if(larvaImage.imageId == imageId)
//				return larvaImage;
//		}
//		
//		return null;
//	}

}
