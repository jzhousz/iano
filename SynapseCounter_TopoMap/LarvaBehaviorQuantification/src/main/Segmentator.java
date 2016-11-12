package main;

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import entities.EdgePoint;
import entities.EdgeSegment;
import entities.PixelElement;
import entities.PixelElementSegment;
import entities.PixelElementTable;
import entities.YaoEdge;
import entities.YaoGraph;
import entities.YaoSegment;
import file.ImageSaver;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.ImageConverter;
import learning.Descriptor;
import learning.Tester;
import manager.DrawingManager;
import manager.FileManager;
import manager.HelperManager;
import manager.ImageManager;
import manager.StringManager;
import pca.Combination;
import segmentation.LarvaImage;
import segmentation.LarvaSegment;
import segmentation.SegmentApproach;

public class Segmentator
{
	// the folder where all the images will be output
	private static String imageFolderOut = "E:/3/segmentation/output_all/";
	// the folder where only the testing images will be output
	private static String imageFolderTest = "E:/3/segmentation/output_segments/";
	// the folder contains all the images that need to be segmented
	private static String imageFolderIn = "E:/3/segmentation/input_images/";
	// the folder contains all the images that are prepared for segmentation, 
	// i.e., rename all images in folder imageFolderIn
	private static String imageFolderPrepared = "E:/3/segmentation/input_prepared/";
	// the folder contains all the output segmented images
	private static String imageFolderSegmented = "E:/3/segmentation/output_segmented/";
	// the image id used to identify it from other images
	private static int segmentGlobeId = 0;
	// the intermediate output folder
//	public static final String intermediateDirName = "aIntermediate"; 
	// the intermediate output folder full path
//	private static String intermediateDirPath = "";
	
	/**
	 * main function.
	 * @param args
	 */
	public static void main(String[] args)
	{
		// delete all files in these directories
		int numFiles = FileManager.deleteAllFiles(imageFolderOut);
		numFiles = FileManager.deleteAllFiles(imageFolderTest);
		numFiles = FileManager.deleteAllFiles(imageFolderPrepared);
		numFiles = FileManager.deleteAllFiles(imageFolderSegmented);
		
		// 
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
		
		ArrayList<LarvaImage> larvaImages = new ArrayList<LarvaImage>() ;
		
		File folderPrepared = new File(imageFolderPrepared);
		File[] filesPrepared = folderPrepared.listFiles();
		
		// divide all the objects in the images into segments after apply watershed to the images.
		for(File file : filesPrepared )
		{
			int imageId = Integer.parseInt( StringManager.getSubStrBetween(file.getAbsolutePath(), "s", ".jpg") );
			// get all the segmented components
			LarvaImage larvaImage = segment(imageId, file.getAbsolutePath());
			larvaImages.add(larvaImage);
		}
			
		// train sample images and test images having segmentation problems.
		// segment testing images with the PCA & probability approach.
		TrainTester.trainTest(larvaImages); 
		
		ArrayList<LarvaImage> larvaImagesSegmented = getLarvaSegmentAppraoch(larvaImages);
			
		for(File file : filesPrepared )
		{
			ImagePlus imagePlusOut = ImageManager.newRGBImagePus("Output", 600, 330, 1, NewImage.FILL_WHITE);
			
			int imageId = Integer.parseInt( StringManager.getSubStrBetween(file.getAbsolutePath(), "s", ".jpg") );
			
			ImagePlus imagePlusSegmenting = ImageManager.getImagePlusFromFile(imageFolderPrepared + "s"+imageId+".jpg");
			// convert to RGB image
			ImageConverter imageConverterSegmenting = new ImageConverter(imagePlusSegmenting);
			imageConverterSegmenting.convertToRGB();
						
			ImagePlus imagePlusStructure = ImageManager.getImagePlusFromFile(imageFolderOut + "aOverall_"+imageId+".jpg");
			// convert to RGB image
			ImageConverter imageConverterStructure = new ImageConverter(imagePlusStructure);
			imageConverterStructure.convertToRGB();
						
			Point pointSegmenting = new Point(30, 30);
			Point pointStructure = new Point(30 + 150, 30);
			
			DrawingManager.drawOnImagePlus(imagePlusOut, imagePlusSegmenting, pointSegmenting, Color.blue);
			DrawingManager.drawOnImagePlus(imagePlusOut, imagePlusStructure, pointStructure, Color.blue);
			
			LarvaImage larvaImageSegmented = LarvaImage.getLarvaImage(larvaImagesSegmented, imageId);
			
			System.out.println("(Test) larvaSegments.size: " + larvaImageSegmented.segmentApproaches.get(0).larvaSegments.size());
			
			for(int i = 0; i < larvaImageSegmented.segmentApproaches.get(0).larvaSegments.size(); i++)
			{
				LarvaSegment larvaSegment = larvaImageSegmented.segmentApproaches.get(0).larvaSegments.get(i);
				
				String larvaSegmentFile = StringManager.getPath( Tester.csvFileOut ) + "o" + larvaSegment.fileName;
				
				System.out.println("(Test) larvaSegmentFile: " + larvaSegmentFile);
				
				ImagePlus imagePlusLarvaSegment = ImageManager.getImagePlusFromFile(larvaSegmentFile);
				
				ImageConverter imageConverter = new ImageConverter(imagePlusLarvaSegment);
				imageConverter.convertToRGB();
				
				Point point = new Point(30 + i * 150, 60 + 120);
				
				DrawingManager.drawOnImagePlus(imagePlusOut, imagePlusLarvaSegment, point, Color.blue);
			}
			
			ImageSaver.saveImagesWithPath(imageFolderSegmented + "segmented" +imageId+".jpg", imagePlusOut);
			
		}
		
		System.out.println("(Larva) Segmentator.segment completed!");
	}
	
