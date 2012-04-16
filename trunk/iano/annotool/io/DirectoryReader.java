package annotool.io;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

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
		
		for(String child : fileList) {
			System.out.println(child);
		}
		System.out.println("----------------------------------");
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
