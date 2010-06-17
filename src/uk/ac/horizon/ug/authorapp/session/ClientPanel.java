/**
 * 
 */
package uk.ac.horizon.ug.authorapp.session;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import uk.ac.horizon.ug.authorapp.FieldCellRenderer;
import uk.ac.horizon.ug.exserver.clientapi.RegisterClientHandler;
import uk.ac.horizon.ug.exserver.model.ClientConversation;
import uk.ac.horizon.ug.exserver.model.ConversationStatus;

/**
 * @author cmg
 *
 */
public class ClientPanel extends JPanel {
	static Logger logger = Logger.getLogger(ClientPanel.class.getName());
	/** conversation */
	protected ClientConversation conversation;
	/** table model */
	protected DefaultTableModel tableModel;
	/**
	 * @param sessionFrame
	 */
	public ClientPanel(final ClientConversation conversation) {
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
		tableModel.addRow(new Object[] { "Status", conversation.getStatus() });
		JTable table = new JTable(tableModel);
		table.setDefaultRenderer(Object.class, new FieldCellRenderer());
		add(new JScrollPane(table), BorderLayout.CENTER);
		
		JPanel buttons = new JPanel(new FlowLayout());
		add(buttons, BorderLayout.SOUTH);
		buttons.add(new JButton(new AbstractAction("End (by client)") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				setConversationState(ConversationStatus.ENDED_BY_CLIENT);
			}
		}));
		buttons.add(new JButton(new AbstractAction("End (by lobby)") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				setConversationState(ConversationStatus.ENDED_BY_LOBBY);
			}
		}));
	}
	protected void setConversationState(ConversationStatus status) {
		conversation.setStatus(status);
		boolean ok = false;
		try {
			ok = RegisterClientHandler.registerInternal(conversation);
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Setting client status", e);
			ok = false;
		}
		if (!ok)
			JOptionPane.showMessageDialog(this, "Problem setting client status", "Set Client Status", JOptionPane.ERROR_MESSAGE);
		else {
			tableModel.setValueAt(conversation.getStatus(), 4, 1);
			tableModel.fireTableCellUpdated(4, 1);
		}
	}
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
}
