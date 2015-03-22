/*
	Object-Oriented Game Architecture
	Copyright (C) 2002 by Paul Heymann, Matthew Hall, Jinendra Raja Jain, and Trey Williams

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

package ooga.logic;


/**
 * A basic observer struct for use in seeing what state a method has progressed
 * to and see if it is still running (the method sets the observer struct,
 * it doesn't really do the observer pattern).  Used by, among other things,
 * the ImageLoadingWatcher.
 *
 * @author paul@emptyrhetoric.com
 */
public class ManagerObserver {
    public double status = 0;
    public boolean running = false;
}
