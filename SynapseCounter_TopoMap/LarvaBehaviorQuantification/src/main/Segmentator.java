package main;

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import entities.EdgePoint;
import entities.EdgeSegment;
import entities.PixelElement;
import entities.PixelElementSegment;
import entities.PixelElementTable;
import entities.TrainingData;
import entities.YaoEdge;
import entities.YaoGraph;
import entities.YaoSegment;
import file.ImageSaver;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.ImageConverter;
import learning.PCACalculator;
import learning.Tester;
import learning.Trainer;
import manager.DrawingManager;
import manager.FileManager;
import manager.HelperManager;
import manager.ImageManager;
import manager.PropertyManager;
import manager.StringManager;
import pca.Combination;
import segmentation.CandidateCase;
import segmentation.CandidateLarva;
import segmentation.LarvaImage;
import test.CandidateCaseSimple;

public class Segmentator
{
//	public static String imageFolderInCropped = PropertyManager.getPath() + "segmentation/input_images_cropped/";
	// the folder where all the images will be output
	private static String imageFolderOut = PropertyManager.getPath() + "segmentation/output_all/";
	// the folder where only the testing images will be output
	private static String imageFolderTest = PropertyManager.getPath() + "segmentation/output_segments/";
	// the folder contains all the images that need to be segmented
	public static String imageFolderIn = PropertyManager.getPath() + "segmentation/input_images/";
	// the folder contains all the images that are prepared for segmentation, 
	// i.e., rename all images in folder imageFolderIn
	private static String imageFolderPrepared = PropertyManager.getPath() + "segmentation/input_prepared/";
	// the folder contains all the output segmented images
	public static String imageFolderSegmented = PropertyManager.getPath() + "segmentation/output_segmented/";
	// the image id used to identify it from other images
	private static int candidateLarvaId = 0;
	// the Training Data need for machine learning
	private TrainingData trainingData = null;
	
	public Segmentator(TrainingData trainingData)
	{
		this.trainingData = trainingData;
	}
	
	/**
	 * main function.
	 * @param args
	 */
	public static void main(String[] args)
	{
		TrainingData trainingData = null; 
		
		trainingData = Trainer.getTrainingData();

		Segmentator segmentator = new Segmentator(trainingData);
		
		// copy and rename the segmenting images to the preparation folder
		segmentator.prepareFiles();
		
		File folderPrepared = new File(imageFolderPrepared);
		File[] filesPrepared = folderPrepared.listFiles();
		
		int avgBodyArea = 1000; // the average body area
		
		// divide all the objects in the images into segments after apply watershed to the images.
		for(File file : filesPrepared )
		{
			int imageId = Integer.parseInt( StringManager.getSubStrBetween(file.getAbsolutePath(), "s", ".jpg") );
			System.out.println("(Segmentator.java) file.getAbsolutePath(): " + file.getAbsolutePath());
			ImagePlus imagePlus = ImageManager.getImagePlusFromFile(file.getAbsolutePath());
			
			ImagePlus imagePlusBinary = imagePlus.duplicate();

			IJ.run(imagePlusBinary, "Make Binary", "");
			
			int numLarvae = 2;
			
			PixelElementTable peTableBefore = new PixelElementTable(imagePlusBinary, true);
			int numSegmentsBefore = peTableBefore.getFrameSegments().size();
			
			// Separate all segments
			IJ.run(imagePlusBinary, "Watershed", "");
			
			PixelElementTable peTable = new PixelElementTable(imagePlusBinary, true);
			
			int numSegments = peTable.getFrameSegments().size();
			
			System.out.println("{** Test} numSegments: " + numSegments);
			
			int bodyArea = ImageManager.getPixelArea(imagePlusBinary, 128);
			
			System.out.println("{** Test} bodyArea: " + bodyArea);
			
			// delete all the temporary files
			deleteTempForMultipleLarvae();
			
			// start to segment the images
			ImagePlus imagePlusSegmented = segmentator.startSegmentation(imagePlus, imageId, numLarvae);
//			if(imagePlusSegmented != null)
//				ImageSaver.saveImagesWithPath(imageFolderSegmented + "separated_" +imageId+".jpg", imagePlusSegmented);
		}
	}
	
