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

package ctagents.recipagents;



import RecipExperiment.RecipConstants;
import java.util.ArrayList;
import java.util.Observable;

import edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsClientImpl;
import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.shared.ScoringUtility;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;

import java.util.Observer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketAppender;

/**
 * An agent class that is a proposer in a CT game
 * 
 * @author ilke
 */
public class RecipAgent implements RecipAgentAdaptor, Observer {
    
    protected Logger logr;

    
    protected ColoredTrailsClientImpl client;
    protected int OppPerGameId;
    protected int MyPerGameId;
    protected ClientGameStatus cgs = null;

    private static int msgId = 0;
    

    public RecipAgent() {
        client = new ColoredTrailsClientImpl(this.getClass().getCanonicalName());
        client.addDiscourseMessageEventListener(this);
        client.addPhasesAdvancedEventListener(this);
        client.addGameStartEventListener(this);
        client.addGameEndedEventListener(this);
        
		client.addGameInitializedEventListener(this);  // this was missing!
    }

    /**
     * Called when a game ends
     */
    public void gameEnded() {
        System.out.println("Game ended ");
        System.out.println("My PlayerStatus is: " + client.getGameStatus().getMyPlayer());
    }

    /**
     * Called when a game starts
     */
    public void gameStarted() {
        
        
        
        System.out.println("#########################Game started");
        cgs = client.getGameStatus();
        cgs.addObserver(this);
        logr.info("My id: " + cgs.getMyPlayer().getPerGameId() + " pin: " + cgs.getMyPlayer().getPin() + " game: " + cgs.getGameId() );
        MyPerGameId = cgs.getMyPlayer().getPerGameId();
        for(PlayerStatus ps : cgs.getPlayers()){
            if(ps.getPerGameId() == MyPerGameId)
                continue;
            else
                OppPerGameId = ps.getPerGameId();
        }
        //logr.info("MyPerGameId: " + MyPerGameId + " OppPerGameId " + OppPerGameId);
    }

    /**
     * Called by the server when the game configuration class' run() method completes
     */
    //NEVER CALLED
    public void gameInitialized()
    {
    }

    /**
     * Gets the client name
     */
    public String getClientName() {
            return client.getPin();
    }

    /**
    * Called when a discourse message is received
    * @param dm discourse message received
    */
    public void onReceipt(DiscourseMessage dm) {
        System.out.println("Received a " + dm.getClass() );
        // check if it is a basic proposal discourse message
        
        if(cgs.getMyPlayer().getRole().equals("Responder")){
            if(dm instanceof BasicProposalDiscourseMessage) {
                BasicProposalDiscourseMessage proposal = (BasicProposalDiscourseMessage) dm;

                BasicProposalDiscussionDiscourseMessage responseMessage = new BasicProposalDiscussionDiscourseMessage(proposal );
                
                ChipSet offer = ChipSet.subChipSets(proposal.getChipsSentByResponder(), proposal.getChipsSentByProposer());
                // check if the proposal is beneficial
                boolean offerResponse = RespondStrategy(cgs, offer, OppPerGameId, MyPerGameId);
                
                PhaseWaiter waiter = new PhaseWaiter(cgs.getPhases());
                waiter.doWait(RecipConstants.minRespondTime, RecipConstants.maxRespondTime);
                    
                // check if the proposal is beneficial
                if( offerResponse ) {
                    //response.setSubjectMsgId(subjectMsgId);
                    responseMessage.acceptOffer();
                } else {
                    //response.setSubjectMsgId(subjectMsgId);
                    responseMessage.rejectOffer();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                   logr.log(Level.FATAL, null, ex);
                }
                client.communication.sendDiscourseRequest(responseMessage);
            }
        } else if( cgs.getMyPlayer().getRole().equals("Proposer")){
            int ProposerId = MyPerGameId;
            int ResponderId = OppPerGameId;
            
            logr.info("Received response from client to my offer");
            logr.info("My id is: " + MyPerGameId + " " + cgs.getPerGameId() + " and opponent's is: " + OppPerGameId);
            logr.info("Message from: "  + dm.getFromPerGameId() + "  to: " + dm.getToPerGameId() + " of type : " + dm.getMsgType());
            logr.info("Message id: " + dm.getMessageId());
            logr.info("Game id: " + cgs.getGameId());
            BasicProposalDiscussionDiscourseMessage response;
            Boolean accepted;
            try{
                response = (BasicProposalDiscussionDiscourseMessage) dm;
                accepted = response.accepted();
            } catch(java.lang.ClassCastException ex){
                logr.log(Level.ERROR, "trying to cast", ex);
                return;
            }
            
            if(accepted)
                System.out.println("My offer has been accepted");
            else
                System.out.println("My offer has been rejected :(");
            
        }
    }

