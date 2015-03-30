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

import edu.harvard.eecs.airg.coloredtrails.server.ServerData;
import edu.harvard.eecs.airg.coloredtrails.server.ServerPhases;
import edu.harvard.eecs.airg.coloredtrails.shared.PlayerConnection;
import edu.harvard.eecs.airg.coloredtrails.shared.ScoringUtility;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;
import edu.harvard.eecs.airg.coloredtrails.shared.logger;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;

import java.io.*;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketAppender;

/**
 * This configuration implements much of the setup and functionality.
 * Including automatic exchange after accept an offer, and automatic movement.
 * 
 * More importantly, it showcases "rounds" and many of the new features of CT.
 * Rounds are not a native feature of CT, but are easy to implement in any sort of style.
 * This config file receives game data from the controller. As such, it is intimately tied to the its associated
 * controller and must be launched by the setuprecip controller.
 * 
 * This config also demonstrates getting input from the GUI (the mood selector) and the arbitrary messaging functionality.
 * 
 */
public class BargainOnlyConfig extends GameConfigDetailsRunnable implements PhaseChangeHandler
{
    Logger logr;
    /**
    * The scoring function used for players in the game.
    * 100 for a player reaching the goal,
    * -15 per unit distance if the player does not reach the goal,
    * 10 for each chip remaining after the player has reached the goal
    * -5 for each dialogue exchange
    * or cannot move any farther towards teh goal.
    */
    Scoring s = new Scoring(100, -15, 10, 0.0, -5);

    logger logbob;
    
    //used for switching roles.
    int	ProposerID	=	0;
    int	ResponderID	=	1;

    //Number of game rounds to play per game, each round we switch roles
    int NumRounds = 5;
    int Round	= 0;
    //board dimensions
    int numRows = 4;
    int numCols = 4;
    //number of chips per player
    static int numChips = 5;
    
    int DialogueExchanges = 0;

    boolean showMoodPanel = true;
    
    //set to true to show goal to opponent for agent with perGameId=array's index
    boolean[] initialGoalVisibility = {false,false};
    
    String CurrentPhaseName = null;
    boolean ended = false;

    int[] playerPins = new int[2];
    
    ScoringUtility SU;
    
    //list of games to setup and play on execution
    ArrayList<GameStatus> games = new ArrayList();
    
    /** Local random generator for creating chipsets */
    static Random localrand = new Random();

    /** determines if there will be automatic movement */
    boolean automaticMovement = true;
    /** determines if the chips will automatically transfer after a
     * proposal has been accepted */
    boolean automaticChipTransfer = true;
    /** determines if the phases will loop. */
    boolean phaseLoop = false;

    //permission flags
        boolean ProposerCommunicationAllowed = false;
        boolean ProposerTransfersAllowed     = false;
        boolean ProposerMovesAllowed         = false;
        boolean ResponderCommunicationAllowed = false;
        boolean ResponderTransfersAllowed     = false;
        boolean ResponderMovesAllowed         = false;


    public void resetPermissionFlags() {
        ProposerCommunicationAllowed = false;
        ProposerTransfersAllowed     = false;
        ProposerMovesAllowed         = false;
        ResponderCommunicationAllowed = false;
        ResponderTransfersAllowed     = false;
        ResponderMovesAllowed         = false;
    }

    public void swapRoles() {
        int tempID = ProposerID;
        ProposerID = ResponderID;
        ResponderID = tempID;
        gs.getPlayerByPerGameId(ProposerID).setRole("proposer");
        gs.getPlayerByPerGameId(ResponderID).setRole("responder");
    }

    public void setPermissions() {
        PlayerStatus Responder = gs.getPlayerByPerGameId(ResponderID);
        PlayerStatus Proposer  = gs.getPlayerByPerGameId(ProposerID);

        Responder.setCommunicationAllowed(ResponderCommunicationAllowed);
        Responder.setTransfersAllowed(ResponderTransfersAllowed);
        Responder.setMovesAllowed(ResponderMovesAllowed);

        Proposer.setCommunicationAllowed(ProposerCommunicationAllowed);
        Proposer.setTransfersAllowed(ProposerTransfersAllowed);
        Proposer.setMovesAllowed(ProposerMovesAllowed);
        
    }

