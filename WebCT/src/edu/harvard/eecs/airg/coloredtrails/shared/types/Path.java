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

package edu.harvard.eecs.airg.coloredtrails.shared.types;

import java.util.Vector;
import java.util.ArrayList;
import java.util.LinkedList;

import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;

/**
<b>Description</b>

A path which consists of a series of (row,col) coordinates on the board in sequence.

<p>

<b>Original Summary</b>
 * A path which consists of a series of two or more (row,col) coordinates
 * on the board in sequence.
 *
 * @author Paul Heymann (ct3@heymann.be)
	@author Sevan G. Ficici (new implementation; class-level review and comments)
 */
public class Path implements Comparable{
    /** the path data */
    private ArrayList<RowCol> pathpoints = new ArrayList<RowCol>();
    /** the board that the path is on*/
    private Board onBoard = null;
    /** the scoring function of the game*/
    private Scoring scoring = null;

    /**
      Constructor specifying the start point only
    */
    public Path(RowCol start)
        {
            pathpoints.add(start);
        }

    public Path()
    {
        
    }

    public Path(Vector<RowCol> path)
    {
        pathpoints.addAll(path);
    }

    public Path(RowCol[] pp)
    {
        for (int i=0; i<pp.length; ++i)
          pathpoints.add(pp[i]);
    }
    
    /**
     * Creates a path on a described board with a described scoring
     <p>
     (ilke) This is a temporary solution to get path weights, after scoring
     is integrated in the GameStatus, we'll no longer need this.
     
     * @param b Board that the path is on
     * @param s The scoring function
     */
    public Path(Board b, Scoring s)
    {
    	onBoard = b;
    	scoring = s;
    }

    /**
      Copy constructor
    */
    public Path(Path p)
    {
        LinkedList<RowCol> points = p.getPoints();
        
        for(RowCol rc:points)
          pathpoints.add(new RowCol(rc));
        
        onBoard = p.onBoard;
        scoring = p.scoring;
    }

    /**
     * Add a new point to the end of the sequence of (row,col) positions
     * which make up this path.
     * 
     *
     * @param newpoint The new position to be added.
     * @exception MalformedPathException when the new point and the last point of the path
     * are not neighbors
     */
    public void addPathPoint(RowCol newpoint)
    {
    	try{
    		if(!pathpoints.isEmpty() && !newpoint.areNeighbors(newpoint, pathpoints.get(pathpoints.size()-1))) {
    			Exception e = new Exception("MalformedPathException");
    			throw e;
    		}
    		pathpoints.add(newpoint);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }


	
	/**
		Returns number of points in the path
	*/
	public int getNumPoints()
	{
		return pathpoints.size();
	}
	
	
	/**
		Returns the n-th point in the path, where 0 is the first point (SGF)
	*/
	public RowCol getPoint(int n)
	{
		return pathpoints.get(n);
	}
	

    /**
        Returns first point in the path
    */
    public RowCol getStartPoint()
    {
        return pathpoints.get(0);
    }
    
    
    /**
        Returns last point in the path
    */
    public RowCol getEndPoint()
    {
        return pathpoints.get(pathpoints.size()-1);
    }
    
    
 
    
    /**
        Returns the points in the path as an array of RowCol instances
        @deprecated
    */
    //public RowCol[] getPoints()
    //{
     //   return pathpoints.toArray(new RowCol[0]);
    //}
    
    /**
     * Returns the points in the path as a list of RowCol instances
     */
    public LinkedList getPoints()
    {
		LinkedList list = new LinkedList();
		
		for(RowCol rc:pathpoints) {
			list.add(rc);
		}
    	return list;
    }
    
    

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append("Path:");
        
        for (int i=0; i<pathpoints.size(); ++i)
        {
          RowCol rc = pathpoints.get(i);
          sb.append(" [" + rc.row + "][" + rc.col + "]");
        }
        
        sb.append("\n");
        
        return sb.toString();
    }
    
    /**
     * Insert the point in the beginning of the path
     * @param rc the point to be inserted
     * @author ilke
     */
    public void appendPathPoint(RowCol rc) {
        pathpoints.add(0, rc);
    }
    
    /**
     * Gets the number of chips required to take the path
     * @param board The board that the path is on
     * @return Number of chips required
     * @author ilke
     */
    public ChipSet getRequiredChips(Board board) {
    	ChipSet cs = new ChipSet();

    	for(RowCol rc:pathpoints) {
    		if(!rc.equals(pathpoints.get(0)))
    		cs.add(board.getSquare(rc).getColor(), 1);
    	}
    	return cs;
    }
    
    /**
     * Check if the path equals the object
     * @param o Object to compare with
     * @return true if they're equal, false otherwise
     */
    public boolean equals (Path o) {
    	Path path = o;
    	if(path.getNumPoints() != getNumPoints()) {
    		return false;
    	}
    	for(int i=0; i<path.getNumPoints(); i++) {
    		if(!path.getPoint(i).equals(getPoint(i)))
    			return false;
    	}
    	return true;
    }
    
    public int hashCode() {
    	return toString().hashCode();
    }
    
    /**
     * Gets the weight of the path with a described scoring and board
     * @return The weight of the path
     * @author ilke
     */
    public int getWeight() {
    	int weight = 0;
    	for(RowCol rc:pathpoints) {
    		weight += scoring.getColorWeight(onBoard.getSquare(rc).getColor());
    	}
    	return weight;
    }
    
    /**
     * Compare the path with an object
     * Added for the priority queue of the path finder.
     * @param o Object to be compared with
     * @return 0 if they're equal, 1 if the argument is lesser and -1 if the argument is greater
     * @author ilke
     */
    public int compareTo(Object o) {
		Path p = (Path) o;
		int weight2 = p.getWeight(), weight1 = getWeight();
		
		if(weight1 > weight2) {
			return 1;
		}
		else if (weight1 < weight2) {
			return -1;
		}
		else {
			return 0;
		}
    }

    /**
     * Check if the path contains the position
     * @param rc Position to be checked
     * @return true if Path contains the position, false otherwise
     * @author ilke
     */
	public boolean contains(RowCol rc) {
		for(RowCol rowcol:pathpoints) {
			if(rowcol.equals(rc)) {
				return true;
			}
		}
		return false;
	}

}