    /**
     * Called when a phase advances
     */
    public void phaseAdvanced(Phases ph) {
        //scoring = cgs.getScoring();
        String phaseName = cgs.getPhases().getCurrentPhaseName();
        

        if(phaseName.equals("Offer Phase")) {
            if(cgs.getMyPlayer().getRole().equals("Proposer"))
            {   
                System.out.println("I'm the proposer, wheeeeee");
                ArrayList<ChipSet> exchange = (ArrayList<ChipSet>) strategy(null);
                ChipSet senderChips;
                ChipSet recipientChips;

                if(exchange == null) {
                    System.out.println("No beneficial " +
                                        "exchanges found among the ones that are beneficial for the responder");
                }
                else {
                    System.out.println("EXCHANGE: " + exchange);
                    senderChips = exchange.get(0);
                    recipientChips = exchange.get(1);
                    BasicProposalDiscourseMessage proposal= new BasicProposalDiscourseMessage(
                                    MyPerGameId, OppPerGameId, -1, senderChips, recipientChips);


                    
                    proposal.setMessageId(msgId);
                    msgId++;
                    
                    PhaseWaiter waiter = new PhaseWaiter(cgs.getPhases());
                    waiter.doWait(RecipConstants.minProposeTime, RecipConstants.maxProposeTime);
                    
                    logr.info("Just about to send a proposal to: " + proposal.toString() + " msg id: " + proposal.getMessageId());
                    client.communication.sendDiscourseRequest(proposal);
                    

                }
            }       
        } else if(phaseName.equals("Movement Phase")){
           
        }    
    }
    
    
	
	/**
	 * Sets the client name
	 * @param name client name
	 */
	public void setClientName(String name) {
            client.setPin(name);
            logr = Logger.getLogger(this.getClass().getName() + client.getPin() );
            SocketAppender appender = new SocketAppender(RecipConstants.LOG_SERVER, 4445);
            logr.addAppender(appender);
            logr.setLevel(Level.INFO);
            logr.log(Level.INFO, "Startup");
	}

	/**
	 * Starts the client
	 */
	public void start() {
		client.start();
                System.out.println("agent type: " + this.getClass().getCanonicalName());
	}


	
    /**
     * Strategy of the proposer
     * @param o null
     * @return An exchange to propose
     */
    public Object strategy(Object o) {
        return(null);
    }
    

    //OFFER ALWAYS GIVEN AS (chips sent by recipient) - (chips sent by proposer)
    
    
    public Boolean RespondStrategy(GameStatus gs, ChipSet proposal, int ProposerId, int ResponderId) {
        return(false);
    }

    public void update(Observable o, Object arg) {
        String s;
        if(arg instanceof String){
            s = (String) arg;
            if(s.equals("NEWROUND")){
                System.out.println("We have a new round, resetting SU");
                int ResponderId, ProposerId;
                if(cgs.getMyPlayer().getRole().equals("Responder")){
                    ResponderId = MyPerGameId;
                    ProposerId = OppPerGameId;
                } else {
                    ResponderId = OppPerGameId;
                    ProposerId = MyPerGameId;
                }
                newRound(ProposerId, ResponderId);
            }
                
        }
    }
    
    public void newRound(int ProposerId, int ResponderId){
        
    }
        
}
