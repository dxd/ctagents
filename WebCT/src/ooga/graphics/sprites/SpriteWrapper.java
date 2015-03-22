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

import net.javage.twoD.TransparencyMap;
import ooga.graphics.GraphicsManager;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Observer;
import java.util.Random;

/**
 * A spritewrapper implements the decorator pattern on a sprite, allowing a
 * sprite to have its behavior modified by something which descends from
 * spritewrapper.
 *
 * @author paul@emptyrhetoric.com
 * @author <A HREF="mailto:jrj2@duke.edu">Jinendra Raja Jain</A>
 */
public class SpriteWrapper extends Sprite {
    protected Sprite wrappedSprite = null;

    public SpriteWrapper(Sprite s) {
        wrappedSprite = s;
    }

    public void move(long ticks) {
        wrappedSprite.move(ticks);
    }

    public void collisionWith(Sprite s) {
        wrappedSprite.collisionWith(s);
    }

    public TransparencyMap getTransparencyMap() {
        return wrappedSprite.getTransparencyMap();
    }

    // forward manager requests:
    public GraphicsManager getGraphicsManager() {
        return wrappedSprite.getGraphicsManager();
    }
//     public SoundManager getSoundManager() {
//         return wrappedSprite.getSoundManager();
//     }
    
    public int getNumImages() {
        return wrappedSprite.getNumImages();
    }

    public int getPointVal() {

        return wrappedSprite.getPointVal();
    }

    public void setPointVal(int v) {
        wrappedSprite.setPointVal(v);
    }

    // forward the current image sequence num:
    public int getImageNum() {
        return wrappedSprite.getImageNum();
    }

    public Image getImage() {
        return wrappedSprite.getImage();
    }

    public void setImageNum(int num) {
        wrappedSprite.setImageNum(num);
    }

    public int getIDnum() {
        return wrappedSprite.getIDnum();
    }

    public void setImageID(int ID) {
        wrappedSprite.setImageID(ID);
    }

    public String getDescription() {
        return wrappedSprite.getDescription();
    }

    // forward parent component related stuff:
    public Component getParent() {
        return wrappedSprite.getParent();
    }

    public void setParent(Component playfield) {
        wrappedSprite.setParent(playfield);
    }

    public Random getRandgen() {
        return wrappedSprite.getRandgen();
    }

    // forwards bounds related stuff:
    public void setBounds(Rectangle bounds) {
        wrappedSprite.setBounds(bounds);
    }

    public Rectangle getBounds() {
        return wrappedSprite.getBounds();
    }

    public void translate(int x, int y) {
        wrappedSprite.translate(x, y);
    }

    public void translate(double x, double y) {
        wrappedSprite.translate(x, y);
    }

    public Point getSpriteMiddle() {
        return wrappedSprite.getSpriteMiddle();
    }

    public void setSpriteMiddle(Point p) {
        wrappedSprite.setSpriteMiddle(p);
    }

    // forward painting stuff:
    public void paintComponent(Graphics g, Component p) {
        wrappedSprite.paintComponent(g, p);
    }

    public void repaint() {
        wrappedSprite.repaint();
    }


    // forward observer stuff:
    public void addObserver(Observer o) {
        wrappedSprite.addObserver(o);
    }

    public void notifyObservers(Object arg) {
        wrappedSprite.notifyObservers(arg);
    }

    public void notifyObservers() {
        wrappedSprite.notifyObservers();
    }

    protected void setChanged() {
        wrappedSprite.setChangedForWrappers();
    }

    public void setChangedForWrappers() {
        setChanged();
    }

    public boolean hasChanged() {
        return wrappedSprite.hasChanged();
    }

    public void deleteObservers() {
        wrappedSprite.deleteObservers();
    }

    public void deleteObserver(Observer o) {
        wrappedSprite.deleteObserver(o);
    }

    public int countObservers() {
        return wrappedSprite.countObservers();
    }

    protected void clearChanged() {
        wrappedSprite.clearChangedForWrappers();
    }

    public void clearChangedForWrappers() {
        clearChanged();
    }


    // forward key stuff:
    public void keyPressed(KeyEvent e) {
        wrappedSprite.keyPressed(e);
    }

    public void keyReleased(KeyEvent e) {
        wrappedSprite.keyReleased(e);
    }

    public void keyTyped(KeyEvent e) {
        wrappedSprite.keyTyped(e);
    }

    public void mouseClicked(MouseEvent e) {
        wrappedSprite.mouseClicked(e);
    }

    public void mouseEntered(MouseEvent e) {
        wrappedSprite.mouseEntered(e);
    }

    public void mouseExited(MouseEvent e) {
        wrappedSprite.mouseExited(e);
    }

    public void mousePressed(MouseEvent e) {
        wrappedSprite.mousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
        wrappedSprite.mouseReleased(e);
    }

    public void mouseDragged(MouseEvent e) {
        wrappedSprite.mouseDragged(e);
    }

    public void mouseMoved(MouseEvent e) {
        wrappedSprite.mouseMoved(e);
    }
}
