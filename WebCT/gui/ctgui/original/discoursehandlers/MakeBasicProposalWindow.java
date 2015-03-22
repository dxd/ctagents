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

import com.intellij.uiDesigner.core.GridConstraints;

import com.intellij.uiDesigner.core.GridLayoutManager;



import ctgui.original.ChipSetInputPanel;

import ctgui.original.GUI;

import ctgui.original.PlayerChooserComboBox;

import ctgui.original.Taskbar;





import edu.harvard.eecs.airg.coloredtrails.shared.Constants;
import java.util.Observable;
import javax.swing.*;

import java.awt.*;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;



import java.util.Observer;



/**

	<b>Description (original)</b>

 * A window used to make a BasicProposal to another player.  Appears

 * when a BasicProposal button is clicked on the Taskbar.

 

	<p>

	

	<b>Issues</b>

	

	

 * @author Paul Heymann (ct3@heymann.be)

 */

public class MakeBasicProposalWindow extends JFrame implements ActionListener, Observer {

    private JPanel theirChipsPanel;

    private JPanel yourChipsPanel;

    private JPanel proposeToPanel;

    private JPanel mainPanel;

    private JButton cancelButton;

    private JButton proposeButton;

    private boolean AllowDeceit = false;

    

    private ChipSetInputPanel theirChipsInput = new ChipSetInputPanel();

    private ChipSetInputPanel yourChipsInput = new ChipSetInputPanel();

        

    private PlayerChooserComboBox playerchooser = new PlayerChooserComboBox();

    private JPanel interiorPanel;



