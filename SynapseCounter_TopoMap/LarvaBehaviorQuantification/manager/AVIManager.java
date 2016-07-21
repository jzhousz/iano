package manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.swing.JTextArea;

public class AVIManager {

	public static String generateVideo(String pathIn, String pathOut, String frameStart)
	{

		System.out.println("(generateVideo()) pathIn:"+pathIn+",pathOut:"+pathOut+",frameStart:"+frameStart);
		
		String output = "";
		try
		{
			ProcessBuilder pb = new ProcessBuilder("plugins/Larva/ffmpeg/bin/ffmpeg.exe",
		            "-start_number", frameStart, "-i", 
		            pathIn,
		            "-pix_fmt", "nv12", "-f", "avi", "-vcodec", "rawvideo",                  
		            pathOut
		    ); //or other command....
			
			Process process =pb.start();
			
//			Process process = Runtime.getRuntime().exec(
//					"plugins/Larva/ffmpeg/bin/ffmpeg.exe -start_number "
//					+frameStart+" -i "
//					+pathIn+" -pix_fmt nv12 -f avi -vcodec rawvideo "
//					+pathOut
//			);
			
//			System.out.println("Getting ffmpeg output message:");
//			
//			BufferedReader reader = new BufferedReader(new InputStreamReader( process.getInputStream() ));
//			StringBuilder builder = new StringBuilder();
//			String line = null;
//			System.out.println("Mark 1");
//			while ( (line = reader.readLine()) != null) {
//			   builder.append(line);
//			   builder.append(System.getProperty("line.separator"));
//			}
//			String result = builder.toString();
//			output = "Standard Output: " + result + "\n\n";
//			
//			System.out.println("result:"+result);
			
//			System.out.println("Getting ffmpeg error output message:");
			BufferedReader readerError = new BufferedReader(new InputStreamReader( process.getErrorStream() ));
			//      new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder builderError = new StringBuilder();
			String lineError = null;
//			System.out.println("Mark 2");
			while ( (lineError = readerError.readLine()) != null) {
			 builderError.append(lineError);
			 builderError.append(System.getProperty("line.separator"));
			}
			String resultError = builderError.toString();																											
			
			output += "Standard Output: " + resultError + "";
			
			System.out.println("resultError: "+resultError);
			
		} catch (IOException e)
		{
		    e.printStackTrace();
		    
		    output += "Exception: IOException e"+ "\n";
		}
	
		return output;
	}
	
	public static void generateAVI3(String pathIn, String pathOut, String frameStart) throws IOException
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
//	    process.getOutputStream()
	    
	    
	    BufferedReader reader = new BufferedReader(new InputStreamReader( process.getInputStream() ));
//                new BufferedReader(new InputStreamReader(process.getInputStream()));
		StringBuilder builder = new StringBuilder();
		String line = null;
		
		while ( (line = reader.readLine()) != null) {
		   builder.append(line);
		   builder.append(System.getProperty("line.separator"));
		}
		String result = builder.toString();
		
		System.out.println("result:"+result);
		
		BufferedReader readerError = new BufferedReader(new InputStreamReader( process.getErrorStream() ));
		//      new BufferedReader(new InputStreamReader(process.getInputStream()));
		StringBuilder builderError = new StringBuilder();
		String lineError = null;
		
		while ( (lineError = readerError.readLine()) != null) {
		 builderError.append(lineError);
		 builderError.append(System.getProperty("line.separator"));
		}
		String resultError = builderError.toString();																											
		
		System.out.println("resultError:"+resultError);
	}
	
	public static void generateAVI2(String pathIn, String pathOut, String frameStart) throws IOException
	{
//		ProcessBuilder pb = new ProcessBuilder("plugins/Larva/ffmpeg/bin/ffmpeg.exe",
//	            "-start_number", frameStart, "-i", 
//	            pathIn,
//	            "-pix_fmt", "nv12", "-f", "avi", "-vcodec", "rawvideo",                  
//	            pathOut
//	    ); //or other command....
		
//		String[] command = {"CMD", "/C", "dir"};
//		String[] command = {"CMD", "/C", "ffmpeg.exe",
//	            "-start_number", frameStart, "-i", 
//	            pathIn,
//	            "-pix_fmt", "nv12", "-f", "avi", "-vcodec", "rawvideo",                  
//	            pathOut};
//		String[] command = {"E:/ffmpeg/bin/ffmpeg.exe"};
//		
//        ProcessBuilder probuilder = new ProcessBuilder( command );
//        //You can set up your work directory
////        probuilder.directory(new File("E:/3/"));
//        probuilder.directory(new File("E:/Summer 2016/Larva Project/Eclipse_Workspace/LarvaProjectYao/plugins/Larva/ffmpeg/bin/"));
        
		
		ProcessBuilder pb = new ProcessBuilder("plugins/Larva/ffmpeg/bin/ffmpeg.exe",
	            "-start_number", frameStart, "-i", 
	            pathIn,
	            "-pix_fmt", "nv12", "-f", "avi", "-vcodec", "rawvideo",                  
	            pathOut
	    ); //or other command....
		
        Process process = pb.start();
        
        //Read out dir output
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
//        System.out.printf("Output of running %s is:\n",
//                Arrays.toString(command));
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        
        //Wait to get exit value
        try {
            int exitValue = process.waitFor();
            System.out.println("\n\nExit Value is " + exitValue);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		
		
	}
	
	public static void generateAVI(String pathExe, String pathIn, String pathOut, String frameStart, JTextArea textStatus) 
	{
		int retCode;

		try {
    		ProcessBuilder pb = new ProcessBuilder(pathExe,
		            "-start_number", frameStart, "-i", 
		            pathIn,
		            "-pix_fmt", "nv12", "-f", "avi", "-vcodec", "rawvideo",                  
		            pathOut
		    ); //or other command....

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
		
		HelperManager.showSysStatus(textStatus, "AVI file has been generated to directory " + pathOut);
	}
	
	public static void generateAVI(String pathIn, String pathOut, String frameStart) 
	{
//		String pathIn = "C:\\AVI_Converted\\Animation0\\Blue_All_%d.jpg";
//		String pathOut = "E:\\output_y3.mp4";
		
//		final Path videoIn = Paths.get("C:\\AVI_Converted\\larva2.mp4");
//		final Path encodingFile = Paths.get("E:\\output.mp4");
		
//		final Path errorFile = Paths.get("E:\\error.txt");
		
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
		
//		String pathIn = "C:\\AVI_Converted\\Animation0\\Blue_All_%d.jpg";
//		String pathOut = "E:\\output_y5.mp4";
		
		String pathIn = "E:/1/larva.avi_07-17-2016_14-39-54/aAnimation/Animation_%d.jpg";
//		String pathOut = "E:/1/larva.avi_07-17-2016_13-44-27/aAnimation.avi";
		String pathOut = "E:/output_y7.avi";
		
		String result = generateVideo(pathIn, pathOut, "1");
		
		System.out.println("result: " + result);
		
//		try {
//			generateAVI4(pathIn, pathOut, "1");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		generateAVI(pathIn, pathOut, "252");
		
	}
}
