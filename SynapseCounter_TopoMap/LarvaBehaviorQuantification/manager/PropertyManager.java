package manager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PropertyManager 
{
	private static String filePath = "plugins/Larva/config.properties";
	
	private String output_speed = "";
	private String output_video = "";
	private String larva_id = "";
	private String larva_perimeter_percentage = "";
	private String rolling_frame = "";
	private String output_complete_csv = "";
	private String max_size = "";
	private String auto_roll = "";
	private String avi_file = "";
	private String larva_perimeter = "";
	private String output_path = "";
	private String max_skeleton = "";
//	private String handle_skeleton = "";
	private String output_roll = "";
	private String from_frame = "";
	private String start_y = "";
	private String output_animated_image = "";
	private String start_x = "";
//	private String handle_size = "";
	private String use_gui = "";
	private String output_curl = "";
	private String chrimson_stimulus = "";
	private String output_chart = "";
	private String output_debug = "";
	private String to_frame = "";
	
	private String min_size = "";
	private String min_skeleton = "";
	
	private String auto_check_size = "";
	private String auto_check_skeleton = "";

	private String fix_invalid_larva = "";
	
	public PropertyManager()
	{
	}
	
	public void getAllProperties()
	{
		output_speed = getProperty("output_speed");
		output_video = getProperty("output_video");
		larva_id = getProperty("larva_id");
		larva_perimeter_percentage = getProperty("larva_perimeter_percentage");
		rolling_frame = getProperty("rolling_frame");
		output_complete_csv = getProperty("output_complete_csv");
		max_size = getProperty("max_size");
		auto_roll = getProperty("auto_roll");
		avi_file = getProperty("avi_file");
		larva_perimeter = getProperty("larva_perimeter");
		output_path = getProperty("output_path");
		max_skeleton = getProperty("max_skeleton");
//		handle_skeleton = getProperty("handle_skeleton");
		output_roll = getProperty("output_roll");
		from_frame = getProperty("from_frame");
		start_y = getProperty("start_y");
		output_animated_image = getProperty("output_animated_image");
		start_x = getProperty("start_x");
//		handle_size = getProperty("handle_size");
		use_gui = getProperty("use_gui");
		output_curl = getProperty("output_curl");
		chrimson_stimulus = getProperty("chrimson_stimulus");
		output_chart = getProperty("output_chart");
		output_debug = getProperty("output_debug");
		to_frame = getProperty("to_frame");
		
		min_size = getProperty("min_size");
		min_skeleton = getProperty("min_skeleton");
//		auto_check_binary = getProperty("auto_check_binary");
		auto_check_size = getProperty("auto_check_size");
		auto_check_skeleton = getProperty("auto_check_skeleton");
		
		fix_invalid_larva = getProperty("fix_invalid_larva");
	}
	
	public boolean saveAllProperties()
	{	
		Properties prop = new Properties();
		OutputStream output = null;
		boolean isFail = false;
		
//		InputStream input = null;
//		Map<String, String> propertyList = new HashMap<String, String>();

		try {
			
			output = new FileOutputStream(filePath);
			
			prop.setProperty("output_speed", output_speed);
			prop.setProperty("output_video", output_video);
			prop.setProperty("larva_id", larva_id);
			prop.setProperty("larva_perimeter_percentage", larva_perimeter_percentage);
			prop.setProperty("rolling_frame", rolling_frame);
			prop.setProperty("output_complete_csv", output_complete_csv);
			prop.setProperty("max_size", max_size);
			prop.setProperty("auto_roll", auto_roll);
			prop.setProperty("avi_file", avi_file);
			prop.setProperty("larva_perimeter", larva_perimeter);
			prop.setProperty("output_path", output_path);
			prop.setProperty("max_skeleton", max_skeleton);
//			prop.setProperty("handle_skeleton", handle_skeleton);
			prop.setProperty("output_roll", output_roll);
			prop.setProperty("from_frame", from_frame);
			prop.setProperty("start_y", start_y);
			prop.setProperty("output_animated_image", output_animated_image);
			prop.setProperty("start_x", start_x);
//			prop.setProperty("handle_size", handle_size);
			prop.setProperty("use_gui", use_gui);
			prop.setProperty("output_curl", output_curl);
			prop.setProperty("chrimson_stimulus", chrimson_stimulus);
			prop.setProperty("output_chart", output_chart);
			prop.setProperty("output_debug", output_debug);
			prop.setProperty("to_frame", to_frame);
			
			prop.setProperty("min_size", min_size);
			prop.setProperty("min_skeleton", min_skeleton);
//			prop.setProperty("auto_check_binary", auto_check_binary);
			
			prop.setProperty("auto_check_size", auto_check_size);
			prop.setProperty("auto_check_skeleton", auto_check_skeleton);
			
			prop.setProperty("fix_invalid_larva", fix_invalid_larva);
			
//			System.out.println("set to_frame:"+to_frame);
			
			prop.store(output, null);

			getAllProperties();
			
			output.close();

		} catch (IOException ex) {
			isFail = true;
			ex.printStackTrace();
		} 
		finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if(isFail)
			return false;
		else
			return true;
	}
	
	public static String getBoolStr(boolean value)
	{	
		if(value)
			return "true";
		else
			return "false";
	}
	
	public static boolean getBool(String value)
	{	
		if(value.equals("true"))
			return true;
		else
			return false;
	}
	
//	public static boolean setBoolStr(String property, boolean value)
//	{	
//		if(property.equals("true"))
//			return true;
//		else
//			return false;
//	}
	
	public static void setBoolean(String property, boolean value)
	{	
		if(value)
			setProperty(property, "true");
		else
			setProperty(property, "false");
	}
	
	public static boolean getBoolean(String property)
	{	
		if(PropertyManager.getProperty(property).equals("true"))
			return true;
		else
			return false;
	}
	
	public static String getProperty(String property)
	{	
		Properties prop = new Properties();
		InputStream input = null;

		String StrProperty = "";  
		
		try {
			input = new FileInputStream(filePath);

			// load a properties file
			prop.load(input);

			StrProperty = prop.getProperty(property);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return StrProperty;
	}
	
	public static void setProperty(String property, String value)
	{	
		Properties prop = new Properties();
		OutputStream output = null;
		InputStream input = null;
		Map<String, String> propertyList = new HashMap<String, String>();

		try {
			input = new FileInputStream(filePath);
			
			// load a properties file
			prop.load(input);

			Set states = prop.keySet(); // get set-view of keys
			Iterator itr = states.iterator();
			
			while (itr.hasNext()) {
				String strKey = (String) itr.next();
				propertyList.put(strKey, prop.getProperty(strKey));
//				System.out.println("prop.getProperty(strKey):" + prop.getProperty(strKey));
			}
			
			input.close();
			
			output = new FileOutputStream(filePath);
			
			for(String key : propertyList.keySet() )
			{
				prop.setProperty(key, propertyList.get(key));
//				System.out.println("key:"+key+", propertyList.get(key):"+propertyList.get(key));
			}
			
			prop.setProperty(property, value);
			
			prop.store(output, null);

			output.close();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} 
		finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	public static String getFilePath() {
		return filePath;
	}

	public static void setFilePath(String filePath) {
		PropertyManager.filePath = filePath;
	}

	public String getOutput_speed() {
		return output_speed;
	}

	public void setOutput_speed(String output_speed) {
		this.output_speed = output_speed;
	}

	public String getOutput_video() {
		return output_video;
	}

	public void setOutput_video(String output_video) {
		this.output_video = output_video;
	}

	public String getLarva_id() {
		return larva_id;
	}

	public void setLarva_id(String larva_id) {
		this.larva_id = larva_id;
	}

	public String getLarva_perimeter_percentage() {
		return larva_perimeter_percentage;
	}

	public void setLarva_perimeter_percentage(String larva_perimeter_percentage) {
		this.larva_perimeter_percentage = larva_perimeter_percentage;
	}

	public String getRolling_frame() {
		return rolling_frame;
	}

	public void setRolling_frame(String rolling_frame) {
		this.rolling_frame = rolling_frame;
	}

	public String getOutput_complete_csv() {
		return output_complete_csv;
	}

	public void setOutput_complete_csv(String output_complete_csv) {
		this.output_complete_csv = output_complete_csv;
	}

	public String getMax_size() {
		return max_size;
	}

	public void setMax_size(String max_size) {
		this.max_size = max_size;
	}

	public String getAuto_roll() {
		return auto_roll;
	}

	public void setAuto_roll(String auto_roll) {
		this.auto_roll = auto_roll;
	}

	public String getAvi_file() {
		return avi_file;
	}

	public void setAvi_file(String avi_file) {
		this.avi_file = avi_file;
	}

	public String getLarva_perimeter() {
		return larva_perimeter;
	}

	public void setLarva_perimeter(String larva_perimeter) {
		this.larva_perimeter = larva_perimeter;
	}

	public String getOutput_path() {
		return output_path;
	}

	public void setOutput_path(String output_path) {
		this.output_path = output_path;
	}

	public String getMax_skeleton() {
		return max_skeleton;
	}

	public void setMax_skeleton(String max_skeleton) {
		this.max_skeleton = max_skeleton;
	}

//	public String getHandle_skeleton() {
//		return handle_skeleton;
//	}
//
//	public void setHandle_skeleton(String handle_skeleton) {
//		this.handle_skeleton = handle_skeleton;
//	}

	public String getOutput_roll() {
		return output_roll;
	}

	public void setOutput_roll(String output_roll) {
		this.output_roll = output_roll;
	}

	public String getFrom_frame() {
		return from_frame;
	}

	public void setFrom_frame(String from_frame) {
		this.from_frame = from_frame;
	}

	public String getStart_y() {
		return start_y;
	}

	public void setStart_y(String start_y) {
		this.start_y = start_y;
	}

	public String getOutput_animated_image() {
		return output_animated_image;
	}

	public void setOutput_animated_image(String output_animated_image) {
		this.output_animated_image = output_animated_image;
	}

	public String getStart_x() {
		return start_x;
	}

	public void setStart_x(String start_x) {
		this.start_x = start_x;
	}

//	public String getHandle_size() {
//		return handle_size;
//	}
//
//	public void setHandle_size(String handle_size) {
//		this.handle_size = handle_size;
//	}

	public String getUse_gui() {
		return use_gui;
	}

	public void setUse_gui(String use_gui) {
		this.use_gui = use_gui;
	}

	public String getOutput_curl() {
		return output_curl;
	}

	public void setOutput_curl(String output_curl) {
		this.output_curl = output_curl;
	}

	public String getChrimson_stimulus() {
		return chrimson_stimulus;
	}

	public void setChrimson_stimulus(String chrimson_stimulus) {
		this.chrimson_stimulus = chrimson_stimulus;
	}

	public String getOutput_chart() {
		return output_chart;
	}

	public void setOutput_chart(String output_chart) {
		this.output_chart = output_chart;
	}

	public String getOutput_debug() {
		return output_debug;
	}

	public void setOutput_debug(String output_debug) {
		this.output_debug = output_debug;
	}

	public String getTo_frame() {
		return to_frame;
	}

	public void setTo_frame(String to_frame) {
		this.to_frame = to_frame;
	}

	public static void main(String[] args) 
	{
		System.out.println("checkSkeleton: |"+PropertyManager.getProperty("checkSkeleton")+"|");
		
		if(PropertyManager.getProperty("auto_check_skeleton").equals("true"))
		{
			System.out.println("checkSkeleton is true");
		}else{
			System.out.println("checkSkeleton is not true");
		}
		
		System.out.println("radian: "+Math.atan(0.777));
		
		System.out.println("degree: "+Math.toDegrees(Math.atan(0.777)));

	}

	public String getMin_size() {
		return min_size;
	}

	public void setMin_size(String min_size) {
		this.min_size = min_size;
	}

	public String getMin_skeleton() {
		return min_skeleton;
	}

	public void setMin_skeleton(String min_skeleton) {
		this.min_skeleton = min_skeleton;
	}

	public String getAuto_check_size() {
		return auto_check_size;
	}

	public void setAuto_check_size(String auto_check_size) {
		this.auto_check_size = auto_check_size;
	}

	public String getAuto_check_skeleton() {
		return auto_check_skeleton;
	}

	public void setAuto_check_skeleton(String auto_check_skeleton) {
		this.auto_check_skeleton = auto_check_skeleton;
	}

	public String getFix_invalid_larva() {
		return fix_invalid_larva;
	}

	public void setFix_invalid_larva(String fix_invalid_larva) {
		this.fix_invalid_larva = fix_invalid_larva;
	}


}
