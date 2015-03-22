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

package edu.harvard.eecs.airg.coloredtrails.shared;

import java.io.*;
import java.util.logging.*;

/**
 *  This is a very simple log implementation. It takes serializable objects and a descriptor, timestamps them,
 * and then writes to a specified file
 * 
 * @author legodude
 */

public class logger{
    ObjectOutputStream output;
    
    /**
     * Initializes logger and creates logfile. Will overwrite any existing file with same name
     * close() _must_ be called when logging is finished
     * @param filename
     */
    public logger(String filename){
        OutputStream file = null;
        try {
            file = new FileOutputStream(filename);
            OutputStream buffer = new BufferedOutputStream(file);
            output = new ObjectOutputStream(buffer);
            System.out.println("output:" + output.toString());

        } catch (IOException ex) {
            System.out.println("WTF");
            Logger.getLogger(logger.class.getName()).log(Level.SEVERE, null, ex);
        }    


    
    }
    
    /**
     * Logs the given object, it will be timestamped also
     * @param o
     * @param s object description
     */
    public void log(Serializable o, String s){
        
        System.out.println("Trying to log: " + s);
        
        try {
            output.writeObject(new logobject(o,s));
            output.flush();
            output.reset();
        } catch (IOException ex) {
            Logger.getLogger(logger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Closes the logfile and write one final object ("done") to signify end of file
     */
    public void close(){
        try {
            log("done", "done");
            output.flush();
            output.close();
        } catch (IOException ex) {
            Logger.getLogger(logger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

