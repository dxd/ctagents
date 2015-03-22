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


import edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsClientImpl;
import edu.harvard.eecs.airg.coloredtrails.client.ui.ColoredTrailsBoardWindow;
import edu.harvard.eecs.airg.coloredtrails.client.ui.ColoredTrailsTaskbar;
import edu.harvard.eecs.airg.coloredtrails.client.ui.discourse.DiscourseHandler;
import edu.harvard.eecs.airg.coloredtrails.client.ui.discourse.GraphicalDiscourseHandler;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import ctgui.original.discoursehandlers.BasicProposalDiscourseHandler;
import ctgui.original.discoursehandlers.BasicProposalDisplayWindow;
import edu.harvard.eecs.airg.coloredtrails.shared.Constants;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Observer;
import java.util.Observable;

/**
 * Graphical taskbar for UI, displaying game options.
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public class Taskbar extends JFrame implements Observer, ColoredTrailsTaskbar {
    private JPanel mainPanel;
    private JButton transferButton = new JButton("Transfer");
    private JButton connectButton = new JButton("Connect...");
    private ConnectDialog connectDialog = new ConnectDialog();
    private BoardWindow boardWindow = new BoardWindow();
    private static Taskbar INSTANCE = null;
    private JPanel buttonPanel;
    private MakeTransferWindow transferWindow = null;
    private ColoredTrailsClientImpl agent;
    private PathFinderWindow pathWindow = null;
    
    private ArrayList<JButton> discourseButtons =
            new ArrayList<JButton>();

    //synchronized creator to defend against multi-threading issues
    //another if check here to avoid multiple instantiation
    private  static void createInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Taskbar();
        }
    }

    public static Taskbar getInstance() {
        if (INSTANCE == null) createInstance();
        return INSTANCE;
    }

    private Taskbar() {
        setTitle("Colored Trails Taskbar - Name: "+ GUI.getAgent().getPin());
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                connectDialog.setVisible(true);
                                          
            }
        });

        transferButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (transferWindow == null) {
                    transferWindow = new MakeTransferWindow();
                }
                transferWindow.reset();
                transferWindow.setVisible(true);
            }
        });


        buttonPanel.add(connectButton);
       
        for (DiscourseHandler dh : GUI.getDiscourseHandlers())         	
        {
        	
        	if(dh instanceof GraphicalDiscourseHandler) {

	        	if (((GraphicalDiscourseHandler)dh).inTaskbar()) {
	                JButton jb = new JButton(((GraphicalDiscourseHandler)dh).getTaskbarName());
	                jb.addActionListener(((GraphicalDiscourseHandler)dh).getDiscourseActionListener());
	                buttonPanel.add(jb);
	                discourseButtons.add(jb);
	            }
    	}
        }
       
        buttonPanel.add(transferButton);
        updateButtonState();
    }

    /* (non-Javadoc)
	 * @see ctgui.original.CTTaskbar#showTaskbar()
	 */
    public void showTaskbar() {
        this.setLocation(300,0);
        pack();
        setVisible(true);
        
        
    }

    /* (non-Javadoc)
	 * @see ctgui.original.CTTaskbar#updateButtonState()
	 */
    public void updateButtonState() {
    	boolean connected = GUI.getAgent().isConnected();
    	
    	
        if (connected == true) {
        	
        	  if (pathWindow == null &&
                      !GUI.getAgent().getGameStatus().getBoard().getGoals().isEmpty()) {
                    pathWindow = new PathFinderWindow();
                    pathWindow.setVisible(true);
              }
              
              
            connectButton.setEnabled(false);
            // monira - check if transfer allowed
            if (GUI.getAgent().areTransfersAllowed() == true) {
                transferButton.setEnabled(true);
            }
            else {
                transferButton.setEnabled(false);
            }


            
            for (JButton button : discourseButtons) {
                if (GUI.getAgent().isCommunicationAllowed()) {
                   button.setEnabled(true);
                }
                else {
                   button.setEnabled(false);
                }
            }
            
            
        } else if (connected == false) {
            connectButton.setEnabled(true);
            transferButton.setEnabled(false);
            for (JButton button : discourseButtons) {
                button.setEnabled(false);
            }
        }

    }

    /* (non-Javadoc)
	 * @see ctgui.original.CTTaskbar#getBoardWindow()
	 */
    public ColoredTrailsBoardWindow getBoardWindow() {
        return boardWindow;
    }
    
    public void reset()
    {
        if(pathWindow != null)
                pathWindow.reset();
        
    }
    
    public void update(Observable o, Object arg) {
        //JOptionPane.showMessageDialog(this," in update " +
        //                ClientData.getInstance().getPerGameId(), "Monira debug",  JOptionPane.PLAIN_MESSAGE);
        
        String s;
        if(arg instanceof String){
            s = (String) arg;
            if( s.startsWith( Constants.AUTOOFFER ) ){
                int msgId = Integer.parseInt(s.substring( Constants.AUTOOFFER.length(), s.indexOf("-"))) ;
                int oppId = Integer.parseInt( s.substring( s.indexOf("-") + 1, s.length()) );
                if( oppId != GUI.getAgent().getPerGameID()){
                    System.out.println("auto offer: " + msgId + " to: " + oppId);
                    Taskbar.getInstance().getAgent().getPerGameID();
                    BasicProposalDiscourseHandler bpdh = new BasicProposalDiscourseHandler();
    //                BasicProposalDiscourseMessage dm = new BasicProposalDiscourseMessage(ICONIFIED, ICONIFIED, WIDTH, chipsSentByProposer, chipsSentByResponder)
                    BasicProposalDiscourseMessage dm = new BasicProposalDiscourseMessage( GUI.getAgent().getPerGameID(), 
                            oppId, msgId, new ChipSet(), new ChipSet());
                    bpdh.makeNewDisplayWindow(dm, BasicProposalDisplayWindow.AUTOOFFER);
                }
            } else if( s.equals( Constants.NEWROUNDDIALOG ) ){
//                JDialog dialog = new JDialog(this, "New Round!", false);
//                dialog.setAlwaysOnTop(true);
//                dialog.setVisible(true);
                NonModalDialog d = new NonModalDialog(this, "Status Update", "New Round!");
            }
        }
        
        updateButtonState();
        repaint();
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
        mainPanel.setLayout(
                new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), 0, 0));
        mainPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Taskbar"));
        buttonPanel = new JPanel();
        mainPanel.add(buttonPanel,
                new GridConstraints(0, 0, 1, 1,
                        GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                GridConstraints.SIZEPOLICY_CAN_GROW,
                        new Dimension(-1, 40), null, null));
    }

	public ColoredTrailsClientImpl getAgent() {
		return agent;
	}

	public void setAgent(ColoredTrailsClientImpl agent) {
		this.agent = agent;
	}


}



 class NonModalDialog extends JDialog implements ActionListener {
     
  public NonModalDialog(JFrame parent, String title, String message) {
    super(parent, title, false);
    if (parent != null) {
      Dimension parentSize = parent.getSize(); 
      Point p = parent.getLocation(); 
      setLocation(p.x + parentSize.width / 4, p.y + parentSize.height / 4);
    }
    setAlwaysOnTop(true);
    JPanel messagePane = new JPanel();
    messagePane.add(new JLabel(message));
    getContentPane().add(messagePane);
    JPanel buttonPane = new JPanel();
    JButton button = new JButton("OK"); 
    buttonPane.add(button); 
    button.addActionListener(this);
    getContentPane().add(buttonPane, BorderLayout.SOUTH);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    pack(); 
    setVisible(true);
    new Timer(5000, this).start();
  }
  
  public void actionPerformed(ActionEvent e) {
    setVisible(false); 
    dispose(); 
  }
  
}
