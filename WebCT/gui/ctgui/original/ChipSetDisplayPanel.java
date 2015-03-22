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

import edu.harvard.eecs.airg.coloredtrails.shared.GlobalColorMap;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;

import javax.swing.*;
import java.awt.*;
import java.util.TreeSet;

/**
	<b>Description</b>
	An extension of JPanel that displays a row of JLabels, one JLabel for each color
	"registered" with the ChipSet (i.e., in the ChipSet's hashtable).  Each JLabel
	displays a string giving the number of chips the ChipSet has of the label's
	corresponding color.
	<p>
	[ADD COMMENTS: how is this class used?]

 *
 * @author Paul Heymann (ct3@heymann.be)
	@author Sevan G. Ficici (class-level review, javadoc comments)
 */
public class ChipSetDisplayPanel extends JPanel {
	/** each element of this array is a label displaying the number of chips of a certain color */
    JLabel[] colorsquares;
	/** the shared.types.ChipSet instance that this panel is displaying */
    ChipSet cs = null;

    public ChipSetDisplayPanel(ChipSet cs) {
        this.cs = cs;
        setup();
    }

    public void setup() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(200, 20));
        setMinimumSize(new Dimension(200, 20));
        setMaximumSize(new Dimension(200, 20));
		// the number of colors registered in this chipset
        int allchipcolors = cs.getColors().size();

        setLayout(new GridLayout(1, allchipcolors));

        /**********************/
        /* squares and arrows */
        /**********************/

        colorsquares = new JLabel[allchipcolors];
        int i = 0;
        for (String color : new TreeSet<String>(cs.getColors())) {  // for each color registered in the chipset...
            JPanel onecontrol = new JPanel(new GridLayout(1, 1));
            colorsquares[i] =
                    new JLabel(Integer.toString(cs.getNumChips(color)),
                            JLabel.CENTER);  // string of label is number of chips of this color
            colorsquares[i].setBackground(GlobalColorMap.getColorByName(
                    color));
            colorsquares[i].setOpaque(true);
            colorsquares[i].setBorder(BorderFactory.createLineBorder(Color.black));

            onecontrol.add(colorsquares[i]);

            add(onecontrol);
            i++;
        }
    }
}
