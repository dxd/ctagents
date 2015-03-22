package ctMW; 
import java.util.ArrayList;
import java.util.HashMap;  
import apapl.data.*;
import aplprolog.datastructures.PrimIntArrayList;
import aplprolog.datastructures.PrimIntKeyHashMap;
import aplprolog.prolog.IntHarvester;
import aplprolog.prolog.Prolog;
/**
 * Instantiations of this class can convert integer arrays to 2APL objects and back.
 * This class was written purely for the experimentations. If you want to get a feel
 * of how the integer encoding works, then you should take look at the parser file (IntProlog.jj).
 * 
 * @author Bas Testerink, Utrecht University, The Netherlands
 * 
 */
public class APAPLTermConverter {
	// translate from APL datatypes to int[]'s
	public PrimIntArrayList array = new PrimIntArrayList(true);
	public PrimIntArrayList splits = new PrimIntArrayList(true);
	public PrimIntKeyHashMap<PrimIntArrayList> varIndices = new PrimIntKeyHashMap<PrimIntArrayList>(PrimIntKeyHashMap.OBJ);
	public HashMap<String, Integer> vars = new HashMap<String, Integer>();
	public PrimIntKeyHashMap<String> varNames = new PrimIntKeyHashMap<String>(PrimIntKeyHashMap.OBJ);
	public int varID = 0;
	public Integer idNot,idTrue,idOr;

	// translate from int[] to 2APL
	public Term hook = null; // upper term when parsing a int[], since only variables values are parsed from int[]  to 2apl, you don't need to consider queries with strings of terms
	public int arCursor = 0;

	public Prolog p;

	public APAPLTermConverter(Prolog p){
		this.p = p;

		if((idNot = p.strStorage.getInt("not"))==null)
			idNot = p.strStorage.add("not");
		if((idTrue = p.strStorage.getInt("true"))==null)
			idTrue = p.strStorage.add("true");
		if((idOr = p.strStorage.getInt(";"))==null)
			idOr = p.strStorage.add(";");
	}
	
	/**
	 * Clear out all the bookkeeping variables.
	 */
	public void reset(){
		array.clear();
		splits.clear();
		varIndices.clear();
		vars.clear();
		varNames.clear();
		hook = null;
		arCursor = 0;
		varID = 0;
	}
	
	/**
	 * Create a hashmap that maps integer id's to Strings.
	 * @return The mapping from variable id's to their names.
	 */
	public PrimIntKeyHashMap<String> idToVarNames(){
		PrimIntKeyHashMap<String> queryVars = new PrimIntKeyHashMap<String>(PrimIntKeyHashMap.OBJ);
		if(varNames.size() > 0)
			for(int k = varNames.firstKey(); varNames.hasNext(); k = varNames.nextKey()){ // clone of the hashmap
				queryVars.putObj(k, varNames.getObj(k));
			}
		return queryVars;
	}

	//////////////////////////
	/// From 2APL to int[] ///
	//////////////////////////
	/**
	 * Extract the integer array from the global variables.
	 * @param varOverhead Whether to add integer indices.
	 * @param splitOverhead Whether to add split indices.
	 * @return Converted 2APL term.
	 */
	public int[] obtainIntConversion(boolean varOverhead, boolean splitOverhead){
		// variable overhead integers
		if(varOverhead){
			int amountOfVars = varID;
			varID--; // id of the first one
			while(varID >= 0){ // for each numbered variable
				PrimIntArrayList list = varIndices.getObj(varID); // get its occurrences
				for(int i = 0; i < list.pointer; i++)
					array.addInt(list.intdata[i]);
				array.addInt(list.pointer);
				varID--;
			}
			array.addInt(amountOfVars);
		}
		// splits overhead
		if(splitOverhead){
			for(int i = splits.pointer-1; i >= 0; i--)
				array.addInt(splits.intdata[i]);
			array.addInt(splits.pointer);
		}
		return p.listToPrim(array);
	}

	/**
	 * Convert a 2APL term (processes it and the result is stored in the global variables).
	 * @param term Term to be converted.
	 */
	public void convertTerm(Term term){
		reset();
		convertT(term); 
	}

