package segmentation;

import java.util.ArrayList;

/**
 * The class used to encapsulate a candidate case.
 * 
 * @author Yaoguang Zhong
 * @version 1.1
 * @since 01-21-2017
 */
public class CandidateCase
{
	public ArrayList<CandidateLarva> candidateLarvae = null;
	private int candidateCaseId = 0;
	
	// the exponent part of the density function, give a some large number
	public double probability1 = 999999999;
	// the natural number, e, part of the density function
	public double probability2 = 0;
		
	/**
	 * Constrctor.
	 * @param candidateCaseId The candidate case id.
	 */
	public CandidateCase(int candidateCaseId)
	{
		this.candidateCaseId = candidateCaseId;
		candidateLarvae = new ArrayList<CandidateLarva>();
	}
	
	/**
	 * Add a candidate larva to a candidate case.
	 * @param candidateLarva The candidate larva.
	 */
	public void addCandidateLarva(CandidateLarva candidateLarva)
	{
		candidateLarvae.add( candidateLarva );
	}
	
	/**
	 * Getter.
	 * @return
	 */
	public int getCandidateCaseId()
	{
		return candidateCaseId;
	}

	/**
	 * Setter.
	 * @param candidateCaseId The candidate case id.
	 */
	public void setCandidateCaseId(int candidateCaseId)
	{
		this.candidateCaseId = candidateCaseId;
	}

}
