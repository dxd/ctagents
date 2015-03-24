package ctMW;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import apapl.data.APLFunction;
import apapl.data.APLIdent;
import apapl.data.APLNum;
import mwspaces.GenericAgentImpl;


	public class MessageListener implements Runnable {
	    // the agent the messagelistener is currently listening to
	    private GenericAgentImpl agent;
	    // agentname of the agent the messagelistener is currently listening to
	    private String agentname;
	    // environment
	    private EnvCT env;
	    // messageid of most recent message
	    private ArrayList<Integer> newmessagelist;
	    // type of the most recent message
	    private String type;

	    private boolean allInitialized = false;

	    /**
	     * Constructor for MessageListener
	     * @param env The environment to which the listener is listening
	     */
	    public MessageListener(EnvCT env) {
	       this.env = env;

	    }
		
		public void run() {
	        boolean checkForMessages = true;
	        System.out.println("[MSG] Listening for messages...");

	        // keep checking for new messages
			while (checkForMessages) {
	                // obtain all agents in the game
	                Set<String> keys = EnvCT.agents.keySet();
	                Iterator it = keys.iterator();
	                
	                boolean game_initialized = GenericAgentImpl.game_initialized;
	                // check wheter there are any agents to listen to
	                if (game_initialized) {

	                    // check whether agents have been initialized
	                    if (!allInitialized) {
	                        String initialize = "game_initialized";
	                        APLFunction event = new APLFunction("message",
	                                       new APLIdent(initialize));
	                        // leave 2nd element of throwEvents() empty to make sure the message
	                        // is delivered to all agents in the environment
	                        System.out.println("[MSG] Let all the agents know the game is initialized...");
	                        env.throwEvents(event);
	                        this.allInitialized = true;
	                    }

	                    // cycle through the HashMap of agents
	                    while (it.hasNext()) {
	                        String response;
	                        agentname = it.next().toString();
	                        agent = EnvCT.agents.get(agentname);
	                        newmessagelist = agent.getNewMessageId();

	                        // check whether there are any messages for this agent
	                        if (!newmessagelist.isEmpty()) {
	                            // There is a new Message for this agent
	                              System.out.println("[MSG] Obtained newMessage from GAI: "
	                                                                    + newmessagelist);

	                              for (int i =0; i<newmessagelist.size(); i++){

	                                int message = newmessagelist.get(i);
	                                Hashtable info = agent.getMessageInfo(message);
	                                type = (String) info.get("type");
	                                System.out.println("[MSG] Message is of type: " + type);

	                                APLFunction event;

	                                if (type.equals("response")) {
	                                    Boolean accepted = (Boolean) info.get("accepted");
	                                    if (accepted) {response = "accept";}
	                                    else {response = "reject";}

	                                    event = new APLFunction("message",
	                                            new APLIdent(type), new APLNum(message),
	                                            new APLIdent(response));
	                                }

	                                else {
	                                    event = new APLFunction("message",
	                                               new APLIdent(type), new APLNum(message));
	                                }
	                                // return message event to the 2APL agent
	                                env.throwEvents(event, agentname);
	                            }

	                        // reset nr of read messages
	                        System.out.println("[MSG] Reset nr of new messages in GAI");
	                        agent.resetNewMessages();

	                        }

	                        if (agent.phaseChanged() ) {

	                            APLFunction event = new APLFunction("message",
	                                                new APLIdent("phasechange"));
	                            env.throwEvents(event, agentname);
	                        }

	                    }


	                }


	        }
	    }
}
