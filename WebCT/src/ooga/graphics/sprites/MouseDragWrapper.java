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

package ooga.graphics.sprites;

import java.awt.event.MouseEvent;

/**
 * A wrapper to allow for dragging of sprites using the mouse.
 *
 * @author paul@emptyrhetoric.com
 */
public class MouseDragWrapper extends SpriteWrapper {
    public MouseDragWrapper(Sprite s) {
        super(s);
    }

    public void mousePressed(MouseEvent e) {
        setSpriteMiddle(e.getPoint());
        repaint();
        super.mousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
        setSpriteMiddle(e.getPoint());
        repaint();
        super.mouseReleased(e);
    }

    public void mouseDragged(MouseEvent e) {
        setSpriteMiddle(e.getPoint());
        repaint();
        super.mouseDragged(e);
    }
    
//    public void mouseMoved(MouseEvent e) {}
}
