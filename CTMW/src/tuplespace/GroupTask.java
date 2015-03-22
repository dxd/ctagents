package tuplespace;

import java.sql.Timestamp;
import java.util.Date;

import oopl.DistributedOOPL;
import net.jini.core.entry.Entry;

public class GroupTask implements TimeEntry {
	
	public Integer id;
	public String agent;
	public String type;
	public Integer clock;
	public Timestamp time;
	
	public GroupTask() {
		
	}
	public GroupTask(String agent, String type, int clock) {
		this.agent = agent;
		this.type = type;
		this.clock = clock;
		this.time = new Timestamp(new Date().getTime());
	}
	public GroupTask(Integer clock) {
		this.clock = clock;
	}

	public GroupTask(String agent, String type) {
		this.agent = agent;
		this.type = type;
	}
	
	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
		return "ActionRequest [id=" + id + ", subject=" + agent + ", type="
				+ type + ", clock=" + clock + ", time="
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
		return "actionRequest(" + agent + "," + type + "," + clock + ")";
	}
}
