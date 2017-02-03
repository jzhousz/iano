package entities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * The class used to read a CSV file.
 * 
 * @author Yaoguang Zhong
 * @version 1.1
 * @since 08-02-2016
 */
public class CSVReader
{
	/**
	 * Read CSV file and save the fields of the file to an array list of a string array.
	 * 
	 * @param csvFile The CSV file to be read.
	 * @return The array list of a string array
	 */
	public static ArrayList<String[]> readCSV(String csvFile)
	{
		ArrayList<String[]> fieldsArr = new ArrayList<String[]>();
		
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {
            br = new BufferedReader(new FileReader(csvFile));
            
            while ((line = br.readLine()) != null) 
            {
                // use comma as separator
                String[] fields = line.split(cvsSplitBy);
                
                try
				{
					float num = Float.parseFloat(fields[0]);
					fieldsArr.add(fields);
				}catch(NumberFormatException ex)
				{
					System.out.print("NumberFormatException");
				}
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return fieldsArr;
	}

}