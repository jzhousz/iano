package learning;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import entities.TrainingData;
import manager.FileManager;
import manager.PropertyManager;
import manager.StringManager;

public class Trainer
{
	public static String csvFileOut = PropertyManager.getPath() + "training_out/aOut.csv"; // the output csv file
//	String imageDir = PropertyManager.getPath() + "training/"; // the input image folder
	String imageDir = PropertyManager.getPath() + "training_larva/";
	public static String imageTrainDir = PropertyManager.getPath() + "training_larva/";

	public static void main(String[] args)
	{
		Trainer.startTrain();
		
//		// delete all files in these directories
//		int numFiles = FileManager.deleteAllFiles(StringManager.getPath(csvFileOut));
////		numFiles = FileManager.deleteAllFiles( imageTrainDir );
//		
//		// the trained
//		Trainer trainer = new Trainer();
//		// convert the larva images to larva descriptors
//		trainer.convertToDescriptos();
//		// get the trainer output csv file name
//		String csvTrainOut = trainer.getCsvFileOut();
//		System.out.println("(Trainer.java) after: trainer.getCsvFileOut()");
//		// new PCA calculator to calculate eigenvector and eigenvalue
//		PCACalculator pcaCalc = new PCACalculator();
//		System.out.println("(Trainer.java) after: new PCACalculator()");
//		// calculate eigenvector and eigenvalue using the training data 
//		pcaCalc.train(csvTrainOut);
//		System.out.println("(Trainer.java) after: pcaCalc.train(csvTrainOut)");
	}
	
	public static void startTrain()
	{
		// delete all files in these directories
		int numFiles = FileManager.deleteAllFiles(StringManager.getPath(csvFileOut));
//				numFiles = FileManager.deleteAllFiles( imageTrainDir );
		
		// the trained
		Trainer trainer = new Trainer();
		// convert the larva images to larva descriptors
		trainer.convertToDescriptos();
		// get the trainer output csv file name
		String csvTrainOut = trainer.getCsvFileOut();
		System.out.println("(Trainer.java) after: trainer.getCsvFileOut()");
		// new PCA calculator to calculate eigenvector and eigenvalue
		PCACalculator pcaCalc = new PCACalculator();
		System.out.println("(Trainer.java) after: new PCACalculator()");
		// calculate eigenvector and eigenvalue using the training data 
		pcaCalc.train(csvTrainOut);
		System.out.println("(Trainer.java) after: pcaCalc.train(csvTrainOut)");
	}
	
	public static TrainingData getTrainingData()
	{
		TrainingData trainingData = null;
		try
		{
			FileInputStream fileIn = new FileInputStream(StringManager.getPath(Trainer.csvFileOut) 
					+ "trainingData.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			trainingData = (TrainingData) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException ex)
		{
			ex.printStackTrace();
		} catch (ClassNotFoundException c)
		{
			System.out.println("TrainingData class not found");
			c.printStackTrace();
		}
		
		return trainingData;
	}
	
	public void convertToDescriptos()
	{
		// delete all files in this directory
		FileManager.deleteAllFiles(StringManager.getPath(csvFileOut));
		
		String fileInput;

		File folder = new File(imageDir);
		File[] listOfFiles = folder.listFiles();

		// for all testing images, save all data about the larvae on them
		for (int i = 0; i < listOfFiles.length; i++)
		{
			fileInput = listOfFiles[i].getAbsolutePath();
			Descriptor.saveDimensionData(i, fileInput, csvFileOut, Descriptor.getNumPoints(), true);

			System.out.println("(Larva) Done with " + (i + 1) + " / " + listOfFiles.length + ".");
		}

		System.out.println("(Larva) Done. Converted all larvae to descriptors.");
	}

	/**
	* Getter.
	* 
	*/
	public String getCsvFileOut()
	{
		return csvFileOut;
	}

	public void setCsvFileOut(String csvFileOut)
	{
		this.csvFileOut = csvFileOut;
	}

	public String getImageDir()
	{
		return imageDir;
	}

	public void setImageDir(String imageDir)
	{
		this.imageDir = imageDir;
	}

}
