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

package ctagents.recipagents.RecipSocialPref2Agent;

import RecipExperiment.RecipLogic;
import RecipExperiment.RecipConstants;
import RecipExperiment.CreateGame;
import ctagents.recipagents.PhaseWaiter;
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
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.math.*;


//--------------------------Finished--------------------
import java.util.List;

public class RecipSocialPref2Agent implements RecipAgentAdaptor{
	private ColoredTrailsClientImpl client;
	/** indicates whether game state has been initialized */
	private boolean game_initialized = false;
	private ChipSet myOffer;
	private Scoring scoring;
	private int OppPerGameId;
	private int MyPerGameId;
	ClientGameStatus cgs = null;
        
        private double bestScore = -1;
        
        RecipConstants constants = new RecipConstants();
	
	public RecipSocialPref2Agent() {
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
                
                ChipSet offer = ChipSet.subChipSets(proposal.getChipsSentByResponder(), proposal.getChipsSentByProposer());
                
                //RecipLogic.updateTypeProbs(constants.retroProbWeights, cgs, OppPerGameId, MyPerGameId, MyPerGameId, OppPerGameId, offer, merit, false);
                for(ArrayList<Double> pw : constants.socialPrefProbWeights)
                    System.out.println("prob prop is: " + pw.get(0)); 
                
                boolean offerResponse = RespondStrategy( offer );
                
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
                    Logger.getLogger(RecipSocialPref2Agent.class.getName()).log(Level.SEVERE, null, ex);
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
            
            updateTypeProbs(constants.socialPrefProbWeights, cgs, ProposerId, ResponderId, MyPerGameId, OppPerGameId, myOffer, accepted);
            
            for(int i = 0; i < constants.socialPrefProbWeights.size(); i++ )
                System.out.println("prob: " + constants.socialPrefProbWeights.get(i).get(0) );
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
                ArrayList<ChipSet> exchange = strategy(cgs, MyPerGameId, OppPerGameId, constants);
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
                System.out.println("agent type: " + this.getClass().getCanonicalName());
	}


	
	/**
	 * Strategy of the proposer
	 * @param o null
	 * @return An exchange to propose
	 */
        public static ArrayList<ChipSet> strategy(GameStatus gs, int ProposerId, int ResponderId, RecipConstants constants) {
            // Get all possible unique exchanges between the players
            Set<ArrayList<ChipSet>> allExchanges = 
                    ChipSet.getAllExchanges( gs.getPlayerByPerGameId(ProposerId).getChips(), 
                    gs.getPlayerByPerGameId(ResponderId).getChips());

            //System.out.println("Total number of unique exchanges: " + allExchanges.size());
            ArrayList<ChipSet> mostBeneficialExchange = null;

            //basic sanity checking
            //System.out.println("my player info: " + gs.getPlayerByPerGameId(ProposerId).toString());

            ScoringUtility SU = new ScoringUtility(gs, ProposerId, ResponderId);

            double PropDefaultScore = SU.getDefaultScore(ProposerId);

            double OppDefaultScore = SU.getDefaultScore(ResponderId);

            double BestScore = PropDefaultScore;
            double newScore;
            
            double tpayoff;

            // search all exchanges
            for(ArrayList<ChipSet> exchange:allExchanges) {
//                System.out.println("-------------------------------------------------");

                ChipSet offer = ChipSet.subChipSets(exchange.get(1), exchange.get(0));
               // double acceptscore = SU.getOfferScore(offer, ProposerId);

                //newScore = payoffforresponder(ChipSet.subChipSets(recipientChips, senderChips), MyDefaultScore, OppDefaultScore, .1, .7, .1, .1);
                double responderPayoff = 0;

                double aScore = 0;
                double rScore = 0;
                double pAccept = 0;

                List<Double> tweights;
                for(ArrayList<Double> weightProb : constants.socialPrefProbWeights ){

                    tweights = weightProb.subList(1, weightProb.size());


                aScore += weightProb.get(0) * socialPref(gs, ProposerId, ResponderId, ProposerId, ResponderId, offer,
                                           PropDefaultScore, OppDefaultScore,   tweights.get(0), tweights.get(1), tweights.get(2), tweights.get(3)    );

                    
                    tpayoff = socialPref(gs, ProposerId, ResponderId, ResponderId, ProposerId, offer,
                             PropDefaultScore, OppDefaultScore,  tweights.get(0), tweights.get(1), tweights.get(2), tweights.get(3));
                    responderPayoff += weightProb.get(0) * tpayoff;
                    
                }

                //double expreject = Math.exp(OppDefaultScore);
                double expaccept = Math.exp(responderPayoff);
                double paccept = expaccept / ( 1 + expaccept);
                double preject = 1 - paccept;
                newScore = paccept*aScore + preject*PropDefaultScore;

//                System.out.println("score: " + newScore + " expaccept " + expaccept);
//                System.out.println("paccept " + paccept + " preject " + preject);

                if(newScore >= BestScore){
                    //System.out.println("Found higher score: " + offer );
                    BestScore = newScore;
                    mostBeneficialExchange = new ArrayList<ChipSet>(exchange);
                }
            }
            return mostBeneficialExchange;
	}
	 
