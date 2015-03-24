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
import java.util.Hashtable;

import net.jini.core.transaction.TransactionException;
import oopl.DistributedOOPL;
import oopl.GUI.GUI;
import tuplespace.*;
import tuplespace.Prohibition;
import tuplespace.Sanction;
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

import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;


/*
 * Extends Environment to be compatible with 2APL and implements ExternalTool to 
 * be compatible with my Prolog engine. 
 */
public class EnvCT  extends Environment implements ExternalTool{
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
	public static String[] agents = {"a1", "a2", "a3", "t1", "c1"};
	
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
			registerOrg();
			p2j = new Prolog2Java();
			// Starting the normative system:
			oopl = new DistributedOOPL(); // Create interpreter object
			//GUI g = new GUI(oopl,"SpaceOrg.2opl","OOPL",null,6677); // Make a GUI for the interpreter
			GUI g = new GUI(oopl,"GroupOrg.2opl","OOPL",null,6677); // Make a GUI for the interpreter
			converter = new APAPLTermConverter(oopl.prolog); // Make a term converter (relies on Prolog engine for string storage)
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
		System.out.println(clock);
		t.setClock(clock);
		space.write(t);

		return clock;
	}
	
	/*
	 * Constructor immediately initializes the space. 
	 */
	public EnvCT(){
		super();
		
		try { initializeGS(); initializeOOPL();} catch (Exception e) { e.printStackTrace(); }
	}
	
	
	/////////////////////////CT
	public void throwEvents(APLFunction event, String ... receivers) {
        throwEvent(event, receivers);
    }
		


     public Term requestChips(String agentname, APLNum opppin, APLNum oppid) throws ExternalActionFailedException {
         System.out.println("[ENV] ==========> IN REQUEST CHIPS");
         int opponentid = oppid.toInt();
        // String goalid = id.toString();
         int opponentpin = opppin.toInt();
         //TODO retrieve chips from spaces
         //ChipSet chips = agents.get(agentname).requestChips(opponentpin, opponentid);

         Term apllist = convertChipSet(chips);

         System.out.println("[ENV] REQUEST CHIPS: RETURNING LIST");
         return apllist; 
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
         //TODO retrieve from spaces
         //ChipSet chips = agents.get(agentname).getChips(p);
         Term aplterm = convertChipSet(chips);
         return aplterm;
     }



     /**
      * Return a list of chips of the agent
      * @param agentname
      * @return Term apllist with color, #chips pairs
      */
     public Term getAgentChips(String agentname) throws ExternalActionFailedException {

         int p = agents.get(agentname).getAgentPin();
         //System.out.println("[ENV] Name (PIN!) of the player who's chipset we want: " +p);

         ChipSet chips = agents.get(agentname).getChips(p);
         Term aplterm = convertChipSet(chips);
         return aplterm;
     }



     /**
      *
      * @param agentname
      */
     public Term getChipsNeeded(String agentname, APLNum opponentid) throws ExternalActionFailedException {
         int opp_id = opponentid.toInt();
         ChipSet missing = agents.get(agentname).getChipsNeeded(opp_id);
         System.out.println("[ENV] Received the missing chipset: " + missing);

         Term aplterm = convertChipSet(missing);

         return aplterm;
     }


     /**
      *
      * @param agentname
      */
     public Term getChipsRedundant(String agentname, APLIdent agentid) throws ExternalActionFailedException {
         String id = agentid.toString();
         ChipSet redundant = agents.get(agentname).getChipsRedundant(id);
         System.out.println("[ENV] Received the redundant chipset: " + redundant);

         Term aplterm = convertChipSet(redundant);

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
        int id = agents.get(agentname).getAgentId();

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
        int id = agents.get(agentname).getOpponentId();

        APLNum aplid = new APLNum(id);
		return aplid;

	}

           /**
      * Get the pin of an agent that is not this agent
      * @param agentname The pin of the agent
      * @return pin (int) the pin of the opponent
      * @throws apapl.ExternalActionFailedException
      */
     public Term getOpponentPin(String agentname)
            throws ExternalActionFailedException {
        int pin = agents.get(agentname).getOpponentPin();

        APLNum aplid = new APLNum(pin);
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
        try {
            int col = agents.get(agentname).getOpponentPosCol();
            //System.out.println("[ENV] " + agentname + " received AgentPosCol: " + col);
            int row = agents.get(agentname).getOpponentPosRow();
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

        String phase = agents.get(agentname).getPhase();
        if (phase.contains(" ")) {
            String[] ph = phase.toLowerCase().split(" ");
            phase = ph[0] + ph[1];
        }

        APLIdent p = new APLIdent(phase);
        return p;

    }


    public Term getScoreAfterExchange(String agentname, APLNum id, APLNum apl_msgid) {
        int perId = id.toInt();
        int msgid = apl_msgid.toInt();
        int score = agents.get(agentname).getScoreAfterExchange(perId, msgid);

        APLNum aplscore = new APLNum(score);
        return aplscore;
    }


    /**
     * TODO: extract to chipset conversion in seperate method
     * */
    public Term getScoreAfterExchange(String agentname, APLNum id, APLList requestedchips) {
        int perId = id.toInt();

         ClientGameStatus cgs = agents.get(agentname).getGameStatus();
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
         
        int score = agents.get(agentname).getScoreAfterExchange(perId, chipset);

        APLNum aplscore = new APLNum(score);
        return aplscore;
    }


    /**
     * is GENERIC!
     */
    public Term getScore(String agentname, APLNum id) {
        int p = id.toInt();
        int score = agents.get(agentname).getScore(p);

        APLNum aplscore = new APLNum(score);
        return aplscore;
    }

    // 
    public Term getScoreCurrentChips(String agentname, APLNum apl_id) {
        int id = apl_id.toInt();
        int score = agents.get(agentname).getScoreCurrentChips(id);

        APLNum aplscore = new APLNum(score);
        return aplscore;

    }
    




     /**
      * Get the pin of an agent
      * @param agentname The pin of the agent
      * @return
      * @throws apapl.ExternalActionFailedException
      */
     public Term getAgentPin(String agentname)
            throws ExternalActionFailedException {
        int pin = agents.get(agentname).getAgentPin();

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
        try {
            int col = agents.get(agentname).getAgentPosCol();
            //System.out.println("[ENV] " + agentname + " received AgentPosCol: " + col);
            int row = agents.get(agentname).getAgentPosRow();
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

        String agentid = agents.get(agentname).getGoalId(t, x, y);

        APLIdent id = new APLIdent(agentid);
        return id; 
    }

    // Return the position of a goal of a specific type
	public Term getGoalPos(String agentname, APLNum type)
                            throws ExternalActionFailedException {

        int t = type.toInt();

        APLList goalPos;
        try {
            int col = agents.get(agentname).getGoalPosCol(t);
            //System.out.println("[ENV] " + agentname + " received GoalPosCol: " + col);
            int row = agents.get(agentname).getGoalPosRow(t);
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
        String role = agents.get(agentname).getRole(id);
        APLIdent apl_role = new APLIdent(role);
        return apl_role;

    }


 //   public Term getResponseAnswer(String agentname, APLNum messageId) {
    //    agents.get(agentname).getResponseAnswer
  //  }


    public void makeProposal(String agentname, APLNum responder) {
        agents.get(agentname).makeProposal(responder.toInt());
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
		boolean upToDate = agents.get(agentname).moveStepToGoal(agentx, agenty, goalid);
        
        // check whether info was up to date and agent was able to move
        if (!upToDate) {
            throw new ExternalActionFailedException("[ENV] Goalposition and Agentposition were out of date. Unable to move");
        }
        else {
            // convert to String
            String uTDString = new Boolean(upToDate).toString();
            APLIdent uTD = new APLIdent(uTDString);
            System.out.println("[ENV] moveStepToGoal returns: " + uTD);
            return uTD; 
        }
    }

	
	public void start(String agentname) {
		agents.get(agentname).start();
	}
	
	public void gameStarted(String agentname) {
		System.out.println("[ENV] Game started!");
	}
	
	public void gameEnded(String agentname) {
		
	}


	/**
	 * Remove an agent (identified by the String agentname).
     * @param String agentname containing the name of the agent calling this method.
	 */
    public void removeAgent(String agentname) {
    }

	/**
	 * Function for test purposes only.
	 * @param String s with the name of the agent.
	 * @param APLIdent containing a message.
	 * @throws ExternalActionFailedException when the function cannot
	 * be executed.
	 */
     public void sayHello(String s, APLIdent aplident)
            throws ExternalActionFailedException {
        System.out.println("[ENV] says hello world");

        String type = "hallo";
        int id = 10;
        APLFunction event = new APLFunction("message", new APLIdent(type),
                                                                new APLNum(id));

        System.out.println("[ENV] The following message will be sent to 2APL: " + event);
		throwEvent(event, s);

    }

    /**
     *
     * @param agentname
     * @return
     */
// HACK! UNDO LATER
//     public Term selfSufficiency(String agentname) {
//         int ss = agents.get(agentname).selfSufficiency();
//
//         APLNum selfsuf = new APLNum(ss);
//         return selfsuf;
//     }


     /**
      * Send a proposal to a player
      * @param agentname
      * @param playerpin Pin of the player the agent will propose to
      * @param requestedchips Requested chips of opponent player
      */
     public Term sendProposal(String agentname, APLNum playerpin,
                                                    APLList requestedchips) {
         ClientGameStatus cgs = agents.get(agentname).getGameStatus();
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

         int messageId = agents.get(agentname).sendProposal(playerpin.toInt(), chipset);

         APLNum msgId = new APLNum(messageId);
         return msgId; 
     }



    /**
     * Informs the sender that the proposal has or has not been accepted.
     * @param agentname The name of the agent requesting the send action
     * @param response Acceptance or rejection of the proposal
     * @param messageid The message which is subject to the response
     */
    public void sendResponse(String agentname, APLIdent response, APLNum messageid) {
        agents.get(agentname).sendResponse(response.toString(), messageid.toInt());
    }


    /**
	* Change the agent's name.
	* @param String agentname containing the name of the agent calling this method.
	*/
	public void setAgentName(String agentname) {
		agents.get(agentname).setClientName(agentname);
	}
	
	
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

				TimeEntry a = createEntry(call);
				//System.out.println(a.toString());
				TimeEntry e = getLast(a);
				//System.out.println(e.toString());
				ea.intResult = entryToArray(e);
			} catch (Exception e) {e.printStackTrace();}
		} else if(call[1] == oopl.prolog.strStorage.getInt("write")){
			System.out.println("write (points)");
			try {
				long lease = get_number(call,oopl.prolog.harvester.scanElement(call, 3, false, false)+1);
				//if(lease <= 0) lease = Lease.FOREVER;
				
				TimeEntry e = createEntry(call);
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
			TimeEntry e = createEntry(recipient, event);
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
			addNumber(r, 0, updateClock(0)); // Use updateClock because of synchronization
			ea.intResult = r;
		}
	}


	@Override
	public void handleCall(Object[] call, ExternalActions p, int returnType) { }
	
	/*
	 * Create an entry object form an integer array. Perhaps we want to replace this with
	 * something like createEntry(oopl.prolog.toPrologString(call)).
	 */
	public TimeEntry createEntry(int[] call) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException{ // e.g.: read(tuple(name,point(2,4),20),0)
		//System.out.println(oopl.prolog.arStr(call));
		return p2j.parseTerm(call, converter, oopl);
		
	}
	

	/*
	 * Convert an entry to an array. Can also be done by calling the prolog compiler and give
	 * it e.toPrologString() as an argument.
	 */
	public int[] entryToArray(TimeEntry e){
		if(e == null){
			int[] r = new int[3];
			addPredicate(r, 0, oopl.prolog.strStorage.getInt("null"), 0);
			return r;
				
		} 
		else {
			return e.toIntArray(oopl);
		}
	
	}
	/*
	 * Gets the int value of a number out of an integer array.
	 * Note that normally this is a double.
	 */
	public int get_number(int[] call, int cursor){
		long l1 = ((long)call[cursor]<<32)>>>32;
		long l2 = ((long)call[cursor+1]<<32)>>>32;
		return (int)Double.longBitsToDouble((l1<<32)|l2);
	} 
	/*
	 * Add predicate integers to an array.
	 */
	public void addPredicate(int[] array, int cursor, int name, int arity){
		array[cursor] = IntHarvester.PREDICATE;
		array[cursor+1] = name;
		array[cursor+2] = arity;
	}
	/*
	 * Add a number to an array.
	 */
	public void addNumber(int[] array, int cursor, int number){
		array[cursor] = IntHarvester.NUMBER;
		array[cursor+1] = getInt(number,true);
		array[cursor+2] = getInt(number,false);
	}
	/*
	 * Convert a regular integer to a Prolog store value. Each number is encoded with 
	 * two integers (64 bit double format), so you can use getInt(i,true) and getInt(i,false) 
	 * to get both i's number representation parts.
	 */
	public int getInt(int i, boolean a){
		long l = Double.doubleToLongBits(i);
		if(a) return (int)((l>>>32));
		else return (int)((l<<32)>>>32);
	}
	
	
	//////////////////////// 2APL/2OPL from APLFunction to TimeEntry AND JAVASPACE

	/**
	 * Convert a Prolog predicate to a suitable JavaSpace datatype.
	 * @param sAgent The object that calls the method (important for the name in the status).
	 * @param call The predicate from the call.
	 * @return The entry representation of the predicate.
	 */
	public TimeEntry createEntry(String sAgent, APLFunction call){ 
		
		//System.out.print("from/for object " + sAgent + "  ");
		//System.out.println(call.toString());
		if(call.getName().equals(TYPE_STATUS)){ // Prolog format: status(position(1,4),30) 
			Cell c = null;
			if(call.getParams().get(0) instanceof APLFunction){ // null is APLIdent  
				APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
				int pointX = ((APLNum)point.getParams().get(0)).toInt(); // Get the position
				int pointY = ((APLNum)point.getParams().get(1)).toInt();
				c = new Cell(pointX,pointY);
			}
			Integer clock = null; // if health is null (which is ident) it stays also in java null
			if(call.getParams().get(1) instanceof APLNum) clock = ((APLNum)call.getParams().get(1)).toInt(); // The health meter
			
			return new Position(sAgent,c,clock); // Create Tuple
		}
		else if(call.getName().equals(TYPE_READINGREQ)){ // Prolog format: readingRequest(position(X,Y))
			Cell c = null;
			if(call.getParams().get(0) instanceof APLFunction){ // null is APLIdent  
				APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
				int pointX = ((APLNum)point.getParams().get(0)).toInt(); // Get the position
				int pointY = ((APLNum)point.getParams().get(1)).toInt();
				c = new Cell(pointX,pointY);
			}
			//System.out.print("from/for object " + sAgent + "  ");
			//System.out.println(call.toString());
			return new SwapRequest(); //TODO Create Tuple
		}
		else if(call.getName().equals(TYPE_READING)){ // Prolog format: reading(position(X,Y))
			Cell c = null;
			if(call.getParams().get(0) instanceof APLFunction){ // null is APLIdent  
				APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
				int pointX = ((APLNum)point.getParams().get(0)).toInt(); // Get the position
				int pointY = ((APLNum)point.getParams().get(1)).toInt();
				c = new Cell(pointX,pointY);
			}
			System.out.print("from/for object " + sAgent + "  ");
			System.out.println(call.toString());
			//System.out.println(new Chip(sAgent,c));
			return new Chip(sAgent); //TODO Create Tuple
		}
		else if(call.getName().equals(TYPE_COIN)){ // Prolog format: coin(position(X,Y),Clock,Agent)
			//System.out.println("create entry coin "+call.getParams().toString());
			Cell c = null;
			if(call.getParams().get(0) instanceof APLFunction){ // null is APLIdent  
				APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
				int pointX = ((APLNum)point.getParams().get(0)).toInt(); // Get the position
				int pointY = ((APLNum)point.getParams().get(1)).toInt();
				c = new Cell(pointX,pointY);
			}
			Integer clock = null; // if health is null (which is ident) it stays also in java null
			if(call.getParams().get(1) instanceof APLNum) clock = ((APLNum)call.getParams().get(1)).toInt(); // The health meter
			String agent = null; // if health is null (which is ident) it stays also in java null
			if(call.getParams().get(2) instanceof APLIdent) agent = ((APLIdent)call.getParams().get(2)).toString(); // The health meter
			
			return new Tile(c,agent,clock); // Create Tuple
		}
		else if(call.getName().equals(TYPE_CARGO)){ // Prolog format: cargo(position(X,Y),Clock)
			//System.out.println("create entry cargo "+call.getParams().toString());
			Cell c = null;
			if(call.getParams().get(0) instanceof APLFunction){ // null is APLIdent  
				APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
				int pointX = ((APLNum)point.getParams().get(0)).toInt(); // Get the position
				int pointY = ((APLNum)point.getParams().get(1)).toInt();
				c = new Cell(pointX,pointY);
			}
			Integer clock = null; // if health is null (which is ident) it stays also in java null
			if(call.getParams().get(1) instanceof APLNum) clock = ((APLNum)call.getParams().get(1)).toInt(); // The health meter
			
			return new tuplespace.Goal(); //TODO Create Tuple
		} 
		else if(call.getName().equals(TYPE_POINTS)){ //points(Agent,Now,NewHealth)
			System.out.println("create entry points "+call.getParams().toString());
			
			Integer clock = null; // if health is null (which is ident) it stays also in java null
			if(call.getParams().get(1) instanceof APLNum) clock = ((APLNum)call.getParams().get(1)).toInt(); // The health meter
			Integer health = null; // if health is null (which is ident) it stays also in java null
			if(call.getParams().get(2) instanceof APLNum) health = ((APLNum)call.getParams().get(2)).toInt(); // The health meter
		
			return new Points(sAgent); // Create Tuple
		}
		else if(call.getName().equals(TYPE_PROHIBITION)){ // Prolog format: status(position(1,4),30) 
			Prohibition p = null;
			//System.out.println("create entry prohibition "+call.getParams().toString());
			
		
			if(call.getParams().get(0) instanceof Term){ // null is APLIdent  
				//APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations 
				String s1 = call.getParams().get(0).toString();// Get the position
				String s2 = call.getParams().get(1).toString();
				p = new Prohibition(sAgent, s1, s2, clock);
			}
			//Integer health = null; // if health is null (which is ident) it stays also in java null
			//if(call.getParams().get(1) instanceof APLNum) health = ((APLNum)call.getParams().get(1)).toInt(); // The health meter
			//System.out.println(call.toString());
			//System.out.println(p.toString());
			return p; // Create Tuple
		} 
		else if(call.getName().equals(TYPE_OBLIGATION)){ // Prolog format: status(position(1,4),30) 
			Obligation o = null;
			//System.out.println("create entry obligation "+call.getParams().toString());
			
		
			if(call.getParams().get(0) instanceof Term){ // null is APLIdent  
				//APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
				String s1 = call.getParams().get(0).toString();// Get the position
				//String s2 = call.getParams().get(1).toString();
				String s3 = call.getParams().get(2).toString();
				
				int deadline = ((APLNum)call.getParams().get(1)).toInt();
				
				o = new Obligation(sAgent, s1, s3, deadline, clock);
				//System.out.println(s2);
			}
			return o; // Create Tuple
		} 
		else if(call.getName().equals(TYPE_SANCTION)){ // Prolog format: status(position(1,4),30) 
			Sanction s = null;
			//System.out.println("create entry obligation "+call.getParams().toString());
			
		
			if(call.getParams().get(0) instanceof Term){ // null is APLIdent  
				//APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
				String s1 = call.getParams().get(0).toString();// Get the position
				//String s2 = call.getParams().get(1).toString();
				//String s3 = call.getParams().get(2).toString();
				
				int value = ((APLNum)call.getParams().get(1)).toInt();
				
				s = new Sanction(sAgent, value, clock);
				//System.out.println(s2);
			}
			return s; // Create Tuple
		}
		else if(call.getName().equals(TYPE_GROUPCOIN)){ // Prolog format: status(position(1,4),30) 
			GroupCoin s = null;
			//System.out.println("create entry obligation "+call.getParams().toString());
			
		
			if(call.getParams().get(0) instanceof Term){ // null is APLIdent  
				//APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
				String s1 = call.getParams().get(0).toString();// Get the position
				//String s2 = call.getParams().get(1).toString();
				//String s3 = call.getParams().get(2).toString();
				
				int value = ((APLNum)call.getParams().get(1)).toInt();
				
				s = new GroupCoin(15, 15); //@TODO remove the hack!!!!
				System.out.println(s);
			}
			return s; // Create Tuple
		}

		return null;
	}
	
	//object use
	//Possibly move to Tuple classes
	public Term entryToTerm(TimeEntry timeEntry){ 
		
		if(timeEntry instanceof Points){ // points(Points)
			Points points = (Points) timeEntry; 
			return new APLFunction("points", new Term[]{new APLNum(points.value)}); // construct result
		} 
		else if(timeEntry instanceof Time){ // clock(Clock)
			Time time = (Time) timeEntry; 
			return new APLFunction("clock", new Term[]{new APLNum(time.clock)}); // construct result
		} 
		else if(timeEntry instanceof Chip){ // reading(at(X,Y),Value,Agent,Clock)
			Chip reading = (Chip) timeEntry;
			//Term term = constructTerm("at("+reading.cell.x+","+reading.cell.y+")");
			return new APLFunction("reading", new Term[]{new APLNum(reading.number.intValue()),new APLIdent(reading.agent),new APLNum(reading.clock)}); // construct result
		} 
		else if(timeEntry instanceof Obligation){ //obligation(Goal, Deadline, Sanction)
			Obligation o = (Obligation) timeEntry; 
			String name = o.agent;
			if(name==null)name="null"; 
			Term posTerm = new APLIdent("null");
			Term posTerm1 = new APLIdent("null");
			Term posTerm2 = new APLIdent("null");
			//all possible obligations

			posTerm = constructTerm(o.obligation);
			
			if(o.deadline!=null){
				posTerm1 = new APLNum(o.deadline);
			}
			if(o.sanction!=null){
				posTerm2 = constructTerm(o.sanction);
			}
			return new APLFunction("obligation", new Term[]{new APLList(posTerm),posTerm1,new APLList(posTerm2)});
		}
		else if(timeEntry instanceof Prohibition){ //prohibition(State,Sanction)
			Prohibition o = (Prohibition) timeEntry; 
			String name = o.agent;
			if(name==null)name="null"; 
			Term posTerm = new APLIdent("null");
			Term posTerm2 = new APLIdent("null");

			posTerm = constructTerm(o.prohibition);

			if(o.sanction!=null){
				posTerm2 = constructTerm(o.sanction);
			}
			return new APLFunction("prohibition", new Term[]{new APLList(posTerm),new APLList(posTerm2)});
		}
		else if(timeEntry instanceof Message){ //message
			Message m = (Message) timeEntry;
            String type = m.type;
            String response;
            System.out.println("[MSG] Message is of type: " + type);

            APLFunction event;

            if (type.equals("response")) {
                Boolean accepted = m.accepted;
                if (accepted) {response = "accept";}
                else {response = "reject";}

                event = new APLFunction("message",
                        new APLIdent(type), new APLNum(m.id),
                        new APLIdent(response));
            }

            else {
                event = new APLFunction("message",
                           new APLIdent(type), new APLNum(m.id));
            }
			return event;
		}
		
		return new APLIdent("null");
	}
	
	private Term constructTerm(String term) {
		term = term.replace("[","");
		term.replace("]","");
		
		int tx = term.indexOf("(");
		String s = term.substring(0, tx).trim();
		
		Term[] t = new Term[10];
		int i = term.indexOf(",");
		int index = 0;
		if (i == -1) {
			return new APLFunction(term);
		}
		else {
			String x = term.substring(s.length() + 1, i).trim();
			t[index] = numOrIdent(x);
			index++;
		}
		while (term.indexOf(",", i+1) > 0) {
			int j = term.indexOf(",", i+1);
			String y = term.substring(i+1, j).trim();
			t[index] = numOrIdent(y);
			i=j;
			index++;
		}
		int j = term.indexOf(")");
		String y = term.substring(i+1, j).trim();
		t[index] = numOrIdent(y);
		Term posTerm = new APLFunction(s, t);
		return posTerm;
		
	}


	private Term numOrIdent(String x) {
		Term xt;
		Integer ix = Integer.getInteger(x);
		if (ix != null) {
			xt = new APLNum(ix);
		}
		else {
			xt = new APLIdent(x);
		}
		return xt;
	}


	//from object program
	public Term read(String sAgent, APLFunction call, APLNum timeOut){
	
		try{ 
			TimeEntry te = createEntry(sAgent,call);
			System.out.println("Agent reads: "+te.toString());
			TimeEntry te1 = space.read(te, 2000);
			System.out.println("Agent finds: "+te1.toString());
			return entryToTerm(te1); 
		} catch(Exception e){ e.printStackTrace(); return new APLIdent("null"); }
	}
	
	public Term write(String sAgent, APLFunction call, APLNum lease){ 
		//System.out.println("write " + sAgent);
		try{

			TimeEntry e = createEntry(sAgent,call);
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
	 */
	@Override
	public void addAgent(String sAgent) {
		System.out.println("register " + sAgent);

			register(sAgent);
		
	}
	
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
		Term t = entryToTerm(e);
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
				for (int i=0; i<agents.length;i++) {
					session.addListener(new Position(agents[i]), handler); 
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
