package manager;

import java.io.File;

import ij.ImagePlus;

public class DirectoryManager {

	public static boolean createDirectory(String directory)
	{
		File dir = new File(directory);

		// attempt to create the directory here
		boolean isSuccessful = dir.mkdirs();
		
//		if (isSuccessful) {
//			// creating the directory succeeded
//			System.out.println("directory was created successfully");
//		} else {
//			// creating the directory failed
//			System.out.println("failed trying to create the directory");
//		}
		
		return isSuccessful;
	}
	
	public static void main(String[] args) 
	{
		boolean isSuccessful = DirectoryManager.createDirectory("E:\\Summer 2016\\Larva Project\\Output\\Test\\Images_Temp\\06-08-2016_19-41\\Animation");
		boolean isSuccessful2 = DirectoryManager.createDirectory("E:\\Summer 2016\\Larva Project\\Output\\Test\\Images_Temp\\06-08-2016_19-41\\Skeleton");
	}
			
}
