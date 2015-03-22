package ctagents.recipagents.FutureMeritBestResponse;


import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.shared.ScoringUtility;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;
import java.util.ArrayList;
import java.util.List;

public class FMBRLogic {
    
    
    public static double getMerit(GameStatus gs, int ProposerId, int ResponderId, int perGameId, int OpponentId, 
            ChipSet Offer, Boolean Response, ScoringUtility SU){
        //We are computing the merit for player (perGameId).
        //Ie, we are computing pi(perGameId)
        
        Scoring scoring = gs.getScoring();
        
        
        
        //ScoringUtility SU = new ScoringUtility(gs, ProposerId, ResponderId);
        double piMin = SU.getMinScore(OpponentId);
        double piMax = SU.getMaxScore(OpponentId);
        
        double piNB = SU.getNashBargainScore(OpponentId);
	//        double piFM = SU.getFairMaxScore(OpponentId);
        //System.out.println("NB: " + piNB + " SU " + SU.getNashBargainScore(OpponentId));
        
        double piC;
        
        if(perGameId == ResponderId){
            if(Response){
                //piC = benefit to proposer
//                PlayerStatus Opponent = new PlayerStatus(gs.getPlayerByPerGameId(OpponentId));
//            
//                BestUse bu = new BestUse(gs, Opponent, scoring, 0);
//                //double nn = bu.getBestState().getScore();
//
//                Opponent.setChips(ChipSet.addChipSets(Opponent.getChips(), Offer));
//
//                bu = new BestUse(gs, Opponent, scoring, 0);
//                piC = bu.getBestState().getScore();
                
                piC = SU.getOfferScore(Offer, OpponentId);

                //piC = os;
            } else {
                piC = 0;
            }
        } else {
            //perGameId is a proposer
            //piC is benefit of exchange to responder
            //need non neg for B
//            PlayerStatus Opponent = new PlayerStatus(gs.getPlayerByPerGameId(OpponentId));
//            
//            BestUse bu = new BestUse(gs, Opponent, scoring, 0);
//            //double nn = bu.getBestState().getScore();
//            
//            Opponent.setChips(ChipSet.addChipSets(Opponent.getChips(), ChipSet.getNegation(Offer)));
//            
//            bu = new BestUse(gs, Opponent, scoring, 0);
//            piC = bu.getBestState().getScore();
            
            piC = SU.getOfferScore(Offer, OpponentId);
            
            //piC = os;
        }
        
        double merit = (piC - piNB) / (piMax - piMin);
        //System.out.println("piMin: " + piMin + " piMax: " + piMax + " piC " + piC + " piNB" + piNB);
        return(merit);
    }

    public static double socialPref(GameStatus gs, int ProposerId, int ResponderId, int perGameId, int OpponentId, 
            ChipSet offer, Boolean response, double[] merit, List<Double> weights, ArrayList<ArrayList<Double>> coeffs,
            int horizon, ScoringUtility SU){
        
        //if(response){
            
            double socialPref = 0;
            //first we calculate individual benefits:
            PlayerStatus Proposer = new PlayerStatus(gs.getPlayerByPerGameId(ProposerId));
            PlayerStatus Responder = new PlayerStatus(gs.getPlayerByPerGameId(ResponderId));

            double[] nnScore = new double[2];
            nnScore[ProposerId] = getBestScore(gs, Proposer, Proposer.getChips());
            nnScore[ResponderId] = getBestScore(gs, Responder, Responder.getChips());

            double[] acceptedScore = new double[2];
            if(response){
                acceptedScore[ProposerId] = getBestScore(gs, Proposer, ChipSet.addChipSets(Proposer.getChips(), offer));
                acceptedScore[ResponderId] = getBestScore(gs, Responder, ChipSet.addChipSets(Responder.getChips(), ChipSet.getNegation(offer)));
            }else {
                acceptedScore[ProposerId] = nnScore[ProposerId];
                acceptedScore[ResponderId] = nnScore[ResponderId];
            }

            double[] benefit = new double[2];
            benefit[ProposerId] = acceptedScore[ProposerId] - nnScore[ProposerId];
            benefit[ResponderId] = acceptedScore[ResponderId] - nnScore[ResponderId];

            //ScoringUtility SU = new ScoringUtility(gs, ProposerId, ResponderId);
            double deltamert = merit[perGameId] + getMerit(gs, ProposerId, ResponderId, perGameId, OpponentId, offer, response, SU);
            double meritscore = coeffs.get( horizon ).get(1) + coeffs.get( horizon ).get(0) * deltamert;
            
            
            //Now we have to compute retrospective benefit
            //at this point, we have merit up to here. 


            double retroBenefit = merit[OpponentId]*benefit[OpponentId];

            socialPref = weights.get(0)*benefit[perGameId] + weights.get(1)*benefit[OpponentId] + 
		weights.get(2)*retroBenefit + weights.get(3) * meritscore;
            socialPref = socialPref / 300;
            
            return( socialPref );   
//        } else {
//            return(0);
//        }
    }
    
    public static double getBestScore(GameStatus gs, PlayerStatus tps, ChipSet chips){
        PlayerStatus ps = new PlayerStatus(tps);
        ps.setChips(chips);
        BestUse bu = new BestUse(gs, ps, gs.getScoring(), 0);
        double score = bu.getBestState().getScore();
        return(score);
    }
    
    public static double probOfferAccepted(GameStatus gs, int ProposerID, int ResponderID, int perGameId, int opponentId, 
            ChipSet offer, double[] merit, List<Double> weights, ArrayList<ArrayList<Double>> coeffs, int horizon, ScoringUtility SU){
//        double acceptPayoff = socialPref(gs, ProposerID, ResponderID, perGameId, opponentId, offer, true, merit, weights, false);
//        double rejectPayoff = socialPref(gs, ProposerID, ResponderID, perGameId, opponentId, offer, false, merit, weights, false);
        double acceptPayoff = socialPref(gs, ProposerID, ResponderID, opponentId, perGameId, 
                offer, true, merit, weights, coeffs, horizon, SU );
        double rejectPayoff = socialPref(gs, ProposerID, ResponderID, opponentId, perGameId, 
                offer, false, merit, weights, coeffs, horizon, SU );
        
        double expaccept = Math.exp(acceptPayoff);
        double expreject = Math.exp(rejectPayoff);
        double paccept = expaccept / ( expreject + expaccept);
        return(paccept);
    }
    
}