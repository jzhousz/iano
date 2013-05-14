package annotool.extract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;

import annotool.ImgDimension;
import annotool.io.DataInput;

/*
 * Statistics regarding objects in the image
 * 
 * 1. The number of objects
 * 2. The average number of above-threshold pixel per object
 * 3. The variance of the number of above-threshold pixels per object
 * 4. The ratio of the size of the largest object to the smallest.
 * 5. The average object distance to the CenterOfMass (CoM)
 * 6. The variance of the object distance from the image CoM.
 * 7. The ratio of the largest to the smallest object to image CoM.
 * 
 * Other: Euler # 
 * Ref: "A neural network classifier capable of recognizing the patterns of all major subcellular
 *    structures in fluorescence microscope images of HeLa cells",  M.V. Boland and R. F. Murphy
 *    Bioinfomratics, 2001.
 *
 *  ---Changelog -Jon Sanders---
 *
 *     3/19/13 - initial adaptation from 2D implementation started.
 *             - move threshold calculation and getData calls to individual methods 
 *               outside calcFeatures, getThreshold() & getValue3D()
 *             - rudimentary getValue3D() just gets a stack slice on demand, todo improve.
 *             
 *     3/22/13 - added Z-dimension to getObjectsInImage() and getCofM()
 *             - changes calls to functions that call getValue3D to pass imageNum
 *			     instead of the actual data.
 *   		   - added z direction to CofM calls. 
 *
 *     4/2/13  - Reworked logic in getObjectsInImage() to combine data structure for binarizing
 *               tagging pixels.
 *     4/5/13  - Changed tag[] data type to short, reworked all method calls
 *     4/9/13  - Fixed offset method to properly function in z direction. adjusted method calls to new 
 *               param lists for getCofM, etc.
 *     4/16/13 - Added z dim to distance calculations
 *             - fix /0 errors.
 *     4/25/13 - Debug use as 2D extractor, fixed off by one slice read error
 *             - corrected for off by 1 calculations in getCoM() functions
 *     5/10/13 - Discovered "black/white background" bug.  Logic for object identification
 *               is grounded in the background intensity, if mixed background images are used,  
 *               then bg pixels are interpreted as one object.	Flipping threshold comparison during 
 *               Binarization can select for light/dark bg color.
 *     5/14/13 - 
 *			 
 */
public class ObjectStatistics3D implements FeatureExtractor {

	protected float[][] features = null;
	int length;
	int imageType;   
	int stackSize;
	DataInput problem = null;
	ArrayList allData = null;
	int mask = 0xff; 
	int longmask = 0xffff;
	int width, height, depth; //if the images are of the same size
	final int NUMBER_OF_FEATURES = 7;

	@Override
	public void setParameters(HashMap<String, String> para) {
		// 

	}

	@Override
	public float[][] calcFeatures(DataInput problem) throws Exception {
		this.problem = problem;
		this.length = problem.getLength();
		if (problem.ofSameSize() != false)
		{
			this.width   = problem.getWidth();
			this.height  = problem.getHeight();
			this.depth   = problem.getDepth();
		}
		this.imageType = problem.getImageType();
		//check imageType .. todo

		features = new float[length][NUMBER_OF_FEATURES];

		return calcFeatures();
	}

	@Override
	public float[][] calcFeatures(ArrayList data, int imageType,
			ImgDimension dim) throws Exception {
		// To be done.
		this.allData   = data;
		this.length    = data.size();
		this.imageType = imageType;
		this.width     = dim.width;
		this.height    = dim.height;
        this.depth     = dim.depth;
		
		return calcFeatures();
	}

	@Override
	public boolean is3DExtractor() {
		return true;
	}