	/**
	 * Segment the objects (larvae) in the image.
	 * 
	 * @param imageId The image id used to identify from other images.
	 * @param imageFileIn The image file path and name where the larvae needed to be segmented.
	 * @return The LarvaImage used to record all the larva image and the segment approach information.
	 */
	public static LarvaImage segment(int imageId, String imageFileIn)
	{
		ImagePlus imagePlusOriginal = ImageManager.getImagePlusFromFile(imageFileIn);

		ImagePlus imagePlusBinary = imagePlusOriginal.duplicate();
		IJ.run(imagePlusBinary, "Make Binary", "");
		
		ImagePlus imagePlusSkelet = imagePlusBinary.duplicate();
		IJ.run(imagePlusSkelet, "Skeletonize", "");
		imagePlusSkelet = ImageManager.to2PixelValue(imagePlusSkelet, 128); // convert to pixel values to 0 or 255
		
		ImagePlus imagePlusSeg = imagePlusBinary.duplicate();
		IJ.run(imagePlusSeg, "Watershed", "");
		
		// convert the image plus to PixelElementTable to encapsulate all information needed.
		PixelElementTable peTableSeg = new PixelElementTable(imagePlusSeg, true);
		// get all node pixel segments from the image.
		ArrayList<PixelElementSegment> peNodeSegments = peTableSeg.getFrameSegments();
		
		System.out.println( "peSegments.size(): " + peNodeSegments.size());
		
		ImagePlus imagePlusOut = imagePlusBinary.duplicate(); // create the image plus for output
		ImageManager.fillImagePlus(imagePlusOut, 0); // fill the image plus with pixel value of 0.
		
		// the edges image plus that contains all the edge blocks
		ImagePlus impEdgesBlock = imagePlusBinary.duplicate();
		
		int index = 0;
		for( PixelElementSegment peSegment : peNodeSegments )
		{
			ImageManager.fillImagePlus(impEdgesBlock, peSegment.getPixelElements(), 0);
			
			ImageManager.fillImagePlus(imagePlusOut, 0); // fill the image plus with pixel value of 0.
			// fill the image plus with the pixels from peSegment and with pixel values of 255.
			ImageManager.fillImagePlus(imagePlusOut, peSegment.getPixelElements(), 255);
			
			// get the image plus with the main skeleton on it from imagePlusOut.
			ImagePlus impSkeleton = ImageManager.getStemSkeleton(imagePlusOut, 128);
			// get the length of skeleton from impSkeleton.
			double lenSkeleton = ImageManager.getSkeletonLength(impSkeleton, 128);
			
			System.out.println( "- lenSkeleton: " + lenSkeleton);
			ImageSaver.saveImagesWithPath(imageFolderOut + "aTest_"+index+".jpg", imagePlusOut);
			
			// if the object have a length of skeleton that is less than the number of control points in
			// specified in Descriptor.
			if(lenSkeleton < Descriptor.getNumPoints())
			{
				// fill the image plus with the pixels from peSegment and with pixel values of 255.
				// remove this segment from the segmented image plus.
				ImageManager.fillImagePlus(imagePlusSeg, peSegment.getPixelElements(), 0);
			}
			
			index++;
		}
		
		// redo to get all connected segments from peTableSeg. 
		// convert the image plus to PixelElementTable to encapsulate all information needed.
		peTableSeg = new PixelElementTable(imagePlusSeg, true);
		// get all node pixel segments from the image.
		peNodeSegments = peTableSeg.getFrameSegments();
		
		ImagePlus imagePlusStruct = imagePlusBinary.duplicate(); // create the image plus for showing image structure
		ImageManager.fillImagePlus(imagePlusStruct, 0); // fill the image plus with pixel value of 0.
		
		// map PixelElementSegments with integers so that we can use integer to get the PixelElementSegment.
		Map<Integer,PixelElementSegment> peNodeSegmentsMap = new HashMap<Integer,PixelElementSegment>();
		
		
		
		// put all pixels with unique identified values to the skeleton image plus.
		// 
		for( int i = 0; i < peNodeSegments.size(); i++ )
		{
			int segmentId = 255 - 1 - i; // the segment id 
			// save the segment Id as the id to get the node segment
			peNodeSegmentsMap.put(segmentId, peNodeSegments.get(i));
			
			// prepare the input (imagePlusSkelet) for the graph.
			// mark all the pixels of the nodes in imagePlusSkelet.
			for(PixelElement pe : peNodeSegments.get(i).getPixelElements())
			{
				pe.setId(segmentId); // pixel id: 255 is the pixels for skeleton of the larva.
				imagePlusSkelet.getProcessor().putPixel(pe.getPoint().x, pe.getPoint().y, pe.getId());
				
				// remove all the node segments in the image plus, just keep the edge (gap between nodes) segments.
				// used for showing information to users. No need to have imagePlusOut.
//				imagePlusOut.getProcessor().putPixel(pe.getPoint().x, pe.getPoint().y, 0);
				
//				impEdgesBlock.getProcessor().putPixel(pe.getPoint().x, pe.getPoint().y, 0);
				
				// fill the image plus and output for viewing the overall structure
				imagePlusStruct.getProcessor().putPixel(pe.getPoint().x, pe.getPoint().y, 255);
			}
		}
		
		ImageSaver.saveImagesWithPath(imageFolderOut + "aOverall_"+imageId+".jpg", imagePlusStruct);
		
		ImageSaver.saveImagesWithPath(imageFolderOut + "imRv_"+imageId+".jpg", impEdgesBlock);
		
		// not good to pass this to ImageManager.getGraph.
		// Need to revise this.
		ArrayList<EdgePoint> edgePoints = new ArrayList<EdgePoint>();
		
		// get a graph from the image plus
		YaoGraph graphMain = ImageManager.getGraph(imagePlusSkelet
				, peNodeSegments.size(), peNodeSegments.get(0).getPixelElements().get(0).getPoint(), 128, edgePoints);
		
//		ArrayList<YaoEdge> yaoEdges = graphMain.getYaoEdgesReflected();
		ArrayList<YaoEdge> yaoEdges = graphMain.getYaoEdges();
		
		// the gap segment between every two nodes
		ArrayList<PixelElementSegment> peEdgeSegments = new ArrayList<PixelElementSegment>();
		// the node gap array
		ArrayList<EdgeSegment> edgeSegments = new ArrayList<EdgeSegment>();
		
		// get all the node gaps segments and save them to EdgeSegment array.
		for(EdgePoint edgePoint : edgePoints)
		{
			// get all the pixels of the node gap segment.
			PixelElementSegment peEdgeSeg = ImageManager.getConnectedSegment( impEdgesBlock, edgePoint.getPoint(), 128);
			
			peEdgeSegments.add(peEdgeSeg);
			
			// add the node gap segment to the EdgeSegment array.
			EdgeSegment edgeSegment = new EdgeSegment(edgePoint.getEdge(), peEdgeSeg);
			edgeSegments.add(edgeSegment);
		}
		
		ImageManager.fillImagePlus(impEdgesBlock, 0);
		
		for( int i = 0; i < edgeSegments.size(); i++ )
		{
			for(PixelElement pe : edgeSegments.get(i).getSegment().getPixelElements())
			{
				impEdgesBlock.getProcessor().putPixel(pe.getPoint().x, pe.getPoint().y, 255);
			}
		}

		ImageSaver.saveImagesWithPath(imageFolderOut + "im_"+imageId+".jpg", impEdgesBlock);
		
		int numLarvae = 2;
		
		// get all the possible segments with nodes and edges.
		ArrayList<YaoSegment> nodesEdgesSegments = getSegments(yaoEdges, numLarvae);
		Set<Integer> setAllNodes = HelperManager.newSet(graphMain.getNodes());
		
		ImageSaver.saveImagesWithPath(imageFolderOut + "bny_"+imageId+".jpg", imagePlusBinary);
		
		// save all the likely-larva segments with nodes and edges.
		LarvaImage larvaImage = saveSegments(nodesEdgesSegments, imageId, setAllNodes, numLarvae
				, imageFolderOut, imageFolderTest, imagePlusBinary, peNodeSegmentsMap, edgeSegments);
		
		return larvaImage;
	}
	
