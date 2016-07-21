package file;

import ij.ImagePlus;
import ij.io.FileSaver;

/**
* Save images.
*
* @author  Yao
* @version 
* @since   
*/
public class ImageSaver
{
	
	/**
	 * Save images.
	 * 
	 * @param imgNum The image number.
	 * @param imageNumSet The image number set.
	 * @param images The images will be saved.
	 * @return void
	 */
	public static void saveImagesToImages_Yao(int imgNum, int imageNumSet, ImagePlus... images)
	{
		int cnt = 1;
		for( ImagePlus image : images)
		{
			FileSaver fs = new FileSaver(image);
			fs.saveAsJpeg("E:\\Summer 2016\\Larva Project\\Output\\Images_Yao\\"+ imgNum  +"-"+ imageNumSet +"-"+cnt+".jpg");
			//fs.saveAsJpeg("/home/shawn/Research/image output/"+ imgNum  +"-"+ imageNumSet +"-"+cnt+".jpg");
			//fs.saveAsJpeg("E:\\Larva\\"+ imgNum  +"-"+ imageNumSet +"-"+cnt+".jpg");
			//fs.saveAsJpeg("C:\\Users\\Shawn\\Desktop\\Research\\fly larvea information\\all\\" + imgNum  +"-"+
			//		imageNumSet +"-"+cnt+".jpg");
			cnt++;
		}
	}
	
	/**
	 * Save images and need to provide a path to which the images save.
	 * 
	 * @param path The path where to save the image. e.g. "E:\\Larva\\Images_Test\\"
	 * @param name The name the image will be saved in the directory. e.g. "testImg"
	 * @param images The images will be saved.
	 * @return void
	 */
	public static void saveImagesWithPath(String path, String name, ImagePlus... images)
	{
		for( ImagePlus image : images)
		{
			FileSaver fs = new FileSaver(image);
			fs.saveAsJpeg(path+ name+".jpg");
		}

	}
	
	public static void saveImagesWithType(String path, String name, Boolean isChrimson, ImagePlus... images)
	{
		int cnt = 1;
		String typeName = "";
		
		if(isChrimson)
			typeName = "Chri";
		else
			typeName = "Blue";
		
		for( ImagePlus image : images)
		{
			FileSaver fs = new FileSaver(image);
			fs.saveAsJpeg(path+ typeName + "_" +name +"_"+cnt+".jpg");
			cnt++;
		}
	}

}
