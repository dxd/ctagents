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

import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Board;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GamePalette;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.HistoryLog;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.RowCol;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameConfigDetailsRunnable;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;



/**
	<b>Description</b>
	This class provides the server-side xml-rpc handlers.
	The methods are commands that clients can send to the server.
	
	<p>
	<b>Notes</b>

	
	<b>Original Description</b>
 * A handler for commands which user or agent clients send to the server.
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public class ClientCommands  extends Thread implements MessageListener {

	/* JMS data structures */
	
    private Connection connection;
    private MessageProducer producer;
    private Session session;	
    private Topic topic;
    private MessageConsumer consumer;
    private ActiveMQConnectionFactory factory; 
    private Logger log = Logger.getRootLogger();
    private int gameid;
        
    Object clientResponseLock = new Object();
    Boolean allClientsResponded = false;

    HashMap responsemap;
	
    public ClientCommands(int gameid, String topicName) {
    	
    	super();
    	
    	this.gameid = gameid;
    	
    	ColoredTrailsServer ct = ColoredTrailsServer.getInstance();
    	String url = "tcp://" + ct.getServerHostname() + ":" + Integer.toString(ct.getServerPort());
		log.debug("ClientCommands creating new Topic: " + topicName);

		factory = new ActiveMQConnectionFactory(url);
		factory.setObjectMessageSerializationDefered(true);

		try {
			connection = factory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			topic =  session.createTopic(topicName);
			
			
			consumer = session.createConsumer(topic, "type = 'server' OR type = 'admin'");
			producer = session.createProducer(topic);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			consumer.setMessageListener(this);
			connection.start();
		
		}  catch (JMSException e) {
			log.fatal("Subscribing to Game:"  + e);
			System.exit(-1);
		}
	}

    public void onMessage(Message msg) {
        
        try{
            String cmd = msg.getStringProperty(ColoredTrailsServer.COMMAND);
            if(cmd != null){
                if(cmd.equals(ColoredTrailsServer.RESPONSEREQUIRED_REPLY)){
                    System.out.println("received RESPONSE REQUIRED REPLY");
                    int clientid = msg.getIntProperty(ColoredTrailsServer.CLIENT_NAME);
                    ServerGameStatus gs = ServerData.getInstance().getGameStatusById(gameid);
                    gs.getPlayerByPerGameId(clientid).set("repliedtocommand", new Boolean(true));
                    
                    if(msg instanceof ObjectMessage)
                        responsemap.put( clientid , ((ObjectMessage)msg).getObject());
                    
                    
                    
                    Boolean allreplied = true;
                    for(PlayerStatus ps : gs.getPlayers()){
                        if(ServerData.getInstance().getPlayerConnection(ps.getPin()).getClientClassType().equals("HumanGUI") && 
                                !((Boolean)ps.get("repliedtocommand")))
                            allreplied = false;
                    }
                    
                    
                    if(allreplied){
                        synchronized(clientResponseLock){
                            allClientsResponded = true;
                            clientResponseLock.notifyAll();
                        }
                    }
                    return;

                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }	
		//log.debug("Game Topic received a  Message: " + msg);

        if(msg instanceof ObjectMessage) {
			
			Object obj = null;
			String req = "";
			int clientid = -1;
			int perGameId = -1;
			int toPerGameId = -1;
			
			try {
				obj = ((ObjectMessage)msg).getObject();
				req = msg.getStringProperty(ColoredTrailsServer.GAMECOMMAND);
				clientid = Integer.parseInt(msg.getStringProperty(ColoredTrailsServer.CLIENT_NAME));
				perGameId = Integer.parseInt(msg.getStringProperty(ColoredTrailsServer.PERGAMEID));
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(req.equals(ColoredTrailsServer.MOVEREQUEST)) {
				
				log.debug("received a move request...: " + clientid);
				 ServerGameStatus gs =
		                ServerData.getInstance().getGameStatusById(gameid);
		
		       gs.getConfigRunnable().doMove(perGameId,(RowCol)obj);
				
			} else if(req.equals(ColoredTrailsServer.TRANSFERREQUEST)) {
				
				log.debug("received a transfer request... " + clientid);
				
				 try {
					toPerGameId = Integer.parseInt(msg.getStringProperty(ColoredTrailsServer.PLAYER_TO_PROPOSE));
				
				} catch (Exception e) {
					
					e.printStackTrace();
				}
				
				
		        ServerGameStatus gs =
	                ServerData.getInstance().getGameStatusById(gameid);
	        gs.getConfigRunnable().doTransfer(perGameId, toPerGameId, (ChipSet)obj);
				
			} else if(req.equals(ColoredTrailsServer.DISCOURSEREQUEST)) {
				log.debug("received a discourse mesage... " + clientid);
				
				ServerGameStatus gs =
					ServerData.getInstance().getGameStatusById(gameid);
				DiscourseMessage dm = (DiscourseMessage) obj;

	        
		        // TODO:
		        // a way to veto DiscourseMessages
		        // currently we are assuming that they arrive
		        // and that they are processed
	        
		        boolean res = true;
		        try {

		        	res = gs.getConfigRunnable().doDiscourse(dm);
		        	//System.out.println(res);
		        } catch (Exception e) {
		        	e.printStackTrace();
		        	log.error("Error with the DiscourseMessage in the Game Configuration" + e);
		        }
			}
		}
	}


    public void sendGameStartMessage(Set<PlayerStatus> name, GameStatus game) {
    	
    	//TODO: clean up GameStatus vs. ClientGameStatus
    	
    	ClientGameStatus c = new ClientGameStatus(game);
    	
    	log.debug("sendGameStart");

        for( PlayerStatus player : name ) {
            try {
                ObjectMessage gameStartMsg = session.createObjectMessage();
                c.setPerGameId(player.getPerGameId());
                gameStartMsg.setObject(c);
                gameStartMsg.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.CLIENT_MSG);
                gameStartMsg.setStringProperty(ColoredTrailsServer.GAMECOMMAND, ColoredTrailsServer.GAMESTART);
                gameStartMsg.setStringProperty(ColoredTrailsServer.OBJECTTYPE, ColoredTrailsServer.GAMESTART);
                gameStartMsg.setStringProperty(ColoredTrailsServer.CLIENT_NAME, Integer.toString(player.getPin()));
                producer.send(gameStartMsg);
            } catch (JMSException e) {
                log.error("Error sending game start message" + e);
            }
        }
    }

    public void sendGameInitializedMessage() {
        sendTexMessage( ColoredTrailsServer.GAMEINIT, "Error sending game initialized message " );
    }

    public void sendGameEndMessage() {
        sendTexMessage( ColoredTrailsServer.GAMEEND, "Error sending game end message" );
    }

    private void sendTexMessage( String property, String error ) {
		try {
			TextMessage msg = session.createTextMessage();
			msg.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.CLIENT_MSG);
			msg.setStringProperty(ColoredTrailsServer.GAMECOMMAND, property );
	    	producer.send(msg);
		} catch (JMSException e) {
			log.error(error + e);
		}
    }

    /**
    	Helper function returns the game config class being used
    */
    private GameConfigDetailsRunnable getGCDR()
    {
    	return ServerData.getInstance().
		    	getGameStatusById(gameid).getConfigRunnable();
    }

    public void sendGameBoardChangedMessage(Board board, int perGameId) {
        String debug = "sendGameBoardChangedMessage";
        String error = "Error sending game board message";
  
        board = getGCDR().filterBoard(new Board(board), perGameId);
        sentSpecificChangeMessage( board, ColoredTrailsServer.BOARD, debug, error, perGameId );
    }

    public void sendGamePhasesChangedMessage(Phases phases, int perGameId) {
        String debug = "sendGamePhasesChangedMessage";
        String error = "Error sending game phases changed message";

        phases = getGCDR().filterPhases(new Phases(phases), perGameId);
        sentSpecificChangeMessage( phases, ColoredTrailsServer.PHASEUPDATE, debug, error, perGameId );
    }

    public void sendGamePlayersChangedMessage(Set<PlayerStatus> players, int perGameId) {
        String debug = "sendGamePlayersChangedMessage";
        String error = "Error sending game players updated message";

        players = getGCDR().filterPlayerStatus(new HashSet(players), perGameId);
        sentSpecificChangeMessage( (Serializable)players, ColoredTrailsServer.PLAYERSUPDATE, debug, error, perGameId );
    }

    public void sendGameLogChangedMessage(HistoryLog historyLog) {
        String debug = "sendGameLogChangedMessage";
        String error = "Error sending game log updated message";
        sentChangeMessage( historyLog, ColoredTrailsServer.HISTORYLOG, debug, error );
    }

    public void sendGamePaletteChangedMessage(GamePalette gamePalette, int perGameId) {
        String debug = "sendGamePaletteChangedMessage";
        String error = "Error sending game palette updated message";

        gamePalette = getGCDR().filterGamePalette(gamePalette, perGameId);
        sentSpecificChangeMessage( gamePalette, ColoredTrailsServer.GAMEPALETTE, debug, error, perGameId );
    }
	
    public void sendArbitraryMessage(String msgtxt)
    {
        String debug = "sendArbitraryMessage";
        String error = "Error sending Arbitrary message";
        sentChangeMessage(msgtxt, ColoredTrailsServer.ARBITRARYMSG, debug, error);
    }


    public void sendScoringChangedMessage( Scoring scoring ) {
        String debug = "sendScoringChangedMessage";
        String error = "Error sending scoring message";
        sentChangeMessage( scoring, ColoredTrailsServer.SCORING, debug, error );
    }

    public void sendGamePhaseAdvancedMessage(Phases phases, int perGameId) {
        String debug = "sendGamePhasesAdvancedMessage";
        String error = "Error sending game phases advanced message";

        phases = getGCDR().filterPhases(new Phases(phases), perGameId);
        sentSpecificChangeMessage( phases, ColoredTrailsServer.PHASEADVANCED, debug, error, perGameId );
    }

    private void sentChangeMessage( Serializable changed, String property , String debug, String error ) {
    
        try {
            ObjectMessage msg = session.createObjectMessage();
            msg.setObject(changed);
            msg.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.CLIENT_MSG);
            msg.setStringProperty(ColoredTrailsServer.OBJECTTYPE, property );
            producer.send(msg);
        } catch (JMSException e) {
            log.error(error + e);
        }
    }

    private void sentSpecificChangeMessage( Serializable changed, String property , String debug, String error, int perGameId ) {
    
        try {
            ObjectMessage msg = session.createObjectMessage();
            msg.setObject(changed);
            msg.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.CLIENT_MSG);
            msg.setStringProperty(ColoredTrailsServer.OBJECTTYPE, property );
            //SET ID HERE
            msg.setIntProperty(ColoredTrailsServer.SPECIFICMSG, perGameId);
            producer.send(msg);
        } catch (JMSException e) {
            log.error(error + e);
        }
    }


    public void sendGameDiscourseMessage(ServerGameStatus game, DiscourseMessage dm) {
		int name = 0;
		
		log.debug("sendGameDiscourseMessage " + dm.getClass() );
        name = game.getPlayerByPerGameId(dm.getToPerGameId()).getPin();

		ObjectMessage msg;
		try {
			msg = session.createObjectMessage();
			msg.setObject(dm);
			msg.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.CLIENT_MSG);
			msg.setStringProperty(ColoredTrailsServer.OBJECTTYPE, ColoredTrailsServer.DISCOURSEMESSAGE);
			msg.setStringProperty(ColoredTrailsServer.CLIENT_NAME, Integer.toString(name));
			log.debug("FROM: " + dm.getFromPerGameId() + " TO: " + dm.getToPerGameId() + "(PIN: " + name + ")");
			producer.send(msg);
		} catch (Exception e) {
			log.error("Error sending game palette updated message" + e); 
		}
	}


    public void sendClientResponseRequiredMessage(String command, Serializable data){
        //initialize
        for(PlayerStatus ps : ServerData.getInstance().getGameStatusById(gameid).getPlayers())
            ps.set("repliedtocommand", new Boolean(false));
        
        Message msg;
        try {
            if(data != null){
                msg = session.createObjectMessage();
                ((ObjectMessage)msg).setObject(data);
            } else {
                msg = session.createTextMessage();
            }
            
            msg.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.CLIENT_MSG);
            msg.setStringProperty(ColoredTrailsServer.COMMAND, ColoredTrailsServer.RESPONSEREQUIRED);
            msg.setStringProperty(ColoredTrailsServer.GAMECOMMAND, command);
            producer.send(msg);
            System.out.println("sent message for response required " + msg);
        } catch (JMSException ex) {
            java.util.logging.Logger.getLogger(ClientCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    public HashMap getClientAction(String command, Serializable data) {
        allClientsResponded = false;
        responsemap = new HashMap();
        
        System.out.println("sending response required to client");
        sendClientResponseRequiredMessage(command, data);
        
        
        
        //now we wait until responses are received
        synchronized(clientResponseLock){
            while(!allClientsResponded){
                try {
                    clientResponseLock.wait();
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(ColoredTrailsServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return(responsemap);
    }
}
