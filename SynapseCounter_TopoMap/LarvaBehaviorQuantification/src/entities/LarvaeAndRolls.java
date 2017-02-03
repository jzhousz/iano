package entities;

import java.util.ArrayList;

/**
* Use this class to encapsulate the frames where the larva rolling
*  and number of rolls the larva rolled during the frames.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   11-09-2016
*/
public class LarvaeAndRolls
{
	// list of frame id where the larva is rolling
	private ArrayList<Integer> frameIds = null;
	// number of rolls the larva rolled.
	private double rolls = 0;
	
	public double getRolls()
	{
		return rolls;
	}
	public void setRolls(double rolls)
	{
		this.rolls = rolls;
	}
	public ArrayList<Integer> getFrameIds()
	{
		return frameIds;
	}
	public void setFrameId(ArrayList<Integer> frameIds)
	{
		this.frameIds = frameIds;
	}
}
