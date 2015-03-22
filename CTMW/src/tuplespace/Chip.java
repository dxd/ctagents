package tuplespace;

import java.awt.Point;
import java.sql.Timestamp;
import java.util.Date;

import oopl.DistributedOOPL;
import net.jini.core.entry.Entry;

public class Chip implements TimeEntry {

	public Integer id;
	public String agent;
	public Timestamp time;
	public String color;
	public Integer clock;
	public Integer number;
	
	public Chip() {

	}
	
	public Chip(Integer id, String agent, String color, int clock, Integer number) {
		this.id = id;
		this.agent = agent;
		this.color = color;
		this.clock = clock;
		this.time = new Timestamp(new Date().getTime());
		this.number = number;
	}
	
	public Chip(String agent, String color, Integer number, int clock) {
		this.agent = agent;
		this.color = color;
		this.number = number;
		this.clock = clock;
		this.time = new Timestamp(new Date().getTime());
	}

	public Chip(String agent) {
		this.agent = agent;
	}

	public Chip(Object[] params) {
		this.agent = params[2].toString();
	}
	
	public Chip(String sAgent, String c) {
		this.agent = sAgent;
		this.color = c;
	}

	public int[] toIntArray(DistributedOOPL oopl) {
		int[] r = new int[21];
		JL.addPredicate(r,0,oopl.prolog.strStorage.getInt("reading"),4, oopl); // cargo/2
		JL.addPredicate(r, 3, oopl.prolog.strStorage.getInt("cell"), 2, oopl);
		//JL.addNumber(r, 6, this.cell.x, oopl);
		//JL.addNumber(r, 9, this.cell.y, oopl);
		//JL.addNumber(r, 12, this.value.intValue(), oopl);
		JL.addPredicate(r,15, JL.makeStringKnown(this.agent, oopl),0, oopl);
		JL.addNumber(r,18,this.clock, oopl);
		//addPredicate(r,3,makeStringKnown(t.agent==null?"null":t.agent),0); // the name
		//for (int i = 0;  i<r.length; i++){
		//	System.out.println("to array: " + oopl.prolog.strStorage.getString(r[i]));
			
		//}
		
		//addNumber(r, c,t.i);
		return r;
	}
	@Override
	public String toPrologString() {
		return "reading(" + agent + "," + color + "," + number.intValue() + "," + clock + ").";
	}
	
	@Override
	public String toString() {
		return "Chip [id=" + id + ", agent=" + agent + ", time=" + time
				+ ", color=" + color + ", clock=" + clock + ", number="
				+ number + "]";
	}

	@Override
	public void setTime() {
		this.time = new Timestamp(new Date().getTime());
		
	}
	@Override
	public Timestamp getTime() {
		return this.time;
	}

	public Integer getNumber() {
		return number.intValue();
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
