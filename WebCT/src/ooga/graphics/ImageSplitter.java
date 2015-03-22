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

import javax.swing.*;
import java.awt.*;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * ImageSplitter is used to split a graphic up into a bunch of smaller images
 * of the same size (both width and height).  This is used in OOGA so that
 * animated sprites can supply one graphic file for their animations, which in
 * turn, will be split up by an ImageSplitter with each new image representing
 * a frame in the sprite's animation sequence.
 *
 * @author matthew.hall@duke.edu
 */
public class ImageSplitter {
    /**
     * Constructs an ImageSplitter from an existing Image object
     *
     * @param Image The image to create the ImageSplitter from
     * @throws NullPointerException If image is null
     */
    public ImageSplitter(Image image) throws NullPointerException {
        if ((myOriginalImage = image) == null) {
            throw new NullPointerException(
                    "Error: ImageSplitter was passed a null Image object");
        }
    }


    /**
     * Constructs an ImageSplitter from an existing graphic file
     *
     * @param filename The graphic file to create the ImageSplitter from
     * @throws IOException If the filename is null or doesn't exist
     */
    public ImageSplitter(String filename) throws IOException {
        if ((filename == null) || (!new File(filename).exists())) {
            throw new IOException(
                    "Error: ImageSplitter could not read the image file " +
                    filename);
        }

        myOriginalImage = new ImageIcon(filename).getImage();
    }


    /**
     * Set the index of our ImageSplitter so that we can control which
     * image GetNextImage() will return next
     *
     * @param index The image to set the index to
     */
    public void SetIndex(int index) {
        myIndex = index;
    }


    /**
     * Get the next image in our set of images
     *
     * @throws Exception If the ArrayList of split images is empty (which would
     *                   be the case if the image has not yet been split, or the split was
     *                   unsuccessful).
     * @returns myImages[0] if myIndex is out of range<br>
     * myImages[myIndex] otherwise.
     */
    public Image GetNextImage() throws Exception {
        Image temp;

        /** Check to make sure that we have images to return */
        if (myImages.isEmpty()) {
            throw new Exception("Error: ImageSplitter contains no images");
        }

        /** Check to make sure our array index isn't out of range
         *  and if it is, reset the index to 0
         */
        if ((myIndex < 0) || (myIndex >= myImages.size())) {
            myIndex = 0;
        }

        /** Return our image */
        return (Image) myImages.get(myIndex++);
    }


    /**
     * Splits our base image file into a bunch of smaller images
     *
     * @param cols The number of columns of images to create
     * @param rows The number of rows of images to create
     * @throws Exception Throws an Exception if the dimensions of myOriginalImage
     *                   are smaller than the number of rows and columns that the image is to
     *                   be split into
     */
    public void Split(int cols, int rows) throws Exception {
        FilteredImageSource newsource;
        ImageFilter cropfilter;
        Image croppedImage;
        int width, height, splitwidth, splitheight, c, d;

        myImages.clear();

        /** Get the original dimension of our image, passing null to the functions
         *  because we don't need to use an ImageObserver object
         */
        width = myOriginalImage.getWidth(null);
        height = myOriginalImage.getHeight(null);

        /** We need to make sure that we have enough pixels in our original image
         *  to create rows*cols new images.  If we can't, then throw an exception.
         */
        if (((width == 0) || (height == 0)) || (width < cols) ||
                (height < rows)) {
            throw new Exception(
                    "Error: ImageSplitter's base image is invalid");
        }

        /** Figure out the dimension that each of our smaller images will be
         *  based upon the dimension of the base image and the number of rows/cols
         *  that we are going to use
         */
        splitwidth = width / cols;
        splitheight = height / rows;

        /** Iterate through our base image by row and also by column... */
        for (c = 0; c < rows; c++) {
            for (d = 0; d < cols; d++) {
                /** Create a new CropFilter whose position is determined by
                 *  multiplying the size of our split images by the row and
                 *  column that we are currently at in our iteration
                 */
                cropfilter =
                        new CropImageFilter(d * splitwidth,
                                c * splitheight, splitwidth, splitheight);
                newsource =
                        new FilteredImageSource(
                                myOriginalImage.getSource(), cropfilter);
                croppedImage =
                        Toolkit.getDefaultToolkit().createImage(newsource);

                /** After the CropFilter is applied, we create a new image from the
                 *  result of the filter and add it to our ArrayList...
                 */
                myImages.add(croppedImage);
            }
        }
    }

    public ArrayList getImageList() {
        return myImages;
    }

    /**
     * Our base image that we will be splitting up
     */
    private Image myOriginalImage = null;

    /**
     * Our ArrayList of split images
     */
    private ArrayList myImages = new ArrayList();

    /**
     * Our current index in the ArrayList
     */
    private int myIndex = 0;
}
