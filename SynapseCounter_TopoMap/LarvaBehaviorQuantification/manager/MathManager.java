package manager;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.swing.JTextArea;

import entities.FeaturesOfLarva;
import entities.LinearLine;
import file.CSVWriter;
import file.TextFileWriter;
import ij.ImagePlus;

public class MathManager 
{
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
	public static double getCurl( Point pointCenter, Point pointHead, Point pointTail )
	{
		//findCenter(currentLarvae, features );
		
		double headRelativeX = pointHead.x - pointCenter.x,
				headRelativeY = pointHead.y - pointCenter.y;
		
		double tailRelativeX = pointTail.x - pointCenter.x,
				tailRelativeY = pointTail.y - pointCenter.y;
		
		double angle = -(180/Math.PI)* Math.atan2(headRelativeX*tailRelativeY-headRelativeY*tailRelativeX, headRelativeX*tailRelativeX + headRelativeY*tailRelativeY);
		//if (angle < 0) angle = 360 + angle;  //to make sure the angle is between [0, 360)
		
		if (angle < 0) angle = -1 * angle;  //to make sure the angle is between [0, 180]

		//System.out.println(angle);
		//features.setCurel(angle);
		// Math.round( distance * 100.0 ) / 100.0
		return Math.round( angle * 100.0 ) / 100.0; // round to 2 decimal points
	}
	
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
	
	public static void fixInvalidLarva( ArrayList<FeaturesOfLarva> listFeaturesOfLarva, JTextArea textStatus ) 
	{
		FeaturesOfLarva larva = null; // the current larva
		FeaturesOfLarva larvaScan = null; // the scanning larva
		int i = 0; // the current larva
		int j = 0; // the scanning larva
		int indexFound = 0; // the index of the found larva
		// is the scanning process done for the current larva?
		Boolean isDone = false; 
		// the linear regression line for the previous larva
		LinearLine line = null; 
		// the linear line gose through the center point of the previous larva 
		// on the full frame
		LinearLine lineParallel = null; 
		// the side way distance
		double distSideways = 0;
		// the average of the sideways distance
		double distSidewaysAvg = 0;
		
		while ( i < listFeaturesOfLarva.size() )
		{
			larva = listFeaturesOfLarva.get(i);
			
//			HelperManager.showSysStatus(textStatus, "[fixInvalidLarva()] Checking frame id:"+larva.getFrameId());
			
			// if the larva is not valid
			if(!larva.getIsValid())
			{
				// the scanning larva index
				j = i + 1; // set the index to start scanning
				
				isDone = false;
				
//				// if j is not larger than the last frame id,
//				// need to scan to find the next valid larva
//				if(j < listFeaturesOfLarva.size())
//					isDone = false;
//				// if j is larger than the last frame id,
//				// don't need to scan to find the next valid larva
//				else
//					isDone = true;
				
				// loop until find the valid larva 
				while(j < listFeaturesOfLarva.size() && !isDone )
				{
					larvaScan = listFeaturesOfLarva.get(j);
					
					//System.out.println("Scaning frame id:"+larvaScan.getFrameId());
					//TextFileWriter.writeToFile("Scaning frame id:"+larvaScan.getFrameId(), "E:\\check1.txt");
					
					if( larvaScan.getIsValid() )
					{
						indexFound = j;
						isDone = true;
					}
					
					j++;
				}

				// if indexFound is less than or equal to the last index of 
				// listFeaturesOfLarva
				// and isDone is true, which means the next valid larva has
				// been found
				if(indexFound <= listFeaturesOfLarva.size() - 1 && isDone)
				{
					line = larva.getFeaturesOfLarvaPrevious().getLinearLineEndPts();
					lineParallel = MathManager.getParallelLine(line.getBeta1(), larva.getFeaturesOfLarvaPrevious().getCenterPointOnFullFrame());
					
					distSideways = MathManager.getNearestDistance(lineParallel, listFeaturesOfLarva.get(indexFound).getCenterPointOnFullFrame());
					
					distSidewaysAvg = distSideways / (indexFound - i);
					
					for(int k = i; k <= indexFound; k++)
					{
						listFeaturesOfLarva.get(k).setDistanceSidewaysEndPts(distSidewaysAvg);
						listFeaturesOfLarva.get(k).setIsSidewaysEndPtsForward(larva.getIsSidewaysEndPtsForward());
						listFeaturesOfLarva.get(k).setIsDistSidewaysPos(larva.getIsDistSidewaysPos());
						
//						HelperManager.showSysStatus(textStatus, "[fixInvalidLarva()] Set frame:"+listFeaturesOfLarva.get(k).getFrameId() +", swyDistEnd:"+distSidewaysAvg);
					}
					
					// fixed larva with index i~j. set the pointer to j+1
					i = indexFound + 1;
					
				// if indexFound is greater than the last index of listFeaturesOfLarva
				// or isDone is false, which means the next valid larva has not
				// been found
				}else{
					// set the checking larva to the index that is equal to 
					// listFeaturesOfLarva size to stop this method
					i = listFeaturesOfLarva.size(); 
				}

			// if the larva is valid
			}else{
				i++;
			}

		}
	}
	
