/*
	Colored Trails
	
	Copyright (C) 2006, President and Fellows of Harvard College.  All Rights Reserved.
	
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
	
	
	Header: $Header: /root/cvs/ct3/gui/ctgui/original/Icons.java,v 1.4 2009-03-04 18:12:46 kamphors Exp $
	
*/

package ctgui.original;

import ooga.graphics.GraphicsManager;
import ooga.graphics.sprites.MouseDragWrapper;
import ooga.graphics.sprites.MovementWrapper;
import ooga.graphics.sprites.Sprite;
import ooga.logic.ManagerObserver;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
	<b>Description (original)</b>
 * Icons is a static class which returns instances of the player tokens
 * necessary to display players on the board or player icons in chip
 * displays.
 *
 
	<p>
	
	<b>Issues</b>
	There are only 3 or 4 icons defined, it seems; there need to be more, we might expect.
	There should also be code to make sure that the number of players does not exceed the 
	number of icons.
	<p>
	In CT3 standup meeting (12/20/05), discussion of icon use brought up the following
	point.  If you happen to meet a particular player P in more than one game, then should
	P consistently have the same icon (not necessarily that the icon be exclusively assigned
	to P), or do we want each appearance of P to be associated with a randomly-picked icon?
	One can imagine cases where either behavior would be desired.  Consider, however, that 
	the constraint that P always have the same icon might be difficult to fulfill if there 
	are few icons available, as we might have a game where more than one player should be 
	assigned the same icon; thus, we will need many more icons to provide consistent icon 
	assignment.  Should we eventually consider dynamic icon creation?
	<p>
	
 * @author Paul Heymann (ct3@heymann.be)
	@author Sevan G. Ficici (additional comments and review)
 */
public class Icons {
    public static final int MEDIUM_ICON = 1;
    public static final int SMALL_ICON = 2;
    private static GraphicsManager gm;
    protected static Toolkit toolkit = Toolkit.getDefaultToolkit();
    private static int numPlayers = 6; // <-- set this to desired number of players
    private static String[] names = new String[numPlayers + 1];  // MONIRA
    private static String[] iconfiles = new String[numPlayers + 1];
    private static int meid = 0;

    static {
        gm = new GraphicsManager();
        ManagerObserver gmo = new ManagerObserver();
        
        // initialize names and iconfiles arrays
        names[0] = "me";
        iconfiles[0] = "me.gif";
        for (int i = 1; i <= numPlayers; ++i) {
            names[i] = "them" + i;
            iconfiles[i] = names[i] + ".gif";
        }

        // load player icons
        for (int i = 0; i < iconfiles.length; i++) {
            loadImage(0, names[i], "images/" + iconfiles[i],
                    "images/square-bitmap.gif", 1, 1, gmo, SMALL_ICON);
        }
    }

    /**
     * Return a sprite for display on the board with the icon
     * corresponding to the given perGameId and parent component.
     *
     * @param perGameId The perGameId for the player whose sprite is being
     *                  requested.
     * @param f         The parent component of the returned sprite.
     * @return A new sprite which has the proper icon for the given
     *         perGameId.
     */
    public static Sprite getPlayerBoardSprite(int perGameId, Component f) {
        return new MovementWrapper(new Sprite(f, "sprite", gm,
        	   perGameId+1, new Rectangle(-1, -1, 20, 20)));
    }

    /**
     * Get the board sprite of the player running the client.
     *
     * @param f The parent component of the new sprite.
     * @return The appropriate sprite.
     */
    public static Sprite getMyBoardSprite(Component f) {
    	System.out.println("Icons.getMyBoardSprite: meid = " + meid);
        return new MovementWrapper(new Sprite(f, "sprite", gm, meid,
                new Rectangle(-1, -1, 20, 20)));
    }


    /**
     * loadImage loads a given image into memory for future display and
     * access by a given image id.
	 
		@param percentage	UNUSED
		@param name			UNUSED
		@param filename		.gif file name
		@param bitmap		FUNCTION UNCLEAR
		@param rows			???
		@param cols			???
		@param gmo			[to watch over image loading process?]
		@param imgsize		desired icon size {MEDIUM_ICON, SMALL_ICON}
		@return				the icon/image's id number
     */
    public static int loadImage(int percentage, String name,
                                String filename,
                                String bitmap, int rows, int cols,
                                ManagerObserver gmo, int imgsize) {
        URL url = ClassLoader.getSystemResource(filename);
        Image image = toolkit.getImage(url);

        url = ClassLoader.getSystemResource(bitmap);
        Image imageb = toolkit.getImage(url);

        switch (imgsize) {
            case 0:
                break;
            case MEDIUM_ICON:
                image = image.getScaledInstance(40, 40,
                        Image.SCALE_SMOOTH);
                imageb = imageb.getScaledInstance(40, 40,
                        Image.SCALE_SMOOTH);
                break;
            case SMALL_ICON:
                image = image.getScaledInstance(20, 20,
                        Image.SCALE_SMOOTH);
                imageb = imageb.getScaledInstance(20, 20,
                        Image.SCALE_SMOOTH);
                break;
            default:
                break;
        }
        return gm.addImage(image, imageb, rows, cols, gmo,
                (Component) Taskbar.getInstance().getBoardWindow().getBoardPanel());
    }

    /**
     * Get the imageicon corresponding to the perGameId requested with
     * the player's perGameId assumed to be acquired from ClientData.
     *
     * @param perGameId The perGameId of the requested icon.
     * @return The imageicon of the requested player.
     */
    public static ImageIcon getIconByPerGameId(int perGameId) {
        return getIconByPerGameId(perGameId,
                GUI.getAgent().getPerGameID());
    }

    /**
     * See getIconByPerGameId, but this method allows myPerGameId
     * to be set explicitly rather than through ClientData.
     * 
     * @param perGameId		the game ID of a player
     * @param myPerGameId	the game ID of the player running on this GUI/JVM
     * @return 				the ME icon if the two game IDs are the same;
     * 						otherwise, the proper player icon is returned 
     */
    public static ImageIcon getIconByPerGameId(int perGameId, int myPerGameId)
    {    
     	if (perGameId == myPerGameId)
            return new ImageIcon(gm.getImage(meid, 0), names[meid]);
    	else
            return new ImageIcon(gm.getImage(perGameId+1, 0), names[perGameId+1]);
    }
}
