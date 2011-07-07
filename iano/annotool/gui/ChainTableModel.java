package annotool.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class ChainTableModel extends AbstractTableModel {
	private boolean DEBUG = false;
	
	private String[] columnNames = {"",
            						"Chained Algorithms"};
	
	private List<Object[]> items = new ArrayList<Object[]>();
	
	public int getColumnCount() {
        return columnNames.length;
    }
	public int getRowCount() {
        return items.size();
    }
	public Object getValueAt(int row, int col) {
        return items.get(row)[col];
    }
	public String getColumnName(int col) {
        return columnNames[col];
    }
	public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
	public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col == 0) {
            return true;
        } else {
            return false;
        }
    }
	public void setValueAt(Object value, int row, int col) {
        if (DEBUG) {
            System.out.println("Setting value at " + row + "," + col
                               + " to " + value
                               + " (an instance of "
                               + value.getClass() + ")");
        }

        items.get(row)[col] = value;
        fireTableCellUpdated(row, col);

        if (DEBUG) {
            System.out.println("New value of data:");
            printDebugData();
        }
    }
	
	private void printDebugData() {
        int numRows = getRowCount();
        int numCols = getColumnCount();

        for (int i=0; i < numRows; i++) {
            System.out.print("    row " + i + ":");
            for (int j=0; j < numCols; j++) {
                System.out.print("  " + items.get(i)[j]);
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }
	
	public void insertNewRow(Object[] values) {
        items.add(values);
        fireTableDataChanged();
    }
	public void removeRow(int row) {
		if(row >= 0 && row < items.size())
			items.remove(row);
	    fireTableDataChanged();
	}
	public void removeAll() {
		items.removeAll(items);
	    fireTableDataChanged();
	}
}
