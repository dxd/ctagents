package tuplespace;


import java.sql.Timestamp;
import java.util.Date;

import oopl.DistributedOOPL;
import net.jini.core.entry.Entry;
//import apapl.data.Term;

public interface TimeEntry extends Entry{
	
	public Timestamp time = null;
	public JiniLib JL = new JiniLib();
	public int[] toIntArray(DistributedOOPL oopl);
	public void setTime();
	public Timestamp getTime();
	//public Term to2APLterm();
	public Integer getClock();
	public void setClock(int clock);
	public String toPrologString();
	

}
