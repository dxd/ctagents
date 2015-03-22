
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.ScoringMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.ForwardMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;
import edu.harvard.eecs.airg.coloredtrails.server.ServerPhases;
import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;

import java.util.Random;

import java.io.FileWriter;

/**
 * This configuration file configures a multi-round game where two proposers
 * send chip proposals to one responder.  There are only two phases, a communication phase
 * and a feedback phase.  The players never actually exchange chips or move.
 */
public class TwoProposersOneResponderConfig extends GameConfigDetailsRunnable implements PhaseChangeHandler {

    /**
     * The scoring function used for players in the game.
     * 100 for a player reaching the goal,
     * -10 per unit distance if the player does not reach the goal,
     * 5 for each chip remaining after the player has reached the goal
     * or cannot move any farther towards teh goal.
     */
    Scoring s = new Scoring(100, -10, 5);

	/** Local random generator for creating chipsets */
	static Random localrand = new Random();

    ChipSet responderCS;    //responder chipset
    ChipSet proposer0CS;	//proposer 0 chipset
    ChipSet proposer1CS;	//proposer 1 chipset

    RowCol responderLoc;    //responder location
    RowCol proposer0Loc;	//proposer 0 location
    RowCol proposer1Loc;	//proposer 1 location

    GamePalette gp;			//The game palette used in the game
    Board board;			//The board used in the game.

    /* The PlayerStatus objects. One for each player. 1 responder and two proposers.
     * Proposer 0 will have id 0
     * Proposer 1 will have id 1
     * Responder  will have id 2 
     */
    PlayerStatus responder;	
    PlayerStatus proposer0;
    PlayerStatus proposer1;

    /* Store the Proposal Messages sent by the two proposers.
     * null if no proposal received.
     */
    BasicProposalDiscourseMessage proposer0Proposal;
    BasicProposalDiscourseMessage proposer1Proposal;
    /* Store whether or not the proposals have been accepted.
     * null means no proposal has been accepted.
     */
    Boolean acceptedProposer0;
    Boolean acceptedProposer1;

    /*will hold the output of the scores*/
    FileWriter out;
    /* The round or the current game we are playing */
    int round = 0;

    /** 
     * The constructor sets up the basic elements of the game, but the game 
     * does not actually start until the run() command is called.  
     */
    public TwoProposersOneResponderConfig() {

        //Create the GamePalette with only two colors.  red and green.
        gp = new GamePalette();
		gp.add("RGBRed");
		gp.add("RGBGreen");

        //Create the board.
        board = new Board(4,4);
		Square[][] squares = new Square[board.getRows()][board.getCols()];

		//Initialize the squares to all red.
		for (int i = 0; i < board.getRows(); i++) {
			for (int j = 0; j < board.getCols(); j++) {
		      squares[i][j] = new Square();
		      squares[i][j].setColor("RGBRed");
			}
		}
        board.setSquares(squares);
		board.setGoal(new RowCol(3, 3), true);	// goal location to the lower right

        /* create the chip sets.
         * The responder has enough red chips to get itself and one
         * proposer to the goal.  The proposers have green chips that
         * they can use to request the red chips, however, proposer 0
         * has one more green chip than proposer 1.
         */
        responderCS = new ChipSet();
        responderCS.add("RGBRed",   9 );
        responderCS.add("RGBGreen", 0 );
        proposer0CS = new ChipSet();
        proposer0CS.add("RGBRed",   0 );
        proposer0CS.add("RGBGreen", 10 );
        proposer1CS = new ChipSet();
        proposer1CS.add("RGBRed",   0 );
        proposer1CS.add("RGBGreen", 9 );

        //define the player positions
        responderLoc = new RowCol(0,0);	//six squares from the goal
        proposer0Loc = new RowCol(3,0);	//three squares from the goal
        proposer1Loc = new RowCol(0,3);	//three squares from the goal

        //Open a file to write output for scores.
        try {
            out = new FileWriter( "outputScores.txt" );
            out.write( "Round\tProposer0\tProposer1\tResponder\n" );
            out.flush();
        }
        catch( Exception e ) {
            throw new RuntimeException( "Problem creating output." );
        }
    }

