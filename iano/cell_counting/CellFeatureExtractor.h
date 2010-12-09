/* Jie Zhou March 2010
 *
 */

#ifndef __CELLFEATUREEX_H__
#define __CELLFEATUREEX_H__

// Pass in a 3D cube. Come back as a 2D Haar feature set.
//

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

#include "FeatureExtractorInterface.h"

class CellFeatureExtractor : public FeatureExtractorInterface
{
private: 
   int dimension_1d;
   int length;
   unsigned char * data;
   int level;  
   int trainingflag; //for training samples feature extraction
   int numfeatures;
public:
    CellFeatureExtractor(unsigned char * cubes, int numberCubes, int dimension_1d, int level, int f);
    float * calcFeatures();  //implement the abstract method in FeatureExtractor interface
    int getNumOfFeatures();  //return dimension_1d*dimension_1d;


};

#endif






