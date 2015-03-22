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

package ctagents.recipagents.RetroAgent;


import RecipExperiment.CreateGame;
import RecipExperiment.RecipLogic;
//import ctagents.recipagents.ScoringUtility;
//import ctagents.recipagents.NashBargain;
import RecipExperiment.RecipConstants;
import ctagents.recipagents.PhaseWaiter;
import java.util.ArrayList;
import java.util.Observable;

import ctagents.recipagents.RecipAgentAdaptor;
import edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsClientImpl;
import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.shared.ScoringUtility;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import java.util.List;
import java.util.Observer;

/**
 * An agent class that is a proposer in a CT game
 * 
 * @author ilke
 */
public class RetroAgent implements RecipAgentAdaptor, Observer {
    private ColoredTrailsClientImpl client;
    /** indicates whether game state has been initialized */
    private boolean game_initialized = false;
    //private ChipSet sending;
    private Scoring scoring;
    private int OppPerGameId;
    private int MyPerGameId;
    ClientGameStatus cgs = null;

    ScoringUtility SU;
    
    CreateGame cg;

    double[] merit = new double[2];

    RecipLogic RL = new RecipLogic();
   
    ChipSet myOffer;
    
    RecipConstants constants = new RecipConstants();


    public RetroAgent() {
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
        cgs.addObserver(this);
        
        MyPerGameId = cgs.getMyPlayer().getPerGameId();
        for(PlayerStatus ps : cgs.getPlayers()){
            if(ps.getPerGameId() == MyPerGameId)
                continue;
            else
                OppPerGameId = ps.getPerGameId();
        }
        merit[0] = 0;
        merit[1] = 0;
    }

