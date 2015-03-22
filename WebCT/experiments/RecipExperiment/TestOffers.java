package RecipExperiment;

import RecipExperiment.CreateGame;
import RecipExperiment.RecipLogic;
import RecipExperiment.RecipConstants;
import ctagents.recipagents.CopyCatAgent.CopyCatAgent;
import ctagents.recipagents.FairnessAgent.FairnessAgent;
import ctagents.recipagents.FutureMeritBestResponse.FMBRLogic;
import ctagents.recipagents.FutureMeritBestResponse.FutureMeritBestResponseAgent;
import ctagents.recipagents.NoRecipAgent.NoRecipAgent;
import ctagents.recipagents.RecipAgentAdaptor;
import ctagents.recipagents.RetroAgent.RetroAgent;
import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;
import edu.harvard.eecs.airg.coloredtrails.shared.ScoringUtility;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import java.util.ArrayList;
import org.apache.log4j.Logger;



public class TestOffers{
    public static void main(String[] arg){

        TestOffersRun tor = new TestOffersRun();

    }
}

class TestOffersRun
{
    public TestOffersRun()
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
        
        ArrayList<GameStatus> SpecialGames = new ArrayList<GameStatus>(5);
        
        //P can reach goal, R can with help (using leftover chips)
        GameStatus gs0 = null;
        //P can reach goal, R cannot even with help
        GameStatus gs1 = null;
        //R can reach goal, P can with help (using leftover chips)
        GameStatus gs2 = null;
        //R can reach goal, P cannot even with help
        GameStatus gs3 = null;
        //both can reach goal
        GameStatus gs4 = null;
        
        
        
        for(int i = 0; i < 4; i++)
            SpecialGames.add(null);
        
        do{
            GameStatus gs;
            
            Boolean allThere = true;
            for( int i = 0; i < 4; i++ ){
                //System.out.println(i + ": " + SpecialGames.get(i) );
                if( SpecialGames.get(i) == null ){
                    allThere = false;
                    System.out.println("missing " + i);
                }
            }
            if(allThere)
                break;
            
            gs = cg.getGame();
            
            //P can reach goal, R can with help (using leftover chips)
            //P can reach goal, R cannot even with help
            
            ps0 = gs.getPlayerByPerGameId(0);
            ps1 = gs.getPlayerByPerGameId(1);
            
            System.out.println("chips: " + ps0.getChips() );
            ps0 = (new BestUse(gs, gs.getPlayerByPerGameId(0), gs.getScoring(), 0)).getBestState();
            if( ps0.getPosition().dist( gs.getBoard().getGoalLocations().get(0) ) == 0 ){
                //Player 0 can reach the goal
                //ps now has the remaining chips
                System.out.println("chips: " + ps0.getChips() );
                System.out.println("gs chips: " + gs.getPlayerByPerGameId(0).getChips() );
                
                ps1 = (new BestUse(gs, gs.getPlayerByPerGameId(1), gs.getScoring(), 0)).getBestState();      
                
                if( ps1.getPosition().dist( gs.getBoard().getGoalLocations().get(0) ) == 0 ){
                    //Player 1 can reach the goal with no help (4)
//                    if(SpecialGames.get(4) == null) {
//                        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Found 4");
//                        gs.put("gametype", "Proposer and Responder can both reach the goal with no exchange");
//                        locs(gs);
//                        SpecialGames.set(4, gs);
//                    }
                    continue;
                }
                
                ps1.setChips( ChipSet.addChipSets(ps1.getChips(), ps0.getChips() ));
                ps1 = (new BestUse(gs, ps1, gs.getScoring(), 0)).getBestState();
                
                if( ps1.getPosition().dist( gs.getBoard().getGoalLocations().get(0) ) == 0 ){
                    //Player 1 can reach the goal with help (0)
                    if(SpecialGames.get(0) == null) {
                        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Found 0");
                        gs.put("gametype", "Proposer can reach the goal, and can give remaining chips to allow Responder to reach goal");
                        locs(gs);
                        
                        
                        SpecialGames.set(0, gs);
                        
                    }
                    continue;
                } else {
                     //P can reach goal, R cannot even with help
                    if(SpecialGames.get(1) == null) {
                        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Found 1");
                        gs.put("gametype", "Proposer can reach goal, Responder cannot, even with help");
                        locs(gs);
                        SpecialGames.set(1, gs);
                    }
                    continue;
                }
            } else {
            
                ps1 = (new BestUse(gs, gs.getPlayerByPerGameId(1), gs.getScoring(), 0)).getBestState();
                if( ps1.getPosition().dist( gs.getBoard().getGoalLocations().get(0) ) == 0 ){
                    //Player  can reach the goal
                    //ps now has the remaining chips
                    
                    //make sure player 0 cannot reach goal on own
                    PlayerStatus tps = (new BestUse(gs, ps0, gs.getScoring(), 0)).getBestState();
                    if( tps.getPosition().dist( gs.getBoard().getGoalLocations().get(0) ) == 0 )
                        continue;

                    System.out.println("ps1 remaining chips: " + ps1.getChips());
                    System.out.println("ps0 chips: " + ps0.getChips());
                    ps0.setChips( ChipSet.addChipSets(ps1.getChips(), ps0.getChips() ));
                    System.out.println("ps0 chips after add: " + ps0.getChips());

                    ps0 = (new BestUse(gs, ps0, gs.getScoring(), 0)).getBestState();

                    if( ps0.getPosition().dist( gs.getBoard().getGoalLocations().get(0) ) == 0 ){
                        //Player 1 can reach the goal with help (0)
                        if(SpecialGames.get(2) == null) {
                            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Found 2");
                            gs.put("gametype", "Responder can reach the goal, and can give remaining chips to allow Proposer to reach goal");
                            locs(gs);
                            SpecialGames.set(2, gs);
                        }
                        continue;
                    } else {
                         //P can reach goal, R cannot even with help
                        if(SpecialGames.get(3) == null) {
                            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Found 3");
                            gs.put("gametype", "Responder can reach goal, Proposer cannot, even with help");
                            locs(gs);
                            SpecialGames.set(3, gs);
                        }
                        continue;
                    }

                }
            }
            
        } while (true);
        
        
        //general idea is to create game, see if game is needed, if so test if it matches criteria
        
