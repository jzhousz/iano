/* ThreeDRoiAnnotator - Jon Sanders 2/14/2014
 * Gui for running the ThreeDROIAnnotator out of BIOCat as an ImageJ plugin
 *
 */
 
 
 /* NOTES and TODO
  * 02/16/14 looking into awt file choosers from venkat
  * 02/18/14 implement rest of GUI, for now, add logic for annotation
  *
  * TODO
  * add support to trainf first. report success to user, and run on new image
  * fix file saving locations, can use similar logic, but requires rework of TDROIAnnotator code.
  */
  
 import ij.*;
 import ij.io.*;
 import ij.IJ;
 import ij.plugin.filter.PlugInFilter;
 import ij.process.*;
 import ij.gui.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.Button;
 import java.util.HashSet;
 
 public class Three_D_ROI_Annotator_Plugin implements PlugInFilter {
   
    int width;
    int depth;
    int height;
    String posRoiPath, negRoiPath, chainPath, savePath;
    HashSet<Point3D> centers;
    ThreeDROISynapseDriver anno;
    
    //static and default vairables
    public static int min_roi_height = 1;
    public static int min_roi_width  = 1;
    public static int min_roi_depth  = 1;
    public static int max_roi_height = 20;
    public static int max_roi_width  = 20;
    public static int max_roi_depth  = 10;
    public static int def_roi_height = 9;
    public static int def_roi_width  = 9;
    public static int def_roi_depth  = 3;
    
    
    
    //gui elements
    GenericDialog gd;
    Panel posChooser, negChooser, chainChooser, saveChooser, saveDirP, SaveNameP, optionsP;
    Button posRoiB, negRoiB, chainB, saveB, trainB;
    TextField posRoiField, negRoiField, chainField, saveLocField, saveNameField;
    Checkbox imageCB;
 
    public int setup(String arg, ImagePlus imp) {
    
        return DOES_8G;
    }
    
    public void run(ImageProcessor ip) {
    	
    	System.out.println("debug test");//debug
        //setup gui
        if (! makeGUI()) return;
        //store data from gui fields
        if (! getDataFromFields()) {
            IJ.log("Missing or incorrect field data.");
            return;
        } 
        
        annotate();
                
        
    }
 
    //create the gui, if completed returns true to run method
    private boolean makeGUI() {
        
        //setup gui main panel
        gd =  new GenericDialog("3D ROI Annotator");
           // gd.setLayout(new GridLayout(10, 1));
       
        MyListener listener = new MyListener(); 
        
        
        //browser for posRoiB file
        posChooser = new Panel();
            posChooser.setLayout(new FlowLayout(FlowLayout.CENTER,5,0));       
        posRoiB = new Button("Positive");
            posRoiB.addActionListener(listener);
        posRoiField = new TextField(Prefs.get("batch.posRoiB",""), 40);      
        posChooser.add(posRoiB);
        posChooser.add(posRoiField);
        
        //browser for negRoiB file
        negChooser = new Panel();
            negChooser.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));       
        negRoiB = new Button("Negative");
            negRoiB.addActionListener(listener);
        negRoiField = new TextField(Prefs.get("batch.negRoiB",""), 40);       
        negChooser.add(negRoiB);
        negChooser.add(negRoiField);
 
        //browser for chainB file
        
        chainChooser = new Panel();
            chainChooser.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        chainB = new Button("Chain file");
            chainB.addActionListener(listener);
        chainField = new TextField(Prefs.get("batch.chainB", ""), 40);
        chainChooser.add(chainB);
        chainChooser.add(chainField);
        
        //browser for saveB location
        saveChooser = new Panel();
            saveChooser.setLayout(new GridLayout(0,1));
        saveB = new Button("Save Folder");
            saveB.addActionListener(listener);
        saveLocField = new TextField(Prefs.get("batch.saveB",""), 40);
        saveNameField = new TextField("Results", 20);
        
        saveDirP = new Panel();
            saveDirP.setLayout(new FlowLayout(FlowLayout.LEFT,5,0));
        saveDirP.add(saveB);
        saveDirP.add(saveLocField);
        
        SaveNameP = new Panel();
            SaveNameP.setLayout(new FlowLayout(FlowLayout.LEFT,5,0));
        Label saveL = new Label("File name:  ");
        SaveNameP.add(saveL);
        SaveNameP.add(saveNameField);
        
        saveChooser.add(saveDirP);
        saveChooser.add(SaveNameP);
        
        //checkbox options setup
        optionsP = new Panel();
            optionsP.setLayout(new FlowLayout(FlowLayout.LEFT,5,0));
        imageCB = new Checkbox("different annotation image");
        
        optionsP.add(imageCB);
        
        
        //put it all in the dialog
        gd.addMessage("Positive ROI file:");
        gd.addPanel(posChooser);
        gd.addMessage("Negative ROI file:");
        gd.addPanel(negChooser);
        gd.addMessage("BioCAT chain file:");
        gd.addPanel(chainChooser);
        
        gd.addMessage("\nROI dimensions:");
        gd.addSlider("Roi height", min_roi_height, max_roi_height, def_roi_height);
        gd.addSlider("Roi width ", min_roi_width, max_roi_width, def_roi_width);
        gd.addSlider("Roi depth ", min_roi_depth, max_roi_depth, def_roi_depth);
        
        gd.addMessage("\nSave Location and file name:");
        gd.addPanel(saveChooser);
        
        gd.addMessage("\nOther options:");
        gd.addPanel(optionsP);
        
        gd.showDialog();
        
        if(gd.wasCanceled())
            return false;
    
        return true;
        

    }//end makeGUI
    
    
    //get all the data from the gui fields and put in vars
    //check for empty fields
    private boolean getDataFromFields() {
    
            posRoiPath = posRoiField.getText();
            negRoiPath = negRoiField.getText();
            chainPath  = chainField.getText();
            
            height = (int) gd.getNextNumber();
            width  = (int) gd.getNextNumber();
            depth  = (int) gd.getNextNumber();
            
            savePath = (saveLocField.getText() + saveNameField.getText());
    
            if(posRoiPath.length()==0 || negRoiPath.length()==0 || chainPath.length()==0 || savePath.length()==0) 
               return false;
    
            //print param list to log
            String s = ", ";
            IJ.log("PARAMS: " + height + s + width + s + depth + s + posRoiPath + s + negRoiPath + s + chainPath );
    
           return true;
    }//end getDataFromFields
    
    
    //annotate the same image after training. todo fix for new image
    private void annotate() {
        try{
        
            ImagePlus imp = WindowManager.getCurrentImage();
            
            //if new image option, use it for annotation
            ImagePlus annoImp = imp;
            
            if(imageCB.getState()) {
                 OpenDialog od = new OpenDialog("Select an image to be annotated");
                 annoImp = IJ.openImage(od.getPath());
                
            }
            
            IJ.showStatus("ROI annotation starting.");
        
            //pack roi path strings
            String[] roiPaths = new String[2];
            roiPaths[0] = posRoiPath;
            roiPaths[1] = negRoiPath;
            
			//construct
            IJ.showStatus("Building data...test");
			anno = new ThreeDROISynapseDriver(imp, roiPaths, depth, width, height, chainPath);		
			IJ.log("sucessful data build");
			
			
			//train test model
            IJ.showStatus("Training and testing...");
            IJ.log("Training and testing...");
            float rate = anno.trainAndTest();	
            IJ.log("recognition rate: " + rate);
            
            
            //annotate
            IJ.showStatus("Annotating...");
            IJ.log("Annotating...");
			anno.annotate(annoImp, savePath, 3);

            IJ.log("Finished!");
            
            //print centers to log
            centers = anno.getDetectedCenters();
            IJ.log("\nDetected Centers: " + centers.size());
			IJ.log("x, y, z");//.marker csv format
		    for(Point3D p : centers)
			{
				IJ.log(p.x + " " + p.y + " " + p.z); //echo to console
			}
          
            
		}
		catch(Exception e) {
			e.printStackTrace();
            IJ.log(e.toString());
        }
	}//end annotate
    
    //crude inner class listener for custom buttons
    private class MyListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
      
        Object source = e.getSource();
        String s;
        String path ="default";
        OpenDialog od;
        DirectoryChooser dc;
        
        if(source.equals(posRoiB)) {
            //IJ.log("posRoiB button press");
            od = new OpenDialog("select positive ROI file", "");
            path = od.getPath();
            if( path.equals(null)) return;
            posRoiField.setText(path);
        } else if(source.equals(negRoiB)) {
            //IJ.log("negRoiB button press");
            od = new OpenDialog("select negative ROI file", "");
            path = od.getPath();
            if( path.equals(null)) return;
            negRoiField.setText(path);
        } else if(source.equals(chainB)) {
            od = new OpenDialog("select BIOCAT chain file", "");
            path = od.getPath();
            if( path.equals(null)) return;
            chainField.setText(path);
            
        } else if(source.equals(saveB)) {
            dc = new DirectoryChooser("select directory to save files to");
            path = dc.getDirectory();
            if( path.equals(null)) return;
            saveLocField.setText(path);
            
        }
        
        
      }
    }//end inner class MyListener
  
 }//endclass ThreeDRoiAnnotator_