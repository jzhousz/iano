/* TesterToV3d.java
*       a program to convert between Tester.java marker format and Vaa3D marker format
*       colorized by synaspe radius bin included in result from Tester.java.
*       ARGUMENTS: a results file from Tester.java image height.
*               call from command line as: java TesterToV3d [file] [image height]
*
*               
*/


import java.io.*;
import java.util.ArrayList;
import java.lang.Math.*;

//main function to oversee the file conversion and display usage
public class TesterToV3d {


	//MAIN function to run from command line.
	// simply calls convert() on the command line args
	public static void main(String[] args) {
        
        if(args.length != 2) {
                System.out.println("USAGE: java TesterToV3d [file] [image height]");
                System.out.println("makes the following conversions:");
                System.out.println(" Tester Results to Vaa3d marker");
                return;
        }
        
        try{
                convert(args[0], Integer.parseInt(args[1]));
        }catch(Exception e) {
                e.printStackTrace();
        }
        
        
        return;
        
	}//endmain

	//do the conversion. requires image height to work.
	public static void convert(String f, int h) throws IOException, Exception{
			ArrayList<String> outputArray =  new ArrayList<String>();

			//open file stream
			FileInputStream fstream = new FileInputStream(f);
			BufferedReader in = new BufferedReader(new InputStreamReader(fstream));

			
			//read and write correct conversion
			String outputLine = "";
			int ID = 0;
			String line = in.readLine();
			while(true) {
					//System.out.println(line);//debug
					
					//discard comments and blank lines 
					if(line.contains(""+"#") || line.length()<=1) {
							outputLine = line;//preserve blanks and comments
					} else {//for Results->v3D
							ID++;
							outputLine = IJTov3d(line, h, ID);
					}
					
					System.out.println(outputLine);//debug
					
					//store outputs to write after all are finished
					outputArray.add(outputLine);
					
					
					//bottom drive for loop
					if((line = in.readLine()) == null) break;
			}

			//set up new file
			String fileName = ("Converted_v3d_" + f + ".marker");
			File file = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			
			//actually write to file
			int counter = 0;
			for( String s:outputArray) {
					out.write(s);
					out.write(System.getProperty("line.separator"));
					System.out.println(s);//echo to console
					counter++;
			}
			System.out.println(System.getProperty("line.separator")+"Written to file: " + fileName);
			System.out.println(counter + " lines written.");
			
			//close streams
			in.close();
			out.close();
			
	}//end convert
	
	
	
	
	static public String IJTov3d(String l, int h, int ID) throws Exception{
			String[] t = l.split(" ");
			
			int tempH = h - Integer.parseInt(t[1].trim());
			
			if( tempH < 0) {
					throw new Exception("Invalid height: " + tempH);
			}
			
			int r=255,b=255,g=255;
			if(t.length > 3) {
					int binNum = Integer.parseInt(t[3].trim());
					switch (binNum) {
			
						//red/blue HSV high saturation
						case 1: r=229; g=0; b=30; //red
							break;
						case 2: r=241; g=114; b=0; //orange
							break;
						case 3: r=140; g=233; b=0; //lime
							break;
						case 4: r=0; g=226; b=65; //green
							break;
						case 5: r=0; g=185; b=218; //teal
							break;
						case 6: r=0; g=59; b=211; //blue
							break;
						default: r=255; g=255; b=255; //white
							break;
					}
					
				}
				
			//IJ format: "x y z"  IJWidth+1   
			//v3d format: "x,y,z,radius,shape,name,comment,color_r,color_g,color_b"
			//                                        IJHeight inverted                    IJDepth+1                               v3d additional params
			String newLine = (""+(Integer.parseInt(t[0].trim()) + 1)+", "+(tempH)+", "+(Integer.parseInt(t[2].trim()) + 1)+", "+"0,1,detected center "+ID+",0,"+r+","+g+","+b);
			
			
			return newLine;
			
	}//end
		
}//endclass