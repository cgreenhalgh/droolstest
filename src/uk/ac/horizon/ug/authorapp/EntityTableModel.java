/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import uk.ac.horizon.ug.authorapp.EntityTableModel.ColumnInfo.ColumnType;
import uk.ac.horizon.ug.exserver.devclient.Fact;
import uk.ac.horizon.ug.exserver.protocol.Operation;
import uk.ac.horizon.ug.exserver.protocol.RawFactHolder;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;
import uk.ac.horizon.ug.exserver.protocol.TypeFieldDescription;
import uk.ac.horizon.ug.exserver.protocol.TypeFieldDescription.FieldMetaKeys;

/** Table model to view/edit Entities, with subset of facets (supertypes) visible.
 * First column is main type primary key.
 * Each non-main type facet visible has a checkbox column to show if a fact of
 * that type exists for the entity.
 * Each non-PK field of each visible fact type has a column.
 * For non-main type facets with max cardinality > 1 shows 2nd and further on 
 * later rows.
 * 
 * @author cmg
 *
 */
public class EntityTableModel extends AbstractTableModel {
	static Logger logger = Logger.getLogger(EntityTableModel.class.getName());
	/** type */
	protected TypeDescription mainType;
	/** primary key field name */
	protected String pkFieldName;
	/** visible facets (fact types) */
	protected List<TypeDescription> visibleTypes;
	/** table specific column info */
	static class ColumnInfo {
		String title;
		TypeDescription type;
		enum ColumnType { exists, field };
		ColumnType columnType;
		String fieldName;
		boolean multiple; // could be more than one row/value per main fact		
		/**
		 * @param title
		 * @param type
		 * @param columnType
		 * @param fieldName
		 * @param multiple
		 */
		public ColumnInfo(String title, TypeDescription type,
				ColumnType columnType, String fieldName, boolean multiple) {
			super();
			this.title = title;
			this.type = type;
			this.columnType = columnType;
			this.fieldName = fieldName;
			this.multiple = multiple;
		}
	}
	/** columns */
	protected List<ColumnInfo> columns;
	/** table-specific fact info */
	static class FactInfo {
		Fact mainFact;
		int rowCount = 1; // if multiple
		/** key is fact (facet) type name */
		Map<String,List<Fact>> subFacts = new HashMap<String,List<Fact>>();
	}
	/** facts */
	List<FactInfo> facts = new LinkedList<FactInfo>();
	/**
	 * @param type
	 */
	public EntityTableModel(TypeDescription mainType, List<TypeDescription> visibleTypes) {
		super();
		this.mainType = mainType;
		// first column
		columns = new LinkedList<ColumnInfo>();
		pkFieldName = mainType.getIdFieldName();
		if (pkFieldName==null)
			pkFieldName = mainType.getSubjectFieldName();
		if (pkFieldName==null) {
			logger.log(Level.WARNING,"Type "+mainType+" does not have a @key field");
		}
		if (pkFieldName!=null)
			columns.add(new ColumnInfo("ID",mainType,ColumnInfo.ColumnType.field,pkFieldName,false));
		for (TypeDescription visibleType : visibleTypes) {
			boolean multiple = false;
			// TODO: check max card.
			if (visibleType!=mainType) {
				columns.add(new ColumnInfo(visibleType.getTypeName()+":", visibleType, ColumnInfo.ColumnType.exists, null, multiple));
			}
			String idField = visibleType.getIdFieldName();
			for (Map.Entry<String, TypeFieldDescription> field : visibleType.getFields().entrySet()) {
				logger.info("Check field "+field.getKey()+" of "+visibleType.getTypeName()+" "+(field.getValue().isSubject() ? "subject ":"")+(field.getValue().isKey() ? "key":""));
				if (field.getValue().isSubject())
					continue;
				if (idField!=null && field.getKey().equals(idField))
					continue;
				columns.add(new ColumnInfo(field.getKey(), visibleType, ColumnInfo.ColumnType.field, field.getKey(), multiple));					
			}
		}
	}
	static String getFieldName(TypeDescription type, FieldMetaKeys key) {
		for (Map.Entry<String, TypeFieldDescription> field : type.getFields().entrySet()) {
			if (field.getValue().getFieldMeta().containsKey(key.name()))
				return field.getKey();
		}
		return null;
	}
	public void clear() {
		facts = new LinkedList<FactInfo>();
	}
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return columns.size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int n) {
		return columns.get(n).title;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		int r = 0;
		for (FactInfo fi : facts) {
			r += fi.rowCount;
		}
		return r;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int col) {
		int r = 0;
		for (FactInfo fi : facts) {
			if (row>=0 && row<fi.rowCount) {
				// TODO
				ColumnInfo ci = columns.get(col);
				if (ci.type==mainType) {
					if (row>0)
						return null;
					return fi.mainFact.getFieldValues().get(ci.fieldName);
				}
				List<Fact> subFacts = fi.subFacts.get(ci.type.getTypeName());
				if (subFacts!=null && subFacts.size()>row) {
					if (ci.columnType==ColumnInfo.ColumnType.exists)
						return true;
					return subFacts.get(row).getFieldValues().get(ci.type.getTypeName());
				}
				if (ci.columnType==ColumnInfo.ColumnType.exists)
					return false;
			}
			r += fi.rowCount;
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		super.setValueAt(aValue, rowIndex, columnIndex);
		System.err.println("SetValueAt("+aValue+","+rowIndex+","+columnIndex+")");
/*		RawFactHolder fh = facts.get(rowIndex);
		Fact fact = (Fact)fh.getFact();
		fact.getFieldValues().put(fieldNames[columnIndex-2], aValue);
		if (fh.getOperation()==Operation.ignore) {
			fh.setOperation(Operation.update);		
			this.fireTableCellUpdated(1, rowIndex);
		}
*/	}
	/** add a fact/row */
	public void addFact(Fact fact) {
		FactInfo fi = new FactInfo();
		fi.mainFact = fact;
		fi.rowCount = 1;
		//fi.subFacts
		facts.add(fi);
	}
}
