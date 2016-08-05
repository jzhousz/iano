package controller;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import javax.swing.JOptionPane;
import entities.AVIHeaderReader;
import entities.Larva;
import entities.LinearLine;
import entities.OptionComponent;
import entities.ROIFrame;
import entities.ROIFrame.FrameType;
import file.CSVWriter;
import file.ImageSaver;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.AVI_Reader;
import manager.AVIManager;
import manager.AnnotationManager;
import manager.DirectoryManager;
import manager.DrawingManager;
import manager.FileManager;
import manager.HelperManager;
import manager.ImageManager;
import manager.MathManager;
import manager.PropertyManager;
import manager.StringManager;

/**
* The controller class that control the main sequence of the program.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class LarvaController implements Runnable 
{
	
	boolean skipFrame[] = { true };
	private final int ROI_WIDTH = 120; // Size of fly larvae ROI
	private final int ROI_HEIGHT = 120;
	
	// the prefix file name for the csv and the folder containing all the debugging images
	private String filePrefix = "";

	private Point roiTopLeft = null;

	// the directory containing all the debug images
	private String dirImageDebug = "";
	// the output animation directory
	private final String DIR_IMAGE_ANIMATION = "aAnnotation";
	// the directory containing the animated images
	private String dirImageAnimation = "";
	
	// the Region of interest frame list used to record frame information
	private Map<Integer, ROIFrame> mapROIFrame = null;
	private ArrayList<Larva> listLarva = null;

	private DateFormat dateFormatFile = null;
	private Date date = null;
	// the avi file name
	private String fileName = "";

	private Thread thread;
	private String threadName;
	   
	private Date dateSys = null;
	private DateFormat dateSysFormat = null;
	
	private String dirTemp = "";
	
	private OptionComponent optionComponent = null;
	   
	private PropertyManager prop = new PropertyManager();
	
	private int firstTrackFrame = 0;
	
	// the total process
	private int totalProcNum = 0;
	
	// the process number
	private int procNum = 0;

	/**
	* The controller class that control the main sequence of the program.
	* 
	* @param optionComponent The GUI object.
	* @param procIndex The process index. 
	* @param totalProcNum The total number of processes.
	* @return None
	*/
	public LarvaController(OptionComponent optionComponent, int procIndex, int totalProcNum)
	{
		this.optionComponent = optionComponent;
		
		this.procNum = procIndex + 1;
		this.totalProcNum = totalProcNum;
		prop.getAllProperties();
		
		mapROIFrame = new HashMap<Integer, ROIFrame>();
		listLarva = new ArrayList<Larva>();
		
		String startx = prop.getStart_x();
		String starty = prop.getStart_y();
		
		roiTopLeft = new Point(Integer.parseInt(startx), Integer.parseInt(starty));
		
		dateFormatFile = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss");
		dateSysFormat = new SimpleDateFormat("HH:mm:ss");
		date = new Date();

		fileName = StringManager.getFileName(prop.getAvi_file());
		
		filePrefix = prop.getOutput_path()+fileName+ "_n" + procNum + "_" + dateFormatFile.format(date);
				
		dirImageDebug = filePrefix +"/";
		dirImageAnimation = dirImageDebug + DIR_IMAGE_ANIMATION + "/";
		
		dirTemp = dirImageDebug + "temp/";
		
		DirectoryManager.createDirectory(dirTemp);
		DirectoryManager.createDirectory(dirImageDebug+"dBinary/" );
		DirectoryManager.createDirectory(dirImageDebug+"dCrop/" );
		
		boolean outputOpt1 = prop.getOutput_complete_csv().equals("true");
		boolean outputOpt2 = prop.getOutput_video().equals("true");
		boolean outputOpt3 = prop.getOutput_animated_image().equals("true");
		boolean outputOpt4 = prop.getOutput_chart().equals("true");
		boolean outputOpt5 = prop.getOutput_debug().equals("true");
		
		if(outputOpt1 || outputOpt2 || outputOpt3 || outputOpt4 || outputOpt5)
		{
			DirectoryManager.createDirectory(dirImageDebug );
		}
		
		threadName = "LarvaProcessor";
		System.out.println("Creating " +  threadName );
	}

	/**
	* Run the thread.
	* 
	* @return None.
	*/
	public void run() 
	{
		// Used to read the AVI movie
		AVIHeaderReader headerReader = new AVIHeaderReader();
		// get the number of total frames
		int aviTotalFrames = headerReader.getTotalFrames(prop.getAvi_file());

		// The ROI of the frame containing a larva that will be tracked
		Roi roiStart = new Roi(roiTopLeft.x, roiTopLeft.y, ROI_WIDTH, ROI_HEIGHT);

		int frameFrom = Integer.parseInt(prop.getFrom_frame());
		int frameTo = Integer.parseInt(prop.getTo_frame());
		
		frameFrom = frameFrom == 0 ? 1 : frameFrom;
		frameTo = frameTo == 0 ? aviTotalFrames : frameTo;
		frameTo = frameTo <= aviTotalFrames  ? frameTo : aviTotalFrames;
		
		// Previous features of the larvae
		Larva larvaPrevious = null;

		showSysStatus("[System] Generated files will be saved to folder: "
				+"\n                                      "+dirImageDebug + ".");
		
		optionComponent.showImageStatusPanel();
		
		// loop through all the frames of the avi video
		for (int frameId = frameFrom; frameId <= frameTo; frameId++) 
		{
			showStatus("*) Processing frame " + frameId + " of " + frameTo+" (larva "+ procNum +" / "+totalProcNum+")\n");
			
			Larva larva = new Larva();

			larva.setLarvaPrevious(larvaPrevious);
			larva.setFrameId(frameId);

			// if the previous larva exists
			if(larva.getLarvaPrevious() != null)
			{
				// if the previous frame is not valid, set the current larva
				// isPreviousValid to false
				if(!larva.getLarvaPrevious().getIsValid())
					larva.setIsPreviousValid(false);
			// if the previous larva doesn't exist
			}else{
				larva.setIsPreviousValid(false);
			}
			
			// crop the full frame and return the cropped frame of ROI
			ImagePlus imagePlusCrop = ImageManager.cropFrame(frameId, roiStart, prop.getAvi_file(), dirImageDebug);

			ImageSaver.saveImagesWithPath(dirTemp, "imagePlusCrop" + frameId, imagePlusCrop);
			
			ImageSaver.saveImagesWithPath(dirImageDebug+"dCrop/", "Crop_" + frameId, imagePlusCrop);
			
			// ========================= Section 0 ============================
			// =============== get the information of the cropped frame =======
			// == check if the frame is needed to be tracked, recorded
			boolean isBuleFrame = ImageManager.detectImageColor(imagePlusCrop);
			ROIFrame roiFrame = new ROIFrame(frameId);
	
			mapROIFrame.put(new Integer(frameId), roiFrame);
			
			boolean isBlackFrame = ImageManager.checkBlackFrame(imagePlusCrop);

			// if the frame does NOT only contain black color.
			// In another word, if the frame contains colors other than black.
			if (!isBlackFrame) 
			{
				// this frame is needed to be tracked for the larva
				roiFrame.setNeedTrack(true); 
				larva.setNeedTrack(true);

				// if it's a Chrimson video
				if (prop.getChrimson_stimulus().equals("true")) {
					// if it's a red frame
					if (!isBuleFrame) {
						roiFrame.setFrameType(FrameType.RED);
						// this frame is needed to be recorded in the csv file
						roiFrame.setNeedRecord(true);
						larva.setNeedRecord(true);
					}

				// if it's a optogenetics video (blue light).
				} else {
					// if it's a blue frame. 
					if (isBuleFrame) {
						roiFrame.setFrameType(FrameType.BLUE);
						// This frame is needed to be recorded in the csv file
						roiFrame.setNeedRecord(true); 
						larva.setNeedRecord(true);
					// if it's a red frame. 
					}else{
						roiFrame.setFrameType(FrameType.RED);
					}
				}
			}
			
			// if the frame is NOT needed to be tracked, skid all the code
			// below. go to process the next frame.
			if (!roiFrame.getNeedTrack())
				continue;

			// ========================= Section 1 ============================
			// ================== get the center of the larva on ROI ==========
			// convert the cropped image to binary and get the binary image
			ImagePlus imagePlusBinay = ImageManager.getBinary(imagePlusCrop);

			ImageSaver.saveImagesWithPath(dirImageDebug+"dBinary/", "Binary_" + frameId, imagePlusBinay);
			
			// if binarization fails
			if (imagePlusBinay == null) {
				return;
			}

			ImagePlus imagePlusPreviousBigObj = null;

			ImagePlus imagePlusBigObj = ImageManager.getLargestBinaryObjectOverlap(imagePlusPreviousBigObj, imagePlusBinay);

			int sizeBigObj = ImageManager.getPixelArea(imagePlusBigObj, 128);
			
			larva.setArea(sizeBigObj);

			// get the skeleton image plus for the larva in the ROI
			ImagePlus imagePlusSkeletonRaw = ImageManager.getStemSkeleton(imagePlusBigObj, 128);

			if(prop.getOutput_debug().equals("true"))
			{
				DirectoryManager.createDirectory(dirImageDebug+"dSkeleton_Before_Centered/" );
				ImageSaver.saveImagesWithPath(dirImageDebug+"dSkeleton_Before_Centered/", "Skeleton1_" + frameId, imagePlusSkeletonRaw);
			}
			
			double lengthSkeleton = ImageManager.getSkeletonLength(imagePlusSkeletonRaw, 128);
			
			larva.setLengthSkeleton(lengthSkeleton);
			
			ArrayList<Point> pointEnds = ImageManager.findEndPoints(imagePlusSkeletonRaw, 128);

			// if the number of end points gotten is other than 2, the get-end-point
			// part fails.
			// don't continue to get other information for this larva.
			// go to process the next larva ROI frame
			if (pointEnds.size() != 2) {
				
				larva.setIsValid(false);
				String msg = "Error: frame " + frameId + " get " + pointEnds.size() + " end points.";
				
				larva.getMsgError().add(msg);

				continue;
			}
			
			// ??? findCenterPoint() repeat findEndPoints() process. Waste calculation resources.
			ArrayList<Point> pointCenters = ImageManager.findCenterPoints(imagePlusSkeletonRaw, 128);
			
			Point pointCenter = pointCenters.get(0); // the center of skeleton

			// ??? getStemSkeleton() repeat findEndPoints() process. Waste calculation resources.
			ImagePlus imagePlusSkeletonStem = ImageManager.getStemSkeleton(imagePlusSkeletonRaw,  128);
			
			// shift the image location
			int diffentsInROIX = imagePlusSkeletonStem.getWidth() / 2 - pointCenter.x;
			int diffentsInROIY = imagePlusSkeletonStem.getHeight() / 2 - pointCenter.y;
			
			roiStart.setLocation(roiStart.getBounds().x - diffentsInROIX, roiStart.getBounds().y - diffentsInROIY);
			pointCenter.x = imagePlusSkeletonStem.getWidth() / 2;
			pointCenter.y = imagePlusSkeletonStem.getHeight() / 2;
			
			larva.setCenterPoint(pointCenter);

			// Save the coordinate of top left corner of the ROI on avi full
			// frame
			roiTopLeft.setLocation(roiStart.getBounds().x, roiStart.getBounds().y);
			larva.setRoiTopLeft(new Point(roiTopLeft.x, roiTopLeft.y));
			
			larva.setRoi(roiStart);

			// ========================= Section 2 New ============================
			// ================ Adjust images based on the center point ===========

			Roi roiCenter = new Roi(roiTopLeft.x, roiTopLeft.y, ROI_WIDTH, ROI_HEIGHT);
			// crop the ROI frame after the larva is centered
			imagePlusCrop = ImageManager.cropFrame(frameId, roiCenter, prop.getAvi_file(), dirImageDebug);

			ImagePlus imagePlusBinayShift = ImageManager.shiftImage(imagePlusBinay, diffentsInROIX, diffentsInROIY, 128);
			ImageSaver.saveImagesWithPath(dirTemp, "imagePlusBinary" + frameId, imagePlusBinayShift);
			
			if(prop.getOutput_debug().equals("true"))
			{
				DirectoryManager.createDirectory(dirImageDebug+"dBinary_Advanced/" );
				ImageSaver.saveImagesWithPath(dirImageDebug+"dBinary_Advanced/", "Binary1_Before_Centered_" + frameId, imagePlusBinay);
			}
			
			ImagePlus imagePlusBigObjShift = ImageManager.shiftImage(imagePlusBigObj, diffentsInROIX, diffentsInROIY, 128);

			IJ.run(imagePlusBigObjShift, "Fill Holes", "");
			
			if(prop.getOutput_debug().equals("true"))
			{
				ImageSaver.saveImagesWithPath(dirImageDebug+"dBinary_Advanced/", "BigObject3_After_Centered_" + frameId, imagePlusBigObjShift);
			}
			
			ImagePlus imagePlusSkeletonShift = ImageManager.shiftImage(imagePlusSkeletonStem, diffentsInROIX, diffentsInROIY, 128);
			ImageSaver.saveImagesWithPath(dirTemp, "imagePlusSkeleton" + frameId, imagePlusSkeletonShift);
			
			ArrayList<Point> pointsEndShift = ImageManager.findEndPoints(imagePlusSkeletonShift, 128);

			larva.setEndPoints(pointsEndShift);
			
			ArrayList<Point> pointCentersShift = ImageManager.findCenterPoints(imagePlusSkeletonShift, 128);
			
			larva.setPoint1stQuartile( pointCentersShift.get(1) );
			larva.setPoint3rdQuartile( pointCentersShift.get(2) );
			
			Point point1stQuartileFullFrame = larva.getPointOnFullFrame(pointCentersShift.get(1));
			Point point3rdQuartileFullFrame = larva.getPointOnFullFrame(pointCentersShift.get(2));
			
			LinearLine lineQuartile = MathManager.getLinearLine(point1stQuartileFullFrame, point3rdQuartileFullFrame);
			
			LinearLine lineQuartileParallel = MathManager.getParallelLine(lineQuartile.getBeta1(),larva.getCenterPointOnFullFrame());
			
			larva.setLineQuartileParallel(lineQuartileParallel);
			
			LinearLine lineQuartilePerp = MathManager.getPerpendicularLine(lineQuartileParallel, larva.getCenterPointOnFullFrame());
			
			larva.setLineQuartilePerp(lineQuartilePerp);
			
			if (larvaPrevious != null) {
				double distQuartile = MathManager.getNearestDistance(larvaPrevious.getLineQuartileParallel(), larva.getCenterPointOnFullFrame());
				larva.setDistQuartile(distQuartile);
			}
			
			// get the edge image plus of the larva in the ROI
			ImagePlus imagePlusEdge = ImageManager.getEdge(imagePlusBigObjShift, frameId, dirImageDebug);
			ImageSaver.saveImagesWithPath(dirTemp, "imagePlusEdge" + frameId, imagePlusEdge);

			ArrayList<Point> pointsDiameter = MathManager.calcQuartilePoints(imagePlusEdge, larva);
			
			larva.setPointsQuartile(pointsDiameter);
			
			double distQuartileLargest = MathManager.calcDiameter(pointsDiameter, larva.getCenterPoint() );
			
			larva.setDiameter(distQuartileLargest);
			
			if (larvaPrevious != null) 
			{
				int endPoint1Index = MathManager.getClosestPointIndex(larva.getEndPointsOnFullFrame(),
						larvaPrevious.getEndPointsOnFullFrame().get(0));

				// if the point closest to the end point 1 (index=0) of the
				// previous larva is end point 2 (index=1).
				// swap the values of the end point 1 (index=0) and 2 (index=1)
				// of the current larva
				if (endPoint1Index == 0) {
					Point pointTemp = new Point(larva.getEndPoints().get(0).x,
							larva.getEndPoints().get(0).y);
					larva.getEndPoints().set(0, new Point(larva.getEndPoints().get(1).x,
							larva.getEndPoints().get(1).y));
					larva.getEndPoints().set(1, new Point(pointTemp.x, pointTemp.y));
				}

				double distanceEndPoint1 = MathManager.getDistance(larva.getEndPointsOnFullFrame().get(0),
						larvaPrevious.getEndPointsOnFullFrame().get(0));
				
				double distanceEndPoint2 = MathManager.getDistance(larva.getEndPointsOnFullFrame().get(1),
						larvaPrevious.getEndPointsOnFullFrame().get(1));

				larva.setDistanceEndPoint1(distanceEndPoint1);
				larva.setDistanceEndPoint2(distanceEndPoint2);

				if (distanceEndPoint1 >= distanceEndPoint2)
					larva.setIsEndPoint1WinDistanceMoved(true);
				else
					larva.setIsEndPoint1WinDistanceMoved(false);
			}

			int amountPixelEndPoint1 = ImageManager.getPixelsAmount(imagePlusBigObjShift,
					larva.getEndPoints().get(0), 3);

			int amountPixelEndPoint2 = ImageManager.getPixelsAmount(imagePlusBigObjShift,
					larva.getEndPoints().get(1), 3);

			// if the amount of pixels of end point 1 is more than end point 2,
			// end point 1 wins this vote as the head.
			if (amountPixelEndPoint1 >= amountPixelEndPoint2)
				larva.setIsEndPoint1WinPixelLevel(true);
			else
				larva.setIsEndPoint1WinPixelLevel(false);

			double distanceMoved = 0; // the distance the larva moved
			
			double sidewaysDistance = 0;
			double sidewaysDistanceEndPts = 0;

			if (larva.getLarvaPrevious() != null) 
			{
				sidewaysDistance = MathManager.getNearestDistance(
						larva.getLarvaPrevious().getLinearLineParallel(),
						larva.getCenterPointOnFullFrame());

				larva.setDistanceSideways(sidewaysDistance);

				Point2D pointAvg = MathManager.getAveragePoint(
						larva.getLarvaPrevious().getEndPointsOnFullFrame().get(0),
						larva.getLarvaPrevious().getEndPointsOnFullFrame().get(1));

				// check whether these two points are in the same side of the
				// linear line.
				// if yes, the larva moved forward in the sideways direction.
				boolean isSideWaysForward = MathManager.isPointsInSameSide(
						larva.getLarvaPrevious().getLinearLineParallel(), pointAvg,
						larva.getCenterPointOnFullFrame());

				larva.setIsSidewaysForward(isSideWaysForward);

				// get the sideways end point distance
				sidewaysDistanceEndPts = MathManager.getNearestDistance(
						larva.getLarvaPrevious().getLinearLineEndPtsParallel(),
						larva.getCenterPointOnFullFrame());
				
				larva.setDistanceSidewaysEndPts(sidewaysDistanceEndPts);

				// check if these two points are in the same side of the
				// line. If yes, the larva moved forward in the sideways direction.
				boolean isSidewaysEndPtsForward = MathManager.isPointsInSameSide(
						larva.getLarvaPrevious().getLinearLineEndPtsParallel(), pointAvg,
						larva.getCenterPointOnFullFrame());

				larva.setIsSidewaysEndPtsForward(isSidewaysEndPtsForward);

				// get the distance the larva moved
				distanceMoved = MathManager.getDistance(larva.getCenterPointOnFullFrame(),
						larvaPrevious.getCenterPointOnFullFrame());
				
				larva.setDistanceCenterPoint(distanceMoved);
			}
			
			double curl = MathManager.getCurl(larva.getCenterPoint(),
					larva.getEndPoints().get(0), larva.getEndPoints().get(1));
			larva.setCurl(curl);

			LinearLine linearRegression = MathManager.getLinearRegression(imagePlusSkeletonShift);
			larva.setLinearRegression(linearRegression);
			
			LinearLine linearLineParallel = MathManager.getParallelLine(linearRegression.getBeta1(),
					larva.getCenterPointOnFullFrame());
			
			LinearLine linearLineEndPt = MathManager.getTwoPointsLine(larva.getEndPointsOnFullFrame().get(0),
					larva.getEndPointsOnFullFrame().get(1));
			larva.setLinearLineEndPts(linearLineEndPt);

			LinearLine linearLineEndPtParallel = MathManager.getParallelLine(linearLineEndPt.getBeta1(),
					larva.getCenterPointOnFullFrame());
			larva.setLinearLineEndPtsParallel(linearLineEndPtParallel);

			double angleEndPointsLinear = MathManager.getAngle(linearLineParallel, linearLineEndPtParallel);
			larva.setAngleEndPointsLinear(angleEndPointsLinear);
			
			if(angleEndPointsLinear > 45)
				linearLineParallel = linearLineEndPtParallel;

			larva.setLinearLineParallel(linearLineParallel);

			LinearLine linearLinePerpendicular = MathManager.getPerpendicularLine(linearLineParallel,
					larva.getCenterPointOnFullFrame());
			larva.setLinearLinePerpendicular(linearLinePerpendicular);

			LinearLine linearLinePerpEndPoints = MathManager.getPerpendicularLine(linearLineEndPt,
					larva.getCenterPointOnFullFrame());
			larva.setLinearLineEndPtsPerp(linearLinePerpEndPoints);

			LinearLine linearLineEnd1Center = MathManager.getLinearLine(
					larva.getEndPointsOnFullFrame().get(0),
					larva.getCenterPointOnFullFrame());

			boolean isCurlRight = MathManager.isPointInRight(linearLineEnd1Center,
					larva.getEndPointsOnFullFrame().get(0), larva.getCenterPointOnFullFrame(),
					larva.getEndPointsOnFullFrame().get(1));

			larva.setIsCurlPos(isCurlRight);
			
			if (larva.getLarvaPrevious() == null) 
				larva.setNumberSign(frameId);
			
			if (larva.getLarvaPrevious() != null) 
			{ 
				boolean isDistSidewaysPos = MathManager.isPointInRight(
						larva.getLarvaPrevious().getLinearLineEndPtsParallel(),
						larva.getLarvaPrevious().getEndPointsOnFullFrame().get(0),
						larva.getLarvaPrevious().getCenterPointOnFullFrame(),
						larva.getCenterPointOnFullFrame());
				
				larva.setIsMoveRight(isDistSidewaysPos);
				
				double angleRef = MathManager.calcReferenceAngle(
						larva.getLarvaPrevious().getLinearLineEndPtsParallel(),
						larva.getLarvaPrevious().getEndPointsOnFullFrame().get(0),
						larva.getLarvaPrevious().getCenterPointOnFullFrame(),
						larva.getCenterPointOnFullFrame());

				larva.setAngleRefCenterN1(angleRef);
				
				boolean isMoveRight = MathManager.isMoveToRight(
						larva.getLarvaPrevious().getLinearLineEndPtsParallel(),
						larva.getLarvaPrevious().getEndPointsOnFullFrame().get(0),
						larva.getLarvaPrevious().getCenterPointOnFullFrame(),
						larva.getCenterPointOnFullFrame());
				
				larva.setIsDistSidewaysPos(isMoveRight);

				if(larva.getIsCurlPos() == larva.getLarvaPrevious().getIsCurlPos())
				{
					larva.setDistanceSidewaysAccumulate( 
							MathManager.get2DecimalPoints( larva.getLarvaPrevious().getDistanceSidewaysAccumulate() + 
							larva.getDistanceSidewaysEndPts() ) );
					larva.setNumberSign(larva.getLarvaPrevious().getNumberSign());
				}else{
					larva.setDistanceSidewaysAccumulate( 
							MathManager.get2DecimalPoints( larva.getDistanceSidewaysEndPts()) );
					larva.setNumberSign(larva.getFrameId());
				}
				
				larva.setDistanceSidewaysTotal(
						MathManager.get2DecimalPoints( larva.getLarvaPrevious().getDistanceSidewaysTotal() + 
								larva.getDistanceSidewaysEndPts()) );
			}

			Point ptMax = MathManager.calcCenterMass( imagePlusSkeletonShift );
			
			larva.setPointCenterMass(ptMax);
			
			LinearLine linearLineCenterMass = MathManager.getLinearLine(larva.getCenterPoint(), ptMax);
			
			larva.setLinearLineCenterMass(linearLineCenterMass);
			
			double angleCenterEndPt1 = MathManager.getAngleBetween(larva.getCenterPoint(), larva.getEndPoints().get(0), larva.getPointCenterMass());
			double angleCenterEndPt2 = MathManager.getAngleBetween(larva.getCenterPoint(), larva.getEndPoints().get(1), larva.getPointCenterMass());
			
			larva.setAngleEndPt1(angleCenterEndPt1);
			larva.setAngleEndPt2(angleCenterEndPt2);
			
			double angleCenterEnd1End2 = MathManager.getAngleBetween(larva.getEndPoints().get(0), larva.getCenterPoint(), larva.getEndPoints().get(1));
			angleCenterEnd1End2 = MathManager.calcSmallAngle( angleCenterEnd1End2 );
			larva.setAngleCenterEnd1End2(angleCenterEnd1End2);
			
			double angleCenterEnd2End1 = MathManager.getAngleBetween(larva.getEndPoints().get(1), larva.getCenterPoint(), larva.getEndPoints().get(0));
			angleCenterEnd2End1 = MathManager.calcSmallAngle( angleCenterEnd2End1 );
			larva.setAngleCenterEnd2End1(angleCenterEnd2End1);
			
			Point avgEndpt = new Point((int) Math.round(larva.getEndPoints().get(0).x + larva.getEndPoints().get(1).x)/2
					, (int) Math.round(larva.getEndPoints().get(0).y + larva.getEndPoints().get(1).y)/2);
			
			double angleAvgCenterEnd1 = MathManager.getAngleBetween(larva.getCenterPoint(), avgEndpt, larva.getEndPoints().get(0));
			angleAvgCenterEnd1 = MathManager.calcSmallAngle( angleAvgCenterEnd1 );
			larva.setAngleAvgCenterEnd1(angleAvgCenterEnd1);
			
			double angleAvgCenterEnd2 = MathManager.getAngleBetween(larva.getCenterPoint(), avgEndpt, larva.getEndPoints().get(1));
			angleAvgCenterEnd2 = MathManager.calcSmallAngle( angleAvgCenterEnd2 );
			larva.setAngleAvgCenterEnd2(angleAvgCenterEnd2);
			
			listLarva.add(larva);
			larvaPrevious = larva;
			
			showSysStatus("Done frame " + frameId + System.lineSeparator());
			
			optionComponent.updateImageStatus(dirImageDebug, frameId);
			
		} // end the main while loop

		optionComponent.updateImageStatusDone();
		
		showSysStatus("[System] Calculating the average diameter, area, and skeleton for the larva ...");
		
		ArrayList<Double> avgDiamPeriSkelArea = MathManager.getAvgDiameterPerimeterSkeletonArea(listLarva);
		double avgDiameter = avgDiamPeriSkelArea.get(0);
		double avgPerimeter = avgDiamPeriSkelArea.get(1);
		int avgSkeletonLen = avgDiamPeriSkelArea.get(2).intValue();
		int avgArea = avgDiamPeriSkelArea.get(3).intValue();
		
		for(Larva larva : listLarva)
		{
			larva.setAvgDiameter(avgDiameter);
			larva.setAvgArea(avgArea);
			larva.setAvgSkeletonLen(avgSkeletonLen);
		}
		
		HelperManager.setInvalidLarvaAndMessage(listLarva, avgDiameter, avgSkeletonLen, avgArea, prop);
		
		ArrayList< ArrayList<Larva> > larvaRoll = MathManager.calcRollingSections(listLarva);
		
		if(PropertyManager.getBool( prop.getFix_invalid_larva() ))
		{
			showSysStatus("[System] Fixing the invalid larva ...");
			// fixing the invalid binary frame
			MathManager.fixInvalidLarva(listLarva, optionComponent.getTextStatus());
		}else{
			showSysStatus("[System] Not fix the invalid larva ..");
		}

		// only set 80% of distance for the perimeter 
		avgPerimeter = MathManager.get2DecimalPoints(avgPerimeter 
				* Double.parseDouble(prop.getLarva_perimeter_percentage()) * 0.01);
		
		if(prop.getAuto_roll().equals("true"))
		{
			prop.setLarva_perimeter(Double.toString(avgPerimeter));
			prop.saveAllProperties();
		}
		
		double perimeterLarva = Double.parseDouble(prop.getLarva_perimeter());
		
		showSysStatus("[System] Analyzing rolling of the larva ...");
		
		LinkedHashSet<Integer> framesRoll = null;
		Iterator<Integer> itr = null;

		int rollingSection = 1;
		
		showSysStatus("(rolling Detection) Number of frames used: " 
				+ Integer.parseInt(prop.getRolling_frame()) + ".");
		showSysStatus("(rolling Detection) Perimeter of the larva used: " + perimeterLarva + ".");
		
		System.out.println("\n (rolling Detection) Section " + rollingSection +":\n\n");
		showSysStatus("(rolling Detection) Total sections of rolling: " + larvaRoll.size() );
		
		for(ArrayList<Larva> larvaSection : larvaRoll)
		{
			System.out.println("\n(rolling Detection) Section " + rollingSection + ":\n\n");
			showSysStatus("(rolling Detection) Frames of Section " + rollingSection +":");
			
			for(Larva larva : larvaSection)
			{
				showStatus(larva.getFrameId()+",");
				System.out.println("larva_id: "+larva.getFrameId());
				
				if(larva.getFrameId() % 20 == 0)
					showStatus("\n");
			}
			
			rollingSection ++;
			
			// Analyzing rolling of larvae
			framesRoll = MathManager.getRollingFrame(larvaSection, prop, Integer.parseInt(prop.getRolling_frame()), perimeterLarva, dirImageDebug);

			for(Larva larva : listLarva)
			{
				// loop through the framesRolling
				itr = framesRoll.iterator();
				   
			    while(itr.hasNext())
			    	// if the frame id is contained in framesRolling Hash set
			    	if( itr.next().equals(larva.getFrameId()))
			    	{
			    		larva.setIsRolling(true);
			    	}
			}
		}
		
		double distRoll = 0;
		double distRollMove = 0;
		double rolls = 0;
		
		for(Larva larva : listLarva)
		{
	    	// if the larva in the frame is rolling
	    	if( larva.getIsRolling() )
	    	{
	    		distRoll = MathManager.get2DecimalPoints( distRoll + larva.getDistanceSidewaysEndPts() );
	    		distRollMove = MathManager.get2DecimalPoints( distRollMove + larva.getDistanceCenterPoint() );
	    		
	    		larva.setDistSidewaysRollTotal( distRoll );
	    		larva.setDistSidewaysRollTotalMove( distRollMove );
	    		rolls = MathManager.get2DecimalPoints( (distRoll / larva.getAvgPerimeter()) );
	    		
	    		larva.setRolls(rolls);
	    	}
		}
		
		showStatus("\n");
		
		if(PropertyManager.getBool( prop.getOutput_animated_image() ) 
				|| PropertyManager.getBool( prop.getOutput_video() ) )
		{
			generateAnnotationImages();
			
		}
		
		showSysStatus("[System] Writing main CSV data ...");
		
		boolean isOutputCurl = prop.getOutput_curl().equals("true");
		boolean isOutputSpeed = prop.getOutput_speed().equals("true");
		boolean isOutputRoll = prop.getOutput_roll().equals("true");
		
		CSVWriter csvMainWriter = new CSVWriter(filePrefix+".csv");
		csvMainWriter.saveMainData(listLarva, prop.getAvi_file(), 
				date, prop.getLarva_id(), isOutputCurl, isOutputSpeed, isOutputRoll );
		
		if(prop.getOutput_complete_csv().equals("true"))
		{
			showSysStatus("[System] Writing complete CSV data ...");
			
			CSVWriter csvCompleteWriter = new CSVWriter(dirImageDebug+"complete.csv");
			csvCompleteWriter.saveData(listLarva, prop.getAvi_file(), date, prop.getLarva_id() );
		}
		
		if( PropertyManager.getBool( prop.getOutput_video() )  )
		{
			firstTrackFrame = Integer.parseInt( prop.getFrom_frame() );
			
			showSysStatus("[System] Generating the annotation video ...");
			showSysStatus("(generateVideo) Generating video beginning from frame " + firstTrackFrame + ".");
			String result = AVIManager.generateVideo(dirImageAnimation+"Annotation_%d.jpg", dirImageDebug+"aAnnotation.avi", Integer.toString( firstTrackFrame ));
			showSysStatus("[System] Results of generation of the annotation video:\n\n" + result);
		}
		
//		// show statistic
//		(new SummaryManager(listLarva)).show();
		showSysStatus("[System] Program has been completed!");
		showSysStatus("[System] Generated files will be saved to folder: "
						+"\n                                     "+dirImageDebug + ".");
		showSysStatus("[System] Total Time Completed: " + HelperManager.getDuration(date) +".");
		
		optionComponent.showAllButtons();
		
		FileManager.deleteDir(new File(dirTemp));

		if(!PropertyManager.getBool( prop.getOutput_debug() ) )
		{
			FileManager.deleteDir(new File(dirImageDebug+"dBinary/"));
			FileManager.deleteDir(new File(dirImageDebug+"dCrop/"));
		}
		
		if(!PropertyManager.getBool( prop.getOutput_animated_image() ) )
			FileManager.deleteDir(new File(dirImageDebug+"aAnnotation/"));
		
		if(procNum == totalProcNum)
			JOptionPane.showMessageDialog(null, "The process has been completed! Please check the output files.");
		
	}
	
	/**
	* Generate the annotation images.
	* 
	* @return None.
	*/
	void generateAnnotationImages()
	{
		showSysStatus("[System] Generating annotation images ...");
		
		showStatus("\nGenerating frame:\n");
		
		DirectoryManager.createDirectory(dirImageAnimation );

		int frameId = 0;
		
		// Generating debug and annotation images
		for(Larva larva : listLarva)
		{
			frameId = larva.getFrameId();

			AVI_Reader videoFeed = new AVI_Reader(); // read AVI
			ImageStack stackFrame = videoFeed.makeStack(prop.getAvi_file(), frameId, frameId, false, false, false);
	
			ImagePlus imagePlusAllFull = new ImagePlus();
			// Grab frame to be processed
			imagePlusAllFull.setProcessor(stackFrame.getProcessor(1));

			// draw all the lines and points on the image
			DrawingManager drawingManager = new DrawingManager(imagePlusAllFull, listLarva, larva, dirTemp);
			drawingManager.drawAll();

			// annotate information about the larva on the image
			(new AnnotationManager(larva, imagePlusAllFull, new Point(30, 15))).annotateAll(prop);
			
			drawingManager.drawSmallColorLarva(new Point(1130,100), Color.white);
			drawingManager.drawSmallOutlineLarva(new Point(1130,250), Color.white);
			drawingManager.drawSmallBinaryLarva(new Point(1130,400), Color.white);
			drawingManager.drawSmallEdgeSkeleton(new Point(1130,550), Color.white);
			
			AnnotationManager annotationManager = new AnnotationManager(larva, imagePlusAllFull, new Point(300, 15));
			annotationManager.annotate(
					"CenterSkeleton: "+MathManager.getPointStr(larva.getCenterPoint()) +
					", CenterMass: "+MathManager.getPointStr(larva.getPointCenterMass()) +
					", Angle1: "+larva.getAngleEndPt1() +
					", Angle2: "+larva.getAngleEndPt2()
					, Color.green);
			
			// annotate text in the animated images
			AnnotationManager annotationMgrErr = new AnnotationManager(larva, imagePlusAllFull,
					new Point(330, 300)); //new Point(30, 555));
			
			// if the larva is not valid, print warning message 
			// on the image
			if(!larva.getIsValid())
			{
				annotationMgrErr.resetErrorMsgPos();
				
				for(String msg : larva.getMsgWarning())
				{
					annotationMgrErr.annotate(msg, Color.yellow);
				}
			}
			
			ImageSaver.saveImagesWithPath(dirImageAnimation, "Annotation_" + frameId, imagePlusAllFull);
				
			showStatus(frameId+",");
			
			if(frameId % 20 == 19)
				showStatus("\n");
		}
	
		showStatus("\n");
	}
	
	/**
	* Start the thread.
	* 
	* @return None
	*/
	public void start ()
	{
      System.out.println("Starting " +  threadName );
      if (thread == null)
      {
    	  thread = new Thread (this, threadName);
    	  thread.start ();
      }
	}
	
	/**
	* Show text status on GUI windows.
	* 
	* @param status The status text.
	* @return None.
	*/
	private void showStatus(String status)
	{
		System.out.print(status);
		showSysStatusBar(status);
		ij.IJ.showStatus(status);
		
		optionComponent.getTextStatus().append(status);
		optionComponent.getTextStatus().setCaretPosition(optionComponent.getTextStatus().getDocument().getLength());
		optionComponent.getTextStatus().validate();
	}
	
	/**
	* Show system status.
	* 
	* @param status The status text.
	* @return None.
	*/
	private void showSysStatus(String status)
	{
		dateSys = new Date();
		showSysStatusBar(status);
		showStatus("\n("+dateSysFormat.format(dateSys)+") "+status+"\n");
	}
	
	/**
	* Show system status text on the lower left bar.
	* 
	* @param text The status text.
	* @return None.
	*/
	private void showSysStatusBar(String text)
	{
		text = "Status: " + text;
		optionComponent.getTextSysStatus().setText(text);
	}

}
