/*
	Colored Trails
	
	Copyright (C) 2006-2008, President and Fellows of Harvard College.
	All Rights Reserved.
	
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

package  edu.harvard.eecs.airg.coloredtrails.server;

import  edu.harvard.eecs.airg.coloredtrails.shared.types.PhaseChangeHandler;
import  edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GamePhase;


/**
 * A server-specific Phases class which interfaces with a
 * PhaseChangeHandler created in a configuration runnable.
 *
 * @author Paul Heymann (ct3@heymann.be)
   @author Sevan G. Ficici (modifications for partial visibility)
 */
public class ServerPhases extends Phases
{
 //   private PhaseChangeHandler handler = null;
	public static final String STRATEGY_PH = "Strategy Prep Phase";
	public static final String REVELATION_PH = "Revelation Phase";
	public static final String COMM_PH = "Communication Phase";
	public static final String COUNTER_PH =	"Counter Offer Phase";
	public static final String FEEDBACK_PH = "Feedback Phase";

	public ServerPhases(PhaseChangeHandler pch)
	{
        super();
        // phase change handler
        set("handler", pch);
        // NOTE THAT we don't have a copy constructor for ServerPhases
        // If we need one, then we must remember to copy the "handler"
        // feature; this feature is stripped out by the Phases copy constructor
        // If we don't strip it out, then we'll get a JMS exception when we
        // try to send Phases to a client because the handler doesn't get sent
        // over JMS
	}
	
	/**
	 * Sets the phase sequence to loop or not
	 *
	 * @param loop phase sequence loops for true, doesn't loop for false
	 */
	public void setLoop( boolean loop )
	{
		set("isLoop", new Boolean(loop));
	}

    /**
     * Get the phase change handler associated with this Phases object.
     *
     * @return The phase change handler.
     */
    public PhaseChangeHandler getPhaseChangeHandler()
    {
    	return (PhaseChangeHandler)get("handler");
    }

    /**
     * A new endPhase method which calls the handler's endPhase.
     */
    public void endPhase()
    {
        super.endPhase();
        getPhaseChangeHandler().
        	endPhase( getCurrentPhase().getName() );//getCurrentPhaseName());
    }

    /**
     * A new beginPhase method which calls the handler's beginPhase.
     */
    public void beginPhase()
    {
        super.beginPhase();
        getPhaseChangeHandler().
        	beginPhase( getCurrentPhase().getName() );//getCurrentPhaseName());
    }

    /**
     * Adds the phase to the phase sequence
     * @param phaseName Name of the phase to be added
     * @param duration Duration of the phase to be added
     */
    public void addPhase(String phaseName, int duration)
    {
        getPhaseSequence().add(new GamePhase( phaseName, duration ));
    }

    /**
     * Adds the phase to the phase sequence
     * @param phaseName Name of the phase to be added
     */
    public void addPhase(String phaseName)
    {
        getPhaseSequence().add(new GamePhase( phaseName ));
    }
}
