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

package ctgui.original;

import java.util.ArrayList;


import edu.harvard.eecs.airg.coloredtrails.client.ui.ColoredTrailsGUI;
import edu.harvard.eecs.airg.coloredtrails.client.ui.SimpleGUIAgentImpl;
import edu.harvard.eecs.airg.coloredtrails.client.ui.WaitingForPlayersScreen;
import edu.harvard.eecs.airg.coloredtrails.client.ui.discourse.DiscourseHandler;
import edu.harvard.eecs.airg.coloredtrails.server.ColoredTrailsServer;
import edu.harvard.eecs.airg.coloredtrails.server.ServerData;
import edu.harvard.eecs.airg.coloredtrails.shared.Utility;
import edu.harvard.eecs.airg.coloredtrails.shared.types.CTStateContainer;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;


import ctgui.original.discoursehandlers.BasicProposalDiscourseHandler;
import ctgui.original.discoursehandlers.BasicProposalDiscussionDiscourseHandler;

import javax.swing.*;


/**
 * TODO: javadoc this
 */
public class GUI implements ColoredTrailsGUI
{

	private String client_name;
	private String server_name;
	private int server_port;
	private static SimpleGUIAgentImpl agent;
        private final static ArrayList<DiscourseHandler> discourseHandlers = new ArrayList<DiscourseHandler>();

	public GUI()
  {
		server_port = ColoredTrailsServer.DEFAULT_PORT;
  }

  
  public void run()
  {
        // discourse handler for making proposals
        discourseHandlers.add(new BasicProposalDiscourseHandler());
        // discourse handler for talking about proposals
        discourseHandlers.add(new BasicProposalDiscussionDiscourseHandler());

        agent = new SimpleGUIAgentImpl();
        agent.setServerHostname(server_name);
        agent.setPin(client_name);
        agent.communication.setDiscourseHandlers(discourseHandlers);

	JFrame.setDefaultLookAndFeelDecorated(true);
   	final Taskbar t = Taskbar.getInstance();
   	t.setAgent(agent);
   	t.showTaskbar();
    
        //If we already have a server name, then we go ahead and connect
        if(server_name != null)
        {
                System.out.println("Connecting to server: " + server_name);
                connectAndWait();
                t.updateButtonState();
        }
    
  }


    /**
     * Connect to the remote host hosting the game and display a waiting
     * window until data is received from the server.
     */
    
    public static void connectAndWait() {
    	boolean res = true;

        try {
           agent.start();
        } catch (Exception e) {
            System.out.println("registration failed: " + e);
                res = false;    			
        }
               
        if (!res) {
            Utility.guiError("Registration with Server failed.");
        } else {
            final WaitingForPlayersScreen wfps = WaitingForPlayersScreen.getInstance();
            final Taskbar taskbar = Taskbar.getInstance();
            taskbar.repaint();

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    wfps.startWaiting();
                    taskbar.getBoardWindow().showBoardWindow();                    
                }
            });
        }
    }



	public void setClientName(String client) {
		client_name = client;

	}

	public void setServerHostname(String server) {
		server_name = server;

	}

	public void setServerPort(int port) {
		
		server_port = port;
		
	}

	public static SimpleGUIAgentImpl getAgent() {
		return agent;
	}

	public static void setAgent(SimpleGUIAgentImpl agent) {
		GUI.agent = agent;
	}

	public static ArrayList<DiscourseHandler> getDiscourseHandlers() {
		return discourseHandlers;
	}
}
