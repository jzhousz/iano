package entities;

import java.awt.Point;
import java.util.ArrayList;

import ij.ImagePlus;
import ij.gui.Roi;
import manager.MathManager;

public class FeaturesOfLarva {

	private ArrayList<Point> endPoints = null; // the end points of the larva skeleton
	private Point centerPoint = null; // the center point of the larva skeleton
//	private ImagePlus imagePlusCrop = null; // the cropped image plus based on ROI
//	private ImagePlus imagePlusBinary = null; // the binary image plus based on ROI
//	private ImagePlus imagePlusSkeleton = null; // the skeleton image plus based on ROI
//	private ImagePlus imagePlusEdge = null; // the edge image plus based on ROI
//	private ImagePlus imagePlusBigObj = null; // the biggest object in the image plus of ROI
	private Point roiTopLeft = null; // the top left corner point of ROI on AVI frame
	private FeaturesOfLarva featuresOfLarvaPrevious = null; // the larva on the previous ROI frame
	private double curl; // the curl of larva
	private Boolean isValid = true; // is this larva valid
	private Boolean isPreviousValid = true; // is the previous larva valid
	private double distanceEndPoint1 = 0.0; // the  distance end point 1 moved
	private double distanceEndPoint2 = 0.0; // the total distance end point 2 moved
	private int areaOf3LevelEndPoint1 = 0; // the 3 level neighbor pixels to end point 1
	private int areaOf3LevelEndPoint2 = 0; // the 3 level neighbor pixels to end point 2
	// whether it's end point 1 has more pixels of 3 level than end point 2
	// for the current ROI frame, true if the end point 1 is the head based on 
	// amount of pixels on N level. Otherwise, end point 2 is the head.
	private Boolean isEndPoint1WinPixelLevel = false; 
	// for the current ROI frame, true if the end point 1 is the head based on 
	// distance the end point moved. Otherwise, end point 2 is the head.
	private Boolean isEndPoint1WinDistanceMoved = false; 
	private double distanceCenterPoint = 0; // the distance the center point moved
	private double distanceSideways = 0; // the sideways distance moved
	// whether the larva moved forward in sideways direction (linear regression)
	private Boolean isSidewaysForward = false; 
	private double isSidewaysEndPts = 0; // the sideways distance moved based on end points
	// the sideways distance moved in sideways direction (end points linear line)
	private double distanceSidewaysEndPts = 0; 
	// whether the larva moved forward in sideways direction (end points linear line)
	private Boolean isSidewaysEndPtsForward = false; 
	private double distanceSidewaysShawn = 0; // the sideways distance moved based on Shawn's code
	private int frameId = 0; // the frame number on the AVI video
	
	// the linear regression, all points are closest to this line
	private LinearLine linearRegression = null; 
	// a linearRegression line passes through the center point
	private LinearLine linearLineParallel = null;
	// a linearLineParallel line with which it is perpendicular 
	private LinearLine linearLinePerpendicular = null;
	// a linear line which passes both end points
	private LinearLine linearLineEndPts = null;
	// a line which is parallel with linearLineEndPt and it passes the center point 
	private LinearLine linearLineEndPtsParallel = null;
	// a line which is Perpendicular with linearLineEndPt
	private LinearLine linearLineEndPtsPerp = null;
	// the area of the biggest object on binary image in binarization process
	private int area = 0; 
	// the interception point Shawn found out
	private Point interceptionPoint = null;
	private double distanceSidewaysSign = 0; // the sideways distance with sign, + or -
	private double curlSign = 0; // the curl of larva with sign, + or -
	// whether the larva moved to the right (positive) when head of the larva is up and tail is down
	private Boolean isDistSidewaysPos = false; 
	// whether the larva bends to the right (positive) when head of the larva is up and tail is down
	private Boolean isCurlPos = false;
	
