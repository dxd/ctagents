package RecipExperiment;

import RecipExperiment.CreateGame;
import RecipExperiment.RecipLogic;
import RecipExperiment.RecipConstants;
import ctagents.recipagents.CopyCatAgent.CopyCatAgent;
import ctagents.recipagents.CopyCatFutureMerit.CopyCatFutureMerit;
import ctagents.recipagents.FairnessAgent.FairnessAgent;
import ctagents.recipagents.FutureMeritBestResponse.FMBRLogic;
import ctagents.recipagents.FutureMeritBestResponse.FutureMeritBestResponseAgent;
import ctagents.recipagents.RecipAgentAdaptor;
import ctagents.recipagents.RecipSocialPref2Agent.RecipSocialPref2Agent;
import ctagents.recipagents.RetroAgent.RetroAgent;
import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;
import edu.harvard.eecs.airg.coloredtrails.shared.ScoringUtility;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import java.util.ArrayList;
import org.apache.log4j.Logger;



public class FindDiffOffers{
    public static void main(String[] arg){
        FindDiffOffersRun tor = new FindDiffOffersRun();
    }
}

class FindDiffOffersRun
{
    public FindDiffOffersRun()
    {
        System.out.println("in testoffers");
        Logger logr = Logger.getLogger( this.getClass().getName() );
        CreateGame cg = new CreateGame();
        BestUse bu;
        RecipAgentAdaptor ra;
        PlayerStatus ps0;
        PlayerStatus ps1;
        
        int ProposerId = 0;
        int ResponderId = 1;
        
        
        
        
        //general idea is to create game, see if game is needed, if so test if it matches criteria
        
        ArrayList<ChipSet> exchange;
        ScoringUtility SU;
        
        
        RecipConstants constants = new RecipConstants();
        RecipLogic RL = new RecipLogic();
        FMBRLogic FL = new FMBRLogic();
        double[] merit = { 0.5, 0.5 };
        
        ChipSet RetroOffer, CopyCatOffer;
        ChipSet FutureOffer, FutureCopyCatOffer;
        
        GameStatus gs;
        
        
        for(int i = 0; i < 1000; i++){
            if(i % 5 == 0)
                System.out.println("i: " + i);
        
            gs = cg.getGame();
            SU = new ScoringUtility(gs, ProposerId, ResponderId);
            
            exchange = RetroAgent.strategy(gs, ProposerId, ResponderId, constants, RL, merit);
            RetroOffer = ChipSet.subChipSets(exchange.get(1), exchange.get(0));
            exchange = CopyCatAgent.strategy(gs, ProposerId, ResponderId, constants, RL, merit, logr );
            CopyCatOffer = ChipSet.subChipSets(exchange.get(1), exchange.get(0));

            int round = 4; 
            exchange = FutureMeritBestResponseAgent.strategy(gs, ProposerId, ResponderId, constants, FL, merit, SU, round);
            FutureOffer = ChipSet.subChipSets(exchange.get(1), exchange.get(0));
            exchange = CopyCatFutureMerit.strategy(gs, ProposerId, ResponderId, constants, FL, merit, logr, SU);
            FutureCopyCatOffer = ChipSet.subChipSets(exchange.get(1), exchange.get(0));
            
            
            
            
            if( (SU.getOfferScore(RetroOffer, ResponderId) != SU.getOfferScore(CopyCatOffer, ResponderId)) || 
                    (SU.getOfferScore(RetroOffer, ProposerId) != SU.getOfferScore(CopyCatOffer, ProposerId)) ||
                    (SU.getOfferScore(FutureOffer, ResponderId) != SU.getOfferScore(FutureCopyCatOffer, ResponderId)) || 
                    (SU.getOfferScore(FutureOffer, ProposerId) != SU.getOfferScore(FutureCopyCatOffer, ProposerId))){
            
                SU.printGameScores();
                
                System.out.println("-----------------------------------------------------------------");
                System.out.println("Proposer chips: " + gs.getPlayerByPerGameId(ProposerId).getChips());
                System.out.println("Responder chips: " + gs.getPlayerByPerGameId(ResponderId).getChips());


                System.out.println("@@@@@@@@@@@@@ Default score for Proposer: " + SU.getDefaultScore(ProposerId));
                System.out.println("@@@@@@@@@@@@@ Default score for Responder: " + SU.getDefaultScore(ResponderId));


                //NashBargain, both retro, socialpref, 
                System.out.println("NashBargain offer: " + SU.getNashBargainOffer() );

                System.out.println("");

                System.out.println("Retro best response offer:" + RetroOffer);
                System.out.println("Proposer offer score: " + SU.getOfferScore(RetroOffer, ProposerId));
                System.out.println("Responder offer score: " + SU.getOfferScore(RetroOffer, ResponderId));

                System.out.println("");

                System.out.println("CopyCatAgent offer:" + CopyCatOffer);
                System.out.println("Proposer offer score: " + SU.getOfferScore(CopyCatOffer, ProposerId));
                System.out.println("Responder offer score: " + SU.getOfferScore(CopyCatOffer, ResponderId));
                System.out.println("");

                System.out.println("Future best response offer:" + FutureOffer);
                System.out.println("Proposer offer score: " + SU.getOfferScore(FutureOffer, ProposerId));
                System.out.println("Responder offer score: " + SU.getOfferScore(FutureOffer, ResponderId));

                System.out.println("");

                System.out.println("FutureCopyCatAgent offer:" + FutureCopyCatOffer);
                System.out.println("Proposer offer score: " + SU.getOfferScore(FutureCopyCatOffer, ProposerId));
                System.out.println("Responder offer score: " + SU.getOfferScore(FutureCopyCatOffer, ResponderId));
            
            }
            
            
        } 
        
        return;
    }
    
    
    static void locs(GameStatus gs){
        PlayerStatus ps0 = gs.getPlayerByPerGameId(0);
        PlayerStatus ps1 = gs.getPlayerByPerGameId(1);
        
        BestUse bu;
        System.out.println(gs.get("gametype"));
        bu = new BestUse(gs, ps0);
        System.out.println("Proposer distance: " + bu.getBestState().getPosition().dist(gs.getBoard().getGoalLocations().get(0)));
        
        bu = new BestUse(gs, ps1);
        System.out.println("Responder distance: " + bu.getBestState().getPosition().dist(gs.getBoard().getGoalLocations().get(0)));
        
    }
}