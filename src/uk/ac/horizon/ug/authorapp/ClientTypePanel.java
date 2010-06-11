/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import uk.ac.horizon.ug.authorapp.BrowserPanel.BrowserTreeCellRenderer;
import uk.ac.horizon.ug.authorapp.BrowserPanel.TypeFilter;
import uk.ac.horizon.ug.authorapp.model.ClientTypeInfo;
import uk.ac.horizon.ug.authorapp.model.Project;
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

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, BorderLayout.CENTER);

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

		refresh(project);
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