    /**
            Returns score of specified player, according to player's current state
    */
    public int getPlayerScore(PlayerStatus ps) {
        RowCol gpos = gs.getBoard().getGoalLocations(ps.getPerGameId()).get(0);  
        // get first goal in list
        return (int)Math.floor(gs.getScoring().score(ps, gpos, DialogueExchanges));  // should change to double at some point
    }

    /**
            Returns score of specified player, according to player's current state and a hypothetical offer
    */
    public int getPlayerScore(PlayerStatus ps, ChipSet offer) {
        RowCol gpos = gs.getBoard().getGoalLocations(ps.getPerGameId()).get(0);
        PlayerStatus psTry = new PlayerStatus(ps);
        //System.out.print(psTry.getChips()+" @ "+psTry.getPosition());
        if(ps.getPerGameId() == ProposerID) {
          psTry.setChips(ChipSet.addChipSets(ps.getChips(), offer));
        } else {
          psTry.setChips(ChipSet.addChipSets(ps.getChips(), ChipSet.getNegation(offer)));
        }
        PlayerStatus psBest = new BestUse(gs, psTry, gs.getScoring(), ps.getPerGameId()).getBestState();
        psTry.setPosition(psBest.getPosition());
        psTry.setChips(psBest.getChips());
        //System.out.println(" >>> " + psTry.getChips()+" @ "+psTry.getPosition());
        return (int)Math.floor(gs.getScoring().score(psTry, gpos, DialogueExchanges));  // should change to double at some point
    }

    /**
            Called by GameConfigDetailsRunnable methods when calculation and assignment
            of player scores is desired
    */
    protected void assignScores() {
        System.out.println("Assigning scores after " + DialogueExchanges + " dialogue exchanges");
        ServerData sd = ServerData.getInstance();
        PlayerConnection pc;
        double score;
        //Get round's original proposer and responder
        int roundProposerID	=	Round % 2;
        for( PlayerStatus ps : gs.getPlayers() ) {
            score = getPlayerScore(ps);
            ps.setScore((int) score);
            System.out.println("Player: " + ps.getPin() + "  Score: " + ps.getScore());
            pc = sd.getPlayerConnection(ps.getPin());
            pc.setScore(pc.getScore() + score);
            logbob.log(new Double(score), ("offer" + (ps.getPerGameId() == roundProposerID ? "P" : "R")));
        }
    }

    protected void assignNonNegoScores() {
        System.out.println("Assigning nn scores");
        double score;
        //Get round's original proposer and responder
        int roundProposerID	=	Round % 2;
        for( PlayerStatus ps : gs.getPlayers() ) {
            score = getPlayerScore(ps, new ChipSet());
            System.out.println("Player: " + ps.getPin() + "  NonNegoScore: " + score);
            logbob.log(new Double(score)
            , ("nn" + (ps.getPerGameId() == roundProposerID ? "P" : "R")));
        }
    }


    /**
            Called by server when a phase begins
    */
    public void beginPhase(String phasename) {
        System.out.println("A New Phase Began: " + phasename);
        
                
        CurrentPhaseName = phasename;
  
        resetPermissionFlags();

        // FYI - for the first phase it won't work from here
        if(phasename.equals("Communication Phase")) {
            System.out.println("------------------ Round: " + Round + " ------------------");
            ProposerCommunicationAllowed	=	true;
            logbob.log(String.valueOf(Round), "newround");
            logbob.log(phasename, "newphase");
            assignNonNegoScores(); //non-negotiation scores
        } else if(phasename.equals("Feedback Phase")) {
            logbob.log(phasename, "newphase");
            if(Round >= (games.size() - 1)){
                System.out.println("Ending game");
                ((ServerPhases)gs.getPhases()).setLoop(false);
                System.out.println("Loop status: " + gs.getPhases().getIsLoop());

                //gs.setEnded();
                ended = true;
                return;
            } else {
                Round++;
            }
        } else {
            logbob.log(phasename, "newphase");
        }

        setPermissions();

    }


