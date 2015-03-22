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

package ctgui.original.discoursehandlers;

import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import ctgui.original.ChipSetDisplayPanel;
import ctgui.original.GUI;
import ctgui.original.Icons;
import ctgui.original.Taskbar;

import edu.harvard.eecs.airg.coloredtrails.shared.Constants;
import java.util.Observable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observer;

/**
 * This window is shown when a BasicProposal is received by the client.
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public class BasicProposalDisplayWindow extends JFrame implements Observer {
    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel buttonPanel;
    private JButton acceptButton = new JButton("Accept");
    private JButton rejectButton = new JButton("Reject");
    private JButton retractButton = new JButton("Retract");
    private final BasicProposalDiscourseMessage discourseMessage;
    private JPanel proposerPanel;
    private JPanel proposerChips;
    private JPanel recipientChips;
    private JPanel recipientPanel;
    private JPanel statusPanel;
    private JLabel statusLabel = new JLabel();
    public static final int SENDER = 1;
    public static final int RECIPIENT = 2;
    public static final int AUTOOFFER = 3;

    public BasicProposalDisplayWindow(int role,
                                      final BasicProposalDiscourseMessage dm) {


        if (role == RECIPIENT) {
            setTitle("Pin " + GUI.getAgent().getPin()+" Received Proposal...");
        } else if (role == SENDER) {
            setTitle("Pin " + GUI.getAgent().getPin()+ " Sent Proposal...");
        } else if ( role == AUTOOFFER ) {
            setTitle( "Pin " + GUI.getAgent().getPin()+ " Automatically Sent Proposal..." );
        }
        
        GUI.getAgent().getGameStatus().addObserver(this);

        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        discourseMessage = dm.clone();
        statusLabel.setText("Pending Reply...");
        statusPanel.add(statusLabel);
        proposerChips.add(new ChipSetDisplayPanel(dm.getChipsSentByProposer()));
        recipientChips.add(
                new ChipSetDisplayPanel(dm.getChipsSentByResponder()));
        proposerPanel.add(new JLabel(
                Icons.getIconByPerGameId(dm.getProposerID())));
        recipientPanel.add(
                new JLabel(Icons.getIconByPerGameId(dm.getResponderID())));

        if (role == SENDER) {
            /*
             * if this were a non-basic proposal,
             * the retract button might be added here.
             */
        } else if (role == RECIPIENT) {
            buttonPanel.add(acceptButton);
            buttonPanel.add(rejectButton);
        }

        acceptButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                int proposerID = discourseMessage.getProposerID();
                int responderID = discourseMessage.getResponderID();
                int subjectMsgId = discourseMessage.getMessageId();

                BasicProposalDiscussionDiscourseMessage accept =
                        new BasicProposalDiscussionDiscourseMessage(dm);
                accept.acceptOffer();

                Taskbar.getInstance().getAgent().communication.sendDiscourseRequest(accept);
                acceptButton.setEnabled(false);
                rejectButton.setEnabled(false);
                setProposalStatus("Accepted Proposal.");
                repaint();
            }
        });

        rejectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int toPerGameId = discourseMessage.getProposerID();
                int fromPerGameId = discourseMessage.getResponderID();
                int subjectMsgId = discourseMessage.getMessageId();

                BasicProposalDiscussionDiscourseMessage reject =
                        new BasicProposalDiscussionDiscourseMessage(dm);
                reject.rejectOffer();

                Taskbar.getInstance().getAgent().communication.sendDiscourseRequest(reject);

                acceptButton.setEnabled(false);
                rejectButton.setEnabled(false);
                setProposalStatus("Rejected Proposal.");
                repaint();
            }
        });
	if(role == RECIPIENT) {
            this.setLocation(690,240);
        } else {
            this.setLocation(690,0);
        }
        pack();
    }

    public void setProposalStatus(String status) {
        statusLabel.setText(status);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// !!! IMPORTANT !!!
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * !!! IMPORTANT !!!
     * DO NOT edit this method OR call it in your code!
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0,
                0, 0), -1,
                -1));
        topPanel = new JPanel();
        topPanel.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0,
                0), -1,
                -1));
        mainPanel.add(topPanel,
                new GridConstraints(0, 0, 1, 1,
                        GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null));
        proposerPanel = new JPanel();
        topPanel.add(proposerPanel,
                new GridConstraints(1, 0, 1, 1,
                        GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null));
        proposerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Proposer"));
        proposerChips = new JPanel();
        topPanel.add(proposerChips,
                new GridConstraints(2, 0, 1, 1,
                        GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null));
        proposerChips.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Proposer Chips"));
        recipientPanel = new JPanel();
        topPanel.add(recipientPanel,
                new GridConstraints(3, 0, 1, 1,
                        GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null));
        recipientPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Recipient"));
        recipientChips = new JPanel();
        topPanel.add(recipientChips,
                new GridConstraints(4, 0, 1, 1,
                        GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null));
        recipientChips.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Recipient Chips"));
        statusPanel = new JPanel();
        topPanel.add(statusPanel,
                new GridConstraints(0, 0, 1, 1,
                        GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null));
        statusPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Status"));
        buttonPanel = new JPanel();
        mainPanel.add(buttonPanel,
                new GridConstraints(1, 0, 1, 1,
                        GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null));
    }

    public void update(Observable o, Object arg) {
        String s;
        if(arg instanceof String){
            s = (String) arg;
            if(s.equals("NEWROUND") || s.equals("GAME_END"))
                setVisible(false);
            if( s.equals(Constants.AUTORESPONSE) ) {
//                rejectButton.
                acceptButton.setEnabled(false);
                rejectButton.setEnabled(false);
                setProposalStatus("Automatically Rejected Proposal");
                repaint();
            }
        }
        
    }
}
