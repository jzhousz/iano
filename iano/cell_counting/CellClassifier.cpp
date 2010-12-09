#include <QtGui>

#include <string>
#include <exception>
#include <iostream>
#include <algorithm>

#include "CellClassifier.h"
#include "CellFeatureExtractor.h"
#include "SVMClassifier.h"
#include "../basic_c_fun/basic_landmark.h"
#include "basicutil.h"

using namespace std;

/*******************************************************************************

  This is a wrapper that has methods taking 3D training data and testing images.
  The role is similar as ANNROIAnnotator.java, but with 3D data.
  
  For Cell Counter, annotating an image becomes counting cells in an image. 
*******************************************************************************/

//some important parameters
//the search radius for getMassCenter().  Insensitive to final results. (?)
float searchMagRate = 1.2;   
//the clear radius for clearSurrounding(). Sensitive. If 2.0, then within 2.0*r will not have two cells (just clear flag). Avoid one cell split into two. But a higher rate may cause missing cells. 
float CLEAR_EXPAND_RATE = 1.5; //about FINAL_CHECK_RATE * 2.
//the check radius for nearACenter(). Sensitive. If 1.0, then all cell centers are 2r apart. Avoid one cell split into 2. But if there is one not in the cell center, then many others won't be in the center.
float FINAL_CHECK_RATE = 0.7;  

#define INF 1E9  //for gaussian filtering

//prototypes of submethods
//2 methods of getting local maxima. One calls the other
unsigned char * check_localMaxima(V3DPluginCallback &callback, Image4DSimple* image, int ch);
unsigned char * check_localMaxima_ker(unsigned char * image1d, long sx, long sy, long sz, int ch);
void getMassCenter(unsigned char* image1d, long sx, long sy, long sz, int ix, int iy, int iz, int r, int ch, double& ncx, double& ncy, double& ncz, unsigned char * flag_lm);
//void getMassCenter(Image4DSimple* image, int ix, int iy, int iz, int r, int ch, double& newx, double& newy, double& newz, unsigned char * flag_lm);
void  clearSurrounding(Image4DSimple* image, int ch , int x, int y, int z, int r, unsigned char* lm);
void getMatchingCenter(Image4DSimple* image, int ix, int iy, int iz, int r, int ch, double& ncx, double& ncy, double& ncz, unsigned char*);
//for determine if a given location is close to  an exisiting cell location. Different from clearSurrouding which just clears the flag.
bool nearACenter(int newx, int newy, int newz, unsigned char * results, int r, int sx, int sy, int sz);
//get a cube of given dimension from a image
unsigned char* getACube(Image4DSimple* image, int ch, int iz,int iy,int ix, int dimension_1d, int dimension_2d, long pagesz, long channelsz);
unsigned char * gaussianfiltering(unsigned char *data1d, long sx, long sy, long sz, int Wx, int Wy, int Wz);


CellClassifier::CellClassifier()
{
    //for now, just SVM.  Later the user can pick which classifier via arguments to constructor
    pclassifier = new SVMClassifier();
}


CellClassifier::~CellClassifier()
{ 
    if(pclassifier !=NULL) delete pclassifier;
}
  
//train a model given small training image cubes
void CellClassifier::trainingWithmarks(unsigned char* cubes, string* targets, int numberCubes, int dimension_1d)
{
     CellFeatureExtractor extractor(cubes, numberCubes, dimension_1d, 1,1);
     float* features = extractor.calcFeatures();
     
     //QMessageBox::information(0, "debug", QObject::tr("feature extraction done"));
     int numOfFeatures = extractor.getNumOfFeatures();

    int* inttargets = new int[numberCubes];
    //convertTargets(targets, inttargets, numberCubes); //don't convert targets to keep the boolean meaning
    for(int i=0; i<numberCubes; i++)
       inttargets[i] = atoi(targets[i].c_str());
    
    pclassifier->train(features, inttargets, numberCubes, numOfFeatures);
    
    delete[] features; //was newed in extractor
    delete[] inttargets;
    
    return;
}


