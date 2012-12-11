package annotool.io;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class loads the algorithms from the xml file, parses it and creates a list of extractor, selector or classifiers
 * that are used in GUI.
 * 
 *
 */
public class AlgoXMLParser 
{
	Document dom;
	
	ArrayList<Algorithm> extractors2D, extractors3D, selectors, classifiers;
	
	public AlgoXMLParser()
	{
		extractors2D = new ArrayList<Algorithm>();
		extractors2D.add(new Algorithm("None", "2DExtractor", "None"));
		extractors3D = new ArrayList<Algorithm>();
		extractors3D.add(new Algorithm("None", "3DExtractor", "None"));
		selectors = new ArrayList<Algorithm>();
		selectors.add(new Algorithm("None", "Selector", "None"));
		classifiers = new ArrayList<Algorithm>();
		//classifiers.add(new Algorithm("Compare All", "Classifier"));
	}
	public void runParser()
	{
		parseXmlFile("Algorithms.xml");
		
		//If dom is null (no algorithm file), then add default classifier
		if (dom == null){
			Algorithm al = new Algorithm("SVM", "Classifier");
			al.setDescription("Support Vector Machine");
			Parameter param = new Parameter("General Parameter", "String");
			param.setParamDefault("-t 0");
			al.addParam(param);
			classifiers.add(al);
			return;
		}
		else
			parseDocument();
		
		//Now, go through the plugins
		PluginScanner pScanner = new PluginScanner();
		pScanner.scan();
		for(String xmlPath : pScanner.getXmlFiles()) {
			parseXmlFile(xmlPath);
			
			if(dom != null)
				parseDocument();
		}
	}
	private void parseXmlFile(String uri) {
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try  {
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			dom = db.parse(uri);
		}
		catch(ParserConfigurationException pce) {
			pce.printStackTrace();
			dom = null;
		}
		catch(SAXException se) {
			se.printStackTrace();
			dom = null;
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
			dom = null;
		}
	}
	private void parseDocument()
	{
		//get the root elememt
		Element docEle = dom.getDocumentElement();
		
		//get a nodelist of <Algorithm> elements
		NodeList nl = docEle.getElementsByTagName("Algorithm");
		if(nl != null && nl.getLength() > 0) 
		{
			for(int i = 0 ; i < nl.getLength();i++) 
			{				
				//get the employee element
				Element el = (Element)nl.item(i);
				
				//Create Algorithm object and add to list
				Algorithm al = getAlgorithm(el);
				
				if(al.getType().equals("2DExtractor"))
					extractors2D.add(al);
				else if(al.getType().equals("3DExtractor"))
					extractors3D.add(al);
				else if(al.getType().equals("Selector"))
					selectors.add(al);
				else if(al.getType().equals("Classifier"))
					classifiers.add(al);
			}
		}
	}
	
	private Algorithm getAlgorithm(Element el)
	{
		String name = getTextValue(el, "Name");
		String type = el.getAttribute("type");
		
		String desc = getTextValue(el, "Desc");
		String className = getTextValue(el, "ClassName");
		String path = getTextValue(el, "Path");
		
		Algorithm al = new Algorithm(name, type);
		al.setDescription(desc);
		al.setClassName(className);
		if(path != null)
			al.setExternalPath(PluginScanner.PLUGIN_PATH + "/" + path);
		
		//Get the parameter for this algorithm
		addParameter(el, al);
		
		return al;
	}
	private void addParameter(Element el, Algorithm al)
	{
		Parameter param = null;
		String name = null,
		       type = null,
		       min = null,
		       max = null,
		       def = null,
		       domain = null;
	
		NodeList nl = el.getElementsByTagName("Parameter");
		if(nl != null && nl.getLength() > 0) 
		{
			for(int i = 0 ; i < nl.getLength();i++) 
			{
				Element paramEl = (Element)nl.item(i);
				name = getTextValue(paramEl, "Name");
				type = paramEl.getAttribute("type");
				min = getTextValue(paramEl, "Min");
				max = getTextValue(paramEl, "Max");
				def = getTextValue(paramEl, "Default");
				domain = getTextValue(paramEl, "Domain");
				
				//Create the parameter object
				param = new Parameter(name, type);
				param.setParamMax(max);
				param.setParamMin(min);
				param.setParamDefault(def);
				if(domain != null)
					param.setParamDomain(domain.split(","));
				
				//Add parameter to list in algorithm
				al.addParam(param);
			}
		}
	}
	public ArrayList<Algorithm> get2DExtractors()
	{
		return extractors2D;
	}
	public ArrayList<Algorithm> get3DExtractors()
	{
		return extractors3D;
	}
	public ArrayList<Algorithm> getSelectors()
	{
		return selectors;
	}
	public ArrayList<Algorithm> getClassifiers()
	{
		return classifiers;
	}
	
	//Returns array of string
	public Algorithm[] get2DExtractorsAr()
	{
		Algorithm[] extractors = new Algorithm[extractors2D.size()];
		extractors2D.toArray(extractors);
		return extractors;
	}
	public Algorithm[] get3DExtractorsAr()
	{
		Algorithm[] extractors = new Algorithm[extractors3D.size()];
		extractors3D.toArray(extractors);
		return extractors;
	}
	public Algorithm[] getSelectorsAr()
	{
		Algorithm[] selectorsAr = new Algorithm[selectors.size()];
		selectors.toArray(selectorsAr);
		return selectorsAr;		
	}
	public Algorithm[] getClassifiersAr()
	{
		Algorithm[] classifiersAr = new Algorithm[classifiers.size()];
		classifiers.toArray(classifiersAr);
		return classifiersAr;
	}
	
	/**
	 * Take a xml element and the tag name, look for the tag and get
	 * the text content 
	 * i.e for <employee><name>John</name></employee> xml snippet if
	 * the Element points to employee node and tagName is name I will return John  
	 * @param ele
	 * @param tagName
	 * @return
	 */
	private String getTextValue(Element ele, String tagName) 
	{
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) 
		{
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}

	
	/**
	 * Calls getTextValue and returns an int value
	 * @param ele
	 * @param tagName
	 * @return
	 */
	private int getIntValue(Element ele, String tagName) 
	{
		String value = getTextValue(ele,tagName);
		int numValue = 0;
		
		if(value != null)
		{
			try
			{			
				numValue = Integer.parseInt(value);
			}
			catch(NumberFormatException e)
			{
				e.printStackTrace();
			}
		}
		return numValue;
	}
}
