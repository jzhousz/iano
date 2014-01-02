import ij.ImagePlus;
import ij.*;
import ij.ImagePlus.*;
import ij.plugin.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;
import ij.util.*;

/* move some of the utility methods out of the original two classes to be shared
 *
 * used by both Exclusive and Inclusive images, if needed. 
 */
public class TopoQuantificationHelper
{
    int Width;
    int Height;

    public TopoQuantificationHelper(int width, int height)
    {
	this.Width = width;
        this.Height = height;	
    }

    //calculate one voxel's TI.
    //If the voxel to the neuropil's dorsal end's distance (with same x,y) is d, to the ventral end's distance is v, then  
    //
    //    ti =    d / (d + v)
    //
    // Only 0 to 1 are considered as valid.  Negative numbers will be ignored.
    //
    //
    private float calcTopologicalIndexForOneVoxel(int z, int minz, int maxz, int limit)
    {
	if  (z < minz || z > maxz) //should not happen but could happen due to segmentation
	{
   	  //axon voxel too far from neuropil will be considered noise.  axon close enough but out of neuropile will be set to 0 or 1 depending on which side.
	  if (z < minz)
	     if ((minz-z) <= limit)  //small but < limit, return 0, otherwise it will be negative
		  return 0;
	     else 
		  return -1;
	  if (z > maxz)
	     if ((z - maxz) <= limit) 
	          return 1;
	     else
	   	  return -1;  //not valid	     
	}

 	return  (z-minz+1)/((float)(maxz-minz+1));
    }

   //Allow some error tolerance for segmentation
   //It should return a number between 0 and 1.
   // -1 indicates error
   private float calcTerritoryPortion(int axonMaxZ, int axonMinZ, int neuropilMaxZ, int neuropilMinZ, int limit)
    {

	float res =0;
	if  (axonMaxZ < axonMinZ) //should not happen
	{
		IJ.log("problem in calculating terri (max < min, possible hollow for that x,y line)");
		return -1;
	}
	if (neuropilMaxZ < neuropilMinZ)
	{
               IJ.log("problem in calc terri (neuroMaxZ < neuroMinZ, possible hollow line for the given x,y");
	       return -1;
	}
	else		          
	    res =  (axonMaxZ - axonMinZ+1)/((float)(neuropilMaxZ - neuropilMinZ+1));

	//maybe good
	if (res > 1.0)
	{
	    //should not happen either, unless due to thresholding the neuropil is smaller than axon	
	    //IJ.log("terr > 1? axonMaxz: "+axonMaxZ + " axonMinz "+ axonMinZ + "neu Max" + neuropilMaxZ + " neu min"+neuropilMinZ);
	    if (( (axonMaxZ - axonMinZ) - (neuropilMaxZ - neuropilMinZ) ) < limit)
		    return 1;
	    else
		    return -1;
	}
	else //finally a good one
	{
  	    //IJ.log("individual terr- "+res);
	    return  res;
	}
    }

    //
    //calculate how many voxels are there in neuropil and axon channel for current x 
    // Different from just counting foreground voxels of an image, it takes in boundary info and consider the boundary validity.
    //
    // return: volumesForCurrentX[0]:  neuropl total voxel;   [1]: axon/clone total voxel count
    private int calcVolumeSum(int axonMaxZ, int axonMinZ, int neuropilMaxZ, int neuropilMinZ, int limit, int[] volumesForCurrentX)
    {
	if  (axonMaxZ < axonMinZ) //should not happen
	{
		IJ.log("problem in calculating volume (max < min, possible hollow for that x,y line)");
		return -1;
	}
	if (neuropilMaxZ < neuropilMinZ)
	{
               IJ.log("problem in calculating volume (neuroMaxZ < neuroMinZ, possible hollow line for the given x,y");
	       return -1;
	}
	//should not happen either, unless due to thresholding or connecting the axon boundary using neighbors, so that the neuropil boundary is smaller than axon	
	if (( (axonMaxZ - axonMinZ) - (neuropilMaxZ - neuropilMinZ) ) >= limit)
	{
              //IJ.log("problem in calculating volume: axon is bigger than neuropil. Axon:" + axonMinZ + "-" + axonMaxZ + " neuropil:" + neuropilMinZ + "-" + neuropilMaxZ);
	      return 0;
	}
	
	//ready to set the value
	volumesForCurrentX[0] = neuropilMaxZ - neuropilMinZ;
	volumesForCurrentX[1] = axonMaxZ - axonMinZ;
	return 1;
    }

