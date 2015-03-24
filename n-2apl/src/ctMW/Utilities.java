package ctMW;

import java.lang.reflect.InvocationTargetException;

import oopl.DistributedOOPL;


import tuplespace.Cell;
import tuplespace.Chip;
import tuplespace.GroupCoin;
import tuplespace.Message;
import tuplespace.Obligation;
import tuplespace.Points;
import tuplespace.Position;
import tuplespace.Prohibition;
import tuplespace.Sanction;
import tuplespace.SwapRequest;
import tuplespace.Tile;
import tuplespace.Time;
import tuplespace.TimeEntry;
import apapl.data.APLFunction;
import apapl.data.APLIdent;
import apapl.data.APLList;
import apapl.data.APLNum;
import apapl.data.Term;
import aplprolog.prolog.IntHarvester;

public class Utilities {
	public static String TYPE_STATUS="status", TYPE_PROHIBITION="prohibition", 
			TYPE_OBLIGATION="obligation", TYPE_READINGREQ = "readingRequest",TYPE_READING = "reading",TYPE_INVESTIGATE = "investigate",TYPE_CARGO = "cargo",TYPE_COIN = "coin",TYPE_POINTS = "points",
				TYPE_OBJECT="object", TYPE_INVENTORY="inventory", TYPE_SANCTION="sanction", TYPE_GROUPCOIN="groupCoin", NULL="null"; // for matching string with class type
		public int[] ar_true, ar_null, ar_state_change, ar_false; // precalculated IntProlog data 
		public int INT_TUPLE=0, INT_POINT=0, INT_NULL=0;
		public APAPLTermConverter converter; // Converts between IntProlog and 2APL
		private Prolog2Java p2j;
		public DistributedOOPL oopl;
		private EnvCT envCT;
		
		
	public Utilities(DistributedOOPL oopl, APAPLTermConverter converter,EnvCT envCT)	{
		this.oopl = oopl;
		this.converter = converter;
		this.envCT = envCT;
	}
	/*
	 * Create an entry object form an integer array. Perhaps we want to replace this with
	 * something like createEntry(oopl.prolog.toPrologString(call)).
	 */
	public TimeEntry createEntry(int[] call) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException{ // e.g.: read(tuple(name,point(2,4),20),0)
		//System.out.println(oopl.prolog.arStr(call));
		return p2j.parseTerm(call, converter, oopl);
		
	}
	

