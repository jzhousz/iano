package manager;

import java.awt.Point;
import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;

public class VectorManager
{
	/**
	* New Point from SimpleMatrix.
	* 
	* @param matrix The SimpleMatrix.
	* @return The point.
	*/
	public static Point newPoint(SimpleMatrix matrix)
	{
		int x = MathManager.getRoundedInt( matrix.get(0, 0) );
		int y = MathManager.getRoundedInt( matrix.get(0, 1) );
		
		return new Point(x,y);
	}
	
	/**
	* New transformation SimpleMatrix.
	* from: https://en.wikipedia.org/wiki/Transformation_matrix
	* 
	* @param angle The angle to be transformed.
	* @return The SimpleMatrix.
	*/
	public static SimpleMatrix newTransformationMatrix(double angle)
	{
		double radians = Math.toRadians(angle);

		double sin = Math.sin(radians);
		double cos = Math.cos(radians);
		
		// transformtion matrix for clockwise
		double[][] matr = {{cos, sin}, {-sin, cos}}; 
		
		return new SimpleMatrix(matr);
	}
	
	/**
	* New SimpleMatrix with a 1D double array.
	* 
	* @param vals The 1D double array.
	* @return The SimpleMatrix.
	*/
	public static SimpleMatrix newSimpleMatrix(ArrayList<Double> vals)
	{
		double[][] matr = new double[1][vals.size()];

		for(int i = 0; i < vals.size(); i ++)
		{
			matr[0][i] = vals.get(i);
		}
		
		return new SimpleMatrix(matr);
	}
	
	/**
	* New SimpleMatrix with a 1D double array.
	* 
	* @param vals The 1D double array.
	* @return The SimpleMatrix.
	*/
	public static SimpleMatrix newSimpleMatrix(double[] vals)
	{
		double[][] matr = new double[1][vals.length];
		matr[0] = vals;
		
		return new SimpleMatrix(matr);
	}
	
	/**
	* Covert Points array to SimpleMatrix array.
	* 
	* @param points The points needs to be converted to simple matrix.
	* @return The SimpleMatrix.
	*/
	public static ArrayList<SimpleMatrix> convertPtToMatrixArray(ArrayList<Point> points)
	{
		ArrayList<SimpleMatrix> ptsMatr = new ArrayList<SimpleMatrix>();
		for(Point point : points)
		{
			ptsMatr.add( convertPtToMatrix(point) );
		}
		
		return ptsMatr;
	}
	
	/**
	* Covert Point to SimpleMatrix.
	* 
	* @param point The point.
	* @return The SimpleMatrix.
	*/
	public static SimpleMatrix convertPtToMatrix(Point point)
	{
		return newSimpleMatrix(new double[]{point.getX(), point.getY()});
	}
	
	/**
	* Covert 1D double array to 2D double array.
	* 
	* @param data The 1D double array.
	* @return The 2D double array.
	*/
	public static double[][] convertTo2DHorizontal(double[] data)
	{
		double[][] vec = new double[1][data.length];
		
		for(int i = 0; i < data.length; i++)
		{
			vec[0][i] = data[i];
		}
		
		return vec;
	}

	
}
