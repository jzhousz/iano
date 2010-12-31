package annotool;

//A singleton of current project's extracted or selected features.
//Use a singleton so that no repeated memory allocation is needed.
public class ComboFeatures {
	float[][] trainingFeatures;
	float[][] testingFeatures;

	private static final ComboFeatures INSTANCE = new ComboFeatures();

	//singleton constructor
	public static ComboFeatures getInstance() {
        return INSTANCE;
    }
	
	//setters
	public void setTrainingFeatures(float[][] fea)
	{   trainingFeatures = fea; }
	
	public void setTestingFeatures(float[][] fea)
	{   testingFeatures = fea; }
	
	//getters
	public float[][] getTrainingFeatures()
	{   return trainingFeatures;  }
	
	public float[][] getTestingFeatures()
	{   return testingFeatures;   }
}
