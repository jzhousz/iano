package annotool.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import annotool.gui.model.ClassifierChain;
import annotool.gui.model.Chain;
import annotool.gui.model.Extractor;
import annotool.gui.model.Selector;


/**
 * Saves and loads the list of algorithm chains for comparison mode.
 * It allows the chain to be constructed once, saved in a file and loaded later for execution.
 * 
 * It is different than ChainModel which is for saving a trained model.
 * 
 */
public class ChainIO {
	public ChainIO() {
	}
	/*
	 * Saves the list of algorithm chains to file
	 */
	public void save(File file, ArrayList<Chain> chainList) throws IOException{		
		//Cross platform new line character
    	String newLine = System.getProperty("line.separator");
    	
    	BufferedWriter writer = null;
    	try {
	    	writer = new BufferedWriter(new FileWriter(file));
	  
	    	for(Chain chain : chainList) {
	    		writer.write("[CHAIN_START]" + newLine);
	    		writer.write("Name=" + chain.getName() + newLine);
	    		for(Extractor ex : chain.getExtractors()) {
		        	//Write feature extractor
		        	writer.write("[FEATURE_EXTRACTOR]" + newLine);
		        	writer.write("Name=" + ex.getName() + newLine);
		        	writer.write("ClassName=" + ex.getClassName() + newLine);
		        	writer.write("Path=" + ex.getExternalPath() + newLine);
		        	writer.write("[PARAMETER_START]" + newLine);
		        	for (String parameter : ex.getParams().keySet()) {
		        		writer.write(parameter + "=" +ex.getParams().get(parameter) + newLine);
		        	}
		        	writer.write("[PARAMETER_END]" + newLine);
	        	}
	    		
	    		for(Selector sel : chain.getSelectors()) {
		        	//Write feature selector
		        	writer.write("[FEATURE_SELECTOR]" + newLine);
		        	writer.write("Name=" + sel.getName() + newLine);
		        	writer.write("ClassName=" + sel.getClassName() + newLine);
		        	writer.write("Path=" + sel.getExternalPath() + newLine);
		        	writer.write("[PARAMETER_START]" + newLine);
		        	for (String parameter : sel.getParams().keySet()) {
		        		writer.write(parameter + "=" +sel.getParams().get(parameter) + newLine);
		        	}
		        	writer.write("[PARAMETER_END]" + newLine);
	        	}
	        	
	    		
	    		/* added 1/16/2014 */
	    		for(ClassifierChain Class : chain.getClassifier()) {
	    			//Write classifier
		        	writer.write("[CLASSIFIER]" + newLine);
		        	writer.write("Name=" + Class.getName() + newLine);
		        	writer.write("ClassName=" + Class.getClassName() + newLine);
		        	writer.write("Path=" + Class.getExternalPath() + newLine);
		        	writer.write("[PARAMETER_START]" + newLine);
		        	for (String parameter : Class.getParams().keySet()) {
		        		writer.write(parameter + "=" + Class.getParams().get(parameter) + newLine);
		        	}
		        	writer.write("[PARAMETER_END]" + newLine);
	        	}
	    		
	    		/* Removed 1/16/2014
	        	if(chain.getClassifier() != null) {
		        	//Write classifier
		        	writer.write("[CLASSIFIER]" + newLine);
		        	writer.write("Name=" + chain.getClassifier() + newLine);
		        	writer.write("ClassName=" + chain.getClassifierClassName() + newLine);
		        	writer.write("Path=" + chain.getClassifierExternalPath() + newLine);
		        	//Write classifier parameters
		        	writer.write("[PARAMETER_START]" + newLine);
		        	for (String parameter : chain.getClassParams().keySet()) {
		        		writer.write(parameter + "=" +chain.getClassParams().get(parameter) + newLine);
		        	}
		        	writer.write("[PARAMETER_END]" + newLine); 
	        	}
	        	*/
	        	writer.write("[CHAIN_END]" + newLine);
			}//End of for loop
	
	    	writer.flush();
    	}
    	catch (IOException ex) {
    		System.out.println("Exception occured while writing file: " + file.getName());
        	ex.printStackTrace();
        	throw ex;
    	}
    	finally {
    		writer.close();
    	}
	}
	
