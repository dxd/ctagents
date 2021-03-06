package ctMW;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.*; 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.core.transaction.TransactionException;
import oopl.DistributedOOPL;
import oopl.GUI.GUI;
import tuplespace.*;
import tuplespace.Prohibition;
import tuplespace.Sanction;
import RecipExperiment.RecipConstants;
import apapl.Environment;
import apapl.ExternalActionFailedException;
import apapl.data.*;
import aplprolog.prolog.IntHarvester;
import aplprolog.prolog.Prolog;
import aplprolog.prolog.builtins.ExternalActions;
import aplprolog.prolog.builtins.ExternalTool;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.space.UrlSpaceConfigurer;

import com.j_spaces.core.IJSpace;
import com.gigaspaces.events.DataEventSession;
import com.gigaspaces.events.EventSessionConfig;
import com.gigaspaces.events.EventSessionFactory;

import ctagents.recipagents.PhaseWaiter;
import ctagents.recipagents.RecipAgentAdaptor;
import edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsClientImpl;
import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;
import edu.harvard.eecs.airg.coloredtrails.alglib.ShortestPaths;
import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.shared.ScoringUtility;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Goal;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Path;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.RowCol;


/*
 * Extends Environment to be compatible with 2APL and implements ExternalTool to 
 * be compatible with my Prolog engine. 
 */
public class EnvCT  extends Environment implements ExternalTool, RecipAgentAdaptor{
	//public static JavaSpace space; // shared data
	public int clock = 0;
	public DistributedOOPL oopl; // norm interpreter
	public static String TYPE_STATUS="status", TYPE_PROHIBITION="prohibition", 
		TYPE_OBLIGATION="obligation", TYPE_READINGREQ = "readingRequest",TYPE_READING = "reading",TYPE_INVESTIGATE = "investigate",TYPE_CARGO = "cargo",TYPE_COIN = "coin",TYPE_POINTS = "points",
			TYPE_OBJECT="object", TYPE_INVENTORY="inventory", TYPE_SANCTION="sanction", TYPE_GROUPCOIN="groupCoin", NULL="null"; // for matching string with class type
	public int[] ar_true, ar_null, ar_state_change, ar_false; // precalculated IntProlog data 
	public int INT_TUPLE=0, INT_POINT=0, INT_NULL=0;
	public APAPLTermConverter converter; // Converts between IntProlog and 2APL
	private Prolog2Java p2j;
	private GigaSpace space;
	private DataEventSession session;
	public static String[] agents1 = {"agent10"};
	private Utilities utilities;
	
	private ColoredTrailsClientImpl client;
	/** indicates whether game state has been initialized */
//	private boolean game_initialized = false;
//	private ChipSet sending;
	ClientGameStatus cgs = null;
    private double bestScore = -1;
	//private Hashtable messages;
	//protected static Map<String, FairMaxAgent> agents = new HashMap();
	private int MyPerGameId;
	private int OppPerGameId;
	
	/*
	 * Just for testing.
	 */
    public static void main(String[] args){ 
		EnvCT st = new EnvCT();
    }
    
    /*
     * A kickoff function to begin the system.
     */
    public void initializeGS() throws RemoteException {
    	
    	//IJSpace ispace = new UrlSpaceConfigurer("jini://*/*/myGrid").space();
        // use gigaspace wrapper to for simpler API
        //this.space = new GigaSpaceConfigurer(ispace).gigaSpace();
        this.space=DataGridConnectionUtility.getSpace("myGrid");
        //space.clear(null);
        dumpGSdata();
        EventSessionConfig config = new EventSessionConfig();
        config.setFifo(true);
        //config.setBatch(100, 20);
        IJSpace ispace = new UrlSpaceConfigurer("jini://*/*/myGrid").space();
        EventSessionFactory factory = EventSessionFactory.getFactory(ispace);
        session = factory.newDataEventSession(config); 
    }
    
	
		
		public void initializeOOPL() throws RemoteException {
			//registerOrg();
			p2j = new Prolog2Java();
			// Starting the normative system:
			oopl = new DistributedOOPL(); // Create interpreter object
			//GUI g = new GUI(oopl,"SpaceOrg.2opl","OOPL",null,6677); // Make a GUI for the interpreter
			GUI g = new GUI(oopl,"GroupOrg.2opl","OOPL",null,6677); // Make a GUI for the interpreter
			converter = new APAPLTermConverter(oopl.prolog); // Make a term converter (relies on Prolog engine for string storage)
			utilities = new Utilities(oopl,converter,this);
			//INT_POINT =makeStringKnown("cell");
			//INT_POINT =makeStringKnown("position");
			INT_NULL =makeStringKnown("null"); 
			makeStringKnown("notifyAgent"); 
			makeStringKnown("clock"); 
			makeStringKnown("obligation"); 
			makeStringKnown("prohibition"); 
			makeStringKnown("position");
			makeStringKnown("reading");
			makeStringKnown("investigate");
			makeStringKnown("cargo");
			makeStringKnown("coin");
			makeStringKnown("points");
			makeStringKnown("read"); 
			makeStringKnown("write"); 
			registerActions(oopl.prolog); // Register the possible actions on this ExternalTool (such as @external(space,theAction(arg1,arg2),Result).)
			// Precompute some data: ('true.', 'null.', 'tuple_space_changed.')
			ar_true = oopl.prolog.mp.parseFact("true.", oopl.prolog.strStorage, false); 
			ar_false = oopl.prolog.mp.parseFact("false.", oopl.prolog.strStorage, false); 
			ar_null = oopl.prolog.mp.parseFact("null.", oopl.prolog.strStorage, false);
			ar_state_change = oopl.prolog.mp.parseFact("tuple_space_changed.", oopl.prolog.strStorage, false);
			// To create a IntProlog structure out of a string use the above lines (but replace the fact string such as "true.")
			// Starting the clock 
			Thread t = new Thread(new ClockTicker(this));
			t.start(); 
			//this.insertTestData();
			
		
	} 

