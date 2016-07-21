package manager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import entities.FeaturesOfLarva;
import entities.GenericStack;
import entities.PixelElement;
import entities.PixelElementSegment;
import entities.PixelElementTable;
import entities.PixelPutter;
import file.ImageSaver;
import file.LogWriter;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.LookUpTable;
import ij.Prefs;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.io.Opener;
import ij.plugin.AVI_Reader;
import ij.plugin.ChannelSplitter;
import ij.plugin.filter.BackgroundSubtracter;
import ij.plugin.filter.Binary;
import ij.plugin.filter.RankFilters;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

public class ImageManager {

	public static ImagePlus getImagePlusFromFile(String file)
	{
		Opener opener = new Opener();  
		String imageFilePath = file;
		ImagePlus imp = opener.openImage(imageFilePath);
		return imp;
	}
	
//	public static void saveAnnotationInImage7(FeaturesOfLarva featuresOfLarva, String path, String name, ImagePlus images, String msg,
//			AnnotationManager annotationManager, int numLine) {
//		// annotate information about the larva on the image
//		annotationManager.annotateMessage(msg, Color.green, numLine);
//		ImageSaver.saveImagesWithPath(path, name, images);
//	}
//	
//	public static void saveWarningInImage7(FeaturesOfLarva featuresOfLarva, String path, String name, ImagePlus images, String msg,
//			AnnotationManager annotationManager, int numLine) {
//		// annotate information about the larva on the image
//		annotationManager.annotateMessage(msg, Color.yellow, numLine);
//		ImageSaver.saveImagesWithPath(path, name, images);
//	}
//	
//	public static void saveErroInImage7(FeaturesOfLarva featuresOfLarva, String path, String name, ImagePlus images, String msg,
//			AnnotationManager annotationManager, int numLine) {
//		// annotate information about the larva on the image
//		annotationManager.annotateMessage(msg, Color.red, numLine);
//		ImageSaver.saveImagesWithPath(path, name, images);
//	}
	
//	public static ImagePlus handleOverLength(FeaturesOfLarva featuresOfLarva, double lengthSkeleton, double MAX_LENGTH_SKELETON_LARVA, 
//			int frameId, String aviFile, String DIR_IMAGE_TEMP, ImagePlus imagePlusAllFull, 
//			AnnotationManager annotationMgrErr, ImagePlus imagePlusBigObjSingle, int NUM_OF_3_PIXELS_EXPAND,
//			ImagePlus imagePlusShrinked, ImagePlus imagePlusPreviousBigObj, ImagePlus imagePlusShrinkedBigObj) 
//	{
//		String msg = "";
//		if(featuresOfLarva.getFeaturesOfLarvaPrevious() != null)
//		{
//			imagePlusShrinked = ImageManager.getShrinkLarva(featuresOfLarva, imagePlusBigObjSingle, NUM_OF_3_PIXELS_EXPAND,
//					DIR_IMAGE_TEMP);
//
//			// the big object binary ImagePlus of the previous larva
//			if(featuresOfLarva.getFeaturesOfLarvaPrevious() != null)
//				imagePlusPreviousBigObj = featuresOfLarva.getFeaturesOfLarvaPrevious().getImagePlusBigObj();
//			
//			imagePlusShrinkedBigObj = ImageManager.getLargestBinaryObjectOverlap(imagePlusPreviousBigObj, imagePlusShrinked);
//			
//			ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP, "Blue_Test_ShrinkSec1_" + frameId, imagePlusShrinkedBigObj);
//			
//			imagePlusBigObjSingle = imagePlusShrinkedBigObj;
//			ImagePlus imagePlusSkeletonRaw = ImageManager.getStemSkeleton(imagePlusBigObjSingle, 128);
////			imagePlusSkeletonRaw = ImageManager.getSkeleton(imagePlusBigObjSingle, frameId, DIR_IMAGE_TEMP);
//
//			ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP, "Blue_SkeletonSec1_" + frameId, imagePlusSkeletonRaw);
//			
//			double lengthSkeletonShrink = ImageManager.getSkeletonLength(imagePlusSkeletonRaw, 128);
//			
//			System.out.println("[lengthSkeletonShrink] lengthSkeletonShrink:"+lengthSkeletonShrink);
//			
////			featuresOfLarva.setLengthSkeleton(lengthSkeleton);
//			
//			if (lengthSkeletonShrink > MAX_LENGTH_SKELETON_LARVA) 
//			{
//				featuresOfLarva.setIsValid(false);
//				msg = "Error: The skeleton length (" + lengthSkeletonShrink
//						+ ") of this larva (2ed check) is [greater than] the threshold (" + MAX_LENGTH_SKELETON_LARVA
//						+ ") of larva's skeleton length in the binary image \n for frame " + Integer.toString(frameId);
//				LogWriter.writeLog(msg, aviFile);
//				ImageManager.saveErroInImage(featuresOfLarva, DIR_IMAGE_TEMP, "Blue_All_" + frameId, imagePlusAllFull, msg,
//						annotationMgrErr,2);
//			}else{
//				featuresOfLarva.setIsValid(true);
//				imagePlusBigObjSingle = imagePlusShrinkedBigObj;
//				featuresOfLarva.setImagePlusBigObj(imagePlusBigObjSingle);
//				featuresOfLarva.setLengthSkeleton(lengthSkeletonShrink);
//				msg = "Information: The over-skeleton-length larva problem (1st check) caused by binarization has been handled with the length of "+lengthSkeletonShrink+".";
//				ImageManager.saveAnnotationInImage(featuresOfLarva, DIR_IMAGE_TEMP, "Blue_All_" + frameId, imagePlusAllFull, msg,
//						annotationMgrErr,2);
//			}
//		
//		}else{
//			featuresOfLarva.setIsValid(false);
//			msg = "Error: The skeleton length (" + lengthSkeleton
//					+ ") of this larva (1st check, without previous larva) is [greater than] the threshold (" + MAX_LENGTH_SKELETON_LARVA
//					+ ") of larva's skeleton length in the binary image \n for frame " + Integer.toString(frameId);
//			LogWriter.writeLog(msg, aviFile);
//			ImageManager.saveErroInImage(featuresOfLarva, DIR_IMAGE_TEMP, "Blue_All_" + frameId, imagePlusAllFull, msg,
//					annotationMgrErr,2);
//		}
//		
//		return imagePlusBigObjSingle;
//	}
	
//	public static ImagePlus handleOverSize(FeaturesOfLarva featuresOfLarva, int sizeBigObj, int MAX_SIZE_LARVA, 
//			int frameId, String aviFile, String DIR_IMAGE_TEMP, ImagePlus imagePlusAllFull, 
//			AnnotationManager annotationMgrErr, ImagePlus imagePlusBigObjSingle, int NUM_OF_3_PIXELS_EXPAND,
//			ImagePlus imagePlusShrinked, ImagePlus imagePlusPreviousBigObj, ImagePlus imagePlusShrinkedBigObj) 
//	{
//		String msg = "";
//		
//		if(featuresOfLarva.getFeaturesOfLarvaPrevious() != null)
//		{
//			imagePlusShrinked = ImageManager.getShrinkLarva(featuresOfLarva, imagePlusBigObjSingle, NUM_OF_3_PIXELS_EXPAND,
//					DIR_IMAGE_TEMP);
//
//			// the big object binary ImagePlus of the previous larva
//			if(featuresOfLarva.getFeaturesOfLarvaPrevious() != null)
//				imagePlusPreviousBigObj = featuresOfLarva.getFeaturesOfLarvaPrevious().getImagePlusBigObj();
//			
//			imagePlusShrinkedBigObj = ImageManager.getLargestBinaryObjectOverlap(imagePlusPreviousBigObj, imagePlusShrinked);
//			
//			ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP, "Blue_Test_Shrink1_" + frameId, imagePlusShrinkedBigObj);
//			
//			int sizeBigObjShrink = ImageManager.getPixelArea(imagePlusShrinkedBigObj, 128);
//			
//			if (sizeBigObjShrink > MAX_SIZE_LARVA ) 
//			{
//				featuresOfLarva.setIsValid(false);
//				msg = "Error: The size (" + sizeBigObjShrink
//						+ ") of this larva (2ed check) is [greater than] the threshold (" + MAX_SIZE_LARVA
//						+ ") of larva's size in the binary image \n for frame " + Integer.toString(frameId);
//				LogWriter.writeLog(msg, aviFile);
//				ImageManager.saveErroInImage(featuresOfLarva, DIR_IMAGE_TEMP, "Blue_All_" + frameId, imagePlusAllFull, msg,
//						annotationMgrErr,2);
//			}else{
//				featuresOfLarva.setIsValid(true);
//				imagePlusBigObjSingle = imagePlusShrinkedBigObj;
//				featuresOfLarva.setImagePlusBigObj(imagePlusBigObjSingle);
//				featuresOfLarva.setArea(sizeBigObjShrink);
//				msg = "Information: The over-size larva problem (1st check) caused by binarization has been handled with size of "+sizeBigObjShrink+".";
//				ImageManager.saveAnnotationInImage(featuresOfLarva, DIR_IMAGE_TEMP, "Blue_All_" + frameId, imagePlusAllFull, msg,
//						annotationMgrErr,2);
//			}
//		
//		}else{
//			featuresOfLarva.setIsValid(false);
//			msg = "Error: The size (" + sizeBigObj
//					+ ") of this larva (1st check, without previous larva) is [greater than] the threshold (" + MAX_SIZE_LARVA
//					+ ") of larva's size in the binary image \n for frame " + Integer.toString(frameId);
//			LogWriter.writeLog(msg, aviFile);
//			ImageManager.saveErroInImage(featuresOfLarva, DIR_IMAGE_TEMP, "Blue_All_" + frameId, imagePlusAllFull, msg,
//					annotationMgrErr,2);
//		}
//		
//		return imagePlusBigObjSingle;
//	}
	
	
	public static PixelElementSegment getWholeSegment(ImagePlus imagePlus, Point point, int threshold) 
	{
		PixelElementTable pixelElementTable = new PixelElementTable(imagePlus, threshold);
		
		PixelElementSegment pixelElementSegment = new PixelElementSegment();
		
		PixelElement pixelElementRoot = new PixelElement();
		pixelElementRoot.setPoint(point);

		PixelElement[] pixelElementNeighbors = null;

		// the current highest level
//		int highestLevel = 0;
		// the pixel element holds the reference to the the pixel element 
		// with the highest level
//		PixelElement pixelElementHighestLevel = pixelElementRoot;
		
		Queue<PixelElement> queueTransverse = new LinkedList<PixelElement>();

		queueTransverse.add(pixelElementRoot);
		pixelElementRoot.setVisited(true);
		pixelElementSegment.getPixelElements().add(pixelElementRoot);

		while (!queueTransverse.isEmpty()) 
		{
			pixelElementRoot = queueTransverse.remove();
			pixelElementNeighbors = getNeighborElements(pixelElementRoot, pixelElementTable);

			// loop through all 8 neighbor pixel elements
			for (int i = 0; i < 8; i++) {
				
				if (pixelElementNeighbors[i] != null) {

					if (pixelElementNeighbors[i].getValue() == 255 && pixelElementNeighbors[i].getVisited() == false) 
					{
//						pixelElementNeighbors[i].setLevel(pixelElementRoot.getLevel() + 1);
						pixelElementNeighbors[i].setVisited(true);

//						pixelElementNeighbors[i].setAntecedent(pixelElementRoot);
						
						pixelElementSegment.getPixelElements().add(pixelElementNeighbors[i]);
						
						queueTransverse.add(pixelElementNeighbors[i]);
						
						
					}

				}
			}
		}
		
		return pixelElementSegment;
	}
	
//	public static Point convertPointToCoordinator(Point pointConverting) 
//	{
//		return new Point(pointConverting.x + 1, pointConverting.y + 1);
//	}
	
