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

import java.io.BufferedReader;
import java.io.PrintWriter;

/**
 * A (possibly out of date) help command for the admin client, printing
 * usage.
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public class HelpCommand extends Command {
    public HelpCommand() {
        name = "help";
    }

    public HelpCommand(String[] argv, StateHolder stater,
                       PrintWriter out, BufferedReader in) {
        super(argv, stater, out, in, "help");
    }

    public void doCommand() {
        out.println(
                "Admin client commands are:\n" +
                "==========================\n" +
                "help                - this menu\n" +
                "list configurations - game configurations uploaded to server\n" +
                "list games          - games in progress or completed\n" +
                "list players        - connected and disconnected players");
    }

    public Command getCommand(String[] argv, StateHolder stater,
                              PrintWriter out, BufferedReader in) {
        return new HelpCommand(argv, stater, out, in);
    }

    public void horribleDeath() throws BadShellInputException {
        maxArgs(argv, 1, "help");
    }
}
