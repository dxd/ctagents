package apapl.data;

public class Sanction {
	
	private Literal sanction;
	private byte	priority;
	
	/**
	 * Constructs a new empty sanction.
	 */
	public Sanction()
	{
		
	}
	
	/**
	 * Constructs a conjunctive goal out of a list of literals.
	 * 
	 * @param goal the list of literals
	 */
	public Sanction(Literal sanction)
	{
		this.sanction = sanction;
		this.priority  = 1;
	}
	
	/**
	 * Constructs a conjunctive goal out of a list of literals.
	 * 
	 * @param goal the list of literals
	 */
	public Sanction(Literal sanction, String priority)
	{
		this.sanction = sanction;
		this.priority = Byte.valueOf(priority);
	}

	public void addPriority(String priority)
	{
		this.priority = Byte.valueOf(priority);
	}
	
	public void addLiteral(Literal sanction)
	{
		this.sanction = sanction;
	}
		
	/**
	 * Gives a string representation of this object.
	 */
	public String toString(boolean inplan)
	{
		String r = "";
		r = r + sanction.toString(inplan) + " -> " + priority;
		//if (r.length()>=5) r = r.substring(0,r.length()-5);	
		return r;
	}
	
	public String toString()
	{
		return toString(false);
	}
	
	public String toRTF(boolean inplan)
	{
		if (sanction.size()<=0) return "";
		
		String r = "";
		String s = "\\cf1  and \\cf0 ";
	
			r = r + sanction.toRTF(inplan) + " -> " + priority + s;
			
	
			
		if (r.length()>=s.length()) r = r.substring(0,r.length()-s.length());	
		
		return r;
	}

	public Literal getSanction() {

		return this.sanction;
	}

	public byte getPriority() {
		
		return priority;
	}
	
	

}
