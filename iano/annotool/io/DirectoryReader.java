package annotool.io;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is equivalent to LabelReader from target file input mode but for directory structure input mode.
 * This class scans the sub directories of the problem directory to get the candidate images to work on.
 * 
 * It also generates the list of class names which is basically the name of the sub directory inside the 
 * problem directory. Each image in the sub directory belong to the class represented by that sub directory.
 * 
 * It also creates annotation label. In directory structure mode, there can be only one annotation label.
 * The name of the root directory (problem directory) is used as annotation label.
 * 
 *
 */
public class DirectoryReader {
	private int length;	//Number of files
	
	ArrayList<String> annotations = null;
	HashMap<String, String> classNames = new HashMap<String, String>();
	ArrayList<String> fileList = new ArrayList<String>();
	
	public DirectoryReader(String path, String ext) {
		File rootDir = new File(path);
		if(rootDir.exists() && rootDir.isDirectory()) {
			annotations = new ArrayList<String>();
			annotations.add(rootDir.getName());
			
			int classKey = 1;
			for(File rootFile: rootDir.listFiles()) {
				if(rootFile.isDirectory()) {
					classNames.put(String.valueOf(classKey), rootFile.getName());
					classKey++;
					
					for(File file : rootFile.listFiles()) {
						if(file.isFile())
							fileList.add(rootFile.getName() + File.separator + file.getName());
					}
				}
			}
		}
		
		//for(String child : fileList) {
		//	System.out.println(child);
		//}
		//System.out.println("----------------------------------");
	}
	
	public HashMap<String, String> getClassNames() {
		return classNames;
	}
	
	public ArrayList<String> getAnnotations() {
		return annotations;
	}
	
	public ArrayList<String> getFileList() {
		return fileList;
	}
	
	public String[] getFileListArray() {
		return fileList.toArray(new String[fileList.size()]);
	}
}
