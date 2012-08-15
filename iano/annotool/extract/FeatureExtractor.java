package annotool.extract;

import annotool.io.DataInput;
import java.util.ArrayList;
import annotool.ImgDimension;

/**
 * FeatureExtractor finds features and saves them to a two dimensional array
 */
public interface FeatureExtractor
{

   /**
    * Sets algorithm parameters from para 
    * 
    * @param  para  Each element of para holds a parameter’s name for its key
    *               and a parameter’s value for its value. The parameters
    *               should be the same as those in the algorithms.xml file.
    */
   public void  setParameters(java.util.HashMap<String, String> para);

   
   
   /**
    * Get features based on raw image stored in problem.
    * 
    * @param   problem    Image to be processed
    * @return             Array of features
    * @throws  Exception  Optional, generic exception to be thrown
    */
   public float[][] calcFeatures(DataInput problem) throws Exception;

// The DataInput class encapsulates the data of an image. It allows for a
// simple interface to the image’s properties via member functions. Member
// functions allow for reading image properties, data, and images from a 
// stack. For further reference, see the DataInput API.

      
   /**
    * Get features based on data, imageType, and dim.
    * <p>
    * ArrayList of array can accommodate FloatProcessor and IntProcessor 
    * 09/02/2011
    * 
    * @param   data       Data taken from the image
    * @param   imageType  Type of the image
    * @param   dim        Dimenstions of the image
    * @return             Array of features
    * @throws  Exception  Optional, generic exception to be thrown
    */
   public float[][] calcFeatures(ArrayList data, int imageType, ImgDimension dim) throws Exception;

   
   
   
   /**
    * Returns whether or not the algorithm is able to extract from a 3D image 
    * stack. 
    * 
    * @return  <code>True</code> if the algorithm is a 3D extractor, 
    *          <code>False</code> if not
    */
   boolean is3DExtractor();
}
