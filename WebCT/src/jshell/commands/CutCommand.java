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
import jshell.util.JShellMisc;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * Get certain fields.
 */
public class CutCommand extends Command {
    public CutCommand() {
        name = "cut";
    }

    public CutCommand(String[] argv, StateHolder stater,
                      PrintWriter out, BufferedReader in) {
        super(argv, stater, out, in, "cut");
    }

    public Command getCommand(String[] argv,
                              StateHolder stater,
                              PrintWriter out,
                              BufferedReader in) {
        return new CutCommand(argv, stater, out, in);
    }

    public void horribleDeath() throws BadShellInputException {
        minArgs(argv, 2, "cut");
        if (!argv[1].matches("[0-9]+")) {
            throw new BadShellInputException("Bad number input to cut.");
        }
    }

    public boolean needsStdin() {
        return (argv.length == 2);
    }

    public void doCommand() {
        if (needsStdin()) {
            out.print(
                    cutString(stdin, Integer.valueOf(argv[1]).intValue()));
        } else {
            for (int i = 2; i < argv.length; i++) {
                out.print(cutString(JShellMisc.fileToString(argv[i]),
                        Integer.valueOf(argv[1]).intValue()));
            }
        }
    }

    /**
     * Cut a string.
     * Pre: input is a string to be cut.
     * Post: fieldnumth field is output on each line.
     */
    private String cutString(String input, int fieldnum) {
        String result = "";
        String[] lines = input.split("[\\n\\r]");
        for (int i = 0; i < lines.length; i++) {
            // delimit by whitespace
            String[] fields = lines[i].split("\\s+");

            // remove blank splits
            Vector vector = new Vector();
            for (int j = 0; j < fields.length; j++) {
                if (!fields[j].matches("^[\\s]*$")) {
                    vector.addElement(fields[j]);
                }
            }
            fields = (String[]) vector.toArray(new String[vector.size()]);

            // print the nth field
            if (fieldnum - 1 < fields.length) {
                result += fields[fieldnum - 1] + "\n";
            }
        }
        return result;
    }
}
