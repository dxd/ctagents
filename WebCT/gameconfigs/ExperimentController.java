/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author sohan dsouza
 */

//package edu.harvard.eecs.airg.coloredtrails.controller;

import edu.harvard.eecs.airg.coloredtrails.controller.ControlImpl;
import edu.harvard.eecs.airg.coloredtrails.controller.Game;
import edu.harvard.eecs.airg.coloredtrails.controller.GameEndWatcher;
import edu.harvard.eecs.airg.coloredtrails.controller.events.GameStartEventListener;
import edu.harvard.eecs.airg.coloredtrails.shared.PlayerConnection;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;
import edu.harvard.eecs.airg.coloredtrails.shared.GameManagerUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Date;


public class ExperimentController {

    //Game setup vars
    private static Scoring s = new Scoring(100, -15, 10, 0.0);
    private static int ProposerID	=	0;
    private static int ResponderID	=	1;
    private static int NumRounds = 1;
    private static int numRows = 4;
    private static int numCols = 4;
    private static int numChips = 7;
    private static boolean[] initialGoalVisibility = {false,false};

    private static Random localRand = new Random();
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
    //experiment setup vars
        int minGamesPerMatch = 1;
        int maxGamesPerMatch = 1;
        int startPoolIndex = 0; //position in pool to start/continue
        boolean restrictCoordinates = false; //whether player and goal has to be in same row or col, so as to have only one shortest path
        boolean overflowPool = false; //whether to generate random games if the pool runs out

        String config = "WebCTChipRevelationConfig";
        String gamePoolFilename = "";//"gamepool.ser";

        ArrayList<ArrayList<GameStatus>> gamePool = null;
        if(args.length > 0 && !args[0].equals("-help")) {
            for(int i = 0; i < args.length; i++) {
                String arg = args[i];
                String param = (i > (args.length - 2) ? null : args[i+1]);
                if(arg.equals("-config")) {
                  config = param.toString(); i++;
                } else if(arg.equals("-file")) {
                  gamePoolFilename = param.toString(); i++;
                } else if(arg.equals("-max")) {
                  maxGamesPerMatch = Integer.parseInt(param); i++;
                } else if(arg.equals("-min")) {
                  minGamesPerMatch = Integer.parseInt(param); i++;
                } else if(arg.equals("-start")) {
                  startPoolIndex = Integer.parseInt(param); i++;
                } else if(arg.equals("-restrict")) {
                  restrictCoordinates = true;
                } else if(arg.equals("-overflow")) {
                  overflowPool = true;
                }
            }
            if(minGamesPerMatch > maxGamesPerMatch) {
                throw new Exception("minGamesPerMatch > maxGamesPerMatch");
            }
            System.out.println("Config to be used: [" + config + "]");
            System.out.println("Will run games from [" + gamePoolFilename + "] starting at index ["+startPoolIndex+"]");
            System.out.println("Between " + minGamesPerMatch + " and " + maxGamesPerMatch + " games per pair in each direction");
            System.out.println("Players and goals will"+(restrictCoordinates ? "" : " NOT")+" be forced to same row or column in random games");
            System.out.println("Games will "+(overflowPool ? "" : " NOT")+" be randomly generated if the pool runs out");
        } else {
            System.out.println("Parameter -config <name_of_config_file> (mandatory)");
            System.out.println("Parameter -file <location_of_file> (default: "+gamePoolFilename+")");
            System.out.println("Parameter -min <minimum_number_of_games_between_each_pair> (default: "+minGamesPerMatch+")");
            System.out.println("Parameter -max <maximim_number_of_games_between_each_pair> (default: "+maxGamesPerMatch+")");
            System.out.println("Parameter -start <index_in_pool_list_to_initialize_cursor> (default: "+startPoolIndex+")");
            System.out.println("Parameter -restrict (default: player and goal"+(restrictCoordinates ? "" : " NOT")+" forced to same column or row in random games)");
            System.out.println("Parameter -overflow (default: games will "+(overflowPool ? "" : " NOT")+" be randomly generated if the pool runs out)");
            return;
        }
        
        System.out.println("Experiment Controller starting for 2 players per game at " + new Date());

        //***********************************
        //Initialize controller, IP and port must be specified
        ControlImpl controlImpl = new ControlImpl("tcp://127.0.0.1:8088");
        
        //Get list of currently connected players
        List<PlayerConnection> players_list = controlImpl.getPlayersWait();
        
        //Now we can print out some of the player information, and then set up the
        //list of players to be used for the game
        int[] playerPins = new int[players_list.size()];
        int numPlayers = 0;
        for(PlayerConnection pc : players_list){
            System.out.println("Connected player: " + pc.toString() + " " + pc.getEClientState() + " " + pc.getClientClassType());
            playerPins[numPlayers++] = pc.getPin();
        }
        //***********************************/
        
        /************FOR TESTING************
        int numPlayers = 4;
        int[] playerPins = {10,20,30,40};
        //***********************************/
        
        System.out.println("Total Number of Players: " + numPlayers);
        
        int [][] games = new int[numPlayers][numPlayers];
        for(int iPlayer = 0; iPlayer < numPlayers; iPlayer++) {
            games[iPlayer][iPlayer] = Integer.MAX_VALUE;
        }
        
        int gamesInPoolCount = 0;
        int gamePoolCursor = -1;
        
        gamePool = GameManagerUtility.loadGamePool(gamePoolFilename);
        
