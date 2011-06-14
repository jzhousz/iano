package annotool.io;

public class Algorithm 
{
	//Data members
	private String name;
	private String type;
	private String description = null;
	
	private Parameter param = null;
	
	//Constructor
	public Algorithm(String name, String type) {
		this.name = name;
		this.type = type;
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

	public Parameter getParam() {
		return param;
	}

	public void setParam(Parameter param) {
		this.param = param;
	}
	//Override toString method to use appropriate data for display (say in Combo box)
	public String toString() {
		return this.name;
	}
}
