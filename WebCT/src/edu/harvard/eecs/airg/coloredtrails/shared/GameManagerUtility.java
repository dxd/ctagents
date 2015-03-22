/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author sohan
 */

package edu.harvard.eecs.airg.coloredtrails.shared;

import edu.harvard.eecs.airg.coloredtrails.controller.ControlImpl;
import edu.harvard.eecs.airg.coloredtrails.controller.Game;
import edu.harvard.eecs.airg.coloredtrails.controller.GameEndWatcher;
import edu.harvard.eecs.airg.coloredtrails.controller.events.GameStartEventListener;
import edu.harvard.eecs.airg.coloredtrails.shared.PlayerConnection;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Date;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;


public class GameManagerUtility {

    //Game setup vars
    private static Scoring s = new Scoring(100, -15, 10, 0.0);
    private static int ProposerID	=	0;
    private static int ResponderID	=	1;
    private static int NumRounds = 1;
    private static int numRows = 5;
    private static int numCols = 5;
    private static int numChips = 7;
    private static boolean[] initialGoalVisibility = {false,false};
    private static Random localRand = new Random();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        int gamePoolSize = 56;
        String gamePoolFilename = "gamepool.ser";
        boolean restrictCoordinates = false; //whether player and goal has to be in same row or col, so as to have only one shortest path
        
        if(args.length > 0 && !args[0].equals("-help")) {
          for(int i = 0; i < args.length; i++) {
            String arg = args[i];
            String param = (i > (args.length - 2) ? null : args[i+1]);
            if(arg.equals("-file")) {
              gamePoolFilename = param.toString(); i++;
            } else if(arg.equals("-size")) {
              gamePoolSize = Integer.parseInt(param); i++;
            } else if(arg.equals("-restrict")) {
              restrictCoordinates = true;
            }
          }
        } else {
            System.out.println("Parameter -size <number_of_games_in_file> (default: "+gamePoolSize+")");
            System.out.println("Parameter -file <location_of_file> (default: "+gamePoolFilename+")");
            System.out.println("Parameter -restrict (default: player and goal "+(restrictCoordinates ? "" : "NOT")+" forced to same column or row, numChips *= 4/5)");
            if(args.length > 0 && args[0].equals("-help")) {
                return;
            }
        }
        
        if(gamePoolSize < 1) {
            throw new Exception("gamePoolSize < 1");
        }
        
        System.out.println("Generating " + gamePoolSize + " games into ["+gamePoolFilename+"]");
        System.out.println("Board   : " + numRows + "x" + numCols);
        System.out.println("Chips   : " + (restrictCoordinates ? (numChips * 4)/5 : numChips));
        System.out.println("Rounds  : " + NumRounds);
        System.out.println("Scoring : " + s);
        System.out.println("Players and goals will"+(restrictCoordinates ? "" : " NOT")+" be forced to same column or row");

        System.out.println("Started generating games at " + new Date());
        ArrayList<ArrayList<GameStatus>> gamePool = new ArrayList<ArrayList<GameStatus>>();
        long start;
        long interval;
        long total = 0;
        for(int gamePoolNum = 0; gamePoolNum < gamePoolSize; gamePoolNum++) {
            start = new Date().getTime();
            gamePool.add(makeGameList(restrictCoordinates));
            total += (interval = new Date().getTime() - start);
            System.out.println("Made game #" + (gamePoolNum + 1) +"\t" +(interval/1000)+ "s " +(interval%1000)+"ms");
        }
        total /= gamePoolSize;
        System.out.println("Completed generating games at " + new Date() +" (average "+(total/1000)+ "s " +(total%1000)+"ms per game)");
        
        ObjectOutputStream oos = null;
        
        try {
            oos = new ObjectOutputStream(new FileOutputStream(new File(gamePoolFilename)));
            oos.writeObject(gamePool);
        } catch (Throwable thr) {
            System.err.println("Failed to write games to file");
            thr.printStackTrace();
        } finally {
            try {
                oos.close();
            } catch (Throwable thr) {
                thr.printStackTrace();
            }
        }
        
    }

    public static int[][] addGamePlayed(int[][] games, int player, int opponent) {
        if(player == opponent) {
              return games;
        }
        games[player][opponent]++;
        return games;
    }

    public static ArrayList<GameStatus> makeGameList() {
        return makeGameList(false);
    }
    
    public static ArrayList<GameStatus> makeGameList(boolean restrictCoordinates) {
        ArrayList<GameStatus> games = new ArrayList();
        while(games.size() < NumRounds) {

            GameStatus xgs = new GameStatus();
            Set<PlayerStatus> xpsset = new LinkedHashSet<PlayerStatus>();
            xpsset.add(new PlayerStatus(ProposerID,-1));
            xpsset.add(new PlayerStatus(ResponderID,-1));
            xgs.setPlayers(xpsset);
    
            xgs.getPlayerByPerGameId(ProposerID).setRole("Proposer");
            xgs.getPlayerByPerGameId(ResponderID).setRole("Responder");

            GamePalette gp = new GamePalette();
            gp.add("RGBRed");
            gp.add("RGBGreen");
            gp.add("purple1");
            gp.add("orange1");
            xgs.setGamePalette(gp);
    
            xgs.setScoring(s);
                        
            for( PlayerStatus player : xgs.getPlayers() ) {
                // set chips for players
                ChipSet cs = getRandChipSet( xgs.getGamePalette(), restrictCoordinates);
                player.setChips( cs );
            }
            // assign player piece locations
            RowCol rcp1 = new RowCol(localRand.nextInt(numRows), localRand.nextInt(numCols));
            RowCol rcp2;
            do {
              rcp2 = new RowCol(localRand.nextInt(numRows), localRand.nextInt(numCols));
             } while(rcp2.equals(rcp1)); 
            xgs.getPlayerByPerGameId(ProposerID).setPosition(rcp1);  // Proposer's location
            xgs.getPlayerByPerGameId(ResponderID).setPosition(rcp2);  // Responder's location
            
            //generate random boards until we get one that is winnable with transactions but not completely winnable individually
            Board b;
            do {
                b = getRandBoard(xgs.getGamePalette(),xgs,restrictCoordinates);
            } while(!checkwinnable(b,xgs) || checktoowinnable(b,xgs));
            xgs.setBoard(b);
            
            games.add(xgs);
        }
        return games;
    }

