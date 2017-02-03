package entities;

import java.util.ArrayList;
import java.util.Map;

/**
 * The class used to encapsulate the edge between two nodes.
 * 
 * @author Yaoguang Zhong
 * @version 1.1
 * @since 01-21-2017
 */
public class YaoEdge
{
	private int node1 = 0;
	private int node2 = 0;
	
	/**
	 * A constructor.
	 * 
	 * @param node1 A node.
	 * @param node2 Another node.
	 */
	public YaoEdge(int node1, int node2)
	{
		this.node1 = node1;
		this.node2 = node2;
	}
	
	@Override
	public String toString() 
	{
		return "("+node1+","+node2+")";
	}
	
	/**
	 * getter
	 */
	public int getNode1()
	{
		return node1;
	}

	/**
	 * setter
	 */
	public void setNode1(int node1)
	{
		this.node1 = node1;
	}

	/**
	 * getter
	 */
	public int getNode2()
	{
		return node2;
	}

	/**
	 * setter
	 */
	public void setNode2(int node2)
	{
		this.node2 = node2;
	}
	
	/**
	 * Check whether two edges are both between the two nodes.
	 * 
	 * @param edge1 The edge to be compared
	 * @return true is two edges are both between the two nodes. false, otherwise. 
	 */
	public boolean isEqual(YaoEdge edge1)
	{
		if(edge1.getNode1() == getNode1() && edge1.getNode2() == getNode2())
			return true;
		
		if(edge1.getNode1() == getNode2() && edge1.getNode2() == getNode1())
			return true;
		
		return false;
	}
	
	/**
	 * Get the Pixel Element Segment from a map containing YaoEdge and PixelElementSegment where the edge in the map
	 * is the same as the edge in the argument list. (same means the edge contains two same nodes.)
	 * 
	 * @param peEdgeSegmentsMap The map containing YaoEdge and PixelElementSegment.
	 * @param edge The edge.
	 * @return The Pixel Element Segment.
	 */
	public static PixelElementSegment getElementSegment(Map<YaoEdge,PixelElementSegment> peEdgeSegmentsMap, YaoEdge edge)
	{
		PixelElementSegment pixelElementSegment = null;
		
		for (Map.Entry<YaoEdge,PixelElementSegment> entry : peEdgeSegmentsMap.entrySet())
		{
			if(entry.getKey().isEqual(edge))
				pixelElementSegment = entry.getValue();
		}
		
		return pixelElementSegment;
	}
	
	/**
	 * Extract the YaoEdge (the key of the map) and store it to an array list.
	 * 
	 * @param peEdgeSegmentsMap The map containing YaoEdge and PixelElementSegment.
	 * @return The array list of YaoEdge.
	 */
	public static ArrayList<YaoEdge> getEdges(Map<YaoEdge,PixelElementSegment> peEdgeSegmentsMap)
	{
		ArrayList<YaoEdge> edges = new ArrayList<YaoEdge>();
		
		for (Map.Entry<YaoEdge,PixelElementSegment> entry : peEdgeSegmentsMap.entrySet())
		{
			edges.add(entry.getKey());
		}
		
		return edges;
	}

}