    public MakeBasicProposalWindow() {
        GUI.getAgent().getGameStatus().addObserver(this);

        //Sets this class as a listener for the combox box to choose player
        playerchooser.setComboListener(this);

        if(!AllowDeceit){
            yourChipsInput.setChipsMax(GUI.getAgent().getGameStatus().getMyPlayer().getChips());
            theirChipsInput.setChipsMax(GUI.getAgent().getGameStatus().getPlayerByPerGameId(playerchooser.getSelectedPerGameId()).getChips());
        }

        

        setTitle("Make Proposal- Pin "+GUI.getAgent().getPin());

        theirChipsPanel.add(theirChipsInput);

        yourChipsPanel.add(yourChipsInput);

        interiorPanel.add(playerchooser);

        setContentPane(mainPanel);

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        proposeButton.requestFocus();





        proposeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                setVisible(false);



                BasicProposalDiscourseMessage dm =

                        new BasicProposalDiscourseMessage(

                        		GUI.getAgent().getPerGameID(),

                                playerchooser.getSelectedPerGameId(),

                                -1,

                                yourChipsInput.getChipSet(),

                                theirChipsInput.getChipSet());  // message ID set in ClientCommunication.sendDiscourseRequest

                Taskbar.getInstance().getAgent().communication.sendDiscourseRequest(dm);



            }
        });



        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        this.setLocation(690,0);
        pack();

    }



    public void reset() {
        GUI.getAgent().getGameStatus().addObserver(this);
        playerchooser.reset();
        if(!AllowDeceit){
            yourChipsInput.setChipsMax(GUI.getAgent().getGameStatus().getMyPlayer().getChips());
            theirChipsInput.setChipsMax(GUI.getAgent().getGameStatus().getPlayerByPerGameId(playerchooser.getSelectedPerGameId()).getChips());
        }
        theirChipsInput.reset();
        yourChipsInput.reset();
    }

    

    public void actionPerformed(ActionEvent e)

    {

        int pergameid = playerchooser.getSelectedPerGameId();
        if(!AllowDeceit){
            theirChipsInput.setChipsMax(GUI.getAgent().getGameStatus().getPlayerByPerGameId(pergameid).getChips());
        }
        System.out.println("current item:" + pergameid);
        theirChipsInput.resetChipCounts();

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

        mainPanel.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0,

                0, 0), -1,

                -1));

        proposeToPanel = new JPanel();

        proposeToPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0,

                0, 0, 0), -1,

                -1));

        mainPanel.add(proposeToPanel,

                new GridConstraints(0, 0, 1, 1,

                        GridConstraints.ANCHOR_CENTER,

                        GridConstraints.FILL_BOTH,

                        GridConstraints.SIZEPOLICY_CAN_SHRINK |

                GridConstraints.SIZEPOLICY_CAN_GROW,

                        GridConstraints.SIZEPOLICY_CAN_SHRINK |

                GridConstraints.SIZEPOLICY_CAN_GROW,

                        null, new Dimension(200, 40), null));

        proposeToPanel.setBorder(BorderFactory.createTitledBorder(

                BorderFactory.createEtchedBorder(), "Propose to"));

        interiorPanel = new JPanel();

        proposeToPanel.add(interiorPanel,

                new GridConstraints(0, 0, 1, 1,

                        GridConstraints.ANCHOR_CENTER,

                        GridConstraints.FILL_BOTH,

                        GridConstraints.SIZEPOLICY_CAN_SHRINK |

                GridConstraints.SIZEPOLICY_CAN_GROW,

                        GridConstraints.SIZEPOLICY_CAN_SHRINK |

                GridConstraints.SIZEPOLICY_CAN_GROW,

                        null, null, null));

        yourChipsPanel = new JPanel();

        mainPanel.add(yourChipsPanel,

                new GridConstraints(1, 0, 1, 1,

                        GridConstraints.ANCHOR_CENTER,

                        GridConstraints.FILL_BOTH,

                        GridConstraints.SIZEPOLICY_CAN_SHRINK |

                GridConstraints.SIZEPOLICY_CAN_GROW,

                        GridConstraints.SIZEPOLICY_CAN_SHRINK |

                GridConstraints.SIZEPOLICY_CAN_GROW,

                        null, null, null));

        yourChipsPanel.setBorder(BorderFactory.createTitledBorder(

                BorderFactory.createEtchedBorder(), "Your Chips"));

        theirChipsPanel = new JPanel();

        mainPanel.add(theirChipsPanel,

                new GridConstraints(2, 0, 1, 1,

                        GridConstraints.ANCHOR_CENTER,

                        GridConstraints.FILL_BOTH,

                        GridConstraints.SIZEPOLICY_CAN_SHRINK |

                GridConstraints.SIZEPOLICY_CAN_GROW,

                        GridConstraints.SIZEPOLICY_CAN_SHRINK |

                GridConstraints.SIZEPOLICY_CAN_GROW,

                        null, null, null));

        theirChipsPanel.setBorder(

                BorderFactory.createTitledBorder(

                        BorderFactory.createEtchedBorder(), "Their Chips"));

        final JPanel panel1 = new JPanel();

        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0,

                0), -1,

                -1));

        mainPanel.add(panel1,

                new GridConstraints(3, 0, 1, 1,

                        GridConstraints.ANCHOR_CENTER,

                        GridConstraints.FILL_BOTH,

                        GridConstraints.SIZEPOLICY_CAN_SHRINK |

                GridConstraints.SIZEPOLICY_CAN_GROW,

                        GridConstraints.SIZEPOLICY_CAN_SHRINK |

                GridConstraints.SIZEPOLICY_CAN_GROW,

                        null, null, null));

        proposeButton = new JButton();

        proposeButton.setText("Propose!");

        panel1.add(proposeButton,

                new GridConstraints(0, 0, 1, 1,

                        GridConstraints.ANCHOR_CENTER,

                        GridConstraints.FILL_HORIZONTAL,

                        GridConstraints.SIZEPOLICY_CAN_SHRINK |

                GridConstraints.SIZEPOLICY_CAN_GROW,

                        GridConstraints.SIZEPOLICY_FIXED, null, null,

                        null));

        cancelButton = new JButton();

        cancelButton.setText("Cancel");

        panel1.add(cancelButton,

                new GridConstraints(0, 1, 1, 1,

                        GridConstraints.ANCHOR_CENTER,

                        GridConstraints.FILL_HORIZONTAL,

                        GridConstraints.SIZEPOLICY_CAN_SHRINK |

                GridConstraints.SIZEPOLICY_CAN_GROW,

                        GridConstraints.SIZEPOLICY_FIXED, null, null,

                        null));

    }

    public void update(Observable o, Object arg) {
//        reset();
        String s;
        if(arg instanceof String){
            s = (String) arg;
            if(s.equals("NEWROUND") || s.equals("GAME_END") || s.startsWith( Constants.AUTOOFFER ))
                setVisible(false);
        }
    }

}

