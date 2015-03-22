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

package ooga.graphics;

import ooga.logic.ManagerObserver;

/**
 * An image loading watcher to watch a managerobserver and update the
 * progress bar accordingly.
 *
 * @author paul@emptyrhetoric.com
 */
public class ImageLoadingWatcher extends Thread {
    double beginning, current, end;
    String job = "";
    SplashScreen s = null;
    ManagerObserver gmo = null;

    public ImageLoadingWatcher(SplashScreen s, ManagerObserver gmo,
                               int current, int end, String job) {
        this.beginning = current;
        this.current = current;
        this.end = end;
        this.job = job;
        this.s = s;
        this.gmo = gmo;
    }

    public void run() {
        while (gmo.running) {
            current = beginning +
                    ((double) gmo.status * 0.01) * (end - beginning);
            s.Update(job + " (" + (int) gmo.status + "%)", (int) current);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }
    }
}