	public void createFolders()
	{
		FileManager.createDirectory(imageFolderOut);
		FileManager.createDirectory( imageFolderTest );
		FileManager.createDirectory( imageFolderIn );
		FileManager.createDirectory( imageFolderPrepared );
		FileManager.createDirectory( imageFolderSegmented );
	}
	
	/**
	 * Delete all the files in the temporary directories.
	 */
	public static void deleteTempForOnlyOneLarva()
	{
		// delete all files in these directories
		int numFiles = FileManager.deleteAllFiles(imageFolderOut);
		numFiles = FileManager.deleteAllFiles(imageFolderTest);
		// *****************************************************************
		//******************* Temporally comment out, for getting the images for paper
		//******************************************************************
		numFiles = FileManager.deleteAllFiles(imageFolderPrepared);
		numFiles = FileManager.deleteAllFiles(imageFolderSegmented);
		numFiles = FileManager.deleteAllFiles(StringManager.getPath(Tester.csvFileOut));
	}
	
	/**
	 * Delete all the files in the temporary directories.
	 */
	public static void deleteTempForMultipleLarvae()
	{
		// delete all files in these directories
		int numFiles = FileManager.deleteAllFiles(imageFolderOut);
		numFiles = FileManager.deleteAllFiles(imageFolderTest);
		// *****************************************************************
		//******************* Temporally comment out, for getting the images for paper
		//******************************************************************
//		numFiles = FileManager.deleteAllFiles(imageFolderPrepared);
//		numFiles = FileManager.deleteAllFiles(imageFolderSegmented);
		numFiles = FileManager.deleteAllFiles(StringManager.getPath(Tester.csvFileOut));
	}
	
	/**
	 * Copy all the files to a new directory with an organized name, e.g., s#.jpg.
	 */
	public void prepareFiles()
	{
		// the folder containing the segmenting input image
		File folder = new File(imageFolderIn);
		File[] listOfFiles = folder.listFiles();
		
		ArrayList<String> imagesInput = new ArrayList<String>();

		// rename all the images and save in folder imageFolderPrepared 
		// from folder imageFolderIn so that all images have an unique id
		for (int i = 0; i < listOfFiles.length; i++)
		{
			imagesInput.add( listOfFiles[i].getAbsolutePath() );
			
			String fileNew = imageFolderPrepared + "s" + i + ".jpg";
			FileManager.copyFile( listOfFiles[i].getAbsolutePath() , fileNew);
		}
	}
	
	/**
	 * Get all the Candidate Case and Candidate Larva.
	 * Candidate Larva: A combination of segments in the image that can possibly form a larva.
	 * Candidate Case: A case to divide the segments to n candidate larvae in the image.
	 * 
	 * @param imagePlus The image plus containing the larvae that needs to be separated.
	 * @param imageId The id used to identify the image from others.
	 * @param numLarvae The number of larvae should be extracted from the image.
	 * @return A data structure that encapsulates the candidate cases and candidate larvae.
	 */
	public LarvaImage getLarvaImage(ImagePlus imagePlus, int imageId, int numLarvae)
	{
		// get all the segmented components
		LarvaImage larvaImage = segment(imageId, imagePlus, numLarvae);
		larvaImage.imageOriginal = imagePlus;
		
		return larvaImage;
	}