    /**
     *	Called by server when a phase ends
     */
    public void endPhase(String phasename) {
        if(ended){
            gs.sendArbitraryMessage("NEWROUND");
            gs.setEnded();
            logbob.close();
            return;
        }
        System.out.println("A Phase Ended: " + phasename);

        // if end of feedback phase, then end the game
        if(phasename.equals("Feedback Phase")) {
            //Prepare roles for next game
            ProposerID	=	Round % 2;
            ResponderID = (ProposerID == 0 ? 1 : 0);
            gs.getPlayerByPerGameId(ResponderID).setRole("responder");
            gs.getPlayerByPerGameId(ProposerID).setRole("proposer");
            loadGame(games.get(Round));
            DialogueExchanges = 0;
            gs.sendArbitraryMessage("NEWROUND");
        }

        // ### automatic movment ###
        else if (phasename.equals("Communication Phase")) {
            logbob.log(new Date(),"timeStampEndRound"+Round);
            resetPermissionFlags();
            setPermissions();
            if( automaticMovement ) {
                doAutomaticMovement( gs.getScoring() );

                // calculate scores after all players have moved
                // (e.g, in case a player's score depends on others' locations)
                assignScores();
            }
        }
    }

	// Override super method do discourse in order to make an automatic transfer after accept a proposal
    @Override
    public boolean doDiscourse(DiscourseMessage dm) {
        logr.info( "Received Discourse Message " );
        logr.info( "Class: " + dm.getClass() );
        logr.info( "Id: " + dm.getMessageId() );
        logr.info( "Type: " + dm.getMsgType() );
        logr.info( "From: " + dm.getFromPerGameId() +" : pin "+ gs.getPlayerByPerGameId(dm.getFromPerGameId()).getPin());
        logr.info( "To: " + dm.getToPerGameId() +" : pin "+ gs.getPlayerByPerGameId(dm.getToPerGameId()).getPin());
        
        ChipSet offer = null;

        // ### automatic exchange of chips upon acceptance of a proposal ###
        if( dm instanceof BasicProposalDiscussionDiscourseMessage ) {
            System.out.println( "---- BasicProposalDiscussionDiscourseMessage ----" );

            BasicProposalDiscussionDiscourseMessage bpddm = (BasicProposalDiscussionDiscourseMessage) dm;
            
            logbob.log( bpddm.accepted() ? "accepted" : "rejected", "offerresponse" + Round + "." + DialogueExchanges);
            logbob.log( new Date(), "timeStampOfferresponse" + Round + "." + DialogueExchanges);
            
            boolean accepted = false;
            if( automaticChipTransfer && bpddm.accepted() ) {
                System.out.println("&&&&&&&&&&&&& We have an accepted offer, chips before: &&&&&&&&&&&&&&&&&&");
                for( PlayerStatus player : gs.getPlayers() ) {
                    System.out.println("player pin: " +  player.getPin());
                    System.out.println("player chips: " + player.getChips());
                }
                
                doAutomaticChipTransfer( bpddm );
                
                System.out.println("&&&&&&&&&&&&&& Chips After &&&&&&&&&&&&&&&&&");
                for( PlayerStatus player : gs.getPlayers() ) {
                    System.out.println("player pin: " +  player.getPin());
                    System.out.println("player chips: " + player.getChips());
                }
                //System.out.println("Offer accepted");
                accepted = true;
            } else {
                System.out.println("Offer rejected");
            }
            
            
            //Now to log the scores from the offer
            //double offerP = SU.getOfferScore(offer, ProposerID);
            //double offerR = SU.getOfferScore(offer, ResponderID);
            
            //logbob.log(new Double(offerP), "offerP");
            //logbob.log(new Double(offerR), "offerR");

            if(!accepted) {
                swapRoles();
                ProposerCommunicationAllowed = true;
                setPermissions();
                if(bpddm.getChipsSentByResponder().getNumChips() != 0
                  || bpddm.getChipsSentByProposer().getNumChips() != 0) {
                    DialogueExchanges++;
                }
            } else {
                if(CurrentPhaseName.equals("Communication Phase"))
                    ((ServerPhases)gs.getPhases()).advancePhase();
            }
        } else if(dm instanceof BasicProposalDiscourseMessage) {
        //Here we compare chips propsed with proposer's and recipient's chipset
        //first check type of message
            //We have an offer
            System.out.println("We have an offer");
            ChipSet ChipsSentByResponder = ((BasicProposalDiscourseMessage)dm).getChipsSentByResponder();
            ChipSet ChipsSentByProposer = ((BasicProposalDiscourseMessage)dm).getChipsSentByProposer();
            ChipSet ResponderChips = gs.getPlayerByPerGameId(ResponderID).getChips();
            ChipSet ProposerChips = gs.getPlayerByPerGameId(ProposerID).getChips();
            boolean result = (ResponderChips.contains(ChipsSentByResponder)) && (ProposerChips.contains(ChipsSentByProposer));
            System.out.println("Result of chip comparison: " + result);
            
            offer = ChipSet.subChipSets(((BasicProposalDiscourseMessage)dm).getChipsSentByResponder(), ((BasicProposalDiscourseMessage)dm).getChipsSentByProposer());
            //System.out.println("Result of chip comparison: " + result);
            if(!result) {
                //invalid offer ... drop and pass
                System.out.println("Dropping invalid offer");
                resetPermissionFlags();
                swapRoles();
                ProposerCommunicationAllowed = true;
                setPermissions();
                return false;
            }
            logbob.log(offer, "offer"  + Round + "." + DialogueExchanges);
            logbob.log(new Date(), "timeStampOffer"  + Round + "." + DialogueExchanges);
            double score;
            //Get round's original proposer and responder
            int roundProposerID	=	Round % 2;
            for( PlayerStatus ps : gs.getPlayers() ) {
                score = getPlayerScore(ps, offer);
                System.out.println("Player: " + ps.getPin() + "  Offer Flavor: " + score);
                logbob.log(new Double(score)
                    , ("flavor" + (ps.getPerGameId() == roundProposerID ? "P" : "R") + Round + "." + DialogueExchanges));
            }
            //Disable proposing by ANYONE until offer has been rejected
            resetPermissionFlags();
            setPermissions();
            
    //        logbob.log(result, "offerresponse" + Round);
    
        }

        return gs.doDiscourse(dm);
    }