    public boolean RespondStrategy(ChipSet proposal) {
        // our input is a proposal
        System.out.println("Received proposal: " + proposal);


        ScoringUtility SU = new ScoringUtility(cgs, OppPerGameId, MyPerGameId);

//               System.out.println("default scores: prop: " + SU.getPlayerScore(gs.getPlayerByPerGameId(ProposerId).getChips(), ProposerId) +
//                       " resp: " + SU.getPlayerScore(gs.getPlayerByPerGameId(ResponderId).getChips(), ResponderId));
//               System.out.println("offer scores: prop: " + SU.getOfferScore(proposal, ProposerId) + " resp: " + SU.getOfferScore(proposal, ResponderId));
//
               //First we need to update merit:
               //Remember, we update merit for the Proposer!!!!! here

               double aScore = 0;
               double rScore = 0;

               List<Double> weights;


         double PropDefaultScore = SU.getDefaultScore(MyPerGameId);

            double OppDefaultScore = SU.getDefaultScore(OppPerGameId);
        //double newScore = SU.getOfferScore(proposal, MyPerGameId);
        
        bestScore = SU.getDefaultScore(MyPerGameId);
        
        // check if the score we can get with the new status is greater than the initial one
        // return true if it is, false otherwise





        List<Double> tweights;

    for(ArrayList<Double> weightProb : constants.retroProbWeights ){
        tweights = weightProb.subList(1, weightProb.size());

            aScore += weightProb.get(0) * socialPref(cgs, MyPerGameId, OppPerGameId, MyPerGameId, OppPerGameId, proposal,
                                   PropDefaultScore, OppDefaultScore,   tweights.get(0), tweights.get(1), tweights.get(2), tweights.get(3)    );

            rScore += weightProb.get(0) * socialPref(cgs, MyPerGameId, OppPerGameId, MyPerGameId, OppPerGameId,
                    proposal, PropDefaultScore, OppDefaultScore,   tweights.get(0), tweights.get(1), tweights.get(2), tweights.get(3)    );

            System.out.println("aScore: " + aScore + " rScore " + rScore);
        }

        boolean accept = false;

        if(aScore >= rScore) {
            System.out.println("Accepting the proposal");
            accept = true;
        }
        else {
            System.out.println("Rejecting the proposal");
            accept = false;
        }

        return accept;
    }



    
    private static double socialPref(GameStatus gs, int ProposerId, int ResponderId,
                                        int   perGameId, int OpponentId, ChipSet offer,
                double nnP, double nnR, double wIB, double wAU, double wAO, double wAT){



            ScoringUtility SU = new ScoringUtility(gs, ProposerId, ResponderId);
            double socialPref = 0;
            //first we calculate individual benefits:
            PlayerStatus Proposer = new PlayerStatus(gs.getPlayerByPerGameId(ProposerId));
            PlayerStatus Responder = new PlayerStatus(gs.getPlayerByPerGameId(ResponderId));

            double[] nnScore = new double[2];
            nnScore[ProposerId] = nnR;
            nnScore[ResponderId] = nnP;

            double[] acceptedScore = new double[2];

            acceptedScore[ProposerId] = SU.getOfferScore(offer, ProposerId) ;
            acceptedScore[ResponderId] = SU.getOfferScore(offer, ResponderId);



            double[] benefit = new double[2];


            benefit[ProposerId] = acceptedScore[ProposerId] - nnScore[ProposerId];
            benefit[ResponderId] = acceptedScore[ResponderId] - nnScore[ResponderId];

    
            double socialUtiility = wIB*benefit[perGameId] + wAU*(benefit[perGameId]+benefit[OpponentId])
                + wAO*(acceptedScore[perGameId]-acceptedScore[OpponentId]) + wAT*(benefit[perGameId]-benefit[OpponentId]);
        
//        System.out.println("IB: " + IB + " AU " + AU + " AO " + AO + " AT " + AT);
//        System.out.println("Payoff for responder: " + payoffForResponder);
                
        return(socialUtiility/300);
    }
    
    
    