	/**
	 * Start to segment the image with the statistical approach (PCA).
	 * 
	 * @param imagePlus The image plus containing the larvae that needs to be separated.
	 *        Important: It hasn't been binarized. Just load from a file as an image plus.
	 * @param imageId The id used to identify the image from others.
	 * @param numLarvae The number of larvae should be extracted from the image.
	 * 
	 * @return The segmented image plus.
	 */
	public ImagePlus startSegmentation(ImagePlus imagePlus, int imageId, int numLarvae)
	{
		// delete all files in these directories
//		deleteTemp();
				
		ImagePlus imagePlusBinary = imagePlus.duplicate();

		IJ.run(imagePlusBinary, "Make Binary", "");
		IJ.run(imagePlusBinary, "Erode", "");
		IJ.run(imagePlusBinary, "Erode", "");
		IJ.run(imagePlusBinary, "Dilate", "");
		IJ.run(imagePlusBinary, "Dilate", "");
		
		PixelElementTable peTableBefore = new PixelElementTable(imagePlusBinary, true);
		int numSegmentsBefore = peTableBefore.getFrameSegments().size();

		if(numSegmentsBefore == numLarvae )
		{
			System.out.println("(System) The image has number of segments equal to the number of larvae.");
			return null;
		}else if(numSegmentsBefore > numLarvae )
		{
			System.out.println("(System) The image has number of segments greater than the number of larvae.");
			return null;
		}
		
		// get all the candidate cases and candidate larvae in the image.
		//Candidate Larva: A combination of segments in the image that can possibly form a larva.
		//Candidate Case: A case to divide the segments to n candidate larvae in the image.
		LarvaImage larvaImage = getLarvaImage(imagePlus, imageId, numLarvae);
		
		// test larvae with the model
		Tester.test(trainingData, larvaImage);
		
		LarvaImage larvaImageSegmented = getHighestCandidateCases(larvaImage);
		
		ImagePlus imagePlusSegmented = ImageManager.newRGBImagePus("Segmented", 120, 120, 1, NewImage.FILL_WHITE);
		
		ImageConverter imageConverterSegmented = new ImageConverter(imagePlusSegmented);
		imageConverterSegmented.convertToGray8();
		
		IJ.run(imagePlusSegmented, "Convert to Mask", "");
		
		for(CandidateLarva candidateLarva : larvaImageSegmented.candidateCases.get(0).candidateLarvae)
		{
			ImagePlus imagePlusLarvaSegmentClean = candidateLarva.imagePlus; // the binary image plus
			
			DrawingManager.addImagePlus(imagePlusSegmented, imagePlusLarvaSegmentClean, new Point(0,0), 128);
		}
		
		ImageSaver.saveImagesWithPath(imageFolderSegmented + "segmented_" +larvaImageSegmented.imageId+".jpg", imagePlusSegmented);
		System.out.println("(Larva) Segmentator.segment completed!");
		
		return imagePlusSegmented;
	}
	
