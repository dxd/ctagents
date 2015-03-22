package blockworld;

/*
 3APL Blockworld program by Jelle Herold, copyright 2003.
 Written for the Intelligent Systems group at Utrecht University.
 This is LGPL software.

 $Id: Agent.java,v 1.6 2004/09/14 10:58:06 cvs-3apl Exp $
 */

import java.awt.Point;

import blockworld.lib.Signal;

// / Agent representation in Env. This doubles as a plugin instance.
public class Agent 
{
	protected String _name;

	/**
	 * _position is null means object did not enter the world. This method is
	 * only for the blockworld package to prevent setting the position by other
	 * means that through the north/south/west/east methods of the environment.
	 * \todo is protection "package" correct?
	 */
	protected Point _position = null;

	protected TypeObject _bomb = null;
	
	int _colorID = 0;

	public Agent( String name ) {
		_name = name;
	}

	public String getName() {
		return _name;
	}

	/**
	 * Get current object position. \returns This will return null if the object
	 * is not entered in the world.
	 */
	public Point getPosition() {
		return _position;
	}

	// / Sense if object is carrying a bomb.
	// / \return True if object is carrying a bomb, false otherwise.
	public TypeObject senseBomb() {
		return _bomb;
	}
	public TypeObject senseBomb(String type) {
		return (_bomb != null && _bomb.getType().equals(type))?_bomb:null;
	}

	public boolean atCapacity() {
		return _bomb != null;
	}

	public void pickBomb(TypeObject bomb) {
		_bomb = bomb;
	}

	public void dropBomb() {
		_bomb = null;
	}

	/**
	 * Check if object is "entered" in the environment. That is, it has a
	 * position in the world. \returns true if object is entered in the
	 * environment
	 */
	public boolean isEntered() {
		return (_position != null);
	}

	/**
	 * Called by the interpreter when the object this instance refers to is
	 * reset. \todo signalMove show become special signal enter/exit
	 */
	public void reset() {
		_position = null;
		_bomb = null; // Fixed bug pointed out by Bas of UU - Sohan
		signalMove.emit();
	}

	/**
	 * returns the unique name of the object this instance refers to.
	 */
	public String toString() {
		return getName();
	}

	// / removes all listeners
	public void deleteObservers() {
		signalMove.deleteObservers();
		signalPickupBomb.deleteObservers();
		signalDropBomb.deleteObservers();

		signalMoveSucces.deleteObservers();
		signalPickupBombSucces.deleteObservers();
		signalDropBombSucces.deleteObservers();
	}

	// / emitted if object attemps movement (succesful or not)
	public transient Signal signalMove = new Signal( "object attempts move" );

	// / emitted if object attemps to pickup a bomb (succesful or not)
	public transient Signal signalPickupBomb = new Signal(
			"object attempts pickup" );

	// / emitted if object attemps to drop a bomb (succesful or not)
	public transient Signal signalDropBomb = new Signal( "object attempts drop" );

	// / emitted if object succesfully moves
	public transient Signal signalMoveSucces = new Signal(
			"object succesful move" );

	// / emitted if object (succesfully) picks up a bomb
	public transient Signal signalPickupBombSucces = new Signal(
			"object succesful pickup" );

	// / emitted if object (succesfully) drops a bomb
	public transient Signal signalDropBombSucces = new Signal(
			"object sucessful drop" );
}
