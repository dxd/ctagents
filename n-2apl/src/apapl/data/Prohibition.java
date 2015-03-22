package apapl.data;

import java.util.ArrayList;

import apapl.SubstList;
import apapl.UnboundedVarException;
import apapl.plans.Plan;
import apapl.plans.PlanSeq;
import apapl.program.BeliefUpdate;
import apapl.program.BeliefUpdates;
import apapl.program.PGrule;

public class Prohibition {
	
	private Literal     prohibition;
	private Literal		sanction;
	private byte		priority;
	
	/**
	 * Constructs a new empty prohibition.
	 */
	public Prohibition()
	{
		
	}
	

	public Prohibition(Literal prohibition)
	{
		this.prohibition = prohibition;

	}
	

	public Prohibition(Literal prohibition, Literal sanction)
	{
		this.prohibition = prohibition;
		this.sanction = sanction;
	}

	public void addSanction(Literal sanction)
	{
		this.sanction = sanction;
	}
	
	public void addLiteral(Literal prohibition)
	{
		this.prohibition = prohibition;
	}
	
	public void addPriority(byte priority)
	{
		this.priority = priority;
	}
		
	/**
	 * Gives a string representation of this object.
	 */
	public String toString(boolean inplan)
	{
		String r = "";
		r = r + prohibition.toString(inplan) + " -> " + sanction.toString(inplan);
		r += ", priority " + this.priority;
		//if (r.length()>=5) r = r.substring(0,r.length()-5);	
		return r;
	}
	
	public String toString()
	{
		return toString(false);
	}
	
	public String toRTF(boolean inplan)
	{
		if (prohibition.size()<=0) return "";
		
		String r = "";
		String s = "\\cf1  and \\cf0 ";
	
			r = r + prohibition.toRTF(inplan) + " -> " + sanction.toRTF(inplan) + s;
			
	
			
		if (r.length()>=s.length()) r = r.substring(0,r.length()-s.length());	
		
		return r;
	}


	public Byte getPriority() {
		
		return priority;
	}


	public Literal getSanction() {

		return sanction;
	}


	public void setPriority(byte priority) {

		this.priority = priority;
	}
	
	public void unvar() throws UnboundedVarException
	{
		prohibition.unvar();
		sanction.unvar();
	}
	
	/**
	 * Clones this object.
	 * 
	 * @return the clone
	 */
	public Prohibition clone()
	{
		Prohibition copy = new Prohibition();
		copy.prohibition = this.prohibition.clone();
		copy.sanction = this.sanction.clone();
		copy.priority = this.priority;
		return copy;
	}
	
	/**
	 * Applies a substitution to this prohibition.
	 * 
	 * @param theta the substitution to apply
	 */
	public void applySubstitution(SubstList<Term> theta)
	{
		prohibition.applySubstitution(theta);
		sanction.applySubstitution(theta);
	}
	
	public void freshVars(ArrayList<String> unfresh, ArrayList<String> own, ArrayList<ArrayList<String>> changes)
	{
		prohibition.freshVars(unfresh,own,changes);
		sanction.freshVars(unfresh,own,changes);
	}
	
	public ArrayList<String> getVariables()
	{
		ArrayList<String> vars = new ArrayList<String>();
		vars.addAll(prohibition.getVariables());
		vars.addAll(sanction.getVariables());
		return vars;
	}


	public Literal getProhibition() {

		return prohibition;
	}
	public void evaluate()
	{
		prohibition.evaluate();
	}

}