	/*
	 * Both used for increasing or just reading the clock. 
	 */
	public synchronized int updateClock(int amount){
		//if(amount>0)  oopl.handleEvent(ar_state_change, false); // clock ticked so deadlines can be passed, handleEvent causes the interpreter to check the norms
		Time t = new Time();
		//TimeEntry e = new TimeEntry();
		clock++;
		//System.out.println(clock);
		t.setClock(clock);
		//space.write(t);

		return clock;
	}
	
	/*
	 * Constructor immediately initializes the space. 
	 */
	public EnvCT(){
		super();
		try { //initializeGS(); 
		initializeOOPL();} catch (Exception e) { e.printStackTrace(); }
		try {
        	File file = new File("./log/"+ new Date(System.currentTimeMillis()) +".log");

            // Create file if it does not exist
            boolean success = file.createNewFile();
            if (success) {
                // File did not exist and was created
            } else {
                // File already exists
            }
            
            PrintStream printStream;
    		try {
    			printStream = new PrintStream(new FileOutputStream(file));
    			System.setOut(printStream);
    		} catch (FileNotFoundException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}
        } catch (IOException e) {
        	
        }
	}
	


	/////////////////////////CT
	public void throwEvents(APLFunction event, String ... receivers) {
        throwEvent(event, receivers);
    }

    /**
	 * Called when a game ends
	 */
	public void gameEnded() {
		System.out.println("Game ended ");
		System.out.println("My PlayerStatus is: " + client.getGameStatus().getMyPlayer());
		 APLFunction event = new APLFunction("message",
	                new APLIdent("game_ended"));
	        throwEvents(event);
	}

	
	/**
	 * Called by the server when the game configuration class' run() method completes
	 */
        //NEVER CALLED
	public void gameInitialized()
	{
		System.out.println("#########################Game Initialized");
		//game_initialized = true;
		
		
		String phaseName = cgs.getPhases().getCurrentPhaseName();
		System.out.println("AGENT " + client.getName() + ": current phase name: " + phaseName);
		System.out.println("we have " + client.getGameStatus().getBoard().getGoals().size() + " goals");
		System.out.println("we have " + client.getGameStatus().getScoring() + " scoring");
	}

	/**
	 * Gets the client name
	 */
	public String getClientName() {
		return client.getPin();
	}




	 public Term sendProposal(String agentname) {
		 ArrayList<ChipSet> exchange = (ArrayList<ChipSet>) strategy(OppPerGameId);
        ChipSet senderChips;
        ChipSet recipientChips;

        if(exchange == null) {
            System.out.println("No beneficial " +
                                "exchanges found among the ones that are beneficial for the responder");
            return new APLNum(-1);
        }
        else {
            System.out.println("EXCHANGE: " + exchange);
            senderChips = exchange.get(1);
            recipientChips = exchange.get(0);
            BasicProposalDiscourseMessage proposal= new BasicProposalDiscourseMessage(
                            cgs.getPerGameId(), OppPerGameId, -1, senderChips, recipientChips);
//            sending = senderChips;
            
            PhaseWaiter waiter = new PhaseWaiter(cgs.getPhases());
            waiter.doWait(RecipConstants.minProposeTime, RecipConstants.maxProposeTime);
            
            
           client.communication.sendDiscourseRequest(proposal);
          
           int messageId = proposal.getMessageId();
           System.out.println("message id: " + messageId);
           APLNum msgId = new APLNum(messageId);
           return msgId; 
        }
	 }
    /**
    * Called when a discourse message is received
    * @param dm discourse message received
    */
    public void onReceipt(DiscourseMessage dm) {
        System.out.println("Received a " + dm.getClass() );
        // check if it is a basic proposal discourse message
        String type = dm.getMsgType();
        System.out.println("[MSG] Message is of type: " + type);
        
        if (dm.getToPerGameId() == cgs.getPerGameId() && (dm instanceof BasicProposalDiscourseMessage)) {
        	BasicProposalDiscourseMessage proposal = (BasicProposalDiscourseMessage) dm;
        if (type.equals("response")) {
        	BasicProposalDiscussionDiscourseMessage response = (BasicProposalDiscussionDiscourseMessage) dm;
        	System.out.println("AGENT " + ": got response to offer");
        	APLFunction event = null;
			if(!response.accepted() ) {
				// The proposal is rejected and we still have more paths to propose, make an offer
				event = new APLFunction("message",
	                    new APLIdent(type), new APLNum(dm.getMessageId()),
	                    new APLIdent("reject"));
			}
			else if(response.accepted()) {
				// The proposal is accepted
				event = new APLFunction("message",
	                    new APLIdent(type), new APLNum(dm.getMessageId()),
	                    new APLIdent("accept"));
			}
			throwEvents(event);
        }
        else if (type.equals("basicproposal")) {
        	 BasicProposalDiscussionDiscourseMessage responseMessage = new BasicProposalDiscussionDiscourseMessage(proposal );
             // check if the proposal is beneficial
             
             boolean offerResponse = RespondStrategy(ChipSet.subChipSets(proposal.getChipsSentByResponder(), proposal.getChipsSentByProposer() ),dm.getFromPerGameId());
             System.out.println("Received a proposal ");
             PhaseWaiter waiter = new PhaseWaiter(cgs.getPhases());
             waiter.doWait(RecipConstants.minRespondTime, RecipConstants.maxRespondTime);
                 
             // check if the proposal is beneficial
             if( offerResponse ) {
                 //response.setSubjectMsgId(subjectMsgId);
                 responseMessage.acceptOffer();
             } else {
                 //response.setSubjectMsgId(subjectMsgId);
                 responseMessage.rejectOffer();
             }
     
             client.communication.sendDiscourseRequest(responseMessage);
        }
        
        }
           
       // System.out.println("Received a message not for me");
               
          
        }
       
        
    
    


