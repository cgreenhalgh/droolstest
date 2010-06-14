/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import uk.ac.horizon.ug.authorapp.model.Project;
import uk.ac.horizon.ug.exserver.devclient.Fact;
//import uk.ac.horizon.ug.exserver.devclient.FactTableModel;
import uk.ac.horizon.ug.exserver.protocol.Operation;
import uk.ac.horizon.ug.exserver.protocol.RawFactHolder;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;

/** Table view for an Entity type.
 * 
 * @author cmg
 *
 */
public class EntityTablePanel extends JPanel implements PropertyChangeListener {
	static Logger logger = Logger.getLogger(EntityTableModel.class.getName());
	/** type name */
	protected String typeName;
	/** project */
	protected Project project;
	/** client type */
	protected TypeDescription type;
	/** type subset panel */
	protected ListSubsetPanel typesPanel;
	protected EntityTableModel model;
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
		
		typesPanel = new ListSubsetPanel();
		splitPane.setTopComponent(typesPanel);
		typesPanel.addPropertyChangeListener("selectedListModel", this);
		//splitPane.setResizeWeight(0.25);
		
		model = new EntityTableModel(type, new LinkedList<TypeDescription>(), null);
		table = new JTable(model);
		// Java 1.6
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
		table.setRowSorter(sorter);

		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setDefaultRenderer(Object.class, new FieldCellRenderer());
		splitPane.setBottomComponent(new JScrollPane(table));
		JPanel buttons = new JPanel(new FlowLayout());
		buttons.add(new JButton(new AbstractAction("Add fact") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				Fact fact = new Fact();
				fact.setNamespace(EntityTablePanel.this.type.getNamespace());
				fact.setTypeName(EntityTablePanel.this.type.getTypeName());
				fact.setFieldValues(new HashMap<String,Object>());
				// TODO ID...
				String idFieldName = EntityTablePanel.this.type.getIdFieldName();
				if (idFieldName==null)
					idFieldName = EntityTablePanel.this.type.getSubjectFieldName();
				if (idFieldName!=null) {
					String defaultId = "";
					String id = JOptionPane.showInputDialog(EntityTablePanel.this, "Enter new ID:", defaultId);
					if (id==null)
						return;
					fact.getFieldValues().put(idFieldName, id);
				}
				addFact(fact);
			}
		}));
		buttons.add(new JButton(new AbstractAction("Delete selected") {
			@Override
			public void actionPerformed(ActionEvent e) {
				int rows[] = table.getSelectedRows();
				Arrays.sort(rows);
				for (int i=rows.length-1; i>=0; i--) {
					// TODO includeSubFacts?
					// Java 1.6! (rowindextomodel)
					model.deleteRow(table.convertRowIndexToModel(rows[i]), false);
					// Pre-1.6 - no TableFiler: 
					// model.deleteRow(rows[i], false);
				}
			}
		}));
		add(buttons, BorderLayout.SOUTH);
		
		splitPane.setAlignmentY(0.5f);
		
		refresh(project, type);
	}

	void clear() {
		model.clear();
	}
	void addFact(Fact fact) {
		try {
			model.addFact(fact);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Add fact", JOptionPane.ERROR_MESSAGE);
		}
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
		typesPanel.clear();

		List<String> selectedTypes = new LinkedList<String>();
		List<String> allTypes = new LinkedList<String>();
		List<TypeDescription> types = project.getTypes();
		selectedTypes.add(typeName);
		//allTypes.add(typeName);

		if (types!=null) {
			// all entity & with subject types for now
			for (TypeDescription typeDesc : types) {
				if (typeDesc.isEntity() || typeDesc.getSubjectFieldName()!=null)
					allTypes.add(typeDesc.getTypeName());
			}
//		String types = type.getTypeMeta().get(TypeDescription.TypeMetaKeys.type.name());
//		if (types!=null) {
//			String typeNames[] = types.split("[, |\"]");
//			for (int i=0; i<typeNames.length; i++)
//				allTypes.add(typeNames[i]);
		}
		Collections.sort(selectedTypes);
		Collections.sort(allTypes);
		DefaultListModel selectedListModel = typesPanel.getSelectedListModel();
		DefaultListModel unselectedListModel = typesPanel.getUnselectedListModel();
		for (String item : selectedTypes)
			selectedListModel.addElement(item);
		for (String item : allTypes) 
			if (!selectedTypes.contains(item))
				unselectedListModel.addElement(item);

		refreshTable();
	}
	public void refreshTable() {
		DefaultListModel selectedListModel = typesPanel.getSelectedListModel();
		List<TypeDescription> facets = new LinkedList<TypeDescription>();
		for (int i=0; i<selectedListModel.size(); i++) 
		{
			String facetName = (String)selectedListModel.elementAt(i);
			TypeDescription facet = project.getTypeDescription(facetName);
			if (facet!=null)
				facets.add(facet);
			else
				logger.log(Level.WARNING, "Cannot find facet type "+facetName);
		}
		model = new EntityTableModel(type, facets, project.getProjectInfo().getDefaultFactStore());
		table.setModel(model);
		table.setColumnModel(model.getColumnModel());
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
		table.setRowSorter(sorter);
	}

	@Override
	public void propertyChange(PropertyChangeEvent pce) {
		refreshTable();
	}
	/** custom cell renderer, e.g. show full value as tooltip */
	static class FieldCellRenderer extends DefaultTableCellRenderer {

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
}
