package file;

import java.io.File;

public class FileRenamer {
	
   public static void main(String[] args) 
   {
      
      File file = null;
      File fileNew = null;
      boolean bool = false;
      
      String path = "E:/1/larva_y3.avi_N5_07-09-2016_21-59-53/aAnimation/";
      String fileName = "";
      String fileNameNew = "";
    
      int num = 180;
      
      try{      
    	  
    	  for(int i = num - 1; i >= 1; i--)
    	  {
    		  int diff = num - i + num;
    		  
    		  fileName = "Blue_All_"+Integer.toString(i)+".jpg";
	         // create new File objects
			file = new File(path + fileName);
			
			fileNew = new File(path + "Blue_All_"+Integer.toString(diff)+"_.jpg");
	         
	         // rename file
	         bool = file.renameTo(fileNew);
	         
	         // print
	         System.out.println("Renamed file: " + fileName + ", succeed: " + bool);
    	  }
      }catch(Exception e){
         // if any error occurs
         e.printStackTrace();
      }
      
   }
}