package ctagents.recipagents.FairnessAgent;


import ctagents.recipagents.RecipAgent;
import edu.harvard.eecs.airg.coloredtrails.shared.ScoringUtility;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;
import java.util.ArrayList;
import java.util.Set;
import org.apache.log4j.Logger;


public class FairnessAgent extends RecipAgent {

    ScoringUtility SU;
    
    @Override
    public Object strategy(Object o) {
        SU = new ScoringUtility(cgs, MyPerGameId, OppPerGameId);
        return(strategy(cgs, MyPerGameId, OppPerGameId, SU, logr) );
    }
    
    public static ArrayList<ChipSet> strategy(GameStatus gs, int ProposerId, int ResponderId, ScoringUtility SU,
            Logger logr){
        
        
        // Get all possible unique exchanges between the players
        ArrayList<ArrayList<ChipSet>> allExchanges = new ArrayList<ArrayList<ChipSet>>(
                ChipSet.getAllExchanges(gs.getPlayerByPerGameId(ProposerId).getChips(), 
                gs.getPlayerByPerGameId(ResponderId).getChips()));
        
//        ArrayList<ChipSet> offers = new ArrayList<ChipSet>();
//        for(ArrayList<ChipSet> offer : allExchanges)
//            offers.add( ChipSet.subChipSets( offer.get(1), offer.get(0)) );

//        System.out.println("Total number of unique exchanges: " + allExchanges.size());
        ArrayList<ChipSet> mostBeneficialExchange = null;
        
        ChipSet bestoffer = null;
        double utility;
        
//        double bestUtility = utility(gs, ChipSet.subChipSets( allExchanges.get(0).get(1), allExchanges.get(0).get(0) ), 
//                true, ProposerId, ResponderId, ProposerId, ResponderId, SU, logr);
        double bestUtility = -100000000000000.0;
        
        ArrayList<Double> weights = new ArrayList();
        weights.add(40.0);
        //weights.add(.8);
        
        ChipSet offer;
        
        for(ArrayList<ChipSet> exchange : allExchanges){
            offer = ChipSet.subChipSets(exchange.get(1), exchange.get(0) );
            
            utility = 0;
            for(Double w : weights)
                utility += utility(gs, offer, true, ProposerId, ResponderId, ProposerId, ResponderId, SU, logr, w);
            
            
            if(utility >= bestUtility){
                bestUtility = utility;
                mostBeneficialExchange = exchange;
//                logr.info("Found better exchange: " + offer.toString());
                bestoffer = offer;
                
            }
        }

//        logr.info("Offering: " + bestoffer);
        
        double Kp = 0;
        double Kr = 0;
        
        Kp = kindness(gs, bestoffer, true, ProposerId, ResponderId, ProposerId, ResponderId, SU);
        Kr = kindness(gs, bestoffer, true, ProposerId, ResponderId, ResponderId, ProposerId, SU);
        
        logr.info("Kp: " + Kp + " Kr: " + Kr);
        
        return( mostBeneficialExchange );
    }
    

    //OFFER ALWAYS GIVEN AS (chips sent by recipient) - (chips sent by proposer)
    
    
    @Override
    public Boolean RespondStrategy(GameStatus gs, ChipSet proposal, int ProposerId, int ResponderId) {
        SU = new ScoringUtility(cgs, ProposerId, ResponderId);
        
        Boolean response = false;
        
        double alpha = 1.0;
        double accept = utility(gs, proposal, true, ProposerId, ResponderId, MyPerGameId, OppPerGameId, SU, logr, alpha);
        double reject = utility(gs, proposal, false, ProposerId, ResponderId, MyPerGameId, OppPerGameId, SU, logr, alpha);
        
        logr.info("accept: " + accept + " reject " + reject);
        
        if(accept >= reject)
            response = true;
        
        return(response);
    }
    
    private static double kindness(GameStatus gs, ChipSet offer, Boolean response, int ProposerId, int ResponderId, 
            int perGameId, int OpponentId, ScoringUtility SU ){
        
        double kindness = 0;
        double nb = SU.getNashBargainScore(OpponentId);
        
        if(perGameId == ProposerId){
            //Proposer
            //(kindness of proposer to responder
            
            double score = SU.getOfferScore(offer, OpponentId);
            
            kindness = score - nb;
            
        } else {
            //Responder
            if(response){
                //accepted offer
                
                double score = SU.getOfferScore(offer, OpponentId);
                
                kindness = score - nb;
                
            } else {
                //rejected offer
                
                double score = SU.getDefaultScore(OpponentId);
                kindness = score - nb;
                
            }
        }
        
        kindness = kindness / (SU.getMaxScore(perGameId) - SU.getMinScore(perGameId));
        
        return( kindness );
    }
    
    private static double utility(GameStatus gs, ChipSet offer, Boolean response, int ProposerId, int ResponderId, 
            int perGameId, int OpponentId, ScoringUtility SU, Logger logr, double alpha){
        
        double utility = 0;
        
        double payoff;
        if(response){
            payoff = SU.getOfferScore(offer, perGameId);
        } else {
            payoff = SU.getDefaultScore(perGameId);
        }
        
        double Kp = 0;
        double Kr = 0;
        
        if(perGameId == ProposerId){
            Kp = kindness(gs, offer, response, ProposerId, ResponderId, perGameId, OpponentId, SU);
            Kr = kindness(gs, offer, response, ProposerId, ResponderId, OpponentId, perGameId, SU);
        } else {
            Kp = kindness(gs, offer, response, ProposerId, ResponderId, OpponentId, perGameId, SU);
            Kr = kindness(gs, offer, response, ProposerId, ResponderId, perGameId, OpponentId, SU);
        }
        
        
        utility = payoff + alpha*Kp*( 1 + Kr );
        
//        logr.info("payoff: " + payoff + " kp " + Kp + " kr " + Kr + " util "  +  utility);
        
        return(utility);
    
    }
}