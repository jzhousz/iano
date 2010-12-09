/* Do cell  counting based on ROI classification
 * March 2010 Jie Zhou
 */

#include <QtGui>

#include <string>
#include <exception>
#include <iostream>
#include <algorithm>

#include "ROICellCounter.h"
#include "CellClassifier.h"

using namespace std;

/*

1. smoothing, enhancement (Gaussian filter)
2. get a foreground mask.
3. get a list of local maximum
----------
Searching for cells is done based on local maxima:









 Functionality design: get a list of V3D images with tagged (labelled) landmarks. (Annotated Images with Tagged Landmarks.)
 Also get an image(s) to be annotated.

 User specify at plugin gui:  specific images to work on?, parameters for annotation such as grid/step, channel to work with:

 1.get the landmark areas from annotated images.
 2.Train on the annotated images.
 3.Apply to the image to annotated.
 4.Generate a mask image.
 5. display the mask image: (V3D can then transform it into surface image in GUI)

*/


//Q_EXPORT_PLUGIN2 ( PluginName, ClassName )
//The value of PluginName should correspond to the TARGET specified in the plugin's project file.
Q_EXPORT_PLUGIN2(ROICellCounter, ROICellcounterPlugin)

int annotation(V3DPluginCallback &callback, QWidget *parent);
void getImageCubesGivenMarks(V3DPluginCallback &callback, v3dhandle win, LandmarkList list, int channel, string targets[], unsigned char * cubes, int dimension, int& num);
void check_localMaxima(V3DPluginCallback &callback, Image4DSimple* image, int ch);
int getROIDimension(LandmarkList list);

const QString title = "ROI Automatic Cell Counter";
QStringList ROICellcounterPlugin::menulist() const
{
    return QStringList() << tr("ROI Automatic Cellcounter")
						 << tr("about this plugin");
}

void ROICellcounterPlugin ::domenu(const QString &menu_name, V3DPluginCallback &callback, QWidget *parent)
{
	if (menu_name == tr("ROI Automatic Cellcounter"))
    {
    	annotation(callback, parent);
    }
	else if (menu_name == tr("about this plugin"))
	{
		QMessageBox::information(parent, "Version info", "3D Image Automatic Annotation March 2010 Jie Zhou. ");
	}

}

