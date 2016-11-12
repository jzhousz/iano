package main;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;

import entities.CSVReader;
import file.ImageSaver;
import ij.ImagePlus;
import ij.process.ImageConverter;
import learning.PCACalculator;
import learning.Tester;
import learning.Trainer;
import manager.AnnotationManager;
import manager.FileManager;
import manager.ImageManager;
import manager.MathManager;
import manager.StringManager;
import manager.VectorManager;
import segmentation.LarvaImage;
import segmentation.LarvaSegment;

public class TrainTester
{
	public static void main(String[] args)
	{
//		trainTest();
	}
	
	public static void trainTest(ArrayList<LarvaImage> larvaImages)
	{
		// the trainer
		Trainer trainer = new Trainer();
		// convert the larva images to larva descriptors
		trainer.convertToDescriptos();
		// get the trainer output csv file name
		String csvTrainOut = trainer.getCsvFileOut();
		
		Tester tester = new Tester();
		tester.convertToDescriptos();
		
		// new PCA calculator to calculate eigenvector and eigenvalue
		PCACalculator pcaCalc = new PCACalculator();
		// calculate eigenvector and eigenvalue using the training data 
		pcaCalc.train(csvTrainOut);
		
		// load all data from testing data file to 2D double array
		double[][] arr = FileManager.getDoubleArray(tester.getCsvFileOut());
		
		// the probabilities for testing larvae
		ArrayList<double[]> probabilities = new ArrayList<double[]>();
		
		// for each row in the testing data file
		for(double[] a : arr)
		{
			// convert the double array to SimpleMatrix
			SimpleMatrix larvaTest = VectorManager.newSimpleMatrix( a ); 
			
			// get the possibility that show how likely the larva evaluate to be a larva
			double[] probability = pcaCalc.test( larvaTest );
			
			probabilities.add(probability  ); // add it to the list to save
		}
		
		drawProbabilityOnImage(tester, probabilities, larvaImages);
		
		System.out.println("(Larva) trainTest completed!");
	}
	
	public static void drawProbabilityOnImage(Tester tester, ArrayList<double[]> probabilities, ArrayList<LarvaImage> larvaImages)
	{
		ArrayList<String[]> fieldsArr = CSVReader.readCSV(tester.getCsvFileOut());
		
		// for each probability, save it to an image with the corresponded larva  
		for(int i = 0; i < probabilities.size(); i++)
		{
			String fileName = StringManager.getPath( tester.getCsvFileOut() ) + "o" + Integer.toString(i) + ".jpg";
			ImagePlus imagePlus = ImageManager.getImagePlusFromFile( fileName );
			
			// convert the image plus to RGB image
			ImageConverter imageConverterOrg = new ImageConverter(imagePlus);
			imageConverterOrg.convertToRGB();
			
			LarvaSegment.setLarvaSegment(larvaImages, i, probabilities.get(i)[0], probabilities.get(i)[1]);
			LarvaSegment larvaSegment = LarvaSegment.getLarvaSegment(larvaImages, i, probabilities.get(i)[0], probabilities.get(i)[1]);
			
			// calculate both part 1 and the complete probabilities for a testing larva
			String str1 = Double.toString( MathManager.get2DecimalPoints( probabilities.get(i)[0])  );
			String str2 = Double.toString( probabilities.get(i)[1] );
			
			// mark both part 1 and the complete probabilities to the image
			AnnotationManager.annotate(imagePlus, new Point(10,10), str1, Color.red, 8);
			AnnotationManager.annotate(imagePlus, new Point(10,40), str2, Color.red, 8);
			// mark the thickness on the image
			AnnotationManager.annotate(imagePlus, new Point(10,70), fieldsArr.get(i)[fieldsArr.get(i).length-1], Color.red, 8);
//			AnnotationManager.annotate(imagePlus, new Point(10,100), fieldsArr.get(i)[fieldsArr.get(i).length-2], Color.red, 8);
			AnnotationManager.annotate(imagePlus, new Point(10,85), "o"+larvaSegment.fileName, Color.red, 8);
			
			if(larvaSegment != null)
				AnnotationManager.annotate(imagePlus, new Point(10,100), larvaSegment.fileName, Color.red, 8);
			
			// save the images
//			ImageSaver.saveImagesWithPath(StringManager.getPath(tester.getCsvFileOut()) + "o"+Integer.toString(i)+".jpg", imagePlus);
			ImageSaver.saveImagesWithPath(StringManager.getPath(tester.getCsvFileOut()) + "o"+larvaSegment.fileName, imagePlus);
			
			System.out.println( "Larva ["+i+"]: "+"Prob1: " + str1 
					+ ", Prob2: " + probabilities.get(i)[1]);
		}
	}

}
