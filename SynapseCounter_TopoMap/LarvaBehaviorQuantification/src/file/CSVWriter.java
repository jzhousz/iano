package file;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import entities.Larva;

/**
* The class used to write data to CSV files.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class CSVWriter {

	private String filePath = "";

	public CSVWriter(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Write a 2D array to a file.
	 * 
	 * @param val The 2D double array.
	 */
	public void writeDouble2DArray(double[][] arr)
	{
		for(double[] val : arr)
		{
			writeDoubleArray(val);
		}
	}
	
	/**
	 * Write a double array vertically.
	 * 
	 * @param val The double array.
	 */
	public void writeDoubleArrayVertical(double[] val)
	{
		String line = "";
		
		for(int i = 0; i < val.length; i++)
		{
			line = Double.toString(val[i]);
			writeln(line);
		}
	}
	
	/**
	 * Write a double array.
	 * 
	 * @param val The double array.
	 */
	public void writeDoubleArray(double[] val)
	{
		String line = "";
		
		for(int i = 0; i < val.length; i++)
		{
			line += Double.toString(val[i]);
			
			// if i is not the index of the last element
			if(i < val.length - 1)
			{
				line += ",";
			}
		}
		
		writeln(line);
	}
	
	/**
	 * Write a line to the CSV file.
	 * 
	 * @param text The text to be written to a CSV file.
	 * @return void
	 */
	public void writeln(String text)
	{

		PrintWriter textWriter = null;

		try
		{
			textWriter = new PrintWriter(new FileWriter(filePath, true));
		} catch (IOException ioe)
		{
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

		try
		{
			textWriter = new PrintWriter(new FileWriter(filePath, true));
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		textWriter.print(text);

		textWriter.close();
	}
	
	/**
	* Save the main data to the main CSV file.
	* 
	* @param listLarva The list frames containing the larva.
	* @param aviFile The AVI file name.
	* @param date The date saved.
	* @param LARVA_ID The larva id.
	* @param isOutputCurl Whether save curl information.
	* @param isOutputSpeed Whether save speeds.
	* @param isOutputRoll Whether save rolling information.
	*/
	public void saveMainData(ArrayList<Larva> listLarva, String aviFile, 
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
		
		Larva larva = null;
	
		String line = "";
		
		for (int i = 0; i < listLarva.size(); i++) 
		{
			
			larva = listLarva.get(i);
			
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
	
	/**
	* Save the data to the CSV file.
	* 
	* @param listLarva The list frames containing the larva.
	* @param aviFile The AVI file name.
	* @param date The date saved.
	* @param LARVA_ID The larva id.
	*/
	public void saveData(ArrayList<Larva> listLarva, String aviFile, 
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
				+ "isRolling,"
				+ "angleCenterEnd1End2,"
				+ "angleCenterEnd2End1,"
				+ "angleAvgCenterEnd1,"
				+ "angleAvgCenterEnd2,"
				+ "isMoveRight"
				);
		
		Larva larva = null;
	
		for (int i = 0; i < listLarva.size(); i++) 
		{
			larva = listLarva.get(i);
	
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
					+ larva.getIsRolling() + ","
					+ larva.getAngleCenterEnd1End2() + ","
					+ larva.getAngleCenterEnd2End1() + ","
					+ larva.getAngleAvgCenterEnd1() + ","
					+ larva.getAngleAvgCenterEnd2() + ","
					+ larva.getIsMoveRight()
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
