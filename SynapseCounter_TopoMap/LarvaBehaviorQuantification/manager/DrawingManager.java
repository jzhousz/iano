package manager;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

import entities.FeaturesOfLarva;
import entities.LinearLine;
import entities.PixelPutter;
import file.ImageSaver;
import ij.IJ;
import ij.ImagePlus;

public class DrawingManager 
{

	// the information of this FeaturesOfLarva will be annotated on the image
	private FeaturesOfLarva featuresOfLarva = null;
	private ImagePlus imagePlusAllFull = null; // the image to be annotated text
												// on
	private ImagePlus imagePlusCrop = null;
	private ImagePlus imagePlusBinary = null;
	private ImagePlus imagePlusEdge = null;
	private ImagePlus imagePlusSkeleton = null;
	
	private String pathFiles = "";
	
	// FeaturesOfLarva list contains all FeaturesOfLarvas
	private ArrayList<FeaturesOfLarva> listFeaturesOfLarva = null;
	private final int PIXEL_NUM = 5;

	public DrawingManager(ImagePlus imagePlus, ArrayList<FeaturesOfLarva> listFeaturesOfLarva,
			FeaturesOfLarva featuresOfLarva, String pathFiles) 
	{
		this.imagePlusAllFull = imagePlus;
		this.featuresOfLarva = featuresOfLarva;
		this.listFeaturesOfLarva = listFeaturesOfLarva;
		this.pathFiles = pathFiles;
		
		imagePlusCrop = ImageManager.getImagePlusFromFile(pathFiles + "imagePlusCrop"+featuresOfLarva.getFrameId()+".jpg");
		imagePlusBinary = ImageManager.getImagePlusFromFile(pathFiles + "imagePlusBinary"+featuresOfLarva.getFrameId()+".jpg");
		IJ.run(imagePlusBinary, "Convert to Mask", "only");
		imagePlusEdge = ImageManager.getImagePlusFromFile(pathFiles + "imagePlusEdge"+featuresOfLarva.getFrameId()+".jpg");
		IJ.run(imagePlusEdge, "Convert to Mask", "only");
		imagePlusSkeleton = ImageManager.getImagePlusFromFile(pathFiles + "imagePlusSkeleton"+featuresOfLarva.getFrameId()+".jpg");
		IJ.run(imagePlusSkeleton, "Convert to Mask", "only");
	}
	
	public void drawLine(ImagePlus imagePlus, Point topLeft, int length, int thickness, Color color) 
	{
		Point ptMark = new Point();
		// draw the top line
		for(int y = topLeft.y; y < topLeft.y + thickness; y++)
			for(int x = topLeft.x; x < topLeft.x + length; x++ )
			{
				ptMark.x = x;
				ptMark.y = y;
				PixelPutter.putPixels(imagePlus, ptMark, 1, 1, color);
			}
	}
	
	public void drawFrame(ImagePlus imagePlus, Point topLeft, int width, int heigth, int thickness, Color color) 
	{
		Point ptMark = new Point();
		// draw the top line
		for(int y = topLeft.y; y < topLeft.y + thickness; y++)
			for(int x = topLeft.x; x < topLeft.x + 2*thickness + width; x++ )
			{
				ptMark.x = x;
				ptMark.y = y;
				PixelPutter.putPixels(imagePlus, ptMark, 1, 1, color);
			}
		
		// draw the left line
		for(int y = topLeft.y + thickness; y < topLeft.y + thickness + heigth; y++)
			for(int x = topLeft.x; x < topLeft.x + thickness; x++ )
			{
				ptMark.x = x;
				ptMark.y = y;
				PixelPutter.putPixels(imagePlus, ptMark, 1, 1, color);
			}
		
		// draw the right line
		for(int y = topLeft.y + thickness; y < topLeft.y + thickness + heigth; y++)
			for(int x = topLeft.x + thickness + width; x < topLeft.x + 2*thickness + width; x++ )
			{
				ptMark.x = x;
				ptMark.y = y;
				PixelPutter.putPixels(imagePlus, ptMark, 1, 1, color);
			}
				
		// draw the bottom line
		for(int y = topLeft.y + thickness + heigth; y < topLeft.y + 2*thickness + heigth ; y++)
			for(int x = topLeft.x; x < topLeft.x + 2*thickness + width; x++ )
			{
				ptMark.x = x;
				ptMark.y = y;
				PixelPutter.putPixels(imagePlus, ptMark, 1, 1, color);
			}
	}
	
