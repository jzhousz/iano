package learning;

import java.io.File;
import manager.FileManager;
import manager.StringManager;

public class Tester
{
	public static String csvFileOut = "E:/3/test_out/aOut.csv";
//	String imageDirIn = "E:/3/test/";
//	String imageDirIn = "E:/3/separated_test/";
	public static String imageDirIn = "E:/3/segmentation/output_segments/";
//	String imageDirIn = "E:/3/segmentation/output_segments_test/";
	
	public void convertToDescriptos()
	{
		// delete all files in this directory
		FileManager.deleteAllFiles(StringManager.getPath(csvFileOut));

		String fileInput;

		File folder = new File(imageDirIn);
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

	public String getCsvFileOut()
	{
		return csvFileOut;
	}

	public void setCsvFileOut(String csvFileOut)
	{
		this.csvFileOut = csvFileOut;
	}

}
