#include <iostream>
#include <math.h>

#include "CellFeatureExtractor.h"
#include "HaarFeatureExtractor.h"
#include "basicutil.h"

void mean_std(unsigned char * cube, int dimension_1d, int xb, int xe, int yb, int ye, int zb, int ze, int *mean, float *std)
{
  int sum =0;
  long size = (xe-xb)*(ye-yb)*(ze-zb);
  //int mean;
  for(int i = xb; i < xe; i++)
    for(int j= yb; j < ye; j++)
     for(int k = zb; k < ze; k++)
     {
        int offset = k*(dimension_1d*dimension_1d) + j*dimension_1d + i;
        sum += cube[offset];
     }
  double dmean = sum/(float)(size); 
  *mean = (int) dmean;
  
  double var=0.0;
  double s = 0.0;
  for(int i = xb; i < xe; i++)
    for(int j= yb; j < ye; j++)
     for(int k = zb; k < ze; k++)
     {
        int offset = k*(dimension_1d*dimension_1d) + j*dimension_1d + i;
		s = cube[offset]-(dmean);
		var += (s*s);
	}
  var=var/(size-1);
  *std=(sqrt(var));
  
}

//for valley: negative; for boundary: about 0; for center: positive
/*
void centeredgediff(unsigned char * cube, int dimension_1d, float * features)
{
  //the difference between a inner cube and the outside area.
  long sum = 0;
  float total = 0;
  long id;
  int i, j, k;
  for(i = dimension_1d/4; i < dimension_1d*3/4; i++)
    for()
      for()
      {  sum++;
         long id =
         total+=cube[id];
      }
   
   inneravg =    
      
  for()
     for()
      for()       
  outsideavg = 
  
  feature[0] = inneravg - outsideavg;    
}
*/

void diagonaldiff(unsigned char * cube, int dimension_1d, float * features)
{
  //extract the difference of diagonal corners on a cube
  //8 quandants. clockwise upper layer: 0123, lower layer 4567
  long xb0 =0, xe0 = dimension_1d/2;
  long yb0 =0, ye0 = dimension_1d/2;
  long zb0 =0, ze0 = dimension_1d/2;

  long xb1 = dimension_1d/2, xe1 = dimension_1d;
  long yb1 =0, ye1 = dimension_1d/2;
  long zb1 =0, ze1 = dimension_1d/2;

  long xb2 = dimension_1d/2, xe2 = dimension_1d;
  long yb2 = dimension_1d/2, ye2 = dimension_1d;
  long zb2 =0, ze2 = dimension_1d/2;

  long xb3 =0, xe3 = dimension_1d/2;
  long yb3 =dimension_1d/2, ye3 = dimension_1d;
  long zb3 =0, ze3 = dimension_1d/2;
  
  //lower layer
  long xb4 =0, xe4 = dimension_1d/2;
  long yb4 =0, ye4 = dimension_1d/2;
  long zb4 = dimension_1d/2, ze4 = dimension_1d;

  long xb5 =dimension_1d/2, xe5 = dimension_1d;
  long yb5 =0, ye5 = dimension_1d/2;
  long zb5 = dimension_1d/2, ze5 = dimension_1d;

  long xb6 =dimension_1d/2, xe6 = dimension_1d;
  long yb6 =dimension_1d/2, ye6 = dimension_1d;
  long zb6 =dimension_1d/2, ze6 = dimension_1d;

  long xb7 =0, xe7 = dimension_1d/2;
  long yb7 = dimension_1d/2, ye7 = dimension_1d;
  long zb7 =dimension_1d/2, ze7 = dimension_1d;

  int mean1 =0, mean2=0; float std =0;
  float f1, f2, f3, f4;
  mean_std(cube, dimension_1d, xb0, xe0, yb0, ye0, zb0, ze0, &mean1, &std);
  mean_std(cube, dimension_1d, xb6, xe6, yb6, ye6, zb6, ze6, &mean2, &std);
  f1 = abs(mean1 - mean2);
  mean_std(cube, dimension_1d, xb1, xe1, yb1, ye1, zb1, ze1, &mean1, &std);
  mean_std(cube, dimension_1d, xb7, xe7, yb7, ye7, zb7, ze7, &mean2, &std);
  f2 = abs(mean1 - mean2);
  mean_std(cube, dimension_1d, xb2, xe2, yb2, ye2, zb2, ze2, &mean1, &std);
  mean_std(cube, dimension_1d, xb4, xe4, yb4, ye4, zb4, ze4, &mean2, &std);
  f3 = abs(mean1 - mean2);
  mean_std(cube, dimension_1d, xb3, xe3, yb3, ye3, zb3, ze3, &mean1, &std);
  mean_std(cube, dimension_1d, xb7, xe5, yb5, ye5, zb5, ze5, &mean2, &std);
  f4 = abs(mean1 - mean2);

  features[0] = (f1+f2+f3+f4)/4.0;
}