    //Count the total foreground voxels
    private int calcTotalVoxelCount(byte[] mask)
    {
       int x, y, value;
       int res = 0;
       for(x=0; x<Width; x++)
         for(y=0; y<Height; y++)
	 {
	   value = mask[y*Width+x]&0xff;	 
	   if (value != 0) //white spot
		   res++;
	 }
       return res;
    }
  
     //
     // Get D/V boundary by linking neighbors
     //
     public boolean getNeuroBoundary(byte[] mask2,  int[] neuropilMinZ, int[] neuropilMaxZ, boolean printBoundary)
     {
        int x,y;
	    int valNeu;
	    int sumEmptyNeuropilX = 0;

        for(x=0; x<Width; x++)	
	    {
	     neuropilMinZ[x] = Height; neuropilMaxZ[x] = 0;
	     for (y=0; y<Height; y++)
	     {
                 valNeu = mask2[y*Width+x]&0xff;
  	         if(valNeu != 0)  //white spot on mask
	         {
	            if (y < neuropilMinZ[x])  
		      neuropilMinZ[x] = y;
                    if (y > neuropilMaxZ[x])
	              neuropilMaxZ[x] = y;
	        }
	     }
	 }
	 //link neighbors for boundary
	 int leftMinZ, rightMinZ, leftMaxZ, rightMaxZ;
	 int leftIndex, rightIndex;
	 for(x=0; x<Width; x++)	
	 {
            if(neuropilMinZ[x] == Height) //empty x
	    {
		sumEmptyNeuropilX++;
		leftIndex = x;
		while(leftIndex>0 && neuropilMinZ[leftIndex] == Height)
		   leftIndex--;	
                leftMinZ = neuropilMinZ[leftIndex]; 
                leftMaxZ = neuropilMaxZ[leftIndex]; 

		rightIndex = x;
		while(rightIndex<Width-1 && neuropilMinZ[rightIndex] == Height)
		   rightIndex++;	
   		rightMinZ = neuropilMinZ[rightIndex];
		rightMaxZ = neuropilMaxZ[rightIndex];
		if(leftMinZ == Height) //didn't find a good left neighbor, will just use right neighbor
		{
		     if (rightMinZ == Height) // neither is good (the entire slice is empty) 	
			     return false;
		     neuropilMinZ[x] = rightMinZ;
		     neuropilMaxZ[x] = rightMaxZ;
		}	     
		else if(rightMinZ == Height) //didn't find a good right neighbor, will just use left neighbor
		{
		     neuropilMinZ[x] = leftMinZ;
		     neuropilMaxZ[x] = leftMaxZ;
		}
		else  // both are good.  
		{
		     neuropilMinZ[x] = (leftMinZ + rightMinZ)/2;
		     neuropilMaxZ[x] = (leftMaxZ + rightMaxZ)/2;
		}
	    }//end of that empty x
	    //IJ.log(x+": min:"+ neuropilMinZ[x] + "max" + neuropilMaxZ[x]);
	 }//end of all x
	 
	 //IJ.log(" -- ratio of empty vertical lines in neuropil that needs linking (%):"+(sumEmptyNeuropilX*100.0)/Width);
	 //find avg min and max
	 if (printBoundary)
	 {
	  int summax = 0; int summin =0;
	  for(x=0; x<Width; x++)
	  {
             summin += neuropilMinZ[x];
             summax += neuropilMaxZ[x];	     
	  }
	  IJ.log("   -- Dorsal limit (avg):"+summin/Width+" ; Ventral limit (avg):"+summax/Width);
	 }

	 return true;
     }


