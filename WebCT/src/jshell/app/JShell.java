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

import jshell.commands.Command;
import jshell.util.BadShellInputException;
import jshell.util.JShellMisc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The class which runs the show for JSHELL.  However, it mostly just gets user
 * input and leaves the real work to Interpreter.
 *
 * @author Paul Heymann
 */
public class JShell extends Thread {
    //////////////////////////////////////////////////////////////
    // state
    private int myNumCommands = 0;
    private BufferedReader myConsole = new BufferedReader(
            new InputStreamReader(System.in));
    protected Interpreter myInterpreter = new Interpreter();
    private boolean finalExit = false;
    
    // private

    /**
     * Interpolate the csh prompt to the proper values.
     * Pre: given prompt has values like %/, %n, &c.
     * Post: %/ is changed to cwd, %n to username, and %1 to history
     * number.
     */
    private String interpolateCshPrompt(String prompt) {
        prompt = prompt.replaceFirst("%/", myInterpreter.getCwd());
        prompt =
                prompt.replaceFirst("%n",
                        myInterpreter.getVariable("USER"));
        prompt = prompt.replaceFirst("%1",
                Integer.toString(myNumCommands));
        return prompt;
    }

    /**
     * Run the user's jshellrc.
     * Post: jshellrc is executed.
     */
    private void doJshellrc(File jshellrc) {
        String[] lines = JShellMisc.fileToString(
                jshellrc.getAbsolutePath()).split("[\\n\\r]");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.trim().length() == 0 || line.startsWith("#")) continue;
            try {
                myInterpreter.processLine(line);
                myInterpreter.clean();
            } catch (BadShellInputException e) {
                System.err.println("Oops! " + e);
            }
        }
    }

    //////////////////////////////////////////////////////////////
    // public functions
    public void setFinalExit(boolean finalExit) {
        this.finalExit = finalExit;
    }

    public void addCommand(Command newcmd) {
        myInterpreter.addCommand(newcmd);
    }

    /**
     * Get a command from the user.
     * Post: a line of input from the user is returned.
     */
    public String getCommand() {
        try {
            myNumCommands++;
            String promptstring = myInterpreter.getVariable("PROMPT");
            System.out.print(interpolateCshPrompt(promptstring) + " ");
            return myConsole.readLine();
        } catch (IOException e) {
            System.err.println("*** error reading from console: " + e);
            return null;
        }
    }

    protected String getJShellRcFileName() {
        return ".jshellrc";
    }

    public static void main(String[] args) {
        JShell js = new JShell();
        js.run();
    }

    //////////////////////////////////////////////////////////////
    // Main
    public void run() {
        // create instance of this class to process input
        JShell shell = new JShell();
        String line = "";

        myInterpreter.makeCommandList();

        // do jshellrc related execution
        File jshellrc = new File(myInterpreter.getVariable("HOME") +
                "/" + getJShellRcFileName());
        if (jshellrc.exists()) {
            shell.doJshellrc(jshellrc);
        }

        // keep getting user input until they're done.
        while ((line = shell.getCommand()) != null &&
                !line.startsWith("logout") &&
                !line.startsWith("exit")) {
            if (line.trim().length() == 0 || line.startsWith("#")) continue;
            try {
                myInterpreter.processLine(line);
                if (!line.trim().startsWith("setenv")) {
                    myInterpreter.clean();
                }
            } catch (BadShellInputException e) {
                System.err.println("Error on input to the Admin " + e);
            }
        }

        if (finalExit) {
            System.out.println("User requested exit.\n" +
                    "Killing all threads with System.exit().");
            System.exit(0);
        }
    }
}
