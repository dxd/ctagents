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

package ctagents.example4;

import java.util.ArrayList;
import org.apache.log4j.Logger;

import edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsClientImpl;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Path;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import ctagents.example2.SimplePlayerAgentAdaptor;

/**
 * An abstract class for the computational CT agent Simple Player
 * @author ilke
 *
 */
public abstract class PhaseLoopPlayer implements SimplePlayerAgentAdaptor {
	/** game started or not */
	protected boolean game_started;
	/** indicates whether game state has been initialized */
	protected boolean game_initialized = false;
	/** client of the agent */
	protected ColoredTrailsClientImpl client;
	/** client name of the agent */
	protected String clientName;
	/** shortest paths to the goal */
	protected ArrayList<Path> shortestPaths;
	/** scoring of the game (this won't be necessary once GameStatus.getScoring() is available */
	protected Scoring scoring;
	/** the offer is accepted or not */
	protected boolean offerAccepted;
	/** the path chosen for proposal */
	protected Path chosenPath;
	/** the chipset that the agent will send */
	protected ChipSet sending;
	/** logger */
	protected Logger log = Logger.getRootLogger();

	/** role id of the proposer */
	//protected static final int PROPOSER = 0;
	/** role id of the responder */
	//protected static final int RESPONDER = 1;
	/** default discourse message id */
	//protected static final int DEFAULTID = -1;
	/** bonus for reaching the goal */
	static final int GOALWEIGHT = 50;
	/** malus for the distance from the goal */
	static final int DISTWEIGHT = -15;
	/** bonus for chips left */
	static final int CHIPWEIGHT = 10;

	/**
	 * Adds the event listeners to the client and initializes some fields
	 */
	public PhaseLoopPlayer() {
		client = new ColoredTrailsClientImpl();
		client.addGameStartEventListener(this);
		client.addGameEndedEventListener(this);
		client.addPhasesAdvancedEventListener(this);
		client.addDiscourseMessageEventListener(this);

		game_started = false;
		offerAccepted = false;
		shortestPaths = null;
		scoring = new Scoring(GOALWEIGHT, DISTWEIGHT, CHIPWEIGHT);
	}

	/**
	 * Called by the server when the game ends
	 */
	public void gameEnded() {
		log.info("AGENT " + clientName + ": Game ended");
		log.info("AGENT " + clientName + ": My PlayerStatus is " + client.getGameStatus().getMyPlayer());
		System.exit(0);

	}

	/**
	 * Called by the server when the game starts
	 */
	public void gameStarted() {
		log.info("AGENT " + client.getPin() + ": Game started");
		game_started = true;
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


	/**
	 * Returns the client name of the agent
	 */
	public String getClientName() {
		return client.getPin();
	}

	/**
	 * Called by the server when the client receives a discourse message
	 * @param dm Received discourse message
	 */
	public abstract void onReceipt(DiscourseMessage dm);

	/**
	 * Sets the client name of the agent
	 * @param name New client name
	 */
	public void setClientName(String name) {
		client.setPin(name);
		clientName = name;

	}

	/**
	 * Starts the client
	 */
	public void start() {
		client.start();

	}

	/**
	 * Called by the server when a phase is advanced
	 */
	public abstract void phaseAdvanced(Phases ph);
}