    //
    //calculate topoindex for two binary mask
    //This is for a given slice
    //
    //limit: The number of voxels that will be included in caculation beyond boundary.
    //
    //For calculating T.I. of inclusive set: the limit is 0.
    //
    //For calculating relative T.I. of exclusive set: the limit is height to include all axon voxels on the side.
    //
    //Some variable names end with z, which are actually y in later data sets (which switched y and z, both referring to DV direction).
    //return:
    //    res[0] = tisum;
    //   res[1] = voxelCount; 
    //   res[2] = terr_sum;
    //   res[3] = terr_count;
    //   res[4]:  neuropil's # of voxels after connecting boundary
    //   res[5]:  axon's #of voxels after connecting boundary
    //   res[6]:  neuropil's foreground # of voxels.  Different from 4, it does not connect boundary or compare check min/max condition
    //   res[7]:  axon's     foreground # of voxels.
	//   res[8]:  dorsal boundary relative distance 
	//   res[9]: ventral boundary relative distance
	//   res[10]: # of ventral-most voxels (it should be the same as res[9], since if there is at least one voxel, then that x has 2 rds.
	//   12/21/2013: add projected neuropil boundary for boundary calculation.
    public float[] getTopoIndexGivenTwoImages(ImagePlus axonImg, ImagePlus neuImg, int limit, int[] twoDNeuMinY, int[] twoDNeuMaxY)
    {
    	float ti =0, tisum =0;
	    int voxelCount = 0;  //total # of axon terminal voxels;
	    int valNeu, valAxon;
	    float terr_proportion =0;  //proportion of territory
	    float terr_sum =0 ;
	    int terr_count = 0;
        int x,y;       
	    int neuropilMinZ, neuropilMaxZ, axonMinZ, axonMaxZ; //actually y after z and y are switched in later data sets.
	    int neuropilFgCount = 0;
        int cloneFgCount = 0;

 	    byte[] mask1 = (byte[]) axonImg.getProcessor().getPixels();
        byte[] mask2 = (byte[]) neuImg.getProcessor().getPixels();

	    cloneFgCount = calcTotalVoxelCount(mask1);
	    neuropilFgCount = calcTotalVoxelCount(mask2);

	    //set boundary
 	    int[] neuropilMinY = new int[Width];
	    int[] neuropilMaxY = new int[Width];
	    //IJ.log("     -- get neuropil boundary for calculating topo index ");
	    if(!getNeuroBoundary(mask2, neuropilMinY, neuropilMaxY, true))
		 return null;

	    //need boundary of axon for volume, 11/22/2012
	    //Note that this is not used by territory proportion!
	    int[] axonMinY = new int[Width];
	    int[] axonMaxY = new int[Width];
	    if(!getNeuroBoundary(mask1, axonMinY, axonMaxY, true))
		   return null;

	    //data holder for volumes
	    int[] volumesForCurrentX = new int[2];
	    int neuropilVolume = 0;
	    int cloneVolume = 0;
        
		//data holder for boundaries 12/19/2013
		float dorsalRd =0, ventralRd=0;
		int boundaryVoxelCount =0;
        for(x=0; x<Width; x++)	
	    {
            neuropilMinZ = neuropilMinY[x]; 
	        neuropilMaxZ = neuropilMaxY[x];

	        //now look at the inner object in another channel  
	        axonMinZ = Height; axonMaxZ = 0;	  
	        //find out how many z are there in the axon terminal (index 1 channel)
	       for (y=0; y<Height; y++)
	       {
	         valAxon = mask1[y*Width+x]&0xff;
	         //check if it is a valid axon voxel and there is sth in the other channel too, if yes, calculate 
	         if ( valAxon != 0 && neuropilMinZ != Height)
	         {
     	        ti =  calcTopologicalIndexForOneVoxel(y, neuropilMinZ, neuropilMaxZ, limit);
 	            if (ti >= 0)  //if ti is < 0, the proportion of ti is not right, does not count (noise)
	            {
                  voxelCount++;
	              tisum += ti;
		
		          //for territory
                  if (y < axonMinZ)  
		            axonMinZ = y;
                  if (y > axonMaxZ)
	                axonMaxZ = y;
		        } 
	          }
	       } //end of 2nd z loop

         //calculate territory proportion if there is sth in both channels
	     if (axonMinZ != Height && neuropilMinZ != Height)
	     {
	       terr_proportion = calcTerritoryPortion(axonMaxZ, axonMinZ, neuropilMaxZ, neuropilMinZ, limit);
	       if (terr_proportion >=0) //only if it is a reasonable one 
	       {
	         terr_count ++;	     
 	         terr_sum +=terr_proportion;
	       }
		   //boundary using 3D neurpil boundary
		   /*if (axonMinZ > neuropilMinZ) //axon boundary is inside neuropil. Otherwise add 0 (possible due to binarization and noise).
		   {
	          dorsalRd += (axonMinZ-neuropilMinZ)/((float)(neuropilMaxZ - neuropilMinZ+1));
		   }
		   if (neuropilMaxZ > axonMaxZ) //axon boundary is inside neuropil. Otherwise add 0 (possible due to binarization and noise).
		   {		      
			  ventralRd += (neuropilMaxZ-axonMaxZ)/((float)(neuropilMaxZ-neuropilMinZ+1));
			}*/
			//boundary using 2D projected neuropil boundary based on Limin's suggestion
			float neuropilBoundaryDis = twoDNeuMaxY[x] - twoDNeuMinY[x];
			if(axonMinZ > twoDNeuMinY[x])
			    dorsalRd += (axonMinZ - twoDNeuMinY[x])/neuropilBoundaryDis;
			if(twoDNeuMaxY[x] > axonMaxZ)
			    ventralRd += (twoDNeuMaxY[x] - axonMaxZ)/neuropilBoundaryDis;
				
	
			  
		   boundaryVoxelCount ++;
	     }

	     //calculate volume ratio. Different from territory porportion, it sums up the voxel num in both clone and neuropil
	     //The method return the volume total for current x
	    if (calcVolumeSum(axonMaxY[x], axonMinY[x], neuropilMaxY[x], neuropilMinY[x], limit, volumesForCurrentX) == -1)
	    {
		  //sth wrong with the calculation. Should not happen if the boundaries are already linked. Discard this x?
		  IJ.log("  Something wrong with the volume calculation! ");	
	    }
	    else
	    {
	         neuropilVolume += volumesForCurrentX[0];
               cloneVolume += volumesForCurrentX[1]; //Different from voxelCount because it does not check if valAxon is 0 -- so it includes dark voxels within territory
	    }

	 }//end of x
       
 
       float[] res = new float[11];
       res[0] = tisum;
       res[1] = voxelCount; 
       res[2] = terr_sum;
       res[3] = terr_count;
       res[4] = neuropilVolume;
       res[5] = cloneVolume;
       res[6] = neuropilFgCount;
       res[7] = cloneFgCount;
	   res[8] = dorsalRd;
	   res[9] = ventralRd;
	   res[10] = boundaryVoxelCount;

       	
       /*
       IJ.log("  -- Total number of voxels in the axon terminal: " + voxelCount);	
       IJ.log("  -- Topographic index for the given plane : " + res[0]);
       IJ.log("  -- Total territory for the given plane: " + terr_sum);
       IJ.log("  -- Total number of x for territory calculation: " + terr_count);
       IJ.log("  -- Size-Normalized territory for the given plane: " + res[1]);
       */

       return res;
    }

	
    /* 12/12/2013 
	   Get 2D boundary by z-projecting the original binary image. 
	   Gifferent from the method that does the projection on RBG Image, 
	   1. It works with one channel, so it does not combine channels depending on inclusive/exclusive.
	   2. It has only one channel and was already binarized.  It is for boundary analysis not denoise. 
	
       It then calls the helper's method to get the neuropil boundary by linking the gaps(if the input two arrays are not null).	
	   
	   It returns the projected image.
	*/
     ImagePlus get2DBoundaryViaProjectionOnBinaryImage(int[] neuropilMinZ, int[] neuropilMaxZ, ImagePlus  imgp)
	 {
	   //do z-project (on a one channel image)
	   ZProjector projector = new ZProjector(imgp);
	   projector.setMethod(ZProjector.MAX_METHOD);
	   projector.doProjection();
	   ImagePlus projected = projector.getProjection();
	
	   if (neuropilMinZ != null && neuropilMaxZ != null) //calculate boundary
	   {
	     byte[] mask = (byte[]) projected.getProcessor().getPixels();
	     this.getNeuroBoundary(mask, neuropilMinZ, neuropilMaxZ, false);
	   }
	   
	   return projected;
	 }
	
