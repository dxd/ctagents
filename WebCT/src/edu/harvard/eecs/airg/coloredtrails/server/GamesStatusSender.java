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

package edu.harvard.eecs.airg.coloredtrails.server;

import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;

import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import java.util.Observable;
import java.util.Observer;

import java.util.Set;
import org.apache.log4j.Logger;


/**
	<b>Description</b>
	This class observes all games in progress, receives update 
	messages (via update() method) from players, boards, phases, 
	etc., when something has changed or happend, and then 
	relays/broadcasts update messages (via
	server.ServerData.addPendingUpdateMessage()) to players in
	the effected game.

	<p>
	
	<b>Observes</b>
	GamesStatusSender observes ServerGameStatus, though the 
	registration is done in server.ServerData.addNewGame()
 
 *
 * @author Paul Heymann (ct3@heymann.be)
	@author Sevan G. Ficici (additional review and comments)
 */
public class GamesStatusSender  implements Observer
{
	private static Logger log = Logger.getRootLogger();
	
    /*
     * TODO: this needs to be rewritten as a proper event dispatcher/listener interface
     */
    public void update(Observable o, Object arg)
    {
        String changeCode = (String) arg;
        GameStatus game = (GameStatus) o;
        ServerData sd = ServerData.getInstance();
        int gameId = game.getGameId();
        Set<PlayerStatus> players = game.getPlayers();
        
        ClientCommands handler = sd.getClientCommands(game.getGameId());//clientHandler;

        if (changeCode.equals("GAME_START"))
        {
        	handler.sendGameStartMessage(game.getPlayers(), game);
            if (sd.ctrlHandler != null){
                //System.out.println("$$$$$$$$$$$$$$$$$" + game.toString());
                sd.ctrlHandler.sendGameStartMessage(game);
            }
        }
        else if (changeCode.equals("GAME_INITIALIZED"))
        {
        	handler.sendGameInitializedMessage();
        }
        else if (changeCode.equals("GAME_END"))
        {
        	handler.sendGameEndMessage();
                if (sd.ctrlHandler != null)
                    sd.ctrlHandler.sendGameEndedMessage(gameId, game);
        }
        else if (changeCode.equals("BOARD_CHANGED"))
        {
            for(PlayerStatus ps : players){
        	handler.sendGameBoardChangedMessage(game.getBoard(), ps.getPerGameId());
            }
        }
        else if (changeCode.equals("PHASES_CHANGED"))
        {
            for(PlayerStatus ps : players){
        	handler.sendGamePhasesChangedMessage(game.getPhases(), ps.getPerGameId());
            }
        }
        else if (changeCode.equals("PLAYER_CHANGED"))
        {
            for(PlayerStatus ps : players){
        	handler.sendGamePlayersChangedMessage(game.getPlayers(), ps.getPerGameId());
            }
        }
        else if (changeCode.equals("PHASE_ADVANCED"))
        {
            for(PlayerStatus ps : players){
        	handler.sendGamePhaseAdvancedMessage(game.getPhases(), ps.getPerGameId());
            }
            if (sd.ctrlHandler != null)
                sd.ctrlHandler.sendPhaseAdvancedMessage(gameId, game.getPhases());
        }
        else if (changeCode.equals("LOG_CHANGED"))
        {
        	//handler.sendGameLogChangedMessage(game.getHistoryLog());
        }
        else if (changeCode.equals("GAME_PALETTE_CHANGE"))
        {
            for(PlayerStatus ps : players){
        	handler.sendGamePaletteChangedMessage(game.getGamePalette(), ps.getPerGameId());
            }
        }
        else if (changeCode.equals("SCORING_CHANGED"))
        {
            System.out.println( "---- Sending Scoring Change ----");
            handler.sendScoringChangedMessage(game.getScoring());
        }
        else
        {
            log.warn("Unknown game status change occurred. " + changeCode );
        }
    }

}
