package annotool.gui.model;

import java.io.Serializable;
import java.util.HashMap;

public class ClassifierInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5461209621064944784L;
	private String name = null;
	private String className = null;
	private String externalPath = null;
	private HashMap<String, String> params = null;
	
	public ClassifierInfo(String name) {
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