	/**
	 * Segment the larvae in the image.
	 * 
	 * @param imageId The image id used to identify from other images.
	 * @param imagePlusOriginal The image that needs to be segmented. 
	 * 		  Important: the image will be binarized, skeletonized, and it will be applied watershed.
	 * @param numLarvae The number of larvae should be extracted from the image.
	 * @return The LarvaImage used to record all the larva image and the segment approach information.
	 */
	public LarvaImage segment(int imageId, ImagePlus imagePlusOriginal, int numLarvae)
	{
		ImagePlus imagePlusBinary = imagePlusOriginal.duplicate();
		IJ.run(imagePlusBinary, "Make Binary", "");
		IJ.run(imagePlusBinary, "Erode", "");
		IJ.run(imagePlusBinary, "Dilate", "");
		IJ.run(imagePlusBinary, "Invert", "");
		
		ImageSaver.saveImagesWithPath(imageFolderTest + "Invert.jpg", imagePlusBinary);
		
		// the max size of an image noise
		int maxSizeNoise = 20;
		
		// convert the image plus to PixelElementTable to encapsulate all information needed.
		PixelElementTable peTable = new PixelElementTable(imagePlusBinary, true);
		// get all node pixel segments from the image.
		ArrayList<PixelElementSegment> peSegments = peTable.getFrameSegments();
		
		for(PixelElementSegment peSegment : peSegments)
		{
			if(peSegment.getPixelElements().size() <= maxSizeNoise)
			{
				for(PixelElement pe : peSegment.getPixelElements())
					imagePlusBinary.getProcessor().putPixel(pe.getPoint().x, pe.getPoint().y, 0);
			}
		}
		
		ImageSaver.saveImagesWithPath(imageFolderTest + "noNoise1.jpg", imagePlusBinary);
		
		IJ.run(imagePlusBinary, "Invert", "");
		
		ImageSaver.saveImagesWithPath(imageFolderTest + "noNoise2.jpg", imagePlusBinary);
		
		ImagePlus imagePlusSeg = imagePlusBinary.duplicate();
		IJ.run(imagePlusSeg, "Watershed", "");
		
		ImageSaver.saveImagesWithPath(imageFolderTest + "imagePlusSeg.jpg", imagePlusSeg);
		
		// convert the image plus to PixelElementTable to encapsulate all information needed.
		PixelElementTable peTableSeg = new PixelElementTable(imagePlusSeg, true);
		// get all node pixel segments from the image.
		ArrayList<PixelElementSegment> peNodeSegments = peTableSeg.getFrameSegments();
		
		// Get all connected segments (as nodes) from peTableSeg after remove segments without 11 control points. 
		// convert the image plus to PixelElementTable to encapsulate all information needed.
		peTableSeg = new PixelElementTable(imagePlusSeg, true);
		// get all node pixel segments from the image.
		peNodeSegments = peTableSeg.getFrameSegments();
		
		// create the image plus for showing image structure
		ImagePlus imagePlusStruct = imagePlusBinary.duplicate(); 
		// fill the image plus with pixel value of 0.
		ImageManager.fillImagePlus(imagePlusStruct, 0); 
		
		// map PixelElementSegments with integers so that we can use integer to get the PixelElementSegment.
		Map<Integer,PixelElementSegment> peNodeSegmentsMap = new HashMap<Integer,PixelElementSegment>();
		
		if(peNodeSegments.size() >= 255)
			System.out.println("Wrong: The number of segments is larger than 255.");
		
		ImagePlus imagePlusSkelet = imagePlusBinary.duplicate();
		IJ.run(imagePlusSkelet, "Skeletonize", "");
		imagePlusSkelet = ImageManager.to2PixelValue(imagePlusSkelet, 128); // convert to pixel values to 0 or 255
		
		ImageSaver.saveImagesWithPath(imageFolderOut + "imagePlusSkeletBF_"+imageId+".jpg", imagePlusSkelet);
		
		ImagePlus imColor = ImageManager.newRGBImagePus("color im", 120, 120, 1, NewImage.FILL_WHITE);
		Color[] colors = new Color[]{Color.red, Color.green, Color.blue, Color.cyan
				, Color.yellow, Color.gray, Color.pink, Color.black, Color.black,Color.black,Color.black};
		// red:1,green:2,blue:3,cyan:4,yellow:5,gray:6,pink:7,black:8~...
		
		// put all pixels with unique identified values to the skeleton image plus.
		for( int i = 0; i < peNodeSegments.size(); i++ )
		{
			int[] colorInt = new int[]{colors[i].getRed(),colors[i].getGreen(),colors[i].getBlue()};
			int segmentId = 255 - 1 - i; // the segment id 
			// save the segment Id as the id to so as to get the node segment easily
			peNodeSegmentsMap.put(segmentId, peNodeSegments.get(i));
			
			// prepare the input (imagePlusSkelet) for the graph.
			// mark all the pixels of the nodes in imagePlusSkelet.
			for(PixelElement pe : peNodeSegments.get(i).getPixelElements())
			{
				pe.setId(segmentId); // pixel id: 255 is the pixels for skeleton of the larva.
				imagePlusSkelet.getProcessor().putPixel(pe.getPoint().x, pe.getPoint().y, pe.getId());
				
				// fill the image plus and output for viewing the overall structure
				imagePlusStruct.getProcessor().putPixel(pe.getPoint().x, pe.getPoint().y, 255);
				imColor.getProcessor().putPixel(pe.getPoint().x, pe.getPoint().y, colorInt);
			}
			
		}
		
		ImageSaver.saveImagesWithPath(imageFolderOut + "aOverall_"+imageId+".jpg", imagePlusStruct);
		ImageSaver.saveImagesWithPath(imageFolderOut + "imagePlusSkelet_"+imageId+".jpg", imagePlusSkelet);
		ImageSaver.saveImagesWithPath(imageFolderOut + "imagePlusSkeletColor_"+imageId+".jpg", imColor);
		ImageSaver.saveImagesWithPath(imageFolderSegmented + "aOverall_"+imageId+".jpg", imagePlusStruct);
		
		// not good to pass this to ImageManager.getGraph.
		// Need to revise this.
		ArrayList<EdgePoint> edgePoints = new ArrayList<EdgePoint>();
		
		// get a graph from the image plus, important: the edgePoints will be assigned values in the method
		YaoGraph graphMain = ImageManager.getGraph(imagePlusSkelet, peNodeSegments, 128, edgePoints);
//		YaoGraph graphMain = ImageManager.getGraph(imagePlusSkelet
//				, peNodeSegments.size(), peNodeSegments.get(0).getPixelElements().get(0).getPoint(), 128, edgePoints);
		
		System.out.println("graphMain: " + graphMain);
		
//		ArrayList<YaoEdge> yaoEdges = graphMain.getYaoEdgesReflected();
		ArrayList<YaoEdge> graphEdges = graphMain.getYaoEdges();
		
		Map<YaoEdge,PixelElementSegment> peEdgeSegmentsMap = new HashMap<YaoEdge,PixelElementSegment>();
		
		// the edges image plus that contains all the edge blocks
		ImagePlus impEdgesBlock = imagePlusBinary.duplicate();
				
		// remove the segments (as nodes in a graph) if the segments don't contain 11 control points
		for( PixelElementSegment peSegment : peNodeSegments )
		{
			ImageManager.fillImagePlus(impEdgesBlock, peSegment.getPixelElements(), 0);
		}
		
		ImageSaver.saveImagesWithPath(imageFolderOut + "impEdgesBlock_"+imageId+".jpg", impEdgesBlock);
		
		// get all the node gaps segments and save them to EdgeSegment array.
		for(EdgePoint edgePoint : edgePoints)
		{
			// get all the pixels of the node gap segment.
			PixelElementSegment peEdgeSeg = ImageManager.getConnectedSegment( impEdgesBlock, edgePoint.getPoint(), 128);
			
			peEdgeSegmentsMap.put(edgePoint.getEdge(), peEdgeSeg);
		}
		
		// get all the possible segments with nodes and edges.
//		ArrayList<CandidateCaseSimple> candidateCases =  getCandidateCasesSimple(graphEdges, numLarvae);
		ArrayList<CandidateCaseSimple> candidateCases =  getCandidateCasesSimple2(graphMain, numLarvae);

		// the nodes in the graph
		Set<Integer> graphNodes = HelperManager.newSet(graphMain.getNodes());
		
		// save all the likely-larva segments with nodes and edges.
		LarvaImage larvaImage = saveSegments(candidateCases, imageId, graphNodes, numLarvae
				, imageFolderOut, imageFolderTest, imagePlusBinary, peNodeSegmentsMap, peEdgeSegmentsMap);
		
		return larvaImage;
	}
	
