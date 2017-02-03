package pca;

import java.util.ArrayList;
import java.util.Arrays;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import pca.DwEigenVector;
import pca.DwVector;

/**
 *
 * @author thomas diewald
 *
 * date: 21.06.2013 PCA - Principal Component Analysis
 *
 * works for any number of dimension using: "Jama" for eigenvector
 * calculation.
 * 
 * Revised: Yaoguang Zhong
 */
public class PCA
{
	public int num_data = 0; // the number of data row
	public int dim_data = 0; // the number of dimensions of data

	public DwVector[] data; // input data-vectors
	public DwVector mean; // translation vector
	Matrix cvmat; // (variance)-covariance-matrix

	EigenvalueDecomposition edec; // eigenvalue decomposition (by Jama)
	public DwEigenVector[] evec; // list of eigenvectors and eigenvalues
	public Matrix emat; // eigenvector matrix (sorted colums by eigenvalues)
	
	public int dim_used = 0; // the dimensions used, which means the eigenvalue above 95% of the overall variation.

	/**
	 * Constructor.
	 */
	public PCA(DwVector[] data)
	{
		this.data = data;
		this.num_data = data.length;
		this.dim_data = data[0].v.length;
	}

	/**
	 * Compute 
	 * @return
	 */
	public PCA compute()
	{
		centerData();
		computeCovarianceMatrix();
		updatePCAMatrix();
		setTransformDimension(dim_data);
		return this;
	}

	/**
	 * Center data.
	 */
	public void centerData()
	{
		// compute mean
		double[] mean_tmp = new double[dim_data];
		
		for (int i = 0; i < num_data; i++)
		{
			for (int j = 0; j < dim_data; j++)
			{
				mean_tmp[j] += data[i].v[j];
			}
		}

		for (int j = 0; j < dim_data; j++)
		{
			mean_tmp[j] /= (double) num_data;
		}

		// center data (subtract mean) -> mean is at origin now
		for (int i = 0; i < num_data; i++)
		{
			for (int j = 0; j < dim_data; j++)
			{
				data[i].v[j] -= mean_tmp[j];
			}
		}
		mean = new DwVector(mean_tmp);
	}

	/**
	 * Compute covariance matrix.
	 */
	public void computeCovarianceMatrix()
	{
		// NxN-matrix. symetric, positive definite or positive semi-definite
		// every square symmetric matrix is orthogonally (orthonormally) diagonalisable.
		// --> S = E D E-transpose
		// . diagonal -> variances
		// . off-diagonal -> co-variances (... how well correlated two variables are)
		// 1. Maximise the signal, measured by variance (maximise the diagonal entries)
		// 2. Minimise the covariance between variables (minimise the off-diagonal entries)
		final double[][] mat = new double[dim_data][dim_data];
		
		for (int r = 0; r < dim_data; r++)
		{ // rows
			for (int c = r; c < dim_data; c++)
			{ // cols

				double sum = 0;
				for (int i = 0; i < num_data; i++)
				{
					sum += data[i].v[r] * data[i].v[c];
				}
				mat[r][c] = mat[c][r] = sum / (num_data - 1);
			}
		}
		
		cvmat = new Matrix(mat);
		// System.out.println("covariance matrix");
		// cvmat.print(8,8);
	}

	/**
	 * Update PCA matrix.
	 */
	public void updatePCAMatrix()
	{
		edec = cvmat.eig(); // get eigenvalue decomposition
		emat = edec.getV(); // get eigenvector matrix

		// System.out.println(" eigenvector matrix (before reordering)");
		// emat.print(8, 8);

		// transpose it, to get eigenvectors from columns
		emat = emat.transpose();

		double[][] emat_dd = emat.getArray();
		double[] eval = edec.getRealEigenvalues();

		// create objects for sorting
		// columns are eigenvectors ... principal components
		evec = new DwEigenVector[eval.length];
//		eigen = new Eigen[eval.length];

		double sumEval = 0; // the sum of eigenvalue
		
		for (int i = 0; i < eval.length; i++)
		{
			sumEval += eval[i];
		}

		for (int i = 0; i < evec.length; i++)
		{
			double evalPercent = eval[i] / sumEval; // the percentage of the eigenvalue
			evec[i] = new DwEigenVector(emat_dd[i], eval[i], evalPercent);
		}

		// sort eigenvectors by eigenvalues (decreasing)
		// System.out.println("unsorted eigenvectors");
		// for(int i = 0; i < evec.length; i++) evec[i].print();
		Arrays.sort(evec);
		// System.out.println("sorted eigenvectors: "+evec.length);
		// for(int i = 0; i < evec.length; i++) evec[i].print();

		// put the sorted vectors back into the matrix ... use all eigenvectors
		setTransformDimension(dim_data);
		
		double percent_used = 0; // the first several eigenvalues that is above 98%
		
		for (int i = 0; i < evec.length; i++)
		{
			if(percent_used < .995)
			{
				percent_used += evec[i].percent;
//				System.out.println("evalPercent["+i+"]: "+evec[i].percent+", percent_used["+i+"]: " + percent_used);
				dim_used++;
			}
		}
	}

