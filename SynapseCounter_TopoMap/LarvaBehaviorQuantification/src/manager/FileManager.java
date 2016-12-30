package manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
* The class used to manage the file.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class FileManager 
{
	/**
	* Create a directory.
	* 
	* @param directory The directory that will be created.
	* @return false if fail to create the directory.
	*/
	public static boolean createDirectory(String directory)
	{
		File dir = new File(directory);

		boolean isSuccessful = true;
		
		// if the directory is not existing, create the directory
		if ( !( dir.exists() && dir.isDirectory() ) ) 
		{
			// attempt to create the directory here
			isSuccessful = dir.mkdirs();
		}
		
		return isSuccessful;
	}
	
	/**
	 * Delete all files in a directory.
	 * 
	 * @param directory The directory.
	 * @return Number of files deleted.
	 */
	public static int deleteAllFiles(String directory)
	{
		File folder = new File(directory);
		
		if ( !folder.exists()  ) 
		{
			return 0;
		}
		
		File[] listOfFiles = folder.listFiles();

		int numFile = 0;
		
		for(File file : listOfFiles)
		{
			FileManager.deleteDir(file);
			numFile++;
		}
		
		return numFile;
	}
	
	/**
	* Get all number cells from csv file and convert them to double values. 
	* Then return the double array.
	* 
	* @param fileName The csv file.
	* @param 2D double array.
	*/
	public static double[][] getDoubleArray(String fileName)
	{
		ArrayList<String[]> doubleList = new ArrayList<String[]>();
		
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        
        double [][] arr = null;

        try {
            br = new BufferedReader(new FileReader(fileName));
            
            while ((line = br.readLine()) != null) 
            {
                // use comma as separator
                String[] fields = line.split(cvsSplitBy);
                
                try
				{
                	// if the 1st column can't converted to double, discard.
                	// ????????????????? Need to check other columns as well
                	// ??????????????????????????
					double num = Double.parseDouble(fields[0]);
					doubleList.add(fields);
				}catch(NumberFormatException ex)
				{
					System.out.print("Y");
				}
            }
            
            arr = new double[doubleList.size()][doubleList.get(0).length];
            
            for(int i = 0; i < doubleList.size(); i ++)
            	for(int j = 0; j < doubleList.get(0).length; j ++)
            	{
            		try
    				{
    					arr[i][j] = Double.parseDouble(doubleList.get(i)[j]);
    					
    				}catch(NumberFormatException ex)
    				{
    					System.out.print("<NumberFormatException>");
    				}

//                    System.out.println("fields[0]:" + fields[0] + " , fields[1]:" + fields[1] + "]");
            	}

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return arr;
	}
	
	/**
	* Rename all the files in a directory.
	* 
	* @param dir The directory the files reside.
	* @param prefix The prefix added to the files.
	*/
	public static void renameFiles(String dir, String prefix)
	{
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();
			
		String nameNew = "";
		
		//		 for (int i = 0; i < listOfFiles.length; i++) 
		for(File file : listOfFiles)
		{
			nameNew = prefix + file.getName();
			File fileNew = new File(dir + nameNew);
			file.renameTo(fileNew);
		}
	}
	
	/**
	 * Copy a file.
	 * 
	 * @param source The source file full path.
	 * @param dest The destination file full path.
	 * @return true if succeed, false if fail.
	 */
	public static boolean copyFile(String source, String dest)
	{
		File fileSource = new File(source);
		File fileDest = new File(dest);
		
		return copyFile(fileSource, fileDest);
	}
	
	/**
	* Copy a file from a place to another place.
	* 
	* @param source The source file.
	* @param dest The destination file.
	* @return true if copy the file successfully.
	*/
	public static boolean copyFile(File source, File dest)
	{
		InputStream input = null;
		OutputStream output = null;
		
		try {
			input = new FileInputStream(source);
			output = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int bytesRead;
			
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}
			
			input.close();
			output.close();
			
			return true;
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	* delete a file.
	* 
	* @param dir The path.
	* @return true if the file was deleted successfully.
	*/
	public static boolean deleteDir(File dir) 
    {
      if (dir.isDirectory()) 
      {
         String[] children = dir.list();
         
         for (int i = 0; i < children.length; i++) 
         {
            boolean success = deleteDir( new File(dir, children[i]) );
            
            if (!success) 
            {
               return false;
            }
         }
      }
      
      return dir.delete();
  }
	
	public static void main(String[] args) 
	{
//		deleteDir( new File("E:/config.properties") );
//		copyFile(new File("E:/config.properties.bk"), new File("E:/config.properties") );
		
//		FileManager.renameFiles("E:/3/temp/", "c5_");
//		System.out.println("Done.");
		
//		System.out.print("delete num: " + deleteAllFiles("E:/3/training_out/"));
		
//		boolean isSuccessful = FileManager.createDirectory("E:/3/training_out/intermediat/");
//		
//		System.out.println("isSuccessful:"+isSuccessful);
//		
//		boolean isSuccessful2 = FileManager.createDirectory("E:/3/training_out/intermediat/");
//		
//		System.out.println(",isSuccessful:"+isSuccessful2);
		
//		int numFiles = FileManager.deleteAllFiles(StringManager.getPath("E:/3/training_out/aOut.csv"));
		System.out.print("path: " + PropertyManager.getPath());
	}
	
}