	//calculate and fill the result array
	private float[][] calcFeatures() throws Exception
	{
		Object data;
		short[] tag;  //tag of objects for each pixel
		HashMap<Integer, Integer> tagCount;  //the size in # of pixels for each object 
		java.util.Set<Integer> keySet;
		ij.ImagePlus iplus;

		for (int image_num = 0; image_num < this.length; image_num++) {
			if (image_num % 50 == 0) System.out.println("Processing ");
			System.out.println("Image " + image_num +  " ---------");

			//get dimension for current image
			if (problem !=null)
			{
				if (!problem.ofSameSize())
				{  	//set the size for this image
					this.height = problem.getHeightList()[image_num];
					this.width = problem.getWidthList()[image_num];
					this.depth = problem.getDepthList()[image_num];
				}
			}
			//get data
			if (problem !=null)
				data = problem.getData(image_num,1);
			else  //alldata is not null
				data = allData.get(image_num);

			//get binarize threshold. More efficient approach without re-reading?
			/*iplus = problem.getImagePlus(image_num);
			int threshold = iplus.getProcessor().getAutoThreshold();*/
			
			//iplus.getProcessor(slice) will get from that slice.
			//getImageStackSize() will get the number of slices.
			//
			//
			// for now:
			iplus = problem.getImagePlus(image_num);
			int threshold = getThreshold(iplus);

			//get objects for current image
			tag = getObjectsInImage(image_num, width, height, depth, threshold);
			//System.out.println("tag: " + Arrays.toString(tag));
		    
			/*
			//diag print out every pixel tag
			System.out.print("[ " );
		    for(int i = 0; i < (width*height*depth); i++) {
		      if( i %width == 0 && i !=0)
		        System.out.print("\n");    
              System.out.printf("%1$-4S", tag[i]+ ", " );
		    }
		    System.out.print("] \n" );
		    */
			
			tagCount = getTagCount(tag, width, height, depth);
			keySet = tagCount.keySet();
			
			int[] center = getCofM(image_num, width, height, depth);
			System.out.println("center: " + Arrays.toString(center));

			//set the features
			features[image_num][0] =  calcNumberOfObjects(tagCount);
			features[image_num][1] =  calcAvgSizeOfObjects(tagCount);
			features[image_num][2] =  calcVarSizeOfObjects(tagCount, features[image_num][1]);
			features[image_num][3] =  calcRatioBigToSmallOfObjects(tagCount);
			features[image_num][4] =  calcAvgDistanceToCoM(image_num, width, height, depth, keySet, tag, center);
			features[image_num][5] =  calcVarDistanceToCoM(image_num, width, height, depth, keySet, tag, center, features[image_num][4]);
			features[image_num][6] =  calcRatioFurthestToClosestToCoM(image_num, width, height, depth, keySet, tag, center);
		}
		return features;
	}

	//Object is the data of a 2D image. It can be byte[] or short[] for float[] depending on image type
	private float calcNumberOfObjects(HashMap<Integer, Integer>tagCount)
	{
		return tagCount.size();
	}

	//The average number of above-threshold pixel per object
	private float calcAvgSizeOfObjects(HashMap<Integer, Integer> tagCount)
	{
		int totalSize = 0;
		int count = 0;
		int sizeThreshold = 1; //the min size to include in calculation
		//Get tags that are within threshold
		for(Integer key : tagCount.keySet()) {
			if(tagCount.get(key) >= sizeThreshold)
			{
				totalSize += tagCount.get(key);
				count ++;
			}
		}		
		return (float) totalSize/(float) count;
	}

	private float calcVarSizeOfObjects(HashMap<Integer, Integer> tagCount, float avg)
	{
		float totalDiff = 0;
		int sizeThreshold = 0;
		int count = 0;
		for(Integer key : tagCount.keySet()) {
			if(tagCount.get(key) >= sizeThreshold)
			{
				totalDiff += (tagCount.get(key) - avg)*(tagCount.get(key) - avg);
				count ++;
			}
		}		
		return (totalDiff/(count -1));
	}

	private float calcVarSizeOfObjects(HashMap<Integer, Integer> tagCount)
	{
		float avg = calcAvgSizeOfObjects(tagCount);
		return calcVarSizeOfObjects(tagCount, avg);
	}


	private float calcRatioBigToSmallOfObjects(HashMap<Integer, Integer> tagCount)
	{
		int aKey = tagCount.keySet().iterator().next();
		int maxCount = tagCount.get(aKey);
		int minCount = tagCount.get(aKey);
		for(Integer key : tagCount.keySet()) {
			int value = tagCount.get(key);
			if(value > maxCount) {
				maxCount = value;
			}
			if (value < minCount) {
				minCount = value;
			}
		}
		return maxCount/(float) minCount;
	}

