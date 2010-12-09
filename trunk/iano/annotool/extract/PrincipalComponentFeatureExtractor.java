package annotool.extract;

import weka.attributeSelection.PrincipalComponents;
import weka.core.Instances;

//use Weka PCA.  Seems slow on images. 
public class PrincipalComponentFeatureExtractor implements FeatureExtractor {

	Instances m_Data = null; //will build it
	protected float[][] features = null;
    protected byte[][] data;
    int length;
    int width;
    int height;

	   
	public PrincipalComponentFeatureExtractor(annotool.io.DataInput problem) {
		
		   data = problem.getData();
		   length = problem.getLength();
		   width = problem.getWidth();
		   height = problem.getHeight();
		   
		   features  = new float[length][width*height]; 
		   
	       for(int i=0; i <length; i++)
 		     for(int j = 0; j< width*height; j++)
			       features[i][j] = data[i][j]&0xff;
	}

	@Override
	public float[][] calcFeatures() {

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

}
