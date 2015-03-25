package mwspaces;

/*
	Colored Trails

	Copyright (C) 2006, President and Fellows of Harvard College.  All Rights Reserved.

	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/


import java.util.Set;
import java.util.ArrayList;
import java.util.Hashtable;

import edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsClientImpl;

import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Board;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GamePalette;
import edu.harvard.eecs.airg.coloredtrails.shared.types.HistoryLog;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.RowCol;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Path;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;

import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;

import edu.harvard.eecs.airg.coloredtrails.alglib.ShortestPaths;

import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Goal;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author Arlette van Wissen (Arlette.vanWissen@phil.uu.nl)
 * 
 * A Generic Agent
 * This agent only listens to all game events
 * 
 * This agent prints out the events it receives to
 * standard output
 *
 */

public class GenericAgentImpl implements GenericAgent {

    Logger logger;
	private ColoredTrailsClientImpl client;
	private boolean game_started = false;
	public static boolean game_initialized = false;
    /** chosen path to move to goal */
    private Path chosenPath;
   	/** shortest paths to the goal */
	private ArrayList<Path> shortestPaths;
    /** scoring function of this configuration */
    // TODO: get from config?
    private Scoring scoring;

    // identifier of newest message
    // TODO: make list such that several messages can be received at same time?
    // boolean?
    private ArrayList<Integer> newMessage = new ArrayList();

    /** all received messages */
    private Hashtable messages = new Hashtable();
    Hashtable info = new Hashtable();
    private int counter = 0;
    private int phasecounter = 0;
    private boolean phasechanged = false;

	//private Object communication;

	public GenericAgentImpl() {
        logger = Logger.getLogger(this.getClass().getName());
        logger.log(Level.DEBUG, "[GAI] Working with the BDIExperiment version!");

		client = new ColoredTrailsClientImpl();
		
		client.addDiscourseMessageEventListener(this);
		client.addGameEndedEventListener(this);
		client.addGameInitializedEventListener(this);
		client.addGamePaletteUpdatedEventListener(this);
		client.addGameStartEventListener(this);
		client.addLogUpdatedEventListener(this);
		client.addPhasesAdvancedEventListener(this);
		client.addPhasesUpdatedEventListener(this);
		client.addPlayersUpdatedEventListener(this);
		
	}
	
        
	/** 
	 * Methods overridden from GenericAgent.java
	 */

    /**
     * Ask for all needed chips that the opponent has
     * @param playerpin
     * @return request (ChipSet) requested chips from player
     */
    public ChipSet requestChips(int opponentpin, int opponentid) {
        ClientGameStatus cgs = client.getGameStatus();

        // agent is still missing the following chips (needed):
        ChipSet needed = getChipsNeeded(opponentid);
        logger.log(Level.INFO, "[GAI] Chips Needed: " + needed);

        return needed;
    }

	/**
         * Is called when board has been updated.
         */
	public void boardUpdated(Board b) {
		logger.log(Level.INFO, "[GAI] Board Updated");
	}
	
	public void boardUpdated() {
		logger.log(Level.INFO, "[GAI] Board Updated");
	}


	/**
         * Is called when the game ends
         */
	public void gameEnded() {
		logger.log(Level.INFO, "[GAI] Game Ended");
		System.exit(0);
	}

        /**
         * Is called when the game configuration class' run() method completes
         */
	public void gameInitialized() {
		game_initialized = true; 
		logger.log(Level.INFO, "[GAI] Game Initialized");
	}

	/**
	 * Is called when game palette is updated
	 * @param g The updated GamePalette 
	 */ 
	public void gamePaletteUpdated(GamePalette g){
		logger.log(Level.INFO, "[GAI] GamePalette Updated");
	}
	
	/**
         * Is called when game is started.
         */
	public void gameStarted() {
		game_started = true; 
		logger.log(Level.INFO, "[GAI] Game Started");
	}
	
