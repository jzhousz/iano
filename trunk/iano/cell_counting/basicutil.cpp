#include <iostream>
#include <math.h>

//Gaussian kernel and correlation compuating copy from ../cellseg/template_matching_seg.cpp
double * genGaussianKernel1D(long szx, long szy, long szz, double sigmax, double sigmay, double sigmaz)
{
         
    //std::cout << "in genKernel: " << szx << " " << szy <<  " " << szz <<  " " << sigmax << " "  << sigmay << " " << sigmaz << "\n";
    
	if (szx<=0 || szy<=0 || szz<=0) 
     {   std::cout << "Invalid sz parameter in genGaussianKernel3D().\n"; 
         return NULL;
     }
    
    double *d3 = new double [szx*szy*szz];
    if(!d3)
    {
      std::cout << "cann't getting memory for Gaussian kernel.";
      return NULL;
    }

    double sx2 = 2.0*sigmax*sigmax, sy2=2.0*sigmay*sigmay, sz2=2.0*sigmaz*sigmaz;
	long cx=(szx-1)>>1, cy=(szy-1)>>1, cz=(szz-1)>>1;
	
    //std::cout << "in genKernel: " << cx << " " << cy <<  " " << cz ;
	
	long i,j,k;
    long pagesz = szx*szy;	
    int offs0, offs1, offs2, offs3, offs4,  offs5, offs6, offs7;
	for (k=0;k<=cz;k++)
		for (j=0;j<=cy;j++)
			for (i=0;i<=cx;i++)
			{
                offs0 = (szz-1-k)*pagesz + (szy-1-j)*szx + szx-1-i;
                offs1 = (szz-1-k)*pagesz + (szy-1-j)*szx + i;
                offs2 = (szz-1-k)*pagesz + (j)*szx + szx-1-i;
                offs3 = (szz-1-k)*pagesz + (j)*szx + i;
                offs4 = (k)*pagesz + (szy-1-j)*szx + szx-1-i;
                offs5 = (k)*pagesz + (szy-1-j)*szx + i;
                offs6 = (k)*pagesz + (j)*szx + szx-1-i;
                offs7 = (k)*pagesz + (j)*szx + i;
                
				d3[offs0] = d3[offs1] =
				d3[offs2] = d3[offs3] =
				d3[offs4] = d3[offs5] =
				d3[offs6] = d3[offs7] =
				exp(-double(i-cx)*(i-cx)/sx2-double(j-cy)*(j-cy)/sy2-double(k-cz)*(k-cz)/sz2);
				
                //std::cout << " d3[" << offs0 << "]:" << d3[offs0] <<  " " ;
				
			}
   return d3;
}

//the template matching coefficient between a cube and a generated Gaussian kernel
float coeff(unsigned char *v1, double *v2, long len)
{
	if (!v1 || !v2 || len<=1) return 0;

	//first compute mean
	double m1=0,m2=0;
	long i;
	for (i=0;i<len;i++)
	{
		m1+=v1[i];
		m2+=v2[i];
	}
	m1/=len;
	m2/=len;

	//now compute corrcoef
	double tmp_s=0, tmp_s1=0, tmp_s2=0, tmp1, tmp2;
	for (i=0;i<len;i++)
	{
		tmp1 = v1[i]-m1;
		tmp2 = v2[i]-m2;
		tmp_s += tmp1*tmp2;
		tmp_s1 += tmp1*tmp1;
		tmp_s2 += tmp2*tmp2;
	}

	//the final score
	double s;
	s = (tmp_s / sqrt(tmp_s1) / sqrt(tmp_s2) + 1 )/2;
	return s;
      
}