//return a list of predictions ? Or a mask image? 
//  Also need numbers of cells, and centers of cells
//work on local maxima
//use different preprocessing, feature extraction etc.
//1d_dimension is ROIDImension + 1
//Note: be aware of the order x, y, z are passed to submethods.
unsigned char* CellClassifier::annotateAnImage(V3DPluginCallback &callback, v3dhandle win, Image4DSimple* image, int ch, int grid, int dimension_1d)
{

   	 std::vector <LocationSimple> detectedPos;
   	 LandmarkList markerList;
     long sx=image->sz0, sy=image->sz1, sz=image->sz2; 
     long pagesz=sx*sy;
     long channelsz=sx*sy*sz;

     //smoothed and get local maxima
     //sensitive gaussian filtering radius
     //  ??????? use distance transform image???????
     
     int Wx = dimension_1d, Wy = dimension_1d, Wz = dimension_1d;  
     unsigned char * filtered = gaussianfiltering(image->getRawData(), sx, sy, sz, Wx, Wy, Wz);
     
     unsigned char * flag_lm = check_localMaxima_ker(filtered, sx, sy, sz, 0);
     
     
     //unsigned char * flag_lm = check_localMaxima(callback, image, ch); //062110

     unsigned char *results = new unsigned char [channelsz]; //prediction results.
     if (!results)
     {
        printf("Fail to allocate memory.\n");
        return NULL;
     }
     unsigned char *clearFlag = new unsigned char [channelsz]; //prediction results.
     if (!clearFlag)
     {
        printf("Fail to allocate memory.\n");
        return NULL;
     }
 
     //get model from file
     //string modelfile = "test_svm_model.txt";
     struct svm_model * pmodel = pclassifier->loadSVMModel(); 

     int r = dimension_1d/2;
     int prediction;
     int total = 0, cellcount = 0;
 
     //use (a somehow more restrict) template matching for adding some candidates.
     bool candidateflag = false;
     long cubesize = dimension_1d*dimension_1d*dimension_1d;
     double sigmax = (dimension_1d-1)/4.0, sigmay = (dimension_1d-1)/4.0, sigmaz = (dimension_1d-1)/4.0;
     double *g_1d = genGaussianKernel1D(dimension_1d, dimension_1d, dimension_1d, sigmax, sigmay, sigmaz);
     double curcoeff;
     //double coeff_th = 0.66; //will use average coeff of positive ones from training!!!!
     
     //init results
     for(long iz = 0; iz < sz; iz++)
     {
        long offsetk = iz*sx*sy;
        for(long iy = 0; iy < sy; iy++)
        {
            long offsetj = iy*sx;
            for(long ix = 0; ix < sx; ix++)
            {
               long idx = offsetk + offsetj + ix;
               results[idx] = 0;
               clearFlag[idx] = 1; //will be cleared later
             }
        }    
     }     

     //start search     
     int denied = 0;
     for(long iz = r; iz < sz-r; iz++)
     {
        long offsetk = iz*sx*sy;
        for(long iy = r; iy < sy-r; iy++)
        {
            long offsetj = iy*sx;
            for(long ix = r; ix < sx-r; ix++)
            {
               long idx = offsetk + offsetj + ix;
 
               //debug:
               //if(ix == 61 && iy==39 && iz==17) cout << "the missed one is here!!" << (int) flag_lm[idx] << endl;
               //if(ix == 63 && iy ==43 && iz ==16) cout << "the valley one (#3) is here!!" << (int) flag_lm[idx] << endl;;
               //if(ix == 65 && iy ==43 && iz ==14) cout << "the valley start point is here!!" << (int) flag_lm[idx] << endl;;

               if(flag_lm[idx] == 255 && clearFlag[idx] == 1 ) //only work on local maxima (and not cleared)
               {
                 //classify to see if it is a center               
                 prediction = classifyAVoxel(image, ch, iz, iy, ix, dimension_1d, pmodel); 
                                       
                 if(prediction == 0)
                    denied ++;
                  
                 candidateflag  = false;

                 if (prediction == 1 || candidateflag)
                 {
                   //cellcount ++;             
                   //results[idx] =  255; 
                   //
                  double ncx, ncy, ncz;
  
                  unsigned char * currentCube =getACube(image, ch, iz, iy, ix, dimension_1d, dimension_1d*dimension_1d, pagesz, channelsz); //fixed a bug 060210
                  curcoeff = coeff(currentCube, g_1d, cubesize);
 
                   //debug
                  if(ix == 65 && iy ==43 && iz ==14) 
                    std::cout << "coeff at valley:" << curcoeff << std::endl ;         
 
                  delete[] currentCube;
                   //if(curcoeff < 0.45)
                   //{
                    //  std::cout << "coeff too low for (" << ix << " " << iy << " " << iz << ") " << " coef: " << curcoeff << endl;                               
                    //  continue;
                   //}

                   //converge toward center of mass, 
                   
                   //move the center using highest score (either matching or classifier). 
                   /*
                   getMatchingCenter(image, ix, iy, iz, r, ch, ncx, ncy, ncz, flag_lm);
                   double nncx, nncy, nncz;
                   //getMassCenter(image, ncx, ncy, ncz, r, ch, nncx, nncy, nncz, flag_lm);
                   getMassCenter(filtered, sx,sy,sz, ncx, ncy, ncz, r, ch, nncx, nncy, nncz, flag_lm);
                   int newx = (int) (nncx+0.5), newy = (int) (nncy+0.5), newz = (int) (nncz+0.5); //rounding
                   */
                   
                   getMassCenter(filtered, sx,sy,sz, ix, iy, iz, r, ch, ncx, ncy, ncz, flag_lm);
                   int newx = (int) (ncx+0.5), newy = (int) (ncy+0.5), newz = (int) (ncz+0.5); //rounding
                   
                   //make a final decision if, after moving, there is a close neighbor identified already on the new spot
                   if (nearACenter(newx, newy, newz, results, r, sx, sy, sz))
                   {
                     // std::cout << "#" << cellcount << ", there's 1 nearby. Skip. " ;                   
                      continue;
                   }

                   long foundidx = newz*sx*sy + newy*sx + newx;
 
                   //check if move to a lm , possible for valley area between cells.
                   //why this made the debug image has 20 down to 9 cells!!
                   /*if ( flag_lm[foundidx] == 0)
                   {
                      std::cout << "non-lm. Skip. " ;                   
                      continue;
                   }*/
 
                   //else: a cell is found, set that to 255
                   results[foundidx] =  255; 

                   //remove foreground around that center from image: based on radius? reestimate based on std?
                   //int testt=0; for(int lll =0; lll < channelsz; lll++) if (flag_lm[lll] ==255) testt ++;
                   //cout << "postive flags: " << testt << endl;
                   //will not consider its neighbors in the future.
                   clearSurrounding(image, ch, newx, newy, newz, r, clearFlag);
                   //testt=0; for(int lll =0; lll < channelsz; lll++) if (flag_lm[lll] ==255) testt ++;
                   //cout << "postive flags after: " << testt << endl;

                   //increase cell counter by 1.
                   LocationSimple pp(newx, newy, newz);
                   detectedPos.push_back(pp);
                   LocationSimple marker(newx +1, newy +1, newz +1); //convert back to 1-base
                   marker.radius = r;
                   markerList.push_back(marker);               
                   
                   cellcount ++;
                   
                  }
                   
               }//end local maxima
          }//end ix
        }//end iy
     }//end iz
     
     std::cout << "total number of cells:" << cellcount << std::endl;
     
     
     char buf[30]; 
     itoa(cellcount, buf, 10);
     string resstring = "total cells:";
     resstring.append(buf);
     QMessageBox::information(0, "debug", QString(resstring.c_str()));
     
     std::cout << "total denied local maximum:" << denied << std::endl;
     
     svm_destroy_model(pmodel);
     
     /* //results only
     Image4DSimple p4DImage;
     p4DImage.setData(results, sx, sy, sz, 1, image->datatype);
     v3dhandle newwin = callback.newImageWindow();
     callback.setImage(newwin, &p4DImage);
     callback.setImageName(newwin,  "prediction results image");
     callback.updateImageWindow(newwin);
     */
     
     //v3d does not allow me to use the same image data in a different (new) window?
     //I need to make a copy of the data first?
     unsigned char *newimage1d = new unsigned char [channelsz]; 
     if(!newimage1d)  
     { 
        std::cout << "not enough memory to create result window";
        return results;
     } 
     unsigned char* image1d = image->getRawData();  
     for(long n = 0; n < channelsz; n++)
        newimage1d[n] = image1d[ch*channelsz + n];
     
     //show resulting cell markers  in a new window   
     Image4DSimple newImage;
     newImage.setData(newimage1d, sx, sy, sz, 1, image->datatype);
     v3dhandle newwin = callback.newImageWindow();
     callback.setImage(newwin, &newImage);
 	 callback.setImageName(newwin, "cell_counted");
	 callback.updateImageWindow(newwin);
	 callback.setLandmark(newwin, markerList); 


    //de-alloc
     //should I close svm file here???
     if(filtered)  { std::cout << "clean up memory for filtered image." << endl;  delete[] filtered; }
     if(flag_lm)   { std::cout << "clean up memory for local maximum." << endl;  delete[] flag_lm;  }   
     if(clearFlag) { std::cout << "clean up memory for flags." << endl;  delete[] clearFlag; }
     
     return results;
    
}

