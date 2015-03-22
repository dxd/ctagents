package edu.harvard.eecs.airg.coloredtrails.controller;

import edu.harvard.eecs.airg.coloredtrails.shared.PlayerConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class to help with starting games and matchings
 * 
 * This class maintains a pin->Player map to simplify lookup.
 * It also allows arbitrary data to be associated with a player.
 * For example, one can associate a specific game with a computer player.
 * @author legodude
 */
public class Player{
    ArrayList<Player> opponentslist = new ArrayList<Player>();
    String type;
    PlayerConnection pc = null;
    Object data = null;
    int pin;
    
    
    //These are used to generate matchings
    public ArrayList<Player> oppListCopy;
    public Player Opponent;
    
    //used to map pins to players
    private static HashMap pinPlayerMap = new HashMap();

    /**
     * Initializes and adds this player to the pin->player map.
     * setPlayerConnection() should be called once the PlayerConnection is obtained
     * @param pin Pin of player
     */
    public Player(int pin){
        this.pin = pin;
        pinPlayerMap.put(pin, this);
    }
    
    /**
     * Initializes object from data in pc
     * @param pc PlayerConnection of player
     */
    public Player(PlayerConnection pc){
        setPlayerConnection(pc);
        pinPlayerMap.put(pin, this);
    }
    
    /**
     * Gets associated PlayerConnection
     * @return null if the PlayerConncetion has not been set
     */
    public PlayerConnection getPlayerConnection(){
        return(pc);
    }
    
    /**
     * Gets arbitrary data
     * @return null if no data set
     */
    public Object getData(){
        return(data);
    }
    
    /**
     * Sets arbitrary data associated with this player
     * @param data
     */
    public void setData(Object data){
        this.data = data;
    }
    
    /**
     * Get the list of Players associated with this player. Intended to be a list of opponents, but can be used for any purpose.
     * @return The list has already been "new"ed, so is ready to use
     */
    public ArrayList<Player> getOppList(){
        return(opponentslist);
    }
    
    /**
     * Pulled from PlayerConnection object
     * @return
     */
    public String getType(){
        return(type);
    }
    
    /**
     * Return pin
     * @return
     */
    public int getPin(){
        return(pin);
    }
    
    /**
     * This should be called if the PlayerConnection object is not passed in the constructor
     * @param pc
     */
    public void setPlayerConnection(PlayerConnection pc){
        if(pc == null){
            
        } else {
            this.pc = pc;
            pin = pc.getPin();
            type = pc.getClientClassType();
        }
    }
    
    /**
     * When a Player object is create, it is added to a pin->player mapping
     * @param pin Pin
     * @return Associated Player object
     */
    public static Player getPlayer(int pin){
        return((Player)pinPlayerMap.get(pin));
    }
    
    /**
     * Returns a sublist of all players of a specified type
     * @param tplayers list to parse
     * @param type type of player to return
     * @return list of players of the specified type
     */
    public static ArrayList<Player> getListofTypes(List<Player> tplayers, String type){
        ArrayList<Player> typeplayers = new ArrayList<Player>();
        for(Player p : tplayers){
            if(p.getType().equals(type))
                typeplayers.add(p);
        }
        return(typeplayers);
    }
    
    /**
     * Get a sublist of all players that do _not_ match a specified type.
     * Useful for example to get all players who are not "HumanGUI"
     * @param tplayers list to parse
     * @param type type of player to exclude
     * @return list of players not of specified type
     */
    public static ArrayList<Player> getListofAllButType(List<Player> tplayers, String type){
        ArrayList<Player> retPlayers = new ArrayList<Player>(tplayers);
        retPlayers.removeAll(getListofTypes(tplayers, type));
        return(retPlayers);
    }
    
    @Override
    public String toString(){
        String st = type;
        int pos = st.lastIndexOf('.');
        if(pos != -1)
            st = st.substring(pos + 1);
        return("Pin: " + pin + "  type: " + st);
    }
}