	/*12/12/2013
	Calculate boundary metric given neuorpil boundaries and a 2D axon binary mask.
	
	Step 1: in the neuropil image (-that is the projection image), along each x-axis, find the dorsal and ventral limits (Nd(x) and Nv(x)), and then calculate the distance between these two limits (Nd(x)-Nv(x));
	Step 2: in the clone image (-also the projection image), along each x-axis, find the dorsal-most pixel (Cd(x)).
	Step 3: calculate the relative distance (rd) by taking the ratio of (Nd(x) - Cd(x))/(Nd(x)-Nv(x))
	Step 4: repeat the second and third steps for the ventral-most pixel at the same x position (Cv(x)).
	
	return  res[0]: dorsal boundary relative distance  d-rd;   res[1]: ventral doundary relative distance v-rd.
	*/
	float[] getBoundaryMetric(int[] twoDNeuMinY, int[] twoDNeuMaxY, ImagePlus axonProjection)
	{
	   float drdsum =0, vrdsum =0;
	   int valAxon;
	   int voxelCount = 0;  //total # of dorsal-most and ventral-most axon terminal voxels; Equal to  number of x-axis that has axon voxels.
	   int neuropilMinY, neuropilMaxY, axonMinY, axonMaxY;  
	   float neuropilBoundaryDis;
	
	   byte[] axonMask = (byte[]) axonProjection.getProcessor().getPixels();
	   for(int x=0; x<Width; x++)	
	   {
  	        //initialize axon ventral and dorsal limits to impossible values
	        axonMinY = Height; axonMaxY = 0;	
            for (int y=0; y<Height; y++)	
			{			
				valAxon = axonMask[y*Width+x]&0xff;
	            if ( valAxon != 0)  // a valid foreground axon voxel
				{
				  //update dorsal-most and ventral-most position
				  if (y < axonMinY)  
		             axonMinY = y;
                  if (y > axonMaxY)
	                 axonMaxY = y;
				}
			}
			//calculate drd and vrd if this x has something in axon channel.
	        if (axonMinY != Height)
	        {
		        neuropilMinY = twoDNeuMinY[x]; 
	            neuropilMaxY = twoDNeuMaxY[x];
			    neuropilBoundaryDis = neuropilMaxY - neuropilMinY +1;
		        voxelCount ++;
		       
			    //dorsal-rd (top)
			    if (axonMinY > neuropilMinY) //axon boundary is inside neuropil. Otherwise add 0 (possible due to binarization and noise).
			       drdsum +=  (axonMinY - neuropilMinY)/neuropilBoundaryDis;
				//ventral-rd (bottom)
			    if (neuropilMaxY > axonMaxY) //axon boundary is inside neuropil. Otherwise add 0 (possible due to binarization and noise).
			        vrdsum +=  (neuropilMaxY - axonMaxY)/neuropilBoundaryDis;
		
			}   
		}  //end of x
	
		float[] res = new float[2];
        res[0] = drdsum/voxelCount;
        res[1] = vrdsum/voxelCount;
        return res;		
	}

}
