/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import javax.swing.table.AbstractTableModel;

import uk.ac.horizon.ug.common.TypeUtils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author cmg
 *
 */
public class BeanPropertiesTableModel<T> extends AbstractTableModel {
	static Logger logger = Logger.getLogger(BeanPropertiesTableModel.class.getName());
	/** the bean */
	protected T bean;
	/** properties */
	protected PropertyDescriptor properties[];
	/**
	 * @throws IntrospectionException 
	 * 
	 */
	public BeanPropertiesTableModel(T bean) throws IntrospectionException {
		this.bean = bean;
		BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
		properties = beanInfo.getPropertyDescriptors();
		for (int i=0; i<properties.length; i++) {
			if (properties[i].getName().equals("class")) {
				// remove
				PropertyDescriptor ops [] = properties;
				properties = new PropertyDescriptor[ops.length-1];
				for (int j=0, k=0; k<ops.length; k++) {
					if (k==i)
						continue;
					properties[j++] = ops[k];					
				}
			}
		}
	}

	public T getBean() {
		return bean;
	}

	public void setBean(T bean) {
		this.bean = bean;
		this.fireTableDataChanged();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return properties.length;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int col) {
		if (col==0) 
			return properties[row].getDisplayName();
		// value...
		Method readMethod = properties[row].getReadMethod();
		if (readMethod==null)
			return ERROR;
		try {
			Object value = readMethod.invoke(bean);
			return value;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error getting "+properties[row].getName()+" from "+bean, e);
			return ERROR;
		}
	}

	@Override
	public String getColumnName(int col) {
		if (col==0)
			return "Property";
		return "Value";
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if (col==0)
			return false;
		Method writeMethod = properties[row].getWriteMethod();
		if (writeMethod==null)
			return false;
		return true;
	}

	@Override
	public void setValueAt(Object val, int row, int col) {
		if (col==0)
			return;
		if (val==ERROR)
			return;
		Method writeMethod = properties[row].getWriteMethod();
		if (writeMethod==null)
			return;
		try {
			Class toClass = properties[row].getPropertyType();
			Object toVal = TypeUtils.coerce(val, toClass);
			writeMethod.invoke(bean, toVal);			
			this.fireTableCellUpdated(row, col);
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Error setting "+properties[row].getName()+" on "+bean, e);
		}
	}

	public static final String ERROR = "Error";
}