    /**
            Start a new game and set up all of the game specifications.
    */
    public void run() {
        
        String filenameRoot = "CT_BO_Log-";
        
        if(gs.getPlayers().size() > 2) {
            return;
        }
        
        //Use the perGameId as the ref index for the pin ... gives the controller order
        for(PlayerStatus ps : gs.getPlayers()){
            playerPins[ps.getPerGameId()] = ps.getPin();
        }
        
        System.out.println("Game Init Players: " + playerPins[0] + " vs " + playerPins[1]);
        
        filenameRoot = filenameRoot + playerPins[0] + "-" + playerPins[1];
        String filename;
        //filename = filename.substring(0, filename.length() - 1 );
        int iFileSerial = 0;
        do {
            filename = filenameRoot + "." + (iFileSerial++) + ".log";
        } while(new File(filename).exists());

        logbob = new logger(filename);
        
        System.out.println("Logging into [" + filename + "]");
        
        if(showMoodPanel) {
            /**
             * Here we give the client a commmand, and block until all clients have responded. The getClientCommands function
             * returns a hash of the pergameIds and client responses (if any)
             */
            HashMap returndata = ServerData.getInstance().getClientCommands(gs.getGameId()).getClientAction("showmoodpanel", null);
            for(Object k : returndata.keySet()){
                Object o = returndata.get(k);
                System.out.println(" returned data for " + k.toString() + "     " + o.toString() + "    " + (String)o);
            }
        }
        
        
        //This makes use of the data passed by the controller
        if(gs.getDataFromController() != null && ((ArrayList<GameStatus>)gs.getDataFromController()).size() > 0 ) { 
            games = (ArrayList<GameStatus>)gs.getDataFromController();
        } else {
            setupGames();
        }
        
        for(PlayerStatus ps : gs.getPlayers() ){
            for(PlayerConnection pc : ServerData.getInstance().getPlayers()){
                if(ps.getPin() == pc.getPin())
                    ps.set("pc", pc);
            }
        }
        
        //gs.setGamePalette(game.get(0).getGamePalette());
        //gs.setScoring( game.get(0).getScoring() );

        // set up phase sequence
        ServerPhases ph = new ServerPhases(this);
        //indefinite, depend on deal being made to cause phase transition.
        ph.addPhase("Communication Phase", 240);
        //ph.addPhase("Exchange Phase", 5);
        ph.addPhase("Movement Phase", 5);
        ph.addPhase("Feedback Phase", 5);
        
        logbob.log((Serializable) ServerData.getInstance().getPlayers(), "playerconnections");
        logbob.log((Serializable) gs.getPlayers(), "players");
        //logbob.log("teststring", "test");
        //logbob.close();
        
        
        String players = " ";
        for(PlayerStatus ps : gs.getPlayers())
            players = players.concat(ps.getPin() + " ");
        
        logr = Logger.getLogger(this.getClass().getName() +  players );
//        SocketAppender appender = new SocketAppender("bogglemac.dyndns.org", 4445);
//        logr.addAppender(appender);
        logr.setLevel(Level.INFO);
        logr.log(Level.INFO, "Startup");
        
        System.out.println("Let the game begin...");

        System.out.println("Our game id= " + gs.getGameId());
        
        loadGame(games.get(0));
        gs.sendArbitraryMessage("NEWROUND");

                
        ph.setLoop(true);
        gs.setPhases(ph);
        
        
        
        gs.setInitialized();  // will generate GAME_INITIALIZED message
        
    }

