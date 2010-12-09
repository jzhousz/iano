//=========================================================
//
// The JNI native methods for calling mRMR in Java
// written by Jie Zhou.
//
//Disclaimer: The author of interface methods is Jie Zhou
//
//The CopyRight is reserved by the author.
//
//Last modification: June/19/2008
//Last update: 081012. by Hanchuan Peng, add MID
//
//
// make -f  javamrmr.makefile
//

#include <iostream>
using namespace std;
#include "annotool_select_mRMRNative.h"
#include "mrmr.cpp"

/***************  Native C Functions Used by Java Interface *************************/
#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT jintArray JNICALL Java_annotool_select_mRMRNative_miq
  (JNIEnv *env, jclass cl, jfloatArray features, jintArray target, jint nfeature, jint nsample, jint nvar)
{
    //Be aware of type compatibility:
    //On windows: jint is long (32 bits), jlong is long long (cygwin) or __int64 (ms) (64 bits)
    cout << " \nIn Native C++ mRMR funciton MIQ ..." << endl;

    //setup the DataTable and its members
    //whether the JVM acutally copies the array depends on how it allocates arrays and does its gc.
    DataTable *myData = new DataTable;
    jboolean isCopyFeature;
    jboolean isCopyLabel;
    myData->data = env->GetFloatArrayElements(features, &isCopyFeature);

    /* cout << "data table in C++" << endl;
    for(int i = 0; i < nsample; i++)
    {
      for(int j = 1; j < nvar ; j++)
		  cout <<  myData->data[i*nvar+j] << " ";
       cout << endl;
    }*/
    //typecast from 32 bit long (jint) to 16 bit int.
    //myData->classLabel = (int *) env->GetIntArrayElements(target, &isCopyLabel);
    //myData->variableNames = ... //not used here

    //Other fields
    myData->nsample = nsample;
    myData->nvar = nvar;

    //build 2D array which is needed by the mRMR_selection()
    //myData->buildData2d();
	int t_discretize=9999; //now all always assume Java has discrtized the data
	//int t_discretize=0;
	if (long (t_discretize) == 9999)
		myData->discretize (t_discretize, 0);	//do not do discretization
	else
		myData->discretize (t_discretize, 1);	//do discretization

    //now call mRMR_selection()
    //On Windows, long is 32 bit long, same as 32 bit jint.
    //Typecasting may be necessary on other platforms with different format of long.
    jint *feaInd = (jint*) mRMR_selection (myData, nfeature, MIQ);

    //pass back the index array.
    jintArray indexarray = env->NewIntArray(nfeature);
    env->SetIntArrayRegion(indexarray, 0, (jsize) nfeature, feaInd);

     //memory management
    //"delete myData" may also free the original array passed in.
    if(isCopyFeature) {
      cout << "Relasing the copy." << endl;                
      if (myData)
        delete myData;
    }
    
    return indexarray;
}
#ifdef __cplusplus
}
#endif
	
	
	//May be merged to the same function of miq.
#ifdef __cplusplus
	extern "C" {
#endif
JNIEXPORT jintArray JNICALL Java_annotool_select_mRMRNative_mid
  (JNIEnv *env, jclass cl, jfloatArray features, jintArray target, jint nfeature, jint nsample, jint nvar)
{
    cout << " \nIn Native C++ mRMR funciton MID ..." << endl;
	
    //setup the DataTable and its members
    //whether the JVM acutally copies the array depends on how it allocates arrays and does its gc.
    DataTable *myData = new DataTable;
    jboolean isCopyFeature;
    jboolean isCopyLabel;
    myData->data = env->GetFloatArrayElements(features, &isCopyFeature);
	
    //typecast from 32 bit long (jint) to 16 bit int.
    //myData->classLabel = (int *) env->GetIntArrayElements(target, &isCopyLabel);
    //myData->variableNames = ... //not used here
	
    //Other fields
    myData->nsample = nsample;
    myData->nvar = nvar;
	
    //build 2D array which is needed by the mRMR_selection()
    //myData->buildData2d();
	int t_discretize=9999; //now all always assume Java has discrtized the data
	//int t_discretize=0;
	if (long (t_discretize) == 9999)
		myData->discretize (t_discretize, 0);	//do not do discretization
	else
		myData->discretize (t_discretize, 1);	//do discretization
	
    //now call mRMR_selection()
    //On Windows, long is 32 bit long, same as 32 bit jint.
    //Typecasting may be necessary on other platforms with different format of long.
    jint *feaInd = (jint*) mRMR_selection (myData, nfeature, MID);
	
    //pass back the index array.
    jintArray indexarray = env->NewIntArray(nfeature);
    env->SetIntArrayRegion(indexarray, 0, (jsize) nfeature, feaInd);
	
	//memory management
    //"delete myData" may also free the original array passed in.
    if(isCopyFeature) {
      cout << "Relasing the copy." << endl;                
      if (myData)
		delete myData;
    }
    	
    return indexarray;	
}
#ifdef __cplusplus
	}
#endif

