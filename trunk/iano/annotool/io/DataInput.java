package annotool.io;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;


/* Read images from a directory using ImageJ's utilities. 
 * Add channel number  (r or g or b) as an input option for RGB images. 
 * This class can read data for CV mode or TT mode.  
 * In TT mode, directory is used as training directory.
 * 
 * Feb. 2012: Added HeightList, WidthList, ofSameSize and getters to accommodate different image size
 * Mar. 2012: Added targets, classNames, Annotations 
 *  The constructors can take targetfile, use the directory structure.
 * It now encapsulates everything about a problem.
 * 
 * Important: readImages(childrenCandidates, stackIndex); is only called by getData().
 *  
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

	//added 03/2012 for uniform interface
	boolean useDirStructure = false; //when not using a target file
	String targetFile = null; 
	int[][] targets = null;
	HashMap<String, String> classNames = null; //key is target(int), value is target label(class name)
	java.util.ArrayList<String> annotations = null;
		
	//used in annotation to get a list of images from a directory.
	public DataInput(String directory, String ext, String channel)
	{
		this.directory = directory;
		this.ext = ext;
		this.channel = channel;
	}
	
	
	//Take the targetfile to populate the label related information
	//This can avoid the need of calling either LabelReader or Annotator's  readTargets(..)
	//The code in ExpertFrame will be cleaner.
	public DataInput(String directory, String ext, String channel, String targetFile) throws Exception
	{
		this.directory = directory;
		this.ext = ext;
		this.channel = channel;
		this.targetFile = targetFile;
		
		if (targetFile != null)
		{
		  //get children
		  getChildren();
		  //set targets
		  LabelReader reader = new LabelReader(children.length,annotations);
          targets = reader.getTargets(targetFile, children);
          annotations = reader.getAnnotations();
          classNames = reader.getClassnames();
		}  
	}
	
	
	//02/27/2012  Take an image and a collection of ROIs. An alternative for ROI reading.
    //move the logic from ROITagger	
	public DataInput(ImagePlus image, Roi[] rois)
	{
	    //need to fill data;widthlist;heightlist;depthlist;
		//what about children, length, width, height, stackisze;
		/*
    	BufferedWriter writer = null;
    	try {
	    	writer = new BufferedWriter(new FileWriter(new File(file, "target.txt")));
	    	
	    	for(int i = 0; i < rois.length; i++) {
				imp.setRoi(rois[i]);
				ImagePlus roiImg = new ImagePlus("ROI", 
						imp.getProcessor().crop());
				ij.IJ.saveAs(roiImg, "jpeg", file.getPath() + "/" + (i + 1) + ".jpg");
				writer.write((i + 1) + ".jpg" + newLine);
			}
	    	
	    	writer.close();
	    	pnlStatus.setOutput("DONE!!!");
         */
		
		//TBA;
	}
	
	//02/27/2012 
	//a directory hierarchy that has images of different classes in different subdirectories
	//It will not need a target file.
	//All the things will be done at the constructor instead of wait until later.
	//Example: Hela 2D
	public DataInput(String directory, String ext, boolean useDirStructureForTarget) throws Exception
	{
		//set useDirStruture to true
		useDirStructure = true;
		this.directory = directory;
		
		//getChildren also sets the Data and ClassNames
		getChildren();
		
		//set targets based on valid children and classnames hashmap
		//only consider one annotation when not using targetfile
		targets = new int[1][children.length];
		for(int i=0; i < children.length; i++)
		{
			String[] path = children[i].split(Pattern.quote(File.separator));
			//find the corresponding key of the value
			//Would be better if I have another hashMap from classname to target!!!
			for(String key: classNames.keySet())
			{
				if (classNames.get(key).equals(path[0]))
				{
				  targets[0][i] = Integer.parseInt(key);
				  break;
				}
			}
		}
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


	Object openOneImage(ImagePlus imgp, int stackIndex)
	{
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
	//This is the working horse for readingImages.
	//Should just be called one for each stack
	//private ArrayList readImages(String directory, String ext, int stackIndex)
	private ArrayList readImages(String[] childrenCandidates, int stackIndex) throws Exception
	{
		ImagePlus imgp = null; 
		int curwidth, curheight;
		
		/*
		//read the 1st one to get some properties
		ImagePlus imgp = null;
		imgp = new ImagePlus(directory+childrenCandidates[0]); //calls the opener.openImage()
		if (imgp.getProcessor() == null && imgp.getStackSize() <=1)
		{
			System.err.println("Image type of" + (directory+childrenCandidates[0]) + " is not supported. Please select right extension.");
			throw new Exception("Image type of the 1st image " + (directory+childrenCandidates[0]) + " is not supported. Please select right extension.");
		}
		imageType = imgp.getType();
		if (!resize)
		{
			width = imgp.getProcessor().getWidth();
			height = imgp.getProcessor().getHeight();
			stackSize = imgp.getStackSize();
		}
        */
		
		//allocate capacity for the problem. May need less.
		ArrayList<String> childrenList = new ArrayList<String>();
		data = new ArrayList(childrenCandidates.length);
		
		//fill the data, //added on 2/27 to allow different image size
		if(widthList == null)
			widthList = new int[childrenCandidates.length];
		if(heightList == null)
		    heightList = new int[childrenCandidates.length];
		if(depthList == null)
		    depthList = new int[childrenCandidates.length];
		for (int i=0; i<childrenCandidates.length; i++)
		{
			String path = directory+childrenCandidates[i];
			imgp = new ImagePlus(path); // an image type not supported by ImageJ
			if (imgp.getProcessor() == null && imgp.getStackSize() <=1) 
			{
				System.out.println(path + ": not supported image type.");
				continue;  
			}
			//update valid children
			childrenList.add(childrenCandidates[i]);
			if(childrenList.size() == 1) //the first image
			{  //these two properties are set once regardless of resizing
			  	imageType = imgp.getType();
			    stackSize = imgp.getStackSize();
			}
			if(!resize)
			{
			  curwidth =  imgp.getProcessor().getWidth();
			  curheight = imgp.getProcessor().getHeight();;
			  widthList[i] = curwidth; 
			  heightList[i] = curheight;
			  depthList[i] = imgp.getStackSize();
			  if (childrenList.size() == 1)
			  {   //set general property only once
				   width = curwidth;
				   height = curheight;
			  }
			  if(widthList[i] != this.width || heightList[i] != this.height || depthList[i] !=this.stackSize)
			  {
				System.err.println("Image" + path + "is not the same size as the 1st one. ");
				ofSameSize = false;
			  }
			}
			else //resize. depth is not resized for now
			{
			  widthList[i] = this.width;
			  heightList[i] = this.height;
			  depthList[i] = imgp.getStackSize();
			}
			//add data.  Resizing will be done inside if needed.	
			data.add(openOneImage(imgp,  stackIndex));
			
			//udpate the index for current data, needed for 3D to avoid re-reading the same stack
			lastStackIndex = stackIndex;
			children = (String[]) childrenList.toArray(new  String[childrenList.size()]);
		}
		
		if (children.length == 0)
			throw new Exception("There is no valid image found in the directory.");
		
		//set general properties to be compatible with the case when all images are of the same size
		
	
		return data;
	}

	/** return the pixel data -- use a mask to avoid sign extension
	  		such as data[i][j]&0xff
	  		The data is for CV mode, or training data in TT mode.
	 **/
	public ArrayList getData() throws Exception
	{
		//return the first slice for normal 2D images.
		return getData(1);
	}

	/** return the pixel data -- use a mask to avoid sign extension
		such as data[i][j]&0xff
		The data is for CV mode, or training data in TT mode.
		stackIndex:  between 1 and stackSize
    **/
	public ArrayList getData(int stackIndex) throws Exception
	{
		//check if need to read the data based on lastStackIndex
		if (data == null ||  lastStackIndex != stackIndex)
	    {
		   String[] childrenCandidates = getChildrenCandidates(directory, ext);
		   data = readImages(childrenCandidates, stackIndex);
	    }  
	    lastStackIndex = stackIndex; //update the index of the last read stack.
	   
	   return data;
	}

	
	//getter should be called by images are read.
	public int getLength() throws Exception
	{
		if (children == null)
		{
			System.out.println("Read the images to get info.");
			getChildren();
		}
			
		return children.length;
	}

	/**
	 * It is the width of the first image. 
	 * 
	 * @return int
	 */
	public int getWidth() throws Exception
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
	 * @return int
	 */
	public int getHeight() throws Exception
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
	public int getStackSize() throws Exception
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

	/* this will be called by getData() depending on training/testing 
	 
	   In the case of using directory structure (i.e not targetfile), it will use set the instance variable
	     className
	     annotations
	*/
	private String[] getChildrenCandidates(String directory, final String ext) throws Exception
	{
	    String[] childrenCandidates;

		if(useDirStructure == false)
	    {

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
		    childrenCandidates = dir.list(filter);
		
		    if (childrenCandidates == null)
			    System.err.println("Problem reading files from the image directory.");
		    
		    //If what I get are some sub-directories instead of images, 
		    //Maybe the user chosen the wrong directory, or the programmer set the boolean wrong
		    //else if( file.List())
		    //{   if foundfile.isDir() ...`	`
		    // 	
		    //}
		    
		    
	    }
	    else //read subdirectory. The String has "subdirectname/filename"
	    {
			DirectoryReader reader = new DirectoryReader(directory, ext);
			childrenCandidates = reader.getFileListArray();
			//if pass the  wrong directory, there may be no subdirectory at all.
			if(childrenCandidates.length == 0 || childrenCandidates == null)
			{
				System.err.println("No directories in the folder. ");
				throw new Exception("No directory found in the directory tree mode.");
			}
			classNames = reader.getClassNames();
			annotations = reader.getAnnotations();
	    }
		
		return childrenCandidates;
	}

	//get the testing files or one set CV files
	public String[] getChildren() throws Exception
	{
		if (children == null)
			getData();
		
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
	    else 
	    	return false;
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
	public ArrayList getAllStacksOfOneImage(int imageindex) throws Exception
	{
		int stackSize = getStackSize();
	    ArrayList data = new ArrayList(stackSize);

	    //stack from 1 to number of slices
		for(int stackIndex = 1; stackIndex <= stackSize; stackIndex++)
			data.add(openOneImage(new ImagePlus(directory+children[imageindex]), stackIndex));
	    
		return data;
	}
	
	public ImagePlus getIP(int imageIndex) {
		ImagePlus imgp = new ImagePlus(directory+children[imageIndex]);
		return imgp;
	}
	

	//the following three getters are typically called after the images are read.
	public int[] getWidthList() throws Exception
	{
		if (widthList == null)
		{
			System.out.println("Read the images to get info.");
			getData();
		}
			
		return widthList;
	}

	public int[] getHeightList() throws Exception
	{
		if (heightList == null)
		{
			System.out.println("Read the images to get info.");
			getData();
		}
			
		return heightList;
	}
	
	public int[] getDepthList() throws Exception
	{
        if (depthList == null)
        {
			System.out.println("Read the images to get info.");
			getData();
        }
        return depthList;
	}
	
    //related with labels	
	public int[][] getTargets() {
		return targets;
	}

	public java.util.ArrayList<String> getAnnotations() {
		return annotations;
	}

	public HashMap<String, String> getClassNames() {
		return classNames;
	}

}