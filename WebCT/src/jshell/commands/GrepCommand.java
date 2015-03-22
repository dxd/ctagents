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

/**
 * Search for specific values.
 */
public class GrepCommand extends Command {
    public GrepCommand() {
        name = "grep";
    }

    public GrepCommand(String[] argv, StateHolder stater,
                       PrintWriter out, BufferedReader in) {
        super(argv, stater, out, in, "grep");
    }

    public void doCommand() {
        String[] lines = new String[0];
        if (argv.length == 2) {
            lines = stdin.split("[\\n\\r]");
            grep(lines, argv[1], "stdin");
        } else {
            for (int i = 2; i < argv.length; i++) {
                String[] thisfilelines =
                        JShellMisc.fileToString(argv[i]).split("[\\n\\r]");
                grep(thisfilelines, argv[1], argv[i]);
            }
        }
    }

    public Command getCommand(String[] argv,
                              StateHolder stater,
                              PrintWriter out,
                              BufferedReader in) {
        return new GrepCommand(argv, stater, out, in);
    }

    public void horribleDeath() throws BadShellInputException {
        minArgs(argv, 2, "grep");
    }

    public boolean needsStdin() {
        return (argv.length == 2);
    }

    /**
     * Search through a string, returning lines that match.
     */
    private void grep(String[] lines, String query, String filename) {
        for (int j = 0; j < lines.length; j++) {
            if (lines[j].matches(".*" + query + ".*")) {
                if (filename.equals("stdin")) {
                    out.println(lines[j]);
                } else {
                    out.println(filename + ": " + lines[j]);
                }
            }
        }
    }
}
