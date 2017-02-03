package pca;

import java.util.Locale;

/**
 * The class used to encapsulate eigenvalue and eigenvector of Principal Component Analysis.
 * 
 * @author Yaoguang Zhong
 * @version 1.1
 * @since 01-21-2017
 */
public class DwEigenVector implements Comparable<DwEigenVector>
{
	public double eval;
	public double[] evec;
	public double percent; // the percentage of variation. 

	/**
	 * Constructor.
	 * @param evec The eigenvectors.
	 * @param eval The eigenvalues.
	 * @param percent Percentage of the variation of Principal Component Analysis.
	 */
	public DwEigenVector(double[] evec, double eval, double percent)
	{
		this.evec = evec; // eigenvector
		this.eval = eval; // eigenvalue
		this.percent = percent; // percentage of eigenvalue to sum of eigenvalues
	}

	/**
	 * print information about eigenvalue, eigenvector, and percentage.
	 */
	public void print()
	{
		System.out.printf(Locale.ENGLISH, "eval:%+8.5f,     ", eval);
		System.out.printf(Locale.ENGLISH, "perc:%+8.5f,     ", percent);
		for (int i = 0; i < evec.length; i++)
		{
			System.out.printf(Locale.ENGLISH, "evec["+i+"]:%+8.5f, ", evec[i]);
		}
		System.out.printf(Locale.ENGLISH, "\n");
	}

	public int compareTo(DwEigenVector o)
	{
		if (eval < o.eval)
			return +1;
		if (eval > o.eval)
			return -1;
		return 0;
	}
}