	//calculate the (square of) distance of each object's CoM to the CoM of entire image
	// the CoM of entire image is passed in
	private float calcAvgDistanceToCoM(int imgNum, int width, int height, int depth, java.util.Set<Integer> keySet, short[] tag, int[] center) 
	{
		int[] objectCoM;
		float totaldist = 0;
		for(Integer key : keySet) 
		{
			objectCoM = getCofM(imgNum, width, height, depth, tag, key);
			totaldist += calc3DDist(objectCoM, center);
		}
		return totaldist/keySet.size();
	}

	private float calcVarDistanceToCoM(int imgNum, int width, int height, int depth, java.util.Set<Integer> keySet, short[] tag, int[] center, float avgDist) 
	{
		int[] objectCoM;
		float dist = 0, totalDist = 0;
		for(Integer key : keySet) 
		{
			objectCoM = getCofM(imgNum, width, height, depth, tag, key);
			dist = calc3DDist(objectCoM, center);
			totalDist +=  (dist - avgDist)*(dist - avgDist); 
		}
		return totalDist/keySet.size();
	}

	private float calcRatioFurthestToClosestToCoM(int imgNum, int width, int height, int depth, java.util.Set<Integer> keySet, short[] tag, int[] center) 
	{
		float furthest = -1, closest = 999;
		float dist;
		int[] objectCoM;
		for(Integer key : keySet) 
		{
			objectCoM = getCofM(imgNum, width, height, depth, tag, key);
			dist = calc3DDist(objectCoM, center);
			if (dist > furthest)  furthest = dist;
			if (dist < closest) closest = dist;
		}
		
		//System.out.println("furthest: "+ furthest);
		//System.out.println("closest : "+ closest);
		
		return furthest/closest;

	}

	//this method go through the images and get the objects of interest
	//It  stores each object's information for later processing, in an array with tag
	// threshold is the binarize threshold
	//Basic algorithm is similar as in ImageJ plugin 3D Object Counter
	//http://rsbweb.nih.gov/ij/plugins/track/objects.html
	private short[]  getObjectsInImage(int imgNum, int width, int height, int depth, int threshold)
	{
		int x, y, z;
		int value;
		int arrayIndex;
		short[] tag = new short[width*height*depth];
		//boolean[] isObjPixel = new boolean[width*height*depth];

		//binarize
		//Foreground considered '0', background considered '-1' to allow for 
		//tag notation to start at ID = 1.
		arrayIndex = 0;
		for(z = 1; z <= depth; z++) {
			for(y = 0; y < height; y++) {
				for(x = 0; x < width; x++) {
					//get value
					value = getValue3D(imgNum, z, arrayIndex);
					
					///// **\/*** background intensity toggle! *******/////
					if(value > threshold)
						tag[arrayIndex] = (short)0;
					else 
						tag[arrayIndex] = (short)-1;
						
					arrayIndex++;
					
				}
			}
		}
		
		
		//System.out.println("Printing pixel tags, initial binarization... ");
		//System.out.println("tag: " + Arrays.toString(tag));
		
		/*
		System.out.print("[ " );
		for(int i = 0; i < (width*height*depth); i++) {
		  if( i %width == 0 && i !=0)
		    System.out.print("\n");    
          System.out.printf("%1$-4S", tag[i]+ ", " );
		}
		System.out.print("] \n" );
		*/
		System.out.println("Threshold = " + threshold);
		
		
		
		short tagvois;
		short ID = 1;
		arrayIndex = 0;
		short minTag;
		int i, offset;
		int nX = -1, nY = -1, nZ =-1;

		//First ID attribution
		System.out.println("First ID attribution...");
		for(z = 1;  z<=depth; z++) {
			for(y = 0; y < height; y++) {
				for(x = 0; x < width; x++) {
					if(tag[arrayIndex] > -1) {
						tag[arrayIndex] = ID;
						minTag = ID;

						i = 0;
						//Find the minimum tag in the neighbors pixels
						for(nZ = z - 1; nZ <= z + 1; nZ++) {
							for (nY = y - 1; nY <= y + 1; nY++) {
								for (nX = x - 1; nX <= x + 1; nX++) {
									if(withinBounds(nX, nY, nZ, width, height, depth)) {
										offset = offset(nX, nY, nZ, width, height);
										if(tag[offset] > -1) {
											i++;						//If neighbor pixel is object, increment neighbor object pixel count
											tagvois = tag[offset];
											if (tagvois > 0 && tagvois < minTag) 	//If any neighbor object pixel is already tagged, use that
												minTag = tagvois;
										}
									}
								}
							}
						}
						tag[arrayIndex] = minTag;
						//System.out.println("tag[arrayIndex]: " + tag[arrayIndex]);
						if (minTag == ID)
						{	//If current pixel was given the new tag, increment tag number for next run
							ID++;
						}
					}
					arrayIndex++;
				}
			}
		}
			
		//Minimization of IDs = connection of structures
		System.out.println("Minimization of IDs...");
		arrayIndex = 0;
		for(z = 1; z <= depth; z++) {
			for(y = 0; y < height; y++) {
				for(x = 0; x < width; x++) {
					if(tag[arrayIndex] > -1) {
						minTag = tag[arrayIndex];

						//Find the minimum tag in the neighbors pixels
						for (nZ = z - 1; nZ <= z + 1; nZ++) {
							for (nY = y - 1; nY <= y + 1; nY++) {
								for (nX = x - 1; nX <= x + 1; nX++) {
									if(withinBounds(nX, nY, nZ, width, height, depth)) {
										offset = offset(nX, nY, nZ, width, depth);
										if(tag[offset] > -1) {
											tagvois = tag[offset];
											if (tagvois > 0 && tagvois < minTag)
												minTag = tagvois;
										}
									}
								}
							}
						}

						//Replacing tag by the minimum tag found
						for (nZ = z - 1; nZ <= z + 1; nZ++) {
							for (nY = y - 1; nY <= y + 1; nY++) {
								for (nX = x - 1; nX <= x + 1; nX++) {
									if(withinBounds(nX, nY, nZ, width, height, depth)) {
										offset = offset(nX, nY, nZ, width, height);
										if(tag[offset] > -1) {
											tagvois = tag[offset];
											if (tagvois > 0 && tagvois != minTag)
												replacetag(tag, tagvois, minTag);
										}
									}
								}
							}
						}
					}
					arrayIndex++;
				}
			}
		}
		/*
		System.out.println("Printing some tags != -1... ");
		for( i = 0; i < (width*height*depth); i++) {
		  if( tag[i] >= 0)
           System.out.println("tag = " + tag[i]);
		}
		*/
		return tag;
	}


