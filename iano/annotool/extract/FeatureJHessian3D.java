package annotool.extract;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import imagescience.feature.Hessian;
import imagescience.image.Axes;
import imagescience.image.Coordinates;
import imagescience.image.Image;

import java.util.ArrayList;
import java.util.HashMap;

import annotool.ImgDimension;
import annotool.io.DataInput;


public class FeatureJHessian3D implements FeatureExtractor {
	protected float[][] features = null;
	protected ArrayList data = null;
	protected DataInput problem = null;
	int length, width, height, depth;
	int imageType;
	double scale = 1.0;
        
	public final static int LARGESTEIGEN = 0;
	public final static int SMALLESTEIGEN = 1;

	int selectedEigenValue = LARGESTEIGEN;
	boolean absolute = true;
	
	public final static String SMOOTHING_KEY = "Smoothing Scale";
	public final static String EIGENVALUE_KEY = "Eigenvalue";
	public final static String ABSOLUTE_KEY = "Absolute Eigen Value Comparison";
	
   /**
    * Sets algorithm parameters from para 
    * 
    * @param  para  Each element of para holds a parameterís name for its key
    *               and a parameterís value for its value. The parameters
    *               should be the same as those in the algorithms.xml file.
    */
	@Override
	public void setParameters(HashMap<String, String> para) {
		if (para != null) {
		    if(para.containsKey(SMOOTHING_KEY)) 
		    	scale = Double.parseDouble(para.get(SMOOTHING_KEY));
		    
		    if(para.containsKey(EIGENVALUE_KEY)) {
		    	if(para.get(EIGENVALUE_KEY).equals("Largest"))
		    		selectedEigenValue = LARGESTEIGEN;
		    	else if(para.get(EIGENVALUE_KEY).equals("Smallest"))
		    		selectedEigenValue = SMALLESTEIGEN;
		    }
		    
		    if(para.containsKey(ABSOLUTE_KEY) && "0".equals(para.get(ABSOLUTE_KEY)))
		    	absolute = false;
		}
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
		
		this.problem = problem;
        
		this.length = problem.getLength();
        
		this.width = problem.getWidth();
		this.height = problem.getHeight();
        this.depth = problem.getDepth();
        
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
        this.depth = dim.depth;
        
		this.imageType = imageType;
		
		return calcFeatures();
	}
	
	protected float[][] calcFeatures() throws Exception {
		this.features = new float[this.length][this.width * this.height * this.depth];

		Image img = null;
        ImagePlus combinedImage;
        
		Hessian hessian = new Hessian();
		Coordinates startCO = new Coordinates(0, 0, 0);
		
		double values[][][] = new double[this.height][this.width][this.depth];

		int currentHeight = 0, currentWidth = 0;
		int stackSize;
        ImageStack stack;
		
        ArrayList currentImage = null;
		for(int imageIndex = 0; imageIndex < length; imageIndex++) {
		
			currentWidth = this.width;
			currentHeight = this.height;
			
			if (problem !=null){
				if (!problem.ofSameSize()) {
				  throw new Exception("3D edge features requires the images to be of the same size");
				}
			}
			else if (data !=null){
				 currentImage = (ArrayList) data.get(imageIndex);
			}
	
			stack = new ImageStack(this.width, this.height);
    		stackSize = this.depth;
                
            //for each slice of a 3D image
            for(int imageSlice = 1; imageSlice <= stackSize; imageSlice++)
            {
                ImageProcessor ip = null;
            
                if(imageType == DataInput.GRAY8 || imageType == DataInput.COLOR_RGB) {
                    if (data != null)
                        ip = new ByteProcessor(currentWidth, currentHeight, (byte[])currentImage.get(imageSlice-1), null);
                    else 
                        ip = new ByteProcessor(currentWidth, currentHeight, (byte[])problem.getData(imageIndex, imageSlice), null);
                }
                else if(imageType == DataInput.GRAY16) {
                    if (data != null)
                        ip = new ShortProcessor(currentWidth, currentHeight, (short[])currentImage.get(imageSlice-1), null);
                    else
                        ip = new ShortProcessor(currentWidth, currentHeight, (short[])problem.getData(imageIndex, imageSlice), null);
                }   
                else if(imageType == DataInput.GRAY32) {
                    if (data != null)
                        ip = new FloatProcessor(currentWidth, currentHeight, (float[])currentImage.get(imageSlice-1), null);
                    else
                        ip = new FloatProcessor(currentWidth, currentHeight, (float[])problem.getData(imageIndex, imageSlice), null);
                }
                else {
                    throw new Exception("Unsupported image type");
                }

                stack.addSlice("", ip); 
            }

            combinedImage = new ImagePlus("Combined", stack);
            
            img = Image.wrap(combinedImage);
            
            img = hessian.run(img, scale, absolute).get(selectedEigenValue);	//2D : index 1 - largest, index 2 - smallest
            img.axes(Axes.X + Axes.Y + Axes.Z);
            img.get(startCO, values);
            
            //For testing purposes
            //img.imageplus().show();
        
            int i = 0;
            for(int z = 0; z < this.depth; z++)
            {
                for(int y = 0; y < this.height; y++)
                    for(int x = 0; x < this.width; x++) {
                        features[imageIndex][i] = (float)values[y][x][z];
                        i++;
                    }
            }
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
		return true;
	}
    
}
