package entities;

import java.io.*;
import java.util.*;

import org.python.google.common.collect.ArrayListMultimap;
import org.python.google.common.collect.Multimap;

/**
 * This class represents a directed graph using adjacency list representation.
 * 
 * @author Yaoguang Zhong
 * @version 1.1
 * @since 01-21-2017
 */
public class YaoGraph
{
	private int[] nodes = null;
	private int[][] edges = null;

	/**
	 * A constructor.
	 * @param numNodes The number of nodes.
	 */
	public YaoGraph(int numNodes)
	{
		nodes = new int[numNodes];
		edges = new int[numNodes][numNodes];

		for (int i = 0; i < edges.length; i++)
		{
			for (int j = 0; j < edges[0].length; j++)
			{
				edges[i][j] = 0;
			}
		}
	}

	/**
	 * Add a node.
	 * @param indexNode The index of the node.
	 * @param valueNode The value of the node.
	 */
	public void addNode(int indexNode, int valueNode)
	{
		nodes[indexNode] = valueNode;
	}

	/**
	 * Add a edge.
	 * @param valueNode1 The value of the node.
	 * @param valueNode2 The value of the node.
	 */
	public void addEdge(int valueNode1, int valueNode2)
	{
		int indexNode1 = valueToIndex(valueNode1);
		int indexNode2 = valueToIndex(valueNode2);

		edges[indexNode1][indexNode2] = 1;
		edges[indexNode2][indexNode1] = 1;
	}

	/**
	 * Convert to an index of a node from the value of a node.
	 * @param valueNode The value of a node.
	 * @return The index of the node.
	 */
	public int valueToIndex(int valueNode)
	{
		int indexNode = 0;

		for (int i = 0; i < nodes.length; i++)
		{
			if (valueNode == nodes[i])
				indexNode = i;
		}

		return indexNode;
	}

	/**
	 * Convert to the value of a node from the index of a node.
	 * @param indexNode The index of the node.
	 * @return The value of a node.
	 */
	public int indexToValue(int indexNode)
	{
		return nodes[indexNode];
	}
	
	/**
	* Breadth first transverse the graph and return the node index set.
	* 
	* @param valueNode The value of the node begin with.
	* @return The set containing the indexes of the nodes transversed.
	*/
	public Set<Integer> breadthFirstTransverse(int valueNode)
	{
		int indexNode = valueToIndex(valueNode);

		boolean visited[] = new boolean[nodes.length];

		Set<Integer> nodeIndexSet = new HashSet<Integer>();
		
		// Create a queue for BFS
		LinkedList<Integer> queue = new LinkedList<Integer>();

		// Mark the current node as visited and enqueue it
		visited[indexNode] = true;
		queue.add(indexNode);

		while (queue.size() != 0)
		{
			// Dequeue a vertex from queue and print it
			indexNode = queue.poll();

			nodeIndexSet.add(indexNode);

			for (int j = 0; j < nodes.length; j++)
			{
				if (edges[indexNode][j] == 1)
				{
					if (!visited[j])
					{
						visited[j] = true;
						queue.add(j);
					}
				}
			}
		}
		
		return nodeIndexSet;
	}

	/**
	* Is there an edge between 2 nodes.
	* 
	* @param valueNode1 The value of node1.
	* @param valueNode1 The value of node2.
	* @return true if there is an edge, false if there is not.
	*/
	public boolean isEdge(int valueNode1, int valueNode2)
	{
		int indexNode1 = valueToIndex(valueNode1);
		int indexNode2 = valueToIndex(valueNode2);

		if (edges[indexNode1][indexNode2] == 1)
			return true;
		else
			return false;
	}

	@Override
	public String toString() 
	{
		String str = "\nNodes: ";
		for(int i = 0; i < nodes.length; i++)
			str += nodes[i] + ", ";
		
		Multimap<Integer, Integer> edges = getEdges();
		
		str += "\nedges: {";
		
		for (int k: edges.keySet())
		{
			for(int val : edges.get(k))
				str += "(" + nodes[k] + ", " + nodes[val] + "),";
		}
		
		str += "}";
			
		return str;
	}
	
//	/**
//	 * Didn't use this method for this project.
//	 * @return
//	 */
//	public String toStringRefected7() 
//	{
//		String str = "Nodes: ";
//		for(int i = 0; i < nodes.length; i++)
//			str += nodeRefected(nodes[i]) + ", ";
//		
//		Multimap<Integer, Integer> edges = getEdges();
//		
//		str += "\nedges: {";
//		
//		for (int k: edges.keySet())
//		{
//			for(int val : edges.get(k))
//				str += "(" + nodeRefected(nodes[k]) + ", " + nodeRefected(nodes[val]) + "),";
//		}
//		
//		str += "}";
//			
//		return str;
//	}
	
	
	public int nodeRefected(int valueNode)
	{
		int numRefected = 255;
		
		return numRefected - valueNode;
	}

