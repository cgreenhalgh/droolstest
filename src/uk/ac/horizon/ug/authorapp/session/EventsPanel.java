/**
 * 
 */
package uk.ac.horizon.ug.authorapp.session;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.drools.event.rule.ActivationCancelledEvent;
import org.drools.event.rule.ActivationCreatedEvent;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.AgendaEventListener;
import org.drools.event.rule.AgendaGroupPoppedEvent;
import org.drools.event.rule.AgendaGroupPushedEvent;
import org.drools.event.rule.BeforeActivationFiredEvent;
import org.drools.event.rule.ObjectInsertedEvent;
import org.drools.event.rule.ObjectRetractedEvent;
import org.drools.event.rule.ObjectUpdatedEvent;
import org.drools.event.rule.WorkingMemoryEventListener;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 * @author cmg
 *
 */
public class EventsPanel extends JPanel implements WorkingMemoryEventListener, AgendaEventListener {
	/** ksession */
	protected StatefulKnowledgeSession ksession;
	/** tabel model */
	protected EventTableModel tableModel;

	/**
	 * @param ksession
	 */
	public EventsPanel(StatefulKnowledgeSession ksession) {
		super(new BorderLayout());
		this.ksession = ksession;
		ksession.addEventListener((WorkingMemoryEventListener)this);
		ksession.addEventListener((AgendaEventListener)this);
		
		tableModel = new EventTableModel();
		JTable table = new JTable(tableModel);
		add(new JScrollPane(table), BorderLayout.CENTER);
		
		JPanel buttons = new JPanel(new FlowLayout());
		add(buttons, BorderLayout.SOUTH);
		buttons.add(new JButton(new AbstractAction("Clear") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tableModel.clear();
			}
		}));
	}
	
	/** close */
	public void dispose() {
		ksession.removeEventListener((WorkingMemoryEventListener)this);
		ksession.removeEventListener((AgendaEventListener)this);
	}
	
	static final String COLUMNS [] = new String[] { "Time", "Event", "Value" };
	static class EventInfo {
		String time;
		String event;
		Object value;
	}
	static class EventTableModel extends AbstractTableModel {
		/** events */
		protected List<EventInfo> events = new LinkedList<EventInfo>();
		protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		@Override
		public int getColumnCount() {
			return COLUMNS.length;
		}
		public void clear() {
			events.clear();
			this.fireTableDataChanged();
		}
		@Override
		public String getColumnName(int col) {
			return COLUMNS[col];
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		@Override
		public int getRowCount() {
			return events.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			EventInfo ei = events.get(row);
			switch(col) {
			case 0: // time
				return ei.time;
			case 1: // event
				return ei.event;
			case 2: // value
				return ei.value;
			}
			return null;
		}
		/** non swing */
		public void addEvent(String event, Object value) {
			final EventInfo ei = new EventInfo();
			ei.event = event;
			ei.value = value;
			ei.time = dateFormat.format(new Date());
			Runnable doit = new Runnable() {
				public void run() {
					int size = events.size();
					events.add(ei);
					EventTableModel.this.fireTableRowsInserted(size, size);
				}
			};
			if (SwingUtilities.isEventDispatchThread())
				doit.run();
			else
				SwingUtilities.invokeLater(doit);
		}
	}
	/** WorkingMemoryEventListener */
	@Override
	public void objectInserted(ObjectInsertedEvent ev) {
		tableModel.addEvent("Object inserted", ev);
	}

	/** WorkingMemoryEventListener */
	@Override
	public void objectRetracted(ObjectRetractedEvent ev) {
		tableModel.addEvent("Object retracted", ev);
	}

	/** WorkingMemoryEventListener */
	@Override
	public void objectUpdated(ObjectUpdatedEvent ev) {
		tableModel.addEvent("Object updated", ev);
	}

	@Override
	public void activationCancelled(ActivationCancelledEvent ev) {
		tableModel.addEvent("Activation cancelled", ev);
	}

	@Override
	public void activationCreated(ActivationCreatedEvent ev) {
		tableModel.addEvent("Activation created", ev);
	}

	@Override
	public void afterActivationFired(AfterActivationFiredEvent ev) {
		tableModel.addEvent("Activation fired", ev);
	}

	@Override
	public void agendaGroupPopped(AgendaGroupPoppedEvent ev) {
		tableModel.addEvent("Activation group popped", ev);
	}

	@Override
	public void agendaGroupPushed(AgendaGroupPushedEvent ev) {
		tableModel.addEvent("Activation group pushed", ev);
	}

	@Override
	public void beforeActivationFired(BeforeActivationFiredEvent ev) {
		tableModel.addEvent("Activation before fired", ev);
	}
}