	public static double getSkeletonLength(ImagePlus imagePlusSkeleton, int threshold) 
	{
		PixelElement pixelElementEnd1 = findLongestPixelElement(imagePlusSkeleton, null, threshold);

		PixelElementTable pixelElementTable2 = new PixelElementTable(imagePlusSkeleton, threshold);
		// pixelElementTable2 is passed in the method and will be modified.
		PixelElement pixelElementEnd2 = findLongestPixelElement(pixelElementTable2, pixelElementEnd1, threshold);

		return pixelElementEnd2.getLevel() + 1;
	}
	
	public static ImagePlus getStemSkeleton(ImagePlus imagePlusSkeleton, int threshold) 
	{
		imagePlusSkeleton = imagePlusSkeleton.duplicate();
		
		IJ.run(imagePlusSkeleton, "Dilate", "");
		IJ.run(imagePlusSkeleton, "Skeletonize", "");
		
		PixelElement pixelElementEnd1 = findLongestPixelElement(imagePlusSkeleton, null, threshold);

		PixelElementTable pixelElementTable2 = new PixelElementTable(imagePlusSkeleton, threshold);
		// pixelElementTable2 is passed in the method and will be modified.
		PixelElement pixelElementEnd2 = findLongestPixelElement(pixelElementTable2, pixelElementEnd1, threshold);

		ImagePlus imagePlusSkeletonStem = imagePlusSkeleton.duplicate();

		for (int y = 0; y < imagePlusSkeletonStem.getHeight(); y++)
			for (int x = 0; x < imagePlusSkeletonStem.getWidth(); x++)
				imagePlusSkeletonStem.getProcessor().putPixel(x, y, 0);
		
		imagePlusSkeletonStem.getProcessor().putPixel(pixelElementEnd2.getPoint().x, pixelElementEnd2.getPoint().y, 255);
		
		// loop through all pixel element to find the path for the stem skeleton
		while(pixelElementEnd2.getLevel() != 0)
		{
			pixelElementEnd2 = pixelElementEnd2.getAntecedent();
			imagePlusSkeletonStem.getProcessor().putPixel(pixelElementEnd2.getPoint().x, pixelElementEnd2.getPoint().y, 255);
		}
			
		return imagePlusSkeletonStem;
	}
	
	// get the center of skeleton, 1st and 3ed quartile points.
	// the 1st point is the center of skeleton.
	// the 2ed and 3ed points are 1st and 3ed quartile points.
	public static ArrayList<Point> findCenterPoints(ImagePlus imagePlusSkeleton, int threshold) 
	{
		ArrayList<Point> points = new ArrayList<Point>();
		
		PixelElement pixelElementEnd1 = findLongestPixelElement(imagePlusSkeleton, null, threshold);
		
//		System.out.println("[findEndPoints()] pixelElementEnd1:"+pixelElementEnd1.getPoint()+", level:"+pixelElementEnd1.getLevel());
		
		PixelElementTable pixelElementTable2 = new PixelElementTable(imagePlusSkeleton, threshold);
		
		// pixelElementTable2 is passed in the method and will be modified.
		PixelElement pixelElementEnd2 = findLongestPixelElement(pixelElementTable2, pixelElementEnd1, threshold);
		
		int halfLevel = pixelElementEnd2.getLevel() / 2 + 1;
		
		// use copy constructor to copy all the data form pixelElementEnd2
		PixelElement pixelElement = new PixelElement(pixelElementEnd2); // allocate new memory
		
		// loop through all pixel element to find the pixel element with level
		// of half of the highest level
		while(pixelElement.getLevel() >= halfLevel)
			pixelElement = pixelElement.getAntecedent();
			
		points.add(new Point(pixelElement.getPoint().x, pixelElement.getPoint().y) );
		
		// calculate the 1st quartile point
		int quartile1stLevel = halfLevel / 2 + 1;
		pixelElement = new PixelElement(pixelElementEnd2); // allocate new memory
		
		// loop through all pixel element to find the pixel element with level
		// of half of the highest level
		while(pixelElement.getLevel() >= quartile1stLevel)
			pixelElement = pixelElement.getAntecedent();
			
		points.add( new Point(pixelElement.getPoint().x, pixelElement.getPoint().y) );
		
		// calculate the 3ed quartile point
		int quartile3edLevel = halfLevel + halfLevel / 2 + 1;
		pixelElement = new PixelElement(pixelElementEnd2); // allocate new memory
		
		// loop through all pixel element to find the pixel element with level
		// of half of the highest level
		while(pixelElement.getLevel() >= quartile3edLevel)
			pixelElement = pixelElement.getAntecedent();
			
		points.add( new Point(pixelElement.getPoint().x, pixelElement.getPoint().y) );
		
			
//		pointsEnd.add(pixelElementEnd1.getPoint());
//		pointsEnd.add(pixelElementEnd2.getPoint());
		
//		return pixelElementEnd2.getPoint();
		return points;
	}
	
	// get only the center of skeleton
	public static Point findCenterPoint(ImagePlus imagePlusSkeleton, int threshold) 
	{
//		ArrayList<Point> pointsEnd = new ArrayList<Point>();
//		Point pointCenter = null;
		
		PixelElement pixelElementEnd1 = findLongestPixelElement(imagePlusSkeleton, null, threshold);
		
//		System.out.println("[findEndPoints()] pixelElementEnd1:"+pixelElementEnd1.getPoint()+", level:"+pixelElementEnd1.getLevel());
		
		PixelElementTable pixelElementTable2 = new PixelElementTable(imagePlusSkeleton, threshold);
		
		// pixelElementTable2 is passed in the method and will be modified.
		PixelElement pixelElementEnd2 = findLongestPixelElement(pixelElementTable2, pixelElementEnd1, threshold);
		
		int halfLevel = pixelElementEnd2.getLevel() / 2 + 1;
		
		// loop through all pixel element to find the pixel element with level
		// of half of the highest level
		while(pixelElementEnd2.getLevel() >= halfLevel)
			pixelElementEnd2 = pixelElementEnd2.getAntecedent();
			
			
//		pointsEnd.add(pixelElementEnd1.getPoint());
//		pointsEnd.add(pixelElementEnd2.getPoint());
		
		return pixelElementEnd2.getPoint();
	}
	
	
	public static PixelElement findLongestPixelElement(PixelElementTable pixelElementTable, PixelElement pixelElementRoot, int threshold) 
	{
		if(pixelElementRoot == null)
		{
			for(int y = 0; y < pixelElementTable.NUM_COLUMN; y++)
				for( int x = 0; x < pixelElementTable.NUM_ROW; x++ )
					if(pixelElementTable.getPixelElements()[x][y].getValue() == 255)
						pixelElementRoot = pixelElementTable.getPixelElements()[x][y];
		}
		
		if(pixelElementRoot == null)
		{
			System.out.println("Error: In ImageManager.findLongestPixelElement(), pixelElementRoot is null.");
			return null;
		}
		
		pixelElementRoot.setLevel(0);

		PixelElement[] pixelElementNeighbors = null;

		// the current highest level
		int highestLevel = 0;
		// the pixel element holds the reference to the the pixel element 
		// with the highest level
		PixelElement pixelElementHighestLevel = pixelElementRoot;
		
		Queue<PixelElement> queueTransverse = new LinkedList<PixelElement>();

		queueTransverse.add(pixelElementRoot);
		pixelElementRoot.setVisited(true);

		while (!queueTransverse.isEmpty()) 
		{
			pixelElementRoot = queueTransverse.remove();
			pixelElementNeighbors = getNeighborElements(pixelElementRoot, pixelElementTable);

			// loop through all 8 neighbor pixel elements
			for (int i = 0; i < 8; i++) {
				
				if (pixelElementNeighbors[i] != null) {

					if (pixelElementNeighbors[i].getValue() == 255 && pixelElementNeighbors[i].getVisited() == false) 
					{
						pixelElementNeighbors[i].setLevel(pixelElementRoot.getLevel() + 1);
						pixelElementNeighbors[i].setVisited(true);

						pixelElementNeighbors[i].setAntecedent(pixelElementRoot);
						
						queueTransverse.add(pixelElementNeighbors[i]);
						
						if(pixelElementNeighbors[i].getLevel() > pixelElementHighestLevel.getLevel())
						{
							// pixelElementHighestLevel points to pixelElementNeighbors[i]
							pixelElementHighestLevel = pixelElementNeighbors[i];
//							System.out.println("[findLongestPixelElement()] pixelElementHighestLevel:"+pixelElementHighestLevel.getPoint()+", level:"+pixelElementHighestLevel.getLevel());
						}
					}

				}
			}
		}
		
		return pixelElementHighestLevel;
	}
	