    /**
     * Called when a phase advances
     */
    public void phaseAdvanced(Phases ph) {
        Scoring scoring = cgs.getScoring();
        String phaseName = cgs.getPhases().getCurrentPhaseName();
        if(bestScore == -1) {
            BestUse bu = new BestUse(cgs, cgs.getMyPlayer(), scoring, 0);     // calculate the best use of player's chips
            bestScore = bu.getBestState().getScore();
        }

        APLFunction event = new APLFunction("message",
                new APLIdent("phasechange"));
        throwEvents(event);
        if(phaseName.equals("Offer Phase")) {
           
        }
    }
	
	/**
	 * Sets the client name
	 * @param name client name
	 */
	public void setClientName(String name) {
		client.setPin(name);
	}

	/**
	 * Starts the client
	 */
	public void start() {
		client.start();
	}


	
	/**
	 * Strategy of the proposer
	 * @param o null
	 * @param id 
	 * @return An exchange to propose
	 */
        public ArrayList<ChipSet> strategy( int id) {
            // Get all possible unique exchanges between the players
            Set<ArrayList<ChipSet>> allExchanges = ChipSet.getAllExchanges(
                            cgs.getMyPlayer().getChips(), cgs.getPlayerByPerGameId(id).getChips());

            //System.out.println("Total number of unique exchanges: " + allExchanges.size());
            ArrayList<ChipSet> mostBeneficialExchange = null;

            //basic sanity checking
            System.out.println("my player info: " + cgs.getMyPlayer().toString());
            System.out.println("my opponent info: " + cgs.getPlayerByPerGameId(id).toString());
           // System.out.println("scoring: " + cgs.getScoring());

            ScoringUtility SU = new ScoringUtility(cgs, cgs.getPerGameId(), id);
           // System.out.println("here " );
            ChipSet offer = SU.getFairMaxOffer();
            //System.out.println("offer: " + offer.toString());
            ChipSet propChips = ChipSet.getNegation(offer);
            ChipSet respChips = new ChipSet(offer);
            for(String color : propChips.getColors()){
                if(propChips.getNumChips(color) < 0)
                    propChips.set(color, 0);
            }

            for(String color : respChips.getColors()){
                if(respChips.getNumChips(color) < 0)
                    respChips.set(color, 0);
            }

            mostBeneficialExchange = new ArrayList<ChipSet>();
            mostBeneficialExchange.add(propChips);
            mostBeneficialExchange.add(respChips);

            return mostBeneficialExchange;
	}
	 
    public boolean RespondStrategy(ChipSet proposal,int id) {
//        // our input is a proposal
//        System.out.println("Received proposal: " + proposal);
//
//        BestUse bu = new BestUse(cgs, cgs.getMyPlayer(), scoring, 0);
//        double MyDefaultScore = bu.getBestState().getScore();
//
//        bu = new BestUse(cgs, cgs.getPlayerByPerGameId(OppPerGameId), scoring, 0);
//        double OppDefaultScore = bu.getBestState().getScore();
//
//        if(payoff(proposal) > MyDefaultScore*OppDefaultScore)
//            return(true);
//        else
//            return(false);
        
        ScoringUtility SU = new ScoringUtility(cgs, id, cgs.getPerGameId());
//        double oppBenefit = SU.getOfferScore(proposal, OppPerGameId) - SU.getDefaultScore(OppPerGameId);
        double myBenefit = SU.getOfferScore(proposal, cgs.getPerGameId()) - SU.getDefaultScore(cgs.getPerGameId());
        if(  (myBenefit >= 0))
            return(true);
        else
            return(false);
        
    }
	public void gameStarted() {
		System.out.println("#########################Game started");
		cgs = client.getGameStatus();
                MyPerGameId = cgs.getMyPlayer().getPerGameId();
                for(PlayerStatus ps : cgs.getPlayers()){
                    if(ps.getPerGameId() == MyPerGameId)
                        continue;
                    else
                        OppPerGameId = ps.getPerGameId();
                }
                String initialize = "game_initialized";
				APLFunction event = new APLFunction("message",
                        new APLIdent(initialize));
         throwEvents(event);       
	}

	/**
     * Add a new agent to the game by creating a new CT FairMaxAgent object.
	 * @Override inherited method from Environment class.
     * Is called immediately after initialization.
     * @param String agentname containing the name of the agent calling this method.
     */
     public void addAgent(String agentname) {
    	//register(agentname);
    	 
    	//FairMaxAgent agent = new FairMaxAgent();
		//agents.put(agentname, agent);
		//System.out.println("[ENV] Added agent to list of agents: " + agents);

        // agents.get(agentname).setClientName(agentname);
        // TODO:
        // hack to overcome naming convention problem
    	client = new ColoredTrailsClientImpl();
        setClientName("10");

        start();
        
		cgs = client.getGameStatus();
		//System.out.println("[ENV] scoring "+ cgs.getScoring());   
        
        client.addDiscourseMessageEventListener(this);
		client.addGameEndedEventListener(this);
		client.addGameInitializedEventListener(this);		
		client.addGameStartEventListener(this);		
		client.addPhasesAdvancedEventListener(this);
		//client.addPhasesUpdatedEventListener(this);
		//client.addPlayersUpdatedEventListener(this);
		
		System.out.println("[ENV] Added a new agent to the game by the name of "
                + agentname + ".");   
	}


     //Broken
     public Term requestChips(String agentname, APLNum opppin, APLNum oppid) throws ExternalActionFailedException {
         System.out.println("[ENV] ==========> IN REQUEST CHIPS");
         int opponentid = oppid.toInt();
        // String goalid = id.toString();
         int opponentpin = opppin.toInt();
         ChipSet myChips = cgs.getMyPlayer().getChips();
         //ChipSet chips = myChips.getMissingChips(cs)

         //Term apllist = convertChipSet(chips);

         //System.out.println("[ENV] REQUEST CHIPS: RETURNING LIST");
         return null; 
     }


