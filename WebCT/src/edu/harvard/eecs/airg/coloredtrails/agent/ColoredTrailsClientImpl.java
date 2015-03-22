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

package edu.harvard.eecs.airg.coloredtrails.agent;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import java.util.logging.Level;


import edu.harvard.eecs.airg.coloredtrails.agent.events.BoardUpdatedEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.DiscourseMessageEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.GameEndedEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.GameInitializedEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.GamePaletteUpdatedEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.GameStartEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.LogUpdatedEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.PhasesAdvancedEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.PhasesUpdatedEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.PlayersUpdatedEventListener;
import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.client.ui.discourse.DiscourseHandler;
import edu.harvard.eecs.airg.coloredtrails.server.ColoredTrailsServer;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Board;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GamePalette;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.HistoryLog;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.RowCol;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import java.io.Serializable;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;


import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;




/**
 * The base Agent Class for the Colored Trail System
 *
 * @author Ricardo De Lima (ricardo@eecs.harvard.edu)
 */

public class ColoredTrailsClientImpl extends Thread implements ColoredTrailsClient, MessageListener, ExceptionListener {


    private Logger log = Logger.getRootLogger();


	private JMSAgentClientThread serverThread;
	
	private String serverHostname;
	private int serverPort;
	private String gameID;
	
		
	private int perGameID;
	private String clientName;
	private ClientGameStatus game;
	
	/*
	 * Event Listener array, this class ought to be able to fire events
	 * to any object that registers interests in particular events
	 */

	private Vector<BoardUpdatedEventListener> boardUpdatedListeners = new Vector<BoardUpdatedEventListener>();
	private Vector<GameStartEventListener> gameStartedListeners = new Vector<GameStartEventListener>();
	private Vector<GameInitializedEventListener> gameInitializedListeners = new Vector<GameInitializedEventListener>();
	private Vector<GameEndedEventListener> gameEndedListeners = new Vector<GameEndedEventListener>();
	private Vector<DiscourseMessageEventListener> discourseMessageListeners = new Vector<DiscourseMessageEventListener>();
	private Vector<GamePaletteUpdatedEventListener> gamePaletteUpdatedListeners = new Vector<GamePaletteUpdatedEventListener>();
	private Vector<LogUpdatedEventListener> logUpdatedListeners = new Vector<LogUpdatedEventListener>();
	private Vector<PlayersUpdatedEventListener> playersUpdatedListeners = new Vector<PlayersUpdatedEventListener>();
	private Vector<PhasesUpdatedEventListener> phasesUpdatedListeners = new Vector<PhasesUpdatedEventListener>();
	private Vector<PhasesAdvancedEventListener> phasesAdvancedListeners = new Vector<PhasesAdvancedEventListener>();
	

	
	
	
	/* JMS data structures */
	
	private Connection connection;
	private MessageProducer producer;
	private Session session;	
	private Topic topic;
	private MessageConsumer consumer;
	
	/*
	 * 
	 * 
	 */
	
	 public Communication communication = new Communication();
	 
	 
	 /*
	  * 
	  */
	 
	 
	 private boolean connected = false;
	 private boolean subscribed = false;
         private boolean ended = false;
         
         private Object lock;
         
//         HeartBeat hb;
         
         //this is the client type
         String clientClassType = "";
     
    /*
	 *  AgentClientThread constructor
	 *  sets the appropriate defaults for a CT client 
	 *  can be overritten by the appropriate 
	 *  property setters before the start()/run() methods 
	 *  are invoked on an Agent
	 *  
	 */
  
     public ColoredTrailsClientImpl(String type) {
        this();
        this.clientClassType = type;
     }
     
     public ColoredTrailsClientImpl() {
  
    	/*
    	 * Set server IP to localhost as a default
    	 * This should be overriden
    	 * 
    	 */
    	try {
            setServerHostname(InetAddress.getLocalHost().getHostAddress());
            setServerPort(ColoredTrailsServer.DEFAULT_PORT);
        } catch (UnknownHostException e) {

            e.printStackTrace();
        }
    }

	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#getGameId()
	 */
	public final String getGameId() {
		return gameID;
	}

	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#setGameId(int)
	 */
	
	public final void setGameId(String currentGameId) {
		gameID = currentGameId;
	}

	/**
	 * @return The old gamestatus object, this is 
	 * for our own use of the legacy object ClientGameStatuse
	 * 
	 */

	public ClientGameStatus getGameStatus() {
		return game;
	}

	/**
	 * @param game The gamestatus object, this is 
	 * for our own use of the legacy object ClientGameStatuse
	 */

