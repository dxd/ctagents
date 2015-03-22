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
import java.io.File;
import java.io.PrintWriter;

/**
 * List a directory or files.
 */
public class ListCommand extends Command {
    public ListCommand() {
        name = "ls";
    }

    public ListCommand(String[] argv, StateHolder stater,
                       PrintWriter out, BufferedReader in) {
        super(argv, stater, out, in, "ls");
    }

    public Command getCommand(String[] argv,
                              StateHolder stater,
                              PrintWriter out,
                              BufferedReader in) {
        return new ListCommand(argv, stater, out, in);
    }

    public void doCommand() {
        argv = JShellMisc.removeProgramName(argv);
        Getopt g = new Getopt("ls", argv, "aF", new LongOpt[0]);
        g.setOpterr(false);

        initializeValues();

        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'a':
                    listhidden = true;
                    break;
                case 'F':
                    listfolder = true;
                    break;
            }
        }

        if (g.getOptind() == argv.length) {
            if (stdin.length() == 0) {
                out.print(getListing("", stater.getCwd()));
            } else {
                String[] items = stdin.split("\\s");
                for (int i = 0; i < items.length; i++) {
                    out.print(getListing(items[i], stater.getCwd()));
                }
            }
        } else {
            for (int i = g.getOptind(); i < argv.length; i++) {
                out.print(getListing(argv[i], stater.getCwd()));
            }
        }
    }


    private boolean listhidden = false;
    private boolean listfolder = false;

    /**
     * Set listhidden or listfolder to false if they have been changed.
     * Post: set to false.
     */
    private void initializeValues() {
        listhidden = false;
        listfolder = false;
    }

    /**
     * A listing is outputted.
     * Post: listing outputted.
     */
    private String getListing(String input, String cwd) {
        if (!input.startsWith("/")) {
            input = cwd + input;
        }
        String result = "";
        File listfile = new File(input);
        if (!listfile.exists()) {
            result += listfile.getAbsolutePath() + " does not exist.\n";
            return result;
        }
        if (listfile.isFile()) {
            result += getFileLine(listfile);
        } else {
            File[] files = listfile.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().startsWith(".")) {
                    if (listhidden) {
                        result += getFileLine(files[i]);
                    }
                } else {
                    result += getFileLine(files[i]);
                }
            }
        }
        return result;
    }

    /**
     * Returns the line for a file, depending on options.
     * Post: line returned.
     */
    private String getFileLine(File file) {
        if (file.isDirectory()) {
            if (listfolder) {
                return file.getName() + "/\n";
            }
        }
        return file.getName() + "\n";
    }

    public boolean isProperCommand(String command) {
        return (command.startsWith("ls"));
    }
}
