package file;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import entities.FeaturesOfLarva;
import manager.StringManager;

public class CSVWriter {

	private String filePath = "";

	public CSVWriter(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Write a line to the CSV file.
	 * 
	 * @param text The text to be written to a CSV file.
	 * @return void
	 */
	public void writeln(String text) {
		
		PrintWriter textWriter = null;

		try {
			textWriter = new PrintWriter(new FileWriter(filePath, true));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		textWriter.println(text);

		textWriter.close();
	}
	
	/**
	 * Write text to the CSV file. Use System.lineSeparator() if want
	 * to start a new line.
	 * 
	 * @param text The text to be written to a CSV file.
	 * @return void
	 */
	public void write(String text) 
	{
		PrintWriter textWriter = null;

		try {
			textWriter = new PrintWriter(new FileWriter(filePath, true));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		textWriter.print(text);

		textWriter.close();
	}
	
	public void saveMainData(ArrayList<FeaturesOfLarva> listFeaturesOfLarva, String aviFile, 
			Date date, String LARVA_ID, boolean isOutputCurl, boolean isOutputSpeed, boolean isOutputRoll )
	{
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		String head = "video,N,frameId";
		
		if(isOutputCurl)
			head += ",curl";
		
		if(isOutputSpeed)
			head += ",sideways_speed";
		
		if(isOutputRoll)
			head += ",isRolling";
		
		writeln(head);
		
		FeaturesOfLarva larva = null;
	
		String line = "";
		
		for (int i = 0; i < listFeaturesOfLarva.size(); i++) 
		{
			
			larva = listFeaturesOfLarva.get(i);
			
			if(larva.getNeedRecord())
			{
				line += aviFile + "," + LARVA_ID + "," + larva.getFrameId();
						
				if(isOutputCurl)
					line += "," + larva.getCurl();
				
				if(isOutputSpeed)
					line += "," + larva.getDistanceSidewaysEndPts();
				
				if(isOutputRoll)
					line += "," + larva.getIsRolling();
				
				line += "\n";
			}
		}
		
		writeln(line);
		
	}
	
	
	public void saveData(ArrayList<FeaturesOfLarva> listFeaturesOfLarva, String aviFile, 
			Date date, String LARVA_ID )
	{
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

		writeln(
				"video,"
				+ "N,"
				+ "frameId,"
				+ "curl,"
				+ "xCenterPoint,yCenterPoint,"
				+ "isValid,"
				+ "XEndPoint1,YEndPoint1,"
				+ "XEndPoint2,YEndPoint2,"
				+ "distanceEndPoint1,"
				+ "distanceEndPoint2,"
				+ "isEndPoint1WinPixelLevel,"
				+ "isEndPoint1WinDistanceMoved,"
				+ "distanceCenterPoint,"
				+ "distanceSideways,"
				+ "isSidewaysForward,"
				+ "isSidewaysEndPts,"
				+ "distanceSidewaysEndPts,"
				+ "isSidewaysEndPtsForward,"
				+ "distanceSidewaysShawn,"
				+ "area,"
				+ "distanceSidewaysSign,"
				+ "curlSign,"
				+ "isDistSidewaysPos,"
				+ "isCurlPos,"
				+ "DistanceSidewaysAccumulate,"
				+ "DistanceSidewaysTotal,"
				+ "isRolling,"
				+ "numberSign,"
				+ "distanceSpeed,"
				+ "lengthSkeleton,"
				+ "angleEndPointsLinear,"
				+ "needRecord,"
				+ "time,"
				+ "rollingActual,"
				+ "angleEndPt1,"
				+ "angleEndPt2,"
				+ "isPreviousValid,"
				+ "isRolling"
				);
		
		FeaturesOfLarva larva = null;
	
		for (int i = 0; i < listFeaturesOfLarva.size(); i++) 
		{
			larva = listFeaturesOfLarva.get(i);
	
			writeln(
					aviFile + ","
					+ LARVA_ID + ","
					+ larva.getFrameId() + ","
					+ larva.getCurl() + ","
					+ larva.getCenterPointOnFullFrame().x + ","
					+ larva.getCenterPointOnFullFrame().y + ","
					+ larva.getIsValid() + ","
					+ larva.getEndPointsOnFullFrame().get(0).x + ","
					+ larva.getEndPointsOnFullFrame().get(0).y + ","
					+ larva.getEndPointsOnFullFrame().get(1).x + ","
					+ larva.getEndPointsOnFullFrame().get(1).y + ","
					+ larva.getDistanceEndPoint1() + ","
					+ larva.getDistanceEndPoint2() + ","
					+ larva.getIsEndPoint1WinPixelLevel() + ","
					+ larva.getIsEndPoint1WinDistanceMoved() + ","
					+ larva.getDistanceCenterPoint() + ","
					+ larva.getDistanceSideways() + ","
					+ larva.getIsSidewaysForward() + ","
					+ larva.getIsSidewaysEndPts() + ","
					+ larva.getDistanceSidewaysEndPts() + ","
					+ larva.getIsSidewaysEndPtsForward() + ","
					+ larva.getDistanceSidewaysShawn() + ","
					+ larva.getArea() + ","
					+ larva.getDistanceSidewaysSign() + ","
					+ larva.getCurlSign() + ","
					+ larva.getIsDistSidewaysPos() + ","
					+ larva.getIsCurlPos() + ","
					+ larva.getDistanceSidewaysAccumulate() + ","
					+ larva.getDistanceSidewaysTotal() + ","
					+ larva.getIsRolling() + ","
					+ larva.getNumberSign() + ","
					+ larva.getDistanceSpeed() + ","
					+ larva.getLengthSkeleton() + ","
					+ larva.getAngleEndPointsLinear() + ","
					+ larva.getNeedRecord() + ","
					+ dateFormat.format(date) + ","
					+ "false" + ","
					+ larva.getAngleEndPt1() + ","
					+ larva.getAngleEndPt2() + ","
					+ larva.getIsPreviousValid() + ","
					+ larva.getIsRolling()
					);	
			
		}
	}
	
	
	public static void main(String args[])
	 {
		CSVWriter csvWriter = new CSVWriter("Output/yao1.csv");
		
		csvWriter.write("test string, guang test, zhong test");
		csvWriter.write("test string2, guang test2, zhong test2");
		csvWriter.write("test string3, guang test3, zhong test3");
		csvWriter.write(System.lineSeparator());
		csvWriter.write("test string4, guang test4, zhong test4");
		csvWriter.write(System.lineSeparator());
		csvWriter.write("test string5, guang test5, zhong test5");

	 }

}
