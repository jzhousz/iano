package manager;

import java.awt.Color;
import java.awt.Point;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import entities.Larva;
import entities.LarvaeAndRolls;
import file.CSVWriter;
import file.TextFileWriter;

/**
* The used to help other class.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class HelperManager
{
	public static Date dateSys = null;
	public static DateFormat dateSysFormat = new SimpleDateFormat("HH:mm:ss");
	
	/**
	 * Get the rolling frames from the list of larvae (the larva in all the frames in the video).
	 * 
	 * @param listLarva The larva in all frames in the video.
	 * @param frequent Between how many frames the positions of the larva will be recorded?
	 */
	public static ArrayList<LarvaeAndRolls> getRollingFrames(ArrayList<Larva> listLarva, int frequent)
	{
		ArrayList<LarvaeAndRolls> listLarvaeRolls = new ArrayList<LarvaeAndRolls>();
		
		ArrayList<Larva> larvae = new ArrayList<Larva>();
		
		// get every frequent number of larvae (e.g., 5,10,15,20,... if frequent = 5)
		for(Larva larva : listLarva)
		{
			if(larva.getFrameId() % frequent == 0)
			{
				larvae.add(larva);
			}
		}
		
		ArrayList<ArrayList<Larva>> larvaeCluster = new ArrayList<ArrayList<Larva>>();
		// is the previous larva (the current larva - frequent) move forward.
		// initialize it to true so the first larva is treated as moving forward
		boolean isPreviousForward = true; 
		
		ArrayList<Larva> larvaeSection = new ArrayList<Larva>();
		larvaeCluster.add(larvaeSection);
		
		// for these chosen larvae, check whether they move forward or backward
		for(int i = 0; i < larvae.size(); i++)
		{
			Larva larva = larvae.get(i);
			Point pointCurr = MathManager.addPoints(larva.getCenterPoint(), larva.getRoiTopLeft());
			
			// if i is not the last index, i.e., larvae.get(i) is not the last larva
			if(i < larvae.size() - 1)
			{
				Larva larvaNext = larvae.get(i+1);
				Point pointNext = MathManager.addPoints(larvaNext.getCenterPoint(), larvaNext.getRoiTopLeft());
				
				Point pointAvgSmall = MathManager.getAveragePoint2(larva.getEndPoints().get(0), larva.getEndPoints().get(1));
				Point pointAvg = MathManager.addPoints(pointAvgSmall, larva.getRoiTopLeft());
				
				boolean isForward = MathManager.isPointsInSameSide(larva.getLinearLineEndPtsParallel(), pointAvg, pointNext);
				
				// if the current larva move in the different direction from the previous larva (the current larva - frequent)
				// e.g., the current larva: forward, the previous larva: backward, it's different direction.
				if(isForward != isPreviousForward)
				{
					larvaeSection = new ArrayList<Larva>();
					larvaeCluster.add(larvaeSection);
				}
				
				larvaeSection.add(larva);
				
				larva.setIsClusterForward(isForward);
				
				isPreviousForward = isForward;
			}
		}
		
		// check whether which cluster contains enough frames for rolling
		for(ArrayList<Larva> larvaeTest : larvaeCluster)
		{
			if(larvaeTest.size() > 0)
			{
				for(int i = larvaeTest.get(0).getFrameId(); i < larvaeTest.get(larvaeTest.size()-1).getFrameId() + frequent; i ++)
				{
					Larva larva = Larva.getLarvaByFrameId(listLarva, i);
					
					larva.setIsClusterForward(larvaeTest.get(0).getIsClusterForward() );
				}
			}
			
			double swDistMv = 0; // the sideways distance moved
			
			// if the cluster contains 20 frames (4 x 5)
			if(larvaeTest.size() >= 2)
			{
				// calculate the total sideways distance moved for these clustered larvae
				for(int i = larvaeTest.get(0).getFrameId(); i <= larvaeTest.get(larvaeTest.size()-1).getFrameId() + frequent; i ++)
				{
					Larva larva = Larva.getLarvaByFrameId(listLarva, i);
					
					swDistMv += larva.getDistanceSidewaysEndPts();
				}
				
				double swDistMvRolling = 0; // distance moved when rolling
//				double numRolls = 0; // number of rolls when rolling
				double numRollsTotal = 0; // number of rolls when rolling
				
				// check whether the rolling sideways distance is larger than the 
				// average perimeter of the larva
//				if(swDistMv >= .8 * larvaeTest.get(0).getAvgPerimeter())
				if(swDistMv >= larvaeTest.get(0).getAvgPerimeter())
				{
					LarvaeAndRolls larvaeAndRolls = new LarvaeAndRolls();
					ArrayList<Integer> frameIds = new ArrayList<Integer>();
					
					for(int i = larvaeTest.get(0).getFrameId(); i <= larvaeTest.get(larvaeTest.size()-1).getFrameId() + frequent; i ++)
					{
						Larva larva = Larva.getLarvaByFrameId(listLarva, i);
						
						swDistMvRolling += larva.getDistanceSidewaysEndPts();
//						numRolls = MathManager.get2DecimalPoints( swDistMvRolling / larva.getAvgPerimeter() ) ;
						
						frameIds.add(i);
					}
					
					larvaeAndRolls.setFrameId(frameIds);
					numRollsTotal = MathManager.get2DecimalPoints( swDistMvRolling / larvaeTest.get(0).getAvgPerimeter() ) ;
					larvaeAndRolls.setRolls(numRollsTotal);
					
					listLarvaeRolls.add(larvaeAndRolls);
				}
			}
		}
		
		return listLarvaeRolls;
		
	}
	
	/**
	 * Save the testing rolling data for the larva. The data can be used to verify correctness 
	 * about rolling.
	 * 
	 * @param listLarva The larva in all frames in the video.
	 * @param frequent Between how many frames the positions of the larva will be recorded?
	 * @param dirImageDebug The path to save the file.
	 */
	public static void saveRollingTestingData(ArrayList<Larva> listLarva, int frequent, String dirImageDebug)
	{
		CSVWriter csvWriter = new CSVWriter(dirImageDebug + "sideSwyDist.csv");
		String line;
		line = "frameId,sidewaysDist,curl,isClusterForward,isRolling,sidewaysTotal,sidewaysAvgSpeed,sidewaysAvgSpeedSign";
		csvWriter.writeln(line);
		
		double swDistEndPts = 0;
//		double swDistEndPtsSign = 0;
		double curl = 0;
		double sidewaysAvgSpeed = 0; // the average sideways speed in a range of frames
		double sidewaysAvgSpeedSign = 0; // the average sideways speed in a range of frames
		
		// save the signed sideways distance
		for(Larva larva : listLarva)
		{
			swDistEndPts += larva.getDistanceSidewaysEndPts();
				
			int numNearLarvae = frequent / 2;
			sidewaysAvgSpeed = 0;
			int numLarvaCounted = 0;
			
			for(int i = larva.getFrameId() - numNearLarvae; i <= larva.getFrameId() + numNearLarvae; i++)
			{
				Larva lv = Larva.getLarvaByFrameId(listLarva, i);
				if(lv != null)
				{
					numLarvaCounted++;
					sidewaysAvgSpeed += lv.getDistanceSidewaysEndPts();
				}
			}
			
			sidewaysAvgSpeed = sidewaysAvgSpeed / numLarvaCounted;
			
			// if the larva in the set moved forward (sideways direction)
			if(larva.getIsClusterForward())
			{
				curl = larva.getCurl();
				sidewaysAvgSpeedSign = sidewaysAvgSpeed;
			// if the larva in the set moved backward (sideways direction)
			}else{
				curl = -1 * larva.getCurl();
				sidewaysAvgSpeedSign = -1 * sidewaysAvgSpeed;
			}
			
			line = larva.getFrameId() + "," 
					+ larva.getDistanceSidewaysEndPts() + ","
					+ curl + ","
					+ larva.getIsClusterForward() + ","
					+ larva.getIsRolling() + ","
					+ swDistEndPts + ","
					+ sidewaysAvgSpeed + ","
					+ sidewaysAvgSpeedSign;
			csvWriter.writeln(line);
				
//			System.out.println("(** Test 2) larva id: "+larva.getFrameId()
//					+", isClusterForward: "+ larva.getIsClusterForward() 
//					+ ", sidewySpeed:" + larva.getDistanceSidewaysEndPts() 
//					+ ", swDistEndPtsSign:" + swDistEndPts);
		}
	}
	
	public static void saveBehaviourImage()
	{
//		ImagePlus impVisualize = ImageManager.newRGBImagePus("Larva Stay Index", imageArr[0].length, imageArr.length, 1, NewImage.FILL_WHITE);
//		
//		ImagePlus impEndPts = impVisualize.duplicate();
//		
//		// convert to 8 gray image
//		ImageConverter imageConverter = new ImageConverter(impVisualize);
//		imageConverter.convertToGray8();
//				
//		ImagePlus impValue = impVisualize.duplicate();
//		
//		ImagePlus impValue3d = impVisualize.duplicate();
//		
//		ImagePlus impThreshold = impVisualize.duplicate();
//		
//		int valueMax = 0;
//		
//		for(int y = 0; y < imageArr.length; y++)
//			for(int x = 0; x < imageArr[0].length; x++)
//			{
//				if(imageArr[y][x] > valueMax)
//				{
//					valueMax = imageArr[y][x];
//				}
//			}
//		
//		int valueUnit = 1;
//		
//		if(valueMax < 255)
//			valueUnit = 255 / valueMax;
//		
//		for(int y = 0; y < imageArr.length; y++)
//			for(int x = 0; x < imageArr[0].length; x++)
//			{
//				if(imageArr[y][x] != 0 && imageArr[y][x] <= 10)
//					impThreshold.getProcessor().putPixel(x, y, imageArr[y][x]);
//				else
//					impThreshold.getProcessor().putPixel(x, y, 255);
//				
//				if(imageArr[y][x] != 0)
//				{
//					impValue.getProcessor().putPixel(x, y, imageArr[y][x]);
//					impValue3d.getProcessor().putPixel(x, y, imageArr[y][x]);
//					impVisualize.getProcessor().putPixel(x, y, imageArr[y][x] * valueUnit);
//				}else
//				{
//					impValue.getProcessor().putPixel(x, y, 255);
//					impValue3d.getProcessor().putPixel(x, y, 0);
//					impVisualize.getProcessor().putPixel(x, y, 0);
//				}
//			}
//				
//		
//		ImageSaver.saveImagesWithPath(dirImageDebug + "imageArrVisualize.jpg", impVisualize);
//		ImageSaver.saveImagesWithPath(dirImageDebug + "imageArrValue.jpg", impValue);
//		ImageSaver.saveImagesWithPath(dirImageDebug + "imageArrValue3d.jpg", impValue3d);
//		ImageSaver.saveImagesWithPath(dirImageDebug + "impThreshold.jpg", impThreshold);
//		
//		int frequent = 5;
//		
//		ArrayList<Larva> larvae = new ArrayList<Larva>();
//		
//		for(Larva larva : listLarva)
//		{
//			if(larva.getFrameId() % frequent == 0)
//			{
//				larvae.add(larva);
//				DrawingManager.drawLine(impEndPts, larva.getEndPointsOnFullFrame().get(0), larva.getEndPointsOnFullFrame().get(1), 1, Color.cyan);
//			}
//		}
//		
//		for(Larva larva : listLarva)
//		{
//			if(larva.getFrameId() % frequent == 0)
//				PixelPutter.putPixels(impEndPts, MathManager.addPoints(larva.getPointCenterMass(), larva.getRoiTopLeft()), 3, 3, Color.red);
//		}
//		
//		ArrayList<Integer> larvaBlock = new ArrayList<Integer>();
//		
//		for(int i = 0; i < larvae.size(); i++)
//		{
//			Larva larva = larvae.get(i);
//			Point pointCurr = MathManager.addPoints(larva.getCenterPoint(), larva.getRoiTopLeft());
//			
//			if(i < larvae.size() - 1)
//			{
//				Larva larvaNext = larvae.get(i+1);
//				Point pointNext = MathManager.addPoints(larvaNext.getCenterPoint(), larvaNext.getRoiTopLeft());
//				
//				Point pointAvgSmall = MathManager.getAveragePoint2(larva.getEndPoints().get(0), larva.getEndPoints().get(1));
//				Point pointAvg = MathManager.addPoints(pointAvgSmall, larva.getRoiTopLeft());
//						
//				
////				LinearLine line = larvaNext.getLinearLineEndPts();
////				LinearLine lineMass = MathManager.getParallelLine(line.getBeta1(), pointCurr);
//				
//				boolean isForward = MathManager.isPointsInSameSide(larva.getLinearLineEndPtsParallel(), pointAvg, pointNext);
//				
//				System.out.println("(isForward) frame id: " + larva.getFrameId() + ", isForward: " + isForward 
//						+ ", pointCurr:"+pointCurr+",ptNext:"+pointNext+",pointAvg:"+pointAvg);
//			}
//		}
//		
//		ImageSaver.saveImagesWithPath(dirImageDebug + "impEndPts.jpg", impEndPts);
	}
	
	/**
	* New a set.
	* 
	* @param values The int array.
	* @param The integer set.
	*/
	public static Set<Integer> newSet(int[] values)
	{
		Set<Integer> set = new HashSet<Integer>();
		
		for(int v : values)
			set.add(v);
		
		return set;
	}
	
	/**
	* Fix the sideways distance for the larva if there is a binarization problem.
	* 
	* @param listLarva The list of the larva in all frames.
	* @param avgDiameter The average diameter of the larva.
	* @param avgSkeletonLen The average of skeleton of larva.
	* @param avgArea The average area of the larva.
	* @param prop The property file.
	*/
	public static void setInvalidLarvaAndMessage7(ArrayList<Larva> listLarva, double avgDiameter
			, int avgSkeletonLen, int avgArea, PropertyManager prop)
	{
		ArrayList<Integer> larvaSizes = MathManager.calcMinMaxLarvaSize(avgArea, prop);
		int maxSizeLarva = larvaSizes.get(1);
		int minSizeLarva = larvaSizes.get(0);
		
		ArrayList<Integer> larvaSkeleton = MathManager.calcMinMaxLarvaSkeleton(avgSkeletonLen, prop);
		int maxSkeletonLarva = larvaSkeleton.get(1);
		int minSkeletonLarva = larvaSkeleton.get(0);
		
		for(Larva larva : listLarva)
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
		}
	}
	
