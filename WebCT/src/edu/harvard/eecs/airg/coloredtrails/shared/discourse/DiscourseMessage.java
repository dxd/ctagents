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

package edu.harvard.eecs.airg.coloredtrails.shared.discourse;

import edu.harvard.eecs.airg.coloredtrails.shared.Utility;
import edu.harvard.eecs.airg.coloredtrails.shared.types.HistoryEntry;

import java.io.Serializable;
import java.util.Hashtable;

/**
	<b>Description (original)</b>
 * A discourse message type which is extended to permit different
 * types of disourse among players.
	<p>
	(sgf) This class is extended by two classes in this package:<br>
	<pre><code>shared.discourse.BasicProposalDiscourseMessage</code></pre><br>
	<pre><code>shared.discourse.BasicProposalDiscussionDiscourseMessage</code></pre><br>
	These message types concern chip-exchange proposals and their discussion, respectively.
	Other discourse message types are found elsewhere.
 
	<p>
	
	<b>TO DO</b>
	(sgf) 
	The DiscourseMessage identifies sender and receiver by gameIDs,
	as opposed to participant IDs.  Describe how the server knows
	the ultimate origin and destination of a message.
 
 * @author Paul Heymann (ct3@heymann.be)
 */
public class DiscourseMessage implements Cloneable, Serializable {

    protected int fromPerGameId = -1;
    protected int toPerGameId = -1;
    protected int messageId = -1;
    protected String msgType = "";

    public DiscourseMessage() {
    }

    public DiscourseMessage( int fromPerGameId, int toPerGameId, String msgType, int messageId) {
        this.fromPerGameId = fromPerGameId;
        this.toPerGameId = toPerGameId;
        this.msgType = msgType;
        this.messageId = messageId;
    }

    /**
        Copy constructor; new version (SGF)
    */
    public DiscourseMessage(DiscourseMessage dm) {
        fromPerGameId = dm.fromPerGameId;
        toPerGameId = dm.toPerGameId;
        msgType = dm.msgType;
        messageId = dm.messageId;
    }
    
    /**
     * Get the message ID of this discourse message.
     *
     * @return The message Id of this discourse message.
     */
    public int getMessageId() {
        return messageId;
    }

    /**
     * Set the message ID of this discourse message.
     *
     * @param messageId The new message Id of this discourse message.
     */
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    /**
     * Get who sent this discourse message.
     *
     * @return The perGameId of the sender of the discourse message.
     */
    public int getFromPerGameId() {
        return fromPerGameId;
    }

    /**
     * Get who this discourse message is sent to.
     *
     * @return The perGameId of the receiver of the discourse message.
     */
    public int getToPerGameId() {
        return toPerGameId;
    }

    /**
     * The name of this message type, used by the server to handle
     * the message and by clients to select an appropriate handler.
     *
     * @return The name of the discourse message.
     */
    public String getMsgType() {
        return msgType;
    }

    public String toString() {
        return "Discourse Message...\n" +
                "From PerGameId: " + fromPerGameId + ".\n" +
                "To PerGameId: " + toPerGameId + ".\n";
    }

    /**
     * Return a history entry describing this discourse message suitable
     * for adding to the history log.
     *
     * @param phaseName        The name of the phase when this discourse message
     *                         was sent.
     * @param phaseNum         The phase number of the phase when this discourse
     *                         message was sent.
     * @param secondsIntoPhase How many seconds into the current phase
     *                         the discourse message was sent.
     * @return A new HistoryEntry describing the discourse message event.
     */
    public HistoryEntry toHistoryEntry(String phaseName, int phaseNum,
                                       int secondsIntoPhase) {
        return new HistoryEntry(phaseName, phaseNum, secondsIntoPhase);
    }

    public DiscourseMessage clone()
    {
      return new DiscourseMessage(this);
    }
}
