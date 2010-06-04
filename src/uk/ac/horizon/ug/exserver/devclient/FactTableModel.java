/**
 * 
 */
package uk.ac.horizon.ug.exserver.devclient;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import uk.ac.horizon.ug.exserver.protocol.Operation;
import uk.ac.horizon.ug.exserver.protocol.RawFactHolder;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;

/**
 * @author cmg
 *
 */
public class FactTableModel extends AbstractTableModel {
	/** type */
	protected TypeDescription type;
	/** field names in order */
	protected String fieldNames[];
	/** facts */
	List<RawFactHolder> facts = new LinkedList<RawFactHolder>();
	/**
	 * @param type
	 */
	public FactTableModel(TypeDescription type) {
		super();
		this.type = type;
		Set<String> fieldNameSet = type.getFields().keySet();
		fieldNames = fieldNameSet.toArray(new String[fieldNameSet.size()]);
	}
	public void clear() {
		facts = new LinkedList<RawFactHolder>();
	}
	public void addFact(RawFactHolder fh) {
		facts.add(fh);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return type.getFields().size()+2;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int n) {
		if (n==0)
			return "Handle";
		else if (n==1)
			return "Op";
		return fieldNames[n-2];
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return facts.size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int col) {
		RawFactHolder fh = facts.get(row);
		if (col==0)
			return fh.getHandle();
		else if (col==1)
			return fh.getOperation();
		Fact fact  = (Fact)fh.getFact();
		return fact.getFieldValues().get(fieldNames[col-2]);
	}
	/** mark to delete.
	 * 
	 * @param row
	 * @return true if row was new (add), in which case it has been removed
	 */
	public boolean markDelete(int row) {
		RawFactHolder fh = facts.get(row);
		if (fh.getOperation()==Operation.add) {
			// need to remove
			facts.remove(row);
			return true;
		}
		fh.setOperation(Operation.delete);
		return false;
	}
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex<2)
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		super.setValueAt(aValue, rowIndex, columnIndex);
		System.err.println("SetValueAt("+aValue+","+rowIndex+","+columnIndex+")");
		RawFactHolder fh = facts.get(rowIndex);
		Fact fact = (Fact)fh.getFact();
		fact.getFieldValues().put(fieldNames[columnIndex-2], aValue);
		if (fh.getOperation()==Operation.ignore) {
			fh.setOperation(Operation.update);		
			this.fireTableCellUpdated(1, rowIndex);
		}
	}
	/** has outstanding changes 
	 * @return */
	public boolean isChanged() {
		for (RawFactHolder fh : facts) {
			if (fh.getOperation()!=Operation.ignore)
				return true;
		}
		return false;
	}
	/** get changed */
	public List<RawFactHolder> getChanges() {
		LinkedList<RawFactHolder> changes = new LinkedList<RawFactHolder>();
		for (RawFactHolder fh : facts) {
			if (fh.getOperation()!=Operation.ignore)
				changes.add(fh);
		}
		return changes;
	}
}
