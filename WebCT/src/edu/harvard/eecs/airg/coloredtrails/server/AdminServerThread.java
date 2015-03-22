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

package edu.harvard.eecs.airg.coloredtrails.server;
import javax.jms.*;
import javax.naming.*;

/**
 * Root server thread.
 */
@Deprecated
public class AdminServerThread extends Thread {
    private Queue queue = null;

    public AdminServerThread() {}

    public void run() {
        Context                 jndiContext = null;
        QueueConnectionFactory  queueConnectionFactory = null;
        QueueConnection         queueConnection = null;
        QueueSession            queueSession = null;
        QueueReceiver           queueReceiver = null;
        TextMessage             message = null;
        String                  queueName = "ADMIN";

        /*
        * Create a JNDI API InitialContext object if none exists
        * yet.
        */
        try {
            jndiContext = new InitialContext();
        } catch (NamingException e) {
            System.out.println("Could not create JNDI API " +
                    "context: " + e.toString());
            return;
        }

        /*
        * Look up connection factory and queue.  If either does
        * not exist, exit.
        */
        try {
            queueConnectionFactory = (QueueConnectionFactory)
                    jndiContext.lookup("QueueConnectionFactory");
            queue = (Queue) jndiContext.lookup(queueName);
        } catch (NamingException e) {
            System.out.println("JNDI API lookup failed: " +
                    e.toString());
            return;
        }


         /*
         * Create connection.
         * Create session from connection; false means session is
         * not transacted.
         * Create receiver, then start message delivery.
         * Receive all text messages from queue until
         * a non-text message is received indicating end of
         * message stream.
         * Close connection.
         */
        try {
            queueConnection =
                    queueConnectionFactory.createQueueConnection();
            queueSession =
                    queueConnection.createQueueSession(false,
                            Session.AUTO_ACKNOWLEDGE);
            queueReceiver = queueSession.createReceiver(queue);
            queueConnection.start();
            while (true) {
                Message m = queueReceiver.receive(1);
                if (m != null) {
                    if (m instanceof TextMessage) {
                        message = (TextMessage) m;
                        System.out.println("Reading message: " +
                                message.getText());
                    } else {
                        break;
                    }
                }
            }
        } catch (JMSException e) {
            System.out.println("Exception occurred: " +
                    e.toString());
        } finally {
            if (queueConnection != null) {
                try {
                    queueConnection.close();
                } catch (JMSException e) {}
            }
        }



    }
}
