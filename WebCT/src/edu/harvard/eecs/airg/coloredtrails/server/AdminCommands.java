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

import edu.harvard.eecs.airg.coloredtrails.shared.CTClassLoader;
import edu.harvard.eecs.airg.coloredtrails.shared.PlayerConnection;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameConfig;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import javax.jms.*;

/**
 * A handler for admin commands which are submitted to the server.
 * Used by the admin client.
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public class AdminCommands {
    public static final String LIST_GAMES = "LIST_GAMES";
    public static final String LIST_CONFIGURATIONS = "LIST_CONFIGURATIONS";
    public static final String LIST_PLAYERS = "LIST_PLAYERS";
    public static final String GET_LOG = "GET_LOG";
    public static final String NEW_GAME = "NEW_GAME";
    public static final String ADD_CONFIGURATION = "ADD_CONFIGURATION";
    public static final String REPLY_NAME = "REPLY_NAME";
    public static final String REPLY = "REPLY";
    public static final String ADMIN_CLIENT = "ADMIN_CLIENT";
    public static final String CONFIGCLASS_NAME = "CONFIGCLASS_NAME";
    public static final String CONFIGCLASS = "CONFIGCLASS";

       

    public void setConfigClass(BytesMessage msg) {
        try {
            log.info("Received message from admin: SET_CONFIGURATION");
            configClass = new byte[(int) msg.getBodyLength()];
            msg.readBytes(this.configClass);
        } catch (JMSException e) {
            log.fatal("Error receiving message:" + e);
        }
    }

    private byte[] configClass = null;

    private static Logger log = Logger.getRootLogger();

    public Message handleMessage(Message msg, Session session) {
        Message reply = null;
        try {
            String msgName = msg.getStringProperty(ColoredTrailsServer.COMMAND);
            log.info("Received message from admin client:" + msgName);
            if (msgName.equals(LIST_GAMES)) {
                reply = listGames(session);
            } else if (msgName.equals(LIST_CONFIGURATIONS)) {
                reply = listConfigurations(session);
            } else if (msgName.equals(LIST_GAMES)) {
                reply = listGames(session);
            } else if (msgName.equals(LIST_PLAYERS)) {
                reply = listPlayers(session);
            } else if (msgName.equals(ADD_CONFIGURATION)) {
                
                reply = addConfiguration(msg.getStringProperty(CONFIGCLASS_NAME), session);
                
            } else if (msgName.equals(NEW_GAME)) {
                String[] playerStrings = msg.getStringProperty(LIST_PLAYERS).split("\\s");
                int[] players = new int[playerStrings.length];
                for (int i = 0; i < playerStrings.length; i++) {
                    players[i] = Integer.parseInt(playerStrings[i]);
                }
                reply = newGame(msg.getStringProperty(CONFIGCLASS_NAME), players, session);
            } else if (msgName.equals(GET_LOG)) {
                reply = getLog(msg.getIntProperty(GET_LOG), session); 
            }

        } catch (JMSException e) {
            log.fatal("Error receiving message:" + e);
        }


        return reply;
    }

    /**
     * List games is an admin client method which returns
     * all games which the server knows about, past and present.
     *
     * @return A message describing individual games.
     */
    public Message listGames(Session session) {
        ServerData data = ServerData.getInstance();
        List<ServerGameStatus> games = data.getGames();
        String gamesString = "";
        for (ServerGameStatus g : games) {
            gamesString += g.toString() + " ";
        }
        TextMessage reply = null;
        try {

            reply = session.createTextMessage();
            reply.setStringProperty(ColoredTrailsServer.MSGTYPE, ADMIN_CLIENT);
            reply.setStringProperty(REPLY_NAME, LIST_GAMES);
            reply.setStringProperty(LIST_GAMES, gamesString);
        } catch (JMSException e) {
            log.fatal("Error creating listGames message:" + e);
        }
        return reply;
    }

    /**
     * List configurations lists all game configurations that have been
     * uploaded to the server.
     *
     * @return A message describing configurations.
     */
    public Message listConfigurations(Session session) {
        ServerData data = ServerData.getInstance();
        List<GameConfig> configs = data.getConfigs();
        TextMessage reply = null;
        String configsString = "";
        for (GameConfig c : configs) {
            configsString += c.toString() + " ";
        }

        try {
            reply = session.createTextMessage();
            reply.setStringProperty(ColoredTrailsServer.MSGTYPE, ADMIN_CLIENT);
            reply.setStringProperty(REPLY_NAME, configsString);
        } catch (JMSException e) {
            log.fatal("Error creating listConfigurations message:" + e);
        }
        return reply;
    }

    /**
     * Lists all players currently or previously connected to the server.
     *
     * @return A message of hashes each describing a player.
     */
    public Message listPlayers(Session session) {
        ServerData data = ServerData.getInstance();
        List<PlayerConnection> players = data.getPlayers();
        TextMessage reply = null;
        String playersString = "";
        for (PlayerConnection p : players) {
            playersString += p.toString() + " ";
        }
        try {
            reply = session.createTextMessage();
            reply.setStringProperty(ColoredTrailsServer.MSGTYPE, ADMIN_CLIENT);
            reply.setStringProperty(REPLY_NAME, LIST_PLAYERS);
            reply.setStringProperty(LIST_PLAYERS, playersString);
        } catch (JMSException e) {
            log.fatal("Error creating listPlayers message:" + e);
        }
        return reply;
    }

    /**
     * Get the history log of a given game.
     *
     * @param gameid The gameid of the log request.
     * @return The log associated with the gameid.
     */
    public Message getLog(int gameid, Session session) {
        TextMessage reply = null;
        String gameLog = ServerData.getInstance().getGameStatusById(gameid).getHistoryLog().toString();
        try {
            reply = session.createTextMessage();
            reply.setStringProperty(ColoredTrailsServer.MSGTYPE, ADMIN_CLIENT);
            reply.setStringProperty(REPLY_NAME, GET_LOG);
            reply.setStringProperty(GET_LOG, gameLog);
        } catch (JMSException e) {
            log.fatal("Error creating listPlayers message:" + e);
        }
        return reply;
    }

    /**
     * Add a configuration (described by a class) to the server.
     *
     * @param name The name of the configuration being uploaded.
     * @return Whether the addition of the configuration was successful.
     */
    public Message addConfiguration(String name, Session session) {
        if (configClass == null) {
            log.fatal("Error adding configuration:");
            return null;
        }
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
            log.debug("Administrator: adding configuration file [" + name + "] class name: " + c.getName());
            success = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            reply = session.createTextMessage();
            reply.setStringProperty(ColoredTrailsServer.MSGTYPE, ADMIN_CLIENT);
            reply.setStringProperty(REPLY_NAME, ADD_CONFIGURATION);
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
    public Message newGame(String configName, int[] playerids, Session session) {

        log.debug("Administrator: newGame configName = [" + configName + "] ");
        TextMessage reply = null;
        Vector<PlayerStatus> v = new Vector<PlayerStatus>();
        for (int playerid : playerids) {
            log.debug("Administrator: newGame adding Player: [" + playerid + "]");
            v.add(new PlayerStatus(playerid));
        }
        int newGame = -1;
        ServerData.getInstance().addNewGame(configName, v, -1);
        try {
            reply = session.createTextMessage();
            reply.setStringProperty(ColoredTrailsServer.MSGTYPE, ADMIN_CLIENT);
            reply.setStringProperty(REPLY_NAME, NEW_GAME);
            reply.setIntProperty(NEW_GAME, newGame);
        } catch (JMSException e) {
            log.fatal("Error adding configuration:" + e);
        }
        return reply;
    }


}
