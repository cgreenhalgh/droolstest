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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import uk.ac.horizon.ug.authorapp.BrowserPanel.BrowserTreeCellRenderer;
import uk.ac.horizon.ug.authorapp.BrowserPanel.TypeFilter;
import uk.ac.horizon.ug.authorapp.model.ClientSubscriptionInfo;
import uk.ac.horizon.ug.authorapp.model.ClientSubscriptionLifetimeType;
import uk.ac.horizon.ug.authorapp.model.ClientTypeInfo;
import uk.ac.horizon.ug.authorapp.model.Project;
import uk.ac.horizon.ug.authorapp.model.QueryConstraintInfo;
import uk.ac.horizon.ug.authorapp.model.QueryConstraintType;
import uk.ac.horizon.ug.authorapp.model.QueryInfo;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;
import uk.ac.horizon.ug.exserver.protocol.TypeFieldDescription;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription.TypeMetaKeys;
import uk.ac.horizon.ug.exserver.protocol.TypeFieldDescription.FieldMetaKeys;

/**
 * @author cmg
 *
 */
public class ClientTypePanel extends JPanel implements PropertyChangeListener {
	static Logger logger = Logger.getLogger(ClientTypePanel.class.getName());
	/** client type name */
	protected ClientTypeInfo clientTypeInfo;
	protected Project project;
	/** browser tree */
	protected JTree tree;
	/** tree model */
	protected DefaultTreeModel treeModel;
	/** root node */
	protected DefaultMutableTreeNode root;
	/** type subset panel */
	protected ListSubsetPanel typesPanel;

