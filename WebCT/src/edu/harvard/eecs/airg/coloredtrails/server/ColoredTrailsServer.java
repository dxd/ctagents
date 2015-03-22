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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.logging.Level;

import mwspaces.CTHandler;
import mwspaces.CTsetup;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.log4j.Logger;

import edu.harvard.eecs.airg.coloredtrails.shared.PlayerConnection;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;

import java.io.Serializable;

import javax.jms.*;


/**
 * @author ricardo@eecs.harvard.edu
 */
public class ColoredTrailsServer extends Thread implements MessageListener {

    public static final int DEFAULT_PORT = 8088;


    private BrokerService broker = new BrokerService();
    private Logger log = Logger.getRootLogger();

    private String url = "tcp://localhost:" + String.valueOf(DEFAULT_PORT);


    public static final String SERVER_NAME = "ct3.server";
    public static final String GAMES_NAME = "ct3.game";

    public static final String SUBSCRIBE_CMD = "subscribe";
    public static final String HEARTBEAT = "hb";
    public static final long hbInterval = 500;
    public static final String SUBSCRIBETOGAME_CMD = "subscribe_to_game";
    public static final String SUBSCRIBETOGAME_CMD_REPLY = "subscribe_to_game_reply";

    public static final String SERVER_MSG = "server";
    public static final String CLIENT_MSG = "client";
    public static final String ADMIN_MSG = "admin";
    public static final String CONTROLLER_MSG = "controller";

    public static final String GAMETOPIC = "gametopic";

    public static final String COMMAND = "command";

    public static final String CLIENT_NAME = "client_name";
    public static final String GAMEID = "game_id";


    public static final String MSGTYPE = "type";


    public static final String CLIENT_IP = "client_ip";
    public static final String CLIENT_PIN = "client_pin";
    public static final String CLIENT_TYPE = "client_type";
    public static final String GAMECOMMAND = "game_command";


    public static final String GAMESTART = "start";
    public static final String GAMEINIT = "initialized";
    public static final String GAMEEND = "end";
    public static final String OBJECTTYPE = "objecttype";
    public static final String BOARD = "board";
    public static final String PHASEUPDATE = "phaseupdate";
    public static final String PHASEADVANCED = "phaseadvanced";
    public static final String GAMEPALETTE = "palette";
    public static final String PLAYERSUPDATE = "players";
    public static final String DISCOURSEMESSAGE = "discourse";
    public static final String MOVEREQUEST = "move";
    public static final String TRANSFERREQUEST = "transfer";
    public static final String DISCOURSEREQUEST = "discourse";
    public static final String SCORING = "scoring";

    public static final String PLAYER_TO_PROPOSE = "player_to_propose";
    public static final String PERGAMEID = "perGameID";
    public static final String GAMESTATUS = "gamestatus";
    public static final String HISTORYLOG = "historylog";
    
    public static final String ARBITRARYMSG = "ARBITRARYMSG";
    public static final String SPECIFICMSG = "SPECIFICMSG";
    
    public static final String RESPONSEREQUIRED = "RESPONSEREQUIRED";
    public static final String RESPONSEREQUIRED_REPLY = "RESPONSEREQUIRED_REPLY";

    private Connection connection;
    private MessageProducer producer;
    private Session session;
    private Topic topic;
    private MessageConsumer consumer;

    private String localhost = "localhost";
    private ActiveMQConnectionFactory factory;

    CtrlCommands ctrlCommands = null;
    AdminCommands  adminCommands;
    CTsetup spaces;
    
    /*
	 * Singleton reference and methods
	 * 
	 */

    private static ColoredTrailsServer ref;
    
  
	

    public static ColoredTrailsServer getInstance() {
        if (ref == null)

            ref = new ColoredTrailsServer();
        return ref;
    }

