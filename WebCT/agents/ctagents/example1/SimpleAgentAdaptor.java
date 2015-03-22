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

package ctagents.example1;

import edu.harvard.eecs.airg.coloredtrails.agent.events.GameEndedEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.GameInitializedEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.GameStartEventListener;

/*
 * Example of a possible Adapter interface
 * This example adaptor agent for example only listens to gameStart and gameEnd events from
 * the base client class (via the *EventListener interfaces)
 * 
 * also a set/get client name has been provided to set the correct parameters on the client class
 * this could be done many ways
 * 
 */

public interface SimpleAgentAdaptor extends GameStartEventListener, GameEndedEventListener,
                                            GameInitializedEventListener
{
	/* This method will be invoked when the Game has ended
	 * 
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.AgentClient#gameEnded()
	 */
	public abstract void gameEnded();

	public abstract void gameStarted();
	
	public abstract void gameInitialized();
	
	public abstract String getClientName();
	
	public abstract void setClientName(String name);

	public abstract void start();
}