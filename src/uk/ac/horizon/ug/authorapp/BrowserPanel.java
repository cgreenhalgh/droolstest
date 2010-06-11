/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

//import uk.ac.horizon.ug.authorapp.ClientTypePanel.ClientFilterObject;
import uk.ac.horizon.ug.authorapp.model.Project;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription;
import uk.ac.horizon.ug.exserver.protocol.TypeFieldDescription;
import uk.ac.horizon.ug.exserver.protocol.TypeDescription.TypeMetaKeys;
import uk.ac.horizon.ug.exserver.protocol.TypeFieldDescription.FieldMetaKeys;

/**
 * @author cmg
 *
 */
public class BrowserPanel extends JPanel implements PropertyChangeListener {
	static Logger logger = Logger.getLogger(BrowserPanel.class.getName());
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
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(new BrowserTreeCellRenderer());
		add(new JScrollPane(tree), BorderLayout.CENTER);
		
		setProject(project);
	}
	/** tree cell renderer */
	static class BrowserTreeCellRenderer extends DefaultTreeCellRenderer {
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
				else if (object instanceof ClientFilterObject)
				{
					leaf = false;
					value = ((ClientFilterObject)object).getTitle();
				}
				else if (object instanceof TypeDescription) {
					value = ((TypeDescription)object).getTypeName();
				}
			}
			return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,
					hasFocus);
		}		
	}
	/** get view client action - swing thread*/
	AbstractAction getViewAction(final Main main) {
		if (viewAction!=null)
			return viewAction;
		viewAction = new AbstractAction("View") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				TreePath path = tree.getSelectionPath();
				if (path==null || path.getPathCount()==0)
					return;
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
				Object object = node.getUserObject();
				if (!(object instanceof TypeDescription))
					return;
				TypeDescription type = (TypeDescription)object;
				if (type.isClient()) {
					// no-op?! main.openClientTypePanel(type);
				}
				else if (type.isEntity()) {
					main.openEntityTablePanel(type);
				}
			}
		};
		return viewAction;
	}
	/** view client action */
	protected AbstractAction viewAction;
	/** type file node user object type */
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
	public static final String CLIENTS = "Clients";
	/** create node as folder for types, filtered by metadata */
	public static DefaultMutableTreeNode makeFilteredTypesNode(String name, String metadataKeys[], List<TypeDescription> types) {
		TypeFilter filter = new TypeFilter(name, metadataKeys);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(filter);
		nexttype:
		for (TypeDescription type : types) {
			for (int i=0; i<metadataKeys.length; i++)
				if (!type.getTypeMeta().containsKey(metadataKeys[i]))
					continue nexttype;
			if (CLIENTS.equals(name))
				node.add(makeTypeNode(type, types));
			else
				node.add(makeSimpleTypeNode(type));
		}
		return node;
	}
	/** create node for type */
	public static DefaultMutableTreeNode makeTypeNode(TypeDescription type, List<TypeDescription> types) {
		if (type.isClient())
			return makeClientTypeNode(type, types);
		return makeSimpleTypeNode(type);
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
	
	/** client type filter node */
	static class ClientFilterObject {
		protected String title;
		protected TypeDescription clientType;
		/** type must have this metadata key */
		protected TypeDescription.TypeMetaKeys requiredTypeKey;
		/** a field my have this key and the clientType in the key value (list) */
		protected TypeFieldDescription.FieldMetaKeys requiredFieldReferenceKey;
		/**
		 * @param clientType
		 * @param requiredTypeKey
		 * @param requiredFieldReferenceKey
		 */
		public ClientFilterObject(String title, TypeDescription clientType,
				TypeMetaKeys requiredTypeKey,
				FieldMetaKeys requiredFieldReferenceKey) {
			super();
			this.title = title;
			this.clientType = clientType;
			this.requiredTypeKey = requiredTypeKey;
			this.requiredFieldReferenceKey = requiredFieldReferenceKey;
		}
		/**
		 * @return the title
		 */
		public String getTitle() {
			return title;
		}
		/**
		 * @return the clientType
		 */
		public TypeDescription getClientType() {
			return clientType;
		}
		/**
		 * @return the requiredTypeKey
		 */
		public TypeDescription.TypeMetaKeys getRequiredTypeKey() {
			return requiredTypeKey;
		}
		/**
		 * @return the requiredFieldReferenceKey
		 */
		public TypeFieldDescription.FieldMetaKeys getRequiredFieldReferenceKey() {
			return requiredFieldReferenceKey;
		}		
	}
	/** create node as folder for types, filtered by metadata */
	public static DefaultMutableTreeNode makeClientFilterNode(String title, TypeDescription clientType, TypeDescription.TypeMetaKeys requiredTypeKey, TypeFieldDescription.FieldMetaKeys requiredFieldReferenceKey, List<TypeDescription> types) {
		ClientFilterObject clientFilter = new ClientFilterObject(title, clientType, requiredTypeKey, requiredFieldReferenceKey);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(clientFilter);
		nexttype:
		for (TypeDescription type : types) {
			if (!type.getTypeMeta().containsKey(requiredTypeKey.name()))
				continue nexttype;
			String fieldName = null;
			nextfield:
			for (Map.Entry<String,TypeFieldDescription> field : type.getFields().entrySet()) {
				if (!field.getValue().getFieldMeta().containsKey(requiredFieldReferenceKey.name()))
					continue nextfield;
				// all refs in fk (foreign key)?!
				String value = field.getValue().getFieldMeta().get(TypeFieldDescription.FieldMetaKeys.fk.name());
				if (value==null) 
					continue nextfield;
				//logger.info("Checking field "+field.getKey()+" of type "+type.getTypeName()+" for metadata "+requiredFieldReferenceKey+"="+value+" for type name "+clientType.getTypeName());
				String values [] = value.split("[\", |]");
				for (int i=0; i<values.length; i++) 
					if (values[i].equals(clientType.getTypeName()))
					{
						//logger.info("Found");
						fieldName = field.getKey();
						break nextfield;
					}
					//else
					//	logger.info("Not found ("+clientType.getTypeName()+") in ["+i+"]: "+values[i]);
			}
			if (fieldName!=null) {
				node.add(makeSimpleTypeNode(type));
			}
		}
		return node;
	}
	/** create node for type */
	public static DefaultMutableTreeNode makeSimpleTypeNode(TypeDescription type) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(type);
		// TODO ....
		return node;
	}
	/** make client type node */
	public static DefaultMutableTreeNode makeClientTypeNode(TypeDescription type, List<TypeDescription> types) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(type);
		
		node.add(makeClientFilterNode("sensed", type, TypeDescription.TypeMetaKeys.sensed, TypeFieldDescription.FieldMetaKeys.subject, types));
		node.add(makeClientFilterNode("property", type, TypeDescription.TypeMetaKeys.property, TypeFieldDescription.FieldMetaKeys.subject, types));
		node.add(makeClientFilterNode("relationship", type, TypeDescription.TypeMetaKeys.relationship, TypeFieldDescription.FieldMetaKeys.subject, types));
		node.add(makeClientFilterNode("message/to", type, TypeDescription.TypeMetaKeys.message, TypeFieldDescription.FieldMetaKeys.to, types));
		node.add(makeClientFilterNode("message/from", type, TypeDescription.TypeMetaKeys.message, TypeFieldDescription.FieldMetaKeys.from, types));
		return node;
	}

	/** create node as folder for types, filtered by metadata */
	public static DefaultMutableTreeNode makeRangeTypesNode(String title, List<TypeDescription> types) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(title);
		Set<String> rangeNames = new TreeSet<String>();
		nexttype:
		for (TypeDescription type : types) {
			nextfield:
			for (Map.Entry<String,TypeFieldDescription> field : type.getFields().entrySet()) {
				String range = field.getValue().getFieldMeta().get(TypeFieldDescription.FieldMetaKeys.range.name());
				if (range==null) 
					continue nextfield;
				rangeNames.add(range);
			}
		}
		for (String rangeName : rangeNames)
			node.add(new DefaultMutableTreeNode(rangeName));
		return node;
	}

	void rebuildTree() {
		root.removeAllChildren();
		if (project!=null && project.getTypes()!=null) {
			List<TypeDescription> types = project.getTypes();
			root.add(makeFilteredTypesNode(CLIENTS, new String[]{TypeDescription.TypeMetaKeys.client.name()}, types));
			root.add(makeFilteredTypesNode("Physical Entities", new String[]{TypeDescription.TypeMetaKeys.physical.name(), TypeDescription.TypeMetaKeys.entity.name()}, types));
			root.add(makeFilteredTypesNode("Digital Entities", new String[]{TypeDescription.TypeMetaKeys.digital.name(), TypeDescription.TypeMetaKeys.entity.name()}, types));
			root.add(makeFilteredTypesNode("Surveyed Entities", new String[]{TypeDescription.TypeMetaKeys.describedbysurvey.name(), TypeDescription.TypeMetaKeys.entity.name()}, types));
			root.add(makeFilteredTypesNode("Authored Entities", new String[]{TypeDescription.TypeMetaKeys.describedbyauthor.name(), TypeDescription.TypeMetaKeys.entity.name()}, types));
			root.add(makeFilteredTypesNode("Authored Properties", new String[]{TypeDescription.TypeMetaKeys.describedbyauthor.name(), TypeDescription.TypeMetaKeys.property.name()}, types));
			root.add(makeFilteredTypesNode("Authored Relationships", new String[]{TypeDescription.TypeMetaKeys.describedbyauthor.name(), TypeDescription.TypeMetaKeys.relationship.name()}, types));
			root.add(makeRangeTypesNode("Range Types", types));
		}
		treeModel.reload();
	}
}
