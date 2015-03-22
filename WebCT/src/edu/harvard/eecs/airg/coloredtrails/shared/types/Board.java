/*
	Colored Trails
	
	Copyright (C) 2006-2008, President and Fellows of Harvard College.  All Rights Reserved.
	
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

package edu.harvard.eecs.airg.coloredtrails.shared.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import edu.harvard.eecs.airg.coloredtrails.shared.Utility;

/**
<b>Description</b>
Represents a rectangular game board.  Board squares are addressed with row-column coordinates (using RowCol object). 
Each board square has an associated color and a flag indicating whether it is a goal square or not.  This code does 
not associate goal squares with players or player roles.

<p>

<b>Issues</b>
A board instance is only aware of the colors actually present on the board; it does not know if the board colors 
are actually a subset of a larger color palette used by the game.  The GamePalette class is now used
to represent the set of colors defined for the game; the Board class is no longer queried for this
information.
<p>

<b>Future Development</b>
May want to add flags that indicate whether a square is an obstacle or not; more generally, each square can hold 
a list of properties that are interpreted by higher-level components of the system (e.g., the configuration class)
to determine behavior.

<p>

<b>Observers</b>
GameStatus, [COMPLETE: others? StatusSender?]

<p>

<b>Notifications</b>
<table width="100%" cellpadding=10 bgcolor=silver>
    <tr><td>Method</td><td>Message Sent</td></tr>
    <tr><td>setSquares()</td><td>BOARD_CHANGED</td></tr>
    <tr><td>setSquareColor()</td><td>BOARD_CHANGED</td></tr>
    <tr><td>setSquareGoal()</td><td>BOARD_CHANGED</td></tr>
</table>

<p>

    @author Paul Heymann (ct3@heymann.be)
    @author Sevan G. Ficici
*/
public class Board extends CTStateContainer
                   implements Serializable, Cloneable
{
    /**
      Constructor

      @param rows   the number of rows in this board
      @param cols   the number of columns in this board
    */
    public Board(int rows, int cols)
    {
    	put("Rows", new Integer(rows));
    	put("Cols", new Integer(cols));
    
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
            	put("Square." + r + "-" + c, new Square("snow"));
    }
    
    
    public Board(Board board)
    {
    	super(board);
    }
    
    public Object clone()
    {
    	return new Board(this);
    }
	
	
    /**
      Set the board state to a two-dimensional array of squares.

      @param squares The squares that make up the board.
    */
    public void setSquares(Square[][] squares)
    {
    	removeSquares();
    	
    	put("Rows", new Integer(squares.length));
    	// assuming all rows same length
    	put("Cols", new Integer(squares[0].length));
    	
    	for (int r=0; r<squares.length; ++r)
    		for (int c=0; c<squares[r].length; ++c)
            	put("Square." + r + "-" + c, squares[r][c]);
    	
        setChanged();
        notifyObservers("BOARD_CHANGED");
    }
    
    
    // may want to change getFeatures so list is not backed by hashtable
    private void removeSquares()
    {
    	Set<String> f = getFeatures();
    	java.util.Iterator<String> fi = f.iterator();
    	
    	while (fi.hasNext())
    	{
    		String k = fi.next();
    		if (k.startsWith("Square."))
    			fi.remove();
    	}
    	
//    	for (String k : f)
  //  		if (k.startsWith("Square."))
    //			f.remove(k);
    }


    /*************************/
    /***** BOARD METRICS *****/
    /*************************/
    /**
      Get the number of rows in the board.

      @return The number of rows in the board.
    */
    public int getRows()
    {
        return (Integer)get("Rows");
    }

    /**
      Get the number of columns in the board.

      @return The number of columns in the board.
    */
    public int getCols()
    {
    	return (Integer)get("Cols");
//        return (getRows() < 1) ? 0 : squares[0].length;
    }


    /*************************/
    /***** BOARD COLORS ******/
    /*************************/


    /**
     * Get the square in the position given
     * @param pos Position
     * @return Square in the position
     */
    public Square getSquare(RowCol pos)
    {
    	return (Square)get("Square." + pos.row + "-" + pos.col);
    }
    
    /**
    	Returns the square at the specified position
    */
    public Square getSquare(int row, int col)
    {
    	return (Square)get("Square." + row + "-" + col);
    }

    /**
    	Get all of the colors present on the board.
    	
	    @return A list of all of the colors on the board.
    */
    public Set<String> getColors()
    {
    	HashSet<String> colors = new HashSet<String>();
		
		for (String k : getFeatures())
			if (k.startsWith("Square."))
				colors.add(((Square)get(k)).getColor());
				
		return colors;
    }

    /*************************/
    /****** BOARD GOALS ******/
    /*************************/
    /**
      Set whether there is a goal at a particular position.

      @param pos        board location
      @param has_goal   true = place a goal, false = remove the goal
      @param goal_type  the type of the goal
    */
    public void setGoal(RowCol pos, boolean has_goal, int goal_type)
    {
    	if (has_goal)
    		((Square)getSquare(pos)).setGoal(goal_type, pos);
    	else
    		((Square)getSquare(pos)).removeGoals(goal_type);
    }

    /**
      Set whether a goal of default type exists on given location

      @param pos        the board location
      @param has_goal   true = place goal, false = remove goal
    */
    public void setGoal(RowCol pos, boolean has_goal)
    {
        setGoal(pos, has_goal, Goal.DEFAULT_GOAL_TYPE);
    }

    /**
     * Set whether a goal exists
     * @param g Goal
     * @param has_goal true = place goal, false = remove goal
     */
    public void setGoal(Goal g, boolean has_goal)
    {
    	setGoal(g.getLocation(), has_goal, g.getType());
    }
    
    /**
     * Moves specified goal from its current square to a new square (SGF)
     *
     * @param g			the goal we're moving
     * @param newpos	the goal's new location
     */
    public void moveGoal(Goal g, RowCol newpos)
    {
    	RowCol prepos = g.getLocation();
    	((Square)getSquare(prepos)).removeGoal(g);
    	g.setLocation(newpos);
    	((Square)getSquare(newpos)).addGoal(g);
		
        setChanged();
        notifyObservers("BOARD_CHANGED");
    }


    /**
     * Move specified goal from its current square to a new square
     * @param id		the ID of the goal we're moving
     * @param newpos	the goal's new location
     * @author SGF
     */
    public void moveGoal(String id, RowCol newpos)
    {
    	for (Goal g : getGoals())
    		if (g.getID().equals(id))
    		{
    			moveGoal(g, newpos);
    			return;
    		}
    }


    /**
     * Move first goal we find of specified type to a new square
     * @param type		the type of the goal we're looking for
     * @param newpos	the goal's new location
     * @author SGF
     */
    public void moveGoal(int type, RowCol newpos)
    {
    	for (Goal g : getGoals())
    		if (g.getType() == type)
    		{
    			moveGoal(g, newpos);
    			return;
    		}
    }

    /**
     * Remove a given Goal object from the board and notify
     * the observers.
     * @param Goal goal
     */
    public void removeGoal(Goal goal)
    {
        RowCol goalPos = goal.getLocation();
    	((Square)getSquare(goalPos)).removeGoal(goal);

        setChanged();
        notifyObservers("BOARD_CHANGED");
    }

    /**
      Returns list of all square locations that contain a goal
      @author SGF
    */
    public ArrayList<RowCol> getGoalLocations()
    {
      ArrayList<RowCol> glist = new ArrayList<RowCol>();

      for (int i=0; i<getRows(); ++i)
        for (int j=0; j<getCols(); ++j)
          if (getSquare(i, j).hasGoal())
            glist.add(new RowCol(i, j));

      return glist;
    }

    /**
    	Returns a list of all square locations that contain a goal
	    of specified type 'goal_type'
    	@param goal_type Goal type to be returned
	    @return Square locations that contain a goal of specified type
    	@author SGF
	*/
    public ArrayList<RowCol> getGoalLocations(int goal_type)
    {
      ArrayList<RowCol> glist = new ArrayList<RowCol>();
      //Add all specific goals
      for (int i=0; i<getRows(); ++i)
        for (int j=0; j<getCols(); ++j)
          if (getSquare(i, j).hasGoal(goal_type))
            glist.add(new RowCol(i, j));
      //Add all default goals
      for (int i=0; i<getRows(); ++i)
        for (int j=0; j<getCols(); ++j)
          if (getSquare(i, j).hasGoal(Goal.DEFAULT_GOAL_TYPE) && !glist.contains(new RowCol(i, j)))
            glist.add(new RowCol(i, j));

      return glist;
    }


    /**
     * Returns list of all goals on the board
     */
    public Set<Goal> getGoals()
    {
    	Set<Goal> goals = new HashSet<Goal>();

    	for (int r=0; r<getRows(); ++r)
    		for (int c=0; c<getCols(); ++c)
    			goals.addAll(getSquare(r, c).getAllGoals());

    	return goals;
    }

    // CAN BE MADE MORE EFFICIENT!
    // ALSO, Square class not so efficient?
    // the automated movement is quite slow for some reason
    /**
     * Gets the nearest goal positions

     @deprecated needs further info or should be deleted (BestUse uses it)
     */
    public ArrayList<RowCol> getNearestGoalLocations(RowCol pos, int goal_type)
    {
      int mind = Integer.MAX_VALUE;
      ArrayList<RowCol> nearest = new ArrayList<RowCol>();

      ArrayList<RowCol> goal_list = getGoalLocations(goal_type);
      for (RowCol rc : goal_list)
        if (pos.dist(rc) < mind)
        {
          mind = pos.dist(rc);
          nearest.clear();
          nearest.add(rc);
        }
        else if (pos.dist(rc) == mind)
          nearest.add(rc);

      return nearest;
    }




    public String toString()
    {
        String rep = "";
        for (int i = 0; i < getRows(); i++)
        {
            for (int j = 0; j < getCols(); j++)
            {
                rep = rep.concat(getSquare(i, j).toString());
                rep = rep.concat(" ");
            }
            rep = rep.concat("\n");
        }
        return rep;
    }
}