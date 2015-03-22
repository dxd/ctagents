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


/**
 * Discourse Message for chip transfers. Used in
 * ctgui.original.ActionHistory
 * @author Bart Kamphorst (bart.kamphorst@phil.uu.nl)
 */
public class TransferDiscourseMessage extends DiscourseMessage {
	static final long serialVersionUID = -3126998878902358685L;
    ChipSet chips;

    public TransferDiscourseMessage(int senderID, int receiverID, int messageId, ChipSet cs) {
        super(senderID, receiverID, "transferrequest", messageId);

        this.chips = cs;
    }

    public ChipSet getTransferredChips() {
        return this.chips;
    }
}