	/**
	 * called when a Discourse Message is received by server
	 * @param dm Discourse Message received
	 */
	public void onReceipt(DiscourseMessage dm) {
		logger.log(Level.INFO, "[GAI] !!! DiscourseMessage Received !!!");

        // TODO: check whether message is directed to agent?
        
        String type = "";
        int messageId = 0;
      
        BasicProposalDiscussionDiscourseMessage resp;
        BasicProposalDiscourseMessage prop;

        if ((dm instanceof BasicProposalDiscourseMessage) ||
                    (dm instanceof BasicProposalDiscussionDiscourseMessage)) {
             if(dm instanceof BasicProposalDiscussionDiscourseMessage) {
                //Message received is response message
                BasicProposalDiscussionDiscourseMessage response =
                          (BasicProposalDiscussionDiscourseMessage) dm;

               logger.log(Level.INFO, "[GAI] New message is of type RESPONSE");
               logger.log(Level.INFO, "[GAI] Messagetype: " + response.getMsgType());
               type = "response";

               resp = response;
               Boolean accepted = response.accepted();

               info.put("accepted", accepted);
               info.put("response", resp);

               messageId = response.getMessageId();
            }
             else if(dm instanceof BasicProposalDiscourseMessage) {
                // Message received is a proposal message

                // only increase the counter if this is a proposal message,
                // otherwise the messageId does not correspond to the original
                // proposal
                counter++;
                BasicProposalDiscourseMessage proposal =
                                (BasicProposalDiscourseMessage) dm;
                logger.log(Level.INFO, "[GAI] New message is of type PROPOSAL");
                logger.log(Level.INFO, "[GAI] Messagetype: " + proposal.getMsgType());
                type = "proposal";

                prop = proposal;
                
                info.put("proposal", prop);

                messageId = getMessageIdCounter();
            }

            info.put("original", dm); // store the original message
            info.put("type", type);
            
            messages.put(messageId, info);

            this.newMessage.add(messageId);
            logger.log(Level.INFO, "[GAI] New message is now: " + newMessage);
        }
        else {
            logger.log(Level.INFO, "[GAI] Received unidentified message");
        }
    }

    /**
     * Called by 2APL environment.
     * @param int name: the name/pin of the agent who's chipset is asked for
     * @return ChipSet chips: the current chipset of the agent
     */
    public ChipSet getChips(int name) {
        ClientGameStatus cgs = client.getGameStatus();
        PlayerStatus ps = cgs.getMyPlayer();
        ChipSet chips = new ChipSet();

        if (name == ps.getPin()) {
            chips = ps.getChips();
        }
        else {
            Set<PlayerStatus> players = cgs.getPlayers();

            for (PlayerStatus p : players) {
                if (name == p.getPin()) {
                    chips = p.getChips();
                }
            }

        }

        return chips;
    }

    /**
     * called by 2 apl environment
     * @return de column of the position of the agent
     */
    public int getAgentPosCol() {
        RowCol position = client.getPosition();
        int col = position.col;
        //logger.log(Level.INFO, "[GAI] Get AgentPosCol " + col);
        return col;
    }


    /**
     * TODO: can only deal with one opponent...
     * called by 2 apl environment
     * @return de column of the position of an agent
     */
    public int getOpponentPosCol() {
        Set<PlayerStatus> players = client.getPlayers();
        ClientGameStatus cgs = client.getGameStatus();
        PlayerStatus ps = cgs.getMyPlayer();
        int col = -1;

        for (PlayerStatus player : players ) {
            if (player.getPerGameId() != ps.getPerGameId()) {
                col = player.getPosition().col; 
            }
        }

        //logger.log(Level.INFO, "[GAI] Get AgentPosCol " + col);
        return col;
    }

    /**
     * Is called by 2apl environment
     * @return the row of the position of the agent
     */
    public int getAgentPosRow() {
        RowCol position = client.getPosition();
        int row = position.row;
        //logger.log(Level.INFO, "[GAI] Get AgentPosRow " + row);
        return row; 
    }

