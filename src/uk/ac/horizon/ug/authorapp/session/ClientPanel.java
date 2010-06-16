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
import uk.ac.horizon.ug.exserver.model.ClientConversation;

/**
 * @author cmg
 *
 */
public class ClientPanel extends JPanel {
	/** conversation */
	protected ClientConversation conversation;
	/** table model */
	protected DefaultTableModel tableModel;
	/**
	 * @param sessionFrame
	 */
	public ClientPanel(ClientConversation conversation) {
		super(new BorderLayout());
		this.conversation = conversation;
		
		tableModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		tableModel.addColumn("Property");
		tableModel.addColumn("Value");
		tableModel.addRow(new Object[] { "Client Type", conversation.getClientType() });
		tableModel.addRow(new Object[] { "Client ID", conversation.getClientId() });
		tableModel.addRow(new Object[] { "Conversation ID", conversation.getConversationId() });
		tableModel.addRow(new Object[] { "Session ID", conversation.getSessionId() });
		JTable table = new JTable(tableModel);
		table.setDefaultRenderer(Object.class, new FieldCellRenderer());
		add(new JScrollPane(table), BorderLayout.CENTER);
		
	}
	
}
