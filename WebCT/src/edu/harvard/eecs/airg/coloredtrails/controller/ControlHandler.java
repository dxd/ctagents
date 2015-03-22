package edu.harvard.eecs.airg.coloredtrails.controller;

import edu.harvard.eecs.airg.coloredtrails.shared.PlayerConnection;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;
import java.util.List;


public class ControlHandler {
    public void PlayerList(List<PlayerConnection> pcs)
    {
        
    }
    
    public void GameEnded(int gameId)
    {
        //System.out.println("Default: game ended: " + gameId);
    }
    
    public void PhaseAdvanced(int gameId, String current)
    {
        //System.out.println(gameId + ": Current phase: " + current);
    }
    
    public void gameList(List<GameStatus> games){
        
    }
 
}