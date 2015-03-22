package tuplespace;

import java.sql.Timestamp;
import java.util.Date;

import oopl.DistributedOOPL;
import net.jini.core.entry.Entry;

public class SwapRequest implements TimeEntry {
	
	public Integer id;
	public String agent;
	public String color;
	public String agent2;
	public Integer clock;
	public Timestamp time;
	
	public SwapRequest() {
		
	}
	public SwapRequest(String agent, String type, String cell, int clock) {
		this.agent = agent;
		this.agent2 = cell;
		this.color = type;
		this.clock = clock;
		this.time = new Timestamp(new Date().getTime());
	}
	public SwapRequest(Integer clock) {
		this.clock = clock;
	}

	public SwapRequest(String agent, String type) {
		this.agent = agent;
		this.color = type;
	}
	
	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public String getType() {
		return color;
	}

	public void setType(String type) {
		this.color = type;
	}


	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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
	public String toString() {
		return "SwapRequest [id=" + id + ", agent=" + agent + ", color="
				+ color + ", agent2=" + agent2 + ", clock=" + clock + ", time="
				+ time + "]";
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
		return "actionRequest(" + agent + "," + color + "," + agent2 + "," + clock + ")";
	}
}
