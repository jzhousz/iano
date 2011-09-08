package annotool.extract;

import annotool.ImgDimension;
import annotool.io.DataInput;

import java.util.ArrayList;


/**
 *  3 directions (x, y, z)  3D wavelet features
 * 
 *  Total extracted features: width*height + width*stackSize + height*stackSize
 *  
 *  revised 09/03/11 to process one image at a time.
 */
public class StackThreeDirectionHaarFeatureExtractor implements  FeatureExtractor {

	protected float[][] features = null;
	//when pass a problem to get an image at a time
	annotool.io.DataInput problem = null; 
	//when pass the data in directly (e.g. for ROI). If pass one image at a time due to memory reason, then the ArrayList has a size of 1.
	ArrayList all3DData = null; 

	int length;
	int stackSize;
	int level;
	ImgDimension dim = new ImgDimension();
	int imageType;
    boolean weighted = false; //default: all images are weighted 1
    
	public final static String LEVEL_KEY = "Wavelet Level";
	public final static String WEIGHT_KEY = "WEIGHTED";
	  
	
	public StackThreeDirectionHaarFeatureExtractor()
	{	}
	
	public void setParameters(java.util.HashMap<String, String> parameters)
	{
	    if (parameters != null && parameters.containsKey(LEVEL_KEY))
	 		     level = Integer.parseInt(parameters.get(LEVEL_KEY));
	    if (parameters != null && parameters.containsKey(WEIGHT_KEY))
		     weighted = Boolean.parseBoolean(parameters.get(WEIGHT_KEY));

	}
	
    public float[][] calcFeatures(annotool.io.DataInput problem) throws Exception
    {
		this.problem = problem;
		this.length = problem.getLength();
		this.dim.width  =  problem.getWidth();
		this.dim.height  = problem.getHeight();
		this.imageType = problem.getImageType();
		this.stackSize  = problem.getStackSize();
		
		return calcFeatures();
    }
    
