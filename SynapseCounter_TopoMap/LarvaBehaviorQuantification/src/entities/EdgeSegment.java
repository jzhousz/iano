package entities;

import java.util.ArrayList;

/**
 * A class used link an edge of a map with the pixel element segment.
 * 
 * @author Yaoguang Zhong
 * @version 1.1
 * @since 01-21-2017
 *
 */
public class EdgeSegment
{
	private YaoEdge edge = null;
	private PixelElementSegment segment = null;
	
	/**
	 * A constructor.
	 * 
	 * @param edge The edge.
	 * @param segment The pixel element segment.
	 */
	public EdgeSegment(YaoEdge edge, PixelElementSegment segment)
	{
		this.edge = edge;
		this.segment = segment;
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
	public PixelElementSegment getSegment()
	{
		return segment;
	}
	
	/**
	 * Get the PixelElement Segment from the EdgeSegment ArrayList.
	 * 
	 * @param edges The EdgeSegment ArrayList contains all the EdgeSegment.
	 * @param edgeFind The edge finding.
	 * @return The PixelElement Segment
	 */
	public static PixelElementSegment getSegment(ArrayList<EdgeSegment> edges, YaoEdge edgeFind)
	{
		for(EdgeSegment edge : edges)
		{
			if(edge.getEdge().isEqual(edgeFind))
				return edge.getSegment();
		}
		
		return null;
	}

}
