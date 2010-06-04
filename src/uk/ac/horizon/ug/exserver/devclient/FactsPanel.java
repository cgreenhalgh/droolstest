/**
 * 
 */
package uk.ac.horizon.ug.exserver.devclient;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import uk.ac.horizon.ug.exserver.model.Session;
import uk.ac.horizon.ug.exserver.protocol.Operation;
import uk.ac.horizon.ug.exserver.protocol.OperationResult;
import uk.ac.horizon.ug.exserver.protocol.RawFactHolder;
import uk.ac.horizon.ug.exserver.protocol.SessionBuildResult;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;

/**
 * @author cmg
 *
 */
public class FactsPanel extends JPanel {
	DevClientApplet applet;
	Session session;
	/** fact type tabbed pane */
	protected JTabbedPane factTypesPane;
	FactsPanel(DevClientApplet applet, Session session) {
		super(new BorderLayout());
		this.applet = applet;
		this.session = session;
		JPanel buttons = new JPanel(new FlowLayout());
		add(buttons, BorderLayout.NORTH);
		buttons.add(new JButton(new AbstractAction("Refresh Types") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				doRefreshTypes();
			}			
		}));
		buttons.add(new JButton(new AbstractAction("Refresh Facts") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				doRefreshFacts();
			}			
		}));
		buttons.add(new JButton(new AbstractAction("Make Changes") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				doMakeChanges();
			}			
		}));
		factTypesPane = new JTabbedPane();
		add(factTypesPane, BorderLayout.CENTER);
		// ....
	}
	/** type panel */
	class TypePanel extends JPanel {
		TypeDescription type;
		FactTableModel model;
		JTable table;
		TypePanel(final TypeDescription type) {
			super(new BorderLayout());
			this.type = type;
			model = new FactTableModel(type);
			table = new JTable(model);
			table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			add(new JScrollPane(table), BorderLayout.CENTER);
			JPanel buttons = new JPanel(new FlowLayout());
			buttons.add(new JButton(new AbstractAction("Add fact") {
				@Override
				public void actionPerformed(ActionEvent ae) {
					RawFactHolder fh = new RawFactHolder();
					Fact fact = new Fact();
					fact.setNamespace(type.getNamespace());
					fact.setTypeName(type.getTypeName());
					fact.setFieldValues(new HashMap<String,Object>());
					fh.setOperation(Operation.add);
					fh.setFact(fact);
					addFact(fh);
				}
			}));
			buttons.add(new JButton(new AbstractAction("Delete selected") {
				@Override
				public void actionPerformed(ActionEvent e) {
					int rows[] = table.getSelectedRows();
					Arrays.sort(rows);
					for (int i=rows.length-1; i>=0; i--) {
						if (model.markDelete(rows[i]))
							model.fireTableRowsDeleted(rows[i], rows[1]);
						else
							model.fireTableCellUpdated(rows[i], 1);
					}
				}
			}));
			add(buttons, BorderLayout.SOUTH);
		}
		void clear() {
			model.clear();
			model.fireTableDataChanged();
		}
		void addFact(RawFactHolder fh) {
			model.addFact(fh);
			model.fireTableRowsInserted(model.getRowCount()-1, model.getRowCount()-1);
		}
		boolean isChanged() {
			return model.isChanged();
		}
		List<RawFactHolder> getChanges() {
			return model.getChanges();
		}
	}
	/** make/send changes to server */
	void doMakeChanges() {
		boolean changed = false;
		LinkedList<RawFactHolder> changes = new LinkedList<RawFactHolder>();
		for (TypePanel tp : typePanels.values())
			if (tp.isChanged()) {
				changed = true;
				changes.addAll(tp.getChanges());
				break;
			}
		if (!changed)
			return;
		System.err.println("Make changes...");
		Cursor c = applet.getCursor();
		try {
			applet.setCursor(applet.getBusyCursor());
			Protocol p = applet.getProtocol();
			
			List<OperationResult> results = p.makeChanges(this.session.getId(), changes);
			
			for (TypePanel tp : typePanels.values())
				tp.clear();
		}
		catch (Exception e) {
			System.err.println("Error making changes: "+e);
			e.printStackTrace(System.err);
			JOptionPane.showMessageDialog(applet, "Error making changes: "+e, "Error making changes", JOptionPane.ERROR_MESSAGE);			
		}
		// restore cursor
		applet.setCursor(c);		
		doRefreshFacts();
	}
	/** type panels */
	protected Map<String,TypePanel> typePanels = new HashMap<String,TypePanel>();
	/** swing thread */
	void doRefreshTypes() {
		boolean changed = false;
		for (TypePanel tp : typePanels.values())
			if (tp.isChanged()) {
				changed = true;
				break;
			}
		if (changed) {
			int option = JOptionPane.showConfirmDialog(applet, "Facts changed: send changes?", "Refresh facts", JOptionPane.YES_NO_CANCEL_OPTION);
			if (option==JOptionPane.CANCEL_OPTION)
				return;
			if (option==JOptionPane.YES_OPTION) {
				doMakeChanges();
				return;
			}
		}
		System.err.println("Refresh types...");
		Cursor c = applet.getCursor();
		factTypesPane.removeAll();
		typePanels.clear();
		factTypesPane.revalidate();
		try {
			applet.setCursor(applet.getBusyCursor());
			Protocol p = applet.getProtocol();
			List<TypeDescription> types = p.getTypes(session.getId());
			for (TypeDescription type : types) {
				TypePanel typePanel = new TypePanel(type);
				typePanels.put(type.getTypeName(), typePanel);
				factTypesPane.add(type.getTypeName(), typePanel);
			}
			factTypesPane.revalidate();
		}
		catch (Exception e) {
			System.err.println("Error refreshing types: "+e);
			e.printStackTrace(System.err);
			JOptionPane.showMessageDialog(applet, "Error refreshing types: "+e, "Error refreshing types", JOptionPane.ERROR_MESSAGE);			
		}
		// restore cursor
		applet.setCursor(c);		
	}
	/** swing thread */
	void doRefreshFacts() {
		boolean changed = false;
		for (TypePanel tp : typePanels.values())
			if (tp.isChanged()) {
				changed = true;
				break;
			}
		if (changed) {
			int option = JOptionPane.showConfirmDialog(applet, "Facts changed: send changes?", "Refresh facts", JOptionPane.YES_NO_CANCEL_OPTION);
			if (option==JOptionPane.CANCEL_OPTION)
				return;
			if (option==JOptionPane.YES_OPTION) {
				doMakeChanges();
				return;
			}
		}
		System.err.println("Refresh facts...");
		Cursor c = applet.getCursor();
		for (TypePanel tp : typePanels.values()) {
			tp.clear();
		}
		try {
			applet.setCursor(applet.getBusyCursor());
			Protocol p = applet.getProtocol();
			List<RawFactHolder> facts = p.getFacts(session.getId());
			for (RawFactHolder fh : facts) {
				Fact fact = (Fact)fh.getFact();
				if (fact.getTypeName()==null) {
					System.err.println("ERROR: Fact without typeName");
					continue;
				}
				TypePanel fp = typePanels.get(fact.getTypeName());
				if (fp==null) {
					System.err.println("ERROR: No fact panel for type "+fact.getTypeName());
					continue;
				}
				fh.setOperation(Operation.ignore);
				fp.addFact(fh);
			}
		}
		catch (Exception e) {
			System.err.println("Error refreshing facts: "+e);
			e.printStackTrace(System.err);
			JOptionPane.showMessageDialog(applet, "Error refreshing facts: "+e, "Error refreshing types", JOptionPane.ERROR_MESSAGE);			
		}
		// restore cursor
		applet.setCursor(c);		
	}
}
