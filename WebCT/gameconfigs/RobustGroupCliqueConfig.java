


import edu.harvard.eecs.airg.coloredtrails.server.ServerPhases;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;

import java.util.Hashtable;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

/**
 * This configuration implements much of the setup and functionality.
 * Including automatic exchange after accept an offer, and automatic movement.
  The game configuration file RobustGroupCliqueConfig.java located in
the gameconfigs directory describes an alternating-phase
minimal-information CT game that can be played with an arbitrary
number of players on an arbitrarily-sized board. (By default, it is
set up for use by 3 players on a 6x6 board, but it is a simple task to
change these numbers.) The goal of the game is to collect as many gold
chips as possible; gold chips are acquired through successful exchange
interactions (success being defined in this game as the formation of a
clique). Each turn, the game alternates between a movement phase and
an exchange phase.

In each movement phase, every player is given some pre-set number
chips that allow them to move a corresponding number of spaces across
the board. Players can move in any direction they wish, as movement is
uninhibited. However, the movements of other players are hidden
locally, and only revealed at the end of each movement phase.

In the exchange phase, each player is given some fixed number of white
chips. They are then free to distribute these white chips to other
players in any manner they wish. (There is also a commented-out
section of code that allows players to hold white chips they do not
wish to distribute.) The game keeps track of all transactions and
creates an "exchange graph" out of these logged interactions. It then
looks for cliques on the graph, and then gives a bonus to players who
are members of these cliques (larger bonuses are conferred to players
who contribute more).

Then it is time for the movement phase again, and the game loops
through these two phases some predetermined number of times. Finally,
the game enters into a 10 second "feedback phase" before ending
entirely.

 */
public class RobustGroupCliqueConfig extends GameConfigDetailsRunnable implements PhaseChangeHandler
{
    Scoring s = new Scoring(100, -10, 5);

    /** Local random generator for creating chipsets */
    static Random localrand = new Random();

    /** determines if there will be automatic movement */
    boolean automaticMovement = false;
    
    /** determines if the chips will automatically transfer after a
     * proposal has been accepted */
    boolean automaticChipTransfer = true; 
    
    /** things to tweak */
    private final int numPlayers = 2; // if you change this, be sure to also
                                      // change the number of players in your
                                      // admin config file / controller program.
    
    private final int boardSize = 6; // to properly display different-sized
                                     // boards, go into BoardPanel.java (in
                                     // gui/ctgui/original) and change boardSize
                                     // there to match whatever you use here.

    private final int turnMax = 2;      // how many turns are in the game
    private final int chipBonus = 3;    // # of chips give to players every turn
    private final int movesPerTurn = 2; // moves allowed per player per turn
    private static String chipColor1 = "white smoke";    // given every round
    private static String chipColor2 = "pale goldenrod"; // worth $$$
    private static String boardColor1 = "powder blue";
    private static String boardColor2 = boardColor1;  // comment this out for
                                                      // a checkered board

    // current turn we are on
    private int turnCount = 0;
    // keeps track of all players' gold counts
    private int[] treasureTrove = new int[numPlayers];
    // store old player positions
    private RowCol[] oldPositions = new RowCol[numPlayers];
    // keeps track of how many moves a play has made this turn
    private int[] movesMadeThisTurn = new int[numPlayers];
    // stores all exchanges
    private int[][] exchanges = new int[numPlayers][numPlayers];
    // tracks whether players have given out all white chips yet
    private boolean[] noChipsLeft = new boolean[numPlayers];

    
    
    
    
