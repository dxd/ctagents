package tuplespace;

import java.sql.Timestamp;
import java.util.Date;

import oopl.DistributedOOPL;
import net.jini.core.entry.Entry;

public class GroupCoin implements TimeEntry {
	


	public Integer x;
	public Integer y;
	public Integer clock;
	public Timestamp time;
	
	public GroupCoin() {
		
	}
	public GroupCoin(int x, int y, int clock) {
		this.x = x;
		this.y = y;
		this.clock = 10; // @TODO
		this.time = new Timestamp(new Date().getTime());
	}
	public GroupCoin(Integer clock) {
		this.clock = clock;
	}

	public GroupCoin(int x, int y) {
		this.x = x;
		this.y = y;
		this.time = new Timestamp(new Date().getTime());
	}
	

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	public GroupCoin(Object[] params) {

		if (params[0] != null)
			this.x = Integer.parseInt((String) params[0]);
		
		if (params[1] != null)
			this.y = Integer.parseInt((String) params[1]);
		if (params[2] != null)
			this.clock = Integer.parseInt((String) params[2]);

		//System.out.println(params[1].toString());
		//System.out.println(params[2].toString());
		//System.out.println(this);
	}
	@Override
	public int[] toIntArray(DistributedOOPL oopl) {
		int[] r = new int[12];
		JL.addPredicate(r,0,oopl.prolog.strStorage.getInt("groupCoin"),4, oopl); // points/2
		
		//JL.addPredicate(r,3,JL.makeStringKnown(this.agent, oopl),0, oopl); // the name
		JL.addNumber(r,3, this.x, oopl);
		JL.addNumber(r, 6, this.y, oopl);
		JL.addNumber(r, 9, this.clock, oopl);

		return r;
		// TODO Auto-generated method stub
	}
	@Override
	public void setTime() {
		this.time = new Timestamp(new Date().getTime());
		
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
	public String toString() {
		return "GroupCoin [x=" + x + ", y=" + y + ", clock=" + clock + "]";
	}
	@Override
	public String toPrologString() {
		return "groupCoin(" + x + "," + y + "," + clock + ").";
	}
}
