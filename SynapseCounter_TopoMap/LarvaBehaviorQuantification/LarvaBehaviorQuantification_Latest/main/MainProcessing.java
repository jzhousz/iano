package main;

import ij.ImagePlus;
import ij.process.ImageConverter;
import manager.HelperManager;

/**
* The main file process the image analysis process without GUI.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class MainProcessing 
{
	public static void main( String args[])
	{
		System.out.println("You specified arguments as following: ");
		for(int i=0; i<args.length; i++)
			System.out.print("args["+i+"]:"+args[i]+", ");

		System.out.println("\n"); // new lines
		
		long timeStart = System.nanoTime();
		
//		String larvaId = "0";
//		String frameIdStart = "0";
		
//		if(args.length > 5)
//		{
//			larvaId = args[4];
//			frameIdStart = args[5];
//		}
		
		if(args.length < 4)
		{
			printUsage();
			return; 
		}
		else
		{
			// if the 4th argument is not true or false, show error message and exit
			if( !( args[3].equalsIgnoreCase("true") || args[3].equalsIgnoreCase("false") ) )
			{
				System.out.println("Error: Wrong 4th argument: "+args[3]+". Need to be true or false.\n");
				printUsage();
				return;
			}
			
			Boolean isChrimsonStimulus = false;
			if( args[3] == "true" )
				isChrimsonStimulus = true;

			Boolean[] outPutOptions = new Boolean[4];
			outPutOptions[0] = true;
			outPutOptions[1] = true;
			outPutOptions[2] = true;
			outPutOptions[3] = true;
			
//			LarvaProcessor flyLarvaProcessor = new LarvaProcessor( args[0], args[1], args[2], false, 
//					"C:\\Users\\Shawn\\Desktop\\Research\\AVIProcessed\\", outPutOptions, isChrimsonStimulus, larvaId, frameIdStart );
			
//			LarvaProcessor flyLarvaProcessor = new LarvaProcessor( args[0], args[1], args[2], false, 
//					"C:\\Users\\Shawn\\Desktop\\Research\\AVIProcessed\\", outPutOptions, isChrimsonStimulus );
			
//			flyLarvaProcessor.run();
		}
		
		HelperManager.printDuration(timeStart);
		
//		double seconds = ( System.nanoTime() - time ) / 1000000000.0;
//		int hours = (int) seconds / 3600;
//		double seconds_Remain = seconds % 3600;
//		int minutes = (int) seconds_Remain / 60;
//		int sec = (int) ( seconds_Remain - minutes * 60 );
//		
////		System.out.println( "Time Elapsed (Format): " + hours + " Hour "+ minutes +" Minute "+ sec +" Second" );
//		String timeStr = "Time Completed: ";
//		
//		if( hours > 0 )
//			timeStr += hours + " Hour ";
//		
//		if( minutes > 0 )
//			timeStr += minutes + " Minute ";
//		
//		timeStr += sec + " Second";
//		
//		System.out.println(timeStr);
		
//		System.out.println( "Time Elapsed: " + ( (System.nanoTime() - time) /  1000000000.0 ) + " Seconds" );
	}
	
	/**
	* Print usage if the input arguments weren't correct.
	* 
	*/
	public static void printUsage()
	{
		System.out.println("Usage: avifile startX startY chrimson");
		System.out.println("Parameters:");
		System.out.println("   - avifile: Full path of the avi video.");
		System.out.println("   - startX : The x coordinate of the top left corner of the larva ROI (ROI size is 120*120) on the first frame of avi video.");
		System.out.println("   - startY : The y coordinate of the top left corner of the larva ROI (ROI size is 120*120) on the first frame of avi video.");
        System.out.println("   - chrimson: false: the optogenetics (blue) as the stimulus, true: the chrimson light as the stimulus.");
		System.out.println(" Example (optogenetics): E:\\larva.avi 644 190 false");
		System.out.println(" Example (chrimson)    : E:\\larva.avi 644 190 true");
		System.out.println(" Note: If the software is provided as a jar file, then do the following");
		System.out.println("      java -jar FlylarvaTracking.jar larva.avi 644 190");
	}
	
}
