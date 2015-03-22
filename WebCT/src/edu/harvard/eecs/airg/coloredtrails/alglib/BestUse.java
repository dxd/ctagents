/*
	Colored Trails
	
	Copyright (C) 2006, President and Fellows of Harvard College.  All Rights Reserved.
	
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

package edu.harvard.eecs.airg.coloredtrails.alglib;

import java.util.ArrayList;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;

/**
	Calculates the best use of chips to maximize score
	<p>
	Currently assumes that there is a single, common goal.  We
	can make this class more general by supplying a target location
	to the constructor (and this target location needn't even be
	a goal square).
	<p>
	Uses simple DFS to find best paths for now.
	<p>
	scoring function in CGDR doesn't help us if we want to be able to 
	perform client-side calculations without a lot of network traffic
	
	@author Sevan G. Ficici
*/
public class BestUse
{
	/** the game state */
	private GameStatus gs;
	/** player's pre-move status (pre- or post-exchange) */
	//private PlayerStatus ps;
	/** the scoring rule we're using */
	Scoring s;
    /** the type of goal we are seeking */
    int goal_type;
	/** (one of) the best post-move state(s) for the player */
	private PlayerStatus beststate;
	
	/** set of best paths for player (working representation) */
	private ArrayList<ArrayList<PlayerStatus>> bestpathset;
	/** set of best paths for player, represented as ArrayList<Path> */
	private ArrayList<Path> paths;
	/** the game board */	
	private Board board;
	/** the goal's location */
	private RowCol goal_loc;  // currently needed to pass to Scoring

  // FIX CONSTRUCTOR to accept a goal type
  // WE CAN IMPROVE base-case of dfs() so that it directly checks the board to
  // see if there is a goal there, rather than comparing against a pre-determined
  // goal location
  // FURTHER, if different goals are worth different amounts, then we might want
  // to traverse one goal square on the way to another goal, in which case we
  // don't want landing on a goal to be a base-case for dfs(); of course, this
  // will make search take longer, since we will then only stop once chips run out
	/**
		Constructor
		
		@param gs         the current game state
		@param ps         the player's current state
		@param s          the scoring rule we're using
    @param goal_type  the type of goal we are seeking
	*/
	public BestUse(GameStatus gs, PlayerStatus ps, Scoring s, int goal_type)
	{
            this(gs.getBoard(), ps, s, goal_type);
	}
        
        /**
         * Calls BestUse with goal type = 0
         * @param gs gamestatus
         * @param ps playerstatus
         */
        public BestUse(GameStatus gs, PlayerStatus ps){
            this(gs.getBoard(), ps, gs.getScoring(), 0);
        }
	
	public BestUse(Board b, PlayerStatus ps, Scoring s, int goal_type)
	{
		this.gs = gs;		
		//this.ps = ps;
		this.s = s;
        this.goal_type = goal_type;

		board = b;
        // assumes nearest goal of desired type is the best for you to seek
        goal_loc = board.getNearestGoalLocations(ps.getPosition(), goal_type).get(0);

		// !!! the system is not calculating player scores as player state changes !!!
		PlayerStatus psstart = new PlayerStatus(ps);                  // copy constructor
		psstart.setScore((int)Math.floor(s.score(psstart, goal_loc)));  // calculate score of current state
		bestpathset = dfs(psstart);                                   // search for best paths
		
		// get a sample best end-state
		beststate = bestpathset.get(0).get(0);
		// convert the paths to Path objects
		convertPaths();
	}
	
	
	/**
		Returns the set of best-paths for the player
	*/
	public ArrayList<Path> getPaths()
	{
		return paths;
	}
	
	
	/**
		Returns a sample best end-state
	*/
	public PlayerStatus getBestState()
	{
		return beststate;
	}


