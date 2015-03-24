package ctMW;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.j_spaces.core.client.*;

import tuplespace.Obligation;
import tuplespace.Points;
import tuplespace.Prohibition;
import tuplespace.Chip;
import tuplespace.TimeEntry;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;

public class AgentHandler extends UnicastRemoteObject implements RemoteEventListener {

	/**
	 * 
	 */
	//private static final long serialVersionUID = 1L;
	EnvCT envCT;
	String agent;
	HashMap<String, Timestamp> timestamps;
	
	public AgentHandler(EnvCT envCT, String agent) throws RemoteException {
		
		this.envCT = envCT;
		this.agent = agent;
		timestamps = new HashMap<String,Timestamp>();
    }

	public void notify(RemoteEvent anEvent) {
		//System.out.println("object notification "+object+" number "+anEvent.getSequenceNumber());
        try {
        	EntryArrivedRemoteEvent arrivedRemoteEvent =(EntryArrivedRemoteEvent) anEvent;
        	TimeEntry e = (TimeEntry) arrivedRemoteEvent.getObject();
        	System.out.println("object notification: "+e);
        	envCT.notifyAgent(agent, e);
        	/*
        	String type = anEvent.getRegistrationObject().get().toString();
        	Timestamp newTime = new Timestamp(System.currentTimeMillis());
        	ArrayList<TimeEntry> r = new ArrayList<TimeEntry>();
            //System.out.println("Got event: " + anEvent.getSource() + ", " +
            //                   anEvent.getID() + ", " +
            //                   anEvent.getSequenceNumber() + ", " + 
            //                   anEvent.getRegistrationObject().get());
            
            if (type.equals("reading")) {
            	System.out.println("object position notification "+object+" number "+anEvent.getSequenceNumber());
            	Reading temp = new Reading(object);
            	r = envGeoSense.readTuple(temp,timestamps.get(type) != null?timestamps.get(type):new Timestamp(0),newTime);
            	
            }
            else if (type.equals("obligation")) {
            	System.out.println("object obligation notification "+object+" number "+anEvent.getSequenceNumber());
            	Obligation temp = new Obligation(object);
            	r = envGeoSense.readTuple(temp,timestamps.get(type) != null?timestamps.get(type):new Timestamp(0),newTime);
            	
            }
            else if (type.equals("prohibition")) {
            	System.out.println("object prohibition notification "+object+" number "+anEvent.getSequenceNumber());
            	Prohibition temp = new Prohibition(object);
            	r = envGeoSense.readTuple(temp,timestamps.get(type) != null?timestamps.get(type):new Timestamp(0),newTime);
            	
            }
            else if (type.equals("points")) {
            	System.out.println("object points notification "+object+" number "+anEvent.getSequenceNumber());
            	Points temp = new Points(object);
            	r = envGeoSense.readTuple(temp,timestamps.get(type) != null?timestamps.get(type):new Timestamp(0),newTime);
            	
            }
            timestamps.put(type, newTime);
            
            */
        } catch (Exception anE) {
           System.out.println("Error while processing object notification");
            anE.printStackTrace(System.out);
        }
    }
}