    /* 
     *  Determine what contribution each player made to a given clique.
     */
    private int getContribution(Integer player, Set<Integer> clique)
    {
        int contribution = 0;
        for (Integer opponent : clique)
            contribution += exchanges[player][opponent];
        
        return contribution;
    }

    
    /*
     *  Returns true if a given subset of players forms an exchange  clique.
     */
    private boolean isClique(Set<Integer> subgroup)
    {
        boolean isClique = true;
        // not a clique if a player didn't give to some 
        // other player in the subgroup
        for (Integer pid : subgroup)
        for (Integer other : subgroup)
            if (pid != other && exchanges[pid][other] == 0)
                isClique = false;

        return isClique;
    }

    
    private Set<Set<Integer>> powerset(Set<PlayerStatus> players) 
    {
        // make set of player ids
        Set<Integer> pids = new HashSet();
        for (PlayerStatus player : players)
            pids.add(player.getPerGameId());
        
        // create the empty powerset
        Set<Set<Integer>> powerset = new HashSet();
        powerset.add(new HashSet());
        
        // iteratively make power set by taking the old power set, adding one
        // element to each member set, and merging the result.
        // e.g.:
        // [[]] --> [[], [1]] --> [[], [1], [2], [1,2]] -->
        // [[], [1], [2], [1,2], [3], [1,3], [1,2], [1,2,3]]
        for (Integer pid : pids) {
            Set<Set<Integer>> newSets = new HashSet();
            for (Set<Integer> group : powerset) {
                Set<Integer> groupCopy = new HashSet(group);
                groupCopy.add(pid);
                newSets.add(groupCopy);
            }
            powerset.addAll(newSets);
        }
        
      return powerset;
    }
    
    
    
    /*
     *  Player's score is the number of gold chips they have
     */
    public int getPlayerScore(PlayerStatus ps) 
    {
        return ps.getChips().getNumChips(chipColor2);
    }


    /*
     *  Called by GameConfigDetailsRunnable methods when calculation and assignment
     *  of player scores is desired
     */
    protected void assignScores() 
    {
        for( PlayerStatus ps : gs.getPlayers() ) {
            ps.setScore(getPlayerScore(ps));
            System.out.println("Player: " + ps.getPin() + "  Score: " + ps.getScore());
        }
    }


    /*
     *  Called by server when a phase begins
     */
    public void beginPhase(String phasename) 
    {
        System.out.println("A New Phase Began: " + phasename);

        boolean communicationAllowed = false;
        boolean transfersAllowed     = false;
        boolean movesAllowed         = false;
        boolean giveBonusChips       = false;
        boolean giveMovementChips    = false;

        if (phasename.equals("Movement Phase"))
        {
            giveMovementChips = true;
            movesAllowed = true;
        }
        
        else if (phasename.equals("Exchange Phase"))
        {
            transfersAllowed = true;
            giveBonusChips = true;
        }

        // Hand out winnings to players and log 'em.
        else if (phasename.equals("Feedback Phase"))
        {
            Phases phases = gs.getPhases();
            
            // mete out the chips stored in the treasure trove
            for (PlayerStatus player : gs.getPlayers())
            {
                ChipSet cs = player.getChips();
                cs.set(chipColor2, 0);
                cs.add(chipColor2, treasureTrove[player.getPerGameId()]);
                player.setChips(cs);
                
                // add player winnings to the log
                HistoryLog log = gs.getHistoryLog();
                Hashtable<String, Object> logEntryHash = 
                        new Hashtable<String, Object>();
                
                logEntryHash.put("type", "chip count -> player " + player.getPerGameId() 
                        + " won " + player.getChips().getNumChips(chipColor2) 
                        + " gold chips");
                
                log.addHistoryItem(new HistoryEntry(phases.getCurrentPhaseName(),
                                                    phases.getPhasesElapsed(),
                                                    phases.getCurrentSecsElapsed(),
                                                    logEntryHash));

                gs.setHistoryLog(log);
//                System.out.println(log.toString());
            }
        }

        for(PlayerStatus player : gs.getPlayers())
        {
            player.setCommunicationAllowed(communicationAllowed);
            player.setTransfersAllowed(    transfersAllowed);
            player.setMovesAllowed(        movesAllowed);
            
            if(giveBonusChips)
            {
                // give players a fixed number of bonus chips
                ChipSet cs = new ChipSet(player.getChips());
                cs.add(chipColor1, chipBonus);
                player.setChips( cs );
            }
            
            if(giveMovementChips)
            {
                ChipSet cs = new ChipSet(player.getChips());
                // give appropriate chip color for checkered board. (Currently
                // set up only to work with one move per turn.)
                if (gs.getBoard().getSquare(player.getPosition()).getColor().equals(boardColor1))
                    cs.add(boardColor2, movesPerTurn);
                else
                    cs.add(boardColor1, movesPerTurn);
                player.setChips( cs );
            }
        }
    }

    
    
    
    
