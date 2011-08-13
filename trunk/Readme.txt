
BI-CAT -- Biological Image Classification and Annotation Tool v1.0
=====================================================================

This tool requires JAVA runtime environment (JRE) to be installed in the target platform. The latest version of the JRE can be downloaded from http://java.com/en/download/index.jsp.

The downloaded rar file for BICAT also contains a sample set of images (k150) and the corresponding target file (k150_4c_target).

The format for the target file should be as follows:

1. The first line should contain the annotation label. If there are multiple annotation labels, they should be separated by white space(s).

2. The second line contains pairs of digital targets and the corresponding class names (for example: 1:a150). Each pair should be separated by white space(s).

3. Each line after the second line indicates target values and corresponding images - each separated by white space(s). The target value for each annotation label should come in the same order as their corresponding label in the first line. The last part in each line should be the name of the image file.