	public void drawSmallBinaryLarva(Point pointBegin, Color colorBorder) 
	{
		int[] colorInts = new int[3];
		int[] colorBorderInts = new int[] { colorBorder.getRed(), colorBorder.getGreen(), colorBorder.getBlue() };
		int[] colorSkeletonInts = new int[] { Color.pink.getRed(), Color.pink.getGreen(), Color.pink.getBlue() };
		int[] colorBinaryInts = new int[] { Color.black.getRed(), Color.black.getGreen(), Color.black.getBlue() };
		
		for (int y = 0; y < imagePlusCrop.getHeight(); y++)
		for (int x = 0; x < imagePlusCrop.getWidth(); x++) 
		{
			imagePlusCrop.getProcessor().getPixel(x, y, colorInts);
			imagePlusAllFull.getProcessor().putPixel(x + pointBegin.x, y + pointBegin.y, colorInts);
		
			if (x < 1 || x > imagePlusCrop.getWidth() - 2)
				imagePlusAllFull.getProcessor().putPixel(x + pointBegin.x, y + pointBegin.y, colorBorderInts);
			
			if(imagePlusBinary.getProcessor().getPixel(x, y) > 128)
			{
				imagePlusAllFull.getProcessor().putPixel(x + pointBegin.x, y + pointBegin.y, colorBinaryInts );
			}
			
			if(imagePlusSkeleton.getProcessor().getPixel(x, y) > 128)
			{
				imagePlusAllFull.getProcessor().putPixel(x + pointBegin.x, y + pointBegin.y, colorSkeletonInts );
			}
		}
		
		Point ptCenter = MathManager.addPoints(pointBegin, featuresOfLarva.getCenterPoint() );
		
		LinearLine lineQuartile = MathManager.getParallelLine(featuresOfLarva.getLineQuartilePerp().getBeta1(), ptCenter);
		
//		drawLine(lineQuartile, ptCenter, 20, 1, Color.orange);
		drawLineRectangle(lineQuartile, ptCenter, ptCenter, 20, 1, Color.orange);
		
		// mark the end points on the image
		PixelPutter.putPixels(imagePlusAllFull, MathManager.addPoints(pointBegin, featuresOfLarva.getEndPoints().get(0)), PIXEL_NUM, PIXEL_NUM,
				Color.red);
		PixelPutter.putPixels(imagePlusAllFull, MathManager.addPoints(pointBegin, featuresOfLarva.getEndPoints().get(1)), PIXEL_NUM, PIXEL_NUM,
				Color.cyan);
		
		PixelPutter.putPixels(imagePlusAllFull, ptCenter, 3, 3, Color.green);
		PixelPutter.putPixels(imagePlusAllFull, MathManager.addPoints(pointBegin, featuresOfLarva.getPointCenterMass() ), 3, 3, Color.orange);
		
		Point pointQuartile = null;
		
		for(Point pt : featuresOfLarva.getPointsQuartile())
		{
			pointQuartile = MathManager.addPoints(pointBegin, pt );
			
			PixelPutter.putPixels(imagePlusAllFull, pointQuartile, 3, 3, Color.cyan);
		}
	}
	
