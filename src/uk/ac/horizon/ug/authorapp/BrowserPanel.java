/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import uk.ac.horizon.ug.authorapp.model.Project;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;

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
		tree.setRootVisible(false);
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
					Object object = cell.getUserObject();
					if ("Root".equals(object))
						leaf = false;
					else if (object instanceof TypeFilter)
					{
						leaf = false;
						value = ((TypeFilter)object).getName();
					}
					else if (object instanceof TypeDescription) {
						value = ((TypeDescription)object).getTypeName();
					}
				}
				return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,
						hasFocus);
			}
			
		};
		tree.setCellRenderer(cellRenderer);
		add(new JScrollPane(tree), BorderLayout.CENTER);
		
		setProject(project);
	}
	static class TypeFilter {
		protected String name;
		protected String metadataKeys[];
		/**
		 * @param name
		 * @param metadataKey
		 */
		public TypeFilter(String name, String metadataKeys[]) {
			super();
			this.name = name;
			this.metadataKeys = metadataKeys;
		}
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		/**
		 * @return the metadataKey
		 */
		public String[] getMetadataKeys() {
			return metadataKeys;
		}
	}
	/** create node as folder for types, filtered by metadata */
	public static DefaultMutableTreeNode makeFilteredTypesNode(String name, String metadataKeys[], List<TypeDescription> types) {
		TypeFilter filter = new TypeFilter(name, metadataKeys);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(filter);
		nexttype:
		for (TypeDescription type : types) {
			for (int i=0; i<metadataKeys.length; i++)
				if (!type.getTypeMeta().containsKey(metadataKeys[i]))
					continue nexttype;
			node.add(makeTypeNode(type));
		}
		return node;
	}
	/** create node for type */
	public static DefaultMutableTreeNode makeTypeNode(TypeDescription type) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(type);
		// TODO ....
		return node;
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
		if (project!=null && project.getTypes()!=null) {
			List<TypeDescription> types = project.getTypes();
			root.add(makeFilteredTypesNode("Physical Entities", new String[]{TypeDescription.TypeMetaKeys.physical.name(), TypeDescription.TypeMetaKeys.entity.name()}, types));
			root.add(makeFilteredTypesNode("Digital Entities", new String[]{TypeDescription.TypeMetaKeys.digital.name(), TypeDescription.TypeMetaKeys.entity.name()}, types));
			root.add(makeFilteredTypesNode("Authored Relationships", new String[]{TypeDescription.TypeMetaKeys.describedbyauthor.name(), TypeDescription.TypeMetaKeys.relationship.name()}, types));
		}
		treeModel.reload();
	}
}
