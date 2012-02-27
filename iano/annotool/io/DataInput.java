package annotool.io;

import java.io.*;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.process.*;
import java.util.ArrayList;


/* Read images from a directory using ImageJ's utilities. 
 * Add channel number  (r or g or b) as an input option for RGB images. 
 * This class can read data for CV mode or TT mode.  
 * In TT mode, directory is used as training directory.
 * 
 * Feb. 2011: Added HeightList, WidthList, ofSameSize and getters to accommodate different image size 
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
	protected int height = 0; //height of the first image
	protected int width = 0;  //width of the first image
	int[] widthList = null;  //add width, height lists. Moved from DataInputDynamic 02/2012
	int[] heightList = null;
	int[] depthList = null;
	protected int stackSize = 0;
	protected int imageType = 0;
	boolean ofSameSize = true; //added Feb 2012 to combine DataInputDynamic
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
	
	//02/27/2012  Take an image and a collection of ROIs. An alternative for ROI reading.
	public DataInput(ImagePlus image, Roi[] rois)
	{
		//TBA;
	}
	
	//02/27/2012 
	//a directory hierarchy that has images of different classes in different subdirectories
	//It will not need a target file.
	public DataInput(String[] directory)
	{
		
		
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

		//get the pixel values. We only deal with one color for RGB picture
		if (ip instanceof ByteProcessor)
			results = ip.getPixels();
		else if (ip instanceof ColorProcessor)
		{
			//System.out.println("RGB image..");
			byte[] pixels= new byte[width*height];
			byte[] tmppixels= new byte[width*height];  //for the irrelevant channels.

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
		}
		else
		{
			System.err.println("Image type is not supported.");
			System.exit(0);
		}
		
		return results;
	}

	//return an arraylist of all images of a particular stack.
	private ArrayList readImages(String directory, String ext, String[] children, int stackIndex)
	{
		//read the 1st one to get some properties
		//System.out.println("The first image in dir is "+ children[0] + ". Reading total "+ children.length + " images ..");
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
		//added on 2/27 to allow different image size
		if(widthList == null)
			widthList = new int[children.length];
		if(heightList == null)
		    heightList = new int[children.length];
		if(depthList == null)
		    depthList = new int[children.length];
		for (int i=0; i<children.length; i++)
		{
			String path = directory+children[i];
			if(!resize)
			{
			  imgp = new ImagePlus(path);
			  widthList[i] = imgp.getProcessor().getWidth();
			  heightList[i] = imgp.getProcessor().getHeight();
			  depthList[i] = imgp.getStackSize();
			  if(widthList[i] != this.width || heightList[i] != this.height || depthList[i] !=this.stackSize)
			  {
				System.err.println("Image" + path + "is not the same size as the 1st one. ");
				ofSameSize = false;
			  }
			}
			else //resize. What about depth?
			{
			  widthList[i] = this.width;
			  heightList[i] = this.height;
			}
			//add data.  Resizing will be done inside if needed.	
			data.add(openOneImage(path,  stackIndex));
		}
		
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

	/**
	 * It is the width of the first image. 
	 * 
	 * @return int
	 */
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

	/**
	 * It is the height of the first image.
	 * 
	 * @return
	 */
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

	/**
	 * This is the stack size of the first image.
	 * 
	 * @return
	 */
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
	
	//if all images are of the same size, return true; otherwise, return false.
	public boolean ofSameSize()
	{
		return ofSameSize;
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
	
	public ImagePlus getIP(int imageIndex) {
		ImagePlus imgp = new ImagePlus(directory+children[imageIndex]);
		return imgp;
	}
	

	//the following three getters are typically called after the images are read.
	
	public int[] getWidthList()
	{
		if (widthList == null)
		{
			System.out.println("Read the images to get info.");
			getData();
		}
			
		return widthList;
	}

	public int[] getHeightList()
	{
		if (heightList == null)
		{
			System.out.println("Read the images to get info.");
			getData();
		}
			
		return heightList;
	}
	
	public int[] getDepthList()
	{
        if (depthList == null)
        {
			System.out.println("Read the images to get info.");
			getData();
        }
        return depthList;
	}
	
	
}