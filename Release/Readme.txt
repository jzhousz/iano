
BIOCAT -- BIOimage Classification and Annotation Tool v1.2
=====================================================================

This tool requires JAVA runtime environment (JRE) to be installed in the target platform. 

The latest version of the JRE can be downloaded from http://java.com/en/download/index.jsp.


How to Run BIOCAT? 
=====================================================================

To run the cross-platform version of BIOCAT, the command is:


   java -Xmx4G -jar biocat/biocat.jar


- The above command assumes a 64-bit system that has enough RAM to set the Java heap size to 4G. 
  The user can adjust the parameter to higher or lower according to the specific system. 
  To have a smooth experience using BIOCAT,  4G and more RAM are suggested.

- The command is in the run.bat or run.sh for your convenience. 

- The above way of launching is especially suggested for working with large image sets. 
  Although the user may click the biocat.jar to launch the application, it would use a default heap size which is small. 

- For the Windows-only version (if provided with the download), the user can click the biocat.exe to start. 
  It assumes a 1G max heap size. 



Sample Set
===============================
The sample set of images (k150) is divided into two sets - k150_test and k150_train. 
 
Each set includes a  target file (target.ext) that associates the training or testing images with labels. 

For other methods of image input without using a target file, please refer to the Users' Guide.



