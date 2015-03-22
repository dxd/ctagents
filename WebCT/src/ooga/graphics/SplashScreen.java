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


/**
 * SplashScreen is used to display a user with a splash screen while an application
 * loads.  Aside from informing the user of the progress of the load, it also helps
 * to let the user know that something is actually going on and the program hasn't
 * frozen.  SplashScreen does this by providing a progress bar to display
 * percentage done, a text messgae to let the user know what is happening and an
 * animated picture as well.
 *
 * @author matthew.hall@duke.edu
 */
public class SplashScreen extends JWindow {
    /**
     * Constructs a new SplashScreen
     *
     * @param imagefile The image file to display to the user (this will actually be an image strip that
     *                  gets split up and then animated, but if you don't want to animated it, then just
     *                  make a single images and set the rows and cols parameters to 1, the delay
     *                  shouldn't matter).
     * @param rows      The number of rows to split the image into
     * @param cols      The number of columns to split the image into
     * @param delay     The delay (in milliseconds) with which to animate the image
     */
    public SplashScreen(Image imagefile, int rows, int cols, int delay) {
        try {
            ImageSplitter splitter;
            ImageIcon icon;

            /** Load our imagefile */
            loadImage(imagefile);
            myProgressBar = new JProgressBar(0, 100);

            /** We have to set this property so that the progress bar will
             *  actually display its percentage
             **/
            myProgressBar.setStringPainted(true);

            /** Create an image splitter and split our image */
            splitter = new ImageSplitter(imagefile);
            splitter.Split(rows, cols);

            /** Paint our image onto the splash screen with the initial
             *  frame of its animation
             */
            icon = new ImageIcon(splitter.GetNextImage());
            myImageLabel.setIcon(icon);
            myImageLabel.repaint();

            /** Start our animation by creating a SplashAnimator object using
             *  the image splitter we just created
             */
            mySplashAnimator =
                    new SplashAnimator(splitter, myImageLabel, delay);
            mySplashAnimator.start();

            /** Initialize our SplashScreen's swing componenets */
            init(icon.getIconWidth(), icon.getIconHeight());
        } catch (Exception e) {
            /* Print the stack is things go awry */
            e.printStackTrace();
        }
    }


    /**
     * Loads the image.  Good old java loads the image as the program goes, so
     * if you want guarantees that the image is actually loaded before doing
     * something with it (like an image split) then you need a method like this.
     *
     * @author paul@emptyrhetoric.com
     */
    private void loadImage(Image i) {
        MediaTracker media_tracker = new MediaTracker(this);

        int id = 0;
        media_tracker.addImage(i, id);

        try {
            media_tracker.waitForID(id);
        } catch (InterruptedException e) {
            System.err.println("Image loading interrupted : " + e);
        }
    }


    /**
     * Initializes our splash screen window by creating its swing components
     *
     * @param width  The width of the splash screen
     * @param height The height of the splash screen
     */
    private void init(int width, int height) {
        GridBagConstraints c = new GridBagConstraints();
        JComponent pane = (JComponent) getContentPane();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int screenx, screeny, windowx, windowy;

        /** Use a gridbag */
        getContentPane().setLayout(new GridBagLayout());

        /** Add our label to hold our image */
        myImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridx = 0;
        c.gridy = 0;
        getContentPane().add(myImageLabel, c);

        /** Add our panel to hold the progress indicators */
        myProgressPanel.setLayout(new GridBagLayout());
        myProgressPanel.add(myProgressBar, c);
        c.gridy = 1;
        myProgressPanel.add(myStatusLabel, c);
        getContentPane().add(myProgressPanel, c);

        /** Set our sizes */
        myImageLabel.setPreferredSize(new Dimension(width, height));
        myImageLabel.setMaximumSize(new Dimension(width, height));
        myProgressPanel.setPreferredSize(
                new Dimension(width,
                        myProgressPanel.getPreferredSize().height));
        myProgressPanel.setMaximumSize(
                new Dimension(width,
                        myProgressPanel.getPreferredSize().height));

        /** Get the size of the screen */
        screenx = dim.width;
        screeny = dim.height;

        /** Get our size from the parameters */
        windowx = width;
        windowy = height + myProgressPanel.getPreferredSize().height;

        /** Set our bounds and center the window in the center of the screen */
        setBounds((screenx - windowx) / 2, (screeny - windowy) / 2,
                windowx, windowy);
    }