int annotation(V3DPluginCallback &callback, QWidget *parent)
{
    std::cout << "annotation started ..." ;

	bool ok1, ok2;
    QStringList items;
    QVector<QString> markedImageNames(1);
    QString markedImageName;
    int  channel = 1; //default: red
    v3dhandle win = NULL;
	v3dhandleList winList = callback.getImageWindowList();
     
    for(int winIndex =0;  winIndex < winList.size(); winIndex ++)
	   items << QObject::tr(callback.getImageName(winList.at(winIndex)).toAscii());
    QMessageBox msgBox;
    msgBox.setText("Do you want to pick another annotated image?");
    msgBox.setStandardButtons(QMessageBox::Yes | QMessageBox::No);
    msgBox.setDefaultButton(QMessageBox::No);

    int number = 0;
    //a loop to get multiple images.
    while(true)
    {
     markedImageName = QInputDialog::getItem(parent, QObject::tr("Pick an Annotated Image"),
               QObject::tr("Which image has annotated landmaks?"), items, 0, false, &ok1);
     if(ok1)
     {
          markedImageNames.append(markedImageName); 
          number ++;
          int ret = msgBox.exec();
          if (ret == QMessageBox::No) 
             break;
     }
	 else
	 {
        if (number ==0) //didn't choose anything
 		   return -1;
		else
            break;
     }

    }

	channel = QInputDialog::getInteger(parent, QObject::tr("Set channel: "),
										  QObject::tr("Which channel should the annotation be based on?"),
										  1, 1, 3, 1, &ok2);

    //get the landmark areas from annotated images.
    //iterate through the list to find out images with specified training images.
    //assume only annotate one image for now. Otherwise append to the list, with corresponding wins and #of markers per win.
    LandmarkList list;
    int ROIDimension = -1; 
    int numberCubes = 0;
    int totalListSize = 0; 

    //go through once to get total size
    for(int winIndex =0;  winIndex < winList.size(); winIndex ++)
    {
       win = winList.at(winIndex);
       //nested to go through multiple images
       for(int markedIndex = 0; markedIndex < markedImageNames.size(); markedIndex++)
       {
        if(callback.getImageName(win) == markedImageNames.at(markedIndex))
        {
          //get landmarklist, store in a structure for trainig
          list = callback.getLandmark(win);
          if (list.size()==0) 
          { QMessageBox::information(0, "Cellcounter", QObject::tr("No landmarks were defined on the selected image.")); return -3;}
          else
          { if(ROIDimension == -1) //never set before
              ROIDimension = getROIDimension(list); //ROIDim will be set as radius * 2.
            totalListSize += list.size();  
          }       
	   } //end of inding a match
      }//end for markedIndex
    }
    
    //allocate memory
    unsigned char *trainingCubes;
    long cubesz = (ROIDimension+1)*(ROIDimension+1)*(ROIDimension+1);
    long CubeN =  cubesz*totalListSize;
	trainingCubes = new unsigned char[CubeN];
    if (!trainingCubes)  
    {
		QMessageBox::information(0, title, QObject::tr("Not enough memory for training cubes."));
        return NULL;
    }
    string targets[totalListSize]; //init

    //go through twince to get all the training cubes;
    int currentCubeIndex = 0;
    int totalnumberCubes;
    for(int winIndex =0;  winIndex < winList.size(); winIndex ++)
    {
       win = winList.at(winIndex);
       //nested to go through multiple images
       for(int markedIndex = 0; markedIndex < markedImageNames.size(); markedIndex++)
       {
        if(callback.getImageName(win) == markedImageNames.at(markedIndex))
        {
          //get landmarklist, store in a structure for trainig
          list = callback.getLandmark(win);
          getImageCubesGivenMarks(callback, win, list, channel, targets+currentCubeIndex, trainingCubes+currentCubeIndex*cubesz, ROIDimension, numberCubes);
          currentCubeIndex += numberCubes;
        }//match  
       }//nested for markedIndex
     }//winIndex
     totalnumberCubes = currentCubeIndex;
    
    //for debug
    //for (int i=0; i< totalnumberCubes ; i++)
    //     std::cout<<"!!!!target of cube:"+targets[i];

    cout << "totalnumberCubes: " <<totalnumberCubes;     

    //debug: check passed in pixel value
    //std::cout << "second cube:\t";
    //int dimension_1d = ROIDimension+1;
    //for(int i = 1*dimension_1d*dimension_1d*dimension_1d; i < 2*dimension_1d*dimension_1d*dimension_1d; i++)
    //   std::cout << i << ":" << (int) trainingCubes[i] << " ";

    //2.Train on the annotated images.
    //get a set of arguments from GUI: algorithm selection, algorithm parameter selection
    //be simple, just use default algorithm for now: wavelet + SVM. (later can pass to constructor)
    CellClassifier classifier;
    classifier.trainingWithmarks(trainingCubes, targets, totalnumberCubes, ROIDimension+1);

    //training cubes are not useful at this point. may be deleted to say memory
    std::cout << "traning is done. free up training cubes' memory ... " << std::endl;
    delete trainingCubes;
	
	
    //3. annotation
	//let user choose an image to annotate
    QString testImageName;
    int grid;
    int grid_max = 30;
    //for(int winIndex =0;  winIndex < winList.size(); winIndex ++)
	 //  items << QObject::tr(callback.getImageName(winList.at(winIndex)).toAscii());
    testImageName = QInputDialog::getItem(parent, QObject::tr("Pick an image needs cell counting"),
               QObject::tr("Which image is to be cell-counted?"), items, 0, false, &ok1);
	
    if (!ok1)
        return -1;

    for(int winIndex =0;  winIndex < winList.size(); winIndex ++)
    {
       win = winList.at(winIndex);
       if(callback.getImageName(win) == testImageName)
 		   break;
    }
	Image4DSimple* tImage = callback.getImage(win);

    //channel-1: 0-2,
	unsigned char* predictions = classifier.annotateAnImage(callback, win, tImage, channel-1, grid, ROIDimension+1);
	std::cout << "Image annotation done.. "  << std::endl;
    
    
    /*
	//4. generate a mask image based on the returned predictions
   //generate it: based on pixel label: 4D value with density being the prediciton 1,2,3 etc.
   //areas (boundaries) without predictions are set to background.
   long N = tImage->getTotalBytes();
   long sx=tImage->sz0, sy=tImage->sz1, sz=tImage->sz2, sc=tImage->sz3;
   long pagesz=sx*sy;
   long channelsz=sx*sy*sz;

   std::cout << "trying to get memory for the new annoated image.. "  << std::endl;
   unsigned char* data1d = new unsigned char[N];
   if (data1d)
	   std::cout << "Obtained memory for the new annoated image.. "  << std::endl;

   int ind = 0;
   int r = ROIDimension/2;
   //same loop as in annotating an image in ROIClassifier.cpp
   int marksz = grid/2;
   int offset;
   for(int xx = r; xx < sx - r; xx +=grid)
	 for(int yy = r; yy < sy - r; yy +=grid)
		for(int zz = r; zz < sz - r; zz +=grid)
		{
			 //xx,yy,zz IS THE CENTER
			 //set surrounding pixels to the same value based on grid size? ...
			 if(predictions[ind]!=0)
			   for(int l = xx-marksz; l<=xx+marksz; l++)
				 for(int m = yy-marksz; m<=yy+marksz; m++)
				   for(int n = zz-marksz; n<=zz+marksz; n++)
				   {
					 offset = l + m*sx + n* pagesz + (channel-1)*channelsz;
					 data1d[offset] = predictions[ind];
				   }
			 else  //bg, i.e. 0; actually don't need to do anything??
			 {
				 offset = xx + yy*sx + zz* pagesz + (channel-1)*channelsz;
				 data1d[offset] = predictions[ind];
			 }

			 ind++;
    }

   Image4DSimple p4DImage;
   p4DImage.setData(data1d, sx, sy, sz, sc, tImage->datatype);
   v3dhandle newwin = callback.newImageWindow();
   callback.setImage(newwin, &p4DImage);
   callback.setImageName(newwin,  "annoated mask image");
   callback.updateImageWindow(newwin);
   */
   
   delete [] predictions;
   return 0;
}