	//get value
	private int getValue(Object imgData, int arrayIndex)
	{
		int value =0;
		if (imageType == DataInput.GRAY8 || imageType ==  DataInput.COLOR_RGB)
			value = (int) (((byte[])imgData)[arrayIndex] & mask);
		else if (imageType == DataInput.GRAY16)
			value = (int) (((short[])imgData)[arrayIndex] & longmask);	
		return value;
	}

    //get any value from any slice, loading slice if necessary
	private int getValue3D(int imageNum, int sliceNumber, int arrayIndex ) 
	{
	    Object slice = null;
		try {
		  slice =  problem.getData(imageNum, sliceNumber);
	    } catch(Exception e) {
		  System.out.print(e.getMessage());
		  e.printStackTrace();
		}
		//offset the arrayIndex to account for current slice.
		arrayIndex -= ((sliceNumber-1)*width*height);
		
		return getValue(slice, arrayIndex);
	}

	
	
	private boolean withinBounds(int x, int y, int z, int width, int height, int depth) {
		return (x >= 0 && x < width && y >= 0 && y < height && z >0 && z <= depth);
	}

	//Calculates the 1D array index for given x, y, and z
	//z is assumed to be starting at 1
	private int offset(int x, int y, int z, int width, int height) {
		z -= 1;//adjust for z
		return x + (y * width) + (z * width * height);
	}

	public void replacetag(short[] tag, short m, short n){
		for (int i=0; i < tag.length; i++) 
			if (tag[i] == m) tag[i] = n;
	}

