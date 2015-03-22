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

import edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsClientImpl;
import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;
import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Path;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;

/**
 * An agent class that is a responder in a CT game.
 * Accepts a proposal if it's beneficial for itself (increases its score)
 * @author ilke
 *
 */
public class Responder implements AgentAdaptorPR{
	private ColoredTrailsClientImpl client;
	/** indicates whether game state has been initialized */
	private boolean game_initialized = false;
	private Scoring scoring;
	private double bestScore;
	static final int GOALWEIGHT = 50;
	static final int DISTWEIGHT = -10;
	static final int CHIPWEIGHT = 15;
	
	public Responder() {
		client = new ColoredTrailsClientImpl();
		client.addDiscourseMessageEventListener(this);
		client.addPhasesAdvancedEventListener(this);
		client.addGameStartEventListener(this);
		client.addGameEndedEventListener(this);
		scoring = new Scoring(GOALWEIGHT, DISTWEIGHT, CHIPWEIGHT);
		bestScore = -1;
	}
	
	/**
	 * Called when the game ends
	 */
	public void gameEnded() {
		System.out.println("Game ended");
		System.out.println("My PlayerStatus is: " + client.getGameStatus().getMyPlayer());
	}

	/**
	 * Called when the game starts
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
	 * @param dm Discourse message received
	 */
	public void onReceipt(DiscourseMessage dm) {
		System.out.println("Received a " + dm.getClass() );
		
		// check if it is a basic proposal discourse message
		if(dm instanceof BasicProposalDiscourseMessage) {
            BasicProposalDiscourseMessage proposal = (BasicProposalDiscourseMessage) dm;

            BasicProposalDiscussionDiscourseMessage response =
                            new BasicProposalDiscussionDiscourseMessage(proposal );


            	
            // check if the proposal is beneficial
            if( ((Boolean) strategy(proposal))) {
	            //response.setSubjectMsgId(subjectMsgId);
	            response.acceptOffer();
	
            }
            else {
                //response.setSubjectMsgId(subjectMsgId);
                response.rejectOffer();
            }
            client.communication.sendDiscourseRequest(response);

       	}
	}
	
	/**
	 * Called when a phase advances
	 */
	public void phaseAdvanced(Phases ph) {
		ClientGameStatus cgs = client.getGameStatus();
		String phaseName = cgs.getPhases().getCurrentPhaseName();
		
		// if the bestScore for the initial status is not computed, compute it
		if(bestScore == -1) {
			BestUse bu = new BestUse(cgs, cgs.getMyPlayer(), scoring, 0);     // calculate the best use of player's chips
			bestScore = bu.getBestState().getScore();
		}
		
		if(phaseName.equals("Communication Phase")) {
			// in the communication phase, simply wait for incoming proposals
			System.out.println("Waiting for proposals...");
		}
		else if(phaseName.equals("Movement Phase")) {
			// in the movement phase, use the best use algorithm to take the path that will
			// take you to the status with max. score possible
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
	 * Strategy for the responder
	 * @param o A proposal (which is an exchange)
	 * @return Boolean.TRUE if the agent accepts, Boolean.FALSE otherwise
	 */
	public Object strategy(Object o) {
		ClientGameStatus cgs = client.getGameStatus();
		// our input is a proposal
		ChipSet proposal = (ChipSet) o;
		System.out.println("Received proposal: " + proposal);
		PlayerStatus ps = new PlayerStatus(cgs.getMyPlayer());
		ChipSet myChips = ps.getChips();
		
		// the agent's new status if it accepts the proposal
		ps.setChips(ChipSet.addChipSets(myChips, proposal));
		
        BestUse bu = new BestUse(cgs, ps, scoring, 0);
        double newScore = bu.getBestState().getScore();
        // check if the score we can get with the new status is greater than the initial one
        // return true if it is, false otherwise
        if(newScore - bestScore >= 0) {
        	System.out.println("Accepting the proposal");
        	return Boolean.TRUE;
        }
        else {
        	System.out.println("Rejecting the proposal");
        	return Boolean.FALSE;
        }
	}
}

