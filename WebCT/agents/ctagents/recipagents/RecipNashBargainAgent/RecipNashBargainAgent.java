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

//DONE FOR CT EXPERIMENT

package ctagents.recipagents.RecipNashBargainAgent;

import RecipExperiment.RecipConstants;
import ctagents.recipagents.PhaseWaiter;
import ctagents.recipagents.RecipAgentAdaptor;
import java.util.ArrayList;
import java.util.Set;

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
public class RecipNashBargainAgent implements RecipAgentAdaptor{
	private ColoredTrailsClientImpl client;
	/** indicates whether game state has been initialized */
//	private boolean game_initialized = false;
//	private ChipSet sending;
	private Scoring scoring;
	private int OppPerGameId;
	private int MyPerGameId;
	ClientGameStatus cgs = null;
        private double bestScore = -1;
	
	public RecipNashBargainAgent() {
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

                BasicProposalDiscussionDiscourseMessage responseMessage = new BasicProposalDiscussionDiscourseMessage(proposal );
                // check if the proposal is beneficial
                
                boolean offerResponse = RespondStrategy(ChipSet.subChipSets(proposal.getChipsSentByResponder(), proposal.getChipsSentByProposer() ));
                
                PhaseWaiter waiter = new PhaseWaiter(cgs.getPhases());
                waiter.doWait(RecipConstants.minRespondTime, RecipConstants.maxRespondTime);
                    
                // check if the proposal is beneficial
                if( offerResponse ) {
                    //response.setSubjectMsgId(subjectMsgId);
                    responseMessage.acceptOffer();
                } else {
                    //response.setSubjectMsgId(subjectMsgId);
                    responseMessage.rejectOffer();
                }
                
                
                    
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RecipNashBargainAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
                client.communication.sendDiscourseRequest(responseMessage);
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

                if(exchange == null) {
                    System.out.println("No beneficial " +
                                        "exchanges found among the ones that are beneficial for the responder");
                }
                else {
                    System.out.println("EXCHANGE: " + exchange);
                    senderChips = exchange.get(0);
                    recipientChips = exchange.get(1);
                    BasicProposalDiscourseMessage proposal= new BasicProposalDiscourseMessage(
                                    MyPerGameId, OppPerGameId, -1, senderChips, recipientChips);
//                    sending = senderChips;
                    
                    PhaseWaiter waiter = new PhaseWaiter(cgs.getPhases());
                    waiter.doWait(RecipConstants.minProposeTime, RecipConstants.maxProposeTime);
                    
                    
                    client.communication.sendDiscourseRequest(proposal);

                }
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

            ScoringUtility SU = new ScoringUtility(cgs, MyPerGameId, OppPerGameId);
            ChipSet offer = SU.getNashBargainOffer();
            ChipSet propChips = ChipSet.getNegation(offer);
            ChipSet respChips = new ChipSet(offer);
            for(String color : propChips.getColors()){
                if(propChips.getNumChips(color) < 0)
                    propChips.set(color, 0);
            }

            for(String color : respChips.getColors()){
                if(respChips.getNumChips(color) < 0)
                    respChips.set(color, 0);
            }

            mostBeneficialExchange = new ArrayList<ChipSet>();
            mostBeneficialExchange.add(propChips);
            mostBeneficialExchange.add(respChips);

            return mostBeneficialExchange;
	}
	 
    public boolean RespondStrategy(ChipSet proposal) {
//        // our input is a proposal
//        System.out.println("Received proposal: " + proposal);
//
//        BestUse bu = new BestUse(cgs, cgs.getMyPlayer(), scoring, 0);
//        double MyDefaultScore = bu.getBestState().getScore();
//
//        bu = new BestUse(cgs, cgs.getPlayerByPerGameId(OppPerGameId), scoring, 0);
//        double OppDefaultScore = bu.getBestState().getScore();
//
//        if(payoff(proposal) > MyDefaultScore*OppDefaultScore)
//            return(true);
//        else
//            return(false);
        
        ScoringUtility SU = new ScoringUtility(cgs, OppPerGameId, MyPerGameId);
//        double oppBenefit = SU.getOfferScore(proposal, OppPerGameId) - SU.getDefaultScore(OppPerGameId);
        double myBenefit = SU.getOfferScore(proposal, MyPerGameId) - SU.getDefaultScore(MyPerGameId);
        if(  (myBenefit > 0))
            return(true);
        else
            return(false);
        
    }
    
    
 }
