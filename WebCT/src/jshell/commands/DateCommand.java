/* Interactive Java Shell (JShell) For CPS108
 * Copyright (C) 2003 Paul Heymann
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package jshell.commands;

import jshell.util.BadShellInputException;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Print the date.
 */
public class DateCommand extends Command {
    public DateCommand() {
        name = "date";
    }

    public DateCommand(String[] argv, StateHolder stater,
                       PrintWriter out, BufferedReader in) {
        super(argv, stater, out, in, "date");
    }

    public void doCommand() {
        out.print(new Date().toString() + "\n");
    }

    public Command getCommand(String[] argv,
                              StateHolder stater,
                              PrintWriter out,
                              BufferedReader in) {
        return new DateCommand(argv, stater, out, in);
    }

    public void horribleDeath() throws BadShellInputException {
        maxArgs(argv, 1, "date");
    }
}
