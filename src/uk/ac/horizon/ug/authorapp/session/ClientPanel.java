/**
 * 
 */
package uk.ac.horizon.ug.authorapp.session;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import uk.ac.horizon.ug.authorapp.ClientSubscriptionTableModel;
import uk.ac.horizon.ug.authorapp.FieldCellRenderer;
//import uk.ac.horizon.ug.authorapp.ClientSubscriptionTableModel.Column;
import uk.ac.horizon.ug.authorapp.model.ClientSubscriptionInfo;
import uk.ac.horizon.ug.authorapp.model.ClientSubscriptionLifetimeType;
import uk.ac.horizon.ug.authorapp.model.QueryConstraintInfo;
import uk.ac.horizon.ug.authorapp.model.QueryConstraintType;
import uk.ac.horizon.ug.authorapp.model.QueryInfo;
import uk.ac.horizon.ug.exserver.clientapi.RegisterClientHandler;
import uk.ac.horizon.ug.exserver.clientapi.protocol.MessageType;
import uk.ac.horizon.ug.exserver.model.ClientConversation;
import uk.ac.horizon.ug.exserver.model.ConversationStatus;
import uk.ac.horizon.ug.exserver.model.DbUtils;
import uk.ac.horizon.ug.exserver.model.MessageToClient;

/**
 * @author cmg
 *
 */
public class ClientPanel extends JPanel {
	static Logger logger = Logger.getLogger(ClientPanel.class.getName());
	/** conversation */
	protected ClientConversation conversation;
	/** table model */
	protected DefaultTableModel propertyTableModel;
	protected MessagesTableModel messagesTableModel;
	/**
	 * @param sessionFrame
	 */
	public ClientPanel(final ClientConversation conversation) {
		super(new BorderLayout());
		this.conversation = conversation;
		
		JTabbedPane tabbedPane = new JTabbedPane();
		add(tabbedPane, BorderLayout.CENTER);
		
		// properties
		JPanel propertyPanel = new JPanel(new BorderLayout());
		tabbedPane.add(propertyPanel,"Properties");
		
		propertyTableModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		propertyTableModel.addColumn("Property");
		propertyTableModel.addColumn("Value");
		propertyTableModel.addRow(new Object[] { "Client Type", conversation.getClientType() });
		propertyTableModel.addRow(new Object[] { "Client ID", conversation.getClientId() });
		propertyTableModel.addRow(new Object[] { "Conversation ID", conversation.getConversationId() });
		propertyTableModel.addRow(new Object[] { "Session ID", conversation.getSessionId() });
		propertyTableModel.addRow(new Object[] { "Status", conversation.getStatus() });
		JTable table = new JTable(propertyTableModel);
		table.setDefaultRenderer(Object.class, new FieldCellRenderer());
		propertyPanel.add(new JScrollPane(table), BorderLayout.CENTER);
		
		JPanel buttons;
		buttons = new JPanel(new FlowLayout());
		propertyPanel.add(buttons, BorderLayout.SOUTH);
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
		
		// messages
		JPanel messagesPanel = new JPanel(new BorderLayout());
		tabbedPane.add(messagesPanel, "Messages");
		
		messagesTableModel = new MessagesTableModel();
		JTable messagesTable = new JTable(messagesTableModel);
		messagesTable.setDefaultRenderer(Object.class, new FieldCellRenderer());
		messagesPanel.add(new JScrollPane(messagesTable), BorderLayout.CENTER);
		
		buttons = new JPanel(new FlowLayout());
		messagesPanel.add(buttons, BorderLayout.SOUTH);
		buttons.add(new JButton(new AbstractAction("Refresh") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				refresh();
			}
		}));
		
		refresh();
	}
	/** swing */
	private void refresh() {
		EntityManager em = DbUtils.getEntityManager();
		Query q = em.createQuery ("SELECT x FROM MessageToClient x WHERE x.clientId = :clientId");
		q.setParameter("clientId", conversation.getClientId());
		List<MessageToClient> messages = (List<MessageToClient>) q.getResultList ();
		messagesTableModel.setMessages(messages);
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
			propertyTableModel.setValueAt(conversation.getStatus(), 4, 1);
			propertyTableModel.fireTableCellUpdated(4, 1);
		}
	}
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	static enum MessageColumn {
		ClientId(String.class, "clientId"),
		ConversationId(String.class, "conversationId"),
		SessionId(String.class, "sessionId"),
		SeqNo(Integer.class, "seqNo"),
		Type(MessageType.class, "type"),
		Time(Long.class, "time"),
		SubsIx(Integer.class, "subsIx"),
		OldValue(String.class, "oldVal"),
		NewValue(String.class, "newVal"),
		Handle(String.class, "handle"),
		Lifetime(ClientSubscriptionLifetimeType.class, "lifetime"),
		Sent(Long.class, "sentToClient"),
		Acked(Long.class, "ackedByClient");
		MessageColumn(Class clazz, String fieldName) {
			this.clazz = clazz;
			this.fieldName = fieldName;
		}
		private Class clazz;
		public Class clazz() { return clazz; }
		private String fieldName;
		public String fieldName() { return fieldName; }
	}
	static class MessagesTableModel extends AbstractTableModel {

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return MessageColumn.values()[columnIndex].clazz();
		}
		/** messages */
		protected MessageToClient[] messages;
		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int col) {
			return MessageColumn.values()[col].toString();
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}

		@Override
		public int getColumnCount() {
			return MessageColumn.values().length;
		}

		@Override
		public int getRowCount() {
			if (messages==null)
				return 0;
			return messages.length;
		}

		@Override
		public Object getValueAt(int row, int column) {
			MessageToClient msg = messages[row];
			MessageColumn col = MessageColumn.values()[column];
			return ClientSubscriptionTableModel.getFieldValue(msg, col.fieldName());
		}
		public void setMessages(Collection<MessageToClient> msgs) {
			messages = msgs.toArray(new MessageToClient[msgs.size()]);
			this.fireTableDataChanged();
		}
	}
}
