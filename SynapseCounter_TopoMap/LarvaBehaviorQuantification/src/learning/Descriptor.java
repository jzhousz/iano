package learning;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

import javax.naming.spi.DirectoryManager;
import org.ejml.simple.SimpleMatrix;
import entities.LinearLine;
import entities.PixelPutter;
import file.CSVWriter;
import file.ImageSaver;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageConverter;
import manager.DrawingManager;
import manager.FileManager;
import manager.ImageManager;
import manager.MathManager;
import manager.StringManager;
import manager.VectorManager;

/**
 * The class is used to extract the features of a larva in images.
 * 
 * @author Yaoguang Zhong
 * @version 1.1
 * @since 01-21-2017
 */
public class Descriptor
{
	static final int NUM_POINTS = 9; // number of control points chosen
	public static final String intermediateDirName = "aIntermediate"; // the intermediate output folder
	private static String intermediateDirPath = "";
	
	/**
	* Save all the dimension data about the descriptor to a file.
	* 
	* @param larvaId The larva id used to identify the larva through the process.
	* @param fileInput The input image file.
	* @param fileOut The output csv file.
	* @param numPoints The number of control points.
	* @param saveOriginalImage Whether you want to save the original image 
	* 		 with a new name to the output directory.
	*/
	public static void saveDimensionData(int larvaId, String fileInput, String fileOut
			, int numPoints, boolean saveOriginalImage)
	{
		ImagePlus imagePlus = ImageManager.getImagePlusFromFile(fileInput);
		IJ.run(imagePlus, "Make Binary", "");

		// get the dimension data
		ArrayList<Double> larvaData = getDimensionData(imagePlus, fileOut, larvaId, numPoints, saveOriginalImage);

		// write all data about larvae's descriptors to the file.
		if (larvaData != null)
		{
			String line;

			CSVWriter csvWriter = new CSVWriter(fileOut);

			line = "";

			for (int i = 0; i < larvaData.size(); i++)
			{
				if (i == 0)
					line += larvaData.get(i);
				else
					line += "," + larvaData.get(i);
			}

			csvWriter.writeln(line);
		}
	}

