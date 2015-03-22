package mwspaces;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.util.Date;

import tuplespace.*;

import com.gigaspaces.events.DataEventSession;
import com.gigaspaces.events.EventSessionConfig;
import com.gigaspaces.events.EventSessionFactory;
import com.j_spaces.core.IJSpace;

import org.apache.log4j.Logger;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.springframework.dao.DataAccessException;

import net.jini.core.event.RemoteEventListener;

public class CTsetup {
	
	private GigaSpace space;
	private DataEventSession session;
	public static String[] agents = {"a1", "a2", "a3", "t1", "c1"};
	
	private Logger log = Logger.getRootLogger();
	 

	public CTsetup() {
		log.info("Starting spaces 1");
		System.out.println("zatim dobry");
	}

	public void initializeGS() {
		log.info("Starting spaces 1");
//	    	try {
//	    		 System.out.println("zatim dobry");
//	        	File file = new File("./log/"+ new Date(System.currentTimeMillis()) +".log");
//
//	            // Create file if it does not exist
//	            boolean success = file.createNewFile();
//	            if (success) {
//	                // File did not exist and was created
//	            } else {
//	                // File already exists
//	            }
//	            
//	            PrintStream printStream;
//	    		try {
//	    			printStream = new PrintStream(new FileOutputStream(file));
//	    			System.setOut(printStream);
//	    		} catch (FileNotFoundException e1) {
//	    			// TODO Auto-generated catch block
//	    			e1.printStackTrace();
//	    		}
//	        } catch (IOException e) {
//	        	
//	        }
	    	//IJSpace ispace = new UrlSpaceConfigurer("jini://*/*/myGrid").space();
	        // use gigaspace wrapper to for simpler API
	        //this.space = new GigaSpaceConfigurer(ispace).gigaSpace();
	        this.space=DataGridConnectionUtility.getSpace("myGrid");
	        //space.clear(null);
	        //dumpGSdata();
	        EventSessionConfig config = new EventSessionConfig();
	        config.setFifo(true);
	        //config.setBatch(100, 20);
	        IJSpace ispace = new UrlSpaceConfigurer("jini://*/*/myGrid").space();
	        EventSessionFactory factory = EventSessionFactory.getFactory(ispace);
	        try {
				session = factory.newDataEventSession(config);
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
	        System.out.println("zatim dobry");
	        try {
				registerCT();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }

	 private void registerCT() throws RemoteException {
			
			CTHandler handler = new CTHandler();
			
			try {
				for (int i=0; i<agents.length;i++) {
					session.addListener(new Position(agents[i]), handler); 
				}
				session.addListener(new tuplespace.Goal(), handler); 
				session.addListener(new Chip(), handler); 
				session.addListener(new Time(), handler); 
				session.addListener(new Tile(), handler); 
				session.addListener(new GroupCoin(), handler); 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	 }
	 public void createEntry (TimeEntry te) {
		 try{

				
				if (te.getTime() == null)
					te.setTime();
				
				//System.out.println("Agent writes: "+e.toString());
				space.write(te);

				return;
			}catch (Exception e){ e.printStackTrace(); 
			
			return; }
	 }
}