	/**
	* Save the possible-larva segments containing nodes and edges from a graph.
	* 
	* @param candidateCases The candidate cases (approaches) used to segmente larva.
	* @param imageId The image Id used to identify it from other images.
	* @param nodesAllIdSet The set containing all nodes.
	* @param numLarvae The number of larvae contained in the image.
	* @param imageOutPath The path where all the images saved.
	* @param folderOutTest The path where only the testing images saved.
	* @param imagePlusBinaryTemp The image plus used for template to create image plus. Every image plus works.
	* @param peNodeSegmentsMap The map containing the node and the PixelElementSegment for the node.
	* @param peEdgeSegmentsMap The pixels segments for edges of the graph.
	* @return The LarvaImage used to record all the larva image and the segment approach information.
	*/
	public LarvaImage saveSegments(ArrayList<CandidateCaseSimple> candidateCasesSimple, int imageId
			, Set<Integer> nodesAllIdSet, int numLarvae, String imageOutPath, String folderOutTest, ImagePlus imagePlusBinaryTemp
			, Map<Integer,PixelElementSegment> peNodeSegmentsMap, Map<YaoEdge,PixelElementSegment> peEdgeSegmentsMap)
	{
		LarvaImage larvaImage = new LarvaImage();
		larvaImage.imageId = imageId;
		larvaImage.larvaNum = numLarvae;
		
		ImagePlus imagePlusBinary = imagePlusBinaryTemp.duplicate();
		ImageManager.fillImagePlus(imagePlusBinary, 0);
		
		// fill all the pixels in nodes to imagePlusBinary 
		for (Map.Entry<Integer,PixelElementSegment> entry : peNodeSegmentsMap.entrySet())
		{
			ImageManager.fillImagePlus(imagePlusBinary, entry.getValue().getPixelElements(), 255);
		}
		
		// fill all the pixels in edge segment to imagePlusBinary 
		for (Map.Entry<YaoEdge,PixelElementSegment> entry : peEdgeSegmentsMap.entrySet())
		{
			ImageManager.fillImagePlus(imagePlusBinary, entry.getValue().getPixelElements(), 255);
		}
		
		ImageSaver.saveImagesWithPath(folderOutTest + "imagePlusBinary.jpg", imagePlusBinary);
		
		
		
		ImagePlus imagePlusTest = imagePlusBinary.duplicate();
		
		int edgeSegId = 0;
		for (Map.Entry<YaoEdge,PixelElementSegment> entry : peEdgeSegmentsMap.entrySet())
		{
			ImageManager.fillImagePlus(imagePlusTest, 0);
			ImageManager.fillImagePlus(imagePlusTest, entry.getValue().getPixelElements(), 255);
			ImageSaver.saveImagesWithPath(folderOutTest + "peEdgeSegments["+edgeSegId+"].jpg", imagePlusTest);
			
			edgeSegId ++;
		}

		int candidateCaseId = 0;
		
		for(CandidateCaseSimple oneCase : candidateCasesSimple)
		{
			ImagePlus imCandidateCase = imagePlusBinary.duplicate();
			
//			CandidateCase candidateCase = new CandidateCase();
//			larvaImage.addCandidateCase(candidateCase);

			ArrayList<YaoEdge> edgesExclude = oneCase.getEdgesExclude();

			System.out.println("edgesExclude["+candidateCaseId+"]: " + edgesExclude);
			
			// loop through all the candidate larvae for this candidate case
			for(int i = 0; i < edgesExclude.size(); i++)
			{
				PixelElementSegment peSegment = YaoEdge.getElementSegment(peEdgeSegmentsMap, edgesExclude.get(i));
				ImageManager.fillImagePlus(imCandidateCase, peSegment.getPixelElements(), 0);
			}
			
			CandidateCase candidateCase = new CandidateCase(candidateCaseId);
			
			PixelElementTable peTable = new PixelElementTable(imCandidateCase, true);
			
			ImageSaver.saveImagesWithPath(folderOutTest + "imCandidateCase[" + candidateCaseId + "].jpg", imCandidateCase);
			
			// to get points of all possible-larvae segments 
			ArrayList<PixelElementSegment> peSubSegments = peTable.getFrameSegments();
			
			// loop through all the candidate larvae
//			for(int i = 0; i < numLarvae; i++)
			for(int i = 0; i < peSubSegments.size(); i++)
			{
				ImagePlus imagePlusCandidateLarva = imagePlusBinary.duplicate();
				ImageManager.fillImagePlus(imagePlusCandidateLarva, 0);
				
				ImageManager.fillImagePlus(imagePlusCandidateLarva, peSubSegments.get(i).getPixelElements(), 255);

				String fileName = "candidateLarva_"+imageId+"["+candidateCaseId+ "]["+i+"].jpg";
				
				// save the possible-larva
				ImageSaver.saveImagesWithPath(folderOutTest + fileName, imagePlusCandidateLarva);
				ImageSaver.saveImagesWithPath(imageOutPath + fileName, imagePlusCandidateLarva);
				
				CandidateLarva candidateLarva = new CandidateLarva();
				candidateLarva.fileName = fileName;
				candidateLarva.candidateLarvaeId = candidateLarvaId;
				candidateLarva.imagePlus = imagePlusCandidateLarva;
				
				candidateCase.addCandidateLarva(candidateLarva);
				
				candidateLarvaId ++;
			}
			
			larvaImage.addCandidateCase(candidateCase);
			
			candidateCaseId ++;
		}
		
		return larvaImage;
	}
	
