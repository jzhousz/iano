package entities;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import ij.ImagePlus;
import manager.ImageManager;

/**
* The class contains the information about a single pixel element table.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class PixelElementTable 
{
	// all the elements in the description
	private PixelElement[][] pixelElements = null; 
	// all the segments in the description
	private ArrayList<PixelElementSegment> frameSegments = null; 
	public int NUM_ROW = 120;
	public int NUM_COLUMN = 120;
		
	/**
	 * A constructor.
	 */
	public PixelElementTable()
	{
		// ??? need to 120 with NUM_COLUMN and NUM_ROW
		pixelElements = new PixelElement[120][120]; // two value, black: 255, white: 0
		
		int id = 0;
		// ??? need to replace NUM_COLUMN and NUM_ROW with each other
		for(int y = 0; y < NUM_COLUMN; y++)
			for( int x = 0; x < NUM_ROW; x++ )
				pixelElements[y][x] = new PixelElement(id++, 0, new Point(x, y) );
			
		frameSegments = new ArrayList<PixelElementSegment>();
	}
	
	/**
	* A class converts all pixels in an image to an table for processing.
	* 
	* @param imagePlus The image plus.
	* @param isBinary Is the image plus binary image? 
	* true: it's binary image plus, false: it's a color image plus.
	*/
	public PixelElementTable(ImagePlus imagePlus, boolean isBinary)
	{
		NUM_ROW = imagePlus.getHeight();
		NUM_COLUMN = imagePlus.getWidth();
		
		pixelElements = new PixelElement[NUM_ROW][NUM_COLUMN]; // two value, black: 255, white: 0
		
		//ROIFrameDescription roiFrameDescription = new ROIFrameDescription();
		int id = 0;
		for(int y = 0; y < imagePlus.getHeight(); y++)
			for(int x = 0; x < imagePlus.getWidth(); x++)
			{
				pixelElements[y][x] = new PixelElement(id++, imagePlus.getProcessor().getPixel(x, y), new Point(x, y) );
			}
		
		frameSegments = new ArrayList<PixelElementSegment>();
	}
	
	/**
	 * A constructor.
	 * 
	 * @param imagePlus The image plus used to build the pixel element table.
	 * @param threshold The threshold of pixels in an image.
	 */
	public PixelElementTable(ImagePlus imagePlus, int threshold)
	{
		this();
		
		//ROIFrameDescription roiFrameDescription = new ROIFrameDescription();
		int id = 0;
		for(int y = 0; y < imagePlus.getHeight(); y++)
			for(int x = 0; x < imagePlus.getWidth(); x++)
				// if the pixel is greater than or equal the threshold, add PixelElement with value of 255, black
				// I use the half of 255, i.e. 128
				if( imagePlus.getProcessor().getPixel(x, y) >= threshold )
					pixelElements[y][x] = new PixelElement(id++, 255, new Point(x, y) );
				// if the pixel is less than the threshold, add PixelElement with value of 0, white
				else
					pixelElements[y][x] = new PixelElement(id++, 0, new Point(x, y) );
		
	}
	
	/**
	* Overlap a PixelElementTable with another.
	* 
	* @param pixelElementTable The pixel Element Table.
	* @return The Pixel Element Table.
	*/
	public PixelElementTable overlap(PixelElementTable pixelElementTable)
	{
		PixelElementTable pixelElementTableNew = new PixelElementTable();
		for(int y = 0; y < pixelElements.length; y++)
			for(int x = 0; x < pixelElements[0].length; x++)
				// if both ROIFrameDescriptions's pixel elements contain the same value, assign the value to
				// the element in the same position of the new ROIFrameDescription. 
				if( pixelElements[y][x].getValue() == pixelElementTable.getPixelElements()[y][x].getValue() )
				{
					int value = pixelElements[y][x].getValue();
					PixelElement[][] pe = pixelElementTableNew.getPixelElements();
					pe[y][x].setValue( value );
				}
			
		return pixelElementTableNew;
	}
	
	public PixelElement[][] getPixelElements() {
		return pixelElements;
	}

	public void setPixelElements(PixelElement[][] pixelElements) {
		this.pixelElements = pixelElements;
	}

	/**
	* Get segments from the pixel element table.
	* 
	* @return The Pixel Element Segment.
	*/
	public ArrayList<PixelElementSegment> getFrameSegments() 
	{
		ArrayList<PixelElementSegment> frameSegments = new ArrayList<PixelElementSegment>();

		Queue<PixelElement> queueDescription = new LinkedList<PixelElement>();
		Queue<PixelElement> queueSegment = new LinkedList<PixelElement>();

		int rowMaxNum = this.getPixelElements().length;
		int columnMaxNum = this.getPixelElements()[0].length;

		for (int y = 0; y < rowMaxNum; y++)
			for (int x = 0; x < columnMaxNum; x++)
				queueDescription.add(this.getPixelElements()[y][x]);

		PixelElement pixelElementCurrent = null;
		PixelElement[] pixelElementNeighbors = new PixelElement[9];

		int x = 0;
		int y = 0;
		int area = 0;

		while (!queueDescription.isEmpty())
		{
			pixelElementCurrent = queueDescription.remove();

			// if the pixel element is a black pixel, which is 255 and
			// it has NOT been visited
			if (pixelElementCurrent.getValue() > 128 && pixelElementCurrent.getVisited() == false)
			{
				area = 0;
				pixelElementCurrent.setVisited(true);
				area++;

				PixelElementSegment frameSegment = new PixelElementSegment();
				frameSegments.add(frameSegment);
				frameSegment.getPixelElements().add(pixelElementCurrent);
				frameSegment.setId(pixelElementCurrent.getId());
				queueSegment.add(pixelElementCurrent);

				while (!queueSegment.isEmpty())
				{
					pixelElementCurrent = queueSegment.remove();

					x = pixelElementCurrent.getPoint().x;
					y = pixelElementCurrent.getPoint().y;

					// just add after removed the code right above it
					pixelElementNeighbors = ImageManager.getNeighborElements(pixelElementCurrent, this);

					// loop through all pixel elements
					for (int i = 0; i < 8; i++)
					{
						if (pixelElementNeighbors[i] != null)
						{
							// if the pixel element is black and has NOT been
							// visited
							if (pixelElementNeighbors[i].getValue() > 128
									&& pixelElementNeighbors[i].getVisited() == false)
							{
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
	
	public String toString()
	{
		String str = "";
		
		for (int y = 0; y < NUM_ROW; y++)
		{
			for (int x = 0; x < NUM_COLUMN; x++)
			{
				str += pixelElements[y][x].getValue() + ", ";
			}
			str += "\n";
		}
		
		return str;
	}

	public void setFrameSegments(ArrayList<PixelElementSegment> frameSegments) {
		this.frameSegments = frameSegments;
	}

	
	
}
