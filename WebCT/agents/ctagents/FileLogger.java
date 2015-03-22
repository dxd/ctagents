package ctagents;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that allows to write to a specific Log file from
 * anywhere in the code. It holds a map of all the FileLoggers that where used
 * in the current run. Usage: FileLogger.getInstance(String
 * sender).writeln(String yourMsg);
 * 
 * @author Yael Ejgenberg
 * 
 */
public class FileLogger {

	private static Map<String, FileLogger> INSTANCE = new HashMap<String, FileLogger>();
	
	private FileWriter out;
	private final static String LOG_SUFFIX = "CT_DbgLog_";
	String suffix = ".txt";
	
	private static void createInstance(String sender, String folder) {
		if (!INSTANCE.containsKey(sender)) {
			
			String suffix = "";
			
			// Creating a difference between log files of a configuration class
			// and other log files.
			if (!sender.contains("Config")) {
				suffix = LOG_SUFFIX ;
			}
			
			if (!folder.equals("")) {
				
				// Checking if the folder exists, if not creating it
				File dir = new File(folder);
				dir.mkdir();

				INSTANCE.put(sender, new FileLogger(folder + "/" + suffix + sender));				
			} else {
				INSTANCE.put(sender, new FileLogger(suffix + sender));
			}
		}
	}

	public static FileLogger getInstance(String sender, String folder) {
		if (!INSTANCE.containsKey(sender))
			createInstance(sender, folder);
		return INSTANCE.get(sender);
	}
	
	public static FileLogger getInstance(String sender) {
		if (!INSTANCE.containsKey(sender))
			createInstance(sender, "");
		return INSTANCE.get(sender);
	}

	private FileLogger(String sender) {
		try {
			out = new FileWriter(sender + suffix);
		} catch (IOException e) {
			System.out.println("Exception trying to open log file: " + e);
		}
		writeln("This is the CT debug logger; svn version 638; sep 27 09");
		writeln(Calendar.getInstance().getTime().toString());
	}

	public void writeln(String msg) {
		try {
			out.write(Calendar.getInstance().getTime().toString() + "\t");
			out.write(msg);
			out.write("\r\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("Exception trying to write " + msg + " to log file: " + e);
		}
	}

	public void close() throws IOException {
		out.close();
	}
}
