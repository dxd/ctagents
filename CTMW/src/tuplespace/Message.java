package tuplespace;

import java.sql.Timestamp;
import java.util.Date;

import oopl.DistributedOOPL;
import net.jini.core.entry.Entry;

public class Message implements TimeEntry {
	
	public String agent;
	public String type;
	
	public Integer id;
	public Timestamp time;
	public Integer clock;
	public Boolean accepted = false;
	
	
	public Message() {
		
	}


	public Message(String agent, String obligation, String sanction,
			Integer deadline, Integer clock) {
		
		this.agent = agent;
		this.type = obligation;		
		this.id = deadline;
		this.clock = clock;
		this.time = new Timestamp(new Date().getTime());
	}


	public Message(String agent) {
		this.agent = agent;
	}

	@Override
	public String toString() {
		return "Message [agent=" + agent + ", type=" + type + ",  id=" + id + ", time=" + time + ", clock="
				+ clock + ", accepted=" + accepted + "]";
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
		return "message(" +id + ","+ agent + "," + type + "," + clock +"," + accepted + ")";
	}
}
