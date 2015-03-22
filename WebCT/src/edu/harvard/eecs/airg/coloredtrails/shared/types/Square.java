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
import java.util.Collection;
import java.util.Set;
import java.util.ArrayList;
import java.util.Hashtable;

/**
    Represents a board square
    
    <b>Description</b>
    Each instance of this class represents a square on the game 
    board (see types.Board).
    
	<p>
	<b>Future Development</b>
	<ul>
	<li>If we want to change square colors as game progresses, then we need to 
	make this class observable; right now, we must update the board, which is
	observed, rather than individual squares.
	Color change could have associated change in functionality, 
	for example, a square turns black when it becomes an obstacle.</li>
	<li>If we are interested in other board geometries, then we need to generalize the 
	board square to a board site.</li>
	</ul>
    
    @author Sevan G. Ficici (new API and implementation, modifications for
                             for partial visibility)
    @author Paul Heymann (ct3@heymann.be) (original API and implementation)
 */
public class Square extends CTStateContainer 
				    implements Cloneable, Serializable
{
    /** holds properties of this square */
//    private Hashtable<String, Object> properties = new Hashtable<String, Object>();
    
    /******************************************************/
    /******************** CONSTRUCTORS ********************/
    /******************************************************/ 

    public Square()
    {
    	super("Square", "Server");  // server's encoding of the square
        put("color", "?");  // IS THIS NEEDED -- used by any UI code?
    }
    
    public Square(String color)
    {
    	super("Square", "Server");  // server's encoding of the square
        put("color", color);
    }

    public Square(String color, boolean has_goal)
    {
    	super("Square", "Server");  // server's encoding of the square
        put("color", color);
        setGoal(has_goal);
    }
    
    public Square(String color, Goal g)
    {
    	super("Square", "Server");  // server's encoding of the square
		put("color", color);
		addGoal(g);
    }
    
    public Square(Square s)
    {
    	super(s);
    }
    
    public Object clone()
    {
    	return new Square(this);
    }
    
    
    /**
        Returns the color of this square
    */
    public String getColor()
    {
        return (String)get("color");
    }
    
    
	/**
	    set the color of this square
	*/
	public void setColor(String col)
	{
	    put("color", col);
	}
    

    /**
      Adds specified goal to the square
    */
    public void addGoal(Goal g)
    {
      put("Goal." + g.getID(), g);
    }
    
    
    /**
      Returns first goal of specified type that is found
    */
    public Goal getGoal(int type)
    {
    	for (String k : getFeatures())
    		if (k.startsWith("Goal."))
    		{
    			Goal g = (Goal)get(k);
    			if (g.getType() == type)
    				return g;
    		}
    	
    	return null;
    }
    
    
    /**
     * Returns goal specified by the ID
     */
    public Goal getGoal(String id)
    {
    	return (Goal)get("Goal." + id);
    }
    
    
    /**
     * Returns all goals in this square, regardless of type
     * @return list of goals
     */
    public ArrayList<Goal> getAllGoals()
    {
    	ArrayList<Goal> goals = new ArrayList<Goal>();

    	for (String k : getFeatures())
    		if (k.startsWith("Goal."))
    			goals.add((Goal)get(k));
		
		return goals;
    }
    
    
    /**
      Removes all goals of specified type that are found
    */
    public void removeGoals(int type)
    {
        for (String k : getFeatures())
    		if (k.startsWith("Goal.") && ((Goal)get(k)).getType() == type)
    			remove(k);
    }

    
    /**
      Removes specified (by ID) goal from the square
    */
    public void removeGoal(Goal g)
    {
      removeGoal(g.getID());
    }
    
    
    /**
      Removes specified goal from the square
    */
    public void removeGoal(String id)
    {
      remove("Goal." + id);
    }
    
    
    /**
     * Remove all goals in the square
     */
    public void removeAllGoals()
    {
        for (String k : getFeatures())
    		if (k.startsWith("Goal."))
    			remove(k);
    }


    /**
        Returns 'true' if a goal of type 'type' is present
    */
    public boolean hasGoal(int type)
    {
        for (String k : getFeatures())
    		if (k.startsWith("Goal.") && ((Goal)get(k)).getType() == type)
				return true;
        
        return false;
    }
    
    
    /**
      Returns true if goal with specified ID is present
    */
    public boolean hasGoal(String id)
    {
      return getGoal(id) != null;
    }
    
    
    /**
        Returns 'true' if any goal is present
    */
    public boolean hasGoal()
    {
        for (String k : getFeatures())
    		if (k.startsWith("Goal."))
				return true;
        
        return false;
    }
	
	
    // SHOULD RETURN Set instead?
    /**
      Returns all of the goals in this square
    */
    public Collection<Goal> getGoals()
    {
    	return getAllGoals();
    }

 
    /**
      Adds or removes a default-type goal
      
      @param b    true == add a default goal; false == remove default goals
    */
    public void setGoal(boolean b)
    {
    	setGoal(Goal.DEFAULT_GOAL_TYPE, b);
    }
    
    /**
     * if b is 'true', adds the goal with the type 'type'
     * if b is 'false', removes the goal with the type 'type'
     * @param type Type of the goal
     * @param b add or remove
     */
    public void setGoal(int type, boolean b)
    {
      if (b)
      {
    	  if (!hasGoal(type))
    		  addGoal(new Goal(type));
      }
      else
    	  removeGoals(type);
    }
    
    /**
     * Set the goal in the position specified. If that type exists, the method
     * removes the goal, If it doesn't, then adds one type of that goal to the position
     * @param type Type of the goal.
     * @param pos Position to insert
     */
    public void setGoal(int type, RowCol pos)
    {
    	if (hasGoal(type))
    		removeGoals(type);
    	
    	addGoal(new Goal(type, pos));
    }

    /**
     * if b is 'true', adds the goal with the id 'id' and type 'type'
     * if b is 'false', removes the goal with the type 'type'
     * @param type Type of the goal
     * @param id Id of the goal
     * @param b add or remove
     */
    public void setGoal(String id, int type, boolean b)
    {
    	if (b)
    	{
    		if (!hasGoal(id))
    			addGoal(new Goal(id, type));
    	}
    	else
    		removeGoal(id);
    }
    
    
    public String toString()
    {
        if (hasGoal())
        {
            return "*" + getColor().charAt(0) + "*";
        }
        else
        {
            return "[" + getColor().charAt(0) + "]";
        }
    }
}