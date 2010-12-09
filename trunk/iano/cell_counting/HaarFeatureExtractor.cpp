#include <iostream>
#include <math.h>

#include "HaarFeatureExtractor.h"

HaarFeatureExtractor::HaarFeatureExtractor(int level, unsigned char * data,  int length, int wid, int height)
{
   this->totalwidth = wid;
   this->totalheight = height;
   this->length = length;
   this->data = data;
   this->totallevel = level;  
}

 
 
//do 2D feature extraction for many images.
float * HaarFeatureExtractor::calcFeatures()
{
       float *   features  = new float[length*totalwidth*totalheight]; //will be cleaned up by caller
        
       for(int i=0; i <length; i++)
       {
          getHaarFeatureOfOneImage(data+i*totalwidth*totalheight, features+i*totalwidth*totalheight);
       }
        
        return features;

}


int HaarFeatureExtractor::getNumOfFeatures()
{
    return totalwidth*totalheight;
}


void HaarFeatureExtractor::getHaarFeatureOfOneImage(unsigned char * data, float* feature)
{
	    //copy data to feature, convert to float
	    for(int i = 0; i< totalwidth*totalheight; i++)
	       feature[i] = (float) data[i];

		//added 03232010:  standardize the image first
		scaleAnImage(feature,totalwidth*totalheight);
		
		//uncomment back later 03/30/2010
		//boundary problem in haar: for odd dim, some were not changed ...
        haarTransform(feature,  totalwidth, totalheight, totallevel, totalwidth);
}

/* 
      recursive function to do multilevel HaarTransform.
     feature is passed back via argument.
     origWid: Original width of the 1D storage of image. Needed for correctly accessing the data for level >= 2.
*/     
void HaarFeatureExtractor::haarTransform(float* data, int wid, int height, int level, int origWid)
{

         if (level < 1)
              return;

         float* temp = new float[wid*height];
         int i, j, k;

         //process rows (addition and subtraction of neighboring rows) and put into temp
		 //No risk of arrayoutofbound here but boundary may be lost. Example: height =9; i= 0-3; k+1 can be up to 7 (row 8).
         for(i = 0; i < height/2; i++)
         {
			k = 2*i;
            for(j = 0; j < wid; j++)
            {
               temp[i*wid+j] = (data[k*origWid+j] + data[(k+1)*origWid+j])/2;  //top half
               temp[(height/2+i)*wid+j] = (data[k*origWid+j] - data[(k+1)*origWid+j])/2;   //lowerhalf
            }
         }

         //process columns and put back to data
         for(i = 0; i < height; i++)
              for(j=0; j< wid/2; j++)
              {
                k = 2*j;
                data[i*origWid+j] = (temp[i*wid+k] + temp[i*wid+(k+1)])/2; //left half
                data[i*origWid+wid/2+j] =  (temp[i*wid+k]- temp[i*wid+k+1])/2;  //right half
              }

         delete[] temp;

         if (level > 1)
         {
             //recursively call data, the top-left  1/4 will be changed.
             haarTransform(data, wid/2, height/2, level -1, origWid);
         }
}

void HaarFeatureExtractor::scaleAnImage(float* features, int dimension)
{

   for(int i=0; i<dimension; i++)
     features[i] = (features[i]-127.0)/127.0;
}


//ZScore a raw image
void HaarFeatureExtractor::zscoreAnImage(float* features, int dimension)
{     //use double for better precision
		int j;
		float cursum, curmean, curstd;

		//standardize an image
		cursum = 0;
		curmean = 0;
		curstd = 0;
		for (j = 0; j < dimension; j++)
		      cursum += features[j];
		curmean = cursum / dimension;
		cursum = 0;
		float tmpf;
		for (j = 0; j < dimension; j++)
		{
		    tmpf = features[j] - curmean;
			cursum += tmpf * tmpf;
		}
		curstd = (dimension == 1) ? 0 : sqrt (cursum /(float) (dimension - 1));	//an unbiased version for Gaussian
		for (j = 0; j < dimension; j++)
		{
		  features[j] = (features[j] - curmean) / curstd;

		}
 
}