	/**
		Converts the working representation of best paths to
		a collection of Path instances; note that the working
		representation uses necessary PlayerStatus instances,
		which contain score information in addition to position
		<p>
		Note also that the working representation of best paths
		is such that each path is in reverse order, where the
		end-state is at the beginning of the ArrayList representing
		the path; this method converts these reverse-order paths
		to forward-order
	*/
	public void convertPaths()
	{
		paths = new ArrayList<Path>();
		
		for (int i=0; i<bestpathset.size(); ++i)
		{
			ArrayList<PlayerStatus> psl = bestpathset.get(i);
			Path pp = new Path();
			
			for (int j=0; j<psl.size(); ++j)
				// we use insert because working representation is reverse order
				pp.appendPathPoint(new RowCol(psl.get(j).getPosition()));
				
			paths.add(pp);
		}
	}
		
	
	/**
		Depth-first search of move options
		
		@param nomove	player's current state
		@return			a list of best paths, where each path 
						is a sequence of PlayerStats instances
						in reverse order (end-state is first
						element of list)
	*/
	private ArrayList<ArrayList<PlayerStatus>> dfs(PlayerStatus nomove)
	{
		// base cases: no more chips left or at goal?
		if (nomove.getChips().isEmpty() ||
//        board.isGoal(nomove.getPosition(), goal_type))  // would be nice to do this instead
			(nomove.getPosition().row == goal_loc.row &&
			 nomove.getPosition().col == goal_loc.col))
		{
			ArrayList<ArrayList<PlayerStatus>> newpathset = 
					new ArrayList<ArrayList<PlayerStatus>>();
			ArrayList<PlayerStatus> newpath = new ArrayList<PlayerStatus>();
			newpath.add(nomove);
			newpathset.add(newpath);
			
			return newpathset;
		}
		
		ArrayList<ArrayList<PlayerStatus>> bestpathset = 
					new ArrayList<ArrayList<PlayerStatus>>();
		ArrayList<PlayerStatus> bestpath = new ArrayList<PlayerStatus>();
		bestpath.add(nomove);
		bestpathset.add(bestpath);

		// decrease row
		if (moveable(-1, 0, nomove))
		{
			ArrayList<ArrayList<PlayerStatus>> testpathset = 
											dfs(movePlayer(nomove, -1, 0));
			bestpathset = checkMove(nomove, testpathset, bestpathset);
		}
		// increase row
		if (moveable(1, 0, nomove))
		{
			ArrayList<ArrayList<PlayerStatus>> testpathset = 
											dfs(movePlayer(nomove, 1, 0));
			bestpathset = checkMove(nomove, testpathset, bestpathset);
		}

		// decrease column
		if (moveable(0, -1, nomove))
		{
			ArrayList<ArrayList<PlayerStatus>> testpathset = 
											dfs(movePlayer(nomove, 0, -1));
			bestpathset = checkMove(nomove, testpathset, bestpathset);
		}

		// increase column
		if (moveable(0, 1, nomove))
		{
			ArrayList<ArrayList<PlayerStatus>> testpathset = 		
											dfs(movePlayer(nomove, 0, 1));
			bestpathset = checkMove(nomove, testpathset, bestpathset);
		}

		return bestpathset;
	}
	
	
	/**
		Checks that proposed move is within board boundaries and that
		the player has at least one chip of same color as target square;
		NOTE that we assume that deltas are in {-1, 0, 1}
		
		@param deltarow		proposed change in row {-1, 0, 1}
		@param deltacol		proposed change in column {-1, 0, 1}
		@param pscur		current player-state
		@return				true if proposed move is possible
	*/
	private boolean moveable(int deltarow, int deltacol, PlayerStatus pscur)
	{
		RowCol ppos = new RowCol(pscur.getPosition(), deltarow, deltacol);
		
		return ppos.row >= 0 && ppos.row < board.getRows() &&
			   ppos.col >= 0 && ppos.col < board.getCols() &&
			   pscur.getChips().getNumChips(board.getSquare(ppos).getColor()) > 0;
	}
	
	
	/**
		Creates a new PlayerStatus based on current state and
		proposed move; assumes that the proposed move is feasible
		
		@param nomove		the player's current state
		@param deltarow		proposed change in row location
		@param deltacol		proposed change in column location
		@return				a new PlayerStatus instance
	*/
	private PlayerStatus movePlayer(PlayerStatus nomove, 
									int deltarow, int deltacol)
	{
		// create clone first
		PlayerStatus psnew = new PlayerStatus(nomove);
		// change board position
		psnew.setPosition(new RowCol(psnew.getPosition(), deltarow, deltacol));
		// change chipset by subtracting a chip of target-square color
		psnew.setChips(
			ChipSet.subChipSets(psnew.getChips(), 
						new ChipSet(board.getSquare(psnew.getPosition()).getColor())));
		// calculate player's score in this new hypothetical state
		// NOTE that we do not use psnew.setScore(), as this will cause
		// observers to be notified, and we do not want this for 
		// hypothetical moves
		psnew.setScore((int)Math.floor(s.score(psnew, goal_loc)));  // SHOULD IMPROVE Scoring to not need goal_loc
                                                              // but instead work with GameStatus
		
		return psnew;
	}
	
	
	/**
		Returns the better of the paths we're testing (in 'testpathset', 
		which all have the same score) and the paths that are the best
		so far (in 'bestpathset', which all share identical scores).
		Grows the test paths, by inserting nomove state, if they are better.
		
		@param nomove		current location of player
		@param testpathset	set of paths we're examining to see if they are better
		@param bestpathset	set of paths we're comparing against
		@return				best paths known so far
	*/
	private ArrayList checkMove(PlayerStatus nomove,
	                            ArrayList<ArrayList<PlayerStatus>> testpathset, 
	                            ArrayList<ArrayList<PlayerStatus>> bestpathset)
	{
		// get path scores
		double testmovescore = testpathset.get(0).get(0).getScore();
		double bestmovescore = bestpathset.get(0).get(0).getScore();
		
		// best-so-far still best?
		if (testmovescore < bestmovescore)
			return bestpathset;
		
		// test paths are at least as good as best-so-far
		// append state 'nomove' to end of each test path
		for (int i=0; i<testpathset.size(); ++i)
			testpathset.get(i).add(nomove);
		
		// if test paths are better, then they are new best-so-far
		if (testmovescore > bestmovescore)
			bestpathset = testpathset;
		// if test paths are equally good, then add them to best-so-far
		else
			bestpathset.addAll(testpathset);
			
		return bestpathset;
	}
	
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		
		for (int i=0; i<bestpathset.size(); ++i)
		{
			sb.append("Path " + i + ":\n");
			
			ArrayList path = bestpathset.get(i);
			for (int j=0; j < path.size(); ++j)
				sb.append(path.get(j));
			sb.append("\n");
		}
		
		return sb.toString();
	}
}