	private double DistanceSidewaysAccumulate = 0.00; // the accumulated sideways distance
	private double DistanceSidewaysTotal = 0.00; // the total sideways distance
	// is the larva rolling in this frame
	private Boolean isRolling = false; 
	// the number of curl sign.
	// e.g. the curl for frame 1~20 is +, then numberSign = 1
	// the curl for frame 21~30 is +, then numberSign = 21
	private int numberSign = 1;
	// whether 
//	private Boolean isLessThanMinFrames = true;
	private double distanceSpeed = 0.00; 
	// the skeleton length
	private double lengthSkeleton = 0;
	// the angle between end points line and linear regression line
	private double angleEndPointsLinear = 0;
	// whether this larva need to track
	private Boolean needTrack = false;
	// the center mass point
	private Point pointCenterMass = null;
	// the line goes through the center point of skeleton and the center mass point
	private LinearLine linearLineCenterMass = null;
	// the angle of end point 1  
	private double angleEndPt1 = 0; 
	// the angle of end point 2 
	private double angleEndPt2 = 0; 
	// is the frame needed to be record for the larva in the csv file?
	private Boolean needRecord = false;
	// the warning message
	private ArrayList<String> msgWarning = null;
	// the error message
	private ArrayList<String> msgError = null;
	
	// the first quartile of the skeleton 
	private Point point1stQuartile = null;
	// the third quartile of the skeleton 
	private Point point3rdQuartile = null;
	
	// the line goes through the center point.
	// the 1st and 3rd quartiles used to calculate the slope.
	private LinearLine lineQuartileParallel = null;
	
	// the perpendicular line with lineQuartileParallel.
	// it goes through the center point.
	private LinearLine lineQuartilePerp = null;
	
	// the distance the center point moved based on quartile line
	private double distQuartile = 0; 
	
	// the list contains the points on the lineQuartilePerp and
	// going through the larva.
	private ArrayList<Point> pointsQuartile = null;
	
	// the diameter of the larva
	private double diameter = 0;
	// the average 5 median diameters
	private double avgDiameter = 0;
	
	// the sideways distance signed (begin) for a larva in a frame
	private double distSwySgnBgn = 0; 
	// the sideways distance with direction (begin) for a larva in a frame
	private double distSwyDirBgn = 0; 
	// the sideways distance signed (end) for a larva in a frame
	private double distSwySgnEnd = 0; 
	// the sideways distance with direction (end) for a larva in a frame
	private double distSwyDirEnd = 0;
	
	// the average area of larvae
	private int avgArea = 0;
	
	// the average area of larvae
	private int avgSkeletonLen = 0;
	
	private Roi roi = null; // the roi of the larva in the frame
	
	public FeaturesOfLarva()
	{
		endPoints = new ArrayList<Point>();
		centerPoint = new Point();
//		imagePlusCrop = new ImagePlus();
//		imagePlusBinary = new ImagePlus();
//		imagePlusSkeleton = new ImagePlus();
//		imagePlusEdge = new ImagePlus();
//		imagePlusBigObj = new ImagePlus();
		roiTopLeft = new Point();
		curl = Double.NaN; //Curl of the fly larvae
		msgWarning = new ArrayList<String>();
		msgError = new ArrayList<String>();
	}
	
//	public FeaturesOfLarva( FeaturesOfLarva featuresOfLarva )
//	{
////		this();
//		endPoints = featuresOfLarva.endPoints;
//		centerPoint = featuresOfLarva.centerPoint;
//		imagePlusCrop = featuresOfLarva.imagePlusCrop;
//		imagePlusBinary = featuresOfLarva.imagePlusBinary;
//		imagePlusSkeleton = featuresOfLarva.imagePlusSkeleton;
//		imagePlusEdge = featuresOfLarva.imagePlusEdge;
//		imagePlusBigObj = featuresOfLarva.imagePlusBigObj;
//		roiTopLeft = featuresOfLarva.roiTopLeft;
//		curl = featuresOfLarva.curl; //Curl of the fly larvae
//		featuresOfLarvaPrevious = featuresOfLarva.featuresOfLarvaPrevious;
//		isValid = featuresOfLarva.isValid;
//		distanceEndPoint1 = featuresOfLarva.distanceEndPoint1;
//		distanceEndPoint2 = featuresOfLarva.distanceEndPoint2;
//		areaOf3LevelEndPoint1 = featuresOfLarva.areaOf3LevelEndPoint1;
//		areaOf3LevelEndPoint2 = featuresOfLarva.areaOf3LevelEndPoint2;
//		isEndPoint1WinPixelLevel = featuresOfLarva.isEndPoint1WinPixelLevel;
//		isEndPoint1WinDistanceMoved = featuresOfLarva.isEndPoint1WinDistanceMoved;
//		distanceCenterPoint = featuresOfLarva.distanceCenterPoint;
//		distanceSideways = featuresOfLarva.distanceSideways;
//		frameId = featuresOfLarva.frameId;
//	}
	