/****************************   Sub methods *********************************************************/
//get ROI dimension based on a list of markers
/***************************************************************************************************/
int getROIDimension(LandmarkList list)
{
    
	//check all the marks, find the smallest mark radius.
	//itearte through the list ...
    int currentDim;
    int ROIDimension;
    ROIDimension = -1;
    for(int i = 0; i< list.size(); i++)
    {
       currentDim = (int) list.at(i).radius*2; //even
       if (ROIDimension == -1 || currentDim < ROIDimension)
          ROIDimension = currentDim;
    }
    std::cout << "ROIDimension of the cubes (actually size is (ROIDimension+1)^3): " << ROIDimension;
    
    return ROIDimension;    
}

/****************************   Sub methods *********************************************************/
//get image cubes, and pass back via parameter. Also pass back the number of valid cubes
/***************************************************************************************************/
void getImageCubesGivenMarks(V3DPluginCallback &callback, v3dhandle win, LandmarkList list, int channel, string targets[], unsigned char * cubes, int  ROIDimension, int &numCubes)
{
 	Image4DSimple* image = callback.getImage(win);
	if (!image)
	{
		QMessageBox::information(0, title, QObject::tr("No such image that is open."));
		return;
	}
	if (image->getDatatype()!=V3D_UINT8)
	{
		QMessageBox::information(0, title, QObject::tr("This program only supports 8-bit data. Your current image data type is not supported."));
		return;
	}


    //get raw data
	long sx=image->sz0, sy=image->sz1, sz=image->sz2, sc=image->sz3;
	long pagesz=sx*sy;
	long channelsz=sx*sy*sz;
	unsigned char* image1d = image->getRawData();  //Q: how is it stored?? x, then y, then z? then channel?

    long cubesz = (ROIDimension+1)*(ROIDimension+1)*(ROIDimension+1);
    long oneCubeN = (ROIDimension+1)*(ROIDimension+1)*(ROIDimension+1)*sc;

    long  ind = 0;
    int   ch = channel-1; //index from 0 to 2.
    for(int i = 0; i< list.size(); i++)
    {
        //for display/debug 1
    	//unsigned char* oneCube = new unsigned char[oneCubeN];

        int xx, yy, zz;
        ((LocationSimple)list.at(i)).getCoord(xx, yy, zz);
  
        //covert from 1-based (for display) to 0-based (for computation)
        xx = xx-1; yy = yy-1; zz = zz-1;
        //get the cube surrounding xx,yy,zz.  ROIDimension is always an even number;
        int r = ROIDimension/2;
        //starting at xx-r,  yy-r, zz-r to, xx+r, yy+r, zz+r.
        //check boundary
        if(xx-r <0||yy-r <0 || zz-r <0 || xx+r >= sx ||yy+r >= sy || zz+r >= sz)
        {
                std::cout << "skip a marker close to boundary." << std::endl;
                continue;
        }
       for(int l = xx-r; l<=xx+r; l++)
                for(int m = yy-r; m<=yy+r; m++)
                        for(int n = zz-r; n<=zz+r; n++)
                        {
                           int shiftx=l-(xx-r),shifty=m-(yy-r), shiftz=n-(zz-r);
                           int offset = shiftx+shifty*(ROIDimension+1)+shiftz*(ROIDimension+1)*(ROIDimension+1);
                           
                           //std::cout << " image1d[l+m*sx+n*pagesz+ch*channelsz]:" << (int) image1d[l+m*sx+n*pagesz+ch*channelsz];
                           
                           cubes[offset+ind*cubesz] = image1d[l+m*sx+n*pagesz+ch*channelsz];
                           
                           targets[ind] =((LocationSimple)list.at(i)).comments;
                           
                           //for display/debug 2
                           //int offset2 = offset+ch*(ROIDimension+1)*(ROIDimension+1)*(ROIDimension+1);
                           //oneCube[offset2] = image1d[l+m*sx+n*pagesz+ch*channelsz];
                        }
         ind ++;  //done with one cube

        //for display/debug: 3 show the little one cube (One the selected channel has meaningful data, others are random.
        /*
       	Image4DSimple p4DImage;
    	p4DImage.setData(oneCube, ROIDimension+1,ROIDimension+1,ROIDimension+1,sc, image->datatype);
    	v3dhandle newwin = callback.newImageWindow();
    	callback.setImage(newwin, &p4DImage);
    	callback.setImageName(newwin,  "cube");
    	callback.updateImageWindow(newwin);
    	*/
       //delete oneCube;  //will be freed when the window is closed

    }

	//Important: The number of valid of cubes may be smaller than list size due to boundary effect.
    numCubes = ind;
    if(numCubes ==0)
	{
		QMessageBox::information(0, title, QObject::tr("No valid landmarks (Maybe too close to boundary)."));
		return;
	}

    return;

}

