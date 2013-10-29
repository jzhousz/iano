/***************************************************************
 * The purpose of this class is to validate algorithms going into
 * the running state of a chain. Currently (10/07/2013) it only
 * checks the single mode (ExpertFrame) on click of the run button.
 * 
 * More to be added later.
****************************************************************/

package annotool;
import annotool.io.Algorithm;

public class AlgorithmValidation {
	public static boolean isWithinBounds( int dimension, int upperbound, Algorithm extractor, Algorithm selector)
	{

		if (dimension <= upperbound)
		{
			// If the dimension is lower than the upper bound, return true
			return true;
		}
		else if(extractor.getName().equalsIgnoreCase("None") && selector.getName().equalsIgnoreCase("None"))
		{
			// If there is no extractor or selector and it is higher than the upperbound, then return false
			return false;
		}
		else
		{
			// All other cases return true
			return true;
		}
	}

	public static boolean isWithinBounds(int dimension, int upperbound,
			boolean extractor, boolean selector) {
		if (dimension <= upperbound)
		{
			// If the dimension is lower than the upper bound, return true
			return true;
		}
		else if( !(extractor || selector) )
		{
			// If there is no extractor or selector and it is higher than the upperbound, then return false
			return false;
		}
		else
		{
			// All other cases return true
			return true;
		}
	}
}
