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

package edu.harvard.eecs.airg.coloredtrails.admin;

import edu.harvard.eecs.airg.coloredtrails.server.ColoredTrailsServer;
import edu.harvard.eecs.airg.coloredtrails.server.AdminCommands;
import jshell.commands.Command;
import jshell.commands.StateHolder;
import jshell.util.BadShellInputException;

import javax.jms.TextMessage;
import javax.jms.JMSException;
import java.io.BufferedReader;
import java.io.PrintWriter;

/**
 * A command to list all players who have registered with the server,
 * whether they are currently in a game or not.
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public class ListPlayersCommand extends AdminClientCommand {
    public ListPlayersCommand() {
        name = "list players";
    }

    public ListPlayersCommand(String[] argv, StateHolder stater,
                              PrintWriter out, BufferedReader in) {
        super(argv, stater, out, in, "list players");
    }

    public void doCommand() {
        try {
            TextMessage txtMsg = communication.getSession().createTextMessage();
            txtMsg.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.ADMIN_MSG);
            txtMsg.setStringProperty(ColoredTrailsServer.COMMAND, AdminCommands.LIST_PLAYERS);
            communication.getProducer().send(txtMsg);
            log.info("Admin: Sending \"list players\" command");
        } catch (JMSException e) {
            log.fatal("Admin:Error sending \"list players\" command to the Server:" + e);
            System.exit(-1);
        }
    }

    public Command getCommand(String[] argv,
                              StateHolder stater,
                              PrintWriter out,
                              BufferedReader in) {
        return new ListPlayersCommand(argv, stater, out, in);
    }

    public void horribleDeath() throws BadShellInputException {
        checkArgs(argv, 2, 2, "list players");
    }

    public boolean isProperCommand(String command) {
        return command.startsWith("list players");
    }
}
