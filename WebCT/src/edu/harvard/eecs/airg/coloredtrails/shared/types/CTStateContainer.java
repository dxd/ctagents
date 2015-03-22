// MODIFIED/ADDED FILES: CTStateContainer, Goal, GameConfigDetailsRunnable, ClientCommands, Square, Board, ChipSet, PlayerStatus, GameStatus, ServerGameStatus, BestUse

// convert Phases also? if yes, then need to check about use of Timer -- not static!

/*
	Colored Trails
	
	Copyright (C) 2006-2008, President and Fellows of Harvard College.  All Rights Reserved.
	
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

import java.util.Set;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Formatter;
import java.util.Collection;
import java.io.Serializable;


/**
	This class is a general container for state information.  
	Subclasses can be made to represent specific data types, such
	as the game board, player status, and so on.
	
	The proposal is to entirely rebuild the representation framework
	used in CT such that we can seemlessly move from one representation
	to another as information flows from within the server to the
	clients.
	
	The server will use a fully-informed representation, which will
	allow it to enforce policies.  When the server needs to update
	a client about game state, the server will first pass the relevant
	state information through an experimenter-defined filter that may
	re-encode the game-state information.  This new encoding, however,
	will still use the same representation framework (i.e., CTStateContainer).
	
	@author Sevan G. Ficici
*/
public class CTStateContainer extends Observable
                              implements Serializable, Cloneable
{
	/** the type of data being stored in this object, e.g., board */
	private String type;
	/** the encodings of the data found here, e.g., "server internal";
		this information helps something examining this object determine
		whether it knows how to understand the data stored here; we 
		allow more than one encoding to be used simultaneously */
	private HashSet<String> encodings;	
	/** the data itself, in feature/value pairs */
	private Hashtable<String, Object> data;
	
	/** a substitute for null that will allow us to define keys that
	    map to null values and avoid Hashtable exceptions **/
	private static final String NULL_VALUE = "CTStateContainer.NULL_VALUE";
	
	
	/**
		Constructor
	*/
	public CTStateContainer()
	{
		type = "";
		encodings = new HashSet<String>();
		data = new Hashtable<String, Object>();
	}
	
	
	/**
		Constructor
		
		@param type		the type of data being placed here
		@param encoding	the encoding of the data being used
	*/
	public CTStateContainer(String type, String encoding)
	{
		this();
		this.type = type;
		encodings.add(encoding);
	}
	
	
	/**
		Copy constructor
	*/
	public CTStateContainer(CTStateContainer c)
	{
		this(c, null);
/*		
		this();
		
		this.type = c.type;
		// SHALLOW COPY -- not copying String contents
		this.encodings = (HashSet<String>)c.encodings.clone();
		// DEEP COPY of values (not keys) if they are also CTStateContainer
		for (String key : c.data.keySet())
		{
			Object value = c.data.get(key);
			if (value instanceof CTStateContainer)
				this.data.put(key, ((CTStateContainer)value).clone());
//				this.data.put(key, value.clone());
			else
				this.data.put(key, value);
		}
*/
	}
	
	
	/**
	 * 	Copy constructor that allows you to specify features to be
	 *  excluded from the copy
	 */
	public CTStateContainer(CTStateContainer c, Collection excludefeatures)
	{
		this();
		
		this.type = c.type;
		// SHALLOW COPY -- not copying String contents
		this.encodings = (HashSet<String>)c.encodings.clone();
		// DEEP COPY of values (not keys) if they are also CTStateContainer
		for (String key : c.data.keySet())
		{
			if (excludefeatures != null && excludefeatures.contains(key))
				continue;
			Object value = c.data.get(key);
			if (value instanceof CTStateContainer)
				this.data.put(key, ((CTStateContainer)value).clone());
//				this.data.put(key, value.clone());
			else
				this.data.put(key, value);
		}
	}
	
	
	
	public Object clone()
	{
		return new CTStateContainer(this);
	}
	
	
	/**
		Returns the type of data being stored here,
		e.g., Board, PlayerStatus, GameStatus, Square, etc.
	*/
	public String getCTContainerType()
	{
		return type;
	}
	
	
	/**
		Returns 'true' if the type of data placed here is 
		of specified type 't'; returns false otherwise.
	*/
	public boolean isCTContainerType(String t)
	{
		return type.equals(t);
	}
	
	
	/**
		Adds the specified encoding tag 'e' to this object
	*/
	public void addEncoding(String e)
	{
		encodings.add(e);
	}
	
	
	/**
		Returns 'true' if the data placed here is encoded
		as specified by 'e'; returns false otherwise;
	*/
	public boolean hasEncoding(String e)
	{
		return encodings.contains(e);
	}
	
	
	/**
		Adds specified data to the state container and labels it
		
		@param fieldname	the label of the datum being added
		@param datum		the datum itself
	*/
	public void put(String fieldname, Object datum)
	{
		// if datum is null, represent it so that we can define the
		// key yet avoid Hashtable exception
		if (datum == null)
			data.put(fieldname, NULL_VALUE);
		else
			data.put(fieldname, datum);
	}
	
	
	/**
		Synonym for 'put' method
	*/
	public void set(String fieldname, Object datum)
	{
		put(fieldname, datum);
	}
	
	
	/**
		Retrieves and returns the specified datum from the state container
		
		@param fieldname	the label of the datum being retrieved
	*/
	public Object get(String fieldname)
	{
		Object value = data.get(fieldname);
		if (value instanceof String && ((String)value).equals(NULL_VALUE))
			return null;
		else
			return value;
	}
	
	
	/**
		Removes the specified datum from the state container
	*/
	public void remove(String fieldname)
	{
		data.remove(fieldname);
	}
	
	
	/**
		Returns set of keys (feature names)
	*/
	public Set<String> getFeatures()
	{
		return data.keySet();
	}
	
	
	/**
		Returns true if the specified feature exists
	*/
	public boolean hasFeature(String fieldname)
	{
		return data.containsKey(fieldname);
	}
	
	
	/**
		Returns set of encodings contained here
	*/
	public Set<String> getEncodings()
	{
		return encodings;
	}
	
	public String toString()
	{
		Formatter frm = new Formatter();
		
		frm.format("Container Type: %s\n", type);
		frm.format("Encodings: ");
		for (String e : getEncodings())
			frm.format("%s ", e);
		frm.format("Features:\n");
		for (String k : getFeatures())
			frm.format("%s ", k);
		
		return frm.toString();
	}
}