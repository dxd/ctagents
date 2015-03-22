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
 * A discourse message permitting a basic, non-enforced proposal.
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public class BasicProposalDiscourseMessage extends DiscourseMessage {
	static final long serialVersionUID = -3126998878902358585L;

    final int proposerID;
    final int responderID;

    final private ChipSet chipsSentByResponder;
    final private ChipSet chipsSentByProposer;

    public BasicProposalDiscourseMessage(int proposerID, int responderID, int messageId, ChipSet chipsSentByProposer, ChipSet chipsSentByResponder) {
        super(proposerID, responderID, "basicproposal", messageId);

        this.proposerID = proposerID;
        this.responderID = responderID;

        this.chipsSentByProposer = chipsSentByProposer;
        this.chipsSentByResponder = chipsSentByResponder;
    }

    public BasicProposalDiscourseMessage(BasicProposalDiscourseMessage other) {
        this( other.proposerID, other.responderID, other.messageId,
                other.chipsSentByProposer, other.chipsSentByResponder);
    }

    public int getProposerID() {
        return proposerID;
    }

    public int getResponderID() {
        return responderID;
    }

    public ChipSet getChipsSentByResponder() {
        return chipsSentByResponder;
    }

    public ChipSet getChipsSentByProposer() {
        return chipsSentByProposer;
    }

	/**
		overloads superclass method
	*/
    public HistoryEntry toHistoryEntry(String phaseName, int phaseNum, int secondsIntoPhase) {
        Hashtable<String, Object> entry = new Hashtable<String, Object>();
        entry.put("senderChips", getChipsSentByProposer());
        entry.put("recipientChips", getChipsSentByResponder());
        entry.put("type", "basicproposal");
        entry.put("toPerGameId", getResponderID());
        entry.put("fromPerGameId", getProposerID());
        entry.put("messageId", getMessageId());

        return new HistoryEntry(phaseName, phaseNum,
                secondsIntoPhase, entry);
    }

    public BasicProposalDiscourseMessage clone(){
      return new BasicProposalDiscourseMessage(this);       
    }
}
