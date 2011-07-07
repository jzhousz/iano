package annotool.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import annotool.Annotator;
import annotool.classify.Classifier;
import annotool.classify.SavableClassifier;
import annotool.gui.Chain;
import annotool.gui.Extractor;

/*
 * Saves the list of algorithm chains from auto comaparision mode.
 * It is different than ChainModel which is for saving a single chain from expert mode
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
		        
	    		for(Extractor ex : chain.getExtractors()) {
		        	//Write feature extractor
		        	writer.write("[FEATURE_EXTRACTOR]" + newLine);
		        	writer.write("Name=" + ex.getName() + newLine);
		        	writer.write("[PARAMETER_START]" + newLine);
		        	for (String parameter : ex.getParams().keySet()) {
		        		writer.write(parameter + "=" +ex.getParams().get(parameter) + newLine);
		        	}
		        	writer.write("[PARAMETER_END]" + newLine);
	        	}
	        	
	        	if(chain.getSelector() != null) {
		    		//Write feature selector
		        	writer.write("[FEATURE_SELECTOR]" + newLine);
		        	writer.write("Name=" + chain.getSelector() + newLine);
		        	//Write selector parameters
		        	writer.write("[PARAMETER_START]" + newLine);
		        	for (String parameter : chain.getSelParams().keySet()) {
		        		writer.write(parameter + "=" +chain.getSelParams().get(parameter) + newLine);
		        	}
		        	writer.write("[PARAMETER_END]" + newLine);
	        	}
	        	
	        	if(chain.getClassifier() != null) {
		        	//Write classifier
		        	writer.write("[CLASSIFIER]" + newLine);
		        	writer.write("Name=" + chain.getClassifier() + newLine);
		        	//Write classifier parameters
		        	writer.write("[PARAMETER_START]" + newLine);
		        	for (String parameter : chain.getClassParams().keySet()) {
		        		writer.write(parameter + "=" +chain.getClassParams().get(parameter) + newLine);
		        	}
		        	writer.write("[PARAMETER_END]" + newLine); 
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
	public ArrayList<Chain> load(File file) throws FileNotFoundException, Exception{
		ArrayList<Chain> chainList = new ArrayList<Chain>();
		
		Scanner scanner = null;
		try {
			scanner = new Scanner(file);
		}
		catch (FileNotFoundException e) {
			System.out.println("Target file not found.");
			e.printStackTrace();
			throw e;
		}
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if(line.startsWith("#")) {
				//Ignore comments
				continue;
			}
			if(line.equals("[CHAIN_START]")) {
				Chain chain = new Chain();
				
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
						//Read selector name
						if(line.startsWith("Name=")) {
							chain.setSelector(line.replaceFirst("Name=", ""));
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
									chain.addSelectorParam(params[0], params[1]);
								else
									throw new Exception("Invalid extractor parameter.");
							}
						}//End selector parameters
					}//End Feature Selector
					
					if(line.equals("[CLASSIFIER]")) {
						line = scanner.nextLine();
						//Read classifier name
						if(line.startsWith("Name=")) {
							chain.setClassifier(line.replaceFirst("Name=", ""));
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
				}
				
				chainList.add(chain);
			}//END CHAIN_START
		}
		
		return chainList;
	}
}
