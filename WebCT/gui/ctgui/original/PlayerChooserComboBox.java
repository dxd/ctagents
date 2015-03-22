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

import ctgui.original.discoursehandlers.MakeBasicProposalWindow;
import javax.swing.*;
import java.awt.*;

import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import java.awt.event.ActionListener;

/**
	<b>Description (original)</b>
 * A combobox showing icons of all players in the current game and
 * permitting a selection of one.
 
	<p>
	
	<b>Issues</b>
	This code does not support restrictions on who may communicate with whom.
	We may add parameters to the constructor to indicate allowed selections.
	For example, we can have two mutually exclusive parameters 'allowed' and
	'excluded', which are lists of gameIDs.  If we can communicate with all
	players except one or two, then it is more convenient to construct an
	'excluded' list; otherwise, we can use the 'allowed' list.
	
 * @author Paul Heymann (ct3@heymann.be)
   @author Sevan G. Ficici (additional code review)
   @author Monira Sarne 2-21-2006 Change the combo box - not to include me player
           Monira Sarne 3-6-2006  Change the combo box - to contain the players game id (fixing the bug of updating chips)
 */
public class PlayerChooserComboBox extends JPanel {

    JComboBox playerList = null;
    ActionListener ComboListener = null;

    public PlayerChooserComboBox() {
        super(new GridLayout(1, 1));
        setup();
    }

    public void setup() {

        int numPlayers = GUI.getAgent().getGameStatus().getPlayers().size();
        // Monira  - change the combo box not include me player
        int numIndBox  =  GUI.getAgent().getGameStatus().getPlayers().size() -1;
        int indCounter = 0;
        int currPlayerId = 0;

        //Load the pet images and create an array of indexes.
        // Monira  - change the combo box not include me player
        Integer[] intArray = new Integer[numIndBox];

        // Monira 3-6-06 - change the combo box to contain players game id
        //for (int i = 0; i < numPlayers; i++) {
        for( PlayerStatus players : GUI.getAgent().getGameStatus().getPlayers() ) {
               currPlayerId = players.getPerGameId();
               if (currPlayerId != GUI.getAgent().getPerGameID()) {
                  intArray[indCounter] = new Integer(currPlayerId);
                  indCounter = indCounter +1;
               }
        }
        //Create the combo box.
        playerList = new JComboBox(intArray);
        ComboBoxRenderer renderer = new ComboBoxRenderer();
        renderer.setPreferredSize(new Dimension(20, 20));
        playerList.setRenderer(renderer);
        // Monira - Why there is a constant maximum?
        playerList.setMaximumRowCount(3);
        if(ComboListener != null)
            playerList.addActionListener(ComboListener);

        //Lay out the demo.
        add(playerList);

    }

    public void reset() {
        removeAll();
        setup();
    }
    
    public JComboBox getComboBox(){
        return(playerList);
    }
    
    
    public void setComboListener(ActionListener ComboListener){
        this.ComboListener = ComboListener;
        if(ComboListener != null)
            playerList.addActionListener(ComboListener);
    }


    /**
     * Get the per game ID of the player whose icon is currently
     * selected.
     *
     * @return The perGameId of the selected player.
     */
    public int getSelectedPerGameId() {
        // monira 3-6-06 Change to return the game id and not index
        //return playerList.getSelectedIndex();
        return (Integer)(playerList.getSelectedItem());
    }

    class ComboBoxRenderer extends JLabel
            implements ListCellRenderer {
        private Font uhOhFont;

        public ComboBoxRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
        }

        /*
         * This method finds the image and text corresponding
         * to the selected value and returns the label, set up
         * to display the text and image.
         */
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {

            // Get the selected index. (The index param isn't
            // always valid, so just use the value.)
            int selectedIndex = ((Integer) value).intValue();

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            //Set the icon and text.  If icon was null, say so.
            if (selectedIndex != GUI.getAgent().getPerGameID()) {
               ImageIcon icon = Icons.getIconByPerGameId(selectedIndex,
            		   GUI.getAgent().getPerGameID());
               setIcon(icon);
               if (icon != null) {
                   setText("");
                   setFont(list.getFont());
               } else {
                   setUhOhText("" + " (no image available)",
                           list.getFont());
               }


            }
            return this;

        }

        //Set the font and text when no image was found.
        protected void setUhOhText(String uhOhText, Font normalFont) {
            if (uhOhFont == null) { //lazily create this font
                uhOhFont = normalFont.deriveFont(Font.ITALIC);
            }
            setFont(uhOhFont);
            setText(uhOhText);
        }
    }
}


