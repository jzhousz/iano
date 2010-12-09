package annotool.preprocess;

import java.util.*;

public class Thresholding implements Preprocessor {
	
	int length;
	int width;
	int height;
	
	/* 
	 *one of the preprocessing methods, others such as PCA etc can be added
	 */
	public Thresholding(int length, int width, int height)
	{
		this.length = length;
		this.width = width;
		this.height = height;
	
	}
	
	public void process(byte[][] data)
	{
			//get the median
			byte[] tmpArray = new byte[width*height];
			for(int i = 0; i< length; i++)
			{
			  System.out.println("processing + "+ i);	
			  for(int j=0; j< width*height; j++)
				  tmpArray[j] = data[i][j];
			  Arrays.sort(tmpArray);
			  
			  //should consider the frequency of intensity
			  //and use the median when counting each occurrence of an intensity as one
			  //e.g.  1 1 1 1 2 5 -- > 1 2 5, so median value is 2.
              
			  //background: 0;  shape: around 20; expression: 100+
			  //the thresholding should be between shape and expression
			  //histogram mode detection?
			  int m = 0, k =0;
			  while(k < width*height-1)
			  {
			  	while (tmpArray[k+1] == tmpArray[m])
				     k++;
			  	tmpArray[m++] = tmpArray[k++];
			  } 	
			  int median = tmpArray[m/2] + 45;
			  //int median = tmpArray[width*height/2];
			  //median = 80;
			  System.out.println("median is: "+ median);
			  for(int j=0; j< width*height; j++)
			  {
				  if (data[i][j] < median)
				  {	  
				  //consider neighborhood
				   //if neighhood is very bright, keep it even though it is dark
					int r = j/width;
				    int c = j % width;
				    if(r < height-1 && c < width-1 && r>1 && c>1)
				   {
				     int neighbormean = ( (int) data[i][(r+1)*width+c] + (int) data[i][r*width+c+1] + (int) data[i][(r+1)*width+c+1]
				                + (int) data[i][(r-1)*width+c]+ (int) data[i][r*width+c-1]+ (int) data[i][(r-1)*width+c-1]
				                + (int) data[i][(r+1)*width+c-1]+ (int) data[i][(r-1)*width+c+1])/8;
				     if (neighbormean < median)
				    	 data[i][j] = (byte) 0;
				   }else //on the edge, set to 0 too.
					   data[i][j] = (byte) 0;
				  
			      }	  
			  }//end of j
	       }//end of i
	}
	
    public int getWidth() {return width;}
    public int getHeight(){ return height; }



}
