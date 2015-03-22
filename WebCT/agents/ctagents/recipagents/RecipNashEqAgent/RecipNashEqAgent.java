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

package ctagents.recipagents.RecipNashEqAgent;

import java.util.ArrayList;
import java.util.Set;

import ctagents.recipagents.RecipAgentAdaptor;
import edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsClientImpl;
import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;
import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.shared.ScoringUtility;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An agent class that is a proposer in a CT game
 * 
 * @author ilke
 */
public class RecipNashEqAgent implements RecipAgentAdaptor{
	private ColoredTrailsClientImpl client;
	/** indicates whether game state has been initialized */
	private boolean game_initialized = false;
	private ChipSet sending;
	private Scoring scoring;
	private int OppPerGameId = 1;
	private int MyPerGameId = 0;
	ClientGameStatus cgs = null;
        private double bestScore = -1;
	
	public RecipNashEqAgent() {
		client = new ColoredTrailsClientImpl(this.getClass().getCanonicalName());
		client.addDiscourseMessageEventListener(this);
		client.addPhasesAdvancedEventListener(this);
		client.addGameStartEventListener(this);
		client.addGameEndedEventListener(this);
	}

	/**
	 * Called when a game ends
	 */
	public void gameEnded() {
		System.out.println("Game ended ");
		System.out.println("My PlayerStatus is: " + client.getGameStatus().getMyPlayer());
	}
	
	/**
	 * Called when a game starts
	 */
	public void gameStarted() {
		System.out.println("#########################Game started");
		cgs = client.getGameStatus();
                MyPerGameId = cgs.getMyPlayer().getPerGameId();
                for(PlayerStatus ps : cgs.getPlayers()){
                    if(ps.getPerGameId() == MyPerGameId)
                        continue;
                    else
                        OppPerGameId = ps.getPerGameId();
                }
                
	}
	
	/**
	 * Called by the server when the game configuration class' run() method completes
	 */
        //NEVER CALLED
	public void gameInitialized()
	{
//		System.out.println("#########################Game Initialized");
//		game_initialized = true;
//		
//		
//		String phaseName = cgs.getPhases().getCurrentPhaseName();
//		System.out.println("AGENT " + client.getClientName() + ": current phase name: " + phaseName);
//		System.out.println("we have " + client.getGameStatus().getBoard().getGoals().size() + " goals");
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
        // check if it is a basic proposal discourse message
        
        if(cgs.getMyPlayer().getRole().equals("Responder")){
            if(dm instanceof BasicProposalDiscourseMessage) {
                BasicProposalDiscourseMessage proposal = (BasicProposalDiscourseMessage) dm;

                BasicProposalDiscussionDiscourseMessage response = new BasicProposalDiscussionDiscourseMessage(proposal );
                // check if the proposal is beneficial
                if( (RespondStrategy(ChipSet.subChipSets(proposal.getChipsSentByResponder(), proposal.getChipsSentByProposer())))) {
                    //response.setSubjectMsgId(subjectMsgId);
                    response.acceptOffer();
                } else {
                    //response.setSubjectMsgId(subjectMsgId);
                    response.rejectOffer();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RecipNashEqAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
                client.communication.sendDiscourseRequest(response);
            }
        }
    }

    /**
     * Called when a phase advances
     */
    public void phaseAdvanced(Phases ph) {
        scoring = cgs.getScoring();
        String phaseName = cgs.getPhases().getCurrentPhaseName();
        if(bestScore == -1) {
            BestUse bu = new BestUse(cgs, cgs.getMyPlayer(), scoring, 0);     // calculate the best use of player's chips
            bestScore = bu.getBestState().getScore();
        }

        if(phaseName.equals("Offer Phase")) {
            if(cgs.getMyPlayer().getRole().equals("Proposer"))
            {   
                System.out.println("I'm the proposer, wheeeeee");
                ArrayList<ChipSet> exchange = (ArrayList<ChipSet>) strategy(null);
                ChipSet senderChips;
                ChipSet recipientChips;

                
                if(exchange != null){
                    System.out.println("EXCHANGE: " + exchange);
                    senderChips = exchange.get(0);
                    recipientChips = exchange.get(1);
                } else {
                    senderChips = new ChipSet();
                    recipientChips = new ChipSet();
                }
                BasicProposalDiscourseMessage proposal= new BasicProposalDiscourseMessage(
                                MyPerGameId, OppPerGameId, -1, senderChips, recipientChips);
                sending = senderChips;
                client.communication.sendDiscourseRequest(proposal);

                
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
	private boolean isBeneficialForResponder(ChipSet offer, ScoringUtility SU) {
            if( SU.getOfferScore( offer, OppPerGameId ) > SU.getDefaultScore(OppPerGameId) )
                return(true);
            else
                return(false);
	}
	
	/**
	 * Strategy of the proposer
	 * @param o null
	 * @return An exchange to propose
	 */
    public Object strategy(Object o) {
        // Get all possible unique exchanges between the players
        Set<ArrayList<ChipSet>> allExchanges = ChipSet.getAllExchanges(
                        cgs.getMyPlayer().getChips(), cgs.getPlayerByPerGameId(OppPerGameId).getChips());

        System.out.println("Total number of unique exchanges: " + allExchanges.size());
        ArrayList<ChipSet> mostBeneficialExchange = null;

        //basic sanity checking
        System.out.println("my player info: " + cgs.getMyPlayer().toString());

        ScoringUtility SU = new ScoringUtility(cgs, MyPerGameId, OppPerGameId );
        
        // Get the agent's best score with the initial chipset
//        BestUse bu = new BestUse(cgs, cgs.getMyPlayer(), scoring, 0);
        double bestScore = SU.getDefaultScore(MyPerGameId);

        double offerScore;
        ChipSet offer;
        
        // search all exchanges
        for(ArrayList<ChipSet> exchange:allExchanges) {
            ChipSet senderChips = exchange.get(0);
            ChipSet recipientChips = exchange.get(1);

            offer = ChipSet.subChipSets(recipientChips, senderChips);
            
            // if it's beneficial for the responder
            if(isBeneficialForResponder(offer, SU)) {

                // if it's greater than the best score so far, set this as the best score
                // and save the exchange
                offerScore = SU.getOfferScore(offer, MyPerGameId);
                if( offerScore >= bestScore) {
                        mostBeneficialExchange = new ArrayList<ChipSet>(exchange);
                        bestScore = offerScore;
                }
            }
        }
        // return the most beneficial exchange
        return mostBeneficialExchange;
    }

 
    public boolean RespondStrategy(ChipSet proposal) {
        // our input is a proposal
        System.out.println("Received proposal: " + proposal);
        
        ScoringUtility SU = new ScoringUtility(cgs, OppPerGameId, MyPerGameId);
        
        // check if the score we can get with the new status is greater than the initial one
        // return true if it is, false otherwise
        if( SU.getOfferScore(proposal, MyPerGameId) > SU.getDefaultScore(MyPerGameId) ) {
            System.out.println("Accepting the proposal");
            return(true);
        }
        else {
            System.out.println("Rejecting the proposal");
            return(false);
        }
    }
 }