    /*
     *	Called by server when a phase ends
     */
    public void endPhase(String phasename)
    {
        // End the game.
        if (phasename.equals("Feedback Phase"))
            gs.setEnded();
        
        // At the end of the movement phase, reset players' movement chips
        // and update oldPositions to reflect everyone's recent moves
        else if (phasename.equals("Movement Phase"))
        {
            // for each player...
            for( PlayerStatus player : gs.getPlayers() )
            {
                // reset players' movement chips
                ChipSet cs = new ChipSet(player.getChips());
                cs.set(boardColor1, 0);
                cs.set(boardColor2, 0);
                player.setChips( cs );

                // now each player can see the moves everyone else made
                oldPositions[player.getPerGameId()] = player.getPosition();
                movesMadeThisTurn[player.getPerGameId()] = 0;
            }

            ++turnCount; // one turn == a movement phase and an exchange phase
        }

        // All the player exchanges are stored in the exchanges[] array,
        // so we use that to give a gold chip bonus to everyone who managed to
        // formed a clique in the exchange graph. Bonuses are propotional to
        // the size of the clique and the number of white chips contributed.
        else if (phasename.equals("Exchange Phase"))
        {
            // for each player...
            for (PlayerStatus player : gs.getPlayers())
            {
                ChipSet cs = new ChipSet(player.getChips());
                // players don't get to accrue white chips over multiple rounds
                cs.set(chipColor1, 0);
                player.setChips(cs);

/* CONVERT HELD WHITE CHIPS INTO GOLD CHIPS IF NOT ALLOWING PLAYERS TO KEEP WHITE CHIPS *
                // transfer unused chips to player's bank
                cs.add(chipColor2, cs.getNumChips(chipColor1));
                cs.set(chipColor1, 0);
/* END */

/* BASIC EXCHANGE DISTRIBUTION - GIVE EACH PLAYER WHAT THEY WERE GIVEN  *
                // add in all chips given to the player from exchanges
                for (int i = 0; i < numPlayers; ++i) {
                    cs.add(chipColor2, exchanges[i][player.getPerGameId()]);
                }
/* END */                
            }
            
            // we will find cliques by exhaustively searching the power set
            Set<Set<Integer>> cliques = new HashSet();
            Set<Set<Integer>> powerset = powerset(gs.getPlayers());

//            // [DEBUG]
//            System.out.println("Power Set: " + powerset.toString());

            
            // find every possible cliques in the power set
            for (Set<Integer> subgroup : powerset)
                // (1 & 2) only consider groups adhering to clique size constraints
                // (3) check if subgroup is in fact a clique
                if (subgroup.size() > 1
                        && subgroup.size() <= chipBonus+1
                        && isClique(subgroup))
                    cliques.add(subgroup);

            
            // filter out redundant sub-cliques for maximalCliques
            Set<Set<Integer>> maximalCliques = new HashSet(cliques);
            
            // compare pairs of cliques to see if one is a sub-clique of the other
            for (Set<Integer> clique1 : cliques)
            for (Set<Integer> clique2 : cliques)
                if (!clique1.equals(clique2) && clique2.containsAll(clique1))
                    maximalCliques.remove(clique1);

//            // [DEBUG] 
//            System.out.println("Cliques found: " + maximalCliques.toString());
            
            // for each clique, reward players for their contribution
            for (Set<Integer> clique : maximalCliques)
                for (Integer player : clique)
                    treasureTrove[player] +=
                            (getContribution(player, clique) * clique.size());

              
            // reinitialize exchanges for the next turn
            for (int i = 0; i < numPlayers; ++i)
            {
                noChipsLeft[i] = false;
                for (int j = 0; j < numPlayers; ++j)
                    exchanges[i][j] = 0;
            }
                
             
/* FOR DEMO PURPOSES ONLY */
            // display player winnings at the end of each turn    
            for (PlayerStatus player : gs.getPlayers())
            {
                ChipSet cs = player.getChips();
                cs.set(chipColor2, 0);
                cs.add(chipColor2, treasureTrove[player.getPerGameId()]);
                player.setChips(cs);
            }
// END */
        }
        
        System.out.println("A Phase Ended: " + phasename);
    }

    
    

