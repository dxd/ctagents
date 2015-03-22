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
import java.util.Iterator;
import java.util.Set;

/**
 * Print environmental variables.
 */
public class PrintenvCommand extends Command {
    public PrintenvCommand() {
        name = "printenv";
    }

    public PrintenvCommand(String[] argv, StateHolder stater,
                           PrintWriter out, BufferedReader in) {
        super(argv, stater, out, in, "printenv");
    }

    public void doCommand() {
        Set mykeyset = stater.getVariableList();
        Iterator iter = mykeyset.iterator();
        while (iter.hasNext()) {
            String var = (String) iter.next();
            out.print("$" + var + " = " +
                    stater.getVariable(var) + "\n");
        }
    }

    public Command getCommand(String[] argv,
                              StateHolder stater,
                              PrintWriter out,
                              BufferedReader in) {
        return new PrintenvCommand(argv, stater, out, in);
    }

    public void horribleDeath() throws BadShellInputException {
        maxArgs(argv, 1, "printenv");
    }
}
