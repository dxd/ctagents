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

package ctgui.original;


import edu.harvard.eecs.airg.coloredtrails.agent.events.DiscourseMessageEventListener;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import java.util.logging.Level;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import edu.harvard.eecs.airg.coloredtrails.server.ColoredTrailsServer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Vector;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.TransferDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.GlobalColorMap;
import javax.swing.BorderFactory;

/**
 *@author Meir Kalech (8/10/2008)
 *@author Bart Kamphorst (Bart.Kamphorst@phil.uu.nl) (02/25/09)
 * Modified the ActionHistoryWindow to be uneditable and unclosable by the user.
 * Removed the Phase reference too.
 * Fixed the issue that only the responder would receive the proper response in the
 * GUI window. (By implementing the DiscourseMessageEventListener.)
 */

public class ActionHistory extends JPanel implements DiscourseMessageEventListener
{
	private Logger 		log = Logger.getRootLogger();
	DefaultTableModel 	model;
	JTable 				table;
	String              playerID;
    private boolean     firstTime;

	public ActionHistory(String perGameId) {
		super(new GridLayout(1,0));
		playerID         = perGameId;
		table            = null;
        firstTime        = true;
    }

	public String getPlayerID() {
		return playerID;
	}
    
	public void setPlayerID(String playerID) {
		this.playerID = playerID;
	}

	public void presentData(ArrayList<ObjectMessage> MessagesDB,String perGameId) {
        for(ObjectMessage om : MessagesDB) {
        	update(om);
        }
    }

    private Vector<Vector<Object>> parsingObject(DiscourseMessage dm) {
        Vector<Vector<Object>> rows = new Vector<Vector<Object>>();
        if (dm.getMsgType().equals("basicproposaldiscussion")) {
            BasicProposalDiscussionDiscourseMessage bpddm = (BasicProposalDiscussionDiscourseMessage) dm;
            try {
                Vector<Object> row1 = new Vector<Object>();
                String proposer_id = Integer.toString(bpddm.getProposerID());
                String responder_id = Integer.toString(bpddm.getResponderID());

                // row1.addElement(ColoredTrailsServer.DISCOURSEMESSAGE);
                row1.addElement(proposer_id);
                row1.addElement(responder_id);
                row1.addElement(bpddm.getChipsSentByProposer());
                row1.addElement(bpddm.getChipsSentByResponder());

                boolean accept = bpddm.accepted();

                if(accept) {
                    // log.debug("[ACH] - actionHistory: Discourse Message accept");
                    row1.addElement("accept");
                }
                else {
                    // log.debug("[ACH] - actionHistory: Discourse Message reject");
                    row1.addElement("reject");
                }
                        rows.addElement(row1);
            } catch (Exception e) {
                log.error("[ACH] Failed in parsingObject method.");
            }
        }
        else if (dm.getMsgType().equals("transferrequest")) {
            TransferDiscourseMessage tdm = (TransferDiscourseMessage)dm;
            try {
                Vector<Object> row2 = new Vector<Object>();
                // String receiver = obj.getStringProperty(ColoredTrailsServer.PLAYER_TO_PROPOSE);
                String transferor = Integer.toString(dm.getFromPerGameId() );
                // ChipSet csTransferor = (ChipSet) obj.getObject();

                // row2.addElement(ColoredTrailsServer.TRANSFERREQUEST);
                row2.addElement(transferor);
                row2.addElement(playerID);
                row2.addElement(new ChipSet());

                
                row2.addElement(tdm.getTransferredChips());
                row2.addElement("transfer");

                rows.addElement(row2);

	            } catch (Exception e) {
	                log.error("Exception trying to get Discourse message");
	            }
        }
        
        return rows;
    }

