/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import javax.swing.table.AbstractTableModel;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
/**
 * @author cmg
 *
 */
public class BeanPropertyTableModel extends AbstractTableModel {
	static Logger logger = Logger.getLogger(BeanPropertyTableModel.class.getName());
	/** the bean */
	protected Object bean;
	/** optional map of property name/title */
	protected Map<String,String> propertyTitles;
	/** info on property */
	static class PropertyInfo {
		String name;
		String title;
		Class clazz;
	}
	/** properties (in order) */
	protected List<PropertyInfo> properties;
	
	/**
	 * @param bean
	 * @param propertyTitles
	 */
	public BeanPropertyTableModel(Object bean,
			Map<String, String> propertyTitles) {
		super();
		this.bean = bean;
		this.propertyTitles = propertyTitles; 
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
			PropertyDescriptor props [] = beanInfo.getPropertyDescriptors();
			// TODO
		}
		catch (Exception e) {
			logger.log(Level.WARNING,"Introspecting "+bean, e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