//make a decision for the given center location
int CellClassifier::classifyAVoxel(Image4DSimple* image, int ch, int iz,int iy,int ix, int dimension_1d, struct svm_model * pmodel)
{
     long sx=image->sz0, sy=image->sz1, sz=image->sz2; //, sc=image->sz3;
     long pagesz=sx*sy;
     long channelsz=sx*sy*sz;
     int dimension_2d = dimension_1d*dimension_1d;
     //get  a cube from image
     unsigned char * cube = getACube(image, ch, iz, iy, ix, dimension_1d, dimension_2d, pagesz, channelsz); 
     
     int res  = classifyACube(cube, dimension_1d, pmodel);

     //double prob[2];
     //int res  = classifyACubeWithProbability(cube, dimension_1d, pmodel, prob);
     //cout << "prob[0]:" << prob[0] << " prob[1]:" << prob[1] << " res: " << res << endl;
     
     delete[] cube;
     
     return res;
}


//test on an image cube given a model
int CellClassifier::classifyACube(unsigned char*  cube, int dimension_1d, struct svm_model * pmodel)
{
   CellFeatureExtractor * pex =  new CellFeatureExtractor(cube, 1, dimension_1d, 1, 0);
   float* features = pex->calcFeatures();
   int numOfFeatures = pex->getNumOfFeatures();
 
   //std::cout << "a cube after feature extraction:\t";
   // for(int i = 0; i < numOfFeatures; i++)
   //    std::cout << i << ":" << features[i] << " ";
   int res =  pclassifier->classifyASample(features, numOfFeatures, pmodel);
   
   //free up the features returned from extractor
   delete pex;
   delete [] features;
   return res;
}

//return probability estimation for all classes via parameter
int CellClassifier::classifyACubeWithProbability(unsigned char*  cube, int dimension_1d, struct svm_model * pmodel, double *probability)
{
   CellFeatureExtractor * pex =  new CellFeatureExtractor(cube, 1, dimension_1d, 1, 0);
   float* features = pex->calcFeatures();
   int numOfFeatures = pex->getNumOfFeatures();
 
   int res =  pclassifier->classifyASampleWithProbability(features, numOfFeatures, pmodel, probability);
   
   delete pex;
   delete [] features;
   return res;
}