	/**
	 * @param type
	 */
	public ClientTypePanel(ClientTypeInfo clientTypeInfo, Project project) {
		super(new BorderLayout());
		this.clientTypeInfo = clientTypeInfo;

		JTabbedPane tabbedPane = new JTabbedPane();
		add(tabbedPane, BorderLayout.CENTER);
		
		JPanel facetsPanel = new JPanel(new BorderLayout());
		tabbedPane.add("Facets", facetsPanel);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		facetsPanel.add(splitPane, BorderLayout.CENTER);

		List<String> selectedTypes = new LinkedList<String>();
		List<String> allTypes = new LinkedList<String>();
		typesPanel = new ListSubsetPanel();
		typesPanel.addPropertyChangeListener("selectedListModel", this);
		splitPane.setTopComponent(typesPanel);
		
		root = new DefaultMutableTreeNode("Root");
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(new BrowserPanel.BrowserTreeCellRenderer());
		splitPane.setBottomComponent(new JScrollPane(tree));

		// TODO subscriptions panel
		JPanel subscriptionsPanel = new JPanel(new BorderLayout());
		tabbedPane.add("Subscriptions", subscriptionsPanel);
		final ClientSubscriptionTableModel subscriptionModel = new ClientSubscriptionTableModel(clientTypeInfo.getSubscriptions(), project);
		final JTable subscriptionTable = new JTable(subscriptionModel);
		ClientSubscriptionTableModel.setDefaultRenderers(subscriptionTable);
		subscriptionsPanel.add(new JScrollPane(subscriptionTable), BorderLayout.CENTER);
		
		JPanel buttons;
		buttons = new JPanel(new FlowLayout());
		subscriptionsPanel.add(buttons, BorderLayout.SOUTH);
		buttons.add(new JButton(new AbstractAction("Add New subscription") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ClientSubscriptionInfo subscription = new ClientSubscriptionInfo();
				subscription.setActive(true);
				subscription.setLifetime(ClientSubscriptionLifetimeType.CONVERSATION);
				//subscription.setDeleteAllowed(true);
				//subscription.setUpdateAllowed(true);
				subscription.setMatchExisting(true);
				QueryInfo pattern = new QueryInfo();
				subscription.setPattern(pattern);
				//pattern.setTypeName(type.getTypeName());
				ClientTypePanel.this.clientTypeInfo.getSubscriptions().add(subscription);
				ClientTypePanel.this.project.setChanged(true);
				subscriptionModel.fireTableDataChanged();
			}
		}));
		buttons.add(new JButton(new AbstractAction("Create default") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				createDefaultSubscriptions();
				ClientTypePanel.this.project.setChanged(true);
				subscriptionModel.fireTableDataChanged();
			}
		}));
		buttons.add(new JButton(new AbstractAction("Delete selected") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int rows[] = subscriptionTable.getSelectedRows();
				// Java 1.6! (rowindextomodel)
				// Pre-1.6 - no TableFiler
				for (int i=0; i<rows.length; i++) {
					//logger.info("Delete selected view row "+rows[i]);
					rows[i] = subscriptionTable.convertRowIndexToModel(rows[i]);
					//logger.info("=> delete selected model row "+rows[i]);
				}				
				// in reverse order so we don't shift positions of ones removed later
				Arrays.sort(rows);
				for (int i=rows.length-1; i>=0; i--) {
					ClientTypePanel.this.clientTypeInfo.getSubscriptions().remove(rows[i]);
					ClientTypePanel.this.project.setChanged(true);
				}
				subscriptionModel.fireTableDataChanged();
			}			
		}));
		
		// TODO publication filter panel
		
		refresh(project);
	}
	/** create default subscriptions for current client type(s) */
	protected void createDefaultSubscriptions() {
		// check @message @to...
		createSubscriptions(TypeDescription.TypeMetaKeys.message, TypeFieldDescription.FieldMetaKeys.to, clientTypeInfo.getClientTypeNames());
	}
	private void createSubscriptions(TypeMetaKeys requiredTypeKey, FieldMetaKeys requiredFieldReferenceKey,
			List<String> clientTypeNames) {
		List<TypeDescription> types = project.getTypes();
		nexttype:
		for (TypeDescription type : types) {
			if (!type.getTypeMeta().containsKey(requiredTypeKey.name()))
				continue nexttype;
			String fieldName = null;
			nextfield:
			for (Map.Entry<String,TypeFieldDescription> field : type.getFields().entrySet()) {
				if (!field.getValue().getFieldMeta().containsKey(requiredFieldReferenceKey.name()))
					continue nextfield;
				// all refs in named field (not fk / foreign key)?!
				String value = field.getValue().getFieldMeta().get(requiredFieldReferenceKey.name());
				if (value==null) 
					continue nextfield;
				//logger.info("Checking field "+field.getKey()+" of type "+type.getTypeName()+" for metadata "+requiredFieldReferenceKey+"="+value+" for type name "+clientType.getTypeName());
				String values [] = value.split("[\", |]");
				for (int i=0; i<values.length; i++) 
					if (clientTypeNames.contains(values[i]))
					{
						//logger.info("Found");
						fieldName = field.getKey();
						break nextfield;
					}
					//else
					//	logger.info("Not found ("+clientType.getTypeName()+") in ["+i+"]: "+values[i]);
			}
			if (fieldName!=null) {
				// found!
				clientTypeInfo.getSubscriptions().add(newSubscription(type, fieldName, QueryConstraintType.EQUAL_TO_CLIENT_ID, null));
			}
		}
	}
	/** new subscription */
	protected static ClientSubscriptionInfo newSubscription(TypeDescription type, String fieldName, QueryConstraintType constraintType, String parameter) {
		ClientSubscriptionInfo subscription = new ClientSubscriptionInfo();
		subscription.setActive(true);
		subscription.setLifetime(ClientSubscriptionLifetimeType.CLIENT);
		subscription.setDeleteAllowed(true);
		subscription.setUpdateAllowed(true);
		subscription.setMatchExisting(true);
		QueryInfo pattern = new QueryInfo();
		subscription.setPattern(pattern);
		pattern.setTypeName(type.getTypeName());
		QueryConstraintInfo constraint = new QueryConstraintInfo();
		pattern.getConstraints().add(constraint);
		constraint.setFieldName(fieldName);
		constraint.setConstraintType(constraintType);
		constraint.setParameter(parameter);
		return subscription;
	}
	/** name */
	public String getName() {
		return clientTypeInfo.getName();
	}

	/** refresh tree */
	public void refresh(Project project) {
		this.project = project;
		
		typesPanel.clear();
		
		if(project!=null && project.getTypes()!=null) {
			List<TypeDescription> types = project.getTypes();
			DefaultListModel selectedListModel = typesPanel.getSelectedListModel();
			DefaultListModel unselectedListModel = typesPanel.getUnselectedListModel();
			for (String clientTypeName : this.clientTypeInfo.getClientTypeNames())
				selectedListModel.addElement(clientTypeName);
			for (TypeDescription type : types)
				if (type.isClient() && !selectedListModel.contains(type.getTypeName()))
					unselectedListModel.addElement(type.getTypeName());
		}
		refreshTree();
	}
	void refreshTree() {
		root.removeAllChildren();
		
		if (project!=null && project.getTypes()!=null) {
			List<TypeDescription> types = project.getTypes();
			List<String> clientTypeNames = this.clientTypeInfo.getClientTypeNames();
			for (TypeDescription type : types) {
				if (clientTypeNames.contains(type.getTypeName()))
					root.add(BrowserPanel.makeClientTypeNode(type, types));
			}
			treeModel.setRoot(root);
		}
		treeModel.reload();

	}
	@Override
	public void propertyChange(PropertyChangeEvent pce) {
		DefaultListModel selectedListModel = typesPanel.getSelectedListModel();
		List<String> clientTypeNames = this.clientTypeInfo.getClientTypeNames();
		List<String> oldTypeNames = new LinkedList<String>();
		oldTypeNames.addAll(clientTypeNames);
		for (String clientTypeName : oldTypeNames)
			if (!selectedListModel.contains(clientTypeName)) {
				// remove it
				clientTypeNames.remove(clientTypeName);
				project.setChanged(true);
			}

		for (int i=0; i<selectedListModel.size(); i++) {
			String item = (String)selectedListModel.elementAt(i);
			if (!clientTypeNames.contains(item)) {
				clientTypeNames.add(item);
				project.setChanged(true);
			}
		}
		refreshTree();
	}
	
}
