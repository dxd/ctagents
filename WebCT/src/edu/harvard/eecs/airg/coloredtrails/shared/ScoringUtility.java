/*
	Colored Trails
	
	Copyright (C) 2008, President and Fellows of Harvard College.  All Rights Reserved.
	
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

package edu.harvard.eecs.airg.coloredtrails.shared;

import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;



/**
 * General use class for computing common values related to chip exchage:
 * NashBargaining Solution, min and max score, and default score
 * Other utility functions for score calculations
 * 
 * Offers are of the form (chips sent by responder) - (chips sent by proposer)
 * 
 * Should be rewritten to use double for speed in loops, hashmaps elsewhere for ease of use
 * 
 * @author legodude
 */
public class ScoringUtility implements Serializable {
    GameStatus gs;
    int ProposerId;
    int ResponderId;
    
    Boolean beenRun = false;
    
    PlayerStatus Proposer, Responder;
    
    Scoring scoring;
    
    
    //These really all should be hashes
    double[] maxScore = new double[2];
    double[] minScore = new double[2];
    
    ChipSet[] maxOffer = new ChipSet[2];
    ChipSet[] minOffer = new ChipSet[2];
    HashMap<Integer, Double> defaultScore = new HashMap<Integer, Double>(2);
    
    HashMap<Integer, Double> nbScore = new HashMap<Integer, Double>(2);
    ChipSet nbOffer = null;
    
    HashMap<Integer, Double> fmScore = new HashMap<Integer, Double>(2);
    ChipSet fmOffer;
    
    /**
     * Computes the NashBargaining, best and worst possible offers (if any) for players, as well as default scores 
     * @param gs
     * @param ProposerId
     * @param ResponderId
     */
    public ScoringUtility(GameStatus gs, int ProposerId, int ResponderId){
        this.gs = gs;
        this.ProposerId = ProposerId;
        this.ResponderId = ResponderId;
        
        Proposer = gs.getPlayerByPerGameId(ProposerId);
        Responder = gs.getPlayerByPerGameId(ResponderId);
        
        scoring = gs.getScoring();
        
        // Get each agent's best score with the initial chipset
        defaultScore.put(ProposerId, getPlayerScore(Proposer.getChips(), ProposerId) );
        defaultScore.put(ResponderId, getPlayerScore(Responder.getChips(), ResponderId) );
        
        //computeOffer();
    }
    
    private void computeOffer(){
        // Get all possible unique exchanges between the players
        Set<ArrayList<ChipSet>> allExchanges = ChipSet.getAllExchanges(
                        Proposer.getChips(), Responder.getChips());

        //System.out.println("Total number of unique exchanges: " + allExchanges.size());
        HashMap<Integer, ArrayList<ChipSet>> maxBeneficialExchange = new HashMap<Integer, ArrayList<ChipSet>>(2);
        HashMap<Integer, ArrayList<ChipSet>> minBeneficialExchange = new HashMap<Integer, ArrayList<ChipSet>>(2);


                //
        maxScore[ProposerId] = defaultScore.get(ProposerId);
        maxScore[ResponderId] = defaultScore.get(ResponderId);
        minScore[ProposerId] = defaultScore.get(ProposerId);
        minScore[ResponderId] = defaultScore.get(ResponderId);
        double[] newScore = new double[2];

        //Nash Bargaining Stuff:
//        double nbBestScore = defaultScore.get(ProposerId)*defaultScore.get(ResponderId);
        double nbBestScore = 0;
        double nbNewScore;
        
        //FairMax
        double fmBestScore = defaultScore.get(ProposerId)*defaultScore.get(ResponderId);
        double fmNewScore;
        
        //System.out.println("Defaultscore = " + DefaultScore);
        ChipSet offer;

        // search all exchanges
        for(ArrayList<ChipSet> exchange:allExchanges) {
                ChipSet senderChips = exchange.get(0);
                ChipSet recipientChips = exchange.get(1);
                
                offer = ChipSet.subChipSets(recipientChips, senderChips);

                
                newScore[ProposerId] = getOfferScore( offer, ProposerId);
                newScore[ResponderId] = getOfferScore( offer, ResponderId);
                //System.out.println("This exchange yields a score of: " + newScore);
                if(newScore[ProposerId] >= maxScore[ProposerId]){
                    maxScore[ProposerId] = newScore[ProposerId];
                    maxBeneficialExchange.put(ProposerId, new ArrayList<ChipSet>(exchange));
                }
                if(newScore[ProposerId] <= minScore[ProposerId]){
                    minScore[ProposerId] = newScore[ProposerId];
                    minBeneficialExchange.put(ProposerId, new ArrayList<ChipSet>(exchange));
                }
                //God I hate double typing this stuff
                if(newScore[ResponderId] >= maxScore[ResponderId]){
                    maxScore[ResponderId] = newScore[ResponderId];
                    maxBeneficialExchange.put(ResponderId, new ArrayList<ChipSet>(exchange));
                }
                if(newScore[ResponderId] <= minScore[ResponderId]){
                    minScore[ResponderId] = newScore[ResponderId];
                    minBeneficialExchange.put(ResponderId, new ArrayList<ChipSet>(exchange));
                }
                
                //FairMax
                fmNewScore = newScore[ProposerId]*newScore[ResponderId];
                if(fmNewScore >= fmBestScore){
                    fmBestScore = fmNewScore;
                    fmOffer = offer;
                    fmScore.put(ProposerId, newScore[ProposerId]);
                    fmScore.put(ResponderId, newScore[ResponderId]);
                }

                
                //NB Stuff
                //first check if both offers are at least as good as nonnegotiation
                double benefitProposer = newScore[ProposerId] - defaultScore.get(ProposerId);
                double benefitResponder = newScore[ResponderId] - defaultScore.get(ResponderId);
                if( (benefitProposer > 0) && (benefitResponder > 0)){
                    nbNewScore = benefitProposer * benefitResponder;
                    if(nbNewScore > nbBestScore){
                        nbBestScore = nbNewScore;
                        nbOffer = offer;
                        nbScore.put(ProposerId, newScore[ProposerId]);
                        nbScore.put(ResponderId, newScore[ResponderId]);
                    }
                }

        }
        
        if(nbOffer == null){
            //there is no Nash Bargaining offer
            nbScore.put(ProposerId, defaultScore.get(ProposerId));
            nbScore.put(ResponderId, defaultScore.get(ResponderId));
            nbOffer = new ChipSet();
        }
        
        maxOffer[ProposerId] = ChipSet.subChipSets( maxBeneficialExchange.get(ProposerId).get(1), maxBeneficialExchange.get(ProposerId).get(0) );
        minOffer[ProposerId] = ChipSet.subChipSets( minBeneficialExchange.get(ProposerId).get(1), minBeneficialExchange.get(ProposerId).get(0) );
        
        maxOffer[ResponderId] = ChipSet.subChipSets( maxBeneficialExchange.get(ResponderId).get(1), maxBeneficialExchange.get(ResponderId).get(0) );
        minOffer[ResponderId] = ChipSet.subChipSets( minBeneficialExchange.get(ResponderId).get(1), minBeneficialExchange.get(ResponderId).get(0) );
        
        
        
        beenRun = true;
    }
 
