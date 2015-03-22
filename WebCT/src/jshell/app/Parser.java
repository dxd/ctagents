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

import jshell.util.BadShellInputException;

import java.util.Iterator;
import java.util.Vector;

/**
 * A class to deal with the parsing of user input on the command
 * line.  However, does not deal with interpolation of variables or
 * special characters like ~.
 */
public class Parser {
    /**
     * Convert our command into argv format.
     * Pre: command is a string.
     * Post: command is an array of strings like argv.
     */
    public static String[] getArgv(String command) {
        String[] tempargv = command.split("\\s");
        Vector tempv = new Vector();
        for (int i = 0; i < tempargv.length; i++) {
            tempv.addElement(tempargv[i].trim());
        }
        while (tempv.remove("")) {
            ;
        }
        String[] newargv = new String[tempv.size()];
        Iterator iter = tempv.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            newargv[i] = (String) iter.next();
        }
        return newargv;
    }

    /**
     * Move the input redirect to the front, so we can just evaluate the
     * commands in order.
     * Pre: usercommands is a vector of all of the commands that
     * are piped together by the user.
     * Post: the < redirect, grabbing input from a file, is put at the
     * beginning of the vector.
     */
    public static Vector moveRedirects(Vector usercommands) {
        Vector fixedvector = new Vector();
        String[] susercommands =
                (String[]) usercommands.toArray(
                        new String[usercommands.size()]);
        int inputRedirect = -1;
        for (int i = 0; i < susercommands.length; i++) {
            if ((susercommands[i]).length() > 1 &&
                    (susercommands[i]).substring(0, 1).equals("<")) {
                inputRedirect = i;
            } else {
                fixedvector.addElement(susercommands[i]);
            }
        }
        if (inputRedirect > -1) {
            fixedvector.insertElementAt(susercommands[inputRedirect], 0);
        }
        return fixedvector;
    }

    /**
     * Parse the last item in the user's command for "foo < bar > baz" nonsense.
     * This gets a little ridiculous, so you mostly just need to understand
     * that < and > aren't in any particular order, so there are 4 different
     * cases based on where they are.  At the end of parseLastItem we end up
     * with vector having 1,2, or 3 new items, 1 of which is the last command,
     * and the other two are two files beginning with < or >.
     * Pre: lastitem is the last command in the user's input string.
     * Post: lastitem is separated into last command and 1 or 2 <file or >file
     * strings depending on whether the user wanted input or output redirection.
     */
    public static Vector parseLastItem(String lastitem) {
        Vector eachcommand = new Vector();
        if (lastitem.matches(".+[<>].+")) {
            String[] outputcommands =
                    lastitem.split("[>]");
            if (outputcommands.length == 1) {
                String[] inputcommands =
                        outputcommands[0].split("[<]");
                eachcommand.addElement(inputcommands[0].trim());
                eachcommand.addElement("<" + inputcommands[1].trim());
            } else {
                String[] inputcommands;
                if (outputcommands[1].split("[<]").length == 2) {
                    inputcommands = outputcommands[1].split("[<]");
                    eachcommand.addElement(outputcommands[0].trim());
                    eachcommand.addElement(">" + inputcommands[0].trim());
                    eachcommand.addElement("<" + inputcommands[1].trim());
                } else if (outputcommands[0].split("[<]").length == 2) {
                    inputcommands = outputcommands[0].split("[<]");
                    eachcommand.addElement(inputcommands[0].trim());
                    eachcommand.addElement("<" + inputcommands[1].trim());
                    eachcommand.addElement(">" + outputcommands[1].trim());
                } else {
                    eachcommand.addElement(outputcommands[0].trim());
                    eachcommand.addElement(">" + outputcommands[1].trim());
                }
            }
        } else {
            eachcommand.addElement(lastitem.trim());
        }
        return eachcommand;
    }

    /**
     * Parse the inputted line into a vector of commands, separated by |s.
     * Pre: line is a line of input from the user.
     * Post: line is returned as a vector of commands separated by pipes.
     */
    public static Vector parseLine(String line)
            throws BadShellInputException {
        if (line.matches("[^>]*[>][^>]*[>].*")) {
            throw new BadShellInputException("Too Many '>'s.");
        } else if (line.matches("[^<]*[<][^<]*[<].*")) {
            throw new BadShellInputException("Too Many '<'s.");
        }

        String[] usercommands = line.split("[|]");
        Vector eachcommand = new Vector();
        for (int i = 0; i < usercommands.length - 1; i++) {
            eachcommand.addElement(usercommands[i].trim());
        }

        if (!line.startsWith("setenv")) {
            eachcommand.addAll(parseLastItem(
                    usercommands[usercommands.length - 1]));
        } else {
            eachcommand.addElement(usercommands[usercommands.length - 1]);
        }

        return eachcommand;
    }
}
