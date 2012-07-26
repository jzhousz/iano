package annotool.io;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;



/**
 * The class that wraps the data.
 * 
 * @author JZhou
 *
 */

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
 * June 2012: by AA. Added files to read just a subset of images in the directory. Needed for ROIAnnotation (ROIANNOMODE). 
 * 
 * Important: readImages(childrenCandidates, stackIndex); is only called by getData().
 * 
 * July 2012: get3DROIData
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
	
	public static final int TARGETFILEMODE = 0;
	public static final int DIRECTORYMODE = 1; 
	public static final int ROIMODE = 2;     //ROI training/testing
	public static final int ROIANNOMODE = 3; //ROI annotation
	 
	//problem properties
	protected ArrayList data = null; //store all images in the dir with given ext.
	int lastStackIndex = 0; // the variable to track if the last getData() was for the same stack
	String[] children = null; //list of image file names in the dir
	protected int height = 0; //height of the first image
	protected int width = 0;  //width of the first image
	protected int depth = 0;
	int[] widthList = null;  //add width, height lists. Moved from DataInputDynamic 02/2012
	int[] heightList = null;
	int[] depthList = null;
	protected int stackSize = 0;
	protected int imageType = 0;
	boolean ofSameSize = true; //added Feb 2012 to combine DataInputDynamic
	String directory;
	String ext;
	String[] files;

	String channel = annotool.Annotator.channel;
	boolean resize = false;

	//added 03/2012 for uniform interface
	String targetFile = null; 
	int[][] targets = null;
	HashMap<String, String> classNames = null; //key is target(int), value is target label(class name)
	java.util.ArrayList<String> annotations = null;
	
	protected int mode;
	
	//For roi mode
	ImagePlus imp = null;
	HashMap<String, Roi> roiList = null;
		
	/**
	 * The constructor takes the target file and resize the (2D) images based on the dimension passed in
	 * If the images are 3D, it resizes each slice.
	 * 
	 */
	//alternative: use a flag, and resize based on the size of the first image.
	// do I need to add these for other input modes?
	public DataInput(String directory, String ext, String channel, String targetFile, int newwidth, int newheight) throws Exception
	{
		this.mode = TARGETFILEMODE;
		resize = true;
		this.height = newheight;
		this.width = newwidth;

		this.directory = directory;
		this.ext = ext;
		this.channel = channel;
		this.targetFile = targetFile;
		if (targetFile != null)
		{
		  getChildren();
		  LabelReader reader = new LabelReader(children.length, annotations);
          targets = reader.getTargets(targetFile, children);
          annotations = reader.getAnnotations();
          classNames = reader.getClassnames();
		}  
	}
	
	/**
	 This constructor takes the target file (in addition to image dir, ext, channel) to populate the label related information.
	 In the Annotation mode, the target file can be null. 
	 The channel can also be set to null if it is a B/W image set.
	 * 
	 */
	public DataInput(String directory, String ext, String channel, String targetFile) throws Exception
	{
		this.mode = TARGETFILEMODE;
		this.directory = directory;
		this.ext = ext;
		this.channel = channel;
		this.targetFile = targetFile;
		
		if (targetFile != null)
		{
		  getChildren();
		  LabelReader reader = new LabelReader(children.length, annotations);
          targets = reader.getTargets(targetFile, children);
          annotations = reader.getAnnotations();
          classNames = reader.getClassnames();
		}  
	}
	
	/**
	 This constructor takes no target file and is strictly for ROI Annotation mode
	 * 
	 */
	public DataInput(String directory, String[] files, String ext, String channel) throws Exception
	{
		this.mode = ROIANNOMODE;
		this.directory = directory;
		this.files = files;
		this.ext = ext;
		this.channel = channel;
		this.depth = depth;
	}
	
	/**
	 * Creates DataInput object in ROI input method.
	 * 
	 * @param image
	 * @param roiList HashMap with roi names for keys and Roi objects for values
	 * @param classMap HashMap with roi names for keys and corresponding class names for values
	 * @param channel r, g or b
	 * @param depth	Not implemented yet
	 * @throws Exception
	 */
	public DataInput(ImagePlus image, HashMap<String, Roi> roiList, HashMap<String, String> classMap, String channel, int depth) throws Exception
	{
		this.channel = channel;
		this.mode = ROIMODE;
		this.stackSize = image.getImageStackSize(); // stacksize of the entire image
		this.depth = depth;
		System.out.println("The ROI depth: " + depth);
		
		this.imp = image;
		this.roiList = roiList;
		
		getChildren(); //based on roiList to get children? 
		
		//Create a single annotation
		annotations = new ArrayList<String>();
		annotations.add("Class");	//Single annotation for roi mode, arbitrarily named "Class" (shows up in target column when images are loaded)
		
		//Create classname hashmap and targets
		targets = new int[1][children.length];
		classNames = new HashMap<String, String>();
		
		int newKey = 1;
		for(int i=0; i < children.length; i++) {
			String className = classMap.get(children[i]);
			int classKey = 0;
			
			for(String key : classNames.keySet())
				if(classNames.get(key).equals(className)) {
					classKey = Integer.parseInt(key);
					break;
				}
			
			if(classKey == 0) {
				//Add the new class name encountered to the classNames with unique key
				classNames.put(String.valueOf(newKey), className);
				classKey = newKey;
				newKey++;
			}
			
			targets[0][i] = classKey;
	
		}
	}
	
	/**
	 This constructor takes a directory hierarchy that has image of different classes
	 There is no need of the target file in this case.
	 The subdirectory names are assumed to be the names of the classes.  
	 The channel can be set to null if it is a B/W image set.
	 * 
	 */
	public DataInput(String directory, String ext, String channel, boolean useDirStructureForTarget) throws Exception
	{
		this.mode = DIRECTORYMODE;
		this.directory = directory;
		this.ext = ext;
		this.channel = channel;
		
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
	
	Object openOneImage(ImagePlus imgp, int stackIndex) throws Exception
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
			else if (channel == "b")
				((ColorProcessor)ip).getRGB(tmppixels,tmppixels,pixels);
			else
				throw new Exception("Not supported channel " + channel);
			
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
	//This is the working horse for reading images.
	//Should just be called once for each stack
	private ArrayList readImages(String[] childrenCandidates, int stackIndex) throws Exception
	{
		ImagePlus imgp = null; 
		int curwidth, curheight;
		
		//temporary lists for children and width, height, depth
		ArrayList<String> childrenList = new ArrayList<String>(childrenCandidates.length);
		ArrayList<Integer> tmpWList = new ArrayList<Integer>(childrenCandidates.length);
		ArrayList<Integer> tmpHList = new ArrayList<Integer>(childrenCandidates.length);
		ArrayList<Integer> tmpDList = new ArrayList<Integer>(childrenCandidates.length);
		data = new ArrayList<String>(childrenCandidates.length);
		
		//go through the files
		for (int i=0; i<childrenCandidates.length; i++)
		{			
			//get the image back (maybe a 3D ROI stack)
			imgp = getImage(childrenCandidates[i]);
						
			if (imgp == null || (imgp.getProcessor() == null && imgp.getStackSize() <=1)) 
			{   //either not a valid image or an image type not supported by ImageJ
				System.out.println(childrenCandidates[i] + ": not supported image type.");
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
			  tmpWList.add(new Integer(curwidth));
			  tmpHList.add(new Integer(curheight));
			  tmpDList.add(new Integer(imgp.getStackSize()));
			  
			  if (childrenList.size() == 1)
			  {   //set general property only once
				   width = curwidth;
				   height = curheight;
			  }
			  if(curwidth != this.width || curheight != this.height || (imgp.getStackSize() != this.stackSize && imgp.getStackSize() != this.depth))
			  {
				System.out.println("Warning: Image" + childrenCandidates[i] + "is not the same size as the 1st one. ");
				ofSameSize = false;
			  }
			}
			else //resize. depth is not resized 
			{
			  tmpWList.add(width);
			  tmpHList.add(height);
			  tmpDList.add(new Integer(imgp.getStackSize()));
			}
			
			if( this.mode == ROIMODE && this.stackSize > 1) //3D ROI
			{
				//!!If 3D ROI, stackindex should not matter because it is already in the ROI.
				// So  ....????  what is data in that case? an array of  ImagePlus..?
				data.add(imgp);
			    //return data; //to be modified 7/25/2012	
			}
			//add data.  Resizing will be done inside if needed.
			data.add(openOneImage(imgp, stackIndex));
			
			
			//update the index for current data, needed for 3D to avoid re-reading the same stack
			lastStackIndex = stackIndex;
			children = (String[]) childrenList.toArray(new  String[childrenList.size()]);
			widthList =  convertListTointArray(tmpWList);  
			heightList = convertListTointArray(tmpHList);
			depthList =  convertListTointArray(tmpDList);
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
	    String[] childrenCandidates = null;;

		if(this.mode == TARGETFILEMODE)
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
	    else if(this.mode == DIRECTORYMODE) //read subdirectory. The String has "subdirectname/filename"
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
	    else if(this.mode == ROIMODE) {
	    	//ignore the directory and ext parameters. Use roiList instead
	    	childrenCandidates = new String[roiList.size()];
	    	return roiList.keySet().toArray(childrenCandidates);
	    }
	    else if(this.mode == ROIANNOMODE)
	    {
	    	childrenCandidates = new String[files.length];
	    	childrenCandidates = files;
	    }
	    else
	    	throw new Exception("Exception: Unsuppported mode for data input.");
		
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
	    ImagePlus imgp = new ImagePlus(directory+children[imageindex]);
	    //stack from 1 to number of slices
		for(int stackIndex = 1; stackIndex <= stackSize; stackIndex++)
		{
			data.add(openOneImage(imgp, stackIndex));			
		}
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

	
	/**
	 * To set channel for already constructed object
	 * @param channel
	 */
	public void setChannel(String channel) {
		if(this.channel != channel) {
			this.channel = channel;
			this.setDataNull();
		}
	}
	public String getChannel() {
		return this.channel;
	}
	
	//a helper for conversion
	private int[] convertListTointArray(ArrayList<Integer>  tmpList)
	{
		int[] res = new int[tmpList.size()];
		int i=0;
		for(Integer bigInt : tmpList)
			res[i++] = bigInt.intValue();
		return res;
	}


	public boolean isDirectoryMode() {
		return (this.mode == DIRECTORYMODE);
	}
	
	
	//get  an image 
	// If 2DROI,  return the ROI
	// If 3DROI,  return the cropped and combined ROI based on depth.  Return null if the roi is too close to surface 
	private ImagePlus getImage(String childrenCandidate) {
		
		//Do differently based on mode
		if(this.mode == ROIMODE) 
		{
			//build 3d when a 3D depth is specified
			if(this.stackSize > 1) //Need to consider 3D ROi (although depth may be 1)
			{
				//if 3D ROI, it has the x, y, z, info!!! - z is the stack index!! 
			   Roi roi = roiList.get(childrenCandidate);
			   //int z = roi.getPosition(); //always return 0, getBounds() does not either?
			   String[] coors = childrenCandidate.split("-");
			   int z = Integer.parseInt(coors[0]);
			   int w = Integer.parseInt(coors[1]);
			   int h = Integer.parseInt(coors[2]);
			   System.out.println("z  position of the 3D ROI:" + z + " ROI w:" + w + " ROI h: " + h);
			   if (z < depth/2 + 1 || z >  this.stackSize - depth/2)
			   {
			     System.out.println(" Too close to image surface, cann't cropt 3D ROI");
			     return null;
			   }
			   else  
			   {
				   this.imp.setRoi(roi);
				   //set up a 3D ROI
				   ImageStack current3DROI = new ImageStack(w, h);
				   int start = z - depth/2; 
				   for(int i = start; i < start + depth; i++)
				   {
					   ImageProcessor slice = this.imp.getStack().getProcessor(i).crop();
					   System.out.println("slice.w" + slice.getWidth() + " slice.h" + slice.getHeight());
					   
				       current3DROI.addSlice("",slice); //java.lang.IllegalArgumentException: Dimensions do not match
				   }
				   System.out.println("3D ROI built!!!");
				   
				   return new ImagePlus("",current3DROI);
			   }
			}
			else //2D ROI 
			{
			  Roi roi = roiList.get(childrenCandidate);
			  this.imp.setRoi(roi);
			  return new ImagePlus(childrenCandidate, this.imp.getProcessor().crop());//Only 1 processor so only works for 2D image for now
			}
		}
		
		//other modes uses directory
		String path = directory+childrenCandidate;
		return (new ImagePlus(path)); 
	}

	public int getMode() {
		return mode;
	}
}