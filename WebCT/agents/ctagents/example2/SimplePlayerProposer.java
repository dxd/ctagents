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

import edu.harvard.eecs.airg.coloredtrails.alglib.ShortestPaths;
import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;
import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Path;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;

/**
 * A class that extends the abstract class SimplePlayer for agents in the proposer role.
 * In the Communication Phase, the proposer agent computes the most beneficial paths to reach the goal
 * and asks the responder agent for the chips that it lacks to take the best path, offering all its extra
 * chips in return. If the proposal is rejected, then makes another one for the chips to take the 2nd
 * best path, 3rd best path..
 * In the Exchange Phase, if there's an offer accepted, sends the chips that are asked for
 * In the Movement Phase, if there's an offer accepted, does the movement
 * @author ilke
 */
public class SimplePlayerProposer extends SimplePlayer{
	private int proposedTo;

	public SimplePlayerProposer() {
		super();
		
	}
	
	/**
	 * Implements the abstract method in SimplePlayer class
	 * If the receipt comes from a responder, then takes proper action according to it
	 * @param dm Received discourse message
	 */
	public void onReceipt(DiscourseMessage dm) {
		if(dm instanceof BasicProposalDiscussionDiscourseMessage) {
            BasicProposalDiscussionDiscourseMessage bpddm = (BasicProposalDiscussionDiscourseMessage) dm;

            // Message received is a response message
			if(bpddm.getProposerID() == proposedTo) {
				BasicProposalDiscussionDiscourseMessage response = (BasicProposalDiscussionDiscourseMessage) dm;
				log.info("AGENT " + clientName + ": got response to offer");
				
				if(!response.accepted() && shortestPaths.size() > 0) {
					// The proposal is rejected and we still have more paths to propose, make an offer
					makeOffer();
				}
				else if(shortestPaths.size() == 0) {
					// We tried all paths, none of our offers are accepted
					log.info("AGENT " + clientName + ": None of the offers were accepted : (");
				}
				else if(response.accepted()) {
					// The proposal is accepted
					offerAccepted = true;
					//sending = response.getRequestedChips();
                    sending = response.getChipsSentByProposer();
                    log.info("AGENT " + clientName + ": The following offer is accepted:");
					log.info("AGENT " + clientName + ": Received ChipSet: " + response.getChipsSentByResponder());
					log.info("AGENT " + clientName + ": Sent ChipSet: " + sending );
				}
			}
			else {
				log.info("Received a response to an invalid proposal: " + dm);
			}
		}
		else {
			log.info("Received a discourse message: " + dm);
		}
	
	}
	
	/**
	 * Implements the abstract method in SimplePlayer class
	 * Every time a phase advances, the agent checks to see if the new phase is either Communication,
	 * Movement or Exchange phase. If it's one of these, takes the proper action.
	 * @param ph New phases object sent by the server
	 */
	public void phaseAdvanced(Phases ph) {
		ClientGameStatus cgs = client.getGameStatus();
		String phaseName = cgs.getPhases().getCurrentPhaseName();
		log.info("AGENT " + clientName + ": A new phase began: " + phaseName);
		
		// Communication Phase: Compute the shortest paths to the goal and make an offer
		if(phaseName.equals("Communication Phase")) {
			
			shortestPaths = ShortestPaths.getShortestPathsToFirstGoal(cgs.getMyPlayer().getPosition(),
					cgs.getBoard(), scoring);
			
			makeOffer();		
		}
		// Movement Phase: If there's an accepted offer, take your path to the goal
		else if(phaseName.equals("Movement Phase")) {
			if(offerAccepted) {
				log.info("Taking the path: " + chosenPath);
				
				for(int i=0; i<chosenPath.getNumPoints(); i++) {
					client.communication.sendMoveRequest(chosenPath.getPoint(i));
				}
				offerAccepted = false;
			}
			else {
				// Go as close to goal as possible with the chips you have
                BestUse bu = new BestUse(cgs, cgs.getMyPlayer(), scoring, 0);     // calculate the best use of player's chips
                Path p = bu.getPaths().get(0);           // use the first best-path in list
                
				for(int i=0; i<p.getNumPoints(); i++) {
					client.communication.sendMoveRequest(p.getPoint(i));
				}
			}
		}
		// Exchange Phase: If there's an accepted offer, send the chips that are asked for
		else if(phaseName.equals("Exchange Phase")) {
			if(offerAccepted) {
				log.info("Sending the following ChipSet to the RESPONDER: " + sending);
				
				int responderID = getResponderID();
				client.communication.sendTransferRequest(responderID, sending);
			}
		}
	
	}
	
	/**
	 * Gets the best path that's not tried among the shortest paths, then asks for the chips that the
	 * agent lacks to take this path and offers all extra chips in return.
	 */
	private void makeOffer() {
		ClientGameStatus cgs = client.getGameStatus();
		//ChipSet missing = new ChipSet();
		//ChipSet extra = new ChipSet();
		ChipSet myChips = cgs.getMyPlayer().getChips();
		chosenPath = shortestPaths.remove(0); // Get the best path available
		ChipSet requiredChipsForPath = chosenPath.getRequiredChips(cgs.getBoard());
		
		ChipSet missing = myChips.getMissingChips(requiredChipsForPath);
		ChipSet extra = myChips.getExtraChips(requiredChipsForPath);
		
		log.info("AGENT " + clientName + ": Current: " + chosenPath);
		log.info("AGENT " + clientName + ": Required Chips: " + requiredChipsForPath);
		log.info("AGENT " + clientName + ": Missing Chips: " + missing);
		log.info("AGENT " + clientName + ": Extra Chips: " + extra);
		
		// Create a proposal discourse message from the proposer to the sender with default id
		int proposerID = getProposerID();
		int responderID = getResponderID();
		BasicProposalDiscourseMessage proposal = 
			new BasicProposalDiscourseMessage(proposerID, responderID, -1, extra, missing);
		proposedTo = proposal.getResponderID();
		
		System.out.println( "---- sending proposal: proposal " );
		
		client.communication.sendDiscourseRequest(proposal);	
	}
	
	private int getProposerID() {
		ClientGameStatus cgs = client.getGameStatus();
		return cgs.getMyPlayer().getPerGameId();
	}
	
	private int getResponderID() {
		int proposerID = getProposerID();
		ClientGameStatus cgs = client.getGameStatus();
		for( PlayerStatus player : cgs.getPlayers() ) {
			int playerID = player.getPerGameId();
			if( playerID != proposerID )
				return playerID;
		}
		throw new RuntimeException("Responder ID not found.");
		//return -1;
	}
}
