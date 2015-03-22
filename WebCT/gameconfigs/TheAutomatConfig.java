

import edu.harvard.eecs.airg.coloredtrails.server.ServerData;
import edu.harvard.eecs.airg.coloredtrails.server.ServerGameStatus;
import edu.harvard.eecs.airg.coloredtrails.server.ServerPhases;
import edu.harvard.eecs.airg.coloredtrails.shared.GlobalColorMap;
import edu.harvard.eecs.airg.coloredtrails.shared.Utility;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;
import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;

import java.util.Hashtable;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This configuration implements much of the setup and functionality.
 * Including automatic exchange after accept an offer, and automatic movement.
 */
public class TheAutomatConfig extends GameConfigDetailsRunnable implements PhaseChangeHandler
{
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

	/** determines if there will be automatic movement */
	boolean automaticMovement = true;
	/** determines if the chips will automatically transfer after a
	 * proposal has been accepted */
	boolean automaticChipTransfer = true;
	/** determines if the phases will loop. */
	boolean phaseLoop = true;


	/**
		Returns score of specified player, according to player's current state
	*/
	public int getPlayerScore(PlayerStatus ps)
	{
		RowCol gpos = gs.getBoard().getGoalLocations().get(0);  // get first goal in list
		return (int)Math.floor(s.score(ps, gpos));  // should change to double at some point
    }


	/**
		Called by GameConfigDetailsRunnable methods when calculation and assignment
		of player scores is desired
	*/
	protected void assignScores() {
		for( PlayerStatus ps : gs.getPlayers() ) {
			ps.setScore(getPlayerScore(ps));
			System.out.println("Player: " + ps.getPin() + "  Score: " + ps.getScore());
		}
	}


	/**
		Called by server when a phase begins
	*/
	public void beginPhase(String phasename) {
		System.out.println("A New Phase Began: " + phasename);

		boolean communicationAllowed = false;
		boolean transfersAllowed     = false;
		boolean movesAllowed         = false;

	    // FYI - for the first phase it won't work from here
	    if(phasename.equals("Communication Phase")) {
	    	communicationAllowed = true;
	    }
	    else if(phasename.equals("Exchange Phase")) {
	    	transfersAllowed = true;
	    }
	    else if(phasename.equals("Movement Phase")) {
	    	movesAllowed = true;
	    }
	    else if(phasename.equals("Feedback Phase")) {
	    }
	  
	    for( PlayerStatus player : gs.getPlayers() ) {
	    	
	    	
		    	player.setCommunicationAllowed(communicationAllowed);
		    	player.setTransfersAllowed(transfersAllowed);
		    	player.setMovesAllowed(movesAllowed);
	    	
	    	

	    }
	}


	/**
	 *	Called by server when a phase ends
	 */
	public void endPhase(String phasename) {
		System.out.println("A Phase Ended: " + phasename);

		// if end of feedback phase, then end the game
		if(phasename.equals("Feedback Phase")) {
			gs.setEnded();
		}

		// ### automatic movment ###
		else if (phasename.equals("Communication Phase")) {
			if( automaticMovement ) {
				doAutomaticMovement( s );

				// calculate scores after all players have moved
				// (e.g, in case a player's score depends on others' locations)
				assignScores();
			}
		}
	}

	// Override super method do discourse in order to make an automatic transfer after accept a proposal
	public boolean doDiscourse(DiscourseMessage dm) {
		System.out.println( "Received Discourse Message " );
        System.out.println( "Class: " + dm.getClass() );
        System.out.println( "From: " + dm.getFromPerGameId() );
        System.out.println( "From: " + dm.getToPerGameId() );

        boolean result = gs.doDiscourse(dm);

		// ### automatic exchange of chips upon acceptance of a proposal ###
		if( dm instanceof BasicProposalDiscussionDiscourseMessage ) {
		    System.out.println( "---- BasicProposalDiscussionDiscourseMessage ----" );

            BasicProposalDiscussionDiscourseMessage bpddm = (BasicProposalDiscussionDiscourseMessage) dm;

			if( automaticChipTransfer && bpddm.accepted() )
				doAutomaticChipTransfer( bpddm );
		}

		return result;
    }

	/**
		Start a new game and set up all of the game specifications.
	*/
	public void run() {
		System.out.println("Let the game begin...");

		System.out.println("game id= " + gs.getGameId());

        GamePalette gp = new GamePalette();
        gp.add("RGBRed");
        gp.add("RGBGreen");
        gp.add("purple1");
        gp.add("orange1");
        gs.setGamePalette(gp);
        // set game palette
		//gs.addColorToGamePalette("RGBRed");
		//gs.addColorToGamePalette("RGBGreen");
		//gs.addColorToGamePalette("purple1");
		//gs.addColorToGamePalette("orange1");
       
		//for all the players
		for( PlayerStatus player : gs.getPlayers() ) {
			// set chips for players
			ChipSet cs = getRandChipSet( gs.getGamePalette() );
			player.setChips( cs );
			System.out.println("player pin: " +  player.getPin());
			System.out.println("player chips: " + player.getChips());
			if(player.getPerGameId() == 0)
			{
				player.setRole("Responder");
			}
			else
			{
				player.setRole("Proposer");
			}
			// set teams for players
			//player.setTeamId(3);
		}

		// assign game-board colors
		setRandBoard(gs.getGamePalette());

		// assign player piece and goal locations
		gs.getPlayerByPerGameId(0).setPosition(new RowCol(1, 1));  // player 1's location
		gs.getPlayerByPerGameId(1).setPosition(new RowCol(2, 1));  // player 2's location
  
		// set up phase sequence
		ServerPhases ph = new ServerPhases(this);
		ph.addPhase("Communication Phase", 70);
		ph.addPhase("Exchange Phase", 60);
		ph.addPhase("Movement Phase", 60); 
		ph.addPhase("Feedback Phase", 60);

        gs.setScoring(this.s );

        ph.setLoop(phaseLoop);
		gs.setPhases(ph);

		gs.setInitialized();  // will generate GAME_INITIALIZED message
	}


	/**
	Places random colors on specified board
	*/
	protected void setRandBoard(GamePalette gp) {
		Board board = new Board(4,4);
		Square[][] squares = new Square[board.getRows()][board.getCols()];

		for (int i = 0; i < board.getRows(); i++) {
			for (int j = 0; j < board.getCols(); j++) {
		      squares[i][j] = new Square();
		      squares[i][j].setColor(gp.getRandomColor());
			}
		}
		board.setSquares(squares);
    board.setGoal(new RowCol(2, 3), true);             // goal location player 0
    //    board.setGoal(new RowCol(2, 3), true,1);             // goal location player 1
		gs.setBoard(board);
	}

	/**
		Generates random chipset for specified player
		<p>
		add total #chips as parameter
	*/
	protected static ChipSet getRandChipSet( GamePalette gp ) {
		ChipSet chipset = new ChipSet();

		for( String color : gp.getColors() )
			chipset.set( color, localrand.nextInt(4) );

		return chipset;
	}
}