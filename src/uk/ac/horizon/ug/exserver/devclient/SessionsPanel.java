/**
 * 
 */
package uk.ac.horizon.ug.exserver.devclient;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JApplet;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import java.util.List;
import uk.ac.horizon.ug.exserver.model.Session;
import java.awt.Cursor;

/** Sessions.
 * 
 * @author cmg
 *
 */
public class SessionsPanel extends JPanel {
	DevClientApplet applet;
	TableModel model;
	JTable table;
	SessionsPanel(final DevClientApplet applet) {
		super(new BorderLayout());
		this.applet = applet;
		model = new TableModel();
		table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(new JScrollPane(table), BorderLayout.CENTER);
		JPanel buttons = new JPanel(new FlowLayout());
		add(buttons, BorderLayout.SOUTH);
		JButton refresh = new JButton(new AbstractAction("Refresh") {	
			public void actionPerformed(ActionEvent ae) {
				doRefresh();
			}
		});
		buttons.add(refresh);
		openAction = new AbstractAction("Open session") {
			public void actionPerformed(ActionEvent ae) {
				int row = table.getSelectedRow();
				if (row<0) 
					JOptionPane.showMessageDialog(applet, "Please select a session", "Open session", JOptionPane.ERROR_MESSAGE);
				else
					doOpen(sessions.get(row));
			}
		};
		//openAction.setEnabled(false);
		buttons.add(new JButton(openAction));
		System.err.println("SessionsPanel v1.0");
	}
	AbstractAction openAction;
	List<Session> sessions;
	// called in swing thread...
	void doRefresh() {
		System.err.println("Refresh...");
		Cursor c = applet.getCursor();
		try {
			table.clearSelection();
			applet.setCursor(applet.getBusyCursor());
			Protocol p = new Protocol(applet.getServer());
			sessions = p.getSessions();
			model.fireTableDataChanged();
		}
		catch (Exception e) {
			System.err.println("Error getting sessions: "+e);
			e.printStackTrace(System.err);
			JOptionPane.showMessageDialog(applet, "Error getting sessions: "+e, "Error gettings sessions", JOptionPane.ERROR_MESSAGE);			
		}
		// restore cursor
		applet.setCursor(c);
	}
	/** "open" a session - swing thread */
	void doOpen(Session session) {
		
	}
	static String COLUMN_NAMES[] = new String[] { "ID", "template", "created" };
	class TableModel extends AbstractTableModel {
		TableModel() {
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int col) {
			return COLUMN_NAMES[col];
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return COLUMN_NAMES.length;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
			if (sessions!=null)
				return sessions.size();
			return 0;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int row, int col) {
			Session s = sessions.get(row);
			switch(col) {
			case 0:
				// id
				return s.getId();
			case 1:
				// template
				return s.getTemplateName();
			case 2:
				//c reated
				return s.getCreatedDate().toString();
			}
			return null;
		}
		
	}
}
