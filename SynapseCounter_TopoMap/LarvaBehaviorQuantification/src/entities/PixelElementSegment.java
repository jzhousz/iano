package entities;

import java.util.ArrayList;

/**
* The class contains the information about a single pixel element segment.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class PixelElementSegment {

	private int area = 0; // the area of the segment in term of pixels
	private int id = 0; // the segment id
	// the list contains all the pixels for this segment
	private ArrayList<PixelElement> pixelElements = null;
	
	public PixelElementSegment()
	{
		pixelElements = new ArrayList<PixelElement>();
	}
	
	public int getArea() {
		return area;
	}
	
	public void setArea(int area) {
		this.area = area;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public ArrayList<PixelElement> getPixelElements() {
		return pixelElements;
	}
	public void setPixelElements(ArrayList<PixelElement> pixelElements) {
		this.pixelElements = pixelElements;
	} 
	
	
}
