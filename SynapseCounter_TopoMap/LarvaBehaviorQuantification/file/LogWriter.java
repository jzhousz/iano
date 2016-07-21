package file;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogWriter {

	public static void main(String args[])
	 {
		LogWriter.writeLog("Yaoguang Zhong");
	 }
	
	public static void writeLog(String text, String aviFile)
	{
		writeLog(text + " Video: " + aviFile);
	}
	
	/**
	 * Write text to the log.
	 * 
	 * @param text The text to be written to a text file.
	 * @return void
	 */
	public static void writeLog(String text)
	{
//		String TEXT_FILE = "Files/log.txt";
		String TEXT_FILE = "plugins/Larva/log.txt";
		PrintWriter textWriter = null;

		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = new Date();
		//System.out.println(dateFormat.format(date));
		
    	try {
    		textWriter = new PrintWriter(new FileWriter(TEXT_FILE, true));
		}catch(IOException ioe){
	    	   ioe.printStackTrace();
	    }
    	System.out.println(text);
    	textWriter.println("*) "+dateFormat.format(date)+": "+text);
    	textWriter.close();
	}
}
