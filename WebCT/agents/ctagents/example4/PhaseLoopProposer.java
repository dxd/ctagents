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

import edu.harvard.eecs.airg.coloredtrails.alglib.ShortestPaths;
import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import ctagents.example2.SimplePlayer;

public class PhaseLoopProposer extends SimplePlayer {
    
    public PhaseLoopProposer() {
		super();
	}

	/**
	 * Implements the abstract method in PhaseLoopPlayer class
	 * If the receipt comes from a responder, then takes proper action according to it
	 * @param dm Received discourse message
	 */
	public void onReceipt(DiscourseMessage dm) {

	}

	/**
	 * Implements the abstract method in PhaseLoopPlayer class
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
    }

	/**
	 * Gets the best path that's not tried among the shortest paths, then asks for the chips that the
	 * agent lacks to take this path and offers all extra chips in return.
	 */
	private void makeOffer() {
		ClientGameStatus cgs = client.getGameStatus();

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
		int proposerID = cgs.getPerGameId();
		int responderID = 2;
		BasicProposalDiscourseMessage proposal =
			new BasicProposalDiscourseMessage(proposerID, responderID, -1, extra, missing);

		System.out.println( "---- sending proposal: proposal ----" );

		client.communication.sendDiscourseRequest(proposal);
	}
}