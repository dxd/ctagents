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

import ctgui.original.AllPlayersChipDisplay;
import java.io.Serializable;

public interface ColoredTrailsBoardWindow {

	/**
	 * Return a reference to the enclosed board panel.
	 *
	 * @return A reference to the enclosed board panel.
	 */
	public ColoredTrailsBoardPanel getBoardPanel();

	/**
	 * Return a reference to the chip display showing all players' chips.
	 *
	 * @return A reference to the chip display showing all players' chips.
	 */
	public AllPlayersChipDisplay getAllPlayersChipsDisplay();

	/**
	 * Show this window.
	 */
	public void showBoardWindow();
        
        public Serializable responseRequired(String command, Serializable data);
	
	
}