//	/**
//	* Show system status text on GUI.
//	* 
//	* @param textStatus The status text area on GUI.
//	* @param status The status text.
//	*/
//	public static void showSysStatus(JTextArea textStatus, String status)
//	{
////		dateSys = new Date();
////		HelperManager.showStatus(textStatus, "\n("+dateSysFormat.format(dateSys)+") "+status+"\n");
//		dateSys = new Date();
//		HelperManager.showStatus(textStatus, "\n("+dateSysFormat.format(dateSys)+") "+status+"\n");
//	}
	
	/**
	* Show system status text on GUI.
	* 
	* @param textStatusPane The status text area on GUI.
	* @param status The status text.
	*/
	public static void showSysStatus(JTextPane textStatusPane, String status, Color color)
	{
		dateSys = new Date();
		HelperManager.showStatus(textStatusPane, "\n("+dateSysFormat.format(dateSys)+") "+status+"\n", color);
	}
	
//	/**
//	* Show status text on GUI.
//	* 
//	* @param textStatus The status text area on GUI.
//	* @param status The status text.
//	*/
//	public static void showStatus(JTextArea textStatus, String status)
//	{
//		System.out.print(status);
//		ij.IJ.showStatus(status);
//		textStatus.append(status);
//		textStatus.setCaretPosition(textStatus.getDocument().getLength());
//		textStatus.validate();
//	}
	
	/**
	* Show status text on GUI.
	* 
	* @param textStatusPane The status text area on GUI.
	* @param status The status text.
	*/
	public static void showStatus(JTextPane textStatusPane, String status, Color color)
	{
		System.out.print(status);
		ij.IJ.showStatus(status);
//		textStatusPane.append(status);
		StyledDocument doc = textStatusPane.getStyledDocument();

		Style style = textStatusPane.addStyle("I'm a Style", null);
		StyleConstants.setForeground(style, color);

		try
		{
			doc.insertString(doc.getLength(), status, style);
		} catch (BadLocationException e)
		{
		}
		
		textStatusPane.setCaretPosition(textStatusPane.getDocument().getLength());
		textStatusPane.validate();
	}
	
	/**
	* Get time duration.
	* 
	* @param timeBegin The status text area on GUI.
	* @return The time duration.
	*/
	public static String getDuration(Date timeBegin)
	{
		long timeStart = timeBegin.getTime();
		Date dt = new Date();
		double seconds = ( dt.getTime() - timeStart ) / 1000.0;
		int hours = (int) seconds / 3600;
		double seconds_Remain = seconds % 3600;
		int minutes = (int) seconds_Remain / 60;
		int sec = (int) ( seconds_Remain - minutes * 60 );
		
		String timeStr = "";
		
		if( hours > 0 )
			timeStr += hours + " Hour ";
		
		if( minutes > 0 )
			timeStr += minutes + " Minute ";
		
		timeStr += sec + " Second";
		
		return timeStr;
	}
	
	/**
	* Print time duration.
	* 
	* @param timeStart The time start.
	* @return None.
	*/
	public static void printDuration(long timeStart)
	{
		double seconds = ( System.nanoTime() - timeStart ) / 1000000000.0;
		int hours = (int) seconds / 3600;
		double seconds_Remain = seconds % 3600;
		int minutes = (int) seconds_Remain / 60;
		int sec = (int) ( seconds_Remain - minutes * 60 );
		
		String timeStr = "Time Completed: ";
		
		if( hours > 0 )
			timeStr += hours + " Hour ";
		
		if( minutes > 0 )
			timeStr += minutes + " Minute ";
		
		timeStr += sec + " Second";
		
		System.out.println(timeStr);
	}
	
	public static void setFramesDistance7(ArrayList<Larva> listLarva, String dirImageTemp, int NUM_FRAMES_ROLLING, double MIN_ROLLING_SPEED, double MIN_ROLLING_DISTANCE )
	{
		int frameIdRollingBegin = 0; // the frame Id a larva start rolling
		int frameIdRollingEnd = 0; // the frame Id a larva start rolling
		
		ArrayList<Integer> rollingFrameIds = new ArrayList<Integer>();
		// the frame id from which the speed of larva is tested
		int frameIdSpeedBegin = 0; 
		// the frame id to which the speed of larva is tested
		int frameIdSpeedEnd = 0; 
		// the distance checked for testing the speed of larva for a few frames.
		double distanceSpeed = 0; 
		double distanceSpeedSet = NUM_FRAMES_ROLLING * MIN_ROLLING_SPEED;
		// whether start testing rolling
		Boolean isStartTest = false;
		// whether the sideways accumulated distance is greater than preset threshold
		Boolean isSidewaysAccuEnough = false;
		int frameIdCurrent = 0;
		int frameIdSign = 0;
		
		for(int i = listLarva.size() - 1; i >= 0; i--)
		{
			distanceSpeed = 0;
			frameIdCurrent = listLarva.get(i).getFrameId();
			frameIdSign = listLarva.get(i).getNumberSign();
			
			//frameIdRollingStart = listLarva.get(i).getFrameId();
			if(listLarva.get(i).getDistanceSidewaysAccumulate() >= MIN_ROLLING_DISTANCE && isStartTest == false)
			{
				isStartTest = true;
				isSidewaysAccuEnough = true;
				frameIdRollingEnd = frameIdCurrent;
			}
			
			// if it has more than NUM_FRAMES_ROLLING for that sign
			if(isSidewaysAccuEnough)
			{
				if( frameIdCurrent - NUM_FRAMES_ROLLING >= frameIdSign )
				{
					frameIdSpeedEnd = frameIdCurrent;
					frameIdSpeedBegin = frameIdCurrent - NUM_FRAMES_ROLLING;
				}else
				{
					frameIdSpeedEnd = frameIdSign + NUM_FRAMES_ROLLING;
					frameIdSpeedBegin = frameIdSign;
				}
				
				System.out.println("[Rolling Test for] i: "+ i +", frameIdCurrent:"+frameIdCurrent+", frameIdSpeedBegin:"+frameIdSpeedBegin+",frameIdSpeedEnd:"+frameIdSpeedEnd+",frameIdSign:"+frameIdSign);
				
				int indexFrameIdSpeedBegin = 0;
				int indexFrameIdSpeedEnd = 0;
				for(int k = 0; k < listLarva.size(); k++)
				{
					if(listLarva.get(k).getFrameId() == frameIdSpeedBegin)
						indexFrameIdSpeedBegin = k;
					
					if(listLarva.get(k).getFrameId() == frameIdSpeedEnd)
						indexFrameIdSpeedEnd = k;
					
				}
				
				distanceSpeed = listLarva.get(indexFrameIdSpeedEnd).getDistanceSidewaysAccumulate() - 
						listLarva.get(indexFrameIdSpeedBegin).getDistanceSidewaysAccumulate();

				listLarva.get(indexFrameIdSpeedBegin).setDistanceSpeed(distanceSpeed);
				
				System.out.println("[Rolling Test for](After) distanceSpeed: "+ distanceSpeed +", distanceSpeedSet:"+distanceSpeedSet+", indexFrameIdSpeedBegin:"+indexFrameIdSpeedBegin+",indexFrameIdSpeedEnd:"+indexFrameIdSpeedEnd);
				
				if(distanceSpeed < distanceSpeedSet )
				{
					frameIdRollingBegin = frameIdSpeedEnd;
					rollingFrameIds.add(frameIdRollingEnd);
					rollingFrameIds.add(frameIdRollingBegin);
					isSidewaysAccuEnough = false;
					isStartTest = false;
				}
				
				if(frameIdCurrent == frameIdSign )
				{
					frameIdRollingBegin = frameIdSign;
					rollingFrameIds.add(frameIdRollingEnd);
					rollingFrameIds.add(frameIdRollingBegin);
					isSidewaysAccuEnough = false;
					isStartTest = false;
				}

			}	
		}
		
		TextFileWriter.writeToFile("The Result for Rolling: ", dirImageTemp+"result.txt");
		
		for(Integer num : rollingFrameIds)
		{
			System.out.println("Rolling frames: "+ num);
			TextFileWriter.writeToFile("Rolling frames: "+ Integer.toString(num), dirImageTemp+"result.txt");
		}
	}
	
	
