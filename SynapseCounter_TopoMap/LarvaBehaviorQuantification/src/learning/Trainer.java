package learning;

import java.io.File;

import manager.FileManager;
import manager.StringManager;

public class Trainer
{
	String csvFileOut = "E:/3/training_out/aOut.csv"; // the output csv file
//	String imageDir = "E:/3/training/"; // the input image folder
	String imageDir = "E:/3/training_test/";

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