     /**
     * TODO: can only deal with one opponent...
     * called by 2 apl environment
     * @return de column of the position of an agent
     */
    public int getOpponentPosRow() {
        Set<PlayerStatus> players = client.getPlayers();
        ClientGameStatus cgs = client.getGameStatus();
        PlayerStatus ps = cgs.getMyPlayer();
        int row = -1;

        for (PlayerStatus player : players ) {
            if (player.getPerGameId() != ps.getPerGameId()) {
                row = player.getPosition().row;
            }
        }

        //logger.log(Level.INFO, "[GAI] Get AgentPosCol " + col);
        return row;
    }


    /**
     * Get the current phase the game is in.
     * @return phase
     */
    public String getPhase() {
        ClientGameStatus gs = client.getGameStatus();
        String phase = gs.getPhases().getCurrentPhaseName();
        return phase;
    }

    /**
     * Get the chips the agent need to reach a specific goal.
     * TODO: can only deal with one goal
     * @param goalid
     * @return needed
     */
    public ChipSet getChipsNeeded(int opponentId) {
		ChipSet needed = new ChipSet();

        ClientGameStatus cgs = client.getGameStatus();
        ChipSet newChips = new ChipSet();
        PlayerStatus ps = cgs.getMyPlayer();
        ChipSet myChips = ps.getChips();
        ChipSet opponentNewChips = new ChipSet();
        int score = 0;


			// Get all possible unique exchanges between the players
			ChipSet agentOldChips = cgs.getMyPlayer().getChips();
			ChipSet opponentOldChips = cgs.getPlayerByPerGameId(opponentId).getChips();
			ChipSet sendingChips = new ChipSet();

			System.out.println("Agent Chips: " + agentOldChips);
			System.out.println("Opponent Chips: " + opponentOldChips);

			// search for the chips we need - offer nothing
			Set<ArrayList<ChipSet>> giveNothingExchanges = ChipSet.getAllExchanges(
					sendingChips, cgs.getPlayerByPerGameId(opponentId).getChips());
			System.out.println("Total number of exchanges: " + giveNothingExchanges.size());

			ArrayList<ChipSet> mostBEGiveNothing = null;

			// search all exchanges to see with which ones agent can reach goal
			for(ArrayList<ChipSet> exchange:giveNothingExchanges) {
				
				System.out.println("Search for best exchange for sender");

                ChipSet senderChips = exchange.get(0);
				ChipSet recipientChips = exchange.get(1);

				if (!(recipientChips.isEmpty())) {
					newChips = ChipSet.subChipSets(ChipSet.addChipSets(myChips,
							recipientChips), senderChips);
					
					PlayerStatus psnew = new PlayerStatus(ps);
					psnew.setChips(newChips);


                   // calculate new maximum score
                    BestUse best = new BestUse(cgs, psnew, cgs.getScoring(), 0);
                    int newscore = best.getBestState().getScore();
                    if (newscore >= score) {

                        if (needed.getNumChips() > 0) {
                            if (recipientChips.getNumChips() < needed.getNumChips()) {

                            needed = recipientChips;
                            System.out.println("[GAI] Returning new high score with these chips: " + newscore);
                            score = newscore;
                        }
                        }
                            else {
                                needed = recipientChips;
                                score = newscore; 
                            }
                       
                    }


                }
				else System.out.println(">>RecipientChips is empty");

			}

			System.out.println("Best chipset to ask for: " + mostBEGiveNothing);


        return needed; 
    }

     /**
     * Get the chips the agent need to reach a specific goal.
     * TODO: not finished yet...
     * @param goalid
     * @return ChipSet needed
     */
    public ChipSet getChipsRedundant(String goalid) {
		ChipSet needed = new ChipSet();


        return needed;
    }