    public static void updateTypeProbs(ArrayList<ArrayList<Double>> probWeights, GameStatus gs, int ProposerId, int ResponderId, 
            int perGameId, int opponentId, ChipSet offer, Boolean accepted){
        
        ArrayList<Double> tProbs = new ArrayList<Double>(probWeights.size());
        ArrayList<Double> margProbA = new ArrayList<Double>(probWeights.size());
        ArrayList<Double> margProbR = new ArrayList<Double>(probWeights.size());
        List<Double> tweights;
        
        double margProbAccept;
        double responderPayoff;
        
        ScoringUtility SU = new ScoringUtility(gs, ProposerId, ResponderId);
        double MyDefaultScore = SU.getDefaultScore(ProposerId);
        double OppDefaultScore = SU.getDefaultScore(ResponderId);
        
        if(perGameId == ProposerId){
            //We are updating for the Responder
            for( ArrayList<Double> l : probWeights ){
                
                tweights = l.subList(1, l.size());
                
                responderPayoff = socialPref(gs, ProposerId, ResponderId, perGameId, opponentId, offer, 
                            MyDefaultScore, OppDefaultScore, tweights.get(0), tweights.get(1), tweights.get(2), tweights.get(3));
                
                System.out.println("Responder payoff: " + responderPayoff);
                
                double expaccept = Math.exp(responderPayoff);
                margProbAccept = expaccept / ( 1 + expaccept);
                
                
                margProbA.add( l.get(0) * margProbAccept);
                margProbR.add( l.get(0) * ( 1 - margProbAccept));
                System.out.println("p: " + l.get(0) + " margProbAccept " + margProbAccept + " accept: " + l.get(0) * margProbAccept + " reject: " + l.get(0) * ( 1 - margProbAccept));
                
                        
            }
            
            double probAccept = 0;
            for(Double td : margProbA)
                probAccept += td;
//            System.out.println("probAccept = " + probAccept);
            //pR now has the probability of an offer being accepted
            
            for( int i = 0; i < margProbA.size(); i++ ){
                if(accepted){
                    tProbs.add( margProbA.get(i) / probAccept );
                } else {
                    tProbs.add( margProbR.get(i) / ( 1 - probAccept) );
//                    System.out.println(margProbR.get(i) / ( 1 - probAccept) );
                }
            }
            
            for(int i = 0; i < margProbA.size(); i++ ){
                double d = tProbs.get(i);
                System.out.println("Prob was: " + probWeights.get(i).get(0) + ", now is: " + d);
                probWeights.get(i).set(0, d);
 
            }
            
        } 
        
    }
 }
