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

import java.util.Hashtable;

import edu.harvard.eecs.airg.coloredtrails.shared.discourse.ReputationReqDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.HistoryEntry;

/**
  Outline of discourse message to reply to a request for reputation
  information.
  
  @author Sevan G. Ficici
*/
public class ReputationReplyDiscourseMessage extends DiscourseMessage
{
    public static final String mname = "reputationreply";
    
    // STANDARD METHODS FIRST:
    /**
      Constructor
    */
    public ReputationReplyDiscourseMessage() {}
    

    public ReputationReplyDiscourseMessage(int fromPerGameId, int toPerGameId, int messageId)
    {
        super(fromPerGameId, toPerGameId, mname, messageId);
    }

    public ReputationReplyDiscourseMessage(DiscourseMessage other)
    {
        super(other);
    }
    
    public String getMsgType()
    {
        return mname;
    }

    public ReputationReplyDiscourseMessage clone()
    {
        return new ReputationReplyDiscourseMessage(this);
    }
    
    public HistoryEntry toHistoryEntry(String phaseName, int phaseNum, int secondsIntoPhase)
    {
        Hashtable<String, Object> entry = new Hashtable<String, Object>();
        
        // STANDARD FIELDS
        entry.put("type", mname);
        entry.put("toPerGameId", new Integer(getToPerGameId()));
        entry.put("fromPerGameId", new Integer(getFromPerGameId()));
        entry.put("messageId", new Integer(getMessageId()));
        
        // FIELDS SPECIFIC TO THIS MESSAGE TYPE
        entry.put("request", getRequestMessage());
        entry.put("reputation", getReputation());

        return new HistoryEntry(phaseName, phaseNum, secondsIntoPhase, entry);
    }

    private ReputationReqDiscourseMessage requestMessage;
    private Object reputation;

    public Object getReputation() {
        return reputation;
    }

    public void setReputation(Object reputation) {
        this.reputation = reputation;
    }

    public ReputationReqDiscourseMessage getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(ReputationReqDiscourseMessage requestMessage) {
        this.requestMessage = requestMessage;
    }
}