	/**
		Start a new game and set up all of the game specifications.
		The values for the PlayerStatus are initialized here, and the
		game status is set up here.
	*/
	public void run() {
		System.out.println("Starting run()");

        //get the players.
        proposer0 = gs.getPlayerByPerGameId(0);
        proposer1 = gs.getPlayerByPerGameId(1);
        responder = gs.getPlayerByPerGameId(2);

        //set all the values of the players, the board, etc.

        // set game palette
		gs.setGamePalette(gp);

        //set the chips of the players
        responder.setChips( responderCS );
        proposer0.setChips( proposer0CS );
        proposer1.setChips( proposer1CS );

        //set the board
        gs.setBoard( board );

        //set the player locations
        responder.setPosition( responderLoc );
        proposer0.setPosition( proposer0Loc );
        proposer1.setPosition( proposer1Loc );

        //Set whether or not the players have sent messages
        proposer0Proposal = null;
        proposer1Proposal = null;
        acceptedProposer0 = null;
        acceptedProposer1 = null;

		// set up phase sequence
		ServerPhases ph = new ServerPhases(this);
		ph.addPhase("Communication Phase", 20);
		/* The Feedback phase will send the scores and does not need to last any time */
		ph.addPhase("Feedback Phase", 0);

        //set the scoring function.
        gs.setScoring(this.s);

        //loop the phases.
        ph.setLoop(true);
		gs.setPhases(ph);

		gs.setInitialized();  // will generate GAME_INITIALIZED message
	}


    /**
		Returns score of specified player, according to player's current state
	*/
	public int getPlayerScore(PlayerStatus ps) {
        throw new RuntimeException( "Not using getPlayerScore(PlayerStatus ps) in TwoProposersOneResponderConfig." );
    }


