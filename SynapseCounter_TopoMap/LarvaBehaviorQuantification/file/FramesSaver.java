package file;

import java.awt.Button;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import entities.AVIHeaderReader;
import file.ImageSaver;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.plugin.AVI_Reader;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

public class FramesSaver
{
	public static void saveFrames() 
	{
		System.out.println("Started!");
		
		String AVI_FILE = "E:\\Summer 2016\\Larva Project\\Output\\larva.avi"; //default location for avi
		int aviTotalFrames = 0; // the total number of avi video frames
		
		System.out.println("Here 1");
		
		//int frameId = 1; //frame Number
		AVIHeaderReader headerReader = new AVIHeaderReader(); //Used to read the movie
		aviTotalFrames = headerReader.getTotalFrames(AVI_FILE); //get total number of frames
		System.out.println("Here 2");
		System.out.println("aviTotalFrames:"+aviTotalFrames);
		AVI_Reader videoFeed = null;
		ImageStack stackFrame = null;
		System.out.println("Here 3");
		for(int frameCounter = 1; frameCounter < aviTotalFrames; frameCounter++)
		{
			System.out.println("Here 4");
			
			videoFeed = new AVI_Reader(); //read AVI
			stackFrame = videoFeed.makeStack(AVI_FILE, frameCounter, frameCounter, false, false, false);
			System.out.println("Here 5");
			if (stackFrame == null || (stackFrame.isVirtual()&&stackFrame.getProcessor(1) == null))
				return;
			System.out.println("Here 6");
			ImagePlus imagePlus = new ImagePlus();
			System.out.println("Here 7");
			imagePlus.setProcessor(stackFrame.getProcessor(1));
			System.out.println("Here 8");
			ImageSaver.saveImagesWithPath("E:\\Summer 2016\\Larva Project\\Output\\Images_Blue_Frames\\", "_blue_" + frameCounter, imagePlus);
			System.out.println("Here 9");
		}
		
		System.out.println("Done!");
    }

}
