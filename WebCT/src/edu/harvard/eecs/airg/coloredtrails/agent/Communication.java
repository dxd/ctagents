/*
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

package edu.harvard.eecs.airg.coloredtrails.agent;

import javax.jms.*;

import ctgui.original.ActionHistory;

import edu.harvard.eecs.airg.coloredtrails.client.ui.discourse.DiscourseHandler;
import edu.harvard.eecs.airg.coloredtrails.server.ColoredTrailsServer;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.TransferDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameConfigDetailsRunnable;
import edu.harvard.eecs.airg.coloredtrails.shared.types.RowCol;

import java.util.ArrayList;
import java.util.Hashtable;
import org.apache.log4j.Logger;

public final class Communication {

	private MessageProducer producer;
	private Session session;
	private String clientName;
	private ArrayList<DiscourseHandler> discourseHandlers = new ArrayList<DiscourseHandler>();
    private Hashtable<String, Object> discourseHandlerData =new Hashtable<String, Object>();
	private String perGameId;

    //this array contains the history of actions of agents (propose, respond, move)
    //	private ArrayList<ObjectMessage> MessagesDB = new ArrayList<ObjectMessage>();
	private ActionHistory myTable;
	private Logger log = Logger.getRootLogger();
	private boolean gui = false;

	public Communication()
    {
         myTable = new ActionHistory(perGameId);
	}
	
  public Communication(Session session, MessageProducer producer, String clientName, String perGameId)
  {
		this.session = session;
		this.producer = producer;
		this.clientName = clientName;
		this.perGameId = perGameId;

         myTable = new ActionHistory(perGameId);
  }

  /**
    * Get the discourse handler data associated with a particular
    * handler name---used by handlers to store persistent data.
    *
    * @param handlerName The name of the handler storing data.
    * @return Whatever data is associated with the handler, or null
    *         if no data is associated.
    */
  public Object getDiscourseHandlerData(String handlerName)
  {
    return discourseHandlerData.get(handlerName);
  }

    /**
     * Set the discourse handler data associated with a particular
     * handler name---used by handlers to store persistent data.
     *
     * @param handlerName The name of the handler storing data.
     * @param o           The data to be stored by the handler.
     */
    public void setDiscourseHandlerData(String handlerName, Object o) {
        discourseHandlerData.put(handlerName, o);
    }
    
  //
  public Hashtable<String,Object> getDHD() { return discourseHandlerData; }
    
    
	
	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#getDiscourseHandlers()
	 */
	public final ArrayList<DiscourseHandler> getDiscourseHandlers() {
	    return discourseHandlers;
	}

	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#setDiscourseHandlers(java.util.ArrayList)
	 */
	public final void setDiscourseHandlers(ArrayList<DiscourseHandler> discourseHandlers) {
		this.discourseHandlers = discourseHandlers;
        gui = true;
	}

    /* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#addDiscourseHandler(edu.harvard.eecs.airg.coloredtrails.client.ui.discourse.DiscourseHandler)
	 */
	public final boolean addDiscourseHandler(DiscourseHandler dh) {
		return this.discourseHandlers.add(dh);
	}
  
	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#removeDiscourseHandler(edu.harvard.eecs.airg.coloredtrails.client.ui.discourse.DiscourseHandler)
	 */
	public final boolean removeDiscourseHandler(DiscourseHandler dh) {
		return this.discourseHandlers.remove(dh);
	}
    
	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#clearDiscourseHandler()
	 */
	public final void clearDiscourseHandler() {
		this.discourseHandlers.clear();
	}
	
	/* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#sendMoveRequest(edu.harvard.eecs.airg.coloredtrails.shared.types.RowCol)
	 */
	public boolean sendMoveRequest(RowCol newpos)
  {
		try
    {
			ObjectMessage movereq = session.createObjectMessage();
			movereq.setObject(newpos);
			movereq.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.SERVER_MSG);
			movereq.setStringProperty(ColoredTrailsServer.GAMECOMMAND, ColoredTrailsServer.MOVEREQUEST);
			movereq.setStringProperty(ColoredTrailsServer.CLIENT_NAME, clientName );
			movereq.setStringProperty(ColoredTrailsServer.PERGAMEID, perGameId);
      			
			producer.send(movereq);
		}
    catch (JMSException e)
    {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    return true;
	}
		
  /* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#sendDiscourseRequest(edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage)
	 */
	public boolean sendDiscourseRequest(DiscourseMessage dm)
  {
		try
    {
			ObjectMessage req = session.createObjectMessage();
			req.setObject(dm);
			req.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.SERVER_MSG);
			req.setStringProperty(ColoredTrailsServer.GAMECOMMAND, ColoredTrailsServer.DISCOURSEREQUEST);
			req.setStringProperty(ColoredTrailsServer.CLIENT_NAME, clientName);
			req.setStringProperty(ColoredTrailsServer.PERGAMEID, Integer.toString(dm.getFromPerGameId()));

            if(gui == true && GameConfigDetailsRunnable.historyWindowIsVisible() )
			{
				//log.debug("[COM] communication discourse " + req.toString());
                // Update the ActionHistoryWindow
                myTable.register();
                myTable.update(req);
			}
            
      producer.send(req);
      
      // invoke onSend methods on appropriate discourse handler(s)
      for (DiscourseHandler dh : discourseHandlers)
        if (dh.getType().equals(dm.getMsgType()))
          dh.onSend(dm);
    }
    catch (JMSException e)
    {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
    
		return true;
	 }

