package entities;

import java.awt.Point;

/**
 * Used to encapsulate the edge and the point in a class.
 * 
 * @author Yaoguang
 *
 */
public class EdgePoint
{
	private YaoEdge edge = null;
	private Point point = null;
	
	/**
	 * A constructor.
	 * 
	 * @param edge An edge.
	 * @param point A point.
	 */
	public EdgePoint(YaoEdge edge, Point point)
	{
		this.edge = edge;
		this.point = point;
	}
	
	@Override
	public String toString() 
	{
		return edge.toString() + point.toString();
	}
	
	/**
	 * getter
	 */
	public YaoEdge getEdge()
	{
		return edge;
	}

	/**
	 * getter
	 */
	public Point getPoint()
	{
		return point;
	}

}
