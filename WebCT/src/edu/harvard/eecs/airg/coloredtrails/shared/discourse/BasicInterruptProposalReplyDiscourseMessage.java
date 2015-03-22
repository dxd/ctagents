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

import edu.harvard.eecs.airg.coloredtrails.shared.types.HistoryEntry;

import java.util.Hashtable;

/**
	<b>Description (original)</b>
 * A discourse message used to reply to a basic interruption protocol
 * 
 * 
 */
public class BasicInterruptProposalReplyDiscourseMessage
        extends DiscourseMessage {

    private boolean answer;
    public BasicInterruptProposalReplyDiscourseMessage() {
    }

   public BasicInterruptProposalReplyDiscourseMessage(int fromPerGameId,
                                                   int toPerGameId,
                                                   int messageId) {
        super(fromPerGameId, toPerGameId,
                "basicinterruptreply", messageId);
    }

    public BasicInterruptProposalReplyDiscourseMessage(DiscourseMessage other) {
        super(other);
    }

    public String getMsgType() {
        return "basicinterruptreply";
    }

    public HistoryEntry toHistoryEntry(String phaseName, int phaseNum,
                                       int secondsIntoPhase) {
        Hashtable<String, Object> entry = new Hashtable<String, Object>();
        entry.put("type", "basicinterruptreply");
        entry.put("toPerGameId", new Integer(getToPerGameId()));
        entry.put("fromPerGameId", new Integer(getFromPerGameId()));
        entry.put("messageId", new Integer(getMessageId()));

        return new HistoryEntry(phaseName, phaseNum,
                secondsIntoPhase, entry);
    }

    public BasicInterruptProposalReplyDiscourseMessage clone()
    {
        return new BasicInterruptProposalReplyDiscourseMessage(this);
    }
}