	public static PixelElement findLongestPixelElement(ImagePlus imagePlusSkeleton, PixelElement pixelElementRoot, int threshold) 
	{
		PixelElementTable pixelElementTable = new PixelElementTable(imagePlusSkeleton, threshold);
		
		return findLongestPixelElement(pixelElementTable, pixelElementRoot, threshold);
		
		// declare the root pixel element to start with
//		PixelElement pixelElementRoot = pixelElementStart;
		
//		if(pixelElementRoot == null)
//		{
//			for(int y = 0; y < pixelElementTable.NUM_COLUMN; y++)
//				for( int x = 0; x < pixelElementTable.NUM_ROW; x++ )
//					if(pixelElementTable.getPixelElements()[x][y].getValue() == 255)
//						pixelElementRoot = pixelElementTable.getPixelElements()[x][y];
//		}
//		
//		if(pixelElementRoot == null)
//		{
//			System.out.println("Error: In ImageManager.findLongestPixelElement(), pixelElementRoot is null.");
//			return null;
//		}
//		
//		pixelElementRoot.setLevel(0);
//
//		PixelElement[] pixelElementNeighbors = null;
//
//		// the current highest level
//		int highestLevel = 0;
//		// the pixel element holds the reference to the the pixel element 
//		// with the highest level
//		PixelElement pixelElementHighestLevel = pixelElementRoot;
//		
//		Queue<PixelElement> queueTransverse = new LinkedList<PixelElement>();
//
//		queueTransverse.add(pixelElementRoot);
//		pixelElementRoot.setVisited(true);
//
//		while (!queueTransverse.isEmpty()) 
//		{
//			pixelElementRoot = queueTransverse.remove();
//			pixelElementNeighbors = getNeighborElements(pixelElementRoot, pixelElementTable);
//
//			// loop through all 8 neighbor pixel elements
//			for (int i = 0; i < 8; i++) {
//				
//				if (pixelElementNeighbors[i] != null) {
//
//					if (pixelElementNeighbors[i].getValue() == 255 && pixelElementNeighbors[i].getVisited() == false) 
//					{
//						pixelElementNeighbors[i].setLevel(pixelElementRoot.getLevel() + 1);
//						pixelElementNeighbors[i].setVisited(true);
//
//						queueTransverse.add(pixelElementNeighbors[i]);
//						
//						if(pixelElementNeighbors[i].getLevel() > pixelElementHighestLevel.getLevel())
//						{
//							// pixelElementHighestLevel points to pixelElementNeighbors[i]
//							pixelElementHighestLevel = pixelElementNeighbors[i];
//							System.out.println("[findLongestPixelElement()] pixelElementHighestLevel:"+pixelElementHighestLevel.getPoint()+", level:"+pixelElementHighestLevel.getLevel());
//						}
//					}
//
//				}
//			}
//		}
//		
//		return pixelElementHighestLevel;
	}
	
	public static ArrayList<Point> findEndPoints(ImagePlus imagePlusSkeleton, int threshold) 
	{
		ArrayList<Point> pointsEnd = new ArrayList<Point>();
		
		PixelElement pixelElementEnd1 = findLongestPixelElement(imagePlusSkeleton, null, threshold);

		PixelElementTable pixelElementTable2 = new PixelElementTable(imagePlusSkeleton, threshold);
		
		PixelElement pixelElementEnd2 = findLongestPixelElement(pixelElementTable2, pixelElementEnd1, threshold);
		
		pointsEnd.add(pixelElementEnd1.getPoint());
		pointsEnd.add(pixelElementEnd2.getPoint());
		
		return pointsEnd;
	}
	
	public static ArrayList<Point> shiftPoints(ArrayList<Point> points, int xDifference, int yDifference) 
	{
		ArrayList<Point> pointsShift = new ArrayList<Point>();
		for(Point pt : points)
		{
			Point ptShift = shiftPoint(pt, xDifference, yDifference);
			pointsShift.add(ptShift);
		}
			
		return pointsShift;
	}
	
	public static Point shiftPoint(Point point, int xDifference, int yDifference) 
	{
		return new Point(point.x + xDifference, point.y + yDifference);
	}
	
	public static ImagePlus shiftImage(ImagePlus imagePlus, int xDifference, int yDifference, int threshold) 
	{
		ImagePlus imagePlusShift = imagePlus.duplicate();
		
		for(int y = 0; y < imagePlus.getHeight(); y++)
			for(int x = 0; x < imagePlus.getWidth(); x++)
			{
				imagePlusShift.getProcessor().putPixel(x, y, 0);
			}
		
		for(int y = 0; y < imagePlus.getHeight(); y++)
			for(int x = 0; x < imagePlus.getWidth(); x++)
			{
				if(x > 0 && (x < imagePlus.getWidth() - 1 - xDifference) && y > 0 && (y < imagePlus.getHeight() - 1 - yDifference) && imagePlus.getProcessor().getPixel(x, y) > threshold )
					imagePlusShift.getProcessor().putPixel(x + xDifference, y + yDifference, 255);
			}
		
		return imagePlusShift;
	}
	
//	public static ImagePlus getShrinkLarva(FeaturesOfLarva featuresOfLarva, ImagePlus imagePlusBigObj2, int expandNumOf3Pixels,
//			String DIR_IMAGE_TEMP) 
//	{
//		int frameId = featuresOfLarva.getFrameId();
//		
//		ImagePlus imagePlusBigObjPre = featuresOfLarva.getFeaturesOfLarvaPrevious().getImagePlusBigObj();
//		ImagePlus imagePlusBigObjPre2 = imagePlusBigObjPre.duplicate();
//
//		int xDifference = featuresOfLarva.getRoiTopLeft().x
//				- featuresOfLarva.getFeaturesOfLarvaPrevious().getRoiTopLeft().x;
//		int yDifference = featuresOfLarva.getRoiTopLeft().y
//				- featuresOfLarva.getFeaturesOfLarvaPrevious().getRoiTopLeft().y;
//
////		System.out.println("x left: " + xDifference + ", y down: " + yDifference);
//
//		int xNew, yNew;
//
////		for (int y = 0; y < imagePlusBigObjPre.getHeight(); y++)
////			for (int x = 0; x < imagePlusBigObjPre.getWidth(); x++) {
////				imagePlusBigObjPre2.getProcessor().putPixel(x, y, 0);
////			}
//
//		// shift the previous binary biggest larva to the same coordinate as that of the current larva
//		for (int y = 0; y < imagePlusBigObjPre.getHeight(); y++)
//			for (int x = 0; x < imagePlusBigObjPre.getWidth(); x++) {
//				if (imagePlusBigObjPre.getProcessor().getPixel(x, y) > 128) {
//					xNew = x - xDifference;
//					yNew = y - yDifference;
////					System.out.println("Previous point: " + MathManager.getPointStr(new Point(x, y))
////							+ ", current point: " + MathManager.getPointStr(new Point(xNew, yNew)));
//					if (xNew > 0 && xNew < imagePlusBigObjPre.getWidth() && yNew > 0
//							&& yNew < imagePlusBigObjPre.getHeight())
//						imagePlusBigObjPre2.getProcessor().putPixel(xNew, yNew, 255);
//				}
//			}
//
//		ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP, "Blue_Test_PreAdjust2Be_" + frameId, imagePlusBigObjPre);
//		ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP, "Blue_Test_PreAdjust2Af_" + frameId, imagePlusBigObjPre2);
//
//		// ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP,
//		// "Blue_Test_Previous2_" + frameId,
//		// featuresOfLarva.getFeaturesOfLarvaPrevious().getImagePlusBigObj());
//
//		// ImagePlus imagePlusWatershed = imagePlusBigObj1.duplicate();
//		// IJ.run(imagePlusWatershed, "Watershed", "");
//		// ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP,
//		// "Blue_Test_Watershed2_" + frameId, imagePlusWatershed);
//
//		ImagePlus imagePlusExpand = ImageManager.expandLarva(imagePlusBigObjPre2, expandNumOf3Pixels);
//
//		// imagePlusBigObj2, imagePlusExpand, 128);
//		ImagePlus imagePlusShrink = ImageManager.shrinkLarva(frameId, DIR_IMAGE_TEMP, featuresOfLarva.getImagePlusCrop(), imagePlusBigObj2,
//				imagePlusExpand, 128);
//
//		ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP, "Blue_Test_Shrink2_" + frameId, imagePlusShrink);
//
//		return imagePlusShrink;
//	}

