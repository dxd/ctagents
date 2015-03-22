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

import jshell.util.JShellMisc;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Only one of each duplicated line is printed.
 */
public class UniqCommand extends Command {
    public UniqCommand() {
        name = "uniq";
    }

    public UniqCommand(String[] argv, StateHolder stater,
                       PrintWriter out, BufferedReader in) {
        super(argv, stater, out, in, "uniq");
    }

    public void doCommand() {
        String[] lines = new String[0];
        if (argv.length == 1) {
            lines = stdin.split("[\\n\\r]");
        } else {
            Vector filelines = new Vector();
            for (int i = 1; i < argv.length; i++) {
                String[] thisfilelines =
                        JShellMisc.fileToString(argv[i]).split("[\\n\\r]");
                for (int j = 0; j < thisfilelines.length; j++) {
                    filelines.addElement(thisfilelines[j]);
                }
            }
            lines = (String[]) filelines.toArray(lines);
        }

        // duvall just told me to use a Set for this.
        // might as well have it sorted while we're at it.
        Set uniqlines = new TreeSet();
        for (int i = 0; i < lines.length; i++) {
            uniqlines.add(lines[i]);
        }

        Iterator iter = uniqlines.iterator();
        while (iter.hasNext()) {
            out.println((String) iter.next());
        }
    }

    public Command getCommand(String[] argv,
                              StateHolder stater,
                              PrintWriter out,
                              BufferedReader in) {
        return new UniqCommand(argv, stater, out, in);
    }

    public boolean needsStdin() {
        return (argv.length == 1);
    }
}
