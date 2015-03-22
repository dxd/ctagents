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
package jshell.util;

import java.io.*;

/**
 * Several miscellaneous Jshell related commands that didn't fit
 * elsewhere.
 */
public class JShellMisc {
    /**
     * Read a file into a string.
     * Pre: filename is the path to a real file.
     * Post: contents of file are returned as a string.
     */
    static public String fileToString(String filename) {
        try {
            BufferedReader r = new BufferedReader(
                    new FileReader(filename));

            String result = "";
            String line = r.readLine();
            while (line != null) {
                result += line + "\n";
                line = r.readLine();
            }
            return result;
        } catch (Exception e) {
            System.err.println(e);
            return "";
        }
    }

    /**
     * Write a file from a string.
     * Pre: filename is the path to a real file.
     * Post: contents of output string are written to the file.
     */
    static public void stringToFile(String filename, String output) {
        try {
            PrintWriter dataOut =
                    new PrintWriter(new FileWriter(filename), false);

            dataOut.print(output);
            dataOut.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Remove the first item from argv, which is the program name.
     * Pre: argv contains a program name.
     * Post: argv without program name is returned.
     */
    static public String[] removeProgramName(String[] argv) {
        String[] newargv = new String[argv.length - 1];
        for (int i = 1; i < argv.length; i++) {
            newargv[i - 1] = argv[i];
        }
        return newargv;
    }
}