	public static LinkedHashSet<Integer> getRollingFrame(
			ArrayList<FeaturesOfLarva> listFeaturesOfLarva, PropertyManager prop,
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
		FeaturesOfLarva larvaScan = null;
		
//		int frameIndex = 0;
//		int frameDifference = 0;
		FeaturesOfLarva larva = null;
		// the signed sideways distance
		double distSwySgn = 0;
		// the directed sideways distance
		double distSwyDir = 0;

		CSVWriter csvWriter = null;
		
		Map<Integer,Integer> frameIds = new HashMap<Integer,Integer>();
				
		System.out.println("listFeatures.size():" + listFeaturesOfLarva.size());
		
//		if(PropertyManager.getProperty("output_complete_csv").equals("true"))
		if(prop.getOutput_complete_csv().equals("true"))
		{
			csvWriter = new CSVWriter(filePrefix+"roll_info.csv");
			csvWriter.writeln("FrameId,distSwy,isSigned,isDirected,distSwySgn,distSwyDir,distSwyEndSgnBgn,distSwyEndSgnEnd,distSwyEndDirBgn,distSwyEndDirEnd");
		}
		
		for(int frameGlobalIndex = 0; frameGlobalIndex < listFeaturesOfLarva.size(); frameGlobalIndex++)
		{
			frameIds.put(listFeaturesOfLarva.get(frameGlobalIndex).getFrameId(), frameGlobalIndex);
		}
		
		for(int frameGlobalIndex = 0; frameGlobalIndex < listFeaturesOfLarva.size(); frameGlobalIndex++)
		{
			larva = listFeaturesOfLarva.get(frameGlobalIndex);
			distSwySgnBgn = 0; 
			distSwyDirBgn = 0; 
			
			System.out.println("[Debug] frameGlobalIndex: "+larva.getFrameId());
			
			// if there are frameRange (number) frames after the current frame
			if( larva.getFrameId() + frameRange < listFeaturesOfLarva.get(listFeaturesOfLarva.size()-1).getFrameId() )
			{
				for(int i = larva.getFrameId(); i < larva.getFrameId() + frameRange; i++)
				{
					System.out.println("[Debug] index(frameGlobalIndex): "+frameGlobalIndex);
					System.out.println("[Debug] index(i): "+i);
					System.out.println("[Debug] frameIds.get(i): "+frameIds.get(i));
					System.out.println("[Debug] listFeaturesOfLarva.get(frameIds.get(i)): "+listFeaturesOfLarva.get(frameIds.get(i)));
					larvaScan = listFeaturesOfLarva.get(frameIds.get(i));
					
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
			if( larva.getFrameId() - frameRange >= listFeaturesOfLarva.get(0).getFrameId() )
			{
				for(int i = larva.getFrameId(); i > larva.getFrameId() - frameRange; i--)
				{
					larvaScan = listFeaturesOfLarva.get(frameIds.get(i));

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
			
			listFeaturesOfLarva.get(frameGlobalIndex).setDistSwyDirBgn(distSwyDirBgn);
			listFeaturesOfLarva.get(frameGlobalIndex).setDistSwySgnBgn(distSwySgnBgn);
			listFeaturesOfLarva.get(frameGlobalIndex).setDistSwyDirEnd(distSwyDirEnd);
			listFeaturesOfLarva.get(frameGlobalIndex).setDistSwySgnEnd(distSwySgnEnd);
			
//			if(PropertyManager.getProperty("output_complete_csv").equals("true"))
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
	
	public static Point addPoints(Point point1, Point point2) 
	{
		return new Point(point1.x + point2.x, point1.y + point2.y);
	}
	
	public static double getAngleBetween(Point center, Point point1, Point point2) 
	{
		double angle = Math.toDegrees( Math.atan2(point2.x - center.x, point2.y - center.y)-
	                        Math.atan2(point1.x - center.x, point1.y - center.y) );
	  
		return get2DecimalPoints(angle);
	}
	
	public static double getDegreeFromTangent(double tangent )
	{
		double degree = Math.toDegrees(Math.atan(tangent));
		return get2DecimalPoints(degree);
	}
	
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

//					featuresOfLarva.setIsDistSidewaysPos(true);
					return true;
				else
//					featuresOfLarva.setIsDistSidewaysPos(false);
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
			// less than that of the center point for the previous larva
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
	
	public static Point getPointRefletion(Point point)
	{
		return new Point(point.x, -point.y);
	}
	
	public static LinearLine getLinearLineRefletion(LinearLine linearLine)
	{
		LinearLine linearLineReflection = new LinearLine();
		
		linearLineReflection.setBeta1(-linearLine.getBeta1());
		linearLineReflection.setBeta0(-linearLine.getBeta0());
		
		return linearLineReflection;
	}
	
	public static Point convertPt2DToPt(Point2D point)
	{
		int x = (int) Math.round( point.getX() );
		int y = (int) Math.round( point.getY() );
		return new Point( x, y );
	}
	
	public static Point2D convertPtToPt2D(Point point)
	{
		return new Point2D.Double( (double) point.x, (double) point.y );
	}
	
	public static String getPointStr( Point2D point )
	{
		return "(" + point.getX() + ", " + point.getY() + ")";
	}
	
	public static Point2D getAveragePoint( Point point1, Point point2 )
	{
		return getAveragePoint( convertPtToPt2D(point1), convertPtToPt2D(point2) );
	}
	
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
	public static Boolean checkPointsSide( LinearLine linearLine, Point2D point1, Point2D point2 )
	{
		double yPoint1 = linearLine.getBeta1() * point1.getX() + linearLine.getBeta0();
		double yPoint2 = linearLine.getBeta1() * point2.getX() + linearLine.getBeta0();
		
//		System.out.println("yPoint1 (PtAvg):"+yPoint1);
//		System.out.println("yPoint2 (PtCnt):"+yPoint2);
		
		// check if point 1 is in the lower side of the linear line
		// if yes, true. Otherwise, false
		Boolean isPt1BlowLine = point1.getY() <= yPoint1 ? true : false;
		// check if point 2 is in the lower side of the linear line
		// if yes, true. Otherwise, false
		Boolean isPt2BlowLine = point2.getY() <= yPoint2 ? true : false;
		
		// return true if both points are in the same side of the linear line.
		// Otherwise, return false.
		return isPt1BlowLine.equals(isPt2BlowLine) ? true : false;
	}
	
	public static double get2DecimalPoints( double number )
	{
		return 	Math.round( number * 100.0 ) / 100.0;	
	}
	
	public static double getNearestDistance(LinearLine linearLine, Point point)
	{
		double distance = 0;
		
		// if the linearLine is NOT parallel with vertical axis
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
			
		// if the linearLine is parallel with vertical axis
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
	
	public static double getDistance(Point point1, Point point2)
	{
		double distance = Math.sqrt( Math.pow((point1.x-point2.x),2) + Math.pow((point1.y-point2.y),2) );
		return (double) Math.round( distance * 100.0 ) / 100.0; // round to 2 decimal points
		
//		return Math.sqrt(Math.pow((point1.x-point2.x),2) + Math.pow((point1.y-point2.y),2));
	}
	
	public static double getDistance(Point2D point1, Point2D point2)
	{
		double distance = Math.sqrt( Math.pow((point1.getX()-point2.getY()),2) + Math.pow((point1.getY()-point2.getY()),2) );
		return (double) Math.round( distance * 100.0 ) / 100.0; // round to 2 decimal points
	}
	
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
	
//	public static LinearRegression getLinearRegression(ImagePlus imagePlusFit)
//	{
//		imagePlusFit = imagePlusFit.duplicate();
//		ArrayList<Point> points = new ArrayList<Point>();
//		
//		for(int y = 0; y < imagePlusFit.getHeight(); y++)
//			for(int x = 0; x < imagePlusFit.getWidth(); x++)
//			{
//				if( imagePlusFit.getProcessor().getPixel(x, y) > 128 )
//					points.add( new Point(x, y) );
//			}
//		
//		LinearRegression linearRegression = new LinearRegression(points);
//		
//		return linearRegression;
//	}
	
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
		
//		LinearRegression linearRegression = new LinearRegression(points);
//		
//		return linearRegression;
	}
	
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
