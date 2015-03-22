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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

public class setuprecip2
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

class Runner2 extends Thread
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
    
    Runner2(ControlImpl Controller)
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
        
        
        
        Room1.addAll(humans.subList(0, humans.size()/2));
        Room2.addAll(humans.subList(humans.size()/2, humans.size()));
        
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
        
       
        
        int numberOfHumansToPlay = 2;
        
        
        
        Matching M = new Matching(Room1, Room2, numberOfHumansToPlay);
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
            Logger.getLogger(Runner2.class.getName()).log(Level.FATAL, null, ex);
            return(null);
        } catch (InstantiationException ex) {
            Logger.getLogger(Runner2.class.getName()).log(Level.FATAL, null, ex);
            return(null);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Runner2.class.getName()).log(Level.FATAL, null, ex);
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
    
    
    
    
    
    
//    public void PlayerList(List<PlayerConnection> pcs) {
//
//        AgentAdaptorPR agent;
//        agent = new RecipSocialPref2Agent();
//        agent.setClientName(String.valueOf(5432));
//        agent.start();
//        
//        for(PlayerConnection pc : pcs){
//            System.out.println("ID: " + pc.getPin() + " " + pc.getEClientState());
//        }
//       
//        
//        
//        int i = pcs.size();
//        System.out.println("i: " + i);
//        if(i < 2)
//            return;
//        //generate matchings
//        int r;
//        int[][] matchings = new int[i / 2][2];
//        System.out.println("mat: " + matchings.length);
//        for(int j = 0; j < matchings.length; j++){
//            for(int k = 0; k < matchings[j].length; k++){
//                r = localrand.nextInt(i);
//                matchings[j][k] = pcs.get(r).getPin();
//                pcs.remove(r);
//                i--;
//            }
//        }
//        
//        for(int j = 0; j < matchings.length; j++){
//            System.out.print(j + ": ");
//            for(int k = 0; k < matchings[j].length; k++){
//                System.out.print(matchings[j][k] + " ");
//            }
//            System.out.println();
//        }
//        
//        for (int l = 0; l < matchings.length; l++){
//            System.out.println("Creating game: " + l);
//            NewGame = new Game(Controller, matchings[l], "gameconfigs", "RecipConfig");
//            Games.add(NewGame);
//        }
//        
//
//        
//        System.out.println("Trying to start games");
//        for( Game StartGame : Games ){
//            System.out.println("Starting: " + StartGame.toString());
//            StartGame.Start();
//        }
//    }
    
    public void halt(){
        for(Game g : Games)
            g.requestGameEnd();
    }
}

