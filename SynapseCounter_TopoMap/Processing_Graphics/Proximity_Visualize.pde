Table table, tableSynapse;
int counter=0;

void setup() {
  
   size(1400,1400, P3D); 
   background(10,15,15);  
   smooth(8);
 
   //load neurite table
   table = loadTable("kibra004.swc", "header, csv");
   println(table.getRowCount() + " total rows in table"); 
   
   //******************************
   // lighting
   
   //lights();
   //spotLight(255, 255, 230, width/2, height/2, 400, 0, 0, -1, PI/4, 2);
   //pointLight(255, 0, 0, width/2, height/2, 400);
   float dimmer = 0.25;
   directionalLight(255, 255, 255, 0, 1, -1);
   ambientLight(255*dimmer,214*dimmer,170*dimmer, 0,0,-400);
 
  
  
  //camera controls
  //camera(width/2 + 300, height/2 + 300, (height/2) / tan(PI/6), width/2, height/2, 0, 0, 1, 0);
  camera(width/2 , height/2-300, (height/2)/4, width/2 -50, height/2 -375 , 0, 0, 1, 0);
  
  
  //draw bgPlane
  /*translate(0,0,-160);
  fill(50,50,50);
  rectMode(CENTER);
  rect(0,0,1024,1024);
  */
  
  //*********************************
  //draw neurite
  sphereDetail(150);
  noStroke();
  fill(0,255,120);
  for (TableRow row : table.rows()) {
      
      int id = row.getInt("##n");
      float x = row.getFloat("x");
      float y = row.getFloat("y");
      float z = row.getFloat("z");
      float r = row.getFloat("radius");
      
      //println("id:" + id + " " + x + ", " + y + ", " + z + ", " + "rad: " +r );
      
      if(counter > 5000 && counter < 10000){
        fill(0,220,120);
        pushMatrix();
        translate(x,y,z);
        if(counter>9232 && counter < 9235) fill(230,50,0);
        sphere(r);
        popMatrix();
      
      }
      counter++;
      //if( counter > 5) break;
   }
   
   println("done with reconstruct!");
   
   
   //*****************************
   //Draw synapse
   int synX=650, synY=336, synZ=25;
   
   
   //draw rings and shell
   pushMatrix();
   translate(synX, synY, synZ);
   
     //draw shell
   noFill();
   stroke(200,0,0,75);
   sphereDetail(30);
   sphere(25);
   
     //draw rings
   noFill();
   strokeWeight(2);
   for(int i=0; i <= 50; i+=5) {
       stroke(0 + 4*i,0, 255 - 4*i);
       ellipse(0,0,i,i); 
         
   }
   
   popMatrix();
   
   
   //draw center
   pushMatrix();
   translate(synX, synY, synZ);
   fill(0,0,255);
   noStroke();
   sphereDetail(30);
   sphere(5); 
   popMatrix();
   
   //save
   save("proximity_visual.tif");
   
  
}    