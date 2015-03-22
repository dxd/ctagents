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

import java.util.ArrayList;
import java.util.Random;

import edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsClientImpl;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.*;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Board;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GamePalette;
import edu.harvard.eecs.airg.coloredtrails.shared.types.HistoryLog;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;

/**
 * @author Ricardo De Lima (ricardo@eecs.harvard.edu)
 * 
 * A SimpleAgentAdaptor example
 * This agent only listens to game start and game end events
 * 
 * This agent prints out the events it receives to
 * standard output
 *
 */
public class SimpleAgentAdaptorImpl implements SimpleAgentAdaptor
{	
	public ColoredTrailsClientImpl client;
	public boolean game_started = false;
	public boolean game_initialized = false;
	public ClientGameStatus GameStat;
	private Object communication;

	public SimpleAgentAdaptorImpl()
	{	
		client = new ColoredTrailsClientImpl();
		client.addGameStartEventListener(this);
		client.addGameInitializedEventListener(this);
		client.addGameEndedEventListener(this);
	}
	

	/*
	 * This method will be invoked when the Game has ended
	 */
	public void gameEnded()
	{	
		System.out.println("Game Ended");
		//System.exit(0);
	}


	public void gameStarted()
	{
		System.out.println("Game Started");
		game_started = true;
		System.out.println("we have " + client.getGameStatus().getBoard().getGoals().size() + " goals");
		GameStat = client.getGameStatus();
	}
	
	public void gameInitialized()
	{
		System.out.println("Game Initialized");
		game_initialized = true;
		
		ClientGameStatus cgs = client.getGameStatus();
		String phaseName = cgs.getPhases().getCurrentPhaseName();
		System.out.println("AGENT " + client.getPin() + ": current phase name: " + phaseName);
		System.out.println("we have " + client.getGameStatus().getBoard().getGoals().size() + " goals");
	}

	public  String getClientName()
	{
		return client.getPin();
	}
	
	
	public void setClientName(String name)
	{
		client.setPin(name);		
	}
	
	
	public void start()
	{
		client.start();
	}
}
