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

import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.HistoryEntry;

import java.util.Hashtable;

/**
	<b>Description (original)</b>
 * A discourse message permitting dicussion of a basic, non-enforced
 * proposal.
 
	<p>
	
	<b>Issues</b>
	(sgf) The toHistoryEntry method does not include all message content into
	the log entry.  Specifically, 'subjectMsgId' and 'commentary' are
	not placed into the entry.
 
 * @author Paul Heymann (ct3@heymann.be)
 */
public class BasicProposalDiscussionDiscourseMessage extends BasicProposalDiscourseMessage {


    //private int subjectMessageId;
    private String commentary;

    /*public BasicProposalDiscussionDiscourseMessage(int proposerID,
                                                   int responderID,
                                                   int messageId,
                                                   int subjectMessageId,
                                                   ChipSet chipsSentByResponder,
                                                   ChipSet chipsSentByProposer) {
        super(responderID, proposerID, "basicproposaldiscussion", messageId);

         int proposerID, int responderID, int messageId, ChipSet chipsSentByProposer, ChipSet chipsSentByResponder

        fromPerGameId;
        toPerGameId;
    }  */

    public BasicProposalDiscussionDiscourseMessage(BasicProposalDiscussionDiscourseMessage other) {
        super( other );


        msgType = "basicproposaldiscussion";

        //this.subjectMessageId = other.subjectMessageId;
        this.commentary = other.commentary;

        fromPerGameId = other.getResponderID();
        toPerGameId   = other.getProposerID();
    }

    public BasicProposalDiscussionDiscourseMessage(BasicProposalDiscourseMessage other) {
        super( other );


        msgType = "basicproposaldiscussion";

        fromPerGameId = other.getResponderID();
        toPerGameId   = other.getProposerID();
    }

    //public void setSubjectMsgId(int messageId) {
    //    subjectMessageId= messageId;
    //}

//    public int getSubjectMsgId() {
//        return -1;//subjectMessageId;
//    }

   //public String getMsgType() {
   //     return "basicproposaldiscussion";
    //}
    
    public boolean accepted() {
    	return commentary.equals("accept");
    }
    
    public void acceptOffer() {
    	commentary = "accept";
    }
    
    public void rejectOffer() {
    	commentary = "reject";
    }

    public HistoryEntry toHistoryEntry(String phaseName, int phaseNum,
                                       int secondsIntoPhase) {
        Hashtable<String, Object> entry = new Hashtable<String, Object>();
        entry.put("type", "basicproposaldiscussion");
        entry.put("Proposer ID  ", getProposerID());
        entry.put("Responder ID ", getResponderID());
        entry.put("messageId", getMessageId());

        return new HistoryEntry(phaseName, phaseNum,
                secondsIntoPhase, entry);
    }

    //public BasicProposalDiscussionDiscourseMessage clone()
    //{
    //    return new BasicProposalDiscussionDiscourseMessage(this);
    //}
}