     /**
      * Convert a ChipSet to an APLlist with [colorname, colornr] pairs
      * @param c the ChipSet to be converted
      * @return
      * TODO: rewrite such that it constructs lists with linkedlist? 
      */
     public Term convertChipSet(ChipSet chips) throws ExternalActionFailedException {
                  // create a list for the [color, #chips] pairs
         APLList pair;
         Term[] pr = new Term[2];

         // create a list for all the pairs: [[color, nr],[color,nr],..]
         APLList apllist;
         Term[] t = new Term[chips.getColors().size()];

         // check whether chipset is empty
         if (chips.isEmpty()) {
             throw new ExternalActionFailedException("[ENV] Empty list");
             //return new APLIdent("empty");
         }
         else {
             // create a counter to add items to a list
             int i = 0;

             //cycle through chipset
             for (String clr : chips.getColors()) {
                 String color = clr.toLowerCase();
                 APLIdent colorname = new APLIdent(color);
                 pr[0] = colorname;
                 APLNum nr = new APLNum(chips.getNumChips(clr));
                 pr[1] = nr;
                 //System.out.println("[ENV] List pr with the pair is now: " + pr);
                 pair = new APLList(pr);
                 t[i] = pair;
                 //System.out.println("[ENV] List t with pairs is now: " + t);
                 i++;
             }
             // create list of all color,nr pairs
             apllist = new APLList(t);
             return apllist;
         }
     }


      /**
      * Find the player closest to the given coordinates. These can also
      * be the coordinates of the current agent.
      * @param x APLNum X-coordinate
      * @param y APLNum Y-coordinate
      * @return An APLList of APLLists of [playerpin, posx, posy]
      */
     
//     public Term findPlayerClosestTo(String agentname, APLNum x, APLNum y)
//                        throws ExternalActionFailedException {
//        int xcoor = x.toInt();
//        int ycoor = y.toInt();
//        HashMap players = agents.get(agentname).findPlayerClosestTo(xcoor, ycoor);
//        Set<Integer> playerPins = players.keySet();
//        APLList closestPlayers;
//        // make a list of players that corresponds with the size of the CT HashMap
//        Term[] t = new Term[playerPins.size()];
//        int i = 0;
//
//        // convert each <pin, RowCol> pair to an APLList and put this in the
//        // list of players
//        for (int pin : playerPins) {
//            RowCol pos = (RowCol) players.get(pin);
//            int row = pos.row;
//            int col = pos.col;
//            APLList position = new APLList(new Term[]
//                    {new APLNum(pin), new APLNum(row), new APLNum(col)});
//            t[i] = position;
//            i++;
//        }
//
//        closestPlayers = new APLList(t);
//        System.out.println("[ENV] This is what closestPlayers looks like: " + closestPlayers);
//        return closestPlayers;
//     }
      

     /**
      * Make an APLList of an APLFunction. Is called by the 2APL
      * agent.
      * @param funct APLFunction (for example thrown by event)
      * @return APLList
      */
     public Term functionToList(String agentname, APLFunction funct) throws
                            ExternalActionFailedException {
         //System.out.println("[ENV] APLFunction: " + funct);

         ArrayList<Term> arraylist = funct.getParams();
         //System.out.println("[ENV] Arraylist: " + arraylist);

         APLList apllist;
         Term[] t = new Term[arraylist.size()];
         arraylist.toArray(t);
         System.out.println("[ENV] Array of Terms: " + t);

         apllist = new APLList(t);
         System.out.println("[ENV] The created APLList : " + apllist);

         return apllist;
     }

     /**
      * Return a list of chips of a specified player
      * @param agentname
      * @return Term apllist with color, #chips pairs
      */
     public Term getOpponentChips(String agentname, APLNum pin) throws ExternalActionFailedException {

         int p = pin.toInt();
         //System.out.println("[ENV] Name (PIN!) of the player who's chipset we want: " +p);

         ChipSet chips = cgs.getPlayerByPerGameId(getPerGameID(p)).getChips();
         Term aplterm = convertChipSet(chips);
         return aplterm;
     }

     private int getPerGameID(int pin) {
 		
 		ClientGameStatus cgs = client.getGameStatus();
 		for( PlayerStatus player : cgs.getPlayers() ) {
 			int playerID = player.getPin();
 			if( playerID != pin )
 				return player.getPerGameId();
 		}
 		throw new RuntimeException("Responder ID not found.");
 		//return -1;
 	}

     /**
      * Return a list of chips of the agent
      * @param agentname
      * @return Term apllist with color, #chips pairs
      */
     public Term getAgentChips(String agentname) throws ExternalActionFailedException {

         int p = this.cgs.getMyPlayer().getPin();
         //System.out.println("[ENV] Name (PIN!) of the player who's chipset we want: " +p);
         return getOpponentChips(agentname, new APLNum(p));
     }



     /**
      *
      * @param agentname
      */
     public Term getChipsNeeded(String agentname, APLNum opponentid) throws ExternalActionFailedException {
         int opp_id = opponentid.toInt();
         ChipSet myChips = cgs.getMyPlayer().getChips();
         ArrayList<Path> shortestPaths = ShortestPaths.getShortestPathsToFirstGoal(cgs.getMyPlayer().getPosition(),
					cgs.getBoard(), cgs.getScoring());
		Path chosenPath = shortestPaths.remove(0); // Get the best path available
		ChipSet requiredChipsForPath = chosenPath.getRequiredChips(cgs.getBoard());
		ChipSet missing = myChips.getMissingChips(requiredChipsForPath);
         //ChipSet missing = agents.get(agentname).getChipsNeeded(opp_id);
         System.out.println("[ENV] Received the missing chipset: " + missing);

         Term aplterm = convertChipSet(missing);

         return aplterm;
     }


