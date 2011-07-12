package annotool.gui.model;

import java.util.HashMap;

public class Selector {
	private String name = null;
	private HashMap<String, String> params = null;
	
	public Selector(String name) {
		this.name = name;
		params = new HashMap<String, String>();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
