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

package edu.harvard.eecs.airg.coloredtrails.admin;

import jshell.app.JShell;


/**
 * An extended JShell which adds Commands specific to the admin
 * client.  This acts as a pseudo-shell which predominantly acts
 * as a command line interface to a remote server.
 *
 * @author Paul Heymann (ct3@heymann.be)
 * 
 *  
 * 
 */
public class AdminClientJShell extends JShell {


    public AdminClientJShell() {
        super();
        addCommand(new ListConfigurationsCommand());
        addCommand(new ListGamesCommand());
        addCommand(new ListPlayersCommand());
        addCommand(new HelpCommand());
        addCommand(new AddConfigurationCommand());
        addCommand(new NewGameCommand());
        addCommand(new PrintLogCommand());
        setFinalExit(true);



    }

    protected String getJShellRcFileName() {
        return ".ctadmin";
    }
}
