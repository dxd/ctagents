package edu.harvard.eecs.airg.coloredtrails.controller;

import edu.harvard.eecs.airg.coloredtrails.controller.events.GameEndEventListener;
import edu.harvard.eecs.airg.coloredtrails.controller.events.GameStartEventListener;
import edu.harvard.eecs.airg.coloredtrails.controller.events.GameUpdatedEventListener;
import edu.harvard.eecs.airg.coloredtrails.controller.events.NewGameEventListener;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Observable;
import java.util.Random;
import java.util.Set;

/**
 * Utility class to help launch and manage ColoredTrails games
 * It is often useful for the controller to send specific information for each game to the server config file. This is 
 * accomplished via the data parameter of the constructor. If it is non-null, the object will be available for the server
 * config file to use.
 * 
 * @author legodude
 */
public class Game extends Observable implements GameStartEventListener, NewGameEventListener, 
        GameEndEventListener, GameUpdatedEventListener
{
    public static final String GAMESTARTED = "GAMESTARTED";
    public static final String GAMEENDED = "GAMEENDED";
    
    private static final HashMap<Integer, Game> gameMap = new HashMap<Integer, Game>();
    
    private int magicnumber = -1;
    private boolean GameStarted = false;
    private boolean GameEnded = false;
    private ControlImpl Controller = null;
    private int[] players = null;
    private int GameID = -1;
    private String className;
    private String current_phase;
    private GameStatus game;
    private Serializable data;
    
    /**
     * In addition to initializing the object, the constructor also sends the config class
     * to the server
     * 
     * @param Controller The controller that should be used
     * @param gplayers Array of players for the game
     * @param configdir Path to config file
     * @param className Name of config file
     * @param data Arbitrary data to be sent to the server config file, set to null if not needed
     */
    public Game(ControlImpl Controller, int[] gplayers, String configdir, String className, Serializable data){
        
        System.out.println("Sending config class");
        this.Controller = Controller;
        Controller.sendConfig(configdir, className);
        
        players = new int[gplayers.length];
        System.arraycopy(gplayers, 0, players, 0, gplayers.length);
        
        
        this.className = className;
        magicnumber = (new Random()).nextInt(Integer.MAX_VALUE);
        
        this.data = data;
    }

    
     /**
      * Starts the game
      */
    public void Start()
    {
        String splayers = new String("");
        for(int j = 0; j < players.length; j++){
            //System.out.println("player: " + String.valueOf(players[j]));
            splayers = splayers.concat(String.valueOf(players[j]) + " ");
        }
        System.out.println("Starting new game with players: " + splayers);
        Controller.newGame(className, splayers, magicnumber, data);
        Controller.addGameStartEventListener(this);
        Controller.addNewGameEventListener(this);
        Controller.addGameEndEventListener(this);
        Controller.addGameUpdatedEventListener(this);
    }

    public void gameStartedEventHandler(GameStatus game) {
//        print("in gamestarted event handler");
        if(game.getGameId() == GameID){
            GameStarted = true;
            print("Game Started");
            
            setChanged();
            notifyObservers(GAMESTARTED);
        }
    }
    
    public void newGameEventHandler(GameStatus game, int magic) {
//        print("in new game event handler");
        if(magic == magicnumber){
            this.game = game;
            GameID = game.getGameId();
            gameMap.put(GameID, this);
            print("Game initialized");
        }
    }
    
    public void gameEndedEventHandler(GameStatus game) {
        if(game.getGameId() == GameID){
            GameEnded = true;
            print("Game ended");
            
            for(PlayerStatus ps : game.getPlayers() )
                print(" (Player " + ps.getPin() + ") " + ps.getRole());
            
            setChanged();
            notifyObservers(GAMEENDED);
        }
    }

    /**
     * Halt the game.
     */
    public void haltGame(){
        print("Halting game");
        Controller.haltGame(GameID);
    }
    
    /**
     * Sends a message to the server config file requesting that it ends the game as soon as possible.
     * Implementation is config file-dependent
     */
    public void requestGameEnd(){
        print("Requesting game end");
        Controller.requestGameEnd(GameID);
    }
    
    private void print(String s){
        System.out.println("(Game: " + GameID + ") " + s);
    }

    public String toString(){
        int perGameId = -1;
        String s = "(Game ";
        if(GameStarted)
            s = s.concat(GameID + ") ");
        else
            s = s.concat("<not started>) ");
        
        s = s.concat("Class: " + className);
        s = s.concat(", Players: ");
        
        if(GameStarted){
            Set<PlayerStatus> pps = game.getPlayers();
            for(int i = 0; i < players.length; i++){
                
                for(PlayerStatus ps : pps){
                    if(players[i] == ps.getPin())
                        perGameId = ps.getPerGameId();
                }
                
                s = s.concat(players[i] + "("+ perGameId + ") ");
            }
        } else {
            for(int i = 0; i < players.length; i++)
                s = s.concat(players[i] + " ");
        }
        s = s.concat("magic " + magicnumber);
        
        return(s);
    }
    
    /**
     * Return players PINs for game
     * @return players
     */
    public int[] getPlayerPINs(){
        return(players);
    }

    /**
     * Has the game ended?
     * @return true if game over
     */
    public Boolean gameEnded(){
        return(GameEnded);
    }
    
    /**
     * Has the game started?
     * @return
     */
    public Boolean gameStarted(){
        return(GameStarted);
    }
    
    /**
     * Get game based on id
     * @param gameId ID of game to fetch
     * @return da game
     */
    public static Game getGame(int gameId){
        return(gameMap.get(gameId));
    }
    
    /**
     * Returns GameId
     * @return
     */
    public int getGameId(){
        return(GameID);
    }
    
    /**
     * Returns the associated gamestatus, not necessarily up to date (in fact, probably not up to date)
     * @return
     */
    public GameStatus getGameStatus(){
        return(game);
    }

    public void gameUpdatedEventHandler(GameStatus game) {
        if(game.getGameId() == this.GameID )
                this.game = game;
    }
}