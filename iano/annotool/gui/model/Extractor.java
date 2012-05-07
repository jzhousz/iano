package annotool.gui.model;

import java.util.HashMap;

/**
 * Class representing properties like name, classname, external path(for plugin) and parameters
 * of a single extractor (not to be confused with actual extractor algorithm)
 * 
 * @author Santosh
 *
 */
public class Extractor {
	private String name = null;
	private String className = null;
	private String externalPath = null;
	private HashMap<String, String> params = null;
	
	public Extractor(String name) {
		this.name = name;
		params = new HashMap<String, String>();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getExternalPath() {
		return externalPath;
	}

	public void setExternalPath(String externalPath) {
		this.externalPath = externalPath;
	}

	public HashMap<String, String> getParams() {
		return params;
	}
	public void setParams(HashMap<String, String> params) {
		this.params = params;
	}
	public void addParams(String key, String value) {
		this.params.put(key, value);
	}
}
