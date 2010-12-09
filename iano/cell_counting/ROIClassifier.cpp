#include <QtGui>

#include <string>
#include <exception>
#include <iostream>
#include <algorithm>

#include "ROIClassifier.h"
#include "StackSimpleHaarFeatureExtractor.h"
#include "SVMClassifier.h"


/*******************************************************************************

  This is a wrapper that has methods taking 3D training data and testing images.
  The role is similar as ANNROIAnnotator.java, but with 3D data.

*******************************************************************************/


ROIClassifier::ROIClassifier()
{
    //for now, just SVM.  Later the user can pick which classifier via arguments to constructor
    pclassifier = new SVMClassifier();
}


ROIClassifier::~ROIClassifier()
{ 
    if(pclassifier !=NULL) delete pclassifier;
}
  
//train a model given small training image cubes
void ROIClassifier::trainingWithmarks(unsigned char* cubes, string* targets, int numberCubes, int dimension_1d)
{
    //debug: check passed in pixel value
    //std::cout << "second cube:\t";
    //for(int i = 1*dimension_1d*dimension_1d*dimension_1d; i < 2*dimension_1d*dimension_1d*dimension_1d; i++)
    //   std::cout << i << ":" << (int) cubes[i] << " ";

    //3D feature extraction. 
    int level = 1;
    StackSimpleHaarFeatureExtractor extractor(cubes, numberCubes, dimension_1d, level);
    //StackSimpleHaarFeatureExtractor * pex =  new StackSimpleHaarFeatureExtractor(cubes, targets, numberCubes, dimension); 
    float* features = extractor.calcFeatures();
    
    //number of features may be changed after extraction (with HAAR partial frames it is dimension^2.)
    int numOfFeatures = extractor.getNumOfFeatures();
    //delete pex;  //will use new/delete if FeatureExtractor has dynamic memory.

    for(int i = 1*numOfFeatures; i < 2*numOfFeatures; i++)
       std::cout << features[i] << "\t";
    
    //convert string targets to int tagets needed by SVM use a map, store the map somewhere?
    int* inttargets = new int[numberCubes];
    convertTargets(targets, inttargets, numberCubes);
  
    pclassifier->train(features, inttargets, numberCubes, numOfFeatures);
    
    delete[] features; //was newed in extractor
    delete[] inttargets;
    
    return;
 
}


//seperate an entire image into little ones, go through them and get predictions,
//may need step parameter etc.
//return a list of predictions
//1d_dimension is ROIDImension + 1
int* ROIClassifier::annotateAnImage(Image4DSimple* image, int ch, int grid, int dimension_1d)
{
   //go through the image to extract cubes based on grid
   long sx=image->sz0, sy=image->sz1, sz=image->sz2; //, sc=image->sz3;
   long pagesz=sx*sy;
   long channelsz=sx*sy*sz;	
   long dimension_2d = dimension_1d*dimension_1d;
   unsigned char* image1d = image->getRawData();  

   int r = dimension_1d/2; //radius e.g.: 11/2 = 5
   //N desicisions need to be made
   int DN1 = (sx -dimension_1d)/grid +1; //e.g: sx=20, grid=5; (20-11)/5+1=2. centers: 5, 10; sz=512,g:10;d:21
   int DN2 = (sy -dimension_1d)/grid +1;
   int DN3 = (sz -dimension_1d)/grid +1;
   int  N = DN1*DN2*DN3;
   int *res = new int[N];
   int ind = 0;
   int stat = 0;
   unsigned char * cube = new unsigned char[dimension_1d*dimension_1d*dimension_1d];
   //load it once
   struct svm_model * pmodel = pclassifier->loadSVMModel();

   for(int xx = r; xx < sx - r; xx+=grid)
   {
     for(int yy = r; yy < sy - r; yy+=grid)
        for(int zz = r; zz < sz - r; zz+=grid)
        {
           //xx,yy,zz IS THE CENTER FOR EACH CUBE
           //std::cout <<"center: x: " << xx << " y: " << yy << " z: " << zz << std::endl;
           for(int l = xx-r; l<=xx+r; l++)
                for(int m = yy-r; m<=yy+r; m++)
                        for(int n = zz-r; n<=zz+r; n++)
                        {
                           int shiftx=l-(xx-r),shifty=m-(yy-r), shiftz=n-(zz-r);
                           int offset = shiftx+shifty*dimension_1d+shiftz*dimension_2d;     
                           cube[offset] = image1d[l+m*sx+n*pagesz+ch*channelsz];
                        }
           //pass cube
           res[ind] = classifyACube(cube, dimension_1d, pmodel); 

           //std::cout << res[ind];
    
           if(res[ind] != 0) 
           {
              stat ++;
           }
           ind ++;  //done with one cube

       }
       std::cout << "." ;  //one . per xx
    }
    std::cout << "total cubes considered as foreground:" << stat << std::endl;
    std::cout << "clean up the testing cube memory. Total decisions:" << ind<< std::endl;
  
  svm_destroy_model(pmodel);
  delete[] cube;      
  return res;

}


//test on an image cube given a model
int ROIClassifier::classifyACube(unsigned char*  cube, int dimension_1d, struct svm_model * pmodel)
{
    //std::cout << "a cube before feature extraction:\t";
    //for(int i = 0; i < dimension_1d*dimension_1d*dimension_1d; i++)
    //   std::cout << i << ":" << (int) cube[i] << " ";

    //test: just use the middle frame raw data
    /*
    float *features = new float[dimension_1d*dimension_1d];
    int pagesz = dimension_1d*dimension_1d;
    int numOfFeatures = pagesz;
    for(int i = 0; i < pagesz; i++)
            features[i] = (float) cube[i+dimension_1d/2*pagesz];
    */
   
   StackSimpleHaarFeatureExtractor * pex =  new StackSimpleHaarFeatureExtractor(cube, 1, dimension_1d, 1);
   float* features = pex->calcFeatures();
   int numOfFeatures = pex->getNumOfFeatures();
   
 
   // std::cout << "a cube after feature extraction:\t";
   // for(int i = 0; i < numOfFeatures; i++)
   //    std::cout << i << ":" << features[i] << " ";

   //maybe several versions with some return real number?
   int res =  pclassifier->classifyASample(features, numOfFeatures, pmodel);
   
   //free up the features returned from extractor
   delete pex;
   delete [] features;
   
   return res;
}

//convert string targets to int. Store the converting list somewhere (string->int, int->string).
void ROIClassifier::convertTargets(string *targets, int *res, int length)
{
        int intt;
        targetList.clear();
		for (int i=0; i < length; i++)
		{
		   if(!targetList.contains(targets[i]))
				targetList.append(targets[i]);
		   intt = targetList.indexOf(targets[i]);
           res[i] = intt;		
        }		
		
		return;
 
}

//convert back to string target
string ROIClassifier::lookUpTarget(int inttarget)
{
       return targetList.at(inttarget);
}
