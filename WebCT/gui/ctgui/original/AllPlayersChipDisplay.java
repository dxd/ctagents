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
	
	
	Header: $Header: /root/cvs/ct3/gui/ctgui/original/AllPlayersChipDisplay.java,v 1.5 2008-06-20 14:51:40 mdombrow Exp $
	
*/

package ctgui.original;


import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.GlobalColorMap;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
	<b>Description</b>
	Builds a JTable Swing component that displays a table indicating how
	many chips of each color each player has.  Players are identified by
	their icons (shown in the leftmost table column).
	<p>
	An instance of this object is used in the .client.ui.BoardWindow class.
	[presumably, this is the only place where AllPlayersChipDisplay is used.]
	
	<p>
	
	<b>Observes</b>
	[ADD LISTS of messages that this class gets and classes that it observes]
	
	<p>
	
	<b>Issues</b>
	
	<p>
	
	<b>Modifications</b>
	Monira 2-9-2006 Use of the GamePalette class
	
	<p>
	<b>Original Summary</b>
 * This table displays the current state of the allocated chips for each
 * player in the game.
 */
public class AllPlayersChipDisplay extends JTable implements Observer {
    private String[] columnNames;
    private Object data[][];
    private ClientGameStatus game;

	/**
		Constructor.
		
		@param game		a GameStatus instance representing the game's state
	*/
    public AllPlayersChipDisplay(ClientGameStatus game) {
        super();
        //setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // rdl: unclear as to why this is not done on this constructor
        // but rather on the reloadInfo() function
        
        this.game = game;
        reloadInfo();
    }

    /**
     * Force the first column to be an ImageIcon, others to be Integers.
     */
    public Class getColumnClass(int column) {
        if (column == 0) {
            return ImageIcon.class;
        }
        return Integer.class;
    }

    /**
     * Don't allow column movement?
     */
    public void moveColumn(int column, int targetColumn) {
    }

    /**
     * Don't make cells editable.
     */
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    /**
     * Set all of the column names properly.  All columns are named
     * the empty string, except for the first, which is named player.
     */
    private void setColumnNames() {
    	int numColors = game.getGamePalette().size();
        columnNames = new String[numColors + 1];  // Monira - change to game palette
        //columnNames = new String[game.getNumChipColors() + 1];  // Monira - change to game palette
        columnNames[0] = "Player";
		/* below would be cleaner? (sgf):
		for (int i=1; i<columnNames.length; ++i)
			columnNames[i] = "";
		*/
        for (int i = 0; i < numColors; i++) {  // Monira - change to game palette
        //for (int i = 0; i < game.getNumChipColors(); i++) {  // Monira - change to game palette
            columnNames[i + 1] = "";
        }
    }

    /**
     * Set the row height of each row we know about to 30 pixels high.
     */
    private void setProperRowColHeightWidth(int rows, int cols) {
        for (int i = 0; i < rows; i++) {
            setRowHeight(i, 30);
        }

        getColumnModel().getColumn(0).setPreferredWidth(30);

        for (int j = 1; j < cols; j++) {
            getColumnModel().getColumn(j).setPreferredWidth(30);
        }
    }


    /**
     * Reload table data and column names, repainting and resizing as
     * necessary.
     */
    private void reloadInfo() {
        this.game = GUI.getAgent().getGameStatus();
        
        if(game != null) {

        DefaultTableModel dm = new DefaultTableModel();

        setColumnNames();
        /*JOptionPane.showMessageDialog(this,
                   " number of colors in game palette for player: " +
                   ClientData.getInstance().getPin() + " is " +game.getNumChipColorsGamePalette(),
                   "Monira debug",  JOptionPane.PLAIN_MESSAGE);      */
        dm.setColumnCount(game.getGamePalette().size() + 1); // Monira - change to game palette
        //dm.setColumnCount(game.getNumChipColors() + 1); // Monira - change to game palette
        dm.setDataVector(data, columnNames);
        setModel(dm);

        /*JOptionPane.showMessageDialog(this,
                   " number of players: " + game.getNumPlayers() ,
                   "Monira debug",  JOptionPane.PLAIN_MESSAGE);  */
        
        //this is so damn ugly I want to have someone else check it in to CVS
        Set<PlayerStatus> tplayers = game.getPlayers();
        ArrayList<PlayerStatus> players = new ArrayList<PlayerStatus>(); 
        players.add(game.getMyPlayer());
        for(PlayerStatus ps : tplayers)
            if(ps.getPerGameId() != game.getMyPlayer().getPerGameId())
                players.add(ps);
        
        int rows = players.size();

        data = new Object[players.size()][game.getGamePalette().size() + 1];  //Monira -change game palette

        int i = 0;
        for(PlayerStatus ps: players){
            data[i][0] = Icons.getIconByPerGameId(ps.getPerGameId());
            int j = 1;
            for(String color : game.getGamePalette().getColors()){
                data[i][j] = new Integer(ps.getChips().getNumChips(color));
                j++;
            }
            i++;
        }



        getTableHeader().setReorderingAllowed(false);
        setDefaultRenderer(Integer.class, new ChipRenderer());

        setProperRowColHeightWidth(rows, game.getGamePalette().size() + 1);  // Monira - change to game palette
        //setProperRowColHeightWidth(rows, game.getNumChipColors() + 1);  // Monira - change to game palette

        repaint();
        
        }
        
    }

    public void update(Observable o, Object notification) {
        reloadInfo();
        clearSelection();
        repaint();
    }

    /**
     * ChipRenderer is a cell renderer for chip objects.
     */
    public class ChipRenderer extends JPanel implements TableCellRenderer {

        public ChipRenderer() {
            super();
        }


        /**
         * Figure out the appropriate background color for a square in
         * the display based on its column index.
         * @param col Column number.
         * @return Proper color for display.
         */
        @SuppressWarnings("unchecked")
		private Color getBackgroundColor(int col) {
        	
        	// XXX: Debug: checking to see if this method is called while game is null
        	
        	// System.err.println("getBackgroundColor: game value: " + game + "AllColorsGamePalette " + game.getAllColorsGamePalette() + "getNumChipColorsGamePalette + 1: " + game.getNumChipColorsGamePalette() + 1);
        	
        	if(game.getGamePalette().size() != 0) {
	             if (col > 0 && col < game.getGamePalette().size() + 1) {  // Monira - chanage to game palette
	            	 //XXX: debugging
	                //ArrayList arrayList = new ArrayList(game.getAllColorsGamePalette());
                        return GlobalColorMap.getColorByName(game.getGamePalette().get(col - 1 ));
	            }
        
        	}
            // default: gray
            return Color.GRAY;
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row, int col) {

        	// XXX: debug
        	
        	//System.err.println("getTableCellRendererComponent: value" + value + "isSelected: " + isSelected + "hasFocus:" + hasFocus + "Row: " + row + "Col: " + col);
       
            this.removeAll();
            setBackground(getBackgroundColor(col));
            if (value == null) {
                add(new JLabel());
            } else {
                add(new JLabel((new Integer((Integer) value)).toString()));
            }

            return this;
        }
    }
}
