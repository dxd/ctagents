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

/**
 * Head and tail.
 */
public class HeadOrTailCommand extends Command {
    public HeadOrTailCommand() {
        name = "headortail";
    }

    public HeadOrTailCommand(String[] argv, StateHolder stater,
                             PrintWriter out, BufferedReader in) {
        super(argv, stater, out, in, "headortail");
    }

    public boolean needsStdin() {
        return (argv.length == 1 ||
                (argv.length == 2 && argv[1].matches("^[0-9]+$")));
    }

    public void doCommand() {
        int numlines = 10;
        String commandname = argv[0].trim();
        argv = JShellMisc.removeProgramName(argv);
        Getopt g = new Getopt(commandname, argv, "", new LongOpt[0]);
        g.setOpterr(false);
        String[] lines;

        if (argv.length != 0) {
            int argnum = g.getOptind();
            String userlines = argv[argnum];

            // if the user gave an int, then do that many lines
            // otherwise restart tokenizer
            if (userlines.matches("^[0-9]+$")) {
                Integer converterint = new Integer(userlines);
                numlines = converterint.intValue();
                argnum++;
                if (argv.length == 1) {
                    lines = stdin.split("[\\n\\r]");
                    out.print(headOrTail(commandname, lines, numlines));
                }
            }

            // for each token, head or tail it
            for (; argnum < argv.length; argnum++) {
                lines = JShellMisc.fileToString(argv[argnum]).split(
                        "[\\n\\r]");
                out.print(headOrTail(commandname, lines, numlines));
            }
        } else {
            lines = stdin.split("[\\n\\r]");
            out.print(headOrTail(commandname, lines, numlines));
        }
    }

    public Command getCommand(String[] argv,
                              StateHolder stater,
                              PrintWriter out,
                              BufferedReader in) {
        return new HeadOrTailCommand(argv, stater, out, in);
    }

    /**
     * Do proper action depending on whether we are heads or tails.
     * Post: lines for head or tail are printed.
     */
    private String headOrTail(String commandname, String[] lines,
                              int numlines) {
        String result = "";
        if (commandname.equals("head")) {
            for (int i = 0; i < numlines && i < lines.length; i++) {
                result = result + lines[i] + "\n";
            }
        } else {
            int start = 0;
            if (numlines <= lines.length) {
                start = lines.length - numlines;
            }
            for (int i = 0;
                 i < numlines && start < lines.length; i++, start++) {
                result = result + lines[start] + "\n";
            }
        }
        return result;
    }

    public boolean isProperCommand(String command) {
        return (command.startsWith("head") ||
                command.startsWith("tail"));
    }
}
