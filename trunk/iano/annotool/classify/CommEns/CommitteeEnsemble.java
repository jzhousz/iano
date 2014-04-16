package annotool.classify.CommEns;
import annotool.classify.Classifier;
import annotool.classify.SavableClassifier;
import annotool.gui.model.ClassifierInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class CommitteeEnsemble implements SavableClassifier, Serializable{
	private static final long serialVersionUID = 1L;
	private ArrayList<ClassifierInfo> classifiersInfo = null;
	private EnsemModel model = null;
	private ArrayList <SavableClassifier> classifiers = null;
	
	
	public CommitteeEnsemble( ArrayList<ClassifierInfo> classifiersInfo)
	{
		this.classifiersInfo = new ArrayList<ClassifierInfo>();
		this.classifiers = new ArrayList<SavableClassifier>();
		Classifier c = null;
		for(int i = 0; i < classifiersInfo.size(); i++)
		{
			this.classifiersInfo.add(classifiersInfo.get(i));
			
			System.out.println(classifiersInfo.get(i).getName());
			try 
			{
				c = ( new annotool.Annotator()).getClassifierGivenName(classifiersInfo.get(i).getClassName(), classifiersInfo.get(i).getExternalPath(), classifiersInfo.get(i).getParams());
			} 
			
			catch (Exception e) 
			{
				e.printStackTrace();
			}
	    	
			   if(c instanceof SavableClassifier){
				   classifiers.add((SavableClassifier) c);
			   }
			
		}
		
		
	}
	
	public void setParameters(HashMap<String, String> para) {

	}


	public void classify(float[][] trainingpatterns, int[] trainingtargets,
			float[][] testingpatterns, int[] predictions, double[] prob)
			throws Exception {
		
		int[] preHolder = null;
		
		/*Trains the model */ 
		model = (EnsemModel) trainingOnly(trainingpatterns, trainingtargets);
		
		
		/* Test the model */
		preHolder = classifyUsingModel(model, testingpatterns, prob );		
		
		for ( int i = 0; i < preHolder.length; i++)
		{
			predictions[i] = preHolder[i];
		}
		
	}


	public boolean doesSupportProbability() {
		return false;
	}

	
	public Object trainingOnly(float[][] trainingpatterns, int[] trainingtargets)
			throws Exception {
		
		ArrayList<Object> Models = new ArrayList<Object>();
		int i = 0;
		for( SavableClassifier classifier : classifiers)
	       {
			   System.out.println(classifiersInfo.get(i).getName());
			   Models.add(classifier.trainingOnly(trainingpatterns, trainingtargets));
			   i++;
	       }
		
		
		return ( new EnsemModel(classifiersInfo, classifiers , Models));
	}

	
	public Object getModel() {
		
		return model;
	}

	
	public void setModel(Object model) throws Exception {
		this.model.setModels(((EnsemModel) model).getClassifiersInfo(), ((EnsemModel) model).getClassifiers() , ((EnsemModel) model).getModels());
		this.classifiersInfo = ((EnsemModel) model).getClassifiersInfo();
		this.classifiers =  ((EnsemModel) model).getClassifiers();
	}

	
	public int classifyUsingModel(Object model, float[] testingPattern,
			double[] prob) throws Exception {
		
		System.out.println("Ensemble Classifing");
		
		if(model instanceof EnsemModel && model != null)
		{
		
			ArrayList<Object> models = ((EnsemModel) model).getModels();
			int k = 0, modelCnt = 0;
			int[] finalResults = new int [classifiersInfo.size()];
		
			for( SavableClassifier classifer : classifiers)
			{
				System.out.println(classifiersInfo.get(k).getName());
				k++;
			  
					finalResults[modelCnt] = classifer.classifyUsingModel(models.get(modelCnt), testingPattern, prob);
					modelCnt++;

			}
			   HashMap<Integer, Integer> votes = new HashMap<Integer, Integer>();  //key: label; value: number of votes
				for( int i = 0; i < finalResults.length ; i++)  //loop through each testing sample
				{
					System.out.print(finalResults[i] + " ");
				
					  Integer previousVoteCount = null;
					  if ((previousVoteCount = votes.get(finalResults[i]))!= null) 
					  {
					
						  	votes.put(finalResults[i], previousVoteCount.intValue() + 1);
					  }
					  else
					  //{
					     //else put the label in the map, with value 1.
						  votes.put(finalResults[i], 1);
					  //}
				} // end if i loop
				   int maxVote = -1;
				   int maxLabel = -1;
				   for(Integer label: votes.keySet())
				   {
				       if (votes.get(label) > maxVote)
		               {
					      maxVote = votes.get(label);
						  maxLabel = label;
		               }
				      
				   }
				   
				   System.out.print("| " + maxLabel + "\n");
				   
		
			return maxLabel;
		
		}
		else
			throw new Exception("Model not vaild");
		
	}

	
	public int[] classifyUsingModel(Object model, float[][] testingPatterns,
			double[] prob) throws Exception {
		
		int[] rec = new int[testingPatterns.length];
		
		if(model instanceof EnsemModel && model != null)
		{
		
			for( int i = 0; i < testingPatterns.length; i++ )
			{
				rec[i]  = ((Integer) classifyUsingModel( model, testingPatterns[i],  prob  ));
				
			}
		}
		else
			throw new Exception("Model not vaild");
		
		
		return rec;
	}


	public void saveModel(Object model, String model_file_name)
			throws IOException {
		
		if(model instanceof EnsemModel && model != null)
		{
			
			ArrayList<ClassifierInfo> classinfo = ((EnsemModel)model).getClassifiersInfo();
			
			File file = new File(model_file_name);
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
			
			out.writeObject(((EnsemModel)model).getClassifiersInfo());
			
			int i = 0;
			String path = "";
			for( SavableClassifier cal : ((EnsemModel)model).getClassifiers())
			{
				path =  model_file_name + "_" + classinfo.get(i).getName() + i;
				
				cal.saveModel(cal.getModel(), path);
				
				out.writeObject(path);
				
				i++;
			}
			
			
			out.close();
		}
		else
			throw new IOException( "Model in class is null");
		
		
		
	}

	public Object loadModel(String model_file_name) throws IOException {
		ArrayList<Object> models = new ArrayList<Object>();	
		try
		{
			
			File file = new File(model_file_name);
			ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
			
			classifiersInfo = (ArrayList<ClassifierInfo>) input.readObject();
			classifiers = new ArrayList<SavableClassifier>();
			
		    //Program Classifiers loaders
			int i = 0;
			String path = "";
			
			for( ClassifierInfo info : classifiersInfo )
				System.out.println( "\n" + info.getName());
			
			while( i < classifiersInfo.size() )
			{
				path = (String) input.readObject();
				
				Classifier cal = ( new annotool.Annotator()).getClassifierGivenName(classifiersInfo.get(i).getClassName(), classifiersInfo.get(i).getExternalPath(), classifiersInfo.get(i).getParams());
						
				models.add(((SavableClassifier) cal).loadModel(path));
				
				classifiers.add((SavableClassifier) cal);
				
				i++;
			}
			
			
			input.close();

		} 
	    catch (ClassNotFoundException e1) 
	    {
	    	e1.printStackTrace();
	    }
		catch (Exception e) 
	    {
			e.printStackTrace();
		}
		
		this.model = new EnsemModel(classifiersInfo, classifiers , models);
		return model;
	}

}
