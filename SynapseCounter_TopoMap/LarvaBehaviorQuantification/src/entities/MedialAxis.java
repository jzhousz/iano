package entities;

import java.util.ArrayList;

import file.ImageSaver;
import ij.IJ;
import ij.ImagePlus;
import manager.ImageManager;

/**
* The class used to calculate the media axis skeleton from a 2D array.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   09-24-2016
*/
public class MedialAxis {

/**
* transform from a 2D array to a skeleton array via Medial axis transform.
* 
* @param arr The 2D array.
* @return The skeleton array via Medial axis transform..
*/
public int[][] transform(int[][] arr)
{
	ArrayList<int[][]> images = new ArrayList<int[][]>();
	
	int k = 0;
	
	images.add(arr);
	
	// initialize the layer for k = 0
//	for(int y = 1; y <= arr.length - 1; y++)
//	{
//		for(int x = 1; x <= arr[0].length - 1; x++)
//		{
//			if(images.get(k)[y][x] >= 128)
//				images.get(k)[y][x] = 1;
//			else
//				images.get(k)[y][x] = 0;
//		}
//	}
	
	boolean isChange = true;
	
	while(isChange)
	{
		System.out.println("k:" + k);
		
		isChange = false;
		
		k = k + 1;
		images.add( copyArr( images.get(k-1) ) ); // use the previous layer and modify
		
		// initialize the k layer
		for(int y = 1; y <= arr.length - 1; y++)
			for(int x = 1; x <= arr[0].length - 1; x++)
				// if the new layer needs to be updated
				if(images.get(k-1)[y][x] == k)
				{
					isChange = true;
					images.get(k)[y][x] = getMinNeighbor(images.get(k-1), x, y) + 1;
				}
	}
	
	return images.get(k);
}

/**
* Get the minimum value from the closet 4 neighbors.
* 
* @param arr The 2D array.
* @param x The column number.
* @param y The row number
* @return The minimum value from the closet 4 neighbors.
*/
public int getMinNeighbor(int[][] arr, int x, int y)
{
	int min = 200000000; // java int max: 2,147,483,647
	
	if(x > 1)
		if(arr[y][x-1] < min)
			min = arr[y][x-1];
	
	if(x < arr[0].length - 1)
		if(arr[y][x+1] < min)
			min = arr[y][x+1];
	
	if(y > 1)
		if(arr[y-1][x] < min)
			min = arr[y-1][x];
	
	if(y < arr.length - 1)
		if(arr[y+1][x] < min)
			min = arr[y+1][x];
	
	return min;
}

/**
* Get the maximum value from the closet 4 neighbors.
* 
* @param arr The 2D array.
* @param x The column number.
* @param y The row number
* @return The minimum value from the closet 4 neighbors.
*/
public int getMaxNeighbor(int[][] arr, int x, int y)
{
	int max = 0; // java int max: 2,147,483,647
	
	if(x > 1)
		if(arr[y][x-1] > max)
			max = arr[y][x-1];
	
	if(x < arr[0].length - 1)
		if(arr[y][x+1] > max)
			max = arr[y][x+1];
	
	if(y > 1)
		if(arr[y-1][x] > max)
			max = arr[y-1][x];
	
	if(y < arr.length - 1)
		if(arr[y+1][x] > max)
			max = arr[y+1][x];
	
	return max;
}

/**
* New space and copy another array.
* 
* @param arr The 2D array.
* @return The copied 2D array.
*/
public int[][] copyArr(int[][] arr)
{
//	int[][] template = new int[][]{
//		{ 9,9,9,9,9,9,9,9,9,9 },
//		  { 9,0,0,0,0,0,0,0,0,9 },
//		  { 9,0,255,255,255,255,255,255,255,0 },
//		  { 9,0,255,255,255,255,255,255,255,0 },
//		  { 9,0,255,255,255,255,255,255,255,0 },
//		  { 9,0,255,255,255,255,255,255,255,0 },
//		  { 9,0,255,255,255,255,255,255,255,0 },
//		  { 9,0,0,0,0,0,0,0,0,9 }
//	};
	
	int[][] template = new int[arr.length][arr[0].length];
			
	for(int y = 1; y <= arr.length - 1; y++)
	{
		for(int x = 1; x <= arr[0].length - 1; x++)
		{
			template[y][x] = arr[y][x];
		}
	}
	
	return template;
}
	
	
public static void main(String[] args) {
		
		System.out.println("Started!");
		
		
		ImagePlus im = ImageManager.getImagePlusFromFile("E:/Binary_1.jpg");
		
		IJ.run(im, "Convert to Mask", "");
		
		int[][] arr = new int[im.getProcessor().getHeight()][im.getProcessor().getWidth()];
		
		for(int y = 1; y < im.getProcessor().getHeight(); y++)
			for(int x = 0; x < im.getProcessor().getWidth(); x++)
			{
				if(im.getProcessor().getPixel(y, x) >= 128 )
					arr[y][x] = 1;
				else
					arr[y][x] = 0;
			}
		
		MedialAxis medialAxis = new MedialAxis();
		int[][] transArr = medialAxis.transform(arr);
		
		System.out.println("The skeleton matrix:");
		
		for(int y = 1; y <= transArr.length - 1; y++)
		{
			for(int x = 1; x <= transArr[0].length - 1; x++)
			{
				System.out.print(transArr[y][x] + ",");
				
//				if( transArr[y][x] == medialAxis.getMaxNeighbor(transArr, x, y) )
//					System.out.print(transArr[y][x] + ",");
//				else
//					System.out.print("0,");
			}
			
			System.out.print("\n");
		}
		
		for(int y = 1; y < im.getProcessor().getHeight(); y++)
			for(int x = 0; x < im.getProcessor().getWidth(); x++)
			{
				if( transArr[y][x] == medialAxis.getMaxNeighbor(transArr, x, y) && transArr[y][x] != 0 )
					im.getProcessor().putPixel(y, x, 0);
				else
					im.getProcessor().putPixel(y, x, 255);
			}
		
		ImageSaver.saveImagesWithPath("E:/test1.jpg", im);
		
		System.out.println("Done!");
		
		/*
		int[][] imageOriginal = new int[][]{
			  { 9,9,9,9,9,9,9,9,9,9 },
			  { 9,0,0,0,0,0,0,0,0,9 },
			  { 9,0,255,255,255,255,255,255,255,0 },
			  { 9,0,255,255,255,255,255,255,255,0 },
			  { 9,0,255,255,255,255,255,255,255,0 },
			  { 9,0,255,255,255,255,255,255,255,0 },
			  { 9,0,255,255,255,255,255,255,255,0 },
			  { 9,0,0,0,0,0,0,0,0,9 }
			};
			
//			int val = getMinNeighbor(imageOriginal, 3, 3);
//			
//			System.out.println("val:" + val);
			
			for(int y = 1; y <= imageOriginal.length - 1; y++)
			{
				for(int x = 1; x <= imageOriginal[0].length - 1; x++)
				{
					if(imageOriginal[y][x] >= 128)
						imageOriginal[y][x] = 1;
					else
						imageOriginal[y][x] = 0;
				}
			}
			
			MedialAxis medialAxis = new MedialAxis();
			int[][] result = medialAxis.transform(imageOriginal);
			
//		ArrayList<int[][]> images = new ArrayList<int[][]>();
//			
//		int k = 0;
//		
//		images.add(template);
//		
//		// initialize the layer for k = 0
//		for(int y = 1; y <= imageOriginal.length - 1; y++)
//		{
//			for(int x = 1; x <= imageOriginal[0].length - 1; x++)
//			{
//				if(images.get(k)[y][x] >= 128)
//					images.get(k)[y][x] = 1;
//				else
//					images.get(k)[y][x] = 0;
//			}
//		}
//		
//		boolean isChange = true;
//		
//		while(isChange)
//		{
//			System.out.println("k:" + k);
//			
//			isChange = false;
//			
//			k = k + 1;
//			images.add( copyArr( images.get(k-1) ) ); // use the previous layer and modify
//			
//			// initialize the k layer
//			for(int y = 1; y <= imageOriginal.length - 1; y++)
//			{
//				for(int x = 1; x <= imageOriginal[0].length - 1; x++)
//				{
//					// if the new layer needs to be updated
//					if(images.get(k-1)[y][x] == k)
//					{
//						isChange = true;
//						images.get(k)[y][x] = getMinNeighbor(images.get(k-1), x, y) + 1;
//					}
//				}
//			}
//		
//		}
		
		System.out.println("The skeleton matrix:");
		
		for(int y = 1; y <= imageOriginal.length - 1; y++)
		{
			for(int x = 1; x <= imageOriginal[0].length - 1; x++)
			{
//				System.out.print(result[y][x] + ",");
  				if( result[y][x] == medialAxis.getMaxNeighbor(result, x, y) )
					System.out.print(result[y][x] + ",");
				else
					System.out.print("0,");
			}
			
			System.out.print("\n");
		}
*/

	}

}
