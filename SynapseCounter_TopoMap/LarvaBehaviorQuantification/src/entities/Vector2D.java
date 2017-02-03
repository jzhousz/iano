package entities;

import java.awt.Point;

import ij.gui.Roi;

/**
 * The 2D vector.
 * 
 * @author Yaoguang
 * @version 1.1
 * @since 01-21-2017
 *
 */
public class Vector2D
{
	protected double dX;
	protected double dY;

	/**
	 * A constructor.
	 */
	public Vector2D()
	{
		dX = dY = 0.0;
	}

	/**
	 * A constructor.
	 * 
	 * @param dX The x coordinate.
	 * @param dY The y coordinate.
	 */
	public Vector2D(double dX, double dY)
	{
		this.dX = dX;
		this.dY = dY;
	}
	
	/**
	 * A constructor.
	 * 
	 * @param point The point in a coordinate.
	 */
	public Vector2D(Point point)
	{
		this.dX = (double) point.x;
		this.dY = (double) point.y;
	}
	
	/**
	 * A constructor.
	 * 
	 * @param roi The region of interest coordinate.
	 */
	public Vector2D(Roi roi)
	{
		this.dX = (double) roi.getBounds().x;
		this.dY = (double) roi.getBounds().y;
	}


	public String toString()
	{
		return "Vector2D(" + dX + ", " + dY + ")";
	}

	/**
	 * Compute magnitude of vector.
	 * @return The magnitude of vector
	 */
	public double length()
	{
		return Math.sqrt(dX * dX + dY * dY);
	}

	/**
	 * Add two vector2D.
	 * @param v1 The vector2D that will be added.
	 * @return The sum of two vectors.
	 */
	public Vector2D add(Vector2D v1)
	{
		Vector2D v2 = new Vector2D(this.dX + v1.dX, this.dY + v1.dY);
		return v2;
	}

	/**
	 * The subtraction of two vectors.
	 * @param v1 The vector that will be subtracted.
	 * @return The subtraction of two vectors.
	 */
	public Vector2D sub(Vector2D v1)
	{
		Vector2D v2 = new Vector2D(this.dX - v1.dX, this.dY - v1.dY);
		return v2;
	}

	/**
	 * Scale vector by a constant
	 * @param scaleFactor The scale factor.
	 * @return The scaled vector.
	 */
	public Vector2D scale(double scaleFactor)
	{
		Vector2D v2 = new Vector2D(this.dX * scaleFactor, this.dY * scaleFactor);
		return v2;
	}

	/**
	 * Normalize a vector.
	 * @return The normalized vector.
	 */
	public Vector2D normalize()
	{
		Vector2D v2 = new Vector2D();

		double length = Math.sqrt(this.dX * this.dX + this.dY * this.dY);
		if (length != 0)
		{
			v2.dX = this.dX / length;
			v2.dY = this.dY / length;
		}

		return v2;
	}

	/**
	 * The dot product of two vectors.
	 * @param v1
	 * @return The dot product.
	 */
	public double dotProduct(Vector2D v1)
	{
		return this.dX * v1.dX + this.dY * v1.dY;
	}
	
	public int getIntX()
	{
		return (int) dX;
	}
	
	public void setIntX(int x)
	{
		dX = (double) x;
	}
	
	public int getIntY()
	{
		return (int) dY;
	}
	
	public void setIntY(int y)
	{
		dY = (double) y;
	}

	/**
	 * The main method.
	 * @param args The arguments.
	 */
	public static void main(String args[])
	{
		Vector2D vA = new Vector2D(1.0, 2.0);
		Vector2D vB = new Vector2D(2.0, 2.0);

		System.out.println("Vector vA =" + vA.toString());
		System.out.println("Vector vB =" + vB.toString());

		System.out.println("Vector vA-vB =" + vA.sub(vB).toString());
		System.out.println("Vector vB-vA =" + vB.sub(vA).toString());

		System.out.println("vA.normalize() =" + vA.normalize().toString());
		System.out.println("vB.normalize() =" + vB.normalize().toString());

		System.out.println("Dot product vA.vB =" + vA.dotProduct(vB));
		System.out.println("Dot product vB.vA =" + vB.dotProduct(vA));
	}

}