	/**
	 * Convert a 2APL term (processes it and the result is stored in the global variables).
	 * @param term Term to be converted.
	 */
	public int[] convertFact(Term term){
		reset();
		convertT(term);
		splits.addInt(array.pointer);
		return obtainIntConversion(true, true);
	}
	/**
	 * Convert a 2APL literal (processes it and the result is stored in the global variables).
	 * @param literal Literal to be converted.
	 */
	public int[] convertLiteral(Literal literal){
		reset();
		convertQ(literal,true); // same as query except for the addition of the 0-split
		return obtainIntConversion(true, true);
	}
	/**
	 * Convert a 2APL query (processes it and the result is stored in the global variables).
	 * @param query Query to be converted.
	 */
	public int[] convertQuery(Query query){ // queries are now binary trees in 2APL, this can be improved upon
		reset(); // reset all data 
		splits.addInt(0); // needs at least one split
		convertQ(query,true); // add the body
		return obtainIntConversion(true,true);
	}
	/**
	 * Convert a 2APL query. Use <code>convertQuery(Query query)</code> if you need an immediate translation from Query to integers.
	 * This is a conversion to global variables only.
	 * @param query Query to be converted.
	 */
	public void convertQ(Query query, boolean top){ // if top = true, then you must add splits, conversion is depth-first
		if(query instanceof ComposedQuery){ // x and y, x or y
			if(query instanceof AndQuery){
				array.addInt(IntHarvester.PARENTHESIZED); // 'and' is converted to parenthesized
				array.addInt(2);
				AndQuery aQuery = (AndQuery)query;
				convertQ(aQuery.getLeft(),false);
				convertQ(aQuery.getRight(),false);
			} else if(query instanceof OrQuery){ // 'or' has its own rule
				array.addInt(IntHarvester.PREDICATE);
				array.addInt(idOr);
				array.addInt(2);
				OrQuery oQuery = (OrQuery)query;
				convertQ(oQuery.getLeft(),false);
				convertQ(oQuery.getRight(),false);
			}
			if(top)splits.addInt(array.pointer); // update splits
		} else if(query instanceof Literal){
			Literal l = (Literal) query;
			if(l.containsNots()){ // 'not' is treated in IntProlog as a predicate
				array.addInt(IntHarvester.PREDICATE);
				array.addInt(idNot);
				array.addInt(1);
			}
			convertT(l.getBody()); // transform the body
			if(top)splits.addInt(array.pointer); // update splits
		} else if(query instanceof True){ // 'true' is always a fact in IntProlog
			array.addInt(IntHarvester.PREDICATE);
			array.addInt(idTrue);
			array.addInt(0);
			if(top)splits.addInt(array.pointer); // update splits
		} 
	}

	/**
	 * Convert a 2APL term. Use <code>convertLiteral(Literal literal)</code> if you need an immediate translation from Literal to integers.
	 * This is a conversion to global variables only.
	 * @param term Term to be converted.
	 */
	public void convertT(Term t){
		if(t instanceof APLFunction){
			APLFunction term = (APLFunction)t;
			if(term.getName().equals(",")){
				array.addInt(IntHarvester.PARENTHESIZED); // parenthesized need an integer less (there is no id)
			} else {
				array.addInt(IntHarvester.PREDICATE); // obtain the number for the identifier
				Integer stringid = p.strStorage.getInt(term.getName());
				if(stringid == null) stringid = p.strStorage.add(term.getName()); // add the identifier if it is unknown
				array.addInt(stringid);
			}
			array.addInt(term.getParams().size()); // the number of arguments
			for(Term t2 : term.getParams()) convertT(t2); // convert the arguments
		} else if(t instanceof APLIdent){ // APLIdent is equal to a predicate with 0 arguments
			APLIdent term = (APLIdent)t;
			array.addInt(IntHarvester.PREDICATE);
			Integer stringid = p.strStorage.getInt(term.getName());
			if(stringid == null) stringid = p.strStorage.add(term.getName()); // identifier needs to be added
			array.addInt(stringid);
			array.addInt(0); // idents do keep their argument number so they fit in the id/arity key combination for rules
		} else if(t instanceof APLList){ // there is a major difference in IntProlog lists, and 2APL lists, 2APL lists are linear trees whereas IntProlog lists are more like distributed arrays
			APLList term = (APLList)t;
			array.addInt(IntHarvester.LIST);
			Term head = term.getHead();
			int elemCount = 0;
			int currentSize = array.pointer;
			array.addInt(0);
			boolean emptyListTail = true;
			if(head!=null){
				elemCount++;
				convertT(head);
				APLListVar tail = (APLListVar)term.getTail();
				while(tail!=null){
					if(tail instanceof APLList){ 
						APLList taillist = (APLList) tail;
						head = taillist.getHead();
						if(head!=null){
							elemCount++;
							convertT(head);
						} 
						tail = taillist.getTail(); 
					} else {
						emptyListTail = false; // it is a var; so no [] at the end!
						convertT(tail); // handle it as a variable
						tail = null; // stop loop
					}
				}
				if(emptyListTail){ // a [] at the end as tail
					array.addInt(IntHarvester.LIST);
					array.addInt(0);
				}
				array.intdata[currentSize] = elemCount;
			}
		} else if(t instanceof APLNum){
			APLNum term = (APLNum)t;
			array.addInt(IntHarvester.NUMBER);
			long l = Double.doubleToLongBits(term.toDouble()); 
			array.addInt((int)((l>>>32)));
			array.addInt((int)((l<<32)>>>32));
		} else if(t instanceof APLVar){
			APLVar term = (APLVar)t;
			if(term.isBounded()){
				convertT(term.getTerm());
			} else if(term.getName().equals("_")){
				array.addInt(IntHarvester.VARIABLE);
				array.addInt(Integer.MIN_VALUE);
			} else {
				array.addInt(IntHarvester.VARIABLE);
				Integer id = vars.get(term.getName()); // get the identifier of the variable
				if(id == null){ // variable hasn't occured before so add it
					id = varID;
					vars.put(term.getName(), id); 
					varIndices.putObj(id, new PrimIntArrayList(true)); 
					varNames.putObj(id,term.getName());
					varID++;
				}
				varIndices.getObj(id).addInt(array.pointer);
				array.addInt(id); 
			}
		}
	}

