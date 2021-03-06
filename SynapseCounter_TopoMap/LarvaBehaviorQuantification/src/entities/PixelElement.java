package entities;

import java.awt.Point;

/**
* The class contains the information about a single pixel element.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class PixelElement 
{
	private int value = 0; // the pixel value, black is 255, white is 0
	private int id = 0; // the pixel id in the image frame
	private Point point = null; // the pixel coordinate in the image frame
	boolean visited = false; // whether the pixel has been visited
	private int amount = 0; // the pixel element amount value
	private Boolean hasSuccessor = false; // whether this pixel element derives other pixel elements
	private int level = 0; // the pixel level.
	private int numNeighbors = 0; // number of neighbors the pixel element has
	// the antecedent of this pixelElement
	private PixelElement antecedent = null;
	
	public PixelElement()
	{
		point = new Point();
	}
	
	/**
	 * A constructor.
	 * 
	 * @param id The pixel element id.
	 * @param value The pixel element value.
	 * @param point The pixel element point.
	 */
	public PixelElement(int id, int value, Point point)
	{
		this();
		this.id = id;
		this.value = value;
		this.point = point;
	}
	
	// the copy constructor.
	// copy all the primitive from pe.
	/**
	 * The copy constructor, which copies all the primitive data from a pixel element.
	 * 
	 * @param pe The pixel element.
	 */
	public PixelElement(PixelElement pe)
	{
		this();
		this.id = pe.getId();
		this.value = pe.getValue();
		this.point.x = pe.getPoint().x;
		this.point.y = pe.getPoint().y;
		this.visited = pe.getVisited();
		this.amount = pe.getAmount();
		this.hasSuccessor = pe.getHasSuccessor();
		this.level = pe.getLevel();
		this.numNeighbors = pe.getNumNeighbors();
		this.antecedent = pe.getAntecedent();
	}
	
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Point getPoint() {
		return point;
	}
	public void setPoint(Point point) {
		this.point = point;
	}
	public boolean getVisited() {
		return visited;
	}
	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public Boolean getHasSuccessor() {
		return hasSuccessor;
	}

	public void setHasSuccessor(Boolean hasSuccessor) {
		this.hasSuccessor = hasSuccessor;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getNumNeighbors() {
		return numNeighbors;
	}

	public void setNumNeighbors(int numNeighbors) {
		this.numNeighbors = numNeighbors;
	}

	public PixelElement getAntecedent() {
		return antecedent;
	}

	public void setAntecedent(PixelElement antecedent) {
		this.antecedent = antecedent;
	}
	
	
}
