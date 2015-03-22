 package blockworld;

/*
 3APL Blockworld program by Jelle Herold, copyright 2003.
 Written for the Intelligent Systems group at Utrecht University.
 This is LGPL software.

 $Id: Env.java,v 1.7 2004/12/27 02:22:41 cvs-3apl Exp $
 */

// 2APL imports
import apapl.Environment;
import apapl.ExternalActionFailedException;
import apapl.data.*;

// Standard java imports
import java.awt.Point;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.LinkedList;
import javax.swing.SwingUtilities;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.transaction.TransactionException;

import eis.exceptions.EntityException;
import eis.exceptions.ManagementException;
import eis.exceptions.NoEnvironmentException;
import eis.iilang.EnvironmentCommand;
import eis.iilang.Function;
import eis.iilang.Numeral;
import eis.iilang.Parameter;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import blockworld.lib.ObsVectListener;
import blockworld.lib.Signal;
import blockworld.lib.ObsVect;

import tuplespace_old.*;



public class Env extends Environment implements ObsVectListener
{


	// To hold our reference to the window
	final protected Window 					m_window;
	
	// max distance of cells visible to each object
	protected int							_senserange = 5;
	
	// size of environment
	protected Dimension 					m_size = new Dimension( 16, 16 );
	
	private HashMap<String,Agent> 			agentmap = new HashMap<String,Agent>();
	
	/* ---- ALL the stuff in the environment -----*/
	
	// list of agents (Agent)
	protected ObsVect 						_agents = new ObsVect( this );
	// location of bomb traps
	protected ObsVect 						_traps = new ObsVect();
	// list of coordinates (Point) containing stones
	protected ObsVect 						_stones = new ObsVect();
	// list of coordinates (Point) containing bombs
	protected ObsVect 						_bombs = new ObsVect();
	// id for identifiable objects
	protected String 						_objType = "default";	
	
	/* ------------------------------------------*/

    protected Service						service;
	/* ---- SIGNALS -------------------------------*/
	// / emitted on collection of a bomb in the bomb trap
	public transient Signal signalBombTrapped = new Signal( "env bomb trapped" );
	
	public transient Signal signalSenseRangeChanged = new Signal(
			"env sense range changed" );

	// emitted if environment is resized
	public transient Signal signalSizeChanged = new Signal( "env size changed" );

	// emitted if bomb traps location changed
	public transient Signal signalTrapChanged = new Signal(
			"env trap position changed" );
	