    /**
     * Get the GameStatus of the client
     * @return gs
     */
	public ClientGameStatus getGameStatus() {
		ClientGameStatus gs = client.getGameStatus();
		logger.log(Level.INFO, "[GAI] Get GameStatus");
		return gs; 
	}


    
    /**
     * Get the id of a goal
     * @param type
     * @param col
     * @param row
     * @return String id
     */
    public String getGoalId(int type, int col, int row) {
        ClientGameStatus gs = client.getGameStatus();
        RowCol goalloc = new RowCol(row, col);
        Set<Goal> goals = gs.getBoard().getGoals();
        String id = "";

        for (Goal g : goals) {
            if (g.getLocation().equals(goalloc)) {
                id = g.getID();
                break;
            }
        }

        return id;
    }


    /**
     * Is called by 2apl environment
     * Returns the column of the position of the goal
     */
    public int getGoalPosCol(int type) {
        ArrayList<RowCol> position =
                client.getGameStatus().getBoard().getGoalLocations(type);
        int goalcol = position.get(0).col;
        //logger.log(Level.INFO, "[GAI] Get GoalPosCol " + goalcol);
        return goalcol;
    }


   /**
    * Is called by 2apl environment
    * @return int goalrow
    * Returns the column of the position of the goal
    */
    public int getGoalPosRow(int type) {
        ArrayList<RowCol> position =
                client.getGameStatus().getBoard().getGoalLocations(type);
        int goalrow = position.get(0).row;
        
        return goalrow; 
    }
    /**
     * Is called by 2apl environment
     * @param int id
     * @return HashTable messageinfo
     */
    public Hashtable getMessageInfo(int id) {
        Hashtable messageinfo = (Hashtable) this.messages.get(id);
        return messageinfo;
    }

    /**
     * Obtain the counter for the new messages
     * @return count integercount of the messages
     */
    public int getMessageIdCounter() {
        int count =  client.getServerPort() * 10 + counter;
        logger.log(Level.DEBUG, "[GAI] Obtained counter message: " + count);
        return count;

    }


    public ArrayList<Integer> getNewMessageId() {
        return this.newMessage;
    }


    /**
     * Is called by 2APL to get the opponent's id
     * @return int id of the opponent
     * TODO: can only deal with one opponent...
     */
    public int getOpponentId() {
        ClientGameStatus cgs = client.getGameStatus();
        Set<PlayerStatus> players = cgs.getPlayers();
        PlayerStatus ps = cgs.getMyPlayer();
        int id = -1;


        for (PlayerStatus player : players ) {
            if (player.getPerGameId()!=ps.getPerGameId()) {
                // if player has not the same id as my player,
                // this is an opponent!
                id = player.getPerGameId();
                break;
            }
        }

        return id; 
    }


    /**
     * Called by 2APL to get the opponent's pin
     * @return int pin of the opponent
     * TODO: can only deal with one opponent because
     * no arguments are provided
     */
    public int getOpponentPin() {
        ClientGameStatus cgs = client.getGameStatus();
        Set<PlayerStatus> players = cgs.getPlayers();
        PlayerStatus ps = cgs.getMyPlayer();
        int pin = -1;


        for (PlayerStatus player : players ) {
            if (player.getPin()!=ps.getPin()) {
                // if player has not the same pin as my player,
                // this is an opponent!
                pin = player.getPin();
                break;
            }
        }

        return pin;
    }


    /**
     * Return the role of a specific player (proposer or responder)
     */
    public String getRole(int id) {
        String role ="";

        ClientGameStatus cgs = client.getGameStatus();
        Set<PlayerStatus> players = cgs.getPlayers();

        System.out.println("[GAI] Looking for role of agent: " + id);

        for (PlayerStatus player : players) {
            if (player.getPerGameId() == id) {
                role = player.getRole();
            }
        }
        System.out.println("[GAI] Returning role: " + role);
        return role;
    }

