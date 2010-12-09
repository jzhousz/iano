#include <iostream>

#include "StackSimpleHaarFeatureExtractor.h"
#include "HaarFeatureExtractor.h"


StackSimpleHaarFeatureExtractor::StackSimpleHaarFeatureExtractor(unsigned char * cubes, int numberCubes, int dimension_1d, int level)
{
   this->dimension_1d = dimension_1d;
   this->length = numberCubes;
   this->data = cubes;
   this->level = level;  
}

 
 
//do feature extraction heres
float * StackSimpleHaarFeatureExtractor::calcFeatures()
{
	float * features  = new float[length*dimension_1d*dimension_1d]; //will delete in caller. init to 0?
	//init
    for(int i=0; i<length*dimension_1d*dimension_1d; i++)
              features[i] = 0;
	
	float *features4CurrentStack;
	//based on bio-image property, focus on x-y pages.
	float weight;
	int mid = dimension_1d/2; 
	int cubesize = dimension_1d*dimension_1d*dimension_1d;
	int pagesize = dimension_1d*dimension_1d;
	
    //different from java version where we can extract a stack of all images, the data are here stored in cube(image) order.
    for(int ci = 0; ci < length; ci ++)
    {
      unsigned char *currentCube = data + ci*cubesize;
	  //for each cube
 	  for(int stackIndex = 0; stackIndex < dimension_1d; stackIndex ++)
	  {
			if (stackIndex > mid - 2 && stackIndex < mid + 2) //using the middle 3 frame for speed purpose
			{
                unsigned char *currentStack = currentCube + stackIndex*pagesize;   
                //just pass one 2d stack image        
                HaarFeatureExtractor haar(level, currentStack, 1, dimension_1d, dimension_1d);
				features4CurrentStack = haar.calcFeatures();

  				//consider weighting: middle stacks are weighted more
  				weight = 1;
				/* if (stackIndex < mid/2 || stackIndex > mid*1.5) weight = 0;
				else  if (stackIndex <= mid)
					weight = (float) stackIndex/mid;
				else
					weight = (float) (dimension_1d - stackIndex)/mid;
				std::cout << "stack index: " << stackIndex << " weight: " << weight;
                */
                int offset = ci*pagesize;
				addFeatures(features+offset,features4CurrentStack, 1, dimension_1d*dimension_1d, weight);
                delete[] features4CurrentStack; //newed in extractor

            }
     }//end if current cube
   }//end of all 3D images
    
	return features;
}

//add the second argument to the 1st argument.
void StackSimpleHaarFeatureExtractor::addFeatures(float* features, float* features4CurrentStack, int length, int dim, float weight)
{
		//for(int i = 0; i < length; i++) 
		//	for (int j = 0; j < dim; j++)
		//		features[i*dim+j] += weight*features4CurrentStack[i*dim+j];

			for (int j = 0; j < dim; j++)
				features[j] += weight*features4CurrentStack[j];

}


int StackSimpleHaarFeatureExtractor::getNumOfFeatures()
{
    return dimension_1d*dimension_1d;
}
