package edu.harvard.eecs.airg.coloredtrails.server;

import java.io.Serializable;
import java.util.logging.Level;
import org.apache.log4j.Logger;

import javax.jms.*;
import java.util.Vector;

import edu.harvard.eecs.airg.coloredtrails.shared.types.GameConfig;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.CTClassLoader;
import edu.harvard.eecs.airg.coloredtrails.shared.PlayerConnection;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import java.util.ArrayList;
import java.util.List;
import javax.jms.ObjectMessage;

/**
 * Created by IntelliJ IDEA.
 * User: rani
 * Date: Feb 6, 2008
 * Time: 4:09:40 PM
 */
public class CtrlCommands {

    public static final String LIST_GAMES = "LIST_GAMES";
    public static final String LIST_GAMES_REPLY = "LIST_GAMES_REPLY";
    
    public static final String LIST_CONFIGURATIONS = "LIST_CONFIGURATIONS";
    public static final String LIST_PLAYERS = "LIST_PLAYERS";
    public static final String LIST_PLAYERS_REPLY = "LIST_PLAYERS_REPLY";
    public static final String PLAYERS = "PLAYERS";
    public static final String GET_LOG = "GET_LOG";
    public static final String NEW_GAME = "NEW_GAME";
    public static final String GET_ACTIVE_GAMES = "GET_ACTIVE_GAMES";
    public static final String GET_ACTIVE_GAMES_REPLY = "GET_ACTIVE_GAMES_REPLY";
    public static final String ADD_CONFIGURATION = "ADD_CONFIGURATION";
    public static final String NEW_GAME_REPLY = "NEW_GAME_REPLY";
    public static final String ADD_CONFIGURATION_REPLY = "ADD_CONFIGURATION_REPLY";
    
    
    public static final String DATAFLAG = "DATAFLAG";
    
    public static final String HALT_GAME = "HALT_GAME";
    public static final String REQUEST_HALT = "REQUEST_HALT";
    
    public static final String GAME_ID = "GAME_ID";

    public static final String MSG_NAME = "MSG_NAME";
    public static final String REPLY = "REPLY";
    public static final String MSG_TYPE = "MSG_TYPE";

    public static final String CONTROLLER_CLIENT = "CONTROLLER_CLIENT";
    public static final String CONFIGCLASS_NAME = "CONFIGCLASS_NAME";
    public static final String CONFIGCLASS = "CONFIGCLASS";
    public static final String MAGIC = "MAGIC";
    
    public static final String CLIENT_DISCONNECT = "CLIENT_DISCONNECT";
    public static final String CLIENT_CONNECT = "CLIENT_CONNECT";

    private Session session = null;
    private MessageProducer producer = null;

    private byte[] configClass = null;

    private static Logger log = Logger.getRootLogger();

    public CtrlCommands(Session session, MessageProducer producer) {
        this.session = session;
        this.producer = producer;
    }

    public void setConfigClass(BytesMessage msg) {
        try {
            log.info("Received message from controller: SET_CONFIGURATION");
            configClass = new byte[(int) msg.getBodyLength()];
            msg.readBytes(this.configClass);
        } catch (JMSException e) {
            log.fatal("Error receiving message:" + e);
        }
    }

    
    public void handleMessage(Message msg) {
        //Might want to change this function structure to call a sepearate function for each of the messsage type
        //just so it is cleaner to read and there is not processing code within it.
            
        Message reply = null;
        try {
            String msgName = msg.getStringProperty(ColoredTrailsServer.COMMAND);
            log.info("Received message from controller:" + msgName);
            
            if (msgName.equals(ADD_CONFIGURATION)) {
                
                setConfigClass((BytesMessage) msg);
                reply = addConfiguration(msg.getStringProperty(CONFIGCLASS_NAME));
                
            } else if (msgName.equals(NEW_GAME)) {
                
                String[] playerStrings = msg.getStringProperty(PLAYERS).split("\\s");
                int[] players = new int[playerStrings.length];
                for (int i = 0; i < playerStrings.length; i++) {
                    players[i] = Integer.parseInt(playerStrings[i]);
                }
                int magic = msg.getIntProperty(MAGIC);
                
                Serializable data = null;
                if(msg.getBooleanProperty(DATAFLAG)){
                    data = ((ObjectMessage) msg).getObject();
                    System.out.println("We have data from the controller");
                }
                
                reply = newGame(msg.getStringProperty(CONFIGCLASS_NAME), players, magic, data);
            
            } else if (msgName.equals(LIST_PLAYERS)){
                reply = listPlayers();
            } else if(msgName.equals(LIST_GAMES)) {
                reply = listGames();
            } else if(msgName.equals(GET_ACTIVE_GAMES)) {
                reply = getActiveGames();
            } else if(msgName.equals(HALT_GAME)) {
                int gameId = msg.getIntProperty(GAME_ID);
                reply = haltGame(gameId);
            } else if(msgName.equals(REQUEST_HALT)) {
                
                int gameId = msg.getIntProperty(GAME_ID);
                reply = requestHalt(gameId);
            }
        } catch (JMSException e) {
            log.fatal("Error receiving message from controller:" + e);
        }
        if (reply == null){
            log.debug("reply is null");
            return;
        }

        try {

            reply.setStringProperty(ColoredTrailsServer.MSGTYPE, CONTROLLER_CLIENT);
            producer.send(reply);
            log.info("Sending reply to controller: " + reply.getStringProperty(MSG_NAME));
        } catch (JMSException e) {
            log.fatal("Error sending reply to controller:" + e);
        }

    }


