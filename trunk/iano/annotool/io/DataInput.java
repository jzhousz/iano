package annotool.io;

import java.io.*;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.*;
import java.util.ArrayList;


/* Read images from a directory using ImageJ's utilities. 
 * Add channel number  (r or g or b) as an input option for RGB images. 
 * This class can read data for CV mode or TT mode.  
 * In TT mode, directory is used as training directory.
 */ 
public class DataInput
{
	// The image types are the same as those defined in ImageJ's ImagePlus.
	//  8-bit grayscale (unsigned)  (return byte[])
	public static final int GRAY8 = 0;  
	    
	//16-bit grayscale (unsigned)  (return int[])
	public static final int GRAY16 = 1; 
	    
	//32-bit floating-point grayscale  (return float[])
	public static final int GRAY32 = 2; 
	    
	//8-bit indexed color  (return byte[])
	public static final int COLOR_256 = 3; 
	    
	//32-bit RGB color  (return byte[])
	public static final int COLOR_RGB = 4; 
	 
	//problem properties
	protected ArrayList data = null; //store all images in the dir with given ext.
	int lastStackIndex = 0; // the variable to track if the last getData() was for the same stack
	String[] children = null; //list of image file names in the dir
	protected int height = 0;
	protected int width = 0;
	protected int stackSize = 0;
	protected int imageType = 0;
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


	Object openOneImage(String path, int stackIndex)
	{
		ImagePlus imgp = new ImagePlus(path); //calls the opener.openImage()

		//stack from 1 to number of slices
		ImageProcessor ip = imgp.getStack().getProcessor(stackIndex);
		Object results = null;

		if (resize)
			ip  = ip.resize(width,height);

		int width = ip.getWidth();
		int height = ip.getHeight();
		//width and height should be the same for all images. -- error handling
		if(width != this.width || height != this.height)
		{
			System.err.println("Image" + path + "is not the same size as the 1st one. Ignored.");
			return null;
		}

		//get the pixel values. We only deal with one color for RGB picture
		if (ip instanceof ByteProcessor)
		{
			/*
			//should use array copy since memory was also allocated in ip, o/w values are not passed back
			byte[] returnedPixels = (byte[]) (ip.getPixels());
			System.arraycopy(returnedPixels, 0, pixels, 0, width*height); //020209
			*/
			
			results = ip.getPixels();
			
		}
		else if (ip instanceof ColorProcessor)
		{
			//System.out.println("RGB image..");
			//note: tmppixels contains the irrelevant channels.
			byte[] pixels= new byte[width*height];
			byte[] tmppixels= new byte[width*height];

			if (channel == "r")
				((ColorProcessor)ip).getRGB(pixels,tmppixels,tmppixels);
			else if (channel == "g")
				((ColorProcessor)ip).getRGB(tmppixels,pixels,tmppixels);
			else
				((ColorProcessor)ip).getRGB(tmppixels,tmppixels,pixels);
			
			results = pixels;

			//debug: show the image
			/* if(annotool.Annotator.debugFlag.equals("true"))
			{
				ImagePlus testimg = NewImage.createByteImage(path+":channel "+channel,  width, height, 1, NewImage.FILL_BLACK);
				ImageProcessor test_ip = testimg.getProcessor();
				test_ip.setPixels(pixels);
				testimg.show();
				//testimg.updateAndDraw();
			}*/
			
			
		}
		else  if (ip instanceof ShortProcessor || ip instanceof FloatProcessor)
		{
			//16 bit or 32 bit grayscale
			results =  ip.getPixels();
			
			
			//get pixels return an array of int
			 /* Returns a reference to the short array containing this image's
	        pixel data. To avoid sign extension, the pixel values must be
	        accessed using a mask (e.g. int i = pixels[j]&0xffff). 
			http://www.imagingbook.com/fileadmin/goodies/ijtutorial/tutorial171.pdf sec 4.8
			*/	
			/* System.out.println("convert 16 bit gray scale or 32 bit floating point images to 8-bit images.");
			//what about the relativity in the image set?
			//ImageProcess ip2= ip.convertToByte(true);	 //scale to 0 and 255, if false, values are clipped.
			//byte[] returnedPixels = (byte[]) (ip2.getPixels());
			//System.arraycopy(returnedPixels, 0, pixels, 0, width*height);
			*/
				
			//alternative solutions without loss of precision:  09/01/2011 
			//1. get int[] or float[], then pass to the algorithm as Object, together with an image type, so that the algorithm would do the casting
			//   i.e.  getData() returns Object.
			//2. pass the ip or ip[] to algorithm so it can do anything, assuming the developer knows ImageJ programming. 
			//Or: convert to byte, but pass ArrayList of byte[] to avoid memory copy. Maybe save some memory, depending on how gc works.
			//    or: getData() return ArrayList of Object, each item is a data array
			//     then: add: getType() to find out the datatype to cast to.
			//Object returnedPixels = Object (ip.getPixels()); //-- int[]
		}
		else
		{
			System.err.println("Image type is not supported.");
			//throw new Exception("Image type is not supported.");
			System.exit(0);
		}
		
		return results;
	}

	private ArrayList readImages(String directory, String ext, String[] children, int stackIndex)
	{
		//read the 1st one to get some properties
		//System.out.println("The first image in dir is "+ children[0] + ". Reading total "+ children.length + " images ..");
		//Assumption: All images are of the same size as image 1.
		ImagePlus imgp = new ImagePlus(directory+children[0]);
		imageType = imgp.getType();

		if (!resize)
		{
			width = imgp.getProcessor().getWidth();
			height = imgp.getProcessor().getHeight();
			stackSize = imgp.getStackSize();
		}

		//allocate the memory for the problem
		data = new ArrayList(children.length);
		
		//fill the data
		for (int i=0; i<children.length; i++)
			data.add(openOneImage(directory+children[i],  stackIndex));
		
		return data;
	}

	/** return the pixel data -- use a mask to avoid sign extension
	  		such as data[i][j]&0xff
	  		The data is for CV mode, or training data in TT mode.
	 **/
	public ArrayList getData()
	{
		//return the first slice for normal 2D images.
		return getData(1);
	}

	/** return the pixel data -- use a mask to avoid sign extension
		such as data[i][j]&0xff
		The data is for CV mode, or training data in TT mode.
		stackIndex:  between 1 and stackSize
    **/
	public ArrayList getData(int stackIndex)
{

	//check if need to read the data based on lastStackIndex
	if (data == null ||  lastStackIndex != stackIndex)
    {
 	   String[] children = getChildren();
	   data = readImages(directory, ext, children, stackIndex);
    }  
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
	
	
    //reset data for facilitate gc
	public void setDataNull()
	{
	    data = null;	
	}

	public ImagePlus getImagePlus(int i)
	{
		return (new ImagePlus(directory+children[i]));
	}
	
	public int getImageType()
	{
		return imageType;
	}
	
	 /** 
	 *  Get one image with all the stacks
	 *  Each item in the ArrayList is a stack.
	 *  Intended for 3D images.
	 *  The stack index for the returned ArrayList starts from 0.
	 *  @param imageindex
	 *  @return return an ArrayList of data array.
	 */
	public ArrayList getAllStacksOfOneImage(int imageindex)
	{
		int stackSize = getStackSize();
	    ArrayList data = new ArrayList(stackSize);

	    //stack from 1 to number of slices
		for(int stackIndex = 1; stackIndex <= stackSize; stackIndex++)
			data.add(openOneImage(directory+children[imageindex], stackIndex));
	    
		return data;
	}
	
}