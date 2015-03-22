package ctagents.recipagents.CopyCatAgent;


import RecipExperiment.CreateGame;
import RecipExperiment.RecipLogic;
import RecipExperiment.RecipConstants;
import ctagents.recipagents.PhaseWaiter;
import ctagents.recipagents.RecipAgent;
import edu.harvard.eecs.airg.coloredtrails.shared.ScoringUtility;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;



public class CopyCatAgent extends RecipAgent{
    ScoringUtility SU;
    
    CreateGame cg;

    double[] merit = new double[2];

    RecipLogic RL = new RecipLogic();
    double[] weights = {.25, .25, 29.37, .25};
    ChipSet myOffer;
    
    RecipConstants constants = new RecipConstants();
    
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
                
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    logr.log(Level.FATAL, null, ex);
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
    
    public Object strategy(Object o) {
        ArrayList<ChipSet> mostBeneficialExchange = strategy(cgs, MyPerGameId, OppPerGameId, constants, RL, merit, logr);
        myOffer = ChipSet.subChipSets(mostBeneficialExchange.get(1), mostBeneficialExchange.get(0));
        return(mostBeneficialExchange);
    }
    //myOffer = ChipSet.subChipSets(mostBeneficialExchange.get(1), mostBeneficialExchange.get(0));
    
    public static ArrayList<ChipSet> strategy(GameStatus gs, int ProposerId, int ResponderId, RecipConstants constants,
            RecipLogic RL, double[] merit, Logger logr) {
        
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
        //ArrayList<Double> socialScoreReject = new ArrayList<Double>();
        //ArrayList<Double> probAccept = new ArrayList<Double>();
        
        
        
        
        // search all exchanges
        int i = 0;
        for(ArrayList<ChipSet> exchange:allExchanges) {
            //System.out.println("-------------------------------------------------");

            ChipSet senderChips = exchange.get(0);
            ChipSet recipientChips = exchange.get(1);

            ChipSet Offer = ChipSet.subChipSets(recipientChips, senderChips);
            
            double aScore = 0;

            
            List<Double> weights;
            for(ArrayList<Double> weightProb : constants.retroProbWeights ){
//                logr.info("New set of weights");
                
                weights = weightProb.subList(1, weightProb.size());

                aScore += weightProb.get(0) * RL.socialPref(gs, ProposerId, ResponderId, ProposerId, ResponderId, 
                        Offer, true, merit, weights, false);
            }
            socialScoreAccept.add( new Double(aScore));
            
            
            i++;
        }
        

        
        double sumSSA = 0;
        ArrayList<Double> expSocialScoreAccept = new ArrayList<Double>();
        for(Double d : socialScoreAccept){
            //logr.info("ssa: " + d + " exp ssa " + Math.exp(d));
            expSocialScoreAccept.add(Math.exp(d));
            sumSSA += Math.exp(d);
        }
        
        double dp = expSocialScoreAccept.get(0) / sumSSA;
        double p;
        for(int l = 0; l < expSocialScoreAccept.size(); l++){
            p = expSocialScoreAccept.get(l) / sumSSA;
            //logr.info("p: " + p + " dp: " + dp );
            if( p >= dp){
                mostBeneficialExchange = allExchanges.get(l);
                dp = expSocialScoreAccept.get(l) / sumSSA;
            }
        }
        
        //myOffer = ChipSet.subChipSets(mostBeneficialExchange.get(1), mostBeneficialExchange.get(0));
        return mostBeneficialExchange;
    }
    

    private void updateMerit(int perGameId, double deltaMerit){
        System.out.println("Player " + perGameId + "'s merit was "+ merit[perGameId] + " and will now change by " + deltaMerit);
        merit[perGameId] += deltaMerit;
    }
  
	 
    
    //OFFER ALWAYS GIVEN AS (chips sent by recipient) - (chips sent by proposer)
    
    
    @Override
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
        }
        
        System.out.println("aScore: " + aScore + " rScore " + rScore);
        
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

    @Override
    public void newRound(int ProposerId, int ResponderId){
        SU = new ScoringUtility(cgs, ProposerId, ResponderId);
    }
    
}