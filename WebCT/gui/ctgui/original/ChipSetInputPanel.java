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

import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.GlobalColorMap;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GamePalette;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.List;

/**
	<b>Description</b>
	Provides a user interface (as a JPanel) with which a player can specify 
	a change of chip counts.  The interface is comprised of three rows of
	Swing components: a row of count-increment buttons (one for each chip color),
	a row of JLabels (to display chip counts), and a row of count-decrement
	buttons. Each row has N columns, where N is the number of chip colors the 
	interface knows about.
	
	<p>
	
	<b>Issues</b>
	There exist some public methods in this class that could probably be made private.
	<p>
	Is the reset() method actually invoked by anything?  Get rid of it?  If not, then
	it can be cleaned up slightly.

	<p>
	
	<b>Original Summary</b>
 * ChipSetInputPanel allows you to select a set of chip colors.  It is
 * accessed primarily through the getChipSet() method, though you can also
 * reset the chip numbers to zero using the reset() method.
 *
 * @author Paul Heymann (ct3@heymann.be)
   @author Sevan G. Ficici (additional javadoc and commentaries)
   @author Monira Sarne 2-22-06 use colors from the game palette instead of colors on board.
 */
public class ChipSetInputPanel extends JPanel implements ActionListener {
	/** row of JLabels to display 'chipcounts' values */
    JLabel[] colorsquares;
	/** row of buttons to increment chip counts of each color */
    JButton[] colorincrements;
	/** row of buttons to decrement chip counts of each color */
    JButton[] colordecrements;
	/** chip counts for each color */
    int[] chipcounts;
	/** minimal chip counts allowable for each color */
    int[] mins;
	/** maximal chip counts allowable for each color */
    int[] maxs;
    
    GamePalette gp;


	/**
		Basic Constructor.  Initializes panel to default values.
	*/
    public ChipSetInputPanel() {
        ClientGameStatus game = GUI.getAgent().getGameStatus();
        gp = game.getGamePalette();
        int numColors = gp.size();

        mins = new int[numColors];
        maxs = new int[numColors];
        for (int i = 0; i < numColors; i++) {
            mins[i] = Integer.MIN_VALUE;
            maxs[i] = Integer.MAX_VALUE;
        }

        setup();
    }

	
	/**
		Constructor that allows you to specify minimal and maximal counts.
		
		@param mins		minimal chip counts allowable for each color
		@param maxs		maximal chip counts allowable for each color
	*/
    public ChipSetInputPanel(int[] mins, int[] maxs) {
        this();

        setChipsMinsMaxs(mins, maxs);
    }


	/**
		builds the panel and initializes to default values.
		<p>
		[this probably doesn't need to be public]
		<p>
		BUG: this method queries the game board to learn the color palette;
		if the game board happens not to have all of the game's colors on it,
		then the chip-exchange related code (such as this) overlooks those
		missing colors.
		<p>
		FIX: we need to construct a new GamePalette object that is a subset
		of the global color set; the GamePalette is defined to be a superset
		of colors on the board and those in chipsets, and specifies the 
		"defined" colors for the game.
	*/
    public void setup() {

        ClientGameStatus game = GUI.getAgent().getGameStatus();
        //int allchipcolors = game.getNumChipColors();  // BUG -- only counts board colors
        //int allchipcolors = game.getNumChipColorsGamePalette(); // Monira 2-22-06 - change to game palette
        gp = game.getGamePalette();
        int numColors = gp.size();
		
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(200, 40));
        setMinimumSize(new Dimension(200, 40));
        setMaximumSize(new Dimension(200, 40));

        setLayout(new GridLayout(1, numColors));

        /**********************/
        /* squares and arrows */
        /**********************/

        colorsquares = new JLabel[numColors];
        colorincrements = new JButton[numColors];
        colordecrements = new JButton[numColors];
        for (int i = 0; i < numColors; i++) {            // for each color...
			// build a column comprised of an increment button, a count label,
			// and a decrement column
            JPanel onecontrol = new JPanel(new GridLayout(3, 1));
            colorsquares[i] = new JLabel("0", JLabel.CENTER);
            // Monira- This is a BUG - only counts board colors
            // colorsquares[i].setBackground(GlobalColorMap.getColorByName(
            //         game.getChipColorByIndex(i)));

            // Monira 2-22-06 - change to count the colors from the game palette
            colorsquares[i].setBackground(GlobalColorMap.getColorByName(gp.get(i)));

            colorsquares[i].setOpaque(true);
            colorsquares[i].setBorder(
                    BorderFactory.createLineBorder(Color.black));
            colorincrements[i] =
                    new BasicArrowButton(BasicArrowButton.NORTH);
            colorincrements[i].addActionListener(this);

            colordecrements[i] =
                    new BasicArrowButton(BasicArrowButton.SOUTH);
            colordecrements[i].addActionListener(this);

            onecontrol.add(colorincrements[i]);
            onecontrol.add(colorsquares[i]);
            onecontrol.add(colordecrements[i]);

            add(onecontrol);
        }


