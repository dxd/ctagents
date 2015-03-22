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

import jshell.commands.*;
import jshell.util.BadShellInputException;

import java.io.*;
import java.util.*;

/**
 * Main class, interprets a line of input and keeps state.
 */
public class Interpreter implements StateHolder {
    private Vector commands = new Vector();
    private Vector history = new Vector();
    private Map variables = new HashMap();
    private String cwd = new String();
    private Stack dirs = new Stack();

    /**
     * Add all of the commands we know about to the commands vector.
     * Pre: command list has not yet been made.
     * Post: puts all of the known commands into the commands vector.
     */
    public void makeCommandList() {
        commands.addElement(new CatCommand());
        commands.addElement(new CdPwdCommand());
        commands.addElement(new CutCommand());
        commands.addElement(new DateCommand());
        commands.addElement(new DiskUsageCommand());
        commands.addElement(new EchoCommand());
        commands.addElement(new FileOutputCommand());
        commands.addElement(new GrepCommand());
        commands.addElement(new HeadOrTailCommand());
        commands.addElement(new HistoryCommand());
        commands.addElement(new ListCommand());
        commands.addElement(new PrintenvCommand());
        commands.addElement(new PushdPopdDirsCommand());
        commands.addElement(new SetenvCommand());
        commands.addElement(new SortCommand());
        commands.addElement(new UniqCommand());
        commands.addElement(new WcCommand());
    }

    public void addCommand(Command newcmd) {
        commands.addElement(newcmd);
    }

    /**
     * Insert HOME, USER, and PROMPT.
     * Pre: HOME, USER, and PROMPT aren't set.
     * Post: HOME, USER, and PROMPT are set to defaults and environment.
     */
    private void insertDefaultUserVariables() {
        // current directory --- for choosing where to start
        if (cwd.length() == 0) {
            String tempcwd = System.getProperty("user.dir");
            tempcwd += "/";
            tempcwd = tempcwd.replaceAll("//", "/");
            // Support windows
            tempcwd = tempcwd.replaceAll("\\\\", "/");
            setCwd(tempcwd);

        }

        // user's account name --- for displaying in prompt
        if (getVariable("USER").length() == 0) {
            setVariable("USER", System.getProperty("user.name"));
        }

        // user's home directory --- for expanding ~
        if (getVariable("HOME").length() == 0) {
            setVariable("HOME", System.getProperty("user.home"));
        }

        if (getVariable("PROMPT").length() == 0) {
            setVariable("PROMPT", "%n:%/%");
        }
    }

    /**
     * Just a constructor.
     */
    public Interpreter() {
        // set properties
        insertDefaultUserVariables();
    }


    // StateHolder implementation follows:
    /**
     * Clean cwd to remove /../, /./, and other miscreants.
     * Pre: /../ /./ and other miscreants are in the cwd.
     * Post: these miscreants are removed.
     */
    public void clean() {
        String newcwd = "";
        while (!cwd.equals(newcwd)) {
            newcwd = cwd;
            cwd = cwd.replaceAll("/\\./", "/");
            cwd = cwd.replaceAll("//", "/");
            cwd = cwd.replaceFirst("/[^/]?[^/\\.][^/]+/\\.\\.", "");
        }
    }

    public void setVariable(String varname, String value) {
        variables.put(varname, value);
    }

    public String getVariable(String varname) {
        String value = (String) variables.get(varname);
        if (value != null) {
            return value;
        }
        return "";
    }

    public boolean checkVariableIsSet(String variable, String value,
                                      PrintWriter out) {
        if (getVariable(variable).length() != 0) {
            return true;
        }

        out.println(variable + " is not set.\n" +
                "Run 'setenv " + variable + " " + value + "'.");
        return false;
    }


    public Set getVariableList() {
        return variables.keySet();
    }

    public void setCwd(String newcwd) {
        cwd = newcwd;
    }

    public String getCwd() {
        return cwd;
    }

    public void pushToDirStack(String dir) {
        dirs.push(dir);
    }

    public String popFromDirStack() {
        return (String) dirs.pop();
    }

