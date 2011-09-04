package annotool.io;

import java.util.ArrayList;

public class Algorithm 
{
	//Data members
	private String name;
	private String type;
	private String description = null;
	private String className = null;
	private String externalPath = null;
	
	private ArrayList<Parameter> paramList = null;
	
	//Constructor
	public Algorithm(String name, String type) {
		this.name = name;
		this.type = type;
		
		paramList = new ArrayList<Parameter>();
	}
	//Constructor
	public Algorithm(String name, String type, String className) {
		this.name = name;
		this.type = type;
		this.className = className;
		
		paramList = new ArrayList<Parameter>();
	}
	
	//Access Methods
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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

	public ArrayList<Parameter> getParam() {
		return paramList;
	}
	public void addParam(Parameter param)
	{
		paramList.add(param);
	}
	
	//Override toString method to use appropriate data for display (say in Combo box)
	public String toString() {
		return this.name;
	}
}
