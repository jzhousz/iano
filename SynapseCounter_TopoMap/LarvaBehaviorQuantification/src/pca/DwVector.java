package pca;

import java.util.Locale;

public class DwVector
{
	public double[] v;

	public DwVector(double... values)
	{
		this.v = new double[values.length];
		for (int i = 0; i < values.length; i++)
		{
			this.v[i] = values[i];
		}
	}

	public void print()
	{
		for (int i = 0; i < v.length; i++)
		{
			System.out.printf(Locale.ENGLISH, "%+8.5f, ", v[i]);
		}
		System.out.printf(Locale.ENGLISH, "\n");
	}

}