/*
	Colored Trails
	
	Copyright (C) 2006-2007, President and Fellows of Harvard College.  All Rights Reserved.
	
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

package edu.harvard.eecs.airg.coloredtrails.agent;

import java.util.ArrayList;
import java.util.Set;

import edu.harvard.eecs.airg.coloredtrails.client.ui.discourse.DiscourseHandler;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Board;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GamePalette;
import edu.harvard.eecs.airg.coloredtrails.shared.types.HistoryLog;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.RowCol;

public interface ColoredTrailsClient {

	
	/*
	 * methods used to connect to the CT server
	 * TODO: perhaps this should be generalized to use a
	 * proper java.net URL? 
	 *  
	 */
	
	public String getServerHostname();
	public void setServerHostname(String hostname);
	
	public int getServerPort();
	public void setServerPort(int port);

	//Pins!!!!!
        public String getPin();
	public void setPin(String name);
	

	
	  /**
     * A message to signify that the game has started.
     *
     * @return Whether the client has accepted this change.
     */
    public void gameStarted();
    
    /**
     * Tell the client that the game has been initialized
     * by the game configuration class' run() method
     */
    public void gameInitialized();

    /**
     * Tell the client that the game has ended.
     *
     * @return Whether the client has accepted this change.
     */
    public void gameEnded();

    /**
     * Tell the client that the board has been updated.
     *
     * @return Whether the client has accepted this change.
     */
    public void boardUpdated(Board b);

    /**
     * Tell the client that the phases have been updated.
     *
     * @return Whether the client has accepted this change.
     */
    public void phasesUpdated(Phases ph);

    /**
     * Tell the client that the players have been updated in some manner.
     *
     * @return Whether the client has accepted this change.
     */
    public void playersUpdated(Set<PlayerStatus> ps);

    /**
     * Tell the client that the phases have advanced one phase.
     *
     * @return Whether the client has accepted this change.
     */
    public void phaseAdvanced(Phases ph);

    /**
     * Tell the client that the history log has been update.
     *
     * @return Whether the client has accepted this change.
     */
    public void logUpdated(HistoryLog hl);

    
    /**
     * Tell the client that the game palette has been updated.
     *
     * @return Whether the client has accepted this change.
     *
     */
    
     public void gamePaletteUpdated(GamePalette g);
     
     
     /**
      * Tell the client a Discourse Message has been sent by the server.
      *
      * 
      *
      */
        
	 public void onReceipt(DiscourseMessage dm);

	

}