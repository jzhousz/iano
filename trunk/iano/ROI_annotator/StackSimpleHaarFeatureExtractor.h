/* Jie Zhou March 2010
 *
 */

#ifndef __STACKSIMPLEHAARFEATUREEX_H__
#define __STACKSIMPLEHAARFEATUREEX_H__

// Pass in a 3D cube. Come back as a 2D Haar feature set.
//

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

#include "FeatureExtractorInterface.h"

class StackSimpleHaarFeatureExtractor : public FeatureExtractorInterface
{
private: 
   int dimension_1d;
   int length;
   unsigned char * data;
   int level;  
public:
    StackSimpleHaarFeatureExtractor(unsigned char * cubes, int numberCubes, int dimension_1d, int level);
    float * calcFeatures();  //implement the abstract method in FeatureExtractor interface
    int getNumOfFeatures();  //return dimension_1d*dimension_1d;
protected:
    void addFeatures(float* features, float* features4CurrentStack, int length, int dim, float weight);
};

#endif