	//////////////////////////
	/// From int[] to 2APL ///
	//////////////////////////
	/*
	 * Note: this is only done when variables from the query were instantiated
	 */
	/**
	 * Transform an integer array to a 2APL term.
	 * @param ar Array to be transformed.
	 * @return Conversion result.
	 */
	public Term get2APLTerm(int[] ar){
		arCursor = 0;
		return convertArray(ar);
	}

	/**
	 * Transform an integer array to a 2APL term. For external use, use get2APLTerm(int[] ar) to ensure
	 * that the global array cursor is correctly set (this one is made for recursion).
	 * @param ar Array to be transformed.
	 * @return Conversion result.
	 */
	public Term convertArray(int[] ar){
		if(ar[arCursor] == IntHarvester.PREDICATE){ 
			String name = p.strStorage.getString(ar[arCursor+1]);
			int nrArguments = ar[arCursor+2];
			Term t = null;
			arCursor+=3;
			if(nrArguments == 0) { // is APLIdent
				t = new APLIdent(name,name.charAt(0)=='\''); 
			} else { // is APLFunction
				ArrayList<Term> params = new ArrayList<Term>();
				for(int i = 0; i < nrArguments; i++)
					params.add(convertArray(ar)); 
				t = new APLFunction(name, params);
			}
			return t;
		} else if(ar[arCursor] == IntHarvester.LIST){
			int nrElems = ar[arCursor+1];
			arCursor+=2;
			APLListVar t = null;
			if(nrElems==0) t = new APLList();
			else if(nrElems==1){
				t = new APLList(true,convertArray(ar),(APLListVar)convertArray(ar)); // first is head, tail must be list as well
			} else t = constructList(ar, nrElems);
			return t;
		} else if(ar[arCursor] == IntHarvester.PARENTHESIZED){
			APLFunction t = null; // is represented with "," 2-ary predicate
			int nrElems = ar[arCursor+1];
			arCursor+=2;
			if(nrElems==1) return convertArray(ar); // no wrapping arround single item in parentheses

			ArrayList<Term> params = new ArrayList<Term>();
			params.add(convertArray(ar)); // add first element
			APLFunction last = new APLFunction(",", params);
			t = last;
			for(int i = nrElems; i > 1; i--){ 
				ArrayList<Term> newParams = new ArrayList<Term>();
				newParams.add(convertArray(ar)); 
				if(i==2){
					newParams.add(convertArray(ar));
					i--; // last two are added at once
				}
				APLFunction newF = new APLFunction(",", params);
				last.getParams().add(newF); // add to last term
				last = newF; // switch
			}
			return t;
		} else if(ar[arCursor] == IntHarvester.NUMBER){ 
			long l1 = ((long)ar[arCursor+1]<<32)>>>32;
			long l2 = ((long)ar[arCursor+2]<<32)>>>32;
			arCursor+=3;
			return new APLNum(Double.longBitsToDouble((l1<<32)|l2));
		} else if(ar[arCursor] == IntHarvester.VARIABLE){ // important note: 2APL requires that belief queries return GROUND literals
			int id = ar[arCursor+1];
			int[] value = p.substitution.value(id); // returned variables must be instantiated
			if(value==null){
				arCursor += 2;
				return new APLVar(); // for test purposes
			}
			int currentCursor = arCursor;
			arCursor = 0;
			Term t = convertArray(value); // return their result
			arCursor = currentCursor+2;
			return t;
		}
		return null;
	}

	/**
	 * Support method for constructing lists.
	 * @param ar Array to be transformed.
	 * @param counter Cursor in the array.
	 * @return Conversion result.
	 */
	public APLList constructList(int[] ar, int counter){
		if(counter == 1)
			return new APLList(true,convertArray(ar),(APLListVar)convertArray(ar)); // reached tail point
		else 
			return new APLList(true,convertArray(ar),constructList(ar,counter-1));

	}

