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

package ctgui.original.discoursehandlers;

import edu.harvard.eecs.airg.coloredtrails.client.ui.discourse.DiscourseHandler;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;

import java.awt.event.ActionListener;
import java.util.Hashtable;

import ctgui.original.GUI;

/**
 * A handler for responses to Basic Proposals.
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public class BasicProposalDiscussionDiscourseHandler
        implements DiscourseHandler {

    public void onReceipt(DiscourseMessage dm) {
        System.out.println("BasicProposalDiscussionDiscoursehandler received message: " + dm.getClass().getSimpleName());
        if( dm instanceof BasicProposalDiscussionDiscourseMessage ) {
            
            System.out.println("Processing message: " + dm.getMessageId());
            BasicProposalDiscussionDiscourseMessage bpddm =
                    new BasicProposalDiscussionDiscourseMessage((BasicProposalDiscussionDiscourseMessage) dm);
            
            if(bpddm == null) {
                System.out.println("bpddm is null");
            }

            Hashtable<Integer, Object> handlerData =
                    (Hashtable<Integer, Object>)
                    GUI.getAgent().communication.getDiscourseHandlerData("basicproposal");

            if(handlerData == null) {
                System.out.println("handlerData is null");
            }
            BasicProposalDisplayWindow bprw =
                    (BasicProposalDisplayWindow) handlerData.get(bpddm.getMessageId());
            if (bpddm.accepted()) {
                bprw.setProposalStatus("Recipient Accepted Proposal.");
            } else  {
                bprw.setProposalStatus("Recipient Rejected Proposal.");
            }
        }
        else {
            System.out.println( "Did nothing with message" );
        }
    }

    public void onSend(DiscourseMessage dm) {
        /**
         * send results are handled by the action handler
         * on the basicproposaldisplaywindow
         */
    }

    public String getType() {
        return "basicproposaldiscussion";
    }

    public String getTaskbarName() {
        return "";
    }

    public boolean inTaskbar() {
        return false;
    }

    public ActionListener getDiscourseActionListener() {
        return null;
    }
}
