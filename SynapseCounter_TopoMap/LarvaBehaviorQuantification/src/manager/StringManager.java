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
	 * Get the substring in a string between two string excluding the fieldStart and fieldEnd.
	 * @param str The string searched from.
	 * @param fieldStart The string in the left of the searching substring.
	 * @param fieldEnd The string in the right of the searching substring.
	 * @return
	 */
	public static String getSubStrBetween(String str, String fieldStart, String fieldEnd) 
	{
		String strReturn = "";
		
		int lastIndex1 = str.lastIndexOf(fieldStart);
		int lastIndex2 = str.lastIndexOf(fieldEnd);
		
//		System.out.println("lastIndex1:"+lastIndex1+",lastIndex2:"+lastIndex2);
		
//		strReturn = str.substring(2, 4);
		
		if (lastIndex1 != -1 && lastIndex2 != -1)
		{
			strReturn = str.substring(lastIndex1 + fieldStart.length(), lastIndex2);
		}
		
		return strReturn;
	}

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
	
	public static String getPath(String filePath) 
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
		
		String imageFileIn = "E:/3/segmentation/input/s1.jpg";
		
		System.out.println("str: "+getSubStrBetween(imageFileIn, "/s",".jpg"));
		
//		String filePath = "E:\\Summer 2016\\Larva Project\\Output\\larva.avi";
//		String filePath2 = "E:/3/test_out/aOut.csv";
//		
//		String fileName = StringManager.getFileName(filePath);
//		
//		System.out.println("fileName: "+fileName);
//		
////		String fileName = StringManager.getFileName(filePath);
//		
//		System.out.println("path: "+StringManager.getPath( filePath2 ));
					
	 }
}