	/////////////////////
	/// Serialization ///
	/////////////////////
	/**
	 * Serialize a integer array to a byte array.
	 * @param ar Array to be serialized.
	 * @return Conversion result (bytes).
	 */
	public byte[] serializeArray(int[] ar){
		byte[] r = new byte[ar.length*4];
		int bytePos = 0;
		for(int i = 0; i < ar.length; i++){
			bytePos = i*4;
			r[bytePos] = (byte)(ar[i] >>> 24);
			r[bytePos+1] = (byte)(ar[i] >>> 16);
			r[bytePos+2] = (byte)(ar[i] >>> 8);
			r[bytePos+3] = (byte)ar[i];
		}
		return r;
	}
	/**
	 * Unserialize a byte array to a integer array.
	 * @param ar Array to be unserialized.
	 * @return Conversion result (integers).
	 */
	public int[] unserializeArray(byte[] bytes){
		int[] r = new int[bytes.length/4];
		int bytePos = 0;
		for(int i = 0; i < r.length; i++){
			bytePos = i*4;
			r[i] = (bytes[bytePos] << 24)+((bytes[bytePos+1] & 0xFF) << 16)+((bytes[bytePos+2] & 0xFF) << 8)+(bytes[bytePos+3] & 0xFF);
		}
		return r;
	} 
	/**
	 * Integer array to string (call to recursive method).
	 * @param ar Array to be converted.
	 * @return Conversion result (String).
	 */
	public String arrayToString(int[] array){ // NOTE: does not look inside variables
		StringBuffer buffer = new StringBuffer();
		arToString(array,buffer);
		return buffer.toString();
	}
	/**
	 * Integer array to string (recursive method, use arrayToString(int[] array)).
	 * @param ar Array to be converted.
	 * @param buffer Accumulator.
	 */
	public void arToString(int[] ar, StringBuffer buffer){ 
		elemToString(ar, 0, buffer); // append the head
		int c = p.harvester.scanElement(ar, 0, false, false);
		int splits = ar[ar.length-1];
		if(splits>1&&ar[ar.length-2]==0)splits = splits-1; // zero-split for queries
		splits--; // you have done the head
		if(splits>0){ // if there is a body
			buffer.append(" :- ");
			for(int i = 0; i < splits; i++){
				elemToString(ar, c,buffer);
				buffer.append(','); // separate with comma's
				c = p.harvester.scanElement(ar, c, false, false);
			}
			buffer.deleteCharAt(buffer.length()-1); // remove last ','
		}
		buffer.append('.');
	}  
	/**
	 * Integer array to string (support method, use arrayToString(int[] array)).
	 * @param ar Array to be converted.
	 * @param int cursor Cursor inside the array.
	 * @param buffer Accumulator.
	 */
	public void elemToString(int[] value, int cursor, StringBuffer buffer){
		int type = value[cursor];
		if(type==IntHarvester.CUT){
			buffer.append('!'); 
		} else if(type==IntHarvester.VARIABLE){ 
			buffer.append('V');
			buffer.append(value[cursor+1]); 
		} else if(type==IntHarvester.NUMBER){
			long l1 = ((long)value[cursor+1]<<32)>>>32;
			long l2 = ((long)value[cursor+2]<<32)>>>32;
			double d = Double.longBitsToDouble((l1<<32)|l2);
			buffer.append(d); 
		} else if(type==IntHarvester.PREDICATE){
			int x = cursor+3; // start of args
			int argcount = value[cursor+2];
			buffer.append(p.strStorage.getString(value[cursor+1])+(argcount>0?"(":"")); // append identifier
			for(int i = 0; i < argcount;i++){
				elemToString(value,x,buffer); 
				x = p.harvester.scanElement(value,x,false,false); // scan over arguments 
				buffer.append(',');
			}
			if(argcount>0){
				buffer.deleteCharAt(buffer.length()-1); // remove last ','
				buffer.append(')');
			}
		} else if(type==IntHarvester.PARENTHESIZED){
			int x = cursor+2; // start of args
			buffer.append('(');
			int argcount = value[cursor+1];
			for(int i = 0; i < argcount;i++){
				elemToString(value, x,buffer);
				x = p.harvester.scanElement(value,x,false,false); // scan over arguments
				buffer.append(',');
			}
			if(argcount>0)buffer.deleteCharAt(buffer.length()-1);
			buffer.append(')');
		} else if(type==IntHarvester.LIST){
			int x = cursor+2; // start of elems
			buffer.append('[');
			int argcount = value[cursor+1];
			for(int i = 0; i < argcount;i++){
				elemToString(value, x,buffer);
				x = p.harvester.scanElement(value,x,false,false); // scan over elems
				buffer.append(',');
			}
			if(argcount>0){
				buffer.deleteCharAt(buffer.length()-1); // remove last ','
				buffer.append('|');
				elemToString(value, x,buffer); // append tail
			}
			buffer.append(']');
		} 
	}
}