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

import net.javage.twoD.TransparencyMap;
import ooga.logic.ManagerObserver;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class GraphicsManager {
    private int currentImageID = 0;

    private HashMap storedImages = new HashMap();

    public GraphicsManager() {
    }

    /**
     * Get an image in a sequence by its id and the number in the sequence.
     *
     * @author paul@emptyrhetoric.com
     */
    public Image getImage(int ID, int seqnum) {
        BitmappedGraphic bm = (BitmappedGraphic) storedImages.get(new Integer(
                ID));
        return (Image) bm.images.get(seqnum);
    }

    /**
     * Get a transparency map in a sequency by its id and the number in the
     * sequence.
     *
     * @author paul@emptyrhetoric.com
     */
    public TransparencyMap getTM(int ID, int seqnum) {
        BitmappedGraphic bm = (BitmappedGraphic) storedImages.get(new Integer(
                ID));
        return (TransparencyMap) bm.transparencymaps.get(seqnum);
    }

    /**
     * Get the number of images in a sequency by its id.
     *
     * @author paul@emptyrhetoric.com
     */
    public int getNumImages(int ID) {
        BitmappedGraphic bm = (BitmappedGraphic) storedImages.get(new Integer(
                ID));
        return bm.images.size();
    }

    /**
     * Loads the image.  Good old java loads the image as the program goes, so
     * if you want guarantees that the image is actually loaded before doing
     * something with it (like an image split) then you need a method like this.
     *
     * @author paul@emptyrhetoric.com
     */
    private void loadImage(Image i, Component c) {
        // create a media tracker to track the loading of
        // i. my_component cannot be null, set it
        // to the component where you will draw the image
        // or to the main Frame in your application
        MediaTracker media_tracker = new MediaTracker(c);

        // add your image to the tracker with an arbitrary id
        int id = 0;
        media_tracker.addImage(i, id);

        // try to wait for image to be loaded
        // catch if loading was interrupted
        try {
            media_tracker.waitForID(id);
        } catch (InterruptedException e) {
            System.err.println("Image loading interrupted : " + e);
        }
    }

    /**
     * Here we are converting a list of images, which are really bitmaps,
     * into a list of transparency maps of *solid* or *notsolid*.
     *
     * @author paul@emptyrhetoric.com
     */
    public ArrayList imagesToTransparencyMaps(Image bitmap, int cols,
                                              int rows,
                                              int eachImageWidth,
                                              int eachImageHeight,
                                              ManagerObserver gmo) {
        TransparencyMap[] tms = new TransparencyMap[cols * rows];

        for (int i = 0; i < tms.length; i++) {
            tms[i] = new TransparencyMap(eachImageWidth, eachImageHeight);
        }

        int bitmapWidth = bitmap.getWidth(null);
        int bitmapHeight = bitmap.getHeight(null);

        int[] pixels = new int[bitmap.getWidth(null) *
                bitmap.getHeight(null)];
        PixelGrabber pg = new PixelGrabber(bitmap, 0, 0, bitmap.getWidth(
                null),
                bitmap.getHeight(null), pixels, 0, bitmap.getWidth(null));
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            System.err.println("Interrupted waiting for pixels!");
            return null;
        }

        if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
            System.err.println("Image fetch aborted or errored.");
            return null;
        }
        for (int j = 0; j < bitmapHeight; j++) {
            for (int i = 0; i < bitmapWidth; i++) {

                /*
                 * Pixel Values:
                 *
                 * alpha = (pixel >> 24) & 0xff
                 * red   = (pixel >> 16) & 0xff
                 * green = (pixel >>  8) & 0xff
                 * blue  = (pixel      ) & 0xff
                 *
                 * Color Values:
                 *
                 * black = 255,0,0,0
                 * white = 0,*,*,* or 255,0,0,0 or probably *,0,0,0
                 *
                 */

                // new hotness:
                if (pixels[j * bitmapWidth + i] == 0xff000000) {
                    tms[(j / eachImageHeight) * cols + i / eachImageWidth]
                            .setStatus(i % eachImageWidth, j %
                            eachImageHeight,
                                    true);
                }

                // old and busted:
                // if (((pixels[j*eachImageWidth+i] >> 24) & 0xff) == 255 &&
                // ((pixels[j*eachImageWidth+i] >> 16) & 0xff) == 0 &&
                // ((pixels[j*eachImageWidth+i] >> 8) & 0xff) == 0 &&
                // ((pixels[j*eachImageWidth+i] ) & 0xff) == 0)
                // tm.setStatus(i, j, true);
            }
        }
        return new ArrayList(Arrays.asList(tms));
    }

    /**
     * Set the image of the sprite to an image, or a series of images,
     * delimited by cols and rows.
     *
     * @author paul@emptyrhetoric.com
     */
    public int addImage(Image i, Image bitmap, int cols, int rows,
                        ManagerObserver gmo, Component parent) {

        // load original big image and bitmap:
        loadImage(i, parent);
        loadImage(bitmap, parent);

        // split the image and bitmap into bite sized pieces:
        int splitwidth = i.getWidth(null) / cols;
        int splitheight = i.getHeight(null) / rows;
        ImageSplitter is = new ImageSplitter(i);
        try {
            is.Split(cols, rows);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // make bitmapped graphic object with image list
        BitmappedGraphic bm = new BitmappedGraphic();
        bm.images = is.getImageList();

        // load all of the images, updating status as we go:
        for (Iterator it = bm.images.iterator(); it.hasNext();) {
            if (gmo != null) {
                gmo.status += 100.0 / bm.images.size();
            }
            loadImage((Image) it.next(), parent);
        }

        // load all of the bitmaps, updating status as we go:
        bm.transparencymaps = imagesToTransparencyMaps(bitmap, cols, rows,
                splitwidth, splitheight, gmo);

        // put our new bitmapped graphic into storage, with an id:
        storedImages.put(new Integer(currentImageID), bm);
        if (gmo != null) {
            gmo.running = false;
        }

        // return the id:
        return currentImageID++;
    }

    /**
     * This is just a struct for holding the data pertaining to one
     * drawable sprite.  A given sprite will have a sequence of images and
     * an accompanying sequence of transparency maps showing where that
     * image is solid and where it is not.
     *
     * @author paul@emptyrhetoric.com
     */
    class BitmappedGraphic {
        public ArrayList images = new ArrayList();
        public ArrayList transparencymaps = new ArrayList();
    }
}