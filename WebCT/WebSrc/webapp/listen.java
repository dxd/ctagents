package webapp;

import ctagents.example1.SimpleAgentAdaptor;
import ctagents.example1.SimpleAgentAdaptorImpl;
import edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsClientImpl;
import edu.harvard.eecs.airg.coloredtrails.agent.events.GameInitializedEventListener;

public class listen implements GameInitializedEventListener {
	public static listen inst;
	public static ColoredTrailsClientImpl client;
	public static SimpleAgentAdaptorImpl agent;
    public  boolean b;
	  public listen() {
		   //client = new ColoredTrailsClientImpl();
		//	client.setPin("20");
			//client.start();
			 agent = new SimpleAgentAdaptorImpl();
			  	agent.setClientName("20");        	
			agent.start();
			System.out.println("started");
	}
	  public static listen GeyIns() {
		if(inst == null)
		{
			inst = new listen();
		}
		return inst;
	}
	public void gameInitialized() {
		
		
	}

}