	public void drawSmallEdgeSkeleton(Point pointBegin, Color colorBorder) 
	{
		int[] colorInts = new int[3];
		int[] colorBorderInts = new int[] { colorBorder.getRed(), colorBorder.getGreen(), colorBorder.getBlue() };
		int[] colorSkeletonInts = new int[] { Color.pink.getRed(), Color.pink.getGreen(), Color.pink.getBlue() };
		
		for (int y = 0; y < imagePlusCrop.getHeight(); y++)
		for (int x = 0; x < imagePlusCrop.getWidth(); x++) 
		{
			imagePlusCrop.getProcessor().getPixel(x, y, colorInts);
			imagePlusAllFull.getProcessor().putPixel(x + pointBegin.x, y + pointBegin.y, colorInts);
		
			if (x < 1 || x > imagePlusCrop.getWidth() - 2)
				imagePlusAllFull.getProcessor().putPixel(x + pointBegin.x, y + pointBegin.y, colorBorderInts);
			
			if(imagePlusSkeleton.getProcessor().getPixel(x, y) > 128)
			{
				imagePlusAllFull.getProcessor().putPixel(x + pointBegin.x, y + pointBegin.y, colorSkeletonInts );
			}
			
			if(imagePlusEdge.getProcessor().getPixel(x, y) > 128)
			{
				imagePlusAllFull.getProcessor().putPixel(x + pointBegin.x, y + pointBegin.y, colorSkeletonInts );
			}
			
		}
		
		Point ptCenter = MathManager.addPoints(pointBegin, featuresOfLarva.getCenterPoint() );

		// mark the end points on the image
		PixelPutter.putPixels(imagePlusAllFull, MathManager.addPoints(pointBegin, featuresOfLarva.getEndPoints().get(0)), PIXEL_NUM, PIXEL_NUM,
				Color.red);
		PixelPutter.putPixels(imagePlusAllFull, MathManager.addPoints(pointBegin, featuresOfLarva.getEndPoints().get(1)), PIXEL_NUM, PIXEL_NUM,
				Color.cyan);
		
		PixelPutter.putPixels(imagePlusAllFull, ptCenter, 3, 3, Color.green);
		PixelPutter.putPixels(imagePlusAllFull, MathManager.addPoints(pointBegin, featuresOfLarva.getPointCenterMass() ), 3, 3, Color.orange);
	}
	
	public void drawSmallOutlineLarva(Point pointBegin, Color colorBorder) 
	{
		int[] colorInts = new int[3];
		int[] colorBorderInts = new int[] { colorBorder.getRed(), colorBorder.getGreen(), colorBorder.getBlue() };
		
		for (int y = 0; y < imagePlusCrop.getHeight(); y++)
		for (int x = 0; x < imagePlusCrop.getWidth(); x++) {
			imagePlusCrop.getProcessor().getPixel(x, y, colorInts);
			imagePlusAllFull.getProcessor().putPixel(x + pointBegin.x, y + pointBegin.y, colorInts);
		
			if (x < 1 || x > imagePlusCrop.getWidth() - 2)
				imagePlusAllFull.getProcessor().putPixel(x + pointBegin.x, y + pointBegin.y, colorBorderInts);
		}
		
//		Point ptRight = new Point(pointStart.x + featuresOfLarva.getCenterPoint().x + 10, pointStart.y + featuresOfLarva.getCenterPoint().y);
		Point ptCenter = MathManager.addPoints(pointBegin, featuresOfLarva.getCenterPoint() );
		
//		Point ptMiddle = MathManager.addPoints(pointBegin, featuresOfLarva.getCenterPoint() );
		
		int[] colorSkeletonInts = new int[] { Color.pink.getRed(), Color.pink.getGreen(), Color.pink.getBlue() };
		for (int y = 0; y < imagePlusSkeleton.getHeight(); y++)
			for (int x = 0; x < imagePlusSkeleton.getWidth(); x++) {
				if(imagePlusSkeleton.getProcessor().getPixel(x, y) > 128)
				{
					imagePlusAllFull.getProcessor().putPixel(x + pointBegin.x, y + pointBegin.y, colorSkeletonInts );
				}
			}
		
		LinearLine lineCenterMassParel = MathManager.getParallelLine(featuresOfLarva.getLinearLineCenterMass().getBeta1(), ptCenter);
		
//		drawLine(lineCenterMassParel, ptCenter, 20, 1, Color.green);
		drawLineRectangle(lineCenterMassParel, ptCenter, ptCenter, 20, 1, Color.green);
		
		LinearLine linearLineCenterPoint1 = MathManager.getLinearLine(ptCenter, 
				MathManager.addPoints(pointBegin, featuresOfLarva.getEndPoints().get(0)) );

//		drawLine(linearLineCenterPoint1, MathManager.addPoints(pointBegin, featuresOfLarva.getEndPoints().get(0)), 20, 1, Color.yellow);
		drawLineRectangle(linearLineCenterPoint1, MathManager.addPoints(pointBegin, featuresOfLarva.getEndPoints().get(0)), ptCenter, 20, 1, Color.yellow);
		
		LinearLine linearLineCenterPoint2 = MathManager.getLinearLine(ptCenter, 
				MathManager.addPoints(pointBegin, featuresOfLarva.getEndPoints().get(1)) );
		
//		drawLine(linearLineCenterPoint2, MathManager.addPoints(pointBegin, featuresOfLarva.getEndPoints().get(1)), 20, 1, Color.cyan);
		drawLineRectangle(linearLineCenterPoint2, MathManager.addPoints(pointBegin, featuresOfLarva.getEndPoints().get(1)), ptCenter, 20, 1, Color.cyan);
		
		// mark the end points on the image
		PixelPutter.putPixels(imagePlusAllFull, MathManager.addPoints(pointBegin, featuresOfLarva.getEndPoints().get(0)), PIXEL_NUM, PIXEL_NUM,
				Color.red);
		PixelPutter.putPixels(imagePlusAllFull, MathManager.addPoints(pointBegin, featuresOfLarva.getEndPoints().get(1)), PIXEL_NUM, PIXEL_NUM,
				Color.cyan);
		
		PixelPutter.putPixels(imagePlusAllFull, MathManager.addPoints(pointBegin, featuresOfLarva.getPointCenterMass() ), 3, 3, Color.orange);
	}
	