	//This method is added for saving the results of the chain. 
	//It is just for providing convenience when dispatching the chain
	//to a node on a cluster in super computer.  It is meant 
	//to be saved with other data info (e.g., dir, ext, channel). 
	//Note that different from ChainModel, it does not contain model parameters.
	public void saveRanks(File file, ArrayList<Chain> chainList, float[][] rates, int chainNum) throws IOException{		
		//Cross platform new line character
    	String newLine = System.getProperty("line.separator");
    	
    	BufferedWriter writer = null;
    	try {
	    	writer = new BufferedWriter(new FileWriter(file));
	  
	    	for(Chain chain : chainList) {
	    		writer.write("[CHAIN_START]" + newLine);
	    		writer.write("Name=" + chain.getName() + newLine);
	    		for(Extractor ex : chain.getExtractors()) {
		        	//Write feature extractor
		        	writer.write("[FEATURE_EXTRACTOR]" + newLine);
		        	writer.write("Name=" + ex.getName() + newLine);
		        	writer.write("ClassName=" + ex.getClassName() + newLine);
		        	writer.write("Path=" + ex.getExternalPath() + newLine);
		        	writer.write("[PARAMETER_START]" + newLine);
		        	for (String parameter : ex.getParams().keySet()) {
		        		writer.write(parameter + "=" +ex.getParams().get(parameter) + newLine);
		        	}
		        	writer.write("[PARAMETER_END]" + newLine);
	        	}
	    		
	    		for(Selector sel : chain.getSelectors()) {
		        	//Write feature selector
		        	writer.write("[FEATURE_SELECTOR]" + newLine);
		        	writer.write("Name=" + sel.getName() + newLine);
		        	writer.write("ClassName=" + sel.getClassName() + newLine);
		        	writer.write("Path=" + sel.getExternalPath() + newLine);
		        	writer.write("[PARAMETER_START]" + newLine);
		        	for (String parameter : sel.getParams().keySet()) {
		        		writer.write(parameter + "=" +sel.getParams().get(parameter) + newLine);
		        	}
		        	writer.write("[PARAMETER_END]" + newLine);
	        	}
	        	
	    		
	    		/* added 1/16/2014 */
	    		for(ClassifierChain Class : chain.getClassifier()) {
	    			//Write classifier
		        	writer.write("[CLASSIFIER]" + newLine);
		        	writer.write("Name=" + Class.getName() + newLine);
		        	writer.write("ClassName=" + Class.getClassName() + newLine);
		        	writer.write("Path=" + Class.getExternalPath() + newLine);
		        	writer.write("[PARAMETER_START]" + newLine);
		        	for (String parameter : Class.getParams().keySet()) {
		        		writer.write(parameter + "=" + Class.getParams().get(parameter) + newLine);
		        	}
		        	writer.write("[PARAMETER_END]" + newLine);
	        	}
	    		
	    		/* Removed 1/16/2014
	    		
	        	if(chain.getClassifier() != null) {
		        	//Write classifier
		        	writer.write("[CLASSIFIER]" + newLine);
		        	writer.write("Name=" + chain.getClassifier() + newLine);
		        	writer.write("ClassName=" + chain.getClassifierClassName() + newLine);
		        	writer.write("Path=" + chain.getClassifierExternalPath() + newLine);
		        	//Write classifier parameters
		        	writer.write("[PARAMETER_START]" + newLine);
		        	for (String parameter : chain.getClassParams().keySet()) {
		        		writer.write(parameter + "=" +chain.getClassParams().get(parameter) + newLine);
		        	}
		        	writer.write("[PARAMETER_END]" + newLine); 
	        	}
	        	*/
	        	
	        	
	        	if(rates != null) {
	        		for(int i = 0; i < rates[chainNum].length; i++) {
		        		writer.write("[RATE_START]" + newLine);
		        		writer.write(rates[chainNum][i] + newLine);
		        		writer.write("[RATE_END]" + newLine);
	        		}
	        	}
	        	writer.write("[CHAIN_END]" + newLine);
			}//End of for loop
	
	    	writer.flush();
    	}
    	catch (IOException ex) {
    		System.out.println("Exception occured while writing file: " + file.getName());
        	ex.printStackTrace();
        	throw ex;
    	}
    	finally {
    		writer.close();
    	}
	}
	/*
	 * Reads the algorithm chains from the specified file and returns an arraylist of read chains
	 */