	/* ------------------------------------------*/	

	
	// The default constructor
	public Env()
	{
		super();
		// Create the window
		m_window = new Window( this );
		service = new Service();
		try {
			service.initialize();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnusableEntryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	}

	/* Called from 2APL */
	
	// Enter the object into the world
	// Succesful returns true, else the ExternalActionFailedException exception is thrown
	public Term enter( String sAgent, APLNum xt, APLNum yt, APLIdent colort) throws ExternalActionFailedException
	{
		int x = xt.toInt();
		int y = yt.toInt();
		String color = colort.toString();
		
		// Get the object
		Agent agent = getAgent(sAgent);
		
		// Give a signal that we want to move
		agent.signalMove.emit();
		
		writeToLog( "Agent entered: " +agent.getName());
		
		Point position = new Point(x,y);
		String pos = "("+x+","+y+")";
		
		// Agent already entered
		if( agent.isEntered() ) 
		{
			writeToLog( "object already entered" );
			throw new ExternalActionFailedException("Agent \""+agent.getName()+"\" has already entered.");
		}
		
		// Is this position within the world?
		if (isOutOfBounds(position)) 
		{
			throw new ExternalActionFailedException("Position "+pos+" out of bounds.");
		}
		// Is this position free?
		if( !isFree( position ) )
		{
			throw new ExternalActionFailedException("Position "+pos+" is not free.");
		}
		
		// Update the object his position
		agent._position = position;
		
		// Which color does the object want to be
		int nColorID = getColorID(color);
		agent._colorID = nColorID;
	
		// Redraw so we can see the object
		validatewindow();
		m_window.repaint();
		
		// We came so far, this means success!
		agent.signalMoveSucces.emit();

		return wrapBoolean(true);
	}
	
	// Move the object north
	public Term north(String sAgent) throws ExternalActionFailedException
	{
		// Get the correct object
		Agent agent = getAgent(sAgent);
		
		// Get the object his position
		Point p = (Point) agent.getPosition().clone();
		p.y = p.y - 1;
		
		// Set the position for the object
		boolean  r = setAgentPosition( agent, p);
		
		// can't move north
		if (!r) throw new ExternalActionFailedException("Moving north failed.");

		// Redraw the window
		validatewindow();
		m_window.repaint();
		return wrapBoolean(r);
	}
	
	// Move the object east
	public Term east(String sAgent) throws ExternalActionFailedException
	{
		// Get the correct object
		Agent agent = getAgent(sAgent);
		
		// Get the object his position
		Point p = (Point) agent.getPosition().clone();
		p.x = p.x + 1;
		
		// Set the position for the object
		boolean  r = setAgentPosition( agent, p);
		
		// can't move north
		if (!r) throw new ExternalActionFailedException("Moving north failed.");

		// Redraw the window
		validatewindow();
		m_window.repaint();
		return wrapBoolean(r);
	}
	
	// Move the object south
	public Term south(String sAgent) throws ExternalActionFailedException
	{
		// Get the correct object
		Agent agent = getAgent(sAgent);
		
		// Get the object his position
		Point p = (Point) agent.getPosition().clone();
		p.y = p.y + 1;
		
		// Set the position for the object
		boolean  r = setAgentPosition( agent, p);
		
		// can't move north
		if (!r) throw new ExternalActionFailedException("Moving north failed.");

		// Redraw the window
		validatewindow();
		m_window.repaint();
		return wrapBoolean(r);
	}
	
	// Move the object west
	public Term west(String sAgent) throws ExternalActionFailedException
	{
		// Get the correct object
		Agent agent = getAgent(sAgent);
		
		// Get the object his position
		Point p = (Point) agent.getPosition().clone();
		p.x = p.x - 1;
		
		// Set the position for the object
		boolean  r = setAgentPosition( agent, p);
		
		// can't move north
		if (!r) throw new ExternalActionFailedException("Moving north failed.");

		// Redraw the window
		validatewindow();
		m_window.repaint();
		return wrapBoolean(r);
	}
	
	// Pickup a bomb
	public Term pickup( String sAgent ) throws ExternalActionFailedException
	{
		// Get the object
		Agent agent = getAgent(sAgent);
		
		// Let everyone know we are going to pick up a bomb	
		agent.signalPickupBomb.emit();

		// see if we are not already carrying a bomb
		if( agent.atCapacity() ) 
		{
			writeToLog( "Pickup bomb failed" );
			throw new ExternalActionFailedException("Pickup bomb failed");
		}

		// we are not already carying a bomb so get this one
		TypeObject bomb = removeBomb( agent.getPosition() );
		if( bomb == null ) 
		{
			writeToLog( "Pickup bomb failed" );
			throw new ExternalActionFailedException("Pickup bomb failed");
		}

		// Yes
		agent.signalPickupBombSucces.emit();

		// there was a bomb at that position, so set token
		agent.pickBomb(bomb);
		
		// show what happened
		validatewindow();
		m_window.repaint();
		
		return wrapBoolean(true);
	}
	
	// Drop a bomb
	public Term drop( String sAgent ) throws ExternalActionFailedException
	{
		// Get the object
		Agent agent = getAgent(sAgent);
		// we are going to drop a bomb
		agent.signalDropBomb.emit();

		TypeObject bomb = agent.senseBomb();
		// see if we are actually carrying a bomb
		if( bomb == null)
		{
			writeToLog( "Drop bomb failed" );
			throw new ExternalActionFailedException("Drop bomb failed");
		}
			
		Point pos = agent.getPosition();
		// see if we can drop that bomb here
		
		if( !addBomb(pos)	&& (isTrap( pos ) == null
			|| agent.senseBomb( isTrap( pos ).getType() ) == null) ) 
		{
			writeToLog( "Drop bomb failed" );
			throw new ExternalActionFailedException("Drop bomb failed");
		}
		
		if(isTrap( pos ) != null
			&& agent.senseBomb( isTrap( pos ).getType() ) != null) 
		{
			signalBombTrapped.emit();
		}

		// unset token
		agent.dropBomb();

		agent.signalDropBombSucces.emit();

		// Show it
		validatewindow();
		m_window.repaint();

		// return success
		return wrapBoolean(true);
	}
	
	// What is the object his Sense Range
	public Term getSenseRange(String agent)
	{
		// the below function is also used by EnvView
		int r = getSenseRange();
		return new APLList(new APLNum(r));
	}

	// Sense all agents. This does not include self.
	public synchronized Term senseAllAgent(String sAgent) throws ExternalActionFailedException
	{
		//Collection c = senseAllAgents(getAgent(object).getPosition());
		Point position = getAgent(sAgent).getPosition();
		// iterate over all agents
		Vector all = new Vector();

		Iterator j = _agents.iterator();
		while( j.hasNext() ) {
			Agent agent = (Agent) j.next();
			Point p = agent.getPosition();
			
			// Changed SA: when there are no other agents, this return null, which
			// causes Java to throw an exception and never return the empty list.
			if (p == null)
				continue;

			// skip self
			if( p.equals( position ) )
				continue;

			all.add( agent );
		}
		
		LinkedList<Term> listpar = new LinkedList<Term>();
		for(Object i : all) {
			final Agent a = (Agent) i;
			APLListVar tmp = new APLList(new APLIdent(a.getName()),new APLNum(a.getPosition().x),new APLNum(a.getPosition().y));
			listpar.add(tmp);
		}
		return new APLList(listpar);
	}
	
	// Sense the given object his position
	public synchronized Term sensePosition(String sAgent) throws ExternalActionFailedException
	{
		Point p = getAgent(sAgent).getPosition();
		return new APLList(new APLNum(p.x),new APLNum(p.y));
	}
	
	// is there a trap in the senserange of the object?
	public synchronized Term senseTraps(String agent) throws ExternalActionFailedException
	{
		// Get the object his position
		Point position = getAgent(agent).getPosition();
		
		// iterate over all traps and decide according to distance if it is in
		// vision range
		Vector visible = new Vector();

		Iterator i = _traps.iterator();
		while( i.hasNext() ) {
			TypeObject t = (TypeObject) i.next();
			if( position.distance( t.getPosition() ) <= _senserange )
				visible.add( t );
		}
		
		return convertCollectionToTerm(visible);
	}
	
	// Get all the traps in the env
	public synchronized Term senseAllTraps(String agent)
	{
		// iterate over all traps
		Vector all = new Vector();

		Iterator i = _traps.iterator();
		while( i.hasNext() ) 
		{
			all.add( (TypeObject) i.next() );
		}
		
		return convertCollectionToTerm(all);
	}
	
	// Sends a bom in the senserange of the object
	public synchronized Term senseBombs(String agent) throws ExternalActionFailedException, RemoteException, TransactionException
	{
		// Get the object his position
		Point position = getAgent(agent).getPosition();
		
		// iterate over all bombs and decide according to distance if it is in
		// vision range
		Vector visible = new Vector();

		Iterator i = _bombs.iterator();
		while( i.hasNext() ) 
		{
			TypeObject b = (TypeObject) i.next();
			if( position.distance( b.getPosition() ) <= _senserange ) {
				visible.add( b );
			service.addBomb(b.getPosition());
			}
				
		}
		
		return convertCollectionToTerm(visible);
	}
	
	// Find all the bombs in the environment
	public synchronized Term senseAllBombs (String agent)
	{
		// iterate over all bombs
		Vector all = new Vector();

		Iterator i = _bombs.iterator();
		while( i.hasNext() ) 
		{
			all.add( (TypeObject) i.next() );
		}
		
		return convertCollectionToTerm(all);
	}
	
	// Sense the stones in the object senserange
	public synchronized Term senseStones(String agent) throws ExternalActionFailedException
	{
		// Get the object his position
		Point position = getAgent(agent).getPosition();
		
		// iterate over all stones and decide according to distance if it is in
		// vision range
		Vector visible = new Vector();

		Iterator i = _stones.iterator();
		while( i.hasNext() ) 
		{
			TypeObject t = (TypeObject) i.next();
			if( position.distance( t.getPosition() ) <= _senserange )
				visible.add( t );
		}
		
		return convertCollectionToTerm(visible);
	}
	
	// Sense all stones
	public synchronized Term senseAllStones (String agent)
	{
		// iterate over all traps
		Vector all = new Vector();

		Iterator i = _stones.iterator();
		while( i.hasNext() ) 
		{
			all.add( (TypeObject) i.next() );
		}
		
		return convertCollectionToTerm(all);
	}
	
	// Sense visible area for agents. This does not include self.
	public synchronized Term senseAgent(String sAgent) throws ExternalActionFailedException
	{
		//Collection c = senseAgents(getAgent(object).getPosition());
		Point position = getAgent(sAgent).getPosition();
		
		// iterate over all agents and decide according to distance if it is in
		// vision range
		Vector visible = new Vector();
		
		Iterator j = _agents.iterator();
		
		while( j.hasNext() )
		{
			Agent agent = (Agent) j.next();

			Point p = agent.getPosition();
			
			// Changed SA: when there are no other agents, this return null, which
			// causes Java to throw an exception and never return the empty list.
			if (p == null)
				continue;
			
			// skip self
			if( p.equals( position ) )
				continue;

			// object within visible range
			if( position.distance( p ) <= _senserange )
				visible.add( agent );
		}
		
		LinkedList<Term> listpar = new LinkedList<Term>();
		for(Object i : visible) {
			final Agent a = (Agent) i;
			APLListVar tmp = new APLList(new APLIdent(a.getName()),new APLNum(a.getPosition().x),new APLNum(a.getPosition().y));
			listpar.add(tmp);
		}
		return new APLList(listpar);
	}


	
	/* Standard functions --------------------------------------*/
	
	private void notifyAgents(APLFunction event, String... receivers) {
		 throwEvent(event, receivers);
	}
	
	private void notifyEvent(String parm1, Point ptPosition)
	{
		APLNum	nX	= new APLNum((double)(ptPosition.getX()));
		APLNum	nY	= new APLNum((double)(ptPosition.getY()));

		// Send an external event to all agents within the senserange.
		ArrayList<String> targetAgents = new ArrayList<String>();
		for (Agent a : agentmap.values())
		{
			// Changed SA: I got no idea why there is always 1 object which does not exists, 
			// but this fixes the exceptions
			if ((a.getPosition() != null) && (ptPosition.distance(a.getPosition()) <= getSenseRange()))
				targetAgents.add(a.getName());
		}

		writeToLog("EVENT: "+parm1+"("+nX+","+nY+")"+" to "+targetAgents);

		if (!targetAgents.isEmpty())
		{
			notifyAgents(new APLFunction(parm1,nX,nY),targetAgents.toArray(new String[0]));
		}
	}
	
	// Add an object to the environment
    public synchronized void addAgent(String sAgent) {
        String sAgentMain = getMainModule(sAgent);
        // Agent not yet in the environment
        if (agentmap.keySet().contains(sAgentMain)) {
            agentmap.put(sAgent,agentmap.get(sAgentMain));  
            writeToLog("linking " + sAgent + "");
        } else{
            final Agent agent = new Agent(sAgentMain);
            _agents.add(agent);
            agentmap.put(sAgent, agent);
            writeToLog("object " + agent + " added");
        }                
    }
	
	// Remove the object from the environment
	public synchronized void removeAgent(String sAgent)
	{
		try 
		{
			//String sAgentMain = getMainModule(sAgent);
			
			Agent a = getAgent(sAgent);			
			agentmap.remove( sAgent );
			
			// there can be several object
			if (!agentmap.containsValue(a)) {
			    _agents.remove(a);		
			    a.reset();
			} 
			
			writeToLog("Agent removed: " + sAgent);
	
			synchronized( this ) 
			{
				notifyAll();
			}
		}
		catch (ExternalActionFailedException e) {}
	}
	
	/* END Standard functions --------------------------------------*/
	
	
	
	
	/* Helper functions --------------------------------------*/
	
	// Get the size of the blockworld
	public synchronized Term getWorldSize(String agent)
	{
		int w = getWidth();
		int h = getHeight();
		return new APLList(new APLNum(w),new APLNum(h));
	}
	
	// Get the object from its name
	private synchronized Agent getAgent(String name) throws ExternalActionFailedException
	{    
		Agent a = null;
		//a = agentmap.get(getMainModule(name));
		a = agentmap.get(name);
		if (a==null) throw new ExternalActionFailedException("No such object: "+name);
		else return a;
		
	}
	
	private static String getMainModule(String sAgent)
	{
		int dotPos;
		if ((dotPos = sAgent.indexOf('.')) == -1)
			return sAgent;
		else
			return sAgent.substring(0, dotPos);
	}
	
	// Get the environment width
	public synchronized int getWidth() {	return m_size.width; }

	// Get the environment height
	public synchronized int getHeight() { return m_size.height; }
	
	// Return the agents
	public synchronized Collection getBlockWorldAgents() 
	{
		return new Vector(_agents);
	}
	
	// convert a collection to a term
	private static Term convertCollectionToTerm(Collection c)
	{
		LinkedList<Term> listpar = new LinkedList<Term>();
		for(Object i : c) {
			final TypeObject o = (TypeObject) i;
			APLListVar tmp = new APLList(new APLIdent(o.getType()),new APLNum(o.getPosition().x),new APLNum(o.getPosition().y));
			listpar.add(tmp);
		}
		return  new APLList(listpar);
	}
	
	// Get senserange
	public int getSenseRange() 
	{
		return _senserange;
	}
	
	// Redrawing the window is a nightmare, this does some redraw stuff
	private void validatewindow()
	{
		Runnable repaint = new Runnable()
		{
			public void run()
			{
				//try {Thread.sleep(500);} catch(Exception e) {}
				m_window.doLayout();
				
				/*if (!m_window.isVisible())
				{
					m_window.setVisible( true );
				}*/
			}
		};
		SwingUtilities.invokeLater(repaint);
	}
	
	// Move the object
	private synchronized boolean setAgentPosition( Agent agent, Point position) 
	{
		agent.signalMove.emit();

		if( isOutOfBounds( position ) )
			return false;

		// suspend thread if some other object is blocking our entrance
		
		// Is the position free?
		if( !isFree( position ) )
			return false;

		agent.signalMoveSucces.emit();

		// there may be other threads blocked because this object was in the way,
		// notify
		// them of the changed state of environment
		synchronized( this ) 
		{
			notifyAll();
		}

		// set the object position
		agent._position = position;
		try {
			service.writePosition(agent.getName(), position);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnusableEntryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	// check if point is within environment boundaries
	// return false is p is within bounds
	protected boolean isOutOfBounds( Point p ) 
	{
		if( (p.x >= m_size.getWidth()) || (p.x < 0) || (p.y >= m_size.getHeight()) || (p.y < 0) )
		{
			return true;
		}
		
		return false;
	}
	
	// Is the position free?
	public synchronized boolean isFree( final Point position ) 
	{
		return (isStone( position )) == null && (isAgent( position ) == null);
	}
	
	 // Check for object at coordinate. \return Null if there is no object at the
	 // specified coordinate. Otherwise return a reference to the object there.
	public synchronized Agent isAgent( final Point p ) 
	{
	    synchronized (_agents) {
    		Iterator i = _agents.iterator();
    		while( i.hasNext() ) {
    			final Agent agent = (Agent) i.next();
    			if( p.equals( agent.getPosition() ) )
    				return agent;
    		}
    		return null;
	    }
	}
	
	// Is there a stone at this point
	public synchronized TypeObject isStone( Point p ) 
	{
	    synchronized (_agents) {
    		Iterator i = _stones.iterator();
    		while( i.hasNext() ) {
    			TypeObject stones = (TypeObject) i.next();
    			if( p.equals( stones.getPosition() ) )
    				return stones;
    		}
    		return null;
	    }
	}
	
	// see if there is a trap at the specified coordinate
	public synchronized TypeObject isTrap( Point p ) {
	    synchronized (_traps) {
    	    Iterator i = _traps.iterator();
    		while( i.hasNext() ) {
    			TypeObject trap = (TypeObject) i.next();
    			if( p.equals( trap.getPosition() ) )
    				return trap;
    		}
    		return null;
	    }
	}
	
	public synchronized TypeObject isBomb( Point p ) 
	{
	    synchronized (_bombs) {
    	    Iterator i = _bombs.iterator();
    		while( i.hasNext() ) {
    			TypeObject bomb = (TypeObject)i.next();
    			if(p.equals(bomb.getPosition())) 
    			    return bomb;
    		}
    		return null;
	    }
	}
	
	// Remove bomb at position TODO Jaap; why is this different then remove stone???
	public synchronized TypeObject removeBomb( Point position )
	{
		// find bomb in bombs list
	    synchronized(this) {
	        Iterator i = _bombs.iterator();
    		while (i.hasNext())
    		{
    			TypeObject bomb = (TypeObject) i.next();
    			if (position.equals(bomb.getPosition()))
    			{
    				i.remove();	
    				service.removeBomb(position);
    				return bomb;
    			}
    		}
	    }
		
		notifyEvent("bombRemovedAt", position);
		return null;
		
	}
	
	// remove stone at position
	public synchronized boolean removeStone( Point position )
	{
		synchronized(_stones) {
    	    // find stone in stones list
    		Iterator i = _stones.iterator();
    		while (i.hasNext())
    			//if( position.equals( i.next() ) ) {
    			// Changed SA:
    			if (position.equals(((TypeObject)i.next()).getPosition()))
    			{
    				i.remove();				
    
    				// there may be other threads blocked because this object was in
    				// the way, notify
    				// them of the changed state of environment
    				synchronized( this ) 
    				{
    					notifyAll();
    				}
    
    				return true;
    			}
		}
		notifyEvent("wallRemovedAt", position);
		return false;
	}
	
	// remove trap at position
	public synchronized boolean removeTrap( Point position )
	{
	    synchronized(_traps) {
		// find trap in traps list
    		Iterator i = _traps.iterator();
    		while (i.hasNext()) {
    			if (position.equals(((TypeObject)i.next()).getPosition()))
    			{
    				i.remove();    
    				
    
    				// Sohan: I believe this notification is unnecessary, commented it out:
    				//synchronized( this ) {
    				//	notifyAll();
    				//}
    
    				return true;
    			}
    	    }
	    }
	    
	    notifyEvent("trapRemovedAt", position);
		return false;
	}
	
	// Add a stone at the given position
	public synchronized boolean addStone( Point position ) throws IndexOutOfBoundsException 
	{
		// valid coordinate
		if( isOutOfBounds( position ) )
			throw new IndexOutOfBoundsException( "setStone out of range: "
					+ position + ", " + m_size );

		// is position clear of other stuff
		// Changed SA:
		if( isBomb( position ) != null || isStone( position ) != null ||  isTrap( position ) != null )
			return false;
		
		synchronized (_stones) {
		    _stones.add( new TypeObject(_objType,position) );
		}
		notifyEvent("wallAt", position);

		return true;
	}
	
	// Add a bomb to the environment
	public synchronized boolean addBomb( Point position ) throws IndexOutOfBoundsException
	{
		if( isOutOfBounds( position ) )
			throw new IndexOutOfBoundsException( "addBomb outOfBounds: "
					+ position + ", " + m_size );

		// is position clear of other stuff
		if( isBomb( position ) != null ||  isStone( position ) != null ||  isTrap( position ) != null )
			return false;

		// all clear, accept bomb
		synchronized (_bombs) {
		    _bombs.add( new TypeObject(_objType,position) );
		}
		notifyEvent("bombAt", position);

		return true;
	}
	
	// Add a trap at the given position
	public synchronized boolean addTrap( Point position ) throws IndexOutOfBoundsException {
		// valid coordinate
		if( isOutOfBounds( position ) )
			throw new IndexOutOfBoundsException( "setTrap out of range: "
					+ position + ", " + m_size );

		// is position clear of other stuff
		// Changed SA:
		if( isBomb( position ) != null ||  isStone( position ) != null ||  isTrap( position ) != null )
			return false;
		synchronized(_traps) {
		    _traps.add( new TypeObject(_objType,position) );
		}
		notifyEvent("trapAt", position);

		return true;
	}
	
	// Print a message to the console
    static public void writeToLog(String message) {
      //System.out.println("blockworld: " + message);
    }
	
	// helper function to wrap a boolean value inside a ListPar.
	static public APLListVar wrapBoolean( boolean b )
	{
		return new APLList(new APLIdent(b ? "true" : "false"));
	}
	
	// Which color does the object want to be!
	private int getColorID(String sColor)
	{
		if (sColor.equals("army") )
		{
			return 0;
		}
		else if (sColor.equals("blue") )
		{
			return 1;
		}
		else if (sColor.equals("gray") )
		{
			return 2;
		}
		else if (sColor.equals("green") )
		{
			return 3;
		}
		else if (sColor.equals("orange") )
		{
			return 4;
		}
		else if (sColor.equals("pink") )
		{
			return 5;
		}
		else if (sColor.equals("purple") )
		{
			return 6;
		}
		else if (sColor.equals("red") )
		{
			return 7;
		}
		else if (sColor.equals("teal") )
		{
			return 8;
		}
		else if (sColor.equals("yellow") )
		{
			return 9;
		}
		
		// Red is the default
		return 7;
	}
	
	// Set the senserange
	public void setSenseRange( int senserange ) 
	{
		_senserange = senserange;
		signalSenseRangeChanged.emit();
	}

	// helper function, calls setSize(Dimension)
	public void setSize( int width, int height ) 
	{
		setSize( new Dimension( width, height ) );
	}
	
	// resize world
	public void setSize( Dimension size ) 
	{
		m_size = size;
		signalSizeChanged.emit();

		Iterator i = _bombs.iterator();
		while( i.hasNext() ) {
			if( isOutOfBounds( ((TypeObject) i.next()).getPosition() ) )
				i.remove();
		}
		i = _stones.iterator();
		while( i.hasNext() ) {
			if( isOutOfBounds( (Point) i.next() ) )
				i.remove();
		}
		i = _traps.iterator();
		while( i.hasNext() ) {
			if( isOutOfBounds( ((TypeObject) i.next()).getPosition() ) )
				i.remove();
		}
	}
	
	// what kind of object is it, bomb, stone, wall ?
	public String getObjType() 
	{
		return _objType;
	}

	// what kind of object is it, bomb, stone, wall ?
	public void setObjType(String objType) 
	{
		_objType = objType;
	}

	// Remove everything
	public void clear() 
	{
		_stones.removeAllElements();
		_bombs.removeAllElements();
		_traps.removeAllElements();
	}
	
	// Save the environment
	public void save( OutputStream destination ) throws IOException 
	{
		ObjectOutputStream stream = new ObjectOutputStream( destination );
		stream.writeObject( m_size );

		stream.writeInt( _senserange );

		stream.writeObject( (Vector) _stones );
		stream.writeObject( (Vector) _bombs );
		stream.writeObject( (Vector) _traps );
		stream.flush();
	}

	// Load the environment
	public void load( InputStream source ) throws IOException, ClassNotFoundException 
	{
		ObjectInputStream stream = new ObjectInputStream( source );
		Dimension size = (Dimension) stream.readObject();

		int senserange = stream.readInt();

		Vector stones = (Vector) stream.readObject();
		Vector bombs = (Vector) stream.readObject();
		Vector traps = (Vector) stream.readObject();

		// delay assignments until complete load is succesfull
		m_size = size;
		_senserange = senserange;

		signalSizeChanged.emit();
		signalTrapChanged.emit();
		signalSenseRangeChanged.emit();

		clear();

		_stones.addAll( stones );
		_bombs.addAll( bombs );
		_traps.addAll( traps );
	}
	
	/* END Helper functions --------------------------------------*/
	
	/* Listeners ------------------------------------------------*/
	
	// / This listener is notified upon changes regarding the Agent list.
	// / Please note that this only involves registering new agents or
	// / removing existing agents. To track object position changes, add
	// / a listener to that specific object.
	// / \sa Agent
	public void addAgentListener( ObsVectListener o ) 
	{
		_agents.addListener( o );
	}

	// / This listener is notified upon changes regarding the Stones list.
	public void addStonesListener( ObsVectListener o ) 
	{
		_stones.addListener( o );
	}

	// / This listener is notified upon changes regarding the Bombs list.
	public void addBombsListener( ObsVectListener o ) 
	{
		_bombs.addListener( o );
	}

	// / This listener is notified upon changes regarding the Traps list.
	public void addTrapsListener( ObsVectListener o ) 
	{
		_traps.addListener( o );
	}
	
	/* END Liseteners ------------------------------------------*/
	

	/* Overrides for ObsVector ---------------------------------*/
	
	public void onAdd( int index, Object o ) {}

	public void onRemove( int index, Object o ) 
	{
		((Agent) o).deleteObservers();
	}
	
	/* END Overrides for ObsVector ---------------------------------*/
	
	/** EIS Environment Management  **/
	
	@Override
    public void manageEnvironment(EnvironmentCommand cmd)
       throws ManagementException, NoEnvironmentException {
       if (cmd.getType() == EnvironmentCommand.INIT) {
           // Initializing the Environment
           int width = 10;
           int height = 10;
           int robots = 0;
           
           for(Parameter param : cmd.getParameters()) {
               if (param instanceof Function) {
                   if (((Function) param).getName().equals("gridWidth")) {
                      width = ((Numeral)((Function) param).getParameters().getFirst()).getValue().intValue();
                   }
                   
                   if (((Function) param).getName().equals("gridHeight")) {
                       height = ((Numeral)((Function) param).getParameters().getFirst()).getValue().intValue();
                    }  
                   
                   if (((Function) param).getName().equals("entities")) {
                       robots = ((Numeral)((Function) param).getParameters().getFirst()).getValue().intValue();
                    }                     
                   
               }
           }
           
           setSize(width, height);
           
           if (robots > 0) {
                for (int i = 0; i <= robots; i++) {
                    addAgent("robot" + i);
                    try {
                        addEntity("robot" + i);
                    } catch (EntityException e) {
                        e.printStackTrace();
                    }
                }
            }
       }
    }
	
    public void addAgentEntity(String agent) {
        // If the number of entities has been specified in the MAS file, 
        // bypass 2APL specific automatic object-entity attaching mechanism.
        if (agentmap.containsKey("robot0")) return;
        
        super.addAgentEntity(agent);
    }

}
