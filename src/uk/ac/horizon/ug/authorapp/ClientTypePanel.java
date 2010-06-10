/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import uk.ac.horizon.ug.authorapp.BrowserPanel.BrowserTreeCellRenderer;
import uk.ac.horizon.ug.authorapp.BrowserPanel.TypeFilter;
import uk.ac.horizon.ug.authorapp.model.Project;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;
import uk.ac.horizon.ug.exserver.protocol.TypeFieldDescription;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription.TypeMetaKeys;
import uk.ac.horizon.ug.exserver.protocol.TypeFieldDescription.FieldMetaKeys;

/**
 * @author cmg
 *
 */
public class ClientTypePanel extends JPanel {
	static Logger logger = Logger.getLogger(ClientTypePanel.class.getName());
	/** client type name */
	protected String typeName;
	protected Project project;
	/** client type */
	protected TypeDescription type;
	/** browser tree */
	protected JTree tree;
	/** tree model */
	protected DefaultTreeModel treeModel;
	/** root node */
	protected DefaultMutableTreeNode root;

	/**
	 * @param type
	 */
	public ClientTypePanel(String typeName, Project project, TypeDescription type) {
		super(new BorderLayout());
		this.typeName = typeName;
		
		root = new DefaultMutableTreeNode("Root");
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(new BrowserPanel.BrowserTreeCellRenderer());
		add(new JScrollPane(tree), BorderLayout.CENTER);

		setType(project, type);
	}
	/** name */
	public String getName() {
		return typeName;
	}
	/**
	 * @return the type
	 */
	public TypeDescription getType() {
		return type;
	}

	/**
	 * @param project2 
	 * @param type the type to set
	 */
	public void setType(Project project2, TypeDescription type) {
		this.project = project2;
		this.type = type;
		refresh();
	}

	/** refresh tree */
	private void refresh() {
		// TODO Auto-generated method stub
		root.removeAllChildren();
		
		if (type!=null && project!=null && project.getTypes()!=null) {
			List<TypeDescription> types = project.getTypes();
			root = BrowserPanel.makeClientTypeNode(type, types);
			treeModel.setRoot(root);
		}
		treeModel.reload();

	}
	
}