	private HashMap<Integer, Integer> getTagCount(short[] tag, int width, int height, int depth) {
		//Key: ID, value: pixel count
		HashMap<Integer, Integer> tagCount = new HashMap<Integer, Integer>();

		System.out.println("getting hashmap of tags...");
		
		int arrayIndex = 0;
		int inttag; 
		int count;
		for(int z=1; z <= depth; z++) { 
			for(int y=0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					if(tag[arrayIndex] > -1) {
						count = 0;
						inttag = (int) tag[arrayIndex]; //type cast from short to int
						if(tagCount.containsKey(inttag)) {
						    //System.out.println("tagCount contains inttag: " + inttag);
							count = tagCount.get(inttag);
						}

						count++;
						
						//System.out.println("put into hashmap inttag: " + inttag + "  count: " + count);
						
						tagCount.put(inttag, count);
					}

					arrayIndex++;
				}
			}
		}

		return tagCount;
	}


	//get  the coordinates of center of mass
	private int[] getCofM(int imgNum, int width, int height, int depth)
	{
		int arrayIndex = 0;
		int sumx = 0, sumvaluex = 0;
		int sumy = 0, sumvaluey = 0;
		int sumz = 0, sumvaluez = 0;
		int value;
		
		for(int z = 1; z <= depth; z++) {
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					value = getValue3D(imgNum, z, arrayIndex);
					sumvaluex +=value*x;
					sumvaluey +=value*y;
					sumvaluez +=value*(z-1);
					sumx += value;
					sumy += value;
					sumz += value;
					arrayIndex++;
				}
			}
		}
			
		int[] center = new int[3];
		center[0] = ((sumx != 0) ? sumvaluex/sumx : 0);
		center[1] = ((sumy != 0) ? sumvaluey/sumy : 0);
		center[2] = ((sumz != 0) ? sumvaluez/sumz : 0);
		
		return center;
	}

	//get CofM for a particular object
	private int[] getCofM(int imgNum, int width, int height, int depth, short[] tag, int index) 
	{
		int arrayIndex = 0;
		int sumx = 0, sumvaluex = 0;
		int sumy = 0, sumvaluey = 0;
		int sumz = 0, sumvaluez = 0;
		int value;
		for(int z = 1; z <= depth; z++) { 
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					if(tag[arrayIndex] == index)
					{
						value = getValue3D(imgNum, z, arrayIndex);
						sumvaluex +=value*x;
						sumvaluey +=value*y;
						sumvaluez +=value*(z-1);
						sumx += value;
						sumy += value;
						sumz += value;
					}
					arrayIndex++;
				}
			}
		}

		int[] center = new int[3];
		center[0] = ((sumx != 0) ? sumvaluex/sumx : 0);
		center[1] = ((sumy != 0) ? sumvaluey/sumy : 0);
		center[2] = ((sumz != 0) ? sumvaluez/sumz : 0);
			
		return center;
	}

	//get a better autothreshold than the first slice only that has 
	//disproportionately high/low avg pixel value.  Uses a center slice instead.
	private int getThreshold(ij.ImagePlus iplus) 
	{
	    int size = iplus.getImageStackSize();
	    int threshold = iplus.getStack().getProcessor((size/2)+1).getAutoThreshold();
	    return threshold;
	}
	
	//calculate the distance between two 3D points
	private float calc3DDist(int[] p1, int[] p2) {
	   return (float) Math.sqrt((p1[0]-p2[0])*(p1[0]-p2[0])+(p1[1]-p2[1])*(p1[1]-p2[1])+(p1[2]-p2[2])*(p1[2]-p2[2]));
	
	}
	
	//Testing main() and diagnostic console output
	public static void main(String[] args)
	{
		String directory = "J:\\BioCAT\\Work\\Image Sets\\to_zj_080925_3DImages\\";
		//String directory =  "F:\\BioCAT\\Work\\Image Sets\\testSet2\\";
		String ext = ".tif";
		String ch = "g";	

		try
		{
			annotool.io.DataInput problem = new annotool.io.DataInput(directory, ext, ch, true);
			FeatureExtractor fe = new ObjectStatistics3D();
			//print out
			System.out.println("Begin feature extraction!");
			float[][] features = fe.calcFeatures(problem);
			System.out.println();
			for(int i = 0; i < features.length; i++)
			{
				System.out.print("Image #" + i + ": ");
				for(int j=0; j< features[i].length; j++)
					System.out.print(features[i][j]+" ");
				System.out.println();
			}
		}catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

}