        /*******************/
        /* counts of chips */
        /*******************/

        chipcounts = new int[numColors];
        for (int i = 0; i < numColors; i++) {
            chipcounts[i] = 0;
//            mins[i] = Integer.MIN_VALUE;
//            maxs[i] = Integer.MAX_VALUE;
        }
	
    }


    /**
     * Set the minimums and maximums for each color in the input panel.
     *
		<p>
		[sgf: might want to add checks to make sure that mins are < maxs]
	 
     * @param mins An array of minimum numbers for each color.
     * @param maxs An array of maximum numbers for each color.
     */
    public void setChipsMinsMaxs(int[] mins, int[] maxs) {
        //we assume that if its not the proper length we can just ignore it
        if (mins != null && mins.length == chipcounts.length) {
            this.mins = mins;
        }
        if (maxs != null && maxs.length == chipcounts.length) {
            this.maxs = maxs;
        }
    }
    
    public void setChipsMax(ChipSet MaxSet)
    {
        if(((MaxSet.getColors()).size() == chipcounts.length) && (MaxSet != null)){
            //my soul cries at this, is there no better way?
            for(int i = 0; i < mins.length; i++)
                mins[i] = 0;
            for(String color : MaxSet.getColors())
                maxs[gp.indexOf(color)] = MaxSet.getNumChips(color);
        }
    }


    /**
     * Reset the chip display to all zeros.
		<p>
		[sgf: is this method actually used?  by what?]
		<p>
		[sgf: what is the purpose of the reset?  necessary to rebuild the gui?
		why not just reset chipcounts and mins/maxs?  if setup() kept, then no
		need to set chipcounts, since setup() does that.]
     */
    public void reset() {
        removeAll();  // remove all Swing components from the JPanel
        setup();      // rebuild the JPanel
        for (int i = 0; i < chipcounts.length; i++) {  // this is redundant
            chipcounts[i] = 0;
        }
        redisplayChipCounts();  // show the panel (see below)
    }
    
    public void resetChipCounts() {
        for (int i = 0; i < chipcounts.length; i++) 
            chipcounts[i] = 0;
        redisplayChipCounts();
    }


    /**
		@return a ChipSet instance that represents the chipcounts currently
		indicated in the ChipSetInputPanel.  Remember that the 
		ChipSetInputPanel can allow negative chip counts.
     */
    public ChipSet getChipSet() {
        ChipSet chips = new ChipSet();
        for (int i = 0; i < chipcounts.length; ++i) {
            // Monira 2-22-06 Fix Bug - refer to colors from game palette instead of colors on board
            String colorName = GUI.getAgent().getGameStatus().getGamePalette().get(i);//GUI.getAgent()
                    //.getGamePalette().indexOf(i);
                    //.getChipColorByIndex(i);
            chips = ChipSet.addChipSets(chips,
                    new ChipSet(colorName, chipcounts[i]));
        }
        return chips;
    }


    /**
		sets the JLabel (colorsquares) strings to reflect current 
		chipcount values and redraws the panel.
		<p>
		[this probably doesn't need to be public]
     */
    public void redisplayChipCounts() {
        for (int i = 0; i < chipcounts.length; i++) {
            colorsquares[i].setText("" + chipcounts[i]);
        }
        repaint();
    }

    /**
     * Handle increment and decrement operations (the arrows).
     */
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        for (int i = 0; i < colorincrements.length; i++) {
            if ((source == colorincrements[i]) && (chipcounts[i] + 1 <= maxs[i])) {
                chipcounts[i]++;
            } else if (source == colordecrements[i] &&
                    (chipcounts[i] - 1 >= mins[i])) {
                chipcounts[i]--;
            }
            redisplayChipCounts();  // set JLabels to reflect any chipcount changes
        }
    }
    
    public void printMax()
    {
        System.out.print(this.getName() + " max: ");
        for(int i = 0; i < colorincrements.length; i++)
            System.out.print(maxs[i] + " ");
        
    }

}
