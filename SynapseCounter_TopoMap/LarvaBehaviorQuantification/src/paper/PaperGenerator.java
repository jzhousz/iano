package paper;

import file.ImageSaver;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.BackgroundSubtracter;
import manager.ImageManager;

public class PaperGenerator
{

	public static void main(String[] args)
	{
		String folder = "E:/4/test/";
		
//		ImagePlus imagePlusCropped = ImageManager.getImagePlusFromFile("E:/4/input/larva_386_small.jpg");
		ImagePlus imagePlusCropped = ImageManager.getImagePlusFromFile("E:/4/input/larva_386_small_binary.jpg");

		IJ.run(imagePlusCropped, "Make Binary", "");
		
		ImagePlus imagePlusBinary = ImageManager.getLargestBinaryObject(imagePlusCropped);
		
		ImageSaver.saveImagesWithPath( folder + "binary_.jpg", imagePlusBinary);
		
//		for(int i = 1; i <= 10; i++)
//		{
//			ImagePlus imagePlus = imagePlusCropped.duplicate();
//			
//			BackgroundSubtracter removeBack = new BackgroundSubtracter();
//			removeBack.rollingBallBackground(imagePlus.getProcessor(), i * 5, false, false, false, false, true); // 25
//	
//	//		// convert to 8 gray image
//	//		ImageConverter imageConverter = new ImageConverter(imagePlusBinary);
//	//		imageConverter.convertToGray8();
//	//
//	//		// Convert to Mask
//	//		IJ.run(imagePlusBinary, "Convert to Mask", "");
//	
//			IJ.run(imagePlus, "Make Binary", "");
//			IJ.run(imagePlus, "Erode", "");
//			IJ.run(imagePlus, "Erode", "");
//			IJ.run(imagePlus, "Dilate", "");
//			IJ.run(imagePlus, "Dilate", "");
//			IJ.run(imagePlus, "Fill Holes", "");
//			ImagePlus imagePlusBinary = ImageManager.getLargestBinaryObject(imagePlus);
//			
//			ImageSaver.saveImagesWithPath( folder + "binary_ball_"+i+".jpg", imagePlusBinary);
//			
//			IJ.run(imagePlusBinary, "Fill Holes", "");
////			IJ.run(imagePlusBinary, "Watershed", "");
//			
//			ImageSaver.saveImagesWithPath( folder + "binary_water_ball_"+i+".jpg", imagePlusBinary);
//		}
		
		System.out.println("Done!");
	}

}
