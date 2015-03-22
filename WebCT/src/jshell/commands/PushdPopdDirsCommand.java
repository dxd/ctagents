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
import java.util.Stack;

/**
 * Print directory stack or push or pop it.
 */
public class PushdPopdDirsCommand extends Command {
    public PushdPopdDirsCommand() {
        name = "pushdpopddirs";
    }

    public PushdPopdDirsCommand(String[] argv, StateHolder stater,
                                PrintWriter out, BufferedReader in) {
        super(argv, stater, out, in, "pushdpopddirs");
    }

    public void doCommand() {
        if (argv[0].startsWith("pushd")) {
            stater.pushToDirStack(stater.getCwd());
            String newcwd = argv[1];
            if (!argv[1].startsWith("/")) {
                newcwd = stater.getCwd() + "/" + newcwd + "/";
            }
            newcwd = newcwd.replaceAll("//", "/");
            stater.setCwd(newcwd);
        } else if (argv[0].startsWith("popd")) {
            Stack dirs = stater.getDirStack();
            stater.setCwd((String) dirs.pop());
        }
        runDirs();
    }

    /**
     * Print the directory stack.
     */
    private void runDirs() {
        Stack dirs = stater.getDirStack();
        Iterator iter = dirs.iterator();
        out.print(stater.getCwd());
        while (iter.hasNext()) {
            out.print(" " + (String) iter.next());
        }
        out.print("\n");
    }

    public Command getCommand(String[] argv,
                              StateHolder stater,
                              PrintWriter out,
                              BufferedReader in) {
        return new PushdPopdDirsCommand(argv, stater, out, in);
    }

    public void horribleDeath() throws BadShellInputException {
        if (argv[0].startsWith("popd") || argv[0].startsWith("dirs")) {
            maxArgs(argv, 1, "popd or dirs");
        } else if (argv[0].startsWith("pushd")) {
            checkArgs(argv, 2, 2, "pushd");
        }
    }

    public boolean isProperCommand(String command) {
        return (command.startsWith("pushd") ||
                command.startsWith("popd") ||
                command.startsWith("dirs"));
    }
}
