/**
 * 
 */
package uk.ac.horizon.ug.authorapp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;

import uk.ac.horizon.ug.authorapp.customview.AbstractViewItem;
import uk.ac.horizon.ug.authorapp.customview.DefaultViewItem;
import uk.ac.horizon.ug.authorapp.customview.ViewCanvas;
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
public class CustomViewPanel extends JPanel {
	/** project */
	protected Project project;
	/** custom view info */
	protected CustomViewInfo customViewInfo;
	/** view canvas */
	protected ViewCanvas viewCanvas;
	/** cons 
	 * @param cvi */
	public CustomViewPanel(Project project, CustomViewInfo cvi) {
		super(new BorderLayout());
		this.customViewInfo = cvi;
		this.project = project;
		
		viewCanvas = new ViewCanvas();
		JScrollPane viewScrollPane = new JScrollPane(viewCanvas);
		viewScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		viewScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(viewScrollPane, BorderLayout.CENTER);
		JPanel buttons = new JPanel (new FlowLayout());
		add(buttons, BorderLayout.SOUTH);
		buttons.add(new JButton(new AbstractAction("Zoom in") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				viewCanvas.setZoomRatio(viewCanvas.getZoomRatio()*2);
			}
			
		}));
		buttons.add(new JButton(new AbstractAction("Zoom out") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				viewCanvas.setZoomRatio(viewCanvas.getZoomRatio()/2);
				if (viewCanvas.getZoomRatio()>0.55 && viewCanvas.getZoomRatio()<1.9) 
					viewCanvas.setZoomRatio(1);
				
			}
			
		}));
		
		refresh();
	}
	/** regenerate view */
	protected void refresh() {
		// TODO for real
		DefaultViewItem item = new DefaultViewItem();
		item.setTextRows(new String[] { "Test" });
		item.setBorderWidth(1);
		item.setWidth(20);
		item.setHeight(10);
		List<AbstractViewItem> items = new LinkedList<AbstractViewItem>();
		items.add(item);
		viewCanvas.getViewItems().add(items);
		viewCanvas.repaint();
	} 
	/** config dialog */
	protected JDialog configDialog;
	/** config tabbed pane */
	protected JTabbedPane configTabbedPane;
	/** config layer table */
	protected Component layerPane;
	/** config layout table */
	protected Component layoutPane;
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
		
		
		
		this.layerTableModel = new LayerTableModel();
		JTable layerTable = new JTable(layerTableModel);
		layerPane = new JScrollPane(layerTable);
		configTabbedPane.add("Layers", layerPane);
		
		layoutTableModel = new LayoutTableModel();
		JTable layoutTable =new JTable(layoutTableModel);
		layoutPane = new JScrollPane(layoutTable);
		configTabbedPane.add("Layouts", layoutPane);
		
		buttons.add(new JButton(new AbstractAction("Add Layer") {

			@Override
			public void actionPerformed(ActionEvent ae) {
				newLayer();
			}
			
		}));
		buttons.add(new JButton(new AbstractAction("Add Layout") {

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
		if (layerTableModel!=null)
			layerTableModel.fireTableDataChanged();
	}
	
	static final String LAYER_TABLE_COLUMNS [] = new String[] { "Layer", "Show", "Include", "Layout" };
	
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
}