        ArrayList<ChipSet> exchange;
        ScoringUtility SU;
        
        
        RecipConstants constants = new RecipConstants();
        RecipLogic RL = new RecipLogic();
        double[] merit = { 0, 0.8 };
        
        ChipSet offer;
        
        for(GameStatus gs : SpecialGames){
            System.out.println("-----------------------------------------------------------------");
            System.out.println((String)gs.get("gametype"));
            System.out.println("Proposer chips: " + gs.getPlayerByPerGameId(ProposerId).getChips());
            System.out.println("Responder chips: " + gs.getPlayerByPerGameId(ResponderId).getChips());
            
            SU = new ScoringUtility(gs, ProposerId, ResponderId);
            
            System.out.println("@@@@@@@@@@@@@ Default score for Proposer: " + SU.getDefaultScore(ProposerId));
            System.out.println("@@@@@@@@@@@@@ Default score for Responder: " + SU.getDefaultScore(ResponderId));
            
            
            //NashBargain, both retro, socialpref, 
            System.out.println("NashBargain offer: " + SU.getNashBargainOffer() );
            
            System.out.println("");
            exchange = RetroAgent.strategy(gs, ProposerId, ResponderId, constants, RL, merit);
            offer = ChipSet.subChipSets(exchange.get(1), exchange.get(0));
            System.out.println("Retro best response offer:" + offer);
            System.out.println("Proposer offer score: " + SU.getOfferScore(offer, ProposerId));
            System.out.println("Responder offer score: " + SU.getOfferScore(offer, ResponderId));
            
            System.out.println("");
            exchange = CopyCatAgent.strategy(gs, ProposerId, ResponderId, constants, RL, merit, logr );
            offer = ChipSet.subChipSets(exchange.get(1), exchange.get(0));
            System.out.println("CopyCatAgent offer:" + offer);
            System.out.println("Proposer offer score: " + SU.getOfferScore(offer, ProposerId));
            System.out.println("Responder offer score: " + SU.getOfferScore(offer, ResponderId));
            
            System.out.println("");
            exchange =  NoRecipAgent.strategy(gs, ProposerId, ResponderId, constants);
            offer = ChipSet.subChipSets(exchange.get(1), exchange.get(0));
            System.out.println("Social pref offer:" + offer);
            System.out.println("Proposer offer score: " + SU.getOfferScore(offer, ProposerId));
            System.out.println("Responder offer score: " + SU.getOfferScore(offer, ResponderId));
            
            System.out.println("");
            
            exchange = FairnessAgent.strategy(gs, ProposerId, ResponderId, SU, logr);
            offer = ChipSet.subChipSets(exchange.get(1), exchange.get(0));
            System.out.println("Fairness offer:" + offer);
            System.out.println("Proposer offer score: " + SU.getOfferScore(offer, ProposerId));
            System.out.println("Responder offer score: " + SU.getOfferScore(offer, ResponderId));
            
            System.out.println("");
            
            FMBRLogic FL = new FMBRLogic();
            exchange = FutureMeritBestResponseAgent.strategy(gs, ProposerId, ResponderId, constants, FL, merit, SU,1);
            offer = ChipSet.subChipSets(exchange.get(1), exchange.get(0));
            System.out.println("FutureMeritBestResponse offer:" + offer);
            System.out.println("Proposer offer score: " + SU.getOfferScore(offer, ProposerId));
            System.out.println("Responder offer score: " + SU.getOfferScore(offer, ResponderId));
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