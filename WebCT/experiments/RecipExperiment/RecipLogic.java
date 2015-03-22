package RecipExperiment;

import ctagents.recipagents.*;
import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.shared.ScoringUtility;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;
import java.util.ArrayList;
import java.util.List;


public class RecipLogic{
    
    public static double getMerit(GameStatus gs, int ProposerId, int ResponderId, int perGameId, int OpponentId, 
            ChipSet Offer, Boolean Response, ScoringUtility SU){
        //We are computing the merit for player (perGameId).
        //Ie, we are computing pi(perGameId)
        
        Scoring scoring = gs.getScoring();
        
        
        
        //ScoringUtility SU = new ScoringUtility(gs, ProposerId, ResponderId);
        double piMin = SU.getMinScore(OpponentId);
        double piMax = SU.getMaxScore(OpponentId);
        
        //double piNB = SU.getNashBargainScore(OpponentId);
        double piFM = SU.getFairMaxScore(OpponentId);
        //System.out.println("NB: " + piNB + " SU " + SU.getNashBargainScore(OpponentId));
        
        double piC;
        
        if(perGameId == ResponderId){
            if(Response){
                //piC = benefit to proposer
                PlayerStatus Opponent = new PlayerStatus(gs.getPlayerByPerGameId(OpponentId));
            
                BestUse bu = new BestUse(gs, Opponent, scoring, 0);
                //double nn = bu.getBestState().getScore();

                Opponent.setChips(ChipSet.addChipSets(Opponent.getChips(), Offer));

                bu = new BestUse(gs, Opponent, scoring, 0);
                piC = bu.getBestState().getScore();

                //piC = os;
            } else {
                piC = 0;
            }
        } else {
            //perGameId is a proposer
            //piC is benefit of exchange to responder
            //need non neg for B
            PlayerStatus Opponent = new PlayerStatus(gs.getPlayerByPerGameId(OpponentId));
            
            BestUse bu = new BestUse(gs, Opponent, scoring, 0);
            //double nn = bu.getBestState().getScore();
            
            Opponent.setChips(ChipSet.addChipSets(Opponent.getChips(), ChipSet.getNegation(Offer)));
            
            bu = new BestUse(gs, Opponent, scoring, 0);
            piC = bu.getBestState().getScore();
            
            //piC = os;
        }
        
        double merit = (piC - piFM) / (piMax - piMin);
        System.out.println("piMin: " + piMin + " piMax: " + piMax + " piC " + piC + " piFM" + piFM);
        return(merit);
    }

    public static double socialPref(GameStatus gs, int ProposerId, int ResponderId, int perGameId, int OpponentId, 
            ChipSet offer, Boolean response, double[] merit, List<Double> weights, Boolean Prospective){
        
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
                acceptedScore[ProposerId] = 0;
                acceptedScore[ResponderId] = 0;
            }

            double[] benefit = new double[2];
            benefit[ProposerId] = acceptedScore[ProposerId] - nnScore[ProposerId];
            benefit[ResponderId] = acceptedScore[ResponderId] - nnScore[ResponderId];


            //Now we have to compute retrospective benefit
            //at this point, we have merit up to here. 


            double retroBenefit = merit[OpponentId]*benefit[OpponentId];

