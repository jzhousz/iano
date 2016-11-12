package entities;

import java.awt.Point;
import java.util.ArrayList;
import ij.gui.Roi;
import manager.MathManager;

/**
* The class contains all information about the larva.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class Larva 
{
	private ArrayList<Point> endPoints = null; // the end points of the larva skeleton
	private Point centerPoint = null; // the center point of the larva skeleton
	private Point roiTopLeft = null; // the top left corner point of ROI on AVI frame
	private Larva larvaPrevious = null; // the larva on the previous ROI frame
	private double curl; // the curl of larva
	private boolean isValid = true; // is this larva valid
	private boolean isPreviousValid = true; // is the previous larva valid
	private double distanceEndPoint1 = 0.0; // the  distance end point 1 moved
	private double distanceEndPoint2 = 0.0; // the total distance end point 2 moved
	private int areaOf3LevelEndPoint1 = 0; // the 3 level neighbor pixels to end point 1
	private int areaOf3LevelEndPoint2 = 0; // the 3 level neighbor pixels to end point 2
	// whether it's end point 1 has more pixels of 3 level than end point 2
	// for the current ROI frame, true if the end point 1 is the head based on 
	// amount of pixels on N level. Otherwise, end point 2 is the head.
	private boolean isEndPoint1WinPixelLevel = false; 
	// for the current ROI frame, true if the end point 1 is the head based on 
	// distance the end point moved. Otherwise, end point 2 is the head.
	private boolean isEndPoint1WinDistanceMoved = false; 
	private double distanceCenterPoint = 0; // the distance the center point moved
	private double distanceSideways = 0; // the sideways distance moved
	// whether the larva moved forward in sideways direction (linear regression)
	private boolean isSidewaysForward = false; 
	private double isSidewaysEndPts = 0; // the sideways distance moved based on end points
	// the sideways distance moved in sideways direction (end points linear line)
	private double distanceSidewaysEndPts = 0; 
	// whether the larva moved forward in sideways direction (end points linear line)
	private boolean isSidewaysEndPtsForward = false; 
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
	private boolean isDistSidewaysPos = false; 
	// whether the larva bends to the right (positive) when head of the larva is up and tail is down
	private boolean isCurlPos = false;
	
	private double DistanceSidewaysAccumulate = 0.00; // the accumulated sideways distance
	private double DistanceSidewaysTotal = 0.00; // the total sideways distance
	// is the larva rolling in this frame
	private boolean isRolling = false; 
	// the number of curl sign.
	// e.g. the curl for frame 1~20 is +, then numberSign = 1
	// the curl for frame 21~30 is +, then numberSign = 21
	private int numberSign = 1;
	// whether 
//	private boolean isLessThanMinFrames = true;
	private double distanceSpeed = 0.00; 
	// the skeleton length
	private double lengthSkeleton = 0;
	// the angle between end points line and linear regression line
	private double angleEndPointsLinear = 0;
	// whether this larva need to track
	private boolean needTrack = false;
	// the center mass point
	private Point pointCenterMass = null;
	// the line goes through the center point of skeleton and the center mass point
	private LinearLine linearLineCenterMass = null;
	// the angle of end point 1  
	private double angleEndPt1 = 0; 
	// the angle of end point 2 
	private double angleEndPt2 = 0; 
	// is the frame needed to be record for the larva in the csv file?
	private boolean needRecord = false;
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
	
	// the relate frame start to roll
	private int frameStartRoll = 0;
	// number of rolls the larva rolls
	private double rolls = 0;
	// the total sideways distance for all rolling frames
	private double distSidewaysRollTotal = 0;
	// the total distance moved in rolling frames
	private double distSidewaysRollTotalMove = 0;
	// the body angle of the larva
	private double angleBody = 0;
	// the angle, center of skeleton-end point 1- end point 2
	private double angleCenterEnd1End2 = 0;
	// the angle, center of skeleton-end point 2- end point 1
	private double angleCenterEnd2End1 = 0;
	// the angle, average end point-center of skeleton-end point 1
	private double angleAvgCenterEnd1 = 0;
	// the angle, average end point-center of skeleton-end point 2
	private double angleAvgCenterEnd2 = 0;
	// the angle between reference line and the center point in frame N+1
	private double angleRefCenterN1 = 0;
	// whether the larva move to the right
	private boolean isMoveRight = false;
	// wheter the cluster of frames is moving forward. 
	// e.g., if we set the size of a cluster to 5 and if the larva in frame 5-15 (cluster 1-2) is
	// moving forward, isClusterForward is true for frame 5-15.
	private boolean isClusterForward = false;
	
	/**
	* The default constructor.
	* 
	*/
	public Larva()
	{
		endPoints = new ArrayList<Point>();
		centerPoint = new Point();
		roiTopLeft = new Point();
		curl = Double.NaN; //Curl of the fly larvae
		msgWarning = new ArrayList<String>();
		msgError = new ArrayList<String>();
	}
	
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

	public Point getRoiTopLeft() {
		return roiTopLeft;
	}

	public void setRoiTopLeft(Point roiTopLeft) {
		this.roiTopLeft = roiTopLeft;
	}

	public Larva getLarvaPrevious() {
		return larvaPrevious;
	}

	public void setLarvaPrevious(Larva featuresOfLarvaPrevious) {
		this.larvaPrevious = featuresOfLarvaPrevious;
	}

	public double getCurl() {
		return curl;
	}

	public void setCurl(double curl) {
		this.curl = curl;
	}

	public boolean getIsValid() {
		return isValid;
	}

	public void setIsValid(boolean isValid) {
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

	public boolean getIsEndPoint1WinPixelLevel() {
		return isEndPoint1WinPixelLevel;
	}

	public void setIsEndPoint1WinPixelLevel(boolean isEndPoint1WinPixelLevel) {
		this.isEndPoint1WinPixelLevel = isEndPoint1WinPixelLevel;
	}

	public boolean getIsEndPoint1WinDistanceMoved() {
		return isEndPoint1WinDistanceMoved;
	}

	public void setIsEndPoint1WinDistanceMoved(boolean isEndPoint1WinDistanceMoved) {
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

	public boolean getIsSidewaysForward() {
		return isSidewaysForward;
	}

	public void setIsSidewaysForward(boolean isSidewaysForward) {
		this.isSidewaysForward = isSidewaysForward;
	}

	public double getIsSidewaysEndPts() {
		return isSidewaysEndPts;
	}

	public void setIsSidewaysEndPts(double isSidewaysEndPts) {
		this.isSidewaysEndPts = isSidewaysEndPts;
	}

	public boolean getIsSidewaysEndPtsForward() {
		return isSidewaysEndPtsForward;
	}

	public void setIsSidewaysEndPtsForward(boolean isSidewaysEndPtsForward) {
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

	public boolean getIsDistSidewaysPos() {
		return isDistSidewaysPos;
	}

	public void setIsDistSidewaysPos(boolean isDistSidewaysPos) {
		this.isDistSidewaysPos = isDistSidewaysPos;
	}

	public boolean getIsCurlPos() {
		return isCurlPos;
	}

	public void setIsCurlPos(boolean isCurlPos) {
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

	public boolean getIsRolling() {
		return isRolling;
	}

	public void setIsRolling(boolean isRolling) {
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

	public boolean getNeedTrack() {
		return needTrack;
	}

	public void setNeedTrack(boolean needTrack) {
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

	public boolean getNeedRecord() {
		return needRecord;
	}

	public void setNeedRecord(boolean needRecord) {
		this.needRecord = needRecord;
	}

	public boolean getIsPreviousValid() {
		return isPreviousValid;
	}

	public void setIsPreviousValid(boolean isPreviousValid) {
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

	public int getFrameStartRoll() {
		return frameStartRoll;
	}

	public void setFrameStartRoll(int frameStartRoll) {
		this.frameStartRoll = frameStartRoll;
	}

	public double getRolls() {
		return rolls;
	}

	public void setRolls(double rolls) {
		this.rolls = rolls;
	}

	public double getDistSidewaysRollTotal() {
		return distSidewaysRollTotal;
	}

	public void setDistSidewaysRollTotal(double distSidewaysRollTotal) {
		this.distSidewaysRollTotal = distSidewaysRollTotal;
	}

	public double getDistSidewaysRollTotalMove() {
		return distSidewaysRollTotalMove;
	}

	public void setDistSidewaysRollTotalMove(double distSidewaysRollTotalMove) {
		this.distSidewaysRollTotalMove = distSidewaysRollTotalMove;
	}

	public double getAngleBody() {
		return angleBody;
	}

	public void setAngleBody(double angleBody) {
		this.angleBody = angleBody;
	}

	public double getAngleCenterEnd1End2() {
		return angleCenterEnd1End2;
	}

	public void setAngleCenterEnd1End2(double angleCenterEnd1End2) {
		this.angleCenterEnd1End2 = angleCenterEnd1End2;
	}

	public double getAngleCenterEnd2End1() {
		return angleCenterEnd2End1;
	}

	public void setAngleCenterEnd2End1(double angleCenterEnd2End1) {
		this.angleCenterEnd2End1 = angleCenterEnd2End1;
	}

	public double getAngleAvgCenterEnd1() {
		return angleAvgCenterEnd1;
	}

	public void setAngleAvgCenterEnd1(double angleAvgCenterEnd1) {
		this.angleAvgCenterEnd1 = angleAvgCenterEnd1;
	}

	public double getAngleAvgCenterEnd2() {
		return angleAvgCenterEnd2;
	}

	public void setAngleAvgCenterEnd2(double angleAvgCenterEnd2) {
		this.angleAvgCenterEnd2 = angleAvgCenterEnd2;
	}

	public double getAngleRefCenterN1() {
		return angleRefCenterN1;
	}

	public void setAngleRefCenterN1(double angleRefCenterN1) {
		this.angleRefCenterN1 = angleRefCenterN1;
	}

	public boolean getIsMoveRight() {
		return isMoveRight;
	}

	public void setIsMoveRight(boolean isMoveRight) {
		this.isMoveRight = isMoveRight;
	}

	public boolean getIsClusterForward()
	{
		return isClusterForward;
	}

	public void setIsClusterForward(boolean isClusterForward)
	{
		this.isClusterForward = isClusterForward;
	}
	
	public static Larva getLarvaByFrameId(ArrayList<Larva> listLarva, int frameId)
	{
		Larva larva = null;
		
		for(Larva lv : listLarva)
			if(lv.getFrameId() == frameId)
				larva = lv;
		
		return larva;
	}

}
