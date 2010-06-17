/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import uk.ac.horizon.ug.authorapp.EntityTableModel.ColumnInfo.ColumnType;
import uk.ac.horizon.ug.authorapp.customtype.CustomFieldType;
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
		enum ColumnType { exists, field, id };
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
	/** fact store */
	protected FactStore factStore;
	/**
	 * @param readonly 
	 * @param type
	 */
	private boolean readonly;
	public EntityTableModel(TypeDescription mainType, List<TypeDescription> visibleTypes, FactStore factStore, boolean readonly) {
		super();
		this.mainType = mainType;
		this.factStore = factStore;
		this.readonly = readonly;
		// first column
		columns = new LinkedList<ColumnInfo>();
		pkFieldName = mainType.getIdFieldName();
		if (pkFieldName==null)
			pkFieldName = mainType.getSubjectFieldName();
		if (pkFieldName==null) {
			logger.log(Level.WARNING,"Type "+mainType+" does not have a @key field");
		}
		if (pkFieldName!=null)
			columns.add(new ColumnInfo("ID",mainType,ColumnInfo.ColumnType.id,pkFieldName,false));
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
		
		// initial values
		if (factStore!=null) {
			// main facts
			List<Fact> mainFacts = factStore.getFacts(mainType.getTypeName());
			for (Fact fact : mainFacts)
				addFactInternal(fact);
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
		fireTableDataChanged();
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
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		ColumnInfo ci = columns.get(columnIndex);
		if (ci.columnType==ColumnInfo.ColumnType.exists)
			return Boolean.class;
		if (ci.fieldName!=null) {
			TypeFieldDescription field = ci.type.getFields().get(ci.fieldName);
			// WARNING: underling type in our representation are currently strings, so this won't work
//			
//			if (field.getTypeName().equals("boolean"))
//				return Boolean.class;
//			else if (field.getTypeName().equals("int"))
//				return Number.class;
//			else if (field.getTypeName().equals("long"))
//				return Number.class;
//			else if (field.getTypeName().equals("short"))
//				return Number.class;
//			else if (field.getTypeName().equals("float"))
//				return Float.class;
//			else if (field.getTypeName().equals("double"))
//				return Double.class;
			// TODO other field types...?
		}
		return super.getColumnClass(columnIndex);
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
					return subFacts.get(row).getFieldValues().get(ci.fieldName);
				}
				if (ci.columnType==ColumnInfo.ColumnType.exists)
					return false;
			}
			row -= fi.rowCount;
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int row, int columnIndex) {
		if (readonly)
			return false;
		ColumnInfo ci = columns.get(columnIndex);
		if (ci.columnType==ColumnInfo.ColumnType.id)
			return false; // can't change ID for now
		for (FactInfo fi : facts) {
			if (row>=0 && row<fi.rowCount) {
				if (ci.type==mainType) {
					if (row==0)
						return true;
					return false;
				}
				List<Fact> subFacts = fi.subFacts.get(ci.type.getTypeName());
				if (subFacts==null) {
					if (ci.columnType==ColumnInfo.ColumnType.exists && row==0)
						return true;
					return false;
				}
				if (row<subFacts.size() || (ci.columnType==ColumnInfo.ColumnType.exists && row==subFacts.size()))
					return true;
				return false;
			}
			row -= fi.rowCount;
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(Object aValue, final int rowIndex, int columnIndex) {
		super.setValueAt(aValue, rowIndex, columnIndex);
		System.err.println("SetValueAt("+aValue+","+rowIndex+","+columnIndex+")");
		ColumnInfo ci = columns.get(columnIndex);
		int row = rowIndex;
		for (FactInfo fi : facts) {
			if (row>=0 && row<fi.rowCount) {
				if (ci.type==mainType) {
					if (row==0 && ci.columnType==ColumnInfo.ColumnType.field) {
						fi.mainFact.getFieldValues().put(ci.fieldName, aValue.toString());
						factStore.updateFact(fi.mainFact);
						return;
					}
					// no op
					logger.log(Level.WARNING,"Ignore setValue for mainType, non-field "+ci.fieldName);
					return;
				}
				List<Fact> subFacts = fi.subFacts.get(ci.type.getTypeName());
				if (subFacts==null || row>=subFacts.size()) {					
					if (ci.columnType==ColumnInfo.ColumnType.exists) {
						if ((subFacts==null && row>0) || (subFacts!=null && row>subFacts.size()))
							// no op
							return;
						// new subfact
						if (subFacts==null) {
							subFacts = new LinkedList<Fact>();
							fi.subFacts.put(ci.type.getTypeName(), subFacts);
						}
						Fact subFact = new Fact();
						subFact.setNamespace(ci.type.getNamespace());
						subFact.setTypeName(ci.type.getTypeName());
						String idName = ci.type.getIdFieldName();
						if (idName==null)
							idName = ci.type.getSubjectFieldName();
						if (idName==null) {
							logger.log(Level.WARNING,"Subfact "+ci.type.getTypeName()+" has no id/subject field");
							return;
						}
						String id = pkFieldName!=null ? (String)fi.mainFact.getFieldValues().get(pkFieldName) : null;
						if (id==null) {
							logger.log(Level.WARNING, "ID "+pkFieldName+" not set in fact "+fi.mainFact);
							return;
						}
						subFact.getFieldValues().put(idName, id);
						subFacts.add(subFact);
						factStore.addFact(subFact);
						// knock-on effect
						this.fireTableRowsUpdated(row, row);
						// extra row?!
						if (subFacts.size()>=fi.rowCount && ci.multiple) {
							fi.rowCount++;
							// extra row
							this.fireTableRowsInserted(rowIndex+1, rowIndex+1);
						}
						return;
					}
					// no op
					return;
				}
				// edit or remove subfact in place
				Fact subFact = subFacts.get(row);
				if (ci.columnType==ColumnInfo.ColumnType.exists) {
					String sval = aValue.toString();
					if (sval.length()==0 || sval.startsWith("f") || sval.startsWith("F") || sval.startsWith("0"))
					{
						logger.info("delete subfact "+subFact);
						// delete
						factStore.removeFact(subFact);
						subFacts.remove(row);
						// TODO maybe reduce rowCount
						this.fireTableRowsUpdated(rowIndex, rowIndex);
					}
					// else leave - no-op
					return;
				} 
				else if (ci.fieldName!=null){
					// edit
					if (aValue==null)
						subFact.getFieldValues().remove(ci.fieldName);
					else {
						String sval = aValue.toString();
						subFact.getFieldValues().put(ci.fieldName, sval);
					}
					factStore.updateFact(subFact);				
				}
			}
			row -= fi.rowCount;
		}
	}
	/** add a fact/row */
	public void addFact(Fact fact) {
		if (pkFieldName!=null) {
			Object id = fact.getFieldValues().get(pkFieldName);
			if (id==null)
				throw new InvalidParameterException("addFact for Fact with no id value ("+fact.getTypeName()+"."+pkFieldName+")");
			if (factStore.getFact(fact.getTypeName(), pkFieldName, id)!=null)
				throw new InvalidParameterException("addFact for Fact already present ("+fact.getTypeName()+"."+pkFieldName+"="+id+")");
		}
		factStore.addFact(fact);
		addFactInternal(fact);
	}
	protected void addFactInternal(Fact fact) {
		FactInfo fi = new FactInfo();
		fi.mainFact = fact;
		fi.rowCount = 1;
		if(pkFieldName!=null) {
			// subfacts...
			Object id = fact.getFieldValues().get(pkFieldName);
			if (id==null) {
				logger.log(Level.WARNING,"Add fact with no id ("+pkFieldName+")");
			}
			else {
				for (ColumnInfo ci : this.columns) {
					// note: only checks/add on exists column...
					if (ci.type!=this.mainType && ci.columnType==ColumnInfo.ColumnType.exists) {
						String idField = ci.type.getIdFieldName();
						if (idField==null)
							idField = ci.type.getSubjectFieldName();
						if (idField==null) {
							logger.log(Level.WARNING,"Add fact with visible type "+ci.type.getTypeName()+" without id/subject");
							continue;
						}
						List<Fact> subFacts = factStore.getFacts(ci.type.getTypeName(), idField, id);
						List<Fact> knownSubFacts = fi.subFacts.get(ci.type.getTypeName());
						if (knownSubFacts==null) {
							knownSubFacts = new LinkedList<Fact>();
							fi.subFacts.put(ci.type.getTypeName(), knownSubFacts);
						}
							// check/add each
						for (Fact subFact : subFacts) {
							if (!knownSubFacts.contains(subFact))
								knownSubFacts.add(subFact);
						}
						if (knownSubFacts.size()>fi.rowCount)
							fi.rowCount = knownSubFacts.size();
						if (ci.multiple && knownSubFacts.size()==fi.rowCount)
							fi.rowCount++;
					}
				}
			}
		}		
		facts.add(fi);
		fireTableRowsInserted(getRowCount()-fi.rowCount, getRowCount()-1);
	}
	/** delete a row !
	 * @return rows deleted */
	public int deleteRow(int rowIndex, boolean includeSubFacts) {
		int row = rowIndex;
		for (int i=0; i<facts.size(); i++) {
			FactInfo fi = facts.get(i);
			if (row>=0 && row<fi.rowCount) {
				if (row==0) {
					factStore.removeFact(fi.mainFact);
					facts.remove(i);
					logger.info("Delete main fact "+fi.mainFact+" ("+fi.rowCount+" rows)");
					// TODO includeSubFacts
					fireTableRowsDeleted(rowIndex, rowIndex+fi.rowCount-1);
					return fi.rowCount;
				}
				// TODO add'l subfacts
				return 0;
			}
			row -= fi.rowCount;
		}
		return 0;
	}
	protected DefaultTableColumnModel columnModel = null;
	/** get column model */
	public synchronized TableColumnModel getColumnModel() {
		if (columnModel!=null)
			return columnModel;
		columnModel = new DefaultTableColumnModel();
		for (int i=0; i<columns.size(); i++) {
			ColumnInfo ci = columns.get(i);
			TableCellRenderer cellRenderer = null;
			TableCellEditor cellEditor = null;
			if (ci.columnType==ColumnInfo.ColumnType.field && ci.fieldName!=null) {
				String type = ci.type.getFields().get(ci.fieldName).getMetaType();
				if (type!=null) {
					CustomFieldType cft = PluginManager.getPluginManager().getCustomFieldType(type);
					if (cft!=null) {
						logger.info("Found CustomFieldType for table: "+type);
						cellRenderer = cft.getTableCellRenderer();
						cellEditor = cft.getTableCellEditor();
					}
				}
			}
			logger.info("Column "+i+", renderer="+cellRenderer+", editor="+cellEditor);
			columnModel.addColumn(new TableColumn(i, 75, cellRenderer, cellEditor));
		}
		return columnModel;
	}
}
