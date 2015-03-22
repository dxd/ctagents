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

import jshell.commands.Command;
import jshell.commands.StateHolder;
import jshell.util.BadShellInputException;

import javax.jms.TextMessage;
import javax.jms.JMSException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Vector;

import edu.harvard.eecs.airg.coloredtrails.server.ColoredTrailsServer;
import edu.harvard.eecs.airg.coloredtrails.server.AdminCommands;

/**
 * A command to initiate a new game thread on the server, notifying all
 * clients and beginning the processes on the server that are required
 * to run the game to completion.
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public class NewGameCommand extends AdminClientCommand {
    public NewGameCommand() {
        name = "new game";
    }

    public NewGameCommand(String[] argv, StateHolder stater,
                          PrintWriter out, BufferedReader in) {
        super(argv, stater, out, in, "new game");
    }

    public void doCommand() {
        try {
            TextMessage txtMsg = communication.getSession().createTextMessage();
            txtMsg.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.ADMIN_MSG);
            txtMsg.setStringProperty(ColoredTrailsServer.COMMAND, AdminCommands.NEW_GAME);
            txtMsg.setStringProperty(AdminCommands.CONFIGCLASS_NAME, argv[3]);
            String players = "";
            for (int i=5; i<argv.length;i++) {
                 players +=(argv[i]) + " ";
            }
            txtMsg.setStringProperty(AdminCommands.LIST_PLAYERS, players);

            communication.getProducer().send(txtMsg);
            log.info("Admin: Sending \"new game\" command");
        } catch (JMSException e) {
            log.fatal("Admin:Error sending \"new game\" command to the Server:" + e);
            System.exit(-1);
        }
    }

    public Command getCommand(String[] argv,
                              StateHolder stater,
                              PrintWriter out,
                              BufferedReader in) {
        return new NewGameCommand(argv, stater, out, in);
    }

    public void horribleDeath() throws BadShellInputException {
        minArgs(argv, 6,
                "new game config <configName> " +
                "players <player1> ... <playerN>");
    }

    public boolean isProperCommand(String command) {
        return command.startsWith("new game config");
    }
}