//	public static void saveDataVisualization(ArrayList<Larva> listLarva, String dirImageTemp)
//	{
//		int numFrameShow = 20; // the number of frames show in the chart
//
//		// add Sideways and curling chart
//		for(int i = 0; i < listLarva.size(); i++)
//		{
//			int frameId = listLarva.get(i).getFrameId();
//
//			DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
//			DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
//			DefaultCategoryDataset dataset3 = new DefaultCategoryDataset();
//			
//			int indexStart = i - numFrameShow / 2;
//			
//			if( indexStart < 0) 
//				indexStart = i;
//			
//			int indexEnd = i + numFrameShow / 2;
//			if( indexEnd > listLarva.size() )
//				indexEnd = listLarva.size();
//			
//			for(int j = indexStart; j < indexEnd; j++)
//			{
//				double sidewaysGet = listLarva.get(j).getDistanceSidewaysEndPts();
//				int frameIdGet = listLarva.get(j).getFrameId();
//				double curlGet = listLarva.get(j).getCurl();
//				
//				if( !listLarva.get(j).getIsCurlPos() )
//				{
//					sidewaysGet = -sidewaysGet;
//					curlGet = -curlGet;
//				}
//				
//		        dataset1.addValue(sidewaysGet, "Sideways", Integer.toString(frameIdGet));
//		        dataset2.addValue(curlGet, "Curl", Integer.toString(frameIdGet));
//		        dataset2.addValue(0, "0", Integer.toString(frameIdGet));
//			}
//			
////	        BarLineChart barLineChart = new BarLineChart(frameId, "Sideways Distance vs. Curl", dataset1, dataset2, dataset3);
//	        
//	        int width = 1000; /* Width of the image */
//	        int height = 240; /* Height of the image */ 
//	        File fileChart = new File( dirImageTemp+"AChart_"+frameId+"_"+"BarLineChart.jpeg" ); 
//	        try {
//				ChartUtilities.saveChartAsJPEG( fileChart , barLineChart.getChart() , width , height );
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
	
}