     /**
      *
      * @param agentname
      */
     public Term getChipsRedundant(String agentname, APLIdent agentid) throws ExternalActionFailedException {
    	 ChipSet myChips = cgs.getMyPlayer().getChips();
    	 ArrayList<Path> shortestPaths = ShortestPaths.getShortestPathsToFirstGoal(cgs.getMyPlayer().getPosition(),
					cgs.getBoard(), cgs.getScoring());
		Path chosenPath = shortestPaths.remove(0); // Get the best path available
		ChipSet requiredChipsForPath = chosenPath.getRequiredChips(cgs.getBoard());
 		ChipSet extra = myChips.getExtraChips(requiredChipsForPath);
         System.out.println("[ENV] Received the redundant chipset: " + extra);

         Term aplterm = convertChipSet(extra);

         return aplterm;
     }


     /**
      * Get the id of an agent
      * @param agentname The name of the agent
      * @return id of the agent
      * @throws apapl.ExternalActionFailedException
      */
     public Term getAgentId(String agentname)
            throws ExternalActionFailedException {
        int id = cgs.getPerGameId();

        APLNum aplid = new APLNum(id);
		return aplid;

	}

      /**
      * Get the id of an agent that is not this agent
      * @param agentname The name of the agent
      * @return id (int) the id of the opponent
      * @throws apapl.ExternalActionFailedException
      */
     public Term getOpponentId(String agentname)
            throws ExternalActionFailedException {
    	
    			int proposerID = cgs.getPerGameId();
    			ClientGameStatus cgs = client.getGameStatus();
    			for( PlayerStatus player : cgs.getPlayers() ) {
    				int playerID = player.getPerGameId();
    				if( playerID != proposerID )
    					return new APLNum(playerID);
    			}
    			throw new RuntimeException("Responder ID not found.");
    			//return -1;
    		}

	

           /**
      * Get the pin of an agent that is not this agent
      * @param agentname The pin of the agent
      * @return pin (int) the pin of the opponent
      * @throws apapl.ExternalActionFailedException
      */
     public Term getOpponentPin(String agentname)
            throws ExternalActionFailedException {
        int pin = Integer.parseInt(getOpponentId(agentname).toString());

        APLNum aplid = new APLNum(cgs.getPlayerByPerGameId(pin).getPin());
		return aplid;

	}

     
	/**
	* Get an agent's position in the game.
	* @returns APLList when succesful containing APLNum(col) and APLNum(row). 
	* @param String agentname containing the name of the agent calling this method.
	* @throws ExternalActionFailedException when the function cannot
	* be executed.
     * TODO: can only deal with one opponent... 
	*/
	public Term getOpponentPos(String agentname)
                            throws ExternalActionFailedException {
        APLList agentPos;
        int id = Integer.parseInt(getOpponentId(agentname).toString());
        RowCol rc = cgs.getPlayerByPerGameId(id).getPosition();
        try {
            int col = rc.col;
            //System.out.println("[ENV] " + agentname + " received AgentPosCol: " + col);
            int row = rc.row;
            //System.out.println("[ENV] " + agentname + " received AgentPosRow: " + row);
            agentPos = new APLList(new Term[] {
                new APLNum(col), new APLNum(row)
            });
            //System.out.println("[ENV] New APLList Position for " + agentname + ":" + agentPos);
        }
        catch (Exception e) {
            //System.out.println("[ENV] An exception was caught: " + e.getMessage());
            return new APLNum(-1);
        }
        System.out.println("[ENV] Agentposition: " + agentPos);
        return agentPos;
	}


   /**
    * Return the current phase of the game
    */
    public Term getPhase(String agentname)
            throws ExternalActionFailedException {

        String phase = cgs.getPhases().getCurrentPhaseName();
        if (phase.contains(" ")) {
            String[] ph = phase.toLowerCase().split(" ");
            phase = ph[0] + ph[1];
        }

        APLIdent p = new APLIdent(phase);
        return p;

    }


  
    /**
     * TODO: extract to chipset conversion in seperate method
     * */
    public Term getScoreAfterExchange(String agentname, APLNum id, APLList requestedchips) {
        int perId = id.toInt();

        
         LinkedList<Term> chips = requestedchips.toLinkedList();
       //  HashMap<String, Integer> proposal = new HashMap();
         ChipSet chipset = new ChipSet();

         Set<String> ctclr = cgs.getBoard().getColors();
         String[] ctcolors = new String[ctclr.size()];
         ctclr.toArray(ctcolors);

         HashMap<String, String> colorsmap = new HashMap();

         // link the normal ct colors to their lower case versions
         for (String color: ctcolors) {
             String colorLC = color.toLowerCase();
             colorsmap.put(colorLC, color);
         }

         for (int i = 0; i<chips.size(); i++) {
             APLList item = (APLList) chips.get(i);
             LinkedList<Term> itemlist = item.toLinkedList();

             // modify color
             APLIdent colorapl = (APLIdent) itemlist.get(0);
             String clr = colorapl.toString();
             String originalcolor = colorsmap.get(clr);

             // modify amount of chips
             APLNum amountapl = (APLNum) itemlist.get(1);
             int amount = amountapl.toInt();

             // add to the proposal chips
             chipset.add(originalcolor, amount);
         }
         
        //int score = agents.get(agentname).getScoreAfterExchange(perId, chipset);

        APLNum aplscore = new APLNum(0);
        return aplscore;
    }


    /**
     * is GENERIC!
     */
    public Term getScore(String agentname, APLNum id) {
        int p = id.toInt();
        int score = cgs.getPlayerByPerGameId(p).getScore();

        APLNum aplscore = new APLNum(score);
        return aplscore;
    }

    // 
    




     /**
      * Get the pin of an agent
      * @param agentname The pin of the agent
      * @return
      * @throws apapl.ExternalActionFailedException
      */
     public Term getAgentPin(String agentname)
            throws ExternalActionFailedException {
        int pin = cgs.getMyPlayer().getPin();

        APLNum aplpin = new APLNum(pin);
		return aplpin;
		
	}
	
	/*
	public APLList getBoard() {
		ClientGameStatus cgs = agent.getGameStatus();
		return cgs.getBoard();
	}
	
	plic APLList getPathtoGoal() {
		
	}
	*/
	
