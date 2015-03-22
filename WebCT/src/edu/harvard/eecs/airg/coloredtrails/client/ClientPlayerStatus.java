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

package edu.harvard.eecs.airg.coloredtrails.client;

import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import ooga.graphics.sprites.Sprite;

import java.util.Hashtable;

/**
	<b>Description</b>
	
	This class extends PlayerStatus by adding a Sprite field, for
	display on the GUI.
	<p>
 * (original) A client-specific version of the shared PlayerStatus type.
 *	
 * @author Paul Heymann (ct3@heymann.be)
 */
public class ClientPlayerStatus extends PlayerStatus {
    Sprite sprite = null;

    public ClientPlayerStatus() {
    }

    public ClientPlayerStatus(int pin) {
        super(pin);
    }

    public ClientPlayerStatus(int perGameId, int pin) {
        super(perGameId, pin);
    }

    public ClientPlayerStatus(PlayerStatus p) {
        super(p);
    }

    /**
     * Get the graphical sprite associated with this player on the board.
     *
     * @return A graphical sprite representing this player.
     */
    public Sprite getSprite() {
        return sprite;
    }

    /**
     * Set the graphical sprite associated with this player on the board.
     *
     * @param sprite A graphical sprite representing this player.
     */
    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }
}
