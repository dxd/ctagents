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
 * Concatenate.
 */
public class CatCommand extends Command {
    public CatCommand() {
        name = "cat";
    }

    public CatCommand(String[] argv, StateHolder stater,
                      PrintWriter out, BufferedReader in) {
        super(argv, stater, out, in, "cat");
    }

    public Command getCommand(String[] argv,
                              StateHolder stater,
                              PrintWriter out,
                              BufferedReader in) {
        return new CatCommand(argv, stater, out, in);
    }

    public void doCommand() {
        if (argv[0].startsWith("<")) {
            argv[0] = argv[0].replace('<', ' ').trim();
        } else {
            argv = JShellMisc.removeProgramName(argv);
        }
        Getopt g = new Getopt("cat", argv, "", new LongOpt[0]);
        g.setOpterr(false);

        if (argv.length != 0) {
            for (int i = g.getOptind(); i < argv.length; i++) {
                out.print(JShellMisc.fileToString(argv[i]));
            }
        } else {
            out.print(stdin);
        }
    }

    public boolean needsStdin() {
        return (argv.length == 1 && !argv[0].startsWith("<"));
    }

    public boolean isProperCommand(String command) {
        return (command.startsWith("cat") ||
                command.startsWith("<"));
    }
}
