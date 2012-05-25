package annotool.io;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/*
 * The difference of DataInputDynamic and DataInput is that DataInputDynamic does not 
 * assume all the images are of the same size.
 * 
 * Can use a superclass later to extract the common methods.
 * 
 * Useful for loading images that need ROI annotations from the same dir
 * 
 * Deprecated in Dec 2011 because DataInput can handle images of various sizes too.
 * 
 */
@Deprecated
public class DataInputDynamic {

	protected ArrayList<byte[]> data = new ArrayList<byte[]>(); //An ArrayList of (byte[])
	String[] children = null; //list of image file names in the dir
	int[] widthList = null;  //add width, height lists 10/14/09
	int[] heightList = null;
	String directory;
	String ext;

	String channel = annotool.Annotator.channel;

	public DataInputDynamic(String directory, String ext, String channel)
	{
		this.directory = directory;
		this.ext = ext;
		this.channel = channel;
	}

	//This constructor uses the default channel setting or when the image is b/w.
	public DataInputDynamic(String directory, String ext)
	{
		this.directory = directory;
		this.ext = ext;
	}

	//This method is the same as the one in DataInput, minus the resize part and size validation
	void openOneImage(String path, byte[] pixels, byte[] tmppixels, int stackIndex)
	{
		ImagePlus imgp = new ImagePlus(path); 
		//stack from 1 to number of slices
		ImageProcessor ip = imgp.getStack().getProcessor(stackIndex);

		int width = ip.getWidth();
		int height = ip.getHeight();

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
			else if (channel == "b")
				((ColorProcessor)ip).getRGB(tmppixels,tmppixels,pixels);
			else //use all rgb data
			{
				int[] imagePixels = ( int[] ) ip.getPixels();
				int  rgb, b, g, r;
				for ( int i = 0; i < height; i++ )
					for ( int j = 0; j < width; j++ )
					{
						rgb = imagePixels[i*width + j];
						b = rgb & 0xff;
						rgb = rgb >> 8;
						g = rgb & 0xff;
						rgb = rgb >> 8;
						r = rgb & 0xff;
						pixels[i*width+j] = (byte)(0.3f * r + 0.6f * g + 0.1f * b);
					}
			}
		}
		else
		System.err.println("ImageProcessor: not a ByteProcessor or ColorProcessor. (Maybe Float or ShortProcessor.)");
		    
		    
	}

	private ArrayList<byte[]> readImages(String directory, String ext, String[] children, int stackIndex)
	{
		widthList = new int[children.length];
		heightList = new int[children.length];
		for(int i=0; i< children.length; i++)
		{
			String path = directory+children[i];
			ImagePlus imgp = new ImagePlus(path);
			int width = imgp.getProcessor().getWidth();
			int height = imgp.getProcessor().getHeight();
			widthList[i] = width;
			heightList[i] = height;
			
			byte[] oneimagedata= new byte[width*height];
			byte[] tmppixels= new byte[width*height]; //temp storage for irrelevant channels (color image)
			openOneImage(path, oneimagedata, tmppixels, stackIndex);

			this.data.add(oneimagedata);
		}

		return data;

	}

	/** return the pixel data -- use a mask to avoid sign extension
		such as data[i][j]&0xff
		The data is for CV mode, or training data in TT mode.
	 **/
	public ArrayList<byte[]> getData()
	{
		//return the first slice for normal 2D images.
		return getData(1);
	}

	/** return the pixel data -- use a mask to avoid sign extension
	such as data[i][j]&0xff
	The data is for CV mode, or training data in TT mode.
	stackIndex:  between 1 and stackSize
	 **/
	public ArrayList<byte[]> getData(int stackIndex)
	{
		String[] children = getChildren();
		return  readImages(directory, ext, children, stackIndex);
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

	//getter should be called by images are read.
	public int[] getWidthList()
	{
		if (widthList == null)
		{
			System.out.println("Read the images to get info.");
			getData();
		}
			
		return widthList;
	}

	//getter should be called by images are read.
	public int[] getHeightList()
	{
		if (heightList == null)
		{
			System.out.println("Read the images to get info.");
			getData();
		}
			
		return heightList;
	}

	
}
