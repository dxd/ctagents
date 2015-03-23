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

package edu.harvard.eecs.airg.coloredtrails.shared.app;

import java.rmi.RemoteException;

import mwspaces.CTHandler;
import mwspaces.CTsetup;

import org.apache.log4j.Logger;

import edu.harvard.eecs.airg.coloredtrails.admin.AdminClientJShell;
import edu.harvard.eecs.airg.coloredtrails.server.ColoredTrailsServer;
import edu.harvard.eecs.airg.coloredtrails.shared.Utility;
import edu.harvard.eecs.airg.coloredtrails.client.ui.ColoredTrailsGUI;
import edu.harvard.eecs.airg.coloredtrails.controller.CodeController;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

/**
 * A generic, gnu getopt utilizing frontend to both the client and server.
 * Adapted from one of my early software engineering
 * projects (OokieMonster).
 * <p/>
 * Acknowledgements: Rock on Raja, Spencer.
 *
 * @author Paul Heymann (ct3@heymann.be)
 * @author Ricardo De Lima (ricardo@eecs.harvard.edu)
 */
public class FrontEnd {
    public static int port = 8088;
	private static CTsetup spaces;

    /**
     * Print a standard help message with command line options.
     */
    private static void printUsage() {
        System.out.println("Arguments\n=========");
        System.out.println("Frontend takes the following arguments: \n"
                + "-s                        : Start a server.\n"
                +
                "--server_localport <port> : Port to start server on.\n"
                + "-c <class name>           : Start a GUI client.\n"
                + "--pin                     : Specify a GUI client pin"
                + "(required for GUI client.\n"
                + "--client_localport <port> : Specify a local port for "
                + "the client to listen on.\n"
                +
                "--client_hostip <ip>      : Specify a remote host's IP "
                + "to connect to.\n"
                + "-a                        : Start an command line "
                + "admin client.\n");
        System.out.println("Usage\n=====");
        System.out.println("Standard Usage:");
        System.out.println("Standalone Server: java -jar ct3.jar -s");
        System.out.println(
                "Standalone Client: java -jar ct3.jar -c <gui class name> --pin <pin number>");
        System.out.println("Standalone Admin:  java -jar ct3.jar -a");
        System.exit(1);
    }

    /**
     * Main mostly deals with command line options using the friendly
     * gnu.getopt package, and then starts a client, server, or both as
     * appropriate.
     */
    public static void main(String[] args) {

        boolean startserver = false;
        boolean startadmin = false;
        boolean startclient = false;
        boolean startctrl = false;
        int clientpin = -1;
        int clientport = Utility.CT3_CLIENT_DEFAULT_PORT;
        String client_hostip = null;
        String guiclassname = null;
        String ctrlConfig = null;
        Class gui = null;
		Object objgui = null;

        // gnu getopt business:
        LongOpt[] longopts = new LongOpt[5];
        longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
        longopts[1] = new LongOpt("pin", LongOpt.REQUIRED_ARGUMENT, null,'#');
        longopts[2] = new LongOpt("client_localport", LongOpt.REQUIRED_ARGUMENT, null, 'z');
        longopts[3] = new LongOpt("client_hostip", LongOpt.REQUIRED_ARGUMENT, null, 'i');
        longopts[4] = new LongOpt("server_localport", LongOpt.REQUIRED_ARGUMENT, null, 'p');
        Getopt g = new Getopt("coloredtrails", args, "p:satc:", longopts);
        g.setOpterr(true);

        int c;
        String arg;

        try {
            while ((c = g.getopt()) != -1) {
                switch (c) {
                    case 0:
                        printUsage();
                        break;
                    case 'h':
                        printUsage();
                        break;
                    case 'p':
                        arg = g.getOptarg();
                        if (!arg.matches("[0-9]+")) {
                            System.err.println(
                                    "Bad port number, using default.");
                        } else {
                            port = Integer.valueOf(arg).intValue();
                        }
                        System.out.println("Port is " + port + ".");
                        break;
                    case 's':
                        startserver = true;
                        break;
                    case 'a':
                        startadmin = true;
                        break;
                    case 't':
                        startctrl = true;
                        break;
                    case 'c':
                        startclient = true;
                        guiclassname = g.getOptarg();
                        break;
                    case '#':
                        clientpin = Integer.valueOf(g.getOptarg());
                        break;
                    case 'z':
                        clientport = Integer.valueOf(g.getOptarg());
                        break;
                    case 'i':
                        client_hostip = g.getOptarg();
                        break;
                    default:
                        System.out.println("Unknown option:" + c);
                        printUsage();
                        System.exit(1);
                        break;
                }
            }
        } catch (Exception e) {
            printUsage();
            System.err.println("Error: Bad Input.");
            System.exit(1);
        }

        // start a server if -s was passed:
        if (startserver) {
     
        	/*
        	 * Starting the JMS Broker and the correct Topics
        	 * 
        	 */
        	
        	ColoredTrailsServer ct = ColoredTrailsServer.getInstance();
        	
        	ct.start();
 
  	    	spaces = new mwspaces.CTsetup();
  	    	//spaces.initializeGS();
  	    

        }

        if (startclient) {
 
        	
            
            
            try {
				gui = Class.forName(guiclassname);
			} catch (ClassNotFoundException e) {
				System.err.println("Colored Trails: could not load the gui class name");
				e.printStackTrace();
			}
            
			/*
			 * Sanity Check before attempting to load the GUI
			 * 
			 */
            
			if(gui != null){

				try {
					objgui = gui.newInstance();
				} catch (InstantiationException e1) {
					System.err.println("Colored Trails: GUI could not be instantiated");
					e1.printStackTrace();
					System.exit(1);
				} catch (IllegalAccessException e1) {
					System.err.println("Colored Trails: Illegal Access to the GUI class");
					e1.printStackTrace();
					System.exit(1);
				}
				
				if(objgui instanceof ColoredTrailsGUI){
					
					// GUI is valid let's instantiate it and start it
					// the default to connect is the localhost
					
					System.out.println("Colored Trails: Starting GUI " + gui.getName());
					
						((ColoredTrailsGUI)objgui).setClientName(Integer.toString(clientpin));
						((ColoredTrailsGUI)objgui).setServerHostname(client_hostip);
						
						Thread guithread = new Thread((Runnable) objgui);					
						guithread.start();

					
					
				} else {
					System.err.println("Colored Trails: GUI is not an instance of ColoredTrailsGUI");
					System.exit(1);
				}
				
				
			} else {
				System.err.println("Colored Trails: GUI is not valid");
				System.exit(1);
				
			}
            
        }

        if (startadmin) {
            System.out.println("Colored Trails Admin Interface v1.0");
            System.out.println("Type 'help' for more information.");

            // add non-standard commands to jshell interpreter and start:
            AdminClientJShell js = new AdminClientJShell();
            js.run();
        }

        if (startctrl) {
            System.out.println("Colored Trails Controller v1.0");
            CodeController cntrl = new CodeController(ctrlConfig);
            cntrl.start();
        }


        // if neither client nor server, then the user messed up, print
        // a help message:
        if (!(startserver || startadmin || startclient || startctrl)) {
            printUsage();
        }

        if (startclient && clientpin == -1) {
            printUsage();
        }
    }
}

