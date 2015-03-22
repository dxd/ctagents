/*
	Colored Trails
	
	Copyright (C) 2006, President and Fellows of Harvard College.  All Rights Reserved.
	
	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package edu.harvard.eecs.airg.coloredtrails.shared.exceptions;

import java.io.PrintWriter;

/**
 * An ExceptionWriter which prints its output to a PrintWriter passed at
 * initialization.
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public class PrintWriterExceptionWriter implements ExceptionWriter {
    private PrintWriter out = null;

    public PrintWriterExceptionWriter(PrintWriter out) {
        this.out = out;
    }

    public void write(String error,
                      Exception e) {
        out.print(error);
        out.println(".\nStack trace follows.");
        out.println(e.getMessage());
    }
}
