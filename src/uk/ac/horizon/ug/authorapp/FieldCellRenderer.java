package uk.ac.horizon.ug.authorapp;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/** custom cell renderer, e.g. show full value as tooltip */
public class FieldCellRenderer extends DefaultTableCellRenderer {

	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable arg0,
			Object value, boolean arg2, boolean arg3, int arg4, int arg5) {
		if (value!=null)
			setToolTipText(value.toString());
		else
			this.setToolTipText(null);
		// TODO Auto-generated method stub
		return super.getTableCellRendererComponent(arg0, value, arg2, arg3, arg4, arg5);
	}
	
}