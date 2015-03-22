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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A window used to transfer a set of chips from one player to another.
 * Appears when the transfer button is clicked on the Taskbar.
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public class MakeTransferWindow extends JFrame {
    private JPanel chipsPanel;
    private JButton transferButton;
    private JButton cancelButton;
    private JPanel transferToPanel;
    private JPanel interiorPanel;
    private JPanel mainPanel;

    private ChipSetInputPanel yourChipsInput = new ChipSetInputPanel();
    private PlayerChooserComboBox playerchooser =
            new PlayerChooserComboBox();

    public MakeTransferWindow() {
        setTitle("Make Transfer - Pin "+ GUI.getAgent().getPin());
        // can't select more chips than you have
        yourChipsInput.setChipsMax(GUI.getAgent().getGameStatus().getMyPlayer().getChips());
        
        chipsPanel.add(yourChipsInput);
        interiorPanel.add(playerchooser);
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        transferButton.requestFocus();

        transferButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);

                Taskbar.getInstance().getAgent().communication.sendTransferRequest(
                        playerchooser.getSelectedPerGameId(),
                        yourChipsInput.getChipSet());
                reset();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        pack();
    }

    public void reset() {
        yourChipsInput.setChipsMax(GUI.getAgent().getGameStatus().getMyPlayer().getChips());
        
        playerchooser.reset();
        yourChipsInput.reset();
    }
}