	/**
	* Get the candidate segmentation cases.
	* 
	* @param graphMain The graph containing segments as nodes and 
	*        connection between segments as edges in a image.
	* @param numLarvae Number of separated parts in the segment.
	* @return The segment containing nodes and edges from a graph.
	*/
	public ArrayList<CandidateCaseSimple> getCandidateCasesSimple2(YaoGraph graphMain, int numLarvae)
	{
		ArrayList<YaoEdge> yaoEdges = graphMain.getYaoEdges();
		Set<Integer> graphNodes = HelperManager.newSet(graphMain.getNodes());

		ArrayList<CandidateCaseSimple> candidateCases = new ArrayList<CandidateCaseSimple>();
		
		int numEdgesExcluded = 0;
		
		for(int numLar = 1; numLar <= graphNodes.size(); numLar++)
		{
			// how many edges will be selected to connect the segments composed of the possible-larva
			numEdgesExcluded = numLar - 1;
			
			// get all the combination edges
			ArrayList<ArrayList<Integer>> edgesCombExcluded = Combination.getCombination( yaoEdges.size() , numEdgesExcluded);
			
			// get all the segments containing all the nodes and the edges of the graph
			for(ArrayList<Integer> edges : edgesCombExcluded)
			{
				// The list contains all the edges that are selected
				ArrayList<YaoEdge> edgesSelect = new ArrayList<YaoEdge>();
				
				// for each edges in a case in the edge set
				for(int edge : edges)
				{
					int yaoEdgesIndex = edge - 1; // get the edge id
					// add this edge to the edgesSelect list
					edgesSelect.add(yaoEdges.get(yaoEdgesIndex));
				}
	
				CandidateCaseSimple candidateCase = new CandidateCaseSimple(edgesSelect);
				candidateCases.add(candidateCase);
			}
		
		}
		
		return candidateCases;
	}
	
