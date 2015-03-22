package tuplespace_old;

import java.awt.Point;

import net.jini.core.entry.Entry;

public class Tuple implements Entry {

	public String type;
	public Point point;
	
	public Tuple() {

	}
	
	public Tuple(String n, Point p) {
		this.type = n;
		this.point = p;
	}
	
	public Tuple(String n) {
		this.type = n;
	}

	public String getType(){
		return this.type;
	}
	
	public Point getPoint(){
		return this.point;
	}
}