	public static ImagePlus overlapImages(ImagePlus imagePlusColor, ImagePlus imagePlus1, Color color1,
			ImagePlus imagePlus2, Color color2, int threshold) {

		int[] colorInt1 = new int[] { color1.getRed(), color1.getGreen(), color1.getBlue() };
		int[] colorInt2 = new int[] { color2.getRed(), color2.getGreen(), color2.getBlue() };
		int[] colorIntWhite = new int[] { Color.white.getRed(), Color.white.getGreen(), Color.white.getBlue() };

		imagePlusColor = imagePlusColor.duplicate();

		for (int y = 0; y < imagePlus1.getHeight(); y++)
			for (int x = 0; x < imagePlus1.getWidth(); x++) {
				imagePlusColor.getProcessor().putPixel(x, y, colorIntWhite);

				if (imagePlus1.getProcessor().getPixel(x, y) > threshold)
					imagePlusColor.getProcessor().putPixel(x, y, colorInt1);

				if (imagePlus2.getProcessor().getPixel(x, y) > threshold)
					imagePlusColor.getProcessor().putPixel(x, y, colorInt2);
			}

		return imagePlusColor;
	}

	public static ImagePlus shrinkLarva(int frameId, String DIR_IMAGE_TEMP, ImagePlus imagePlusCrop, ImagePlus imgpShrink, ImagePlus imagePlusExpand,
			int threshold) {

		imgpShrink = imgpShrink.duplicate();
		ImagePlus imagePlusWatershed = imgpShrink.duplicate();
		imagePlusExpand = imagePlusExpand.duplicate();

		// Separate all segments
		IJ.run(imagePlusWatershed, "Watershed", "");

		// imgpShrink.getProcessor().invert();
		// imagePlusExpand.getProcessor().invert();

//		String frameId = "375";
//		String DIR_IMAGE_TEMP = "E:\\Summer 2016\\Larva Project\\Output\\Test\\Images_Temp\\";
		ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP, "Blue_Test_Watershed2_" + frameId, imagePlusWatershed);

		ImagePlus imagePlusOverlap = ImageManager.overlapImages(imagePlusCrop, imagePlusExpand, Color.blue,
				imagePlusWatershed, Color.red, 128);

		ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP, "Blue_Test_Overlap2_" + frameId, imagePlusOverlap);

		// convert the image to PixelElementTable
		PixelElementTable peTableShrink = new PixelElementTable(imagePlusWatershed, threshold);
		// get all segments of the image
		ArrayList<PixelElementSegment> peSegments = peTableShrink.getFrameSegments();
		// ArrayList<PixelElementSegment> peSegments =
		// ImageManager.getFrameSegments(peTableShrink);

//		System.out.println("[shrinkLarva] number of segments: " + peSegments.size());

		PixelElementTable peTableRefer = new PixelElementTable(imagePlusExpand, threshold);

		// if the segment of pixels is not contained in the expanded larva on
		// imgpRefer,
		// the id of the segment will be added to segmentIds.
		ArrayList<Integer> segmentIds = new ArrayList<Integer>();

		int x = 0;
		int y = 0;
		Boolean isSegmentDone = false;
		PixelElement pe = null;
		int j = 0;

		for (int i = 0; i < peSegments.size(); i++) {
			isSegmentDone = false;
			j = 0;
			// for( PixelElement pe : peSegments.get(i).getPixelElements() )
			while (!isSegmentDone && j < peSegments.get(i).getPixelElements().size()) {
				pe = peSegments.get(i).getPixelElements().get(j);
				x = pe.getPoint().x;
				y = pe.getPoint().y;
				if (peTableRefer.getPixelElements()[x][y].getValue() != 255) {
					segmentIds.add(i);
					// go for the next segment
					isSegmentDone = true;
				}
				j++;
			}
		}

		for (Integer segmentId : segmentIds) {
			// System.out.println("[shrinkLarva] segmentId out boundary:
			// "+segmentId);
			for (PixelElement peFill : peSegments.get(segmentId).getPixelElements()) {
				imgpShrink.getProcessor().putPixel(peFill.getPoint().x, peFill.getPoint().y, 0);
			}
		}

		return imgpShrink;

	}

	public static ImagePlus expandLarva(ImagePlus imagePlus, int num3Pixels) {
		imagePlus = imagePlus.duplicate();
		int num = num3Pixels;

		// expand the larva edge in the image
		for (int i = 0; i < num; i++)
			IJ.run(imagePlus, "Dilate", "");

		return imagePlus;
	}

	public static int getPixelArea(ImagePlus imagePlus, int threshold) {
		int area = 0;
		for (int y = 0; y < imagePlus.getHeight(); y++)
			for (int x = 0; x < imagePlus.getWidth(); x++)
				if (imagePlus.getProcessor().getPixel(x, y) > threshold)
					area++;

		return area;
	}

	/**
	 * Get amount of pixels close to a point in N level.
	 * 
	 * @param imagePlus
	 *            The image plus to be checked.
	 * @param level
	 *            The number of level.
	 * @return the amount of pixels close to the point in N level
	 */
	public static int getPixelsAmount(ImagePlus imagePlus, Point point, int level) {
		PixelElementTable pixelElementTable = new PixelElementTable(imagePlus, 128);
		PixelElement pixelElementCurrent = new PixelElement();
		pixelElementCurrent.setPoint(point);
		pixelElementCurrent.setAmount(0);
		pixelElementCurrent.setLevel(0);

		PixelElement[] pixelElementNeighbors = null;

		int amount = 0; // the total number of pixels of level (the parameter
						// passed in) for the point passed in
		Queue<PixelElement> queueTransverse = new LinkedList<PixelElement>();

		queueTransverse.add(pixelElementCurrent);
		pixelElementCurrent.setVisited(true);

		while (!queueTransverse.isEmpty()) {
			pixelElementCurrent = queueTransverse.remove();
			// System.out.println("[getPixelsAmount] pixelElementCurrent:
			// ("+pixelElementCurrent.getPoint().x+",
			// "+pixelElementCurrent.getPoint().y+") level:
			// "+pixelElementCurrent.getLevel() );
			pixelElementNeighbors = getNeighborElements(pixelElementCurrent, pixelElementTable);

			// loop through all 8 neighbor pixel elements
			for (int i = 0; i < 8; i++) {
				if (pixelElementNeighbors[i] != null) {
					// if the pixel element is black and has NOT been visited
					// and the number of level is less
					// than level (the parameter passed in)
					if (pixelElementNeighbors[i].getValue() == 255 && pixelElementNeighbors[i].getVisited() == false
							&& pixelElementCurrent.getLevel() < level) {
						// System.out.println("[getPixelsAmount] - pixel
						// (!null): ("+pixelElementNeighbors[i].getPoint().x+",
						// "+pixelElementNeighbors[i].getPoint().y+"),
						// value:"+pixelElementNeighbors[i].getValue() + ",
						// level: "+pixelElementCurrent.getLevel());
						// pixelElementCurrent.setHasSuccessor(true);
						// the successor has 1 more level than the predecessor
						pixelElementNeighbors[i].setLevel(pixelElementCurrent.getLevel() + 1);
						pixelElementNeighbors[i].setVisited(true);

						queueTransverse.add(pixelElementNeighbors[i]);

						// System.out.println("removeTripleBlock - pixel
						// (visited): ("+pixelElementCurrent.getPoint().x+",
						// "+pixelElementCurrent.getPoint().y+")");
						amount++;
					}

				}
			}

		}
		return amount;
	}

	public static ImagePlus removeTripleBlock(ImagePlus imagePlusSkeleton) {
		// System.out.println("inside removeTripleBlock");
		imagePlusSkeleton = imagePlusSkeleton.duplicate();

		PixelElementTable pixelElementTable = new PixelElementTable(imagePlusSkeleton, 128);

		// for(int y=0; y<pixelElementTable.getPixelElements().length; y++)
		// for(int x=0; x<pixelElementTable.getPixelElements()[0].length; x++)
		// {
		// if(pixelElementTable.getPixelElements()[x][y].getValue() == 255)
		//// TextFileWriter.writeToFile("removeTripleBlock - pixel (ske):("+
		//// pixelElementTable.getPixelElements()[x][y].getPoint().x+",
		// "+pixelElementTable.getPixelElements()[x][y].getPoint().y+")");
		// System.out.println("removeTripleBlock - pixel (ske):("+
		// pixelElementTable.getPixelElements()[x][y].getPoint().x+",
		// "+pixelElementTable.getPixelElements()[x][y].getPoint().y+")");
		// }

		// ArrayList<PixelElementSegment> segments =
		// ImageManager.getFrameSegments(pixelElementTable);
		ArrayList<PixelElementSegment> segments = pixelElementTable.getFrameSegments();

		// System.out.println("removeTripleBlock - num of
		// segments:"+segments.size());

		PixelElement pixelElementSeed = new PixelElement();

		// randomly get a seed pixel element which is the pixel containing value
		// of 255
		// use the seed pixel element to find all the end points
		for (int y = 0; y < pixelElementTable.getPixelElements().length; y++)
			for (int x = 0; x < pixelElementTable.getPixelElements().length; x++)
				if (pixelElementTable.getPixelElements()[x][y].getValue() == 255) {
					pixelElementSeed.setPoint(new Point(x, y));
					pixelElementSeed.setValue(255);
					break; // break out the for loop since we find the black
							// pixel element
				}

		// System.out.println("removeTripleBlock - pixel (seed):
		// ("+pixelElementSeed.getPoint().x+",
		// "+pixelElementSeed.getPoint().y+")");

		// -- remove 1 pixel from triple pixels structure
		Queue<PixelElement> queueTransverse = new LinkedList<PixelElement>();
		// Queue<PixelElement> queueTriple = new LinkedList<PixelElement>();
		GenericStack<PixelElement> stackTriple = new GenericStack<PixelElement>();

		int numNeighbor = 0;

		pixelElementTable = new PixelElementTable(imagePlusSkeleton, 128);
		PixelElement pixelElementCurrent = pixelElementSeed;
		PixelElement[] pixelElementNeighbors = null;

		queueTransverse.add(pixelElementCurrent);
		pixelElementCurrent.setVisited(true);

		while (!queueTransverse.isEmpty()) {
			pixelElementCurrent = queueTransverse.remove();

			// System.out.println("removeTripleBlock - check:
			// ("+pixelElementCurrent.getPoint().x+",
			// "+pixelElementCurrent.getPoint().y+")");

			// if the pixel element is a black pixel, which is 255 and
			// it has NOT been visited
			// if( pixelElementCurrent.getValue() == 255 &&
			// pixelElementCurrent.getVisited() == false )
			// {
			// pixelElementCurrent.setVisited(true);
			// get all the neighbors of the element
			pixelElementNeighbors = getNeighborElements(pixelElementCurrent, pixelElementTable);

			numNeighbor = 0; // count how many neighbors it has

			// loop through all pixel elements
			for (int i = 0; i < 8; i++) {
				if (pixelElementNeighbors[i] != null) {
					// if the pixel element is black and has NOT been visited
					if (pixelElementNeighbors[i].getValue() == 255 && pixelElementNeighbors[i].getVisited() == false) {
						// System.out.println("removeTripleBlock - pixel
						// (!null): ("+pixelElementNeighbors[i].getPoint().x+",
						// "+pixelElementNeighbors[i].getPoint().y+"),
						// value:"+pixelElementNeighbors[i].getValue());
						pixelElementCurrent.setHasSuccessor(true);
						pixelElementNeighbors[i].setVisited(true);

						queueTransverse.add(pixelElementNeighbors[i]);

						// System.out.println("removeTripleBlock - pixel
						// (visited): ("+pixelElementCurrent.getPoint().x+",
						// "+pixelElementCurrent.getPoint().y+")");
					}

					// if the pixel element is black
					if (pixelElementNeighbors[i].getValue() == 255) {
						numNeighbor++;
					}
				}
			}

			// if the current pixelElement has more than 2 neighbors
			if (numNeighbor > 2) {
				// System.out.println("removeTripleBlock - removed:
				// ("+pixelElementCurrent.getPoint().x+",
				// "+pixelElementCurrent.getPoint().y+") has neighbors of
				// "+numNeighbor);
				stackTriple.push(pixelElementCurrent);
			}
			// }

		} // end of while queueTransverse

		while (!stackTriple.isEmpty()) {
			pixelElementCurrent = stackTriple.pop();
			// reload the pixelElementTable from imagePlusSkeleton because it
			// might be changed
			pixelElementTable = new PixelElementTable(imagePlusSkeleton, 128);
			// get all neighbors
			pixelElementNeighbors = getNeighborElements(pixelElementCurrent, pixelElementTable);
			numNeighbor = 0; // count number of neighbors it has

			// loop through all pixel elements
			for (int i = 0; i < 8; i++) {
				if (pixelElementNeighbors[i] != null) {
					// if the pixel element is black
					if (pixelElementNeighbors[i].getValue() == 255)
						numNeighbor++;
				}
			}

			// if the pixel element has more than 2 neighbors and doesn't derive
			// other pixel element
			// remove it from the image plus
			if (numNeighbor > 2 && !pixelElementCurrent.getHasSuccessor()) {
				// System.out.println("removeTripleBlock - pixel:
				// ("+pixelElementCurrent.getPoint().x+",
				// "+pixelElementCurrent.getPoint().y+")");
				imagePlusSkeleton.getProcessor().putPixel(pixelElementCurrent.getPoint().x,
						pixelElementCurrent.getPoint().y, 0);
			}

		}

		return imagePlusSkeleton;
	}

	public static PixelElement[] getNeighborElements(PixelElement pixelElementCurrent,
			PixelElementTable pixelElementTable) {
		
		PixelElement[] pixelElementNeighbors = new PixelElement[8];

		int rowMaxNum = pixelElementTable.getPixelElements().length;
		int columnMaxNum = pixelElementTable.getPixelElements()[0].length;

		int x = pixelElementCurrent.getPoint().x;
		int y = pixelElementCurrent.getPoint().y;

		// if the pixelElementCurrent is not the first row and the first column
		if (x > 0 && y > 0)
			// top left pixel element
			pixelElementNeighbors[0] = pixelElementTable.getPixelElements()[x - 1][y - 1];
		else
			pixelElementNeighbors[0] = null;

		if (y > 0){
//			System.out.println("[getNeighborElements()] pixelElementNeighbors[1]: ("+x+","+(y - 1)+")");
			// top pixel element
			pixelElementNeighbors[1] = pixelElementTable.getPixelElements()[x][y - 1];
		}else
			pixelElementNeighbors[1] = null;

		if (x < columnMaxNum - 1 && y > 0)
			// top right pixel element
			pixelElementNeighbors[2] = pixelElementTable.getPixelElements()[x + 1][y - 1];
		else
			pixelElementNeighbors[2] = null;

		if (x > 0)
			// left pixel element
			pixelElementNeighbors[3] = pixelElementTable.getPixelElements()[x - 1][y];
		else
			pixelElementNeighbors[3] = null;

		if (x < columnMaxNum - 1)
			// right pixel element
			pixelElementNeighbors[4] = pixelElementTable.getPixelElements()[x + 1][y];
		else
			pixelElementNeighbors[4] = null;

		if (x > 0 && y < rowMaxNum - 1)
			// bottom left pixel element
			pixelElementNeighbors[5] = pixelElementTable.getPixelElements()[x - 1][y + 1];
		else
			pixelElementNeighbors[5] = null;

		if (y < rowMaxNum - 1)
			// Bottom pixel element
			pixelElementNeighbors[6] = pixelElementTable.getPixelElements()[x][y + 1];
		else
			pixelElementNeighbors[6] = null;

		if (x < columnMaxNum - 1 && y < rowMaxNum - 1)
			// bottom right pixel element
			pixelElementNeighbors[7] = pixelElementTable.getPixelElements()[x + 1][y + 1];
		else
			pixelElementNeighbors[7] = null;

		return pixelElementNeighbors;
	}

