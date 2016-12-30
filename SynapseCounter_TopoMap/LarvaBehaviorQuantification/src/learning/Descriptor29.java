package learning;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import org.ejml.simple.SimpleMatrix;
import entities.LinearLine;
import entities.PixelElementSegment;
import entities.PixelElementTable;
import entities.PixelPutter;
import file.CSVWriter;
import file.ImageSaver;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageConverter;
import manager.FileManager;
import manager.ImageManager;
import manager.MathManager;
import manager.StringManager;
import manager.VectorManager;

public class Descriptor29
{
	static final int NUM_POINTS = 11; // number of control points chosen
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
	* @param imagePlusBinary The binary object image.
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
		
//		ArrayList<Double> dimData = new ArrayList<Double>(); // the generated dimension data
		ArrayList<Double> dimDataShort = new ArrayList<Double>(); // the generated dimension data
		
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
		
//		ArrayList<Point> ptsChosen = new ArrayList<Point>();
		 
		int ptLeftIndex = 0; 
		int ptRightIndex = 0;
		
		// add the center of skeleton point
		ptsChosenMatr.add(VectorManager.convertPtToMatrix(centerPt));
//		ptsChosen.add(centerPt);
		
		// get the NUM_POINTS points that equally distributes along the skeleton 
		for(int i = 1; i < numPoints / 2 + 1; i ++)
		{
			// add the points in the left and the right of the median point
			ptLeftIndex = 0 + i * pixelsPt; // index increasing from 0 to the median index
			// index increasing from the last index to the median index
			ptRightIndex = ptsSkeletonArr.length - 1 - i * pixelsPt; 
			
			// get the left and the right of the median point
			Point ptLeft = (Point) ptsSkeletonArr[ptLeftIndex];
			Point ptRight = (Point) ptsSkeletonArr[ptRightIndex];
			
			// first convert the point to a matrix and then add to ptsChosenMatr
			ptsChosenMatr.add(VectorManager.convertPtToMatrix(ptLeft));
			ptsChosenMatr.add(VectorManager.convertPtToMatrix(ptRight));
			
//			ptsChosen.add(ptLeft);
//			ptsChosen.add(ptRight);
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
		ImageManager.fillImagePlus(impSke_keep, imagePlusBin_ske, new Point(0,0), 128, Color.cyan);
		
		impSkeleton = imagePlusBin_ske.duplicate();
		
		// set the x and y axes of the origin in the new coordinate
		Point pointOrigin = new Point(60,60); 
				
		// print the vectors closet to the center of skeleton after translation and transformation on the output images
		PixelPutter.putPixels(impSkeleton, pointOrigin , 4, 4, Color.cyan);
		
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
		ImageManager.printMatrixPixels(impSkeleton, ptsSkeletonMatr, pointOrigin, 1, Color.gray);
				
		// print all the control points after translation on the output images
		ImageManager.printMatrixPixels(impSkeleton, ptsChosenMatr, pointOrigin, 3, Color.orange);

		// save the translated skeleton of the larva
		ImageSaver.saveImagesWithPath(intermediateDirPath + "ske_translated_"+Integer.toString(larvaId)+".jpg", impSkeleton);
				
		// print all the control points after translation on the output images
		ImageManager.printMatrixPixels(impSkeleton, ptsChosenMatr, pointOrigin, 2, Color.green);
		
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
		ImageManager.printMatrixPixels(impSkeleton, ptsSkeletonMatr, pointOrigin, 1, Color.gray);
				
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
		
//		for(int i = 0; i < ptsChosenMatr.size(); i++)
//		{
//			dimData.add( ptsChosenMatr.get(i).get(0, 0) );
//			dimData.add( ptsChosenMatr.get(i).get(0, 1) );
//		}
		
		PixelPutter.putPixels(impSkeleton, centerPt, 4, 4, Color.pink);

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
		ImageManager.printMatrixPixels(impSkeleton, ptsSkeletonMatr, pointOrigin, 1, Color.gray);
				
		// print all the vectors after reflection on the output images
		ImageManager.printMatrixPixels(impSkeleton, ptsChosenMatr, pointOrigin, 2, Color.blue);
		
		// print the vectors closet to the center of skeleton after translation and transformation on the output images
		PixelPutter.putPixelsOrigin(pointOrigin, impSkeleton, VectorManager.newPoint( ptsChosenMatr.get(0) ) , 4, 4, Color.cyan);
		
		// print the end point vectors after translation and transformation on the output images
		PixelPutter.putPixelsOrigin(pointOrigin, impSkeleton, VectorManager.newPoint( ptsChosenMatr.get(1) ) , 4, 4, Color.red);
		PixelPutter.putPixelsOrigin(pointOrigin, impSkeleton, VectorManager.newPoint( ptsChosenMatr.get(2) ), 4, 4, Color.red);
				
		ImageSaver.saveImagesWithPath(StringManager.getPath(fileOut) + "b"+Integer.toString(larvaId)+".jpg", impSkeleton);
		
//		PixelPutter.putPixels(imagePlusBin_ske, ptEdgeNearest, 4, 4, Color.red);
		
		LinearLine linearLine = MathManager.getLinearLine(ptEdgeNearest, centerPt);
//		DrawingManager.drawLine(imagePlusBin_ske, centerPt, ptEdgeNearest, 2, Color.orange);
		
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
		
		double thicknessDouble = bodyArea / (double) lenSkeleton;
		int thickness = MathManager.getRoundedInt(thicknessDouble);
		
		double waistDouble = bodyArea / ( (double) lenSkeleton * Math.PI );
//		int waist = MathManager.getRoundedInt(waistDouble);
		
		boolean flag = FileManager.createDirectory(intermediateDirPath);
		
		for(Point pt : ptsSkeletonChosen)
		{
			PixelPutter.putPixels(imagePlusBin_ske, pt, 3, 3, Color.orange);
		}
		
		ImageSaver.saveImagesWithPath(intermediateDirPath + "binSke"+Integer.toString(larvaId)+".jpg", imagePlusBin_ske);
		
		if(saveOriginalImage)
			ImageSaver.saveImagesWithPath(StringManager.getPath(fileOut) + "o"+Integer.toString(larvaId)+".jpg", imagePlusOriginal);

		ImagePlus imagePlusOutline = imagePlusOriginal.duplicate();
		IJ.run(imagePlusOutline, "Make Binary", "");
		IJ.run(imagePlusOutline, "Outline", "");
		
		PixelElementTable pixelElementTable = new PixelElementTable(imagePlusOutline, false);
		ArrayList<PixelElementSegment> peSegments = pixelElementTable.getFrameSegments();
		// the number of pixel in the larva's outline
		double perimeterOutline = 0;
		
		for(PixelElementSegment peSegment : peSegments)
		{
			perimeterOutline += peSegment.getPixelElements().size();
		}
		
		
		
//		dimData.add(radius);
//		dimData.add(thicknessDouble);
		dimDataShort.add(bodyArea);
		dimDataShort.add(lenSkeleton);
		dimDataShort.add(waistDouble);
		dimDataShort.add(perimeterOutline);
		
//		return dimData;
		return dimDataShort;
	}

	public static int getNumPoints()
	{
		return NUM_POINTS;
	}

	public static void main(String[] args)
	{
	}

}
