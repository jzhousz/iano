package test;

import java.util.ArrayList;

import entities.YaoEdge;

public class CandidateCaseSimple
{

//	private ArrayList<CandidateLarva> candidateLarvae =  null;
	private ArrayList<YaoEdge> edgesExclude = null;
	
	public CandidateCaseSimple(ArrayList<YaoEdge> edgesExclude)
	{
		this.edgesExclude = edgesExclude;
	}
	
	public static void main(String[] args)
	{

	}

	public ArrayList<YaoEdge> getEdgesExclude()
	{
		return edgesExclude;
	}

	public void setEdgesExclude(ArrayList<YaoEdge> edgesExclude)
	{
		this.edgesExclude = edgesExclude;
	}
	
	

}
