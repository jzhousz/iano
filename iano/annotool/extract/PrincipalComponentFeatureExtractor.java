package annotool.extract;

import java.util.ArrayList;

import annotool.ImgDimension;
import weka.attributeSelection.PrincipalComponents;
import weka.core.Instances;

/*
// This is a work in progress.   
// It uses Weka PCA. It is too slow on images.  Not used in GUI after May 2012 
//
// 5/25/2012  Due to the incompatability with the updated DataInput, the methods are commented out.
*/
public class PrincipalComponentFeatureExtractor implements FeatureExtractor {

	Instances m_Data = null; //will build it
	protected float[][] features = null;
    protected byte[][] data;
    int length;
    int width;
    int height;

    /**
     * Default constructor
     */
    public PrincipalComponentFeatureExtractor() 
    {}
    
    /**
     * Empty implementation of setParameters 
     * 
     * @param  para  Each element of para holds a parameter’s name for its key
     *               and a parameter’s value for its value. The parameters
     *               should be the same as those in the algorithms.xml file.
     */
    public void setParameters(java.util.HashMap<String, String> parameters) {
  	  //process parameter if any	
  	}
  	
    /**
     * Empty implementation of PrincipalComponentFeatureExtractor(java.util.HashMap<String, String> parameters)
     * 
     * @param  para  Each element of para holds a parameter’s name for its key
     *               and a parameter’s value for its value. The parameters
     *               should be the same as those in the algorithms.xml file.
     */
    public PrincipalComponentFeatureExtractor(java.util.HashMap<String, String> parameters) {
	  //process parameter if any	
	}
	   
    /**
     * Empty implementation of PrincipalComponentFeatureExtractor(annotool.io.DataInput problem)
     * 
     * @param  problem  Image to be processed
     */
	public PrincipalComponentFeatureExtractor(annotool.io.DataInput problem) {
		
		/*
		   data = problem.getData();
		   length = problem.getLength();
		   width = problem.getWidth();
		   height = problem.getHeight();
		   
		   features  = new float[length][width*height]; 
		   
	       for(int i=0; i <length; i++)
 		     for(int j = 0; j< width*height; j++)
			       features[i][j] = data[i][j]&0xff;
	   */
	}

   /**
    * Empty implementation of calcFeatures(ArrayList data, int imageType, ImgDimension dim)
    * 
    * @param   data       Data taken from the image
    * @param   imageType  Type of the image
    * @param   dim        Dimenstions of the image
    * @return             Array of features
    * @throws  Exception  (Not used)
    */
	public float[][] calcFeatures(ArrayList data, int imageType, ImgDimension dim) throws Exception
	{ 
		System.out.println("This method is not yet supported.");
		throw new Exception("Not supported.");
	}

    /**
     * Empty implementation of calcFeatures(annotool.io.DataInput problem)
     * 
     * @param   problem    Image to be processed
     * @return             Array of features
     * @throws  Exception  (Not used)
     */
	@Override
	public float[][] calcFeatures(annotool.io.DataInput problem) throws Exception
	{
		System.out.println("This method is not yet supported.");
		throw new Exception("Not supported.");

		/*
		   data = problem.getData();
		   length = problem.getLength();
		   width = problem.getWidth();
		   height = problem.getHeight();
		   
		   features  = new float[length][width*height]; 
		   
	       for(int i=0; i <length; i++)
 		     for(int j = 0; j< width*height; j++)
			       features[i][j] = data[i][j]&0xff;
		         
		  return calcFeatures();
		  */
	       
	}
	
   /**
    * Calculates features based on data and dim.
    * 
    * @param   data       Data taken from the image
    * @param   dim        Dimenstions of the image
    * @return             Array of features
    */
	private float[][] calcFeatures(byte[][] data, ImgDimension dim)
	{
		   length = data.length;
		   width = dim.width;
		   height = dim.height;
		   
		   features  = new float[length][width*height]; 
		   
	       for(int i=0; i <length; i++)
 		     for(int j = 0; j< width*height; j++)
			       features[i][j] = data[i][j]&0xff;
		
	       return calcFeatures();
	}
	   
	
	
	protected float[][] calcFeatures() {

	     //build a target to make Weka happy. Not needed for PCA.
		int[] targets = new int[features.length];
		for(int i=0; i<features.length; i++) targets[i] = 1;
		m_Data = (new annotool.classify.WekaHelper()).buildM_Data(features, targets, "PCExtractionProblem");
		
		PrincipalComponents extractor = new PrincipalComponents();
		try{
		  System.out.println("Start extracting. May be slow on large sets...");	
		  extractor.buildEvaluator(m_Data);
		  Instances new_data = extractor.transformedData();
		  //convert back to float[][]
          for(int i=0; i <length; i++)
	 		     for(int j = 0; j< width*height; i++)
				       features[i][j] = (float) new_data.instance(i).value(j);

		}catch(Exception e)
		{ e.printStackTrace();}
		
		
		return features;
	}

    /**
     * Returns whether or not the algorithm is able to extract from a 3D image 
     * stack. 
     * 
     * @return  <code>True</code> if the algorithm is a 3D extractor, 
     *          <code>False</code> if not. Default is <code>False</code>
     */
	public boolean is3DExtractor()
	{  return false;} 
}
