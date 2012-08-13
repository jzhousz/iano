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
 * 8/1/2012:  3D ROI resize: done.  2D ROI resize: to be added.
 *  
 *  ReTHINK DATA INTERFACE:   8/6/2012

Conclusion:
    How to get 3D ROI data out of DataInput?
     a)  Use getData(int)  to get a ArrayList of all data of certain slice
     b)  Use getAllStackofAnImage(int)  to get a 3D ROI, an Arraylist of all slices
    No method gets you everything in 3D image or ROI. (For saving memory.)
 
    If 3D images: same as above 
    If 2D images: The stackIndex is 1, so just use getData() to get all image data. 
    If 2D ROI:  Just use getData() to get all ROI data.

- Feature extractors need to use a mask to avoid sign extension of the data
		such as data[i][j]&0xff (for byte)
- Feature extractors can call  ofSameSize() to check of the images are of the same size
- 3D feature extractors need to check if it is ROI before deciding to call getStackSize() or getDepth() 

	//case 1: 2D image set: depth = 0; stackSize = 1
	//case 2: 3D image set: depth = 0; stackSize > 1
	//case 3: 2D ROI:       depth = 1; stackSize =1 
	//case 4: 3D ROI:       depth > 1; stackSize > 1
	//case 5: 3D ROI:       depth = 1; stackSize >=1 (depending on the image)
	//2D feature extractor for cases 1, 3, and 5
	//3D feature extractor for cases 2, and 4. (stackSize > 1 and depth != 1)
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
	protected int height = 0; //height of the first image (or of the entire set if of the same size)
	protected int width = 0;  //width of the first image
	protected int depth = 0;  //depth in the case of 3D ROI.
	int[] widthList = null;  //Moved from DataInputDynamic to allow dynamic size 02/2012
	int[] heightList = null;
	int[] depthList = null;
	protected int stackSize = 0;
	protected int imageType = 0;
	boolean ofSameSize = true; //added 02/2012 to combine DataInputDynamic
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
		this.resize = true;
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
	 * @param depth	for 3D ROI
	 * @param newwidth   only if need to resize
	 * @param newheight  only if need to resize
	 * @throws Exception
	 */
	public DataInput(ImagePlus image, HashMap<String, Roi> roiList, HashMap<String, String> classMap, String channel, int depth, int newwidth, int newheight) throws Exception
	{
		this.mode = ROIMODE;

		//check if resize
		if (newwidth!=0 && newheight !=0)
		{
			System.out.println("resizing to " + newwidth + "x"+newheight);
			this.resize = true;
			this.height = newheight;
			this.width = newwidth;
		}
		this.depth = depth;
		this.stackSize = image.getImageStackSize(); // stacksize of the entire image
		System.out.println("The ROI depth: " + depth + " The stack size of tne entire image:"+ stackSize);
		this.channel = channel;
		this.imp = image;
		this.imageType = image.getType();
		this.roiList = roiList;
		
		getChildren(); //based on roiList to get children 
		
		//Create a single annotation
		annotations = new ArrayList<String>();
		annotations.add("Class");	//Single annotation for roi mode, arbitrarily named "Class" (shows up in target column when images are loaded)
		
		//Create classname hash map and targets
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
			//Would be better if I have another hashMap from classname to target!
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

	//return an array whose type depending on image type
	//The size of the array can be either based on the original image dimension or resized dimension.
	private Object openOneImage(ImagePlus imgp, int stackIndex, int curw, int curh) throws Exception
	{
		//stack from 1 to number of slices
		ImageProcessor ip = imgp.getStack().getProcessor(stackIndex);
		Object results = null;
		int w = curw, h = curh;
		
		if (resize)
		{
			ip  = ip.resize(this.width,this.height);
			w = this.width; 
			h = this.height;
		}

		//get the pixel values. We only deal with one color for RGB picture
		if (ip instanceof ByteProcessor)
			results = ip.getPixels();
		else if (ip instanceof ColorProcessor)
		{
			//System.out.println("RGB image..");
			byte[] pixels= new byte[w*h];
			byte[] tmppixels= new byte[w*h];  //for the irrelevant channels.

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
			//16 bit or 32 bit grayscale
			results =  ip.getPixels();
		else
		{
			System.err.println("Image type is not supported.");
			System.exit(0);
		}
		
		return results;
	}
	
	// Open one individual ROI image based on image type and channel (if RGB). 
	// Return an array of byte[]or int[] or float[] based on image type.
	// 
	//  It works with both 3D and 2D ROIs. In the case of 3D, it gets one slice out (of given channel). 
	//
	//  Note that this method combines the cropping and getting data into the data structure
	//  If call ImageProcessor's crop() first, then get data, the memory will be allocated twice, which would be a waste.
	//  A possible risk is that 3D ROI's z position is decided based on its name.
	//   -- e.g. 10-5-88 means the ROI is at slice 10.
	//   -- which replies on ImageJ's ROI management mechanism
	private Object openROIImage(String childrenCandidate, int stackIndex, int[] dim) 
	{
	   	int w, h; 
	   	int d = depth;
	    Roi roi = roiList.get(childrenCandidate);
	    //work with rectangle ROI with width/height. ROI type check tba.
	    //x,y is the uppperleft corner of ROI
	    int x = roi.getBounds().x;
		int y = roi.getBounds().y;
		if (resize)
		{ 	// if resize, use the passed in w,h of ROI 
			w = this.width;
		    h = this.height;
		}
		else // else use ROI w, h 
		{
		    w = roi.getBounds().width;
		    h = roi.getBounds().height;
		}
		if (dim!= null) //pass back to the caller, e.g. readImages()
		{   dim[0] = w; 
		    dim[1] = h; 
		}
		
		int cz = 1;  //In the case of 2d, depth, cz is 1.
		if (stackSize > 1)//assume an ImageJ 3D ROI label if it is a 3D image.  
		{ //GUI add a class label for more than 1 class. "0010-0021-0052-1". Assume cz is 10.
		  //It is a problem if 3D ROI label is not formatted correctly
	      String[] coors = childrenCandidate.split("-");
	      if (coors.length >= 3)
	    	  cz = Integer.parseInt(coors[0]);
	      else 
	      {
	    	  System.err.println("3D ROI "+childrenCandidate+ ": label is in incorrect format!");
	    	  cz = roi.getZPosition(); //return 0 most of the time.
	    	  if (cz == 0) cz = 1;
	      }
		}
	    //check bounds
	    if (!checkROIBounds(x, y, cz, w, h, d, this.imp.getWidth(), this.imp.getHeight(), this.stackSize))
	    		return null;
	    
	    //check stackIndex with ROI depth
	    if (stackIndex > depth) { System.err.println("ROI slice index bigger than depth");return null;}
	    
	    //start getting the data
	    int startz = cz - depth/2;
	    System.out.println("startx:" + x + " starty:" + y + " startz:" + startz);

	    Object result;  // a 2D slice
	    ImageProcessor impr = this.imp.getStack().getProcessor(1);
	    if (impr instanceof ByteProcessor)
	    	result = new byte[w*h];
	    else if (impr instanceof ColorProcessor)
	    	result = new byte[w*h];
	    else if (impr instanceof ShortProcessor)
	    	result = new int[w*h];
	    else if (impr instanceof FloatProcessor)	
	        result = new float[w*h];
    	else
    	{
    		System.err.println("not supported image type in 3D ROI");
	    	  return null;
    	}
	    	    
	    int index = 0;
	    int[] channeldata =  new int[3];
	    int i = startz + stackIndex - 1;
	    //for(int i = startz; i < startz + depth; i++) {
	    index = 0;
	    impr = this.imp.getStack().getProcessor(i);
	    //check image type
		if(impr instanceof ByteProcessor) 
		{   //get the data around the center
		   	for(int j = y; j < y + h; j++ )
		   		for(int k = x; k < x + w; k++)
		   	     ((byte[]) result)[index++] = (byte) impr.getPixel(k,j);
		}
		else if(impr instanceof ShortProcessor) 
		{
		   	for(int j = y; j < y + h; j++ )
		   		for(int k = x; k < x + w; k++)
		   	     ((int[]) result)[index++] = impr.getPixel(k,j);
		}
		else if(impr instanceof FloatProcessor)
		{
		   	for(int j = y; j < y + h; j++ )
		   		for(int k = x; k < x + w; k++)
		   	     ((float[]) result)[index++] = Float.intBitsToFloat(impr.getPixel(k,j));
		}
	    else if (impr instanceof ColorProcessor)
	    {
	       	for(int j = y; j < y + h; j++ )
		   		for(int k = x; k < x + w; k++)
		   		{
		   		   impr.getPixel(k,j, channeldata);
		   		   if (channel == "r")
		   			   ((byte[]) result)[index++] = (byte) channeldata[0];
		  		   else if (channel == "g")
		    		   ((byte[]) result)[index++] = (byte) channeldata[1];	
		    	   else if (channel == "b")
		    		  ((byte[]) result)[index++] = (byte)channeldata[2];
		    	}
	    }
	    // entireROI.add(result);}
	    
       return result;
	}

	//get  an image 
	// If 2DROI,  return the ROI
	// If 3DROI,  return the cropped and combined ROI based on depth.  Commented out to save re-allocating memory
	private ImagePlus getImage(String childrenCandidate) 
	{
		//Do differently based on mode
		/* if(this.mode == ROIMODE) 
		{
			System.out.println("should not reach here in ROI reading");
			
			if(this.stackSize > 1) //3D ROi
			{  //should not reach this block because it is read in a different method to avoid double cropping.
			    Roi roi = roiList.get(childrenCandidate);
			    //calculate centers
			    int x = roi.getBounds().x;
				int y = roi.getBounds().y;
				int w = roi.getBounds().width;
				int h = roi.getBounds().height;
				int cx = x + w / 2;
				int cy = y + h / 2;
			    String[] coors = childrenCandidate.split("-");
			    int cz = Integer.parseInt(coors[0]);
			    if (cz < depth/2 + 1 || cz >  this.stackSize - depth/2)
				{
				     System.out.println("roi depth: "+depth+ " stackSize:" + stackSize + ". Too close to image surface, cann't cropt 3D ROI");
				     return null;
				}
			   //build 3D imagestack 
			   ImageStack current3DROI = new ImageStack(w, h);
			   int startz = cz - depth/2; 
			   for(int i = startz; i < startz + depth; i++)
			   {
			      ImageProcessor slice = this.imp.getStack().getProcessor(i);
				  slice.setRoi(roi);
				  ImageProcessor cropped = slice.crop(); //waste memory
				  current3DROI.addSlice("",cropped); 
			   }
			   return new ImagePlus(childrenCandidate,current3DROI);
			}
			else //2D ROI 
			{
			  Roi roi = roiList.get(childrenCandidate);
			  this.imp.setRoi(roi);
			  return new ImagePlus(childrenCandidate, this.imp.getProcessor().crop());//Only 1 processor so only works for 2D image for now
			}
		} //end of ROI Mode
		*/
		
		//other modes uses directory
		String path = directory+childrenCandidate;
		return (new ImagePlus(path)); 
	}
	
     /*
	//returns 1 big array of entire 3D ROI.  Not used. 
	Object openROIImageAsArray(String childrenCandidate, int[] dim) 
	{
	   	int w, h; 
	   	int d = depth;
	    Roi roi = roiList.get(childrenCandidate);
	    //only work with rectangle ROI with width/height. ROI type check tba.
	    //x,y is the uppperleft corner of ROI
	    int x = roi.getBounds().x;
		int y = roi.getBounds().y;
		if (resize)
		{ 	// if resize, use the passed in w,h of ROI 
			w = this.width;
		    h = this.height;
		}
		else // else use ROI w, h 
		{
		    w = roi.getBounds().width;
		    h = roi.getBounds().height;
		}
		if (dim!= null) //pass back to the caller, e.g. readImages()
		{   dim[0] = w; 
		    dim[1] = h; 
		}
		
		int cz = 1;  //In the case of 2d, depth, cz are 1.
		if (stackSize > 1) //assume a 3D ROI if it is a 3D image. any risk here?
		{
	      String[] coors = childrenCandidate.split("-");
	      cz = Integer.parseInt(coors[0]);
		}
	    //check bounds
	    if (!checkROIBounds(x, y, cz, w, h, d, this.imp.getWidth(), this.imp.getHeight(), this.stackSize))
	    		return null;
	    
	    //start getting the data
	    int startz = cz - depth/2;
	    System.out.println("startx:" + x + " starty:" + y + " startz:" + startz);

	    Object result;
	    ImageProcessor impr = this.imp.getStack().getProcessor(1);
	    if (impr instanceof ByteProcessor)
	    	result = new byte[w*h*d];
	    else if (impr instanceof ColorProcessor)
	    	result = new byte[w*h*d];
	    else if (impr instanceof ShortProcessor)
	    	result = new int[w*h*d];
	    else if (impr instanceof FloatProcessor)	
	        result = new float[w*h*d];
    	else
    	{
    		System.err.println("not supported image type in 3D ROI");
	    	  return null;
    	}
	    	    
	    int index = 0;
	    int[] channeldata =  new int[3];
	    for(int i = startz; i < startz + depth; i++)
		{
	    	impr = this.imp.getStack().getProcessor(i);
	        //go through slices  //check image type
		    if(impr instanceof ByteProcessor) 
		    {   //get the data around the center
		    	for(int j = y; j < y + h; j++ )
		    		for(int k = x; k < x + w; k++)
		    	     ((byte[]) result)[index++] = (byte) impr.getPixel(k,j);
		    }
		    else if(impr instanceof ShortProcessor) 
		    {
		    	for(int j = y; j < y + h; j++ )
		    		for(int k = x; k < x + w; k++)
		    	     ((int[]) result)[index++] = impr.getPixel(k,j);
		    }
		    else if(impr instanceof FloatProcessor)
		    {
		    	for(int j = y; j < y + h; j++ )
		    		for(int k = x; k < x + w; k++)
		    	     ((float[]) result)[index++] = Float.intBitsToFloat(impr.getPixel(k,j));
		    }
	    	else if (impr instanceof ColorProcessor)
	    	{
	        	for(int j = y; j < y + h; j++ )
		    		for(int k = x; k < x + w; k++)
		    		{
		    		   impr.getPixel(k,j, channeldata);
		    		   if (channel == "r")
		    			   ((byte[]) result)[index++] = (byte) channeldata[0];
		    			 else if (channel == "g")
		    				 ((byte[]) result)[index++] = (byte) channeldata[1];	
		    			else if (channel == "b")
		    				((byte[]) result)[index++] = (byte)channeldata[2];
		    		}
	    	}
	      }
	    	return result;
	}
   */
	
	//check bounds based on the ROI dimension and entire image's dimension
	// cz start from 1; 
	// x, y start from 0. (upperleft corner of ROI)
    private boolean checkROIBounds(int x, int y, int cz, int w, int h, int d, int imwidth, int imheight, int stackSize)
    {
        if (cz < d/2 + 1 || cz >  stackSize - d/2)
		  return false;
        if (x + w >= imwidth)
          return false;
        if (y + h >= imheight)
           return false;
  
        return true;
    }

	//return an arraylist of all images (or ROIs) of a certain stackIndex.
	//This is the working horse for reading images.
	//Should just be called once throughout the program.
	private ArrayList readImages(String[] childrenCandidates, int stackIndex) throws Exception
	{
		ImagePlus imgp = null; Object oneroidata = null;
		int curwidth, curheight, curdepth;
		
		//temporary lists for children and width, height, depth
		ArrayList<String> childrenList = new ArrayList<String>(childrenCandidates.length);
		ArrayList<Integer> tmpWList = new ArrayList<Integer>(childrenCandidates.length);
		ArrayList<Integer> tmpHList = new ArrayList<Integer>(childrenCandidates.length);
		ArrayList<Integer> tmpDList = new ArrayList<Integer>(childrenCandidates.length);
		data = new ArrayList<Object>(childrenCandidates.length);
		
		//go through the files
		for (int i=0; i<childrenCandidates.length; i++)
		{
			//get data
			if( this.mode == ROIMODE) 
			{   //read the ROI data, get the ROI width and height
				int[] dim = new int[2];
				oneroidata = openROIImage(childrenCandidates[i], stackIndex, dim);
				if (oneroidata == null) continue;

				curwidth = dim[0];
			    curheight = dim[1];
			    curdepth = this.depth;
			    System.out.println("curwdith " + curwidth + "curheight "+curheight + " curdepth "+ curdepth);
			}
			else //otherwise get the imageplus and then read data
			{
			   imgp = getImage(childrenCandidates[i]);
			   if (imgp == null || (imgp.getProcessor() == null && imgp.getStackSize() <=1)) 
			   {   //either not a valid image or an image type not supported by ImageJ
				System.out.println(childrenCandidates[i] + ": not supported image type.");
				continue;  
			   }
			   curwidth =  imgp.getProcessor().getWidth();
			   curheight = imgp.getProcessor().getHeight();;
			   curdepth  = imgp.getStackSize();
			}
			//update valid children
			childrenList.add(childrenCandidates[i]);
			if(childrenList.size() == 1 && mode != ROIMODE) //the first image and not ROI
			{  //these two properties are set only once 
			   //If ROI, the properties were set in the constructor and shouldn't be changed.	
			   imageType = imgp.getType();
		       stackSize = imgp.getStackSize();
			}
			if(!resize)
			{
			  tmpWList.add(new Integer(curwidth));
			  tmpHList.add(new Integer(curheight));
			  if (childrenList.size() == 1)
			  {   //set general property only once
				   width = curwidth;
				   height = curheight;
			  }
			  if(curwidth != this.width || curheight != this.height || (curdepth != this.stackSize && curdepth != this.depth))
			  {
				System.out.println("Warning: Image" + childrenCandidates[i] + "is not the same size as the 1st one. ");
				ofSameSize = false;
			  }
			}
			else //resize. So the list is the same. 
			{
			  tmpWList.add(width);
			  tmpHList.add(height);
			}
			tmpDList.add(curdepth);  //depth is never resized 
			
			if( this.mode == ROIMODE) 
				data.add(oneroidata);
			else //must be called after setting the width and height
			    data.add(openOneImage(imgp, stackIndex, curwidth, curheight));

			//update the index for current data, needed for 3D image to avoid re-reading the same stack
			lastStackIndex = stackIndex;
			children = (String[]) childrenList.toArray(new  String[childrenList.size()]);
			widthList =  convertListTointArray(tmpWList);  
			heightList = convertListTointArray(tmpHList);
			depthList =  convertListTointArray(tmpDList);
		}
		
		if (children.length == 0)
			throw new Exception("There is no valid image found in the directory.");
	
		return data;
	}

	/** return the pixel data -- 
	    The algorithm needs use a mask to avoid sign extension
	  		such as data[i][j]&0xff
	 **/
	public ArrayList getData() throws Exception
	{
		//return the first slice for normal 2D images.
		return getData(1);
	}

	/** return the pixel data -- 
	    The algorithm needs to use a mask to avoid sign extension
		such as data[i][j]&0xff (for byte)
		
		stackIndex:  between 1 and stackSize. 
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
	
	//getter should be called after images are read.
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
	 * It is the width of the first image (or ROI). 
	 * 
	 * @return int
	 */
	public int getWidth() throws Exception
	{
		if(width == 0)
		{
			System.out.println("Read the image to get width info.");
			String[] children = getChildren();
			
			//if getChildren() calls getData(), width was already set. The code below may be not reachable.
			if(width == 0 && mode!= ROIMODE)  
			{			
			  ImagePlus imgp = new ImagePlus(directory+children[0]);
			  width = imgp.getProcessor().getWidth();
			}else if(width == 0) //ROI Mode
			{ 
			    int[]  dim = new int[2];
			    openROIImage(children[0], 1, dim);
				width = dim[0]; //ROIMode
			}
		}
		return width;
	}

	/**
	 * It is the height of the first image (or ROI).
	 * 
	 * @return int
	 */
	public int getHeight() throws Exception
	{
		if(height == 0)
		{
			System.out.println("Read the image to get height info.");
			String[] children = getChildren();
			
			//if getChildren() calls getData(), height was already set.
			if(height == 0 && mode!= ROIMODE)  
			{
			 ImagePlus imgp = new ImagePlus(directory+children[0]);
			 height = imgp.getProcessor().getHeight();
			}else if (height == 0)
			{
			    int[]  dim = new int[2];
			    openROIImage(children[0], 1, dim);
				height = dim[1]; //ROIMode
			}
		}

		return height;
	}

	/**
	 * This is the stack size of the first image in the case of image set.
	 * In the case of ROI, this is the stack size of the entire image.
	 * 
	 * @return
	 */
	public int getStackSize() throws Exception
	{
		if(stackSize == 0)
		{
			if(mode == ROIMODE)
			{ //the stackSize should have been set in the constructor
			  System.err.println("Problem in returning stack size for the image containing ROIs - it was not set correctly.");
			}
			else
			{
			 System.out.println("Read the first image to get info.");
			 String[] children = getChildren();
			 ImagePlus imgp = new ImagePlus(directory+children[0]);
			 stackSize = imgp.getStackSize();
			}
		}
		return stackSize;
	}
	
	/**
	 * This returns the depth. 
	 * In the case of 3D ROI, it is 1+.
	 * If 2D ROI:  should be the fault depth from ROI input dialog: 1;
	 * If not a ROI: should be 0 (from default value of DataInput.
	 * @return   int depth
	 */
	public int getDepth() 
	{
		//0 if non ROI.  1 if 2D ROI,  1+ if 3D ROI.  
		return depth;
	}

	/* This method is called by getData() depending on training/testing 
	   When using directory structure (i.e no targetfile), it sets the instance variable
	     className
	     annotations
	   In ROImode, the two parameters of dir and ext are ignored.  
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
		    
		    //TBA: If what I get are some sub-directories instead of images, 
		    //Maybe the user chosen the wrong directory, or the programmer set the boolean wrong
		    //else if( file.List())
		    //{   if foundfile.isDir() ... }
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

	//return true if the given image is a color image
	public boolean isColor(String path)
	{
		ImagePlus imgp = new ImagePlus(path); 
		ImageProcessor ip = imgp.getProcessor();
		if(ip instanceof ColorProcessor)
		   return true;
	   else
			return false;
	}

	//return true if the given image is a 3D image
	public boolean is3D(String path)
	{
		ImagePlus imgp = new ImagePlus(path);
		
		int stackSize = imgp.getStackSize();
	    if (stackSize > 1) 
	    	return true;
	    else 
	    	return false;
	}
	
	
    //reset data to facilitate gc
	public void setDataNull()
	{
	    data = null;	
	}

	/**
	 * Return the ImagePlus object of the given image index.
	 * In the case of ROI, the parameter is ignore and the entire image is returned.
	 * 
	 * @param i  The index of the image to be returned. (Ignored in ROI mode.)
	 * @return The ith image in the case of image set. Or the entire image in the case of ROI.
	 */
	public ImagePlus getImagePlus(int i) throws Exception
	{
		if (mode == ROIMODE) //ignore the parameter and return the entire image
		  return imp;
		else
		{
		  if (children == null)
		  {   //typically it should already be set, e.g. in the constructor.
				System.err.println("Children is not yet set.");
				getChildren();
		  }
		  return (new ImagePlus(directory+children[i]));
		}
	}
	
	public int getImageType()
	{
		return imageType;
	}
	
	/**
	 * if all images are of the same size, return true; otherwise, return false.
	 * @return boolean
	 */
	public boolean ofSameSize()
	{
		return ofSameSize;
	}

	/** 
	 *  Get one image or ROI with all the stacks.
	 *  Each item in the ArrayList is a stack.
	 *  Intended for 3D images.
	 *  The stack index for the returned ArrayList starts from 0.
	 *  
	 *  @param  imageindex
	 *  @return an ArrayList of 3D data array.
	 */
	// This will re-read the image since data only contains one slices
	public ArrayList getAllStacksOfOneImage(int imageindex) throws Exception
	{
		if (children == null)
		{   //typically it should already be set, e.g. in the constructor.
			System.err.println("Children is not yet set.");
			getChildren();
		}
			
		if (mode == ROIMODE)  //if 3D ROI, return that ROI
		{
		   int stackSize = getDepth();
		   ArrayList data = new ArrayList(stackSize);
		   for(int stackIndex = 1; stackIndex <= stackSize; stackIndex++)
		     data.add(openROIImage(children[imageindex], stackIndex, null));
		   return data;
		}
		
		int stackSize = getStackSize();
	    ArrayList data = new ArrayList(stackSize);
	    ImagePlus imgp = new ImagePlus(directory+children[imageindex]);
	    //stack from 1 to number of slices
		for(int stackIndex = 1; stackIndex <= stackSize; stackIndex++)
			data.add(openOneImage(imgp, stackIndex, imgp.getWidth(), imgp.getHeight()));			

		return data;
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
	 * To set channel for an already constructed object.
	 * This will clear the existing data if the channel is changed.
	 * 
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

	public int getMode() {
		return mode;
	}
}