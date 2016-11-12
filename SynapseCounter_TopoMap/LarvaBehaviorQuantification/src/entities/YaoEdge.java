package entities;

import org.python.google.common.collect.Multimap;

public class YaoEdge
{
	private int node1 = 0;
	private int node2 = 0;
	
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
	
	public int getNode1()
	{
		return node1;
	}

	public void setNode1(int node1)
	{
		this.node1 = node1;
	}

	public int getNode2()
	{
		return node2;
	}

	public void setNode2(int node2)
	{
		this.node2 = node2;
	}
	
	public boolean isEqual(YaoEdge edge1)
	{
		if(edge1.getNode1() == getNode1() && edge1.getNode2() == getNode2())
			return true;
		
		if(edge1.getNode1() == getNode2() && edge1.getNode2() == getNode1())
			return true;
		
		return false;
	}

	public static void main(String[] args)
	{

	}

}
