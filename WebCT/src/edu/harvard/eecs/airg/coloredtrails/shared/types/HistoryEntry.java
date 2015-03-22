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

import java.util.Hashtable;
import java.io.Serializable;

/**
 * <b>Description</b>
 * A single entry suitable for adding to the history log.  Represents
 * a single game event happening at a given point in time.
 * <p/>
 * [sgf: Using a hashtable for the 'entry' field allows a general way to represent events.]
 * For more documentation and examples /docs/historyLog.pdf
 * <p/>
 * [sgf: is this class also serializable for an arbitrary output stream, such as disk?]
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public class HistoryEntry implements Serializable {

    private String phaseName = "";
    private int phaseNum = -1;
    private int secondsIntoPhase = -1;
    private Hashtable<String, Object> entry =
            new Hashtable<String, Object>();

    public HistoryEntry(String phaseName, int phaseNum,
                        int secondsIntoPhase) {
        this.phaseName = phaseName;
        this.secondsIntoPhase = secondsIntoPhase;
        this.phaseNum = phaseNum;
    }

    public HistoryEntry(String phaseName, int phaseNum,
                        int secondsIntoPhase,
                        Hashtable<String, Object> entry) {
        this.phaseName = phaseName;
        this.secondsIntoPhase = secondsIntoPhase;
        this.entry = entry;
        this.phaseNum = phaseNum;
    }


    /**
     * Get the phase name when this event occurred.
     *
     * @return The phase name when this event occurred.
     */
    public String getPhaseName() {
        return phaseName;
    }

    /**
     * Get the number of seconds into the phase that the event occurred.
     *
     * @return Number of seconds since the current phase started.
     */
    public int getSecondsIntoPhase() {
        return secondsIntoPhase;
    }

    /**
     * Get the entry as represented by a hashtable.
     *
     * @return The entry as represented by a hashtable.
     */
    public Hashtable<String, Object> getEntry() {
        return entry;
    }

    /**
     * Get the number of phases which have passed.
     *
     * @return The number of phases which have passed before this point.
     */
    public int getPhaseNum() {
        return phaseNum;
    }

    /**
     * Get a particular history entry value based on a key.
     *
     * @param key The name of a key indexing into the history entry.
     * @return The value associated with the given key.
     */
    public Object getDataByValue(String key) {
        return entry.get(key);
    }

    /**
     * Get the type of action which this entry represents.
     *
     * @return The type of action which this entry represents.
     */
    public String getType() {
        return (String) entry.get("type");
    }

    /**
     * Set the type of action which this entry represents.
     *
     * @param type The name of the action which this entry represents.
     */
    public void setType(String type) {
        entry.put("type", type);
    }


    public String toString() {
        String entries = "";

        for (String key : entry.keySet()) {
            entries += key + ": " + entry.get(key) + ".\n";
        }
        return "Entry...\n" +
                "Phase Num: " + phaseNum + ".\n" +
                "Phase Name: " + phaseName + ".\n" +
                "Seconds Into Phase: " + secondsIntoPhase + ".\n" +
                entries +
                "End of entry...\n";
    }
}
