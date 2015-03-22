package tuplespace;

import java.io.Serializable;
import java.util.Date;

public class Cell implements Serializable {
	
	
	public int x;
	public int y;
	
	public Cell(int i, int j) {
		x = i;
		y = j;
	}

	@Override
	public String toString() {
		return "Cell [x=" + x + ", y=" + y + "]";
	}

	public String toPrologString() {
		return "cell(" + x + "," + y + ")";
	}
}
