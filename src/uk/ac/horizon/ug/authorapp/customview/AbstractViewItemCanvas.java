package uk.ac.horizon.ug.authorapp.customview;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;

public abstract class AbstractViewItemCanvas extends JComponent {
	private static final String VIEW_ITEM_MIME_TYPE = "application/x-ug-authorapp-view-items";

	static Logger logger = Logger.getLogger(AbstractViewItemCanvas.class.getName());
	
	/** items */
	protected List<List<AbstractViewItem>> viewItems = new LinkedList<List<AbstractViewItem>>();
	boolean useExcluded;

	public AbstractViewItemCanvas(boolean useExcluded) {
		super();
		this.useExcluded = useExcluded;
		this.setTransferHandler(new ViewItemTransferHandler());
	}

	/**
	 * @return the viewItems
	 */
	public List<List<AbstractViewItem>> getViewItems() {
		if (viewItems==null)
			viewItems = new LinkedList<List<AbstractViewItem>>();
		return viewItems;
	}
	/**
	 * @param viewItems the viewItems to set
	 */
	public void setViewItems(List<List<AbstractViewItem>> viewItems) {
		this.viewItems = viewItems;
	}

	public void clearSelection() {
		boolean changed = false;
		for (List<AbstractViewItem> viewItems : getViewItems()) {
			for (AbstractViewItem viewItem : viewItems) {
				if (useExcluded ^ viewItem.isExcludedByLayout())
					continue;
				if (viewItem.isSelected()) {
					viewItem.setSelected(false);
					changed = true;
				}
			}
		}
		if (changed)
			repaint();
	}
	/** placeholder */
	public float getZoomRatio() {
		return 1.0f;
	}

	public AbstractViewItem getViewItemAt(int x, int y) {
		// search in reverse order (i.e. visible order) 
		if (viewItems==null)
			return null;
		double sx = x/getZoomRatio();
		double sy = y/getZoomRatio();
		for (int li=viewItems.size()-1; li>=0; li--) {
			List<AbstractViewItem> vis = viewItems.get(li);
			for (int ii=vis.size()-1; ii>=0; ii--) {
				AbstractViewItem vi = vis.get(ii);
				Rectangle visibleExtent = vi.getVisibleExtent();
				if (visibleExtent.contains(sx-vi.getX(), sy-vi.getY()))
					return vi;
			}
		}
		return null;
	}
	/** hack to pass to Transferable */
	protected Point lastFromPoint;
	/** handle mouse events on this canvas */
	class MouseEventHandler implements MouseListener, MouseMotionListener {
		boolean pressedInSelected = false;
		boolean dragInProgress = false;
		protected boolean moveInProgress = false;
		boolean exited = false;
		int pressX, pressY, lastX, lastY;
		
		@Override
		public void mouseClicked(MouseEvent ev) {
			// TODO Auto-generated method stub
			// select?
			if (!ev.isShiftDown() && !ev.isControlDown())
				clearSelection();
			AbstractViewItem vi = getViewItemAt(ev.getX(), ev.getY());
			if (vi==null)
				return;
			if (vi.isSelected())
				return;
			vi.setSelected(true);
			logger.info("Selected "+vi);
			repaint();			
		}

		@Override
		public void mouseEntered(MouseEvent ev) {
			exited = false;
		}

		@Override
		public void mouseExited(MouseEvent ev) {
			exited = true;
		}

		@Override
		public void mousePressed(MouseEvent ev) {
			if (ev.getButton()==MouseEvent.BUTTON1) {
				AbstractViewItem vi = getViewItemAt(ev.getX(), ev.getY());
				if (vi!=null && vi.isSelected())
					pressedInSelected = true;
				else
					pressedInSelected = false;
			}
			else if (ev.getButton()!=MouseEvent.BUTTON1) {
  				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  				moveInProgress = true;	
  				lastX = ev.getXOnScreen();
  				lastY = ev.getYOnScreen();
			}
			dragInProgress = false;
			exited = false;
			pressX = ev.getX();
			pressY = ev.getY();
			lastFromPoint = new Point(ev.getX(), ev.getY());
		}