//FOR GAME-MAKING

    //Returns random board with goals
    public static Board getRandBoard(GamePalette gp, GameStatus xgs, boolean restrictCoordinates){
        Board board = getRandomBoard(gp, numRows, numCols);
        RowCol pp0 = xgs.getPlayerByPerGameId(ProposerID).getPosition(); 
        RowCol pp1 = xgs.getPlayerByPerGameId(ResponderID).getPosition();
        RowCol pg0;  
        RowCol pg1; 
        do{
            if(restrictCoordinates) {
              board = getRandomBoard(gp, numRows, numCols);
            }
            pg0 = new RowCol(localRand.nextInt(board.getRows()), localRand.nextInt(board.getCols()));
            pg1 = new RowCol(localRand.nextInt(board.getRows()), localRand.nextInt(board.getCols()));
            
            if(pg0.equals(pg1) || pg0.equals(pp0) || pg0.equals(pp1) || pg1.equals(pp0) || pg1.equals(pp1)) {
              continue;
            } else if(restrictCoordinates) {
              if((pg0.row == pp0.row || pg0.col == pp0.col) && (pg1.row == pp1.row || pg1.col == pp1.col)) {
                break;
              } else {
                continue;
              }
            } else {
              break;
            }
        } while (true);
        board.setGoal(new Goal(ProposerID, pg0), true);
        board.setGoal(new Goal(ResponderID, pg1), true);
        return(board);
    }

      //returns random board, just the board, without goals or players
    public static Board getRandomBoard(GamePalette gp, int numRows, int numCols){
        Board board = new Board(numRows, numCols);
        Square[][] squares = new Square[board.getRows()][board.getCols()];

        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
              squares[i][j] = new Square();
              squares[i][j].setColor(gp.getRandomColor());
            }
        }
        board.setSquares(squares);
        return(board);
    }

    
    /**
            Generates random chipset for specified player
            <p>
            add total #chips as parameter
    */
    protected static ChipSet getRandChipSet( GamePalette gp, boolean restrictCoordinates ) {
        ChipSet chipset = new ChipSet();
        for( String color : gp.getColors() )
            chipset.set( color, 0 );
        int numChipsCorrected = restrictCoordinates ? ((numChips * 4) / 5) : numChips;
        for(int i = 0; i < numChipsCorrected; i++)
            chipset.add(gp.getRandomColor(), 1);
        return chipset;
    }

    //Check if, using a win-all transaction, both players can reach the goal
    //Add the chips of both players together, then check if any players can reach the goal using these chips
    private static boolean checkwinnable(Board b, GameStatus xgs){
        ChipSet together = ChipSet.addChipSets(xgs.getPlayerByPerGameId(ProposerID).getChips(), xgs.getPlayerByPerGameId(ResponderID).getChips());
        if(together.getNumChips() == 0)
            return(false);
        Path p;
        BestUse bu;
        PlayerStatus tempps = new PlayerStatus();
        
        tempps.setChips(together);

        for(PlayerStatus ps : xgs.getPlayers()){
            tempps.setPosition(ps.getPosition());
            //if the best use of the combined chips is 0 distance from the goal, then we have reached it
            if(new BestUse(b, tempps, s, ps.getPerGameId()).getBestState().getPosition().dist(b.getGoalLocations(ps.getPerGameId()).get(0)) != 0){
                return(false);    
            }
        }
        return(true);
    }

    //Check if, without using a transaction, both players can reach the goal
    private static boolean checktoowinnable(Board b, GameStatus xgs){
        for(PlayerStatus ps : xgs.getPlayers()){
            //if the best use of the combined chips is 0 distance from the goal, then we have reached it
            if((new BestUse(b, ps, s, ps.getPerGameId())).getBestState().getPosition().dist(b.getGoalLocations(ps.getPerGameId()).get(0)) != 0){
                return(true);    
            }
        }
        return(false);
    }
    
    public static ArrayList<ArrayList<GameStatus>> loadGamePool(String gamePoolFilename) {
        ObjectInputStream ois = null;
        ArrayList<ArrayList<GameStatus>> gamePool = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(new File(gamePoolFilename)));
            gamePool = (ArrayList) ois.readObject();
        } catch (Throwable thr) {
            System.err.println("Failed to read games from file [" + gamePoolFilename + "]");
            thr.printStackTrace();
        } finally {
            try {
                ois.close();
            } catch (Throwable thr) {
                thr.printStackTrace();
            }
        }
        return gamePool;
    }

}
