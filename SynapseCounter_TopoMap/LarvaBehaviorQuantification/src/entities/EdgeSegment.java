package entities;

import java.util.ArrayList;

public class EdgeSegment
{
	private YaoEdge edge = null;
	private PixelElementSegment segment = null;
	
	public EdgeSegment(YaoEdge edge, PixelElementSegment segment)
	{
		this.edge = edge;
		this.segment = segment;
	}
	
	public YaoEdge getEdge()
	{
		return edge;
	}

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
	

	public static void main(String[] args)
	{

	}

}
