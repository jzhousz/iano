package file;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TextFileWriter {
	
	static DateFormat dateSysFormat = new SimpleDateFormat("HH:mm:ss");
	static Date dateSys = null;
			
	public static void writeToDebug(String text)
	{
		dateSys = new Date();
		
		String TEXT_FILE = "plugins/Larva/debug.txt";
		PrintWriter textWriter = null;

    	try {
    		textWriter = new PrintWriter(new FileWriter(TEXT_FILE, true));
    		
		}catch(IOException ioe){
	    	   System.out.println("Exception occurred:");
	    	   ioe.printStackTrace();
	    }
    	
    	textWriter.println("("+dateSysFormat.format(dateSys)+ ") " + text);
    	textWriter.close();
	}
	
	public static void writeToFile(String text, String path)
	{
		String TEXT_FILE = path;
		PrintWriter textWriter = null;

    	try {
    		textWriter = new PrintWriter(new FileWriter(TEXT_FILE, true));
    		
		}catch(IOException ioe){
	    	   System.out.println("Exception occurred:");
	    	   ioe.printStackTrace();
	    }
    	
    	textWriter.println(text);
    	textWriter.close();
	}
	
	/**
	 * Write text to a text file.
	 * 
	 * @param text The text to be written to a text file.
	 * @return void
	 */
	public static void writeToFile(String text)
	{
		String TEXT_FILE = "E:\\Summer 2016\\Larva Project\\Output\\yaoLog.txt";
		PrintWriter textWriter = null;

    	try {
			//yaoWriter = new PrintWriter(new File(TEXT_FILE), true);
    		//textWriter = new PrintWriter(new BufferedWriter(new FileWriter(TEXT_FILE)), true);
    		
    		textWriter = new PrintWriter(new FileWriter(TEXT_FILE, true));
    		
		}catch(IOException ioe){
	    	   System.out.println("Exception occurred:");
	    	   ioe.printStackTrace();
	    }
    	
    	textWriter.println(text);
    	
//    	textWriter.append("");
//    	textWriter.append("This is first line");
//    	textWriter.append("This is the second line");
//    	textWriter.append("This is third line");
    	
    	
//    	textWriter.println("This is first line");
//    	textWriter.println("This is the second line");
//    	textWriter.println("This is third line");
    	
    	textWriter.close();
	}
}
