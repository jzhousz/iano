package controller;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import entities.AVIHeaderReader;
import entities.FeaturesOfLarva;
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
 * The larva program processor, used to call all classes and functions
 *
 */
public class LarvaController implements Runnable {
	
	Boolean skipFrame[] = { true };
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
	private ArrayList<FeaturesOfLarva> listLarva = null;

	private DateFormat dateFormat = null;
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
	private boolean isSetFirstTrackFrame = false;
	
	// the total process
	private int totalProcNum = 0;
	
	// the process number
	private int procNum = 0;

	public LarvaController(OptionComponent optionComponent, int procIndex, int totalProcNum)
	{
		this.optionComponent = optionComponent;
		
		this.procNum = procIndex + 1;
		this.totalProcNum = totalProcNum;
		prop.getAllProperties();
		
		mapROIFrame = new HashMap<Integer, ROIFrame>();
		listLarva = new ArrayList<FeaturesOfLarva>();
		
		String startx = prop.getStart_x();
		String starty = prop.getStart_y();
		
		roiTopLeft = new Point(Integer.parseInt(startx), Integer.parseInt(starty));
		
		dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
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
		FeaturesOfLarva larvaPrevious = null;

		showSysStatus("[System] Generated files will be saved to folder: "
				+"\n                                      "+dirImageDebug + ".");
		
		optionComponent.showImageStatusPanel();
		
		// loop through all the frames of the avi video
		for (int frameId = frameFrom; frameId <= frameTo; frameId++) 
		{
			showStatus("*) Processing frame " + frameId + " of " + frameTo+" (larva "+ procNum +" / "+totalProcNum+")\n");
			
			FeaturesOfLarva larva = new FeaturesOfLarva();

			larva.setFeaturesOfLarvaPrevious(larvaPrevious);
			larva.setFrameId(frameId);

			// if the previous larva exists
			if(larva.getFeaturesOfLarvaPrevious() != null)
			{
				// if the previous frame is not valid, set the current larva
				// isPreviousValid to false
				if(!larva.getFeaturesOfLarvaPrevious().getIsValid())
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
//					showSysStatus("Inside chrimson_stimulus = false");
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

			LinearLine lineQuartilePerpRoi = MathManager.getParallelLine(larva.getLineQuartilePerp().getBeta1(), larva.getCenterPoint());
			
			ArrayList<Point> pointsDiameter = new ArrayList<Point>();
			
			Point pointQuartile1 = null;
			Point pointQuartile2 = null;
			double distQuartileLargest = 0;
			double distQuartile = 0;

			for(int y = 0; y < imagePlusEdge.getHeight(); y++ )
				for(int x = 0; x < imagePlusEdge.getWidth(); x++)
				{
					if( imagePlusEdge.getProcessor().getPixel(x, y) >= 128 )
					{
						int yLine = (int) lineQuartilePerpRoi.getY((double)x);
						
						if( Math.abs( yLine - y ) < 3 )
						{
							Point pt = new Point(x, y);
							
							pointsDiameter.add(pt);
							
							distQuartile = MathManager.getDistance(pt, larva.getCenterPoint());
							
							if(distQuartile >= distQuartileLargest)
							{
								pointQuartile1 = pt;
								distQuartileLargest = distQuartile;
							}
							
						}
							
					}
				}
			
			distQuartileLargest = 0;
			
			for(Point pt : pointsDiameter)
			{
				distQuartile = MathManager.getDistance(pt, pointQuartile1);
				
				if(distQuartile >= distQuartileLargest)
				{
					pointQuartile2 = pt;
					distQuartileLargest = distQuartile;
				}
				
			}
			
			larva.setPointsQuartile(pointsDiameter);
			
			larva.setDiameter(distQuartileLargest);
			
			if (larvaPrevious != null) {
				
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

			if (larva.getFeaturesOfLarvaPrevious() != null) 
			{
				sidewaysDistance = MathManager.getNearestDistance(
						larva.getFeaturesOfLarvaPrevious().getLinearLineParallel(),
						larva.getCenterPointOnFullFrame());

				larva.setDistanceSideways(sidewaysDistance);

				Point2D pointAvg = MathManager.getAveragePoint(
						larva.getFeaturesOfLarvaPrevious().getEndPointsOnFullFrame().get(0),
						larva.getFeaturesOfLarvaPrevious().getEndPointsOnFullFrame().get(1));

				// check whether these two points are in the same side of the
				// linear line.
				// if yes, the larva moved forward in the sideways direction.
				Boolean isSideWaysForward = MathManager.checkPointsSide(
						larva.getFeaturesOfLarvaPrevious().getLinearLineParallel(), pointAvg,
						larva.getCenterPointOnFullFrame());

				larva.setIsSidewaysForward(isSideWaysForward);

				// get the sideways end point distance
				sidewaysDistanceEndPts = MathManager.getNearestDistance(
						larva.getFeaturesOfLarvaPrevious().getLinearLineEndPtsParallel(),
						larva.getCenterPointOnFullFrame());
				
				larva.setDistanceSidewaysEndPts(sidewaysDistanceEndPts);

				// check whether these two points are in the same side of the
				// linear line.
				// if yes, the larva moved forward in the sideways direction.
				Boolean isSidewaysEndPtsForward = MathManager.checkPointsSide(
						larva.getFeaturesOfLarvaPrevious().getLinearLineEndPtsParallel(), pointAvg,
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

			Boolean isCurlRight = MathManager.isPointInRight(linearLineEnd1Center,
					larva.getEndPointsOnFullFrame().get(0), larva.getCenterPointOnFullFrame(),
					larva.getEndPointsOnFullFrame().get(1));

			larva.setIsCurlPos(isCurlRight);
			
			if (larva.getFeaturesOfLarvaPrevious() == null) 
				larva.setNumberSign(frameId);
			
			if (larva.getFeaturesOfLarvaPrevious() != null) 
			{
				Boolean isMoveRight = MathManager.isPointInRight(
						larva.getFeaturesOfLarvaPrevious().getLinearLineEndPtsParallel(),
						larva.getFeaturesOfLarvaPrevious().getEndPointsOnFullFrame().get(0),
						larva.getFeaturesOfLarvaPrevious().getCenterPointOnFullFrame(),
						larva.getCenterPointOnFullFrame());

				larva.setIsDistSidewaysPos(isMoveRight);

				if(larva.getIsCurlPos() == larva.getFeaturesOfLarvaPrevious().getIsCurlPos())
				{
					larva.setDistanceSidewaysAccumulate( 
							MathManager.get2DecimalPoints( larva.getFeaturesOfLarvaPrevious().getDistanceSidewaysAccumulate() + 
							larva.getDistanceSidewaysEndPts() ) );
					larva.setNumberSign(larva.getFeaturesOfLarvaPrevious().getNumberSign());
				}else{
					larva.setDistanceSidewaysAccumulate( 
							MathManager.get2DecimalPoints( larva.getDistanceSidewaysEndPts()) );
					larva.setNumberSign(larva.getFrameId());
				}
				
				larva.setDistanceSidewaysTotal(
						MathManager.get2DecimalPoints( larva.getFeaturesOfLarvaPrevious().getDistanceSidewaysTotal() + 
								larva.getDistanceSidewaysEndPts()) );
			}

			int numPixel = 0;
			int xSum = 0;
			int ySum = 0;

			for (int y = 0; y < imagePlusSkeletonShift.getHeight(); y++)
				for (int x = 0; x < imagePlusSkeletonShift.getWidth(); x++) {
					if(imagePlusSkeletonShift.getProcessor().getPixel(x, y) > 128)
					{
						xSum += x;
						ySum += y;
						numPixel++;
					}
				}
			
			int xMass = xSum / numPixel;
			int yMass = ySum / numPixel;
			
			Point ptMax = new Point(xMass, yMass);
			
			larva.setPointCenterMass(ptMax);
			
			LinearLine linearLineCenterMass = MathManager.getLinearLine(larva.getCenterPoint(), ptMax);
			
			larva.setLinearLineCenterMass(linearLineCenterMass);
			
			double angleCenterEndPt1 = MathManager.getAngleBetween(larva.getCenterPoint(), larva.getEndPoints().get(0), larva.getPointCenterMass());
			double angleCenterEndPt2 = MathManager.getAngleBetween(larva.getCenterPoint(), larva.getEndPoints().get(1), larva.getPointCenterMass());
			
			larva.setAngleEndPt1(angleCenterEndPt1);
			larva.setAngleEndPt2(angleCenterEndPt2);
			
			
			listLarva.add(larva);
			larvaPrevious = larva;
			
			showSysStatus("Done frame " + frameId + System.lineSeparator());
			
			final ImageIcon imgaeIconCrop = new ImageIcon(dirImageDebug+"dCrop/Crop_"+ frameId + ".jpg");
			final ImageIcon imgaeIcon2 = (ImageIcon) optionComponent.getLabelsCrop().get(2).getIcon();
			final ImageIcon imgaeIcon1 = (ImageIcon) optionComponent.getLabelsCrop().get(1).getIcon();

			optionComponent.getLabelsCrop().get(2).setIcon(imgaeIconCrop);
			optionComponent.getLabelsCropText().get(2).setText( "frame " + Integer.toString( frameId ) );
			
			if(frameId - Integer.parseInt( prop.getFrom_frame() ) >= 2)
			{
				optionComponent.getLabelsCrop().get(1).setIcon(imgaeIcon2);
				optionComponent.getLabelsCropText().get(1).setText( "frame " + Integer.toString( frameId - 1 ) );
				optionComponent.getLabelsCrop().get(0).setIcon(imgaeIcon1);
				optionComponent.getLabelsCropText().get(0).setText( "frame " + Integer.toString( frameId - 2 ) );
			}
			
			final ImageIcon imgaeIconBinary = new ImageIcon(dirImageDebug+"dBinary/Binary_"+ frameId + ".jpg");
			final ImageIcon imgaeIconBinary2 = (ImageIcon) optionComponent.getLabelsBinary().get(2).getIcon();
			final ImageIcon imgaeIconBinary1 = (ImageIcon) optionComponent.getLabelsBinary().get(1).getIcon();
			
			optionComponent.getLabelsBinary().get(2).setIcon(imgaeIconBinary);
			optionComponent.getLabelsBinaryText().get(2).setText( "frame " + Integer.toString( frameId ) );
			
			if(frameId - Integer.parseInt( prop.getFrom_frame() ) >= 2)
			{
				optionComponent.getLabelsBinary().get(1).setIcon(imgaeIconBinary2);
				
				optionComponent.getLabelsBinaryText().get(1).setText( "frame " + Integer.toString( frameId - 1 ) );
				
				optionComponent.getLabelsBinary().get(0).setIcon(imgaeIconBinary1);
				
				optionComponent.getLabelsBinaryText().get(0).setText( "frame " + Integer.toString( frameId - 2 ) );
			}
			

		} // end the while loop

		for(int i = 0; i < 3; i++)
		{
			optionComponent.getLabelsCrop().get(i).setIcon(optionComponent.getSampleCropImageIcon());
			optionComponent.getLabelsCropText().get(i).setText( "Read all frames" );
			
			optionComponent.getLabelsBinary().get(i).setIcon(optionComponent.getSampleBinaryImageIcon());
			optionComponent.getLabelsBinaryText().get(i).setText( "Read all frames" );
		}
		
		optionComponent.showTextStatusPanel();
		
		showSysStatus("[System] Calculating the average diameter, area, and skeleton for larvae ...");
		
		// the list containing the larvae that ware needed to be recorded.
		// the blue frame in Optogenetic video and red frame in Chrimson video
		ArrayList<FeaturesOfLarva> listLarvaRecord = new ArrayList<FeaturesOfLarva>();
		ArrayList< ArrayList<FeaturesOfLarva> > larvaRoll = new ArrayList< ArrayList<FeaturesOfLarva> >();
		
		// the list contains all the larvae's diameters for all the frames
		// that needed to be recorded.
		ArrayList<Double> diameters = new ArrayList<Double>();
		// the list contains all the larvae's pixel area for all the frames
		// that needed to be recorded.
		ArrayList<Integer> areas = new ArrayList<Integer>();
		
		ArrayList<Double> skeletonLens = new ArrayList<Double>();
		
		ArrayList<FeaturesOfLarva> listLarvaRoll = null;
		boolean isNeedStart1 = true;
				
		for(FeaturesOfLarva larva : listLarva)
		{
			if(larva.getNeedRecord())
			{
				listLarvaRecord.add(larva);
				
				if(isNeedStart1)
				{
					isNeedStart1 = false;
					
					listLarvaRoll = new ArrayList<FeaturesOfLarva>();
					
					larvaRoll.add(listLarvaRoll);
				}
				
				listLarvaRoll.add(larva);
			}else{
				isNeedStart1 = true;
			}
			
			diameters.add(larva.getDiameter());
			areas.add(larva.getArea());
			skeletonLens.add(larva.getLengthSkeleton());
		}
				
		//sort the list
		Collections.sort(diameters);
		
		Collections.sort(areas);
		
		Collections.sort(skeletonLens);
		
		int indexBegin = 0;
		int indexEnd = 0;
		double avgDiameter = 0;
		double avgPerimeter = 0;
		
		if(diameters.size() >= 5)
		{
			indexBegin = ( diameters.size() - 1 ) / 2 - 2;
			indexEnd = ( diameters.size() - 1 ) / 2 + 2;

			for(int i = indexBegin; i <= indexEnd; i++)
			{
				avgDiameter += diameters.get(i);
			}
			
			avgDiameter = avgDiameter / 5;
			avgPerimeter = MathManager.get2DecimalPoints(avgDiameter * Math.PI);
			
		}
		
		int avgArea = 0;
		
		if(areas.size() >= 5)
		{
			indexBegin = ( areas.size() - 1 ) / 2 - 2;
			indexEnd = ( areas.size() - 1 ) / 2 + 2;

			for(int i = indexBegin; i <= indexEnd; i++)
			{
				avgArea += areas.get(i);
			}
			
			avgArea = avgArea / 5;
		}
		
		int avgSkeletonLen = 0;
		
		if(skeletonLens.size() >= 5)
		{
			indexBegin = ( skeletonLens.size() - 1 ) / 2 - 2;
			indexEnd = ( skeletonLens.size() - 1 ) / 2 + 2;

			for(int i = indexBegin; i <= indexEnd; i++)
			{
				avgSkeletonLen += skeletonLens.get(i);
			}
			
			avgSkeletonLen = avgSkeletonLen / 5;
		}
		
		int maxSizeLarva = 0;
		int minSizeLarva = 0;
		
		int maxSkeletonLarva = 0;
		int minSkeletonLarva = 0;
		
		if( PropertyManager.getBool( prop.getAuto_check_size() ) )
		{
			maxSizeLarva = (int) (avgArea * 1.4);
			minSizeLarva = (int) (avgArea * 0.6);
			
			prop.setMax_size(Integer.toString( maxSizeLarva ));
			prop.setMin_size(Integer.toString( minSizeLarva ));
		}else{
			maxSizeLarva = Integer.parseInt( prop.getMax_size() );
			minSizeLarva = Integer.parseInt( prop.getMin_size() );
		}
		
		if( PropertyManager.getBool( prop.getAuto_check_skeleton() ) )
		{
			maxSkeletonLarva = (int) (avgSkeletonLen * 1.4);
			minSkeletonLarva = (int) (avgSkeletonLen * 0.6);
			
			prop.setMax_skeleton(Integer.toString( maxSkeletonLarva ));
			prop.setMin_skeleton(Integer.toString( minSkeletonLarva ));
		}else{
			maxSkeletonLarva = Integer.parseInt( prop.getMax_skeleton() );
			minSkeletonLarva = Integer.parseInt( prop.getMin_skeleton() );
		}
		
		for(FeaturesOfLarva larva : listLarva)
		{
			larva.setAvgDiameter(avgDiameter);
			larva.setAvgArea(avgArea);
			larva.setAvgSkeletonLen(avgSkeletonLen);
			
			if (larva.getArea() >  maxSizeLarva ) 
			{
				larva.setIsValid(false);
				
				String msg = "Warning: The size of this larva (" + larva.getArea()
						+ ") is greater than the threshold (" + maxSizeLarva
						+ ").";
				
//				LogWriter.writeLog(msg, prop.getAvi_file());
				
				larva.getMsgWarning().add(msg);
			}
			
			if (larva.getArea() <  minSizeLarva ) 
			{
				larva.setIsValid(false);
				
				String msg = "Warning: The size of this larva (" + larva.getArea()
						+ ") is less than the threshold (" + minSizeLarva
						+ ").";
				
				larva.getMsgWarning().add(msg);
			}
			
			if (larva.getLengthSkeleton() > maxSkeletonLarva ) 
			{
				larva.setIsValid(false);
				
				String msg = "Warning: The skeleton of this larva (" + larva.getLengthSkeleton()
				+ ") is greater than the threshold (" + maxSkeletonLarva
				+ ").";
				
				larva.getMsgWarning().add(msg);
			}
			
			if (larva.getLengthSkeleton() < minSkeletonLarva ) 
			{
				larva.setIsValid(false);

				String msg = "Warning: The skeleton of this larva (" + larva.getLengthSkeleton()
				+ ") is less than the threshold (" + minSkeletonLarva
				+ ").";
				
				larva.getMsgWarning().add(msg);
			}

			// find the first frame that needs to be tracked.
			if(!isSetFirstTrackFrame)
				if(larva.getNeedTrack())
				{
					isSetFirstTrackFrame = true;
					firstTrackFrame =larva.getFrameId();
				}
		}
		
//		prop.saveAllProperties();
		
		if(PropertyManager.getBool( prop.getFix_invalid_larva() ))
		{
			showSysStatus("[System] Fixing the invalid larvae ...");
	
			// fixing the invalid binary frame
			MathManager.fixInvalidLarva(listLarva, optionComponent.getTextStatus());
		}else{
			showSysStatus("[System] Not fix the invalid larvae ..");
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
		
		showSysStatus("[System] Analyzing rolling of larvae ...");
		
		LinkedHashSet<Integer> framesRoll = null;
		Iterator<Integer> itr = null;

		int rollingSection = 1;
		
		showSysStatus("(rolling Detection) Number of frames used: " 
				+ Integer.parseInt(prop.getRolling_frame()) + ".");
		showSysStatus("(rolling Detection) Perimeter of the larva used: " + perimeterLarva + ".");
		
		
		System.out.println("\n (rolling Detection) Section " + rollingSection +":\n\n");
		showSysStatus("(rolling Detection) Total sections of rolling: " + larvaRoll.size() );
		
		for(ArrayList<FeaturesOfLarva> larvaSection : larvaRoll)
		{
			System.out.println("\n(rolling Detection) Section " + rollingSection + ":\n\n");
			showSysStatus("(rolling Detection) Frames of Section " + rollingSection +":");
			
			for(FeaturesOfLarva larva : larvaSection)
			{
				showStatus(larva.getFrameId()+",");
				System.out.println("larva_id: "+larva.getFrameId());
				
				if(larva.getFrameId() % 20 == 0)
					showStatus("\n");
			}
			
			rollingSection ++;
			
			// Analyzing rolling of larvae
			framesRoll = MathManager.getRollingFrame(larvaSection, prop, Integer.parseInt(prop.getRolling_frame()), perimeterLarva, dirImageDebug);

			for(FeaturesOfLarva larva : listLarva)
			{
				// loop through the framesRolling
				itr = framesRoll.iterator();
				   
			    while(itr.hasNext())
			    	// if the frame id is contained in framesRolling
			    	if( itr.next().equals(larva.getFrameId()))
			    		larva.setIsRolling(true);
			}
		}
		
		showStatus("\n");
		
		if(PropertyManager.getBool( prop.getOutput_animated_image() ) 
				|| PropertyManager.getBool( prop.getOutput_video() ) )
		{
			showSysStatus("[System] Generating annotation images ...");
			
			showStatus("\nGenerating frame:\n");
			
			DirectoryManager.createDirectory(dirImageAnimation );

			int frameId = 0;
			
			// Generating debug and animated images
			for(FeaturesOfLarva larva : listLarva)
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
				(new AnnotationManager(larva, imagePlusAllFull, new Point(30, 25))).annotateAll(prop);
				
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
		
		optionComponent.getBtnStart().setEnabled(true);
		optionComponent.getBtnLeft1().setEnabled(true);
		optionComponent.getBtnLeft2().setEnabled(true);
		optionComponent.getBtnLeft5().setEnabled(true);
		optionComponent.getBtnLeft4().setEnabled(true);
		optionComponent.getBtnDefault().setEnabled(true);
		optionComponent.getBtnUpdateSet().setEnabled(true);
		
		optionComponent.getPanelRight1().setEnabled(true);
		optionComponent.getPanelRight2().setEnabled(true);
		optionComponent.getPanelRight4().setEnabled(true);
		optionComponent.getPanelRight5().setEnabled(true);
		
		FileManager.deleteDir(new File(dirTemp));

		if(!PropertyManager.getBool( prop.getOutput_debug() ) )
		{
			FileManager.deleteDir(new File(dirImageDebug+"dBinary/"));
			
			FileManager.deleteDir(new File(dirImageDebug+"dCrop/"));
		}
		
		if(!PropertyManager.getBool( prop.getOutput_animated_image() ) )
		{
			FileManager.deleteDir(new File(dirImageDebug+"aAnnotation/"));
		}
		
		if(procNum == totalProcNum)
			JOptionPane.showMessageDialog(null, "The process has been completed! Please check the output files.");
		
	}
	
	public void start ()
	{
      System.out.println("Starting " +  threadName );
      if (thread == null)
      {
    	  thread = new Thread (this, threadName);
    	  thread.start ();
      }
	}
	
	private void showStatus(String status)
	{
		System.out.print(status);
		showSysStatusBar(status);
		ij.IJ.showStatus(status);
		optionComponent.getTextStatus().append(status);
		optionComponent.getTextStatus().setCaretPosition(optionComponent.getTextStatus().getDocument().getLength());
		optionComponent.getTextStatus().validate();
	}
	
	private void showSysStatus(String status)
	{
		dateSys = new Date();
		showSysStatusBar(status);
		showStatus("\n("+dateSysFormat.format(dateSys)+") "+status+"\n");
	}
	
	private void showSysStatusBar(String text)
	{
		text = "Status: " + text;
		optionComponent.getTextSysStatus().setText(text);
	}

}
