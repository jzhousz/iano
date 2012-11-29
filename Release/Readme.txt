
BIOCAT -- BIOimage Classification and Annotation Tool v1.2
=====================================================================

This tool requires JAVA runtime environment (JRE) to be installed in the target platform. 

The latest version of the JRE can be downloaded from http://java.com/en/download/index.jsp.

To have a smooth experience using BIOCAT,  4G and more RAM are suggested.

To run the cross-platform verion BIOCAT, the command is:


   java -Xmx4G -jar biocat/biocat.jar


The above command assumes that the computer has enough RAM to set the Java heap size to 4G. The user may adjust the parameter according to the hardware.

The command is in the biocat.bat or biocat.sh for your convenience.


Sample Set
===============================
The sample set of images (k150) is divided into two sets - k150_test and k150_train. 
 
Each set includes a  target file (target.ext) that associates the training or testing images with labels. 

For other methods of image input without using a target file, please refer to the Users' Guide.



