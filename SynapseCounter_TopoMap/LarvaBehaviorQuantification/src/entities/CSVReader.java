package entities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CSVReader 
{

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
					System.out.print("Y");
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
	
	
    public static void main(String[] args) {

//        String csvFile = "E:/3/training_out/out.csv";
//        BufferedReader br = null;
//        String line = "";
//        String cvsSplitBy = ",";
//
//        try {
//
//            br = new BufferedReader(new FileReader(csvFile));
//            while ((line = br.readLine()) != null) {
//
//                // use comma as separator
//                String[] country = line.split(cvsSplitBy);
//
//                System.out.println("Country [code= " + country[4] + " , name=" + country[5] + "]");
//
//            }
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

    }

}