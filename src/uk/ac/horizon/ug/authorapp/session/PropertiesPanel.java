/**
 * 
 */
package uk.ac.horizon.ug.authorapp.session;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import uk.ac.horizon.ug.authorapp.FieldCellRenderer;

/**
 * @author cmg
 *
 */
public class PropertiesPanel extends JPanel {
	/** session frame */
	protected SessionFrame sessionFrame;
	/** table model */
	protected DefaultTableModel tableModel;
	/**
	 * @param sessionFrame
	 */
	public PropertiesPanel(SessionFrame sessionFrame) {
		super(new BorderLayout());
		this.sessionFrame = sessionFrame;
		
		tableModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		tableModel.addColumn("Property");
		tableModel.addColumn("Value");
		tableModel.addRow(new Object[] { "Server URL", sessionFrame.getServerUrl() });
		JTable table = new JTable(tableModel);
		table.setDefaultRenderer(Object.class, new FieldCellRenderer());
		add(new JScrollPane(table), BorderLayout.CENTER);
		
	}
	
}
