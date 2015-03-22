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

import java.util.Hashtable;
import java.io.Serializable;

/**
<b>Description</b>

Represents the basic state information of a single player in a particular CT3 game.
<p>
This class allows for rudimentary representation of what a player can and cannot do.  
In particular, the booleans 'movesAllowed', 'transfersAllowed', and 'communicationAllowed' 
provide coarse control over the agent's permissions.
<p>
[it is yet unclear how these booleans are actually used to determine whether a 
player will be able to take some action; for example, are these booleans used by 
the game configuration class to decide what a player can and cannot do?]

<table width="100%" cellpadding=10 bgcolor=silver>
    <tr><td>Method</td><td>Used By</td></tr>
    <tr><td>areMovesAllowed()</td><td>.client.ClientGameStatus.myPlayerCanMove(), 
                                    which is used by .client.ui.BoardPanel.drawPlayers()
                                    and .BoardPanel.PanelBoardMouseListener.mouseReleased()</td></tr>
    <tr><td>areMovesAllowed()</td><td>.server.ServerGameStatus.doMove()</td></tr>
</table>


<p>

<b>Observers</b>
GameStatus, [others?]

<p>

<b>Notifications</b>
<table width="100%" cellpadding=10 bgcolor=silver>
    <tr><td>Method</td><td>Message Sent</td></tr>
    <tr><td>setScore()</td><td>PLAYER_CHANGED</td></tr>
    <tr><td>setMovesAllowed()</td><td>PLAYER_CHANGED</td></tr>
    <tr><td>setChips()</td><td>PLAYER_CHANGED</td></tr>
    <tr><td>setPosition()</td><td>PLAYER_CHANGED</td></tr>
</table>

<p>

<b>Issues</b>
Several 'set' methods do not cause notifications to be sent.  These are:
<ul>
<li>public void setTransfersAllowed(boolean transfersAllowed)</li>
<li>public void setCommunicationAllowed(boolean communicationAllowed)</li>
<li>public void setPerGameId(int perGameId)</li>
<li>public void setPin(int pin)</li>
</ul>

The first two methods above likely need correction.  The purpose of the 
latter two 'set' methods is less clear; 'setPin' in particular is surprising, 
as this value is set in the constructor and there appears to be no circumstance 
under which this should change for a player.  The 'setPerGameId' is similarly 
worth considering; the relationship between the game ID and the concept of a 
role needs to be clarified.  Is the game ID a short-hand for a particular setting 
of permissions and prohibitions that defines a role? If a player's role can change 
during a game, then would its ID change as well?  While the notion of a role is 
clear in simple games, we can imagine that more complex scenarios may lead to a 
combinatorial explosion of permissions and prohibitions, potentially making the 
concept of a role tenuous.
<p>
Alternatively, the game ID may exist simply to ease record keeping, and not have 
any deep relationship to player permissions.  In this case, the need for the 
'setPerGameId' method is unclear, given that this ID is provided to the class constructor.
<p>
The 'score' field appears to represent the outcome of a single game; we also need
a way to represent cumulative scores of an player over an experiment.  (Need to verify
that a PlayerStatus instance persists for a player over an entire experiment.)
<p>
The 'score' field is not updated by the system, for example, when a player moves
or trades chips; this calculation of score is most flexibly left to the 
GameConfigDetailsRunnable subclass that defines the game

<p>

<b>Future Development</b>
We can provide finer-grained representation of permissions in this class.  For example, 
we can specify to whom transfers are allowed, and with whom communication is allowed.  
Such enhancements provide the essential requirements for roles.

<p>

 *
 * @author Paul Heymann (ct3@heymann.be)
   @author Sevan G. Ficici (modifications for partial visibility)
 */
