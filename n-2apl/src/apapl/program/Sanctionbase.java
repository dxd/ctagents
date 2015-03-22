package apapl.program;

import apapl.SolutionIterator;
import apapl.data.Goal;
import apapl.data.GoalCompare;
import apapl.data.Query;
import apapl.data.Sanction;
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

public class Sanctionbase extends Base implements Iterable<Sanction>{
	
	private ArrayList<Sanction> sb = new ArrayList<Sanction>();
	/**
	 * Constructs a new goal base.
	 */
	public Sanctionbase()
	{		
	}
	
	/**
	 * Constructs a new goal base using given list of goals
	 */
	public Sanctionbase(ArrayList<Sanction> sb)
	{		
		this.sb = new ArrayList<Sanction>(sb);
	}

	/**
	 * Returns an iterator to iterate over the goals stored in this goal base.
	 * 
	 * @return iterator to iterate over all goals in this base
	 */
	public  Iterator<Sanction> iterator()
	{
		return sb.iterator();
	}
	/**
	 * Adds a plan to this plan base.
	 * 
	 * @param p the plan to be added to the plan base
	 */
	public void add(Sanction s)
	{
		sb.add(s);
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
		for (Sanction g : sb) r = r + g.toRTF(false) + s;
			
		if (r.length()>=s.length()) r = r.substring(0,r.length()-s.length());	
		return r;
	}
	
	/**
	 * @return clone of the goalbase
	 */
	public Sanctionbase clone()
	{
		Sanctionbase sb = new Sanctionbase(this.getSanctionbase());
		return sb;
	}
	
	/**
	 * @return the goalbase
	 */
	public ArrayList<Sanction> getSanctionbase() {
		return sb;
	}

	public boolean isEmpty() {
		
		return sb.isEmpty();
	}
}