    private void loadGame(GameStatus igs) {
        System.out.println("Loading game config "+igs.getGameId());
        gs.setPlayers(igs.getPlayers());
        gs.getPlayerByPerGameId(0).setPin(playerPins[0]);
        gs.getPlayerByPerGameId(1).setPin(playerPins[1]);
        for(PlayerStatus ps : gs.getPlayers() ){
            for(PlayerConnection pc : ServerData.getInstance().getPlayers()){
                if(ps.getPin() == pc.getPin()) {
                    ps.set("pc", pc);
                }
            }
        }
        
        gs.setGamePalette(igs.getGamePalette());
        gs.setScoring(igs.getScoring());
        gs.setBoard(igs.getBoard());
        
        logbob.log((GameStatus)igs,"round"+Round);
        logbob.log(new Date(),"timeStampStartRound"+Round);
        for(PlayerStatus ps : gs.getPlayers() ){
            logr.info("Player:  pin: " + ps.getPin() + " pergameId: " + ps.getPerGameId() );
        }
    }

    private void setupGames(){
        while(games.size() < NumRounds) {
            print("setting up game: " + games.size());

            GameStatus xgs = new GameStatus();
            Set<PlayerStatus> xpsset = new LinkedHashSet<PlayerStatus>();
            xpsset.add(new PlayerStatus(0,-1));
            xpsset.add(new PlayerStatus(1,-1));
            xgs.setPlayers(xpsset);
    
            xgs.getPlayerByPerGameId(0).setRole("proposer");
            xgs.getPlayerByPerGameId(1).setRole("responder");

            GamePalette gp = new GamePalette();
            gp.add("RGBRed");
            gp.add("RGBGreen");
            gp.add("purple1");
            gp.add("orange1");
            xgs.setGamePalette(gp);
    
            xgs.setScoring(s);
            
            for( PlayerStatus player : xgs.getPlayers() ) {
                if(player.getPerGameId() < initialGoalVisibility.length) {
//                    player.setGoalVisible(initialGoalVisibility[player.getPerGameId()]);
                } else {
//                    player.setGoalVisible(true);
                }
            }
            for( PlayerStatus player : xgs.getPlayers() ) {
                // set chips for players
                ChipSet cs = getRandChipSet( xgs.getGamePalette() );
                player.setChips( cs );
            }
            // assign player piece locations
            RowCol rcp1 = new RowCol(localrand.nextInt(numRows), localrand.nextInt(numCols));
            RowCol rcp2;
            do {
              rcp2 = new RowCol(localrand.nextInt(numRows), localrand.nextInt(numCols));
             } while(rcp2.equals(rcp1)); 
            xgs.getPlayerByPerGameId(ProposerID).setPosition(rcp1);  // Proposer's location
            xgs.getPlayerByPerGameId(ResponderID).setPosition(rcp2);  // Responder's location
            
            //generate random boards until we get one that is winnable with transactions but not completely winnable individually
            Board b;
            do {
                b = getRandBoard(xgs.getGamePalette(),xgs);
            } while(!checkwinnable(b,xgs) || checktoowinnable(b,xgs));
            xgs.setBoard(b);
            
            games.add(xgs);
        }
    }
    
