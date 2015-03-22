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

package edu.harvard.eecs.airg.coloredtrails.alglib;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;

import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Board;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Path;
import edu.harvard.eecs.airg.coloredtrails.shared.types.RowCol;

/**
 * Returns the shortest paths between a start and an end point while taking the weights
 * of the square colors into account. The path with the smallest weight is the best. The
 * algorithm however, does not terminate after getting the best one possible, but continues
 * to get other options with same or more weights.
 *<p>
 * !!! Will be modified to work more efficient !!!
 * @author ilke
 *
 */
public class ShortestPaths {
	/**
	 * Returns an ArrayList of shortest paths between two points, according to 
	 * the weights of the squares
	 * @param start Starting point
	 * @param end Ending point
	 * @param board Board that the path is on
	 * @param scoring Scoring function of the game
	 * @param maxPathAmount Max no. of paths returned. Set to Integer.MAX_VALUE for all paths
	 * @return An ArrayList of shortest paths
	 * @author ilke
	 */
	public static ArrayList<Path> getShortestPaths(RowCol start, RowCol end, Board board, Scoring scoring, int maxPathAmount)
	{
		ArrayList<Path> paths = new ArrayList<Path>();
		PriorityQueue<Path> queue = new PriorityQueue<Path>();
		
		Path path = new Path(board, scoring);
		path.addPathPoint(start);
		queue.offer(path);
		Path p;
		LinkedHashSet<RowCol> neighbors;
		int counter = 0;
		
		while(!queue.isEmpty() && counter<maxPathAmount) {
			p = queue.poll();
			
			if(p.getEndPoint().equals(end)) {
				paths.add(p);
				counter++;
			}
			else
			{
				neighbors = (LinkedHashSet<RowCol>) p.getEndPoint().getNeighbors(board);
				
				for(RowCol rc:neighbors) {
					if(!p.contains(rc)) {
						
						Path newpath = new Path(p);
						newpath.addPathPoint(rc);			
						queue.offer(newpath);			
					}
				}
			}
		}
		return paths;
	}
	
	/**
	 * Returns an array list of first 10 paths from the starting point to the first goal on the board
	 * Used in PhaseLoopPlayer class to simplify the usage of the getShortestPaths method
	 * @param start Starting position
	 * @param board Board of the game
	 * @param scoring Scoring of the game
	 * @return 10 best paths from the starting point to the first goal
	 */
	public static ArrayList<Path> getShortestPathsToFirstGoal(RowCol start, Board board, Scoring scoring) {
		return getShortestPaths(start, board.getGoalLocations().get(0), board, scoring, 10);
	}
	
	/**
	 * Returns all paths (w/o backtracking - circling) to the first goal
	 * Paths are ordered according to their weights
	 * @param start Starting position
	 * @param board Board of the game
	 * @param scoring Scoring of the game
	 * @return All paths from the starting point to the first goal
	 */
	public static ArrayList<Path> getPathsToFirstGoal(RowCol start, Board board, Scoring scoring) {
		return getShortestPaths(start, board.getGoalLocations().get(0), board, scoring, Integer.MAX_VALUE);
	}
}