//convert string targets to int. Store the converting list somewhere (string->int, int->string).
void CellClassifier::convertTargets(string *targets, int *res, int length)
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
string CellClassifier::lookUpTarget(int inttarget)
{
       return targetList.at(inttarget);
}


/****************************************************************************************************/
/********************  SUPPORTING SUBMETHODS ********************************************************/
/****************************************************************************************************/

bool nearACenter(int newx, int newy, int newz, unsigned char * results, int r, int sx, int sy, int sz)
{
     float rs = FINAL_CHECK_RATE*r;
      for(int l = newx - rs; l <= newx +rs ; l++)
      {
          if (l < 0 || l >= sx)  continue;
          for (int m = newy- rs; m <= newy + rs; m++)
          {
             if (m < 0 || m >= sy) continue;
             for (int n = newz - rs; n <= newz + rs; n++)
             {
                 if  (n < 0 || n>= sz) continue;
                 long idx = n*sx*sy + m*sx + l;
                 
                 if(results[idx] == 255)
                       return true;
             }
          }
       }
       return false;         
}


unsigned char* getACube(Image4DSimple* image, int ch, int iz,int iy,int ix, int dimension_1d, int dimension_2d, long pagesz, long channelsz)
{
     long sx = image->sz0;
     int r = dimension_1d/2;
     unsigned char * cube = new unsigned char[dimension_1d*dimension_1d*dimension_1d];
     unsigned char* image1d = image->getRawData();  

     //get cube from image
     int shiftx, shifty, shiftz, offset;
     long offsetk, offsetj;
     for(int n = iz-r; n <= iz+r; n++)
     {
          offsetk = n*pagesz;
          shiftz=n-(iz-r);
          for(int m = iy-r; m <= iy+r; m++)
          {
             offsetj = m*sx;
             shifty=m-(iy-r); 
             for(int l = ix-r; l<=ix+r; l++)
             {
                shiftx=l-(ix-r); 
                offset = shiftx+shifty*dimension_1d+shiftz*dimension_2d;    
                long idx = ch*channelsz + offsetk + offsetj + l;
                cube[offset] = image1d[idx];
             }
          }
     }
     return cube;
}


//pass in a pointer of 1 channel data, instead of passing in the image.
//provided so that a filtered image can be passed in directly
unsigned char * check_localMaxima_ker(unsigned char * image1d, long sx, long sy, long sz, int ch)
{
   long pagesz=sx*sy;
   long channelsz=sx*sy*sz;
         
   //get meanv for channel ch (threshold for foreground mask) 
   //maybe change to adapative thresholding after Gaussian filtering
   long sum = 0;
   int meanv; 
   for(long iz = 0; iz < sz; iz++)
   {
        long offsetk = iz*sx*sy;
        for(long iy = 0; iy < sy; iy++)
        {
            long offsetj = iy*sx;
            for(long ix = 0; ix < sx; ix++)
            {
                long idx = ch*channelsz + offsetk + offsetj + ix;
                sum += image1d[idx];
             }
        }
    }
    meanv = sum/channelsz;
    std::cout << "mean" << meanv <<std::endl; //usually a bit small since more areas are bg
    int iterationnum = 1; // find the valley in gray level historgram, keep small to avoid under segmentation
    for(int it =0; it < iterationnum; it++)
    {
      long sum1=0, num1=0;
      long sum2=0, num2=0;
      for(long iz = 0; iz < sz; iz++)
      {
        long offsetk = iz*sx*sy;
        for(long iy = 0; iy < sy; iy++)
        {
            long offsetj = iy*sx;
            for(long ix = 0; ix < sx; ix++)
            {
                long idx = ch*channelsz + offsetk + offsetj + ix;
                if (image1d[idx] > meanv)
                {  sum1 += image1d[idx]; num1++; }
                else
                {  sum2 += image1d[idx]; num2++;}
             }
        }
     }
     //adjust new threshold
     meanv = (int) (0.5*(sum1/num1+sum2/num2));
     std::cout << "mean adjusted:" << meanv <<std::endl;
    } 
    //calculate local maxima
    unsigned char *flag_lm = new unsigned char [channelsz]; //not pagesz 04042010
    if (!flag_lm)
    {
        printf("Fail to allocate memory.\n");
        return NULL;
    }
    else
    {
       //max filter
       double maxfl = 0;
       //can also change to adaptive local max filter
       unsigned int Wx=3, Wy=3, Wz=3;

       for(long iz = 0; iz < sz; iz++)
       {
            long offsetk = iz*sx*sy;
            for(long iy = 0; iy < sy; iy++)
            {
                long offsetj = iy*sx;
                for(long ix = 0; ix < sx; ix++)
                {
                    flag_lm[offsetk + offsetj + ix] = 0;

                    maxfl = 0; //minfl = INF;

                    long xb = ix-Wx; if(xb<0) xb = 0;
                    long xe = ix+Wx; if(xe>=sx-1) xe = sx-1;
                    long yb = iy-Wy; if(yb<0) yb = 0;
                    long ye = iy+Wy; if(ye>=sy-1) ye = sy-1;
                    long zb = iz-Wz; if(zb<0) zb = 0;
                    long ze = iz+Wz; if(ze>=sz-1) ze = sz-1;

                    for(long k=zb; k<=ze; k++)
                    {
                        long offsetkl = k*sx*sy;
                        for(long j=yb; j<=ye; j++)
                        {
                            long offsetjl = j*sx;
                            for(long i=xb; i<=xe; i++)
                            {
                                long dataval = image1d[ offsetkl + offsetjl + i];
                                if(maxfl<dataval) maxfl = dataval;
                            }
                      }
                   }

                    //set value:  replace the current block with local maximum (morphological dilation)
                    flag_lm[offsetk + offsetj + ix] = maxfl;
                }
            }
        }
    }

    long totallocalmax = 0;
    for(long iz = 0; iz < sz; iz++)
    {
        long offsetk = iz*sx*sy;
        for(long iy = 0; iy < sy; iy++)
        {
            long offsetj = iy*sx;
            for(long ix = 0; ix < sx; ix++)
            {
               long idx = offsetk + offsetj + ix;
               if( (flag_lm[idx] == image1d[idx]) && image1d[idx]>meanv ) //orignal value is local maxima
               {
                 flag_lm[idx] = 255;
                 totallocalmax ++;
               }
               else
                 flag_lm[idx] = 0;
          }
        }
     }
     
     
     std::cout <<"total local maxima : " << totallocalmax << std::endl;

     return flag_lm;

}


