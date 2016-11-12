package manager;

import java.util.ArrayList;

import entities.Larva;

/**
* The class used to calculate summary information.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class SummaryManager 
{

	private ArrayList<Larva> listLarva = null; 
		
	public SummaryManager(ArrayList<Larva> listLarva)
	{
		this.listLarva = listLarva;
	}
	
	public void show()
	{
		System.out.println("\n=================================================================");
		System.out.println("========================= Statistic  ============================");
		System.out.println("=================================================================");
		
//		for( Larva larva : listLarva )
//		{
//			if( larva.getLarvaPrevious() != null )
//			{
//				System.out.println("[listLarva] larva frameId: " + larva.getFrameId()+", previous frameId:"+larva.getLarvaPrevious().getFrameId());
//			}else{
//				System.out.println("[listLarva] larva frameId: " + larva.getFrameId()+", previous frameId:none");
//			}
//		}
		
		int AmountEndPoint1Win = 0; 
		int AmountEndPoint2Win = 0;
		
//		double distanceEndPoint1 = 0;
//		double distanceEndPoint2 = 0;
	
		int AmountEndPoint1Win_DistanceMoved = 0; 
		int AmountEndPoint2Win_DistanceMoved = 0;
		
		//****************
		//*************** Note: need to check whether the information of larva is valid
		//*************** e.g, only one end point or more than 2 end points, not center point, etc.
		for( Larva larva : listLarva )
		{
//			distanceEndPoint1 += larva.getDistanceEndPoint1();
//			distanceEndPoint2 += larva.getDistanceEndPoint2();
			
			if( larva.getIsEndPoint1WinDistanceMoved() )
				AmountEndPoint1Win_DistanceMoved ++;
			else
				AmountEndPoint2Win_DistanceMoved ++;
			
			if( larva.getIsEndPoint1WinPixelLevel() )
				AmountEndPoint1Win ++;
			else 
				AmountEndPoint2Win ++;
		}
		
		if( AmountEndPoint1Win_DistanceMoved >= AmountEndPoint2Win_DistanceMoved )
			System.out.println("{Distance Moved Votes} End point 1 is the head. Got "+ 
					AmountEndPoint1Win_DistanceMoved +" votes out of " + 
					(AmountEndPoint1Win_DistanceMoved+AmountEndPoint2Win_DistanceMoved) +
					". %" + (double) ( (double) AmountEndPoint1Win_DistanceMoved / (double) (AmountEndPoint1Win_DistanceMoved+AmountEndPoint2Win_DistanceMoved) ) +
					" wins.");
		else
			System.out.println("{Distance Moved Votes} End point 2 is the head. Got "+ 
					AmountEndPoint2Win_DistanceMoved +" votes out of " + 
					(AmountEndPoint1Win_DistanceMoved+AmountEndPoint2Win_DistanceMoved) +
					". " + (double) ( (double) AmountEndPoint2Win_DistanceMoved / (double) (AmountEndPoint1Win_DistanceMoved+AmountEndPoint2Win_DistanceMoved) ) +
					" wins.");
		
		
		if( AmountEndPoint1Win >= AmountEndPoint2Win )
			System.out.println("{Pixel Level Votes} End point 1 is the head. Got "+ AmountEndPoint1Win +" votes out of " + (AmountEndPoint1Win+AmountEndPoint2Win));
		else
			System.out.println("{Pixel Level Votes} End point 2 is the head. Got "+ AmountEndPoint2Win +" votes out of " + (AmountEndPoint1Win+AmountEndPoint2Win));
		
	}
}
