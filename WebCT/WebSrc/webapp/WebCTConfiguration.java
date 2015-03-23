package webapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


public class WebCTConfiguration {

	static String LogFileFullPath = "";
	static boolean IsChipRevelationGame = false;
	static boolean ShowPathFinder = true;
		
	static {
		Properties prop = new Properties();
		try {
			  
			prop.load(new FileInputStream("./webct.ini"));
			
			LogFileFullPath = prop.getProperty("LogFileFullPath");
			IsChipRevelationGame = Boolean.parseBoolean(prop.getProperty("IsChipRevelationGame"));
			ShowPathFinder = Boolean.parseBoolean(prop.getProperty("ShowPathFinder"));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogFileFullPath = "WebCTLog.log";
		}
	}
	
	public static String getLogFileFullPath() {
		return LogFileFullPath;
	}
	
	public static boolean getIsChipRevelationGame() {
		return IsChipRevelationGame;
	}
	
	public static boolean getShowPathFinder() {
		return ShowPathFinder;
	}
}
