/*
	Colored Trails
	
	Copyright (C) 2008, President and Fellows of Harvard College.  All Rights Reserved.
	
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

/**
 * This class is a wrapper for objects to be logged, and adds a timestamp and descriptor
 * 
 * @author legodude 
 */


package edu.harvard.eecs.airg.coloredtrails.shared;
import java.io.*;

public class logobject implements Serializable {
    private long timestamp;
    private Serializable o;
    private String s;

    /**
     * Initializes class. Timestamp is set when this constructor runs
     * @param o object to be logged
     * @param s description of logged object
     */
    logobject(Serializable o, String s){
        this.o = o;
        this.s = s;
        timestamp = System.currentTimeMillis();
    }
    
    public long getTimestamp(){
        return(timestamp);
    }
    
    public Serializable getObject() {
        return(o);
    }
    
    public String getDescriptor(){
        return(s);
    }
}