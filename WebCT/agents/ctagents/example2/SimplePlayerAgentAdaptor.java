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

package ctagents.example2;

import edu.harvard.eecs.airg.coloredtrails.agent.events.DiscourseMessageEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.GameEndedEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.GameStartEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.PhasesAdvancedEventListener;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;

public interface SimplePlayerAgentAdaptor extends GameStartEventListener, GameEndedEventListener, 
PhasesAdvancedEventListener, DiscourseMessageEventListener {


	/**
	 * Called to get the client name
	 */
	public abstract String getClientName();
	
	/**
	 * Called to set the client name to the string argument name
	 * @param name New name of the client
	 */
	public abstract void setClientName(String name);

	/**
	 * Called to start the client
	 */
	public abstract void start();
	
	/**
	 * Called by the server when the game is started
	 * @param clientGameStatus The game
	 */
	public abstract void gameStarted();
	
	/**
	 * Called by the server when the game configuration class' run() method completes
	 */
	public abstract void gameInitialized();
	
	/**
	 * Called by the server when the game ends
	 */
	public abstract void gameEnded();
	
	
	/**
	 * Called when a phase is advanced
	 */
	public abstract void phaseAdvanced(Phases ph);

	/**
	 * Called when a Discourse Message is received
	 * @param dm Discourse Message received
	 */
	public abstract void onReceipt(DiscourseMessage dm);
	
	
}

