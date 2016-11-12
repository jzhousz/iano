package learning;

import java.util.ArrayList;
import org.ejml.simple.SimpleMatrix;

import entities.CSVReader;
import manager.MathManager;
import manager.VectorManager;
import pca.DwVector;
import pca.PCA;

public class PCACalculator
{
	private PCA pca = null;
//	private final String pcaFile = "E:/3/training_out/aOut.csv";
	
	public void train(String fileOut)
	{
		ArrayList<String[]> fieldsArr = CSVReader.readCSV(fileOut);
		
		DwVector vectors[] = new DwVector[fieldsArr.size()];

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
			}

			DwVector vector = new DwVector(fieldsDouble);
			vectors[j] = vector;
		}

		// new a PCA instance
		pca = new PCA(vectors);

		pca.compute(); // start PCA calculation

		// the eigenvector in double form
//		double[][] eigenvecs = new double[pca.dim_used][pca.evec[0].evec.length]; 
		double[][] eigenvecs = new double[pca.dim_used][pca.evec.length]; 
		
		// initialize eigenvector of SimpleMatrix type
		for(int i = 0; i < pca.dim_used; i ++)
		{
			eigenvecs[i] = pca.evec[i].evec;
		}
		
		// the eigenvector in SimpleMatrix form
		SimpleMatrix eigenvecMatr = new SimpleMatrix(eigenvecs);
		
//		System.out.println("eigenvecMatr: " + eigenvecMatr);
		
//		System.out.println("Before transform: dim_used: " + pca.dim_used + ",num_data: " + pca.num_data +",dim_data: " + pca.dim_data 
//				+ ",pca.evec: " + pca.evec.length + ",pca.evec[0].evec: " + pca.evec[0].evec.length);
		
		System.out.println("After transform: dim_used: " + pca.dim_used + ",num_data: " + pca.num_data +",dim_data: " + pca.dim_data 
				+ ",pca.evec: " + pca.evec.length + ",pca.evec[0].evec: " + pca.evec[0].evec.length);
		
		System.out.println("(Larva) Done with PCA train. pca object has been saved.");
	}
	

	public double[] test(SimpleMatrix larvaTest)
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
//		SimpleMatrix[] wtsMatr = new SimpleMatrix[pca.dim_used];
		
		// calculate the weight matrix of SimpleMatrix type
		for(int i = 0; i < pca.dim_used; i ++)
		{
			wts[0][i] = eigenvec[i].dot( matrDiff );
		}
		
		System.out.println("wts:");
		MathManager.printHorizontal(wts);
		
		SimpleMatrix wtsMatr_used = new SimpleMatrix(wts);
		System.out.println("\n wtsMatr_used: " + wtsMatr_used +"\n");
		
		SimpleMatrix eigenvecsMatr_used = new SimpleMatrix(eigenvecs);
		
		SimpleMatrix matrDiff_rec = wtsMatr_used.mult( eigenvecsMatr_used );
		System.out.println("\n matrDiff_recs: " + matrDiff_rec +"\n");
		
		System.out.println("\n matrDiff: " + matrDiff +"\n");
		
		SimpleMatrix larva_rec = matrMean.plus( matrDiff_rec ) ;
		
		System.out.println("\n larva_rec: " + larva_rec +"\n");
		System.out.println("\n matrLarva: " + matrLarva +"\n");
		
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
		
		System.out.println("\n diaMatr: "+diaMatr +"\n");
		
		SimpleMatrix diaMatr_inverse = diaMatr.invert();
		
		System.out.println("\n diaMatr_inverse: "+diaMatr_inverse +"\n");
		
		// calculate possibility
		SimpleMatrix probilityMatr1 = wtsMatr_used.mult(diaMatr_inverse);
		
		System.out.println("\n probilityMatr1: "+probilityMatr1 +"\n");
		
		SimpleMatrix probilityMatr2 = probilityMatr1.mult(wtsMatr_used.transpose());
		
		System.out.println("\n probilityMatr2: "+probilityMatr2 +"\n");
		
		double probility = Math.exp(probilityMatr2.get(0, 0) * -1);
		
		System.out.println("\n probility: "+probility +"\n");
		
		double[] probilities = {probilityMatr2.get(0, 0), probility };
		return probilities;
	}

//	public String getPcaFile()
//	{
//		return pcaFile;
//	}

	public PCA getPca()
	{
		return pca;
	}
	
}
