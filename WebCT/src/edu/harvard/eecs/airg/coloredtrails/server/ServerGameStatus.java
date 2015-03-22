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

package edu.harvard.eecs.airg.coloredtrails.server;

import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;

import java.io.Serializable;
import java.util.*;

/**
	<b>Description</b>
	This class is the server's extension of GameStatus and provides several
	important functions.  Methods of note include:
	<p>
	doMove(); this method implements the policy that determines whether a
	requested move will be allowed.  It requires that the move be 1) to a
	neighboring square, 2) that the player has the required chip, and 3) 
	that the player is allowed to move (by checking areMovesAllowed()).
	<p>
	doTransfer(); this method leads to the transfer of chips from one
	player A to another B, provided that A actually possesses the chips to
	be transfered.  Note that this function implements a one-way transfer;
	thus, for a chip exchange to occur, we need two chip transfers (chip
	exchanges are generally two-way).
	<p>
	doDiscourse(); this method relays a discourse message from the sender
	to the receiver.  This method always returns true (i.e., "succeeds").
	
	<p>
	
	<b>Observed By</b>
	This class is observed by the GamesStatusSender class.
	
	<p>
	
	<b>Observes</b>
	This class observes the Board class.  Note that the superclass GameStatus
	does not register itself as an observer of Board; that is done here.
	(This is to isolate ClientGameStatus.)
	
	<p>
	
	<b>Issues</b>
	
	All of the "doXYZ()" methods log the action in the log file, but the
	information being logged needs to be augmented.  For example, the log
	entries do not identify the players involved in the actions.
	<p>
	Only doMove() checks whether the player has permission to take this
	action (by calling .shared.types.PlayerStatus.areMovesAllowed()); the
	methods doTransfer() and doDiscourse() do not check corresponding
	permissions areTransfersAllowed() and isCommunicationAllowed().  The
	UI is where such permissions are enforced.
	
	<p>
	
	<b>Questions</b>
	In doDiscourse(), the discourse message being sent is not directly used
	to build a log entry; rather, another instance of the same type is built
	and used.  It is currently unclear why this is needed.
	
  @author Paul Heymann (ct3@heymann.be)
	@author Sevan G. Ficici (code review and additional comments)
 */
