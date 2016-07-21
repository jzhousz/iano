package manager;

public class StringManager {

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
	
	public static String getPath2(String filePath) 
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
