/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import java.beans.Introspector;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;

import uk.ac.horizon.ug.authorapp.model.ClientSubscriptionInfo;
import uk.ac.horizon.ug.authorapp.model.ClientSubscriptionLifetimeType;
import uk.ac.horizon.ug.authorapp.model.Project;
import uk.ac.horizon.ug.authorapp.model.QueryConstraintInfo;
import uk.ac.horizon.ug.authorapp.model.QueryConstraintType;
import uk.ac.horizon.ug.authorapp.model.QueryInfo;

/**
 * @author cmg
 *
 */
public class ClientSubscriptionTableModel extends AbstractTableModel {
	static Logger logger = Logger.getLogger(ClientSubscriptionTableModel.class.getName());

	static enum Column {
		Name(String.class, "name"),
		Active(Boolean.class, "active"), 
		Type(String.class, "typeName"), 
		Field(String.class, "fieldName"), 
		Constraint(QueryConstraintType.class, "constraintType"), 
		Value(String.class, "parameter"), 
		IncExisting(Boolean.class, "matchExisting"), 
		Lifetime(ClientSubscriptionLifetimeType.class, "lifetime"), 
		Update(Boolean.class, "updateAllowed"), 
		Delete(Boolean.class, "deleteAllowed"), 
		Priority(Float.class, "priority");
		Column(Class clazz, String fieldName) {
			this.clazz = clazz;
			this.fieldName = fieldName;
		}
		private Class clazz;
		public Class clazz() { return clazz; }
		private String fieldName;
		public String fieldName() { return fieldName; }
	}
	/** subscriptions */
	protected List<ClientSubscriptionInfo> subscriptions;
	/** project - to set changed */
	protected Project project;
	/** cons */
	public ClientSubscriptionTableModel(List<ClientSubscriptionInfo> subscriptions, Project project) {
		super();
		this.subscriptions = subscriptions;
		this.project = project;
	}
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return Column.values().length;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return Column.values()[columnIndex].clazz();
	}
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return Column.values()[column].toString();
	}
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		if (subscriptions==null)
			return 0;
		return subscriptions.size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		ClientSubscriptionInfo subscription = subscriptions.get(rowIndex);
		Column col = Column.values()[columnIndex];
		switch(col) {
		case Type: {
			QueryInfo pattern = subscription.getPattern();
			if (pattern==null)
				return null;
			return pattern.getTypeName();
		}
		case Field:
		case Constraint:
		case Value: {
			QueryInfo pattern = subscription.getPattern();
			if (pattern==null)
				return null;
			if (pattern.getConstraints().size()==0)
				return null;
			QueryConstraintInfo constraint = pattern.getConstraints().get(0);
			return getFieldValue(constraint, col.fieldName());
		}
		default:
			return getFieldValue(subscription, col.fieldName());
		}
	}
	public static Object getFieldValue(Object object, String fieldName) {
		if (object==null)
			return null;
		try {
			BeanInfo info = Introspector.getBeanInfo(object.getClass());
			PropertyDescriptor properties[] = info.getPropertyDescriptors();
			for (int i=0; i<properties.length; i++) {
				if (properties[i].getName().equals(fieldName)) {
					Method readMethod = properties[i].getReadMethod();
					return readMethod.invoke(object);
				}
			}
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Unable to read field "+fieldName+" from "+object.getClass().getName()+" "+object, e);			
		}
		return null;
	}
	private boolean setFieldValue(Object object, String fieldName, Object value) {
		if (object==null)
			return false;
		try {
			BeanInfo info = Introspector.getBeanInfo(object.getClass());
			PropertyDescriptor properties[] = info.getPropertyDescriptors();
			for (int i=0; i<properties.length; i++) {
				if (properties[i].getName().equals(fieldName)) {
					Method writeMethod = properties[i].getWriteMethod();
					writeMethod.invoke(object, value);
					return true;
				}
			}
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Unable to write set "+fieldName+"="+value+" in "+object.getClass().getName()+" "+object, e);			
		}
		return false;
	}
//	static class EnumCellRenderer ?!
	public static void setDefaultRenderers(JTable table) {
		table.setDefaultRenderer(Object.class, new FieldCellRenderer());
		table.setDefaultRenderer(String.class, new FieldCellRenderer());
		table.setDefaultEditor(QueryConstraintType.class, new DefaultCellEditor(new JComboBox(QueryConstraintType.values())));
		table.setDefaultEditor(ClientSubscriptionLifetimeType.class, new DefaultCellEditor(new JComboBox(ClientSubscriptionLifetimeType.values())));
		//table.setDefaultRenderer(columnClass, renderer)
	}
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		ClientSubscriptionInfo subscription = subscriptions.get(rowIndex);
		Column col = Column.values()[columnIndex];
		boolean set = false;
		switch(col) {
		case Type: {
			QueryInfo pattern = subscription.getPattern();
			if (pattern==null) {
				pattern = new QueryInfo();
				subscription.setPattern(pattern);
				project.setChanged(true);
			}
			set = setFieldValue(pattern, col.fieldName(), aValue);
			break;
		}
		case Field:
		case Constraint:
		case Value: {
			QueryInfo pattern = subscription.getPattern();
			if (pattern==null) {
				pattern = new QueryInfo();
				subscription.setPattern(pattern);
				project.setChanged(true);
			}
			QueryConstraintInfo constraint = null;
			if (pattern.getConstraints().size()==0) {
				constraint = new QueryConstraintInfo();
				pattern.getConstraints().add(constraint);
				project.setChanged(true);
			}
			else
				constraint = pattern.getConstraints().get(0);
			set = setFieldValue(constraint, col.fieldName(), aValue);
			break;
		}
		default:
			set = setFieldValue(subscription, col.fieldName(), aValue);
			break;
		}
		if (set) {
			project.setChanged(true);
			this.fireTableCellUpdated(rowIndex, columnIndex);
		}
	}
}