	/**
	* Get the agent's position in the game.
	* @returns APLList when succesful containing APLNum(col) and APLNum(row). 
	* @param String agentname containing the name of the agent calling this method.
	* @throws ExternalActionFailedException when the function cannot
	* be executed.
	*/
	public Term getAgentPos(String agentname)
                            throws ExternalActionFailedException {
        APLList agentPos;
        RowCol rc = cgs.getMyPlayer().getPosition();
        try {
            int col = rc.col;
            //System.out.println("[ENV] " + agentname + " received AgentPosCol: " + col);
            int row = rc.row;
            //System.out.println("[ENV] " + agentname + " received AgentPosRow: " + row);
            agentPos = new APLList(new Term[] {
                new APLNum(col), new APLNum(row)
            });
            //System.out.println("[ENV] New APLList Position for " + agentname + ":" + agentPos);
        }
        catch (Exception e) {
            //System.out.println("[ENV] An exception was caught: " + e.getMessage());
            return new APLNum(-1);
        }
        System.out.println("[ENV] Agentposition: " + agentPos);
        return agentPos;
	}


//    public Term getAgentRole(String agentname) {
//        int pin = agents.get(agentname).getAgentRole();
//
//        String agentrole = agents.get(agentname).getRole(pin).toLowerCase();
//
//        APLIdent role = new APLIdent(agentrole);
//        return role;
//
//    }


    public Term getGoalId(String agentname, APLNum type, APLNum xcoor, APLNum ycoor) {
        int t = type.toInt();
        int x = xcoor.toInt();
        int y = ycoor.toInt();

        RowCol goalloc = new RowCol(x, y);
        Set<Goal> goals = cgs.getBoard().getGoals();
        String id = null;
        if (goals != null)
        	
        for (Goal g : goals) {
            if (g.getLocation().equals(goalloc)) {
                id = g.getID();
                return new APLIdent(id);
            }
        }

       
        return new APLNum(-1);
        
    }

    // Return the position of a goal of a specific type
	public Term getGoalPos(String agentname, APLNum type)
                            throws ExternalActionFailedException {

        int t = type.toInt();

        APLList goalPos;
        
        try {
        	RowCol rc = cgs.getBoard().getGoalLocations(t).get(0);
            int col = rc.col;
            //System.out.println("[ENV] " + agentname + " received GoalPosCol: " + col);
            int row = rc.row;
            //System.out.println("[ENV] " + agentname + " received GoalPosRow: " + row);
            goalPos = new APLList(new Term[] {
                new APLNum(col), new APLNum(row)
            });
            //System.out.println("[ENV] New APLList GoalPosition for " + agentname + ":" + goalPos);
        }
        catch (Exception e) {
            //System.out.println("[ENV] An exception was caught: " + e.getMessage());
            return new APLNum(-1);
        }
		return goalPos;
	}

    public Term getRole(String agentname, APLNum apl_id)
                        throws ExternalActionFailedException {

        int id = apl_id.toInt();
        //TODO remove the hack
        //cgs.getPlayerByPerGameId(id).setRole("proposer");
        String role = cgs.getPlayerByPerGameId(id).getRole();

    	if (role != "")
    		return new APLIdent(role);
    	else
    		return new APLNum(-1);
        

    }


 //   public Term getResponseAnswer(String agentname, APLNum messageId) {
    //    agents.get(agentname).getResponseAnswer
  //  }


    public void makeProposal(String agentname, APLNum responder) {
    	System.out.println("[ENV] trying to make a proposal: " + agentname);
    	 System.out.println("I'm the proposer, wheeeeee");
    	 int id = responder.toInt();
         ArrayList<ChipSet> exchange = (ArrayList<ChipSet>) strategy(id);
         ChipSet senderChips;
         ChipSet recipientChips;

         if(exchange == null) {
             System.out.println("No beneficial " +
                                 "exchanges found among the ones that are beneficial for the responder");
         }
         else {
             System.out.println("EXCHANGE: " + exchange);
             senderChips = exchange.get(0);
             recipientChips = exchange.get(1);
             BasicProposalDiscourseMessage proposal= new BasicProposalDiscourseMessage(
                             cgs.getPerGameId(), id, -1, senderChips, recipientChips);
//             sending = senderChips;
             
             PhaseWaiter waiter = new PhaseWaiter(cgs.getPhases());
             waiter.doWait(RecipConstants.minProposeTime, RecipConstants.maxProposeTime);
             
             
            client.communication.sendDiscourseRequest(proposal);

         }
    }

    /**
     * called by 2APL agent, convert APLNums to ints
     * @param ax: x coordinate of agent
     * @param ay: y coordinate of agent
     * @param gx: x coordinate of goal
     * @param gy: y coordinate of goal
     */
    
	public Term moveStepToGoal(String agentname, APLNum ax,
                                APLNum ay, APLIdent id) throws
                                ExternalActionFailedException {

        int agentx = ax.toInt();
        int agenty = ay.toInt();
        String goalid = id.toString();
        ClientGameStatus cgs = client.getGameStatus();
        Scoring scoring = cgs.getScoring();
        ArrayList<Path> shortestPaths = ShortestPaths.getShortestPathsToFirstGoal(cgs.getMyPlayer().getPosition(),
					cgs.getBoard(), scoring);
        

        // Get the best path available
        Path chosenPath = shortestPaths.remove(0); // why remove(0)??
        

        // Send move request
		client.communication.sendMoveRequest(chosenPath.getPoint(1));
        
     
            APLIdent uTD = new APLIdent("true");
            System.out.println("[ENV] moveStepToGoal returns: " + uTD);
            return uTD; 
        
	}

