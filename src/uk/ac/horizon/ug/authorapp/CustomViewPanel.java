/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;

import uk.ac.horizon.ug.authorapp.customview.AbstractViewItem;
import uk.ac.horizon.ug.authorapp.customview.DefaultViewItem;
import uk.ac.horizon.ug.authorapp.customview.ViewBuilder;
import uk.ac.horizon.ug.authorapp.customview.ViewCanvas;
import uk.ac.horizon.ug.authorapp.customview.ViewItemPaletteCanvas;
import uk.ac.horizon.ug.authorapp.model.CustomViewInfo;
import uk.ac.horizon.ug.authorapp.model.Project;
import uk.ac.horizon.ug.authorapp.model.ViewItemSetInfo;
import uk.ac.horizon.ug.authorapp.model.ViewLayerInfo;
import uk.ac.horizon.ug.authorapp.model.ViewLayoutInfo;

/** A Custom View...
 * 
 * ViewArea <-  ViewLayer*  <-ViewItemLayout<- ViewItemSet*  <-   EntitySet  <- Entity* EntitySet*
 *                                                           <-   ViewItem* (<-  EntityView  <- Entity)
 *                                                
 * @author cmg
 *
 */
public class CustomViewPanel extends JPanel implements PropertyChangeListener {
	static Logger logger = Logger.getLogger(CustomViewPanel.class.getName());
	/** project */
	protected Project project;
	/** custom view info */
	protected CustomViewInfo customViewInfo;
	/** view canvas */
	protected ViewCanvas viewCanvas;
	/** view item palette */
	protected ViewItemPaletteCanvas paletteCanvas;
	/** cons 
	 * @param cvi */
	public CustomViewPanel(Project project, CustomViewInfo cvi) {
		super(new BorderLayout());
		this.customViewInfo = cvi;
		this.project = project;
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, BorderLayout.CENTER);
		splitPane.setResizeWeight(0.1);
		
		paletteCanvas = new ViewItemPaletteCanvas();
		splitPane.setTopComponent(new JScrollPane(paletteCanvas));
		
		JPanel bottomPane = new JPanel(new BorderLayout());
		splitPane.setBottomComponent(bottomPane);
		
