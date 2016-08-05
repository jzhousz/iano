package manager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
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
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

/**
* The class contains static methods used to do calculation on image pluses.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class ImageManager {

	/**
	* Get image plus from a file.
	* 
	* @param file The name of a file.
	* @return The image plus.
	*/
	public static ImagePlus getImagePlusFromFile(String file)
	{
		Opener opener = new Opener();  
		String imageFilePath = file;
		ImagePlus imp = opener.openImage(imageFilePath);
		return imp;
	}
	
	/**
	* Get pixel element segment.
	* 
	* @param imagePlus The image plus checked.
	* @param point The point which the segment includes.
	* @param threshold The threshold with which to find black pixels on the image plus.
	* @return The Pixel Element Segment.
	*/
	public static PixelElementSegment getWholeSegment(ImagePlus imagePlus, Point point, int threshold) 
	{
		PixelElementTable pixelElementTable = new PixelElementTable(imagePlus, threshold);
		
		PixelElementSegment pixelElementSegment = new PixelElementSegment();
		
		PixelElement pixelElementRoot = new PixelElement();
		pixelElementRoot.setPoint(point);

		PixelElement[] pixelElementNeighbors = null;

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
						pixelElementNeighbors[i].setVisited(true);

						pixelElementSegment.getPixelElements().add(pixelElementNeighbors[i]);
						
						queueTransverse.add(pixelElementNeighbors[i]);
						
						
					}

				}
			}
		}
		
		return pixelElementSegment;
	}
	
	/**
	* Get the skeleton length.
	* 
	* @param imagePlusSkeleton The image plus containing the skeleton.
	* @param threshold The threshold with which to find black pixels on the image plus.
	* @return The skeleton length.
	*/
	public static double getSkeletonLength(ImagePlus imagePlusSkeleton, int threshold) 
	{
		PixelElement pixelElementEnd1 = findLongestPixelElement(imagePlusSkeleton, null, threshold);

		PixelElementTable pixelElementTable2 = new PixelElementTable(imagePlusSkeleton, threshold);
		// pixelElementTable2 is passed in the method and will be modified.
		PixelElement pixelElementEnd2 = findLongestPixelElement(pixelElementTable2, pixelElementEnd1, threshold);

		return pixelElementEnd2.getLevel() + 1;
	}
	
	/**
	* Get the stem skeleton.
	* 
	* @param imagePlusSkeleton The image plus containing the skeleton.
	* @param threshold The threshold with which to find black pixels on the image plus.
	* @return The stem skeleton.
	*/
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
	
	/**
	* Get the center of skeleton, 1st and 3ed quartile points.
	* 
	* @param imagePlusSkeleton The image plus containing the skeleton.
	* @param threshold The threshold with which to find black pixels on the image plus.
	* @return The ArrayList of points. The 1st point is the center of skeleton. 
	* 		  The 2ed and 3ed points are 1st and 3ed quartile points.
	*/
	public static ArrayList<Point> findCenterPoints(ImagePlus imagePlusSkeleton, int threshold) 
	{
		ArrayList<Point> points = new ArrayList<Point>();
		
		PixelElement pixelElementEnd1 = findLongestPixelElement(imagePlusSkeleton, null, threshold);
		
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
		
		return points;
	}
	
	/**
	* Get only the center of skeleton
	* 
	* @param imagePlusSkeleton The image plus containing the skeleton.
	* @param threshold The threshold with which to find black pixels on the image plus.
	* @return The the center of skeleton.
	*/
	public static Point findCenterPoint(ImagePlus imagePlusSkeleton, int threshold) 
	{
		PixelElement pixelElementEnd1 = findLongestPixelElement(imagePlusSkeleton, null, threshold);
		
		PixelElementTable pixelElementTable2 = new PixelElementTable(imagePlusSkeleton, threshold);
		
		// pixelElementTable2 is passed in the method and will be modified.
		PixelElement pixelElementEnd2 = findLongestPixelElement(pixelElementTable2, pixelElementEnd1, threshold);
		
		int halfLevel = pixelElementEnd2.getLevel() / 2 + 1;
		
		// loop through all pixel element to find the pixel element with level
		// of half of the highest level
		while(pixelElementEnd2.getLevel() >= halfLevel)
			pixelElementEnd2 = pixelElementEnd2.getAntecedent();
			
		return pixelElementEnd2.getPoint();
	}
	
	/**
	* Get the pixel element with the longest distance to an element.
	* 
	* @param pixelElementTable The pixel Element Table.
	* @param pixelElementRoot The Root pixel Element.
	* @param threshold The threshold with which to find black pixels on the image plus.
	* @return The pixel element with the longest distance to an element.
	*/
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
						}
					}

				}
			}
		}
		
		return pixelElementHighestLevel;
	}
	
	/**
	* Get the pixel element with the longest distance to an element.
	* 
	* @param imagePlusSkeleton The image plus containing the skeleton.
	* @param pixelElementRoot The Root pixel Element.
	* @param threshold The threshold with which to find black pixels on the image plus.
	* @return The pixel element with the longest distance to an element.
	*/
	public static PixelElement findLongestPixelElement(ImagePlus imagePlusSkeleton, PixelElement pixelElementRoot, int threshold) 
	{
		PixelElementTable pixelElementTable = new PixelElementTable(imagePlusSkeleton, threshold);
		
		return findLongestPixelElement(pixelElementTable, pixelElementRoot, threshold);
	}
	
	/**
	* Find both end points of the skeleton of the larva.
	* 
	* @param imagePlusSkeleton The image plus containing the skeleton.
	* @param threshold The threshold with which to find black pixels on the image plus.
	* @return The ArrayList containing both end points of the skeleton of the larva.
	*/
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
	
	/**
	* Shift points.
	* 
	* @param points The points.
	* @param xDifference The x-axis difference.
	* @param yDifference The y-axis difference.
	* @return The ArrayList of points shifted.
	*/
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
	
	/**
	* Shift points.
	* 
	* @param point The point.
	* @param xDifference The x-axis difference.
	* @param yDifference The y-axis difference.
	* @return The point shifted.
	*/
	public static Point shiftPoint(Point point, int xDifference, int yDifference) 
	{
		return new Point(point.x + xDifference, point.y + yDifference);
	}
	
	/**
	* Shift image plus.
	* 
	* @param imagePlus The image plus.
	* @param xDifference The x-axis difference.
	* @param yDifference The y-axis difference.
	* @param threshold The threshold used to get black pixels.
	* @return The shifted image plus.
	*/
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
	
	/**
	* Print image pluses on a image plus.
	* 
	* @param imagePlusColor The image plus.
	* @param imagePlus1 The image plus.
	* @param color1 The color.
	* @param imagePlus2 The image plus.
	* @param color2 The color.
	* @param threshold The threshold used to get black pixels.
	* @return The overlap image plus.
	*/
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

		ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP, "Blue_Test_Watershed2_" + frameId, imagePlusWatershed);

		ImagePlus imagePlusOverlap = ImageManager.overlapImages(imagePlusCrop, imagePlusExpand, Color.blue,
				imagePlusWatershed, Color.red, 128);

		ImageSaver.saveImagesWithPath(DIR_IMAGE_TEMP, "Blue_Test_Overlap2_" + frameId, imagePlusOverlap);

		// convert the image to PixelElementTable
		PixelElementTable peTableShrink = new PixelElementTable(imagePlusWatershed, threshold);
		// get all segments of the image
		ArrayList<PixelElementSegment> peSegments = peTableShrink.getFrameSegments();

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

	/**
	* Get pixel area.
	* 
	* @param imagePlusColor The image plus.
	* @param threshold The threshold used to get black pixels.
	* @return The number of pixels.
	*/
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
			pixelElementNeighbors = getNeighborElements(pixelElementCurrent, pixelElementTable);

			// loop through all 8 neighbor pixel elements
			for (int i = 0; i < 8; i++) {
				if (pixelElementNeighbors[i] != null) {
					// if the pixel element is black and has NOT been visited
					// and the number of level is less
					// than level (the parameter passed in)
					if (pixelElementNeighbors[i].getValue() == 255 && pixelElementNeighbors[i].getVisited() == false
							&& pixelElementCurrent.getLevel() < level) {
						// the successor has 1 more level than the predecessor
						pixelElementNeighbors[i].setLevel(pixelElementCurrent.getLevel() + 1);
						pixelElementNeighbors[i].setVisited(true);

						queueTransverse.add(pixelElementNeighbors[i]);

						amount++;
					}

				}
			}

		}
		return amount;
	}

	public static ImagePlus removeTripleBlock7(ImagePlus imagePlusSkeleton) 
	{
		imagePlusSkeleton = imagePlusSkeleton.duplicate();

		PixelElementTable pixelElementTable = new PixelElementTable(imagePlusSkeleton, 128);

		ArrayList<PixelElementSegment> segments = pixelElementTable.getFrameSegments();

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

			// get all the neighbors of the element
			pixelElementNeighbors = getNeighborElements(pixelElementCurrent, pixelElementTable);

			numNeighbor = 0; // count how many neighbors it has

			// loop through all pixel elements
			for (int i = 0; i < 8; i++) {
				if (pixelElementNeighbors[i] != null) {
					// if the pixel element is black and has NOT been visited
					if (pixelElementNeighbors[i].getValue() == 255 && pixelElementNeighbors[i].getVisited() == false) {
						pixelElementCurrent.setHasSuccessor(true);
						pixelElementNeighbors[i].setVisited(true);

						queueTransverse.add(pixelElementNeighbors[i]);
					}

					// if the pixel element is black
					if (pixelElementNeighbors[i].getValue() == 255) {
						numNeighbor++;
					}
				}
			}

			// if the current pixelElement has more than 2 neighbors
			if (numNeighbor > 2) {
				stackTriple.push(pixelElementCurrent);
			}

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
				imagePlusSkeleton.getProcessor().putPixel(pixelElementCurrent.getPoint().x,
						pixelElementCurrent.getPoint().y, 0);
			}
		}

		return imagePlusSkeleton;
	}

	/**
	* Get neighbor elements.
	* 
	* @param pixelElementCurrent The Current pixel Element.
	* @param pixelElementTable The pixel Element Table.
	* @return The neighbor elements.
	*/
	public static PixelElement[] getNeighborElements(PixelElement pixelElementCurrent,
			PixelElementTable pixelElementTable) 
	{
		
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

	/**
	* Get the largest binary object.
	* 
	* @param imagePreviousBigObj The previous big binary object.
	* @param imagePlusSegments The image plus containing all binary segments.
	* @return The image plus containing the big binary object.
	*/
	public static ImagePlus getLargestBinaryObject(ImagePlus imagePreviousBigObj, ImagePlus imagePlusSegments) 
	{
		
		imagePlusSegments = imagePlusSegments.duplicate();

		// convert the imagePlus pixel table to roiFrameDescription pixel
		// element table
		PixelElementTable pixelElementTable = new PixelElementTable(imagePlusSegments, 128);
	
		// find all the segments (objects) on the roiFrameDescription pixel
		// element table
		ArrayList<PixelElementSegment> frameSegments = pixelElementTable.getFrameSegments();
		
		if(frameSegments.size() == 0)
		{
			String msg = "Error: Pixel Element Segment is 0. Can Not continue this process. Thus, terminate here.";
			LogWriter.writeLog(msg, "Not need to Specify");
		}

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
				
		return imagePlusBigObj;
	}

	/**
	* Get the overlapped binary object.
	* 
	* @param imagePreviousBigObj The previous big binary object.
	* @param imagePlusSegments The image plus containing all binary segments.
	* @return The image plus containing the overlapped binary object.
	*/
	public static ImagePlus getLargestBinaryObjectOverlap(ImagePlus imagePreviousBigObj, ImagePlus imagePlusSegments) {
		
		imagePlusSegments = imagePlusSegments.duplicate();

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
		ArrayList<PixelElementSegment> frameSegments = pixelElementTable.getFrameSegments();
		
		if(frameSegments.size() == 0)
		{
			String msg = "Error: Pixel Element Segment is 0. Can Not continue this process. Thus, terminate here.";
			LogWriter.writeLog(msg, "Not need to Specify");
		}

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

	/**
	* Map an image plus to another image plus.
	* 
	* @param imagePlusFrom The image plus.
	* @param imagePlusTo The image plus.
	* @param pointStart The point starts.
	* @param threshold The threshold with which get black pixels.
	* @param color The color with which map the image plus.
	* @return None.
	*/
	public static void mapColorPixels(ImagePlus imagePlusFrom, ImagePlus imagePlusTo, Point pointStart, int threshold,
			Color color) 
	{
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

		int rowHalf = row / 2;
		int columnHalf = column / 2;

		for (int i = y - columnHalf; i < y + columnHalf; i++) {
			for (int j = x - rowHalf; j < x + rowHalf; j++) {
				imageProcessor.putPixel(j, i, colorMark);
			}
		}

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
	public static void annotateImage2(Point point, int fontSize, String text, ImagePlus imagePlus, Color color) {
		Font font = new Font("SansSerif", Font.PLAIN, fontSize);
		TextRoi roi = new TextRoi(point.x, point.y, text, font);
		Overlay overlay = new Overlay();
		overlay.add(roi);

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

		return imagePlusToBinary;
	}

	/**
	* Get binary image plus from the cropped image plus.
	* 
	* @param imagePlusCrop The cropped image plus.
	* @return The binary image plus.
	*/
	public static ImagePlus getBinary(ImagePlus imagePlusCrop) 
	{
		
		ImagePlus imagePlusBinary = imagePlusCrop.duplicate();

		BackgroundSubtracter removeBack = new BackgroundSubtracter();
		removeBack.rollingBallBackground(imagePlusBinary.getProcessor(), 25, false, false, false, false, true); // 25

		// convert to 8 gray image
		ImageConverter imageConverter = new ImageConverter(imagePlusBinary);
		imageConverter.convertToGray8();

		// Convert to Mask
		IJ.run(imagePlusBinary, "Convert to Mask", "");

		return imagePlusBinary;
	}

	/**
	* Get the edge of the larva.
	* 
	* @param imagePlusBinary The binary image plus.
	* @param frameId The frame id.
	* @param DIR_IMAGE_TEMP The path that save the image plus.
	* @return The edge image plus.
	*/
	public static ImagePlus getEdge(ImagePlus imagePlusBinary, int frameId, String DIR_IMAGE_TEMP) 
	{
		ImagePlus imagePlusEdge = imagePlusBinary.duplicate();

		IJ.run(imagePlusEdge, "Find Edges", "");
		IJ.run(imagePlusEdge, "Skeletonize", "");

		return imagePlusEdge;
	}

	private static void applyShortOrFloatThreshold7(ImagePlus imp) {
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

	/**
	* Detect image color. Written by Shawn.
	* 
	* @param imagePlusColor The color image plus.
	* @return .
	*/
	public static boolean detectImageColor(ImagePlus imagePlusColor) 
	{
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

	/**
	* Check the black frame.
	* 
	* @param imagePlusColor The color image plus.
	* @return true if it's a black frame.
	*/
	public static boolean checkBlackFrame(ImagePlus imagePlusColor) 
	{
		ImagePlus imagePlusCheck = imagePlusColor.duplicate(); 

		int pixelValuesSum = 0;
		int[] valueHolder;

		for (int i = 0; i < imagePlusCheck.getProcessor().getWidth(); i++) 
		{
			valueHolder = imagePlusCheck.getProcessor().getPixel(i, imagePlusCheck.getProcessor().getHeight() / 2,
					null);
			pixelValuesSum += (valueHolder[0] + valueHolder[1] + valueHolder[2]);
		}

		long pixelValueAvg = pixelValuesSum / imagePlusCheck.getProcessor().getWidth() / 3;

		if (pixelValueAvg < 10) {
			return true;
		}

		return false;
	}
}
