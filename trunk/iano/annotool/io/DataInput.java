package annotool.io;

import java.io.*;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.*;


/* Read images from a directory using ImageJ's utilities. 
 * Add channel number  (r or g or b) as an input option for RGB images. 
 * This class can read data for CV mode or TT mode.  
 * In TT mode, directory is used as training directory.
 */ 
public class DataInput
{
	//problem properties
	protected byte[][] data = null; //store all images in the dir with given ext.
	int lastStackIndex = 0; // the variable to track if the last getData() was for the same stack
	String[] children = null; //list of image file names in the dir
	protected int height = 0;
	protected int width = 0;
	protected int stackSize = 0;
	String directory;
	String ext;

	String channel = annotool.Annotator.channel;
	boolean resize = false;

	public DataInput(String directory, String ext, String channel)
	{
		this.directory = directory;
		this.ext = ext;
		this.channel = channel;
	}
	
	//if the image is to be resized before processing
	public DataInput(String directory, String ext, String channel, int newwidth, int newheight)
	{
		this.directory = directory;
		this.ext = ext;
		this.channel = channel;
		this.height = newheight;
		this.width = newwidth;
		resize = true;
	}

	//This constructor uses the default channel setting or when the image is b/w.
	public DataInput(String directory, String ext)
	{
		this.directory = directory;
		this.ext = ext;
	}


	void openOneImage(String path, byte[] pixels, byte[] tmppixels, int stackIndex)
	{
		ImagePlus imgp = new ImagePlus(path); //calls the opener.openImage()
		//ImageProcessor ip = imgp.getProcessor();
		//stack from 1 to number of slices
		ImageProcessor ip = imgp.getStack().getProcessor(stackIndex);

		if (resize)
			ip  = ip.resize(width,height);

		int width = ip.getWidth();
		int height = ip.getHeight();
		//width and height should be the same for all images. -- error handling
		if(width != this.width || height != this.height)
		{
			System.err.println("Image" + path + "is not the same size as the 1st one. Ignored.");
			return;
		}

		//get the pixel values. We only deal with the one color for RGB picture
		if (ip instanceof ByteProcessor)
		{
			//should use array copy since memory was also allocated in ip, o/w values are not passed back
			byte[] returnedPixels = (byte[]) (ip.getPixels());
			System.arraycopy(returnedPixels, 0, pixels, 0, width*height); //020209
		}
		else if (ip instanceof ColorProcessor)
		{
			//System.out.println("RGB image..");
			//note: tmppixels contains the irrelevant channels.
			if (channel == "r")
				((ColorProcessor)ip).getRGB(pixels,tmppixels,tmppixels);
			else if (channel == "g")
				((ColorProcessor)ip).getRGB(tmppixels,pixels,tmppixels);
			else
				((ColorProcessor)ip).getRGB(tmppixels,tmppixels,pixels);

			//debug: show the image
			if(annotool.Annotator.debugFlag.equals("true"))
			{
				ImagePlus testimg = NewImage.createByteImage(path+":channel "+channel,  width, height, 1, NewImage.FILL_BLACK);
				ImageProcessor test_ip = testimg.getProcessor();
				test_ip.setPixels(pixels);
				testimg.show();
				//testimg.updateAndDraw();
			}
		}
		else
			System.err.println("ImageProcessor: not a ByteProcessor or ColorProcessor. (Maybe Float or ShortProcessor.)");
	}

	private byte[][] readImages(String directory, String ext, String[] children, int stackIndex)
	{
		//read the 1st one to get some properties
		//System.out.println("The first image in dir is "+ children[0] + ". Reading total "+ children.length + " images ..");
		//Assumption: All images are of the same size as image 1.
		ImagePlus imgp = new ImagePlus(directory+children[0]);

		if (!resize)
		{
			width = imgp.getProcessor().getWidth();
			height = imgp.getProcessor().getHeight();
			stackSize = imgp.getStackSize();
		}

		//allocate the memory for the problem
		int length = children.length;
		data= new byte[length][width*height];
		//used as temporary storage for irrelevant channels (color image)
		byte[] tmppixels= new byte[width*height];

		//fill the data
		for (int i=0; i<length; i++)
			openOneImage(directory+children[i], data[i], tmppixels, stackIndex);
		
		return data;
	}

	/** return the pixel data -- use a mask to avoid sign extension
	  		such as data[i][j]&0xff
	  		The data is for CV mode, or training data in TT mode.
	 **/
	public byte[][] getData()
	{
		//return the first slice for normal 2D images.
		return getData(1);
	}

	/** return the pixel data -- use a mask to avoid sign extension
		such as data[i][j]&0xff
		The data is for CV mode, or training data in TT mode.
		stackIndex:  between 1 and stackSize
**/
public byte[][] getData(int stackIndex)
{
   //if (data == null) //bug: always get the first stack with this check. 01/09/09
   //{
     if (data == null ||  lastStackIndex != stackIndex)
     {
 	   String[] children = getChildren();
	   data = readImages(directory, ext, children, stackIndex);
     }  
   //}
   lastStackIndex = stackIndex; //update the index of the last read stack.
   
   return data;
}

	
	//getter should be called by images are read.
	public int getLength()
	{
		if (children == null)
		{
			System.out.println("Read the images to get info.");
			String[] children = getChildren();
		}
			
		return children.length;
	}

	public int getWidth()
	{
		if(width == 0)
		{
			System.out.println("Read the first image to get info.");
			String[] children = getChildren();
			ImagePlus imgp = new ImagePlus(directory+children[0]);
			width = imgp.getProcessor().getWidth();
		}

		return width;
	}

	public int getHeight()
	{
		if(height == 0)
		{
			System.out.println("Read the first image to get info.");
			String[] children = getChildren();
			ImagePlus imgp = new ImagePlus(directory+children[0]);
			height = imgp.getProcessor().getHeight();
		}

		return height;
	}

	public int getStackSize()
	{
		if(stackSize == 0)
		{
			System.out.println("Read the first image to get info.");
			String[] children = getChildren();
			ImagePlus imgp = new ImagePlus(directory+children[0]);
			stackSize = imgp.getStackSize();
		}

		return stackSize;
	}

	/* this will be called by public interface methods depending on training/testing */
	private String[] getChildren(String directory, final String ext)
	{
		    String[] children;
		
			File dir = new File(directory);
			FilenameFilter filter = new FilenameFilter()
			{
				public boolean accept(File dir, String name)
				{ 
					if (ext.equals(".*"))
						return true;
					else
						return name.endsWith(ext);
				}
			};
			children = dir.list(filter);
			if (children == null)
				System.err.println("Problem reading files from the image directory.");
		
		return children;
	}

	//get the testing files or one set CV files
	public String[] getChildren()
	{
		if (children == null)
	   	 children = getChildren(directory, ext);
		
		return children;
	}

	//return true if the images are color images
	public boolean isColor(String path)
	{
		ImagePlus imgp = new ImagePlus(path); 
		ImageProcessor ip = imgp.getProcessor();
		if(ip instanceof ColorProcessor)
		   return true;
	   else
			return false;
	}

	public boolean is3D(String path)
	{
		ImagePlus imgp = new ImagePlus(path);
		int stackSize = imgp.getStackSize();
	    if (stackSize > 1) 
	      return true;
	      else return false;
	}
	
	
    //reset data
	public void setDataNull()
	{
	    data = null;	
	}

	//
	public ImagePlus getImagePlus(int i)
	{
		return (new ImagePlus(directory+children[i]));
	}
	
	
}