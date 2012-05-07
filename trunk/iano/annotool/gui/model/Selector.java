package annotool.gui.model;

import java.util.HashMap;

/**
 * Class representing properties of a single selector.
 * It contains properties such as selector name, class, external path (if plugin), parameters,
 * and selected indices (if after execution).
 * 
 * Not to be confused with actual algorithm to select features.
 * 
 * @author Santosh
 *
 */
public class Selector {
	private String name = null;
	private String className = null;
	private String externalPath = null;
	private HashMap<String, String> params = null;
	private int[] selectedIndices = null;
	
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

	public int[] getSelectedIndices() {
		return selectedIndices;
	}

	public void setSelectedIndices(int[] selectedIndices) {
		this.selectedIndices = selectedIndices;
	}
}
