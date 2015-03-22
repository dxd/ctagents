package tuplespace;
import oopl.DistributedOOPL;

public class JiniLib {
	
	
	public JiniLib(DistributedOOPL oopl) {
		super();
		this.oopl = oopl;
	}

	public JiniLib() {
		
	}

	public DistributedOOPL oopl; // norm interpreter

	/*
	 * Add a number to an array.
	 */
	public void addNumber(int[] array, int cursor, int number, DistributedOOPL oopl){
		array[cursor] = oopl.prolog.harvester.NUMBER;
		array[cursor+1] = getInt(number,true);
		array[cursor+2] = getInt(number,false);
	}
	
	/*
	 * Convert a regular integer to a Prolog store value. Each number is encoded with 
	 * two integers (64 bit double format), so you can use getInt(i,true) and getInt(i,false) 
	 * to get both i's number representation parts.
	 */
	public int getInt(int i, boolean a){
		long l = Double.doubleToLongBits(i);
		if(a) return (int)((l>>>32));
		else return (int)((l<<32)>>>32);
	}
	
	/*
	 * Make sure String s has an index in the Prolog engine.
	 */
	public int makeStringKnown(String s, DistributedOOPL oopl){
		if(oopl.prolog.strStorage.getInt(s)==null) oopl.prolog.strStorage.add(s);
		return oopl.prolog.strStorage.getInt(s);
	}
	/*
	 * Gets the int value of a number out of an integer array.
	 * Note that normally this is a double.
	 */
	public int get_number(int[] call, int cursor){
		long l1 = ((long)call[cursor]<<32)>>>32;
		long l2 = ((long)call[cursor+1]<<32)>>>32;
		return (int)Double.longBitsToDouble((l1<<32)|l2);
	} 
	/*
	 * Add predicate integers to an array.
	 */
	public void addPredicate(int[] array, int cursor, int name, int arity, DistributedOOPL oopl){
		array[cursor] = oopl.prolog.harvester.PREDICATE;
		array[cursor+1] = name;
		array[cursor+2] = arity;
	}
}
