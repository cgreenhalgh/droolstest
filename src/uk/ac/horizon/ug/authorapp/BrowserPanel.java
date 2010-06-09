/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import uk.ac.horizon.ug.authorapp.model.Project;

/**
 * @author cmg
 *
 */
public class BrowserPanel extends JPanel implements PropertyChangeListener {
	/** project */
	protected Project project;
	/** browser tree */
	protected JTree tree;
	/** tree model */
	protected DefaultTreeModel treeModel;
	/** root node */
	protected DefaultMutableTreeNode root;
	/**
	 * @param project
	 */
	public BrowserPanel(Project project) {
		super(new BorderLayout());
		root = new DefaultMutableTreeNode("Root");
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		DefaultTreeCellRenderer cellRenderer = new DefaultTreeCellRenderer() {

			/* (non-Javadoc)
			 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
			 */
			@Override
			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean sel, boolean expanded, boolean leaf,
					int row, boolean hasFocus) {
				if (value instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode cell = (DefaultMutableTreeNode)value;
					if ("Root".equals(cell.getUserObject()))
						leaf = false;
				}
				return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,
						hasFocus);
			}
			
		};
		tree.setCellRenderer(cellRenderer);
		add(new JScrollPane(tree), BorderLayout.CENTER);
		
		setProject(project);
	}

	/**
	 * @param project the project to set
	 */
	public void setProject(Project project) {
		if (this.project!=null)
			project.removePropertyChangeListener("types", this);
		this.project = project;
		if (project!=null) 
			project.addPropertyChangeListener("types", this);
		rebuildTree();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("types".equals(evt.getPropertyName()))
			// refresh types view
			rebuildTree();
	}
	
	void rebuildTree() {
		root.removeAllChildren();
		treeModel.reload();
	}
}
