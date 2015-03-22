/*
	Colored Trails
	
	Copyright (C) 2006-2008, President and Fellows of Harvard College.
	All Rights Reserved.
	
	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package edu.harvard.eecs.airg.coloredtrails.shared.types;

import java.io.Serializable;

/**
 * This class implements a single game phase; properties of a game phase are
 * its name and duration; the game phase may have indefinite duration, which
 * is useful for tutorial phases or synchronization points. The time still
 * counts elapsed time even when game phase is indefinite.
 
   @author Sevan G. Ficici (modifications for partial visibility)
 */
public class GamePhase extends CTStateContainer
                       implements Serializable, Cloneable
{
	/**
	 * the phase's name
	 */
//	private final String name;
	/**
	 * the phase's duration in seconds
	 */
//	private final int duration;
	/**
	 * flag indicating whether this game phase has indefinite length
	 */
//	private final boolean indefinite;

	/**
	 * Private constructor that sets all the private values.
	 * @param name the phase's name
	 * @param duration the phase's duration in seconds
	 * @param indefinite whether the phase has indefinite duration.
	 */
	private GamePhase( String name, int duration, boolean indefinite )
	{
		super("GamePhase", "Server");
		// phase's name
		set("name", name);
		// phase's duration in seconds
		set("duration", duration);
		// flag indicating whether this game phase has indefinite length
		set("indefinite", indefinite);
	}

	/**
	 * Constructor
	 *
	 * @param name     the phase's name
	 * @param duration the phase's duration in seconds
	 */
	public GamePhase( String name, int duration )
	{
		this( name, duration, false );
	}

	/**
	 * Constructor for GamePhase with an indefinite duration
	 *
	 * @param name the phase's name
	 */
	public GamePhase( String name )
	{
		this( name, -1, true );
	}

	/**
	 * Copy constructor
	 * @param g The GamePhase to be coppied.
	 */
	public GamePhase( GamePhase g )
	{
		super(g);
	}
	
	public Object clone()
	{
		return new GamePhase(this);
	}

	/**
	 * Gets the name of the phase
	 *
	 * @return Name of the phase
	 */
	public String getName()
	{
		return (String)get("name");
	}

	/**
	 * Gets the duration of the phase
	 *
	 * @return Duration of the phase
	 */
	public int getDuration()
	{
		return (Integer)get("duration");
	}

	/**
	 * Returns true if this game phase has indefinite length
	 *
	 * @return true if this is a indefinite phase.
	 */
	public boolean isIndefinite()
	{
		return (Boolean)get("indefinite");
	}
}
