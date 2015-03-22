/*
	Colored Trails
	
	Copyright (C) 2006-2008, President and Fellows of Harvard College.  All Rights Reserved.
	
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

import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GamePhase;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

/**
	Client-specific version of Phases
	
	@author Paul Heymann
	@author Sevan G. Ficici (modifications for partial visibility)
*/
public class ClientPhases extends Phases
{
	public ClientPhases( Phases phases )
	{
		super( phases );
	}

	/**
	 * actionPerformed overrides Phases actionPerformed so that
	 * the GamePhase is not advanced when the current phase's
	 * counter runs to 0.
	 */
	public void actionPerformed( ActionEvent e )
	{
		set("currentelapsed", new Integer(getCurrentSecsElapsed() + 1));
		
		GamePhase current = getCurrentPhase();
		if( ( current != null ) && ( !current.isIndefinite() ) )
		{
			set("currentleft", new Integer(getCurrentSecsLeft() - 1));
			
			// if game phase is not indefinite, and the decremented time is <= 0 ...
			if( getCurrentSecsLeft() < 0 )
				set("currentleft", new Integer(0));
		}
	}
}
