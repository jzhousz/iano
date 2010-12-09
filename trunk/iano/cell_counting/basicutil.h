
#ifndef __BASICUTIL_H__
#define __BASICUTIL_H__

double * genGaussianKernel1D(long szx, long szy, long szz, double sigmax, double sigmay, double sigmaz);
//the template matching coefficient between a cube and a generated Gaussian kernel
float coeff(unsigned char *v1, double *v2, long len);

#endif 