    /**
     * Add a configuration (described by a class) to the server.
     *
     * @param name The name of the configuration being uploaded.
     * @return Whether the addition of the configuration was successful.
     */
    public Message addConfiguration(String name) {
        if (configClass == null) {
            log.fatal("Error adding configuration:");
            return null;
        }
        System.out.println("Trying to add configureation: " + name);
        ServerData data = ServerData.getInstance();
        CTClassLoader ct = new CTClassLoader();
        ct.defClass(name, configClass);
        boolean success = false;
        TextMessage reply = null;

        try {
            Class c = ct.loadClass(name);
            data.addConfiguration(new GameConfig(name, c, configClass));

            // reset configClass for next time
            configClass = null;
            log.debug("Controller: adding configuration file [" + name + "] class name: " + c.getName());
            success = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            reply = session.createTextMessage();
            reply.setStringProperty(MSG_NAME, ADD_CONFIGURATION_REPLY);
            reply.setBooleanProperty(REPLY, success);

        } catch (JMSException e) {
            log.fatal("Error adding configuration:" + e);
        }
        return reply;
    }

    /**
     * Create a new game with the designated players, configuration name,
     * and start the game.
     *
     * @param configName The name of the configuration details class
     *                   which will define this game.
     * @param playerids  A list of player ids for this game.
     * @return The id of the created game.
     */
    public Message newGame(String configName, int[] playerids, int magicnumber, Serializable data) {

        log.debug("Controller: newGame configName = [" + configName + "] ");
        ObjectMessage reply = null;
        Vector<PlayerStatus> v = new Vector<PlayerStatus>();
        for (int playerid : playerids) {
            log.debug("Controller: newGame adding Player: [" + playerid + "]");
            v.add(new PlayerStatus(playerid));
        }
        
        
        ServerData.getInstance().addNewGame(configName, v, magicnumber, data);
        
        return null;
    }
    
    
    public void sendNewGameReply(int newGame)
    {
        try {
            ObjectMessage reply = session.createObjectMessage();
            reply.setObject(new GameStatus(ServerData.getInstance().getGameStatusById(newGame)));
 //           reply.setObject(new GameStatus(ServerData.getInstance().getGameStatusById(newGame), true));
            reply.setStringProperty(MSG_NAME, NEW_GAME_REPLY);
            reply.setIntProperty(NEW_GAME, newGame);
            reply.setIntProperty(MAGIC, ServerData.getInstance().getGameStatusById(newGame).getMagicNumber());
            
            reply.setStringProperty(ColoredTrailsServer.MSGTYPE, CONTROLLER_CLIENT);
            producer.send(reply);
            log.info("Sending reply to controller: " + reply.getStringProperty(MSG_NAME));

        } catch (JMSException e) {
            log.fatal("Error adding configuration:" + e);
        }
    }

    
    
    public void sendGameStartMessage(GameStatus game) {
        try {
            log.debug("sendGameStart to controller");
            ObjectMessage gameStartMsg = session.createObjectMessage();
            gameStartMsg.setObject(new GameStatus(game));
//            gameStartMsg.setObject(new GameStatus(game, true));
            log.debug("sending game id = " + game.getGameId());
            
            gameStartMsg.setStringProperty(ColoredTrailsServer.MSGTYPE, CONTROLLER_CLIENT);
            gameStartMsg.setStringProperty(MSG_NAME, ColoredTrailsServer.GAMESTART);
            gameStartMsg.setStringProperty(ColoredTrailsServer.OBJECTTYPE, ColoredTrailsServer.GAMESTART);

            producer.send(gameStartMsg);
        }
        catch (JMSException e) {
            log.fatal("Error sending game start message to controller:" + e);
        }

    }    
    
    
    private Message listPlayers(){
        try {
            Message reply = null;

            System.out.println("~~~~~~~~~~~~received command to list players");

            ServerData data = ServerData.getInstance();
            List<PlayerConnection> players = data.getPlayers();
            reply = session.createObjectMessage();

            ((ObjectMessage) reply).setObject((Serializable) players);
            reply.setStringProperty(MSG_NAME, LIST_PLAYERS_REPLY);

            return(reply);
        } catch (JMSException ex) {
            java.util.logging.Logger.getLogger(CtrlCommands.class.getName()).log(Level.SEVERE, null, ex);
            return(null);
        }
    }
    
