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
import java.util.Iterator;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * This configuration implements much of the setup and functionality.
 * Including automatic exchange after accept an offer, and automatic movement.
 */
public class InterruptionConfig extends GameConfigDetailsRunnable implements PhaseChangeHandler
{
    /**
     * The scoring function used for players in the game.
     * 100 for a player reaching the goal,
     * -10 per unit distance if the player does not reach the goal,
     * 5 for each chip remaining after the player has reached the goal
     * or cannot move any farther towards teh goal.
     */
    Scoring s = new Scoring(100, -10, 5);

    
    private static String boardColor1 = "powder blue";
	/** current turn we are on */
	private int turnCount;
	
	private static int numOfRounds=5;
	
	/** Local random generator for creating chipsets */
	static Random localrand = new Random();

	/** determines if there will be automatic movement */
	boolean automaticMovement = false;
	/** determines if the chips will automatically transfer after a
	 * proposal has been accepted */
	boolean automaticChipTransfer = true;
	/** determines if the phases will loop. */
	boolean phaseLoop = true;
	
	boolean movementAllowedInRound=true; //indicates whether players can move (if there was no interruption , they can move)
	boolean goalRevealed=false; //indicates whether the goal has been revealed to the agent (used for filtering board staturs)
	boolean goalStatusChanged=false;
	
	int accumulatedScore=0;
	
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

		boolean principalCommunicationAllowed = false;
		boolean principalTransfersAllowed     = false;
		boolean principalMovesAllowed         = false;
		
		boolean agentCommunicationAllowed = false;
		boolean agentTransfersAllowed     = false;
		boolean agentMovesAllowed         = false;

	    // FYI - for the first phase it won't work from here
	    if(phasename.equals("Communication Phase")) {
	    	agentCommunicationAllowed = true;
	    }

	    else if(phasename.equals("Movement Phase")) {
	    	
	    	System.out.println("in movement phase");
	    	if (movementAllowedInRound) //if movement is allowed, give each player one chip
	    	{
	    		System.out.println("movements allowed");
				for (PlayerStatus player : gs.getPlayers())
				{
					ChipSet cs = new ChipSet();
					cs.add(boardColor1, 1);
					player.setChips(cs);
	
				}
	    	}
	    	gs.getPlayerByPerGameId(0).setMovesAllowed(movementAllowedInRound);
		    gs.getPlayerByPerGameId(1).setMovesAllowed(movementAllowedInRound);

	    }

	    //set principal capabilities
	    gs.getPlayerByPerGameId(0).setCommunicationAllowed(principalCommunicationAllowed);
	    
	    gs.getPlayerByPerGameId(0).setTransfersAllowed(principalTransfersAllowed);
	    
