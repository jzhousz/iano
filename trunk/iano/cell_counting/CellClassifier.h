/* Jie Zhou March 2010
 *
 */

#ifndef __CELLCLASSIFIER_H__
#define __CELLCLASSIFIER_H__

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

class CellClassifier 
{
private:      
   //DataClassifierInterface *pclassifier;
   SVMClassifier *pclassifier;
   QList<string> targetList;
public:
    CellClassifier();
    ~CellClassifier(); 
    void trainingWithmarks(unsigned char *trainingCubes, string*, int, int);   
    unsigned char* annotateAnImage(V3DPluginCallback &callback,v3dhandle win, Image4DSimple* image, int ch, int grid, int dimension_1d);

    string lookUpTarget(int inttarget);

protected:
     //make a decision for a voxel, may call classifyACube().     
     int classifyAVoxel(Image4DSimple* image, int ch, int iz,int iy,int ix, int r, struct svm_model * pmodel);
     int classifyACube(unsigned char*  cube, int dimension_1d, struct svm_model * pmodel);
     int classifyACubeWithProbability(unsigned char*  cube, int dimension_1d, struct svm_model * pmodel, double *p);
     void convertTargets(string *targets, int*res, int l);

};

#endif



