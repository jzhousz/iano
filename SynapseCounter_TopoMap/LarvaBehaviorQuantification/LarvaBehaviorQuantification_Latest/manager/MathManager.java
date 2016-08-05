package manager;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import javax.swing.JTextArea;
import entities.Larva;
import entities.LinearLine;
import file.CSVWriter;
import ij.ImagePlus;

/**
* The MathManager class contains all the static methods used
* to do math calculation.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class MathManager
{
	/**
	* This method is used to convert an angle to an angle between -180 (exclusive)
	* and 180 (inclusive).
	* 
	* @param angle An input angle
	* @return The angle between -180 (exclusive) and 180 (inclusive).
	*/
	public static double calcSmallAngle( double angle )
	{
		double angleSmall = angle;
		
		if(angle > 180)
			angleSmall = 180 - angle;
		
		if(angle <= -180)
			angleSmall = 360 + angle;
		
		return get2DecimalPoints( angleSmall );
	}
	
	/**
	* Calculate the diameter of the larva.
	* @param pointsDiameter An ArrayList contains two points used to calculate the diameter of the larva.
	* @param centerSkeletonLarva The center of the skeleton of the larva.
	* @return The diameter of the larva.
	*/
	public static double calcDiameter( ArrayList<Point> pointsDiameter, Point centerSkeletonLarva )
	{
		double distQuartileLargest = 0;
		double distQuartile = 0;
		Point pointQuartile1 = null;
		
		// find the point with longest distance to centerSkeletonLarva
		for(Point pt : pointsDiameter)
		{
			distQuartile = MathManager.getDistance(pt, centerSkeletonLarva );
			
			if(distQuartile >= distQuartileLargest)
			{
				pointQuartile1 = pt;
				distQuartileLargest = distQuartile;
			}
		}
		
		// find the larva diameter
		for(Point pt : pointsDiameter)
		{
			distQuartile = MathManager.getDistance(pt, pointQuartile1);
			
			if(distQuartile >= distQuartileLargest)
			{
				distQuartileLargest = distQuartile;
			}
		}
		
		return distQuartileLargest;
	}
	
	/**
	* Calculate 1st and 3rd quartile point.
	* @param imagePlusEdge The edge image plus of the larva.
	* @param larva The larva.
	* @return The 1st and 3rd quartile point.
	*/
	public static ArrayList<Point> calcQuartilePoints( ImagePlus imagePlusEdge, Larva larva )
	{
		// get the line L2 that is parallel with L1. L1 is perpendicular with lineQuartileParallel.
		// L2 also goes through the center point.
		LinearLine lineQuartilePerpRoi = MathManager.getParallelLine(larva.getLineQuartilePerp().getBeta1(), larva.getCenterPoint());
		
		ArrayList<Point> pointsDiameter = new ArrayList<Point>();
		
		Point pointQuartile1 = null;
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
		
		return pointsDiameter;
	}
	
	/**
	* Calculate the center mass of the larva.
	* @param imagePlusSkeleton The skeleton image plus of the larva.
	* @return The center mass.
	*/
	public static Point calcCenterMass( ImagePlus imagePlusSkeleton )
	{
		int numPixel = 0;
		int xSum = 0;
		int ySum = 0;

		// find the sum of x- and y-axis
		for (int y = 0; y < imagePlusSkeleton.getHeight(); y++)
			for (int x = 0; x < imagePlusSkeleton.getWidth(); x++) {
				if(imagePlusSkeleton.getProcessor().getPixel(x, y) > 128)
				{
					xSum += x;
					ySum += y;
					numPixel++;
				}
			}
		
		int xMass = xSum / numPixel;
		int yMass = ySum / numPixel;
		
		return new Point(xMass, yMass);
	}
	
	/**
	 * calculate minimal and maximal larva skeleton.
	 * 
	 * @param avgSkeletonLen The length of the skeleton of the larva.
	 * @param prop The PropertyManager.
	 * @return ArrayList<Integer> containing the minimal and maximal larva skeleton respectively.
	 */
	public static ArrayList<Integer> calcMinMaxLarvaSkeleton( int avgSkeletonLen, PropertyManager prop )
	{
		int maxSkeletonLarva = 0;
		int minSkeletonLarva = 0;
		
		// +++++++++++ move the below to the controller
		if( PropertyManager.getBool( prop.getAuto_check_skeleton() ) )
		{
			// use 1.4 as the threshold for maximal skeleton
			maxSkeletonLarva = (int) (avgSkeletonLen * 1.4);
			// use 0.6 as the threshold for minimal skeleton
			minSkeletonLarva = (int) (avgSkeletonLen * 0.6);
			
			// +++++++++++ move the below to the controller
			prop.setMax_skeleton(Integer.toString( maxSkeletonLarva ));
			// +++++++++++ move the below to the controller
			prop.setMin_skeleton(Integer.toString( minSkeletonLarva ));
		}else{
			maxSkeletonLarva = Integer.parseInt( prop.getMax_skeleton() );
			minSkeletonLarva = Integer.parseInt( prop.getMin_skeleton() );
		}
		
		ArrayList<Integer> larvaSkeletons = new ArrayList<Integer>();
		larvaSkeletons.add(minSkeletonLarva);
		larvaSkeletons.add(maxSkeletonLarva);
		
		return larvaSkeletons;
	}
	
	/**
	 * calculate minimal and maximal larva size.
	 * 
	 * @param avgArea The area larva pixel area.
	 * @param prop The PropertyManager.
	 * @return ArrayList<Integer> containing the minimal and maximal larva size respectively.
	 */
	public static ArrayList<Integer> calcMinMaxLarvaSize( int avgArea, PropertyManager prop )
	{
		int maxSizeLarva = 0;
		int minSizeLarva = 0;
		
		// +++++++++++ move the below to the controller
		if( PropertyManager.getBool( prop.getAuto_check_size() ) )
		{
			maxSizeLarva = (int) (avgArea * 1.4);
			minSizeLarva = (int) (avgArea * 0.6);
			
			// +++++++++++ move the below to the controller
			prop.setMax_size(Integer.toString( maxSizeLarva ));
			// +++++++++++ move the below to the controller
			prop.setMin_size(Integer.toString( minSizeLarva ));
		}else{
			maxSizeLarva = Integer.parseInt( prop.getMax_size() );
			minSizeLarva = Integer.parseInt( prop.getMin_size() );
		}
		
		ArrayList<Integer> larvaSizes = new ArrayList<Integer>();
		larvaSizes.add(minSizeLarva);
		larvaSizes.add(maxSizeLarva);
		
		return larvaSizes;
	}
	
	/**
	 * calculate the rolling sections from all frames.
	 * 
	 * @param listLarva The ArrayList<Larva> containing all the larvae.
	 * @return ArrayList< ArrayList<Larva> > containing all rolling sections.
	 */
	public static ArrayList< ArrayList<Larva> > calcRollingSections( ArrayList<Larva> listLarva )
	{
		// the list containing the larvae that ware needed to be recorded.
		// the blue frame in Optogenetic video and red frame in Chrimson video
		ArrayList<Larva> listLarvaRecord = new ArrayList<Larva>();
		ArrayList< ArrayList<Larva> > larvaRoll = new ArrayList< ArrayList<Larva> >();
		ArrayList<Larva> listLarvaRoll = null;
		boolean isNeedStart1 = true;
		
		for(Larva larva : listLarva)
		{
			// check only the frames needed to be recorded
			if(larva.getNeedRecord())
			{
				listLarvaRecord.add(larva);
				
				if(isNeedStart1)
				{
					isNeedStart1 = false;
					listLarvaRoll = new ArrayList<Larva>();
					larvaRoll.add(listLarvaRoll);
				}
				
				listLarvaRoll.add(larva);
			}else{
				isNeedStart1 = true;
			}
			
		}
		
		return larvaRoll;
	}
	
	/**
	 * get average diameter, perimeter, and skeleton.
	 * 
	 * @param listLarva The ArrayList<Larva> containing all the larvae.
	 * @return Average Diameter, Perimeter, Skeleton, and Area respectively.
	 */
	public static ArrayList<Double> getAvgDiameterPerimeterSkeletonArea( ArrayList<Larva> listLarva )
	{
		// the list contains all the larvae's diameters for all the frames
		// that needed to be recorded.
		ArrayList<Double> diameters = new ArrayList<Double>();
		// the list contains all the larvae's pixel area for all the frames
		// that needed to be recorded.
		ArrayList<Integer> areas = new ArrayList<Integer>();
		ArrayList<Double> skeletonLens = new ArrayList<Double>();
		
		for(Larva larva : listLarva)
		{
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
		
		double avgArea = 0;
		
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
		
		double avgSkeletonLen = 0;
		
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
		
		ArrayList<Double> avgs = new ArrayList<Double>();
		
		avgs.add(avgDiameter);
		avgs.add(avgPerimeter);
		avgs.add(avgSkeletonLen);
		avgs.add(avgArea);
		
		return avgs;
	}
	
	/**
	 * find the interception point
	 * 
	 * @param line1 A linear line.
	 * @param line2 Another linear line.
	 * @return The interception point when both lines intercept.
	 */
	public static Point2D getInterception( LinearLine line1, LinearLine line2 )
	{
//		m1*x+b1 = m2*x+b2    
//		m1*x-m2*x = b2 - b2    
//		x(m1-m2) = (b2-b1)    
//		x = (b2-b1) / (m1-m2)
//		y = m1 * [(b2-b1) / (m1-m2)] + b1
		
		double x, y;
		
		if( Double.isNaN(line1.getBeta1()) )
		{
			x = line1.getBeta0();
			y = line2.getBeta1() * line1.getBeta0() + line2.getBeta0();
		}
		
		if( Double.isNaN(line2.getBeta1()) )
		{
			x = line2.getBeta0();
			y = line1.getBeta1() * line2.getBeta0() + line1.getBeta0();
		}
		
		// if two lines are parallel
		if(line1.getBeta1() == line2.getBeta1())
		{
			return null;
		// if two lines are Not parallel
		}else{
			x = ( line2.getBeta0() - line1.getBeta0() ) / ( line1.getBeta1() - line2.getBeta1() );
		    y = line1.getBeta1() * x + line1.getBeta0();
		}
		
	    x = MathManager.get2DecimalPoints( x );
	    y = MathManager.get2DecimalPoints( y );
		
		
	    return new Point2D.Double( x, y );
	}
	
	/**
	 * check whether the string contains a double number.
	 * 
	 * @param input The string to be checked.
	 * @return true if the string contains a double number.
	 */
	public static boolean isDouble( String input )
	{
	   try
	   {
	      Double.parseDouble( input );
	      return true;
	   }
	   catch( Exception e )
	   {
	      return false;
	   }
	}
	
	/**
	 * check whether the string contains a integer number.
	 * 
	 * @param input The string to be checked.
	 * @return true if the string contains a integer number.
	 */
	public static boolean isInteger( String input )
	{
	   try
	   {
	      Integer.parseInt( input );
	      return true;
	   }
	   catch( Exception e )
	   {
	      return false;
	   }
	}
	
	// Shwan's method
	/**
	 * Get the curl of the larva. Written by Shwan.
	 * 
	 * @param pointCenter The center of the skeleton of the larva.
	 * @param pointHead The head point of the larva.
	 * @param pointTail The tail point of the larva.
	 * @return The angle between line L1 and L2. L1 passes pointHead and pointCenter.
	 *         L2 passes pointTail and pointCenter.
	 */
	public static double getCurl( Point pointCenter, Point pointHead, Point pointTail )
	{
		double headRelativeX = pointHead.x - pointCenter.x,
				headRelativeY = pointHead.y - pointCenter.y;
		
		double tailRelativeX = pointTail.x - pointCenter.x,
				tailRelativeY = pointTail.y - pointCenter.y;
		
		double angle = -(180/Math.PI)* Math.atan2(headRelativeX*tailRelativeY-headRelativeY*tailRelativeX, headRelativeX*tailRelativeX + headRelativeY*tailRelativeY);
		
		if (angle < 0) angle = -1 * angle;  //to make sure the angle is between [0, 180]

		return Math.round( angle * 100.0 ) / 100.0; // round to 2 decimal points
	}
	
	/**
	 * Get the signed distance.
	 * 
	 * @param sign The sign of the signed sideways speed.
	 * @param swyDist The distance of the signed sideways speed.
	 * @return The signed distance.
	 */
	public static double getSignedDist( boolean sign, double swyDist ) 
	{
		if(sign)
		{
			return swyDist;
		}else
		{
			return -1 * swyDist;
		}
	}
	
	/**
	 * Fix invalid larva in frames.
	 * 
	 * @param listLarva The larva in all frames.
	 * @param textStatus The text area showing status on GUI.
	 * @return None.
	 */
	public static void fixInvalidLarva( ArrayList<Larva> listLarva, JTextArea textStatus ) 
	{
		Larva larva = null; // the current larva
		Larva larvaScan = null; // the scanning larva
		int i = 0; // the current larva
		int j = 0; // the scanning larva
		int indexFound = 0; // the index of the found larva
		// is the scanning process done for the current larva?
		boolean isDone = false; 
		// the linear regression line for the previous larva
		LinearLine line = null; 
		// the linear line gose through the center point of the previous larva 
		// on the full frame
		LinearLine lineParallel = null; 
		// the side way distance
		double distSideways = 0;
		// the average of the sideways distance
		double distSidewaysAvg = 0;
		
		while ( i < listLarva.size() )
		{
			larva = listLarva.get(i);
			
			// if the larva is not valid
			if(!larva.getIsValid())
			{
				// the scanning larva index
				j = i + 1; // set the index to start scanning
				
				isDone = false;
				
				// loop until find the valid larva 
				while(j < listLarva.size() && !isDone )
				{
					larvaScan = listLarva.get(j);
					
					if( larvaScan.getIsValid() )
					{
						indexFound = j;
						isDone = true;
					}
					
					j++;
				}

				// if indexFound is less than or equal to the last index of 
				// listLarva
				// and isDone is true, which means the next valid larva has
				// been found
				if(indexFound <= listLarva.size() - 1 && isDone)
				{
					line = larva.getLarvaPrevious().getLinearLineEndPts();
					lineParallel = MathManager.getParallelLine(line.getBeta1(), larva.getLarvaPrevious().getCenterPointOnFullFrame());
					
					distSideways = MathManager.getNearestDistance(lineParallel, listLarva.get(indexFound).getCenterPointOnFullFrame());
					
					distSidewaysAvg = MathManager.get2DecimalPoints( distSideways / (indexFound - i) );
					
					for(int k = i; k <= indexFound; k++)
					{
						listLarva.get(k).setDistanceSidewaysEndPts(distSidewaysAvg);
						listLarva.get(k).setIsSidewaysEndPtsForward(larva.getIsSidewaysEndPtsForward());
						listLarva.get(k).setIsDistSidewaysPos(larva.getIsDistSidewaysPos());
						
						HelperManager.showSysStatus(textStatus, "[fixInvalidLarva()] Set frame:"+listLarva.get(k).getFrameId() +", swyDistEnd:"+distSidewaysAvg);
					}
					
					// fixed larva with index i~j. set the pointer to j+1
					i = indexFound + 1;
					
				// if indexFound is greater than the last index of listLarva
				// or isDone is false, which means the next valid larva has not
				// been found
				}else{
					// set the checking larva to the index that is equal to 
					// listLarva size to stop this method
					i = listLarva.size(); 
				}

			// if the larva is valid
			}else{
				i++;
			}

		}
	}
	
	/**
	 * Get all rolling frames.
	 * 
	 * @param listLarva The larva in all frames.
	 * @param prop The property file entity.
	 * @param frameRange How many frames checked for detection of rolling.
	 * @param distanceThreshold The distance used for the detection.
	 * @param filePrefix The file name with which the rolling information will be saved.
	 * @return The list of the rolling frames.
	 */
	public static LinkedHashSet<Integer> getRollingFrame(
			ArrayList<Larva> listLarva, PropertyManager prop,
			int frameRange, double distanceThreshold, String filePrefix) 
	{
		// the sideways distance signed (begin) for a larva in a frame
		double distSwySgnBgn = 0; 
		// the sideways distance with direction (begin) for a larva in a frame
		double distSwyDirBgn = 0; 
		// the sideways distance signed (end) for a larva in a frame
		double distSwySgnEnd = 0; 
		// the sideways distance with direction (end) for a larva in a frame
		double distSwyDirEnd = 0; 
		// the set holding the the sideways distance signed
		LinkedHashSet<Integer> setDistSwySgn = new LinkedHashSet<Integer>();
		// the set holding the the sideways distance with direction
		LinkedHashSet<Integer> setDistSwyDir = new LinkedHashSet<Integer>();
		Larva larvaScan = null;
		
		Larva larva = null;
		// the signed sideways distance
		double distSwySgn = 0;
		// the directed sideways distance
		double distSwyDir = 0;

		CSVWriter csvWriter = null;
		
		Map<Integer,Integer> frameIds = new HashMap<Integer,Integer>();
				
		System.out.println("listFeatures.size():" + listLarva.size());
		
		if(prop.getOutput_complete_csv().equals("true"))
		{
			csvWriter = new CSVWriter(filePrefix+"roll_info.csv");
			csvWriter.writeln("FrameId,distSwy,isSigned,isDirected,distSwySgn,distSwyDir,distSwyEndSgnBgn,distSwyEndSgnEnd,distSwyEndDirBgn,distSwyEndDirEnd");
		}
		
		for(int frameGlobalIndex = 0; frameGlobalIndex < listLarva.size(); frameGlobalIndex++)
		{
			frameIds.put(listLarva.get(frameGlobalIndex).getFrameId(), frameGlobalIndex);
		}
		
		for(int frameGlobalIndex = 0; frameGlobalIndex < listLarva.size(); frameGlobalIndex++)
		{
			larva = listLarva.get(frameGlobalIndex);
			distSwySgnBgn = 0; 
			distSwyDirBgn = 0; 
			
			System.out.println("[Debug] frameGlobalIndex: "+larva.getFrameId());
			
			// if there are frameRange (number) frames after the current frame
			if( larva.getFrameId() + frameRange < listLarva.get(listLarva.size()-1).getFrameId() )
			{
				for(int i = larva.getFrameId(); i < larva.getFrameId() + frameRange; i++)
				{
					System.out.println("[Debug] index(frameGlobalIndex): "+frameGlobalIndex);
					System.out.println("[Debug] index(i): "+i);
					System.out.println("[Debug] frameIds.get(i): "+frameIds.get(i));
					System.out.println("[Debug] listLarva.get(frameIds.get(i)): "+listLarva.get(frameIds.get(i)));
					larvaScan = listLarva.get(frameIds.get(i));
					
					System.out.println("[Debug] index(distSwyBgn): "+larvaScan.getFrameId());
					
					// if the larva is valid and the previous larva
					// of this larva is valid
					if(larvaScan.getIsPreviousValid() && larvaScan.getIsValid())
					{
						distSwySgnBgn += getSignedDist( larvaScan.getIsSidewaysEndPtsForward(), 
								larvaScan.getDistanceSidewaysEndPts() ) ;
						
						distSwyDirBgn += getSignedDist( larvaScan.getIsDistSidewaysPos(), 
								larvaScan.getDistanceSidewaysEndPts() ) ;
					}
				}
				
			}
			
			distSwySgnEnd = 0; 
			distSwyDirEnd = 0; 
			
			// if there are frameRange frames before the current frame
			if( larva.getFrameId() - frameRange >= listLarva.get(0).getFrameId() )
			{
				for(int i = larva.getFrameId(); i > larva.getFrameId() - frameRange; i--)
				{
					larvaScan = listLarva.get(frameIds.get(i));

					System.out.println("[Debug] index(distSwyEnd): "+larvaScan.getFrameId());
					
					// if the larva is valid and the previous larva
					// of this larva is valid
					if(larvaScan.getIsPreviousValid() && larvaScan.getIsValid())
					{
						distSwySgnEnd += getSignedDist( larvaScan.getIsSidewaysEndPtsForward(), 
								larvaScan.getDistanceSidewaysEndPts() ) ;
						
						distSwyDirEnd += getSignedDist( larvaScan.getIsDistSidewaysPos(), 
								larvaScan.getDistanceSidewaysEndPts() ) ;
					}
				}
				
			}
			
			distSwyDirBgn = MathManager.get2DecimalPoints(distSwyDirBgn);
			distSwySgnBgn = MathManager.get2DecimalPoints(distSwySgnBgn);
			distSwyDirEnd = MathManager.get2DecimalPoints(distSwyDirEnd);
			distSwySgnEnd = MathManager.get2DecimalPoints(distSwySgnEnd);
			
			if(Math.abs(distSwySgnBgn) >= distanceThreshold)
				setDistSwySgn.add(larva.getFrameId());
			
			if(Math.abs(distSwyDirBgn) >= distanceThreshold)
				setDistSwyDir.add(larva.getFrameId());
			
			if(Math.abs(distSwySgnEnd) >= distanceThreshold)
				setDistSwySgn.add(larva.getFrameId());
			
			if(Math.abs(distSwyDirEnd) >= distanceThreshold)
				setDistSwyDir.add(larva.getFrameId());
			
			listLarva.get(frameGlobalIndex).setDistSwyDirBgn(distSwyDirBgn);
			listLarva.get(frameGlobalIndex).setDistSwySgnBgn(distSwySgnBgn);
			listLarva.get(frameGlobalIndex).setDistSwyDirEnd(distSwyDirEnd);
			listLarva.get(frameGlobalIndex).setDistSwySgnEnd(distSwySgnEnd);
			
			if(prop.getOutput_complete_csv().equals("true"))
			{
				distSwySgn = getSignedDist( larva.getIsSidewaysEndPtsForward(), 
						larva.getDistanceSidewaysEndPts() ) ;
				
				distSwyDir = getSignedDist( larva.getIsDistSidewaysPos(), 
						larva.getDistanceSidewaysEndPts() ) ;
				
				csvWriter.writeln(
						larva.getFrameId() + "," +
						larva.getDistanceSidewaysEndPts() + "," +
						larva.getIsSidewaysEndPtsForward() + "," +
						larva.getIsDistSidewaysPos() + "," +	
						Double.toString(distSwySgn) + "," +
						Double.toString(distSwyDir) + "," +
						Double.toString(distSwySgnBgn) + "," +
						Double.toString(distSwySgnEnd) + "," +
						Double.toString(distSwyDirBgn) + "," +
						Double.toString(distSwyDirEnd)
						);
			}
		}
		
		// retain only common elements from the two sets
		setDistSwySgn.retainAll(setDistSwyDir);
					
		return setDistSwySgn;
	}
	
	/**
	 * Add two points.
	 * 
	 * @param point1 A point to be added.
	 * @param point2 Another point to be added.
	 * @return The result point as two points were added.
	 */
	public static Point addPoints(Point point1, Point point2) 
	{
		return new Point(point1.x + point2.x, point1.y + point2.y);
	}
	
	/**
	 * Get the angle between L1 and L2. L1 passes point2 and center.
	 * L1 passes point1 and center.
	 * @param center The center point.
	 * @param point1 Point 1.
	 * @param point2 Point 2.
	 * @return The angle.
	 */
	public static double getAngleBetween(Point center, Point point1, Point point2) 
	{
		double angle = Math.toDegrees( Math.atan2(point2.x - center.x, point2.y - center.y)-
	                        Math.atan2(point1.x - center.x, point1.y - center.y) );
	  
		return get2DecimalPoints(angle);
	}
	
	/**
	 * Get degree from tangent.
	 * @param tangent The tangent.
	 * @return The degree.
	 */
	public static double getDegreeFromTangent(double tangent )
	{
		double degree = Math.toDegrees(Math.atan(tangent));
		return get2DecimalPoints(degree);
	}
	
	/**
	 * Get angle between 2 lines.
	 * @param linearLine1 The linearLine 1.
	 * @param linearLine2 The linearLine 2.
	 * @return The angle.
	 */
	public static double getAngle(LinearLine linearLine1, LinearLine linearLine2 )
	{
		// formula from: http://planetmath.org/anglebetweentwolines
		
		double angle = 0;
			
		if( Double.isNaN(linearLine1.getBeta1()) )
		{
			angle = Math.abs(1/linearLine2.getBeta1());
			return getDegreeFromTangent(angle);
		}else if( Double.isNaN(linearLine2.getBeta1()))
		{
			angle = Math.abs(1/linearLine1.getBeta1());
			return getDegreeFromTangent(angle);
		}
		
		if(linearLine1.getBeta1() * linearLine2.getBeta1() == -1)
		{
			angle = 90;
			return angle;
		}
		
		angle = Math.abs( (linearLine1.getBeta1() - linearLine2.getBeta1()) / 
				(1 + linearLine1.getBeta1() * linearLine2.getBeta1() ));
		
		return getDegreeFromTangent(angle);
		
	}
	
	/**
	 * Get the angle between reference line and L1. L1 passes pointCenterN0 and pointCenterN1.
	 * @param linearLine The reference line in frame N
	 * @param pointEnd1N0 The end point 1 in frame N.
	 * @param pointCenterN0 The center point in frame N.
	 * @param pointCenterN1 The center point in frame N+1
	 * @return The angle between reference line and L1. L1 passes pointCenterN0 and pointCenterN1.
	 */
	public static double calcReferenceAngle(LinearLine linearLine, Point pointEnd1N0, Point pointCenterN0, Point pointCenterN1 )
	{
		LinearLine linePerp = getPerpendicularLine(linearLine, pointEnd1N0);
		Point2D point2dOnLineN0 = getInterception(linearLine, linePerp);
		Point pointOnLineN0 = new Point( (int) Math.round(point2dOnLineN0.getX()) 
				, (int) Math.round(point2dOnLineN0.getY()) );
		
		double angle = getAngleBetween(pointCenterN0, pointCenterN1, pointOnLineN0 );
		
		if(angle > 180)
			angle = 180 - angle;
		
		if(angle < -180)
			angle = 360 + angle;
		
		return angle;
	}
	
	/**
	 * Check whether the larva moved to the right based on the reference line.
	 * @param linearLine The reference line in frame N
	 * @param pointEnd1N0 The end point 1 in frame N.
	 * @param pointCenterN0 The center point in frame N.
	 * @param pointCenterN1 The center point in frame N+1
	 * @return true if the larva moved to the right based on the reference line.
	 */
	public static boolean isMoveToReferenceRight(LinearLine linearLine, Point pointEnd1N0, Point pointCenterN0, Point pointCenterN1 )
	{
		// if the distance between the point and the line is less than 1
		if(getNearestDistance(linearLine, pointCenterN1) < 1)
			return true;
		
		double angle = calcReferenceAngle( linearLine,  pointEnd1N0,  pointCenterN0,  pointCenterN1 );
		
		// yes, move to the right
		if(angle >= 0)
			return true;
		// move to the left
		else
			return false;
	}
	
	/**
	 * Check whether the larva moved to the right based on the reference line.
	 * @param linearLine The reference line in frame N
	 * @param pointEnd1N0 The end point 1 in frame N.
	 * @param pointCenterN0 The center point in frame N.
	 * @param pointCenterN1 The center point in frame N+1
	 * @return true if the larva moved to the right based on the reference line.
	 */
	public static boolean isMoveToRight(LinearLine linearLine, Point pointEnd1N0, Point pointCenterN0, Point pointCenterN1 )
	{
		// the distance of both center points is less than 1
		if(getDistance(pointCenterN0, pointCenterN1) < 1)
			return true;
		
		// if the distance between the point and the line is less than 1
		if(getNearestDistance(linearLine, pointCenterN1) < 1)
			return true;
		
		LinearLine linePerp = getPerpendicularLine(linearLine, pointEnd1N0);
		Point2D point2DLineN0 = getInterception(linearLine, linePerp);
		Point pointLineN0 = new Point( (int) Math.round(point2DLineN0.getX()) 
				, (int) Math.round(point2DLineN0.getY()) );
		
		double angle = getAngleBetween(pointCenterN0, pointLineN0, pointCenterN1);
		
		if(angle > 180)
			angle = 180 - angle;
		
		if(angle < -180)
			angle = 360 + angle;
		
		// yes, move to the right
		if(angle <= 0)
			return true;
		// move to the left
		else
			return false;
	}
	
	
	public static Boolean isPointInRight(LinearLine linearLine, Point pointEnd1, Point pointCenter, Point pointCheck )
	{
		if ( Double.isNaN(linearLine.getBeta1()) ) 
		{
			// if pointEnd1 is in the right side of or on the pointCenter (linearLine, pre-requirement: pointCenter is on linearLine line)
			if(pointEnd1.x >= pointCenter.x)
			{
				// if the point checked is in the right side of pointCenter
				if(pointCheck.x >= pointCenter.x)
					return true;
				else
					return false;
			// if pointEnd1 is in the left side of pointCenter
			}else
			{
				// if the point checked is in the right side of pointCenter
				if(pointCheck.x >= pointCenter.x)
					return false;
				else
					return true;
			}
		}
		
		// if the linear regression line is parallel to horizontal line
		if (linearLine.getBeta1() == 0) 
		{
			// if the end point 1 is in the left of the center point for
			// the previous larva
			if (pointEnd1.x > pointCenter.x) {

				if (pointCheck.y >= pointCenter.y)
					return true;
				else
					return false;
				// if the end point 1 is in the right of the center
				// point for the previous larva
			} else {
				if (pointCheck.y <= pointCenter.y)

//					featuresOfLarva.setIsDistSidewaysPos(true);
					return true;
				else
//					featuresOfLarva.setIsDistSidewaysPos(false);
					return false;
			}

			// if the y of end point 1 (the head point of the larva) is
			// greater than that of the center point
		} else if (pointEnd1.y > pointCenter.y) {

			// if the center point of the current larva is in the right
			// side of the linear regress line, including on the line
			if (pointCheck.x >= pointCenter.y)

//				featuresOfLarva.setIsDistSidewaysPos(true);
				return true;
			else
//				featuresOfLarva.setIsDistSidewaysPos(false);
				return false;
			// if the y of end point 1 (the head point of the larva) is
			// less than y of the center point for the previous larva
		} else {
			LinearLine linearLineEndPtParallelRelection = MathManager.getLinearLineRefletion(
					linearLine);

			Point centerPointRelection = MathManager
					.getPointRefletion(pointCheck);

			// if the center point is in the right side of the line,
			// including on the line
			if (centerPointRelection.x >= linearLineEndPtParallelRelection.getX(centerPointRelection.y))

//				featuresOfLarva.setIsDistSidewaysPos(true);
				return true;
			else
//				featuresOfLarva.setIsDistSidewaysPos(false);
				return false;

		}
		
	}
	
	/**
	 * get the reflection point.
	 * @param point The point checked.
	 * @return The reflected point.
	 */
	public static Point getPointRefletion(Point point)
	{
		return new Point(point.x, -point.y);
	}
	
	/**
	 * get the reflection linearLine.
	 * @param linearLine The linear line checked.
	 * @return The reflected linear line.
	 */
	public static LinearLine getLinearLineRefletion(LinearLine linearLine)
	{
		LinearLine linearLineReflection = new LinearLine();
		
		linearLineReflection.setBeta1(-linearLine.getBeta1());
		linearLineReflection.setBeta0(-linearLine.getBeta0());
		
		return linearLineReflection;
	}
	
	/**
	 * Convert point2D to point.
	 * @param point A point2D.
	 * @return The converted point.
	 */
	public static Point convertPt2DToPt(Point2D point)
	{
		int x = (int) Math.round( point.getX() );
		int y = (int) Math.round( point.getY() );
		return new Point( x, y );
	}
	
	/**
	 * Convert point to point2D.
	 * @param point A point.
	 * @return The converted point2D.
	 */
	public static Point2D convertPtToPt2D(Point point)
	{
		return new Point2D.Double( (double) point.x, (double) point.y );
	}
	
	/**
	 * Get a point printing format.
	 * @param point A point2D.
	 * @return The format string.
	 */
	public static String getPointStr( Point2D point )
	{
		return "(" + point.getX() + ", " + point.getY() + ")";
	}
	
	/**
	 * Get the average point.
	 * @param point1 A point.
	 * @param point2 A point.
	 * @return The average point.
	 */
	public static Point2D getAveragePoint( Point point1, Point point2 )
	{
		return getAveragePoint( convertPtToPt2D(point1), convertPtToPt2D(point2) );
	}
	
	/**
	 * Get the average point2D.
	 * @param point1 A point2D.
	 * @param point2 A point2D.
	 * @return The average point2D.
	 */
	public static Point2D getAveragePoint( Point2D point1, Point2D point2 )
	{
		double xAvg = ( point1.getX() + point2.getX() ) / 2;
		double yAvg = ( point1.getY() + point2.getY() ) / 2;
		
		return new Point2D.Double(xAvg, yAvg);
	}
	
	/**
	 * Check whether two points are in the same side of a linear line. 
	 * 
	 * @param linearLine The line used to divide area into two sides.
	 * @param point1 The point to be checked.
	 * @param point2 The point to be checked.
	 * @return true if both points are in the same side of a linear line.
	 */
	public static boolean isPointsInSameSide( LinearLine linearLine, Point2D point1, Point2D point2 )
	{
		// if the line is parallel with y-axis
		if( Double.isNaN( linearLine.getBeta1() ) )
		{
			// if point1 and point2 are in the same side of the linearLine
			if( (point1.getX() - linearLine.getBeta0() ) * (point2.getX() - linearLine.getBeta0() ) >= 0 )
				return true;
			// if point1 and point2 are NOT in the same side of the linearLine
			else
				return false;
		}
		
		double yPoint1 = linearLine.getBeta1() * point1.getX() + linearLine.getBeta0();
		double yPoint2 = linearLine.getBeta1() * point2.getX() + linearLine.getBeta0();
		
		// check if point 1 is in the lower side of the linear line
		// if yes, true. Otherwise, false
		boolean isPt1BlowLine = point1.getY() <= yPoint1 ? true : false;
		// check if point 2 is in the lower side of the linear line
		// if yes, true. Otherwise, false
		boolean isPt2BlowLine = point2.getY() <= yPoint2 ? true : false;
		
		// return true if both points are in the same side of the linear line.
		// Otherwise, return false.
		return isPt1BlowLine == isPt2BlowLine ? true : false;
	}
	
	/**
	 * Get a rounded double number. 
	 * 
	 * @param number The double.
	 * @return The rounded double.
	 */
	public static double get2DecimalPoints( double number )
	{
		return 	Math.round( number * 100.0 ) / 100.0;	
	}
	
	/**
	 * Get the nearset distance between a point to a line. 
	 * 
	 * @param linearLine The line.
	 * @param point The point.
	 * @return The distance.
	 */
	public static double getNearestDistance(LinearLine linearLine, Point point)
	{
		double distance = 0;
		
		// if the linearLine is NOT parallel with y-axis
		if( !Double.isNaN(linearLine.getBeta1()) )
		{
			double a = linearLine.getBeta1();
			double b = -1;
			double c = linearLine.getBeta0();
			
			int x0 = point.x;
			int y0 = point.y;
			
			// formula from https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
			double numerator = Math.abs( a*x0 + b*y0 + c );
			double denominator = Math.sqrt( a*a + b*b );
			
			distance = numerator / denominator;
			
		// if the linearLine is parallel with y-axis
		}else
		{
			distance = Math.abs( point.x - linearLine.getBeta0() );
		}
		
		return get2DecimalPoints(distance);
	}

	/**
	 * Get the closest point for the point. 
	 * 
	 * @param points The array containing all the points from which the point 
	 * 			will be get
	 * @param point The point to be compared with and a point closest to it will be gotten.
	 * @return the index of the closest point in the array list will be return.
	 */
	public static int getClosestPointIndex(ArrayList<Point> points, Point point)
	{
		double distance = 0;
		int pointIndex = 0;
		
		for(int i = 0; i < points.size(); i++)
		{
			if( getDistance(point, points.get(i)) > distance )
			{
				distance = getDistance(point, points.get(i));
				pointIndex = i;
			}
		}
		
		return pointIndex;
	}
	
	/**
	 * Get the nearest distance between 2 points. 
	 * 
	 * @param point1 A point.
	 * @param point2 A point.
	 * @return The distance.
	 */
	public static double getDistance(Point point1, Point point2)
	{
		double distance = Math.sqrt( Math.pow((point1.x-point2.x),2) 
				+ Math.pow((point1.y-point2.y),2) );
		return get2DecimalPoints( distance ); 
	}
	
	/**
	 * Get the nearest distance between 2 points. 
	 * 
	 * @param point1 A point.
	 * @param point2 A point.
	 * @return The distance.
	 */
	public static double getDistance(Point2D point1, Point2D point2)
	{
		double distance = Math.sqrt( Math.pow((point1.getX()-point2.getY()),2) + Math.pow((point1.getY()-point2.getY()),2) );
		return (double) Math.round( distance * 100.0 ) / 100.0; // round to 2 decimal points
	}
	
	/**
	 * Get a line passing 2 points. 
	 * 
	 * @param point1 A point.
	 * @param point2 A point.
	 * @return The line.
	 */
	public static LinearLine getTwoPointsLine(Point point1, Point point2)
	{
		LinearLine linearLine = new LinearLine();

		double beta1 = 0;
		double beta0 = 0;
				
		// if the line which goes through point1 and point2 is NOT parallel with vertical axis
		if(point1.x != point2.x)
		{
			beta1 = (double) (point2.y - point1.y) / (point2.x - point1.x);
			beta1 = get2DecimalPoints(beta1);
			
			 beta0 = (double) point1.y - beta1 * point1.x;
			 beta0 = get2DecimalPoints(beta0);
		}
		else
		{
			beta1 = Double.NaN;
			beta0 = point1.x;
		}
				
		linearLine.setBeta1(beta1);
		linearLine.setBeta0(beta0);
		
		return linearLine;
	}
	
	/**
	 * Get a line that is perpendicular to another line. 
	 * 
	 * @param linearLineFrom A line.
	 * @param pointThrough A point.
	 * @return The line.
	 */
	public static LinearLine getPerpendicularLine(LinearLine linearLineFrom, Point pointThrough)
	{
		LinearLine linearLine = new LinearLine();
		
		double beta1 = 0;
		double beta0 = 0;
			
		// if line linearLineFrom is parallel with vertical axis.
		// Double.NaN is the beta1 (slope) for the line is parallel with vertical axis.
		// That is what I defined the the line is parallel with vertical axis.
		if( Double.isNaN(linearLineFrom.getBeta1()) )
		{
			beta1 = 0;
			beta0 = pointThrough.y;
		}
		
		// if line linearLineFrom is parallel with horizontal axis
		if(linearLineFrom.getBeta1() == 0 )
		{
			beta1 = Double.NaN;
			beta0 = pointThrough.x;
		}
		
		// if line linearLineFrom is NOT parallel with horizontal axis and vertical axis
		if(linearLineFrom.getBeta1() != 0 && !Double.isNaN(linearLineFrom.getBeta1()) )
		{
			beta1 =	(double) - ( 1 / linearLineFrom.getBeta1() );
			beta1 = (double) get2DecimalPoints( beta1 ) ; // round to 2 decimal points
			beta0 = (double) pointThrough.y + ( 1 / linearLineFrom.getBeta1() ) * pointThrough.x;
			beta0 = (double) get2DecimalPoints( beta0 ); // round to 2 decimal points
		}
		
		linearLine.setBeta1(beta1);
		linearLine.setBeta0(beta0);
		
		return linearLine;
	}
	
	/**
	 * Get a line that is parallel to another line. 
	 * 
	 * @param beta1 The beta1 of a line.
	 * @param point A point.
	 * @return The line.
	 */
	public static LinearLine getParallelLine(double beta1, Point point)
	{
		LinearLine linearLine = new LinearLine();
		
		linearLine.setBeta1(beta1);
		
		double beta0 = 0;
		
		if( !Double.isNaN(beta1) )
		{
			beta0 = point.y - beta1 * point.x;
			beta0 = (double) get2DecimalPoints( beta0 ); // round to 2 decimal points
		}
		else
		{
			beta0 = point.x;
		}
		
		linearLine.setBeta0(beta0);
		
		return linearLine;
	}
	
	/**
	 * Get the linear regression line. 
	 * 
	 * @param imagePlusFit The image plus.
	 * @return The line.
	 */
	public static LinearLine getLinearRegression(ImagePlus imagePlusFit)
	{
		imagePlusFit = imagePlusFit.duplicate();
		ArrayList<Point> points = new ArrayList<Point>();
		
		for(int y = 0; y < imagePlusFit.getHeight(); y++)
			for(int x = 0; x < imagePlusFit.getWidth(); x++)
			{
				if( imagePlusFit.getProcessor().getPixel(x, y) > 128 )
					points.add( new Point(x, y) );
			}
		
		int N = points.size();

        // first pass
        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        for (int i = 0; i < N; i++) {
            sumx  += points.get(i).x;
            sumx2 += points.get(i).x * points.get(i).x;
            sumy  += points.get(i).y;
        }
        double xbar = sumx / N;
        double ybar = sumy / N;

        // second pass: compute summary statistics
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < N; i++) {
            xxbar += (points.get(i).x - xbar) * (points.get(i).x - xbar);
            yybar += (points.get(i).y - ybar) * (points.get(i).y - ybar);
            xybar += (points.get(i).x - xbar) * (points.get(i).y - ybar);
        }

        double beta1  = 0;
        double beta0 = 0;
        
        if( xxbar != 0 )
        {
	        beta1 =		xybar / xxbar; //Math.round( speedCrab * 100.0 ) / 100.0
	//        beta1 = Math.round( beta1 * 100.0 ) / 100.0;
	        beta1 = get2DecimalPoints(beta1);
	        
	        beta0 = ybar - beta1 * xbar;
	        beta0 = get2DecimalPoints(beta0);
        }else {
        	beta1 = Double.NaN;
        	// ??? make sure beta0 is correct when beta1 = Double.NaN;
        	beta0 = ybar;
        	beta0 = get2DecimalPoints(beta0);
        }

        LinearLine linearLine = new LinearLine();
        linearLine.setBeta1(beta1);
        linearLine.setBeta0(beta0);
        
        return linearLine;
	}
	
	/**
	 * Get a linear line passing two points. 
	 * 
	 * @param point1 A point.
	 * @param point2 A point.
	 * @return The line.
	 */
	public static LinearLine getLinearLine(Point point1, Point point2)
	{
		LinearLine linearLine = new LinearLine();
		
		double beta1 = 0;
		double beta0 = 0;

		// if the line is not parallel with Y axis
		if(point2.x == point1.x)
		{
			beta1 = Double.NaN;
			beta0 = point1.x;
		}else
		{
			beta1 = (double) (point2.y - point1.y) / (point2.x - point1.x);
			beta1 = get2DecimalPoints(beta1);
			beta0 = point1.y - beta1 * point1.x;
			beta0 = get2DecimalPoints(beta0);
		}
				
		linearLine.setBeta1(beta1);
		linearLine.setBeta0(beta0);
		
		return linearLine;
		
	}
	
}
