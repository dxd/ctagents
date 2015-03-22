package RecipExperiment;


//import CreateGame;
import RecipExperiment.RecipConstants;
import RecipExperiment.CreateGame;
import ctagents.recipagents.*;
import edu.harvard.eecs.airg.coloredtrails.controller.*;
import edu.harvard.eecs.airg.coloredtrails.server.ClientState.EClientState;
import edu.harvard.eecs.airg.coloredtrails.shared.PlayerConnection;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketAppender;


/**
 * This class is an implementation of a controller which is associated with the RecipConfig file.
 * Here we interact with the server to query for human players, set up game matchings, and 
 * generate the board/data that each game will use
 * 
 * @author legodude
 */

public class setuprecip3
{
    public static void main(String arg[])
    {
        
        String server = "localhost";
        int port = 8081;
        
        Runner3 Frank;
        
        
        ControlImpl Controller = new ControlImpl("tcp://" + server + ":" + port);
        Frank = new Runner3(Controller);
        Frank.start();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        do{
            try {
                input = br.readLine();
                if (input.equals("g")) {
                    Controller.listGames();
                } else if (input.equals("h")){
                    System.out.println("Trying to halt games");
                    Frank.halt();
                }
            } catch (IOException ex) {
                Logger.getLogger(setuprecip.class.getName()).log(Level.FATAL, null, ex);
            }
            
        } while (!input.equals("q"));
        

    }

}

class Runner3 extends Thread
{ 
    //List of classes to use for agents
//    String[] agenttypes = { "ctagents.recipagents.RetroAgent.RetroAgent",
//    "ctagents.recipagents.RecipSocialPref2Agent.RecipSocialPref2Agent",
//    "ctagents.recipagents.CopyCatAgent.CopyCatAgent",
//    "ctagents.recipagents.FairnessAgent.FairnessAgent" };
     String[] agenttypes = { "ctagents.recipagents.RetroAgent.RetroAgent",
    "ctagents.recipagents.RecipNashBargainAgent.RecipNashBargainAgent" };

//    String[] agenttypes = { "ctagents.recipagents.RecipSocialPref2Agent.RecipSocialPref2Agent",
//    "ctagents.recipagents.RecipNashEqAgent.RecipNashEqAgent" };

    int numRounds = RecipConstants.numRounds + 1;
    
    List<Game> Games = new ArrayList<Game>();
    Game NewGame;
    static Random localrand = new Random();
    ControlImpl Controller;
    int agentcounter = localrand.nextInt();
    
    Logger logr;
    
    Timer timer;
    Updater timerupdater;
    
    Runner3(ControlImpl Controller)
    {
        
        
        
        agentcounter = 444;
        
        this.Controller = Controller;
        //Controller.setControlHandler(this);
    }
    
