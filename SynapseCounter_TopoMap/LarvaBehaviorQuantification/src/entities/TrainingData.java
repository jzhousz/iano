package entities;

import java.io.Serializable;

/**
 * The class used to encapsulate the generated results for the training data.
 * 
 * @author Yaoguang Zhong
 * @version 1.1
 * @since 01-21-2017
 */
public class TrainingData implements Serializable
{
	double[][] eigenVectors = null;
	double[] eigenValues = null;
	double[] meanLarva = null;
	
	/**
	 * A constructor.
	 */
	public TrainingData()
	{
	}

	public double[][] getEigenVectors()
	{
		return eigenVectors;
	}

	public void setEigenVectors(double[][] eigenVectors)
	{
		this.eigenVectors = eigenVectors;
	}

	public double[] getEigenValues()
	{
		return eigenValues;
	}

	public void setEigenValues(double[] eigenValues)
	{
		this.eigenValues = eigenValues;
	}

	public double[] getMeanLarva()
	{
		return meanLarva;
	}

	public void setMeanLarva(double[] meanLarva)
	{
		this.meanLarva = meanLarva;
	}

}
