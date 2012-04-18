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
		   Filterpoint[][] leftTableDir = new Filterpoint[totaldirections][filtersize];
		   Filterpoint[][] rightTableDir = new Filterpoint[totaldirections][filtersize];

		   //direction 0
		   leftTableDir[0][0] = new Filterpoint(0,-1,-1);
		   leftTableDir[0][1] = new Filterpoint(1,-1,-1);
		   leftTableDir[0][2] = new Filterpoint(2,-1,-1);
		   leftTableDir[0][3] = new Filterpoint(3,-1,-1);
		   leftTableDir[0][4] = new Filterpoint(4,-1,-1);
		   leftTableDir[0][5] = new Filterpoint(5,-2,-1);
		   leftTableDir[0][6] = new Filterpoint(0,-2,-2);
		   leftTableDir[0][7] = new Filterpoint(1,-2,-2);
		   leftTableDir[0][8] = new Filterpoint(2,-2,-2);
		   leftTableDir[0][9] = new Filterpoint(3,-2,-2);
		   leftTableDir[0][10] = new Filterpoint(4,2,-2);
		   leftTableDir[0][11] = new Filterpoint(5,-2,-2);
		   leftTableDir[0][12] = new Filterpoint(0,-3,0);
		   leftTableDir[0][13] = new Filterpoint(1,-3,0);
		   leftTableDir[0][14] = new Filterpoint(2,-3,0);
		   leftTableDir[0][15] = new Filterpoint(3,-3,0);
		   leftTableDir[0][16] = new Filterpoint(4,-3,0);
		   leftTableDir[0][17] = new Filterpoint(5,-3,0);
		   leftTableDir[0][18] = new Filterpoint(0,-4,2);
		   leftTableDir[0][19] = new Filterpoint(1,-4,2);
		   leftTableDir[0][20] = new Filterpoint(2,-4,2);
		   leftTableDir[0][21] = new Filterpoint(3,-4,2);
		   leftTableDir[0][22] = new Filterpoint(4,-4,2);
		   leftTableDir[0][23] = new Filterpoint(5,-4,2);
		   leftTableDir[0][24] = new Filterpoint(0,-5,1);
		   leftTableDir[0][25] = new Filterpoint(1,-5,1);
		   leftTableDir[0][26] = new Filterpoint(2,-5,1);
		   leftTableDir[0][27] = new Filterpoint(3,-5,1);
		   leftTableDir[0][28] = new Filterpoint(4,-5,1);
		   leftTableDir[0][29] = new Filterpoint(5,-5,1);
		   
		   rightTableDir[0][0] = new Filterpoint(0,1,-1);
		   rightTableDir[0][1] = new Filterpoint(1,1,-1);
		   rightTableDir[0][2] = new Filterpoint(2,1,-1);
		   rightTableDir[0][3] = new Filterpoint(3,1,-1);
		   rightTableDir[0][4] = new Filterpoint(4,1,-1);
		   rightTableDir[0][5] = new Filterpoint(5,1,-1);
		   rightTableDir[0][6] = new Filterpoint(0,1,-2);
		   rightTableDir[0][7] = new Filterpoint(1,2,-2);
		   rightTableDir[0][8] = new Filterpoint(2,2,-2);
		   rightTableDir[0][9] = new Filterpoint(3,2,-2);
		   rightTableDir[0][10] = new Filterpoint(4,2,-2);
		   rightTableDir[0][11] = new Filterpoint(5,2,-2);
		   rightTableDir[0][12] = new Filterpoint(0,3,0);
		   rightTableDir[0][13] = new Filterpoint(1,3,0);
		   rightTableDir[0][14] = new Filterpoint(2,3,0);
		   rightTableDir[0][15] = new Filterpoint(3,3,0);
		   rightTableDir[0][16] = new Filterpoint(4,3,0);
		   rightTableDir[0][17] = new Filterpoint(5,3,0);
		   rightTableDir[0][18] = new Filterpoint(0,4,2);
		   rightTableDir[0][19] = new Filterpoint(1,4,2);
		   rightTableDir[0][20] = new Filterpoint(2,4,2);
		   rightTableDir[0][21] = new Filterpoint(3,4,2);
		   rightTableDir[0][22] = new Filterpoint(4,4,2);
		   rightTableDir[0][23] = new Filterpoint(5,4,2);
		   rightTableDir[0][24] = new Filterpoint(0,5,1);
		   rightTableDir[0][25] = new Filterpoint(1,5,1);
		   rightTableDir[0][26] = new Filterpoint(2,5,1);
		   rightTableDir[0][27] = new Filterpoint(3,5,1);
		   rightTableDir[0][28] = new Filterpoint(4,5,1);
		   rightTableDir[0][29] = new Filterpoint(5,5,1);
		   
		   //direction 8 (to the left, 9 0'clock, or -180)
		   for(int i=0;i<filtersize; i++)
		   {   //the opposite of 0.
			   leftTableDir[8][i] = new Filterpoint(-1*(leftTableDir[0][i].xdisplacement), leftTableDir[0][i].ydisplacement,rightTableDir[0][i].value); 
			   rightTableDir[8][i] = new Filterpoint(-1*(rightTableDir[0][i].xdisplacement), rightTableDir[0][i].ydisplacement,rightTableDir[0][i].value); 
		   }
				   
		   //45 degree, direction 2
		   /* not correctly filled yet
		   leftTableDir[2][0] = new Filterpoint(-1,-1,-1);
		   leftTableDir[2][1] = new Filterpoint(0,-2,-1);
		   leftTableDir[2][2] = new Filterpoint(1,-3,-1);
		   leftTableDir[2][3] = new Filterpoint(2,-4,-1);
		   leftTableDir[2][4] = new Filterpoint(3,-5,-1);
		   leftTableDir[2][5] = new Filterpoint(4,-6,-1);
		   leftTableDir[2][6] = new Filterpoint(-2,-1,-2);
		   leftTableDir[2][7] = new Filterpoint(-1,-2,-2);
		   leftTableDir[2][8] = new Filterpoint(0,-3,-2);
		   leftTableDir[2][9] = new Filterpoint(1,-4,-2);
		   leftTableDir[2][10] = new Filterpoint(2,-5,-2);
		   leftTableDir[2][11] = new Filterpoint(3,-6,-2);
		   leftTableDir[2][12] = new Filterpoint(-2,-2,0);
		   leftTableDir[2][13] = new Filterpoint(-1,-3,0);
		   leftTableDir[2][14] = new Filterpoint(0,-4,0);
		   leftTableDir[2][15] = new Filterpoint(1,-5,0);
		   leftTableDir[2][16] = new Filterpoint(2,-6,0);
		   leftTableDir[2][17] = new Filterpoint(3,-7,0);
		   leftTableDir[2][18] = new Filterpoint(-3,-2,2);
		   leftTableDir[2][19] = new Filterpoint(-2,-4,2);
		   leftTableDir[2][20] = new Filterpoint(-1,-4,2);
		   leftTableDir[2][21] = new Filterpoint(0,-4,2);
		   leftTableDir[2][22] = new Filterpoint(1,-4,2);
		   leftTableDir[2][23] = new Filterpoint(2,-4,2);
		   leftTableDir[2][24] = new Filterpoint(-3,-3,1);
		   leftTableDir[2][25] = new Filterpoint(-2,-4,1);
		   leftTableDir[2][26] = new Filterpoint(-1,-5,1);
		   leftTableDir[2][27] = new Filterpoint(0,-6,1);
		   leftTableDir[2][28] = new Filterpoint(1,-7,1);
		   leftTableDir[2][29] = new Filterpoint(2,-8,1);
		   
		   rightTableDir[2][0] = new Filterpoint(2,1,-1);
		   rightTableDir[2][1] = new Filterpoint(3,0,-1);
		   rightTableDir[2][2] = new Filterpoint(4,-1,-1);
		   rightTableDir[2][3] = new Filterpoint(5,-2,-1);
		   rightTableDir[2][4] = new Filterpoint(6,-3,-1);
		   rightTableDir[2][5] = new Filterpoint(7,-4,-1);
		   rightTableDir[2][6] = new Filterpoint(2,2,-2);
		   rightTableDir[2][7] = new Filterpoint(3,1,-2);
		   rightTableDir[2][8] = new Filterpoint(4,0,-2);
		   rightTableDir[2][9] = new Filterpoint(5,-1,-2);
		   rightTableDir[2][10] = new Filterpoint(6,-1,-2);
		   rightTableDir[2][11] = new Filterpoint(7,-3,-2);
		   rightTableDir[2][12] = new Filterpoint(0,3,0);
		   rightTableDir[2][13] = new Filterpoint(1,3,0);
		   rightTableDir[2][14] = new Filterpoint(2,3,0);
		   rightTableDir[2][15] = new Filterpoint(3,3,0);
		   rightTableDir[2][16] = new Filterpoint(4,3,0);
		   rightTableDir[2][17] = new Filterpoint(5,3,0);
		   rightTableDir[2][18] = new Filterpoint(0,4,2);
		   rightTableDir[2][19] = new Filterpoint(1,4,2);
		   rightTableDir[2][20] = new Filterpoint(2,4,2);
		   rightTableDir[2][21] = new Filterpoint(3,4,2);
		   rightTableDir[2][22] = new Filterpoint(4,4,2);
		   rightTableDir[2][23] = new Filterpoint(5,4,2);
		   rightTableDir[2][24] = new Filterpoint(0,5,1);
		   rightTableDir[2][25] = new Filterpoint(1,5,1);
		   rightTableDir[2][26] = new Filterpoint(2,5,1);
		   rightTableDir[2][27] = new Filterpoint(3,5,1);
		   rightTableDir[2][28] = new Filterpoint(4,5,1);
		   rightTableDir[2][29] = new Filterpoint(5,5,1);
		  */
		   
		   //other directions' lookup table for filters to be added.
		   
		   //unit vector for directions, 
		   double unitvectorx[] = new double[totaldirections];
		   double unitvectory[] = new double[totaldirections];
		   for(int i=0; i<totaldirections; i++)
		   {
			   unitvectorx[i] = Math.cos(2*Math.PI*i/totaldirections);
			   unitvectory[i] = Math.sin(2*Math.PI*i/totaldirections);
		   }
		   System.out.println("unitx in direction 8"+unitvectorx[8]);
		   System.out.println("unity in direction 8"+unitvectory[8]);
		   
		   //start to search from seed point
  	       float response =0;
		   //init direction 
		   int currentdirection = initdirection;
		   int currentx = seedx, currenty = seedy;
		   int centerx = seedx, centery = seedy;
		   //while(!stop)
		   {
		     //for one centerline point
  		     //the left side
	  	     response =0;
		     int leftr = 0;
		     System.out.println("left side:");
		     for(int r=0; r< diameterlimit/2; r++)
		     {
		       System.out.println("r:"+r);	 
		       //use vectory for x, due to penpendiculr direction for searching r
			   currentx = centerx + (int) unitvectory[currentdirection]*r;
			   currenty = centery + (int) unitvectorx[currentdirection]*r;
			   System.out.println("currentx:"+currentx+" curreny:"+currenty);
			   int tmp = caclulateCorrelation(currentx, currenty, leftTableDir[currentdirection]);
			   System.out.println("current template response:"+tmp);
			   if (tmp > response)
			   {
				   response = tmp;
				   leftr = r;
				   System.out.print("response:"+response);
				   System.out.print("left:"+leftr);
			   }
		     }//end of left
		     leftr = leftr+3;//considering the height of the filter.
		     System.out.print("max response:"+response);
		     System.out.print("estimated left r:"+leftr);
 		     IJ.showStatus("estimated left r:"+leftr);

	          //the right side
	   	     response =0;
	   	     int rightr = 0;
	         for(int r=0; r< diameterlimit/2; r++)
	         {
		       currentx = centerx + (int) unitvectory[currentdirection]*r;
		       currenty = centery - (int) unitvectorx[currentdirection]*r;
		       System.out.println("currentx:"+currentx+" curreny:"+currenty);
		       int tmp = caclulateCorrelation(currentx, currenty, rightTableDir[currentdirection]);
		       if (tmp > response)
		       {
			     response = tmp;
			     rightr = r;
		       }
		     }
	         rightr=rightr+3;
		     System.out.println("max response:"+response);
		     System.out.println("estimated right r:"+rightr);
	      
		     //try other neighboring directions to find the best fit...
		     
	         //save centerx, centery, currentdirection and r  for extracted centerline point ..
		     
 		     //trace next step until stop criterion is met
		     centerx += (int)stepsize*unitvectorx[currentdirection];
		     centery += (int)stepsize*unitvectory[currentdirection];
		     
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

}
