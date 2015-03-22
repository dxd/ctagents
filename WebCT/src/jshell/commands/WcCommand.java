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

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import jshell.util.JShellMisc;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * Word count.
 */
public class WcCommand extends Command {
    public WcCommand() {
        name = "wc";
    }

    public WcCommand(String[] argv, StateHolder stater,
                     PrintWriter out, BufferedReader in) {
        super(argv, stater, out, in, "wc");
    }

    public void doCommand() {
        String result = "";
        argv = JShellMisc.removeProgramName(argv);
        Getopt g = new Getopt("wc", argv, "", new LongOpt[0]);
        g.setOpterr(false);

        if (argv.length != 0) {
            for (int i = g.getOptind(); i < argv.length; i++) {
                String filename = argv[i];
                doWC(JShellMisc.fileToString(filename));
                out.println(" " + filename);
            }
        } else {
            // stdin
            doWC(stdin);
            out.print("\n");
        }
    }

    public Command getCommand(String[] argv,
                              StateHolder stater,
                              PrintWriter out,
                              BufferedReader in) {
        return new WcCommand(argv, stater, out, in);
    }

    /**
     * Do the word count on a string.
     * Post: string is split by boundaries and then non-empty strings are
     * counted and printed.
     */
    private void doWC(String string2count) {
        String[] lines = string2count.split("[\\n]");
        String[] words = string2count.split("[\\s\\n]");

        // remove blank splits
        Vector vector = new Vector();
        for (int j = 0; j < words.length; j++) {
            if (!words[j].matches("^[\\s]*$")) {
                vector.addElement(words[j]);
            }
        }
        words = (String[]) vector.toArray(new String[vector.size()]);

        out.print(lines.length + " " +
                words.length + " " + string2count.length());
    }

    public boolean needsStdin() {
        return (argv.length == 1);
    }
}
