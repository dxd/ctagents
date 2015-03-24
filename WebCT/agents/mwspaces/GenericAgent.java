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


import edu.harvard.eecs.airg.coloredtrails.agent.events.*;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.HistoryLog;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GamePalette;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;

//controller event listeners?

/**
* Generic Agent Adapter interface
* This generic adaptor interface listens to all events from
* the base client class (via the *EventListener interfaces)
* 
* also a set/get client name has been provided to set the correct parameters on the client class
* this could be done many ways???
*/

//TODO: clean up comments (javadoc) enzo

public interface GenericAgent extends BoardUpdatedEventListener, DiscourseMessageEventListener, GameEndedEventListener,
									GameInitializedEventListener, GamePaletteUpdatedEventListener, GameStartEventListener, 
									LogUpdatedEventListener, PhasesAdvancedEventListener, PhasesUpdatedEventListener,
									PlayersUpdatedEventListener 
									{

/** 
 * All methods from interface ColoredTrailsClient
* These methods are called by the server when a specific event occurs
*/

// called when board has been updated
public abstract void boardUpdated();

// called the game ends
public abstract void gameEnded();

// called when the game configuration class' run() method completes  
public abstract void gameInitialized();

/*
 * called when game palette is updated
 * @param g The updated GamePalette 
 */ 
public abstract void gamePaletteUpdated(GamePalette g); 

// called when game is started
public abstract void gameStarted();

/*
 * called when a Discourse Message is received
 * @param dm Discourse Message received
 */
public abstract void onReceipt(DiscourseMessage dm);

// returns the pin of the client
//public abstract String getPin();


// returns serverhostname
public abstract String getServerHostname();
 
// returns serverport
public abstract int getServerPort();
           
/*
 *tell the client that the history log has been updated
 * @param hl History log that has been updated 
 */ 
public abstract void logUpdated(HistoryLog hl);

/*
 * Called when a phase is advanced
 * @param ph advanced phase
 */
public abstract void phaseAdvanced(Phases ph);

/*
 * Called when phases are updated
 *  @param ph updated phases
 */
public abstract void phasesUpdated(Phases ph);

/*
 * Called when players are updated
 *  @param ps new Playerstatus
 */
public abstract void playersUpdated(PlayerStatus ps);

/*
 * called to set pin of client
 * @param name: pin of client
 */
//public abstract void setPin(String name);

/*
 * called to set name of serverhost
 * @param name of serverhost
 */
public abstract void setServerHostName(String name); 

/*
 * called to set serverport
 * @param port of server
 */
public abstract void setServerPort(int port);

// called to start client
public abstract void start();


}
