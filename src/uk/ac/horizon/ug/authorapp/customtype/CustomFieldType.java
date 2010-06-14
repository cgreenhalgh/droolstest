/**
 * 
 */
package uk.ac.horizon.ug.authorapp.customtype;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/** interface for a custom field type.
 * 
 * @author cmg
 *
 */
public interface CustomFieldType {
	/** custom field type name */
	public String getFieldTypeName();
	/** custom Table renderer ? */
	public TableCellRenderer getTableCellRenderer();
	/** custom Table editor ? */
	public TableCellEditor getTableCellEditor();
}