	private void setGameStatus(ClientGameStatus game) {
		 this.game = game;
	}



	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#getServerHostPort()
	 */
	public final int getServerHostPort() {
		return serverPort;
	}
	
    
    /* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#getServerHostIp()
	 */
    public final String getServerHostname() {
		
		return serverHostname;
	}



	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#setServerHostIp(java.lang.String)
	 */
	public final void setServerHostname(String s) {
		this.serverHostname = s;
		//System.out.println("Agent Hostname: " + this.serverHostname);
	}



	public  String getPin() {
		return clientName;
	}
	
	public  void setPin(String pin) {
		clientName = pin;
	}





    /* (non-Javadoc)
	 * @see javax.jms.ExceptionListener#onException(javax.jms.JMSException)
	 */
	public void onException(JMSException arg) {
		log.fatal("JMS Exception occured.  Shutting down client." + arg);
		System.exit(-1);
		
	}



	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#run()
	 */
	public final void run()
	{
		serverThread = new JMSAgentClientThread(this);
		serverThread.setPin(this.getPin());
		serverThread.setServerHostname(this.getServerHostname());
		serverThread.setServerPort(this.getServerPort());

		log.debug("JMSAgentClient run() method invoked");
		
		serverThread.start();	    
	}
	

    /* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#sendMoveRequest(edu.harvard.eecs.airg.coloredtrails.shared.types.RowCol)
	 */
	
	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#getPosition()
	 */
    public RowCol getPosition() {
        return getGameStatus().getMyPlayer().getPosition();
    }

    /* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#setPosition(edu.harvard.eecs.airg.coloredtrails.shared.types.RowCol)
	 */
    public void setPosition(RowCol position) {
    	getGameStatus().getMyPlayer().setPosition(position);
    }
    
    
    /* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#areMovesAllowed()
	 */
    public boolean areMovesAllowed() {
        return getGameStatus().getMyPlayer().areMovesAllowed();
    }

    
    /* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#getScore()
	 */
    public int getScore() {
        return getGameStatus().getMyPlayer().getScore();
    }

    /* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#getChips()
	 */
    public ChipSet getChips() {
        return getGameStatus().getMyPlayer().getChips();
    }
    
    /* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#areTransfersAllowed()
	 */
    public boolean areTransfersAllowed() {
        return getGameStatus().getMyPlayer().areTransfersAllowed();
    }
    
   
    public boolean isCommunicationAllowed() {
        return getGameStatus().getMyPlayer().isCommunicationAllowed();
    }
    
    
    
    /* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#getPlayers()
	 */
    public Set<PlayerStatus> getPlayers() {
        return getGameStatus().getPlayers();
    }  
    
    
    
    /*
     * (non-Javadoc)
     * @see edu.harvard.eecs.airg.coloredtrails.agent.GameEventListener#boardUpdated(edu.harvard.eecs.airg.coloredtrails.shared.types.Board)
     */
                                               
	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#boardUpdated(edu.harvard.eecs.airg.coloredtrails.shared.types.Board)
	 */
	public void boardUpdated(Board b) {
		
	}


