package annotool;

//A singleton of current project's extracted or selected features.
//Use a singleton so that no repeated memory allocation is needed.
public class ComboFeatures {
	float[][] trainingFeatures;
	float[][] testingFeatures;
	int[]  selectedIndices;

	//private static final ComboFeatures INSTANCE = new ComboFeatures();

	//singleton constructor -- Removed to allow multiple expert run.  Jun 2011.
	//public static ComboFeatures getInstance() {
    //    return INSTANCE;
    //}
	
	//setters
	public void setTrainingFeatures(float[][] fea)
	{   trainingFeatures = fea; }
	
	public void setTestingFeatures(float[][] fea)
	{   testingFeatures = fea; }
	
	public void setIndices(int[] ind)
	{   selectedIndices = ind; }
	
	//getters
	public float[][] getTrainingFeatures()
	{   return trainingFeatures;  }
	
	public float[][] getTestingFeatures()
	{   return testingFeatures;   }
	
	public int[] getSelectedIndices()
	{   return selectedIndices;   }
}

