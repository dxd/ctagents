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

package edu.harvard.eecs.airg.coloredtrails.client;

import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GamePalette;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.io.Serializable;
import java.util.Set;

/**
	<b>Description (original)</b>
 * A client version of the shared GameStatus type, featuring some
 * client-specific game data.
 
	<p>
	
	<b>Questions</b>
	Unclear why the second two constructors are needed, as they
	simply call the superclass constructor without doing extra work.
	It may be that these are needed for some reason.  The first 
	constructor is required for serialization?
	<p>
	What is the point of the protected method "playerHashesToPlayersVector"
	when all it does is call the public static method "hashesToPlayers"?
	It appears that it is simply to override the superclass version of this 
	method and change the type of vector elements.  Thus, the question
	might then be, What's the point of the public static method?
	<p>
	Purpose of setPlayers method is unclear, as it doesn't appear to
	do anything super.setPlayers() doesn't do, and indeed this method
	appears to repeat the setChanged and notifyOberservers calls that
	are found at the end of super.setPlayers(). Are these repeat calls 
	actually redundant, or do they serve some purpose?
	<p>
	Purpose of getClientPlayers method is unclear at the moment.  This
	method simply converts the ArrayList<PlayerStatus> to an
	ArrayList<ClientPlayerStatus> -- need to see what the differences
	between these classes are.
	<p>
	Purpose of getClientPhases method is unclear; it merely casts 
	super.phases from Phases to ClientPhases -- need to find the 
	differences between these classes.
	
	<p>
	
	<b>Notifications</b>
	<table width="100%" cellpadding=10 bgcolor=silver>
    <tr><td>Method</td><td>Message Sent</td></tr>
	<tr><td>setPlayers(...)</td><td>PLAYERS_CHANGED</td></tr>
    <tr><td>notifyObserversPhaseAdvanced(...)</td><td>NEXT_PHASE</td></tr>
	</table>

	Who calls notifyObserversPhaseAdvanced?  Also, see note above about apparent 
	redundancy of setPlayers method.
 
 * @author Paul Heymann (ct3@heymann.be)
	@author Sevan G. Ficici (additional commentary)
 */
public class ClientGameStatus extends GameStatus
                              implements Serializable, Cloneable
{
//    private int perGameId;

	public ClientGameStatus()
	{
		super("ClientGameStatus");
    }
	
	
    // 2-9-2006 Monira : Adding game pallete 
    public ClientGameStatus(String configName, Collection players,
                            int gameID, GamePalette gamePalette)
	{
        super("ClientGameStatus", configName, players, gameID,gamePalette);
    }

    public ClientGameStatus(GameStatus gs)
    {
        super(gs);
    }
    
    public Object clone()
    {
    	return new ClientGameStatus(this);
    }
	
	
    /**
     * Get a list of all players in the game as a list of
     * ClientPlayerStatus objects.
     *
	 <p>
	 (sgf) Purpose of this method is unclear at the moment.  This
	 method simply converts the ArrayList<PlayerStatus> to an
	 ArrayList<ClientPlayerStatus> -- need to see what the differences
	 between these classes are.
	 
     * @return All players in the game as a vector of ClientPlayerStatus
     *         objects.
     */
    /**
     * Get a list of all players in the game as a list of
     * ClientPlayerStatus objects.
     *
	 <p>
	 (sgf) Purpose of this method is unclear at the moment.  This
	 method simply converts the ArrayList<PlayerStatus> to an
	 ArrayList<ClientPlayerStatus> -- need to see what the differences
	 between these classes are.
	 
     * @return All players in the game as a vector of ClientPlayerStatus
     *         objects.
     */
    public ArrayList<ClientPlayerStatus> getClientPlayers() {
        ArrayList<ClientPlayerStatus> al =
                new ArrayList<ClientPlayerStatus>();
        for (PlayerStatus p : getPlayers()) {
        	ClientPlayerStatus cp = new ClientPlayerStatus(p);
            al.add(cp);
        }
        return al;
    }

    /**
     * Get the ClientPlayerStatus object associated with the player.
     *
     * @return The ClientPlayerStatus object associated with the player.
     */
    public PlayerStatus getMyPlayer() {
        return  getPlayerByPerGameId(getPerGameId());
    }
    
    /**
     * Get the set of the players that are in the same team with the player (exluding the player)
     * @return The set of players that have the same teamId with the player
     */
    public Set<PlayerStatus> getMyTeam() {
    	Set<PlayerStatus> team = getTeam(getMyPlayer().getTeamId());
    	team.remove(getMyPlayer());
		return team;
    }


    /**
     * Tell watching observers that the phase has advanced.
     */
    public void notifyObserversPhaseAdvanced() {
        setChanged();
        notifyObservers("NEXT_PHASE");
    }

    /**
     * Determine whether the client's owner can move.
     *
     * @return Whether the client's owner can move.
     */
    public boolean myPlayerCanMove() {
        return getMyPlayer().areMovesAllowed();
    }

	public int getPerGameId()
	{
		return (Integer)get("perGameId");
	}

	public void setPerGameId(int perGameId)
	{
		set("perGameId", new Integer(perGameId));
	}

	public void setPhases(Phases phases)
	{
	    System.out.println( "setPhases in ClientGameStatus, the current phase is " + phases.getCurrentPhaseName() );

	    if( getPhases() != null ) {
	        getPhases().deleteObservers();
	        
/*
	        // ---- Checking for exceptions ----
	        if( (phases.getPhasesElapsed() > 0) && 
	        		((getPhases().getPhasesElapsed() + 1 != phases.getPhasesElapsed()) || 
	        		(getPhases().getCurrentPhaseName().equals( phases.getCurrentPhaseName())) )) {
	    		String str = "\nClientGameStatus - Phase advancement problem. \n" + 
		    		"Old phase name:		" + getPhases().getCurrentPhaseName() + "\n" +
	    			"Old phases elapsed:	" + getPhases().getPhasesElapsed() + "\n" +
		    		"New phase name:		" + phases.getCurrentPhaseName() + "\n" +
	    			"New phases elapsed:	" + phases.getPhasesElapsed() + "\n" + 
	    			"Time differenc			" + (getPhases().getTime()-phases.getTime());
	    		throw new RuntimeException( str );
	    	}
	    	// ---- End checking for exceptions ----
*/
	    }
		
		set("phases", new ClientPhases(phases));
        getPhases().addObserver(this);
        // Phases has a static Timer. The new Phases instance should be the
        // timer's only action listener -- remove all others
		for (ActionListener al : Phases.tm.getActionListeners())
			Phases.tm.removeActionListener(al);
		Phases.tm.addActionListener(getPhases());
        setChanged();
        notifyObservers("PHASES_CHANGED");
    }
}