	/**
	* Save the possible-larva segments containing nodes and edges from a graph.
	* 
	* @param nodesEdgesSegments The segments being saved.
	* @param imageId The image Id used to identify it from other images.
	* @param setAllNodes The set containing all nodes.
	* @param numLarvae The number of larvae contained in the image.
	* @param imageOutPath The path where all the images saved.
	* @param folderOutTest The path where only the testing images saved.
	* @param imagePlusBinary The image plus used for template to create image plus. Every image plus works.
	* @param peNodeSegmentsMap The map containing the node and the PixelElementSegment for the node.
	* @param edgeSegments The pixels segments for edges of the graph.
	* @return The LarvaImage used to record all the larva image and the segment approach information.
	*/
	public static LarvaImage saveSegments(ArrayList<YaoSegment> nodesEdgesSegments, int imageId
			, Set<Integer> setAllNodes, int numLarvae, String imageOutPath, String folderOutTest, ImagePlus imagePlusBinary
			, Map<Integer,PixelElementSegment> peNodeSegmentsMap, ArrayList<EdgeSegment> edgeSegments)
	{
		LarvaImage larvaImage = new LarvaImage();
		larvaImage.imageId = imageId;
		larvaImage.larvaNum = numLarvae;
		
		int approachId = 0;
		
		// loop through all possible-larva segmentation approaches
		for(YaoSegment segment : nodesEdgesSegments)
		{
			SegmentApproach segmentApproach = new SegmentApproach();
			larvaImage.addLarvaSegmentApproach(segmentApproach);
			
			// all nodes in the graph
			Set<Integer> allNodes = new HashSet<Integer>(setAllNodes);
			// the nodes contained in this segmentation approach
			Set<Integer> approachNodes = segment.getNodes();
			
			// possible-larva image plus array
			ImagePlus[] possibleLarva = new ImagePlus[numLarvae];
			
			// clear all the images plus with pixel values of 0
			for(int i = 0; i < numLarvae; i++)
			{
				possibleLarva[i] = imagePlusBinary.duplicate();
				ImageManager.fillImagePlus(possibleLarva[i], 0);
			}
			
			// the segmentation plan, i.e., which segments are treated as a larva.
			ImagePlus segmentationPlan = imagePlusBinary.duplicate();
			// clear the image plus for fill with information later.
			ImageManager.fillImagePlus(segmentationPlan, 0);
			
			// fill all the nodes in the image plus with all segments
			for(int node : segment.getNodes())
			{
				PixelElementSegment peNodeSegment = peNodeSegmentsMap.get(node);
				// use 255 as the image plus so as can see the difference from pixel values of 0
				ImageManager.fillImagePlus(segmentationPlan, peNodeSegment.getPixelElements(), 255);
			}
			// remove the nodes filled in setAllNodesUsed
			allNodes.removeAll(approachNodes);
			
			// fill all the nodes that haven't been filled
			for(int node : allNodes)
			{
				PixelElementSegment peNodeSegment = peNodeSegmentsMap.get(node);
				
				ImageManager.fillImagePlus(segmentationPlan, peNodeSegment.getPixelElements(), 255);
			}
			
//			System.out.println("for sg_"+imageId+"["+approachId+ "].jpg: ");
			
			// fill all the edges in the segmentationPlan image plus
			for(YaoEdge edge : segment.getEdges())
			{
//				System.out.println("{** Yao} edge connecting: " + edge);
				// get the PixelElement Segment so as to get all the points from the edge, then fill them
				// in the image plus
				PixelElementSegment peEdgeSegment = EdgeSegment.getSegment(edgeSegments, edge);
				// fill the edge in the image plus
				ImageManager.fillImagePlus(segmentationPlan, peEdgeSegment.getPixelElements(), 255);
			}
			
//			System.out.println("{** Yao} next step! " );
			
			PixelElementTable peTable = new PixelElementTable(segmentationPlan, true);
			// to get points of all possible-larvae segments 
			ArrayList<PixelElementSegment> peSubSegments = peTable.getFrameSegments();
			
			// loop through all the possible-larvae
			for(int i = 0; i < peSubSegments.size(); i++)
			{
				// fill the possible-larva with the segment points
				ImageManager.fillImagePlus(possibleLarva[i], peSubSegments.get(i).getPixelElements(), 255);
				
				String fileName = "sg_"+imageId+"["+approachId+ "]["+i+"].jpg";
				
				// save the possible-larva
				ImageSaver.saveImagesWithPath(folderOutTest + fileName, possibleLarva[i]);
				ImageSaver.saveImagesWithPath(imageOutPath + fileName, possibleLarva[i]);
				
				LarvaSegment larvaSegment = new LarvaSegment();
				larvaSegment.fileName = fileName;
				larvaSegment.segmentId = segmentGlobeId;
				
				segmentApproach.addLarvaSegment(larvaSegment);
				
				segmentGlobeId ++;
			}
			
			// save the segmentationPlan
			ImageSaver.saveImagesWithPath(imageOutPath + "sg_"+imageId+"["+approachId+ "].jpg", segmentationPlan);
			
			approachId++;
		}
		
		return larvaImage;
	}
	