//    public void sendHeartBeat(){
//        try{
//           TextMessage heartbeat = session.createTextMessage();
//           heartbeat.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.SERVER_MSG);
//           heartbeat.setStringProperty(ColoredTrailsServer.CLIENT_NAME, clientName);
//           heartbeat.setStringProperty(ColoredTrailsServer.COMMAND, ColoredTrailsServer.HEARTBEAT);
//           producer.send(heartbeat);
//        }
//        catch (JMSException e){
//           System.exit(-1);
//        }    
//    }
	 
  /* (non-Javadoc)
	 * @see edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsAgentInterface#sendTransferRequest(int, edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet)
	 */
    public boolean sendTransferRequest(int toPerGameId, ChipSet myChips) {
		try {
			ObjectMessage req = session.createObjectMessage();
			req.setObject(myChips);
			req.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.SERVER_MSG);
			req.setStringProperty(ColoredTrailsServer.GAMECOMMAND, ColoredTrailsServer.TRANSFERREQUEST);
			req.setStringProperty(ColoredTrailsServer.PLAYER_TO_PROPOSE, Integer.toString(toPerGameId));
			req.setStringProperty(ColoredTrailsServer.PERGAMEID, perGameId);
			req.setStringProperty(ColoredTrailsServer.CLIENT_NAME, clientName);

            if(gui == true && GameConfigDetailsRunnable.historyWindowIsVisible() )
			{
				//log.debug("[COM] communication transfer " + req.toString());
                
                // Update the ActionHistoryWindow
				myTable.update(req);
                myTable.register();
                // Send out a transferDiscourse Message
                TransferDiscourseMessage tdm = new TransferDiscourseMessage(Integer.parseInt(perGameId), toPerGameId, -1, myChips);
                sendDiscourseRequest(tdm);
			}

			producer.send(req);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
    }



	public String getClientName() {
		return clientName;
	}



	public void setClientName(String clientName) {
		this.clientName = clientName;
	}



	public MessageProducer getProducer() {
		return producer;
	}



	public void setProducer(MessageProducer producer) {
		this.producer = producer;
	}



	public Session getSession() {
		return session;
	}



	public void setSession(Session session) {
		this.session = session;
	}



	public String getPerGameId() {
		return perGameId;
	}



	public void setPerGameId(String perGameId) {
		this.perGameId = perGameId;
        if ( GameConfigDetailsRunnable.historyWindowIsVisible() ) {
            myTable.setPlayerID(perGameId);
        }
	}
}