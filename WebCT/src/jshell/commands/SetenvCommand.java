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

/**
 * Set an environmental variable.
 */
public class SetenvCommand extends Command {
    public SetenvCommand() {
        name = "(un)setenv";
    }

    public SetenvCommand(String[] argv, StateHolder stater,
                         PrintWriter out, BufferedReader in) {
        super(argv, stater, out, in, "(un)setenv");
    }

    public void doCommand() {
        String variablename = argv[1];
        if (argv[0].startsWith("unsetenv")) {
            stater.setVariable(variablename, "");
        } else {
            stater.setVariable(variablename, argv[2]);
        }
    }

    public Command getCommand(String[] argv,
                              StateHolder stater,
                              PrintWriter out,
                              BufferedReader in) {
        return new SetenvCommand(argv, stater, out, in);
    }

    public void horribleDeath() throws BadShellInputException {
        if (argv[0].startsWith("setenv")) {
            checkArgs(argv, 3, 3, "setenv");
        } else if (argv[0].startsWith("unsetenv")) {
            checkArgs(argv, 2, 2, "unsetenv");
        }
    }

    public boolean isProperCommand(String command) {
        return (command.startsWith("setenv") ||
                command.startsWith("unsetenv"));
    }
}
