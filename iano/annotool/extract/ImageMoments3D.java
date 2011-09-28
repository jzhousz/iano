package annotool.extract;

import java.util.ArrayList;
import java.util.HashMap;

import annotool.ImgDimension;
import annotool.io.DataInput;

public class ImageMoments3D implements FeatureExtractor {
	protected float[][] features = null;
	int length;
	int imageType;              //the type of image (defined in DataInput)
	int stackSize;
	
	DataInput problem = null;
	ArrayList all3DData = null;
	
	ImgDimension dim = new ImgDimension();
	
	private static final int numFeatures = 5;
	
	@Override
	public void setParameters(HashMap<String, String> parameter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float[][] calcFeatures(DataInput problem) throws Exception {
		this.problem = problem;
		this.length = problem.getLength();
		this.dim.width  =  problem.getWidth();
		this.dim.height  = problem.getHeight();
		this.imageType = problem.getImageType();
		this.stackSize  = problem.getStackSize();
		features = new float[length][numFeatures];
		
		return calcFeatures();
	}

	@Override
	public float[][] calcFeatures(ArrayList all3DData, int imageType,
			ImgDimension dim) throws Exception {
		this.length = all3DData.size();
		this.dim = dim;
		this.imageType = imageType;
		this.all3DData = all3DData;
		this.stackSize = ((ArrayList)all3DData.get(0)).size();
		features = new float[length][numFeatures];
		
		return calcFeatures();
	}
	
	protected float[][] calcFeatures() throws Exception {
		//Raw moments
		double M_000, M_100, M_010, M_001;
		
		double mean_x, mean_y, mean_z;
		
		//Central moments
		double mu_200, mu_020, mu_002,
			   mu_011, mu_101, mu_110,
			   mu_100;
		
		//Normalized central moments for scale invariance
		double nu_200, nu_020, nu_002,
		   	   nu_011, nu_101, nu_110,
		   	   nu_100;
		
		double J1, J2, J3, I1, I2;
		
		//Process one image at a time
		for(int imgIndex=0; imgIndex < length; imgIndex++) {
			System.out.println("Reading image: " + (imgIndex + 1));
			ArrayList currentImage = null;
			
			if (problem != null)
			  currentImage = problem.getAllStacksOfOneImage(imgIndex);
	        else if (all3DData != null)
	          currentImage = (ArrayList)all3DData.get(imgIndex);
			
			//Calculate raw moments
			System.out.println("Calculating raw moments...");
			M_000 = M_pqr(currentImage, 0, 0, 0);
			M_100 = M_pqr(currentImage, 1, 0, 0);
			M_010 = M_pqr(currentImage, 0, 1, 0);
			M_001 = M_pqr(currentImage, 0, 0, 1);
			
			//Calculate centroid co-ordinates
			mean_x = M_100 / M_000;
			mean_y = M_010 / M_000;
			mean_z = M_001 / M_000;
			
			//Calcualte central moments
			System.out.println("Calculating central moments...");
			mu_200 = mu_pqr(currentImage, 2, 0, 0, mean_x, mean_y, mean_z);
			mu_020 = mu_pqr(currentImage, 0, 2, 0, mean_x, mean_y, mean_z);
			mu_002 = mu_pqr(currentImage, 0, 0, 2, mean_x, mean_y, mean_z);
			mu_011 = mu_pqr(currentImage, 0, 1, 1, mean_x, mean_y, mean_z);
			mu_101 = mu_pqr(currentImage, 1, 0, 1, mean_x, mean_y, mean_z);
			mu_110 = mu_pqr(currentImage, 1, 1, 0, mean_x, mean_y, mean_z);
			mu_100 = mu_pqr(currentImage, 1, 0, 0, mean_x, mean_y, mean_z);
			
			//Calculate scale normalized moments
			System.out.println("Calculating normalized moments...");
			nu_200 = nu_pqr(mu_200, 2, 0, 0, M_000);
			nu_020 = nu_pqr(mu_020, 0, 2, 0, M_000);
			nu_002 = nu_pqr(mu_002, 0, 0, 2, M_000);
			nu_011 = nu_pqr(mu_011, 0, 1, 1, M_000);
			nu_101 = nu_pqr(mu_101, 1, 0, 1, M_000);
			nu_110 = nu_pqr(mu_110, 1, 1, 0, M_000);
			nu_100 = nu_pqr(mu_100, 1, 0, 0, M_000);
			
			
			System.out.println("Calculating moment invariants...");
			J1 = nu_200 + nu_020 + nu_002;
			
			J2 = nu_020 * nu_002 - nu_011 * nu_011 + nu_200 * nu_002 
					- nu_101 * nu_101 + nu_200 * nu_020 - nu_110 * nu_110;
			
			J3 = nu_200 * nu_020 * nu_002 + 2 * nu_110 * nu_101 * nu_011
				- nu_002 * nu_110 * nu_110 - nu_020 * nu_101 * nu_101
					- nu_200 * nu_011 * nu_011;
			
			I1 = J1 * J1 / J2;
			
			I2 = J3 / (J1 * J1 * J1);
			
			features[imgIndex][0] = (float)J1;						//J1
			features[imgIndex][1] = (float)J2;						//J2
			features[imgIndex][2] = (float)J3;						//J3 (Delta2)
			features[imgIndex][3] = (float)I1;						//I1
			features[imgIndex][4] = (float)I2;						//I2
			
			System.out.println("Done: " + (imgIndex + 1));
		}
		
		return features;
	}
	
	/**
	 * Calculate raw image moments
	 * 
	 * @param image
	 * @param p
	 * @param q
	 * @param r
	 * @return
	 * @throws Exception
	 */
    protected double M_pqr(ArrayList image, int p, int q, int r) throws Exception {
        CompSumDouble m = new CompSumDouble();
        
        int mask = 0xff; 
        int longmask = 0xffff;
        Object data = null;
        
        int size = dim.width * dim.height;
        int x, y;
        for(int z = 0; z < image.size(); z++) {
        	data = image.get(z);
        	for(int i = 0; i < size; i++) {
        		x = i % dim.width;
        		y = i / dim.width;
        		if (imageType == DataInput.GRAY8 || imageType ==  DataInput.COLOR_RGB)
        			m.Add(getLowerPower(x, p) * getLowerPower(y, q) * getLowerPower(z, r) * (float) (((byte[])data)[i] & mask));
        		else if (imageType == DataInput.GRAY16)
        			m.Add(getLowerPower(x, p) * getLowerPower(y, q) * getLowerPower(z, r) * (float) (((int[])data)[i] & longmask));
        		else if (imageType == DataInput.GRAY32)
        			m.Add(getLowerPower(x, p) * getLowerPower(y, q) * getLowerPower(z, r) * ((float[])data)[i]);
        		else
        			throw new Exception("Not supported image type in Moments. Type = " + imageType);
        	}
        }
        
        return m.getSum();
    }
    
    /**
     * Calculate central moments
     * 
     * @param image
     * @param p
     * @param q
     * @param r
     * @param mean_x
     * @param mean_y
     * @param mean_z
     * @return
     * @throws Exception
     */
    protected double mu_pqr(ArrayList image, int p, int q, int r,
    		double mean_x, double mean_y, double mean_z) throws Exception {
        CompSumDouble mu = new CompSumDouble();
        
        int mask = 0xff; 
        int longmask = 0xffff;
        Object data = null;
        
        int size = dim.width * dim.height;
        int x, y;
        for(int z = 0; z < image.size(); z++) {
        	data = image.get(z);
        	for(int i = 0; i < size; i++) {
        		x = i % dim.width;
        		y = i / dim.width;
        		if (imageType == DataInput.GRAY8 || imageType ==  DataInput.COLOR_RGB)
        			mu.Add(getLowerPower(x - mean_x, p) * getLowerPower(y - mean_y, q) * getLowerPower(z - mean_z, r) * (float) (((byte[])data)[i] & mask));
        		else if (imageType == DataInput.GRAY16)
        			mu.Add(getLowerPower(x - mean_x, p) * getLowerPower(y - mean_y, q) * getLowerPower(z - mean_z, r) * (float) (((int[])data)[i] & longmask));
        		else if (imageType == DataInput.GRAY32)
        			mu.Add(getLowerPower(x - mean_x, p) * getLowerPower(y - mean_y, q) * getLowerPower(z - mean_z, r) * ((float[])data)[i]);
        		else
        			throw new Exception("Not supported image type in Moments. Type = " + imageType);
        	}
        }
        
        return mu.getSum();
    }
    
    //Works with power 0, 1 and 2
    private double getLowerPower(double base, int power) {
    	if (power == 0)
    		return 1;
    	else if(power == 1)
    		return base;
    	else if(power == 2)
    		return base * base;
    	else
    		return 0;
    }
    
    /**
     * Calculates scale normalized moments
     * 
     * @param mu_pqr
     * @param p
     * @param q
     * @param r
     * @param mu_000
     * @return
     */
    protected double nu_pqr(double mu_pqr, int p, int q, int r, double mu_000) {
        return (mu_pqr / Math.pow(mu_000, (p + q + r + 3) / 3));
    }

    @Override
	public boolean is3DExtractor() {
		return true;
	}

}
