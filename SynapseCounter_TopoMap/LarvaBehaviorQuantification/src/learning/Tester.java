package learning;

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import org.ejml.simple.SimpleMatrix;
import entities.CSVReader;
import entities.TrainingData;
import file.CSVWriter;
import file.ImageSaver;
import ij.ImagePlus;
import ij.process.ImageConverter;
import manager.AnnotationManager;
import manager.FileManager;
import manager.ImageManager;
import manager.MathManager;
import manager.PropertyManager;
import manager.StringManager;
import manager.VectorManager;
import segmentation.CandidateCase;
import segmentation.CandidateLarva;
import segmentation.LarvaImage;

public class Tester
{
	public static String csvFileOut = PropertyManager.getPath() + "test_out/aOut.csv";
	public static String imageDirIn = PropertyManager.getPath() + "segmentation/output_segments/";
	public static String imageTestDir = PropertyManager.getPath() + "testing_larva/";
	
	public static void test(TrainingData trainingData, LarvaImage larvaImage)
	{
		// delete all files in this directory
		FileManager.deleteAllFiles(StringManager.getPath(csvFileOut));
//		FileManager.deleteAllFiles( imageTestDir );
		
		Tester tester = new Tester();
		
		int segmentIdGlobal = 0;
		double sumProbability1 = 0; // the sum of probability 1
		double sumProbability2 = 0; // the sum of probability 2
		
		for(CandidateCase candidateCase : larvaImage.candidateCases)
		{
			sumProbability1 = 0;
			sumProbability2 = 0;
			
			for(CandidateLarva candidateLarva : candidateCase.candidateLarvae)
			{
				// get the dimension data
				ArrayList<Double> larvaData = Descriptor.getDimensionData(candidateLarva.imagePlus
						, Tester.csvFileOut, segmentIdGlobal++, Descriptor.NUM_POINTS, false);
				
				// if the return larva descriptor is not null
				if(larvaData != null)
				{
					String line;

					CSVWriter csvWriter = new CSVWriter(csvFileOut);

					line = "";

					for (int i = 0; i < larvaData.size(); i++)
					{
						if (i == 0)
							line += larvaData.get(i);
						else
							line += "," + larvaData.get(i);
					}

					csvWriter.writeln(line);
					
					// set the larva feature data
					candidateLarva.larvaFeatureData = larvaData;
					
					// convert the double array to SimpleMatrix
					SimpleMatrix larvaTest = VectorManager.newSimpleMatrix( candidateLarva.larvaFeatureData ); 
					
					// get the possibility that show how likely the larva evaluate to be a larva
					double[] probability = PCACalculator.test( trainingData, larvaTest );
					
					candidateLarva.probability1 = probability[0];
					candidateLarva.probability2 = probability[1];
					sumProbability1 += candidateLarva.probability1;
					sumProbability2 += candidateLarva.probability2;
				// if the return larva descriptor is not null
				}else
				{
					// set the larva feature data
					candidateLarva.larvaFeatureData = larvaData; // null
					// just give a big number because the segment isn't possible to be a
					// larva since it have less than 10 skeleton points.
					candidateLarva.probability1 = 999999999;
					candidateLarva.probability2 = 0;
					sumProbability1 += candidateLarva.probability1;
					sumProbability2 += candidateLarva.probability2;
				}
			}
			candidateCase.probability1 = sumProbability1;
			candidateCase.probability2 = sumProbability2;
		}
		
		saveProbabilityImage(tester, larvaImage);
		
		System.out.println("(Larva) trainTest completed!");
	}
	
	public static void saveProbabilityImage(Tester tester, LarvaImage larvaImage)
	{
		// write all data about larvae's descriptors to the file.
		CSVWriter csvWriter = new CSVWriter(StringManager.getPath(Tester.csvFileOut) + "aCandidateCases.csv");
		csvWriter.writeln("Candidate cases and cadidate larvae");
			
		int indexCandidateLarva = 0;
		for(CandidateCase candidateCase : larvaImage.candidateCases)
		{
			indexCandidateLarva = 0;
			csvWriter.writeln(candidateCase.getCandidateCaseId() + ") Candidate cases[" 
					+ candidateCase.getCandidateCaseId() + "] # prob2:" + candidateCase.probability2
					+ " & prob1:" + candidateCase.probability1);
			
			for(CandidateLarva candidateLarva : candidateCase.candidateLarvae)
			{
				ImagePlus imagePlusSegment = candidateLarva.imagePlus.duplicate();
				
				// convert the image plus to RGB image
				ImageConverter imageConverterOrg = new ImageConverter(imagePlusSegment);
				imageConverterOrg.convertToRGB();
				// calculate both part 1 and the complete probabilities for a testing larva
				String str1 = Double.toString( MathManager.get2DecimalPoints( candidateLarva.probability1)  );
				String str2 = Double.toString( candidateLarva.probability2 );
				
				// mark both part 1 and the complete probabilities to the image
				AnnotationManager.annotate(imagePlusSegment, new Point(10,10), str1, Color.red, 8);
				AnnotationManager.annotate(imagePlusSegment, new Point(10,40), str2, Color.red, 8);
				
//				AnnotationManager.annotate(imagePlusSegment, new Point(10,85), "o"+larvaImage.imageId+"_" +larvaSeg.candidateLarvaeId, Color.red, 8);
				AnnotationManager.annotate(imagePlusSegment, new Point(10,85), "cadidateLarva["+candidateCase.getCandidateCaseId()+"]["+indexCandidateLarva+"]", Color.red, 8);
				
				// save the images
				ImageSaver.saveImagesWithPath(StringManager.getPath(tester.getCsvFileOut()) + "o"+larvaImage.imageId+"_" +candidateLarva.candidateLarvaeId+".jpg", imagePlusSegment);
			
				csvWriter.writeln("    Candidate larva[" + candidateCase.getCandidateCaseId() + "][" +indexCandidateLarva+"] - " 
				+ "o"+larvaImage.imageId+"_" +candidateLarva.candidateLarvaeId+".jpg" + " # prob2:" + candidateLarva.probability2
				+ " & prob1:" + candidateLarva.probability1);
				
				indexCandidateLarva ++;
			}
		}
		
		double maxProbability = 0;
		int indexCandidateCase = 0;

		// get the segmentation segments-combination approach with the highest probability
		// from all the segments-combination approaches.
		for (int j = 0; j < larvaImage.candidateCases.size(); j++)
		{
			CandidateCase candidateCase = larvaImage.candidateCases.get(j);

			// always get the highest probability and save the sum of the probability and
			// the index of the segments-combination approach.
			if (candidateCase.probability2 > maxProbability)
			{
				maxProbability = candidateCase.probability2;
				indexCandidateCase = j;
			}
		}
		
		CandidateCase candidateCase = larvaImage.candidateCases.get(indexCandidateCase);
		
		csvWriter.writeln("*) Highest Case: Candidate cases[" 
				+ candidateCase.getCandidateCaseId() + "] - prob2:" + candidateCase.probability2
				+ "- prob1:" + candidateCase.probability1);
	}
	
	public String getCsvFileOut()
	{
		return csvFileOut;
	}

	public void setCsvFileOut(String csvFileOut)
	{
		this.csvFileOut = csvFileOut;
	}

}