    /**
		Called by server when a phase begins
	*/
	public synchronized void beginPhase(String phasename) {
        //Initialize communication, transfers and movement to false.
        boolean communicationAllowed = false;

        if(phasename.equals("Communication Phase")) {
            ++round; //only update the round for communicaton phase.
        }
        System.err.println( "---------- Beginning " + phasename + " at round " + round + " ----------" );

	    // FYI - for the first phase it won't work from here
	    if(phasename.equals("Communication Phase")) {
            proposer0Proposal = null;
            proposer1Proposal = null;
            acceptedProposer0 = null;
            acceptedProposer1 = null;

            communicationAllowed = true; //allow communication
        }
	    else if(phasename.equals("Feedback Phase")) {

            //first we determine the scores, then we assign them.

            //if there wasn't a proposal then it cannot be accepted.
            if( acceptedProposer0 == null )
                acceptedProposer0 = false;
            if( acceptedProposer1 == null )
                acceptedProposer1 = false;

            //make a copy of all the player statuses.
            PlayerStatus p0New = new PlayerStatus( proposer0 );
            PlayerStatus p1New = new PlayerStatus( proposer1 );
            PlayerStatus rNew  = new PlayerStatus( responder );

            if( !acceptedProposer0 && !acceptedProposer1 ) {
                //No proposals accepted, so no need to change chips.
            }
            //else, if both the proposals are accepted.
            else if( acceptedProposer0 && acceptedProposer1 ) {
                /*check to make sure that transfers can be made.*/

                //first, calculate chips to be sent
                ChipSet proposer0toSend = proposer0Proposal.getChipsSentByProposer();
                ChipSet proposer1toSend = proposer1Proposal.getChipsSentByProposer();
                ChipSet responderToSendToP0 = proposer0Proposal.getChipsSentByResponder();
                ChipSet responderToSendToP1 = proposer1Proposal.getChipsSentByResponder();

                ChipSet responderCStoSend = ChipSet.addChipSets( responderToSendToP0, responderToSendToP1 );

                //then see if the chips can be sent
                boolean responderCanSend = responder.getChips().contains( responderCStoSend );
                boolean proposer0CanSend = proposer0.getChips().contains( proposer0toSend );
                boolean proposer1CanSend = proposer1.getChips().contains( proposer1toSend );

                //if all transactions can be made then exchange the chips.
                if( responderCanSend && proposer0CanSend && proposer1CanSend ) {
                    exchange( p0New, rNew, proposer0toSend, responderToSendToP0 );
                    exchange( p1New, rNew, proposer1toSend, responderToSendToP1 );
                }
            }
            //else, if only the proposal from Proposer 0 was accepted.
            else if( acceptedProposer0 ) {
                /*check to make sure that transfers can be made.*/

                ChipSet proposer0toSend = proposer0Proposal.getChipsSentByProposer();
                ChipSet responderToSend = proposer0Proposal.getChipsSentByResponder();

                exchange( p0New, rNew, proposer0toSend, responderToSend );
            }
            //else, if only the proposal from Proposer 1 was accepted.
            else if( acceptedProposer1 ) {
                /*check to make sure that transfers can be made.*/

                ChipSet proposer1toSend = proposer1Proposal.getChipsSentByProposer();
                ChipSet responderToSend = proposer1Proposal.getChipsSentByResponder();

                exchange( p1New, rNew, proposer1toSend, responderToSend );
            }

            //assign the scores.
            int score0 = (new BestUse( gs, p0New, s, 0 )).getBestState().getScore();
            int score1 = (new BestUse( gs, p1New, s, 0 )).getBestState().getScore();
            int score2 = (new BestUse( gs, rNew,  s, 0 )).getBestState().getScore();

            //create a scoring message to be sent to each of the three players
            ScoringMessage scoreTo0 = new ScoringMessage( -1, 0, round, score0, score1, score2 );
            ScoringMessage scoreTo1 = new ScoringMessage( -1, 1, round, score0, score1, score2 );
            ScoringMessage scoreTo2 = new ScoringMessage( -1, 2, round, score0, score1, score2 );

            //send the score messages
            gs.doDiscourse(scoreTo0);
            gs.doDiscourse(scoreTo1);
            gs.doDiscourse(scoreTo2);

            //Write the score out to file.
            try {
                out.write( round + "\t" + score0 + "\t" + score1 + "\t" + score2 + "\n" );
                out.flush();
            }
            catch( Exception e ) {
                throw new RuntimeException( "Problem creating output." );
            }

            System.err.println( "Scores:\t" + score0 + "\t" + score1 + "\t" + score2 );

            //gs.getServerPhases().advancePhase();
            //return;
        }

        //set communication, transfers, and moves of the agents.
        for( PlayerStatus player : gs.getPlayers() ) {
	    	player.setCommunicationAllowed(communicationAllowed);
	    	player.setTransfersAllowed(    false);
	    	player.setMovesAllowed(        false);
	    }
	}

    /**
     * Exchanges chips between agents.
     * @param a A player that is to exchange chips with b.
     * @param b A player that is to exchange chips with a.
     * @param a_to_b The chips a wishes to send to b.
     * @param b_to_a The chips b wishes to send to a.
     */
    private static void exchange( PlayerStatus a, PlayerStatus b, ChipSet a_to_b, ChipSet b_to_a ) {
        //determine of the chips can be transfered
        boolean aCanSend = a.getChips().contains( a_to_b );
        boolean bCanSend = b.getChips().contains( b_to_a );

        //If the chips can be transfered, then transfer them.
        if( aCanSend && bCanSend ) {
            ChipSet aCS = ChipSet.subChipSets( a.getChips(), a_to_b );
            aCS = ChipSet.addChipSets( aCS, b_to_a );

            ChipSet bCS = ChipSet.subChipSets( b.getChips(), b_to_a );
            bCS = ChipSet.addChipSets( bCS, a_to_b );

            a.setChips( aCS );
            b.setChips( bCS );
        }
    }