//the version that pass in Image4DSimple, and displays the result in a a window
unsigned char * check_localMaxima(V3DPluginCallback &callback, Image4DSimple* image, int ch)
{
   long sx=image->sz0, sy=image->sz1, sz=image->sz2; //, sc=image->sz3;
   //long pagesz=sx*sy;
   //long channelsz=sx*sy*sz;
   unsigned char* image1d = image->getRawData();

   unsigned char* result = check_localMaxima_ker(image1d, sx, sy, sz, ch);
     
   Image4DSimple p4DImage;
   p4DImage.setData(result, sx, sy, sz, 1, image->datatype);
   v3dhandle newwin = callback.newImageWindow();
   callback.setImage(newwin, &p4DImage);
   callback.setImageName(newwin,  "local maximum image");
   callback.updateImageWindow(newwin);
     
    return result;
}


//start from ix, iy, iz, until converge to a stable center of mass
// If in valley, will not move to high lands. 
//
void getMassCenter(unsigned char* image1d, long sx, long sy, long sz, int ix, int iy, int iz, int r, int ch, double& ncx, double& ncy, double& ncz, unsigned char * flag_lm)
//void getMassCenter(Image4DSimple* image, int ix, int iy, int iz, int r, int ch, double& ncx, double& ncy, double& ncz, unsigned char * flag_lm)
{
     //long sx=image->sz0, sy=image->sz1, sz=image->sz2; 
     //unsigned char* image1d = image->getRawData();


     //searching radius is the same for 3 coordiates for now.
     //float searchMagRate = 1.2;
 	 double rx_ms = searchMagRate*r+ 0.5, ry_ms = searchMagRate*r+ 0.5 , rz_ms = searchMagRate*r+ 0.5;
 	 int rx = r, ry = r, rz = r;
     double scx=0,scy=0,scz=0,si=0;
	 double ocx,ocy,ocz; //old center position
     
     int k1, j1, i1;
     long pagesz=sx*sy;
     long channelsz=sx*sy*sz;
     long offsetk, offsetj, idx;

     ncx=ix,ncy=iy,ncz=iz; //new center position
	 
     //flag_p3d[k][j][i] = 1; //do not search later
	 while (1) //mean shift to estimate the true center
	 {
			ocx=ncx; ocy=ncy; ocz=ncz;

			//rx_ms = 1.2*rx + 0.5, ry_ms = 1.2*ry + 0.5, rz_ms = 1.2*rz + 0.5; //enlarge the radius    
			for (k1=ocz-rz_ms;k1<=ocz+rz_ms;k1++)
			{
 			   if (k1<0 || k1>=sz)  continue;
               offsetk = k1*pagesz;
			   for (j1=ocy-ry_ms;j1<=ocy+ry_ms;j1++)
			   {
						if (j1<0 || j1>=sy)	continue;
                        offsetj = j1*sx;
						for (i1=ocx-rx_ms;i1<=ocx+rx_ms;i1++)
						{
							if (i1<0 || i1>=sx)		continue;
                            idx = ch*channelsz + offsetk + offsetj + i1;
							double cv = image1d[idx];

							//if(cv<shift_thresh) cv=0; /////////////////////////////////////////
                        	scz += k1*cv;
							scy += j1*cv;
							scx += i1*cv;
							si += cv;
						}
				}
			}
			if (si>0)
				{ncx = scx/si; ncy = scy/si; ncz = scz/si;}
			else
				{ncx = ocx; ncy = ocy; ncz = ocz;}

			if (ncx<rx || ncx>=sx-1-rx || ncy<ry || ncy>=sy-1-ry || ncz<rz || ncz>=sz-1-rz) //move out of boundary
			{
				ncx = ocx; ncy = ocy; ncz = ocz; //use the last valid center
				break;
			}
			
			/*
			int incx = (int) (ncx+0.5), incy = (int) (ncy+0.5), incz = (int) (ncz+0.5);
			int iocx = (int) (ocx+0.5), iocy = (int) (ocy+0.5), iocz = (int) (ocz+0.5);
			if ((!flag_lm[(int)incz*sx*sy+incy*sx+incx]) && (flag_lm[iocz*sx*sy+iocy*sx+iocx] == 255)) //06122010
            { //if from an lm to a non-lm, use the last one.
              //should not happen if processed ? (Gaussian, Distance transform) or the classifier only detect close to center.
              //ideally, find a one close to center, then move to center (all lm).
              ncx = ocx; ncy = ocy; ncz = ocz; //use the last valid center
               break;
              //problem: one cell may be recognized as 2 due to 2 sections of lm in the cell. 
               
            }*/
			
			//stop when the difference between old center and new center is small enough
			if (sqrt((ncx-ocx)*(ncx-ocx)+(ncy-ocy)*(ncy-ocy)+(ncz-ocz)*(ncz-ocz))<=1)
			{
				cout << "cell mass center found: "<< (int) (ncx+0.5) << " " << (int) (ncy+0.5) <<  " " << (int) (ncz+0.5) << " " << "(moved from " << ix << " " << iy << " " << iz <<  ")" <<  endl;
				break;
			}
		}//end of search for mass center
}

