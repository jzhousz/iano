package annotool.io;

import java.io.*;
import java.util.Scanner;

/** 
 * Read labels (targets) from a file.
 */

/* The file is formatted as below (0909080): 1st line: annotation labels,  other line: target values, then file name
a150 a273
1  0   k150_a150_4-23-07_L10_Sum.lsm__AJ-F__GF.tif
1  0   k150_a150_4-23-07_L11_Sum.lsm__AJ-F__GF.tif
1  0   k150_a150_4-23-07_L12_Sum.lsm__AJ-F__GF.tif
1  0   k150_a150_4-23-07_L7_Sum.lsm__AJ-F__GF.tif
1  0   k150_a150_4-23-07_L8_Sum.lsm__AJ-F__GF.tif
0  1   k150_a273_8-14-07_L10_B1_GR1_R1.lsm__AJ-F__GF.tif
0  1   k150_a273_8-14-07_L11_B1_GR1_R1.lsm__AJ-F__GF.tif
0  1   k150_a273_8-14-07_L13_B1_GR1_R1.lsm__AJ-F__GF.tif
0  1   k150_a273_8-14-07_L8_B1_GR1_R1.lsm__AJ-F__GF.tif
0  1   k150_a273_8-14-07_L9_B1_GR1_R1.lsm__AJ-F__GF.tif

Added July 2011: The second line contains pairs of digital targets and the corresponding class names.
The class names are used in reports.  
For example:
1:Yes 0:No
1:a150 2:a273 3:ato
 ****/

public class LabelReader
{
	int numOfAnno = 0;
	int maxNumOfClass = 0;
	int length;
	java.util.ArrayList<String> annotations = null;
	java.util.HashMap<String, String> classNames = null;

	public LabelReader(int nsample, java.util.ArrayList<String> annoLabels)
	{
		length = nsample;
		annotations = annoLabels;
	}

	//read in a matrix of binary targets, one column per annotation
    //Example: 0 1 0 1 1
    //         1 1 1 0 0
	//return: targets = new int[nanno][nsample]
	//
    public int[][] getTargets(String targetfilename, String[] filenames) throws FileNotFoundException,NumberFormatException
	{
           int nsample = 0;
           int nanno = 0;
           int[][] targets;
           Scanner scanner = null;
           //annotations = null;
           
           try
		   {
               scanner = new Scanner(new File(targetfilename));
	       }catch(FileNotFoundException e)
	       {
               e.printStackTrace();
               throw e; //System.exit(1);
	       }
           //scan the first line to get # of annotations.
	       if (annotations == null) 
	         annotations = new java.util.ArrayList<String>();
	       if(scanner.hasNextLine())
           {
			   String line = scanner.nextLine();
			   Scanner lineScanner = new Scanner(line);
               while(lineScanner.hasNext())
               {
                  if (annotations != null) 
                	  annotations.add(lineScanner.next());
                  nanno++;
               }
               lineScanner.close();
           }
	       //scan the second line to get names of classes
	       if (classNames == null)
	    	   classNames = new java.util.HashMap<String, String>();
	       if(scanner.hasNextLine())
	       {
			   String line = scanner.nextLine();
			   Scanner lineScanner = new Scanner(line);
               while(lineScanner.hasNext())
               {
                  if (classNames!= null)
                  {
                	  String pair[] = lineScanner.next().split(":");
                	  classNames.put(pair[0],pair[1]);
                  }
               }
               lineScanner.close();
	       }
	       
           //scan the rest lines to get nsample.
           while(scanner.hasNextLine())
           {
			   scanner.nextLine();
               nsample ++;
           }
           scanner.close();

		   System.out.println("num of rows in target file: " + nsample);
		   System.out.println("num of images: " + length);
		   System.out.println("num of annotations (columns): " + nanno);
	       if(nsample != length)
           {
               System.out.println("The number of rows in the target file must be the same as the number of observations");
               throw new NumberFormatException("The number of rows in the target file must be the same as the number of observations");               
               //System.exit(0);
           }

           //go ahead
           numOfAnno = nanno;
           //targets = new int[nsample][nanno];
           targets = new int[nanno][nsample];  //one row per anno. Easier for passing to classifier.

   		   try
   		   {
                 scanner = new Scanner(new File(targetfilename));
   	       }catch(FileNotFoundException e)
   	       { //should have been found in the first time
                  e.printStackTrace();
                  throw e;
   	       }
           //skip the first lines of annotations and classnames.
   	       scanner.nextLine();
   	       scanner.nextLine();
   	       
   	       //rewrite to consider filenames; 
   	       //Note: the order of data array replies on the order of reading files in dir, not the order in targetfile
   	       int[] linetarget = new int[nanno];
   	       String linename = null;
   	       //read in the filename, and find the index of the corresponding file
           for(int i=0; i<nsample; i++)
           {
        	   for(int j=0; j<nanno; j++)
        		   linetarget[j] =  scanner.nextInt();
        	   //last item on the line is the file name
        	   linename = scanner.next();
        	   //search for the index of that file name
        	   int index = 0;
        	   for(int k=0; k<filenames.length; k++)
        	       if (linename.equals(filenames[k]))
        	    	   index = k;
        	   //now set the targets for that file
               for(int j=0; j<nanno; j++)
                  targets[j][index] = linetarget[j];
           }   
           scanner.close();
           
           calNumOfClasses(targets);

           return targets;
	}

	//read in the list of annotations (a list of strings)
    //for the purpose of displaying output etc.
	public java.util.ArrayList<String> getAnnotations() throws Exception
	{
		if (annotations == null)
		{
	          System.out.println("Error: Reading annotation and targets first!");
	          throw new Exception("Error: Reading annotation and targets first!");
		}
		return annotations;
    }

	public java.util.HashMap<String, String> getClassnames() throws Exception
	{
		if (classNames == null)
		{
	          System.out.println("Error: The classnames are not yet read!");
	          throw new Exception("Error: The classnames are not yet read!");
		}
		return classNames;
    }
	
    public int getNumOfAnnotations()
    {
	   return numOfAnno;
    }

    public int getNumOfClasses()
    {
	   return maxNumOfClass;
    }
    
    public java.util.ArrayList<Integer> calNumOfClasses(int[][] targets)
    {
       //one row per annotation.
    	int maxNum = 0;
    	java.util.ArrayList<Integer> infoNumOfClass = new java.util.ArrayList<Integer>();
        for(int i=0; i< numOfAnno; i++)
        {
        	java.util.ArrayList<Integer> labels = new java.util.ArrayList<Integer>();
        	for(int j=0; j < length; j++)
        		if (!labels.contains(targets[i][j]))
        			labels.add(targets[i][j]);
        	if (maxNum < labels.size())
        		maxNum = labels.size();
        	infoNumOfClass.add(labels.size());
        }
        this.maxNumOfClass = maxNum;
        return infoNumOfClass;
    }

    //an utility method to return number of classes for one annotation target
    public static int infoNumOfClasses(int[] targets)
    {
        java.util.ArrayList<Integer> labels = new java.util.ArrayList<Integer>();
        for(int j=0; j < targets.length; j++) 
        	if (!labels.contains(targets[j]))
        		labels.add(targets[j]);
        return labels.size();
    }
    
 }