    //Returns random board with goals
    public Board getRandBoard(GamePalette gp, GameStatus xgs){
        Board board = getRandomBoard(gp, numRows, numCols);
        RowCol pp0 = xgs.getPlayerByPerGameId(ProposerID).getPosition(); 
        RowCol pp1 = xgs.getPlayerByPerGameId(ResponderID).getPosition();
        RowCol pg0;  
        RowCol pg1; 
        do{
            pg0 = new RowCol(localrand.nextInt(board.getRows()), localrand.nextInt(board.getCols()));
            pg1 = new RowCol(localrand.nextInt(board.getRows()), localrand.nextInt(board.getCols()));
            
            if(!pg0.equals(pg1) && !pg0.equals(pp0) && !pg0.equals(pp1) && !pg1.equals(pp0) && !pg1.equals(pp1)) {
               break;
            }
        } while (true);
        board.setGoal(new Goal(ProposerID, pg0), true);
        board.setGoal(new Goal(ResponderID, pg1), true);
        return(board);
    }

      //returns random board, just the board, without goals or players
    public static Board getRandomBoard(GamePalette gp, int numRows, int numCols){
        Board board = new Board(numRows, numCols);
        Square[][] squares = new Square[board.getRows()][board.getCols()];

        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
              squares[i][j] = new Square();
              squares[i][j].setColor(gp.getRandomColor());
            }
        }
        board.setSquares(squares);
        return(board);
    }

    
    /**
            Generates random chipset for specified player
            <p>
            add total #chips as parameter
    */
    protected static ChipSet getRandChipSet( GamePalette gp ) {
        ChipSet chipset = new ChipSet();
        for( String color : gp.getColors() )
            chipset.set( color, 0 );
        for(int i = 0; i < numChips; i++)
            chipset.add(gp.getRandomColor(), 1);
        return chipset;
    }

    //Check if, using a win-all transaction, both players can reach the goal
    //Add the chips of both players together, then check if any players can reach the goal using these chips
    private boolean checkwinnable(Board b, GameStatus xgs) {
        ChipSet together = ChipSet.addChipSets(xgs.getPlayerByPerGameId(ProposerID).getChips(), xgs.getPlayerByPerGameId(ResponderID).getChips());
        if(together.getNumChips() == 0)
            return(false);
        Path p;
        BestUse bu;
        PlayerStatus tempps = new PlayerStatus();
        
        tempps.setChips(together);

        for(PlayerStatus ps : xgs.getPlayers()){
            tempps.setPosition(ps.getPosition());
            //if the best use of the combined chips is 0 distance from the goal, then we have reached it
            if(new BestUse(b, tempps, s, ps.getPerGameId()).getBestState().getPosition().dist(b.getGoalLocations(ps.getPerGameId()).get(0)) != 0){
                return(false);    
            }
        }
        return(true);
    }

    //Check if, without using a transaction, both players can reach the goal
    private boolean checktoowinnable(Board b, GameStatus xgs) {
        for(PlayerStatus ps : xgs.getPlayers()){
            //if the best use of the combined chips is 0 distance from the goal, then we have reached it
            if((new BestUse(b, ps, s, ps.getPerGameId())).getBestState().getPosition().dist(b.getGoalLocations(ps.getPerGameId()).get(0)) != 0){
                return(false);    
            }
        }
        return(true);
    }

    // Prevents players from seeing each others goals if it is not allowed
    public Board filterBoard(Board serverboard, int perGameId) {
        for(PlayerStatus ps : gs.getPlayers()){
            if(ps.getPerGameId() != perGameId) {
                int opponentId = ps.getPerGameId();
//                if(!gs.getPlayerByPerGameId(opponentId).isGoalVisible()) {
                    Map<Goal,RowCol> forRemoval = new HashMap<Goal,RowCol>();
                    for(RowCol rcGoal : serverboard.getGoalLocations(opponentId)) {
                        forRemoval.put(serverboard.getSquare(rcGoal).getGoal(opponentId),rcGoal);
                    }
                    for(Goal gRemove : forRemoval.keySet()) {
                      serverboard.getSquare(forRemoval.get(gRemove)).removeGoal(gRemove);
                    }
//                }
            }
        }
        return serverboard;
    }

    private void print(String s){
        System.out.println("(Game " + gs.getGameId() + ") " + s);
    }

//    public void setGoalVisible(int perGameId, boolean isVisible) {
//        gs.getPlayerByPerGameId(perGameId).setGoalVisible(isVisible);
//    }
        
    public void requestGameEnd(){
        print("requested to end game nicely");
        Round = NumRounds;
    }
}