public static ImagePlus getLargestBinaryObject(ImagePlus imagePreviousBigObj, ImagePlus imagePlusSegments) {
		
		imagePlusSegments = imagePlusSegments.duplicate();

//		IJ.run(imagePlusSegments, "Fill Holes", "");
		
		// convert the imagePlus pixel table to roiFrameDescription pixel
		// element table
		PixelElementTable pixelElementTable = new PixelElementTable(imagePlusSegments, 128);
	
		 //if the previous larva exists
//		 if( imagePreviousBigObj != null )
//		 {
//			 // create overlapped ROIFrameDescription pixel element table
//			 PixelElementTable pixelElementTablePrevious = new PixelElementTable(imagePreviousBigObj, 128);
//			 pixelElementTable = pixelElementTable.overlap(pixelElementTablePrevious);
//		 }

		// find all the segments (objects) on the roiFrameDescription pixel
		// element table
		// ArrayList<PixelElementSegment> frameSegments =
		// ImageManager.getFrameSegments(pixelElementTable);
		ArrayList<PixelElementSegment> frameSegments = pixelElementTable.getFrameSegments();
		
		if(frameSegments.size() == 0)
		{
			String msg = "Error: Pixel Element Segment is 0. Can Not continue this process. Thus, terminate here.";
			LogWriter.writeLog(msg, "Not need to Specify");
		}

		// for(PixelElementSegment frameSegment : frameSegments )
		// System.out.println("[PixelElementSegment] frameSegment
		// id:"+frameSegment.getId()+" area:"+frameSegment.getArea());

		PixelElementSegment frameSegment = null;
		int maxSegmentIndex = 0; // the index of the segment with largest pixel
									// area
		int maxSegmentArea = 0; // the largest pixel area

		// loop through all the segments to find the segment with largest pixel
		// area
		for (int i = 0; i < frameSegments.size(); i++) {
			frameSegment = frameSegments.get(i);

			if (frameSegment.getArea() > maxSegmentArea) {
				maxSegmentArea = frameSegment.getArea();
				maxSegmentIndex = i;
			}
		}

		// System.out.println("largest segment id:"+maxSegmentIndex+",
		// area:"+maxSegmentArea);

//		PixelElement pixelElementInside = frameSegments.get(maxSegmentIndex).getPixelElements().get(0);
//		
//		PixelElementSegment frameSegmentBigObj = getWholeSegment(imagePlusSegments, pixelElementInside.getPoint(), 128);
//		
		ImagePlus imagePlusBigObj = imagePlusSegments.duplicate();

		// make imagePlusBinary contain only white points
		for (int y = 0; y < imagePlusBigObj.getHeight(); y++)
			for (int x = 0; x < imagePlusBigObj.getWidth(); x++)
				imagePlusBigObj.getProcessor().putPixel(x, y, 0);

		// print all the black pixels of the largest segment (object) on the
		// imagePlus
		for (PixelElement pixelElement : frameSegments.get(maxSegmentIndex).getPixelElements()) 
		{
			PixelPutter.putPixels(imagePlusBigObj, pixelElement.getPoint(), 1, 1, 255);
		}
				
//		for (PixelElement pixelElement : frameSegmentBigObj.getPixelElements()) {
//			// System.out.println("point("+pixelElement.getPoint().x+","+pixelElement.getPoint().y+")");
//			PixelPutter.putPixels(imagePlusBigObj, pixelElement.getPoint(), 1, 1, 255);
//		}
		
		return imagePlusBigObj;
	}

	public static ImagePlus getLargestBinaryObjectOverlap(ImagePlus imagePreviousBigObj, ImagePlus imagePlusSegments) {
		
		imagePlusSegments = imagePlusSegments.duplicate();

//		IJ.run(imagePlusSegments, "Fill Holes", "");
		
		// convert the imagePlus pixel table to roiFrameDescription pixel
		// element table
		PixelElementTable pixelElementTable = new PixelElementTable(imagePlusSegments, 128);
	
		 //if the previous larva exists
		 if( imagePreviousBigObj != null )
		 {
			 // create overlapped ROIFrameDescription pixel element table
			 PixelElementTable pixelElementTablePrevious = new PixelElementTable(imagePreviousBigObj, 128);
			 pixelElementTable = pixelElementTable.overlap(pixelElementTablePrevious);
		 }

		// find all the segments (objects) on the roiFrameDescription pixel
		// element table
		// ArrayList<PixelElementSegment> frameSegments =
		// ImageManager.getFrameSegments(pixelElementTable);
		ArrayList<PixelElementSegment> frameSegments = pixelElementTable.getFrameSegments();
		
		if(frameSegments.size() == 0)
		{
			String msg = "Error: Pixel Element Segment is 0. Can Not continue this process. Thus, terminate here.";
			LogWriter.writeLog(msg, "Not need to Specify");
		}

		// for(PixelElementSegment frameSegment : frameSegments )
		// System.out.println("[PixelElementSegment] frameSegment
		// id:"+frameSegment.getId()+" area:"+frameSegment.getArea());

		PixelElementSegment frameSegment = null;
		int maxSegmentIndex = 0; // the index of the segment with largest pixel
									// area
		int maxSegmentArea = 0; // the largest pixel area

		// loop through all the segments to find the segment with largest pixel
		// area
		for (int i = 0; i < frameSegments.size(); i++) {
			frameSegment = frameSegments.get(i);

			if (frameSegment.getArea() > maxSegmentArea) {
				maxSegmentArea = frameSegment.getArea();
				maxSegmentIndex = i;
			}
		}

		// System.out.println("largest segment id:"+maxSegmentIndex+",
		// area:"+maxSegmentArea);

		

		// print all the black pixels of the largest segment (object) on the
		// imagePlus
//		for (PixelElement pixelElement : frameSegments.get(maxSegmentIndex).getPixelElements()) {
//			// System.out.println("point("+pixelElement.getPoint().x+","+pixelElement.getPoint().y+")");
//			PixelPutter.putPixels(imagePlusBigObj, pixelElement.getPoint(), 1, 1, 255);
//		}
		
		PixelElement pixelElementInside = frameSegments.get(maxSegmentIndex).getPixelElements().get(0);
		
		PixelElementSegment frameSegmentBigObj = getWholeSegment(imagePlusSegments, pixelElementInside.getPoint(), 128);
		
		ImagePlus imagePlusBigObj = imagePlusSegments.duplicate();

		// make imagePlusBinary contain only white points
		for (int y = 0; y < imagePlusBigObj.getHeight(); y++)
			for (int x = 0; x < imagePlusBigObj.getWidth(); x++)
				imagePlusBigObj.getProcessor().putPixel(x, y, 0);
		
		for (PixelElement pixelElement : frameSegmentBigObj.getPixelElements()) {
			// System.out.println("point("+pixelElement.getPoint().x+","+pixelElement.getPoint().y+")");
			PixelPutter.putPixels(imagePlusBigObj, pixelElement.getPoint(), 1, 1, 255);
		}
		
		return imagePlusBigObj;
	}

	public static ArrayList<PixelElementSegment> getFrameSegments7(PixelElementTable roiFrameDescription) {
		ArrayList<PixelElementSegment> frameSegments = new ArrayList<PixelElementSegment>();

		Queue<PixelElement> queueDescription = new LinkedList<PixelElement>();
		Queue<PixelElement> queueSegment = new LinkedList<PixelElement>();

		int rowMaxNum = roiFrameDescription.getPixelElements().length;
		int columnMaxNum = roiFrameDescription.getPixelElements()[0].length;

		for (int y = 0; y < rowMaxNum; y++)
			for (int x = 0; x < columnMaxNum; x++)
				queueDescription.add(roiFrameDescription.getPixelElements()[x][y]);

		PixelElement pixelElementCurrent = null;
		PixelElement[] pixelElementNeighbors = new PixelElement[9];

		int x = 0;
		int y = 0;
		int area = 0;
		// int xNeighbor = 0;
		// int yNeighbor = 0;

		while (!queueDescription.isEmpty()) {
			pixelElementCurrent = queueDescription.remove();
			// System.out.println("pixelElementCurrent:("+pixelElementCurrent.getPoint().x+","+
			// pixelElementCurrent.getPoint().y+"):"+pixelElementCurrent.getValue());

			// if the pixel element is a black pixel, which is 255 and
			// it has NOT been visited
			if (pixelElementCurrent.getValue() == 255 && pixelElementCurrent.getVisited() == false) {
				area = 0;
				pixelElementCurrent.setVisited(true);
				area++;

				PixelElementSegment frameSegment = new PixelElementSegment();
				frameSegments.add(frameSegment);
				frameSegment.getPixelElements().add(pixelElementCurrent);
				frameSegment.setId(pixelElementCurrent.getId());
				queueSegment.add(pixelElementCurrent);

				while (!queueSegment.isEmpty()) {
					pixelElementCurrent = queueSegment.remove();

					x = pixelElementCurrent.getPoint().x;
					y = pixelElementCurrent.getPoint().y;

					// if the pixelElementCurrent is not the first row and the
					// first column
					if (x > 0 && y > 0)
						// top left pixel element
						pixelElementNeighbors[0] = roiFrameDescription.getPixelElements()[x - 1][y - 1];
					else
						pixelElementNeighbors[0] = null;

					if (y > 0)
						// top pixel element
						pixelElementNeighbors[1] = roiFrameDescription.getPixelElements()[x][y - 1];
					else
						pixelElementNeighbors[1] = null;

					if (x < columnMaxNum - 1 && y > 0)
						// top right pixel element
						pixelElementNeighbors[2] = roiFrameDescription.getPixelElements()[x + 1][y - 1];
					else
						pixelElementNeighbors[2] = null;

					if (x > 0)
						// left pixel element
						pixelElementNeighbors[3] = roiFrameDescription.getPixelElements()[x - 1][y];
					else
						pixelElementNeighbors[3] = null;

					if (x < columnMaxNum - 1)
						// right pixel element
						pixelElementNeighbors[4] = roiFrameDescription.getPixelElements()[x + 1][y];
					else
						pixelElementNeighbors[4] = null;

					if (x > 0 && y < columnMaxNum - 1)
						// bottom left pixel element
						pixelElementNeighbors[5] = roiFrameDescription.getPixelElements()[x - 1][y + 1];
					else
						pixelElementNeighbors[5] = null;

					if (y < columnMaxNum - 1)
						// Bottom pixel element
						pixelElementNeighbors[6] = roiFrameDescription.getPixelElements()[x][y + 1];
					else
						pixelElementNeighbors[6] = null;

					if (x < columnMaxNum - 1 && y < columnMaxNum - 1)
						// bottom right pixel element
						pixelElementNeighbors[7] = roiFrameDescription.getPixelElements()[x + 1][y + 1];
					else
						pixelElementNeighbors[7] = null;

					// loop through all pixel elements
					for (int i = 0; i < 8; i++) {
						if (pixelElementNeighbors[i] != null) {
							// if the pixel element is black and has NOT been
							// visited
							if (pixelElementNeighbors[i].getValue() == 255
									&& pixelElementNeighbors[i].getVisited() == false) {
								pixelElementNeighbors[i].setVisited(true);
								frameSegment.getPixelElements().add(pixelElementNeighbors[i]);
								area++;
								queueSegment.add(pixelElementNeighbors[i]);
							}
						}
					}
				}

				frameSegment.setArea(area);

			}

		}
		return frameSegments;
	}

	// public static ROIFrameDescription getROIFrameDescription(ImagePlus
	// imagePlus, int threshold)
	// {
	// ROIFrameDescription roiFrameDescription = new ROIFrameDescription();
	// int id = 0;
	// for(int y = 0; y < imagePlus.getHeight(); y++)
	// for(int x = 0; x < imagePlus.getWidth(); x++)
	// // if the pixel is greater than or equal the threshold, add PixelElement
	// with value of 255, black
	// // I use the half of 255, i.e. 128
	// if( imagePlus.getProcessor().getPixel(x, y) >= threshold )
	// roiFrameDescription.getPixelElements()[x][y] = new PixelElement(id++,
	// 255, new Point(x, y) );
	// // if the pixel is less than the threshold, add PixelElement with value
	// of 0, white
	// else
	// roiFrameDescription.getPixelElements()[x][y] = new PixelElement(id++, 0,
	// new Point(x, y) );
	//
	// return roiFrameDescription;
	// }

	public static void mapColorPixels(ImagePlus imagePlusFrom, ImagePlus imagePlusTo, Point pointStart, int threshold,
			Color color) {
		int[] colorMark = new int[] { color.getRed(), color.getGreen(), color.getBlue() };

		for (int y = 0; y < imagePlusFrom.getHeight(); y++)
			for (int x = 0; x < imagePlusFrom.getWidth(); x++)
				if (imagePlusFrom.getProcessor().getPixel(x, y) > threshold)
					imagePlusTo.getProcessor().putPixel(pointStart.x + x, pointStart.y + y, colorMark);
	}

	/**
	 * Make a mark on an image.
	 * 
	 * @param x
	 *            The X coordinator.
	 * @param y
	 *            The Y coordinator.
	 * @param row
	 *            The number of row (pixels) will be marked.
	 * @param column
	 *            The number of column (pixels) will be marked.
	 * @param imageProcessor
	 *            The image processor to be marked.
	 * @param color
	 *            The color used to mark with.
	 * @return void
	 */
	public static void markImage(int x, int y, int row, int column, ImageProcessor imageProcessor, Color color) {
		int[] colorMark = new int[] { color.getRed(), color.getGreen(), color.getBlue() };
		// System.out.println("red:"+ color.getRed() +",green:"+
		// color.getGreen() +",bule:"+ color.getBlue() );

		int rowHalf = row / 2;
		int columnHalf = column / 2;

		for (int i = y - columnHalf; i < y + columnHalf; i++) {
			for (int j = x - rowHalf; j < x + rowHalf; j++) {
				imageProcessor.putPixel(j, i, colorMark);
			}
		}

		// int[] colorMark = new int[]{255,0,0};
		// int colorMark = 10; // any byte value from 0-255 since this is a
		// ByteImage (8-bit)
		// image.putPixel(100,100,colorMark); // this is your 'setPixels'
	}

	/**
	 * Annotate on an image.
	 * 
	 * @param x
	 *            The X coordinator.
	 * @param y
	 *            The Y coordinator.
	 * @param fontSize
	 *            The font size of the annotation.
	 * @param text
	 *            The text size of the annotation.
	 * @param imageProcessor
	 *            The image processor to be annotated.
	 * @param color
	 *            The color used to annotate with.
	 * @return void
	 */
	// public static void annotateImage(Point point, int fontSize, String text,
	// ImagePlus imagePlus, Color color)
	// {
	// Font font = new Font("SansSerif", Font.PLAIN, fontSize);
	// TextRoi roi = new TextRoi(point.x, point.y, text, font);
	// //roi.setStrokeColor(Color.green);
	// Overlay overlay = new Overlay();
	// overlay.add(roi);
	// //Dupimp.setOverlay(overlay);
	//
	// imagePlus.getProcessor().setOverlay(overlay);
	//
	// imagePlus.getProcessor().setColor(color);
	// roi.drawPixels(imagePlus.getProcessor());
	// }

	/**
	 * Annotate on an image.
	 * 
	 * @param x
	 *            The X coordinator.
	 * @param y
	 *            The Y coordinator.
	 * @param fontSize
	 *            The font size of the annotation.
	 * @param text
	 *            The text size of the annotation.
	 * @param imageProcessor
	 *            The image processor to be annotated.
	 * @param color
	 *            The color used to annotate with.
	 * @return void
	 */
	public static void annotateImage2(Point point, int fontSize, String text, ImagePlus imagePlus, Color color) {
		Font font = new Font("SansSerif", Font.PLAIN, fontSize);
		TextRoi roi = new TextRoi(point.x, point.y, text, font);
		// roi.setStrokeColor(Color.green);
		Overlay overlay = new Overlay();
		overlay.add(roi);
		// Dupimp.setOverlay(overlay);

		imagePlus.getProcessor().setOverlay(overlay);

		imagePlus.getProcessor().setColor(color);
		roi.drawPixels(imagePlus.getProcessor());
	}

	/**
	 * Get the AVI full frame and crop the full frame to a ROI frame.
	 * 
	 * @param frameId
	 *            The frame id of AVI file
	 * @param roiStart
	 *            The ROI set before the image process starts
	 * @param aviFile
	 *            The path of the AVI file
	 * @param DIR_IMAGE_TEMP
	 *            The path to save the intermediate image frame
	 * @return The cropped ImagePlus
	 */
	public static ImagePlus cropFrame(int frameId, Roi roiStart, String aviFile, String DIR_IMAGE_TEMP) 
	{
		
		AVI_Reader videoFeed = null;
		ImageStack stackFrame = null;

		videoFeed = new AVI_Reader(); // read AVI
		stackFrame = videoFeed.makeStack(aviFile, frameId, frameId, false, false, false);

		if (stackFrame == null || (stackFrame.isVirtual() && stackFrame.getProcessor(1) == null)) {
			System.out.println("stackFrame == null || (stackFrame.isVirtual()&&stackFrame.getProcessor(1) == null)");
			return null;
		}

		ImagePlus imagePlusCurrent = new ImagePlus();
		// Grab frame to be processed
		imagePlusCurrent.setProcessor(stackFrame.getProcessor(1));

		// ----- crop image from the avi image to the size of region of interest
		// -----
		ImagePlus imagePlusCrop = new ImagePlus();
		// scan image is used for finding the fly larva
		imagePlusCrop.setImage(imagePlusCurrent.duplicate());
		imagePlusCrop.setRoi(roiStart);
		// An ROI is created, and crop out. It is the location of the previous
		// Larvae
		imagePlusCrop.setProcessor(imagePlusCrop.getProcessor().crop());

//		ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP, "Blue_Crop_" + frameId, imagePlusCrop);

		return imagePlusCrop;
	}

	/**
	 * Convert the image plus to binary image.
	 * 
	 * @param imagePlusToBinary
	 *            Image plus will be converted to binary.
	 * @param frameId
	 *            The frame the image plus belonging to.
	 * @param DIR_IMAGE_TEMP
	 *            The temporary image saved directory.
	 * @return ImagePlus The converted image plus if succeed, otherwise, null.
	 */