    private Vector<Vector<Object>> parsingObject(ObjectMessage msg) {

        Vector<Vector<Object>> rows = new Vector<Vector<Object>>();
        String cmd = "";
    	if(msg instanceof ObjectMessage) {
	        try {
	            cmd = msg.getStringProperty(ColoredTrailsServer.GAMECOMMAND);
                //log.debug("[ACH] actionHistory " + cmd.toString());
	        } catch (JMSException e) {
	                log.warn("Error trying to get the game object type from the message");
	        }
	
	    	if (cmd.equals(ColoredTrailsServer.DISCOURSEMESSAGE)) {
	    		//log.debug("[ACH] - actionHistory: Discourse Message  Event");
	            ObjectMessage obj = (ObjectMessage)msg;

                DiscourseMessage dm = null;
                try {
                    dm = (DiscourseMessage) obj.getObject();
                } catch (JMSException ex) {
                    java.util.logging.Logger.getLogger(ActionHistory.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (dm.getMsgType().equals("basicproposaldiscussion")) {
                    BasicProposalDiscourseMessage bpdm = null;
                    try {
                        Vector<Object> row1 = new Vector<Object>();
                        bpdm = (BasicProposalDiscourseMessage) obj.getObject();
                        String proposer_id = Integer.toString(bpdm.getProposerID());
                        String responder_id = Integer.toString(bpdm.getResponderID());

                        // row1.addElement(ColoredTrailsServer.DISCOURSEMESSAGE);
                        row1.addElement(proposer_id);
                        row1.addElement(responder_id);
                        row1.addElement(bpdm.getChipsSentByProposer());
                        row1.addElement(bpdm.getChipsSentByResponder());

                        if(dm instanceof BasicProposalDiscussionDiscourseMessage) {
                            boolean accept = ((BasicProposalDiscussionDiscourseMessage)dm).accepted();

                            if(accept) {
                                // log.debug("[ACH] - actionHistory: Discourse Message accept");
                                row1.addElement("accept");
                            }
                            else {
                                // log.debug("[ACH] - actionHistory: Discourse Message reject");
                                row1.addElement("reject");
                            }
                        }
                        else {
                        // log.debug("[ACH] - no instanceof BasicProposalDiscussionDiscourseMessage");
                        row1.addElement("");
                        }

                        rows.addElement(row1);
                    } catch (JMSException e) {
                        log.error("Exception trying to get Discourse message");
                    }
                }
	    	}
	    	else if(cmd.equals(ColoredTrailsServer.TRANSFERREQUEST)) {
	    	    //log.debug("[ACH] - actionHistory: Transfer Message  Event");
	            ObjectMessage obj = (ObjectMessage)msg;
	            
	            try {
	            	Vector<Object> row2 = new Vector<Object>();
		            String receiver = obj.getStringProperty(ColoredTrailsServer.PLAYER_TO_PROPOSE);
	                String transferor = playerID;
	                ChipSet csTransferor = (ChipSet) obj.getObject();
                    
	                row2.addElement(transferor);
	                row2.addElement(receiver);
	                row2.addElement(csTransferor);
                    row2.addElement(new ChipSet());
                    row2.addElement("transfer");

                    rows.addElement(row2);
	                
	            } catch (JMSException e) {
	                log.error("Exception trying to get Discourse message");
	            }
	    	}
        }
    	return rows;
    }
    /**
     * Build up the initial Swing components needed for the ActionHistory window.
     */
    private void Initialize() {
    	Vector<String> columnNames = new Vector<String>();
        
        columnNames.addElement("Proposer");
        columnNames.addElement("Responder");
        columnNames.addElement("Given Chips");
        columnNames.addElement("Received Chips");
        columnNames.addElement("Response");
        
        
        Vector<Vector> data = new Vector<Vector>();
        
        model = new DefaultTableModel(data, columnNames) {
            @Override
	        public boolean isCellEditable(int row, int col){
            return false;
            }
        };
        table = new JTable(model);
        table.getColumnModel().getColumn(0).setCellRenderer(new iconRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new iconRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new chipsRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new chipsRenderer());
        // Set RowHeight to 20 to account for the borders in the embedded JTable.
        table.setRowHeight(20);
        
        table.setPreferredScrollableViewportSize(new Dimension(700, 60));
        table.setGridColor(Color.BLACK);
        TableColumn column = null;
        for (int i = 0; i < 5; i++) {
            column = table.getColumnModel().getColumn(i);
            if (i == 2 || i==3) {
                column.setPreferredWidth(150); //columns with chipsets are bigger
            } else {
                column.setPreferredWidth(100);
            }
        }
        TableCellRenderer tcr = table.getDefaultRenderer(String.class);
        DefaultTableCellRenderer dtcr = (DefaultTableCellRenderer)tcr;
        dtcr.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        //table.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(table);

        //Add the scroll pane to this panel.
        add(scrollPane);
    }

    /**
     * Register each ActionHistory window once as a DiscourseMessageEventListener.
     */
    public void register() {
        if (firstTime) {
            GUI.getAgent().addDiscourseMessageEventListener(this);
            // log.debug("[ACH] register(): Registered agent: " + GUI.getAgent().getPin());
            firstTime = false;
        }
    }

    /**
     * Update the ActionHistory window when the message is a
     * BasicProposalDiscussionDiscourseMessage.
     * @param dm
     */
    private void update(DiscourseMessage dm) {

        if (dm.getMsgType().equals("basicproposaldiscussion")) {
            BasicProposalDiscussionDiscourseMessage bpddm = (BasicProposalDiscussionDiscourseMessage) dm;

            if(table == null) {

                Initialize();
                //Create and set up the window.
                Vector<Vector<Object>> rows = parsingObject(bpddm);
                for(Vector<Object> row: rows) {
                    model.insertRow(0, row);
                    JFrame frame = new JFrame("Action History - Name " + playerID );
                    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

                    //content panes must be opaque
                    setOpaque(true);
                    frame.setContentPane(this);

                    //Display the window.
                    frame.pack();
                    frame.setVisible(true);
                    }
            }
            else {
                Vector<Vector<Object>> rows = parsingObject(bpddm);
                for(Vector<Object> row: rows) {
                    model.insertRow(0, row);
                    model.fireTableDataChanged();
                    table.repaint();
                }
            }
        }
        else if (dm.getMsgType().equals("transferrequest")) {
            
            if(table == null) {

                Initialize();
                //Create and set up the window.
                Vector<Vector<Object>> rows = parsingObject(dm);
                for(Vector<Object> row: rows) {
                    model.insertRow(0, row);
                    JFrame frame = new JFrame("Action History - Name " + playerID );
                    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

                    //content panes must be opaque
                    setOpaque(true);
                    frame.setContentPane(this);

                    //Display the window.
                    frame.pack();
                    frame.setVisible(true);
                    }
            }
            else {
                Vector<Vector<Object>> rows = parsingObject(dm);
                for(Vector<Object> row: rows) {
                    model.insertRow(0, row);
                    model.fireTableDataChanged();
                    table.repaint();
                }
            }
        }

    }

    public void update(ObjectMessage om) {
        // If the first update request comes in, add this
        // class as a DiscourseMessageEventListener
        // register();

        String cmd = "";
        try {
	            cmd = om.getStringProperty(ColoredTrailsServer.GAMECOMMAND);
                //log.debug("[ACH] actionHistory " + cmd.toString());
	        } catch (JMSException e) {
	                log.warn("Error trying to get the game object type from the message");
	        }

	    	if (cmd.equals(ColoredTrailsServer.DISCOURSEMESSAGE)) {
	    		//log.debug("[ACH] - actionHistory: Discourse Message  Event");
	            ObjectMessage obj = (ObjectMessage)om;
                
	            DiscourseMessage dm = null;
                try {
	                dm = (DiscourseMessage) obj.getObject();
                } catch (JMSException e) {}

                // Only build up the ActionHistoryWindow if the ObjectMessage
                // concerns an accept or a reject message.
                if(dm instanceof BasicProposalDiscussionDiscourseMessage) {
                    update(dm);
                }
                else if ( ! (dm instanceof BasicProposalDiscussionDiscourseMessage) &&
                        ( ! (dm instanceof BasicProposalDiscourseMessage)) ) {
                    
                    if(table == null) {

                        Initialize();
                        //Create and set up the window.
                        Vector<Vector<Object>> rows = parsingObject(om);
                        for(Vector<Object> row: rows)
                            model.insertRow(0, row);
                        JFrame frame = new JFrame("Action History - Name " + playerID );
                        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

                        //content panes must be opaque
                        setOpaque(true);
                        frame.setContentPane(this);

                        //Display the window.
                        frame.pack();
                        frame.setVisible(true);
                    }
                    else {
                        Vector<Vector<Object>> rows = parsingObject(om);
                        for(Vector<Object> row: rows)
                            model.insertRow(0, row);
                            model.fireTableDataChanged();
                            table.repaint();
                        }
                 }
            }
            else if (cmd.equals(ColoredTrailsServer.TRANSFERREQUEST)) {
                ObjectMessage obj = (ObjectMessage)om;
                ChipSet cs = null;

	            try {
	                cs = (ChipSet) obj.getObject();
                } catch (JMSException e) {}
                
                if (! cs.isEmpty() ) {

                    if(table == null) {

                        Initialize();
                        //Create and set up the window.
                        Vector<Vector<Object>> rows = parsingObject(om);
                        for(Vector<Object> row: rows)
                            model.insertRow(0, row);
                        JFrame frame = new JFrame("Action History - Name " + playerID );
                        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

                        //content panes must be opaque
                        setOpaque(true);
                        frame.setContentPane(this);

                        //Display the window.
                        frame.pack();
                        frame.setVisible(true);
                    }
                    else {
                        Vector<Vector<Object>> rows = parsingObject(om);
                        for(Vector<Object> row: rows)
                            model.insertRow(0, row);
                            model.fireTableDataChanged();
                            table.repaint();
                        }
                }

            }
    }

    public void onReceipt(DiscourseMessage dm) {
        if (dm.getMsgType().equals("basicproposaldiscussion")) {
            BasicProposalDiscussionDiscourseMessage bpddm = (BasicProposalDiscussionDiscourseMessage)dm;

            // Only show the history of actions you have played a part in.
            if ( (bpddm.getProposerID() == GUI.getAgent().getPerGameID() ) ||
                    bpddm.getResponderID() == GUI.getAgent().getPerGameID() ) {
                
                update(bpddm);
            }
        }
        else if (dm.getMsgType().equals("transferrequest")) {
            update(dm);
        }
    }
    
    /** the following class for the icons */
    public class iconRenderer extends DefaultTableCellRenderer {
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object obj,boolean isSelected, boolean hasFocus, int row, int column) {
			String id = (String)obj;
			
			setHorizontalAlignment(CENTER);
			setIcon(Icons.getIconByPerGameId(Integer.valueOf(id)));

            return this;
        }
    }
    /** the following class for the chips display */
    public class chipsRenderer extends DefaultTableCellRenderer {
        
        @Override
        public Component getTableCellRendererComponent(	JTable table, 
        												Object obj,
        												boolean isSelected, 
        												boolean hasFocus, 
        												int row, int column) {

        	final ChipSet chips = (ChipSet)obj;
        	if(chips != null) {
	        	/* We create the table that will hold the multivalue
	             *fields and that will be embedded in the main table */
	            JTable embedded = new JTable(
	                new AbstractTableModel() {
	                    public int getColumnCount() {
	                    	//log.debug("[ACH] - getColumnCount. Size=" + chips.getColors().size()+ ". " + chips.toString());
		                	return chips.getColors().size();
	                    }
	                    public int getRowCount() {
	                        return 1;
	                    }
	                    public Object getValueAt(int rowIndex, int columnIndex) {
	                    	
	                    	return new Chip((String)chips.getColors().toArray()[columnIndex],chips.getNumChips((String)chips.getColors().toArray()[columnIndex]));
	                    }
                        @Override
	                    public boolean isCellEditable(int row, int col){ 
                        return false;
                    }
	            });
	            for (int i = 0; i < chips.getColors().size() ; i++) {
	            	embedded.getColumnModel().getColumn(i).setCellRenderer(new chipCellRenderer());
	            }
                embedded.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
	            embedded.setRowHeight(20);
                return embedded;
        	}
        	else {
        		return null;
            }
        }
    }

    public class chipCellRenderer extends DefaultTableCellRenderer {
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object obj,boolean isSelected, boolean hasFocus, int row, int column) {
        	
        	Chip chip = (Chip)obj;
        	
        	setBackground(GlobalColorMap.getColorByName(chip.color));
        	String id = Integer.toString(chip.num);
        	setHorizontalAlignment(CENTER);
			setText(id);
			return this;
        }

    }

    public class Chip {
    	public String color;
    	public int num;

    	Chip(String color, int num) {
    		this.color  = color;
    		this.num    = num;
    	}
    }
}