	public float[][] calcFeatures(ArrayList  all3DData, int imageType, ImgDimension dim) throws Exception
	{
		this.length = all3DData.size();
		this.dim = dim;
		this.imageType = imageType;
		this.all3DData = all3DData;
		this.stackSize = ((ArrayList)all3DData.get(0)).size();
		
		return calcFeatures();
	}

	
	protected float[][] calcFeatures() throws Exception {
		
		//number of extracted features
		int totaldim = dim.width*dim.height + dim.width*stackSize + dim.height*stackSize;
		features  = new float[length][totaldim];
		
		HaarFeatureExtractor haar = new HaarFeatureExtractor();
		java.util.HashMap<String, String> para = new java.util.HashMap<String, String>();
		para.put(HaarFeatureExtractor.LEVEL_KEY, String.valueOf(level));
		haar.setParameters(para);
		
		//feature storage, shared by all images in the set
	    float[] features4OneXYImage = new float[dim.width*dim.height];
		float[] features4OneXZImage = new float[dim.width*stackSize];
		float[] features4OneYZImage = new float[dim.height*stackSize];

		//temp storage for an image data at XZ or YZ direction
		//different type is needed for different kind of images.
		byte[] bdata4OneXZImage = null,bdata4OneYZImage = null;
		int[] idata4OneXZImage = null,idata4OneYZImage = null;
		float[] fdata4OneXZImage = null,fdata4OneYZImage = null;
		if(imageType == DataInput.GRAY8 || imageType == DataInput.COLOR_RGB )
		{
			bdata4OneXZImage = new byte[dim.width*stackSize];
			bdata4OneYZImage = new byte[dim.height*stackSize];
		}
		else if (imageType == DataInput.GRAY16)
		{
			idata4OneXZImage = new int[dim.width*stackSize];
			idata4OneYZImage = new int[dim.height*stackSize];
		}else if (imageType == DataInput.GRAY32)
		{
		    fdata4OneXZImage = new float[dim.width*stackSize];
		    fdata4OneYZImage = new float[dim.height*stackSize];
		}else
			throw new Exception("not supported image type");
		
		float weight =1;
		for(int imgIndex=0; imgIndex<length; imgIndex++)
		{
			//get one image at a time to save memory!
			ArrayList currentImage = null;
			if (problem != null)
			  currentImage = problem.getAllStacksOfOneImage(imgIndex);
	        else if (all3DData != null)
	          currentImage = (ArrayList)all3DData.get(imgIndex);
			
			//xy plane:
			for(int stackIndex = 0; stackIndex < stackSize; stackIndex ++)
			{
				Object oneImageData = currentImage.get(stackIndex);
		        haar.getHaarFeatureOfOneImage(oneImageData, features4OneXYImage);
		        weight = getWeightForImage(stackIndex, stackSize); 
		        for(int j=0; j<dim.width*dim.height; j++)
		        	features[imgIndex][j] += weight*features4OneXYImage[j];
			}
			//xz plane:
			for(int yIndex = 0; yIndex < dim.height; yIndex ++)
			{
				//build the one image data based on the corresponding dataType...
				if(imageType == DataInput.GRAY8 || imageType == DataInput.COLOR_RGB )
				{	
				    for(int m = 0; m< dim.width; m++)
				      for(int n = 0; n<stackSize; n++)
				        bdata4OneXZImage[n*dim.width+m]= ((byte[])currentImage.get(n))[yIndex*dim.width+m];
		            haar.getHaarFeatureOfOneImage(bdata4OneXZImage, features4OneXZImage);
				}
				else if(imageType == DataInput.GRAY32)
				{
					 for(int m = 0; m< dim.width; m++)
						  for(int n = 0; n<stackSize; n++)
						    idata4OneXZImage[n*dim.width+m]= ((int[])currentImage.get(n))[yIndex*dim.width+m];
				     haar.getHaarFeatureOfOneImage(idata4OneXZImage, features4OneXZImage);
				}else
				{
					 for(int m = 0; m< dim.width; m++)
						  for(int n = 0; n<stackSize; n++)
						    fdata4OneXZImage[n*dim.width+m]= ((float[])currentImage.get(n))[yIndex*dim.width+m];
			        haar.getHaarFeatureOfOneImage(fdata4OneXZImage, features4OneXZImage);
				}
		        weight = getWeightForImage(yIndex, dim.height); 
		        //add all yindex together
		        for(int j=0; j<dim.width*stackSize; j++)
		        	features[imgIndex][j+dim.width*dim.height] += weight*features4OneXZImage[j];
			  }	
			  //yz plane:
			  for(int xIndex =0; xIndex < dim.width; xIndex ++)
			  {
					if(imageType == DataInput.GRAY8 || imageType == DataInput.COLOR_RGB )
					{	
					    for(int m = 0; m< dim.height; m++)
					      for(int n = 0; n<stackSize; n++)
					        bdata4OneYZImage[n*dim.height+m]= ((byte[])currentImage.get(n))[m*dim.width+xIndex];
			            haar.getHaarFeatureOfOneImage(bdata4OneYZImage, features4OneYZImage);
					}
					else if(imageType == DataInput.GRAY16)
					{	
					    for(int m = 0; m< dim.height; m++)
					      for(int n = 0; n<stackSize; n++)
					        idata4OneYZImage[n*dim.height+m]= ((int[])currentImage.get(n))[m*dim.width+xIndex];
			            haar.getHaarFeatureOfOneImage(idata4OneYZImage, features4OneYZImage);
					}else
					{
					    for(int m = 0; m< dim.height; m++)
						      for(int n = 0; n<stackSize; n++)
						        fdata4OneYZImage[n*dim.height+m]= ((float[])currentImage.get(n))[m*dim.width+xIndex];
				        haar.getHaarFeatureOfOneImage(fdata4OneYZImage, features4OneYZImage);
					}
			        weight = getWeightForImage(xIndex, dim.width); 
			        //add all xindex together
			        for(int j=0; j<dim.height*stackSize; j++)
			        	features[imgIndex][j+dim.width*dim.height+dim.width*stackSize] += weight*features4OneYZImage[j];
			  }//End OF yz plane
			}//end of all images in the set
			
			return features;
		
		
        /*		
		//get into 3D (4D with length) data format (temporary storage)
		//not a scalable solution for large 3D sets   09/02/2011
		//example: 1024*1024*128; 20 image: --> 2G just for this array
		//Need to revisit!!
		
		//byte[][][][] ThreeDData = new byte[length][width][height][stackSize];

		//////////////////////////////
		for(int stackIndex = 1; stackIndex <= stackSize; stackIndex ++)
		{
			//length * (width*height)
			ArrayList currentImage = problem.getAllStacksOfOneImage(imageindex);
			byte[][] currentStack = problem.getData(stackIndex);
			for(int i=0; i<length; i++)
				for(int j=0; j<height; j++)
					for(int k=0; k<width; k++)
						ThreeDData[i][j][k][stackIndex-1]=currentStack[i][j*width+k];
		}

		//fill features at xy planes
		byte[][] data; //data at current plane of all images
		float[][] currentFeatures; //features at current plane of all images 
		for(int stackIndex = 1; stackIndex <= stackSize; stackIndex ++)
		{
			data = problem.getData(stackIndex); 
			currentFeatures = (new HaarFeatureExtractor(level, data, length, width, height)).getFeatures();
			//add and fill back into features
			addFeatures(features,currentFeatures, length, width*height, 1, 0);
		}

		//fill features at xz planes
		for(int yIndex = 0; yIndex < height; yIndex++)
		{
			//get that xz plane
			data = new byte[length][width*stackSize];
			for(int i=0; i<length; i++)
				for(int j=0; j<stackSize; j++)
					for(int k=0; k<width; k++)
						data[i][j*width+k] = ThreeDData[i][k][yIndex][j];

			currentFeatures = (new HaarFeatureExtractor(level, data, length, width, stackSize)).getFeatures();
			addFeatures(features,currentFeatures, length, width*stackSize, 1, width*height);
		}

		//fill features at yz planes
		for(int xIndex = 0; xIndex < width; xIndex++)
		{
			//get that yz plane
			data = new byte[length][height*stackSize];
			for(int i=0; i<length; i++)
				for(int j=0; j<stackSize; j++)
					for(int k=0; k<height; k++)
						data[i][j*height+k] = ThreeDData[i][xIndex][k][j];

			currentFeatures = (new HaarFeatureExtractor(level, data, length, height, stackSize)).getFeatures();
			addFeatures(features,currentFeatures, length, height*stackSize, 1, width*height+width*stackSize);
		}

		ThreeDData = null;

		return features;
		*/
	}

    private float getWeightForImage(int currentIndex, int totalSize)
    {
    	float weight = 1;
    	
    	if (weighted)
    	{
    	  int mid = totalSize/2;
		  //consider weighting: middle stacks are weighted more
		  // N(stackSize/2, 1.0) or linear
		  if (currentIndex < mid/2 || currentIndex > mid*1.5) weight = 0;
		  else  if (currentIndex <= mid)
			weight = (float) currentIndex/mid;
		  else
			weight = (float) (totalSize - currentIndex)/mid;
    	}
		return weight;
    }

	public boolean is3DExtractor()
	{  return true;} 

}