//	public static ImagePlus getBinaryForChrimsonStimulus(ImagePlus imagePlusToBinary, int frameId,
//			String DIR_IMAGE_TEMP) 
//	{
//		
//		imagePlusToBinary = imagePlusToBinary.duplicate();
//		BackgroundSubtracter removeBack = new BackgroundSubtracter();
//		removeBack.rollingBallBackground(imagePlusToBinary.getProcessor(), 120, false, false, false, false, true); // 25
//
//		ImageConverter ic = new ImageConverter(imagePlusToBinary);
//		ic.convertToGray32();
//
//		applyShortOrFloatThreshold(imagePlusToBinary);
//
//		Binary pro = new Binary();
//		pro.setup("fill holes", imagePlusToBinary);
//		pro.run(imagePlusToBinary.getProcessor());
//
//		imagePlusToBinary.getProcessor().erode();
//		for (int i = 0; i < imagePlusToBinary.getHeight(); i++) {
//
//			for (int j = 0; j < imagePlusToBinary.getWidth(); j++) {
//				if (imagePlusToBinary.getProcessor().getPixel(j, i) == 255) {
//					imagePlusToBinary.getProcessor().putPixel(j, i, 0);
//				} else {
//					imagePlusToBinary.getProcessor().putPixel(j, i, 255);
//				}
//			}
//		}
//
//		ObjectDetection deNose = new ObjectDetection(); // Class to remove
//														// objects
//		int tags[] = deNose.run(imagePlusToBinary.getProcessor()); // Which
//																	// pixels
//																	// are the
//																	// fly larva
//
//		for (int i = 0; i < imagePlusToBinary.getHeight(); i++)
//			for (int j = 0; j < imagePlusToBinary.getWidth(); j++)
//				imagePlusToBinary.getProcessor().putPixel(j, i, 0);
//
//		int largObj = deNose.getLargestObject();
//		for (int i = 0; i < imagePlusToBinary.getHeight(); i++) {
//
//			for (int j = 0; j < imagePlusToBinary.getWidth(); j++) {
//				if ((tags[i * imagePlusToBinary.getWidth() + j] == largObj)) {
//					imagePlusToBinary.getProcessor().putPixel(j, i, 255);
//				} else {
//					imagePlusToBinary.getProcessor().putPixel(j, i, 0);
//				}
//			}
//		}
//
//		RankFilters meanFilter = new RankFilters();
//		meanFilter.rank(imagePlusToBinary.getProcessor(), 6, RankFilters.MEAN);
//		imagePlusToBinary.getProcessor().autoThreshold();
//
//		// SaveImages.savesImages(num,1, imageToBinay);
//
//		// ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP, "Blue_Binary_" +
//		// frameId, imagePlusToBinary);
//
//		return imagePlusToBinary;
//	}

	/**
	 * Convert the image plus to binary image.
	 * 
	 * @param imagePlusToBinary
	 *            Image plus will be converted to binary.
	 * @param frameId
	 *            The frame the image plus belonging to.
	 * @param DIR_IMAGE_TEMP
	 *            The temporary image saved directory.
	 * @return ImagePlus The converted image plus if succeed, otherwise, null.
	 */
	public static ImagePlus getBinaryForBlueStimulus(ImagePlus imagePlusToBinary, int frameId, String DIR_IMAGE_TEMP) 
	{
		
		imagePlusToBinary = imagePlusToBinary.duplicate();
		BackgroundSubtracter removeBack = new BackgroundSubtracter();
		removeBack.rollingBallBackground(imagePlusToBinary.getProcessor(), 125, false, false, true, false, false);

		ImageConverter ic = new ImageConverter(imagePlusToBinary);
//		ic.convertToGray16();
		ic.convertToGray8();

		AutoThresholder autoThr = new AutoThresholder();

		int threshold = autoThr.getThreshold("Minimum", imagePlusToBinary.getProcessor().getHistogram());

		if (threshold == -1)
			return null;

		imagePlusToBinary.getProcessor().threshold(threshold);
		imagePlusToBinary.setProcessor(imagePlusToBinary.getProcessor().convertToByteProcessor());
		Binary pro = new Binary();
		imagePlusToBinary.getProcessor().invertLut();
		pro.setup("fill holes", imagePlusToBinary);
		pro.run(imagePlusToBinary.getProcessor());

		// ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP, "Blue_Binary_" +
		// frameId, imagePlusToBinary);

		return imagePlusToBinary;
	}

