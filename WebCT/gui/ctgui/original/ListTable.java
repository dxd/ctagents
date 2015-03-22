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

import javax.swing.table.AbstractTableModel;
import java.util.Vector;

/**
 * An abstract table model which gets its data from a vector.
 *
 * @author Paul Heymann (ct3@heymann.be)
 * @author JTable Tutorial?
 */
public class ListTable extends AbstractTableModel {

    private String[] columnNames;

    protected Vector data = new Vector();

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        if (data == null) {
            return 0;
        }
        return data.size();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        if (((Object[]) data.get(row))[col] == null) {
            System.out.println(
                    "ListTable.getValueAt(" + row + "," + col +
                    ") was null");
            System.exit(0);
        }
        return ((Object[]) data.get(row))[col];
    }

    public void setColumnNames(String[] columns) {
        columnNames = columns;
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
        if (getValueAt(0, c).getClass() == null) {
            System.out.println("ListTable.getColumnClass(" + c +
                    ") was null");
            System.exit(0);
        }
        return getValueAt(0, c).getClass();
    }
}

