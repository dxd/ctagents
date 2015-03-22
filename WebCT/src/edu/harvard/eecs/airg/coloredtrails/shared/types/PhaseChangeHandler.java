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
*/

package edu.harvard.eecs.airg.coloredtrails.shared.types;

/**
 * A handler which handles beginPhase and endPhase events fired from
 * the Phases type.
 */
public interface PhaseChangeHandler {
    /**
     * A new phase has begun.
     *
     * @param phaseName The phase name of the phase which just began.
     */
    public void beginPhase(String phaseName);

    /**
     * A phase has ended.
     *
     * @param phaseName The name of the phase which just ended.
     */
    public void endPhase(String phaseName);
}
