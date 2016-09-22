Table table, tableSynapse;
int counter=0;

void setup() {
  
   size(1024,1024, P3D); 
   background(20,15,15);  
   smooth(8);
   
   //load neurite table
   table = loadTable("kibra008.swc", "header, csv");
   //table = loadTable("rdl.swc", "header, csv");
   println(table.getRowCount() + " neurite points loaded."); 
   
   tableSynapse = loadTable("synapses008.marker", "header, csv");
   //tableSynapse = loadTable("synapseRdl.marker", "header, csv");
   println(tableSynapse.getRowCount() + " synapses loaded.");
   
   
   //******************************
   // lighting
   
   //lights();
   //spotLight(255, 255, 230, width/2, height/2, 400, 0, 0, -1, PI/4, 2);
   //pointLight(255, 0, 0, width/2, height/2, 400);
   float dimmer = 0.7;
   float dimmer2 = 0.8;
   directionalLight(220*dimmer2, 214*dimmer2, 170*dimmer2, 0, 1, -3);
   //directionalLight(220*dimmer, 214*dimmer, 170*dimmer,1000,0,-5);
   ambientLight(220*dimmer,214*dimmer,200*dimmer, 0,0,-400);
 
  
  
  //camera controls
  camera(width/2, height/2, (height/2)/.5, width/2, height/2, 0, 0, 1, 0); 
  
  
   //*****************************
   //draw axis
   float axisX=1024, axisY=1024, axisZ=0;
   pushMatrix();
   strokeCap(ROUND);
   noFill();
     //X axis
   stroke(200,0,0); //R
   strokeWeight(3);
   line(axisX,axisY,axisZ,0,axisY,axisZ);
     //Y axis
   stroke(0,200,0); //G
   strokeWeight(3);
   line(axisX,axisY,axisZ,axisX,50,axisZ);
     //Z axis
   stroke(0,70,220); //B
   strokeWeight(3.5);
   line(axisX,axisY,axisZ,axisX-20,axisY,125);
   popMatrix();
   
   
   //*****************************
   //Draw synapse spheres
   float synX, synY, synZ;
   int rad;
   float r, g, b;
   
   
   noStroke();
   sphereDetail(60);
   for(TableRow row : tableSynapse.rows()) {
     synX = row.getFloat("x");
     synY = row.getFloat("y");
     synZ = row.getFloat("z");
     rad  = row.getInt("radius");
     r    = row.getFloat("red");
     g    = row.getFloat("green");
     b    = row.getFloat("blue");
     
     
     pushMatrix();
     translate(synX, synY, synZ);
     fill(r-20,g-20,b+20);
     sphere(4);     
     popMatrix(); 
     

     
   }
   
   println("done with synapses!");
  
 
 
  //*********************************
  //draw neurite
  int id;
  float x,y,z,radius, opacity;
  
  
  //draw neurite center
  stroke(100,225,120,200);
  strokeWeight(1.5);
  for (TableRow row : table.rows()) {
      
      //id = row.getInt("##n");
      x = row.getFloat("x");
      y = row.getFloat("y");
      z = row.getFloat("z");
      //radius = row.getFloat("radius");

      point(x,y,z);    
      
   }   
  
   
   
  //draw spheres
  noStroke();
  sphereDetail(30);
  for (TableRow row : table.rows()) {
      
      id = row.getInt("##n");
      x = row.getFloat("x");
      y = row.getFloat("y");
      z = row.getFloat("z");
      radius = row.getFloat("radius");
      
      
      //set thicker branches to lower opacity
      opacity = 45-radius*1.5;
      if(opacity < 15) {
        opacity = 15;
      } 

      fill(150,150,200,opacity);
      pushMatrix();
      translate(x,y,z);
      sphere(radius);
      popMatrix();

      counter++;
   }
   
   
   println("done with reconstruct!");
   
   
   
   //****************************
   //saving file
   save("kibra_008_render.tif");
   println("done saving!");
}    