	/**
	* Get the candidate segmentation cases.
	* 
	* @param yaoEdges Edges of a graph.
	* @param numLarvae Number of separated parts in the segment.
	* @return The segment containing nodes and edges from a graph.
	*/
	public ArrayList<CandidateCaseSimple> getCandidateCasesSimple9(ArrayList<YaoEdge> yaoEdges, int numLarvae)
	{
		// how many edges will be selected to connect the segments composed of the possible-larva
		int numEdgesExcluded = numLarvae - 1;
		
		ArrayList<CandidateCaseSimple> candidateCases = new ArrayList<CandidateCaseSimple>();
		
		// get all the combination edges
		ArrayList<ArrayList<Integer>> edgesCombExcluded = Combination.getCombination( yaoEdges.size() , numEdgesExcluded);
		
		// get all the segments containing all the nodes and the edges of the graph
		for(ArrayList<Integer> edges : edgesCombExcluded)
		{
			// The list contains all the edges that are selected
			ArrayList<YaoEdge> edgesSelect = new ArrayList<YaoEdge>();
			
			// for each edges in a case in the edge set
			for(int edge : edges)
			{
				int yaoEdgesIndex = edge - 1; // get the edge id
				// add this edge to the edgesSelect list
				edgesSelect.add(yaoEdges.get(yaoEdgesIndex));
			}

			CandidateCaseSimple candidateCase = new CandidateCaseSimple(edgesSelect);
			candidateCases.add(candidateCase);
		}
		
		return candidateCases;
	}
	
	
	/**
	* Get the segment containing nodes and edges from a graph.
	* 
	* @param yaoEdges Edges of a graph.
	* @param numLarvae Number of separated parts in the segment.
	* @return The segment containing nodes and edges from a graph.
	*/
	public ArrayList<YaoSegment> getSegments(ArrayList<YaoEdge> yaoEdges, int numLarvae)
	{
		// how many edges will be selected to connect the segments composed of the possible-larva
		int numEdges = yaoEdges.size() - numLarvae + 1;
		
		// get all the combination edges
		ArrayList<ArrayList<Integer>> edgesComb = Combination.getCombination( yaoEdges.size() , numEdges);
		
		ArrayList<YaoSegment> segments = new ArrayList<YaoSegment>();
		
		// get all the segments containing all the nodes and the edges of the graph
		for(ArrayList<Integer> edges : edgesComb)
		{
			// The list contains all the edges that are selected
			ArrayList<YaoEdge> edgesSelect = new ArrayList<YaoEdge>();
			// the set contains all the nodes selected
			Set<Integer> nodesSelect = new HashSet<Integer>();
			
			// for each edges in a case in the edge set
			for(int edge : edges)
			{
				int yaoEdgesIndex = edge - 1; // get the edge id
				// add this edge to the edgesSelect list
				edgesSelect.add(yaoEdges.get(yaoEdgesIndex));
			}
			
			// add the nodes connecting the edge to the node set
			for(YaoEdge edgeSelect : edgesSelect)
			{
				// add the nodes connecting the edge to the node set
				nodesSelect.add(edgeSelect.getNode1());
				nodesSelect.add(edgeSelect.getNode2());
			}
			
			// add the segment to the segment list
			YaoSegment segment = new YaoSegment(nodesSelect, edgesSelect);
			segments.add(segment);
		}
		
		return segments;
	}
	