		viewCanvas = new ViewCanvas();
		final JScrollPane viewScrollPane = new JScrollPane(viewCanvas);
		viewScrollPane.setWheelScrollingEnabled(false);
		viewScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		viewScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		bottomPane.add(viewScrollPane, BorderLayout.CENTER);
		JPanel buttons = new JPanel (new FlowLayout());
		bottomPane.add(buttons, BorderLayout.SOUTH);
		buttons.add(new JButton(new AbstractAction("Zoom in") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				zoomIn();
			}
			
		}));
		buttons.add(new JButton(new AbstractAction("Zoom out") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				zoomOut();
			}
			
		}));
		viewScrollPane.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent ev) {
				// TODO Auto-generated method stub
				int rots = ev.getWheelRotation();
				for (int i=0; i<rots; i++)
					zoomIn();
				for (int i=0; i>rots; i--)
					zoomOut();
			}
			
		});
		
		viewBuilder = ViewBuilder.getViewBuilder();
		// can't create usefully in construction as view component doesn't exist
		// to size things
		//refresh();
	}
	protected void zoomOut() {
		Rectangle visible = viewCanvas.getVisibleRect();//viewScrollPane.getV
		Rectangle contentRect = new Rectangle((int)(visible.getCenterX()/2-visible.getWidth()/2), (int)(visible.getCenterY()/2-visible.getHeight()/2), (int)(visible.getWidth()), (int)(visible.getHeight()));
		viewCanvas.setZoomRatio(viewCanvas.getZoomRatio()/2);
		if (viewCanvas.getZoomRatio()>0.55 && viewCanvas.getZoomRatio()<1.9) 
			viewCanvas.setZoomRatio(1);
		viewCanvas.scrollRectToVisible(contentRect);				
		logger.info("Scroll visible="+visible+" to "+contentRect);

	}
	protected void zoomIn() {
		//viewScrollPane.getViewport().scrollRectToVisible(contentRect)
		Rectangle visible = viewCanvas.getVisibleRect();//viewScrollPane.getV
		Rectangle contentRect = new Rectangle((int)(2*visible.getCenterX()-visible.getWidth()/2), (int)(2*visible.getCenterY()-visible.getHeight()/2), (int)(visible.getWidth()), (int)(visible.getHeight()));
		viewCanvas.setZoomRatio(viewCanvas.getZoomRatio()*2);
		viewCanvas.scrollRectToVisible(contentRect);
		logger.info("Scroll visible="+visible+" to "+contentRect);
	}
	/** view builder */
	protected ViewBuilder viewBuilder;
	/** regenerate view */
	public void refresh() {
		if (viewCanvas.getMaxx()!=customViewInfo.getMinimumWidth())
			viewCanvas.setMaxx(customViewInfo.getMinimumWidth());
		if (viewCanvas.getMaxy()!=customViewInfo.getMinimumHeight())
			viewCanvas.setMaxy(customViewInfo.getMinimumHeight());
		
		List<List<AbstractViewItem>> items2 = new LinkedList<List<AbstractViewItem>>();
		if (project.getTypes()!=null)
			items2 = viewBuilder.getView(project, customViewInfo, viewCanvas);
		viewCanvas.setViewItems(items2);
		viewCanvas.repaint();
		paletteCanvas.setViewItems(items2);
		paletteCanvas.updateItems();
	} 
	/** config dialog */
	protected JDialog configDialog;
	/** properties bean */
	protected PropertiesBean propertiesBean;
	/** config tabbed pane */
	protected JTabbedPane configTabbedPane;
	/** show configuration dialog */
	public void showConfigDialog() {
		if (configDialog==null)
			createConfigDialog();
		configDialog.setVisible(true);
	}
	/** create dialog */
	protected JDialog createConfigDialog() {
		configDialog = new JDialog();
		configDialog.setLocationRelativeTo(this);
		configDialog.setModal(true);
		configDialog.setTitle("Configure view: "+customViewInfo.getName());
		configDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		configDialog.addWindowListener(new WindowAdapter() {

			/* (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowClosing(WindowEvent arg0) {
				// TODO Auto-generated method stub
				super.windowClosing(arg0);
				configDialog.setVisible(false);
				refresh();
			}
			
		});
		JPanel p = new JPanel(new BorderLayout());
		
		JPanel buttons = new JPanel(new FlowLayout());
		p.add(buttons, BorderLayout.SOUTH);
		
		configTabbedPane = new JTabbedPane();
		p.add(configTabbedPane, BorderLayout.CENTER);
		
		try {
			propertiesBean = new PropertiesBean(this.customViewInfo);
			propertiesBean.addPropertyChangeListener(this);
			AbstractTableModel propertiesModel = new BeanPropertiesTableModel<PropertiesBean>(propertiesBean);
			JTable propertiesTable = new JTable(propertiesModel);
			configTabbedPane.add("Properties", new JScrollPane(propertiesTable));
		} catch (Exception e) {
			logger.log(Level.WARNING, "Problem adding properties view", e);
		}
		this.layerTableModel = new LayerTableModel();
		final JTable layerTable = new JTable(layerTableModel);
		JPanel layerPane = new JPanel(new BorderLayout());
		layerPane.add(new JScrollPane(layerTable), BorderLayout.CENTER);
		configTabbedPane.add("Layers", layerPane);
		JPanel layerButtons = new JPanel(new FlowLayout());
		layerPane.add(layerButtons, BorderLayout.SOUTH);
		
		layoutTableModel = new LayoutTableModel();
		final JTable layoutTable =new JTable(layoutTableModel);
		JPanel layoutPane = new JPanel(new BorderLayout());
		layoutPane.add(new JScrollPane(layoutTable), BorderLayout.CENTER);
		configTabbedPane.add("Layouts", layoutPane);
		JPanel layoutButtons = new JPanel(new FlowLayout());
		layoutPane.add(layoutButtons, BorderLayout.SOUTH);
		
		layerButtons.add(new JButton(new AbstractAction("Add Layer") {

			@Override
			public void actionPerformed(ActionEvent ae) {
				newLayer();
			}
			
		}));
		layerButtons.add(new JButton(new AbstractAction("Delete selected") {

			@Override
			public void actionPerformed(ActionEvent ae) {
				int rows[] = layerTable.getSelectedRows();
				Arrays.sort(rows);
				for (int i=rows.length-1; i>=0; i--) {
					customViewInfo.getLayers().remove(rows[i]);
				}
				layerTableModel.fireTableDataChanged();
				project.setChanged(true);
			}
			
		}));
		layerButtons.add(new JButton(new AbstractAction("Move selected up") {

			@Override
			public void actionPerformed(ActionEvent ae) {
				int rows[] = layerTable.getSelectedRows();
				Arrays.sort(rows);
				boolean ok = false;
				for (int i=0; i<rows.length; i++) {
					if ((i==0 && rows[i]>0) || (i>0 && rows[i]>rows[i-1]+1))
						ok = true;
					if (!ok)
						continue;
					ViewLayerInfo vli = customViewInfo.getLayers().remove(rows[i]);
					customViewInfo.getLayers().add(rows[i]-1, vli);
				}
				layerTableModel.fireTableDataChanged();
				project.setChanged(true);
			}
			
		}));
		layerButtons.add(new JButton(new AbstractAction("Move selected down") {

			@Override
			public void actionPerformed(ActionEvent ae) {
				int rows[] = layerTable.getSelectedRows();
				Arrays.sort(rows);
				boolean ok = false;
				for (int i=rows.length-1; i>=0; i--) {
					if ((i==rows.length-1 && rows[i]<layerTable.getRowCount()-1) || 
							(i<rows.length-1 && rows[i]<rows[i+1]-1))
						ok = true;
					if (!ok)
						continue;
					ViewLayerInfo vli = customViewInfo.getLayers().remove(rows[i]);
					customViewInfo.getLayers().add(rows[i]+1, vli);
				}
				layerTableModel.fireTableDataChanged();
				project.setChanged(true);
			}
			
		}));
		layoutButtons.add(new JButton(new AbstractAction("Add Layout") {

			@Override
			public void actionPerformed(ActionEvent ae) {
				newLayout();
			}
			
		}));
		buttons.add(new JButton(new AbstractAction("OK") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				configDialog.setVisible(false);
				refresh();
			}
			
		}));
		configDialog.setContentPane(p);
		configDialog.pack();
		configDialog.setLocationRelativeTo(this);
		
		return configDialog;
	}
	
	/** new layer - swing thread */
	void newLayer() {
		String name = JOptionPane.showInputDialog("New Layer name?");
		if (name==null || name.length()==0)
			return;
		if (customViewInfo.getLayer(name)!=null) {
			// already exists
			JOptionPane.showMessageDialog(CustomViewPanel.this, "Layer "+name+" already exists", "Edit Layer", JOptionPane.ERROR_MESSAGE);
			return;
		}
		ViewLayerInfo vli = new ViewLayerInfo();
		vli.setName(name);
		vli.setVisible(true);
		customViewInfo.getLayers().add(vli);
		project.setChanged(true);
		if (layerTableModel!=null)
			layerTableModel.fireTableDataChanged();
	}
	
	static final String LAYER_TABLE_COLUMNS [] = new String[] { "Layer", "Show", "Include", "View type", "Layout" };
	
	/** layer table model */
	protected LayerTableModel layerTableModel;
	
	/** layer table model */
	class LayerTableModel extends AbstractTableModel {

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int column) {
			if (column==1) // show
				return Boolean.class;
			return String.class;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int column) {
			return LAYER_TABLE_COLUMNS[column];
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return true;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
		 */
		@Override
		public void setValueAt(Object value, int row, int column) {
			ViewLayerInfo vli = customViewInfo.getLayers().get(row);
			switch(column) {
			case 0: {
				String name = (String)value;
				if (vli.getName().equals(name))
					// noop
					return;
				if (customViewInfo.getLayer(name)!=null) {
					// already exists
					JOptionPane.showMessageDialog(CustomViewPanel.this, "Layer "+name+" already exists", "Edit Layer", JOptionPane.ERROR_MESSAGE);
					return;
				}
				vli.setName(name);
				project.setChanged(true);
				break;
			}
			case 1: 
				vli.setVisible((Boolean)value);
				project.setChanged(true);
				break;
			case 2: {
				String typeNames [] = value==null ? new String[0] : value.toString().split("[, |]");
				ViewItemSetInfo vlsi = null;
				if (vli.getViewItemSets().size()>0) {
					vlsi = vli.getViewItemSets().get(0);					
				}
				else if (typeNames.length>0){
					vlsi = new ViewItemSetInfo();
					vli.getViewItemSets().add(vlsi);					
				}
				// TODO second& later view item sets
				if (vlsi!=null) {
					List<String> tnl = vlsi.getTypeNames();
					tnl.clear();
					for (int i=0; i<typeNames.length; i++)
						tnl.add(typeNames[i]);
					project.setChanged(true);
				}
				break;
			}
			case 3: {
				ViewItemSetInfo vlsi = null;
				if (vli.getViewItemSets().size()>0) {
					vlsi = vli.getViewItemSets().get(0);					
				}
				else {
					vlsi = new ViewItemSetInfo();
					vli.getViewItemSets().add(vlsi);					
				}
				// TODO second& later view item sets
				if (value==null)
					vlsi.setViewItemType(null);
				else
					vlsi.setViewItemType(value.toString());
				project.setChanged(true);
				break;
			}
			case 4: {
				ViewItemSetInfo vlsi = null;
				if (vli.getViewItemSets().size()>0) {
					vlsi = vli.getViewItemSets().get(0);					
				}
				else {
					vlsi = new ViewItemSetInfo();
					vli.getViewItemSets().add(vlsi);					
				}
				// TODO second& later view item sets
				if (value==null)
					vlsi.setViewLayoutName(null);
				else
					vlsi.setViewLayoutName(value.toString());
				project.setChanged(true);
				break;
			}
			}
		}

		@Override
		public int getColumnCount() {
			return LAYER_TABLE_COLUMNS.length;
		}

		@Override
		public int getRowCount() {
			return customViewInfo.getLayers().size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			ViewLayerInfo vli = customViewInfo.getLayers().get(row);
			switch(column) {
			case 0:
				return vli.getName();
			case 1:
				return vli.isVisible();
			case 2:
			{
				List<ViewItemSetInfo> viss = vli.getViewItemSets();
				if (viss.size()==0)
					return null;
				StringBuilder sb = new StringBuilder();
				// TODO: second and later ViewItemSet(s)
				for (String typeName : viss.get(0).getTypeNames()) {
					if (sb.length()>0)
						sb.append(",");
					sb.append(typeName);
				}
				return sb.toString();
			}
			case 3:
			{
				List<ViewItemSetInfo> viss = vli.getViewItemSets();
				if (viss.size()==0)
					return null;
				// TODO: second and later ViewItemSet(s)
				return viss.get(0).getViewItemType();
			}
			case 4:
			{
				List<ViewItemSetInfo> viss = vli.getViewItemSets();
				if (viss.size()==0)
					return null;
				// TODO: second and later ViewItemSet(s)
				return viss.get(0).getViewLayoutName();
			}
			}
			return null;
		}
		
	}
	protected void newLayout() {
		String name = JOptionPane.showInputDialog("New Layout name?");
		if (name==null || name.length()==0)
			return;
		if (customViewInfo.getLayout(name)!=null) {
			// already exists
			JOptionPane.showMessageDialog(CustomViewPanel.this, "Layout "+name+" already exists", "Edit Layer", JOptionPane.ERROR_MESSAGE);
			return;
		}
		ViewLayoutInfo vli = new ViewLayoutInfo();
		vli.setName(name);
		customViewInfo.getLayouts().add(vli);
		project.setChanged(true);
		if (layoutTableModel!=null)
			layoutTableModel.fireTableDataChanged();
	}
	static final String LAYOUT_TABLE_COLUMNS [] = new String[] { "Layout", "Type", "Parameters" };
	
	/** layout table model */
	protected LayoutTableModel layoutTableModel;
	
	/** layout table model */
	class LayoutTableModel extends AbstractTableModel {

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int column) {
			return String.class;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int column) {
			return LAYOUT_TABLE_COLUMNS[column];
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return true;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
		 */
		@Override
		public void setValueAt(Object value, int row, int column) {
			ViewLayoutInfo vli = customViewInfo.getLayouts().get(row);
			switch(column) {
			case 0: {
				// name
				String name = (String)value;
				if (vli.getName().equals(name))
					// noop
					return;
				if (customViewInfo.getLayout(name)!=null) {
					// already exists
					JOptionPane.showMessageDialog(CustomViewPanel.this, "Layout "+name+" already exists", "Edit Layer", JOptionPane.ERROR_MESSAGE);
					return;
				}
				vli.setName(name);
				project.setChanged(true);
				break;
			}
			case 1: 
				// type
				vli.setLayoutType(value.toString());
				project.setChanged(true);
				// TODO update view?!
				break;
			case 2: 
				// pamameters
				// TODO
				break;
			}
		}

		@Override
		public int getColumnCount() {
			return LAYOUT_TABLE_COLUMNS.length;
		}

		@Override
		public int getRowCount() {
			return customViewInfo.getLayouts().size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			ViewLayoutInfo vli = customViewInfo.getLayouts().get(row);
			switch(column) {
			case 0:
				return vli.getName();
			case 1:
				return vli.getLayoutType();
			case 2:
				return vli.getProperties().toString();
			}
			return null;
		}
		
	}
	
	/** properties bean (delegate/proxy) */
	class PropertiesBean implements java.io.Serializable {
		protected transient CustomViewInfo customViewInfo;
		protected transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
		
		public PropertiesBean() {			
		}
		/**
		 * @param customViewInfo
		 */
		public PropertiesBean(CustomViewInfo customViewInfo) {
			super();
			this.customViewInfo = customViewInfo;
		}

		public String getName() {
			return customViewInfo.getName();
		}

		public void setName(String name) {
			String oldValue = getName();
			customViewInfo.setName(name);
			project.setChanged(true);
			propertyChangeSupport.firePropertyChange("name", oldValue, name);
		}

		public int getWidth() {
			return customViewInfo.getMinimumWidth();
		}

		public void setWidth(int minimumWidth) {
			int oldValue = getWidth();
			customViewInfo.setMinimumWidth(minimumWidth);
			project.setChanged(true);
			propertyChangeSupport.firePropertyChange("width", oldValue, minimumWidth);
		}

		public int getHeight() {
			return customViewInfo.getMinimumHeight();
		}

		public void setHeight(int minimumHeight) {
			int oldValue = getHeight();
			customViewInfo.setMinimumHeight(minimumHeight);
			project.setChanged(true);
			propertyChangeSupport.firePropertyChange("height", oldValue, minimumHeight);
		}
		public void addPropertyChangeListener(PropertyChangeListener arg0) {
			propertyChangeSupport.addPropertyChangeListener(arg0);
		}
		public void addPropertyChangeListener(String arg0,
				PropertyChangeListener arg1) {
			propertyChangeSupport.addPropertyChangeListener(arg0, arg1);
		}
		public void removePropertyChangeListener(PropertyChangeListener arg0) {
			propertyChangeSupport.removePropertyChangeListener(arg0);
		}
		public void removePropertyChangeListener(String arg0,
				PropertyChangeListener arg1) {
			propertyChangeSupport.removePropertyChangeListener(arg0, arg1);
		}
		
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource()==propertiesBean) {
			// ?!
		}
	}
}
