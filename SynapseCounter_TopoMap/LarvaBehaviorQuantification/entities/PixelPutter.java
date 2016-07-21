package entities;

import java.awt.Color;
import java.awt.Point;

import ij.ImagePlus;

public class PixelPutter {

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