	/**
	 * Duplicate the features of the larva and return a copy of the features of the larva. 
	 * 
	 * @param none
	 * @return the duplicated FeaturesOfLarva
	 */
//	public FeaturesOfLarva duplicate()
//	{
//		FeaturesOfLarva featuresOfLarvaCopy = new FeaturesOfLarva();
//		ArrayList<Point> endPointsCopy = new ArrayList<Point>();
//		
//		for(Point point : endPoints)
//			endPointsCopy.add( new Point(point.x, point.y) );
//		
//		featuresOfLarvaCopy.setEndPoints(endPointsCopy);
//		featuresOfLarvaCopy.setCenterPoint(new Point(centerPoint.x, centerPoint.y) );
//		featuresOfLarvaCopy.setImagePlusCrop(imagePlusCrop.duplicate());
//		featuresOfLarvaCopy.setImagePlusBinary(imagePlusBinary.duplicate());
//		featuresOfLarvaCopy.setImagePlusSkeleton(imagePlusSkeleton.duplicate());
//		featuresOfLarvaCopy.setImagePlusEdge(imagePlusEdge.duplicate());
//		featuresOfLarvaCopy.setImagePlusBigObj(imagePlusBigObj.duplicate());
//		featuresOfLarvaCopy.setRoiTopLeft(new Point(roiTopLeft.x, roiTopLeft.y) );
//		//featuresOfLarvaCopy.setFeaturesOfLarvaPrevious(featuresOfLarvaPrevious);
//		
//		featuresOfLarvaCopy.curl = curl; //Curl of the fly larvae
//		featuresOfLarvaCopy.featuresOfLarvaPrevious = featuresOfLarvaPrevious;
//		featuresOfLarvaCopy.isValid = isValid;
//		featuresOfLarvaCopy.distanceEndPoint1 = distanceEndPoint1;
//		featuresOfLarvaCopy.distanceEndPoint2 = distanceEndPoint2;
//		featuresOfLarvaCopy.areaOf3LevelEndPoint1 = areaOf3LevelEndPoint1;
//		featuresOfLarvaCopy.areaOf3LevelEndPoint2 = areaOf3LevelEndPoint2;
//		featuresOfLarvaCopy.isEndPoint1WinPixelLevel = isEndPoint1WinPixelLevel;
//		featuresOfLarvaCopy.isEndPoint1WinDistanceMoved = isEndPoint1WinDistanceMoved;
//		featuresOfLarvaCopy.distanceCenterPoint = distanceCenterPoint;
//		featuresOfLarvaCopy.distanceSideways = distanceSideways;
//		featuresOfLarvaCopy.frameId = frameId;
//		
//		return featuresOfLarvaCopy;
//	}
	
	/**
	 * Get the location of a point on AVI full frame instead of relative 
	 * location on ROI (Region of interest). 
	 * 
	 * @param point The point will be converted to the AVI full frame coordinate
	 * @return the converted point based on the AVI full frame coordinate
	 */
	public Point getPointOnFullFrame(Point point)
	{
		return new Point(roiTopLeft.x + point.x, roiTopLeft.y + point.y);
	}
	
