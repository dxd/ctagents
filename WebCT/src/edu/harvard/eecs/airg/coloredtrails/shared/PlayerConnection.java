/*
	Colored Trails
	
	Copyright (C) 2006, President and Fellows of Harvard College.  All Rights Reserved.
	
	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package edu.harvard.eecs.airg.coloredtrails.shared;

import edu.harvard.eecs.airg.coloredtrails.server.ClientState;
import java.io.Serializable;


/**
<b>Description</b>

Represents the status of a player's connection to the server.  
The code is entirely for reading and writing state fields; there 
is no code that performs any network operations in this class.
<p>
[STAE what classes use this class.  includes a 'pin' field -- this is 
the same as the player's ID found in PlayerStatus class?]

<p>

<b>Original Summary</b>
 * A type describing the state of a given player who has connected
 * to the server.
 *
 * @author Paul Heymann (ct3@heymann.be)
	@author Sevan G. Ficici (class-level review and comments)
 */
public class PlayerConnection extends InitializableObject implements Serializable
{
    private String status;
    private int pin = -1;
    private String hostip = null;
    private int commport = -1;
    //type is class type of client...
    private String clientClassType;
    private ClientState.EClientState myState;
    
    //total score for player across games
    private double Score = 0;

    /**
     * Get the connection status of the associated player.
     *
     * @return The connection status of the associated player.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set the connection status of the associated player.
     *
     * @param status The connection status of the associated player.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get the port which the remote client is listening on.
     *
     * @return The port which the remote client is listening on.
     */
    public int getCommport() {
        return commport;
    }

    /**
     * Set the port which the remote client is listening on.
     *
     * @param commport The port which the remote client is listening on.
     */
    public void setCommport(int commport) {
        this.commport = commport;
    }

    public PlayerConnection(String status, int pin, String hostip,
                            int hostport, String clientClassType) {
        setPrivateVariables(status, pin, hostip, hostport, clientClassType);
    }

    /**
     * Get the host IP of the remote client.
     *
     * @return The host IP of the remote client.
     */
    public String getHostip() {
        return hostip;
    }

    /**
     * Set the host IP of the remote client.
     *
     * @param hostip The host IP of the remote client.
     */
    public void setHostip(String hostip) {
        this.hostip = hostip;
    }

    private void setPrivateVariables(String status, int pin,
                                     String hostip, int hostport, String clientClassType) {
        this.status = status;
        this.pin = pin;
        this.hostip = hostip;
        this.commport = hostport;
        this.clientClassType = clientClassType;
        setInitialized(true);
    }



    /**
     * Get the pin of the player who has connected.
     *
     * @return The pin of the player who has connected.
     */
    public int getPin() {
        throwIfNotInitialized();
        return pin;
    }

    /**
     * Set the pin of the player who has connected.
     *
     * @param pin The pin of the player who has connected.
     */
    public void setPin(int pin) {
        this.pin = pin;
    }

    public void setEClientState(ClientState.EClientState cs){
        myState = cs;
    }
    
    public ClientState.EClientState getEClientState(){
        return(myState);
    }
    
    /**
     * Used for identifying type of client, automatically called
     * @param type
     */
    public void setClientClassType(String type){
        this.clientClassType = type;
    }
    
    /**
     * Allows controller/server to determine type of client
     * @return client type, "HumanGUI" for GUI, class type for agent
     */
    public String getClientClassType(){
        return(clientClassType);
    }

    public String toString() {
        throwIfNotInitialized();
        return "Player...\nStatus: " + status +
                "\nPIN: " + pin + "\n" +
                "IP Address: " + hostip + ".\n" +
                "Comm port: " + commport + ".\n";
    }
    
    /**
     * Gets total score for player
     * @return score
     */
    public double getScore(){
        return(Score);
    }
    
    /**
     * Set score
     * @param score
     */
    public void setScore(double score){
        Score = score;
    }
    
}