	public void drawSmallColorLarva(Point pointStart, Color colorBorder) 
	{
		// drawing the small window with the larva 
		int[] colorInts = new int[3];
//					int colorInt = 0;
		int[] colorBorderInts = new int[] { colorBorder.getRed(), colorBorder.getGreen(), colorBorder.getBlue() };

		for (int y = 0; y < imagePlusCrop.getHeight(); y++)
			for (int x = 0; x < imagePlusCrop.getWidth(); x++) {
				imagePlusCrop.getProcessor().getPixel(x, y, colorInts);
				imagePlusAllFull.getProcessor().putPixel(x + pointStart.x, y + pointStart.y, colorInts);

				if (x < 1 || x > imagePlusCrop.getWidth() - 2)
					imagePlusAllFull.getProcessor().putPixel(x + pointStart.x, y + pointStart.y, colorBorderInts);
			}
	}
	
	public void drawTwoPoints(LinearLine linearLine, int x1, int x2, Color colorPoint1, Color colorPoint2) 
	{
		int xLine = x1;
		int xBefore = 0;
		int xAfter = 0;
		if (x1 > x2) {
			xAfter = xLine + 20;
			xBefore = xLine - 20;
		} else {
			xAfter = xLine - 20;
			xBefore = xLine + 20;
		}

		int yBefore = (int) Math.round(linearLine.getY(xBefore));
		PixelPutter.putPixels(imagePlusAllFull, new Point(xBefore, yBefore), 5, 5, colorPoint2);

		int yAfter = (int) Math.round(linearLine.getY(xAfter));
		PixelPutter.putPixels(imagePlusAllFull, new Point(xAfter, yAfter), 5, 5, colorPoint1);
	}

	public void drawLine(LinearLine linearLine, int x, int lengthHalf, int thickness, Color color) {
		drawLine(linearLine, new Point(x, 0), lengthHalf, thickness, color);
	}

	public void drawLine(LinearLine linearLine, Point point, int lengthHalf, int thickness, Color color) 
	{
		int xLine = point.x;
		for (int x = xLine - lengthHalf; x < xLine + lengthHalf; x++) {
			double y = linearLine.getBeta1() * x + linearLine.getBeta0();
			int yInt = (int) Math.round(y);
			PixelPutter.putPixels(imagePlusAllFull, new Point(x, yInt), thickness, thickness, color);
		}
	}
	
	public void drawLineRectangle(LinearLine linearLine, Point point, Point pointMiddle, int lengthHalf, int thickness, Color color) 
	{
		int length = 60;
		int xLine = point.x;
		for (int x = xLine - lengthHalf; x < xLine + lengthHalf; x++) {
			double y = linearLine.getBeta1() * x + linearLine.getBeta0();
			int yInt = (int) Math.round(y);
			
			if(Math.abs(x - pointMiddle.x) <= length && Math.abs(y - pointMiddle.y) <= length)
				PixelPutter.putPixels(imagePlusAllFull, new Point(x, yInt), thickness, thickness, color);
		}
	}
	