    private Message listGames()
    {
        ServerData data = ServerData.getInstance();
        //ugh this is ugly
        List<ServerGameStatus> sgames = data.getGames();
        List<GameStatus> games = new ArrayList<GameStatus>();
        for(ServerGameStatus sd : sgames)
            games.add(new GameStatus((GameStatus)sd));
        
        Message reply;
        try {
            reply = session.createObjectMessage();
            ((ObjectMessage) reply).setObject((Serializable) games);
            reply.setStringProperty(MSG_NAME, LIST_GAMES_REPLY);
            
            return(reply);
        } catch (JMSException ex) {
            java.util.logging.Logger.getLogger(CtrlCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return(null);
    }

    private Message getActiveGames()
    {
        ServerData data = ServerData.getInstance();
        //ugh this is ugly
        List<ServerGameStatus> sgames = data.getGames();
        List<GameStatus> games = new ArrayList<GameStatus>();
        for(ServerGameStatus sd : sgames){
            if(sd.isStarted() && !sd.isEnded())
                games.add(new GameStatus((GameStatus)sd));
        }
        
        Message reply;
        try {
            reply = session.createObjectMessage();
            ((ObjectMessage) reply).setObject((Serializable) games);
            reply.setStringProperty(MSG_NAME, GET_ACTIVE_GAMES_REPLY);
            
            return(reply);
        } catch (JMSException ex) {
            java.util.logging.Logger.getLogger(CtrlCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return(null);
    }
    
    private Message haltGame(int gameId){
        System.out.println("trying to halt game: " + gameId);
        ServerData data = ServerData.getInstance();
        ServerGameStatus game = data.getGameStatusById(gameId);
        ((ServerPhases)game.getPhases()).setLoop(false);
        try {
            ((ServerPhases) game.getPhases()).finalize();
        } catch (Throwable ex) {
            java.util.logging.Logger.getLogger(CtrlCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
        game.setEnded();
        return(null);
    }

    private Message requestHalt(int gameId){
        System.out.println("Sending game halt request: " + gameId);
        ServerData.getInstance().getGameStatusById(gameId).getConfigRunnable().requestGameEnd();
        return(null);
    }

    public void sendGameEndedMessage(int gameId, GameStatus game){
        try {
            ObjectMessage gameEndedMsg = session.createObjectMessage();
            gameEndedMsg.setObject(new GameStatus(game));
//            gameEndedMsg.setObject(new GameStatus(game, true));
            log.debug("Game ended id = " + game.getGameId());

            gameEndedMsg.setStringProperty(ColoredTrailsServer.MSGTYPE, CONTROLLER_CLIENT);
            gameEndedMsg.setStringProperty(MSG_NAME, ColoredTrailsServer.GAMEEND);
            gameEndedMsg.setStringProperty(ColoredTrailsServer.OBJECTTYPE, ColoredTrailsServer.GAMEEND);
            gameEndedMsg.setIntProperty(GAME_ID, gameId);

            producer.send(gameEndedMsg);
        } catch (JMSException ex) {
            java.util.logging.Logger.getLogger(CtrlCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendPhaseAdvancedMessage(int gameId, Phases phases){
        try {
            ObjectMessage PhaseAdvancedMsg = session.createObjectMessage();
            PhaseAdvancedMsg.setObject(new Phases(phases));
            
            PhaseAdvancedMsg.setStringProperty(ColoredTrailsServer.MSGTYPE, CONTROLLER_CLIENT);
            PhaseAdvancedMsg.setStringProperty(MSG_NAME, ColoredTrailsServer.PHASEADVANCED);
            PhaseAdvancedMsg.setStringProperty(ColoredTrailsServer.OBJECTTYPE, ColoredTrailsServer.PHASEADVANCED);
            PhaseAdvancedMsg.setIntProperty(GAME_ID, gameId);

            producer.send(PhaseAdvancedMsg);
        } catch (JMSException ex) {
            java.util.logging.Logger.getLogger(CtrlCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void clientDisconnected(int pin){
        try {
            TextMessage clientdisconnectmsg = session.createTextMessage();
            clientdisconnectmsg.setStringProperty(ColoredTrailsServer.MSGTYPE, CONTROLLER_CLIENT);
            clientdisconnectmsg.setStringProperty(MSG_NAME, CLIENT_DISCONNECT);
            clientdisconnectmsg.setIntProperty(ColoredTrailsServer.CLIENT_PIN, pin);
            producer.send(clientdisconnectmsg);
        } catch (JMSException ex) {
            java.util.logging.Logger.getLogger(CtrlCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void clientConnected(int pin){
        try {
            TextMessage clientconnectmsg = session.createTextMessage();
            clientconnectmsg.setStringProperty(ColoredTrailsServer.MSGTYPE, CONTROLLER_CLIENT);
            clientconnectmsg.setStringProperty(MSG_NAME, CLIENT_CONNECT);
            clientconnectmsg.setIntProperty(ColoredTrailsServer.CLIENT_PIN, pin);
            producer.send(clientconnectmsg);
        } catch (JMSException ex) {
            java.util.logging.Logger.getLogger(CtrlCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}


