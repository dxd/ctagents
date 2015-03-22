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
import java.io.File;
import java.io.PrintWriter;

/**
 * Change directory or get the current working directory.
 */
public class CdPwdCommand extends Command {
    public CdPwdCommand() {
        name = "cdpwd";
    }

    public CdPwdCommand(String[] argv, StateHolder stater,
                        PrintWriter out, BufferedReader in) {
        super(argv, stater, out, in, "cdpwd");
    }

    public Command getCommand(String[] argv,
                              StateHolder stater,
                              PrintWriter out,
                              BufferedReader in) {
        return new CdPwdCommand(argv, stater, out, in);
    }

    public void horribleDeath() throws BadShellInputException {
        if (argv[0].startsWith("cd")) {
            maxArgs(argv, 2, "cd");
        }
        if (argv[0].startsWith("pwd")) {
            maxArgs(argv, 1, "pwd");
        }

        // check if cd is good:
        if (argv[0].startsWith("cd")) {
            String newcwd = argv[1];
            if (!argv[1].startsWith("/")) {
                newcwd = stater.getCwd() + "/" + newcwd + "/";
            }
            newcwd = newcwd.replaceAll("//", "/");
            File cwdexists = new File(newcwd);
            if (!cwdexists.exists()) {
                throw new BadShellInputException(
                        "Can't cd to " + newcwd + ".");
            }
        }
    }

    public void doCommand() {
        String result = "\n";
        if (argv[0].equals("pwd")) {
            result = stater.getCwd() + "\n";
        } else if (argv[0].equals("cd")) {
            if (argv.length == 1) {
                stater.setCwd(stater.getVariable("HOME"));
            } else {
                String newcwd = argv[1];
                if (!argv[1].startsWith("/")) {
                    newcwd = stater.getCwd() + "/" + newcwd + "/";
                }
                newcwd = newcwd.replaceAll("//", "/");

                stater.setCwd(newcwd);
            }
        }
        out.print(result);
    }

    public boolean isProperCommand(String command) {
        return (command.startsWith("cd") ||
                command.startsWith("pwd"));
    }
}