	/**
	 * For each larva image, get the segments-combination approach with the lowest probability.
	 * 
	 * @param larvaImage The larva image that 
	 * @return The LarvaImage ArrayList containing only the approach with the lowest sum of probability.
	 */
	public LarvaImage getHighestCandidateCases(LarvaImage larvaImage)
	{
		LarvaImage larvaImageSegmented = new LarvaImage(larvaImage.imageId, larvaImage.imageOriginal,
				larvaImage.larvaNum);

		double maxProbability = 0;
		int indexCandidateCase = 0;
//		double probabilitySum = 0;

		// get the segmentation segments-combination approach with the highest probability
		// from all the segments-combination approaches.
		for (int j = 0; j < larvaImage.candidateCases.size(); j++)
		{
			CandidateCase candidateCase = larvaImage.candidateCases.get(j);

//			double probabilitySum = 0;

			// sum all the probabilities of all the segments
//			for (int k = 0; k < segmentApproach.candidateLarvae.size(); k++)
//			{
//				CandidateLarva larvaSegment = segmentApproach.candidateLarvae.get(k);
//				probabilitySum += larvaSegment.probability2;
//			}

			// always get the highest probability and save the sum of the probability and
			// the index of the segments-combination approach.
			if (candidateCase.probability2 > maxProbability)
			{
				maxProbability = candidateCase.probability2;
				indexCandidateCase = j;
			}
		}
		
//		for (int j = 0; j < larvaImage.candidateCases.size(); j++)
//		{
//			CandidateCase segmentApproach = larvaImage.candidateCases.get(j);
//
//			double probabilitySum = 0;
//
//			// sum all the probabilities of all the segments
//			for (int k = 0; k < segmentApproach.candidateLarvae.size(); k++)
//			{
//				CandidateLarva larvaSegment = segmentApproach.candidateLarvae.get(k);
//				probabilitySum += larvaSegment.probability2;
//			}
//
//			// always get the highest probability and save the sum of the probability and
//			// the index of the segments-combination approach.
//			if (probabilitySum > maxProbability)
//			{
//				maxProbability = probabilitySum;
//				indexCandidateCase = j;
//			}
//		}

		CandidateCase candidateCase = larvaImage.candidateCases.get(indexCandidateCase);
		larvaImageSegmented.addCandidateCase(candidateCase);

		return larvaImageSegmented;
	}
	
}