//	public static ImagePlus getBinary(ImagePlus imagePlusCrop, int frameId, String DIR_IMAGE_TEMP) {
	public static ImagePlus getBinary(ImagePlus imagePlusCrop) 
	{
		
		ImagePlus imagePlusBinary = imagePlusCrop.duplicate();

		BackgroundSubtracter removeBack = new BackgroundSubtracter();
		removeBack.rollingBallBackground(imagePlusBinary.getProcessor(), 25, false, false, false, false, true); // 25

//		IJ.run(imagePlusBinary, "HSB Stack", "");
//		imagePlusBinary.setSlice(3);
//		IJ.setAutoThreshold(imagePlusBinary, "Triangle dark");
//		Prefs.blackBackground = true;
//		IJ.run(imagePlusBinary, "Convert to Mask", "only");
//		IJ.run(imagePlusBinary, "Invert", "");
		
		
		// convert to 8 gray image
		ImageConverter imageConverter = new ImageConverter(imagePlusBinary);
		imageConverter.convertToGray8();

		// set auto threshold
//		IJ.setAutoThreshold(imagePlusBinary, "Huang");
//		IJ.setAutoThreshold(imagePlusBinary, "Triangle");
		
		// Convert to Mask
		IJ.run(imagePlusBinary, "Convert to Mask", "");
//		imagePlusBinary.getProcessor().invert();
		
		// IJ.run(imagePlusBinary, "Fill Holes", "");
		// IJ.run(imagePlusBinary, "Watershed", "");
		// IJ.run(imagePlusBinary, "Dilate", "");
		// IJ.run(imagePlusBinary, "Erode", "");

		// ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP, "Blue_Binary_" +
		// frameId, imagePlusBinary);

		return imagePlusBinary;
	}

	// public static ImagePlus getBinary(ImagePlus imagePlusCrop, int frameId,
	// String DIR_IMAGE_TEMP)
	// {
	// ImagePlus imagePlusBinary = imagePlusCrop.duplicate();
	//
	// BackgroundSubtracter removeBack = new BackgroundSubtracter();
	// removeBack.rollingBallBackground(imagePlusBinary.getProcessor(), 120,
	// false, false, false, false, true); //25
	//
	// // convert to 32 gray image
	// ImageConverter imageConverter = new ImageConverter(imagePlusBinary);
	// imageConverter.convertToGray32();
	//
	// // make binary, fill holes, dilate, and erode the binary image
	// IJ.run(imagePlusBinary, "Make Binary", "");
	// IJ.run(imagePlusBinary, "Fill Holes", "");
	// //IJ.run(imagePlusBinary, "Dilate", "");
	// IJ.run(imagePlusBinary, "Erode", "");
	//
	// ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP, "Blue_Binary_" + frameId,
	// imagePlusBinary);
	//
	// return imagePlusBinary;
	// }

	public static ImagePlus getEdge(ImagePlus imagePlusBinary, int frameId, String DIR_IMAGE_TEMP) {
		ImagePlus imagePlusEdge = imagePlusBinary.duplicate();

		IJ.run(imagePlusEdge, "Find Edges", "");
		IJ.run(imagePlusEdge, "Skeletonize", "");

		// imagePlusEdge = ImageManager.removeTripleBlock(imagePlusEdge);

//		ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP, "Blue_Edge_" + frameId, imagePlusEdge);

		return imagePlusEdge;
	}

