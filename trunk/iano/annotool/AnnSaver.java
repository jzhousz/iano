package annotool;

import java.io.*;


//This class is  a work-in-progress for saving workspace to a project.
public class AnnSaver {
	
	
   public void save()
   {
	//prompt a filename for saving.
	PrintWriter pw = getSaveFile();
	//save all parameters 
	saveParas(pw);
	//save mode and results: if tt or cv, save recognition rates;
	saveResults(pw);
	//if roi, save  annotation results.
	pw.close();
   }
   
   //get a file opened for writing
   private PrintWriter getSaveFile()
   {
	   PrintWriter bw = null;
		ij.io.SaveDialog sd = new ij.io.SaveDialog("Save file...", "save",".txt");
		String directory = sd.getDirectory();
		String fileName = sd.getFileName();
		try {
			bw = new PrintWriter(directory+fileName);
		}catch(Exception e)
		{
			System.out.println("file opening error");
		}
	   return bw;
   }
   
   private void saveResults(PrintWriter pw)
   {
	   //if tt or cv, save recognition rates;
		//if roi, save  annotation results.
   }
	
   private void saveParas(PrintWriter pw)
   {
	   pw.println("imgdir="+Annotator.dir);
	   pw.println("imgext="+Annotator.ext);
	   pw.println("target="+Annotator.targetFile);
  	   pw.println("testimgdir="+Annotator.testdir);
	   pw.println("testimgext="+Annotator.testext);
	   pw.println("testtarget="+Annotator.testtargetFile);
	   pw.println("extractor="+Annotator.featureExtractor);
	   pw.println("selector="+Annotator.featureSelector);
	   pw.println("classifier="+Annotator.classifierChoice);
	   pw.println("numoffeature="+Annotator.featureNum);
	   pw.println("channel="+Annotator.channel);
	   //pw.println("debug="+Annotator.debugFlag);
	   pw.println("fileflag="+Annotator.fileFlag);
	   pw.println("discreteflag="+Annotator.discreteFlag);
	   pw.println("threshold="+Annotator.threshold);
	   pw.println("shuffleflag="+Annotator.shuffleFlag);
	   pw.println("svmpara="+Annotator.svmpara);
	   pw.println("waveletlevel="+Annotator.waveletLevel);
	   pw.println("fold="+Annotator.fold);
	   pw.println("output="+Annotator.output); //this is the mode!
   }
   
}
