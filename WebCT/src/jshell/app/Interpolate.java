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

package jshell.app;

import jshell.commands.StateHolder;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.Set;

/**
 * A file filter to deal with globbing file names in a directory.
 */
class GlobFilter implements FileFilter {
    private String glob;
    private String cwd;

    public GlobFilter(String myglob, String newcwd) {
        glob = myglob.replaceAll("\\.", "\\\\.");
        glob = glob.replaceAll("\\*", ".*");
        cwd = newcwd;
    }

    public boolean accept(File pathname) {
        return (pathname.getAbsolutePath().matches(glob) ||
                pathname.getAbsolutePath().matches(cwd + glob));
    }
}


/**
 * A class to deal with interpolation of the command line.
 */
public class Interpolate {
    private StateHolder stateholder;

    public Interpolate(StateHolder stateholder) {
        this.stateholder = stateholder;
    }

    /**
     * Return the line, but with $foo style variables interpolated to their
     * values.
     * Pre: line is a line without variables like $foo interpolated.
     * Post: variables are switched with their values.
     */
    public String interpolateVariables(String line) {
        Set mykeyset = stateholder.getVariableList();
        String oldline = "";
        final int MAX_INTERPOLATES = 1000;
        int interpolates = 0;

        // loops as long as interpolation is still going on, i.e. as long
        // as the line on the last round through was different from the line
        // this time through:
        while (!oldline.equals(line) && interpolates < MAX_INTERPOLATES) {
            oldline = line;
            Iterator iter = mykeyset.iterator();

            // run through all of the keys, switching $key to val of key
            while (iter.hasNext()) {
                String key = (String) iter.next();
                String tempold = "";
                while (!tempold.equals(line)) {
                    tempold = line;
                    line = line.replaceAll("\\" + "$" + key + "([ $])",
                            stateholder.getVariable(key) + "$1");
                }

                // yes, i could use replaceAll() here too.  if it worked.  ;-(
                if (line.matches("(.*)\\$" + key + "$")) {
                    line =
                            line.substring(0,
                                    line.length() - key.length() - 1);
                    line = line + stateholder.getVariable(key);
                }
            }
            interpolates++;
        }

        // change any remaining $variables to a blank string.
        // the end of a variable is either "$" or " "
        line = line.replaceAll("\\$[^ $]+([$ ])", "$1");
        if (line.matches("(.*)\\$[^ $]+$")) {
            line = line.replaceFirst("\\$[^ $]+$", "");
        }

        return line;
    }


    /**
     * Return the line, but with miscellaneous interpolations like ~.
     * Pre: line is a line without values like ~ or * interpolated.
     * Post: ~ and * and such are changed to the proper directory of files.
     */
    public String interpolateMisc(String line) {
        File cwddir = new File(stateholder.getCwd());

        // oh, and why not interpolate home too.
        line =
                line.replaceAll(" ~",
                        " " + stateholder.getVariable("HOME"));

        // change known .. . and //s
        String newline = "";
        while (!line.equals(newline)) {
            newline = line;
            line = line.replaceAll("/\\./", "/");
            line = line.replaceAll("//", "/");
            line = line.replaceFirst("/[^/]?[^/\\.][^/]+/\\.\\.", "");
            line = line.replaceFirst("http:/", "http://");
        }

        // globbing.  the basic deal here is that for each of the globs,
        // we filter the current working directory with the glob.  its not
        // as nice as a real shell, but for regular use its not bad.  doing
        // cases like /*/*.foo would be really hard, though presumably
        // foo/*.foo wouldn't be too bad.
        String[] args = line.split("\\s");
        line = "";
        for (int i = 0; i < args.length; i++) {
            if (args[i].matches(".*\\*.*")) {
                File[] files = cwddir.listFiles(
                        new GlobFilter(args[i], stateholder.getCwd()));
                for (int j = 0; j < files.length; j++) {
                    line += files[j].getAbsolutePath() + " ";
                }
                if (files.length == 0) {
                    // noglob is just a string to let us know that globbing
                    // failed and that we should throw if we have all noglobs
                    // to a command.
                    line += "noglob" + " ";
                }
            } else {
                line += args[i] + " ";
            }
        }
        return line;
    }
}