    /**
	 *	Called by server when a phase ends
	 */
	public void endPhase(String phasename) {
        System.err.println( "---------- End Phase " + phasename + " at round " + round + " ----------" );
	}

	// Override super method do discourse in order to make an automatic transfer after accept a proposal
	public boolean doDiscourse(DiscourseMessage dm) {
		//System.out.println( "---------- Received Discourse Message ----------" );
        //System.out.println( "Class: " + dm.getClass().toString().substring(59) );
        //System.out.println( "From:  " + dm.getFromPerGameId() );
        //System.out.println( "To:    " + dm.getToPerGameId() );
        //System.out.println( "Msg ID " + dm.getMessageId() );

        //Send the message on.
        boolean result = gs.doDiscourse(dm);

        //If we have a response message.
        if( dm instanceof BasicProposalDiscussionDiscourseMessage) {
            BasicProposalDiscussionDiscourseMessage bpddm = (BasicProposalDiscussionDiscourseMessage) dm;

            if( bpddm.getFromPerGameId() != responder.getPerGameId() ) {
                throw new RuntimeException( "Only responder should send BasicProposalDiscussionDiscourseMessage.\n" +
                                            "BPDDM Sent by id " + bpddm.getFromPerGameId() );
            }

            if( bpddm.getProposerID() == proposer0.getPerGameId() ) {
                acceptedProposer0 = bpddm.accepted();
                //System.out.println( "Proposer 0 accepted? " + acceptedProposer0 );
            }
            else if( bpddm.getProposerID() == proposer1.getPerGameId() ) {
                acceptedProposer1 = bpddm.accepted();
                //System.out.println( "Proposer 1 accepted? " + acceptedProposer1 );
            }
            else {
                throw new RuntimeException( "Response message sent to unknown proposer.\n" );
            }

            if( acceptedProposer0 != null && acceptedProposer1 != null ) {
                //once two proposal responses are received then we can end the communication round.
            	System.err.println( "Ending communication phase because two responses received." );
                gs.getServerPhases().advancePhase();
            }
        }
        //else, if we have a proposal message
        else if( dm instanceof BasicProposalDiscourseMessage) {
            BasicProposalDiscourseMessage bpdm = (BasicProposalDiscourseMessage) dm;


            //System.out.println( "Proposer  to send:    " + bpdm.getChipsSentByProposer()  );
            //System.out.println( "Responder to send:    " + bpdm.getChipsSentByResponder() );

            //If the proposal is from Proposer 0
            if( bpdm.getProposerID() == proposer0.getPerGameId() ) {
                //Make sure the proposer isn't sending multiple proposals.
                if( proposer0Proposal != null ) {
                   throw new RuntimeException( "Proposer 0 has already sent a proposal message this communication phase." );
                }
                proposer0Proposal = bpdm;

	            //Forward the message to proposer 1
	            ForwardMessage forward = new ForwardMessage( proposer1.getPerGameId(), bpdm );
                gs.doDiscourse(forward);
            }
            //If the proposal is from Proposer 1
            else if( bpdm.getProposerID() == proposer1.getPerGameId() ) {
                if( proposer1Proposal != null ) {
                //Make sure the proposer isn't sending multiple proposals.
                    throw new RuntimeException( "Proposer 1 has already sent a proposal message this communication phase." );
                }
                proposer1Proposal = bpdm;

	            //Forward the message to proposer 0.
	            ForwardMessage forward = new ForwardMessage( proposer0.getPerGameId(), bpdm );
                gs.doDiscourse(forward);
            }
            //If the proposal is from the responder then we have a problem.
            else if( bpdm.getProposerID() == responder.getPerGameId() ) {
                throw new RuntimeException( "Responder should not be sending proposal messages." );
            }
            else {
                throw new RuntimeException( "Proposal came from unknown source." );
            }
        }

		return result;
	}
}
