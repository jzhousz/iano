package manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.JTextArea;

/**
* The class used to generate a video.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class AVIManager 
{
	/**
	* Generate a video.
	* 
	* @param pathIn The path containing the generating images.
	* @param pathOut The path containing the generated video.
	* @param frameStart From which frame starts.
	* @return The output of the process.
	*/
	public static String generateVideo(String pathIn, String pathOut, String frameStart)
	{
		System.out.println("(generateVideo()) pathIn:" + pathIn + ",pathOut:" + pathOut + ",frameStart:" + frameStart);

		String output = "";
		try
		{
			ProcessBuilder pb = new ProcessBuilder("plugins/Larva/ffmpeg/bin/ffmpeg.exe", "-start_number", frameStart,
					"-i", pathIn, "-pix_fmt", "nv12", "-f", "avi", "-vcodec", "rawvideo", pathOut); // or
																									// other
																									// command....

			Process process = pb.start();

			BufferedReader readerError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			StringBuilder builderError = new StringBuilder();
			String lineError = null;
			while ((lineError = readerError.readLine()) != null)
			{
				builderError.append(lineError);
				builderError.append(System.getProperty("line.separator"));
			}
			String resultError = builderError.toString();

			output += "Standard Output: " + resultError + "";

			System.out.println("resultError: " + resultError);

		} catch (IOException e)
		{
			e.printStackTrace();

			output += "Exception: IOException e" + "\n";
		}

		return output;
	}
	
	/**
	 * The main method.
	 * @param args The arguments.
	 */
	public static void main(String[] args) {
		
		String pathIn = "E:/1/larva.avi_07-17-2016_14-39-54/aAnimation/Animation_%d.jpg";
		String pathOut = "E:/output_y7.avi";
		
		String result = generateVideo(pathIn, pathOut, "1");
		
		System.out.println("result: " + result);
		
	}
}