	    //set agent capabilities
	    gs.getPlayerByPerGameId(1).setCommunicationAllowed(agentCommunicationAllowed);
	    gs.getPlayerByPerGameId(1).setTransfersAllowed(agentTransfersAllowed);
	    

	}


	/**
	 *	Called by server when a phase ends
	 */
	public void endPhase(String phasename) {
		System.out.println("A Phase Ended: " + phasename);

		
//		// if end of Communication phase, then if the interruption was accepted, reveal the goal, and disallow movement
//		//in the next phase
		if(phasename.equals("Communication phase")) {
			
		}
		
		
		else if (phasename.equals("Movement Phase")){
			turnCount++;
			//check whether to end game
			if (turnCount==numOfRounds)
			{
				gs.setEnded();
			}
			
			goalRevealed=false;//update indicator until next round
			
			movementAllowedInRound=true; //re-allow movement until next interruption in case it was disabled
			
			
			//if one of the players reached his goal, add score and change its goal location
			for (Goal g:gs.getBoard().getGoals())
			{
				if (g.getLocation().equals(gs.getPlayerByPerGameId(g.getType()).getPosition()))
				{
					accumulatedScore=accumulatedScore+10; //add score
					RowCol newLocation=getNewRandGoalLocation(g.getLocation());
					gs.getBoard().moveGoal(g, newLocation);
					
				}
			}
			
			
			//select new goal location uniformly from adjacent squares
			for (Goal g:gs.getBoard().getGoals())
			{
				RowCol dest=getNewGoalLocation(g.getLocation());
				gs.getBoard().moveGoal(g, dest);
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

			if( bpddm.accepted() )
			{
				//revealing goal to agent
				goalRevealed=true;
				gs.getBoard().moveGoal(0, gs.getBoard().getGoalLocations(0).get(0));
				gs.getBoard().notifyObservers();
				//disallow movement for next round
				movementAllowedInRound=false;				
			}
		}

		return result;
    }

	/**
		Start a new game and set up all of the game specifications.
	*/
	public void run() {
		System.out.println("Interruption game begins...");

		System.out.println("game id= " + gs.getGameId());

        GamePalette gp = new GamePalette();
        gp.add(boardColor1);
        gs.setGamePalette(gp);


		// assign game-board colors
		setRandBoard(gs.getGamePalette());


		// set up phase sequence
		ServerPhases ph = new ServerPhases(this);
		ph.addPhase("Communication Phase", 30);
		ph.addPhase("Movement Phase", 20);

        gs.setScoring(this.s);

        ph.setLoop(phaseLoop);
		gs.setPhases(ph);

		gs.setInitialized();  // will generate GAME_INITIALIZED message
	}


	/**
	Places random colors on specified board
	*/
	protected void setRandBoard(GamePalette gp) {
		Board board = new Board(6,6);
		Square[][] squares = new Square[board.getRows()][board.getCols()];

		for (int i = 0; i < board.getRows(); i++) {
			for (int j = 0; j < board.getCols(); j++) {
		      squares[i][j] = new Square();
		      squares[i][j].setColor(boardColor1);
			}
		}
		board.setSquares(squares);
		
		ArrayList<RowCol> takenPositions=new ArrayList<RowCol>();
		// assign player piece and goal locations
		RowCol principalPos=new RowCol(localrand.nextInt(6), localrand.nextInt(6));
		gs.getPlayerByPerGameId(0).setPosition(principalPos);  // principal's location
		takenPositions.add(principalPos);
		
		//set agent's position, make sure to not land on same position as principal
		RowCol agentPos=new RowCol(localrand.nextInt(6), localrand.nextInt(6));
		while(takenPositions.contains(agentPos))
		{
			agentPos=new RowCol(localrand.nextInt(6), localrand.nextInt(6));
		}
		gs.getPlayerByPerGameId(1).setPosition(agentPos);  // agent location
		takenPositions.add(agentPos);
		
		RowCol principalGoalLoc=new RowCol(localrand.nextInt(6), localrand.nextInt(6));
		while(takenPositions.contains(principalGoalLoc))
		{
			principalGoalLoc=new RowCol(localrand.nextInt(6), localrand.nextInt(6));
		}
		board.setGoal(principalGoalLoc, true,0);             // goal location for principal
		takenPositions.add(principalGoalLoc);
		
		RowCol agentGoalLoc=new RowCol(localrand.nextInt(6), localrand.nextInt(6));
		while(takenPositions.contains(agentGoalLoc))
		{
			agentGoalLoc=new RowCol(localrand.nextInt(6), localrand.nextInt(6));
		}
		board.setGoal(agentGoalLoc, true,1);             // goal location for agent
		takenPositions.add(agentGoalLoc);
		
		
		
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
			chipset.set( color, 10 ); //need to see what to do with chips

		return chipset;
	}
	
	public Board filterBoard(Board serverboard, int perGameId)
	{
		
		System.out.println("in filterBoard");
		if (turnCount>0)
		{
			Set<Goal> goals=serverboard.getGoals();
			Iterator<Goal> it = goals.iterator();
			RowCol principalGoal= gs.getBoard().getGoalLocations(0).get(0);
			RowCol agentGoal= gs.getBoard().getGoalLocations(1).get(0);
	
			
			if (perGameId==1)
			{
	//			System.out.println(" goal g features");
	//			System.out.println(serverboard.getSquare(g.getLocation()).getFeatures());
	//			System.out.println(" goal gother features");
	//			System.out.println(serverboard.getSquare(gOther.getLocation()).getFeatures());
	//			
				if (!goalRevealed)
				{
					System.out.println("i'm removing my goal");
					serverboard.getSquare(agentGoal).remove("Goal.goal1");
				}
	
				
			}
		}
			return serverboard;
		
	}
	
	
	/**
	 * Given a current location, picks a random adjacent location, without falling off the board
	 * 
	 * @param cur
	 * @return
	 * @author KPozin
	 */
	private RowCol getNewGoalLocation(RowCol cur)
	{
		// Get a list of valid locations to move the goal
		ArrayList<RowCol> neighbors = new ArrayList<RowCol>(cur.getNeighbors(gs.getBoard()));

		// We'll try to avoid placing goals on top of players and on the other goal
		ArrayList<RowCol> occupiedCells = new ArrayList<RowCol>();
		//players' locations
		for (PlayerStatus p : gs.getPlayers())
		{
			occupiedCells.add(p.getPosition());
		}
		//other goal location
		for (RowCol goalLoc: gs.getBoard().getGoalLocations())
		{
			if (!goalLoc.equals(cur))
			{
				occupiedCells.add(goalLoc);
			}
		}
		if (!(occupiedCells.containsAll(neighbors)))
		{
			neighbors.removeAll(occupiedCells);
		}
		
		return neighbors.get(localrand.nextInt(neighbors.size()));
	}
	
	/**
	 * Given a current location, picks a random adjacent location, without falling off the board
	 * 
	 * @param cur
	 * @return
	 * @author KPozin
	 */
	private RowCol getNewRandGoalLocation(RowCol cur)
	{
		// Get a list of valid locations to move the goal
		
		// We'll try to avoid placing goals on top of players and on the other goal
		ArrayList<RowCol> occupiedCells = new ArrayList<RowCol>();
		//players' locations
		for (PlayerStatus p : gs.getPlayers())
		{
			occupiedCells.add(p.getPosition());
		}
		//other goal location
		for (RowCol goalLoc: gs.getBoard().getGoalLocations())
		{
			if (!goalLoc.equals(cur))
			{
				occupiedCells.add(goalLoc);
			}
		}
		
		RowCol newLocation=new RowCol(localrand.nextInt(6), localrand.nextInt(6));
		while (occupiedCells.contains(newLocation))
		{
			 newLocation=new RowCol(localrand.nextInt(6), localrand.nextInt(6));
		}
		
		return newLocation;

	}
}

