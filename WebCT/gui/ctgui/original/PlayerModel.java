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

package ctgui.original;

import javax.swing.*;

import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;

public class PlayerModel extends DefaultComboBoxModel {
	private boolean withMe;
	private ClientGameStatus cgs;
	
	public PlayerModel(ClientGameStatus cgs) {
		this(cgs, false);
	}
	
	public int getPlayerIndex(int selectedIndex){
		if ( !withMe && selectedIndex >= cgs.getMyPlayer().getPerGameId()) {
			return selectedIndex + 1;
		}
		return selectedIndex;
	}
	
	public PlayerModel(ClientGameStatus cgs, boolean withMe) {
		this.withMe = withMe;
		this.cgs = cgs;
		
		int myId = cgs.getMyPlayer().getPerGameId();
		for (int i = 0; i < cgs.getPlayers().size(); i++) {
			if (!withMe && myId == i){
				continue;
			}
			PlayerStatus ps = cgs.getPlayerByPerGameId(i);
			JLabel jlabel = new JLabel(Icons.getIconByPerGameId(i));
			addElement(jlabel);
		}
	}
}


