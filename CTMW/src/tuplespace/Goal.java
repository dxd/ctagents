package tuplespace;

import java.sql.Timestamp;
import java.util.Date;

import oopl.DistributedOOPL;

import com.javadocmd.simplelatlng.LatLng;

import net.jini.core.entry.Entry;

public class Goal implements TimeEntry {

	public Integer id;
	public Cell cell;
	public String agent;
	public Timestamp time;
	public Integer clock;
	
	public Goal() {

	}
	public Goal(Integer clock) {
		this.clock = clock;
	}
	public Goal(int id, Cell cell, int clock) {
		
		this.id = id;
		this.cell = cell;
		this.clock = clock;
		this.time = new Timestamp(new Date().getTime());

	}
	public Goal(Cell cell, Integer clock) {
		
		this.cell = cell;
		this.clock = clock;
		this.time = new Timestamp(new Date().getTime());
	}
	
	public Goal(Object[] params) {
		if (params[0] != null)
    		this.cell = (Cell) params[0];
		if (params[1] != null)
			this.clock = Integer.parseInt((String) params[1]);
	}
	
	@Override
	public int[] toIntArray(DistributedOOPL oopl) {
		//JL = new JiniLib();
		int[] r = new int[15];
		JL.addPredicate(r,0,oopl.prolog.strStorage.getInt("cargo"),2, oopl); // cargo/2

		
		JL.addPredicate(r, 3, oopl.prolog.strStorage.getInt("cell"), 2, oopl);
		JL.addNumber(r, 6, this.cell.x, oopl);
		JL.addNumber(r, 9, this.cell.y, oopl);
			
		JL.addNumber(r,12,this.clock, oopl);
		//addPredicate(r,3,makeStringKnown(t.agent==null?"null":t.agent),0); // the name
		//for (int i = 0;  i<r.length; i++){
		//	System.out.println("to array: " + oopl.prolog.strStorage.getString(r[i]));
			
		//}
		
		//addNumber(r, c,t.i);
		return r;
	}
	
	@Override
	public String toPrologString() {
		return "cargo(" + cell.toPrologString() + "," + clock + ").";
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
	public Timestamp getTime() {
		return time;
	}
	public void setTime(Timestamp time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "Cargo [id=" + id + ", cell=" + cell + ", time=" + time
				+ ", clock=" + clock + "]";
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
}