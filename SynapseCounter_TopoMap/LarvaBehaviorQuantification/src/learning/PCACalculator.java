package learning;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import org.ejml.simple.SimpleMatrix;

import entities.CSVReader;
import entities.TrainingData;
import file.CSVWriter;
import manager.MathManager;
import manager.StringManager;
import manager.VectorManager;
import pca.DwVector;
import pca.PCA;

/**
 * The class used to train a statistical shape model and test images.
 * 
 * @author Yaoguang Zhong
 * @version 1.1
 * @since 01-21-2017
 */
public class PCACalculator
{
	private PCA pca = null;
	
	/**
	 * Train the statistical shape model.
	 * @param fileOut The training file containing the features of the training larvae.
	 */
	public void train(String fileOut)
	{
		ArrayList<String[]> fieldsArr = CSVReader.readCSV(fileOut);
		
		DwVector vectors[] = new DwVector[fieldsArr.size()];
		
		double[][] larvaeDescriptor = new double[fieldsArr.size()][fieldsArr.get(0).length];

		// initialize DwVector array so as to use PCA class
		for (int j = 0; j < fieldsArr.size(); j++)
		{
			String[] fields = fieldsArr.get(j);
			double fieldsDouble[] = new double[fieldsArr.get(0).length];

			for (int i = 0; i < fields.length; i++)
			{
				try
				{
					fieldsDouble[i] = Double.parseDouble(fields[i]);
				} catch (NumberFormatException ex)
				{
					int num = Integer.parseInt(fields[i]);
					
					fieldsDouble[i] = (double) num;
				}
				
				System.out.println("(PCACalculator.java) fieldsDouble[i]: " + fieldsDouble[i]);
			}

			DwVector vector = new DwVector(fieldsDouble);
			vectors[j] = vector;
			
			larvaeDescriptor[j] = fieldsDouble;
		}

		// new a PCA instance
		pca = new PCA(vectors);
		System.out.println("(PCACalculator.java) after: new PCA(vectors)");
		pca.compute(); // start PCA calculation

		// the eigenvector in double form
		double[][] eigenvecs = new double[pca.dim_used][pca.evec[0].evec.length]; 
		
		double[] eigenValues = new double[pca.dim_used];
		
		for(int i = 0; i < pca.dim_used; i ++)
		{
			eigenvecs[i] = pca.evec[i].evec;
			eigenValues[i] = pca.evec[i].eval;
		}
		
		CSVWriter wrt = new CSVWriter(StringManager.getPath(fileOut)+"eigenvector.csv");
		wrt.writeDouble2DArray(eigenvecs);
		
		CSVWriter wrtValue = new CSVWriter(StringManager.getPath(fileOut)+"eigenvalue.csv");
		wrtValue.writeDoubleArrayVertical(eigenValues);
		
		// ---------- get weights coresponding to eigenvector --------
		for(double[] larva : larvaeDescriptor)
		{
			SimpleMatrix larvaMatr = VectorManager.newSimpleMatrix(larva);
			double[] probilities = saveWeights(larvaMatr);
		}
		// -------------- end -----------------------------------------
		
		// ---------- save the mean larva and larva eigen weight --------
		
		// calculate the mean of SimpleMatrix type
		SimpleMatrix matrMean = new SimpleMatrix(VectorManager.convertTo2DHorizontal(pca.mean.v)); // the PCA mean vector
		
		CSVWriter writerLarvaMean = new CSVWriter(StringManager.getPath(Trainer.csvFileOut)+"larvaMean.csv");
		writerLarvaMean.writeDoubleArray(pca.mean.v);
		
		double[] probilities = saveWeights(matrMean);
		// -------------- end -----------------------------------------
		
		// the eigenvector in SimpleMatrix form
		SimpleMatrix eigenvecMatr = new SimpleMatrix(eigenvecs);
		
		TrainingData trainingData = new TrainingData();
		trainingData.setEigenVectors(eigenvecs);
		trainingData.setEigenValues( eigenValues );
		trainingData.setMeanLarva( pca.mean.v );
		
		try
		{
			FileOutputStream fileOutSeri = new FileOutputStream(StringManager.getPath(Trainer.csvFileOut) 
					+ "trainingData.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOutSeri);
			out.writeObject(trainingData);
			out.close();
			fileOutSeri.close();
		} catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	/**
	 * Save the weights to a double array.
	 * @param larvaTest The test larva.
	 * @return The probability.
	 */
	public double[] saveWeights(SimpleMatrix larvaTest)
	{
		double[][] eigenvecs = new double[pca.dim_used][pca.evec[0].evec.length]; // the eigenvector of double type
		
		SimpleMatrix[] eigenvec = new SimpleMatrix[pca.dim_used];
		
		// initialize eigenvector of SimpleMatrix type
		for(int i = 0; i < pca.dim_used; i ++)
		{
			eigenvecs[i] = pca.evec[i].evec;
			double[][] vec = new double[1][pca.evec[i].evec.length];
			vec[0] = pca.evec[i].evec;
			eigenvec[i] = new SimpleMatrix(vec);
		}
		
		// calculate the mean of SimpleMatrix type
		SimpleMatrix matrMean = new SimpleMatrix(VectorManager.convertTo2DHorizontal(pca.mean.v)); // the PCA mean vector
		
		SimpleMatrix matrLarva = larvaTest; // the test larva
		
		SimpleMatrix matrDiff = matrLarva.minus(matrMean);
		
		double[][] wts = new double[1][pca.dim_used];
		
		// calculate the weight matrix of SimpleMatrix type
		for(int i = 0; i < pca.dim_used; i ++)
		{
			wts[0][i] = eigenvec[i].dot( matrDiff );
		}
		
		CSVWriter writerWeight = new CSVWriter(StringManager.getPath(Trainer.csvFileOut)+"eigenWeight.csv");
		writerWeight.writeDouble2DArray(wts);
		
		SimpleMatrix wtsMatr_used = new SimpleMatrix(wts);
		
		SimpleMatrix eigenvecsMatr_used = new SimpleMatrix(eigenvecs);
		
		SimpleMatrix matrDiff_rec = wtsMatr_used.mult( eigenvecsMatr_used );
		
		SimpleMatrix larva_rec = matrMean.plus( matrDiff_rec ) ;
		
		double[][] diaArr = new double[pca.dim_used][pca.dim_used];
		
		// calculate the dia matrix of SimpleMatrix type
		for(int i = 0; i < pca.dim_used; i ++)
		{
			for(int j = 0; j < pca.dim_used; j ++)
			{
				if(i == j)
					diaArr[i][j] = pca.evec[i].eval;
				else
					diaArr[i][j] = 0;
			}
		}
		
		SimpleMatrix diaMatr = new SimpleMatrix(diaArr);
		
		SimpleMatrix diaMatr_inverse = diaMatr.invert();
		
		// calculate possibility
		SimpleMatrix probilityMatr1 = wtsMatr_used.mult(diaMatr_inverse);
		
		SimpleMatrix probilityMatr2 = probilityMatr1.mult(wtsMatr_used.transpose());
		
		double probility = Math.exp(probilityMatr2.get(0, 0) * -1);
		
		double[] probilities = {probilityMatr2.get(0, 0), probility };
		return probilities;
	}
	
	/**
	 * Test an image containing adjoining larvae.
	 * @param trainingData The training data containing features of the training larvae.
	 * @param larvaTest The matrix representation of the test larva.
	 * @return The probability of the test larva.
	 */
	public static double[] test(TrainingData trainingData, SimpleMatrix larvaTest )
	{
		int dim_used = trainingData.getEigenValues().length;
		int eigenVectorLen = trainingData.getEigenVectors()[0].length;
		
//		double[][] eigenvecs = new double[dim_used][eigenVectorLen]; // the eigenvector of double type
		
		double[][] eigenvecs = trainingData.getEigenVectors();
		
		SimpleMatrix[] eigenvec = new SimpleMatrix[dim_used];
		
		// initialize eigenvector of SimpleMatrix type
		for(int i = 0; i < dim_used; i ++)
		{
			eigenvecs[i] = eigenvecs[i];
			double[][] vec = new double[1][eigenVectorLen];
			vec[0] = eigenvecs[i];
			eigenvec[i] = new SimpleMatrix(vec);
		}
		
		// calculate the mean of SimpleMatrix type
		SimpleMatrix matrMean = new SimpleMatrix(VectorManager.convertTo2DHorizontal(trainingData.getMeanLarva())); // the PCA mean vector
		SimpleMatrix matrLarva = larvaTest; // the test larva
		
		SimpleMatrix matrDiff = matrLarva.minus(matrMean);
		
		double[][] wts = new double[1][dim_used];
		
		// calculate the weight matrix of SimpleMatrix type
		for(int i = 0; i < dim_used; i ++)
		{
			wts[0][i] = eigenvec[i].dot( matrDiff );
		}
		
		SimpleMatrix wtsMatr_used = new SimpleMatrix(wts);
		
		SimpleMatrix eigenvecsMatr_used = new SimpleMatrix(eigenvecs);
		
		SimpleMatrix matrDiff_rec = wtsMatr_used.mult( eigenvecsMatr_used );
		
		SimpleMatrix larva_rec = matrMean.plus( matrDiff_rec ) ;
		
		double[][] diaArr = new double[dim_used][dim_used];
		
		// calculate the dia matrix of SimpleMatrix type
		for(int i = 0; i < dim_used; i ++)
		{
			for(int j = 0; j < dim_used; j ++)
			{
				if(i == j)
					diaArr[i][j] = trainingData.getEigenValues()[i];
				else
					// dianose elements is 0
					diaArr[i][j] = 0; 
			}
		}
		
		SimpleMatrix diaMatr = new SimpleMatrix(diaArr);
		
		SimpleMatrix diaMatr_inverse = diaMatr.invert();
		
		// calculate possibility
		SimpleMatrix probilityMatr1 = wtsMatr_used.mult(diaMatr_inverse);
		
		SimpleMatrix probilityMatr2 = probilityMatr1.mult(wtsMatr_used.transpose());
		
//		double probility = Math.exp(probilityMatr2.get(0, 0) * -1);
		double probility = Math.exp(probilityMatr2.get(0, 0) * -0.5);
		
		double[] probilities = {probilityMatr2.get(0, 0), probility };
		return probilities;
	}

	/**
	 * Getter
	 */
	public PCA getPca()
	{
		return pca;
	}
	
}
