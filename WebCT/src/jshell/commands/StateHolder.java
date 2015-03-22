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

import java.io.PrintWriter;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

/**
 * An interface which describes how to hold jshell state.
 */
public interface StateHolder {
    // variable stuff:

    /**
     * Set a variable to a value.
     */
    public void setVariable(String varname, String value);

    /**
     * Get a variable's value based on its name.
     */
    public String getVariable(String varname);

    /**
     * Get a list of all of the variable names.
     */
    public Set getVariableList();

    /**
     * Check if a variable is set, and output an appropriate error message
     * if not.
     *
     * @author Paul Heymann (ctsource@heymann.be)
     */
    public boolean checkVariableIsSet(String variable, String value,
                                      PrintWriter out);


    
    // cwd stuff:

    /**
     * Set the current working directory to a string.
     */
    public void setCwd(String newcwd);

    /**
     * Get the current working directory.
     */
    public String getCwd();

    // dirs stuff:

    /**
     * Push a value to the directory stack.
     */
    public void pushToDirStack(String dir);

    /**
     * Pop a value from the directory stack.
     */
    public String popFromDirStack();

    /**
     * Get the actual directory stack.
     */
    public Stack getDirStack();


    
    // history stuff:

    /**
     * Get the history of commands.
     */
    public Vector getHistory();
}