        if(gamePool != null && gamePool.size() > 0) {
            gamesInPoolCount = gamePool.size();
            System.out.println("Found " + gamesInPoolCount + " games in pool file named [" + gamePoolFilename + "]");
            gamePoolCursor = startPoolIndex;
            System.out.println("Starting at " + startPoolIndex);
        } else {
            if(overflowPool) {
              System.err.println("No games in pool ["+gamePoolFilename+"]; will generate and run random games");
            } else {
              throw new Exception("Game pool ["+gamePoolFilename+"] "
              + ( gamePool == null ? "empty" : "unreadable")
              + "; use '-overflow' command parameter to enable random game generation");
            }
        }
        
        int numGameRuns = 0;
        int numGamePlays = 0;
        while(!allGamesPlayed(games, minGamesPerMatch) && (gamePoolCursor < gamesInPoolCount || overflowPool)) {
            List<Game> runningGames = new ArrayList<Game>();
            boolean[] playing = new boolean[numPlayers];
            System.out.println("==========\nGame Run No. " + (++numGameRuns));
            int offset = localRand.nextInt(numPlayers); //so that games are not always allotted in order
            //match up each player
            for(int xPlayer = 0; xPlayer < numPlayers && (gamePoolCursor < gamesInPoolCount || overflowPool); xPlayer++) {
                //obviously the player cannot already be playing
                int iPlayer = xPlayer + offset;
                if(iPlayer >= numPlayers) {
                  iPlayer -= numPlayers;
                }
                if(!playing[iPlayer]) {
                    //get the next match-up
                    int opponent = nextMatch(games, iPlayer, playing, true, maxGamesPerMatch);
                    if(opponent >= 0) {
                        //there is an opponent to play
                        int[] gamePlayerPins = {playerPins[iPlayer], playerPins[opponent]};
                        //register player and opponent as playing
                        playing[iPlayer] = true;
                        playing[opponent] = true;
                        //update gameplay tracker
                        games = addGamePlayed(games,iPlayer,opponent);
                        //get games from pool or from random game generator
                        ArrayList<GameStatus> gameToPlay = null;
                        if(gamePool == null || gamePoolCursor >= gamesInPoolCount) {
                            System.out.println(gamePlayerPins[0] + " vs " + gamePlayerPins[1] + ": generated random game");
                            gameToPlay = GameManagerUtility.makeGameList(restrictCoordinates);
                        } else {
                            System.out.println(gamePlayerPins[0] + " vs " + gamePlayerPins[1] + ": game pool index " + gamePoolCursor);
                            gameToPlay = gamePool.get(gamePoolCursor++);
                        }
                        numGamePlays++;
                        try {
                          Thread.sleep(5000);
                        } catch(Exception ex){
                          ex.printStackTrace();
                        }
                        //run games and add to watchlist
                        runningGames.add(0, new Game(controlImpl, gamePlayerPins, "gameconfigs", config, gameToPlay));
                        runningGames.get(0).Start();
                    }
                }
            }
            System.out.println("==========");
            if(runningGames.size() > 0) {
                //wait until watched games are done with before next run
                new GameEndWatcher(runningGames).waitForGamesToEnd();
            }
        }
        if(!overflowPool && gamePoolCursor == gamesInPoolCount) {
            System.out.println("Ran out of games ... had only "+ gamesInPoolCount +" to work with");
        }
        System.out.println("Experiment Complete at " + new Date());
        System.exit(0);
    }

    public static int[][] addGamePlayed(int[][] games, int player, int opponent) {
        if(player == opponent) {
              return games;
        }
        games[player][opponent]++;
        return games;
    }
    
    public static boolean allGamesPlayed(int[][] games, int minGames) {
        for(int iPlayer = 0; iPlayer < games.length; iPlayer++) {
            for(int jOpponent = 0; jOpponent < games[iPlayer].length; jOpponent++) {
                if(games[iPlayer][jOpponent] < minGames) {
                    return false;
                }
            }
        }
        return true;
    }

    /*
    * get index of next player to match with this player as determined by
    * the 2D count array games already played (games)
    * the array of players already engages in games (playing)
    * the (random) flag
    * and the maximum number of games allowable for playing per pair per direction             
    */
    public static int nextMatch(int[][] games, int player, boolean[] playing, boolean random, int maxGamesPerMatch) {
        int minGamesPlayersCount = 0;
        int[] minGamesPlayers = new int[games.length];
        int minGameHistory = Integer.MAX_VALUE;
        //Find opponents who are not already playing, with highest urgency and within max limit
        for(int jOpponent = 0; jOpponent < games[player].length; jOpponent++) {
            if(games[player][jOpponent] < maxGamesPerMatch
            && (playing == null ? true : !playing[jOpponent])
            && games[player][jOpponent] <= minGameHistory) {
                if(games[player][jOpponent] < minGameHistory) {
                    minGamesPlayers[0] = jOpponent;
                    minGamesPlayersCount = 1;
                } else {
                    minGamesPlayers[minGamesPlayersCount] = jOpponent;
                    minGamesPlayersCount++;
                }
                minGameHistory = games[player][jOpponent];
            }
        }
        if(minGameHistory == Integer.MAX_VALUE) {
            //no one applicable and left to play
            return -1;
        }
        if(random) {
            //random from queue
            return minGamesPlayers[localRand.nextInt(minGamesPlayersCount)];
        } else {
            //first in queue
            return minGamesPlayers[0];
        }
    }

}
