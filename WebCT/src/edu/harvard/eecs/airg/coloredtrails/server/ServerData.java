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

import edu.harvard.eecs.airg.coloredtrails.shared.PlayerConnection;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameConfig;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameConfigDetailsRunnable;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;

import java.io.Serializable;
import java.util.*;

import org.apache.log4j.Logger;

/**
	<b>Description</b>
 * The data contained in the server, including all currently running
 * game states, connected players, and other data.  This singleton
 * is available throughout the server.
 
	<p>
	
	<b>Notes</b> (sgf)
	Method addNewGame() is where GameStatusSender objects are registered
	as observers of ServerGameStatus objects.
	<p>
	Method sendDiscourseMessage() is a convenience function; the real
	work is done by GamesStatusSender.sendDiscourseMessage().  The method
	ServerData.sendDiscourseMessage() is called from 
	.server.ServerGameStatus.doDiscourse().
	<p>
	Method getDiscourseMessageTypes() returns a list of possible 
	discourse messages.  As new discourse messages are created, they
	need to be included in this method to augment the list.  This method
	is called by server.ServerGameStatus.doDiscourse() [anyone else?].
	<p>
	Method getNextUpdateMessage() is used to read off the message queue.
	The messages on these message queues are read by SinglePlayerMessageSenderThread
	instances, which are daemon threads.
 
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public class ServerData extends TimerTask {
	
	private static Logger log = Logger.getRootLogger();
	
	/** the singleton instance */
    private static ServerData INSTANCE = null;
	
    /** list of games running on this server */
    private List<ServerGameStatus> games =
            Collections.synchronizedList(
                    new ArrayList<ServerGameStatus>());
	/** list of game configuration classes that this server knows about */
    private List<GameConfig> configs =
            Collections.synchronizedList(new ArrayList<GameConfig>());

	/** list of players registered with this server */
    private List<PlayerConnection> players =
            Collections.synchronizedList(
                    new ArrayList<PlayerConnection>());

    private GamesStatusSender statusSender;
          

    private HashMap<String, String> nameToIpMap =
            new HashMap<String, String>();


    /** A mapping of game id to the client commands */
    private Map<Integer,ClientCommands> clientHandler;
    
    public CtrlCommands ctrlHandler = null;
    private HashMap<Integer, ClientState> clientStateMap;
    Timer clientStateTimer;
 

    // Private constructor suppresses
    // default public constructor
    private ServerData() {

    	clientHandler = new Hashtable<Integer,ClientCommands>();
    	statusSender = new GamesStatusSender();
        clientStateMap = new HashMap<Integer, ClientState>();

    	clientStateTimer = new Timer("Server heartbeat timer", true);
        clientStateTimer.scheduleAtFixedRate(this, 0, ColoredTrailsServer.hbInterval);
        //clientStateTimer.
    }

    //synchronized creator to defend against multi-threading issues
    //another if check here to avoid multiple instantiation
    private synchronized static void createInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServerData();
        }
    }

    public static ServerData getInstance() {
        if (INSTANCE == null) createInstance();
        return INSTANCE;
    }

    /**
     * Get a list of all games running or run on the server.
     *
     * @return A list of all games running or run on the server.
     */
    public List<ServerGameStatus> getGames() {
        return games;
    }

    /**
     * Get a list of all game configurations that the server knows about.
     *
     * @return A list of all game configurations which the server knows
     *         about.
     */
    public List<GameConfig> getConfigs() {
        return configs;
    }

    /**
     * Get a list of all players who have connected to the server
     * (connection data).
     *
     * @return A list of all players who have connected to the server.
     */
    public List<PlayerConnection> getPlayers() {
        return players;
    }
    
    public PlayerConnection getPlayerConnection(int pin){
        for(PlayerConnection pc : players)
            if(pc.getPin() == pin)
                return(pc);
        return(null);
    }

    /**
     * Add the given GameConfig object to the list of configurations
     * which the server knows about.
     *
     * @param c The game configuration to be added.
     */
    public void addConfiguration(GameConfig c) {
        configs.add(c);
    }

    public ClientState getClientState(int pin){
        return clientStateMap.get(pin);
    }


    /**
     * Add the given player connection object to the list of players
     * which the server knows about.
     *
     * @param p The player connection object to be added.
     */
    public void addPlayer(PlayerConnection p) {
        players.add(p);
        System.err.println("Adding client state for " + p.getPin());
        
        ClientState cs = new ClientState(ColoredTrailsServer.getInstance().getCtrlCommands(), p.getPin());
        clientStateMap.put(p.getPin(), cs);
        p.setEClientState(cs.getEClientState());
    }
    
    public void removePlayer(int pin) {
        for(PlayerConnection pc : players)
            if(pc.getPin() == pin)
                players.remove(pc);
    }

    /**
     * Get the IP of a player given that player's PIN.
     *
     * @param pin The player's pin whose host ip needs to be looked up.
     * @return The host ip of the player looked up by pin.
     */
    public String getHostByPin(int pin) {
        for (PlayerConnection p : players) {
            if (p.getPin() == pin) {
                return p.getHostip();
            }
        }
        return "NO_SUCH_PIN";
    }
    
    /**
     * Check whether a configuration associated with a given config name
     * exists.
     *
     * @param configName The configuration name.
     * @return Whether the configuration exists.
     */
    public boolean configExists(String configName) {
        List<GameConfig> configs =
                new ArrayList<GameConfig>(getConfigs());
        for (GameConfig g : configs) {
            if (g.getConfigDetailsClassName().equals(configName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the configuration associated with the given config name.
     *
     * @param configName The config name to be looked up.
     * @return The GameConfig object associated with the config name.
     */
    public GameConfig getConfigByName(String configName) {
        List<GameConfig> configs =
                new ArrayList<GameConfig>(getConfigs());
        for (GameConfig g : configs) {
            if (g.getConfigDetailsClassName().equals(configName)) {
                return g;
            }
        }
        return null;
    }

    /**
     * Check that a player is connected and that they have not been
     * assigned to a game yet.  Used before creating a game with the
     * given player participating.
     *
     * @param playerId The pin of the player to be checked.
     * @return Whether the player is connected and unassigned.
     */
    public boolean playerAvailableAndUnassigned(int playerId) {
      
    	// TODO: veto a player's subscription if he is already subscribed
    	
        return true;
    }

    /**
     * Add a new game and start up the proper threads and configuration
     * thread.
     *
     * @param gameConfig The game configuration to start as this game's
     *                   configuration.
     * @param players    The players to be included in the game.
     * @return The new ID of the game, or a negative number to denote
     *         failure.
     */
    public synchronized void addNewGame(String gameConfig, Vector<PlayerStatus> players, int magicnumber) {
         addNewGame(gameConfig, players, magicnumber, null);
    }
    
    public synchronized void addNewGame(String gameConfig, Vector<PlayerStatus> players, int magicnumber, Serializable data) {
    	
    	if (!configExists(gameConfig)) {
            //Handle this
            return;
        }
        
        log.debug("ServerData addNewGame -- config file"); 
        
        for (PlayerStatus player : players) {
        	// log.debug("ServerData addNewGame -- checking players: [" + player.getPin() + "] ");
            if (!playerAvailableAndUnassigned(player.getPin())) {
            	// log.debug("ServerData addNewGame -- player: [" + player.getPin() + "] ALREADY ASSIGNED");
                
                //handle this
                return;
            }
        }
        
        log.debug("ServerData addNewGame -- Starting ServerGameStatus");
        
        ServerGameStatus newgame = new ServerGameStatus();
        
        newgame.setConfigName(gameConfig);
 
        /*
         *  TODO: Better ways to do a role based system than a per-game-id integer system
         *  
         */
        
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setPerGameId(i);
            newgame.setClientSubscribed(i, false);
        }
        
        newgame.setPlayers(new HashSet<PlayerStatus>(players));
        newgame.setMagicNumber(magicnumber);
        newgame.setDataFromController(data);
        
        int gameid = 0; 
      
        synchronized (games) {
            games.add(newgame);
            gameid = games.size() - 1;
        }
        
        log.debug("ServerData addNewGame -- Starting a new game with id: " + gameid);
        

        /* TODO: gameIds are obtuse, one ought to be able to give a game a particular name from the 
         *       admin interface.
         */
        newgame.setGameId(gameid);
        newgame.addObserver(statusSender);  // GamesStatusSender observes ServerGameStatus
        
        /*
         *  Start the JMS Server for this Game.
         *  
         *  
         */
        
        final int fgameid = gameid;
        final Vector<PlayerStatus> fplayers = players;
       
        //TODO: cleanup ClientCommands constructor
        ClientCommands ch = new ClientCommands(fgameid, ColoredTrailsServer.GAMES_NAME + "." + Integer.toString(fgameid));
	    ch.start();
		clientHandler.put( gameid, ch );
		
        /*
         * And Now we must notify the Players involved to subscribe to this particular game/topic
         * 
         */
        
        ColoredTrailsServer ct = ColoredTrailsServer.getInstance();
        
        ct.sendClientSubscribeToGameMessage(gameid, players);
        
    }
    
    public void startgame(int gameId){
        /*
         * This will generate a GAME_STARTED event
         */
        ServerGameStatus newgame = getGameStatusById(gameId);
        String gameConfig = newgame.getConfigName();
        
        CtrlCommands controller = ColoredTrailsServer.getInstance().getCtrlCommands();
        if(controller != null)
            controller.sendNewGameReply(gameId);
        
        newgame.setStarted();
        
        try {
	        GameConfigDetailsRunnable gcdr = getConfigByName(gameConfig).getRunnable();
            gcdr.setCurrentServerGameStatus( newgame );
            //gcdr.setGameId(gameid);
	        Thread gameThread = new Thread(gcdr);
	        newgame.setConfigRunnable(gcdr);
                
	        gameThread.start();
        
        } catch(Exception e) {
        	log.fatal("ServerData addNewGame -- exception occurred: " +e);
            e.printStackTrace();
        }
        
//        CtrlCommands controller = ColoredTrailsServer.getInstance().getCtrlCommands();
//        if(controller != null)
//            controller.sendNewGameReply(gameId);
        
    }

	/**
	 * Add a name  to IP mapping, stating that the player with a particular
	 * name  is at a particular IP.
	 *
	 * @param name The name of the client player.
	 * @param ip  The ip of the player.
	 */
	public void addNameToIpMapping(String name, String ip) {
	    nameToIpMap.put(name, ip);
	}
	
	/**
	 * Get the IP of a player based on that player's pin.
	 *
	 * @param name The name of the player.
	 * @return The IP of the player.
	 */
	public String getIpByPlayerName(String name) {
	    return nameToIpMap.get(name);
	}
    
    /**
     * Get a game status based on its game id.
     *
     * @param gameid The game id of a sought after game.
     * @return The game status itself.
     */
    public ServerGameStatus getGameStatusById(int gameid) {
        for(ServerGameStatus sgs : games){
            if(sgs.getGameId() == gameid)
                return(sgs);
        }
        return(null);
        //return games.get(gameid);
    }

    /**
     * Send a particular discourse message to a player.
	 *	<p>
	 *	(sgf) looks like a convenience function; GamesStatusSender is
	 *	doing the work
     *
     * @param game The game status associated with the dicourse message.
     * @param dm   The discourse message.
     */
    public void sendDiscourseMessage(ServerGameStatus game, DiscourseMessage dm) {
    	int id = game.getGameId();
        clientHandler.get(id).sendGameDiscourseMessage(game, dm);
    }
    
    public ClientCommands getClientCommands( int gameId ) {
    	return clientHandler.get( gameId );
    }

    @Override
    public void run() {
//  
        for(ClientState cs : clientStateMap.values())
            cs.updateState();
    }
}
