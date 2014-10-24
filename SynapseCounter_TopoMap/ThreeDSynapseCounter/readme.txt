This folder is for materials related to Three Dimensional Synapse Quantification
including:
	-annotator code
	-imageJ plugin elements
	-test data sets
	

      It will be the home of the following:
	   1. The class "ThreeDSynapseDriver", which takes images and outputs marker files. It can run by itself in with a IJ plugin GUI, or as a command line program.
	   2. The ImageJ plugin GUI "Three_D_ROI_Annotator_Plugin": Jon's driver that calls the 3DROI Annotator (takes images, chain files .. and output marker file).
	   3. The ImageJ plugin for 3D connected component analysis and splitting (Object_Counter3D_2Channel.java)  -- It is the one that outputs synapse count and other statistics.
	   4. Other related datasets
	This location overrides the old folder "3DSynapse_Tracing"  or any duplicate file in "SynapseCounter_topomap/Axon Topo Map"


----HOW TO USE Three_D_ROI_Annotator_Plugin----

1) Create a subfolder for an ImageJ plugin.

2) Place these files and jars in the folder:

	AnnotatorUtility.java 			(contains maxima detection and other utils)
	Point3D.java				(helper class for using storign voxel locations)
	Three_D_ROI_Annotator_Plugin.java	(IJ dependant GUI for annotator tool)
	ThreeDSynapseDriver.java		(main annotator tool and logic. can be command line)
	RatsSliceProcessor.java			(wrapper for RATSForAxon)
	RATSForAxon.java			(main RATS code for 2D Image handling)
	RATSQuadTree.java			(RATS dependancy)


	biocat.jar				(BioCAT resources, chain reading, extraction/classification)
	imagescience.jar			(FeatureJ resources, extractors)
	libsvm.jar				(SVM resources, classifier)
	weka.jar				(weka resources, classifiers)

3) Load a gray scale image into ImageJ.

4) Click Plugins -> compile and run -> (subfolder for plugin) -> Three_D_ROI_Annotator_Plugin.java

5) Specify locations for the required files using the text fields or browsers.

6) Set desired ROI dimensions. 9x9x3 is generally suficient for synapse detection. 

7a)Set the desired threshold. leave slider at 0 to auto threshold.
7b)Otherwise, check the RATS option and supply rats parameters to use adaptive thresholding.
	Noise level = estimated threshold level of bachground (low for mostly black images)
	lambda      = scaling factor 
	min leaf    = minimum quad tree elaf size (dynamically determined by plugin)

8) Select desired save options and destination folder.
	Check any desired file types.

9) If training is to be done on one image, and testing on another, check "different annotation image" box. 
	After clicking OK, a file browser will open to select image.

10) Click "OK" to run the annotator. On larger images, completion time of the Annotation step may be very long.
  
TIP: execute IJ.jar from the command line to add more than IJ supported ram as well as see biocat output 
     for diagnosing chain behavior.
	java -Xms4g -jar IJ.jar	