public class ServerGameStatus extends GameStatus
                              implements Serializable, Cloneable
{
//    private GameConfigDetailsRunnable gcdr = null;

    /************************
		CONSTRUCTORS
	************************/
    public ServerGameStatus()
    {
        super("ServerGameStatus");
        getGamePalette().addObserver(this);
    }
	
    // 2-15-2006 Monira : Adding constructos to add an observer for the game pallete
    public ServerGameStatus(String configName, Collection players,
                            int gameID, GamePalette gamePalette)
	{
        super("ServerGameStatus", configName, players, gameID, gamePalette);
        getGamePalette().addObserver(this);
    }
    
    
    public ServerGameStatus(ServerGameStatus gs)
    {
    	super(gs);
    }
    
    public Object clone()
    {
    	return new ServerGameStatus(this);
    }
    

//    public Hashtable<Integer, Semaphore> avoidClobberingGameStatus =
  //          new Hashtable<Integer, Semaphore>();

    /**
     * Make a new board for the game of a given size.
     *
     * @param rows Number of rows in the new board.
     * @param cols Number of columns in the new board.
     */
    public void makeNewBoard(int rows, int cols)
    {
    	// new SGF -- helps garbage collection?
    	if (getBoard() != null)
    		getBoard().deleteObserver(this);
    		
    	setBoard(new Board(rows, cols));
        getBoard().addObserver(this);
    }
    
    /**
     * Set the current board configuration of the game.
     *
     * @param board The board status of the game.
     */
    public void setBoard(Board board)
    {
    	// new SGF -- helps garbage collection?
    	if (getBoard() != null)
    		getBoard().deleteObserver(this);
    		
    	super.setBoard(board);
        getBoard().addObserver(this);
    }

    /**
     * Get the runnable configuration associated with this game.
     *
     * @return The configuration runnable.
     */
    public GameConfigDetailsRunnable getConfigRunnable()
    {
    	return (GameConfigDetailsRunnable)get("gcdr");
//        return gcdr;
    }

    /**
     * Set the configuration runnable associated with this game.
     *
     * @param gcdr The configuration runnable associated with this game.
     */
    public void setConfigRunnable(GameConfigDetailsRunnable gcdr)
    {
    	set("gcdr", gcdr);
//        this.gcdr = gcdr;
    }

    /**
     * Set this game's gameid.
     *
     * @param gameId This game's gameid.
     */
    public void setGameId(int gameId)
    {
    	set("gameId", new Integer(gameId));
        doAllInitializedCheck();
    }

    /**
     * Set the configuration name of this game's configuration.
     *
     * @param configName The new configuration name of this game's
     *                   configuration.
     */
    public void setConfigName(String configName)
    {
    	set("configName", configName);
        doAllInitializedCheck();
    }

    /**
     * Set the players involved in this game.
     *
     * @param al The new list of players participating in this game.
     */
    public void setPlayers(Set<PlayerStatus> al)
    {
        super.setPlayers(al);

//        for (PlayerStatus ps : al)
  //          avoidClobberingGameStatus.put(ps.getPin(), new Semaphore(0));
    }

	// NOTE: I think this code is obviated by JMS (SGF)
    /**
     * Get the semaphore associated with sending data to a given player
     * whose pin is known.
     *
     * @param pin The pin of the player whose semaphore needs to be
     *            gotten.
     * @return The semaphore associated with the player's pin.
     */
/*
    public Semaphore getSemaphore(int pin) {
        while (avoidClobberingGameStatus.get(new Integer(pin)) == null) {
            Utility.sleep(500);
        }

        return (Semaphore) avoidClobberingGameStatus.get(new Integer(pin));
    }
*/

    /**
     * Set the Phases object associated with this game and start it's
     * timer.
     *
     * @param p The new phases object to be associated with the game.
     */
    public void setPhases(Phases p) {
        super.setPhases(p);
	    p.advancePhase();
    }

    /**
     * Get the Phases associated with this game.
     *
     * @return phases
     */
    public ServerPhases getServerPhases() {
        return (ServerPhases) getPhases();  // SGF will cast work now?
    }

    /**
     * Excecute a move if the player involved has the chips and the
     * new position is a neighbor of the current position.
     *
     * @param perGameId The per game id of the player executing the move.
     * @param newpos    The requested new position of that player.
     * @return Whether the move was successful.
     */
    public boolean doMove(int perGameId, RowCol newpos) {
        PlayerStatus ps = getPlayerByPerGameId(perGameId);
        if (!ps.getChips().contains(new ChipSet(getBoard().getSquare(newpos).getColor()))) {
            /* player doesn't have the chips */
            return false;
        }

        if (!RowCol.areNeighbors(ps.getPosition(), newpos)) {
            /* only allow moves to adjacent squares for the moment */
            return false;
        }

        if (!getPlayerByPerGameId(perGameId).areMovesAllowed()) {
            /* don't move if moves NOT allowed */
            return false;
        }

        ps.setChips(ChipSet.subChipSets(ps.getChips(),
                new ChipSet(getBoard().getSquare(newpos).getColor())));
        ps.setPosition(newpos);

        Hashtable<String, Object> logEntryHash =
                new Hashtable<String, Object>();
        logEntryHash.put("type", "move");

        getHistoryLog().
                addHistoryItem(new HistoryEntry(getPhases().getCurrentPhaseName(),
                getPhases().getPhasesElapsed(),
                getPhases().getCurrentSecsElapsed(),
                logEntryHash));

        return true;
    }
	
    /**
		Moves the player according to specified path provided
		1) the agent is allowed to move;
		2) the agent possesses all required chips;
		3) each subsequent position in the path neighbors previous position.
		Moves are all-or-nothing; an alternative policy would be to allow
		the longest feasible prefix of the path.
		
		@param perGameId	The per game id of the player executing the move.
		@param path			The requested new position of that player.
		@return				Whether the move was successful.
		Written by: Monira Sarne
		(minor modification SGF)
	*/
	public boolean doPathMove(int perGameId, Path path)
	{
		PlayerStatus ps = getPlayerByPerGameId(perGameId);  // player's current state
		ChipSet newChipSet = ps.getChips();                 // player's current chips
		RowCol  playerPosition = ps.getPosition();          // player's current position

		System.out.println("begin path move");
		System.out.println("player chipeset" + newChipSet);
		  
		/* check if moves allowed for this player - don't move if moves NOT allowed */
		if (!ps.areMovesAllowed()) {
		//	System.out.println("player is not allowed to move");
			return false;
		}
		System.out.println("player is allowed to move");

		// some checks for each step in the path
		int numSteps = path.getNumPoints() - 1;  // number of steps (NOT points) in the path
		//System.out.println("#steps in path: " + numSteps);

		for (int stepPoint=0; stepPoint<numSteps; stepPoint++)
		{
			  RowCol pathpoint = path.getPoint(stepPoint + 1);  // point at end of this step
			  String ppcolor = getBoard().getSquare(pathpoint).getColor();       // color of the point
			  System.out.println("Step point: " + stepPoint +
								 "  Row: " + pathpoint.row + "  Col: " + pathpoint.col +
								 "  Color: " + ppcolor);
			  
              // check if the player has the required chip
              if (!ps.getChips().contains(new ChipSet(getBoard().getSquare(pathpoint).getColor()))) {
                 System.out.println("Status: player lacks needed chip");
                 return false;
              }
          //    System.out.println("Status: player has needed chip");
			  
              // check if the player's current position is adjacent to the step point
              if (!RowCol.areNeighbors(playerPosition, pathpoint)) {
                  System.out.println("Status: next point is not a neighbor");
                 return false;
              }
    //          System.out.println("Status: next point is a neighbor");
			  
              // advance player to the next position
			  // NOT using PlayerStatus.setPostion() to avoid Observer notifications
              playerPosition = pathpoint;
			  
              // change the chip count
			  // NOT using PlayerStatus.setChips() to avoid Observer notifications
              newChipSet = ChipSet.subChipSets(newChipSet, new ChipSet(ppcolor, 1));
              System.out.println("chipset after step" + newChipSet);
		}

		// update the player's state
		ps.setChips(newChipSet);         // new chipset
		ps.setPosition(playerPosition);  // new position

		// create log
		Hashtable<String, Object> logEntryHash = new Hashtable<String, Object>();
		logEntryHash.put("type", "move");

		getHistoryLog().
			addHistoryItem(new HistoryEntry(getPhases().getCurrentPhaseName(),
                  getPhases().getPhasesElapsed(),
                  getPhases().getCurrentSecsElapsed(),
                  logEntryHash));

		return true;
	}


    /**
     * Transfer chips from one player to another if the player has the
     * necessary chips.
     *
     * @param perGameId   The per game id of the transferer.
     * @param toPerGameId The per game id of the transferee.
     * @param chips       The chips to be transferred.
     * @return Whether the transfer was successful.
     */
    public boolean doTransfer(int perGameId, int toPerGameId,
                              ChipSet chips) {

        PlayerStatus ps = getPlayerByPerGameId(perGameId);
        if (!ps.getChips().contains(chips)) {
            /* player doesn't have the chips */
            System.out.println("player doesn't have the chips");
            return false;
        }

        // MONIRA - the notification is in the ps.setchips
        ps.setChips(ChipSet.subChipSets(ps.getChips(), chips));
        getPlayerByPerGameId(toPerGameId).setChips(ChipSet.addChipSets(
                getPlayerByPerGameId(toPerGameId).getChips(),
                chips));



        // Adding transfer to log
        Hashtable<String, Object> logEntryHash =
                new Hashtable<String, Object>();
        logEntryHash.put("type", "transfer");

        getHistoryLog().
        	addHistoryItem(new HistoryEntry(getPhases().getCurrentPhaseName(),
                getPhases().getPhasesElapsed(),
                getPhases().getCurrentSecsElapsed(),
                logEntryHash));

        //System.out.println(log.toString());
        return true;
    }

    /**
      Relay a discourse message to its target and record it in the history log;
      modified to use DiscourseMessage.clone (SGF)
      
      @param dm The discourse message to be relayed and recorded.
      @return Whether the discourse message was sent.
     */
    public boolean doDiscourse(DiscourseMessage dm)
    {
		ServerData.getInstance().sendDiscourseMessage(this, dm); 
        DiscourseMessage specificMessage = dm.clone();
		
        getHistoryLog().
        	addHistoryItem(specificMessage.toHistoryEntry(
                getPhases().getCurrentPhaseName(),
                getPhases().getPhasesElapsed(),
                getPhases().getCurrentSecsElapsed()));

        for (PlayerStatus p : getPlayers())
            getHistoryLog().
            	addHistoryItem(p.toHistoryEntry(
                       getPhases().getCurrentPhaseName(),
                       getPhases().getPhasesElapsed(),
                       getPhases().getCurrentSecsElapsed()));
		
        return true;
    }

    /**
     * Set the game's status to ended.
     */
    public void setEnded() {
        for (PlayerStatus ps : getPlayers()) {
            ps.setScore(getConfigRunnable().getPlayerScore(ps));
        }
        super.setEnded();
    }
    
    /**
     * Add a new color to the palette of the game using string.
     * The color names are defined in the global color map class.
     *
     * @param color - The name of the color to be added (from the global color class).
     * Written by: Monira Sarne    2-9-2006
     */
    public void addColorToGamePalette (String color) {
      getGamePalette().add(color);
      setChanged();
      notifyObservers("GAME_PALETTE_CHANGE");
    }
    
    /**
     * This sends an arbitrary message to the client which causes and update to the GameStatus object.
     * To use, the client must be modified and the GameStatus object must have an observer which will parse this message
     * @param txtmsg message to send to client, this is passed to the update function of the observer
     */
    public void sendArbitraryMessage(String txtmsg){
        ServerData.getInstance().getClientCommands(getGameId()).sendArbitraryMessage(txtmsg);
    }
    
    public Boolean getClientSubscribed(int pergameId){
        return((Boolean)get("isclientsubscribed." + String.valueOf(pergameId)));
    }
    
    public void setClientSubscribed(int pergameId, Boolean subscribed){
        put("isclientsubscribed." + String.valueOf(pergameId), subscribed);
    }
    
    public int getMagicNumber(){
        return( (Integer)get("magicnumber") );
    }
    
    public void setMagicNumber(int magic){
        set("magicnumber", new Integer(magic));
    }
    
    public void setDataFromController(Serializable data){
        if(data != null)
            set("DataFromController", (Object)data);
    }
    
    /**
     * The controller can send arbitrary data to the server config file, this method accesses
     * that data
     * @return object sent by controller
     */
    public Object getDataFromController(){
        return(get("DataFromController"));
    }

}