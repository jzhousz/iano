package manager;

/**
* The class used to handle string problems.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class StringManager 
{

	/**
	* Get the file name from a path.
	* 
	* @param filePath The file path.
	* @return The file name.
	*/
	public static String getFileName(String filePath) 
	{
//		String filePath = "E:\\Summer 2016\\Larva Project\\Output\\larva.avi";
//		String filePath = "E:\\larva.avi";
		
		int lastIndex = 0;
		
		lastIndex = filePath.lastIndexOf('\\');
		
		if (lastIndex == -1)
			lastIndex = filePath.lastIndexOf('/');
		
		String fileName = filePath.substring(lastIndex+1, filePath.length());
		
		System.out.println("fileName: "+fileName+", lastIndex: "+lastIndex);
		return fileName;
	}
	
	public static String getPath27(String filePath) 
	{
		int lastIndex = 0;
		// for windows system
		lastIndex = filePath.lastIndexOf('\\');
		if (lastIndex == -1)
			// for Mac system
			lastIndex = filePath.lastIndexOf('/');
		
		String fileName = filePath.substring(0, lastIndex + 1);
		return fileName;
	}
	
	public static void main(String args[])
	 {
		String filePath = "E:\\Summer 2016\\Larva Project\\Output\\larva.avi";
		
		String fileName = StringManager.getFileName(filePath);
		
		System.out.println("fileName: "+fileName);
					
	 }
}
