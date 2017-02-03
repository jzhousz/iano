package pca;

import java.util.Locale;

/**
 * The class used to encapsulate a double array.
 * 
 * @author Yaoguang Zhong
 * @version 1.1
 * @since 01-21-2017
 */
public class DwVector
{
	public double[] v;

	/*
	 * Constructor.
	 * @param values The double array.
	 * values
	 */
	public DwVector(double... values)
	{
		this.v = new double[values.length];
		for (int i = 0; i < values.length; i++)
		{
			this.v[i] = values[i];
		}
	}

	/**
	 * Print information about the double array.
	 */
	public void print()
	{
		for (int i = 0; i < v.length; i++)
		{
			System.out.printf(Locale.ENGLISH, "%+8.5f, ", v[i]);
		}
		System.out.printf(Locale.ENGLISH, "\n");
	}

}