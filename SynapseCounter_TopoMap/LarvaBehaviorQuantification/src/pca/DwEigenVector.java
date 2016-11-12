package pca;

import java.util.Locale;

public class DwEigenVector implements Comparable<DwEigenVector>
{
	public double eval;
	public double[] evec;
	public double percent; // the percentage of variation. 

	public DwEigenVector(double[] evec, double eval, double percent)
	{
		this.evec = evec; // eigenvector
		this.eval = eval; // eigenvalue
		this.percent = percent; // percentage of eigenvalue to sum of eigenvalues
	}

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