		@Override
		public void mouseReleased(MouseEvent ev) {
			// TODO Auto-generated method stub
			if (moveInProgress) {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				Rectangle visible = getVisibleRect();
				Rectangle scrollTo = new Rectangle(visible.x-(ev.getXOnScreen()-lastX), visible.y-(ev.getYOnScreen()-lastY), visible.width, visible.height);
				scrollRectToVisible(scrollTo);
				moveInProgress = false;
			}
			else if (dragInProgress) {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}

		@Override
		public void mouseDragged(MouseEvent ev) {
			// TODO Auto-generated method stub
			if (moveInProgress) {
				Rectangle visible = getVisibleRect();
				Rectangle scrollTo = new Rectangle(visible.x-(ev.getXOnScreen()-lastX), visible.y-(ev.getYOnScreen()-lastY), visible.width, visible.height);
				scrollRectToVisible(scrollTo);
				lastX = ev.getXOnScreen();
				lastY = ev.getYOnScreen();
			}
			else if (!dragInProgress) {
				dragInProgress = true;
				if (pressedInSelected) {
					getTransferHandler().exportAsDrag(AbstractViewItemCanvas.this, ev, TransferHandler.MOVE);
					//setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				}
				else if (ev.getButton()==MouseEvent.BUTTON1)
					setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			}
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	class ViewItemTransferHandler extends TransferHandler {

		/* (non-Javadoc)
		 * @see javax.swing.TransferHandler#createTransferable(javax.swing.JComponent)
		 */
		@Override
		protected Transferable createTransferable(JComponent c) {
			logger.info("CreateTransferable!");
			if (c instanceof AbstractViewItemCanvas) {
				List<AbstractViewItem> items = ((AbstractViewItemCanvas)c).getSelectedViewItems();
				Transferable t = new ViewItemTransferable(new ViewItems(items, lastFromPoint, (AbstractViewItemCanvas)c));
				return t;
			}
			logger.log(Level.WARNING, "createTransferable called for non-ViewCanvas "+c);
			return null;
		}

		/* (non-Javadoc)
		 * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
		 */
		@Override
		public int getSourceActions(JComponent c) {
			return MOVE | COPY;
		}
		/* (non-Javadoc)
		 * @see javax.swing.TransferHandler#exportAsDrag(javax.swing.JComponent, java.awt.event.InputEvent, int)
		 */
		@Override
		public void exportAsDrag(JComponent comp, InputEvent e, int action) {
			logger.info("exportAsDrag("+e+","+action+")");
			// TODO Auto-generated method stub
			super.exportAsDrag(comp, e, action);
		}

		/* (non-Javadoc)
		 * @see javax.swing.TransferHandler#canImport(javax.swing.TransferHandler.TransferSupport)
		 */
		@Override
		public boolean canImport(TransferSupport support) {
			if (support.getDropAction()!=TransferHandler.MOVE) {
				logger.info("Cannot import a non-MOVE action: "+support.getDropAction());
				return false;
			}
			DataFlavor flavors[] = support.getDataFlavors();
			for (int i=0; i<flavors.length; i++)
				if (flavors[i].equals(viewItemDataFlavor)) {
					try {
						ViewItems items = (ViewItems)support.getTransferable().getTransferData(viewItemDataFlavor);
						if (items!=null && getViewItems()!=items.getCanvas().getViewItems()) {
							logger.info("Cannot drag onto Canvas with different item list");
							return false;
						}
						return true;
					}
					catch (Exception e) {
						logger.log(Level.WARNING, "Problem on canImport", e);
					}
				}
			return false;
		}

		/* (non-Javadoc)
		 * @see javax.swing.TransferHandler#exportDone(javax.swing.JComponent, java.awt.datatransfer.Transferable, int)
		 */
		@Override
		protected void exportDone(JComponent source, Transferable data,
				int action) {
			
			logger.info("ExportDone "+action);
		}

		/* (non-Javadoc)
		 * @see javax.swing.TransferHandler#importData(javax.swing.TransferHandler.TransferSupport)
		 */
		@Override
		public boolean importData(TransferSupport support) {
			if (!canImport(support))
				return false;
			ViewItems items;
			try {
				items = (ViewItems)support.getTransferable().getTransferData(viewItemDataFlavor);
				if (items==null) {
					logger.log(Level.WARNING, "Null data from drop");
					return false;				
				}
				DropLocation location = support.getDropLocation();
				Point at = location.getDropPoint();
				logger.info("import "+items.getViewItems().size()+" items at "+at);
				AbstractViewItemCanvas canvas = (AbstractViewItemCanvas)support.getComponent();
				canvas.handleImport(items.getViewItems(), at, items.getCanvas(), items.getFromPoint());
				return true;
				
			} catch (Exception e) {
				logger.log(Level.WARNING, "Error in drop", e);
				return false;
			}
		}
	}
	static class ViewItems {
		protected transient List<AbstractViewItem> viewItems;
		protected transient AbstractViewItemCanvas canvas;
		protected Point fromPoint;
		/**
		 * @param viewItems
		 * @param lastFromPoint 
		 * @param canvas
		 */
		public ViewItems(List<AbstractViewItem> viewItems,
				Point lastFromPoint, AbstractViewItemCanvas canvas) {
			super();
			this.viewItems = viewItems;
			this.canvas = canvas;
			this.fromPoint = lastFromPoint;
		}
		/**
		 * @return the viewItems
		 */
		public List<AbstractViewItem> getViewItems() {
			return viewItems;
		}
		/**
		 * @return the canvas
		 */
		public AbstractViewItemCanvas getCanvas() {
			return canvas;
		}
		/**
		 * @return the fromPoint
		 */
		public Point getFromPoint() {
			return fromPoint;
		}
		/**
		 * @param fromPoint the fromPoint to set
		 */
		public void setFromPoint(Point fromPoint) {
			this.fromPoint = fromPoint;
		}
		
		
	}
	static DataFlavor viewItemDataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, ViewItems.class.getName());
	static class ViewItemTransferable implements Transferable {
		protected ViewItems viewItems;
		/**
		 * @param viewItem
		 * @param canvas
		 */
		public ViewItemTransferable(ViewItems viewItems) {
			super();
			this.viewItems = viewItems;
		}

		@Override
		public Object getTransferData(DataFlavor df)
				throws UnsupportedFlavorException, IOException {
			if (!df.equals(viewItemDataFlavor))
				throw new UnsupportedFlavorException(df);
			return viewItems;
		}
		static DataFlavor dataFlavors[] = new DataFlavor[] {  viewItemDataFlavor };

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return dataFlavors;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor df) {
			return df.equals(viewItemDataFlavor);
		}
		
	}
	public List<AbstractViewItem> getSelectedViewItems() {
		List<AbstractViewItem> items = new LinkedList<AbstractViewItem>();
		for (List<AbstractViewItem> viewItems : getViewItems()) {
			for (AbstractViewItem viewItem : viewItems) {
				if (useExcluded ^ viewItem.isExcludedByLayout())
					continue;
				if (viewItem.isSelected()) {
					items.add(viewItem);
				}
			}
		}
		return items;
	}

	public void handleImport(List<AbstractViewItem> viewItems2,
			Point at, AbstractViewItemCanvas fromCanvas, Point fromPoint) {
		if ((this!=fromCanvas)) {
			clearSelection();
		}
		for (AbstractViewItem viewItem : viewItems2) {
			if (useExcluded ^ viewItem.isExcludedByLayout()) {
				if ((this==fromCanvas)) {
					logger.log(Level.WARNING, "Include/exclude but same canvas");
					continue;
				}
				viewItem.setExcludedByLayout(useExcluded);
				// include/exclude...
				AbstractViewLayout vl = viewItem.getViewLayout();
				if (vl==null) {
					logger.log(Level.WARNING, "No ViewLayout for include/excluded item "+viewItem);
					continue;											
				}
				if (fromCanvas instanceof ViewCanvas)
					vl.handleItemDragOff((ViewCanvas)fromCanvas, viewItem);
				if (this instanceof ViewCanvas)
					vl.handleItemDragOn((ViewCanvas)this, viewItem, (int)(at.getX()+(viewItem.getX()-fromPoint.getX())), (int)(at.getY()+(viewItem.getY()-fromPoint.getY())));
			}
			else {
				if (!(this==fromCanvas)) {
					logger.log(Level.WARNING, "Move but different canvases ("+this+" vs "+fromCanvas);
					continue;
				}
				//logger.log(Level.WARNING, "include already included item "+viewItem);
				if (at.getX()>=getWidth() || at.getY()>=getHeight() || at.getX()<0 || at.getY()<0) {
					logger.info("Released after move outside canvas: "+at);							
				}
				else {
					// move all selected...
					boolean changed = false;
					int dx = (int)(at.getX()-fromPoint.getX());
					int dy = (int)(at.getY()-fromPoint.getY());
					AbstractViewLayout vl = viewItem.getViewLayout();
					if (vl==null) {
						logger.log(Level.WARNING, "No ViewLayout for moved item "+viewItem);
						continue;											
					}
					if (AbstractViewItemCanvas.this instanceof ViewCanvas) 
						vl.handleItemMove((ViewCanvas)AbstractViewItemCanvas.this, viewItem, dx, dy);
				}
			}			
		}
		updateAfterDrag();
		fromCanvas.updateAfterDrag();
	}

	protected void updateAfterDrag() {
		// TODO Auto-generated method stub
		repaint();		
	}
	}