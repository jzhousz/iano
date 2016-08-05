package entities;

import java.awt.Color;
import java.awt.Point;

import ij.ImagePlus;

/**
* The class used to put pixel on image plus.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class PixelPutter 
{

	/**
	* Constructor.
	* 
	* @param imagePlus The image plus.
	* @param point The point at which pixels put.
	* @param rowNum The thickness.
	* @param columnNum The thickness.
	* @param color The color.
	* @return None.
	*/
	public static void putPixels(ImagePlus imagePlus, Point point, int rowNum, int columnNum, int color)
	{
		int rowNumHalf = rowNum / 2;
		int columnNumHalf = columnNum / 2;
		
		if(rowNum == 1)
			imagePlus.getProcessor().putPixel(point.x, point.y, color);
		else{
			for(int y = point.y - columnNumHalf; y < point.y + columnNumHalf; y++)
				for(int x = point.x - rowNumHalf; x < point.x + rowNumHalf; x++)
					imagePlus.getProcessor().putPixel(x, y, color);
		}
	
	}
	
	/**
	* Put pixels on the image plus.
	* 
	* @param imagePlus The image plus.
	* @param point The point at which pixels put.
	* @param rowNum The thickness.
	* @param columnNum The thickness.
	* @param color The color.
	* @return None.
	*/
	public static void putPixels(ImagePlus imagePlus, Point point, int rowNum, int columnNum, Color color)
	{
		int[] colorMark = new int[]{color.getRed(), color.getGreen(), color.getBlue()};
		
		int rowNumHalf = rowNum / 2;
		int columnNumHalf = columnNum / 2;
		
		if(rowNum == 1)
			imagePlus.getProcessor().putPixel(point.x, point.y, colorMark);
		else{
			for(int y = point.y - columnNumHalf; y < point.y + columnNumHalf; y++)
				for(int x = point.x - rowNumHalf; x < point.x + rowNumHalf; x++)
					imagePlus.getProcessor().putPixel(x, y, colorMark);
		}
	}
}