	public Point getCenterPointOnFullFrame()
	{
		return getPointOnFullFrame(centerPoint);
	}
	
	public void setEndPoints(ArrayList<Point> endPoints)
	{
		this.endPoints = endPoints;
	}
	
	public ArrayList<Point> getEndPoints()
	{
		return endPoints;
	}

	/**
	 * Get the location of a point on AVI full frame instead of relative 
	 * location on ROI (Region of interest). 
	 * 
	 * @return the converted point array list based on the AVI full frame coordinate
	 */
	public ArrayList<Point> getEndPointsOnFullFrame()
	{
		ArrayList<Point> points = new ArrayList<Point>();
		
//		for(Point pt : endPoints)
		for(int i = 0; i < endPoints.size(); i++)
			points.add( getPointOnFullFrame( endPoints.get(i) ) );
		
		return points;
	}
	
	// ======================= getters and setters ===========================
	
	public Point getCenterPoint() {
		return centerPoint;
	}

	public void setCenterPoint(Point centerPoint) {
		this.centerPoint = centerPoint;
	}

//	public ImagePlus getImagePlusCrop() {
//		return imagePlusCrop;
//	}
//
//	public void setImagePlusCrop(ImagePlus imagePlusCrop) {
//		this.imagePlusCrop = imagePlusCrop;
//	}
//
//	public ImagePlus getImagePlusBinary() {
//		return imagePlusBinary;
//	}
//
//	public void setImagePlusBinary(ImagePlus imagePlusBinary) {
//		this.imagePlusBinary = imagePlusBinary;
//	}
//
//	public ImagePlus getImagePlusSkeleton() {
//		return imagePlusSkeleton;
//	}
//
//	public void setImagePlusSkeleton(ImagePlus imagePlusSkeleton) {
//		this.imagePlusSkeleton = imagePlusSkeleton;
//	}
//
//	public ImagePlus getImagePlusEdge() {
//		return imagePlusEdge;
//	}
//
//	public void setImagePlusEdge(ImagePlus imagePlusEdge) {
//		this.imagePlusEdge = imagePlusEdge;
//	}
//
//	public ImagePlus getImagePlusBigObj() {
//		return imagePlusBigObj;
//	}
//
//	public void setImagePlusBigObj(ImagePlus imagePlusBigObj) {
//		this.imagePlusBigObj = imagePlusBigObj;
//	}

	public Point getRoiTopLeft() {
		return roiTopLeft;
	}

	public void setRoiTopLeft(Point roiTopLeft) {
		this.roiTopLeft = roiTopLeft;
	}

	public FeaturesOfLarva getFeaturesOfLarvaPrevious() {
		return featuresOfLarvaPrevious;
	}

	public void setFeaturesOfLarvaPrevious(FeaturesOfLarva featuresOfLarvaPrevious) {
		this.featuresOfLarvaPrevious = featuresOfLarvaPrevious;
	}

	public double getCurl() {
		return curl;
	}

	public void setCurl(double curl) {
		this.curl = curl;
	}

