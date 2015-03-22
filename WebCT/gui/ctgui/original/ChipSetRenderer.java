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
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
	<b>Description</b>
	Draws a row of chips corresponding to the contents of the 
	specified ChipSet, grouped by color.  Thus, only positive
	chip counts result in something being drawn.
	<p>
	[ADD COMMENTS: where is this object used?  for pathfinder?]
	
	<p>
	
	<b>Issues</b>
	Mysterious .setColor(Color.BLACK) -- no reason to have this code
	
	<p>
	
	<b>Original Summary</b>
	A renderer for JTables which displays ChipSets in a cell.
 
 * @author Paul Heymann (ct3@heymann.be)
	@author Sevan G. Ficici (additional commentary)
 */
public class ChipSetRenderer extends JLabel
        implements TableCellRenderer {

    ChipSet missingChips;

    public Component getTableCellRendererComponent(JTable table,
                                                   Object cs,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row, int column) {

        missingChips = (ChipSet) cs;
        return this;
    }

    public void paint(Graphics g) {
    	
    	if(g!= null) {
	        Graphics2D g2 = (Graphics2D) g;
	        int xpos = (getHeight() - 10) / 2;
	
	        for (String curcolor : missingChips.getColors()) {
	            for (int j = 0;
	                 j < missingChips.getNumChips(curcolor); j++) {
	                Color c = GlobalColorMap.getColorByName(curcolor);
	                g.setColor(c);
	                g.fillRect(xpos, getHeight() / 2 - 5, 10, 10);
	                xpos += 12;
					// [sgf: what's this for? was there a plan to draw a black 
					// outline for the chip?]
	                g.setColor(Color.BLACK);
	            }
	        }
    	}
    }
}
