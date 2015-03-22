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

package edu.harvard.eecs.airg.coloredtrails.client.ui.discourse;

import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;

import java.awt.event.ActionListener;

/**
 * An interface for handling discourse messages which can be initiated,
 * sent, or received by a graphical user interface client.
 *
 * @author Ricardo De Lima (ricardo@eecs.harvard.edu)
 */
public interface GraphicalDiscourseHandler extends DiscourseHandler {


    /**
     * An action which takes place when the discourse message that this
     * handler is associated with is received.
     * @param dm The discourse message.
     */
    public void onReceipt(DiscourseMessage dm);

    /**
     * An action which takes place when the discourse message that this
     * handler is associated with is sent.
     * @param dm
     */
    public void onSend(DiscourseMessage dm);

    /**
     * The name of the type of discourse message which this handler
     * is associated with.
     * @return The type name.
     */
    public String getType();

    /**
     * Return the name to be added to a button on the taskbar if this
     * handler places a visible button on the taskbar.
     * @return Button name.
     */
    public String getTaskbarName();

    /**
     * Whether this discourse handler should cause a button to appear
     * in the taskbar.
     * @return Whether or not to create the button.
     */
    public boolean inTaskbar();

    /**
     * An action to be associated with a button on the taskbar, if
     * inTaskbar() is true.
     * @return An actionlistener to handle the button click event.
     */
    public ActionListener getDiscourseActionListener();
}