	public Boolean getIsValid() {
		return isValid;
	}

	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}

	public int getAreaOf3LevelEndPoint1() {
		return areaOf3LevelEndPoint1;
	}

	public void setAreaOf3LevelEndPoint1(int areaOf3LevelEndPoint1) {
		this.areaOf3LevelEndPoint1 = areaOf3LevelEndPoint1;
	}

	public int getAreaOf3LevelEndPoint2() {
		return areaOf3LevelEndPoint2;
	}

	public void setAreaOf3LevelEndPoint2(int areaOf3LevelEndPoint2) {
		this.areaOf3LevelEndPoint2 = areaOf3LevelEndPoint2;
	}
	
	public double getDistanceEndPoint1() {
		return distanceEndPoint1;
	}

	public void setDistanceEndPoint1(double distanceEndPoint1) {
		this.distanceEndPoint1 = distanceEndPoint1;
	}

	public double getDistanceEndPoint2() {
		return distanceEndPoint2;
	}

	public void setDistanceEndPoint2(double distanceEndPoint2) {
		this.distanceEndPoint2 = distanceEndPoint2;
	}

	public Boolean getIsEndPoint1WinPixelLevel() {
		return isEndPoint1WinPixelLevel;
	}

	public void setIsEndPoint1WinPixelLevel(Boolean isEndPoint1WinPixelLevel) {
		this.isEndPoint1WinPixelLevel = isEndPoint1WinPixelLevel;
	}

	public Boolean getIsEndPoint1WinDistanceMoved() {
		return isEndPoint1WinDistanceMoved;
	}

	public void setIsEndPoint1WinDistanceMoved(Boolean isEndPoint1WinDistanceMoved) {
		this.isEndPoint1WinDistanceMoved = isEndPoint1WinDistanceMoved;
	}

	public double getDistanceCenterPoint() {
		return distanceCenterPoint;
	}

	public void setDistanceCenterPoint(double distanceCenterPoint) {
		this.distanceCenterPoint = distanceCenterPoint;
	}

	public double getDistanceSideways() {
		return distanceSideways;
	}

	public void setDistanceSideways(double distanceSideways) {
		this.distanceSideways = distanceSideways;
	}

	public int getFrameId() {
		return frameId;
	}

	public void setFrameId(int frameId) {
		this.frameId = frameId;
	}

	public double getDistanceSidewaysShawn() {
		return distanceSidewaysShawn;
	}

	public void setDistanceSidewaysShawn(double distanceSidewaysShawn) {
		this.distanceSidewaysShawn = distanceSidewaysShawn;
	}

	public LinearLine getLinearRegression() {
		return linearRegression;
	}

	public void setLinearRegression(LinearLine linearRegression) {
		this.linearRegression = linearRegression;
	}

	public entities.LinearLine getLinearLineParallel() {
		return linearLineParallel;
	}

	public void setLinearLineParallel(entities.LinearLine linearLineParallel) {
		this.linearLineParallel = linearLineParallel;
	}

	public entities.LinearLine getLinearLinePerpendicular() {
		return linearLinePerpendicular;
	}

	public void setLinearLinePerpendicular(entities.LinearLine linearLinePerpendicular) {
		this.linearLinePerpendicular = linearLinePerpendicular;
	}


	public int getArea() {
		return area;
	}

	public void setArea(int area) {
		this.area = area;
	}

	public double getDistanceSidewaysEndPts() {
		return distanceSidewaysEndPts;
	}

	public void setDistanceSidewaysEndPts(double distanceSidewaysEndPts) {
		this.distanceSidewaysEndPts = distanceSidewaysEndPts;
	}

	public entities.LinearLine getLinearLineEndPts() {
		return linearLineEndPts;
	}

	public void setLinearLineEndPts(entities.LinearLine linearLineEndPts) {
		this.linearLineEndPts = linearLineEndPts;
	}

	public entities.LinearLine getLinearLineEndPtsParallel() {
		return linearLineEndPtsParallel;
	}

	public void setLinearLineEndPtsParallel(entities.LinearLine linearLineEndPtsParallel) {
		this.linearLineEndPtsParallel = linearLineEndPtsParallel;
	}

	public entities.LinearLine getLinearLineEndPtsPerp() {
		return linearLineEndPtsPerp;
	}

	public void setLinearLineEndPtsPerp(entities.LinearLine linearLineEndPtsPerp) {
		this.linearLineEndPtsPerp = linearLineEndPtsPerp;
	}

	public Boolean getIsSidewaysForward() {
		return isSidewaysForward;
	}

	public void setIsSidewaysForward(Boolean isSidewaysForward) {
		this.isSidewaysForward = isSidewaysForward;
	}

	public double getIsSidewaysEndPts() {
		return isSidewaysEndPts;
	}

	public void setIsSidewaysEndPts(double isSidewaysEndPts) {
		this.isSidewaysEndPts = isSidewaysEndPts;
	}

	public Boolean getIsSidewaysEndPtsForward() {
		return isSidewaysEndPtsForward;
	}

	public void setIsSidewaysEndPtsForward(Boolean isSidewaysEndPtsForward) {
		this.isSidewaysEndPtsForward = isSidewaysEndPtsForward;
	}

	public Point getInterceptionPoint() {
		return interceptionPoint;
	}

	public void setInterceptionPoint(Point interceptionPoint) {
		this.interceptionPoint = interceptionPoint;
	}

	public double getDistanceSidewaysSign() {
		return distanceSidewaysSign;
	}

	public void setDistanceSidewaysSign(double distanceSidewaysSign) {
		this.distanceSidewaysSign = distanceSidewaysSign;
	}

	public double getCurlSign() {
		return curlSign;
	}

	public void setCurlSign(double curlSign) {
		this.curlSign = curlSign;
	}

	public Boolean getIsDistSidewaysPos() {
		return isDistSidewaysPos;
	}

	public void setIsDistSidewaysPos(Boolean isDistSidewaysPos) {
		this.isDistSidewaysPos = isDistSidewaysPos;
	}

	public Boolean getIsCurlPos() {
		return isCurlPos;
	}

	public void setIsCurlPos(Boolean isCurlPos) {
		this.isCurlPos = isCurlPos;
	}

	public double getDistanceSidewaysAccumulate() {
		return DistanceSidewaysAccumulate;
	}

	public void setDistanceSidewaysAccumulate(double distanceSidewaysAccumulate) {
		DistanceSidewaysAccumulate = distanceSidewaysAccumulate;
	}

	public double getDistanceSidewaysTotal() {
		return DistanceSidewaysTotal;
	}

	public void setDistanceSidewaysTotal(double distanceSidewaysTotal) {
		DistanceSidewaysTotal = distanceSidewaysTotal;
	}

	public Boolean getIsRolling() {
		return isRolling;
	}

	public void setIsRolling(Boolean isRolling) {
		this.isRolling = isRolling;
	}

	public int getNumberSign() {
		return numberSign;
	}

	public void setNumberSign(int numberSign) {
		this.numberSign = numberSign;
	}

	public double getDistanceSpeed() {
		return distanceSpeed;
	}

	public void setDistanceSpeed(double distanceSpeed) {
		this.distanceSpeed = distanceSpeed;
	}

	public double getLengthSkeleton() {
		return lengthSkeleton;
	}

	public void setLengthSkeleton(double lengthSkeleton) {
		this.lengthSkeleton = lengthSkeleton;
	}

	public double getAngleEndPointsLinear() {
		return angleEndPointsLinear;
	}

	public void setAngleEndPointsLinear(double angleEndPointsLinear) {
		this.angleEndPointsLinear = angleEndPointsLinear;
	}

	public Boolean getNeedTrack() {
		return needTrack;
	}

	public void setNeedTrack(Boolean needTrack) {
		this.needTrack = needTrack;
	}

	public Point getPointCenterMass() {
		return pointCenterMass;
	}

	public void setPointCenterMass(Point pointMass) {
		this.pointCenterMass = pointMass;
	}

	public LinearLine getLinearLineCenterMass() {
		return linearLineCenterMass;
	}

	public void setLinearLineCenterMass(LinearLine linearLineCenterMass) {
		this.linearLineCenterMass = linearLineCenterMass;
	}

	public double getAngleEndPt1() {
		return angleEndPt1;
	}

	public void setAngleEndPt1(double angleEndPt1) {
		this.angleEndPt1 = angleEndPt1;
	}

	public double getAngleEndPt2() {
		return angleEndPt2;
	}

	public void setAngleEndPt2(double angleEndPt2) {
		this.angleEndPt2 = angleEndPt2;
	}

	public Boolean getNeedRecord() {
		return needRecord;
	}

	public void setNeedRecord(Boolean needRecord) {
		this.needRecord = needRecord;
	}

	public Boolean getIsPreviousValid() {
		return isPreviousValid;
	}

	public void setIsPreviousValid(Boolean isPreviousValid) {
		this.isPreviousValid = isPreviousValid;
	}

	public ArrayList<String> getMsgWarning() {
		return msgWarning;
	}

	public void setMsgWarning(ArrayList<String> msgWarning) {
		this.msgWarning = msgWarning;
	}

	public ArrayList<String> getMsgError() {
		return msgError;
	}

	public void setMsgError(ArrayList<String> msgError) {
		this.msgError = msgError;
	}

	public Point getPoint1stQuartile() {
		return point1stQuartile;
	}

	public void setPoint1stQuartile(Point point1stQuartile) {
		this.point1stQuartile = point1stQuartile;
	}

	public Point getPoint3rdQuartile() {
		return point3rdQuartile;
	}

	public void setPoint3rdQuartile(Point point3rdQuartile) {
		this.point3rdQuartile = point3rdQuartile;
	}

	public LinearLine getLineQuartileParallel() {
		return lineQuartileParallel;
	}

	public void setLineQuartileParallel(LinearLine lineQuartileParallel) {
		this.lineQuartileParallel = lineQuartileParallel;
	}

	public LinearLine getLineQuartilePerp() {
		return lineQuartilePerp;
	}

	public void setLineQuartilePerp(LinearLine lineQuartilePerp) {
		this.lineQuartilePerp = lineQuartilePerp;
	}

	public double getDistQuartile() {
		return distQuartile;
	}

	public void setDistQuartile(double distQuartile) {
		this.distQuartile = distQuartile;
	}

	public ArrayList<Point> getPointsQuartile() {
		return pointsQuartile;
	}

	public void setPointsQuartile(ArrayList<Point> pointsQuartile) {
		this.pointsQuartile = pointsQuartile;
	}

	public double getDiameter() {
		return diameter;
	}

	public void setDiameter(double diameter) {
		this.diameter = diameter;
	}

	public double getPerimeter() {
		return MathManager.get2DecimalPoints(diameter * Math.PI);
	}

	public double getAvgDiameter() {
		return avgDiameter;
	}

	public void setAvgDiameter(double avgDiameter) {
		this.avgDiameter = avgDiameter;
	}
	
	public double getAvgPerimeter() {
		return MathManager.get2DecimalPoints(avgDiameter * Math.PI);
	}

	public double getDistSwySgnBgn() {
		return distSwySgnBgn;
	}

	public void setDistSwySgnBgn(double distSwySgnBgn) {
		this.distSwySgnBgn = distSwySgnBgn;
	}

	public double getDistSwyDirBgn() {
		return distSwyDirBgn;
	}

	public void setDistSwyDirBgn(double distSwyDirBgn) {
		this.distSwyDirBgn = distSwyDirBgn;
	}

	public double getDistSwySgnEnd() {
		return distSwySgnEnd;
	}

	public void setDistSwySgnEnd(double distSwySgnEnd) {
		this.distSwySgnEnd = distSwySgnEnd;
	}

	public double getDistSwyDirEnd() {
		return distSwyDirEnd;
	}

	public void setDistSwyDirEnd(double distSwyDirEnd) {
		this.distSwyDirEnd = distSwyDirEnd;
	}

	public int getAvgArea() {
		return avgArea;
	}

	public void setAvgArea(int avgArea) {
		this.avgArea = avgArea;
	}

	public int getAvgSkeletonLen() {
		return avgSkeletonLen;
	}

	public void setAvgSkeletonLen(int avgSkeletonLen) {
		this.avgSkeletonLen = avgSkeletonLen;
	}

	public Roi getRoi() {
		return roi;
	}

	public void setRoi(Roi roi) {
		this.roi = roi;
	}
}
