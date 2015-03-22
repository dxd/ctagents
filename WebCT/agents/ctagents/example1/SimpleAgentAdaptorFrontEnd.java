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

package ctagents.example1;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * A generic front end to boot strap the test CT Agent
 *
 * @author Ricardo De Lima
 */
public final class SimpleAgentAdaptorFrontEnd {
	
	private static Logger log = Logger.getRootLogger();

    /**
     * appropriate end points (host, port number) are passed to the 
     * agent and then it is started
     */
    public static void main(String[] args) {

        SimpleAgentAdaptor agent = new SimpleAgentAdaptorImpl();

        // Start the Agent
        // by not setting the ServerHostIP address to connect
        // we are assuming the CT server resides on the same machine
        
        if(args.length != 0 && args[0] != null) {
        	agent.setClientName(args[0]);        	
        	agent.start();
        } else {
        	log.fatal("You need to specify an id or a name for the agent: SimpleAgentFrontEnd [name]");
        }
    }
}