    public Stack getDirStack() {
        return dirs;
    }

    public Vector getHistory() {
        return history;
    }
    // End StateHolder implementation.


    /**
     * Test for the case in which there are no results for a glob.  We need
     * to catch this since otherwise we end up with unexpected results for
     * things like "ls *".
     * Pre: argv is the argv of a command to be tested for globbing errors.
     * Post: throws if there is a line stopping globbing error.
     */
    private void testForGlobbingError(String[] argv)
            throws BadShellInputException {
        if (argv.length > 1 &&
                argv[1].equals("noglob")) {
            boolean nofilesforglob = true;

            for (int i = 2; i < argv.length; i++) {
                if (!argv[i].equals("noglob")) {
                    nofilesforglob = false;
                }
            }
            if (nofilesforglob) {
                throw new BadShellInputException(
                        "There weren't any results for your glob.");
            }
        }
    }

    /**
     * Remove icky noglobs from the commands, used to make sure we don't have
     * weird glob related bugs when nothing matches.
     * Pre: argv has 0 or more strings named "noglob".
     * Post: these strings are removed.
     */
    private String[] removeNoGlobs(String[] argv) {
        Vector v = new Vector();
        for (int i = 0; i < argv.length; i++) {
            if (!argv[i].equals("noglob")) {
                v.addElement(argv[i]);
            }
        }
        argv = (String[]) v.toArray(new String[v.size()]);
        return argv;
    }

    /**
     * Parse line and process commands.
     * Pre: line is a line of user input straight from the user
     * Post: the line is processed and executed.
     */
    public void processLine(String line)
            throws BadShellInputException {
        Iterator cmditer, useriter;
        Interpolate interpolate = new Interpolate(this);
        Reader input = new InputStreamReader(System.in);

        try {
            // add line to history
            history.addElement(line);

            // interpolate for everything but setenv.  setenv should allow
            // things like "setenv FOO $bar$baz"
            if (!line.startsWith("setenv")) {
                line = interpolate.interpolateVariables(line);
            }

            // interpolate in miscellaneous things like ~, .., &c.
            line = interpolate.interpolateMisc(line);

            // get a vector of the piped together commands, by parsing
            // them and then moving redirects to the proper order.
            Vector eachcommand = Parser.moveRedirects(
                    Parser.parseLine(line));
            useriter = eachcommand.iterator();

            // go through each of the commands the user inputted:
            while (useriter.hasNext()) {

                // get the current command in the list of user inputted commands
                String currentcommand = (String) useriter.next();
                currentcommand = currentcommand.trim();

                // test for a globbing error in the current command:
                String[] argv = Parser.getArgv(currentcommand);
                testForGlobbingError(argv);
                argv = removeNoGlobs(argv);

                // run through available commands, testing if they will take
                // the user inputted command:
                cmditer = commands.iterator();
                while (cmditer.hasNext()) {

                    // use a command in the command list
                    Command tempcommand = (Command) cmditer.next();

                    // ... and test if it is the proper command for the users
                    // input:
                    if (tempcommand.isProperCommand(currentcommand)) {

                        // set up a new piped thread which represents this
                        // command.  setup returns a reader and takes a reader,
                        // and it will either use the initial System.in input
                        // or not based on whether the command needs it.
                        input = tempcommand.setUp(argv, this, input);

                        // set cmditer to start so we know if we were
                        // successful or not (see below)
                        cmditer = commands.iterator();
                        break;
                    }
                }

                // oops, command wasn't matched.  just return.  why bother
                // evaluating a broken user line?
                if (!cmditer.hasNext()) {
                    System.err.println("Bad Command: " + currentcommand);
                    return;
                }
            }

            // print our output from the piped commands:
            BufferedReader in = new BufferedReader(input);
            String inputstring;
            while ((inputstring = in.readLine()) != null) {
                System.out.println(inputstring);
            }
            in.close();
        } catch (jshell.util.BadShellInputException e) {
            throw new BadShellInputException(e.getMessage());
        } catch (IOException e) {
            System.out.println("Oops! " + e);
        }
    }
}
