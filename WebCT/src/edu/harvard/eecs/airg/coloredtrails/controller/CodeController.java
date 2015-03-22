package edu.harvard.eecs.airg.coloredtrails.controller;

import edu.harvard.eecs.airg.coloredtrails.controller.events.GameStartEventListener;
import edu.harvard.eecs.airg.coloredtrails.shared.PlayerConnection;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;
import java.util.List;

/**
 * @author Rani, legodude
 */
public class CodeController extends Thread implements GameStartEventListener {

    public CodeController(String config) {
    }

    @Override
    public void run() {
        System.out.println("Code Controller starting");
        
        //Initialize controller, IP and port must be specified
        ControlImpl controlImpl = new ControlImpl("tcp://127.0.0.1:8081");
        
        //Get list of currently connected players
        List<PlayerConnection> players_list = controlImpl.getPlayersWait();
        
        //we only need two players for our game
        players_list = players_list.subList(0, 2);
        
        //Now we can print out some of the player information, and then set up the
        //list of players to be used for the game
        String players = "";
        for(PlayerConnection pc : players_list){
            System.out.println("Connected player: " + pc.toString() + " " + pc.getEClientState() + " " + pc.getClientClassType());
            players = players.concat(pc.getPin() + " ");
        }
       
        //Now we start the game
        controlImpl.sendConfig("gameconfigs", "TheAutomatConfig");
        controlImpl.newGame("TheAutomatConfig", players);
        
        //There are a couple such listener types
        controlImpl.addGameStartEventListener(this);
    }

    public void gameStartedEventHandler(GameStatus game){
        System.out.println("Game has started id=" + game.getGameId());        
    }

}
