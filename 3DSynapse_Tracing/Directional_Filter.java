import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

class Filterpoint
{
	   Filterpoint(int x, int y, int v) {xdisplacement = x; ydisplacement = y; value = v;}
	   int xdisplacement;
	   int ydisplacement;
	   int value;
}

public class Directional_Filter implements PlugIn {

	   static final String pluginName = "Directional Filter";
	   boolean debug = false;
	   ImagePlus img;
	   ImageProcessor ip;

	   Filterpoint[][] leftTableDir;
           Filterpoint[][] rightTableDir; 
	   double unitvectorx[] ;
	   double unitvectory[] ;
	   
	   public void run(String arg) {
	        if (! setupGUI(arg)) return;
	        analyze();
	    }
	   
	   public boolean setupGUI(String arg) {
		   
		img = WindowManager.getCurrentImage();
		    if (img==null){
	            IJ.noImage();
	            return false;
	        } else if (img.getStackSize() > 1) {
	            IJ.error("2D image required");
	            return false;
	        }
	        /*if (img.getType() != ImagePlus.COLOR_RGB)
	        {
	            IJ.error("RBG image with channels required for 2 channel analysis");
	            return false;
	        }*/
	        ip=img.getProcessor();
	        
		return true;
	   }
	   
	   
	   
	   void analyze() 
	   {
		   IJ.showStatus("start analyzing ...");
		   //seed point; interactively and from BioCAT. 
                   //For patch tracing, starting points will come from boundaries
		   int seedx = 1018;
		   int seedy = 330;	

		   //adjustable parameters
		   // can be learned iteratively and initial value varies based on type of branch
		   int totaldirections = 16; //16 direction 0-15 clockwise, with 0 at 3 o'clock.
		   int initdirection = 8;  //to the left
		   int filterwidth = 6;  // K 
		   int diameterlimit = 84; //maximum expected diameter of the branch.(a difference in diame terdue to filter height) 
		   int stepsize = 3;  //the smaller the more accurate tracing, yet the slower.
		   
		   //tables for filters
		   int filtersize = 5*filterwidth; 
		   leftTableDir = new Filterpoint[totaldirections][filtersize];
		   rightTableDir = new Filterpoint[totaldirections][filtersize];
		   Masks.setMasks(leftTableDir, rightTableDir);

		   //unit vector for all the directions, 
		   unitvectorx[] = new double[totaldirections];
		   unitvectory[] = new double[totaldirections];
		   for(int i=0; i<totaldirections; i++)
		   {
			unitvectorx[i] = Math.cos(2*Math.PI*i/totaldirections);
			unitvectory[i] = Math.sin(2*Math.PI*i/totaldirections);
		   }

		   System.out.println("unitx in direction 8"+unitvectorx[8]);
		   System.out.println("unity in direction 8"+unitvectory[8]);
		   
		   //start to search from seed point
  	           float response =0, tmp = 0;
		   //init direction 
		   int currentDirection = initDirection;
		   int currentx = seedx, currenty = seedy;
		   int centerx = seedx, centery = seedy;
                   int updatedDirection; 
                   int diam; 
		   //while(!stop)
		   {
	      
		     //try other neighboring directions (total 7 directions) to find the best fit...
                     // . to update the current direction...
                     int[] diameters = new int[1];
                     
                     for(int i = (currentdirection -3 + 16) %16; i < (currentdirection+3) %16; i++)
                     {
                        tmp = getResponseForADirection(centerx, centery, i, diameter);
                        if (tmp > response)
		        {
			     response = tmp;
			     diam = diameter[0];
                             updatedDirection = i;
		        }
                     }
		     currentDirection = updatedDirection;

		     //save centerx, centery, currentdirection and r  for extracted centerline point ..
 		     //trace next step until stop criterion is met
		     centerx += (int)stepsize*unitvectorx[currentDirection];
		     centery += (int)stepsize*unitvectory[currentDirection];
		     
		     //check stop criterion
		     //a. if centerx and center y are outside image field  (or surrounding is too dark?)
		     //b. if connected with other detected regions
		     //c. if find seeds of different cateogry (medium, thin ...)
		     //d. if combined response is too low.
		     
		     
		     
	       }//end of while
      }


      //calculate the response of filter based on image at starting position x, y
      int caclulateCorrelation(int x, int y, Filterpoint[] filter)
      {
    	  int sum =0;
    	  for(int i=0; i<filter.length; i++)
    		  sum+=(ip.getPixel(x + filter[i].xdisplacement, y + filter[i].ydisplacement))*filter[i].value;
    	  return sum;
      }

      // pass in a direction,  return the response (avg of left and right), and a diameter (via array)
      //
      double getResponseForADirection(int centerx, int centery, int direction, int[] diameter)
      {
   
	     //for one centerline point
  	     //the left side
	     double leftresponse =0, rightresponse = 0;
	     int leftr = 0; //final estimation
	     System.out.println("left side:");
	     for(int r=0; r< diameterlimit/2; r++)
	     {
	           System.out.println("current left radius:"+r);	 
	           //use vectory for x, due to penpendiculr direction for searching r
		   currentx = centerx + (int) unitvectory[currentdirection]*r;
		   currenty = centery + (int) unitvectorx[currentdirection]*r;
		   System.out.println("currentx:"+currentx+" curreny:"+currenty);

		   int tmp = caclulateCorrelation(currentx, currenty, leftTableDir[currentdirection]);
		   System.out.println("current template response:"+tmp);
		   if (tmp > leftresponse)
		   {
			   leftresponse = tmp;
			   leftr = r;
			   System.out.print("left response:"+leftresponse);
			   System.out.print("left:"+leftr);
		   }
	     }//end of left
	     leftr = leftr+3;//adjust the final result considering the height of the filter.
	     System.out.print("max left response:"+leftresponse);
	     System.out.print("estimated left r:"+leftr);
	     IJ.showStatus("estimated left r:"+leftr);

             //the right side
   	     rightresponse =0;
   	     int rightr = 0;
	     for(int r=0; r< diameterlimit/2; r++)
	     {
		   currentx = centerx + (int) unitvectory[currentdirection]*r;
		   currenty = centery - (int) unitvectorx[currentdirection]*r;
		   System.out.println("currentx:"+currentx+" curreny:"+currenty);
		       int tmp = caclulateCorrelation(currentx, currenty, rightTableDir[currentdirection]);
		       if (tmp > rightresponse)
		       {
			     rightresponse = tmp;
			     rightr = r;
		       }
		     }
	             rightr=rightr+3;
		     System.out.println("max right response:"+rightresponse);
		     System.out.println("estimated right r:"+rightr);
             }
            
             diameter[0] = leftr+rightr;
             return (leftresponse + rightresponse)/2;
}
