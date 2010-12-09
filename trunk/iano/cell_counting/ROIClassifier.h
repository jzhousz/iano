/* Jie Zhou March 2010
 *
 */

#ifndef __ROICLASSIFIER_H__
#define __ROICLASSIFIER_H__

//
//  Classisfier that trains with markers, and annotate 3D images
//

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

#include <v3d_interface.h>
#include "DataClassifierInterface.h"
#include "SVMClassifier.h"

class ROIClassifier 
{
private:      
   //DataClassifierInterface *pclassifier;
   SVMClassifier *pclassifier;
   QList<string> targetList;
public:
    ROIClassifier();
    ~ROIClassifier(); 
    void trainingWithmarks(unsigned char *trainingCubes, string*, int, int);   
    int* annotateAnImage(Image4DSimple* entireImage, int channel, int grid, int ROIDimension);       
    string lookUpTarget(int inttarget);

protected:
     int classifyACube(unsigned char*  cube, int dimension_1d, struct svm_model * pmodel);
     void convertTargets(string *targets, int*res, int l);

};

#endif



