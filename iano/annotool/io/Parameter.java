package annotool.io;

/**
 * Represents a single parameter for an algorithm.
 * 
 * @author Santosh
 *
 */
public class Parameter 
{	
	private String paramName;
	private String paramType;
	private String paramMin = null;
	private String paramMax = null;
	private String paramDefault = null;
	private String[] paramDomain = null;
	
	//Constructor
	public Parameter(String paramName, String paramType) {
		this.paramName = paramName;
		this.paramType = paramType;
	}

	//Access methods
	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getParamType() {
		return paramType;
	}

	public void setParamType(String paramType) {
		this.paramType = paramType;
	}

	public String getParamMin() {
		return paramMin;
	}

	public void setParamMin(String paramMin) {
		this.paramMin = paramMin;
	}

	public String getParamMax() {
		return paramMax;
	}

	public void setParamMax(String paramMax) {
		this.paramMax = paramMax;
	}

	public String getParamDefault() {
		return paramDefault;
	}

	public void setParamDefault(String paramDefault) {
		this.paramDefault = paramDefault;
	}

	public String[] getParamDomain() {
		return paramDomain;
	}

	public void setParamDomain(String[] paramDomain) {
		this.paramDomain = paramDomain;
	}
	
	
}
