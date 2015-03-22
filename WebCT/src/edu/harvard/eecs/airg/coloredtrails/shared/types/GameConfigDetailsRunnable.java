/*
	Colored Trails
	
	Copyright (C) 2006-2008, President and Fellows of Harvard College.  All Rights Reserved.
	
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

package edu.harvard.eecs.airg.coloredtrails.shared.types;

import edu.harvard.eecs.airg.coloredtrails.server.ServerData;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.server.ServerGameStatus;
import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.shared.types.CTStateContainer;


import java.io.Serializable;
import java.util.Set;

/**
<b>Description</b>
Base class for configuration classes.  This class must be extended by 
experimenter when designing a new game scenario.
<p>
Each instance of this class has a gameID field, used to associate the 
configuration class instance with a GameStatus class instance.
<p>
This class provides default implementations of the following methods: 
doMove(), doTransfer(), doDiscourse(), and getPlayerScore().
<p>
[move, transfer, and discourse methods return a boolean indicating success 
of action -- DESCIBE where and how are these boolean values actually are generated.  
how can these actions fail?  LOOK at GameStatus object.]

<p>

<b>Issues</b>
INCOMPLETE CODE:  getPlayerScore() only returns zero; it is not functional.
The Scoring class is used to address player scores.

<p>

<b>Original Summary</b>
 * A configuration details runnable which defines a colored trails game.
 * This is extended by any configuration which runs a colored trails game.
 *
 * @author Paul Heymann (ct3@heymann.be)
   @author Sevan G. Ficici (modifications for partial visibility)
   @author Monira Sarne 3-8-06 Add an option to run an automatic transfer 
   of the chips once the recipient accept the offer.
 */
abstract public class GameConfigDetailsRunnable implements Serializable, Runnable {

    //private int gameid = -1;
    // Set this variable to true if you would like to
    // have the ActionHistory Window present.
    private static boolean HISTORYWINDOW = true;
    private static boolean PATHWINDOW = false;

    public static boolean historyWindowIsVisible() {
        return HISTORYWINDOW;
    }

    public static void setHistoryWindowVisible(boolean b) {
            HISTORYWINDOW = b;
    } 

    public static void setPathFinderVisible(boolean b) {
    	PATHWINDOW = b;
    }

    public static boolean pathFinderIsVisible() {
        return PATHWINDOW;
    }
    
    abstract public void run();

    /**
     * Get the gameid that the server has assigned this game config at
     * runtime.
     *
     * @return The gameid that the server has assigned this game config
     *         for this particular run.
     */
    //public int getGameId() {
    //    return gameid;
    //}

    /**
     * Set the gameid that the server has assigned this game config at
     * runtime.
     *
     * @param gameid The gameid that the server has assigned this game config
     *               for this particular run.
     */
    //public void setGameId(int gameid) {
    //    this.gameid = gameid;
    //}

    /**
     * Do a move: this is by default forwarded to a default mechanism
     * in ServerData, but the configuration can modify this behavior
     * here.
     *
     * @param perGameId The per game id of the player doing the move.
     * @param newpos    The new position that the player wishes to move to.
     * @return Whether the action was successful.
     */
    public boolean doMove(int perGameId, RowCol newpos) {
        return gs.doMove(perGameId, newpos);
    }

    /**
     * Do a transfer: this is by default forwarded to a default mechanism
     * in ServerData, but the configuration can modify this behavior
     * here.
     *
     * @param perGameId   The per game id of the transferer.
     * @param toPerGameId The per game id of the transferee.
     * @param chips       The chips to be transferred.
     * @return Whether the action was successful.
     */
    public boolean doTransfer(int perGameId, int toPerGameId,
                              ChipSet chips) {
        return gs.doTransfer(perGameId, toPerGameId, chips);
    }

    /**
     * Relay a discourse message: this is by default forwarded to a
     * default mechanism in ServerData, but the configuration can modify
     * this behavior here.
     *
     * @param dm The discourse message to be relayed.
     * @return Whether the action was successful.
     */
    public boolean doDiscourse(DiscourseMessage dm) {

         return gs.doDiscourse(dm);

    }