//
//set the  ball (cube?) area to 0 so that it would not be considered as cell center again
//
void  clearSurrounding(Image4DSimple* image, int ch, int x, int y, int z, int r, unsigned char* clearflag)
{
   
    long i,j,k;
    long sx=image->sz0, sy=image->sz1, sz=image->sz2; //, sc=image->sz3;
    long pagesz=sx*sy;
    //long channelsz=sx*sy*sz;
    unsigned char* image1d = image->getRawData();
 
    // expand r? r = r*1.5; ?? (2*std?  estimate std first??)s???
    // cube:
    r = CLEAR_EXPAND_RATE * r;  
    int offset, offset2; 
    for( i = x-r; i < x+r; i++)
       for(j = y-r; j < y+r; j++)        
         for(k = z-r; k < z+r; k++)        
         {
           if (i < 0 || i >= sx || j < 0 || j >= sy || k <0 || k >=sz) 
                           continue; //check boundary
           offset = k*pagesz + j*sx + i;
           clearflag[offset] = 0;
         }  
}


//
//  move from one location to another location in surrounding with higher matching score
//
void getMatchingCenter(Image4DSimple* image, int ix, int iy, int iz, int r, int ch, double& ncx, double& ncy, double& ncz, unsigned char * flag_lm)
{
     //searching radius currently is the same for x, y, z (modify later).
 	 double rx_ms = r, ry_ms = r , rz_ms = r;
 	 int rx = r, ry = r, rz = r;
     
     int k1, j1, i1;
     long sx=image->sz0, sy=image->sz1, sz=image->sz2; //, sc=image->sz3;
     long pagesz=sx*sy;
     long channelsz=sx*sy*sz;
 
     ncx=ix,ncy=iy,ncz=iz; //init the new center positions
	 double ocx,ocy,ocz; //old center position for searching
	 
	 long dimension_1d = 2*r + 1;
	 long cubesize = dimension_1d*dimension_1d*dimension_1d;
     double sigmax = (dimension_1d-1)/4.0, sigmay = (dimension_1d-1)/4.0, sigmaz = (dimension_1d-1)/4.0;
     double *g_1d = genGaussianKernel1D(dimension_1d, dimension_1d, dimension_1d, sigmax, sigmay, sigmaz);

     double maxcoeff = 0, curcoeff;
     
     unsigned char * currentCube =getACube(image, ch, iz, iy, ix, dimension_1d, dimension_1d*dimension_1d, pagesz, channelsz); //fixed a bug 060210
     maxcoeff = coeff(currentCube, g_1d, cubesize);

	 while (1) //mean shift to estimate the true center
	 {
			ocx=ncx; ocy=ncy; ocz=ncz;
			for (k1=ocz-rz_ms;k1<=ocz+rz_ms;k1++)
			{
 			   if (k1<r || k1>=sz-r)  continue;
  			   for (j1=ocy-ry_ms;j1<=ocy+ry_ms;j1++)
			   {
						if (j1<r || j1>=sy-r)	continue;
  						for (i1=ocx-rx_ms;i1<=ocx+rx_ms;i1++)
						{
							if (i1<r || i1>=sx-r)		continue;
	
    						//should be local max
							//if (!flag_lm[k1*sx*sy+j1*sx+i1]) continue;
							
                            unsigned char * currentCube =getACube(image, ch, k1, j1, i1, dimension_1d, dimension_1d*dimension_1d, pagesz, channelsz); //bug fixed

							//can be replaced by classification confidence score here for better accuracy
                            
                            /* //need to pass in pmodel and current instance of CellClassifier
                            double probability[2];
                            thiscellclassifier->classifyACubeWithProbability(currentCube, dimension_1d, pmodel, probability);
                            curcoeff = (float)probability[0];
                            */

                            curcoeff = coeff(currentCube, g_1d, cubesize);
                            
                            if (curcoeff > maxcoeff)
                            {  //std::cout << " bigger:"  << curcoeff;
                               maxcoeff = curcoeff;
                               ncx = i1;
                               ncy = j1;
                               ncz = k1;
                            }
                            delete[] currentCube;
						}
				}
			}//end of searching for max
            
			if (ncx<rx || ncx>=sx-1-rx || ncy<ry || ncy>=sy-1-ry || ncz<rz || ncz>=sz-1-rz) //move out of boundary
			{
				ncx = ocx; ncy = ocy; ncz = ocz; //use the last valid center
				break;
			}
			//stop when the difference between old center and new center is small enough
			if (sqrt((ncx-ocx)*(ncx-ocx)+(ncy-ocy)*(ncy-ocy)+(ncz-ocz)*(ncz-ocz))<=1)
			{
				cout << "higher score found: "<< (int) ncx << " " << (int) ncy <<  " " << (int) ncz << " " << "old:" << ix << " " << iy << " " << iz <<  endl;
				break;
			}
		}//end of search for mass center

}