	/**
	 * Print sorted eigenvalues.
	 */
	void printEigenValuesSorted()
	{
		System.out.println("sorted eigenvalues: " + evec.length);
		for (int i = 0; i < evec.length; i++)
			System.out.println("[" + i + "] " + evec[i].eval);
	}

	/**
	 * Print sorted eigenvectors.
	 */
	public void printEigenVectorsSorted()
	{
		System.out.println("sorted eigenvectors: " + evec.length);
		for (int i = 0; i < evec.length; i++)
			evec[i].print();
	}
	
	/**
	 * Can be used to reduce dimensions,
	 * e.g. dimensions with very low eigenvalues can be removed same, 
	 * as setting the dimension of the vector to 0, after the transformation.
	 * @param dim_new The new number of dimensions.
	 */
	public void setTransformDimension(int dim_new)
	{
		// compose new transform matrix of sorted eigenvectors,
		// if dim_new is smaller than the original size, than dimensions are reduced!
		double[][] emat_dd = new double[dim_new][dim_data];
		
		for (int i = 0; i < dim_new; i++)
		{
			emat_dd[i] = evec[i].evec;
		}
		
		emat = new Matrix(emat_dd); // to keep evec object untouched

		// System.out.println(" new matrix");
		// emat.print(8, 8);

		// not necessary, because already transposed while inserting the sorted
		// data
		// emat = emat.transpose();
		// System.out.println(" new matrix transposed");
		// emat.print(8, 8);
	}

//	/**
//	 * Transform vector.
//	 * @param vec
//	 * @return
//	 */
//	DwVector transformVector7(DwVector vec)
//	{
//		int cols = emat.getColumnDimension();
//		int rows = emat.getRowDimension();
//
//		if (cols != vec.v.length)
//		{
//			System.out.println("error, cant transform vector");
//		}
//		double[][] emat_dd = emat.getArray();
//		double[] vec_new = new double[rows];
//
//		for (int r = 0; r < rows; r++)
//		{
//			double val = 0;
//			for (int c = 0; c < cols; c++)
//			{
//				val += emat.get(r, c) * vec.v[c];
//			}
//			vec_new[r] = val;
//		}
//		return new DwVector(vec_new);
//	}

	
//	DwVector[] transformData7(DwVector[] data, boolean transpose)
//	{
//		Matrix mat = emat;
//		if (transpose)
//		{
//			mat = emat.transpose();
//		}
//
//		final int cols = mat.getColumnDimension();
//		final int rows = mat.getRowDimension();
//
//		final double[][] emat_dd = mat.getArray();
//		final int num_data = data.length;
//
//		DwVector[] data_new = new DwVector[num_data];
//		for (int i = 0; i < num_data; i++)
//		{
//			DwVector vec = data[i];
//
//			double[] vec_new = new double[rows];
//			for (int r = 0; r < rows; r++)
//			{
//				double val = 0;
//				for (int c = 0; c < cols; c++)
//				{
//					val += emat_dd[r][c] * vec.v[c];
//				}
//				vec_new[r] = val;
//			}
//			data_new[i] = new DwVector(vec_new);
//		}
//
//		if (transpose)
//		{
//			mat = emat.transpose();
//		}
//
//		return data_new;
//	}

//	public static void main(String[] args)
//	{
//		ArrayList<String[]> fieldsArr = CSVReader.readCSV("E:/3/training_out/out.csv");
//
//		ArrayList<double[]> fieldsArrDouble = new ArrayList<double[]>();
//
//		DwVector vectors[] = new DwVector[fieldsArr.size()];
//
//		// System.out.print("All rows:");
//
//		for (int j = 0; j < fieldsArr.size(); j++)
//		{
//			String[] fields = fieldsArr.get(j);
//
//			// System.out.print("Row:");
//
//			double fieldsDouble[] = new double[fieldsArr.get(0).length];
//
//			for (int i = 0; i < fields.length; i++)
//			{
//				try
//				{
//					fieldsDouble[i] = Double.parseDouble(fields[i]);
//				} catch (NumberFormatException ex)
//				{
//					// System.out.println("(j:"+j+",i:"+i+"):"+fields[i]);
//					int num = Integer.parseInt(fields[i]);
//					fieldsDouble[i] = (double) num;
//				}
//
//				// System.out.print(fields[i] + ",");
//			}
//
//			fieldsArrDouble.add(fieldsDouble);
//
//			DwVector vector = new DwVector(fieldsDouble);
//
//			vectors[j] = vector;
//
//			// System.out.println("");
//		}
//
//		PCA pca = new PCA(vectors);
//
//		pca.compute();
//
//		// pca.printEigenValuesSorted();
//		pca.printEigenVectorsSorted();
//		System.out.println("dim_used: " + pca.dim_used + ",num_data: " + pca.num_data +",dim_data: " + pca.dim_data 
//				+ ",pca.evec: " + pca.evec.length + ",pca.evec[0].evec: " + pca.evec[0].evec.length);
//		
//		pca.mean.print();
//		
////		pca.data[0].print();
//		
//		double[][] eigenvecs = new double[pca.evec.length][pca.evec[0].evec.length]; // the eigenvectors in double format
//		
//		for(int i = 0; i < pca.evec.length; i ++)
//		{
//			for(int j = 0; j < pca.evec[0].evec.length; j ++)
//			{
////				System.out.print("pca.evec["+i+"].evec["+j+"]: " + pca.evec[i].evec[j]);
//				eigenvecs[i][j] = pca.evec[i].evec[j];
//			}
////			System.out.println("");
//		}
//		
//		pca.dim_used = pca.dim_data;
//		
//		double[][] eigenvecs_part = test.Matrix_Double.getFirstNRows(eigenvecs, pca.dim_used);
//				
//		double[][] eigenvecs_transpose = test.Matrix_Double.transpose( eigenvecs );
//		
//		double[][] eigenvecs_transpose_part = test.Matrix_Double.transpose( eigenvecs_part );
//		
////		for(int i = 0; i < eigenvecs_transpose_part.length; i ++)
////		{
////			System.out.print("eigenvecs_transpose["+i+"]:");
////			
////			for(int j = 0; j < eigenvecs_transpose_part[0].length; j ++)
////			{
////				System.out.print(eigenvecs_transpose[i][j] + ",");
////			}
////			
////			System.out.print("\neigenvecs_tran_part["+i+"]:");
////			
////			for(int j = 0; j < eigenvecs_transpose_part[0].length; j ++)
////			{
////				System.out.print(eigenvecs_transpose_part[i][j] + ",");
////			}
////			System.out.println("");
////		}
//		
//		double[] larva_diff = test.Matrix_Double.subtract(pca.data[0].v, pca.mean.v);
//		
//		double[] wts = test.Matrix_Double.multiply(eigenvecs_transpose, larva_diff);
//		
////		double[] wts_part = test.Matrix_Double.multiply(eigenvecs_transpose_part, larva_diff);
//		
////		double[] larva_variable = test.Matrix_Double.multiply(eigenvecs, wts);
//		
//		double[] wts_part = test.Matrix_Double.getFirstNRows(wts, pca.dim_used);
//		
////		System.out.println("\nwts:");
////		test.Matrix_Double.printHorizontal(wts);
////		
////		System.out.println("\nwts_part:");
////		test.Matrix_Double.printHorizontal(wts_part);
//		
//		double[] larva_variable_part = test.Matrix_Double.multiply(eigenvecs_transpose_part, wts_part);
//		
//		System.out.println("\nlarva_variable_part:");
//		test.Matrix_Double.printHorizontal(larva_variable_part);
//		
////		double[] larva_recovered = test.Matrix_Double.add(pca.mean.v, larva_variable);
//		
//		double[] larva_recovered_part = test.Matrix_Double.add(pca.mean.v, larva_variable_part);
//		
//		System.out.println("\nlarva_recovered_part:");
//		test.Matrix_Double.printHorizontal(larva_recovered_part);
//		
////		System.out.println("larva_recovered:");
////		test.Matrix_Double.printHorizontal(larva_recovered);
////		
//		System.out.println("\npca.data[0]:");
//		test.Matrix_Double.printHorizontal(pca.data[0].v);
//		
//	}

}