    /**
     * Returns offer that yields max score for perGameId
     * @param perGameId
     * @return
     */public ChipSet getMaxOffer(int perGameId) {
        if(!beenRun)
            computeOffer();
        return(maxOffer[perGameId]);
    }
    
    /**
     * Returns offer that yields min score for perGameId
     * @param perGameId
     * @return
     */
     public ChipSet getMinOffer(int perGameId) {
        if(!beenRun)
            computeOffer();
        return(minOffer[perGameId]);
    }
    
    /**
     * Returns max possible score
     * @param perGameId
     * @return
     */
     public double getMaxScore(int perGameId){
        if(!beenRun)
            computeOffer();
        return(maxScore[perGameId]);
    }
    
    /**
     * Min possible score
     * @param perGameId
     * @return
     */
     public double getMinScore(int perGameId){
        if(!beenRun)
            computeOffer();
        return(minScore[perGameId]);
    }
    
    /**
     * Returns non-negotiation score
     * @param perGameId
     * @return
     */
     public double getDefaultScore(int perGameId){
        return(defaultScore.get(perGameId));
    }
    
    /**
     *  Offer for Nash Bargaining solution
     *  ***Returns empty ChipSet if no such solution exists***
     * @return
     */
    public ChipSet getNashBargainOffer(){
        if(!beenRun)
            computeOffer();
        return(nbOffer);
    }
    
    /**
     * Score if NB solution accepted
     * 
     * @param perGameId
     * @return Score for NB solution, otherwise if there is no NB solution, returns the default score
     */
    public double getNashBargainScore(int perGameId){
        if(!beenRun)
            computeOffer();
        return(nbScore.get(perGameId));
    }
    
    /**
     *  Offer for FairMax solution
     * @return
     */
    public ChipSet getFairMaxOffer(){
        if(!beenRun)
            computeOffer();
        return(fmOffer);
    }
    
    /**
     * Score if FairMax solution accepted
     * @param perGameId
     * @return
     */
    public double getFairMaxScore(int perGameId){
        if(!beenRun)
            computeOffer();
        return(fmScore.get(perGameId));
    }
    
    public void printGameScores(){
        if(!beenRun)
            computeOffer();
        
        System.out.println("max proposer score: " + maxScore[ProposerId] + " " + maxOffer[ProposerId].toString());
        System.out.println("min proposer score: " + minScore[ProposerId] + " " + minOffer[ProposerId].toString());
        System.out.println("max responder score: " + maxScore[ResponderId] + " " + maxOffer[ResponderId].toString());
        System.out.println("min responder score: " + minScore[ResponderId] + " " + minOffer[ResponderId].toString());

        
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println("Nash bargaining solution is : " + nbOffer.toString());
        System.out.println("Proposer score: " + nbScore.get(ProposerId) + " responder score: " + nbScore.get(ResponderId));
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println("FairMax solution is : " + fmOffer.toString());
        System.out.println("Proposer score: " + fmScore.get(ProposerId) + " responder score: " + fmScore.get(ResponderId));
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    }
    /**
     * Returns the score associated with any offer
     * @param offer
     * @param perGameId
     * @return
     */
    public double getOfferScore(ChipSet offer, int perGameId){
        PlayerStatus ps = new PlayerStatus(gs.getPlayerByPerGameId(perGameId));
        
        if(perGameId == ProposerId)
            return( getPlayerScore( ChipSet.addChipSets(ps.getChips(), offer) , perGameId ));
        else
            return( getPlayerScore( ChipSet.addChipSets(ps.getChips(), ChipSet.getNegation(offer)) , perGameId ) );
   
    }
    
    /**
     * Returns the score of a player with these chips
     * @param chips
     * @param perGameId
     * @return
     */
    public double getPlayerScore(ChipSet chips, int perGameId){
        PlayerStatus ps = new PlayerStatus(gs.getPlayerByPerGameId(perGameId));
        ps.setChips(chips);
        
        BestUse bu = new BestUse(gs, ps, gs.getScoring(), 0);
        
        return(bu.getBestState().getScore());
    }
}