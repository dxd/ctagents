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

import edu.harvard.eecs.airg.coloredtrails.shared.discourse.PairingDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.HistoryEntry;

/**
  Discourse message sent from clients to server to indicate whether they
  approve of the pairing given by the server.
  
  NOTE that we identify other players with game IDs, not
  experiment IDs.  This means that each game includes all of the players,
  so that they can address each other by game IDs; also means that each 
  player's game ID must persist from one game to the next so reputation
  information is consistently maintained.
  
  @author Sevan G. Ficici
*/
public class PairingReplyDiscourseMessage extends DiscourseMessage
{
    public static final String mname = "pairingreply";
    
    // STANDARD METHODS FIRST:
    /**
      Constructor
    */
    public PairingReplyDiscourseMessage() {}
    

    
    public PairingReplyDiscourseMessage(int fromPerGameId, int toPerGameId, int messageId)
    {
        super(fromPerGameId, toPerGameId, mname, messageId);
    }

    public PairingReplyDiscourseMessage(DiscourseMessage other)
    {
        super(other);
    }
    
    public String getMsgType()
    {
        return mname;
    }

    public PairingReplyDiscourseMessage clone()
    {
        return new PairingReplyDiscourseMessage(this);
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
        entry.put("reply", isReply());
        entry.put("pairing", getPairing());

        return new HistoryEntry(phaseName, phaseNum, secondsIntoPhase, entry);
    }

    private boolean reply;
    private PairingDiscourseMessage pairing;

    public boolean isReply() {
        return reply;
    }

    public void setReply(boolean reply) {
        this.reply = reply;
    }

    public PairingDiscourseMessage getPairing() {
        return pairing;
    }

    public void setPairing(PairingDiscourseMessage pairing) {
        this.pairing = pairing;
    }
}
