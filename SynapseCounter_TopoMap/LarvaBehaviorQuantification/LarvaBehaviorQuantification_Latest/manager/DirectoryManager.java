package manager;

import java.io.File;

/**
* The class used to create directory.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class DirectoryManager 
{

	/**
	* Create a directory.
	* 
	* @param directory The directory that will be created.
	* @return true is the directory has been created.
	*/
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
