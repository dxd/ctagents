/*
	Colored Trails
	
	Copyright (C) 2006-2007, President and Fellows of Harvard College.  All Rights Reserved.
	
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

package edu.harvard.eecs.airg.coloredtrails.client.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A class to display a "waiting for other players" window while
 * the client is waiting for other players.  Also plays a
 * "non-progress" bar.
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public class WaitingForPlayersScreen extends JFrame {
    private static WaitingForPlayersScreen INSTANCE = null;

    private JProgressBar progressBar = new JProgressBar();
    private final int SWING_CONST_X = 12;
    private final int SWING_CONST_Y = 45;

    //synchronized creator to defend against multi-threading issues
    //another if check here to avoid multiple instantiation
    private synchronized static void createInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WaitingForPlayersScreen();
        }
    }

    public static WaitingForPlayersScreen getInstance() {
        if (INSTANCE == null) createInstance();
        return INSTANCE;
    }

    protected WaitingForPlayersScreen() {
        ImageIcon ii = new ImageIcon(
                JPanel.class.getResource("/images/waiting.jpg"));
        Image waitimage = ii.getImage();


        int width = waitimage.getWidth(null);
        int height = waitimage.getHeight(null);

        Box b = Box.createVerticalBox();
        JLabel label = new JLabel(ii);

        setPreferredSize(new Dimension(width + SWING_CONST_X,
                height + SWING_CONST_Y));

        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(width, 15));

        b.add(label);
        b.add(progressBar);
        getContentPane().add(b);
        pack();
    }

    /**
     * Display the waiting screen.
     */
    public void startWaiting() {
        setVisible(true);
    }

    /**
     * Hide the waiting screen.
     */
    public void stopWaiting() {
        setVisible(false);
    }
}
