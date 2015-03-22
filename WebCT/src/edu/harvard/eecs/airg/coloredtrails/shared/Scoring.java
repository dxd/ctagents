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

package edu.harvard.eecs.airg.coloredtrails.shared;

import java.io.Serializable;
import java.lang.Math;
import java.util.HashMap;

import edu.harvard.eecs.airg.coloredtrails.shared.types.*;

/**
	Represents the rule used to score a player based on the game state
	
	@author Sevan G. Ficici
*/
public class Scoring implements Serializable
{
  /** points earned for reaching the goal */
	public int goalweight;
  /** penalty for each unit distance away from goal (< 0) */
	public int distweight;
  /** points earned for each chip in possession */
	public int chipweight;
        
        //This is the default score
        double basescore = 0.0;
	
  /** penalty for each offer rejected (< 0) ~ sohan*/
	public int commweight = 0;

  /** weights for each color */  // NEED TO CHANGE THIS and clarify semantics (SGF)
	private HashMap<String,String> colorweights = null;
  
  
	/**
		Constructor.
		
		@param goalweight	value of being at the goal
		@param distweight	cost of unit distance from goal (<= 0)
		@param chipweight	value of chip still in possession
	*/
	public Scoring(int goalweight, int distweight, int chipweight)
	{
		this.goalweight = goalweight;
		this.distweight = distweight;
		this.chipweight = chipweight;
	}
	
        /**
         * 
         * @param goalweight    value of being at the goal
         * @param distweight    cost of unit distance from goal (<= 0)
         * @param chipweight    value of chip still in possession
         * @param defaultscore  base score (equals zero unless specified here)
         */
        public Scoring(int goalweight, int distweight, int chipweight, double basescore)
        {
            this.goalweight = goalweight;
            this.distweight = distweight;
            this.chipweight = chipweight;
            this.basescore = basescore;
	}

	
        /**
         * 
         * @param goalweight    value of being at the goal
         * @param distweight    cost of unit distance from goal (<= 0)
         * @param chipweight    value of chip still in possession
         * @param defaultscore  base score (equals zero unless specified here)
         * @param commweight    value of each rejected offer (equals zero unless specified here)
         */
        public Scoring(int goalweight, int distweight, int chipweight, double basescore, int commweight)
        {
            this.goalweight = goalweight;
            this.distweight = distweight;
            this.chipweight = chipweight;
            this.basescore = basescore;
        		this.commweight = commweight;
	}

  /**
    Constructor
    
		@param goalweight	value of being at the goal
		@param distweight	cost of unit distance from goal (<= 0)
		@param chipweight	value of chip still in possession
    @param colorweights weights for each color
	*/    
	public Scoring(int goalweight, int distweight, int chipweight, HashMap<String,String> colorweights)
	{
		this.goalweight = goalweight;
		this.distweight = distweight;
		this.chipweight = chipweight;
		this.colorweights = colorweights;
	}
	
  /**
    Copy constructor
  */
	public Scoring(Scoring scoring)
  {
		this.goalweight = scoring.goalweight;
		this.distweight = scoring.distweight;
		this.chipweight = scoring.chipweight;
		this.commweight = scoring.commweight;
		this.colorweights = scoring.colorweights;
	}
  
  
	/**
		Calculates the score of an agent with the specified state.
		NOTE that we need to generalize this to accept an entire game state;
		we would then add an argument to indicate which player we are 
		interested to score.
    NOTE also that we are applying color weights for chips that are
    in posession, but are not using weights in calculating the penalty
    for not reaching the goal; of course, given a distance N from the
    goal, there may be many length-N paths, and each such path may
    traverse a different set of colors. Thus, there may not be a 
    unique set of colors to weight in calculating the penalty; we 
    would need to decide how we'd apply the weights to calculate
    the penalty.
		
		@param ps     an agent's state
		@param gpos		the target goal's location
		@return       the agent's score
	*/
	public double score(PlayerStatus ps, RowCol gpos)
	{
		RowCol ppos = ps.getPosition();
		
		int delta_r = Math.abs(ppos.row - gpos.row);
		int delta_c = Math.abs(ppos.col - gpos.col);
		int manhattan = delta_r + delta_c;
		
		double score = basescore;
		
		if (manhattan == 0)
			score += goalweight;
		else
			score += distweight * manhattan;
		
        score += getChipSetWeight(ps.getChips());
			
		return score;
	}
	
  /*compute scoring including ~ sohan*/
	public double score(PlayerStatus ps, RowCol gpos, int comms){
	   return score(ps, gpos) + ((double)comms * commweight);
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer("GoalWeight: " + goalweight +
									       "  DistWeight: " + distweight + 
									       "  ChipWeight: " + chipweight + 
									       "  CommWeight: " + commweight);
		return sb.toString();
	}
	
	/**
	 * Sets the HashMap of color weights to the argument
	 * @param hm HashMap of the colors' weights
	 * @author ilke
	 */
	public void setColorWeights(HashMap<String,String> hm)
	{
		colorweights = hm;
	}
  
  
  /**
    Sets the weight of the specified color
    
    @param color  the color whose weight we are setting
    @param weight the weight of the color
  */
  public void setColorWeight(String color, int weight)
  {
    if (colorweights == null)
      colorweights = new HashMap<String,String>();
      
    colorweights.put(color, Integer.toString(weight));
  }
  
	
	/**
	 * Gets the weight of a specific color
	 * @param color Color
	 * @return Weight of the color
	 * @author ilke
	 */
    public int getColorWeight(String color) {
        if (colorweights == null)
            return chipweight;
        else
            return Integer.parseInt((String)(colorweights.get(color)));
    }
	
	public int getChipSetWeight(ChipSet cs) {
        int sum = 0;
		for(String color : cs.getColors())
			sum += cs.getNumChips(color) * getColorWeight(color);
    
		return sum;
	}

  public void setCommWeight(int commweight) {
      this.commweight = commweight;
  }

  public int getCommWeight() {
      return commweight;
  }
}