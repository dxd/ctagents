package tuplespace;

import java.sql.Timestamp;
import java.util.Date;

import oopl.DistributedOOPL;
import net.jini.core.entry.Entry;

public class Sanction implements TimeEntry {
	
	public String subject;
	public Timestamp time;
	public Integer clock;
	public Integer value;
	public Integer id;
	
	public Sanction() {

	}
	
	public Sanction(String agent, int clock, int value) {

		this.subject = agent;
		this.clock = clock;
		this.time = new Timestamp(new Date().getTime());
		this.value = value;

	}
	public Sanction(String agent, int value) {

		this.subject = agent;
		this.value = value;
		//this.time = new Timestamp(new Date().getTime());

	}

	public Sanction(int clock) {
		this.clock = clock;
	}

	public Sanction(String agent) {
		this.subject = agent;
	}
	public Sanction(Object[] params) {
		this.subject = params[0].toString();
		if (params[1] != null)
			this.clock = Integer.parseInt((String) params[1]);
		
		if (params[2] != null)
			this.value = Integer.parseInt((String) params[2]);
		//System.out.println(params[1].toString());
		//System.out.println(params[2].toString());
		//System.out.println(this);
	}

	@Override
	public String toPrologString() {
		return "sanction(" + subject + "," + clock + "," + value + ").";
	}
	public int[] toIntArray(DistributedOOPL oopl) {
		int[] r = new int[12];
		JL.addPredicate(r,0,oopl.prolog.strStorage.getInt("sanction"),4, oopl); // points/2
		
		JL.addPredicate(r,3,JL.makeStringKnown(this.subject, oopl),0, oopl); // the name
		JL.addNumber(r,6, this.clock, oopl);
		JL.addNumber(r, 9, this.value, oopl);

		return r;
	}
	
	
	@Override
	public String toString() {
		return "Sanction [subject=" + subject + ", time=" + time + ", clock=" + clock
				+ ", value=" + value + ", id=" + id + "]";
	}
	@Override
	public void setTime() {
		this.time = new Timestamp(new Date().getTime());
		
	}
	@Override
	public Timestamp getTime() {
		return this.time;
	}
	@Override
	public Integer getClock() {
		return clock;
	}

	@Override
	public void setClock(int clock) {
		this.clock = clock;
	}
}

