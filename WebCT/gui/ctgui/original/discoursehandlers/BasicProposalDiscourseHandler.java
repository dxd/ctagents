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

import edu.harvard.eecs.airg.coloredtrails.client.ui.discourse.GraphicalDiscourseHandler;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import ctgui.original.GUI;

/**
 * An initial proof of concept BasicProposal discourse handler
 * which allows players to send basic proposals that are untouched
 * by the server and then see and respond to these proposals.
 * No retraction is provided for these proposals.
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public class BasicProposalDiscourseHandler
        implements GraphicalDiscourseHandler, ActionListener {

    private MakeBasicProposalWindow mpw = null;
    Hashtable<Integer, Object> handlerData = null;
    

    public String getType() {
        return "basicproposal";
    }

    public boolean inTaskbar() {
        return true;
    }

    public void makeNewDisplayWindow(BasicProposalDiscourseMessage dm, int type) {

    
        BasicProposalDiscourseMessage temp =
                new BasicProposalDiscourseMessage(dm);

        BasicProposalDisplayWindow bprw =
                new BasicProposalDisplayWindow(type, temp);
        bprw.setVisible(true);
        
        try {
        	handlerData =
                (Hashtable<Integer, Object>)
                GUI.getAgent().communication.getDiscourseHandlerData("basicproposal");
        
        } catch(Exception e) {
        	
        	e.printStackTrace();
        }
        
        if (handlerData == null) {
            handlerData = new Hashtable<Integer, Object>();
        }
        handlerData.put(temp.getMessageId(), bprw);
        GUI.getAgent().communication.setDiscourseHandlerData("basicproposal", handlerData);
        
    }

    public void onReceipt(DiscourseMessage dm) {
        if( dm instanceof BasicProposalDiscourseMessage )
            makeNewDisplayWindow((BasicProposalDiscourseMessage) dm, BasicProposalDisplayWindow.RECIPIENT);
    }

    public void onSend(DiscourseMessage dm) {
        if( dm instanceof BasicProposalDiscourseMessage )
             makeNewDisplayWindow((BasicProposalDiscourseMessage) dm, BasicProposalDisplayWindow.SENDER);
    }

    public String getTaskbarName() {
        return "Propose";
    }


    public ActionListener getDiscourseActionListener() {
        return this;
    }

    public void actionPerformed(ActionEvent e) {
        if (mpw == null) {
            mpw = new MakeBasicProposalWindow();
        }
        mpw.reset();
        mpw.setVisible(true);
    }
}
