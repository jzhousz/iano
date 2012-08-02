package annotool.extract;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import imagescience.feature.Laplacian;
import imagescience.image.Axes;
import imagescience.image.Coordinates;
import imagescience.image.Image;

import java.util.ArrayList;
import java.util.HashMap;

import annotool.ImgDimension;
import annotool.io.DataInput;

/**
 *  This class wraps a FeatureJ extractor:
 * 
 *  http://www.imagescience.org/meijering/software/featurej/
 *
 */
public class FeatureJLaplacian implements FeatureExtractor {
	protected float[][] features = null;
	protected ArrayList data;
	int length;
	int width;
	int height;
	int imageType;
	double scale = 1.0;
	
	public final static String SCALE_KEY = "Smoothing Scale";
	
   /**
    * Sets algorithm parameters from para 
    * 
    * @param  para  Each element of para holds a parameterís name for its key
    *               and a parameterís value for its value. The parameters
    *               should be the same as those in the algorithms.xml file.
    */
	@Override
	public void setParameters(HashMap<String, String> para) {
		if (para != null && para.containsKey(SCALE_KEY))
		     scale = Double.parseDouble(para.get(SCALE_KEY));

	}

   /**
    * Get features based on raw image stored in problem.
    * 
    * @param   problem    Image to be processed
    * @return             Array of features
    * @throws  Exception  (Not used)
    */
	@Override
	public float[][] calcFeatures(DataInput problem) throws Exception {
		this.data = problem.getData();
		this.length = problem.getLength();
		this.width = problem.getWidth();
		this.height = problem.getHeight();
		this.imageType = problem.getImageType();
		
		return calcFeatures();
	}

   /**
    * Get features based on data, imageType, and dim.
    * 
    * @param   data       Data taken from the image
    * @param   imageType  Type of the image
    * @param   dim        Dimenstions of the image
    * @return             Array of features
    * @throws  Exception  (Not used)
    */
	@Override
	public float[][] calcFeatures(ArrayList data, int imageType,
			ImgDimension dim) throws Exception {		
		this.data = data;
		this.length = data.size();
		this.width = dim.width;
		this.height = dim.height;
		this.imageType = imageType;
		
		return calcFeatures();
	}
	
	protected float[][] calcFeatures() throws Exception {
		this.features = new float[this.length][this.width * this.height];
		
		ImageProcessor ip = null;
		Image img = null;
		
		Laplacian laplacian = new Laplacian();
		Coordinates startCO = new Coordinates(0, 0);
		
		double values[][] = new double[this.height][this.width];
		
		for(int imageIndex = 0; imageIndex < data.size(); imageIndex++) {
			if(imageType == DataInput.GRAY8 || imageType == DataInput.COLOR_RGB) {
				ip = new ByteProcessor(this.width, this.height, (byte[])data.get(imageIndex), null);
		    }
		    else if(imageType == DataInput.GRAY16) {
		    	ip = new FloatProcessor(this.width, this.height, (int[])data.get(imageIndex));
		    }	
	 	    else if(imageType == DataInput.GRAY32) {
		    	ip = new FloatProcessor(this.width, this.height, (float[])data.get(imageIndex), null);
	 	    }
	 	    else {
	 	    	throw new Exception("Unsuppored image type");
	 	    }
			
			img = Image.wrap(new ImagePlus("Image", ip));
			
			img = laplacian.run(img, scale);
        	img.axes(Axes.X + Axes.Y);
        	img.get(startCO, values);
        	
        	int i = 0;
        	for(int y = 0; y < this.height; y++)
        		for(int x = 0; x < this.width; x++) {
        			features[imageIndex][i] = (float)values[y][x];
        			i++;
        		}
        	
        	//if(imageIndex == (this.length - 1))
        		//img.imageplus().show();
		}
		
		return features;
	}

   /**
    * Returns whether or not the algorithm is able to extract from a 3D image 
    * stack. 
    * 
    * @return  <code>True</code> if the algorithm is a 3D extractor, 
    *          <code>False</code> if not. Default is <code>False</code>
    */
	@Override
	public boolean is3DExtractor() {
		return false;
	}

}
