/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author greg
 */

//package edu.harvard.eecs.airg.coloredtrails.controller;

import edu.harvard.eecs.airg.coloredtrails.controller.ControlImpl;
import edu.harvard.eecs.airg.coloredtrails.controller.Game;
import edu.harvard.eecs.airg.coloredtrails.controller.GameEndWatcher;
import edu.harvard.eecs.airg.coloredtrails.controller.events.GameStartEventListener;
import edu.harvard.eecs.airg.coloredtrails.shared.PlayerConnection;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;
import java.util.ArrayList;
import java.util.List;


public class InterruptionControl
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        System.out.println("Code Controller starting");
        int numPlayers = 2;
        
        //Initialize controller, IP and port must be specified
        ControlImpl controlImpl = new ControlImpl("tcp://127.0.0.1:8081");
        
        //Get list of currently connected players
        List<PlayerConnection> players_list = controlImpl.getPlayersWait();
        
        //we only need two players for our game
        List<PlayerConnection> players_list1 = players_list.subList(0, numPlayers);
        List<PlayerConnection> players_list2 = players_list.subList(numPlayers, numPlayers*2);
        
        //Now we can print out some of the player information, and then set up the
        //list of players to be used for the game
        String players = "";
        int[] playerIDs1 = new int[numPlayers];
        int[] playerIDs2 = new int[numPlayers];
        int i = 0;
        for(PlayerConnection pc : players_list){
            System.out.println("Connected player: " + pc.toString() + " " + pc.getEClientState() + " " + pc.getClientClassType());
            players = players.concat(pc.getPin() + " ");
            if (i<numPlayers)
            	playerIDs1[i++] = pc.getPin();
            else
            	playerIDs2[i++ - 2] = pc.getPin();
        }
       
        // The final field in the Game constructor allows us to send arbitrary
        // data to the game config class. In this example, a simple string is
        // sent. It is retrieved in the game config class via a call to
        // gs.getDataFromController(). [see RobustGroupCliqueConfig.java, 548-9]
        Game game1 = new Game(controlImpl, playerIDs1, "gameconfigs", "InputFileConfig", "Special message for game 1");
        Game game2 = new Game(controlImpl, playerIDs2, "gameconfigs", "InputFileConfig", "Special message for game 2");
        
        // start the first game
        System.out.println("Trying to start game 1");
        game1.Start();
        
        // GameEndWatcher constructor requires a list of gamess
        List<Game> Game1 = new ArrayList<Game>();
        Game1.add(game1);
        
        
        // start the first game
        System.out.println("Trying to start game 2");
        game2.Start();
        
        // GameEndWatcher constructor requires a list of gamess
        List<Game> GamesList = new ArrayList<Game>();
        GamesList.add(game1);
        GamesList.add(game2);
        // wait for the first game to end and start the second game
        GameEndWatcher gew = new GameEndWatcher(GamesList);
        gew.waitForGamesToEnd();
//        System.out.println("Trying to start game 2");
//        game2.Start();
    }

}
