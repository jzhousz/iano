package entities;

import manager.MathManager;

/**
* The linear line.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class LinearLine {

	private double beta0; // it's also the interception of a line
	private double beta1; // it's also the slope of a line

	public double getX(double y)
	{
		double x = ( y - beta0 ) / beta1;
		return MathManager.get2DecimalPoints(x);
	}
	
	public double getY(double x)
	{
		return beta1 * x + beta0;
	}
	
	public void setBeta0(double beta0)
	{
		this.beta0 = beta0;
	}
	
	public void setBeta1(double beta1)
	{
		this.beta1 = beta1;
	}
	
	public double getBeta0()
	{
		return beta0;
	}
	
	public double getBeta1()
	{
		return beta1;
	}
	
	public String toString()
	{
		return "y = " + beta1 + " * x" + " + " + beta0;
	}
}