	public int[] getNodes()
	{
		return nodes;
	}

	/**
	 * Get the edges of the graph.
	 * @return The edges
	 */
	public Multimap<Integer, Integer> getEdges()
	{
		Multimap<Integer, Integer> edgesSet =  ArrayListMultimap.create();

		for(int i = 0; i < nodes.length; i++)
		{
			for(int j = i + 1; j < nodes.length; j++)
			{
				if(edges[i][j] == 1)
				{
					edgesSet.put(i, j);
				}
			}
		}
		
		return edgesSet;
	}
	
	/**
	 * Get the edges of the graph.
	 * @return The edges
	 */
	public ArrayList<YaoEdge> getYaoEdges()
	{
		ArrayList<YaoEdge> yaoEdges = new ArrayList<YaoEdge>();
		
		for(int i = 0; i < nodes.length; i++)
		{
			for(int j = i + 1; j < nodes.length; j++)
			{
				if(edges[i][j] == 1)
				{
					YaoEdge yaoEdge = new YaoEdge(nodes[i], nodes[j]);
					yaoEdges.add(yaoEdge);
				}
			}
		}
		
		return yaoEdges;
	}
	
//	public ArrayList<YaoEdge> getYaoEdgesReflected()
//	{
//		ArrayList<YaoEdge> yaoEdges = new ArrayList<YaoEdge>();
//		
//		for(int i = 0; i < nodes.length; i++)
//		{
//			for(int j = i + 1; j < nodes.length; j++)
//			{
//				if(edges[i][j] == 1)
//				{
//					YaoEdge yaoEdge = new YaoEdge(nodeRefected( nodes[i] ), nodeRefected(nodes[j]));
//					yaoEdges.add(yaoEdge);
//				}
//			}
//		}
//		
//		return yaoEdges;
//	}
	
	/**
	 * Get all the edges of the graph that connect the node.
	 * @param valueNode The value of a node.
	 * @return The indexes of the edges.
	 */
	public Multimap<Integer, Integer> getEdgesIndex(int valueNode)
	{
		int indexNode = valueToIndex(valueNode);
		
		Multimap<Integer, Integer> edgesSet =  ArrayListMultimap.create();

		for(int i = 0; i < nodes.length; i++)
		{
			if(edges[indexNode][i] == 1)
			{
				edgesSet.put(indexNode, i);
			}
		}
		
		return edgesSet;
	}
	
	/**
	 * Get all the edges of the graph that connect the node.
	 * @param valueNode The value of a node.
	 * @return The values of the edges.
	 */
	public Multimap<Integer, Integer> getEdgesValue(int valueNode)
	{
		int indexNode = valueToIndex(valueNode);
		
		Multimap<Integer, Integer> edgesSet =  ArrayListMultimap.create();

		for(int i = 0; i < nodes.length; i++)
		{
			if(edges[indexNode][i] == 1)
			{
				edgesSet.put(nodes[indexNode], nodes[i]);
			}
		}
		
		return edgesSet;
	}

	/**
	 * The main method.
	 * @param args The arguments.
	 */
	public static void main(String args[])
	{
		YaoGraph g = new YaoGraph(5);

		g.addNode(0, 5);
		g.addNode(1, 3);
		g.addNode(2, 6);
		g.addNode(3, 1);
		g.addNode(4, 7);

		g.addEdge(5, 3);
		g.addEdge(6, 1);
		g.addEdge(1, 3);

		System.out.println("graph:" + g);
		
		Multimap<Integer, Integer> edges = g.getEdgesIndex(7);
		
		System.out.println("edges:" + edges);
		
		System.out.println("graph structure: " + g);

		g.breadthFirstTransverse(5);
	}
	
	/**
	 * Find all the graphs containing the node with the value.
	 * 
	 * @param graphsCheck The graph array checked.
	 * @param valueNode The value of the node checked.
	 * @return The arrayList of the graphs containing the node with the value.
	 */
	public static ArrayList<YaoGraph> getAllGraphs(ArrayList<YaoGraph> graphsCheck, int valueNode)
	{
		ArrayList<YaoGraph> graphs = new ArrayList<YaoGraph>();
		
		for(YaoGraph g : graphsCheck)
		{
			Set<Integer> set = new HashSet<Integer>();
			for(int node : g.getNodes())
				set.add(node);
			
			if( set.contains(valueNode))
			{
				graphs.add(g);
			}
		}
		
		return graphs;
	}

}