    /**
     * Return the score of a specific player
     * @param id
     * @return
     */
    public int getScore(int id) {
        int score = 0;
        ClientGameStatus cgs = client.getGameStatus();
        Set<PlayerStatus> players = cgs.getPlayers();

        for (PlayerStatus player : players) {
            if (player.getPerGameId() == id) {
                score = player.getScore();
            }
        }
        return score; 
    }

    /**
     * Return the score of this agent as it would be when he accepts a given proposal.
     * @param id of the player
     * @param msgid message that contains the proopsal
     * @return the new maximum score if the player accepts
     */

    public int getScoreAfterExchange(int id, int msgid) {
        System.out.println("[GAI] ================ GetScoreAfterExchange===============");
        int newscore = 0;

        ClientGameStatus cgs = client.getGameStatus();
        PlayerStatus ps = client.getGameStatus().getMyPlayer();
        ChipSet oldchips = ps.getChips();
        System.out.println("[GAI] Current chipset player: " + oldchips);
        ChipSet newchips = new ChipSet();

        // retrieve original proposal
        Hashtable info = (Hashtable) messages.get(msgid);
        try {
            // create new chipset
            BasicProposalDiscourseMessage proposal = (BasicProposalDiscourseMessage) info.get("proposal");
            System.out.println("[GAI] Original proposal was:" + proposal );
            newchips = newchips.addChipSets(oldchips, proposal.getChipsSentByProposer());
            newchips = newchips.subChipSets(newchips, proposal.getChipsSentByResponder());
            System.out.println("[GAI] New chipset player would be: " + newchips);
        }
        catch(NullPointerException e) {e.printStackTrace();}

        //pretend player has this chipset
        PlayerStatus psnew = ps;
        psnew.setChips(newchips);

        // calculate new maximum score
        BestUse best = new BestUse(cgs, psnew, cgs.getScoring(), 0);
        System.out.println("[GAI] ===> Best use for chips of this agent: " + best);
        newscore = best.getBestState().getScore() + ps.getScore();
        System.out.println("[GAI] Returning new high score with these chips: " + newscore);

        return newscore;
    }

    /**
     * Calculate the new score of this agent if he were to get these chips
     * TODO: should be able to deal with given chips as well!
     * @param id
     * @param requestedchips
     * @return
     */

    public int getScoreAfterExchange(int id, ChipSet requestedchips) {

        ClientGameStatus cgs = client.getGameStatus();
        PlayerStatus ps = client.getGameStatus().getMyPlayer();
        ChipSet oldchips = ps.getChips();
        System.out.println("[GAI] Current chipset player: " + oldchips);
        ChipSet newchips = new ChipSet();
        newchips = newchips.addChipSets(oldchips, requestedchips);

        //pretend player has this chipset
        PlayerStatus psnew = ps;
        psnew.setChips(newchips);

        // calculate new maximum score
        BestUse best = new BestUse(cgs, psnew, cgs.getScoring(), 0);
        System.out.println("[GAI] ===> Best use for chips of this agent: " + best);
        int newscore = best.getBestState().getScore() + ps.getScore();
        System.out.println("[GAI] Returning new high score with these chips: " + newscore);

        return newscore;

    }


    /**
     * Return the score the player would obtain if using his current
     * chipset to reach the goal.
     * @param id
     * @return the maximum score of this player with his own chips
     */
    public int getScoreCurrentChips(int id) {
        int newscore = 0;
        ClientGameStatus cgs = client.getGameStatus();
        PlayerStatus ps = client.getGameStatus().getMyPlayer();

        BestUse best = new BestUse(cgs, ps, cgs.getScoring(), 0);
        System.out.println("[GAI] ===> Best use for chips of this agent: " + best);
        newscore = best.getBestState().getScore() + ps.getScore();
        System.out.println("[GAI] Returning new high score with these chips: " + newscore);

        return newscore;
    }




     /**
     * @return String serverhostname
     */
	public String getServerHostname() {
		String name = client.getServerHostname();
		logger.log(Level.INFO, "[GAI] ServerHostname: " + name);
		return name; 
	}
	 
