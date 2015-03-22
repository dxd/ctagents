/*
	Colored Trails
	
	Copyright (C) 2006-2007, President and Fellows of Harvard College.  All Rights Reserved.
	
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

package edu.harvard.eecs.airg.coloredtrails.client.ui;

import ctgui.original.AllPlayersChipDisplay;
import java.util.Set;
import java.util.Observer;

import ctgui.original.Taskbar;

import edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsClientImpl;
import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.*;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Board;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GamePalette;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.HistoryLog;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import java.io.Serializable;

/**
 * @author Ricardo De Lima (ricardo@eecs.harvard.edu)
 * 
 * A SimpleAgent using the CT Agent API for GUI's use:
 * 
 * 
 *
 */
public class SimpleGUIAgentImpl extends ColoredTrailsClientImpl {
	

	public SimpleGUIAgentImpl() {
            super("HumanGUI");	
	}

	/* This method will be invoked when the Board is updated
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.AgentClient#boardUpdated(edu.harvard.eecs.airg.coloredtrails.shared.types.Board)
	 */
	@Override
	public void boardUpdated(Board b) {

		System.out.println("Board Updated");
                GameStatus gs = getGameStatus();
                gs.setBoard(b);
	}

	/* This method will be invoked when the Game has ended
	 * 
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.AgentClient#gameEnded()
	 */
	@Override
	public void gameEnded() {
            System.out.println("Game Ended");
            getGameStatus().setEnded();
            super.gameEnded();
	}

	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.AgentClient#gamePaletteUpdated(edu.harvard.eecs.airg.coloredtrails.shared.types.GamePalette)
	 */
	@Override
	public void gamePaletteUpdated(GamePalette g) {

            System.out.println("GamePalette Updated");
            GameStatus gs = getGameStatus();
            gs.setGamePalette(g);

	}

	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.AgentClient#gameStarted()
	 */
	@Override
	public void gameStarted() {
		 ClientGameStatus gs = getGameStatus();

		System.out.println("Game Started: ");
		System.out.println("My player info: name[" + gs.getMyPlayer().getPin() + "," + gs.getMyPlayer().getPerGameId() + "] ");

	        
	        AllPlayersChipDisplay apcd = getTaskbar().getBoardWindow().getAllPlayersChipsDisplay();
                if(apcd != null)
                {
	        	gs.addObserver(apcd);
                        for(PlayerStatus ps : gs.getPlayers())
                            ps.addObserver(apcd);
                }
	        	
	        
	        
	       gs.addObserver(getTaskbar());
	       (getTaskbar().getBoardWindow()).getBoardPanel().setGame(gs);
	       
	        WaitingForPlayersScreen.getInstance().stopWaiting();



	}

	private Taskbar getTaskbar() {

		return Taskbar.getInstance();
	}

	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.AgentClient#logUpdated(edu.harvard.eecs.airg.coloredtrails.shared.types.HistoryLog)
	 */
	@Override
	public void logUpdated(HistoryLog hl) {

            System.out.println("Log Updated");
            try {
                ClientGameStatus gs = getGameStatus();
                gs.setHistoryLog(hl);
            } catch (Exception e) {
                e.printStackTrace();
            }

	}

	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.AgentClient#phaseAdvanced(edu.harvard.eecs.airg.coloredtrails.shared.types.Phases)
	 */
	@Override
	public void phaseAdvanced(Phases ph) {
		
            System.out.println("Phase Advanced");
            try {
                ClientGameStatus gs = getGameStatus();
                //if (gs.getPhases() != null)
                //  gs.getPhases().stopPhase();  // is there a way to dispose of these resources?
                gs.setPhases(ph);
                //ph.advancePhase();//.startPhase();
                gs.notifyObserversPhaseAdvanced();
            } catch (Exception e) {
                e.printStackTrace();
            }

	
	}

	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.AgentClient#phasesUpdated(edu.harvard.eecs.airg.coloredtrails.shared.types.Phases)
	 */
	@Override
	public void phasesUpdated(Phases ph) {
            //System.out.println("Phases Updated");

            ClientGameStatus gs = getGameStatus();
            gs.setPhases(ph);
            //ph.resetTimer();
            //phaseAdvanced(ph);
	}

	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.AgentClient#playersUpdated(java.util.ArrayList)
	 */
	@Override
	public void playersUpdated(Set<PlayerStatus> ps) {

            //System.out.println("Players Updated");
            //PlayerStatus MyPlayer = getGameStatus().getMyPlayer();        
			//System.out.println("-------------Player role: " + MyPlayer.getRole() + "-------------");

            try {
                getGameStatus().setPlayers(ps);
            } catch (Exception e) {
                e.printStackTrace();
            }


	}
	
	/*
	 * method invoked after registration has been completed
	 * 
	 */
	
	public void connected() {
		
		System.out.println("Connected to server. Waiting...");
	
	}

	public void onReceipt(DiscourseMessage dm) {

		
		System.out.println("Received a Discourse Message from the server:" + dm);
		
	}
        
    @Override
    public Serializable responserequired(String command, Serializable data){
        System.out.println("GUI response required");
        return(getTaskbar().getBoardWindow().responseRequired(command, data));
    }
        

}