CellFeatureExtractor::CellFeatureExtractor(unsigned char * cubes, int numberCubes, int dimension_1d, int level, int trainingflag)
{
   this->dimension_1d = dimension_1d;
   this->length = numberCubes;
   this->data = cubes;
   this->level = level;  
   this->trainingflag = trainingflag;  

}

/*******************************************************************
//do feature extraction here
//Features: 
//a. 3d mean and std               (2)
//b. normalized cross-correlation with Gaussian template     (1)
//c. 3d diagnoal differences       (4)
//d. wavelet coefficients of central pages.   dimension_1d*dimension_1d   
// In the future:   features of 2 cubes from 2 channels
*********************************************************************/
float * CellFeatureExtractor::calcFeatures()
{
   
    int numofFeatures = dimension_1d*dimension_1d + 4;
	float * features  = new float[length*numofFeatures]; 

    //gaussian kernel for coeff
    double sigmax = (dimension_1d-1)/4.0, sigmay = (dimension_1d-1)/4.0, sigmaz = (dimension_1d-1)/4.0;
    double *g_1d = genGaussianKernel1D(dimension_1d, dimension_1d, dimension_1d, sigmax, sigmay, sigmaz);

    //extract middel page wavelet: based on bio-image property, focus on x-y pages.
	float *features4CurrentStack;
	int mid = dimension_1d/2; 
	int cubesize = dimension_1d*dimension_1d*dimension_1d;
	int pagesize = dimension_1d*dimension_1d;
    for(int ci = 0; ci < length; ci ++)
    {
      unsigned char *currentCube = data + ci*cubesize;

	  this->numfeatures = dimension_1d*dimension_1d + 4;
	  int mean =0;
      mean_std(currentCube, dimension_1d, 0, dimension_1d, 0, dimension_1d, 0, dimension_1d, &mean, features+ ci*numofFeatures+1);
      features[ci*numofFeatures] = (float) mean;
      features[ci*numofFeatures + 2] = coeff(currentCube, g_1d, cubesize);
      
      if (trainingflag) std::cout << ci << ": " << features[ci*numofFeatures + 2] << ' ';
      
      diagonaldiff(currentCube, dimension_1d, features+ ci*numofFeatures + 3);
      //just pass one 2d stack image      
      unsigned char *currentStack = currentCube + mid*pagesize;   
      HaarFeatureExtractor haar(level, currentStack, 1, dimension_1d, dimension_1d);
      features4CurrentStack = haar.calcFeatures();
      //assign
      for(int i =0; i < dimension_1d*dimension_1d; i++)       
          features[ci*numofFeatures + 4 + i]= features4CurrentStack[i];
      delete[] features4CurrentStack;
      
      
      /*
	  this->numfeatures = dimension_1d*dimension_1d + 4;
      int mean =0;
      mean_std(currentCube, dimension_1d, 0, dimension_1d, 0, dimension_1d, 0, dimension_1d, &mean, features+ ci*numofFeatures+1);
      features[ci*numofFeatures] = (float) mean;
      features[ci*numofFeatures + 2] = coeff(currentCube, g_1d, cubesize);
      diagonaldiff(currentCube, dimension_1d, features+ ci*numofFeatures + 3);
      //just pass one 2d stack image      
      unsigned char *currentStack = currentCube + mid*pagesize;   
      HaarFeatureExtractor haar(level, currentStack, 1, dimension_1d, dimension_1d);
      features4CurrentStack = haar.calcFeatures();
      //assign
      for(int i =0; i < dimension_1d*dimension_1d; i++)       
          features[ci*numofFeatures + 3 + i]= features4CurrentStack[i];
      delete[] features4CurrentStack;
      */
      
      
   }//end of all 3D images
    
    delete[] g_1d; //newed in genGaussianKernel
	return features;
}



//should be called after calcFeatures
int CellFeatureExtractor::getNumOfFeatures()
{
    return  numfeatures; //dimension_1d*dimension_1d + 3;
}