    /**
     * Updates the progress bar of the SplashScreen and the message that is displayed
     * to the user.
     *
     * @param status     The message to display to the user
     * @param percentage The percentage of completion that the SplashScreen should display
     */
    public void Update(String status, int percentage) {
        myProgressBar.setValue(percentage);
        myStatusLabel.setText(status);
    }


    /**
     * Holds our progress bar
     */
    private JProgressBar myProgressBar;

    /**
     * Holds the label to display text messages to the user
     */
    private JLabel myStatusLabel = new JLabel("Loading...");

    /**
     * Holds the label upon which our image is displayed
     */
    private JLabel myImageLabel = new JLabel();

    /**
     * JPanel for the progressbar and the statuslabel
     */
    private JPanel myProgressPanel = new JPanel();

    /**
     * Holds our animator object
     */
    private SplashAnimator mySplashAnimator;
}


/**
 * SplashAnimator is a threadded class used to animate an image splitter.  This class
 * is separate from the SplashScreen because we want to be able to create an object
 * of this type in the SplashScreen and then start() it in SplashScreen so that the
 * animation runs as a separate thread.  If this wasn't the case, you wouldn't be
 * able to call Update() or anything on SplashScreen because it would be locked into
 * animating the image.
 *
 * @author matthew.hall@duke.edu
 */
class SplashAnimator implements Runnable {
    /**
     * Construct a new SplashAnimator
     *
     * @param imagesplitter The imagesplitter to animate
     * @param imagelabel    The JLabel upon which to paint the animation
     * @param delay         The delay of animation (in milliseconds)
     */
    public SplashAnimator(ImageSplitter imagesplitter, JLabel imagelabel,
                          int delay) {
        myImageSplitter = imagesplitter;
        myImageLabel = imagelabel;
        myDelay = delay;
    }


    /**
     * Start our animation thread
     */
    public void start() {
        /** We only want to start if we're not already running... */
        if (myThread == null) {
            myThread = new Thread(this);
            myThread.start();
        }
    }


    /**
     * Updates our JLabel with our ImageSplitter object's next image, sleeps
     * for the amount of time indicated in our delayed, the loops again
     */
    public void run() {
        Thread thread = Thread.currentThread();

        /** Loop while we're not being interrupted... */
        while (myThread == thread) {
            /** Otherwise.. sleep for our delay and then update the iamge */
            try {
                Thread.sleep(myDelay);
                myImageLabel.setIcon(
                        new ImageIcon(myImageSplitter.GetNextImage()));
            }
                    /** If we got interrupted then stop our thread */ catch (InterruptedException ie) {
                stop();
            }
                    /** Ditto if we catch an exception (which would be thrown by the
                     *  ImageSplitter's GetNextImage() methods
                     */ catch (Exception e) {
                e.printStackTrace();
                stop();
            }
        }
    }


    /**
     * We only want to stop if we'return already running...
     */
    public void stop() {
        if (myThread != null) {
            myThread = null;
        }
    }


    /**
     * Holds the imagesplitter that we're animating
     */
    private ImageSplitter myImageSplitter;

    /**
     * Holds the imagelabel that we're going to paint the aniamtion on
     */
    private JLabel myImageLabel;

    /**
     * Holds our thread
     */
    private Thread myThread = null;

    /**
     * Holds the delay of our animation in milliseconds
     */
    private int myDelay = 0;
}
