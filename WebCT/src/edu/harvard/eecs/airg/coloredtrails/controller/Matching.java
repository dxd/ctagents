package edu.harvard.eecs.airg.coloredtrails.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class creates matchings for players divided into two separate groups (think rooms). In each group there are two
 * broad classes of players: humans and agents. Each human must play each agent once, and other humans a specified number of 
 * times. A player in one room will only play opponents in the other room. A player will never play the same player (computer or human) twice.
 * 
 * There is not a lot of error checking here. For example, if there are 
 * @author legodude
 */
public class Matching {
    ArrayList<ArrayList<Player>> Players = new ArrayList<ArrayList<Player>>();
    
    int numhumangames;
    Random localrand = new Random();
    int numgames;
    
    boolean doLog = false;
    FileWriter out;
    
            
    /**
     * Initializes object
     * @param Group1 list of players in one group
     * @param Group2 list of players in the other group
     * @param numhumangames number of times each human is to play other humans
     */
    public Matching(ArrayList<Player> Group1, ArrayList<Player> Group2, int numhumangames){
        Players.add(new ArrayList<Player>(Group1));
        Players.add(new ArrayList<Player>(Group2));
        this.numhumangames = numhumangames;
    }
    
    /**
     * Writes the output of the matching algorithm to a file
     * @param filename name of file to write log to
     */
    public void enableMatchingLog(String filename){
        try {
            out = new FileWriter(filename);
        } catch (IOException ex) {
            Logger.getLogger(Matching.class.getName()).log(Level.SEVERE, null, ex);
        }
        doLog = true;
    }
    
    /**
     * Creates the matching
     */
    public void doMatch(){
        
        int numhumans = Player.getListofTypes(Players.get(0), "HumanGUI").size();
        int numagents = Player.getListofAllButType(Players.get(0), "HumanGUI").size();
        
        numgames = numagents + numhumangames;
        
        //Need to first prepare each player's list of opponents
        prepareCompOpponents(Players.get(0), Players.get(1));
        prepareCompOpponents(Players.get(1), Players.get(0));
        
        playEachOther(Player.getListofTypes(Players.get(0), "HumanGUI"), Player.getListofTypes(Players.get(1), "HumanGUI"), numhumangames);
        playEachOther( Player.getListofAllButType(Players.get(0), "HumanGUI"), 
                Player.getListofAllButType(Players.get(1), "HumanGUI"), 
                numgames - numhumans );
        //numgames - numhumans
        
        
        
    }
    
    /**
     * Each player in list1 will have (numTimes) players from list2 added to her/his/its opponent list
     * @param list1
     * @param list2
     * @param numTimes
     */
    private void playEachOther(ArrayList<Player> list1, ArrayList<Player> list2, int numTimes){

        Player tp, p;
        for( int i = numTimes; i > 0; i--){
            for (int l = 0; l < list1.size(); l++ ){
                p = list1.get(l);
                tp = list2.get( (l + i) % list2.size());
                p.getOppList().add(tp);
                tp.getOppList().add(p);
            }
        }
    }
    
    /**
     * For every human player in (players), this adds all nonhuman players in (opponents) to its list of opponents
     * @param players
     * @param opponents
     * @param numTimes
     */
    private void prepareCompOpponents(ArrayList<Player> players, ArrayList<Player> opponents){
        
        ArrayList<Player> nonhumans = new ArrayList<Player>();
        for(Player p : opponents){
            if(!p.getType().equals("HumanGUI"))
                nonhumans.add(p);
        }
        for(Player p : players){
            if(p.getType().equals("HumanGUI")){
                p.getOppList().addAll(nonhumans);
                for(Player a : nonhumans)
                    a.getOppList().add(p);
            }
        }
        
    }
 
    /**
     * Returns matchings for one round of play. gamesLeft() should be called prior to ensure that another round exists
     * @return Array of pairings
     */
    public ArrayList<int[]> getMatchings(){
        
        numgames--;
        
        System.out.println("starting to generate matchings");
        
        Stack<Player> assignment = new Stack<Player>();
        ArrayList<Player> unassignedPlayers = new ArrayList<Player>(Players.get(0));
        
        for(Player p : unassignedPlayers){
            p.Opponent = null;
            p.oppListCopy = new ArrayList<Player>(p.getOppList());
            Collections.shuffle(p.oppListCopy);
        }
        
        Collections.shuffle(unassignedPlayers);
        
        assignment = assignOpp(assignment, unassignedPlayers);
        if(assignment == null)
            System.out.println("kill me now");
        else{
//            System.out.println("##########################################");
            print("##########################################");
            for(Player p : assignment){
//                System.out.println("player " + p.toString() + " will play " + p.Opponent.toString());
                print("player " + p.toString() + " will play " + p.Opponent.toString());
                p.getOppList().remove(p.Opponent);
            }
            System.out.println("##########################################");
            
        }

        System.out.println("done generating matchings");
        
        ArrayList<int[]> matchings = new ArrayList<int[]>();
        int [] tplys;
        for(Player p : assignment){
            tplys = new int[2];
            tplys[0] = p.getPin();
            tplys[1] = p.Opponent.getPin();
            matchings.add( tplys );
        }
        
        if( (numgames == 0) && doLog ){
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(Matching.class.getName()).log(Level.SEVERE, null, ex);
            }
            //just for safety
            doLog = false;
        }
        return(matchings);
    }
    
    /**
     * Writes to screen and to file if logging enabled
     * @param s
     */
    private void print(String s){
        System.out.println(s);
        if(doLog){
            try {
                out.write(s + "\n");
            } catch (IOException ex) {
                Logger.getLogger(Matching.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
            
    }
    
    /**
     * Recursive algorithm to solve constrain satisfaction problem
     * @param assignment
     * @param unassignedPlayers
     * @return
     */
    private Stack<Player> assignOpp(Stack<Player> assignment, ArrayList<Player> unassignedPlayers){
        if(unassignedPlayers.size() == 0)
            return(assignment);
        
        Stack<Player> result;
        
        //now we select an unassigned player
        //Player p = unassignedPlayers.get(localrand.nextInt(unassignedPlayers.size()));
        Player p = unassignedPlayers.get(0);
        
        //System.out.println("finding an opponent for: " + p.toString());
        
        for( Player Opp : p.oppListCopy ){
            
            //Check to see if this player is valid
            Boolean alreadyAssigned = false;
            for(Player ap : assignment){
                if(ap.Opponent.equals(Opp))
                    alreadyAssigned = true;
            }
            
            if(!alreadyAssigned){
                p.Opponent = Opp;
                //System.out.println("Player: " + p.toString() + " is going to play " + Opp.toString());
                assignment.push(p);
                unassignedPlayers.remove(p);
                result = assignOpp(assignment, unassignedPlayers);
                if(result != null)
                    return(result);
                else{
                    if(!assignment.isEmpty()){
                        assignment.pop();
                        unassignedPlayers.add(p);
                        Collections.shuffle(unassignedPlayers);
                    }
                }
            }
            
        }
        return(null);
    }
    
    /**
     * Checks to see if more rounds of play are needed
     * @return true if more rounds need to be played
     */
    public Boolean gamesLeft(){
        System.out.println("Number of games left: " + numgames);
        if(numgames > 0)
            return(true);
        else
            return(false);
    }
}

