package RecipExperiment;


//import CreateGame;
import RecipExperiment.RecipConstants;
import RecipExperiment.CreateGame;
import ctagents.recipagents.*;
import ctagents.recipagents.CopyCatAgent.CopyCatAgent;
import ctagents.recipagents.CopyCatFutureMerit.CopyCatFutureMerit;
import ctagents.recipagents.FairnessAgent.FairnessAgent;
import ctagents.recipagents.FutureMeritBestResponse.FutureMeritBestResponseAgent;
import ctagents.recipagents.RecipNashBargainAgent.RecipNashBargainAgent;
import ctagents.recipagents.RecipNashEqAgent.RecipNashEqAgent;
import ctagents.recipagents.RecipSocialPref2Agent.RecipSocialPref2Agent;
import ctagents.recipagents.RetroAgent.RetroAgent;
import edu.harvard.eecs.airg.coloredtrails.controller.*;
import edu.harvard.eecs.airg.coloredtrails.server.ClientState;
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



/**
 * This class is an implementation of a controller which is associated with the RecipConfig file.
 * Here we interact with the server to query for human players, set up game matchings, and 
 * generate the board/data that each game will use
 * 
 * @author legodude
 */

public class setuprecip
{
    public static void main(String arg[])
    {
        
        String server = "localhost";
        int port = 8081;
        
        Runner Frank;
        
        
        ControlImpl Controller = new ControlImpl("tcp://" + server + ":" + port);
        Frank = new Runner(Controller);
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        do{
            try {
                input = br.readLine();
                if (input.equals("g")) {
                    Controller.listGames();
                } else if (input.equals("h")){
                    Frank.halt();
                }
            } catch (IOException ex) {
                Logger.getLogger(setuprecip.class.getName()).log(Level.FATAL, null, ex);
            }
            
        } while (!input.equals("q"));
        

    }

}

class Runner extends ControlHandler
{ 
    List<Game> Games = new ArrayList<Game>();
    Game NewGame;
    static Random localrand = new Random();
    ControlImpl Controller;
    int agentcounter = 43432;
    
    Runner(ControlImpl Controller)
    {
        
        
        this.Controller = Controller;
        Controller.setControlHandler(this);
        System.out.println("Listing players");
        List<PlayerConnection> players;
        
        players = Controller.getPlayersWait();
        
        System.out.println("players gotten");
        //Controller.listPlayers();
        for(PlayerConnection pc : players){
            System.out.println("ID: " + pc.getPin() + " " + pc.getEClientState() + " " + pc.getClientClassType());
            
        }
        
        ArrayList<PlayerConnection> humans = new ArrayList<PlayerConnection>();
        for(PlayerConnection pc : players)
            if((pc.getEClientState() == ClientState.EClientState.ACTIVE) && (pc.getClientClassType().equals("HumanGUI")))
                humans.add(pc);
        
        
        int[] playerstowaitfor = new int[humans.size()];
        for(int i = 0; i < playerstowaitfor.length; i++)
            playerstowaitfor[i] = launchagent(0);
        Controller.waitForPlayers(playerstowaitfor);
        
        System.out.println("here we go");
        
        players = Controller.getPlayersWait();
        for(PlayerConnection pc : players)
            System.out.println("ID: " + pc.getPin() + " " + pc.getEClientState() + " " + pc.getClientClassType());
            
        //well, shit, we have our players, time to lauch blastoff!
        
        System.out.println("humans: ");
        for(int i = 0; i < humans.size(); i++)
            System.out.println(humans.get(i).toString());
        
        
        CreateGame cg = new CreateGame();
        ArrayList<GameStatus> game = new ArrayList<GameStatus>();
        for(int i = 0; i < RecipConstants.numRounds; i++)
            game.add(cg.getGame());
        
        int[] tplys = new int[2];
        for(int i = 0; i < humans.size(); i++){
            tplys[0] = humans.get(i).getPin();
            tplys[1] = playerstowaitfor[i];
            System.out.println("tplys " + tplys[0] + "  " + tplys[1]);
//            NewGame = new Game(Controller, tplys, "experiments/RecipExperiment", "RecipConfig", game);
            NewGame = new Game(Controller, tplys, "gameconfigs", "RecipConfig", game);
            Games.add(NewGame);
        }
        
        System.out.println("Trying to start games");
        for( Game StartGame : Games ){
            System.out.println("Starting: " + StartGame.toString());
            StartGame.Start();
        }
    
    }
    
    private int launchagent(int round){
        agentcounter++;
        RecipAgentAdaptor agent;
//        agent = new RetroAgent();
        agent = new RecipSocialPref2Agent();
//        agent = new FutureMeritBestResponseAgent();
//        agent = new RecipNashEqAgent();
//        agent = new RecipNashBargainAgent();
//        agent = new FairnessAgent();
//        agent = new CopyCatFutureMerit();
        agent.setClientName(String.valueOf(agentcounter));
        agent.start();
        return(agentcounter);
        
    }
    
    
 
    
    public void halt(){
        for(Game g : Games)
            g.haltGame();
    }
}

