/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ooga.graphics.sprites;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * REPLACEMENT CLASS FOR MOUSEDRAGWRAPPER.
 * IT INCLUDES KEYEVENTS.
 * Possibly interesting for further development.
 * @author Bart Kamphorst (Bart.Kamphorst@phil.uu.nl)
 */
public class MovementWrapper extends SpriteWrapper {

    public MovementWrapper (Sprite s) {
        super(s);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        super.keyReleased(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        super.keyTyped(e);
    }

    // Same as MouseDragWrapper
    
    @Override
    public void mousePressed(MouseEvent e) {
        setSpriteMiddle(e.getPoint());        
        repaint();
        super.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        setSpriteMiddle(e.getPoint());
        repaint();
        super.mouseReleased(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        setSpriteMiddle(e.getPoint());
        repaint();
        super.mouseDragged(e);
    }
}
