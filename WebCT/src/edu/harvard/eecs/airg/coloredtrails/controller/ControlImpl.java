package edu.harvard.eecs.airg.coloredtrails.controller;

import edu.harvard.eecs.airg.coloredtrails.controller.events.GameEndEventListener;
import edu.harvard.eecs.airg.coloredtrails.shared.Utility;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;
import edu.harvard.eecs.airg.coloredtrails.server.ColoredTrailsServer;
import edu.harvard.eecs.airg.coloredtrails.server.CtrlCommands;
import edu.harvard.eecs.airg.coloredtrails.controller.events.GameStartEventListener;

import edu.harvard.eecs.airg.coloredtrails.controller.events.GameUpdatedEventListener;
import edu.harvard.eecs.airg.coloredtrails.controller.events.NewGameEventListener;
import edu.harvard.eecs.airg.coloredtrails.server.ClientState;
import edu.harvard.eecs.airg.coloredtrails.shared.PlayerConnection;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import java.util.logging.Level;
import javax.jms.*;
import java.util.logging.Logger;
import java.util.Vector;
import java.net.*;
import java.io.File;
import java.io.IOException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * This class implements the controller interface
 * @author legodude
 */
public class ControlImpl implements Control, MessageListener, ExceptionListener {
    private Session session = null;
    private Topic topic = null;
    private Connection connection = null;

    private MessageProducer producer = null;
    private MessageConsumer consumer = null;


    protected Logger log = Logger.getAnonymousLogger();

    private Vector<GameStartEventListener> gameStartedListeners = new Vector<GameStartEventListener>();
    private Vector<NewGameEventListener> newGameListeners = new Vector<NewGameEventListener>();
    private Vector<GameEndEventListener> gameEndedListeners = new Vector<GameEndEventListener>();
    private Vector<GameUpdatedEventListener> gameUpdatedEventListeners = new Vector<GameUpdatedEventListener>();
    
    
    private ControlHandler CH = null;
    
    List<PlayerConnection> players = new ArrayList<PlayerConnection>();
    Boolean playersUpdated = false;
    Object playersLock = new Object();
    
    Boolean newPlayer = false;
    Object waitplayersLock = new Object();
    
    Boolean activeGamesReceived = false;
    Object getActiveGamesLock = new Object();
    List<GameStatus> activeGames = new ArrayList<GameStatus>();


    /**
     * Initializes controller
     * @param host Hostname to use, in the form "tcp://127.0.0.1:8081"
     */
    public ControlImpl(String host) {
        log.info("Connecting Controller to Colored Trails Server..." + host);

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(host);

        try {
            connection = factory.createConnection();
            connection.setExceptionListener(this);
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            topic = session.createTopic(ColoredTrailsServer.SERVER_NAME);

            /*
            * This selector to the consumer specifies that this client is to listen
            * for messages directed to controller client
            */

            consumer = session.createConsumer(topic, "(type = 'CONTROLLER_CLIENT')");
            consumer.setMessageListener(this);

            producer = session.createProducer(topic);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            connection.start();

        } catch (JMSException e) {
            log.severe("Creating Connection With the Server Failed:" + e);
            System.exit(-1);
        }
        log.info("Connected Controller to Colored Trails Server..." + host);
    }