	public void drawLineCircle(LinearLine linearLine, Point point, Point pointMiddle, int lengthHalf, int thickness, Color color) 
	{
		int length = 80;
		int xLine = point.x;
		for (int x = xLine - lengthHalf; x < xLine + lengthHalf; x++) {
			double y = linearLine.getBeta1() * x + linearLine.getBeta0();
			int yInt = (int) Math.round(y);
			
			if(Math.abs(x - pointMiddle.x) <= length && Math.abs(y - pointMiddle.y) <= length)
				PixelPutter.putPixels(imagePlusAllFull, new Point(x, yInt), thickness, thickness, color);
		}
	}

	public void drawAll() 
	{
		int indexLarva = 0;
		// draw the center point and both end points of the larvae from 1 
		// frame to the current frame
		for (int i = listFeaturesOfLarva.get(0).getFrameId(); i <= featuresOfLarva.getFrameId(); i++) {
			indexLarva = i - listFeaturesOfLarva.get(0).getFrameId();
			PixelPutter.putPixels(imagePlusAllFull, listFeaturesOfLarva.get(indexLarva).getCenterPointOnFullFrame(), 3, 3, Color.orange);
		}

		// map the edge of the larva to the debug image
		if(featuresOfLarva.getIsRolling())
			ImageManager.mapColorPixels(imagePlusEdge, imagePlusAllFull,
					featuresOfLarva.getRoiTopLeft(), 128, Color.green);
		else
			ImageManager.mapColorPixels(imagePlusEdge, imagePlusAllFull,
				featuresOfLarva.getRoiTopLeft(), 128, Color.pink);

		// map the skeleton of the larva to the debug image
		ImageManager.mapColorPixels(imagePlusSkeleton, imagePlusAllFull,
		featuresOfLarva.getRoiTopLeft(), 128, Color.orange);
		
		// mark the end points on the image
		PixelPutter.putPixels(imagePlusAllFull, featuresOfLarva.getEndPointsOnFullFrame().get(0), PIXEL_NUM, PIXEL_NUM,
				Color.red);
		PixelPutter.putPixels(imagePlusAllFull, featuresOfLarva.getEndPointsOnFullFrame().get(1), PIXEL_NUM, PIXEL_NUM,
				Color.cyan);

		int xLine = 0;
		
//		LinearLine linearRegression = MathManager.getParallelLine(featuresOfLarva.getLinearRegression().getBeta1(),
//				featuresOfLarva.getInterceptionPoint());

//		drawLine(linearRegression,
//				featuresOfLarva.getInterceptionPoint(), 50, 1, Color.pink);

		drawLineCircle(featuresOfLarva.getLinearLineParallel(), featuresOfLarva.getCenterPointOnFullFrame(), featuresOfLarva.getCenterPointOnFullFrame(), 40, 1,
				Color.cyan);

		xLine = featuresOfLarva.getEndPointsOnFullFrame().get(0).x + (featuresOfLarva.getEndPointsOnFullFrame().get(1).x
				- featuresOfLarva.getEndPointsOnFullFrame().get(0).x) / 2;

//		drawLine(featuresOfLarva.getLinearLineEndPts(), xLine, 20, 1, Color.green);
		drawLineCircle(featuresOfLarva.getLinearLineEndPts(), new Point(xLine, 0), featuresOfLarva.getCenterPointOnFullFrame(), 20, 1, Color.green);

		// mark the center point of the current larva on the image
		PixelPutter.putPixels(imagePlusAllFull, featuresOfLarva.getCenterPointOnFullFrame(),  PIXEL_NUM, PIXEL_NUM,
				Color.green);
		
//		if(featuresOfLarva.getIsRolling())
//			annotate(new Point(1130, 15), "Rolling...", Color.yellow);
		
//		drawFrame(imagePlusAllFull, new Point(1125, 30), 120, 30, 1, Color.white);
		drawLine(imagePlusAllFull, new Point(1130, 70), 110, 1, Color.red);
	}

}
