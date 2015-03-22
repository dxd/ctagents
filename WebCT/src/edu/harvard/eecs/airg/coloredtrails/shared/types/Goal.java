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

import java.net.URL;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * Represents a goal
 * 
 * @author Sevan G. Ficici (modifications for partial visibility)
 */
public class Goal extends CTStateContainer implements Cloneable, Serializable {
	/** goal types */
	public static final int DEFAULT_GOAL_TYPE = Integer.MAX_VALUE;
	// Any type that is grater or equal to this constant represents a possible
	// goal.
	public static final int POSSIBLE_GOAL_TYPE = 10;
	/**
	 * default goal image URL
	 * 
	 * @deprecated
	 */
	public static URL DEFAULT_GOAL_IMAGE;

	/** the goal's ID; must be uniqe */
	// private String id = "noname";
	/** the goal's type; interpretation is up to the experimenter */
	// private int type = DEFAULT_GOAL_TYPE;
	/** the goal's value; interpretation is up to the experimenter */
	// private double value = 0.0;
	/** the goal's location on the board */
	// private RowCol location = null;
	/** whether the goal is active; interpretation is up to the experimenter */
	// private boolean active = true;
	/** the goal's image URL */
	// private URL imageloc = DEFAULT_GOAL_IMAGE;

	/******************************************************/
	/***************** MANAGING GOAL NAMES ****************/
	/******************************************************/

	/** used to generate unique automatic goal names */
	private static int idnum = 0;

	private static String autoName() {
		return "goal" + (idnum++);
	}

	/******************************************************/
	/***************** MANAGING GOAL IMAGES ***************/
	/******************************************************/

	/**
	 * list of image URLs we can index into with 'type' field
	 * 
	 * @deprecated Image
	 */
	private static ArrayList<URL> goalimages;

	/**
	 * buils a default list of pre-built goal images that we can index into
	 */
	static {
		goalimages = new ArrayList<URL>();
		try {
			DEFAULT_GOAL_IMAGE = new URL("file://lib/images/goal.gif");
			goalimages.add(new URL("file://lib/images/goal.gif"));
			goalimages.add(new URL("file://lib/images/goal.gif"));
			goalimages.add(new URL("file://lib/images/goal.gif"));
		} catch (Exception e) {
			System.out
					.println("Goal.static: unable to build default goal image list");
		}
	}

	/**
	 * Returns the specified goal image URL
	 * 
	 * @param index
	 *            the index into the list of goal images
	 * @return the specified URL
	 * @deprecated Image
	 */
	public static URL getImage(int index) {
		if (index == DEFAULT_GOAL_TYPE) {
			return DEFAULT_GOAL_IMAGE;
		}
		return goalimages.get(index % 10);
	}

	/**
	 * clears the list of goal images that we index into
	 * 
	 * @deprecated Image
	 */
	public static void clearImages() {
		goalimages.clear();
	}

	/**
	 * Adds an image to the end of the list of goal images we can index into
	 * 
	 * @param urlstring
	 *            a string containing the URL of the image
	 * @deprecated Image
	 */
	public static void addImage(String urlstring) {
		try {
			goalimages.add(new URL(urlstring));
		} catch (Exception e) {
		}
	}

	/**
	 * Sets the image list at the specified index to the specified image
	 * 
	 * @param urlstring
	 *            a string containing the URL of the image
	 * @param index
	 *            the index into the list where we want the image placed
	 * @deprecated Image
	 */
	public static void setImage(String urlstring, int index) {
		try {
			goalimages.set(index, new URL(urlstring));
		} catch (Exception e) {
		}
	}

	/******************************************************/
	/****************** INSTANCE METHODS ******************/
	/******************************************************/

	/**
	 * Constructor
	 */
	public Goal() {
		super("Goal", "Server"); // server's encoding of the goal
		put("id", autoName());
	}

	/**
	 * Constructor
	 */
	public Goal(int type) {
		this();
		put("type", new Integer(type));
		put("imageloc", getImage(type));
	}

	/**
	 * Constructor
	 */
	public Goal(String id, int type) {
		super("Goal", "Sever");
		put("id", id);
		put("type", new Integer(type));
		put("imageloc", getImage(type));
	}

	/**
	 * Constructor
	 */
	public Goal(int type, RowCol location) {
		this();
		put("type", new Integer(type));
		put("location", location);
		put("imageloc", getImage(type));
	}

	/**
	 * Constructor. Used when a possible goal is created and therefore
	 * initializes the probability of this goal
	 */
	public Goal(int type, RowCol location, int probability) {
		this();
		put("type", new Integer(type));
		put("location", location);
		put("imageloc", getImage(type));
		put("probability", new Integer(probability));
	}

	/**
	 * Constructor
	 */
	public Goal(RowCol pos) {
		this(DEFAULT_GOAL_TYPE, pos);
	}

	/**
	 * Constructor
	 */
	public Goal(String id, int type, RowCol location) {
		super("Goal", "Sever");
		put("id", id);
		put("type", new Integer(type));
		put("location", location);
		put("imageloc", getImage(type));
	}

	/**
	 * Constructor
	 * 
	 * @param type
	 *            the goal's type; interpretation is up to the experimenter
	 * @param id
	 *            the goal's ID; interpretation is up to the experimenter
	 * @param imageloc
	 *            the goal's image URL
	 * @deprecated image
	 */
	public Goal(String id, int type, URL imageloc) {
		super("Goal", "Sever");
		put("id", id);
		put("type", new Integer(type));
		put("imageloc", imageloc);
	}

	/**
	 * Copy constructor
	 */
	public Goal(Goal g) {
		super(g);
	}

	public Object clone() {
		return new Goal(this);
	}

	/**
	 * Gets the Id of the Goal
	 * 
	 * @return Id of the goal
	 */
	public String getID() {
		return (String) get("id");
	}

	/**
	 * Sets the Id of the Goal to id
	 * 
	 * @param id
	 *            New Id
	 */
	public void setID(String id) {
		put("id", id);
	}

	/**
	 * Gets the type of the goal
	 * 
	 * @return Type of the goal
	 */
	public int getType() {
		return (Integer) get("type");
	}

	/**
	 * Sets the type of the goal
	 * 
	 * @param type
	 *            New type
	 */
	public void setType(int type) {
		put("type", new Integer(type));
	}

	/**
	 * Gets the location of the goal
	 * 
	 * @return Location of the goal
	 */
	public RowCol getLocation() {
		return (RowCol) get("location");
	}

	/**
	 * Sets the location of the goal
	 * 
	 * @param location
	 *            New location of the goal
	 */
	public void setLocation(RowCol location) {
		put("location", location);
	}

	/**
	 * Gets the probability of the goal
	 * 
	 * @return probability of the goal
	 */
	public int getProbability() {
		return (Integer) get("probability");
	}

	/**
	 * Sets the probability of the goal
	 * 
	 * @param probability
	 *            New probability of the goal
	 */
	public void setProbability(int probability) {
		put("probability", new Integer(probability));
	}

	/**
	 * Sets the location of the goal
	 * 
	 * @param row
	 *            New row value of the goal
	 * @param col
	 *            New colomn value of the goal
	 */
	public void setLocation(int row, int col) {
		put("location", new RowCol(row, col));
	}

	/**
	 * Returns the goal's image URL
	 */
	public URL getImageloc() {
		return (URL) get("imageloc");
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("Goal: id = " + getID() + "  type = " + getType()
				+ "  location = " + getLocation() + "  URL = " + getImageloc());

		return sb.toString();
	}

	// use super-class' clone method?
	/*
	 * public Object clone() { return new Goal(this); }
	 */
}
