package learning;
import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;

import entities.CSVReader;
import manager.VectorManager;
import pca.DwVector;
import pca.PCA;

public class PCACalc
{

	public static void main(String[] args)
	{
		ArrayList<String[]> fieldsArr = CSVReader.readCSV("E:/3/training_out/out.csv");

		ArrayList<double[]> fieldsArrDouble = new ArrayList<double[]>();

		DwVector vectors[] = new DwVector[fieldsArr.size()];

		// System.out.print("All rows:");

		for (int j = 0; j < fieldsArr.size(); j++)
		{
			String[] fields = fieldsArr.get(j);

			// System.out.print("Row:");

			double fieldsDouble[] = new double[fieldsArr.get(0).length];

			for (int i = 0; i < fields.length; i++)
			{
				try
				{
					fieldsDouble[i] = Double.parseDouble(fields[i]);
				} catch (NumberFormatException ex)
				{
					// System.out.println("(j:"+j+",i:"+i+"):"+fields[i]);
					int num = Integer.parseInt(fields[i]);
					fieldsDouble[i] = (double) num;
				}

				// System.out.print(fields[i] + ",");
			}

			fieldsArrDouble.add(fieldsDouble);

			DwVector vector = new DwVector(fieldsDouble);

			vectors[j] = vector;

			// System.out.println("");
		}

		PCA pca = new PCA(vectors);

		pca.compute();

		// pca.printEigenValuesSorted();
		pca.printEigenVectorsSorted();
		System.out.println("dim_used: " + pca.dim_used + ",num_data: " + pca.num_data +",dim_data: " + pca.dim_data 
				+ ",pca.evec: " + pca.evec.length + ",pca.evec[0].evec: " + pca.evec[0].evec.length);
		
		pca.mean.print();
		
//		pca.data[0].print();
		
		double[][] eigenvecs = new double[pca.evec.length][pca.evec[0].evec.length]; // the eigenvectors in double format
		
		for(int i = 0; i < pca.evec.length; i ++)
		{
			for(int j = 0; j < pca.evec[0].evec.length; j ++)
			{
//				System.out.print("pca.evec["+i+"].evec["+j+"]: " + pca.evec[i].evec[j]);
				eigenvecs[i][j] = pca.evec[i].evec[j];
			}
//			System.out.println("");
		}
		
//		pca.dim_used = 44;

		SimpleMatrix matrEigenVec = new SimpleMatrix(eigenvecs); // eigenvector matrix
		SimpleMatrix matrEigenVec_trans = matrEigenVec.transpose(); // transpose vectors of eigenvector matrix
		
//		double[] larva_diff = entities.Matrix.subtract(pca.data[0].v, pca.mean.v);
//		double[] wts = entities.Matrix.multiply(eigenvecs_transpose, larva_diff);
		
		double[][] larvaDiff = new double[pca.data.length][pca.data[0].v.length];
		
		SimpleMatrix matrMean = new SimpleMatrix(VectorManager.convertTo2DHorizontal(pca.mean.v)); // the PCA mean vector
		SimpleMatrix matrLarva = new SimpleMatrix(VectorManager.convertTo2DHorizontal(pca.data[0].v)); // the larva data row
		SimpleMatrix matrDiff = matrLarva.minus(matrMean);
		
//		System.out.println("\nmatrEigenVec_trans:\n"+matrEigenVec_trans);
//		System.out.println("\nmatrDiff:\n"+matrDiff);
		
//		SimpleMatrix matrWts = matrEigenVec_trans.dot(matrDiff);
		
		
		
		
//		double[][] a = new double[1][6];
//		double[][] b = new double[6][1];
//		
//		for(int i = 0; i < 6; i++)
//		{
//			a[0][i] = i;
//			b[i][0] = i;
//		}
//		
//		SimpleMatrix matA = new SimpleMatrix(a);
//		SimpleMatrix matA_trans = matA.transpose();
//		SimpleMatrix matB = new SimpleMatrix(b);
//		
//		SimpleMatrix matC = matA.mult(matB);
//		
//		System.out.println(matA);
//		System.out.println(matA_trans);
		
		/*
		
		double[][] eigenvecs_part = entities.Matrix.getFirstNRows(eigenvecs, pca.dim_used);
				
		double[][] eigenvecs_transpose = entities.Matrix.transpose( eigenvecs );
		
		double[][] eigenvecs_transpose_part = entities.Matrix.transpose( eigenvecs_part );
		
//		for(int i = 0; i < eigenvecs_transpose_part.length; i ++)
//		{
//			System.out.print("eigenvecs_transpose["+i+"]:");
//			
//			for(int j = 0; j < eigenvecs_transpose_part[0].length; j ++)
//			{
//				System.out.print(eigenvecs_transpose[i][j] + ",");
//			}
//			
//			System.out.print("\neigenvecs_tran_part["+i+"]:");
//			
//			for(int j = 0; j < eigenvecs_transpose_part[0].length; j ++)
//			{
//				System.out.print(eigenvecs_transpose_part[i][j] + ",");
//			}
//			System.out.println("");
//		}
		
		double[] larva_diff = entities.Matrix.subtract(pca.data[0].v, pca.mean.v);
		
		double[] wts = entities.Matrix.multiply(eigenvecs_transpose, larva_diff);
		
//		double[] wts_part = entities.Matrix.multiply(eigenvecs_transpose_part, larva_diff);
		
//		double[] larva_variable = entities.Matrix.multiply(eigenvecs, wts);
		
		double[] wts_part = entities.Matrix.getFirstNRows(wts, pca.dim_used);
		
//		System.out.println("\nwts:");
//		entities.Matrix.printHorizontal(wts);
//		
//		System.out.println("\nwts_part:");
//		entities.Matrix.printHorizontal(wts_part);
		
		double[] larva_variable_part = entities.Matrix.multiply(eigenvecs_transpose_part, wts_part);
		
		System.out.println("\nlarva_variable_part:");
		entities.Matrix.printHorizontal(larva_variable_part);
		
//		double[] larva_recovered = entities.Matrix.add(pca.mean.v, larva_variable);
		
		double[] larva_recovered_part = entities.Matrix.add(pca.mean.v, larva_variable_part);
		
		System.out.println("\nlarva_recovered_part:");
		entities.Matrix.printHorizontal(larva_recovered_part);
		
//		System.out.println("larva_recovered:");
//		entities.Matrix.printHorizontal(larva_recovered);
//		
		System.out.println("\npca.data[0]:");
		entities.Matrix.printHorizontal(pca.data[0].v);
		*/
	}
	
}