    /*
     * No discourse in this setup, so this function doesn't do anything.
     */
    @Override public boolean doDiscourse(DiscourseMessage dm) {
        System.out.println("Received Discourse Message ");
        System.out.println("Class: " + dm.getClass());
        System.out.println("From: " + dm.getFromPerGameId());
        System.out.println("From: " + dm.getToPerGameId());

        boolean result = gs.doDiscourse(dm);

        // ### automatic exchange of chips upon acceptance of a proposal ###
        if(dm instanceof BasicProposalDiscussionDiscourseMessage)
        {
            System.out.println( "---- BasicProposalDiscussionDiscourseMessage ----" );
            BasicProposalDiscussionDiscourseMessage bpddm = 
                    (BasicProposalDiscussionDiscourseMessage) dm;

            if(automaticChipTransfer && bpddm.accepted())
                doAutomaticChipTransfer( bpddm );
        }

        return result;
    }




   /*
    * The movement phase ends when all players make all their moves. If the
    * phase is timed, it ends early.
    */
    
    @Override public boolean doMove(int perGameId, RowCol newpos)
    {
        PlayerStatus ps = gs.getPlayerByPerGameId(perGameId);

        // check if player has the chips
        ChipSet requiredChips = new ChipSet();
        requiredChips.add(gs.getBoard().getSquare(newpos).getColor(), 1);
        if (!ps.getChips().contains(requiredChips))
            return false;

        // only allow newPositions to adjacent squares
        if (!RowCol.areNeighbors(ps.getPosition(), newpos))
            return false;

        // don't move if newPositions NOT allowed
        if (!ps.areMovesAllowed())
            return false;

        // if we made it here, the player can move
        ps.setChips(ChipSet.subChipSets(ps.getChips(), requiredChips));
        ps.setPosition(newpos);

        // add move to log
        HistoryLog log = gs.getHistoryLog();
        Phases phases = gs.getPhases();

        Hashtable<String, Object> logEntryHash =
                new Hashtable<String, Object>();
        logEntryHash.put("type", "move -> player " + perGameId + " to " + newpos.toString());

        log.addHistoryItem(new HistoryEntry(phases.getCurrentPhaseName(),
                phases.getPhasesElapsed(),
                phases.getCurrentSecsElapsed(),
                logEntryHash));

        gs.setHistoryLog(log);
//        System.out.println(log.toString());

        movesMadeThisTurn[perGameId]++;

        // check if all players have movesMadeThisTurn yet
        boolean allMoved = true;
        for (int i = 0; i < numPlayers; ++i)
            allMoved = allMoved && (movesMadeThisTurn[i] == movesPerTurn);
        
        // if so, end the turn early
        if (allMoved)
            gs.getPhases().advancePhase();

        return true;
    }
    
    
    
    
   /*
    * Keep track of player transfers, but wait till the end of the turn to
    * actually calculate the transfers.
    */

