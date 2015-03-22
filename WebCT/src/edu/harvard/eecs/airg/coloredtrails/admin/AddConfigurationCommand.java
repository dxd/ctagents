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

import edu.harvard.eecs.airg.coloredtrails.shared.Utility;
import edu.harvard.eecs.airg.coloredtrails.server.ColoredTrailsServer;
import edu.harvard.eecs.airg.coloredtrails.server.AdminCommands;
import jshell.commands.Command;
import jshell.commands.StateHolder;
import jshell.util.BadShellInputException;

import java.io.*;
import java.net.*;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * An administrator command to add a configuration class to the
 * server which exists as bytecode in the directory pointed to
 * by the CONFIGDIR environmental variable.
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public class AddConfigurationCommand extends AdminClientCommand{



    public AddConfigurationCommand() {
        super();
        name = "add configuration";
    }


    public AddConfigurationCommand(String[] argv, StateHolder stater,
                                   PrintWriter out, BufferedReader in) {
        super(argv, stater, out, in, "add configuration");
    }

    private void connectToServer(String host){
        //host = "tcp://127.0.0.1:8080";
        host = host.replaceAll("/", "//");
        log.info("Connecting Admin to Colored Trails Server..." + host);

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(host);

        try {
            if (communication == null){
                System.err.println("communication is null");
                throw new NullPointerException();
            }
            Connection connection = factory.createConnection();
            connection.setExceptionListener(this);
            communication.setConnection(connection);

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            communication.setSession(session);

            Topic topic = session.createTopic(ColoredTrailsServer.SERVER_NAME);
            communication.setTopic(topic);

            /*
             * This selector to the consumer specifies that this client is to listen
             * for messages directed to admin client
             */

            MessageConsumer consumer = session.createConsumer(topic,"(type = 'ADMIN_CLIENT')");
            consumer.setMessageListener(this);
            communication.setConsumer(consumer);

            MessageProducer producer = session.createProducer(topic);
            communication.setProducer(producer);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            connection.start();

        }  catch (JMSException e) {
            log.fatal("Creating Connection With the Server Failed:" + e);
            System.exit(-1);
        }
        log.info("Connected Admin to Colored Trails Server..." + host);

    }

    private void sendConfig(String configdir){
        String className = argv[2];
        byte[] myclass;

            URI debug_uri = null;

            try {
                URL urls[] = {new File(configdir).toURI().toURL()};
                URLClassLoader u = URLClassLoader.newInstance(urls);

                URL path = u.getResource(className + ".class");

                debug_uri = path.toURI();
                myclass = Utility.getBytesFromFile(new File(path.toURI()));
            } catch (MalformedURLException e) {
                writeException(out, "Configuration directory URL invalid", e);
                return;
            } catch (URISyntaxException e) {
                writeException(out, "Configuration directory URL invalid", e);
                return;
            } catch (IOException e) {
                writeException(out, "Configuration directory URL invalid", e);
                return;
            } catch (IllegalArgumentException e) {
                writeException(out, "Configuration directory URL invalid", e);
                out.println("URI was: " + debug_uri);
                return;
            }
            catch (NullPointerException e){
                writeException(out, "cannot find configuration class. Make sure it's compiled.", e);
                return;
            }


            try {
                BytesMessage msg = communication.getSession().createBytesMessage();
                msg.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.ADMIN_MSG);
                msg.writeBytes(myclass);
                communication.getProducer().send(msg);
                log.info("Admin: Sending config class");

                TextMessage txtMsg = communication.getSession().createTextMessage();
                txtMsg.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.ADMIN_MSG);
                txtMsg.setStringProperty(ColoredTrailsServer.COMMAND, AdminCommands.ADD_CONFIGURATION);
                txtMsg.setStringProperty(AdminCommands.CONFIGCLASS_NAME, className);
                communication.getProducer().send(txtMsg);
                log.info("Admin: Sending config class name");

            }  catch (JMSException e) {
                   log.fatal("Admin:Error sending config class to the Server:" + e);
                   System.exit(-1);
            }
    }


    public void doCommand() {

        if (!(stater.checkVariableIsSet("CONFIGDIR", "/absolute/path/dir/",
                out) &&
                stater.checkVariableIsSet("HOST", "http://host:port/path",
                        out))) {
            return;
        }

        connectToServer(stater.getVariable("HOST"));
        sendConfig(stater.getVariable("CONFIGDIR"));
    }

    public Command getCommand(String[] argv,
                              StateHolder stater,
                              PrintWriter out,
                              BufferedReader in) {
        return new AddConfigurationCommand(argv, stater, out, in);
    }

    public void horribleDeath() throws BadShellInputException {
        checkArgs(argv, 3, 3, "add configuration <configclass>");
    }

    public boolean isProperCommand(String command) {
        return (command.startsWith("add configuration"));
    }
}
