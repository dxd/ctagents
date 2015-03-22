package apapl.program;

import apapl.SolutionIterator;
import apapl.data.Goal;
import apapl.data.GoalCompare;
import apapl.data.Query;
import apapl.data.Prohibition;
import apapl.data.True;
import apapl.Logger;
import apapl.Unifier;
import apapl.APLModule;
import apapl.Parser;
import apapl.data.Literal;
import apapl.data.Term;
import apapl.plans.PlanSeq;
import apapl.Prolog;

import java.io.FileInputStream;
import java.io.StringReader;
import java.util.ArrayList;

import apapl.SubstList;

import java.util.Enumeration;
import java.util.Collections;
import java.util.Iterator;

public class Prohibitionbase extends Base implements Iterable<Prohibition>{
	
	private ArrayList<Prohibition> sb = new ArrayList<Prohibition>();
	/**
	 * Constructs a new goal base.
	 */
	public Prohibitionbase()
	{		
	}
	
	/**
	 * Constructs a new goal base using given list of goals
	 */
	public Prohibitionbase(ArrayList<Prohibition> sb)
	{		
		this.sb = new ArrayList<Prohibition>(sb);
	}

	/**
	 * Returns an iterator to iterate over the goals stored in this goal base.
	 * 
	 * @return iterator to iterate over all goals in this base
	 */
	public  Iterator<Prohibition> iterator()
	{
		return sb.iterator();
	}
	/**
	 * Adds a plan to this plan base.
	 * 
	 * @param p the plan to be added to the plan base
	 */
	public void add(Prohibition s)
	{
		sb.add(s);
		//System.out.println("Prohibitionbase added:   "+s);
	}
	/**
	 * Converts this object to a <code>String</code> representation.
	 * 
	 * @return The <code>String</code> representation of this object.
	 */
	public String toString()
	{
		if (sb.size()>0)
			return concatWith(sb,",\n")+".\n\n";
		else return "";
	}
	
	/**
	 * Covnerts this goal base to a RTF string.
	 * 
	 * @return the RTF string
	 */
	public String toRTF()
	{
		if (sb.size()<=0) return "";
		
		String r = "";
		String s = ",\\par\n";
		for (Prohibition g : sb) r = r + g.toRTF(false) + s;
			
		if (r.length()>=s.length()) r = r.substring(0,r.length()-s.length());	
		return r;
	}
	
	/**
	 * @return clone of the goalbase
	 */
	public Prohibitionbase clone()
	{
		Prohibitionbase sb = new Prohibitionbase(this.getPbase());
		return sb;
	}
	
	/**
	 * @return the goalbase
	 */
	public ArrayList<Prohibition> getPbase() {
		return sb;
	}

	public boolean lowerThan(Byte priority) {
		for (Prohibition p : sb)
		{
			if (p.getPriority() < priority)
				return false;
		}
		return true;
	}

	public ArrayList<Prohibition> getHigher(Byte priority) {
		
		ArrayList<Prohibition> prohibitions = new ArrayList<Prohibition>();
		for (Prohibition p : sb)
		{
			if (p.getPriority() < priority)
				prohibitions.add(p);
		}
		return prohibitions;
	}
}
