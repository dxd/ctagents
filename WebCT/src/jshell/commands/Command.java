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

import java.io.*;

/**
 * Each command (cat, head, &c) is a subclass of this class.
 * They are threads so advanced i/o could be done later easily,
 * i.e. line by line piping, or as the user inputs.
 */
abstract public class Command extends Thread {

    // vars:
    protected PrintWriter out = null;
    protected BufferedReader in = null;
    protected String[] argv;
    protected String name = "not_a_command";
    protected String stdin = "";
    protected StateHolder stater;

    // constructors:
    public Command() {
    }

    public Command(String[] argv, StateHolder stater,
                   PrintWriter out, BufferedReader in,
                   String name) {
        this.out = out;
        this.in = in;
        this.stater = stater;
        this.argv = argv;
        this.name = name;
    }


    //
    // Interpreter related commands:
    //
    
    /**
     * Main function for setting up a command.  Takes argv,
     * a stateholder (which ends up being interpreter) and a
     * source to read from, starts a thread and returns the
     * reader to read from the thread.  It also only uses
     * the inputted source if the command needs stdin based
     * on its arguments and whatever other information it has.
     * Pre: command is not created yet.
     * Post: starts a new thread of this command, possibly with a
     * specified source input, and returns a way to read from the
     * commands output.
     */
    public Reader setUp(String[] argv,
                        StateHolder stater,
                        Reader source)
            throws BadShellInputException {
        BufferedReader in = new BufferedReader(source);

        PipedWriter pipeOut = new PipedWriter();
        PipedReader pipeIn = new PipedReader();

        try {
            pipeOut = new PipedWriter();
            pipeIn = new PipedReader(pipeOut);

        } catch (IOException e) {
            System.out.println(e);
        }

        PrintWriter out = new PrintWriter(pipeOut);

        // this is so we can throw, which we can't do later without
        // using threadgroup.  we check for obvious errors in commands
        // with horribledeath.
        getCommand(argv, stater, new PrintWriter(System.out),
                new BufferedReader(new InputStreamReader(System.in)))
                .horribleDeath();

        // if it needs stdin, than pass it stdin, otherwise just give it
        // a blank stringreader so the user isn't nagged for input
        if (getCommand(argv, stater, new PrintWriter(System.out),
                new BufferedReader(new InputStreamReader(System.in)))
                .needsStdin()) {
            getCommand(argv, stater, out, in).start();
        } else {
            getCommand(argv, stater, out,
                    new BufferedReader(new StringReader(""))).start();
        }
        return pipeIn;
    }

    /**
     * Let the command throw if it needs to.
     * Post: if command input is broken, an exception is thrown.
     */
    public void horribleDeath() throws BadShellInputException {
    }

    /**
     * The thread code for the command, by default.
     * Post: by default, just grabs all of the input and then
     * lets doCommand() work with the stdin string.
     */
    public void run() {
        stdin = "";
        if (out != null && in != null) {
            try {
                String input;
                while ((input = in.readLine()) != null) {
                    stdin += input + "\n";
                }
                doCommand();
                out.flush();
                out.close();
            } catch (IOException e) {
                System.err.println("Command run: " + e);
            }
        }
    }

    /**
     * Used by run by default.
     * Post: the command has done its thing.
     */
    public abstract void doCommand();


    /**
     * Get a command.  This is abstract since it requires new.
     * Post: a new command of the same type is returned.
     */
    public abstract Command getCommand(String[] argv,
                                       StateHolder stater,
                                       PrintWriter out,
                                       BufferedReader in);

    /**
     * Determines if this is the proper command for the command described
     * in command.
     * Post: returns whether it is the proper command.
     */
    public boolean isProperCommand(String command) {
        return (command.startsWith(name));
    }

    /**
     * Determines if the command needs stdin.  Defaults to false.
     * Post: returns whether it needs stdin.
     */
    public boolean needsStdin() {
        return false;
    }

    
    // 
    // misc. for commands
    //
    
    /**
     * Check the arguments of a function for a certain number of args.
     * Post: throws an error if not between fewestArgs and mostArgs.
     */
    protected void checkArgs(String[] argv, int fewestArgs,
                             int mostArgs, String programName)
            throws BadShellInputException {
        if (argv.length < fewestArgs || argv.length > mostArgs) {
            throw new BadShellInputException("Wrong number of args: " +
                    programName +
                    " takes between "
                    + fewestArgs +
                    " and " +
                    mostArgs +
                    " arguments.");
        }
    }

    /**
     * Check the arguments of a function for a certain number of args.
     * Post: throws an error if not greater than or equal to minArgs.
     */
    protected void minArgs(String[] argv, int minArgs, String programName)
            throws BadShellInputException {
        if (argv.length < minArgs) {
            throw new BadShellInputException("Wrong number of args: " +
                    programName +
                    " takes >= "
                    + minArgs + " arguments.");
        }
    }

    /**
     * Check the arguments of a function for a certain number of args.
     * Post: throws an error if not less than or equal to maxArgs.
     */
    protected void maxArgs(String[] argv, int maxArgs, String programName)
            throws BadShellInputException {
        if (argv.length > maxArgs) {
            throw new BadShellInputException("Wrong number of args: " +
                    programName +
                    " takes <= "
                    + maxArgs + " arguments.");
        }
    }
}