    /*
      * Prevent the ColoredTrailsServer Object from being cloned.
      * @see java.lang.Object#clone()
      */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();

    }


    private ColoredTrailsServer() {

        // Get the current hostname
        //

        try {
            localhost = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {

            log.fatal("Error getting the current hostname: " + e);
        }

        // Try to Start the JMS Broker

        try {


            broker.setUseJmx(true);
            url = "tcp://" + localhost + ":" + Integer.toString(DEFAULT_PORT);
            broker.addConnector(url);
            broker.start();


        } catch (Exception e) {

            log.fatal("Error starting the JMS Broker: " + e);

        }

        /*
        * Start Server Data Thread
        * This needs cleanup
        */

        ServerData sd = ServerData.getInstance();


        log.info("Started the JMS Server");

        /*
        * Here we create the main Topic of CT. Clients will connect and publish to
        * this Topic initially
        */
        createMainServer();


        // create interface to controller
        ctrlCommands = new CtrlCommands(session, producer);
        sd.ctrlHandler = ctrlCommands;
        log.info("Controller Interface has been started");

        // keep admin interface for backwards compatability
        adminCommands = new AdminCommands();
        
		
    }



    private void createMainServer() {

        factory = new ActiveMQConnectionFactory(url);

        try {
            connection = factory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            topic = session.createTopic(SERVER_NAME);
            
            //Set up server to receive ADMIN_MSG, CONTROLLER_MSG, or SERVER_MSG type messages:
            consumer = session.createConsumer(topic, "type = 'server' OR type = 'admin' OR type = 'controller'");
            producer = session.createProducer(topic);
            consumer.setMessageListener(this);
            connection.start();

        } catch (JMSException e) {
            log.fatal("Error starting the Main Topic:" + e);
        }

    }


    public void onMessage(Message msg) {
        //System.out.println("CT server received message");

        try {
            String msgType = msg.getStringProperty(MSGTYPE);
            if(msgType.equals(CONTROLLER_MSG)){
                ctrlCommands.handleMessage(msg);
            } else {
                if (msg instanceof TextMessage) {
                    if (msgType.equals(SERVER_MSG)) {
                        
                        String cmd = msg.getStringProperty(COMMAND);
                        
                        if(cmd.equals(SUBSCRIBE_CMD)){
                            subscribeClient(msg);
                        
                        } else if(cmd.equals(SUBSCRIBETOGAME_CMD_REPLY)){
                            
                            System.out.println("received client subscribed successfully message");
                            clientsubscribed(msg.getIntProperty(GAMEID), msg.getIntProperty(PERGAMEID));
                        
                        } else if (cmd.equals(ColoredTrailsServer.HEARTBEAT)) {

                            int clientid = Integer.parseInt(msg.getStringProperty(ColoredTrailsServer.CLIENT_NAME));
                            ServerData.getInstance().getClientState(clientid).heartBeatReceived();
                        } else if (cmd.equals(ColoredTrailsServer.RESPONSEREQUIRED_REPLY)){
                            ServerData.getInstance().getClientCommands(msg.getIntProperty(GAMEID)).onMessage(msg);
                        }

                    } else if (msgType.equals(ADMIN_MSG)) {

                        // For backwards compatability only
                        //System.out.println("************received admin message");
                        producer.send(adminCommands.handleMessage(msg, session));
                    }

                } else if (msg instanceof BytesMessage) {

                    System.out.println("Received message to add config class from: " + msgType);
                    //These messages are for adding config classes

                    if (msgType.equals(ADMIN_MSG)) {
                        adminCommands.setConfigClass((BytesMessage) msg);

                    }
                }
            }

        } catch (JMSException e) {
            log.fatal("Error receiving message:" + e);
        }


    }

    /**
     * Subscribing a client to the CT Server: the client is available for games
     *
     * @param msg
     * @throws JMSException
     */
    private void subscribeClient(Message msg) throws JMSException {

        ServerData sd = ServerData.getInstance();
        String client_name = msg.getStringProperty(CLIENT_NAME);
        String client_ip = msg.getStringProperty(CLIENT_IP);
        String clientClassType = msg.getStringProperty(CLIENT_TYPE);
        log.info("Colored Trails Server received a subcription message from client: " + client_name + " [" + client_ip + "] of type " + clientClassType);

        ctrlCommands.clientConnected(Integer.valueOf(client_name));
        
        sd.addNameToIpMapping(client_name, client_ip);

        if (sd.getIpByPlayerName(client_name) == null) {
            /*
             * TODO: Send a message indicating this failure
             */
        }

        /*
        * TODO: Clean up PlayerConnection interface
        * it ought to accept player String identifying the player and not just an
        * integer id
        */

        //sd.removePlayer(Integer.parseInt(client_name));
        sd.addPlayer(new PlayerConnection("UNASSIGNED", Integer.parseInt(client_name),
                sd.getIpByPlayerName(client_name), 0, clientClassType)); // TODO: clean up PlayerConnection so it doesn't take a port number

    }




    /* (non-Javadoc)
      * @see java.lang.Thread#run()
      */
    @Override
    public void run() {
        log.info("ColoredTrails is now running...");
        
    }

    
    
    /*
      * Send a message to clients to subscribe to the particular
      */


    public void sendClientSubscribeToGameMessage(int gameid, Vector<PlayerStatus> players) {

        try {

            // create the message to send to the players involved in this game


            for (int i = 0; i < players.size(); i++) {
                log.debug("sending sendClientSubscribeToGameMessage: for game [" + gameid + "] and player [" + players.get(i).getPin() + "]");
                TextMessage subscribeMessage = session.createTextMessage();
                subscribeMessage.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.CLIENT_MSG);
                subscribeMessage.setStringProperty(ColoredTrailsServer.COMMAND, ColoredTrailsServer.SUBSCRIBETOGAME_CMD);
                subscribeMessage.setStringProperty(ColoredTrailsServer.CLIENT_NAME, Integer.toString(players.get(i).getPin()));
                subscribeMessage.setStringProperty(ColoredTrailsServer.PERGAMEID, Integer.toString(players.get(i).getPerGameId()));
                subscribeMessage.setStringProperty(ColoredTrailsServer.GAMETOPIC, GAMES_NAME + "." + Integer.toString(gameid));
                subscribeMessage.setStringProperty(ColoredTrailsServer.GAMEID, Integer.toString(gameid));
                producer.send(subscribeMessage);

            }


        } catch (JMSException e) {
            log.fatal("Error during sendClientSubscribeToGameMessage:" + e);
        }

    }

    public int getServerPort() {

        return DEFAULT_PORT;
    }

    public String getServerHostname() {
        // TODO: generealize this

        String hostname_ip = localhost;
        try {
            hostname_ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return hostname_ip;

    }
    
    private void clientsubscribed(int gameid, int pergameid)
    {
        //called after client has subscribed to game
        ServerGameStatus game = ServerData.getInstance().getGameStatusById(gameid);
        game.setClientSubscribed(pergameid, true);
        for(PlayerStatus ps : game.getPlayers()){
            if(!game.getClientSubscribed(ps.getPerGameId()))
                return;
        }
        System.out.println("All players subscribed to game, trying to start");
        ServerData.getInstance().startgame(gameid);
    }

    public CtrlCommands getCtrlCommands(){
        return(ctrlCommands);
    }
    
    

}