    /**
     * Called by the server when the game configuration class' run() method completes
     */
    //NEVER CALLED
    public void gameInitialized()
    {
    	System.out.println("AGENT: gameInitialized() called");
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
                
                ChipSet offer = ChipSet.subChipSets(proposal.getChipsSentByResponder(), proposal.getChipsSentByProposer());
                
                RL.updateTypeProbs(constants.retroProbWeights, cgs, OppPerGameId, MyPerGameId, MyPerGameId, OppPerGameId, offer, merit, false);
                for(ArrayList<Double> pw : constants.retroProbWeights)
                    System.out.println("prob prop is: " + pw.get(0)); 
                
                
                boolean offerResponse = RespondStrategy(cgs, offer, OppPerGameId, MyPerGameId);
                
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
                
                
                client.communication.sendDiscourseRequest(responseMessage);
            }
        } else if( cgs.getMyPlayer().getRole().equals("Proposer")){
            int ProposerId = MyPerGameId;
            int ResponderId = OppPerGameId;
            
            System.out.println("Received response from client to my offer");
            BasicProposalDiscussionDiscourseMessage response = (BasicProposalDiscussionDiscourseMessage) dm;
            Boolean accepted = response.accepted();
            if(accepted)
                System.out.println("My offer has been accepted");
            else
                System.out.println("My offer has been rejected :(");
            
            RL.updateTypeProbs(constants.retroProbWeights, cgs, ProposerId, ResponderId, ProposerId, ResponderId, myOffer, merit, accepted);
            for(int i = 0; i < constants.retroProbWeights.size(); i++ )
                System.out.println("prob: " + constants.retroProbWeights.get(i).get(0) );
            
            updateMerit(ResponderId, RL.getMerit(cgs, ProposerId, ResponderId, ResponderId, ProposerId, myOffer, accepted, SU) );
            updateMerit(ProposerId, RL.getMerit(cgs, ProposerId, ResponderId, ProposerId, ResponderId, myOffer, accepted, SU) );
        }
    }

    /**
     * Called when a phase advances
     */
    public void phaseAdvanced(Phases ph) {
        scoring = cgs.getScoring();
        String phaseName = cgs.getPhases().getCurrentPhaseName();
        

        if(phaseName.equals("Offer Phase")) {
            if(cgs.getMyPlayer().getRole().equals("Proposer"))
            {   
                System.out.println("I'm the proposer, wheeeeee");
                ArrayList<ChipSet> exchange = strategy( cgs, MyPerGameId, OppPerGameId, constants, RL, merit );
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
                    myOffer = ChipSet.subChipSets(recipientChips, senderChips);
                    //sending = senderChips;
                    
                    PhaseWaiter waiter = new PhaseWaiter(cgs.getPhases());
                    waiter.doWait(RecipConstants.minProposeTime, RecipConstants.maxProposeTime);
                    
                    client.communication.sendDiscourseRequest(proposal);

                }
            }       
        } else if(phaseName.equals("Movement Phase")){
           
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
                System.out.println("agent type: " + this.getClass().getCanonicalName());
	}


	
    /**
     * Strategy of the proposer
     * @param o null
     * @return An exchange to propose
     */
    public static ArrayList<ChipSet> strategy(GameStatus gs, int ProposerId, int ResponderId, RecipConstants constants,
            RecipLogic RL, double[] merit) {
        
        //Here we play BestResponse recip strategy
        
        // Get all possible unique exchanges between the players
        ArrayList<ArrayList<ChipSet>> allExchanges = new ArrayList<ArrayList<ChipSet>>(
                ChipSet.getAllExchanges(gs.getPlayerByPerGameId(ProposerId).getChips(), 
                gs.getPlayerByPerGameId(ResponderId).getChips()));

        //System.out.println("Total number of unique exchanges: " + allExchanges.size());
        ArrayList<ChipSet> mostBeneficialExchange = null;

        //basic sanity checking
        //System.out.println("my player info: " + gs.getPlayerByPerGameId(ProposerId).toString());

        ArrayList<Double> socialScoreAccept = new ArrayList<Double>();
        ArrayList<Double> socialScoreReject = new ArrayList<Double>();
        ArrayList<Double> probAccept = new ArrayList<Double>();
        
        
        
        
        // search all exchanges
        int i = 0;
        for(ArrayList<ChipSet> exchange:allExchanges) {
            //System.out.println("-------------------------------------------------");

            ChipSet senderChips = exchange.get(0);
            ChipSet recipientChips = exchange.get(1);

            ChipSet Offer = ChipSet.subChipSets(recipientChips, senderChips);
            
            double aScore = 0;
            double rScore = 0;
            double pAccept = 0;

        

            List<Double> tweights;
            for(ArrayList<Double> weightProb : constants.retroProbWeights ){
            
                tweights = weightProb.subList(1, weightProb.size());
                
                aScore += weightProb.get(0) * RL.socialPref(gs, ProposerId, ResponderId, ProposerId, ResponderId, 
                        Offer, true, merit, tweights, false);
                rScore += weightProb.get(0) * RL.socialPref(gs, ProposerId, ResponderId, ProposerId, ResponderId, 
                        Offer, false, merit, tweights, false);

                pAccept += weightProb.get(0) * RL.probOfferAccepted(gs, ProposerId, ResponderId, ProposerId, ResponderId, 
                        Offer, merit, tweights);

            }
            
            socialScoreAccept.add( new Double(aScore));
            socialScoreReject.add( new Double(rScore));
            probAccept.add( new Double(pAccept));
            
            
            i++;
        }
        
        //System.out.println("all: " + allExchanges.size() + "  ssA "  + probAccept.size());
        
        double BestScore = socialScoreAccept.get(0)*probAccept.get(0) + socialScoreReject.get(0)*(1 - probAccept.get(0));
        mostBeneficialExchange = allExchanges.get(0);
        double newScore;
        for( int j = 0; j < socialScoreAccept.size(); j++){
            newScore = socialScoreAccept.get(j)*probAccept.get(j) + socialScoreReject.get(j)*(1 - probAccept.get(j));
            if(newScore > BestScore){
                BestScore = newScore;
                mostBeneficialExchange = allExchanges.get(j);
            }
        }
        
        //At this point, we have an ArrayList of all social pref scores
        
        return mostBeneficialExchange;
    }
    

    private void updateMerit(int perGameId, double deltaMerit){
        System.out.println("Player " + perGameId + "'s merit was "+ merit[perGameId] + " and will now change by " + deltaMerit);
        merit[perGameId] += deltaMerit;
        System.out.println("Player " + perGameId + "'s merit is "+ merit[perGameId]);
    }
  
	 
    
    //OFFER ALWAYS GIVEN AS (chips sent by recipient) - (chips sent by proposer)
    
    
    public Boolean RespondStrategy(GameStatus gs, ChipSet proposal, int ProposerId, int ResponderId) {
        // our input is a proposal
        System.out.println("Received proposal: " + proposal);
        
        ScoringUtility SU = new ScoringUtility(gs, ProposerId, ResponderId);
        System.out.println("default scores: prop: " + SU.getPlayerScore(gs.getPlayerByPerGameId(ProposerId).getChips(), ProposerId) + 
                " resp: " + SU.getPlayerScore(gs.getPlayerByPerGameId(ResponderId).getChips(), ResponderId));
        System.out.println("offer scores: prop: " + SU.getOfferScore(proposal, ProposerId) + " resp: " + SU.getOfferScore(proposal, ResponderId));
        
        //First we need to update merit:
        //Remember, we update merit for the Proposer!!!!! here
        updateMerit(ProposerId, RL.getMerit(gs, ProposerId, ResponderId, ProposerId, ResponderId, proposal, null, SU) );
        
        double aScore = 0;
        double rScore = 0;
        
        List<Double> weights;
        
        for(ArrayList<Double> weightProb : constants.retroProbWeights ){
            
            weights = weightProb.subList(1, weightProb.size());
            
            aScore += weightProb.get(0) * RL.socialPref(cgs, ProposerId, ResponderId, MyPerGameId, OppPerGameId, 
                    proposal, true, merit, weights, false);
            rScore += weightProb.get(0) * RL.socialPref(cgs, ProposerId, ResponderId, MyPerGameId, OppPerGameId, 
                    proposal, false, merit, weights, false);
            System.out.println("aScore: " + aScore + " rScore " + rScore);
        }
        
        Boolean accept;
        
        if(aScore >= rScore) {
            System.out.println("Accepting the proposal");
            accept = true;
        }
        else {
            System.out.println("Rejecting the proposal");
            accept = false;            
        }
        updateMerit(ResponderId, RL.getMerit(gs, ProposerId, ResponderId, ResponderId, ProposerId, proposal, accept, SU) );
        return(accept);
    }

    public void update(Observable o, Object arg) {
        String s;
        if(arg instanceof String){
            s = (String) arg;
            if(s.equals("NEWROUND")){
                System.out.println("We have a new round, resetting SU");
                int ResponderId, ProposerId;
                if(cgs.getMyPlayer().getRole().equals("Responder")){
                    ResponderId = MyPerGameId;
                    ProposerId = OppPerGameId;
                } else {
                    ResponderId = OppPerGameId;
                    ProposerId = MyPerGameId;
                }
                SU = new ScoringUtility(cgs, ProposerId, ResponderId);
            }
                
        }
    }
        
}