//	public static ImagePlus getSkeletonShwan7(ImagePlus imagePlusBinary, int frameId, String DIR_IMAGE_TEMP) 
//	{
//		
//		ImagePlus imagePlusSkeleton = imagePlusBinary.duplicate();
//
//		// Dilate to expanse 3 more pixels for the binary image
//		// because Skeletonize command will Erode a few pixels from both ends
//		IJ.run(imagePlusSkeleton, "Dilate", "");
//		// skeletonize the binary image to get the skeleton
//		IJ.run(imagePlusSkeleton, "Skeletonize", "");
//		SpurRemover.removeSpurs(imagePlusSkeleton); // Clean up the skeleton
//
//		return imagePlusSkeleton;
//
//		// ----- make binary image from the cropped image -----
//		// ImagePlus imagePlusBinary = new ImagePlus();
//		// imagePlusBinary.setImage(imagePlusCrop.duplicate());
//		// ImagePlus imagePlusBinary = imagePlusCrop.duplicate();
//		//
//		// BackgroundSubtracter removeBack = new BackgroundSubtracter();
//		// removeBack.rollingBallBackground(imagePlusBinary.getProcessor(), 120,
//		// false, false, false, false, true); //25
//		//
//		// // convert to 32 gray image
//		// ImageConverter imageConverter = new ImageConverter(imagePlusBinary);
//		// imageConverter.convertToGray32();
//		//
//		// // make binary, fill holes, dilate, and erode the binary image
//		// IJ.run(imagePlusBinary, "Make Binary", "");
//		// IJ.run(imagePlusBinary, "Fill Holes", "");
//		// IJ.run(imagePlusBinary, "Dilate", "");
//		// IJ.run(imagePlusBinary, "Erode", "");
//		//
//		// ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP, "Blue_Binary_" +
//		// frameId, imagePlusBinary);
//
//		// generate the edge for the larva
//		// ImagePlus imagePlusEdge = new ImagePlus();
//		// imagePlusEdge.setImage(imagePlusBinary.duplicate());
//		// ImagePlus imagePlusEdge = imagePlusBinary.duplicate();
//		//
//		// IJ.run(imagePlusEdge, "Find Edges", "");
//		// //IJ.run(imagePlusEdge, "Skeletonize", "");
//		// ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP, "Blue_Edge_" + frameId,
//		// imagePlusEdge);
//
//		// ImageSaver.saveImagesWithPath("E:\\Summer 2016\\Larva
//		// Project\\Output\\Test\\Images_Temp\\", "Blue_Skeleton_" + 1,
//		// imagePlusSkeleton);
//
//		// imagePlusSkeleton =
//		// ImageManager.removeTripleBlock(imagePlusSkeleton);
//	}

	private static void applyShortOrFloatThreshold(ImagePlus imp) {
		if (!imp.lock())
			return;

		int width = imp.getWidth();
		int height = imp.getHeight();
		int size = width * height;

		boolean isFloat = imp.getType() == ImagePlus.GRAY32;
		int currentSlice = imp.getCurrentSlice();

		int nSlices = imp.getStackSize();
		ImageStack stack1 = imp.getStack();
		ImageStack stack2 = new ImageStack(width, height);
		ImageProcessor ip = imp.getProcessor();

		float t1 = (float) ip.getMinThreshold();
		float t2 = (float) ip.getMaxThreshold();

		if (t1 == ImageProcessor.NO_THRESHOLD) {
			double min = ip.getMin();
			double max = ip.getMax();

			ip = ip.convertToByte(true);

			// autoThreshold(ip);

			ip.setAutoThreshold(ImageProcessor.ISODATA2, ImageProcessor.NO_LUT_UPDATE);
			double minThreshold = ip.getMinThreshold();
			double maxThreshold = ip.getMaxThreshold();

			t1 = (float) (min + (max - min) * (minThreshold / 255.0));
			t2 = (float) (min + (max - min) * (maxThreshold / 255.0));
		}

		float value;
		ImageProcessor ip1, ip2;

		for (int i = 1; i <= nSlices; i++) {
			String label = stack1.getSliceLabel(i);
			ip1 = stack1.getProcessor(i);
			ip2 = new ByteProcessor(width, height);
			for (int j = 0; j < size; j++) {
				value = ip1.getf(j);
				if (value >= t1 && value <= t2)
					ip2.set(j, 255);
				else
					ip2.set(j, 0);
			}
			stack2.addSlice(label, ip2);
		}

		imp.setStack(null, stack2);
		ImageStack stack = imp.getStack();
		stack.setColorModel(LookUpTable.createGrayscaleColorModel(!Prefs.blackBackground));
		imp.setStack(null, stack);

		if (imp.isComposite()) {
			CompositeImage ci = (CompositeImage) imp;
			ci.setMode(ImagePlus.GRAY32);
			ci.resetDisplayRanges();
			ci.updateAndDraw();
		}

		imp.getProcessor().setThreshold(255, 255, ImageProcessor.NO_LUT_UPDATE);
		imp.unlock();
	}

	public static boolean detectImageColor(ImagePlus imagePlusColor) {
		ImagePlus result = imagePlusColor.duplicate(); // use the copied one
		imagePlusColor = imagePlusColor.duplicate(); // use the copied one

		// Color Detection
		ImageStack rgbStack[] = ChannelSplitter.splitRGB(imagePlusColor.getImageStack(), true);

		imagePlusColor.setStack(rgbStack[2]);
		int blueSum = 0;
		for (int intsity = 0; intsity < 256; intsity++) {
			blueSum += intsity * imagePlusColor.getProcessor().getHistogram()[intsity];
		}

		imagePlusColor.setStack(rgbStack[0]);
		int redSum = 0;
		for (int intsity = 0; intsity < 256; intsity++) {
			redSum += intsity * imagePlusColor.getProcessor().getHistogram()[intsity];
		}

		// TextFileWriter.writeToFile("blueSum:"+blueSum+", redSum:"+redSum);

		if (blueSum >= redSum / 1.5) {
			imagePlusColor.setImage(result.duplicate());
			return true;
		}

		imagePlusColor.setStack(rgbStack[0]);
		return false;
	} // end of color detection

	public static boolean checkBlackFrame(ImagePlus imagePlusColor) {
		
		ImagePlus imagePlusCheck = imagePlusColor.duplicate(); 

		// Detect if it is a completely black image. If yes, return null.
		// ImagePlus testBlack = new ImagePlus();
		// testBlack.setProcessor(frames.getProcessor(1));

		int pixelValuesSum = 0;
		int[] valueHolder;

		for (int i = 0; i < imagePlusCheck.getProcessor().getWidth(); i++) 
		{
			valueHolder = imagePlusCheck.getProcessor().getPixel(i, imagePlusCheck.getProcessor().getHeight() / 2,
					null);
			pixelValuesSum += (valueHolder[0] + valueHolder[1] + valueHolder[2]);
		}

		long pixelValueAvg = pixelValuesSum / imagePlusCheck.getProcessor().getWidth() / 3;

//		 System.out.println("avg intensity in the center row: " + pixelValueAvg);

		// Some black frame in chrimson video has a uniform intensity of 16
		// (12/10/2015)
//		if (pixelValueAvg < 20) {
		if (pixelValueAvg < 10) {
//			 System.out.println("detected a black frame (average intensity less than 20)");

			// this is a place holder for return value to indicate if
			// segmentation has failed (if true, segmentation failed).
			return true;
		}

		return false;
	}
}