	/*
	 * (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.GameEventListener#gameEnded()
	 */
	
	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#gameEnded()
	 */
	public  void gameEnded() {
            System.out.println("(((((((((((((((((Game ended)))))))))))");
            ended = true;
            //hb.close();
            try {
                game.getPhases().finalize();
            } catch (Throwable ex) {
                java.util.logging.Logger.getLogger(ColoredTrailsClientImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
	}
        
        public boolean getEnded()
        {
            return(ended);
        }

	/*
	 * (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.GameEventListener#gamePaletteUpdated(edu.harvard.eecs.airg.coloredtrails.shared.types.GamePalette)
	 */

	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#gamePaletteUpdated(edu.harvard.eecs.airg.coloredtrails.shared.types.GamePalette)
	 */
	public void gamePaletteUpdated(GamePalette g) {
		
	}

	/*
	 * (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.GameEventListener#gameStarted()
	 */

	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#gameStarted()
	 */
	public  void gameStarted() {
		
	}
	
	/*
	 * NEW (SGF) for new event to signal game has been initialized
	 */
	public void gameInitialized()
	{
		
	}

	/*
	 * (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.GameEventListener#logUpdated(edu.harvard.eecs.airg.coloredtrails.shared.types.HistoryLog)
	 */

	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#logUpdated(edu.harvard.eecs.airg.coloredtrails.shared.types.HistoryLog)
	 */
	public  void logUpdated(HistoryLog hl) {
		
	}

	/*
	 * (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.GameEventListener#phaseAdvanced(edu.harvard.eecs.airg.coloredtrails.shared.types.Phases)
	 */

	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#phaseAdvanced(edu.harvard.eecs.airg.coloredtrails.shared.types.Phases)
	 */
	public  void phaseAdvanced(Phases ph) {
		
	}

	/*
	 * (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.GameEventListener#phasesUpdated(edu.harvard.eecs.airg.coloredtrails.shared.types.Phases)
	 */

	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#phasesUpdated(edu.harvard.eecs.airg.coloredtrails.shared.types.Phases)
	 */
	public  void phasesUpdated(Phases ph) {
		
	}

	/*
	 * (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.GameEventListener#playersUpdated(java.util.ArrayList)
	 */

	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#playersUpdated(java.util.ArrayList)
	 */
	public  void playersUpdated(Set<PlayerStatus> ps) {
		
	}



	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.GameEventListener#onReceipt(edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage)
	 */
	public  void onReceipt(DiscourseMessage dm) {
		
	}





	public int getServerPort() {
		return serverPort;
	}



	public void setServerPort(int port) {
		serverPort = port;
		
	}



	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message msg)
	{
            try
            {
               
                
                String cmd = msg.getStringProperty(ColoredTrailsServer.COMMAND);
                if(cmd != null){
                    if(cmd.equals(ColoredTrailsServer.RESPONSEREQUIRED)){
                        System.out.println("received RESPONSE REQUIRED message from server");
                        Serializable data = null;
                        if(msg instanceof ObjectMessage)
                            data = ((ObjectMessage)msg).getObject();
                        String command = msg.getStringProperty(ColoredTrailsServer.GAMECOMMAND);
                        Serializable response = responserequired(command, data);
                        
                        Message reply;
                        if(response == null){
                            reply = session.createTextMessage();
                        } else {
                            reply = session.createObjectMessage();
                            ((ObjectMessage)reply).setObject(response);
                        }
                        reply.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.SERVER_MSG);
                        reply.setStringProperty(ColoredTrailsServer.COMMAND, ColoredTrailsServer.RESPONSEREQUIRED_REPLY);
                        reply.setIntProperty(ColoredTrailsServer.CLIENT_NAME, perGameID);
                        reply.setIntProperty(ColoredTrailsServer.GAMEID, Integer.parseInt(gameID));
                        producer.send(reply);
                        return;

                    }
                }
            }
            catch (JMSException e1)
            {
                    e1.printStackTrace();
            }
            
            if(msg instanceof TextMessage)
            {
                /*
                 * First obtain the command from the Message
                 */
                String cmd = "";

                try
                {
                        cmd = msg.getStringProperty(ColoredTrailsServer.GAMECOMMAND);
                }
                catch (JMSException e)
                {
                        log.warn("Error trying to get the game command from the message");
                }


                /*
                 * now check which type of message it is and call the appropriate method
                 */
                if (cmd.equals(ColoredTrailsServer.GAMEEND))
                {
                    log.debug("Game End Event");

                    getGameStatus().setEnded();
                    this.gameEnded();				

                    // Notify all listeners the game has ended
                    for(GameEndedEventListener gl : gameEndedListeners)
                        gl.gameEnded();
                } else if (cmd.equals(ColoredTrailsServer.GAMEINIT)) {
                    log.debug("Game Initialized Event");

                    getGameStatus().setInitialized();
                    this.gameInitialized();

                    // notify all listeners that the game has been initialized
                    for (GameInitializedEventListener gl : gameInitializedListeners)
                        gl.gameInitialized();
                }
            } 

            // These are the Board, Phase, Players, Discourse, etc
            else if(msg instanceof ObjectMessage)
            {
                String cmd = "";

                try
                {
                        cmd = msg.getStringProperty(ColoredTrailsServer.OBJECTTYPE);
                }
                catch (JMSException e)
                {
                        log.warn("Error trying to get the game object type from the message");
                }


                /*
                 * now check which type of message it is and call the appropriate method
                 */
                if(cmd.equals(ColoredTrailsServer.GAMESTART))
                {
                    log.debug("Game Started Event");
                    ObjectMessage obj = (ObjectMessage) msg;

                    ClientGameStatus c = null;

                    try
                    {
                        c = (ClientGameStatus) obj.getObject();
                    }
                    catch (Exception e)
                    {
                        log.error("Exception trying to get Game Start message:" + e);
                    }

                    /*
                     * pergameId (soon to be roles) and gameId have been set by the subscribing mechanism
                     * at this point
                     */
                    c.setPerGameId(getPerGameID());
                    setGameStatus(c);
//                    System.out.println("BEFORE");
//                    hb = new HeartBeat(communication);
//                    System.out.println("AFTER");

                    this.gameStarted();

                            // Notify all listeners the game has started
                    for(GameStartEventListener gl : gameStartedListeners)
                        gl.gameStarted();			

                } else if(cmd.equals(ColoredTrailsServer.BOARD)) {
                    //log.debug("Board Updated Event");
                    ObjectMessage obj = (ObjectMessage)msg;

                    Board board = null;

                    try {
                        int forperGameId = obj.getIntProperty(ColoredTrailsServer.SPECIFICMSG);
                        if(forperGameId != perGameID)
                            return;
                        board = (Board) obj.getObject();
                    } catch (JMSException e) {
                        log.error("Exception trying to get BoardUpdate message");
                    }
                    ClientGameStatus gs = getGameStatus();
                    gs.setBoard(board);
                    boardUpdated(board);

                            // Notify all listeners the board has been updated
                    for(BoardUpdatedEventListener gl:boardUpdatedListeners)
                        gl.boardUpdated(board);
                } else if(cmd.equals(ColoredTrailsServer.ARBITRARYMSG)) {
                    ObjectMessage obj = (ObjectMessage)msg;
                    
                    String txtmsg = null;
                    try {
                        txtmsg = (String) obj.getObject();
                    } catch (JMSException e) {
                        log.error("Exception trying to get Arbitrary message");
                    }
                    
                    System.out.println("Got arbitrary message: " + txtmsg);
                    
                    ClientGameStatus gs = getGameStatus();
                    ((GameStatus)gs).setArbitraryMessage(txtmsg);

                    
                } else if(cmd.equals(ColoredTrailsServer.SCORING)) {
                    log.debug("Scoring Updated Event");
                    ObjectMessage obj = (ObjectMessage)msg;

                    Scoring scoring = null;

                    try {
                        scoring = (Scoring) obj.getObject();
                    } catch (JMSException e) {
                        log.error("Exception trying to get BoardUpdate message");
                    }
                    ClientGameStatus gs = getGameStatus();
                    gs.setScoring(scoring);
                } else if (cmd.equals(ColoredTrailsServer.GAMEPALETTE)){
                    log.debug("GamePalette Updated Event");
                    ObjectMessage obj = (ObjectMessage)msg;

                    GamePalette gp = null;

                    try {
                        int forperGameId = obj.getIntProperty(ColoredTrailsServer.SPECIFICMSG);
                        if(forperGameId != perGameID)
                            return;
                        gp = (GamePalette) obj.getObject();
                    } catch (JMSException e) {
                        log.error("Exception trying to get GamePalette message");
                    }

                    ClientGameStatus gs = getGameStatus();
                    gs.setGamePalette(gp);

                    gamePaletteUpdated(gp);

                    // Notify all listeners the game palette has been updated
                    for(GamePaletteUpdatedEventListener gl : gamePaletteUpdatedListeners)
                        gl.gamePaletteUpdated(gp);				
                } else if (cmd.equals(ColoredTrailsServer.HISTORYLOG)){
                    log.debug("HistoryLog Updated Event");
                    ObjectMessage obj = (ObjectMessage)msg;

                    HistoryLog hlog = null;

                    try {
                        hlog = (HistoryLog) obj.getObject();
                    } catch (JMSException e) {
                        log.error("Exception trying to get History Log message");
                    }

                    ClientGameStatus gs = getGameStatus();
                    gs.setHistoryLog(hlog);

                    // Notify all listeners the log has been updated		        
                    for(LogUpdatedEventListener gl:logUpdatedListeners)
                        gl.logUpdated(hlog);
                } else if (cmd.equals(ColoredTrailsServer.PHASEADVANCED)) {
                    log.debug("PhaseAdvanced  Event");
                    ObjectMessage obj = (ObjectMessage)msg;

                    Phases p = null;

                    try {
                        int forperGameId = obj.getIntProperty(ColoredTrailsServer.SPECIFICMSG);
                        if(forperGameId != perGameID)
                            return;
                        p = (Phases) obj.getObject();
                    } catch (JMSException e) {
                        log.error("Exception trying to get PhaseAdvanced message");
                    }
                    ClientGameStatus gs = getGameStatus();
                    gs.setPhases(p);
                    phaseAdvanced(p);

                    // Notify all listeners the phases have been advanced
                    for(PhasesAdvancedEventListener gl:phasesAdvancedListeners)
                        gl.phaseAdvanced(p);
                } else if (cmd.equals(ColoredTrailsServer.PHASEUPDATE)) {
                    log.debug("PhaseUpdated  Event");
                    ObjectMessage obj = (ObjectMessage)msg;

                    Phases p = null;

                    try {
                        int forperGameId = obj.getIntProperty(ColoredTrailsServer.SPECIFICMSG);
                                                
                        if(forperGameId != perGameID)
                            return;
                        p = (Phases) obj.getObject();
                    } catch (JMSException e) {
                        log.error("Exception trying to get PhaseAdvanced message:" + e.getMessage());
                        if (e.getLinkedException() != null)
                        {
                        	System.out.println("LINKED EXCEPTION: " + e.getLinkedException().getMessage());
                        }
                    }

                    /*
                    * CT was designed to call PhaseAdvanced(p)
                    * every time the phases were update
                    * Now that we are breaking this protocol in order
                    * to do proper synchronization, will there be repercussions
                    * on the legacy code of the UI?
                    * TODO: revisit the UI
                    */
                    // phaseAdvanced(p);

                    ClientGameStatus gs = getGameStatus();
                    gs.setPhases((Phases)p);

                    // TODO: consider pushing the responsibility to 
                    // start the phase timer to
                    // the particular implementation of an Agent

                    //gs.getPhases().resetTimer();

                    phasesUpdated(p);

                            // Notify all listeners the phases have been updated
                    for(PhasesUpdatedEventListener gl:phasesUpdatedListeners)
                        gl.phasesUpdated(p);
                } else if (cmd.equals(ColoredTrailsServer.PLAYERSUPDATE)) {
                    //log.debug("Players Updated  Event");
                    ObjectMessage obj = (ObjectMessage)msg;

                    Set<PlayerStatus> p = null;

                    try {
                        int forperGameId = obj.getIntProperty(ColoredTrailsServer.SPECIFICMSG);
                        if(forperGameId != perGameID)
                            return;
                        p = (Set<PlayerStatus>) obj.getObject();
                    } catch (JMSException e) {
                        log.error("Exception trying to get PlayerStatus message");
                    }
                    ClientGameStatus gs = getGameStatus();
                    gs.setPlayers(p);
                    playersUpdated(p);

                    // Notify all listeners the players have been updated		        
                    for(PlayersUpdatedEventListener gl:playersUpdatedListeners)
                        gl.playersUpdated(p);
                    
                } else if (cmd.equals(ColoredTrailsServer.DISCOURSEMESSAGE)){
                    log.debug("Discourse Message  Event");
                    ObjectMessage obj = (ObjectMessage)msg;

                    DiscourseMessage dm = null;

                    try {
                        dm = (DiscourseMessage) obj.getObject();
                    } catch (JMSException e) {
                        log.error("Exception trying to get Discourse message");
                    }

                    try {
                        for (DiscourseHandler dh :communication.getDiscourseHandlers()) {
                            if (dh.getType().equals(dm.getMsgType())) {
                                dh.onReceipt(dm);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    onReceipt(dm);

                    // Notify all listeners a discourse message has arrived
                    // TODO: there are now to distinct ways of doing discourseMessages
                    // we need to coalesce and pick one strategy, either "handlers" or 
                    // just this interface "onReceipt(DicourseMessage dm) or a equivalent
                    // I think the latter is simpler to understand and makes the user write less code
                    for(DiscourseMessageEventListener gl:discourseMessageListeners)
                            gl.onReceipt(dm);
                }
            }
	}

	
	/*
	 * used by the Server Thread to set the JMS to topic for this Agent to
	 * subscribe to
	 */
	public void subscribeToGame(String topicName)
	{		
		/*
		 *  Creating the First JMS Elements before connecting to the server
		 */
		String url = "tcp://" + getServerHostname() + ":" + Integer.toString(getServerPort());
		log.info("Subscribing to Game: [" + url + "] " + topicName);
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);

		try
		{
			connection = factory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			topic =  session.createTopic(topicName);

			connection.setExceptionListener(this);
			
			/*
			 * This selector to the consumer specifies that this client is to listen
			 * for messages directed to ALL CLIENTS
			 * and also for messages directed directly to it
			 */
			consumer = session.createConsumer(topic, "(type = 'client' AND client_name IS NULL) OR (type = 'client' AND client_name = '" + getPin() + "')");
			// consumer = session.createConsumer(topic);
			producer = session.createProducer(topic);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			consumer.setMessageListener(this);

			connection.start();
                        
		}
		catch (JMSException e)
		{
			log.fatal("Subscribing to Game:"  + e);
			System.exit(-1);
		}    	
		
		/*
		 * create the communication object here
		 */
		communication.setSession(session);
		communication.setProducer(producer);
		communication.setClientName(clientName);
		communication.setPerGameId(String.valueOf(perGameID));
		
		/*
		 *  After the JMS subscription to the particular game is completed
		 *  we can now wait to receive messages and execute the code in connected
		 */		
		subscribedToGame();
		waitAndLock();
                
                //We are shutting down now:
                try {
                    consumer.close();
                    producer.close();
                    session.close();
                    connection.close();
                    
                } catch (JMSException ex) {
                    java.util.logging.Logger.getLogger(ColoredTrailsClientImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
	}


	private void subscribedToGame(){	
		log.debug("JMSAgentClient has succesfully Subscribed to the Game");
        setSubscribed(true);
                
        //HERE
        serverThread.sendsubscribedreply();
        
		connected();

    }
	

    public  void connected(){
        
    }

	private void waitAndLock()
	{
		// now lets wait forever to avoid the JVM terminating immediately
		lock = new Object();
		synchronized (lock)
		{
			try
			{
				lock.wait();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public boolean isSubscribed() {
		return subscribed;
	}

	public void setSubscribed(boolean subscribed) {
		this.subscribed = subscribed;
	}

	public int getPerGameID() {
		return perGameID;
	}

	public void setPerGameID(int perGameID) {
		this.perGameID = perGameID;
	}

	public int getTeamId() {
		return game.getMyPlayer().getTeamId();
	}
	
	public void setTeamId(int teamId) {
		game.getMyPlayer().setTeamId(teamId);
	}
	
	
	/*
	 *  Event Listeners add/remove
	 */
	public synchronized void addBoardUpdatedEventListener(BoardUpdatedEventListener l) {
		boardUpdatedListeners.addElement(l);
	}

	public synchronized void removeBoardUpdatedListener(BoardUpdatedEventListener l) {
		boardUpdatedListeners.removeElement(l);
	}
	

	public synchronized void addGameStartEventListener(GameStartEventListener l) {
		gameStartedListeners.addElement(l);
	}

	public synchronized void removeGameStartEventListener(GameStartEventListener l) {
		gameStartedListeners.removeElement(l);
	}
	
	
	public synchronized void addGameInitializedEventListener(GameInitializedEventListener l) {
		gameInitializedListeners.addElement(l);
	}

	public synchronized void removeGameInitializedEventListener(GameInitializedEventListener l) {
		gameInitializedListeners.removeElement(l);
	}
	
	
	public synchronized void addGameEndedEventListener(GameEndedEventListener l) {
		gameEndedListeners.addElement(l);
	}

	public synchronized void removeGameEndedEventListener(GameEndedEventListener l) {
		gameEndedListeners.removeElement(l);
	}
	
	
	public synchronized void addDiscourseMessageEventListener(DiscourseMessageEventListener l) {
		discourseMessageListeners.addElement(l);
	}

	public synchronized void removeDiscourseMessageEventListener(DiscourseMessageEventListener l) {
		discourseMessageListeners.removeElement(l);
	}
	
	
	public synchronized void addGamePaletteUpdatedEventListener(GamePaletteUpdatedEventListener l) {
		gamePaletteUpdatedListeners.addElement(l);
	}

	public synchronized void removeGamePaletteUpdatedEventListener(GamePaletteUpdatedEventListener l) {
		gamePaletteUpdatedListeners.removeElement(l);
	}
	
	
	
	public synchronized void addLogUpdatedEventListener(LogUpdatedEventListener l) {
		logUpdatedListeners.addElement(l);
	}

	public synchronized void removeLogUpdatedEventListener(LogUpdatedEventListener l) {
		logUpdatedListeners.removeElement(l);
	}
	
	
	
	
	public synchronized void addPlayersUpdatedEventListener(PlayersUpdatedEventListener l) {
		playersUpdatedListeners.addElement(l);
	}

	public synchronized void removePlayersUpdatedEventListener(PlayersUpdatedEventListener l) {
		playersUpdatedListeners.removeElement(l);
	}
	
	
	public synchronized void addPhasesUpdatedEventListener(PhasesUpdatedEventListener l) {
		phasesUpdatedListeners.addElement(l);
	}

	public synchronized void removePhasesUpdatedEventListener(PhasesUpdatedEventListener l) {
		phasesUpdatedListeners.removeElement(l);
	}
	
	
	public synchronized void addPhasesAdvancedEventListener(PhasesAdvancedEventListener l) {
		phasesAdvancedListeners.addElement(l);
	}

	public synchronized void removePhasesAdvancedEventListener(PhasesAdvancedEventListener l) {
		phasesAdvancedListeners.removeElement(l);
	}
	
	
	//this function releases the lock acquired in waitandlock()
	public void releaseLock(){
            if(lock != null){
                synchronized(lock){
                    lock.notifyAll();
                }
            }
        }
        
        public String getClientClassType(){
            return(clientClassType);
        }
        
        public Serializable responserequired(String command, Serializable data){
            return(null);
        }
	
}





/**
 * A private class to handle CT Server subscription duties 
 * and future functionality that will allow us to control
 * the client from the admin
 * 
 * @author Ricardo De Lima (ricardo@eecs.harvard.edu)
 */

class JMSAgentClientThread extends Thread implements MessageListener {

	private Logger log = Logger.getRootLogger();


	private String serverHostname;
	private int serverPort;
	private String clientName;
		
	/* JMS data structures */
	private Connection connection;
	private MessageProducer producer;
	private Session session;	
	private Topic topic;
	private MessageConsumer consumer;


	private ColoredTrailsClientImpl agent;
	
	private boolean gameSubscription = false;
	

	private String currentGameTopic;
        
        HeartBeat hb;
        
        //thread used for running ctclient
        private Thread t = null;
	
	
	/*
	 *  AgentClientThread constructor
	 *  sets the appropriate defaults for a CT client 
	 *  can be overriden by the appropriate 
	 *  property setters before the start()/run() methods 
	 *  are invoked on an Agent
	 *  
	 */
    public JMSAgentClientThread(ColoredTrailsClientImpl agent) {
  
    	this.agent = agent;
    	
 
    	/*
    	 * Set server IP to localhost as a default
    	 * This should be overriden
    	 * 
    	 */
    	try {
			setServerHostname(InetAddress.getLocalHost().getHostAddress());
			setServerPort(ColoredTrailsServer.DEFAULT_PORT);
		} catch (UnknownHostException e) {

			e.printStackTrace();
		}

    	
    }

    
    




	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#getServerHostIp()
	 */
    public final String getServerHostname() {
		
		return serverHostname;
	}



	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#setServerHostIp(java.lang.String)
	 */
	public final void setServerHostname(String s) {
		this.serverHostname = s;
	}




	public  String getPin() {
		return clientName;
	}
	
	public  void setPin(String name) {
		clientName = name;
	}



	/**
     *  Method called after the registration has taken place
     */
    
    protected  boolean connected() {
  
    	/*
    	 * We are connected at this point, let's change the agent status
    	 * to that state
    	 * 
    	 */
    	
    	agent.setConnected(true);
        
        hb = new HeartBeat(producer, session, clientName);
    	
    	/*
    	 *  loop and sleep until a game subscription message is received
    	 */
    	
    	
    	while(isGameSubscription() == false) {

            try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			
				log.warn("Error waiting for the game subsription message: " + e);
			}
            
    	}
    	
        /*
         * delegate to the  agent proper to subscribe to the game
         * the server has asked us to subscribed to
         * 
         */

        t = new Thread (new Runnable() {
            public void run() {
                agent.subscribeToGame(getCurrentGameTopic());
            }
        });
        t.setName("Main Agent Thread");
        t.start(); 
        return true;
        	
    }

    /*
     * Invoked when the AgentClient is ready to be bootstrapped
     * TODO:
     * Sanity checks on the information needed to succesfully bootstrap an Agent Client
     * before we attempt to do so. Currently we are trusting the user to pass the right things
     * (thank you unix).
     * Ricardo De Lima (ricardo@eecs.harvard.edu) 
     */
    
	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#run()
	 */
	public final void run() {
	
		
		/*
		 *  Creating the First JMS Elements before connecting to the server
		 */
		
		String url = "tcp://" + getServerHostname() + ":" + Integer.toString(getServerPort());
		log.info("Connecting to Colored Trails Server..." + url);
		
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);

		try {
			connection = factory.createConnection();
			connection.setExceptionListener(agent);
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			topic =  session.createTopic(ColoredTrailsServer.SERVER_NAME);
			
			/*
			 * This selector to the consumer specifies that this client is to listen
			 * for messages directed to ALL CLIENTS
			 * and also for messages directed directly to it
			 */
			
			consumer = session.createConsumer(topic,"(type = 'client' AND client_name = '" + getPin() + "') OR (type = 'client' AND client_name IS NULL)");
			consumer.setMessageListener(this);
			connection.start();
		
		}  catch (JMSException e) {
			log.fatal("Creating Connection With the Server Failed:" + e);
			System.exit(-1);
		}    	


		
		
		
	     if (registerWithServer() == false) {
	         log.fatal("Registration with Server failed.");
	         System.exit(-1);
	     	} else {
	     		connected();
	     	}

	      
	    
	}
	
	
    /* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#registerWithServer()
	 */
	
	private boolean registerWithServer() {

		
		
		TextMessage subscribeMessage;
		
		String local_ip = "";
		
		try {
			local_ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			
			log.fatal("Client failed to get local IP address" + e1);
			System.exit(-1);
			
		}
		
		
		
		try {
			producer = session.createProducer(topic);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
	
			subscribeMessage = session.createTextMessage();
			subscribeMessage.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.SERVER_MSG);
			subscribeMessage.setStringProperty(ColoredTrailsServer.COMMAND, ColoredTrailsServer.SUBSCRIBE_CMD);
			subscribeMessage.setStringProperty(ColoredTrailsServer.CLIENT_NAME, getPin());
			subscribeMessage.setStringProperty(ColoredTrailsServer.CLIENT_IP, local_ip);
                        subscribeMessage.setStringProperty(ColoredTrailsServer.CLIENT_TYPE, agent.getClientClassType());
			
			producer.send(subscribeMessage);
		}  catch (JMSException e) {
				log.fatal("Error subscribing to the Server:" + e);
				System.exit(-1);
		}   

		
			log.info("Subscribing to Colored Trails Server...");
			return true;

	}



	public int getServerPort() {
		return serverPort;
	}



	public void setServerPort(int port) {
		serverPort = port;
		
	}



	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message msg) {
            try {
                if(msg instanceof TextMessage) {
                    if(msg.getStringProperty(ColoredTrailsServer.COMMAND).equals(ColoredTrailsServer.SUBSCRIBETOGAME_CMD)); {		
                        //We may want to not do this. Might have to move this inside of a check if game ended
                        subscribeToGame(msg);
                        //Check to see if thread is valid or not. If the game has ended, then we shut down the thread
                        //and wait for it to finish, then fire up a new game.
                        if(t != null){
                            if(t.isAlive()) {
                                System.out.println("CT Client thread still alive");
                            } else {
                                System.out.println("CT Client thread is tot");
                            }
                            if(agent.getEnded()){
                                System.out.println("Game is ended, so we are safe to start a new one");
                                //shut down thread and wait for it to finish execution
                                agent.releaseLock();
                                if(t.isAlive()){
                                    try{
                                        t.join();
                                    }catch(InterruptedException e){

                                    }
                                }
                                //start new game
                                System.out.println("trying to start new game");
                                t = new Thread (new Runnable() {
                                        public void run() {
                                            agent.subscribeToGame(getCurrentGameTopic());
                                        }
                                    });
                                t.setName("Main Agent Thread");   
                                t.start(); 


                            }
                        }
                    }
                }
            } catch (JMSException e) {
                    log.fatal("Error receiving message:" + e);
            } 
	}

	/*
	 * The server has sent us notification that we have been
	 * asked to join a game
	 * 
	 */


	private void subscribeToGame(Message subscribeMessage) {
		
            String game_topic = null;
            String pergameId = null;
            String gameId = null;
            try {
                game_topic = subscribeMessage.getStringProperty(ColoredTrailsServer.GAMETOPIC);
                pergameId = subscribeMessage.getStringProperty(ColoredTrailsServer.PERGAMEID);
                gameId = subscribeMessage.getStringProperty(ColoredTrailsServer.GAMEID);
                log.info("client has been asked to subscribe to a game: " + game_topic + " with perGameId: " + pergameId);
                agent.setPerGameID(Integer.parseInt(pergameId));
                agent.setGameId(gameId);
                setCurrentGameTopic(game_topic);
                setGameSubscription(true);

            } catch (JMSException e) {
                log.fatal("Error receiving message:" + e);
            }
        }
        
        public void sendsubscribedreply(){

            //this is where we send "client subscribed messages"
            try {
                Message subscribedMessage = session.createTextMessage();
                subscribedMessage.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.SERVER_MSG);
                subscribedMessage.setStringProperty(ColoredTrailsServer.COMMAND, ColoredTrailsServer.SUBSCRIBETOGAME_CMD_REPLY);
                subscribedMessage.setIntProperty(ColoredTrailsServer.GAMEID, Integer.parseInt(agent.getGameId()));
                subscribedMessage.setIntProperty(ColoredTrailsServer.PERGAMEID, agent.getPerGameID());

                producer.send(subscribedMessage);
                
            } catch (JMSException e) {
                log.fatal("Sending subscribed message:" + e);
            }
		
	}



	/**
	 * @return the gameSubsciption
	 */
	private boolean isGameSubscription() {
		return gameSubscription;
	}



	/**
	 * @param gameSubsciption the gameSubsciption to set
	 */
	private void setGameSubscription(boolean gameSubsciption) {
		this.gameSubscription = gameSubsciption;
	}



	/**
	 * @return the currentGame
	 */
	private String getCurrentGameTopic() {
		return currentGameTopic;
	}



	/**
	 * @param currentGame the currentGame to set
	 */
	private void setCurrentGameTopic(String currentGame) {
		this.currentGameTopic = currentGame;
	}
}