	/**
	* Get the descriptor of a larva.
	* 
	* @param imagePlusBinary The binary objec image.
	* @param larvaId The larva id used to identify the larva through the process.
	* @param fileInput The input image file.
	* @param fileOut The output csv file.
	* @param numPoints The number of control points.
	* @param saveOriginalImage Whether you want to save the original image 
	* 		 with a new name to the output directory.
	* @return The double array list contain all the dimension data about the larva.
	*/
	public static ArrayList<Double> getDimensionData(ImagePlus imagePlusBinary, String fileOut
			, int larvaId, int numPoints, boolean saveOriginalImage)
	{
		intermediateDirPath = StringManager.getPath(fileOut) + intermediateDirName + "/";
		
		ArrayList<Double> dimData = new ArrayList<Double>(); // the generated dimension data
		
		// imagePlusOriginal will be kept for copying for other image plus.
		// Thus, it will not be modified.
		ImagePlus imagePlusOriginal = imagePlusBinary.duplicate();
		
		// convert to RGB image
		ImageConverter imageConverterOrg = new ImageConverter(imagePlusOriginal);
		imageConverterOrg.convertToRGB();
				
		ImagePlus impSkeleton = ImageManager.getStemSkeleton(imagePlusBinary, 128);
		
		// this image plus will contain all the skeleton points and will not be changed.
		ImagePlus impSke_keep = impSkeleton.duplicate();
		
		double bodyArea = (double) ImageManager.getPixelArea(imagePlusBinary, 128);
		Point centerPt = ImageManager.findCenterPoint(impSkeleton, 128);

		ArrayList<Point> endPts = ImageManager.findEndPoints(impSkeleton, 128);

		double lenSkeleton = ImageManager.getSkeletonLength(impSkeleton, 128);
		double pixelsPtDouble = lenSkeleton / numPoints; // get the number of pixels for each point
		int pixelsPt = (int) Math.floor(pixelsPtDouble);

		ArrayList<Point> ptsSkeleton = null;

		try
		{
			ptsSkeleton = ImageManager.getAllPointsInSkeleton(impSkeleton, endPts.get(0), 128);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		Object[] ptsSkeletonArr = ptsSkeleton.toArray();
		
		if(ptsSkeletonArr.length < numPoints)
		{
			System.out.println("Small problem: ptsSkeletonArr is less than numPoints. Can't find numPoints number of control points. Didn't store data for this larva.");
			return null;
		}
		
		// the simple matrix contains all skeleton points
		ArrayList<SimpleMatrix> ptsChosenMatr = new ArrayList<SimpleMatrix>();
		
		ArrayList<SimpleMatrix> ptsSkeletonMatr = VectorManager.convertPtToMatrixArray(ptsSkeleton);
		
		ArrayList<Point> ptsSkeletonChosen2 = new ArrayList<Point>();
		
		double indexDoublePtsChosen[] = get9PointIndex(NUM_POINTS, ptsSkeletonArr.length);
		int indexInt = 0;
		
		// get the NUM_POINTS points that equally distributes along the skeleton 
		for(double indexDouble : indexDoublePtsChosen)
		{
			indexInt = MathManager.getRoundedInt( indexDouble ) - 1;
			// first convert the point to a matrix and then add to ptsChosenMatr
			ptsChosenMatr.add(VectorManager.convertPtToMatrix((Point) ptsSkeletonArr[indexInt]));
			ptsSkeletonChosen2.add( (Point) ptsSkeletonArr[indexInt]  );
		}
		
		// if the number of control points is not equal to the number we picked
		if(ptsChosenMatr.size() != numPoints)
		{
			System.out.println("Small problem: dimData is not equal to NUM_POINTS. Didn't store data for this larva." 
					+ "ptsChosenMatr.size(): " + ptsChosenMatr.size() + ", NUM_POINTS: " + numPoints);
			return null;
		}
		
		FileManager.createDirectory(intermediateDirPath);

		ImagePlus imagePlusBin_ske = imagePlusOriginal.duplicate();
		
		// fill the imagePlusBin_ske with positions of pixels from impSke_keep with the color
		ImageManager.fillImagePlus(impSke_keep, imagePlusBin_ske, new Point(0,0), 128, Color.white);
		
		impSkeleton = imagePlusBin_ske.duplicate();
		
		for(Point pt : ptsSkeletonChosen2)
		{
			PixelPutter.putPixels(impSkeleton, pt, 2, 2, Color.white);
		}
		
		// set the x and y axes of the origin in the new coordinate
		Point pointOrigin = new Point(60,60); 
		
		ArrayList<Point> ptsSkeletonChosen = new ArrayList<Point>();
		
		for(SimpleMatrix mtr : ptsChosenMatr)
		{
			Point pt = new Point(MathManager.getRoundedInt(mtr.get(0, 0)), MathManager.getRoundedInt(mtr.get(0, 1)));
			ptsSkeletonChosen.add(pt);
		}
		
		// translate a matrix, shit units in the old coordinate
		double[] translateArr= new double[]{0 - centerPt.getX(),0 - centerPt.getY()};
		
		// the translating matrix
		// the transformation vector in matrix form
		// matrix transformation formula from: https://en.wikipedia.org/wiki/Transformation_matrix
		SimpleMatrix translateMatr = VectorManager.newSimpleMatrix(translateArr); 
		
		double angle = MathManager.getAngleBetween2Lines(endPts.get(0), endPts.get(1), new Point(0,0), new Point(0,10));
		
		// get the angle transforming, the transforming matrix
		SimpleMatrix transformMatr = VectorManager.newTransformationMatrix(angle);
				
		// translate the chosen skeleton points matrix
		for(int i = 0; i < ptsChosenMatr.size(); i++)
		{
			// translate the matrix with a translating matrix, i.e., shift a matrix
			ptsChosenMatr.set(i, ptsChosenMatr.get(i).plus( translateMatr ));
		}
		
		// translate all skeleton points matrix
		for(int i = 0; i < ptsSkeletonMatr.size(); i++)
		{
			// translate the matrix with a translating matrix, i.e., shift a matrix
			ptsSkeletonMatr.set(i, ptsSkeletonMatr.get(i).plus( translateMatr ));
		}
		
		// print all the skeleton points after translation on the output images
		ImageManager.printMatrixPixels(impSkeleton, ptsSkeletonMatr, pointOrigin, 1, Color.cyan);
				
		// print all the control points after translation on the output images
		ImageManager.printMatrixPixels(impSkeleton, ptsChosenMatr, pointOrigin, 2, Color.cyan);

		// save the translated skeleton of the larva
		ImageSaver.saveImagesWithPath(intermediateDirPath + "ske_translated_"+Integer.toString(larvaId)+".jpg", impSkeleton);
		
		// transform all the matrices
		for(int i = 0; i < ptsChosenMatr.size(); i++)
		{
			// transform the matrix with a angle
			ptsChosenMatr.set(i, transformMatr.mult(ptsChosenMatr.get(i).transpose()).transpose());
		}
		
		// transform all the skeleton point matrices
		for(int i = 0; i < ptsSkeletonMatr.size(); i++)
		{
			// transform the matrix with a angle
			ptsSkeletonMatr.set(i, transformMatr.mult(ptsSkeletonMatr.get(i).transpose()).transpose());
		}
		
		// print all the skeleton points after transformation on the output images
		ImageManager.printMatrixPixels(impSkeleton, ptsSkeletonMatr, pointOrigin, 1, Color.orange);
				
		// print all the control points after transformation on the output images
		ImageManager.printMatrixPixels(impSkeleton, ptsChosenMatr, pointOrigin, 2, Color.orange);
		// save the translated skeleton of the larva
		ImageSaver.saveImagesWithPath(intermediateDirPath + "ske_transformed_"+Integer.toString(larvaId)+".jpg", impSkeleton);
		
		// check whether need to convert the matrices to its reflected matrices.
		if(ptsChosenMatr.get(1).get(0, 0) < 0 || ptsChosenMatr.get(2).get(0, 0) < 0)
		{
			// convert the matrices to the reflected matrices if need
			for(int i = 0; i < ptsChosenMatr.size(); i++)
			{
				// convert the matrices to the reflected matrices if need
				ptsChosenMatr.set(i
						, VectorManager.newSimpleMatrix( new double[] {ptsChosenMatr.get(i).get(0, 0) * -1 , ptsChosenMatr.get(i).get(0, 1) }));
			}
			
			// convert the matrices to the reflected matrices if need
			for(int i = 0; i < ptsSkeletonMatr.size(); i++)
			{
				// convert the matrices to the reflected matrices if need
				ptsSkeletonMatr.set(i
						, VectorManager.newSimpleMatrix( new double[] {ptsSkeletonMatr.get(i).get(0, 0) * -1 , ptsSkeletonMatr.get(i).get(0, 1) }));
			}
		}
		
		for(int i = 0; i < ptsChosenMatr.size(); i++)
		{
			dimData.add( ptsChosenMatr.get(i).get(0, 0) );
			dimData.add( ptsChosenMatr.get(i).get(0, 1) );
		}
		
		ImagePlus imagePlusEdge = ImageManager.getEdge(imagePlusBinary);

		Point ptEdgeNearest = null;

		try
		{
			ptEdgeNearest = ImageManager.getNearestEdgePoint(imagePlusEdge, centerPt, 128);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		double radius = MathManager.getDistance(centerPt, ptEdgeNearest);

		// print all skeleton points after reflection on the output images
		ImageManager.printMatrixPixels(impSkeleton, ptsSkeletonMatr, pointOrigin, 1, Color.red); //.cyan
				
		// print all the vectors after reflection on the output images
		ImageManager.printMatrixPixels(impSkeleton, ptsChosenMatr, pointOrigin, 2, Color.red); //.cyan
				
		ImageSaver.saveImagesWithPath(StringManager.getPath(fileOut) + "b"+Integer.toString(larvaId)+".jpg", impSkeleton);
		
		LinearLine linearLine = MathManager.getLinearLine(ptEdgeNearest, centerPt);
		
		for(Point pt : ptsSkeletonChosen)
		{
			Point ptEdge = null;

			try
			{
				ptEdge = ImageManager.getNearestEdgePoint(imagePlusEdge, pt, 128);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		// rectangle thickness approach
//		double waistDouble = bodyArea / (double) lenSkeleton;
		
//		int thickness = MathManager.getRoundedInt(thicknessDouble);
//		
//		double waistDouble = (bodyArea / (double) lenSkeleton) * Math.PI;
//		int waist = MathManager.getRoundedInt(waistDouble);
		
		// oval thickness approach
		double waistDouble = (4 * bodyArea) / ( (double) lenSkeleton * Math.PI );
		
		boolean flag = FileManager.createDirectory(intermediateDirPath);
		
		ImageSaver.saveImagesWithPath(intermediateDirPath + "binSke"+Integer.toString(larvaId)+".jpg", imagePlusBin_ske);
		
		if(saveOriginalImage)
			ImageSaver.saveImagesWithPath(StringManager.getPath(fileOut) + "o"+Integer.toString(larvaId)+".jpg", imagePlusOriginal);

		dimData.add(waistDouble);
		
		return dimData;
	}
	
	/**
	 * Get the indexes of 9 points equally in the array[0..totalPoints-1].
	 * 
	 * @param numPoints The number of points. In this case numPoints is 9.
	 * @param totalPoints The total number of points.
	 * @return The indexes of the 9 points.
	 */
	public static double[] get9PointIndex(int numPoints, int totalPoints)
	{
		double indexDoublePtsChosen[] = new double[numPoints];
		
		indexDoublePtsChosen[0] = 1;
		indexDoublePtsChosen[8] = totalPoints;
		indexDoublePtsChosen[4] = ( indexDoublePtsChosen[0] + indexDoublePtsChosen[8]) / 2;
		
		indexDoublePtsChosen[2] = ( indexDoublePtsChosen[0] + indexDoublePtsChosen[4]) / 2;
		indexDoublePtsChosen[6] = (indexDoublePtsChosen[4] + indexDoublePtsChosen[8]) / 2;
		indexDoublePtsChosen[1] = (indexDoublePtsChosen[0] + indexDoublePtsChosen[2]) / 2;
		indexDoublePtsChosen[3] = (indexDoublePtsChosen[2] + indexDoublePtsChosen[4]) / 2;
		indexDoublePtsChosen[5] = (indexDoublePtsChosen[4] + indexDoublePtsChosen[6]) / 2;
		indexDoublePtsChosen[7] = (indexDoublePtsChosen[6] + indexDoublePtsChosen[8]) / 2;
		
		return indexDoublePtsChosen;
	}
	
	/**
	 * Get the number of skeleton points chosen.
	 * 
	 * @return The number of skeleton points chosen
	 */
	public static int getNumPoints()
	{
		return NUM_POINTS;
	}

}