	/**
         * Get server port
         * @return int serverport
         */
	public int getServerPort() {
		int port = client.getServerPort();
		logger.log(Level.INFO, "[GAI] ServerPort: " + port);
		return port;
	}
	           
	/*
	 * Tell the client that the history log has been updated
	 * @param hl History log that has been updated 
	 */ 
	public void logUpdated(HistoryLog hl) {
		logger.log(Level.INFO, "[GAI] HistoryLog Updated: " + hl);
	}


    /**
     * * BROKEN!
     * Create a proposal and send this to responder
     * @param responderpin
     */
    public void makeProposal(int responderpin) {
    }

    /**
     * move one step to the nearest goal
     * NOTE: does not use the goalid yet...
     * TODO: what if player cannot move? is this handled by
     * the environment (throwing an exception)?
     * @param ax
     * @param ay
     * @param goalid
     * @return
     */
    public boolean moveStepToGoal(int ax, int ay, String goalid) {
        boolean upToDate;

        // Check whether environment hasn't changed since last APL update
        int agentx = client.getPosition().row;
        int agenty = client.getPosition().col;

        if ((agentx == ax) && (agenty == ay)) {
                upToDate = true;
                logger.info("[GA] Positions of agent match 2APL");
            }
            else {
            logger.info("[GA] Position of Agent does NOT match 2APL");
            upToDate = false;
            return upToDate;
            }

        // Position of agent and goal are upToDate, continue move
        ClientGameStatus cgs = client.getGameStatus();
        scoring = cgs.getScoring();
        shortestPaths = ShortestPaths.getShortestPathsToFirstGoal(cgs.getMyPlayer().getPosition(),
					cgs.getBoard(), scoring);
        logger.info("[GA] Shortest Paths: " + shortestPaths);

        // Get the best path available
        chosenPath = shortestPaths.remove(0); // why remove(0)??
        logger.info("[GA] Chosen Path: " + chosenPath);

        // Send move request
		client.communication.sendMoveRequest(chosenPath.getPoint(1));
        logger.info("[GA] Move step to goal to " + chosenPath.getPoint(1));

        return upToDate;
    }



	/**
	 * Is called when a phase is advanced
	 * @param ph advanced phase
	 */
    public void phaseAdvanced(Phases ph) {
	logger.log(Level.INFO, "[GAI] Phase Advanced: " + ph);
        logger.log(Level.INFO, "[GAI] PhaseCounter was: " + phasecounter);
        phasecounter++;
        phasechanged = true;
        logger.log(Level.INFO, "[GAI] Incremented PhaseCounter to: " + phasecounter);
    }

    /**
     * Check if the phase has changed or not
     * @return boolean
     */
    public boolean phaseChanged() {
        if (phasechanged) {
            phasechanged = false;
            return true;
        }
        return false;
    }

    /**
     * Returns the number of phases 
     * @return int phasecounter
     */
    public int getPhaseCounter() {
        logger.log(Level.INFO, "[GAI] getPhaseCounter() called. Phasecounter is: " + phasecounter);
        return phasecounter;
    }

    /**
     * Called when phases are updated
     *  @param ph updated phases
     */
    public void phasesUpdated(Phases ph) {
       logger.log(Level.INFO, "[GAI] Phases Updated");
    }
	
    /**
     * Called when players are updated
     *  @param ps new Playerstatus
     */
    public void playersUpdated(Set<PlayerStatus> ps) {
	logger.log(Level.INFO, "[GAI] Players Updated");
    }

    /**
     * Called when one player is updated.
     * @param ps
     */
    public void playersUpdated(PlayerStatus ps) {
	logger.log(Level.INFO, "[GAI] Players Updated");		
    }

    /**
     * Reset Messages.
     */
    public void resetNewMessages() {
        newMessage.clear();
    }

	
    /**
     * called to set name of serverhost
     * @param String name of serverhost
     */
    public void setServerHostName(String name) {
        logger.log(Level.INFO, "[GAI] ServerHostName: " + name);
    }
	
