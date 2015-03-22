/*
	Colored Trails
	
	Copyright (C) 2006-2007, President and Fellows of Harvard College.  All Rights Reserved.
	
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

import java.awt.*;
import javax.swing.*;


public class PlayerRenderer extends JLabel implements ListCellRenderer{
    public PlayerRenderer(){
        setOpaque(true);
    }

    public Component getListCellRendererComponent(
        JList jlist, Object obj, int index, boolean isSelected, boolean focus){
        setIcon( ( (JLabel) obj ).getIcon() );
        setText( ( (JLabel) obj ).getText() );
        
        if( !isSelected ){
            setBackground( jlist.getBackground() );
            setForeground( jlist.getForeground() );
        }
        else{
        	setBackground( jlist.getSelectionBackground() );
            setForeground( jlist.getSelectionForeground() );
        }
        return this;
    }
}