	/*
	 * Convert an entry to an array. Can also be done by calling the prolog compiler and give
	 * it e.toPrologString() as an argument.
	 */
	public int[] entryToArray(TimeEntry e){
		if(e == null){
			int[] r = new int[3];
			addPredicate(r, 0, oopl.prolog.strStorage.getInt("null"), 0);
			return r;
				
		} 
		else {
			return e.toIntArray(oopl);
		}
	
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
	public void addPredicate(int[] array, int cursor, int name, int arity){
		array[cursor] = IntHarvester.PREDICATE;
		array[cursor+1] = name;
		array[cursor+2] = arity;
	}
	/*
	 * Add a number to an array.
	 */
	public void addNumber(int[] array, int cursor, int number){
		array[cursor] = IntHarvester.NUMBER;
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
	////////////////////////2APL/2OPL from APLFunction to TimeEntry AND JAVASPACE

	/**
	 * Convert a Prolog predicate to a suitable JavaSpace datatype.
	 * @param sAgent The object that calls the method (important for the name in the status).
	 * @param call The predicate from the call.
	 * @return The entry representation of the predicate.
	 */
	public TimeEntry createEntry(String sAgent, APLFunction call){ 

		//System.out.print("from/for object " + sAgent + "  ");
		//System.out.println(call.toString());
		if(call.getName().equals(TYPE_STATUS)){ // Prolog format: status(position(1,4),30) 
			Cell c = null;
			if(call.getParams().get(0) instanceof APLFunction){ // null is APLIdent  
				APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
				int pointX = ((APLNum)point.getParams().get(0)).toInt(); // Get the position
				int pointY = ((APLNum)point.getParams().get(1)).toInt();
				c = new Cell(pointX,pointY);
			}
			Integer clock = null; // if health is null (which is ident) it stays also in java null
			if(call.getParams().get(1) instanceof APLNum) clock = ((APLNum)call.getParams().get(1)).toInt(); // The health meter

			return new Position(sAgent,c,clock); // Create Tuple
		}
		else if(call.getName().equals(TYPE_READINGREQ)){ // Prolog format: readingRequest(position(X,Y))
			Cell c = null;
			if(call.getParams().get(0) instanceof APLFunction){ // null is APLIdent  
				APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
				int pointX = ((APLNum)point.getParams().get(0)).toInt(); // Get the position
				int pointY = ((APLNum)point.getParams().get(1)).toInt();
				c = new Cell(pointX,pointY);
			}
			//System.out.print("from/for object " + sAgent + "  ");
			//System.out.println(call.toString());
			return new SwapRequest(); //TODO Create Tuple
		}
		else if(call.getName().equals(TYPE_READING)){ // Prolog format: reading(position(X,Y))
			Cell c = null;
			if(call.getParams().get(0) instanceof APLFunction){ // null is APLIdent  
				APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
				int pointX = ((APLNum)point.getParams().get(0)).toInt(); // Get the position
				int pointY = ((APLNum)point.getParams().get(1)).toInt();
				c = new Cell(pointX,pointY);
			}
			System.out.print("from/for object " + sAgent + "  ");
			System.out.println(call.toString());
			//System.out.println(new Chip(sAgent,c));
			return new Chip(sAgent); //TODO Create Tuple
		}
		else if(call.getName().equals(TYPE_COIN)){ // Prolog format: coin(position(X,Y),Clock,Agent)
			//System.out.println("create entry coin "+call.getParams().toString());
			Cell c = null;
			if(call.getParams().get(0) instanceof APLFunction){ // null is APLIdent  
				APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
				int pointX = ((APLNum)point.getParams().get(0)).toInt(); // Get the position
				int pointY = ((APLNum)point.getParams().get(1)).toInt();
				c = new Cell(pointX,pointY);
			}
			Integer clock = null; // if health is null (which is ident) it stays also in java null
			if(call.getParams().get(1) instanceof APLNum) clock = ((APLNum)call.getParams().get(1)).toInt(); // The health meter
			String agent = null; // if health is null (which is ident) it stays also in java null
			if(call.getParams().get(2) instanceof APLIdent) agent = ((APLIdent)call.getParams().get(2)).toString(); // The health meter

			return new Tile(c,agent,clock); // Create Tuple
		}
		else if(call.getName().equals(TYPE_CARGO)){ // Prolog format: cargo(position(X,Y),Clock)
			//System.out.println("create entry cargo "+call.getParams().toString());
			Cell c = null;
			if(call.getParams().get(0) instanceof APLFunction){ // null is APLIdent  
				APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
				int pointX = ((APLNum)point.getParams().get(0)).toInt(); // Get the position
				int pointY = ((APLNum)point.getParams().get(1)).toInt();
				c = new Cell(pointX,pointY);
			}
			Integer clock = null; // if health is null (which is ident) it stays also in java null
			if(call.getParams().get(1) instanceof APLNum) clock = ((APLNum)call.getParams().get(1)).toInt(); // The health meter

			return new tuplespace.Goal(); //TODO Create Tuple
		} 
		else if(call.getName().equals(TYPE_POINTS)){ //points(Agent,Now,NewHealth)
			System.out.println("create entry points "+call.getParams().toString());

			Integer clock = null; // if health is null (which is ident) it stays also in java null
			if(call.getParams().get(1) instanceof APLNum) clock = ((APLNum)call.getParams().get(1)).toInt(); // The health meter
			Integer health = null; // if health is null (which is ident) it stays also in java null
			if(call.getParams().get(2) instanceof APLNum) health = ((APLNum)call.getParams().get(2)).toInt(); // The health meter

			return new Points(sAgent); // Create Tuple
		}
		else if(call.getName().equals(TYPE_PROHIBITION)){ // Prolog format: status(position(1,4),30) 
			Prohibition p = null;
			//System.out.println("create entry prohibition "+call.getParams().toString());


			if(call.getParams().get(0) instanceof Term){ // null is APLIdent  
				//APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations 
				String s1 = call.getParams().get(0).toString();// Get the position
				String s2 = call.getParams().get(1).toString();
				p = new Prohibition(sAgent, s1, s2, envCT.clock);
			}
			//Integer health = null; // if health is null (which is ident) it stays also in java null
			//if(call.getParams().get(1) instanceof APLNum) health = ((APLNum)call.getParams().get(1)).toInt(); // The health meter
			//System.out.println(call.toString());
			//System.out.println(p.toString());
			return p; // Create Tuple
		} 
		else if(call.getName().equals(TYPE_OBLIGATION)){ // Prolog format: status(position(1,4),30) 
			Obligation o = null;
			//System.out.println("create entry obligation "+call.getParams().toString());


			if(call.getParams().get(0) instanceof Term){ // null is APLIdent  
				//APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
				String s1 = call.getParams().get(0).toString();// Get the position
				//String s2 = call.getParams().get(1).toString();
				String s3 = call.getParams().get(2).toString();

				int deadline = ((APLNum)call.getParams().get(1)).toInt();

				o = new Obligation(sAgent, s1, s3, deadline, envCT.clock);
				//System.out.println(s2);
			}
			return o; // Create Tuple
		} 
		else if(call.getName().equals(TYPE_SANCTION)){ // Prolog format: status(position(1,4),30) 
			Sanction s = null;
			//System.out.println("create entry obligation "+call.getParams().toString());


			if(call.getParams().get(0) instanceof Term){ // null is APLIdent  
				//APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
				String s1 = call.getParams().get(0).toString();// Get the position
				//String s2 = call.getParams().get(1).toString();
				//String s3 = call.getParams().get(2).toString();

				int value = ((APLNum)call.getParams().get(1)).toInt();

				s = new Sanction(sAgent, value, envCT.clock);
				//System.out.println(s2);
			}
			return s; // Create Tuple
		}
		else if(call.getName().equals(TYPE_GROUPCOIN)){ // Prolog format: status(position(1,4),30) 
			GroupCoin s = null;
			//System.out.println("create entry obligation "+call.getParams().toString());


			if(call.getParams().get(0) instanceof Term){ // null is APLIdent  
				//APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
				String s1 = call.getParams().get(0).toString();// Get the position
				//String s2 = call.getParams().get(1).toString();
				//String s3 = call.getParams().get(2).toString();

				int value = ((APLNum)call.getParams().get(1)).toInt();

				s = new GroupCoin(15, 15); //@TODO remove the hack!!!!
				System.out.println(s);
			}
			return s; // Create Tuple
		}

		return null;
	}

	//object use
	//Possibly move to Tuple classes
	public Term entryToTerm(TimeEntry timeEntry){ 

		if(timeEntry instanceof Points){ // points(Points)
			Points points = (Points) timeEntry; 
			return new APLFunction("points", new Term[]{new APLNum(points.value)}); // construct result
		} 
		else if(timeEntry instanceof Time){ // clock(Clock)
			Time time = (Time) timeEntry; 
			return new APLFunction("clock", new Term[]{new APLNum(time.clock)}); // construct result
		} 
		else if(timeEntry instanceof Chip){ // reading(at(X,Y),Value,Agent,Clock)
			Chip reading = (Chip) timeEntry;
			//Term term = constructTerm("at("+reading.cell.x+","+reading.cell.y+")");
			return new APLFunction("reading", new Term[]{new APLNum(reading.number.intValue()),new APLIdent(reading.agent),new APLNum(reading.clock)}); // construct result
		} 
		else if(timeEntry instanceof Obligation){ //obligation(Goal, Deadline, Sanction)
			Obligation o = (Obligation) timeEntry; 
			String name = o.agent;
			if(name==null)name="null"; 
			Term posTerm = new APLIdent("null");
			Term posTerm1 = new APLIdent("null");
			Term posTerm2 = new APLIdent("null");
			//all possible obligations

			posTerm = constructTerm(o.obligation);

			if(o.deadline!=null){
				posTerm1 = new APLNum(o.deadline);
			}
			if(o.sanction!=null){
				posTerm2 = constructTerm(o.sanction);
			}
			return new APLFunction("obligation", new Term[]{new APLList(posTerm),posTerm1,new APLList(posTerm2)});
		}
		else if(timeEntry instanceof Prohibition){ //prohibition(State,Sanction)
			Prohibition o = (Prohibition) timeEntry; 
			String name = o.agent;
			if(name==null)name="null"; 
			Term posTerm = new APLIdent("null");
			Term posTerm2 = new APLIdent("null");

			posTerm = constructTerm(o.prohibition);

			if(o.sanction!=null){
				posTerm2 = constructTerm(o.sanction);
			}
			return new APLFunction("prohibition", new Term[]{new APLList(posTerm),new APLList(posTerm2)});
		}
		else if(timeEntry instanceof Message){ //message
			Message m = (Message) timeEntry;
			String type = m.type;
			String response;
			System.out.println("[MSG] Message is of type: " + type);

			APLFunction event;

			if (type.equals("response")) {
				Boolean accepted = m.accepted;
				if (accepted) {response = "accept";}
				else {response = "reject";}

				event = new APLFunction("message",
						new APLIdent(type), new APLNum(m.id),
						new APLIdent(response));
			}

			else {
				event = new APLFunction("message",
						new APLIdent(type), new APLNum(m.id));
			}
			return event;
		}

		return new APLIdent("null");
	}

	public Term constructTerm(String term) {
		term = term.replace("[","");
		term.replace("]","");

		int tx = term.indexOf("(");
		String s = term.substring(0, tx).trim();

		Term[] t = new Term[10];
		int i = term.indexOf(",");
		int index = 0;
		if (i == -1) {
			return new APLFunction(term);
		}
		else {
			String x = term.substring(s.length() + 1, i).trim();
			t[index] = numOrIdent(x);
			index++;
		}
		while (term.indexOf(",", i+1) > 0) {
			int j = term.indexOf(",", i+1);
			String y = term.substring(i+1, j).trim();
			t[index] = numOrIdent(y);
			i=j;
			index++;
		}
		int j = term.indexOf(")");
		String y = term.substring(i+1, j).trim();
		t[index] = numOrIdent(y);
		Term posTerm = new APLFunction(s, t);
		return posTerm;

	}


	public Term numOrIdent(String x) {
		Term xt;
		Integer ix = Integer.getInteger(x);
		if (ix != null) {
			xt = new APLNum(ix);
		}
		else {
			xt = new APLIdent(x);
		}
		return xt;
	}
}