	/**
	* Get the segment containing nodes and edges from a graph.
	* 
	* @param yaoEdges Edges of a graph.
	* @param numLarvae Number of separated parts in the segment.
	* @return The segment containing nodes and edges from a graph.
	*/
	public static ArrayList<YaoSegment> getSegments(ArrayList<YaoEdge> yaoEdges, int numLarvae)
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
	 * Print all the information about the LarvaImage list.
	 * 
	 * @param larvaImages The LarvaImage list
	 */
	public static void printLarvaImages(ArrayList<LarvaImage> larvaImages)
	{
		System.out.println("{Larva} Segmeted all the input larvae.");
		
		for(int i = 0; i < larvaImages.size(); i++)
		{
			LarvaImage larvaImage = larvaImages.get(i);
			
			System.out.println("------------------------");
			System.out.println(i + "} larvaImage[" + i + "]: ");
			
			for(int j = 0; j < larvaImage.segmentApproaches.size(); j++)
			{
				SegmentApproach segmentApproach = larvaImage.segmentApproaches.get(j);
				
				System.out.println("  "+j+"] segmentApproach[" + j + "]: ");
				
				for(int k = 0; k < segmentApproach.larvaSegments.size(); k++)
				{
					LarvaSegment larvaSegment = segmentApproach.larvaSegments.get(k);
					
					System.out.println("    "+k + ")larvaSegment[" + k + "]: ");
					
					System.out.println("    - segmentId: " + larvaSegment.segmentId 
							+ ", prob1: " +  larvaSegment.probability1
							+ ", prob2: " +  larvaSegment.probability2
							+ ",file: " + larvaSegment.fileName );
				}
			}
		}
	}
	
