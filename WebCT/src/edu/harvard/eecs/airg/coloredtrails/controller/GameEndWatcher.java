package edu.harvard.eecs.airg.coloredtrails.controller;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Waits for a set of games to end
 * @author legodude
 */
public class GameEndWatcher implements Observer{
    
    Object lock = new Object();
    Boolean allEnded = false;
    List<Game> games;
    
    /**
     * Initialization
     * @param games List of games to wait for
     */
    public GameEndWatcher(List<Game> games){
        this.games = games;
        for(Game g : games)
            g.addObserver(this);
    }
    
    /**
     * Blocks until games specified in constructor have ended
     */
    public void waitForGamesToEnd(){
        synchronized(lock){
            System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$ Waiting for games to end $$$$$$$$$$$$$$$$$$$$$");
            while(!allEnded){
                try {
                    lock.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(GameEndWatcher.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ Games have ended $$$$$$$$$$$$$$$$$$$$$$$$$");
        }
    }

    public void update(Observable o, Object arg) {
        synchronized(lock){
            allEnded = true;
            for(Game g: games){
                if(!g.gameEnded())
                    allEnded = false;
            }
            if(allEnded)
               lock.notifyAll();
        }
    }
}