    @Override public boolean doTransfer(int perGameId, int toPerGameId, ChipSet chips)
    {
//        // [DEBUG]
//        System.out.println("Player " + perGameId + " gives " + chips.getNumChips() 
//                              + " chips to player " + toPerGameId);
  
        // sanity checks
        PlayerStatus ps = gs.getPlayerByPerGameId(perGameId);
        if (!ps.getChips().contains(chips))
        {
            System.out.println("player doesn't have the chips");
            return false;
        }
        
        if (chips.getNumChips(chipColor2) != 0)
        {
            System.out.println("cannot give gold chips");
            return false;
        }
        
        // deduct chips from giver, store transaction in 2-D array
        ps.setChips(ChipSet.subChipSets(ps.getChips(), chips));
        exchanges[perGameId][toPerGameId] += chips.getNumChips();
                
        // add transfer to log
        HistoryLog log = gs.getHistoryLog();
        Phases phases = gs.getPhases();

        Hashtable<String, Object> logEntryHash =
                new Hashtable<String, Object>();
        logEntryHash.put("type", "transfer -> player " + perGameId + " gives " 
                + chips.toString() + " to " + toPerGameId);

        log.addHistoryItem(new HistoryEntry(phases.getCurrentPhaseName(),
                phases.getPhasesElapsed(),
                phases.getCurrentSecsElapsed(),
                logEntryHash));

        gs.setHistoryLog(log);
//        System.out.println(log.toString());
        
        // has this player given out all their tokens?
        if (ps.getChips().getNumChips(chipColor1) == 0)
            noChipsLeft[perGameId] = true;
        
        // have all players have given out all tokens?
        boolean allExchanged = true;
        for (int i = 0; i < numPlayers; ++i)
            allExchanged = allExchanged && noChipsLeft[i];

        // if so, end the turn early
        if (allExchanged)
            gs.getPhases().advancePhase();
        
        return true;
    }
    
    
    
     
    /*
     *  Start a new game and set up all of the game specifications.
     */
    public void run() {
    System.out.println("Let the game begin...");

    System.out.println("game id= " + gs.getGameId());

    GamePalette gp = new GamePalette();
    gp.add(chipColor1);
    gp.add(chipColor2);
    gp.add(boardColor1);
    gp.add(boardColor2);
    gs.setGamePalette(gp);

    // an example of retrieving data from the controller
    if(gs.getDataFromController() != null)
        System.out.println("we have data!!!!!!! " + (String) gs.getDataFromController());
    
    //for all the players...
    for( PlayerStatus player : gs.getPlayers() )
    {
        // set player chips
        ChipSet cs = initChips( gs.getGamePalette() );
        player.setChips( cs );

        // place player on board
        player.setPosition(new RowCol(localrand.nextInt(boardSize), 
                                      localrand.nextInt(boardSize)));
        oldPositions[player.getPerGameId()] = player.getPosition();

        // add initial player position to log
        HistoryLog log = gs.getHistoryLog();
        Phases phases = gs.getPhases();

        Hashtable<String, Object> logEntryHash =
                new Hashtable<String, Object>();
        logEntryHash.put("type", "placement -> player " + player.getPerGameId() + 
                " starts at " +  player.getPosition().toString());

        log.addHistoryItem(new HistoryEntry("Initialization Phase", -1, 0, logEntryHash));
        
        gs.setHistoryLog(log);
 //       System.out.println(log.toString());
   
        System.out.println("player pin: " +  player.getPin());
        System.out.println("player chips: " + player.getChips());
    }
    
//    // [DEBUG] print out player locationns    
//    for (int i = 0; i < numPlayers; ++i)
//        System.out.println("Player " + i + " is positioned at " + oldPositions[i]);

    colorBoard(gs.getGamePalette());
    
    // set up phase sequence
    ServerPhases ph = new ServerPhases(this);
    for (int i = 0; i < turnMax; ++i)
    {
        ph.addPhase("Movement Phase"/*, 50*/);
        ph.addPhase("Exchange Phase"/*, 50*/);
    }
    ph.addPhase("Feedback Phase", 10);

    gs.setScoring(this.s);

    ph.setLoop(false);
    gs.setPhases(ph);

    gs.setInitialized();  // will generate GAME_INITIALIZED message
    System.out.println("End game setup!");
    }

