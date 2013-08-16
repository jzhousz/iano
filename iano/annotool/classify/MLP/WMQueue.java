package annotool.classify.MLP;

import java.util.LinkedList;
import java.util.Queue;
import java.io.Serializable;


public class WMQueue implements Serializable  {

	private Queue<Double> q = new LinkedList<Double>();
	private Double sum = (double) 0;
	private int window;
	
	 WMQueue( int new_window )
	 {
		 window = new_window;
	 }
	
	public Double push(Double d)
	{
		sum += d;
		q.add(d);
		if(window == 0)
		{
			sum -= q.remove();
		}
		else
			window--;
		
		return sum;
	}

	public Double sum()
	{
		return sum;
	}
	

	public int QSize()
	{
		return q.size();
	}
}
