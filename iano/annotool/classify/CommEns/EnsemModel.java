package annotool.classify.CommEns;

import java.io.Serializable;
import java.util.ArrayList;

import annotool.classify.SavableClassifier;
import annotool.gui.model.ClassifierInfo;

public class EnsemModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private ArrayList<ClassifierInfo> classifiersInfo = null;
	private ArrayList <SavableClassifier> classifiers = null;
	private ArrayList<Object> models = null;
	
	EnsemModel(ArrayList<ClassifierInfo> classifiersInfo, ArrayList <SavableClassifier> classifiers , ArrayList<Object> models)
	{
		this.classifiersInfo = new ArrayList<ClassifierInfo>();
		this.classifiers = new ArrayList<SavableClassifier>();
		this.models = new ArrayList<Object>();
		
		for(int i = 0; i < classifiersInfo.size(); i++)
		{
			this.classifiers.add(classifiers.get(i));
			this.classifiersInfo.add(classifiersInfo.get(i));
			this.models.add(models.get(i));
			
		}
	}
	
	public ArrayList<Object> getModels()
	{
		return models;
	}
	

	public ArrayList<ClassifierInfo> getClassifiersInfo()
	{
		return classifiersInfo;
	}
	
	public ArrayList<SavableClassifier> getClassifiers()
	{
		return classifiers;
	}
	
	public void setModels (ArrayList<ClassifierInfo> classifiersInfo, ArrayList<SavableClassifier> classifiers, ArrayList<Object> models)
	{
		this.classifiers = new ArrayList<SavableClassifier>();
		this.classifiersInfo = new ArrayList<ClassifierInfo>();
		this.models = new ArrayList<Object>();
		
		for(int i = 0; i < classifiersInfo.size(); i++)
		{
			this.classifiers.add(classifiers.get(i));
			this.classifiersInfo.add(classifiersInfo.get(i));
			this.models.add(models.get(i));
			
		}
		
	}
	
}
