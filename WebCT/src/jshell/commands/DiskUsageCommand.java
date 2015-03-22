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
 * Print disk usage.
 */
public class DiskUsageCommand extends Command {
    public DiskUsageCommand() {
        name = "du";
    }

    public DiskUsageCommand(String[] argv, StateHolder stater,
                            PrintWriter out, BufferedReader in) {
        super(argv, stater, out, in, "du");
    }

    public void doCommand() {
        argv = JShellMisc.removeProgramName(argv);
        Getopt g = new Getopt("du", argv, "", new LongOpt[0]);
        g.setOpterr(false);
        String result = "";

        if (g.getOptind() == argv.length) {
            result = dodu("", stater.getCwd());
        } else {
            for (int i = g.getOptind(); i < argv.length; i++) {
                result += dodu(argv[i], stater.getCwd());
            }
        }
        result = result.replaceAll(stater.getCwd(), "");
        out.print(result);
    }

    public Command getCommand(String[] argv,
                              StateHolder stater,
                              PrintWriter out,
                              BufferedReader in) {
        return new DiskUsageCommand(argv, stater, out, in);
    }

    /**
     * Start recursive du.
     * Post: recursive du started.
     */
    private String dodu(String dirorfile, String cwd) {
        if (!dirorfile.startsWith("/")) {
            dirorfile = cwd + "/" + dirorfile;
        }
        File dufile = new File(dirorfile);
        return du(dufile);
    }

    /**
     * Recursively do du on directories, and for each file print its size
     * and absolute path.
     * Post: du values printed.
     */
    private String du(File file) {
        String result = "";
        if (file.isFile()) {
            result = fileSize(file) + "\t" + file.getAbsolutePath() +
                    "\n";
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    result += du(files[i]);
                }
            }
            result += duchildren(file) + "\t" + file.getAbsolutePath() +
                    "\n";
        }
        return result;
    }

    /**
     * Get the size of children.
     * Post: returns the total size of files beneath a parent dir.
     */
    private long duchildren(File file) {
        File[] files = file.listFiles();
        long size = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                size += fileSize(files[i]);
            } else if (files[i].isDirectory()) {
                size += duchildren(files[i]);
            }
        }
        return size;
    }

    /**
     * Returns the size of a file.
     * Post: size returned.
     */
    private long fileSize(File file) {
        long filesize = (long) file.length() / 1024;
        // round UP
        if (file.length() % 1024 > 0) filesize += 1;
        return filesize;
    }
}
