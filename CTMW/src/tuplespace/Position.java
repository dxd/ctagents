package tuplespace;

import java.sql.Timestamp;
import java.util.Date;

import oopl.DistributedOOPL;
import net.jini.core.entry.Entry;

public class Position implements TimeEntry {
	
	public Integer id = null;
	public String agent = null;
	public Cell cell = null;
	public Timestamp time = null;
	public Integer clock;
	
	public Position() {

	}
	public Position(Integer id, Cell cell) {
		this.id = id;
		this.cell = cell;
		this.time = new Timestamp(new Date().getTime());
	}
	public Position(String agent, Cell cell) {
		this.agent = agent;
		this.cell = cell;
		this.time = new Timestamp(new Date().getTime());
	}
	public Position(Integer id, String agent, Cell cell, int clock) {
		this.id = id;
		this.agent = agent;
		this.cell = cell;
		this.clock = clock;
		this.time = new Timestamp(new Date().getTime());
	}
	public Position(String agent, Cell cell, int clock) {
		this.agent = agent;
		this.cell = cell;
		this.clock = clock;
		this.time = new Timestamp(new Date().getTime());
	}

	public Position(String agent) {
		this.agent = agent;
	}

	
	public Position(Object[] params) {
		this.agent = params[0].toString();
		if (params[1] != null)
			this.cell = new Cell(Integer.getInteger(params[1].toString()), Integer.getInteger(params[2].toString()));
	}
	
	public int[] toIntArray(DistributedOOPL oopl) {
		//JL = new JiniLib();
		int[] r = new int[this.cell==null?12:18];
		JL.addPredicate(r,0,oopl.prolog.strStorage.getInt("position"),2, oopl); // tuple/3
		JL.addPredicate(r,3,JL.makeStringKnown(this.agent==null?"null":this.agent, oopl),0, oopl); // the name
		int c = 9;
		if(this.cell == null) JL.addPredicate(r, 6, JL.oopl.prolog.strStorage.getInt("null"), 0, oopl);
		else {
			JL.addPredicate(r, 6, oopl.prolog.strStorage.getInt("cell"), 2, oopl);
			JL.addNumber(r, 9, this.cell.x, oopl);
			JL.addNumber(r, 12, this.cell.y, oopl);
			c = 15;
		} 
		//System.out.println("to array: " + r.toString());
		//addNumber(r, c,t.i);
		return r;
	}
	
	@Override
	public String toPrologString() {
		return "position(" + agent + "," + cell.toPrologString()+ "," + clock + ").";
	}
	
	@Override
	public String toString() {
		return "Position [subject=" + agent + ", id=" + id + ", cell=" + cell
				+ ", time=" + time + ", clock=" + clock + "]";
	}

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Cell getCell() {
		return cell;
	}

	public void setCell(Cell cell) {
		this.cell = cell;
	}


	public void setTime(Timestamp time) {
		this.time = time;
	}


	public void setClock(Integer clock) {
		this.clock = clock;
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