	public ArrayList<Chain> load(Object resourceAsStream) throws FileNotFoundException, Exception{
		ArrayList<Chain> chainList = new ArrayList<Chain>();
		
		Scanner scanner = null;
		if(resourceAsStream instanceof InputStream)
		{
			scanner = new Scanner( (InputStream) resourceAsStream);
		}
		else if( resourceAsStream instanceof File )
		{
			scanner = new Scanner( (File) resourceAsStream);
		}

		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if(line.startsWith("#")) {
				//Ignore comments
				continue;
			}
			if(line.equals("[CHAIN_START]")) {
				//First line after this tag should be name of the chain
				line = scanner.nextLine();				
				Chain chain = null;
				if(line.startsWith("Name=")) {
					chain = new Chain(line.replaceFirst("Name=", ""));	//Create chain object from name
				}
				else
					throw new Exception("Invalid chain file.");
				
				while(scanner.hasNextLine()) {
					line = scanner.nextLine();
					
					//Check for the end of chain
					if(line.equals("[CHAIN_END]"))
						break;
					
					if(line.equals("[FEATURE_EXTRACTOR]")) {
						//First line after this tag should be name of the extractor
						line = scanner.nextLine();
						
						//Read extractor
						Extractor ex = null;
						if(line.startsWith("Name=")) {
							ex = new Extractor(line.replaceFirst("Name=", ""));	//Create extractor object from name
						}
						else
							throw new Exception("Invalid chain file.");
						
						//Read class name
						line = scanner.nextLine();
						if(line.startsWith("ClassName=")) {
							ex.setClassName(line.replaceFirst("ClassName=", ""));
						}
						else
							throw new Exception("Invalid chain file.");
						
						//Read class path
						line = scanner.nextLine();
						if(line.startsWith("Path=")) {
							String path = line.replaceFirst("Path=", "");
							if(!path.equals("null"))
								ex.setExternalPath(path);
						}
						else
							throw new Exception("Invalid chain file.");
						
						//Read extractor parameters
						line = scanner.nextLine();
						if(line.equals("[PARAMETER_START]")) {
							while(scanner.hasNextLine()) {
								line = scanner.nextLine();
								if(line.equals("[PARAMETER_END]"))
									break;
								String params[] = line.split("=");
								if(params.length == 2)
									ex.addParams(params[0], params[1]);
								else
									throw new Exception("Invalid extractor parameter.");
							}
						}//End extractor parameters
						chain.addExtractor(ex);
					}//End extractor
					
					if(line.equals("[FEATURE_SELECTOR]")) {
						//First line after this tag should be name of the selector
						line = scanner.nextLine();
						
						//Read selector
						Selector sel = null;
						if(line.startsWith("Name=")) {
							sel = new Selector(line.replaceFirst("Name=", "")); //Create selector object from name
						}
						else
							throw new Exception("Invalid chain file.");
						
						//Read class name
						line = scanner.nextLine();
						if(line.startsWith("ClassName=")) {
							sel.setClassName(line.replaceFirst("ClassName=", ""));
						}
						else
							throw new Exception("Invalid chain file.");
						
						//Read class path
						line = scanner.nextLine();
						if(line.startsWith("Path=")) {
							String path = line.replaceFirst("Path=", "");
							if(!path.equals("null"))
								sel.setExternalPath(path);
						}
						else
							throw new Exception("Invalid chain file.");
						
						//Read selector parameters
						line = scanner.nextLine();
						if(line.equals("[PARAMETER_START]")) {
							while(scanner.hasNextLine()) {
								line = scanner.nextLine();
								if(line.equals("[PARAMETER_END]"))
									break;
								String params[] = line.split("=");
								if(params.length == 2)
									sel.addParams(params[0], params[1]);
								else
									throw new Exception("Invalid selector parameter.");
							}
						}//End selector parameters
						chain.addSelector(sel);
					}//End Feature Selector
					
					/* added 1/16/2014 */
					if(line.equals("[CLASSIFIER]")) {
						//First line after this tag should be name of the CLASSIFIER
						line = scanner.nextLine();
						
						//Read CLASSIFIER
						ClassifierChain Class = null;
						if(line.startsWith("Name=")) {
							Class = new ClassifierChain(line.replaceFirst("Name=", ""));	//Create CLASSIFIER object from name
						}
						else
							throw new Exception("Invalid chain file.");
						
						//Read class name
						line = scanner.nextLine();
						if(line.startsWith("ClassName=")) {
							Class.setClassName(line.replaceFirst("ClassName=", ""));
						}
						else
							throw new Exception("Invalid chain file.");
						
						//Read class path
						line = scanner.nextLine();
						if(line.startsWith("Path=")) {
							String path = line.replaceFirst("Path=", "");
							if(!path.equals("null"))
								Class.setExternalPath(path);
						}
						else
							throw new Exception("Invalid chain file.");
						
						//Read Classifier parameters
						line = scanner.nextLine();
						if(line.equals("[PARAMETER_START]")) {
							while(scanner.hasNextLine()) {
								line = scanner.nextLine();
								if(line.equals("[PARAMETER_END]"))
									break;
								String params[] = line.split("=");
								if(params.length == 2)
									Class.addParams(params[0], params[1]);
								else
									throw new Exception("Invalid Classifier parameter.");
							}
						}//End extractor parameters
						chain.addClassifier(Class);
					}//End Classifier
					
					/* removed 1/16/2014
					if(line.equals("[CLASSIFIER]")) {
						line = scanner.nextLine();
						//Read classifier name
						if(line.startsWith("Name=")) {
							chain.setClassifier(line.replaceFirst("Name=", ""));
						}
						else
							throw new Exception("Invalid chain file.");
						
						//Read class name
						line = scanner.nextLine();
						if(line.startsWith("ClassName=")) {
							chain.setClassifierClassName(line.replaceFirst("ClassName=", ""));
						}
						else
							throw new Exception("Invalid chain file.");
						
						//Read class path
						line = scanner.nextLine();
						if(line.startsWith("Path=")) {
							String path = line.replaceFirst("Path=", "");
							if(!path.equals("null"))
								chain.setClassifierExternalPath(path);
						}
						else
							throw new Exception("Invalid chain file.");
						
						//Read classifier parameters
						line = scanner.nextLine();
						if(line.equals("[PARAMETER_START]")) {
							while(scanner.hasNextLine()) {
								line = scanner.nextLine();
								if(line.equals("[PARAMETER_END]"))
									break;
								String params[] = line.split("=");
								if(params.length == 2)
									chain.addClassifierParam(params[0], params[1]);
								else
									throw new Exception("Invalid classifier parameter.");
							}	
						}//End classifier parameters
					}//End classifier
				*/
				}
				
				chainList.add(chain);
			}//END CHAIN_START
		}
		scanner.close();
		return chainList;
	}
}