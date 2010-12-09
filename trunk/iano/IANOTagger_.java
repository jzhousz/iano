import annotool.AnnotatorGUI;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.PlugInFrame;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.*;
import ij.plugin.frame.*;
import ij.io.*;
import java.io.*;

/**
 *  Jie Zhou March 2009
 *
 *  This is the IANOTagger under IMAGEJ used as an IJ plugin.
 *  Its compiled class should be put under ImageJ/plugins/IANO/.
 *  Note that ImageJ/ImageJ.cfg needs to be modified to add cp, library path.
 *
 *  This class gets the Region Of Interest from the opened image, tag it, and
 *  save to a file for later use as training samples of IANO annotation.
 *
 *  Tagger GUI is a simple pop up dialog.
 *  Tag file format:
 *      name_of_problem
 *      1  a1.tiff
 *      2  a2.tiff
 *
 */
public class IANOTagger_ implements PlugInFilter {

    ImagePlus imp;
	ImageProcessor ip;

    public IANOTagger_() {

        System.out.println("In IANOTagger");
    }


    public void run(ImageProcessor ip) {

		//use GenericDialog
		GenericDialog dialog = new GenericDialog("IANO Tagger");
		dialog.addStringField("tag for the Region of Interest", "1");
		dialog.addStringField("tag for the Region of Interest", "image1.tiff");
		dialog.addStringField("file name to save the tag", "tag.txt");
		dialog.showDialog();
		if (dialog.wasCanceled()) {
		  IJ.error("PlugIn canceled!");
		  return;
         }
		String tag = dialog.getNextString();
		String imgfilename = dialog.getNextString();
		String tagfilename = dialog.getNextString();

		//save the image
        java.awt.Rectangle rec = ip.getRoi();
        ImageProcessor roiip = ip.crop();
        ImagePlus roiplus = new ImagePlus(imgfilename, roiip);
        FileSaver saver = new FileSaver(roiplus);
        boolean ok1 = saver.save(); //contains the save dialog

		//save the tag (append)
		boolean ok2 = writeTag(tag, tagfilename, imgfilename);

		if (ok1 && ok2)
			IJ.showMessage("Both image file and tag file were saved successfully.");
		else
			IJ.error("Either image file or tag file was not successfully saved!");
    }


	public int setup(java.lang.String arg, ImagePlus imp)
	{
		this.imp = imp;
		return DOES_ALL;
	}

	//append the tag info to a file.
	protected boolean writeTag(String tag, String tagfilename, String imagefilename)
	{
		boolean ok2 = true;
		SaveDialog sd = new SaveDialog("Save tag...", tagfilename,".txt");
		String directory = sd.getDirectory();
		String fileName = sd.getFileName();
		IJ.showStatus("Saving: " + directory + fileName);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(directory+fileName, true));
			bw.write(tag+"\t"+imagefilename+"\n");
			bw.close();
		}catch(Exception e)
		{
			ok2 = false;
		}

		return ok2;
	}



}



