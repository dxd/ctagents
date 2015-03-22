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

import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Observable;
import java.util.Random;


/**
 * A fairly basic sprite class, which holds an image or series of images which
 * is drawn on the screen.
 *
 * @author paul@emptyrhetoric.com
 * @author <A HREF="mailto:jrj2@duke.edu">Jinendra Raja Jain</A>
 */
public class Sprite extends Observable
        implements KeyListener, MouseInputListener, Cloneable {
    // bounds and oldbounds for drawing and repainting:
    Rectangle2D bounds = null;
    Rectangle2D oldbounds = null;


    // currently used image and transparency map:
    Image spriteImage = null;
    TransparencyMap transparencyMap = null;

    // sprite's object id:
    int spriteid = 0;

    // sprite's point value, if any:
    int myVal = 0;

    // sprite's image id:
    int imageid = -1;
    int imageSequenceNumber = 0;

    // sprite's description string:
    String myDescription = new String();

    // references to related objects:
    Component parent = null;
    GraphicsManager gm = null;

    //random number generator used in various Sprites
    static Random randgen = new Random();

    public Sprite() {
    }

    public Sprite(Component playfield, String description,
                  GraphicsManager gm) {
        parent = playfield;
        //	spriteid = parent.getSpriteManager().nextID();
        myDescription = description;
        this.gm = gm;
        //	this.sm = sm;
    }

    public Sprite(Component playfield, String description, GraphicsManager gm,
                  int imageID, Rectangle bounds) {
        parent = playfield; 
        //	spriteid = parent.getSpriteManager().nextID();
        myDescription = description;
        this.gm = gm;
        //	this.sm = sm;
        setImageID(imageID);
        setBounds(bounds);
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen
            throw new InternalError(e.toString());
        }
    }

    /**
     * Sprite gets an opportunity to move every tick.
     *
     * @author paul@emptyrhetoric.com
     */
    public void move(long ticks) {
    }

    /**
     * Sprite gets notified when it collides with another sprite.
     * If sprite is null, then collided with the side of the playfield.
     *
     * @author paul@emptyrhetoric.com
     */
    public void collisionWith(Sprite s) {
    }

    /**
     * Return the transparency map associated with this sprite.
     *
     * @author paul@emptyrhetoric.com
     */
    public TransparencyMap getTransparencyMap() {
        TransparencyMap tm = (TransparencyMap) transparencyMap.clone();
        tm.setX((int) bounds.getX());
        tm.setY((int) bounds.getY());
        return tm;
    }

    /**
     * Returns this sprite's graphics manager.
     *
     * @author paul@emptyrhetoric.com
     */
    public GraphicsManager getGraphicsManager() {
        return gm;
    }
    
    /**
     * Returns this sprite's sound manager.
     * @author paul@emptyrhetoric.com
     */
    //     public SoundManager getSoundManager() {
    //         return sm;
    //     }
    
    /**
     * Get the number of images in the sequence of images which the sprite
     * was initialized with.
     *
     * @author paul@emptyrhetoric.com
     */
    public int getNumImages() {
        return gm.getNumImages(imageid);
    }

    /**
     * Get the current image sequence number which the sprite is displaying.
     *
     * @author paul@emptyrhetoric.com
     */
    public int getImageNum() {
        return imageSequenceNumber;
    }

    /**
     * Set the image number in the sequence of images which the sprite was
     * initialized with.
     *
     * @author paul@emptyrhetoric.com
     */
    public void setImageNum(int num) {
        spriteImage = (Image) gm.getImage(imageid, num);
        transparencyMap = (TransparencyMap) gm.getTM(imageid, num);
        imageSequenceNumber = num;
    }

    /**
     * Set the sprite's image to another image, based on a given image id.
     *
     * @author paul@emptyrhetoric.com
     */
    public void setImageID(int ID) {
        imageid = ID;
        spriteImage = (Image) gm.getImage(imageid, 0);
        transparencyMap = (TransparencyMap) gm.getTM(imageid, 0);
    }

    /**
     * Get the object/sprite ID number for this sprite.
     *
     * @author Trey Williams (rew6@duke.edu)
     */
    public int getIDnum() {
        return new Integer(spriteid).intValue();
    }

    /**
     * Get the description for this sprite.
     *
     * @author Trey Williams (rew6@duke.edu)
     */
    public String getDescription() {
        return new String(myDescription);
    }

    /**
     * Get the parent for this sprite.
     *
     * @author Trey Williams (rew6@duke.edu)
     */
    public Component getParent() {
        return parent;
    }

    /**
     * Get the Point Value  for this sprite.
     *
     * @author Trey Williams (rew6@duke.edu)
     */
    public int getPointVal() {
        return myVal;
    }

    /**
     * Get the image for this sprite.
     *
     * @author Trey Williams (rew6@duke.edu)
     */
    public Image getImage() {
        return (Image) gm.getImage(imageid, 0);
    }


    /**
     * Set the parent of the sprite to a playfield
     *
     * @author Trey Williams (rew6@duke.edu)
     */
    public void setParent(Component playfield) {
        parent = playfield;
        //        spriteid = parent.getSpriteManager().nextID();
    }

    /**
     * Sets the Point Value  for this sprite.
     *
     * @author Trey Williams (rew6@duke.edu)
     */
    public void setPointVal(int v) {
        myVal = v;
    }

    /**
     * Set the bounds of this particular image.
     *
     * @author paul@emptyrhetoric.com
     */
    public void setBounds(Rectangle bounds) {
        this.oldbounds = this.bounds;
        this.bounds = bounds;
    }

    /**
     * Get the bounds of the sprite.
     *
     * @author paul@emptyrhetoric.com
     */
    public Rectangle getBounds() {
        return bounds.getBounds();
    }

    /**
     * Get the middle of the sprite as a point.  Useful for centered moving,
     * locating, etc.
     *
     * @author Paul Heymann (paul@emptyrhetoric.com)
     */
    public Point getSpriteMiddle() {
        Rectangle b = bounds.getBounds();
        return new Point(b.x + b.width / 2, b.y + b.height / 2);
    }

    public void setSpriteMiddle(Point p) {
        Rectangle b = getBounds();
        // nudge them a little randomly - Rani  unnudge - Paul
        b.x = p.x - b.width / 2;
        //int plusMinus1 = (randgen.nextDouble() > 0.5 ? 1 : -1);
        //b.x += plusMinus1 * b.width / 4;

        b.y = p.y - b.height / 2;
        //plusMinus1 = (randgen.nextDouble() > 0.5 ? 1 : -1);
        //b.y += plusMinus1 * b.height / 4;
        setBounds(b);
    }

    /**
     * Get the bounds of the sprite.
     *
     * @author paul@emptyrhetoric.com
     */
    public Rectangle2D getBounds2D() {
        return bounds;
    }

    public Random getRandgen() {
        return randgen;
    }

    /**
     * Translate the bounds.
     *
     * @author paul@emptyrhetoric.com
     */
    public void translate(int x, int y) {
        bounds =
                new Rectangle(x + (int) bounds.getX(),
                        y + (int) bounds.getY(), (int) bounds.getWidth(),
                        (int) bounds.getHeight());
    }

    public void translate(double x, double y) {
        bounds =
                new Rectangle2D.Double(x + bounds.getX(),
                        y + bounds.getY(), (double) bounds.getWidth(),
                        bounds.getHeight());
    }

    /**
     * Paint the sprite, given a graphics and a playfield to paint onto.
     *
     * @author paul@emptyrhetoric.com
     */
    public void paintComponent(Graphics g, Component p) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(spriteImage, (int) bounds.getX(), (int) bounds.getY(),
                (int) bounds.getWidth(), (int) bounds.getHeight(), p);
    }

    /**
     * Repaint the sprite's area on the playing field.
     *
     * @author paul@emptyrhetoric.com
     */
    public void repaint() {
        Rectangle2D repaintarea = new Rectangle2D.Double();
        if (oldbounds == null) {
            repaintarea = bounds;
        } else {
            bounds.union(oldbounds, bounds, repaintarea);
        }
        parent.repaint((int) repaintarea.getBounds().getX(),
                (int) repaintarea.getBounds().getY(),
                (int) repaintarea.getBounds().getWidth(),
                (int) repaintarea.getBounds().getHeight());
        oldbounds = bounds;
        //        p.repaint(bounds.getBounds());
    }


    /**
     * Set the fact that the sprite has changed.  This extra method is
     * needed as a work-around to fix the observer pattern while still using
     * the decorator.
     *
     * @author paul@emptyrhetoric.com
     */
    public void setChangedForWrappers() {
        setChanged();
    }

    /**
     * Clear the fact that the sprite has changed.  This extra method is
     * needed as a work-around to fix the observer pattern while still using
     * the decorator.
     *
     * @author paul@emptyrhetoric.com
     */
    public void clearChangedForWrappers() {
        clearChanged();
    }
    
    // sprites can listen on any events they desire
    // all sprites receive all key events
    
    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        setLocationToMouse(e);
        repaint();
    }

    public void mouseReleased(MouseEvent e) {
        setLocationToMouse(e);
        repaint();
    }

    public void mouseDragged(MouseEvent e) {
        setLocationToMouse(e);
        repaint();
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void setLocationToMouse(MouseEvent e) {
        Rectangle r = getBounds();
        r.setLocation((int) (e.getX() - r.getWidth() / 2),
                (int) (e.getY() - r.getHeight() / 2));
        setBounds(r);
    }
}
