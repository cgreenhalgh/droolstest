/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import uk.ac.horizon.ug.authorapp.model.Project;
import uk.ac.horizon.ug.exserver.devclient.Fact;
import uk.ac.horizon.ug.exserver.devclient.FactTableModel;
import uk.ac.horizon.ug.exserver.protocol.Operation;
import uk.ac.horizon.ug.exserver.protocol.RawFactHolder;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;

/** Table view for an Entity type.
 * 
 * @author cmg
 *
 */
public class EntityTablePanel extends JPanel {
	/** type name */
	protected String typeName;
	/** project */
	protected Project project;
	/** client type */
	protected TypeDescription type;
	/** type subset panel */
	protected ListSubsetPanel typesPanel;
	protected FactTableModel model;
	protected JTable table;

	/**
	 * @param typeName
	 * @param project
	 * @param type
	 */
	public EntityTablePanel(String typeName, Project project,
			TypeDescription type) {
		super(new BorderLayout());
		this.typeName = typeName;

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, BorderLayout.CENTER);

		// TODO refresh...?
		List<String> selectedTypes = new LinkedList<String>();
		List<String> allTypes = new LinkedList<String>();
		selectedTypes.add(typeName);
		allTypes.add(typeName);
		String types = type.getTypeMeta().get(TypeDescription.TypeMetaKeys.type.name());
		if (types!=null) {
			String typeNames[] = types.split("[, |\"]");
			for (int i=0; i<typeNames.length; i++)
				allTypes.add(typeNames[i]);
		}
		
		typesPanel = new ListSubsetPanel(allTypes, selectedTypes);
		splitPane.setTopComponent(typesPanel);
		
		model = new FactTableModel(type);
		table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		splitPane.setBottomComponent(new JScrollPane(table));
		JPanel buttons = new JPanel(new FlowLayout());
		buttons.add(new JButton(new AbstractAction("Add fact") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				RawFactHolder fh = new RawFactHolder();
				Fact fact = new Fact();
				fact.setNamespace(EntityTablePanel.this.type.getNamespace());
				fact.setTypeName(EntityTablePanel.this.type.getTypeName());
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
		
		splitPane.setAlignmentY(0.5f);
		
		refresh(project, type);
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
	/**
	 * @return the typeName
	 */
	public String getTypeName() {
		return typeName;
	}

	/** refresh */
	public void refresh(Project project, TypeDescription type ) {
		this.project = project;
		this.type = type; 
		// TODO ...
	}
}