    public void onException(JMSException arg) {
        log.severe("CONTROLLER: JMS Exception occured.  Shutting down." + arg);
        System.exit(-1);
    }

/**
 * Handles messages from the server
 * @param msg message from server
 */
    public void onMessage(Message msg) {
        if(msg != null){
            
            //Might want to change this function structure to call a sepearate function for each of the messsage type
            //just so it is cleaner to read and there is not processing code within it.
            
            //decision: only code here should be enough to call function which properly handles action
            
            //System.out.println("Controller received message: " + msg );
            try {

                String msgName = msg.getStringProperty(CtrlCommands.MSG_NAME);
                
                if(msgName != null){
                    //System.out.println("received message: " + msgName);
                    if(msgName.equals(ColoredTrailsServer.GAMESTART )){
                        
//                        System.out.println("received a game start message");

                        for(GameStartEventListener gl : gameStartedListeners){
                            gl.gameStartedEventHandler((GameStatus) ((ObjectMessage)msg).getObject());
                        }
                        
                    } else if(msgName.equals(CtrlCommands.LIST_PLAYERS_REPLY)){
                        synchronized(playersLock){
                            players = (List<PlayerConnection>) ((ObjectMessage)msg).getObject();
                            playersUpdated = true;
                            playersLock.notifyAll();
                        }
                        //System.out.println("wow, that seemed to have worked");
                        //CH.PlayerList(players);
                    
                    } else if (msgName.equals(CtrlCommands.ADD_CONFIGURATION_REPLY)) {

                        if (msg.getBooleanProperty(CtrlCommands.REPLY)) {
                            log.info("CONTROLLER: successfully added configuration");
                        } else {
                            log.info("CONTROLLER: error adding configuration");
                        }
                        
                    } else if (msgName.equals(CtrlCommands.NEW_GAME_REPLY)) {
                        
                        int gameId = msg.getIntProperty(CtrlCommands.NEW_GAME);
                        int magic = msg.getIntProperty(CtrlCommands.MAGIC);
                        GameStatus game = (GameStatus) ((ObjectMessage)msg).getObject();
                        log.info("Added new game:" + gameId);
                        
                        for(NewGameEventListener gl : newGameListeners){
                            gl.newGameEventHandler(game, magic);
                        }
                        
                    } else if(msgName.equals(ColoredTrailsServer.GAMEEND)){
                        
                        int gameId = msg.getIntProperty(CtrlCommands.GAME_ID);
                        //CH.GameEnded(gameId);
                        GameStatus game = (GameStatus) ((ObjectMessage)msg).getObject();
                        
                        for(GameEndEventListener gl : gameEndedListeners)
                            gl.gameEndedEventHandler(game);
                        
                    } else if(msgName.equals(ColoredTrailsServer.PHASEADVANCED)){
                        
                        int gameId = msg.getIntProperty(CtrlCommands.GAME_ID);
                        Phases phases = ((Phases) ((ObjectMessage)msg).getObject());
                        //CH.PhaseAdvanced(gameId, phases.getCurrentPhaseName());
                        
                    } else if(msgName.equals(CtrlCommands.LIST_GAMES_REPLY)){
                        
                        List<GameStatus> games = (List<GameStatus>) ((ObjectMessage)msg).getObject();
                        System.out.println("there are this many games: " + games.size());
                        //CH.gameList(games);
                    
                    } else if(msgName.equals(CtrlCommands.GET_ACTIVE_GAMES_REPLY)){
                        
                        activeGames = (List<GameStatus>) ((ObjectMessage)msg).getObject();
                        synchronized(getActiveGamesLock){
                            activeGamesReceived = true;
                            getActiveGamesLock.notifyAll();
                        }
                        //System.out.println("there are this many games: " + games.size());
                        //CH.gameList(games);
                    
                    } else if(msgName.equals(CtrlCommands.CLIENT_DISCONNECT)){
                        
                        int pin = msg.getIntProperty(ColoredTrailsServer.CLIENT_PIN);
                        System.out.println("Client: " + pin + " disconnected");
                    
                    } else if(msgName.equals(CtrlCommands.CLIENT_CONNECT)){
                        
                        int pin = msg.getIntProperty(ColoredTrailsServer.CLIENT_PIN);
                        System.out.println("Client: " + pin + " connected");
                        synchronized(waitplayersLock){
                            newPlayer = true;
                            waitplayersLock.notifyAll();
                        }
                        
                    } else {
                        System.out.println("Unknown message");
                    }  
                }
                
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        }
    }


    /**
     * Sends server config file to server, must be called before newGame
     * @param configdir path to config file
     * @param className name of class to use
     */
    public void sendConfig(String configdir, String className) {
        byte[] myclass;

        URI debug_uri = null;

        try {
            URL urls[] = {new File(configdir).toURI().toURL()};
            URLClassLoader u = URLClassLoader.newInstance(urls);

            URL path = u.getResource(className + ".class");

            debug_uri = path.toURI();
            myclass = Utility.getBytesFromFile(new File(path.toURI()));
            sendConfig(myclass, className);

        } catch (MalformedURLException e) {
            log.severe("Configuration directory URL invalid " + e.getMessage());
            return;
        } catch (URISyntaxException e) {
            log.severe("Configuration directory URL invalid " + e.getMessage());
            return;
        } catch (IOException e) {
            log.severe("Configuration directory URL invalid " + e.getMessage());
            return;
        } catch (IllegalArgumentException e) {
            log.severe("Configuration directory URL invalid. URI was: " + debug_uri);
            log.severe(e.getMessage());
            return;
        }
        catch (NullPointerException e) {
            log.severe("cannot find configuration class. Make sure it's compiled. " + e.getMessage());
            return;
        }
    }

    /**
     * Private function to actually send class to server
     * @param myclass class bytestream
     * @param className name of class
     */
    private void sendConfig(byte[] myclass, String className) {
        try {
            
            BytesMessage msg = session.createBytesMessage();
            msg.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.CONTROLLER_MSG);
            msg.writeBytes(myclass);
            msg.setStringProperty(ColoredTrailsServer.COMMAND, CtrlCommands.ADD_CONFIGURATION);
            msg.setStringProperty(CtrlCommands.CONFIGCLASS_NAME, className);
            producer.send(msg);
            System.out.println("Sent message to load: " + className);

        } catch (JMSException e) {
            log.severe("Controller:Error sending config class to the Server:" + e);
            System.exit(-1);
        }
    }