    /**
     * Get a player's current score based on the game state.
     *
     * @return The score of the player at the current time.
     */
    public abstract int getPlayerScore(PlayerStatus ps);// {
//        return 0;
 //   }
    
    private boolean doAutomaticChipTransfer(int proposerID,
                                           int responderID,
                                           ChipSet chipsSentByProposer,
                                           ChipSet chipsSentByResponder){
        System.out.println( "Initiating Automatic Chip Transfer" );
        System.out.println( "Proposer:  " + proposerID );
        System.out.println( "Sending: " + chipsSentByProposer );
        System.out.println( "Responder: " + responderID );
        System.out.println( "Sending: " + chipsSentByResponder );


        // do transfer method - Checks if the sender and recipient has chips to transfer
		// but we need to know before the transfer if both players has enough chips to transfer
		if (gs.getPlayerByPerGameId(proposerID).getChips().contains(chipsSentByProposer) &&
            gs.getPlayerByPerGameId(responderID).getChips().contains(chipsSentByResponder)) {
			System.out.println("EXECUTING AUTOMATIC CHIP TRANSFER");
			// transfer the chips from the sender
			doTransfer(proposerID, responderID, chipsSentByProposer);
			// transfer the chips from the recipient
			doTransfer(responderID, proposerID, chipsSentByResponder);
			  
			//No reason for the phase to automatically advance phase              
			//gs.getServerPhases().advancePhase();
			return true;
        }
        else {
            System.out.println("Not enough chips to transfer");
        }
        return false;

    }

    public boolean doAutomaticChipTransfer(BasicProposalDiscussionDiscourseMessage bpddm){

        boolean result = false;

        if(bpddm.accepted()) {
            result = doAutomaticChipTransfer(
                    bpddm.getProposerID(),
                    bpddm.getResponderID(),
                    bpddm.getChipsSentByProposer(),
                    bpddm.getChipsSentByResponder());
        }
        
        return result;
    }
    
      public void doAutomaticMovement( Scoring s ) {    	
    	doAutomaticMovement(gs, s);
    }
    
    public void doAutomaticMovement(ServerGameStatus sgs, Scoring s ) {    	
    	System.out.println("AUTOMATIC MOVEMEMT");
		
		// for each player in game, make best use of their chips and move accordingly
		for (PlayerStatus ps : sgs.getPlayers()) {
			ps.setMovesAllowed(true);				// allow movement
			BestUse bu = new BestUse(sgs, ps, s, ps.getPerGameId());  // calculate the best use of player's chips
			Path p = bu.getPaths().get(0);           // use the first best-path in list
			sgs.doPathMove(ps.getPerGameId(), p);     // move the player
			System.out.println(bu);                  // print BU instance to console
			System.out.println(p);                   // print path to console
		}
    }

    protected ServerGameStatus gs;

    public void setCurrentServerGameStatus( ServerGameStatus gs ) {
        this.gs = gs;    
    }
    
    /**
      Filter CTStateContainer class before it is sent out to player;
      override this method if you want to include filtering
    */
	public Board filterBoard(Board fromserver, int perGameId)
	{
		return fromserver;
	}
	
	public Set<PlayerStatus> filterPlayerStatus(Set<PlayerStatus> fromserver, int perGameId)
	{
		return fromserver;
	}
	
	// probably don't want to filter game phase information, but it is
	// nevertheless conceivable that we'd want uncertainty about what phase
	// we're in or how long it will last
	public Phases filterPhases(Phases fromserver, int perGameId)
	{
		return fromserver;
	}

	// probably don't want to filter game palette, but here just in case
	public GamePalette filterGamePalette(GamePalette fromserver, int perGameId)
	{
		return fromserver;
	}	
     
    //this is called by the server to request that the config file ends the game
    //what this means in config file-specific
    /**
     * This is called when the controller requests that the game end. Implementation is config file-dependent
     */
    public void requestGameEnd(){
    
    }
}