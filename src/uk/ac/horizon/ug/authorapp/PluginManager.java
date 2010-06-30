/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import uk.ac.horizon.ug.authorapp.customtype.CustomFieldType;
import uk.ac.horizon.ug.authorapp.customview.AbstractViewItem;
import uk.ac.horizon.ug.authorapp.customview.AbstractViewLayout;
import uk.ac.horizon.ug.authorapp.customview.DefaultViewItem;
import uk.ac.horizon.ug.authorapp.customview.NullViewLayout;
import uk.ac.horizon.ug.authorapp.model.ViewLayoutInfo;

/** Handle "plugins" e.g. CustomFieldTypes.
 * 
 * @author cmg
 *
 */
public class PluginManager {
	static Logger logger = Logger.getLogger(PluginManager.class.getName());
	/** get instance */
	public static synchronized PluginManager getPluginManager() {
		if (instance==null)
			instance = new PluginManager();
		return instance;
	}
	/** singleton */
	protected static PluginManager instance;
	/** cons */
	protected PluginManager() {		
	}
	/** custom field types */
	protected Map<String,CustomFieldType> customFieldTypes = new HashMap<String,CustomFieldType>();
	/** get custom field type (if defined) */
	public synchronized CustomFieldType getCustomFieldType(String type) {
		if (type.startsWith("\"")) {
			type = type.substring(1);
			if (type.lastIndexOf('"')>=0)
				type = type.substring(0, type.lastIndexOf('"'));
		}
		CustomFieldType cft = customFieldTypes.get(type);
		if (cft!=null)
			return cft;
		try {
			cft = (CustomFieldType)Class.forName("uk.ac.horizon.ug.authorapp.customtype."+type).newInstance();
			logger.info("Found CustomFieldType "+cft.getClass());
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Could not find CustomFieldType "+type+" ("+e+")");
			cft = new DefaultFieldType(type);
		}
		customFieldTypes.put(type, cft);
		return cft;
	}
	/** default */
	class DefaultFieldType implements CustomFieldType  {

		protected String name;
		DefaultFieldType(String name) {
			this.name = name;
		}
		@Override
		public String getFieldTypeName() {
			// TODO Auto-generated method stub
			return name;
		}

		@Override
		public TableCellEditor getTableCellEditor() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public TableCellRenderer getTableCellRenderer() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	/** get view layout (not cached) */
	public synchronized AbstractViewLayout newViewLayout(String name, ViewLayoutInfo viewLayoutInfo) {
		if (name==null || name.length()==0) {
			logger.log(Level.WARNING, "newViewLayout called with no name");
			return new NullViewLayout();
		}
		AbstractViewLayout viewLayout = null;
		try {
			viewLayout = (AbstractViewLayout)Class.forName("uk.ac.horizon.ug.authorapp.customview."+name+"ViewLayout").newInstance();
			logger.info("Found ViewLayout "+viewLayout.getClass());
			viewLayout.setViewLayoutInfo(viewLayoutInfo);
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Could not find ViewLayout "+name+" ("+e+")");
			viewLayout = new NullViewLayout();		
		}
		return viewLayout;
	}
	/** get view item (not cached) */
	public synchronized AbstractViewItem newViewItem(String name) {
		if (name==null || name.length()==0) {
			//logger.log(Level.WARNING, "newViewItem called with no name");
			return new DefaultViewItem();
		}
		AbstractViewItem viewItem = null;
		try {
			viewItem = (AbstractViewItem)Class.forName("uk.ac.horizon.ug.authorapp.customview."+name+"ViewItem").newInstance();
			logger.info("Found ViewItem "+viewItem.getClass());
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Could not find ViewLayout "+name+" ("+e+")");
			viewItem = new DefaultViewItem();		
		}
		return viewItem;
	}
}