    /*
     * Creates checkered board
     */
    protected void colorBoard(GamePalette gp) {
            Board board = new Board(boardSize,boardSize);
            Square[][] squares = new Square[board.getRows()][board.getCols()];

            // alternate between powder blue and light coral
            for (int i = 0; i < board.getRows(); i++)
                for (int j = 0; j < board.getCols(); j++)
                {
                    squares[i][j] = new Square();
                    if ((i+j) % 2 == 0)
                        squares[i][j].setColor(boardColor1);
                    else
                        squares[i][j].setColor(boardColor2);
                }

            board.setSquares(squares);
            gs.setBoard(board);
    }

    /*
     *  Initialize player chips
     */
    protected static ChipSet initChips( GamePalette gp ) {
            ChipSet chipset = new ChipSet();

            chipset.set(chipColor1, 0);
            chipset.set(chipColor2, 0);
            chipset.set(boardColor1, 0);
            chipset.set(boardColor2, 0);

            return chipset;
    }

    
    
    /**
     * Filter players' views of other players' positions
     * 
     * @param fromserver complete set of player statuses as known by the server
     * @param perGameId  ID of the player filtered data is being sent to
     * @return           player status filtered based on receiving player's ID
     * 
     * This function intercepts the set of player statuses as they are being 
     * sent from the server to the client, and filters the data according to
     * what the receiving player is supposed to know. This allows us to create
     * a game of partial visibility.
     *
     * In this case, we don't want players to see other players' moves before
     * the end of the movement phase; however, we DO want moves to be registered
     * locally. To do this, we keep track of all players' board positions as
     * they were at the beginning of the turn in the array 'oldPositions'. Then,
     * we manipulate the information that is sent to client by replacing every
     * other player's board location with the old position. (The client's own
     * position is left unchanged, because the move should be seen locally.)
     * Finally, the filtered set of player statuses is returned.
     */

    @Override public Set<PlayerStatus> filterPlayerStatus
                        (Set<PlayerStatus> fromserver, int perGameId)
    {
        Set<PlayerStatus> newStatuses = new HashSet<PlayerStatus>();
        
        // copy fromserver into a new set and make changes to the new copy
        for (PlayerStatus ps : fromserver)
        {
            PlayerStatus ps_temp = new PlayerStatus(ps);

            if (ps_temp.getPerGameId() != perGameId)
            {
                // hide opponents' chips
                ChipSet hiddenOpponentCS = new ChipSet();
                hiddenOpponentCS.set(chipColor1, 0);
                hiddenOpponentCS.set(chipColor2, 0);                
                hiddenOpponentCS.set(boardColor1, 0);    
                hiddenOpponentCS.set(boardColor2, 0);
                ps_temp.setChips(hiddenOpponentCS);

                /* 
                 * Note: Though we have masked their true chip counts, opponents
                 * will still show up in the agent's player chip display
                 * (their chip counts will be displayed as 0). Still, this
                 * may be confusing to experimentees. If you if you want the
                 * opposing players to be completely invisible (i.e., they don't
                 * show up at all on the chip display panel), you must make
                 * changes to AllPlayersChipDisplay.java in gui/ctgui/original
                 * [lines 177-179].
                 */
                                
                // send opponents' old positions rather than the current ones
                if (oldPositions[ps_temp.getPerGameId()] != null)
                {
                    RowCol oldPosition = new RowCol(oldPositions[ps_temp.getPerGameId()]);
                    ps_temp.setPosition(oldPosition);
                }
            }
                    
            newStatuses.add(ps_temp);
        }
        
        return newStatuses;
    }

 }