     /**
      * Send a proposal to a player
      * @param agentname
      * @param playerpin Pin of the player the agent will propose to
      * @param requestedchips Requested chips of opponent player
      */
//     public Term sendProposal(String agentname, APLNum playerpin,
//                                                    APLList requestedchips) {
//    	 System.out.println("[ENV] trying to send a proposal: " + agentname);
//
//         LinkedList<Term> chips = requestedchips.toLinkedList();
//       //  HashMap<String, Integer> proposal = new HashMap();
//         ChipSet chipset = new ChipSet();
//
//         Set<String> ctclr = cgs.getBoard().getColors();
//         String[] ctcolors = new String[ctclr.size()];
//         ctclr.toArray(ctcolors);
//
//         HashMap<String, String> colorsmap = new HashMap<String, String>();
//
//         // link the normal ct colors to their lower case versions
//         for (String color: ctcolors) {
//             String colorLC = color.toLowerCase();
//             colorsmap.put(colorLC, color);
//         }
//         
//         for (int i = 0; i<chips.size(); i++) {
//             APLList item = (APLList) chips.get(i);
//             LinkedList<Term> itemlist = item.toLinkedList();
//
//             // modify color
//             APLIdent colorapl = (APLIdent) itemlist.get(0);
//             String clr = colorapl.toString();
//             String originalcolor = colorsmap.get(clr);
//
//             // modify amount of chips
//             APLNum amountapl = (APLNum) itemlist.get(1);
//             int amount = amountapl.toInt();
//
//             // add to the proposal chips
//             chipset.add(originalcolor, amount);
//         }
//
//         BasicProposalDiscourseMessage proposal= new BasicProposalDiscourseMessage(
//                 cgs.getGameId(), playerpin.toInt(), -1, new ChipSet(), chipset);
//
//     		client.communication.sendDiscourseRequest(proposal);
//
//     
//             
//         int messageId = proposal.getMessageId();
//         APLNum msgId = new APLNum(messageId);
//         return msgId; 
//     }


//
//    /**
//     * BROKEN
//     * Informs the sender that the proposal has or has not been accepted.
//     * @param agentname The name of the agent requesting the send action
//     * @param response Acceptance or rejection of the proposal
//     * @param messageid The message which is subject to the response
//     */
//    public void sendResponse(String agentname, APLIdent response, APLNum messageid) {
//   
//    }

    
	//////////////////////////////
	
	
	
	
	
	
	
	//////////////////////// 2OPL TO JAVASPACE AND 2APL
	/*
	 * Make sure String s has an index in the Prolog engine.
	 */
	public int makeStringKnown(String s){
		if(oopl.prolog.strStorage.getInt(s)==null) oopl.prolog.strStorage.add(s);
		return oopl.prolog.strStorage.getInt(s);
	}
	/*
	 * Make the possible external actions known to the Prolog engine. These will be the actions that
	 * the organization can do.
	 */
	@Override
	public void registerActions(Prolog p) { 
		oopl.prolog.builtin.external.registerAction("read", this, ExternalActions.INTAR, ExternalActions.INTAR);
		oopl.prolog.builtin.external.registerAction("write", this, ExternalActions.INTAR, ExternalActions.INTAR);
		oopl.prolog.builtin.external.registerAction("notifyAgent", this, ExternalActions.INTAR, ExternalActions.INTAR);
		oopl.prolog.builtin.external.registerAction("clock", this, ExternalActions.INTAR, ExternalActions.INTAR);
	}

	/*
	 * Handle a call from the organization (actually: from Prolog). These calls are in IntProlog datatypes (int arrays). 
	 * ExternalActions ea is a part of the Prolog engine which reads returns ea.intResult after the
	 * external call.
	 */
	@Override
	public void handleCall(int[] call, ExternalActions ea, int returnType) {  
		/*
		 * For JavaSpace calls: the integer array is first transformed to an Entry object, then passed
		 * to the JavaSpaced using the appropriate method call, and then the result is converted back
		 * to an integer array.
		 */
		if(call[1] == oopl.prolog.strStorage.getInt("read")){
			try {

				TimeEntry a = utilities.createEntry(call);
				//System.out.println(a.toString());
				TimeEntry e = getLast(a);
				//System.out.println(e.toString());
				ea.intResult = utilities.entryToArray(e);
			} catch (Exception e) {e.printStackTrace();}
		} else if(call[1] == oopl.prolog.strStorage.getInt("write")){
			System.out.println("write (points)");
			try {
				long lease = utilities.get_number(call,oopl.prolog.harvester.scanElement(call, 3, false, false)+1);
				//if(lease <= 0) lease = Lease.FOREVER;
				
				TimeEntry e = utilities.createEntry(call);
				if (e.getTime() == null)
					e.setTime();
				if (e.getClock() == null) {
					//updateClock(0);
					e.setClock(clock);
				}
				System.out.println("Organization writes: "+e.toString());
				space.write(e);
				//System.out.println(e+"  "+lease+"   "+Lease.FOREVER);
				ea.intResult = ar_true;
			} catch (Exception e) {e.printStackTrace();}
	    /*
	     * The next case throws towards the object an event that its status is changed.
	     */
		} else if(call[1] == oopl.prolog.strStorage.getInt("notifyAgent")){ // notifyAgent(name,obligation(blabla)).
			
			String recipient = oopl.prolog.strStorage.getString(call[4]);
			APLFunction event = (APLFunction)converter.get2APLTerm(Arrays.copyOfRange(call, 6, call.length));
			TimeEntry e = utilities.createEntry(recipient, event);
			if (e.getTime() == null)
				e.setTime();
			if (e.getClock() == null) {
				//updateClock(0);
				e.setClock(clock);
			}
			System.out.println("Organization notifies object (write): "+e.toString());
			space.write(e);
			
			//throwEvent(event, new String[]{recipient});
			ea.intResult = ar_true;
		} else if(call[1] == oopl.prolog.strStorage.getInt("clock")){ // Read the clock
			int[] r = new int[3];
			utilities.addNumber(r, 0, updateClock(0)); // Use updateClock because of synchronization
			ea.intResult = r;
		}
	}


	@Override
	public void handleCall(Object[] call, ExternalActions p, int returnType) { }
	
	
	
	
	

