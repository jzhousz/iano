package entities;

import java.io.Serializable;

public class TrainingData implements Serializable
{
	double[][] eigenVectors = null;
	double[] eigenValues = null;
	double[] meanLarva = null;
//	int dim_used = 0;
	
	public TrainingData()
	{
	}
	
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub

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

//	public int getDim_used()
//	{
//		return dim_used;
//	}
//
//	public void setDim_used(int dim_used)
//	{
//		this.dim_used = dim_used;
//	}

}
