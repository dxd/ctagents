


import edu.harvard.eecs.airg.coloredtrails.server.ServerData;
import edu.harvard.eecs.airg.coloredtrails.server.ServerGameStatus;
import edu.harvard.eecs.airg.coloredtrails.server.ServerPhases;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;
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
public class PhaseTestConfig extends GameConfigDetailsRunnable implements PhaseChangeHandler
{
     // 100 for goal, -10 per unit distance, 5 for each chip
     Scoring s = new Scoring(100, -10, 5);


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
	private void assignScores()
	{
		for( PlayerStatus ps : gs.getPlayers() ) {
			ps.setScore(getPlayerScore(ps));
			System.out.println("Player: " + ps.getPin() + "  Score: " + ps.getScore());
		}
	}


	/**
		Called by server when a phase begins
	*/
	public void beginPhase(String phasename)
	{
      System.out.println("New Phase Beginning: " + phasename);

      // FYI - for the first phase it won't work from here
      if (phasename.equals("Communication Phase"))
      {
        gs.getPlayerByPerGameId(0).setCommunicationAllowed(true);
        gs.getPlayerByPerGameId(1).setCommunicationAllowed(true);

        gs.getPlayerByPerGameId(0).setTransfersAllowed(false);
        gs.getPlayerByPerGameId(1).setTransfersAllowed(false);

        gs.getPlayerByPerGameId(0).setMovesAllowed(false);
        gs.getPlayerByPerGameId(1).setMovesAllowed(false);
      }

      else if (phasename.equals("Exchange Phase"))
      {
        gs.getPlayerByPerGameId(0).setCommunicationAllowed(false);
        gs.getPlayerByPerGameId(1).setCommunicationAllowed(false);

        gs.getPlayerByPerGameId(0).setTransfersAllowed(true);
        gs.getPlayerByPerGameId(1).setTransfersAllowed(true);

        gs.getPlayerByPerGameId(0).setMovesAllowed(false);
        gs.getPlayerByPerGameId(1).setMovesAllowed(false);
      }

      else if (phasename.equals("Movement Phase"))
      {
        gs.getPlayerByPerGameId(0).setCommunicationAllowed(false);
        gs.getPlayerByPerGameId(1).setCommunicationAllowed(false);

        gs.getPlayerByPerGameId(0).setTransfersAllowed(false);
        gs.getPlayerByPerGameId(1).setTransfersAllowed(false);

        gs.getPlayerByPerGameId(0).setMovesAllowed(true);
        gs.getPlayerByPerGameId(1).setMovesAllowed(true);
      }

      else if (phasename.equals("Feedback Phase"))
      {
        gs.getPlayerByPerGameId(0).setCommunicationAllowed(false);
        gs.getPlayerByPerGameId(1).setCommunicationAllowed(false);

        gs.getPlayerByPerGameId(0).setTransfersAllowed(false);
        gs.getPlayerByPerGameId(1).setTransfersAllowed(false);

        gs.getPlayerByPerGameId(0).setMovesAllowed(false);
        gs.getPlayerByPerGameId(1).setMovesAllowed(false);
      }
    }


    /**
      Called by server when a phase ends
    */
    public void endPhase(String phasename)
    {
      System.out.println("Phase Ended: " + phasename);

      // if end of feedback phase, then end the game
      if (phasename.equals("Feedback Phase"))
      {
        gs.setEnded();
      }
/*
      // ### automatic movment ###
      else if (phasename.equals("Communication Phase"))
      {
        System.out.println("AUTOMATIC MOVEMEMT");
        gs.getPlayerByPerGameId(0).setMovesAllowed(true);  // allow movement
        gs.getPlayerByPerGameId(1).setMovesAllowed(true);

        // for each player in game, make best use of their chips and move accordingly
        ArrayList<PlayerStatus> playersInGame = gs.getPlayers();
        for (PlayerStatus ps : playersInGame)
        {
          BestUse bu = new BestUse(gs, ps, s, 0);  // calculate the best use of player's chips
          Path p = bu.getPaths().get(0);           // use the first best-path in list
          gs.doPathMove(ps.getPerGameId(), p);     // move the player
          System.out.println(bu);                  // print BU instance to console
          System.out.println(p);                   // print path to console
        }

        // calculate scores after all players have moved
        // (e.g, in case a player's score depends on others' locations)
        assignScores();
      }
*/
    }


