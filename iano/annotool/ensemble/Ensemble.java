package annotool.ensemble;


import annotool.Annotation;

public interface Ensemble {
	
	public void addResult(Annotation[] annotedPredictions);
	
	public int[] classify();

}
