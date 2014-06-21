/* SynapseValidator.java
*       a Class used to validate the results of synapse annotation 
*		There is an included main() that can be run on the command line to perform the test.
*       ARGUMENTS:  a folder containing the RDL ground truth regions subfolders with IJ (converted) marker and image files, 
*					the IJ format result file from a whole image annotation with the plugin.
*			          
*/

///////////////////////////////////////////////////////////////
// NOTES:
// -currently using file walker utilities available in jdk 7+
// - 3d to 1d index = X + Y*width + Z*width*depth
// 
// TODO:
// -precision recall
// 
//	map unique 1D value into hashmap
// for(detected synapse)
//  	go through real
//      	if(fall into neighborhood)
//          	count it as +1

//
//
// for fraction: go through entire detected list
//		for each, see if in region.
//			if yes, add to an array for processing
//			
// for Precision:
//			index detected (in region) against truth synapses. 
//				if( truth nearby)
//					numerator +1
//
//		denominator == total detected
//
//   since their are few truths, check them against the neighborhood.
//
// for recall: go through region ground truth and check for detectedRegionList
//			if(detected in neighborhood)
//				numerator +1 
//	
//		denominator == total truths
//
// 		for total count, just compare truth to list of detected within region.
//





import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class SynapseValidator {
	//****************************************
	//CONSTANTS AND MEMBERS
	//
	//****************************************
	
	//constants
	static final int TEST_NUMBER = 3;
	
	//members
	private File groundTruthDir;
	private File annoResultFile;
	
	//private HashMap gtMarkers;
	private HashMap<Integer, Point3D> arMarkers;						//all markers in full image
	private ArrayList<ArrayList<Point3D>> gtRegionMarkers;//each region's markers
	private ArrayList<RegionData> gtRegionData; 					//the location and dimension for each region
	private double[][] valResults;					//validation results for each region
	
	//orig image dims
	private int width;
	private int height;
	//private int depth;
	
	//****************************************
	//CONSTRUCTORS
	//
	//****************************************
	
	//constructor that takes File objects, one for groud truth Dir, and one for Annotation results
	public SynapseValidator( File gtDir, File arFile, int x, int y ) throws FileNotFoundException, IOException {
		if(gtDir.exists()) {
			groundTruthDir = gtDir;
			//System.out.println("gtDir exists");
			
			if(arFile.exists()) {
				annoResultFile = arFile;
				//System.out.println("arFile exists");
			}
		} else {
			throw new IOException("files not valid.");
		}
				
		//valResults = new double[TEST_NUMBER];
		//gtMarkers = new HashMap<Integer, Point3D>();
		arMarkers = new HashMap<Integer, Point3D>();
		gtRegionMarkers = new ArrayList<ArrayList<Point3D>>();
		gtRegionData = new ArrayList<RegionData>();
		
		width  = x;
		height = y;
		//depth  = z;
	
	}
	
	
	//constructor that takes file paths as strings instead
	public SynapseValidator(String groundPath, String annoResultPath, int x, int y) throws FileNotFoundException, IOException{	
		this(createFile(groundPath), createFile(annoResultPath), x, y);
	}
	
	//helper for constructor
	private static File createFile(String s) throws FileNotFoundException, IOException {
		File f = new File(s);
		return f;
	}
	
	//****************************************
	//PUBLIC METHODS
	//
	//****************************************
	
	//public method for validation. takes an (currently unused) option param and returns an array of test results
	public double[][] validate(int options) throws IOException{
		buildMarkers();
		
		validate();

		return valResults;
	
	}

	//accessor for results
	public double[][] getResults(){
		return valResults;
	}
	
	
	//****************************************
	//PRIVATE METHODS
	//
	//****************************************
	
	//private validator. does actual validation
	private void validate(){
		//get results for each region.
		// use offsets based on efficiency?
		// if possible is small, index each into the list of detected.
		// see notes at head
		/*
		* val results organization:
		*	head row = region
		*		0 = correlation coefficient
		*		1 = precision
		*		2 = recall
		*/
		
		System.out.println("\n*** Validation ***");
		
		
		valResults= new double[gtRegionMarkers.size()][TEST_NUMBER];
		int hashIndex;
		ArrayList<Point3D> arMarkersInRegion = new ArrayList<Point3D>();
		
		//process each region.
		int regionIndex = 0;
		Point3D p3d;
		
		
		
		//scan each region for markers from arMarkers that exist here.
		for(RegionData rData : gtRegionData){
			System.out.println("calculating correlation for region "+(regionIndex+1));
			arMarkersInRegion.clear();
			
			//do total count. need to determine what results synapses fall in regions.
			for(int z = rData.cornerZ; z< rData.cornerZ+rData.depth; z++){
				System.out.print("\nz = " +z+" ");
				for(int y = rData.cornerY; y<=rData.cornerY+rData.height; y++){
					for(int x = rData.cornerX; x<=rData.cornerX+rData.width; x++){
						hashIndex = calcIndex(x, y, z, width, height);
					
						p3d = arMarkers.get(hashIndex);
						if(p3d != null){
							arMarkersInRegion.add(p3d);
							System.out.print('x');
						} 
					}
				} 
			}
			
			
			//record results for detected over real
			double fraction =  (double)arMarkersInRegion.size() / gtRegionMarkers.get(regionIndex).size();
			valResults[regionIndex][0] = fraction;
			System.out.println("\ncorrelation = "+ arMarkersInRegion.size() + " / " + gtRegionMarkers.get(regionIndex).size() + " = " + fraction );
		
		
		
			// precision
			// use current arraylist of markers in region
			int preciseMarkers = 0;
			double precision = 0;
		
			for(Point3D p : arMarkersInRegion) {
				if(markerNear(p, rData, gtRegionMarkers.get(regionIndex), 7,7,5)) {
					preciseMarkers++;
					System.out.println("preciseMarker " + preciseMarkers);
				}
			
			}
			precision = (double) preciseMarkers / arMarkersInRegion.size();
			valResults[regionIndex][1] = precision;
		
		
			// recall
			// use current arrayList of markers in region
			int recallMarkers =0;
			double recall =0;
			
			for (Point3D p : gtRegionMarkers.get(regionIndex)) {
				if(markerNear(p, rData, arMarkersInRegion, 7,7,5)){
					recallMarkers++;
					System.out.println("recallMarker "+recallMarkers);
				}
			
			
			}
			recall = (double)recallMarkers / gtRegionMarkers.get(regionIndex).size();
			valResults[regionIndex][2] = recall;
		
	
			//last in region loop
			regionIndex++;
			System.out.println("...done");
		}
		
		
		//testing code
		/*int count =0;
		for( Entry<Integer, Point3D> entry : (Set<Entry>)arMarkers.entrySet()) {
			System.out.print("<" + entry.getKey() +", " + entry.getValue().convertToString() + ">");
			count++;
		}
		System.out.println();
		*/
	
	}//end validate

	
	//find if there are any points in list nearby to p. if yes, return true
	private Boolean markerNear(Point3D p, RegionData rd, ArrayList<Point3D> list, int w, int h, int d) {
		//todo
		
		return false;
		
		//if( insidebounds) return true;
		for( Point3d p3d: 
		//return false;
	
	}
	
	//read the file data and build the hashmaps
	private void buildMarkers() throws IOException{
		System.out.println("**BuildMarkers**");
		
		//read the anno results markers.
		arMarkers = hashMarkers(parseMarkerFile(annoResultFile), width, height);
		
		
		
		//read the gt directory for markers and offset info
		FileVisitor<Path> fv = new FileWalker();
	
		Files.walkFileTree(groundTruthDir.toPath(), fv);
		
		//gtRegionMarkers data are set by walker
		/*
		int i = 0;
		for(ArrayList<Point3D> ar : gtRegionMarkers) {
			System.out.println("region " + (i+1) + "++++++++++");
			for(Point3D p3d : ar) {
				System.out.println(p3d.convertToString());
			}
		
			System.out.println(gtRegionMarkers.get(i).size()+" markers");
		
			i++;
		}
		*/
		
		System.out.println("annoResultFile = " + annoResultFile.getName());

		
		
		System.out.println("**END BuildMarkers**\n");
	}//end buildMarkers

	
	//read a file and build an arraylist of markers
	private static ArrayList<Point3D> parseMarkerFile(File markerFile) throws IOException{
	
		ArrayList<Point3D> markers = new ArrayList<Point3D>();
		System.out.println("**reading line 1 **");
		
		//open file stream
		FileInputStream fstream = new FileInputStream(markerFile);
		BufferedReader in = new BufferedReader(new InputStreamReader(fstream));

		//read
		String line = in.readLine();
		//Point3D p3d = null;
		int count = 0;
		while((line = in.readLine()) != null) {

				//discard comments and blank lines 
				if(line.contains(""+"#") || line.length()<=1) {
						line = in.readLine();//ignore comments and space
				} else {
					//System.out.println("Parser: " + line);//debug
					
					//store point in markers
					markers.add(lineToPoint3D(line));
					count++;
					//p3d = lineToPoint3D(line);
					//rawID = p3d.x + p3d.y*width + p3d.z*width*depth;
					//markers.put(new Integer(rawID),lineToPoint3D(line));
				}
				
		}

		System.out.println("parsed " + count + " markers from file.");
		
		
		fstream.close();
		return markers;
	
	}//end parseMarkerFile
	

	//hash the markers for later access
	private static HashMap<Integer, Point3D> hashMarkers(ArrayList<Point3D> markers, int w, int h) {
		HashMap<Integer, Point3D> hashedMarkers = new HashMap<Integer, Point3D>();
		
		int rawID =0;
		for(Point3D p3d : markers) {
					rawID = calcIndex(p3d.x, p3d.y, p3d.z, w, h);
					hashedMarkers.put(new Integer(rawID),p3d);
		}
		
		
		//System.out.println('\n'+"Number of markers = "+markers.size()); 
		//System.out.println('\n'+"Number of hashes = "+hashedMarkers.size()); 
		
		
		return hashedMarkers;
	}

	
	//tokenize the raw file line and return a Point3D
	private static Point3D lineToPoint3D(String l) {
		int x,y,z;
			
		String[] t = l.split(" ");
		x=Integer.parseInt(t[0].trim());
		y=Integer.parseInt(t[1].trim());
		z=Integer.parseInt(t[2].trim());

		
		return new Point3D(x,y,z);
	}
	
	//convert the p3d values into a hashable unique index
	private static int calcIndex(int x, int y, int z, int w, int h) {
		return x + y*w + z*w*h;
	}
	
	//****************************************
	//MAIN()
	// 
	//****************************************
	
	//main for execution on command line.
	public static void main(String[] args) {
		if( args.length == 0 ) {
			System.out.println("USAGE: java SynapseValidator [ground truth dir] [anno Result File] [orig image width] [height]");
			return;
		}
		
		try{
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		SynapseValidator validator;
		
		try{
			validator = new SynapseValidator(args[0], args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
			
			validator.validate(1);
		
			int regionCount = 1;
			int resultCount = 0;
			for(double[] region : validator.getResults() ) {
				System.out.println("region: "+regionCount);
				regionCount++;
				for(double result : region)
					System.out.println("Results ["+resultCount+"] = " + result);
					 resultCount++;
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}//end main

	//****************************************
	//File Walker inner class
	// 	NOTE: only jdk 7+
	//
	// -upon entry into a region dir, data is read in and recorded.
	// -upon region dir exit, that data is converted into whole image index 
	// and put into array.
	//****************************************
	private class FileWalker extends SimpleFileVisitor<Path> {
		
		//constants
		static final String DATA_DIR_NAME = "region";
		static final String ROOT_DIR_NAME = "truth";
		static final String MARKER_EXT    = ".marker";
		static final String IMG_EXT       = ".tif";
		
		//members
		private int regionCounter = 0;
		private Boolean regionFlag = false;
		//private ArrayList<HashMap<Integer, Point3D>> regions = null;
		private ArrayList<Point3D> tempMarkers = null;
		//private Point3D offset = null;
		private RegionData tempRegionData = null;
		
		
		
		
		//****************************************
		//public methods
		// 
		//****************************************
		
		//define file visit behavior
		@Override public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
			System.out.println("	Processing file:" + file);
			
			String fileName = file.getFileName().toString();
			
			if(regionFlag.equals(true)) { //ignore if in a dir that is not a region
				if(fileName.contains(MARKER_EXT)) {//file is a marker 
					System.out.println("		"+fileName+" file is MARKER.");
					//process the file data if markers, and read location if picture. TODO
					tempMarkers = parseMarkerFile(file.toFile());
					
				} else if(fileName.contains(IMG_EXT)) {//file is a region image
					System.out.println("		"+fileName+" is IMAGE.");
					//just parse the useful info from the file name
					fileName = fileName.substring(0,fileName.lastIndexOf('.'));
					String[] tempRegionDataStrings = fileName.split("_");
					
					/*
					System.out.println();
					for(String s : tempRegionDataStrings) {
						System.out.print(s+", ");
					}
					System.out.println();
					*/
				
					//store region data				//x										//y										//z										//w									//h										//d
					tempRegionData = new RegionData(Integer.parseInt(tempRegionDataStrings[1]), Integer.parseInt(tempRegionDataStrings[2]), Integer.parseInt(tempRegionDataStrings[3]), 
												    Integer.parseInt(tempRegionDataStrings[4]), Integer.parseInt(tempRegionDataStrings[5]), Integer.parseInt(tempRegionDataStrings[6]));
					System.out.println("		"+tempRegionData.toString());
				
				}
			}
			
			return FileVisitResult.CONTINUE;
			
		}
		
		//define directory pre visit behavior
		@Override  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
			System.out.println("Processing directory:" + dir);
			
			//if dir is a region then process
			if(dir.getFileName().toString().toLowerCase().contains(DATA_DIR_NAME.toLowerCase())) {
				System.out.println(dir.getFileName() + " contains \"region.\"");
				regionFound();
				return FileVisitResult.CONTINUE;
			//if dir is the root then continue	
			} else if(dir.getFileName().toString().toLowerCase().contains(ROOT_DIR_NAME.toLowerCase())) {
				return FileVisitResult.CONTINUE;
			//if dir is not relevant, skip it
			} else {
				System.out.println(dir.getFileName() + " does not contain \"region\"");
				return FileVisitResult.SKIP_SUBTREE;
			}
			
			//return FileVisitResult.CONTINUE;
		}
	
		//define directory exit behavior
		@Override public FileVisitResult postVisitDirectory( Path dir, IOException e ) {
			System.out.println("dirCounter = " + regionCounter);
			
			if(regionFlag){
				//convert the markers to global coordinates
				System.out.println("converting markers to full scale image.");
				for( Point3D p3d : tempMarkers) {
					p3d.x += tempRegionData.cornerX;
					p3d.y += tempRegionData.cornerY;
					p3d.z += tempRegionData.cornerZ;
					//System.out.println("new point: " + p3d.convertToString());
					
				}
				
				//record the markers
				gtRegionMarkers.add(tempMarkers);
				
				//record regionData
				gtRegionData.add(tempRegionData);
			
			}
			
			//reset data for a new region.
			
			reset();
			
			
			return FileVisitResult.CONTINUE;
		}
	
	
		//****************************************
		//private methods
		// 
		//****************************************
		private void regionFound() {
			regionCounter++;
			regionFlag=true;
			
		}
		
		private void reset() {
			regionFlag=false;
			tempMarkers = null;
			//offset = null;
			tempRegionData = null;
			//tempRegionDataStrings = null;
		
		}
		
	
	}//end FileWalker
	
	
}//end class



//helper region class
class RegionData {

	int cornerX, cornerY, cornerZ, width, height, depth;
	
	public RegionData(int x, int y, int z, int w, int h, int d) {
		cornerX = x;
		cornerY = y;
		cornerZ = z;
		width   = w;
		height  = h;
		depth	= d;
	
	}

	public @Override String toString() {
		return "<"+cornerX+", "+cornerY+", "+cornerZ+", "+width+", "+height+", "+depth+">";
	}

}