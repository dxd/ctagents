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

import edu.harvard.eecs.airg.coloredtrails.server.AdminCommands;
import jshell.commands.Command;
import jshell.commands.StateHolder;

import javax.jms.*;
import java.io.BufferedReader;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

/**
 * An administrator client command base class which defines some
 * useful helper routines when writing JShell Commands specifically
 * for the admin client, like making XML-RPC requests and failing
 * if variables are undefined.
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public abstract class AdminClientCommand extends Command implements MessageListener, ExceptionListener {
    protected Logger log = Logger.getRootLogger();
    protected Communication communication = Communication.getInstance();

    /* (non-Javadoc)
    * @see javax.jms.ExceptionListener#onException(javax.jms.JMSException)
    */
    public void onException(JMSException arg) {
        log.fatal("ADMIN: JMS Exception occured.  Shutting down." + arg);
        System.exit(-1);

    }


    public void onMessage(Message msg) {
        try {
            String reply = msg.getStringProperty(AdminCommands.REPLY_NAME);
            if (reply == null){
                log.debug("ADMIN: received a message intended for the controller. Ignoring it.");
                return;
            }
            log.debug("ADMIN: received a message:  " + reply);
            if (reply.equals(AdminCommands.ADD_CONFIGURATION)) {
                if (msg.getBooleanProperty(AdminCommands.REPLY)){
                    log.debug("ADMIN: successfully added configuration");
                } else {
                    log.fatal("ADMIN: error adding configuration");
                }
            }
            else if (reply.equals(AdminCommands.LIST_GAMES)) {
                log.info("list of games:" + msg.getStringProperty(AdminCommands.LIST_GAMES));
             }
            else if (reply.equals(AdminCommands.LIST_CONFIGURATIONS)) {
                log.info("list of configurations:" + msg.getStringProperty(AdminCommands.LIST_CONFIGURATIONS));
             }
            else if (reply.equals(AdminCommands.LIST_PLAYERS)) {
                log.info("list of players" + msg.getStringProperty(AdminCommands.LIST_PLAYERS));
             }
            else if (reply.equals(AdminCommands.NEW_GAME)) {
                int gameId = msg.getIntProperty(AdminCommands.NEW_GAME);
                log.info("Added new game:" + gameId);
            }

        } catch (JMSException e1) {
            e1.printStackTrace();
        }
    }

    public AdminClientCommand() {
        super();
    }

    public AdminClientCommand(String[] argv, StateHolder stater,
                              PrintWriter out, BufferedReader in,
                              String name) {
        super(argv, stater, out, in, name);
    }


    /**
     * Write an exception out to the command's out writer.
     *
     * @param out   The Writer to write to.
     * @param error The error string.
     * @param e     The exception which is being written.
     */
    public void writeException(PrintWriter out, String error,
                               Exception e) {
        out.print(error);
        out.println(".\nStack trace follows.");
        out.println(e.getMessage());
    }
}
