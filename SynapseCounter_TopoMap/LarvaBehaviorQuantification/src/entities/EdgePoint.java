package entities;

import java.awt.Point;

public class EdgePoint
{
	private YaoEdge edge = null;
	private Point point = null;
	
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
	
	public YaoEdge getEdge()
	{
		return edge;
	}

	public Point getPoint()
	{
		return point;
	}

	public static void main(String[] args)
	{

	}

}
