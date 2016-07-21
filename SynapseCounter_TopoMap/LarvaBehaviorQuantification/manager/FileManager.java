package manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileManager 
{

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
	
	public static boolean deleteDir(File dir) 
   {
      if (dir.isDirectory()) {
         String[] children = dir.list();
         for (int i = 0; i < children.length; i++) {
            boolean success = deleteDir
            (new File(dir, children[i]));
            if (!success) {
               return false;
            }
         }
      }
      
      return dir.delete();
  }
	
	public static void main(String[] args) 
	{
//		deleteDir( new File("E:/config.properties") );
		copyFile(new File("E:/config.properties.bk"), new File("E:/config.properties") );
	}
	
}
