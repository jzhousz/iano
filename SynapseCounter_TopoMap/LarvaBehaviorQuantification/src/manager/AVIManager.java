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
	
	public static void generateAVI7(String pathIn, String pathOut, String frameStart) throws IOException
	{
//		ProcessBuilder pb = new ProcessBuilder("E:/ffmpeg/bin/ffmpeg.exe");
		
		ProcessBuilder pb = new ProcessBuilder("plugins/Larva/ffmpeg/bin/ffmpeg.exe",
	            "-start_number", frameStart, "-i", 
	            pathIn,
	            "-pix_fmt", "nv12", "-f", "avi", "-vcodec", "rawvideo",                  
	            pathOut
	    ); //or other command....
		
		pb.redirectErrorStream(true);
		
	    Process process =pb.start();
	    
	    
	    BufferedReader reader = new BufferedReader(new InputStreamReader( process.getInputStream() ));
		StringBuilder builder = new StringBuilder();
		String line = null;
		
		while ( (line = reader.readLine()) != null) {
		   builder.append(line);
		   builder.append(System.getProperty("line.separator"));
		}
		String result = builder.toString();
		
		System.out.println("result:"+result);
		
		BufferedReader readerError = new BufferedReader(new InputStreamReader( process.getErrorStream() ));
		StringBuilder builderError = new StringBuilder();
		String lineError = null;
		
		while ( (lineError = readerError.readLine()) != null) {
		 builderError.append(lineError);
		 builderError.append(System.getProperty("line.separator"));
		}
		String resultError = builderError.toString();																											
		
		System.out.println("resultError:"+resultError);
	}
	
	
//	public static void generateAVI37(String pathExe, String pathIn, String pathOut, String frameStart, JTextArea textStatus) 
//	{
//		int retCode;
//
//		try {
//    		ProcessBuilder pb = new ProcessBuilder(pathExe,
//		            "-start_number", frameStart, "-i", 
//		            pathIn,
//		            "-pix_fmt", "nv12", "-f", "avi", "-vcodec", "rawvideo",                  
//		            pathOut
//		    ); //or other command....
//
//		    final Process p = pb.start();
//
//		    // CHECK FOR THIS!
//		    try {
//				retCode = p.waitFor();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		HelperManager.showSysStatus(textStatus, "AVI file has been generated to directory " + pathOut);
//	}
	
	public static void generateAVI27(String pathIn, String pathOut, String frameStart) 
	{
		int retCode;

		try {
//		    Files.deleteIfExists(encodingFile);
//		    Files.deleteIfExists(errorFile);

    		ProcessBuilder pb = new ProcessBuilder("plugins/Larva/ffmpeg/bin/ffmpeg.exe",
		            "-start_number", frameStart, "-i", 
		            pathIn,
		            "-pix_fmt", "nv12", "-f", "avi", "-vcodec", "rawvideo",                  
		            pathOut
		    ); //or other command....

//		    pb.redirectError(errorFile.toFile());
//		    pb.redirectOutput(encodingFile.toFile());

		    final Process p = pb.start();

		    // CHECK FOR THIS!
		    try {
				retCode = p.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
		String pathIn = "E:/1/larva.avi_07-17-2016_14-39-54/aAnimation/Animation_%d.jpg";
		String pathOut = "E:/output_y7.avi";
		
		String result = generateVideo(pathIn, pathOut, "1");
		
		System.out.println("result: " + result);
		
	}
}