	//from object program
	public Term read(String sAgent, APLFunction call, APLNum timeOut){
	
		try{ 
			TimeEntry te = utilities.createEntry(sAgent,call);
			System.out.println("Agent reads: "+te.toString());
			TimeEntry te1 = space.read(te, 2000);
			System.out.println("Agent finds: "+te1.toString());
			return utilities.entryToTerm(te1); 
		} catch(Exception e){ e.printStackTrace(); return new APLIdent("null"); }
	}
	
	public Term write(String sAgent, APLFunction call, APLNum lease){ 
		//System.out.println("write " + sAgent);
		try{

			TimeEntry e = utilities.createEntry(sAgent,call);
			if (e.getTime() == null)
				e.setTime();
			if (e.getClock() == null) {
				//updateClock(0);
				e.setClock(clock);
			}
			//System.out.println("Agent writes: "+e.toString());
			space.write(e);

			return new APLIdent("true");
		}catch (Exception e){ e.printStackTrace(); return new APLIdent("null"); }
	}
	

	/*
	 * ENVIRONMENT OVERRIDES
	 
	@Override
	public void addAgent(String sAgent) {
		System.out.println("register " + sAgent);

			register(sAgent);
		
	}
	*/
	private void register(String agent) {
		
		AgentHandler handler;
		try {
			handler = new AgentHandler(this, agent);
			try {
				session.addListener(new Prohibition(agent), handler);
				session.addListener(new Obligation(agent), handler); 
				session.addListener(new Points(agent), handler);
				session.addListener(new Message(agent), handler);
				session.addListener(new Time(), handler);
			} catch (TransactionException e) {
				e.printStackTrace();
			} 
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private TimeEntry getLast(TimeEntry a) {
		try {
			TimeEntry[] result = space.readMultiple(a);
				if (result.length > 0)
					return getLatest(result);
			} catch (Exception e) {
				e.printStackTrace();
			} 

		return null;
	}
	
	private TimeEntry getLatest(TimeEntry[] result) {
		Arrays.sort(result, new Comparator<TimeEntry>(){
			@Override
			public int compare(TimeEntry t1, TimeEntry t2) {
				return t1.getTime().compareTo(t2.getTime());
			}

		});
		System.out.println("latest      "+result[result.length-1]);
		return result[result.length-1];
	}

	public void notifyAgent(String agent, TimeEntry e) {
		Term t = utilities.entryToTerm(e);
		if (t.toString() == "null")
			return;
		throwEvent((APLFunction) t, new String[]{agent});
		System.out.println("Event sent to object      "+agent+ " " +t.toString());
		
	}
	public void notifyAgent(String agent, ArrayList<TimeEntry> r) {
		for (TimeEntry te : r) {
			notifyAgent(agent,te);
		}
	}

	public void notifyOrg(TimeEntry te) {
		System.out.println("org notified found "+te.toString());
		System.out.println("org notified sent "+te.toPrologString());
		//int[] OOPLformat = te.toIntArray(oopl);
		//oopl.handleEvent(ar_state_change, false);
		oopl.handleEvent(oopl.getProlog().mp.parseFact(te.toPrologString(),oopl.getProlog().strStorage,false),false);
	}
	
	 private void registerOrg() throws RemoteException {
			
			OrgHandler handler = new OrgHandler(this);
			
			try {
				for (int i=0; i<agents1.length;i++) {
					session.addListener(new Position(agents1[i]), handler); 
				}
				session.addListener(new tuplespace.Goal(), handler); 
				session.addListener(new Chip(), handler); 
				session.addListener(new Time(), handler); 
				session.addListener(new Tile(), handler); 
				session.addListener(new GroupCoin(), handler); 
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransactionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	 }
	
    
   /* private void insertTestData()
    {
    	Cargo cargo = new Cargo(5, new Cell(10,10), 1);
    	Points p1 = new Points("a1", 1000, 1);
    	Points p2 = new Points("a2", 1000, 1);
    	Points p3 = new Points("a3", 1000, 1);
    	Points p4 = new Points("c1", 1000, 1);
    	Points p5 = new Points("t1", 1000, 1);
    	Reading r1 = new Reading(11, "a1", new Cell(11,11), 1, 50);
    	Reading r2 = new Reading(12, "a2", new Cell(1,11), 1, 60);
    	Reading r3 = new Reading(13, "a3", new Cell(11,1), 1, 10);
    	Coin c1 = new Coin(10, new Cell(15,15), "a1", 1);
    	Coin c2 = new Coin(20, new Cell(1,15), "a2", 1);
    	Coin c3 = new Coin(30, new Cell(15,1), "a3", 1);
    	Time t1 = new Time(0);
    	Prohibition px = new Prohibition("t1","[at(5, 5, t1)]", "[reduce_300(t1)]",0);
    	try {
			space.write(cargo, null, Lease.FOREVER);
			space.write(p1, null, Lease.FOREVER);
			space.write(p2, null, Lease.FOREVER);
			space.write(p3, null, Lease.FOREVER);
			space.write(p4, null, Lease.FOREVER);
			space.write(p5, null, Lease.FOREVER);
			space.write(r1, null, Lease.FOREVER);
			space.write(r2, null, Lease.FOREVER);
			space.write(r3, null, Lease.FOREVER);
			space.write(c1, null, Lease.FOREVER);
			space.write(c2, null, Lease.FOREVER);
			space.write(c3, null, Lease.FOREVER);
			space.write(t1, null, Lease.FOREVER);
			space.write(px, null, Lease.FOREVER);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    
    	
    }

*/

    private void dumpGSdata() {
    	TimeEntry entry;
        //Entry temp = new Time();
    	System.out.println("-------------------------last log tuples start--------------------------------");
    	ArrayList<TimeEntry> result = new ArrayList<TimeEntry>();
		while ((entry = (TimeEntry) space.take(null)) != null){
			System.out.println(entry.toString());
			result.add(entry);
		}
System.out.println("-------------------------last log tuples end----------------------------------");
return;
	}

	
}
