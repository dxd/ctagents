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

import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;
import edu.harvard.eecs.airg.coloredtrails.alglib.ShortestPaths;
import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;     
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Path;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import ctagents.example2.SimplePlayer;

/**
 * A class that extends the abstract class PhaseLoopPlayer for agents in the responder role.
 * In the Communication Phase, the responder agent waits for a proposal, and when received, first checks if the proposal is feasible.
 * If not, rejects it. Then, checks if the proposal is beneficial, in this version it means being
 * able to reach the goal with the ChipSet that the agent has after the exchange. If it's beneficial,
 * accepts the offer.
 * In the Exchange Phase, if there's an offer accepted, sends the chips that are asked for
 * In the Movement Phase, if there's an offer accepted, does the movement
 * @author ilke
 */
public class PhaseLoopResponder extends SimplePlayer {

	public PhaseLoopResponder() {
		super();
	}

	/**
	 * Implements the abstract method in PhaseLoopPlayer class
	 * If the receipt comes from a proposer, then takes proper action according to it
	 * @param dm Received discourse message
	 */
	public void onReceipt(DiscourseMessage dm) {
		if(dm instanceof BasicProposalDiscourseMessage) {
			// Message received is a proposal message
			BasicProposalDiscourseMessage proposal = (BasicProposalDiscourseMessage) dm;

			ClientGameStatus cgs = client.getGameStatus();
			ChipSet chipsForPath = new ChipSet(); // ChipSet after the possible exchange
			ChipSet willBeReceived = proposal.getChipsSentByProposer(); // Chips that will be received
			ChipSet willBeSent = proposal.getChipsSentByResponder(); //  Chips that will be sent
			ChipSet myChips = cgs.getMyPlayer().getChips();
		    System.out.println( "will be sent " + willBeSent );
			// ChipSet after the exchange is myChips - sentChips + receivedChips
			chipsForPath = ChipSet.subChipSets(myChips, willBeSent);
			chipsForPath = ChipSet.addChipSets(chipsForPath, willBeReceived);

			// Create the message for response
			BasicProposalDiscussionDiscourseMessage response =
			    new BasicProposalDiscussionDiscourseMessage( proposal );

			// Check if the proposal is feasible or not, reject if not
			if(!isFeasible(chipsForPath)) {
				response.rejectOffer();
				client.communication.sendDiscourseRequest(response);
				log.info("AGENT " + clientName + ": Sending response: OFFER REJECTED");
				System.out.println( "---- Rejecting (Not Feasible) ---- \n");
				return;
			}

			// Check if the exchange is beneficial or not
			if(shortestPaths == null) {
				shortestPaths = ShortestPaths.getPathsToFirstGoal(cgs.getMyPlayer().getPosition(),
						cgs.getBoard(), scoring);
			}
			for(Path path:shortestPaths) {
				if(chipsForPath.contains(path.getRequiredChips(cgs.getBoard()))) {
					response.acceptOffer();
					offerAccepted = true;
					sending = new ChipSet(willBeSent);
					chosenPath = path;
					client.communication.sendDiscourseRequest(response);
					log.info("AGENT " + clientName + ": Sending response: OFFER ACCEPTED. My path needs: " + path.getRequiredChips(cgs.getBoard()));

					System.out.println( "---- Accepting ---- \n");
					return;
				}
			}

			if( willBeSent.isEmpty() ) {
				response.acceptOffer();
				offerAccepted = true;
				sending = new ChipSet(willBeSent);
				client.communication.sendDiscourseRequest(response);
				log.info("AGENT " + clientName + ": Sending response: OFFER ACCEPTED.");

				System.out.println( "---- Accepting ---- \n");
				return;
			}

			response.rejectOffer();
			client.communication.sendDiscourseRequest(response);
			log.info("AGENT " + clientName + ": Sending response: OFFER REJECTED");
			System.out.println( "---- Rejecting ---- \n");

		}
		else {
			System.out.println( "---- Received a discourse message ---- \n" + dm );
			log.info("Received a discourse message: " + dm);
		}
	}

	/**
	 * Implements the abstract method in PhaseLoopPlayer class
	 * Every time a phase advances, the agent checks to see if the new phase is either Communication,
	 * Movement or Exchange phase. If it's one of these, takes the proper action.
	 * @param ph New phases object sent by the server
	 */
	public void phaseAdvanced(Phases ph) {
		/*ClientGameStatus cgs = client.getGameStatus();
		String phaseName = cgs.getPhases().getCurrentPhaseName();
		log.info("AGENT " + clientName + ": A new phase began: " + phaseName);

		// Communication Phase: Wait for incoming proposals
		if(phaseName.equals("Communication Phase")) {
				log.info("AGENT " + clientName + ": Waiting for incoming messages...");
		}   */
	}

	/**
	 * Checks if the exchange is feasible or not
	 * @param chipsForPath The ChipSet after the possible exchange
	 * @return true if all color numbers are greater or equal to 0, false otherwise
	 */
	private boolean isFeasible(ChipSet chipsForPath) {
		for(String color:chipsForPath.getColors()) {
			if(chipsForPath.getNumChips(color) < 0) {
				return false;
			}
		}
		return true;
	}
}