            if(Prospective){
                CreateGame cg= new CreateGame();
                ArrayList<GameStatus> games = cg.getGames(5);
                
                if(perGameId == ProposerId){
                    //Player is proposer
                } else {
                    //Player is responder
                }
            } else {
                socialPref = weights.get(0)*benefit[perGameId] + weights.get(1)*benefit[OpponentId] + weights.get(2)*retroBenefit;
                socialPref = socialPref / 300;
            }
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
            ChipSet offer, double[] merit, List<Double> weights){
//        double acceptPayoff = socialPref(gs, ProposerID, ResponderID, perGameId, opponentId, offer, true, merit, weights, false);
//        double rejectPayoff = socialPref(gs, ProposerID, ResponderID, perGameId, opponentId, offer, false, merit, weights, false);
        double acceptPayoff = socialPref(gs, ProposerID, ResponderID, opponentId, perGameId, offer, true, merit, weights, false);
        double rejectPayoff = socialPref(gs, ProposerID, ResponderID, opponentId, perGameId, offer, false, merit, weights, false);
        
        double expaccept = Math.exp(acceptPayoff);
        double expreject = Math.exp(rejectPayoff);
        double paccept = expaccept / ( expreject + expaccept);
        return(paccept);
    }
    
    public static void updateTypeProbs(ArrayList<ArrayList<Double>> probWeights, GameStatus gs, int ProposerId, int ResponderId, 
            int perGameId, int opponentId, ChipSet offer, double[] merit, Boolean accepted){
        
        ArrayList<Double> tProbs = new ArrayList<Double>(probWeights.size());
        ArrayList<Double> margProbA = new ArrayList<Double>(probWeights.size());
        ArrayList<Double> margProbR = new ArrayList<Double>(probWeights.size());
        List<Double> tweights;
        
        double margProbAccept;
        
        
        if(perGameId == ProposerId){
            //We are updating for the Responder
            for( ArrayList<Double> l : probWeights ){
                
                tweights = l.subList(1, l.size());
                
                margProbAccept = probOfferAccepted(gs, ProposerId, ResponderId, perGameId, opponentId, offer, merit, tweights);
                
                margProbA.add( l.get(0) * margProbAccept);
                margProbR.add( l.get(0) * ( 1 - margProbAccept));
//                System.out.println("p: " + l.get(0) + " accept: " + l.get(0) * margProbAccept + " reject: " + l.get(0) * ( 1 - margProbAccept));
                
                        
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
                probWeights.get(i).set(0, d);
//                System.out.println("Prob was: " + probWeights.get(i).get(0) + ", now is: " + d);
            }
            
        } else {
            //We are updating for the Proposer
            //need to figure out the prob of the given offer.
            //for each set of weights, we need to generate all possible offers
            //use these to calc the prob of the offer that was made
            //then simply apply bayes rule and have at it.
            //not so bad really
            
            //RecipLogic RL = new RecipLogic();

            ArrayList<ArrayList<Double>> socialScoreAccept = new ArrayList<ArrayList<Double>>();
            
            ArrayList<ArrayList<ChipSet>> allExchanges = new ArrayList<ArrayList<ChipSet>>(
                ChipSet.getAllExchanges(gs.getPlayerByPerGameId(ProposerId).getChips(), 
                gs.getPlayerByPerGameId(ResponderId).getChips()));
            
            List<Double> weights;
            ArrayList<Double> tsocialScoreAccept;

            ChipSet senderChips;
            ChipSet recipientChips;
            ChipSet tmpOffer;
            double aScore;
                    
            for(ArrayList<Double> pweights : probWeights){
                tsocialScoreAccept = new ArrayList<Double>();
                weights = pweights.subList(1, pweights.size());
                
                
                for(ArrayList<ChipSet> exchange:allExchanges) {
                    //System.out.println("-------------------------------------------------");

                    senderChips = exchange.get(0);
                    recipientChips = exchange.get(1);

                    tmpOffer = ChipSet.subChipSets(recipientChips, senderChips);

                    aScore = socialPref(gs, ProposerId, ResponderId, ProposerId, ResponderId, 
                        tmpOffer, true, merit, weights, false);
                    
                    tsocialScoreAccept.add(aScore);
                }

                socialScoreAccept.add(tsocialScoreAccept);
                            
            }
            
            //Now we need to generate marginal probs, but first regular probs
            ArrayList<Double> expSums = new ArrayList<Double>();
            
            double expscore = 0;
//            double p;
            for(ArrayList<Double> scores : socialScoreAccept){
                expscore = 0;
                for(Double d : scores){
                    expscore += Math.exp(d);
                }
                expSums.add(expscore);
                
            }
            
            double p;
            double pOffer = 0;
            double marg;
            ArrayList<Double> offerProbs = new ArrayList<Double>();
            ArrayList<Double> margProbs = new ArrayList<Double>();
            int i = 0;
            for(ArrayList<Double> pweights : probWeights){
                weights = pweights.subList(1, pweights.size());
                p = Math.exp( socialPref(gs, ProposerId, ResponderId, ProposerId, ResponderId, offer, true, merit, weights, false)) / expSums.get(i);
                offerProbs.add(p);
                marg = p * pweights.get(0);
                margProbs.add( marg );
                pOffer += marg;
                i++;
            }
            
            ArrayList<Double> newProbs = new ArrayList<Double>();
            
            for( Double margProb : margProbs ){
                newProbs.add( margProb / pOffer );
            }
            
//            for( Double newProb : newProbs )
//                System.out.println("new prob: " + newProb);
            
            //finally, update the probabilities
            for( int j = 0; j < probWeights.size(); j++ ){
                probWeights.get(j).set(0, newProbs.get(j));
            }
            
        }
        
    }
}