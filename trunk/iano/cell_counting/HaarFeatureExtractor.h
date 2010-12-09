/* Jie Zhou March 2010
 *
 */

#ifndef __HAARFEATUREEX_H__
#define __HAARFEATUREEX_H__

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

#include "FeatureExtractorInterface.h"

class HaarFeatureExtractor : public FeatureExtractorInterface
{
private: 
   int totalwidth;
   int totalheight;
   int length;
   unsigned char * data;
   int totallevel;  
public:
    HaarFeatureExtractor(int level, unsigned char * data,  int length, int wid, int height);
    float * calcFeatures();  
    int getNumOfFeatures();  
protected:
   void getHaarFeatureOfOneImage(unsigned char * data, float* feature);
   void haarTransform(float* data, int wid, int height, int level, int origWid);
   void zscoreAnImage(float* features, int dimension);
   void scaleAnImage(float* features, int dimension);
};

#endif
