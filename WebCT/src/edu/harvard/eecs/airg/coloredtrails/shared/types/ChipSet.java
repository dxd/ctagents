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

import edu.harvard.eecs.airg.coloredtrails.shared.Utility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
<b>Description</b>
Represents a set of chips of different colors with <color, number_of_chips> pairs in a 
hashtable.  Methods exist to combine two sets of chips (e.g., addChipSets() and subChipSets()).  
There are no methods to otherwise modify chip counts; for example, no code to specify a color 
and a new count or an increment/decrement.
<p>
Note that the combining methods like 'addChipSets()' will work even if the two chip sets
being combined do not have all of their colors in common; if a chipset lacks a certain color
in its hashtable, then the number of chips of that color is given to be zero.
<p>
[could it be that chipsets are not even initialized with chips of a certain color if that
color is not on the board? this in contrast to chipset having such colors registered, but
they are simply not being displayed; the client.ui.ChipSetDisplayPanel doesn't appear to have
a problem, though.]
<p>

 *
 * @author Paul Heymann (ct3@heymann.be)
   @author Sevan G. Ficici (modifications for partial visibility)
 */
public class ChipSet extends CTStateContainer
                     implements Serializable, Cloneable
{
    public ChipSet()
    {
    	super("ChipSet", "Server");  // server's encoding of the chip set
    }
    
    /**
     * @deprecated Used in the configuration & server side, agent designers don't need this
     */
    public ChipSet(String color)
    {
        this(color, 1);
    }

    /**
     * @deprecated Used in the configuration & server side, agent designers don't need this
     */
    public ChipSet(String color, int num)
    {
    	this();
    	put("Color." + color, new Integer(num));
    }
    
    public ChipSet(ChipSet cs)
    {
    	super(cs);
    }
    
    public Object clone()
    {
    	return new ChipSet(this);
    }
    
    
    /**
      * Get a Set of all colors in the ChipSet
      * @return A set of all the colors.
      * @author PGH
        @rewritten SGF
      */
    public Set<String> getColors()
    {
    	HashSet<String> hs = new HashSet<String>();
    	
    	for (String f : getFeatures())
    		if (f.startsWith("Color."))
    		{
    			String thecolor = f.substring(new String("Color.").length());
    			hs.add(thecolor);
    		}
    			
    	return hs;
    }
    
    /**
     * Get the number of chips of a given color.
     *
     * @param color The color to count the chips of.
     * @return The number of chips of the color.
     */
    public int getNumChips(String color)
    {
    	Integer n = (Integer)get("Color." + color);
    	
    	if (n == null)
    		return 0;
    	else
    		return n;
    }
	
    /**
        Returns the total number of chips in this chipset (SGF)
        Modified by PGH, SGF
    */
    public int getNumChips()
    {
    	int n = 0;
    	
    	for (String c : getColors())
    		n += getNumChips(c);
		
        return n;
    }
	
    /**
    	Sets the number of chips of specified color to specified number
    */
    public void setNumChips(String color, int num)
    {
    	put("Color." + color, new Integer(num));
    }
    
    // NOTE: this method overloads set of superclass
    // should not be a problem, but would be nice if we
    // can eliminated the overloading
    /**
     * Set the number of chips of color to num
     * @param color The color to be set
     * @param num The number of chips.
     * @author PGH
     */
    public void set( String color, int num )
    {
    	setNumChips(color, num);
    }
	
    /**
        Returns 'true' if the chipset has no chips (SGF)
    */
    public boolean isEmpty()
    {
        return getNumChips() == 0;
    }
	
    public String toString()
    {
        String str = "{ chips ";
        
        for (String c : getColors())
        	str += " '" + c + "':" + getNumChips(c);

        str += " }";

        return str;
    }

    /**
     * Determine whether a chipset is completely contained within this
     * one.  In other words, for every chip in the contained chipset, is
     * there a chip of the same color in the container chipset?
     * Throws a NegativeChipSetException when one of the chipsets has negative
     * amount of chips
     *
     * @param contained The possibly contained chipset.
     * @return Whether this chipset contains the contained chipset.
     */
    public boolean contains(ChipSet contained)
    {
    	try
    	{
    		// check this chipset for negative values
	    	for (String color : getColors())
	    		if (getNumChips(color) < 0)
	    		   	throw new Exception("NegativeChipSetException");
			
			// check other chipset of negative values
	    	for (String color : contained.getColors())
	    		if (contained.getNumChips(color) < 0)
	    			throw new Exception("NegativeChipSetException");
			
			// check for containment
	        ChipSet subend = subChipSets(this, contained);
	        for (String color : subend.getColors())
	            if (subend.getNumChips(color) < 0)
	                return false;

	        return true;
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
		return false;
    }

    
	/**
	 * Add the number of chips of color to num.
	 * If the color of the chips does not exist 
	 * in the ChipSet then it will be added.
	 * @param color The color to be set
	 * @param num The number of chips.
     * @author PGH
       @author SGF modified code
	 */
    public void add( String color, int num )
    {
    	setNumChips(color, getNumChips(color) + num);
    }
    
    /**
     * Returns whether the ChipSet is equal to the Object
     * @param  o Object to be compared with
     * @return true if equal, false otherwise
     */
    public boolean equals (Object o)
    {
    	ChipSet cs = (ChipSet) o;
    	for(String color : cs.getColors()) {
    		if(cs.getNumChips(color) != getNumChips(color)) {
    			return false;
            }
        }
        for(String color : getColors()) {
    		if(getNumChips(color) != cs.getNumChips(color)) {
    			return false;
            }
        }
    	return true;
    }
    
    /**
     * Gets the ChipSet that has the chips the first ChipSet lacks
     * So that chipset can be requested
     * @param cs ChipSet that the caller is compared with
     * @return ChipSet of missing chips
     * @author ilke
     */
    public ChipSet getMissingChips(ChipSet cs)
    {
    	ChipSet missing = new ChipSet();
    	
    	for(String color : cs.getColors())
    		if(this.getNumChips(color) < cs.getNumChips(color))
    			missing.add(color, cs.getNumChips(color) - this.getNumChips(color));
    	
    	return missing;
    }
    
    /**
     * Gets the ChipSet that has the chips the first ChipSet has extra
     * @param cs ChipSet that the caller is compared with
     * @return ChipSet of extra chips
     * @author ilke
     */
    public ChipSet getExtraChips(ChipSet cs)
    {
    	ChipSet extra = new ChipSet();
    	
    	for(String color : cs.getColors())
    		if(this.getNumChips(color) > cs.getNumChips(color))
    			extra.add(color, this.getNumChips(color) - cs.getNumChips(color));
		
    	return extra;
    }
    
    /**
     * Get the negation of a chipset, e.g., the chipset with all
     * chip totals multiplied by -1.
     *
     * @param cs The chipset to negate.
     * @return A new chipset which is the negation of the given chipset.
     */
    public static ChipSet getNegation(ChipSet cs)
    {
        ChipSet newchips = new ChipSet();
        
        for (String color : cs.getColors())
        	newchips.setNumChips(color, -1 * cs.getNumChips(color));
		
        return newchips;
    }

    /**
     * Add two chipsets, adding the numbers of each color in each chipset.
     *
     * @param cs1 The first chipset to be added.
     * @param cs2 The second chpiset to be added.
     * @return A new chipset representing the sum of the two component
     *         chipsets.
     */
    public static ChipSet addChipSets(ChipSet cs1, ChipSet cs2)
    {
        ChipSet cs = new ChipSet();

        HashSet<String> allcolors = new HashSet<String>();
        allcolors.addAll(cs1.getColors());
        allcolors.addAll(cs2.getColors());

        for (String color : allcolors)
        	cs.setNumChips(color, cs1.getNumChips(color) + cs2.getNumChips(color));
		
        return cs;
    }

    /**
     * Subtract the second given chipset from the first, returning a new
     * chipset representing the result.
     *
     * @param initial    The chipset to be subtracted from.
     * @param subtracted The chipset to be subtracted.
     * @return A new chipset representing the result of the subtraction
     *         operation.
     */
    public static ChipSet subChipSets(ChipSet initial, ChipSet subtracted)
    {
        return addChipSets(initial, getNegation(subtracted));
    }
	
    /**
     * Get the power set of the chipset
     * @param cs The chipset which's power set will be returned
     * @return The set of all subsets of the chipset
     * @author ilke
     */
    public static Set<ChipSet> getPowerSet(ChipSet cs) {
    	Set<ChipSet> s = new HashSet<ChipSet>();
    	s.add(new ChipSet());	// add an empty chipset
    	
    	//for all chips in the chipset
    	for(String color:cs.getColors()) {
    		for( int chip = 0; chip < cs.getNumChips(color); ++chip) {
    			
    			//create a copy of the existing chipset
    			//add the chip to each of chipsets
    			//and add the chipsets back to the main set
    			
    			Set<ChipSet> q = new HashSet<ChipSet>();
    			for(ChipSet c:s) {
    			ChipSet cc = (ChipSet)c.clone();
    			cc.add(color,1);
    			q.add(cc);
    			}
    			
    			s.addAll(q);
    		}
    	}
    	return s;
    }
    
    /**
     * Get the all possibles exchanges that can be done between the clients 
     * with ChipSets cs1 and cs2 (even offering and requesting the same chips)
     * @param cs1 ChipSet of the first agent
     * @param cs2 ChipSet of the second agent
     * @return An array of "ChipSet arrays with 2 elements", each element 
     * representing a possible exchange between the clients. The ChipSet arrays'
     * first entries represent the chips that the first client will give, and the
     * second one represents the second's.
     * <p>
     * All exchanges are unique
     * @author ilke
     */
    public static Set<ArrayList<ChipSet>> getAllExchanges(ChipSet cs1, ChipSet cs2) {
    	HashSet<ArrayList<ChipSet>> allExchanges = new HashSet<ArrayList<ChipSet>>();
    	Set<ChipSet> ps1 = getPowerSet(cs1);
    	Set<ChipSet> ps2 = getPowerSet(cs2);
    	ArrayList<ChipSet> t;
    	
    	for(ChipSet c:ps1) {
    		for(ChipSet s:ps2) {
    			ChipSet newcs = ChipSet.subChipSets(c, s);
    			ChipSet newc = new ChipSet();
    			ChipSet news = new ChipSet();
    			
    			for(String color:newcs.getColors()) {
    				if(newcs.getNumChips(color) > 0) {
    					newc.add(color, newcs.getNumChips(color));
    				}
    				else if(newcs.getNumChips(color) < 0) {
    					news.add(color, newcs.getNumChips(color) * (-1));
    				}
    			}
    			
    			t = new ArrayList<ChipSet>();
    			t.add(newc);
    			t.add(news);
    			allExchanges.add(t);
    			
    		}
    	}

    	return allExchanges;    	
    }
}