    // Override super method do discourse in order to make an automatic transfer after accept a proposal
    public boolean doDiscourse(DiscourseMessage dm){
        boolean result = gs.doDiscourse(dm);
        // ### automatic exchange of chips upon acceptance of a proposal ###
		if( dm instanceof BasicProposalDiscussionDiscourseMessage ) {
		    System.out.println( "---- BasicProposalDiscussionDiscourseMessage ----" );

            BasicProposalDiscussionDiscourseMessage bpddm = (BasicProposalDiscussionDiscourseMessage) dm;

			if( bpddm.accepted() )
				doAutomaticChipTransfer( bpddm );
		}
        return result;
    }

    boolean id0moved = false;
    boolean id1moved = false;

    /**
     * The movement phase waits until both players move on the board
     * before it ends
     */
    public boolean doMove(int perGameId, RowCol newpos)
    {
    	boolean result = gs.doMove(perGameId, newpos);

    	if (result)
    		if (perGameId == 0)
    			id0moved = true;
    		else if (perGameId == 1)
    			id1moved = true;

    	if (id0moved && id1moved)
    		gs.getPhases().advancePhase();

    	return result;
    }


    /**
      Start a new game
    */
    public void run()
	{
      System.out.println("Starting CT game ...");

      System.out.println("game id = " + gs.getGameId());

      // set game palette
	  gs.addColorToGamePalette("RGBRed");
	  gs.addColorToGamePalette("RGBGreen");
	  gs.addColorToGamePalette("purple1");
	  gs.addColorToGamePalette("orange1");

	  // set chips for players
	  randChipSet(gs.getPlayerByPerGameId(0), gs.getGamePalette());
	  randChipSet(gs.getPlayerByPerGameId(1), gs.getGamePalette());

	  // set teams for players
	  gs.getPlayerByPerGameId(0).setTeamId(3);
	  gs.getPlayerByPerGameId(1).setTeamId(3);

	  // assign game-board colors
      gs.makeNewBoard(4, 4);
	  randBoardColors(gs.getBoard(), gs.getGamePalette());

	  // assign player piece and goal locations
	  gs.getPlayerByPerGameId(0).setPosition(new RowCol(1, 1));  // player 1's location
	  gs.getPlayerByPerGameId(1).setPosition(new RowCol(2, 1));  // player 2's location
	  gs.getBoard().setGoal(new RowCol(2, 3), true);             // goal location

	  // set up phase sequence
	  ServerPhases ph = new ServerPhases(this);
	  ph.addPhase("Communication Phase");
	  ph.addPhase("Movement Phase");
	  ph.addPhase("Feedback Phase", 10);
      ph.setLoop(false);
      gs.setPhases(ph);

      if(ph.getCurrentPhaseName().equals("Communication Phase"))
	  {
        gs.getPlayerByPerGameId(0).setCommunicationAllowed(true);
        gs.getPlayerByPerGameId(1).setCommunicationAllowed(true);
      }

      gs.setInitialized();  // will generate GAME_INITIALIZED message
    }


	/**
	Places random colors on specified board
	*/
	private void randBoardColors(Board board, GamePalette gp)
	{
		Square[][] squares = new Square[board.getRows()][board.getCols()];

		for (int i = 0; i < board.getRows(); i++)
		for (int j = 0; j < board.getCols(); j++)
				{
		  squares[i][j] = new Square();
		  squares[i][j].setColor(gp.getRandomColor());
		}

		board.setSquares(squares);
	}


	/** Local random generator for creating chipsets */
	static Random localrand = new Random();

	/**
		Generates random chipset for specified player
		<p>
		add total #chips as parameter
	*/
	private void randChipSet(PlayerStatus ps, GamePalette gp)
	{
		ChipSet playerChipset = new ChipSet();

			List<String> chipcolors = gp.getColors();
			for (int i=0; i<6; ++i)
				playerChipset.add(chipcolors.get(localrand.nextInt(4)), 1);

		ps.setChips(playerChipset);
		System.out.println("player pin: " +  ps.getPin());
		System.out.println("player chips: " + ps.getChips());
	}
}