    public void run() {
        
        logr = Logger.getLogger(this.getClass().getName());
        SocketAppender appender = new SocketAppender(RecipConstants.LOG_SERVER, 4445);
        logr.addAppender(appender);
        logr.log(Level.INFO, "test'");
        
        timer = new Timer("auto update timer");
        timerupdater = new Updater(Controller, logr);
        timer.schedule(timerupdater, 0, 10000);
        
        
        System.out.println("Listing players");
        List<PlayerConnection> players;
        
        players = Controller.getPlayersWait();
        if(( (players.size() + agenttypes.length) % 2) == 1){
            System.out.println("Odd number of players");
            return;
        }
        
        System.out.println("players gotten");
        
        
        ArrayList<Player> humans = new ArrayList<Player>();
        ArrayList<Player> nonhumans = new ArrayList<Player>();
        
        for(PlayerConnection p : players){
            if(p.getEClientState() == EClientState.ACTIVE)
                humans.add(new Player(p));
        }
        
        ArrayList<Player> Room1 = new ArrayList<Player>();
        ArrayList<Player> Room2 = new ArrayList<Player>();
        
        
        
//        Room1.addAll(humans.subList(0, humans.size()/2));
//        Room2.addAll(humans.subList(humans.size()/2, humans.size()));
        for(Player p : humans){
            if(  p.getPin() < 200 )
                Room1.add(p);
            else
                Room2.add(p);
        }
        //set up agents
                
        
        for(int i = 0; i < agenttypes.length; i++){
            Room1.add( new Player((Integer.valueOf(launchagent(agenttypes[i]).getClientName())) ) );
            Room2.add( new Player((Integer.valueOf(launchagent(agenttypes[i]).getClientName())) ) );
            
        }
        
        ArrayList<Integer> playerstowaitfor = new ArrayList<Integer>();
        for(Player p : Room1)
            playerstowaitfor.add(new Integer(p.getPin()));
        for(Player p : Room2)
            playerstowaitfor.add(new Integer(p.getPin()));
        
        Controller.waitForPlayers(playerstowaitfor);
        
        System.out.println("--------------------Agents launched-------------------");
        
        
        players = Controller.getPlayersWait();
        
        for(PlayerConnection pc : players){
            System.out.println("ID: " + pc.getPin() + " " + pc.getEClientState() + " " + pc.getClientClassType());
            
        }
        
        
        //At this point we have all the players
        
        for(PlayerConnection pc : players){
            if(!pc.getClientClassType().equals("HumanGUI") && (pc.getEClientState()  == EClientState.ACTIVE))
                Player.getPlayer(pc.getPin()).setPlayerConnection(pc);
        }
        
        System.out.println("----------Room1-------------");
        for(Player p : Room1)
            System.out.println(p.toString());
        
        System.out.println("----------Room2----------------");
        for(Player p : Room2)
            System.out.println(p.toString());
        
       
        
        int numberOfHumansToPlay = 1;
        
        
        
        Matching M = new Matching(Room1, Room2, numberOfHumansToPlay);
        M.enableMatchingLog("Matching.log");
        M.doMatch();
        
        for(Player p : Room1){
            System.out.println("***********************************************");
            System.out.println(p.toString());
            for(Player tp : p.getOppList())
                System.out.println("     opp: " + tp.toString());
        }
        for(Player p : Room2){
            System.out.println("***********************************************");
            System.out.println(p.toString());
            for(Player tp : p.getOppList())
                System.out.println("     opp: " + tp.toString());
        }
        
        
        ArrayList<ArrayList<int[]>> ExperimentDesign = new ArrayList<ArrayList<int[]>>();
        
        while(M.gamesLeft()){
            System.out.println("////////////////////////////////////////////////");
            ArrayList<int[]> matchings = M.getMatchings();
            
            ArrayList<int[]> removeme = new ArrayList<int[]>();

            for(int[] mat : matchings){
                if( !Player.getPlayer(mat[0]).getType().equals("HumanGUI")  && !Player.getPlayer(mat[1]).getType().equals("HumanGUI") )
                    removeme.add(mat);
            }
            matchings.removeAll(removeme);
            for(int[] mat : matchings)
                System.out.println("player " + mat[0] + " will play " + mat[1]);
            ExperimentDesign.add(matchings);
        }
        
        
        //Now we need to give each agent the same game to play
        for(String agenttype : agenttypes){
            ArrayList<GameStatus> game = getGame(numRounds);
            for(PlayerConnection pc : players){
                if(pc.getClientClassType().equals(agenttype))
                    Player.getPlayer(pc.getPin()).setData(game);
            }
        }
        
        for(ArrayList<int[]> matchings : ExperimentDesign){
            Games = new ArrayList<Game>();
            
            for(int[] mat : matchings){
                ArrayList<GameStatus> game = null;
                for(int i = 0; i < mat.length; i++){
                    Player p = Player.getPlayer(mat[i]);
                    if(!p.getType().equals("HumanGui"))
                        game = (ArrayList<GameStatus>)p.getData();
                }
                if( game == null )
                    game = getGame(numRounds);
                NewGame = new Game(Controller, mat, "gameconfigs", "RecipConfig", game);
                Games.add(NewGame);
            }

            
            timerupdater.setGames( (ArrayList<Game>) Games);
            
            System.out.println("Trying to start games");
            for( Game StartGame : Games ){
                logr.info("Starting: " + StartGame.toString());
                StartGame.Start();
            }
            GameEndWatcher gew = new GameEndWatcher(Games);
            gew.waitForGamesToEnd();
            
            for( Game g : Games)
                logr.info("Ended: " + g.toString());
            
        }
    
        
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%% The Experiment is over %%%%%%%%%%%%%%%%%%%%%");
        players = Controller.getPlayersWait();
        for(PlayerConnection pc : players){
            //if(pc.getClientClassType().equals("HumanGUI"))
            System.out.println("Player: " + pc.getPin() + " score: " + pc.getScore() + " " + pc.getClientClassType());
        }

    
    }
    
    private RecipAgentAdaptor launchagent(String classname){
        agentcounter++;
        RecipAgentAdaptor agent;
        try {
                agent = (RecipAgentAdaptor) Class.forName(classname).newInstance();
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Runner3.class.getName()).log(Level.FATAL, null, ex);
            return(null);
        } catch (InstantiationException ex) {
            Logger.getLogger(Runner3.class.getName()).log(Level.FATAL, null, ex);
            return(null);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Runner3.class.getName()).log(Level.FATAL, null, ex);
            return(null);
        }
        
        
        agent.setClientName(String.valueOf(agentcounter));
        agent.start();
        return(agent);
        
    }
    
    private ArrayList<GameStatus> getGame(int numRounds){
        CreateGame cg = new CreateGame();
        ArrayList<GameStatus> game = new ArrayList<GameStatus>();
        for(int i = 0; i < numRounds; i++)
            game.add(cg.getGame());
        return(game);
    }
    //enum playerstate = {READY, INGAME}
    
    
    
    
    
    

    
    public void halt(){
        for(Game g : Games)
            g.requestGameEnd();
    }
}


class Updater extends TimerTask {

    ArrayList<Game> games = null;
    ControlImpl ctrl = null;
    Logger logr;
//    BufferedWriter out;
    FileWriter out;
    
    public Updater(ControlImpl ctrl, Logger logr) {
        this.ctrl = ctrl;
        this.logr = logr;
        try {
//            out = new BufferedWriter(new FileWriter("ExperimentStatus.txt"));
            out = new FileWriter("ExperimentStatus.txt");
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Updater.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    public void setGames(ArrayList<Game> games){
        this.games = games;
    }
    
    private void print(String s){
        logr.info(s);
        try {
            out.write(s + "\n");
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Updater.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
            
    }
    
    @Override
    public void run() {
        if(games != null){
            ctrl.getActiveGames();
            print( "-------------------------- Updates on " + games.size() + " games ----------------------------------------" );
            for(Game g : games){
//                logr.info("Game: " + g.getGameId() + " started: " + g.gameStarted() + " ended: " + g.gameEnded());
                if( g.gameStarted() && !g.gameEnded() ){
                    print("(Game " + g.getGameId() + ") round: " + g.getGameStatus().get("round") );
                    
                }
            }
            print("========== Players: =============");
            List<PlayerConnection> pcs = ctrl.getPlayersWait();
            for(PlayerConnection pc : pcs)
//                if(pc.getClientClassType().equals(edu.harvard.eecs.airg.coloredtrails.shared.Constants.HUMAN)){
                print("Player: " + pc.getPin() + " Score: " + pc.getScore());
//                }
        }
        try {
            out.flush();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Updater.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    
    
}