//get a smoothed image,  for center of mass,  revised from Yang Yu's version
// gaussian filtering 
// sx sy sz: length of image on x,y,z direction
// Wx Wy Wz: filtering window radius
// return: filtered image
unsigned char * gaussianfiltering(unsigned char *data1d, long sx, long sy, long sz, int Wx, int Wy, int Wz)
{
	//filtering
	long N=sx, M=sy, P=sz, pagesz=N*M*P;
	double max_val=0, min_val=INF;

	//declare temporary pointer
	float *pImage = new float [pagesz];
	if (!pImage)
	{
		printf("Fail to allocate memory.\n");
		return NULL;
	}
	else
	{
		for(long i=0; i<pagesz; i++)
			pImage[i] = data1d[i];  //first channel data (red in V3D, green in ImageJ)
	}

	//Filtering
	//
	//   Filtering along x
	if(N<2)
	{
		//do nothing
	}
	else
	{
		//create Gaussian kernel
		float  *WeightsX = 0;
		WeightsX = new float [Wx];
		if (!WeightsX)
			return NULL;

		unsigned int Half = Wx >> 1;
		WeightsX[Half] = 1.;

		for (unsigned int Weight = 1; Weight < Half + 1; ++Weight)
		{
			const float  x = 3.* float (Weight) / float (Half);
			WeightsX[Half - Weight] = WeightsX[Half + Weight] = exp(-x * x / 2.);	// Corresponding symmetric WeightsX
		}

		double k = 0.;
		for (unsigned int Weight = 0; Weight < Wx; ++Weight)
			k += WeightsX[Weight];

		for (unsigned int Weight = 0; Weight < Wx; ++Weight)
			WeightsX[Weight] /= k;


		//   Allocate 1-D extension array
		float  *extension_bufferX = 0;
		extension_bufferX = new float [N + (Wx<<1)];

		unsigned int offset = Wx>>1;

		//	along x
		const float  *extStop = extension_bufferX + N + offset;

		for(long iz = 0; iz < P; iz++)
		{
			for(long iy = 0; iy < M; iy++)
			{
				float  *extIter = extension_bufferX + Wx;
				for(long ix = 0; ix < N; ix++)
				{
					*(extIter++) = pImage[iz*M*N + iy*N + ix];
				}

				//   Extend image
				const float  *const stop_line = extension_bufferX - 1;
				float  *extLeft = extension_bufferX + Wx - 1;
				const float  *arrLeft = extLeft + 2;
				float  *extRight = extLeft + N + 1;
				const float  *arrRight = extRight - 2;

				while (extLeft > stop_line)
				{
					*(extLeft--) = *(arrLeft++);
					*(extRight++) = *(arrRight--);
				}

				//	Filtering
				extIter = extension_bufferX + offset;

				float  *resIter = &(pImage[iz*M*N + iy*N]);

				while (extIter < extStop)
				{
					double sum = 0.;
					const float  *weightIter = WeightsX;
					const float  *const End = WeightsX + Wx;
					const float * arrIter = extIter;
					while (weightIter < End)
						sum += *(weightIter++) * float (*(arrIter++));
					extIter++;
					*(resIter++) = sum;

					//for rescale
					if(max_val<*arrIter) max_val = *arrIter;
					if(min_val>*arrIter) min_val = *arrIter;
				}
			}
		}

		//de-alloc
		if (WeightsX) {delete []WeightsX; WeightsX=0;}
		if (extension_bufferX) {delete []extension_bufferX; extension_bufferX=0;}

	}

	//   Filtering along y
	if(M<2)
	{
		//do nothing
	}
	else
	{
		//create Gaussian kernel
		float  *WeightsY = 0;
		WeightsY = new float [Wy];
		if (!WeightsY)
			return NULL;

		unsigned int Half = Wy >> 1;
		WeightsY[Half] = 1.;

		for (unsigned int Weight = 1; Weight < Half + 1; ++Weight)
		{
			const float  y = 3.* float (Weight) / float (Half);
			WeightsY[Half - Weight] = WeightsY[Half + Weight] = exp(-y * y / 2.);	// Corresponding symmetric WeightsY
		}

		double k = 0.;
		for (unsigned int Weight = 0; Weight < Wy; ++Weight)
			k += WeightsY[Weight];

		for (unsigned int Weight = 0; Weight < Wy; ++Weight)
			WeightsY[Weight] /= k;

		//	along y
		float  *extension_bufferY = 0;
		extension_bufferY = new float [M + (Wy<<1)];

		unsigned int offset = Wy>>1;
		const float *extStop = extension_bufferY + M + offset;

		for(long iz = 0; iz < P; iz++)
		{
			for(long ix = 0; ix < N; ix++)
			{
				float  *extIter = extension_bufferY + Wy;
				for(long iy = 0; iy < M; iy++)
				{
					*(extIter++) = pImage[iz*M*N + iy*N + ix];
				}

				//   Extend image
				const float  *const stop_line = extension_bufferY - 1;
				float  *extLeft = extension_bufferY + Wy - 1;
				const float  *arrLeft = extLeft + 2;
				float  *extRight = extLeft + M + 1;
				const float  *arrRight = extRight - 2;

				while (extLeft > stop_line)
				{
					*(extLeft--) = *(arrLeft++);
					*(extRight++) = *(arrRight--);
				}

				//	Filtering
				extIter = extension_bufferY + offset;

				float  *resIter = &(pImage[iz*M*N + ix]);

				while (extIter < extStop)
				{
					double sum = 0.;
					const float  *weightIter = WeightsY;
					const float  *const End = WeightsY + Wy;
					const float * arrIter = extIter;
					while (weightIter < End)
						sum += *(weightIter++) * float (*(arrIter++));
					extIter++;
					*resIter = sum;
					resIter += N;

					//for rescale
					if(max_val<*arrIter) max_val = *arrIter;
					if(min_val>*arrIter) min_val = *arrIter;
				}
			}
		}

		//de-alloc
		if (WeightsY) {delete []WeightsY; WeightsY=0;}
		if (extension_bufferY) {delete []extension_bufferY; extension_bufferY=0;}
	}

	//  Filtering  along z
	if(P<2)
	{
		//do nothing
	}
	else
	{
		//create Gaussian kernel
		float  *WeightsZ = 0;
		WeightsZ = new float [Wz];
		if (!WeightsZ)
			return NULL;

		unsigned int Half = Wz >> 1;
		WeightsZ[Half] = 1.;

		for (unsigned int Weight = 1; Weight < Half + 1; ++Weight)
		{
			const float  z = 3.* float (Weight) / float (Half);
			WeightsZ[Half - Weight] = WeightsZ[Half + Weight] = exp(-z * z / 2.);	// Corresponding symmetric WeightsZ
		}

		double k = 0.;
		for (unsigned int Weight = 0; Weight < Wz; ++Weight)
			k += WeightsZ[Weight];

		for (unsigned int Weight = 0; Weight < Wz; ++Weight)
			WeightsZ[Weight] /= k;


		//	along z
		float  *extension_bufferZ = 0;
		extension_bufferZ = new float [P + (Wz<<1)];

		unsigned int offset = Wz>>1;
		const float *extStop = extension_bufferZ + P + offset;

		for(long iy = 0; iy < M; iy++)
		{
			for(long ix = 0; ix < N; ix++)
			{

				float  *extIter = extension_bufferZ + Wz;
				for(long iz = 0; iz < P; iz++)
				{
					*(extIter++) = pImage[iz*M*N + iy*N + ix];
				}

				//   Extend image
				const float  *const stop_line = extension_bufferZ - 1;
				float  *extLeft = extension_bufferZ + Wz - 1;
				const float  *arrLeft = extLeft + 2;
				float  *extRight = extLeft + P + 1;
				const float  *arrRight = extRight - 2;

				while (extLeft > stop_line)
				{
					*(extLeft--) = *(arrLeft++);
					*(extRight++) = *(arrRight--);
				}

				//	Filtering
				extIter = extension_bufferZ + offset;

				float  *resIter = &(pImage[iy*N + ix]);

				while (extIter < extStop)
				{
					double sum = 0.;
					const float  *weightIter = WeightsZ;
					const float  *const End = WeightsZ + Wz;
					const float * arrIter = extIter;
					while (weightIter < End)
						sum += *(weightIter++) * float (*(arrIter++));
					extIter++;
					*resIter = sum;
					resIter += M*N;

					//for rescale
					if(max_val<*arrIter) max_val = *arrIter;
					if(min_val>*arrIter) min_val = *arrIter;
				}

			}
		}

		//de-alloc
		if (WeightsZ) {delete []WeightsZ; WeightsZ=0;}
		if (extension_bufferZ) {delete []extension_bufferZ; extension_bufferZ=0;}

	}

	//rescaling for display
	unsigned char *filteredresult = new unsigned char [pagesz];
	if (!filteredresult)
	  return NULL;
	
	
	float dist = max_val - min_val;
	for(long k=0; k<P; k++)
	{
		long offsetk = k*M*N;
		for(long j=0; j<M; j++)
		{
			long offsetj = j*N;
			for(long i=0; i<N; i++)
			{
				long indLoop = offsetk + offsetj + i;
				//data1d[indLoop] = 255*(pImage[indLoop]-min_val)/(dist);
				filteredresult[indLoop] = 255*(pImage[indLoop]-min_val)/(dist);
			}
		}
	}

	//de-alloc
	if (pImage) {delete []pImage; pImage=0;}
	
    return filteredresult;

}

