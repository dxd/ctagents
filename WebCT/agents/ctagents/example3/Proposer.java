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

package ctagents.example3;

import java.util.ArrayList;
import java.util.Set;

import edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsClientImpl;
import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;
import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Path;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;

/**
 * An agent class that is a proposer in a CT game
 * 
 * @author ilke
 */
public class Proposer implements AgentAdaptorPR{
	private ColoredTrailsClientImpl client;
	/** indicates whether game state has been initialized */
	private boolean game_initialized = false;
	private ChipSet sending;
	private Scoring scoring;
	private static final int RESPONDER = 1;
	private static final int PROPOSER = 0;
	static final int GOALWEIGHT = 50;
	static final int DISTWEIGHT = -10;
	static final int CHIPWEIGHT = 15;
	
	public Proposer() {
		client = new ColoredTrailsClientImpl();
		client.addDiscourseMessageEventListener(this);
		client.addPhasesAdvancedEventListener(this);
		client.addGameStartEventListener(this);
		client.addGameEndedEventListener(this);
		scoring = new Scoring(GOALWEIGHT, DISTWEIGHT, CHIPWEIGHT);
	}

	/**
	 * Called when a game ends
	 */
	public void gameEnded() {
		System.out.println("Game ended");
		System.out.println("My PlayerStatus is: " + client.getGameStatus().getMyPlayer());
	}
	
	/**
	 * Called when a game starts
	 */
	public void gameStarted() {
		System.out.println("Game started");
		
	}
	
	/**
	 * Called by the server when the game configuration class' run() method completes
	 */
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
	 * Gets the client name
	 */
	public String getClientName() {
		return client.getPin();
	}

	/**
	 * Called when a discourse message is received
	 * @param dm discourse message received
	 */
	public void onReceipt(DiscourseMessage dm) {
		System.out.println("Received a " + dm.getClass() );
		
	}

	/**
	 * Called when a phase advances
	 */
	public void phaseAdvanced(Phases ph) {
		ClientGameStatus cgs = client.getGameStatus();
		String phaseName = cgs.getPhases().getCurrentPhaseName();
		if(phaseName.equals("Communication Phase")) {
			ArrayList<ChipSet> exchange = (ArrayList<ChipSet>) strategy(null);
			ChipSet senderChips;
			ChipSet recipientChips;
			
			if(exchange == null) {
				System.out.println("No beneficial " +
						"exchanges found among the ones that are beneficial for the responder");
			}
			else {
				System.out.println("EXCHANGE: " + exchange);
				senderChips = exchange.get(0);
				recipientChips = exchange.get(1);
				BasicProposalDiscourseMessage proposal= new BasicProposalDiscourseMessage(
						PROPOSER, RESPONDER, -1, senderChips, recipientChips);
				sending = senderChips;
				client.communication.sendDiscourseRequest(proposal);
				
			}
		}
		else if(phaseName.equals("Movement Phase")) {
			BestUse bu = new BestUse(cgs, cgs.getMyPlayer(), scoring, 0);     // calculate the best use of player's chips
			Path p = bu.getPaths().get(0);           // use the first best-path in list
			
			for(int i=0; i<p.getNumPoints(); i++) {
				client.communication.sendMoveRequest(p.getPoint(i));
			}
		}
	}
	
	/**
	 * Sets the client name
	 * @param name client name
	 */
	public void setClientName(String name) {
		client.setPin(name);
	}

	/**
	 * Starts the client
	 */
	public void start() {
		client.start();
	}

	/**
	 * Checks if an exchange is beneficial for the responder or not
	 * @param senderChips Chips the the proposer will send
	 * @param recipientChips Chips the the recipient will send
	 * @return true if beneficial, false otherwise
	 */
	private boolean isBeneficialForResponder(ChipSet senderChips, ChipSet recipientChips) {
		ClientGameStatus cgs = client.getGameStatus();
		PlayerStatus responder = cgs.getPlayerByPerGameId(RESPONDER);
		BestUse bu = new BestUse(cgs, responder, scoring, 0);
		double initialBestScore =  bu.getBestState().getScore();
		// compute what responder's new chipset will be
		ChipSet newChips = ChipSet.subChipSets(
				ChipSet.addChipSets(responder.getChips(), senderChips), recipientChips);
		PlayerStatus newResponder = new PlayerStatus(responder);
		newResponder.setChips(newChips);
		// compute it's best score with this new status
		bu = new BestUse(cgs, newResponder, scoring, 0);
		double newBestScore = bu.getBestState().getScore();
		
		// if it's better than the initial score, then it's beneficial
		if(newBestScore > initialBestScore) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Strategy of the proposer
	 * @param o null
	 * @return An exchange to propose
	 */
	public Object strategy(Object o) {
		ClientGameStatus cgs = client.getGameStatus();
		// Get all possible unique exchanges between the players
		Set<ArrayList<ChipSet>> allExchanges = ChipSet.getAllExchanges(
				cgs.getMyPlayer().getChips(), cgs.getPlayerByPerGameId(RESPONDER).getChips());
		
		System.out.println("Total number of unique exchanges: " + allExchanges.size());
		ArrayList<ChipSet> mostBeneficialExchange = null;
		// Get the agent's best score with the initial chipset
		BestUse bu = new BestUse(cgs, cgs.getMyPlayer(), scoring, 0);
		double bestScore = bu.getBestState().getScore();
		
		PlayerStatus myPlayer = cgs.getMyPlayer();
		ChipSet myChips = myPlayer.getChips();
		ChipSet newChips;
		PlayerStatus newPlayer;
		double newScore;
		
		// search all exchanges
		for(ArrayList<ChipSet> exchange:allExchanges) {
			ChipSet senderChips = exchange.get(0);
			ChipSet recipientChips = exchange.get(1);
			
			// if it's beneficial for the responder
			if(isBeneficialForResponder(senderChips, recipientChips)) {
				newChips = ChipSet.subChipSets(ChipSet.addChipSets(
						myChips, recipientChips), senderChips);
				
				newPlayer = new PlayerStatus(myPlayer);
				newPlayer.setChips(newChips);
				// compute what the agent's new score will be
				bu = new BestUse(cgs, newPlayer, scoring, 0);
				newScore = bu.getBestState().getScore();
				// if it's greater than the best score so far, set this as the best score
				// and save the exchange
				if(newScore > bestScore) {
					mostBeneficialExchange = new ArrayList<ChipSet>(exchange);
					bestScore = newScore;
				}
			}
		}
		// return the most beneficial exchange
		return mostBeneficialExchange;
	}
 }
