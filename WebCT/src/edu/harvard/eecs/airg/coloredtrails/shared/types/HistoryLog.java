/*
	Colored Trails
	
	Copyright (C) 2006, President and Fellows of Harvard College.  All Rights Reserved.
	
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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Vector;

/**
<b>Description</b>

This class holds and manages an ordered list of HistoryEntry instances.
For more documentation and examples /docs/historyLog.pdf

<p>

<b>Observers</b>
This class is observed by type.GameStatus

<p>

<b>Notifications</b>
Method addHistoryItem() issues "LOG_CHANGED" message.

<p>

<b>Original Summary</b>
 * A history log which holds all previous actions which have occurred in
 * a game.
 *
 * @author Paul Heymann (ct3@heymann.be)
	@author Sevan G. Ficici (class-level review and comments)
 */
public class HistoryLog extends Observable implements Serializable {

    public HistoryLog() {
    }

    public ArrayList<HistoryEntry> loglist = new ArrayList<HistoryEntry>();

    /**
     * Add a new history entry to the log.
     *
     * @param he The history entry to be added.
     */
    public void addHistoryItem(HistoryEntry he) {
        loglist.add(he);
        setChanged();
        notifyObservers("LOG_CHANGED");
    }

    /**
     * Get a list of all history log entries added to this log.
     *
     * @return A list of all history log entries added to this log.
     */
    public ArrayList<HistoryEntry> getLoglist() {
        return loglist;
    }

    public String toString() {
        String entries = "";
        for (HistoryEntry he : loglist) {
            entries += he.toString();
        }
        return "HistoryLog...\n" + entries +
               "End of HistoryLog...\n\n";
    }
}
