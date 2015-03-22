package tuplespace;

import java.sql.Timestamp;
import java.util.Date;

import oopl.DistributedOOPL;
import net.jini.core.entry.Entry;

public class Obligation implements TimeEntry {
	
	public String agent;
	public String obligation;
	public String sanction;
	public Integer deadline;
	public Timestamp time;
	public Integer clock;
	
	
	public Obligation() {
		
	}


	public Obligation(String agent, String obligation, String sanction,
			Integer deadline, Integer clock) {
		
		this.agent = agent;
		this.obligation = obligation;
		this.sanction = sanction;
		this.deadline = deadline;
		this.clock = clock;
		this.time = new Timestamp(new Date().getTime());
	}


	public Obligation(String agent) {
		this.agent = agent;
	}


	@Override
	public String toString() {
		return "Obligation [subject=" + agent + ", obligation=" + obligation
				+ ", sanction=" + sanction + ", deadline=" + deadline
				+ ", time=" + time + ", clock=" + clock + "]";
	}


	@Override
	public int[] toIntArray(DistributedOOPL oopl) {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public String toPrologString() {
		return "obligation(" + agent + "," + obligation + "," + sanction + "," + deadline + "," + clock + ")";
	}
}