    /**
     * Starts new game, overloaded compaitibility interface
     * @param ConfigClassName
     * @param players list of pins separated by spaces "10 20 30"
     */
    public void newGame(String ConfigClassName, String players) {
        newGame(ConfigClassName, players, -1, null);
    }

    public void newGame(String ConfigClassName, String players, Serializable data) {
        newGame(ConfigClassName, players, -1, data);
    }
    
    //magic number is a unique identifier that is returned by the server with the new/start game message
    //this enables the controller to associate newGame calls with the game that is created (and its gameId)
    /**
     * Starts a new game on the server with specified players. 
     * @param ConfigClassName name of config class as sent by sendConfig
     * @param players list of pins separated by spaces "10 20 30"
     * @param magicnumber controller-generated unique identifier, returned by the server when the game is created
     * This allows the controller to match gameID with calls to newGame
     * @param data Arbitrary data to send to the config class
     */
    public void newGame(String ConfigClassName, String players, int magicnumber, Serializable data) {
        try {
            ObjectMessage msg = session.createObjectMessage();
            msg.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.CONTROLLER_MSG);
            msg.setStringProperty(ColoredTrailsServer.COMMAND, CtrlCommands.NEW_GAME);
            msg.setStringProperty(CtrlCommands.CONFIGCLASS_NAME, ConfigClassName);
            msg.setStringProperty(CtrlCommands.PLAYERS, players);
            msg.setIntProperty(CtrlCommands.MAGIC, magicnumber);

            if(data != null){
                msg.setObject(data);
                msg.setBooleanProperty(CtrlCommands.DATAFLAG, true);  
            } else {
                msg.setBooleanProperty(CtrlCommands.DATAFLAG, false);  
            }
            producer.send(msg);
            log.info("Controller: Sending \"new game\" command");
        } catch (JMSException e) {
            log.severe("Controller:Error sending \"new game\" command to the Server:" + e);
            System.exit(-1);
        }
    }

    /**
     * Adds game start event listener
     * @param l listener to add
     */
    public synchronized void addGameStartEventListener(GameStartEventListener l) {
		gameStartedListeners.addElement(l);
	}
    
    /**
     * Adds game end event listener
     * @param l listener to add
     */
    public synchronized void addGameEndEventListener(GameEndEventListener l) {
		gameEndedListeners.addElement(l);
	}
    
    /**
     * Adds game start event listener
     * @param l listener to add
     */
    public synchronized void addNewGameEventListener(NewGameEventListener l) {
		newGameListeners.addElement(l);
    }
    
    /**
     * Adds game updated event listener
     * @param l listener to add
     */
    public synchronized void addGameUpdatedEventListener(GameUpdatedEventListener l) {
        gameUpdatedEventListeners.addElement(l);
    }
    
    /**
     * Server will send message containing list of PlayerConnections
     */
    public void listPlayers()
    {
        try {
            TextMessage txtMsg = session.createTextMessage();
            txtMsg.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.CONTROLLER_MSG);
            txtMsg.setStringProperty(ColoredTrailsServer.COMMAND, CtrlCommands.LIST_PLAYERS);
            
            producer.send(txtMsg);
            
//            log.info("Controller: Sending \"list players\" command");
        } catch (JMSException e) {
            log.severe("Controller:Error sending \"list players\" command to the Server:" + e);
            System.exit(-1);
        }
    }
    
    /**
     * Server will send message containing list of GameStatus objects
     */
    public void listGames()
    {
        try {
            TextMessage txtMsg = session.createTextMessage();
            txtMsg.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.CONTROLLER_MSG);
            txtMsg.setStringProperty(ColoredTrailsServer.COMMAND, CtrlCommands.LIST_GAMES);
            
            producer.send(txtMsg);
            
            log.info("Controller: Sending \"list games\" command");
        } catch (JMSException e) {
            log.severe("Controller:Error sending \"list games\" command to the Server:" + e);
            System.exit(-1);
        }
    }
    
    /**
     * Gets all active games (slightly less bandwidth than listgames)
     * @return
     */
    public List<GameStatus> getActiveGames(){
        
        activeGamesReceived = false;
        
        try {
            TextMessage txtMsg = session.createTextMessage();
            txtMsg.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.CONTROLLER_MSG);
            txtMsg.setStringProperty(ColoredTrailsServer.COMMAND, CtrlCommands.GET_ACTIVE_GAMES);
            
            producer.send(txtMsg);
            
//            log.info("Controller: Sending \"get active games\" command");
        } catch (JMSException e) {
            log.severe("Controller:Error sending \"get active games\" command to the Server:" + e);
            System.exit(-1);
        }
        
        synchronized(getActiveGamesLock){
            while(!activeGamesReceived){
                try {
                    getActiveGamesLock.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ControlImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        for(GameStatus g : activeGames){
            for(GameUpdatedEventListener l : gameUpdatedEventListeners)
                l.gameUpdatedEventHandler(g);
        }
//        log.info("done with active games");
        return(activeGames);
    }
    /**
     * Halts game
     * @param gameId ID of game to halt
     */
    public void haltGame(int gameId)
    {
        try {
            TextMessage txtMsg = session.createTextMessage();
            txtMsg.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.CONTROLLER_MSG);
            txtMsg.setStringProperty(ColoredTrailsServer.COMMAND, CtrlCommands.HALT_GAME);
            txtMsg.setIntProperty(CtrlCommands.GAME_ID, gameId);
            
            producer.send(txtMsg);
            
            log.info("Controller: Sending \"halt game\" command");
        } catch (JMSException e) {
            log.severe("Controller:Error sending \"halt game\" command to the Server:" + e);
            System.exit(-1);
        }
    }
    
    /**
     * Sends request to server config file to halt game, implementation is 
     * config file-dependent
     * @param gameId ID of game to request end
     */
    public void requestGameEnd(int gameId){
        try {
            TextMessage txtMsg = session.createTextMessage();
            txtMsg.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.CONTROLLER_MSG);
            txtMsg.setStringProperty(ColoredTrailsServer.COMMAND, CtrlCommands.REQUEST_HALT);
            txtMsg.setIntProperty(CtrlCommands.GAME_ID, gameId);
            
            producer.send(txtMsg);
            
            log.info("Controller: Sending \"request halt game\" command");
        } catch (JMSException e) {
            log.severe("Controller:Error sending \"request halt game\" command to the Server:" + e);
            System.exit(-1);
        }
    }
    
    /**
     * This will probably be removed
     * @param ch
     */
    public void setControlHandler(ControlHandler ch){
        this.CH = ch;
    }

    /**
     * Gets list of players connected to the server. Blocks until server has replied with list
     * @return list of PlayerConnection objects
     */
    public List<PlayerConnection> getPlayersWait() {
        playersUpdated = false;
        listPlayers();
        synchronized(playersLock){
            while(!playersUpdated){
                try {
                    playersLock.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ControlImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return(players);
    }
    
    
    
    
    public void waitForPlayers(ArrayList<Integer> pins){
        int[] tpins = new int[pins.size()];
        for(int i = 0; i < pins.size(); i++)
            tpins[i] = pins.get(i).intValue();
        waitForPlayers(tpins);
    }
    
    /**
     * Blocks until specified players have connected to the server.
     * This is useful if the controller spawns computer agent players, and wants to wait 
     * until they have connected to try and start games
     * @param pins list of pins to wait for
     */
    public void waitForPlayers(int[] pins){
        Boolean allthere = true;
        HashMap<Integer, Boolean> connectedstate = new HashMap();
        
        long time = System.currentTimeMillis();
        
        
        if(pins == null || pins.length == 0)
            return;
        
        for(int i = 0; i < pins.length; i++)
            connectedstate.put(pins[i], false);
        List<PlayerConnection> pcs;
        
        System.out.println("Waiting for these players");
        for(Integer i : connectedstate.keySet())
            System.out.print(i + "  ");
        System.out.println("");
        
        do{
            pcs = getPlayersWait();
            for(PlayerConnection pc : pcs){
                if(pc.getEClientState() == ClientState.EClientState.ACTIVE){
                    if(connectedstate.get(pc.getPin()) != null)
                        connectedstate.put(pc.getPin(), true);
                }
            }

            allthere = true;
            for(Boolean there : connectedstate.values())
                if(!there)
                    allthere = false;
            if(allthere)
                return;
            
            //we're still waiting on someone
            newPlayer = false;
            synchronized(waitplayersLock){
                while(!newPlayer){
                    try {
                        waitplayersLock.wait(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ControlImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if((System.currentTimeMillis() - 3000) > time){
                        time = System.currentTimeMillis();
                        newPlayer = true;
                    }
                }
            }
            
            
        }while(true);
        
    }
}