public class PlayerStatus extends CTStateContainer
                          implements Serializable, Cloneable
{
	
	
    public PlayerStatus()
    {
		super("PlayerStatus", "Server");  // server's encoding of the player state
		// the role of this player; note initialization
		set("role", "");
		// the ID of this player in current game; note initialization
		set("perGameId", new Integer(-1));
		// the global/persistent ID of this player; note initialization
		set("pin", new Integer(-1));
		// position of player's piece on board; note initialization
		set("position", new RowCol(-1, -1));
		// player's chips
		set("chips", new ChipSet());
		// whether player is allowed to move on board
		set("movesAllowed", Boolean.FALSE);
		// whether player is allowed to transfer chips
		set("transfersAllowed", Boolean.FALSE);
		// whether player is allowed to send messages
		set("communicationAllowed", Boolean.FALSE);
		// team ID of this player; note initialization
		set("teamId", new Integer(-1));
		// player's score for this game (not cumulative over all games?)
		set("score", new Integer(0));
		//chips to be shown on the GUI (only for the WebCT client)
		set("revelationChips", new ChipSet());
		
						// hide goal from opponents
		set("goalVisible", Boolean.TRUE);
		
				// whether player is allowed to reveal its goal
		set("revelationAllowed", Boolean.FALSE);
    }

    public PlayerStatus(int pin)
    {
    	this();
    	put("pin", new Integer(pin));
    }

    public PlayerStatus(int perGameId, int pin)
    {
    	this(pin);
    	put("perGameId", perGameId);
    }
    
    public PlayerStatus(PlayerStatus p)
    {
    	super(p);
    }
    
    public Object clone()
    {
    	return new PlayerStatus(this);
    }
	
	// WE SHOULD MAKE SCORES double!
    /**
     * Get the score of the player in the current game.
     *
     * @return The score of the player in the current game.
     */
    public int getScore()
    {
    	return (Integer)get("score");
    }

    /**
     * Set the score of the player in the current game.
     *
     * @param score The score of the player in the current game.
     */
    public void setScore(int score)
    {
    	put("score", new Integer(score));
        setChanged();
        notifyObservers("PLAYER_CHANGED");
    }

    /**
     * Determine if the player is currently allowed to transfer chips.
     *
     * @return Whether the player is currently allowed to transfer chips.
     */
    public boolean areTransfersAllowed()
    {
    	return (Boolean)get("transfersAllowed");
    }

    /**
     * Set whether the player is currently allowed to transfer chips.
     *
     * @param transfersAllowed Whether the player is currently allowed to
     *                         transfer chips.
     */
    public void setTransfersAllowed(boolean transfersAllowed)
    {
    	set("transfersAllowed", new Boolean(transfersAllowed));
        //MONIRA
        setChanged();
        notifyObservers("PLAYER_CHANGED");
    }

    /**
     * Determine if the player is allowed to send discourse messages.
     *
     * @return Whether the player is allowed to send discourse messages.
     */
    public boolean isCommunicationAllowed()
    {
    	return (Boolean)get("communicationAllowed");
    }

    /**
     * Set whether the player is allowed to send discourse messages.
     *
     * @param communicationAllowed Whether the player is allowed to send
     *                             discourse messages.
     */
    public void setCommunicationAllowed(boolean communicationAllowed)
    {
    	set("communicationAllowed", new Boolean(communicationAllowed));
        // MONIRA
        setChanged();
        notifyObservers("PLAYER_CHANGED");
    }

	 /**
     * Determine if the player is allowed to reveal its goal.
     *
     * @return Whether the player is allowed to reveal its goal.
     */
    public boolean isRevelationAllowed() {
    	return (Boolean)get("revelationAllowed");
    }

    /**
     * Set whether the player is allowed to reveal its goal.
     *
     * @param communicationAllowed Whether the player is allowed to reveal its goal.
     */
    public void setRevelationAllowed(boolean revelationAllowed) {
    	set("revelationAllowed", new Boolean(revelationAllowed));
        setChanged();
        notifyObservers("PLAYER_CHANGED");
    }

	
    /**
     * Determine if the player is allowed to move.
     *
     * @return Whether the player is allowed to move.
     */
    public boolean areMovesAllowed()
    {
    	return (Boolean)get("movesAllowed");
    }

    /**
     * Set whether the player is allowed to move.
     *
     * @param movesAllowed Whether the player is allowed to move.
     */
    public void setMovesAllowed(boolean movesAllowed)
    {
    	set("movesAllowed", new Boolean(movesAllowed));
        setChanged();
        notifyObservers("PLAYER_CHANGED");
    }

    /**
     * Get the chips allocated to the player.
     *
     * @return The chips allocated to the player.
     */
    public ChipSet getChips()
    {
    	return (ChipSet)get("chips");
    }

    /**
     * Set the chips allocated to the player.
     *
     * @param chips The chips allocated to the player.
     */
    public void setChips(ChipSet chips)
    {
    	set("chips", chips);
        setChanged();
        notifyObservers("PLAYER_CHANGED");
    }

    /**
     * Get the current position of the player on the board.
     *
     * @return The current position of the player on the board.
     */
    public RowCol getPosition()
    {
    	return (RowCol)get("position");
    }
    
    /**
     * Get the chips to be shown on the GUI
     * @return the revelation ChipSet (chips to be displayed on the WebCT GUI) of the player
     */
    public ChipSet getRevelationChips() {
    	return (ChipSet)get("revelationChips");
    }

    /**
     * Set the current position of the player on the board.
     *
     * @param position The current position of the player on the board.
     */
    public void setPosition(RowCol position)
    {
    	set("position", position);
        setChanged();
        notifyObservers("PLAYER_CHANGED");
    }
    
    /**
     * Returns role
     * @return
     */
    public String getRole()
    {
    	return (String)get("role");
    }
    
    /**
     * Sets the player's role. This should probably be in ServerGameStatus
     * If set, this causes a field to appear in the GUI informing the client of the role
     * Agents should query it on their own
     * @param role Experimenter-defined role
     */
    public void setRole(String role)
    {
    	set("role", role);
    	setChanged();
        notifyObservers("PLAYER_CHANGED");
    }

    /**
     * Get the per game id of the player.
     *
     * @return The per game id of the player.
     */
    public int getPerGameId()
    {
    	return (Integer)get("perGameId");
    }

    /**
     * Set the per game id of the player.
     *
     * @param perGameId The per game id of the player.
     */
    public void setPerGameId(int perGameId)
    {
    	set("perGameId", new Integer(perGameId));
		// presumably, we want to send update for this
    	setChanged();
        notifyObservers("PLAYER_CHANGED");
    }

    /**
     * Gets the teamId of the player
     * @return teamId of the player
     */
    public int getTeamId()
    {
    	return (Integer)get("teamId");
    }

    /**
     * Sets the teamId of the player to the argument teamId
     * @param teamId New teamId of the player
     */
    public void setTeamId(int teamId)
    {
    	set("teamId", new Integer(teamId));
    }

    /**
     * Get the PIN of this player.
     *
     * @return The PIN of this player.
     */
    public int getPin()
    {
    	return (Integer)get("pin");
    }

    /**
     * Set the PIN of this player.
     *
     * @param pin The PIN of this player.
     */
    public void setPin(int pin)
    {
    	set("pin", new Integer(pin));
    }
    
    /**
     * Set the chips to be displayed on the GUI
     * IMPORTANT: only for the WebCT GUI!!!!!!!
    */
    public void setRevelationChips(ChipSet cs) {
    	set("revelationChips", cs);
    	setChanged();
        notifyObservers("PLAYER_CHANGED");
    }
	
	    /**
     * Determine if the player is allowed to reveal its goal.
     *
     * @return Whether the player is allowed to reveal its goal.
     
    public boolean isRevelationAllowed() {
		System.out.println("i'm here");
    	return (Boolean)get("revelationAllowed");
    }
*/
    /**
     * Set whether the player is allowed to reveal its goal.
     *
     * @param communicationAllowed Whether the player is allowed to reveal its goal.
  
    public void setRevelationAllowed(boolean revelationAllowed) {
	System.out.println("i'm here 2");
    	set("revelationAllowed", new Boolean(revelationAllowed));
        setChanged();
        notifyObservers("PLAYER_CHANGED");
    }
	   */
	   
	   
	   
	    /**

     * Get the GOAL VISIBILITY of this player.
     *
     * @return The GOAL VISIBILITY of this player.
     */
    public boolean isGoalVisible()
    {
    	return (Boolean)get("goalVisible");
    }


    /**
     * Set the GOAL VISIBILITY of this player.
     *
     * @param goalVisible The GOAL VISIBILITY of this player.
     */
    public void setGoalVisible(boolean goalVisible)
    {
    	set("goalVisible", new Boolean(goalVisible));
        setChanged();
        notifyObservers("PLAYER_CHANGED");
    }
	   
	   
	   
	/*   
	 public int stam()
	 {
	 return 1;
	}
	*/
    public String toString()
    {
        return "Game Player...\n" +
                "PerGameId: " + getPerGameId() + ".\n" +
                "PIN: " + getPin() + ".\n" +
                "Pos: " + getPosition().toString() + ".\n" +
                "Chips: " + getChips().toString() + ".\n" +
                "Score: " + getScore() + ".\n" +
                "Team: " + getTeamId() + ".\n";
    }
    
    /**
     * Return a history entry describing this player status suitable
     * for adding to the history log.
     *
     * @param phaseName        The name of the phase when this discourse message
     *                         was sent.
     * @param phaseNum         The phase number of the phase when this discourse
     *                         message was sent.
     * @param secondsIntoPhase How many seconds into the current phase
     *                         the discourse message was sent.
     * @return A new HistoryEntry describing the player status.
     */
    public HistoryEntry 
   		toHistoryEntry(String phaseName, int phaseNum, int secondsIntoPhase)
	{
        Hashtable<String, Object> entry = new Hashtable<String, Object>();
        entry.put("type", "playerStatus");
        entry.put("PerGameId", getPerGameId());
        entry.put("Position", getPosition());
        entry.put("Score", getScore());
        entry.put("ChipSet", getChips());
		
        return new HistoryEntry(phaseName, phaseNum, secondsIntoPhase, entry);
    }
}