	/**
	 * For each larva image, get the segments-combination approach with the lowest probability.
	 * 
	 * @param larvaImages The larva images that contains all needed information about an testing image
	 *        needed to segment the larvae in the image.
	 * @return The LarvaImage ArrayList containing only the approach with the lowest sum of probability.
	 */
	public static ArrayList<LarvaImage> getLarvaSegmentAppraoch(ArrayList<LarvaImage> larvaImages)
	{
		ArrayList<LarvaImage> larvaImagesSegmented = new ArrayList<LarvaImage>();
		
		// for each larva image, get the segments-combination approach with the lowest probability
		for(int i = 0; i < larvaImages.size(); i++)
		{
			LarvaImage larvaImage = larvaImages.get(i);
			
			LarvaImage LarvaImageSegmented = new LarvaImage();
			LarvaImageSegmented.imageId = larvaImage.imageId;
			LarvaImageSegmented.larvaNum = larvaImage.larvaNum;
			larvaImagesSegmented.add( LarvaImageSegmented);
			
			double minProbability = 999999; // give any large number here.
			int indexSegmentApproache = 0;
			
			// get the segmentation segments-combination approach with the lowest probability
			// from all the segments-combination approaches.
			for(int j = 0; j < larvaImage.segmentApproaches.size(); j++)
			{
				SegmentApproach segmentApproach = larvaImage.segmentApproaches.get(j);
				
				double probabilitySum = 0; // give any large number here.
				
				// sum up all the probabilities of all the segments
				for(int k = 0; k < segmentApproach.larvaSegments.size(); k++)
				{
					LarvaSegment larvaSegment = segmentApproach.larvaSegments.get(k);
					
					probabilitySum += larvaSegment.probability1;
				}
				
				
				// always get the lowest probability and save the sum of the probability and 
				// the index of the segments-combination approach.
				if(probabilitySum < minProbability)
				{
					minProbability = probabilitySum;
					indexSegmentApproache = j;
				}
			}
			
			SegmentApproach segmentApproachSegmented = larvaImage.segmentApproaches.get(indexSegmentApproache);
			LarvaImageSegmented.addLarvaSegmentApproach(segmentApproachSegmented);
			
		}
		
		return larvaImagesSegmented;
	}
}
