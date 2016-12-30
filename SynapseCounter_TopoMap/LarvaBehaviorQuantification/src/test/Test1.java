package test;

import java.awt.Point;
import java.util.ArrayList;

import entities.TrainingData;
import file.ImageSaver;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.BackgroundSubtracter;
import learning.Trainer;
import manager.ImageManager;
import manager.MathManager;

public class Test1
{
	public static int binlog( int bits ) // returns 0 for bits=0
	{
	    int log = 0;
	    if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
	    if( bits >= 256 ) { bits >>>= 8; log += 8; }
	    if( bits >= 16  ) { bits >>>= 4; log += 4; }
	    if( bits >= 4   ) { bits >>>= 2; log += 2; }
	    return log + ( bits >>> 1 );
	}
	
	public static double getDouble(double low, double high, int level, int levelSet)
	{
		double num = -1;
		
		if(level <= levelSet)
		{
			num = (low + high) / 2;
		}
		
		return num;
	}
	
	public static ArrayList<Double> getDouble(int numPoints)
	{
		ArrayList<Double> numArr = new ArrayList<Double>();
		
		double[] number = new double[numPoints];
		
		int numLevel = 4;
		
		double low = 0;
		double high = number.length -1;
		double mid = 0;
		
//		double mid = (low + high) / 2;
		
		numArr.add(low);
		numArr.add(high);
//		numArr.add(mid);
		
//		while(low <= high && number[mid] != searchValue)
		for(int i = 0; i < numLevel; i++)
		{
			
//			if(number[mid] < searchValue)
//			{
//				low = mid + 1;
//			}else{
//				high = mid - 1;
//			}
//			low = mid + 1;
//			high = mid - 1;
			mid = (low + high) / 2;
			
			numArr.add(mid);
			
			
		}
//		if(low > high)
//		{
//			mid = NOT_Found;
//		}
		
		return numArr;
	}
	
	public static void main(String[] args)
	{
//		// get the edge image plus of the larva in the ROI
//		ImagePlus imagePlusEdge = ImageManager.getEdge(imagePlusBigObjShift);
//		ImageSaver.saveImagesWithPath(dirTemp, "imagePlusEdge" + frameId, imagePlusEdge);
//				
//		ArrayList<Point> pointsDiameter = MathManager.calcQuartilePoints(imagePlusEdge, larva);
//
//		larva.setPointsQuartile(pointsDiameter);
		
//		String folder = "E:/1/test/";
//		
//		ImagePlus imagePlusCropped = ImageManager.getImagePlusFromFile("E:/Crop_383.jpg");
//
//		for(int i = 1; i <= 10; i++)
//		{
//			ImagePlus imagePlus = imagePlusCropped.duplicate();
//			
//			BackgroundSubtracter removeBack = new BackgroundSubtracter();
//			removeBack.rollingBallBackground(imagePlus.getProcessor(), i * 5, false, false, false, false, true); // 25
//	
//	//		// convert to 8 gray image
//	//		ImageConverter imageConverter = new ImageConverter(imagePlusBinary);
//	//		imageConverter.convertToGray8();
//	//
//	//		// Convert to Mask
//	//		IJ.run(imagePlusBinary, "Convert to Mask", "");
//	
//			IJ.run(imagePlus, "Make Binary", "");
//			IJ.run(imagePlus, "Erode", "");
//			IJ.run(imagePlus, "Erode", "");
//			IJ.run(imagePlus, "Dilate", "");
//			IJ.run(imagePlus, "Dilate", "");
//			IJ.run(imagePlus, "Fill Holes", "");
//			ImagePlus imagePlusBinary = ImageManager.getLargestBinaryObject(imagePlus);
//			
//			ImageSaver.saveImagesWithPath( folder + "binary_ball_"+i+".jpg", imagePlusBinary);
//			
//			IJ.run(imagePlusBinary, "Fill Holes", "");
//			IJ.run(imagePlusBinary, "Watershed", "");
//			
//			ImageSaver.saveImagesWithPath( folder + "binary_water_ball_"+i+".jpg", imagePlusBinary);
//		}
//		
//		System.out.println("Done!");
		
		
//		TrainingData trainingData = null; 
//		
//		trainingData = Trainer.getTrainingData();
//
//		for(double[] vecs : trainingData.getEigenVectors())
//		{
//			System.out.println("\neigenvector:");
//			for(double vec : vecs)
//				System.out.print(vec + ",");
//		}
		
//		TrainingData trainingData = null;
//		try
//		{
////			FileInputStream fileIn = new FileInputStream("C:/1/trainingData.ser");
//			FileInputStream fileIn = new FileInputStream("C:/Fall_2016/Larva/Eclipse_Workspace/LarvaProjectYao/plugins/Larva/train_test/training_out/trainingData.ser");
//			ObjectInputStream in = new ObjectInputStream(fileIn);
//			trainingData = (TrainingData) in.readObject();
//			in.close();
//			fileIn.close();
//		} catch (IOException ex)
//		{
//			ex.printStackTrace();
//			return;
//		} catch (ClassNotFoundException c)
//		{
//			System.out.println("TrainingData class not found");
//			c.printStackTrace();
//			return;
//		}
//		
//		System.out.println("eigenValue: " + trainingData.getEigenValues());
//		
//		for(double eigenValue : trainingData.getMeanLarva())
//		{
//			System.out.println("eigenValue: " + eigenValue);
//		}
		
		
//		double num = 0.2;
//		int num2 = (int) Math.ceil(num);
//		System.out.println("num2: " + num2);
//		
//		double[][] arr = {{1,2,3},{2,3,4},{3,4,5}};
//		
//		CSVWriter wrt = new CSVWriter("E:/1.csv");
//		wrt.writeDouble2DArray(arr);
//		System.out.println("Data written.");
	}

}
