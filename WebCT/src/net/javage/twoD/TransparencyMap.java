package net.javage.twoD;

/*
 * TransparencyMap.java 1.00 25/2/2002
 *
 * (C) Dewi Williams 2002.
 */


/**
 * Terms of use: You may use this class in your own work or publish it provided,
 * <br> 1) The source code for this file and any modifications of this file
 * is included in all distributions.
 * <br> 2) The author (Dewi Williams) is given full credit for
 * this class, this should be recognised somewhere within your
 * work and the URL www.javage.net should appear somewhere.
 * You can claim credit for any modifications made under point 4.
 * <br> 3) You may not charge any fee for anything that includes this
 * class without first consulting the author.
 * <br> 4) You may modify this class on the condition
 * it is correcting a bug or making sure that this class performs
 * within the contract for each method. If you do this then please
 * E-mail me to notify me. This class is not a final class, you can
 * add functionality through inheritance if you wish, but
 * distribution restrictions still apply to this class, but not to
 * any subclasses. The only other modification that can be made is
 * placing this class within a package, the only package it can be
 * placed in is net.javage.twoD. My E-mail address is
 * Dewi@javage.net.
 * <br> 5) This class does not come with any warranties, expressed
 * or implied, you use it at your own risk, as does anyone
 * who uses it as part of your software.
 * <p/>
 * <br><br>
 * This class implements a transparency map for a sprite. To use
 * this class you should create an instance using the constructor
 * and then make successive calls to <tt>setPixel(int x, int y,
 * boolean status)</tt> until we have all of the pixel data. By
 * default a pixel is not solid. Ideally this should be done before
 * the game starts, ie. when the sprites are loaded, doing this will
 * give maximum performance from this class. When you want to check
 * for collisions you should call <tt>collide(TransparencyMap
 * TM)</tt>, this will return <tt>true</tt> if the two sprites do
 * collide. During collision detection the first test carried out is
 * checking interesection of bounding boxes, only during the second
 * phase will this class look at specific pixel information.
 * The width, height and co-ordinates for this class are restricted to the range
 * of values that the <tt>int</tt> data type can accomodate.
 */

public class TransparencyMap implements Cloneable {

    /*
     * Our transparency information
     */
    private int[] data;

    /*
     * Or if this sprite is over 32 pixels in width, all of the
     * subsprites. Either this or data will be null.
     */
    private TransparencyMap[] subMaps;

    /* The width of the sprite. */
    private int width;

    /* The height of the sprite. */
    private int height;

    /*
     * The x co-ordinate of the top-left corner of this
     * sprite/map.
     */
    private int x;

    /*
     * The y co-ordinate of the rop-left corner of this
     * sprite/map.
     */
    private int y;

    /**
     * Creates an instance of this class, all pixels are
     * initialised to non-solid.
     *
     * @param width  The width of the sprite.
     * @param height The height of the sprite.
     */
    public TransparencyMap(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Both width and height must be >0");
        }
        this.width = width;
        this.height = height;
        if (width > 32) {
            subMaps = new TransparencyMap[((width - 1) / 32) + 1];
            for (int i = 0; i < subMaps.length - 1; i++) {
                subMaps[i] = new TransparencyMap(32, height);
            }
            int lastWidth = width % 32;
            subMaps[subMaps.length - 1] =
                    new TransparencyMap(lastWidth == 0 ? 32 : lastWidth,
                            height);
        } else {
            data = new int[height];
        }
        setX(0);
        setY(0);
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
     * Sets the status of one pixel.
     *
     * @param x      The x co-ordinate of the pixel.
     * @param y      The y co-ordinate of the pixel.
     * @param status If this is true, then the pixel will be
     *               interpreted as being 'solid'
     */
    public void setStatus(int x, int y, boolean status) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException(
                    "Pixel does not lie within the bounds of the sprite.");
        }
        if (subMaps != null) {
            subMaps[x / 32].setStatus(x % 32, y, status);
        } else {
            if (status) {
                data[y] |= 1 << (31 - x);
            } else {
                data[y] &= ~(1 << (31 - x));
            }
        }
    }

    /**
     * Retrieves the status of one pixel. Returns <tt>true</tt> if
     * the specified pixel is solid.
     *
     * @param x The x co-ordinate of the pixel.
     * @param y The y co-ordinate of the pixel.
     */
    public boolean getStatus(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException(
                    "Pixel does not lie within the bounds of the sprite.");
        }
        if (subMaps != null) {
            return subMaps[x / 32].getStatus(x % 32, y);
        } else {
            int t = 1 << (31 - x);
            return (data[y] & t) == t;
        }
    }

    /**
     * Retrieves the width of this map.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Retrieves the height of the map.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the x co-ordinate of the top-left corner of this map.
     * This can be negative.
     */
    public void setX(int x) {
        this.x = x;
        if (subMaps != null) {
            for (int i = 0; i < subMaps.length; i++) {
                subMaps[i].x = x + i * 32;
            }
        }
    }

    /**
     * Retrives the x co-ordinate of the top-leftt corner of this
     * map.
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the y co-ordinate of the top-left corner of this map.
     * This can be negative.
     */
    public void setY(int y) {
        this.y = y;
        if (subMaps != null) {
            for (int i = 0; i < subMaps.length; i++) {
                subMaps[i].y = y;
            }
        }
    }

    /**
     * Retrives the y co-ordinate of the top-leftt corner of this
     * map.
     */
    public int getY() {
        return y;
    }

    /**
     * The collision detection. Returns <tt>true</tt> iff they have
     * collided.
     *
     * @param TM The transparency map for the other
     *           sprite.
     */
    public boolean collide(TransparencyMap TM) {
        if (subMaps != null) {
            if (!intersects(TM)) return false;
            for (int i = 0; i < subMaps.length; i++) {
                if (TM.collide(subMaps[i])) return true;
            }
            return false;
        } else if (TM.subMaps != null) {
            return TM.collide(this);
        } else {
            return collide0(TM);
        }
    }

    private boolean collide0(TransparencyMap TM) {
        if (TM.x < x) return TM.collide0(this);
        if (!intersects(TM)) return false;
        int i = TM.y > y ? TM.y - y : 0;
        int j = TM.y > y ? 0 : y - TM.y;
        int limit = y + height < TM.y + TM.height ? height : TM.y - y +
                TM.height;
        int shift = TM.x - x;
        for (; i < limit; i++, j++) {
            if ((data[i] & (TM.data[j] >>> shift)) != 0) {
                return true;
            }
        }
        return false;
    }

    private boolean intersects(TransparencyMap TM) {
        return x + width > TM.x &&
                TM.x + TM.width > x &&
                y + height > TM.y &&
                TM.y + TM.height > y;
    }

    /**
     * Returns a string representation of this transparency map,
     * it consists of a rectangle of binary values, 1 indicating
     * that that pixel is solid.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("Width: " + width);
        result.append(" Height: " + height);
        result.append("\n");
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                result.append(getStatus(j, i) ? 1 : 0);
            }
            result.append("\n");
        }
        return result.toString();
    }

}
