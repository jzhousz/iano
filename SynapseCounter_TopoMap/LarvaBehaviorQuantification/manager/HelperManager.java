package manager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JTextArea;
import entities.FeaturesOfLarva;
import file.TextFileWriter;

public class HelperManager
{
	public static Date dateSys = null;
	public static DateFormat dateSysFormat = new SimpleDateFormat("HH:mm:ss");
	
	public static void showSysStatus(JTextArea textStatus, String status)
	{
//		dateSys = new Date();
//		HelperManager.showStatus(textStatus, "\n("+dateSysFormat.format(dateSys)+") "+status+"\n");
		dateSys = new Date();
		HelperManager.showStatus(textStatus, "\n("+dateSysFormat.format(dateSys)+") "+status+"\n");
	}
	
	public static void showStatus(JTextArea textStatus, String status)
	{
		System.out.print(status);
		ij.IJ.showStatus(status);
		textStatus.append(status);
		textStatus.setCaretPosition(textStatus.getDocument().getLength());
		textStatus.validate();
	}
	
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
	
	
	public static void setFramesDistance(ArrayList<FeaturesOfLarva> listFeaturesOfLarva, String dirImageTemp, int NUM_FRAMES_ROLLING, double MIN_ROLLING_SPEED, double MIN_ROLLING_DISTANCE )
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
		
		for(int i = listFeaturesOfLarva.size() - 1; i >= 0; i--)
		{
			distanceSpeed = 0;
			frameIdCurrent = listFeaturesOfLarva.get(i).getFrameId();
			frameIdSign = listFeaturesOfLarva.get(i).getNumberSign();
			
			//frameIdRollingStart = listFeaturesOfLarva.get(i).getFrameId();
			if(listFeaturesOfLarva.get(i).getDistanceSidewaysAccumulate() >= MIN_ROLLING_DISTANCE && isStartTest == false)
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
				for(int k = 0; k < listFeaturesOfLarva.size(); k++)
				{
					if(listFeaturesOfLarva.get(k).getFrameId() == frameIdSpeedBegin)
						indexFrameIdSpeedBegin = k;
					
					if(listFeaturesOfLarva.get(k).getFrameId() == frameIdSpeedEnd)
						indexFrameIdSpeedEnd = k;
					
				}
				
				distanceSpeed = listFeaturesOfLarva.get(indexFrameIdSpeedEnd).getDistanceSidewaysAccumulate() - 
						listFeaturesOfLarva.get(indexFrameIdSpeedBegin).getDistanceSidewaysAccumulate();

				listFeaturesOfLarva.get(indexFrameIdSpeedBegin).setDistanceSpeed(distanceSpeed);
				
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
	
	
//	public static void saveDataVisualization(ArrayList<FeaturesOfLarva> listFeaturesOfLarva, String dirImageTemp)
//	{
//		int numFrameShow = 20; // the number of frames show in the chart
//
//		// add Sideways and curling chart
//		for(int i = 0; i < listFeaturesOfLarva.size(); i++)
//		{
//			int frameId = listFeaturesOfLarva.get(i).getFrameId();
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
//			if( indexEnd > listFeaturesOfLarva.size() )
//				indexEnd = listFeaturesOfLarva.size();
//			
//			for(int j = indexStart; j < indexEnd; j++)
//			{
//				double sidewaysGet = listFeaturesOfLarva.get(j).getDistanceSidewaysEndPts();
//				int frameIdGet = listFeaturesOfLarva.get(j).getFrameId();
//				double curlGet = listFeaturesOfLarva.get(j).getCurl();
//				
//				if( !listFeaturesOfLarva.get(j).getIsCurlPos() )
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