    /**
     * called to set serverport
     * @param int port of server
     */
    public void setServerPort(int port) {
        logger.log(Level.INFO, "[GAI] ServerPort: " + port);
    }
	
    /**
     * Is called to start client
     */
    public void start() {
        client.start();
	logger.log(Level.INFO, "[GAI] Client Started");
    }

     /**
     * Called by 2APL to get the agents id
     * @return int id of player
     */
    public int getAgentId() {
        ClientGameStatus cgs = client.getGameStatus();
        PlayerStatus ps = cgs.getMyPlayer();

        return ps.getPerGameId();
    }


    /**
     * Called by 2APL to get the agents pin
     * @return int pin of playerstatus
     */
    public int getAgentPin() {
        ClientGameStatus cgs = client.getGameStatus();
        PlayerStatus ps = cgs.getMyPlayer();

        return ps.getPin();
    }


    /**
     * * BROKEN PROPOSAL MESSAGE
     * Send a proposal to a specified player. 
     * @param pin The pin of the player to whom this proposal will be sent. 
     * @param chips The proposal (requested chips)
     * @return the messageId of the sent proposal
     * TODO: enable sending chips instead of only receiving
     */

    public int sendProposal(int pin, ChipSet chips) {
        System.out.println("[GAI] Trying to send proposal: " + chips + " to " + pin);
        counter++;
        ClientGameStatus cgs = client.getGameStatus();
        ChipSet send = new ChipSet(); // send an empty chipset!  

        int proposerId = cgs.getPerGameId();

	int responderId = -1;
        
        Set<PlayerStatus> players = cgs.getPlayers();

        // TODO: make responderId more flexible
        for (PlayerStatus player : players) {
            if (player.getPerGameId()!=proposerId) {
                responderId = player.getPerGameId();
            }
        }

        int messageId = -1;

        logger.log(Level.INFO, "[GAI] Id for this message (proposal): " + messageId);

	BasicProposalDiscourseMessage proposal= new BasicProposalDiscourseMessage(
            proposerId, responderId, messageId, send, chips);

        logger.log(Level.INFO, "[GAI] And if we request the messageID we get: " + proposal.getMessageId());

        logger.log(Level.INFO, "[GAI] Messagetype: " + proposal.getMsgType());
        String type = "proposal";

        info.put("proposal", proposal);
        info.put("type", type);

        messages.put(messageId, info);

		client.communication.sendDiscourseRequest(proposal);

        return proposal.getMessageId();
    }

 
   /**
    * Sends a response to a proposal
    * @param String r
    * @param int messageid
    */
    public void sendResponse(String r, int messageid) {
       // retrieve original proposal
        Hashtable info = (Hashtable) messages.get(messageid);
        DiscourseMessage dm = (DiscourseMessage) info.get("original");

        // retrieve original proposal
        BasicProposalDiscourseMessage originalproposal =
                                (BasicProposalDiscourseMessage) dm;

        // create message for response
        BasicProposalDiscussionDiscourseMessage response =
			    new BasicProposalDiscussionDiscourseMessage( originalproposal );

   
        if (r.equals("reject")) {
            response.rejectOffer();
            logger.log(Level.INFO, "[GAI] Rejected offer!");
        }
        else if (r.equals("accept")) {
            response.acceptOffer();
            logger.log(Level.INFO, "[GAI] Accepted offer!");
        }

        client.communication.sendDiscourseRequest(response);

    }


    /**
     * Called to set the client name to the string argument name
     * @param name New name of the client
     */
    public void setClientName(String name) {
        client.setPin(name);
	logger.log(Level.INFO, "[GAI] Client Name: " + name);
    }

    /**
     * Get the name of the client
     * @return String pin
     */
    public String getClientName() {
        return client.getPin();
    }

}