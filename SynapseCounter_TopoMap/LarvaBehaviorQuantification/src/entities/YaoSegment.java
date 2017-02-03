package entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
* The class used to encapsulate the nodes and edges in a graph.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   10-23-2016
*/
public class YaoSegment
{
	ArrayList<YaoEdge> edges = null;
	Set<Integer> nodes = null;
	
	/**
	 * A constructor.
	 */
	public YaoSegment()
	{
		edges = new ArrayList<YaoEdge>();
		nodes = new HashSet<Integer>();
	}
	
	/**
	 * A constructor
	 * @param nodes The node set.
	 * @param edges The edge set.
	 */
	public YaoSegment(Set<Integer> nodes, ArrayList<YaoEdge> edges)
	{
		this.nodes = nodes;
		this.edges = edges;
	}

	public ArrayList<YaoEdge> getEdges()
	{
		return edges;
	}

	public Set<Integer> getNodes()
	{
		return nodes;
	}

}
