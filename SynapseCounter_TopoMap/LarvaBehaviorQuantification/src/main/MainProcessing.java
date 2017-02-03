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
		}
		
		HelperManager.printDuration(timeStart);
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
